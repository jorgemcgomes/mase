processRepo <- function(setup, jobs=0:4, nvars=7, red=NULL, k=3, bc="sdbc", srcdir="/home/jorge/Dropbox/mase/src/mase/app/playground/rep/", builddir="/home/jorge/Dropbox/mase/build/classes/mase/app/playground/rep/") {
  for(j in jobs) {
    cat(setup, j, "\n")
    file.copy(from=paste0(setup,"/job.",j,".finalarchive.tar.gz"), to=paste0(srcdir,"sdbc_",setup,"_",j,".tar.gz"), overwrite=F)
    file.copy(from=paste0(setup,"/job.",j,".finalarchive.tar.gz"), to=paste0(builddir,"sdbc_",setup,"_",j,".tar.gz"), overwrite=F)
    if(!is.null(red)) {
      d <- fread(paste0(setup,"/job.",j,".archive.stat"))
      vars <- paste0("Behav_",0:(nvars-1))
      r <- reduceData(d, vars, method=red, k=k)
      write.table(r[,c("Index",paste0("V",1:k)), with=F], file=paste0(srcdir,bc,"_",setup,"_",j,"_",red,k,".txt"), row.names=F, col.names=F, sep=" ")
      write.table(r[,c("Index",paste0("V",1:k)), with=F], file=paste0(builddir,bc,"_",setup,"_",j,"_",red,k,".txt"), row.names=F, col.names=F, sep=" ")
    }
  }
}

setwd("~/exps/playground/rep10")
processRepo("nsneat", jobs=9)
processRepo("rand", jobs=0:9)

shrinkRepo <- function(setup, jobs=0:4, nvars=7, size=NULL, bc="sdbc", srcdir="/home/jorge/Dropbox/mase/src/mase/app/playground/rep/", builddir="/home/jorge/Dropbox/mase/build/classes/mase/app/playground/rep/") {
  for(j in jobs) {
    cat(setup, j, "\n")
    file.copy(from=paste0(setup,"/job.",j,".finalarchive.tar.gz"), to=paste0(srcdir,"sdbc_",setup,"_",j,".tar.gz"), overwrite=F)
    file.copy(from=paste0(setup,"/job.",j,".finalarchive.tar.gz"), to=paste0(builddir,"sdbc_",setup,"_",j,".tar.gz"), overwrite=F)
    if(!is.null(size)) {
      d <- fread(paste0(setup,"/job.",j,".archive.stat"))
      vars <- paste0("Behav_",0:(nvars-1))
      cl <- pam(d[,vars,with=F], k=size, do.swap=F)
      d <- d[cl$id.med]
      write.table(d[,c("Index",vars), with=F], file=paste0(srcdir,bc,"_",setup,"_",j,"_pam",size,".txt"), row.names=F, col.names=F, sep=" ")
      write.table(d[,c("Index",vars), with=F], file=paste0(builddir,bc,"_",setup,"_",j,"_pam",size,".txt"), row.names=F, col.names=F, sep=" ")
    }
  }
}

shrinkRepo("nsneat", jobs=0, size=50)
shrinkRepo("nsneat", jobs=0, size=100)
shrinkRepo("nsneat", jobs=0, size=500)
shrinkRepo("nsneat", jobs=0, size=1000)
shrinkRepo("nsneat", jobs=0, size=2500)

processRepo("nsneat", jobs=0, red="mds", k=1)
processRepo("nsneat", jobs=0, red="mds", k=2)
processRepo("nsneat", jobs=0, red="mds", k=3)
processRepo("nsneat", jobs=0, red="mds", k=4)
processRepo("nsneat", jobs=0, red="mds", k=5)



qdscore <- function(data, vars, d=0.5) {
  datacopy <- copy(data)
  bins <- paste0("bin",0:(length(vars)-1))
  datacopy[ , (bins) := lapply(.SD, function(x){round(x/d)}), .SDcols=vars]
  maxes <- datacopy[, .(Fitness=max(Fitness)), by=.(bin0,bin1,bin2,bin3,bin4,bin5,bin6)]
  qdscore <- sum(maxes$Fitness)
  return(list(QDscore=qdscore, N=nrow(maxes), MeanFitness=mean(maxes$Fitness)))
}


# DATA LOAD ######################

setwd("~/exps/playground")

fit <- loadData("tasks10/*", "postfitness.stat", fun=loadFitness, auto.ids.sep="_", auto.ids.names=c("Domain","Task","BC","Repo","RepoJob","Reduction"))
#fit[, Reference := mean(.SD[is.na(Repo) & Generation==max(Generation)]$BestSoFar), by=.(Task)]
#fit[, RelativeFitness := ((BestSoFar-Reference) / Reference) * 100]
fit[, ScaledFitness := (BestSoFar - min(BestSoFar)) / (max(BestSoFar)-min(BestSoFar)) , by=.(Task)]
fit[, Task := factor(Task, levels=c("freeforaging","obsforaging","dynforaging","simplephototaxis","phototaxis","exploration","maze","avoidance","predator","dynphototaxis"), labels=c("Foraging","Foraging-O","Foraging-D","Phototaxis","Phototaxis-O","Exploration","Maze","Avoidance","Prey","Tracking"))]
fit <- fit[Task != "Foraging-D"]
fit[Reduction=="direct", Reduction := NA]
fit[, Conf := paste(BC,Repo,Reduction, sep="-")]

vars <- paste0("Behav_", 0:6)
options(scipen = 99)

load("reducedrep.rdata")
rep[, grep("Genome_*", colnames(rep)) := NULL]
gc()


# Dimensionality reduction ###############

rep <- loadData("rep10/*", "archive.stat", auto.ids.names=c("NA","Repo"))
rep <- reduceData(rep, vars=vars, method="Rtsne", k=2)
save(rep, file="reducedrep.rdata")


red <- reduceData(rep[Repo%in%c("nsneat","rand")], vars=vars, method="rpca")
plotReduced2D(red) + facet_grid(Repo ~ Job)



# Correlation of tasks ###############

means <- fit[, .(MeanHighest=mean(ScaledFitness)), by=.(Conf,RepoJob,Task)]
means[, EvoSetup := factor(paste(Conf,RepoJob,sep="-"))]
setorder(means, Conf, RepoJob, Task)

c <- dcast(means, EvoSetup ~ Task, value.var="MeanHighest")
co <- round(cor(c[,-1,with=F], method="spearman"), digits=3)
co[co > 0.8]


# Base comparison ##################

subfit <- fit[Repo %in% c("nsneat","rand",NA) & is.na(Reduction)]
subfit[, Repo := factor(Repo, levels=c("nsneat","rand"), labels=c("EvoRBC","EvoRBC(R)"))]
subfit[is.na(Repo), Repo := "TR"]

sum <- lastGen(subfit)[, .(Fitness=mean(ScaledFitness), SE=se(ScaledFitness)), by=.(Task, Repo)]
sum <- rbind(sum, sum[, .(Task="Average",Fitness=mean(Fitness),SE=se(Fitness)), by=.(Repo)])

ggplot(sum, aes(Task, Fitness,fill=Repo)) + geom_bar(stat="identity", position="dodge") + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.25, position=position_dodge(.9)) +
  scale_y_continuous(limits=c(0.4,1),oob=rescale_none) + labs(y="Scaled fitness",fill="Method")

ggplot(lastGen(subfit), aes(Task,ScaledFitness,colour=Repo)) + geom_boxplot(outlier.size=1) + 
  geom_vline(xintercept=seq(1.5,9.5,by=1), size=.25) + coord_flip()

m <- metaAnalysis(lastGen(subfit), ScaledFitness ~ Repo, ~ Task)
sapply(m, function(x) x$ttest$holm[1,2])
sapply(m, function(x) x$ttest$holm[3,2])
metaAnalysis(lastGen(subfit)[,.(MeanFitness=mean(ScaledFitness)), by=.(Task,Repo)], MeanFitness ~ Repo, paired=T)

genmean <- subfit[Repo!="EvoRBC(R)", .(Fitness=mean(ScaledFitness), SE=se(ScaledFitness)), by=.(Generation,Task,Repo)]
ggplot(genmean, aes(Generation, Fitness, group=Repo)) + geom_ribbon(aes(ymin=Fitness-SE,ymax=Fitness+SE,fill=Repo), alpha=.15) +
  geom_line(aes(colour=Repo)) + facet_wrap(~ Task, scales="free_y", ncol=3) + labs(y="Scaled Fitness", colour="Method", fill="Method")


# Repo variability (base) ################

bred <- reduceData(unique(rep[Repo=="nsneat"],by=vars), vars=vars, method="Rtsne")
ggplot(bred, aes(x=V1, y=V2)) + geom_point(shape=16, size=1.5, alpha=.2) + facet_wrap(~ Job) +
  coord_fixed() + theme(axis.text.x = element_blank(), axis.text.y = element_blank(), axis.ticks=element_blank()) + labs(x=NULL, y=NULL) +
  ggtitle("Behaviour space coverage (t-SNE)")

ggplot(bred, aes(x=V1, y=V2)) + geom_point(shape=16, size=1) + 
  facet_wrap(~ Job) + coord_fixed() + 
  theme(axis.text.x = element_blank(), axis.text.y = element_blank(), axis.ticks=element_blank()) + labs(x=NULL, y=NULL) +
  ggtitle("Behaviour space coverage (t-SNE)")


#gvars <- paste0("Genome_",0:101)
#gred <- reduceData(unique(sub,by=gvars), vars=gvars, method="Rtsne")
#ggplot(gred, aes(x=V1, y=V2)) + geom_point(aes(colour=Job), shape=16, size=1.5, alpha=.2) + 
#   coord_fixed() + theme(axis.text.x = element_blank(), axis.text.y = element_blank(), axis.ticks=element_blank()) + labs(x=NULL, y=NULL) +
#   ggtitle("Genome space coverage (t-SNE)")

# identify which primitives belong to a bin that has not been visited by the other repo under comparison, and vice versa (colorize)
# TODO: use distance to nearest neighbour instead?
bred[, Bin := do.call(paste, lapply(.SD/.5, round)), by=.(Job,Index), .SDcols=vars]
visited <- unique(bred, by=c("Job","Bin")) # remove repeated
ubiquity <- visited[, .(Ubiquity=.N), by=.(Bin)]
bred <- merge(bred, ubiquity)
ggplot(bred, aes(x=V1, y=V2, colour=factor(Ubiquity))) + geom_point(shape=16, size=1) + facet_wrap(~ Job) +
  coord_fixed() + theme(axis.text.x = element_blank(), axis.text.y = element_blank(), axis.ticks=element_blank()) + labs(x=NULL, y=NULL) +
  ggtitle("Behaviour space coverage (t-SNE)") + scale_color_brewer(palette="Spectral")


subfit <- fit[Repo=="nsneat" & is.na(Reduction)]

ggplot(lastGen(subfit), aes(Task,ScaledFitness,colour=RepoJob)) + geom_boxplot(outlier.size=1) + 
  geom_vline(xintercept=seq(1.5,9.5,by=1), size=.25) + coord_flip() +
  ggtitle("Fitness achieved in each task, using different repertoires (10 runs per rep.)")

m <- metaAnalysis(lastGen(subfit), ScaledFitness ~ RepoJob, ~Task)
sapply(m, function(x) x$ttest$kruskal$p.value)

metaAnalysis(lastGen(subfit)[,.(MeanFitness=mean(ScaledFitness)), by=.(Task,RepoJob)], MeanFitness ~ RepoJob, paired=T)



# Primitive complexity ###########

subfit <- fit[is.na(Reduction) & Repo %in% c("ns","nsneat","nsmlp5","nsslp","nsmlp1010") & Arbitrator=="NEAT"]
subfit[, Repo := factor(Repo, levels=c("nsslp","nsmlp5","ns","nsmlp1010","nsneat"), labels=c("SLP","MLP-5","MLP-10","MLP-10-10","NEAT"))]

sum <- lastGen(subfit)[, .(Fitness=mean(ScaledFitness), SE=se(ScaledFitness)), by=.(Task, Repo)]
sum <- rbind(sum, sum[, .(Task="Average",Fitness=mean(Fitness),SE=se(Fitness)), by=.(Repo)])

ggplot(sum, aes(Task, Fitness, fill=Repo)) + geom_bar(stat="identity", position="dodge") + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.25, position=position_dodge(.9)) + ylab("Scaled fitness") +
  ggtitle("Fitness achieved in each task, using different repertoires (each setup 5 repertoires X 10 runs)") +
  scale_y_continuous(limits=c(0.65,1),oob=rescale_none)

metaAnalysis(lastGen(subfit)[!is.na(Repo),.(MeanFitness=mean(ScaledFitness)), by=.(Task,Repo)], MeanFitness ~ Repo, paired=T)

m <- metaAnalysis(lastGen(subfit)[!is.na(Repo)], ScaledFitness ~ Repo,  ~Task)
sapply(m, function(x) x$ttest$kruskal$p.value)

bests <- function(x) {
  b <- as.character(x$summary$Repo[which.max(x$summary$ScaledFitness.mean)])
  b2 <- names(which(x$ttest$holm[b,] > 0.05))
  return(c(b,b2))
}
lapply(m, bests)


subrep <- rep[Repo %in% c("nsslp","nsmlp5","ns","nsmlp1010","nsneat"), c("Index","Repo","Job",vars), with=F]
subrep[, Repo := factor(Repo, levels=c("nsslp","nsmlp5","ns","nsmlp1010","nsneat"), labels=c("SLP","MLP-5","MLP-10","MLP-10-10","NEAT"))]

red <- reduceData(subrep[Job==0], vars=vars, method="Rtsne", k=2)

ggplot(red, aes(x=V1, y=V2)) + geom_point(shape=16, size=1) + facet_wrap(~ Repo) + coord_fixed() + 
  theme(axis.text.x = element_blank(), axis.text.y = element_blank(), axis.ticks=element_blank()) + labs(x=NULL, y=NULL) +
  geom_density2d() +
  ggtitle("Behaviour space coverage (t-SNE) [Only one arbitrary run for each is shown]")


plotReduced2D(red, color.var="Repo") + facet_wrap(~ Repo) + ggtitle("Behaviour space, one job (t-SNE)")
ggplot(red, aes(V1,V2)) + stat_density_2d(geom="polygon", aes(fill=..level..)) + facet_wrap(~ Repo) + 
  xlim(-0.1,1.1) + ylim(-0.1,1.1) + scale_fill_distiller(palette="Spectral") + ggtitle("Behaviour space, all jobs (t-SNE)")

#scores <- subrep[, qdscore(.SD, vars=vars, d=0.5), by=.(Repo,Job)]
#ggplot(scores[, .(Mean=mean(N), SE=se(N)), by=.(Repo)], aes(Repo, Mean)) + geom_bar(stat="identity") + 
#  geom_errorbar(aes(ymin=Mean-SE,ymax=Mean+SE), width=.5) + ylab("Number of regions reached")
#metaAnalysis(scores, N ~ Repo)


# identify which primitives belong to a bin that has not been visited by the other repo under comparison, and vice versa (colorize)
# TODO: look up how it was done before, ubiquity
# red[ , Bin := do.call(paste, lapply(.SD/.5, round)), by=.(Repo,Job,Index), .SDcols=vars]
# r1 <- red[Repo=="MLP-10" & Job==0]
# r2 <- red[Repo=="SLP" & Job==0]
# r1[, Common := Bin %in% r2$Bin]
# r2[, Common := Bin %in% r1$Bin]
# plotReduced2D(rbind(r1,r2), color.var="Common") + facet_wrap(~ Repo)


# Arbitrator complexity ################

subfit <- fit[is.na(Reduction) & Repo=="ns" & RepoJob==3]

sum <- lastGen(subfit)[, .(Fitness=mean(ScaledFitness), SE=se(ScaledFitness)), by=.(Task, Arbitrator)]
sum <- rbind(sum, sum[, .(Task="Average",Fitness=mean(Fitness),SE=se(Fitness)), by=.(Arbitrator)])

ggplot(sum, aes(Task, Fitness, fill=Arbitrator)) + geom_bar(stat="identity", position="dodge") + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.25, position=position_dodge(.9)) + ylab("Scaled fitness") +
  ggtitle("Fitness achieved in each task, using different repertoires (each setup 5 repertoires X 10 runs)") +
  scale_y_continuous(limits=c(0.7,1),oob=rescale_none)

metaAnalysis(lastGen(subfit)[!is.na(Repo),.(MeanFitness=mean(ScaledFitness)), by=.(Task,Arbitrator)], MeanFitness ~ Arbitrator, paired=T)



# Environment #####################

subfit <- fit[is.na(Reduction) & Repo %in% c("ns","nsfew","nsnoobj","nsnoobs",NA)]
subfit[, Repo := factor(Repo, levels=c("ns","nsfew","nsnoobj","nsnoobs"), labels=c("Base","Few","No-Objects","No-Obstacles"))]
sum <- lastGen(subfit)[, .(Fitness=mean(ScaledFitness), SE=se(ScaledFitness)), by=.(Task, Repo)]
sum <- rbind(sum, sum[, .(Task="Average",Fitness=mean(Fitness),SE=se(Fitness)), by=.(Repo)])

ggplot(sum, aes(Task, Fitness, fill=Repo)) + geom_bar(stat="identity", position="dodge") + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.25, position=position_dodge(.9)) +
  scale_y_continuous(limits=c(0.7,1),oob=rescale_none) +
  ggtitle("Fitness achieved in each task, using different repo setups (each setup 5 repertoires X 10 runs)")

m <- metaAnalysis(lastGen(subfit)[!is.na(Repo)], ScaledFitness ~ Repo,  ~Task)
sapply(m, function(x) x$ttest$kruskal$p.value)
sapply(m, function(x) x$ttest$holm[1,2])

metaAnalysis(lastGen(subfit)[!is.na(Repo),.(MeanFitness=mean(ScaledFitness)), by=.(Task,Repo)], MeanFitness ~ Repo, paired=T)
metaAnalysis(lastGen(subfit)[,.(MeanFitness=mean(ScaledFitness)), by=.(Task,Repo)], MeanFitness ~ Repo, paired=T)



# Usage of the repertoire ################

r <- reduceData(rep[Repo=="ns" & Job==3], vars, method="Rtsne", k=2)

act <- loadData("tasks10/pl_*_sdbc_ns_3_direct", "postbest.xml.stat", auto.ids.sep="_", auto.ids.names=c("Domain","Task","BC","Repo","RepoJob","Reduction"))
act[, Task := factor(Task, levels=c("freeforaging","obsforaging","dynforaging","simplephototaxis","phototaxis","dynphototaxis","exploration","maze","avoidance","predator"), labels=c("Foraging","Foraging-O","Foraging-D","Phototaxis","Phototaxis-O","Phototaxis-D","Exploration","Maze","Avoidance","Prey"))]
act <- merge(act, r[, .(Index,V1,V2)], by.x="Primitive", by.y="Index")

#https://stackoverflow.com/questions/21193138/transparency-and-alpha-levels-for-ggplot2-stat-density2d-with-maps-and-layers-in
ggplot(act, aes(V1,V2,colour=Job)) + coord_fixed() +
  geom_point(data=r, colour="lightgray", shape=16, size=1) +
  geom_density2d(h=0.2,bins=5) + facet_wrap(~ Task, ncol=3) +
  theme(axis.text.x = element_blank(), axis.text.y = element_blank(), axis.ticks=element_blank()) + labs(x=NULL, y=NULL) +
  ggtitle("Primitives chosen by the best solutions of each run, for each task")

ggplot(act, aes(V1,V2)) + coord_fixed() +
  geom_point(data=r, colour="lightgray", shape=16, size=1) +
  stat_density2d(geom="polygon", h=0.2,bins=10, aes(fill=..level..), alpha=.6) + facet_wrap(~ Task, ncol=3) +
  scale_fill_distiller(palette="Spectral") + xlim(c(-0.05,1.05)) + ylim(c(-0.05,1.05)) +
  theme(axis.text.x = element_blank(), axis.text.y = element_blank(), axis.ticks=element_blank()) + labs(x=NULL, y=NULL, fill="Density") +
  ggtitle("Primitives chosen by the best solutions of all runs, for each task")  


# Plot region codes ############

# switch to cluster::clara function if needed --faster
require(cluster)
cluster <- pam(r[,.(V1,V2)], k=40, pamonce=2)
centers <- r[cluster$id.med, c("Index","V1","V2",vars), with=F]

m <- melt(centers, measure.vars=vars)
m[, variable := factor(variable, labels=c("Walls-Dist","Obstacle-Mean-Dist","Obstacle-Closest-Dist","Object-Mean-Dist","Object-Closest-Dist","Linear-Speed","Turn-Speed"))]
m[, angle := ((as.numeric(variable) - 1) / length(levels(variable))) * -2 * pi + pi/2]
m[, rad := (value - min(value)) / (max(value)-min(value)) , by=.(variable)] # normalize each var to [0,1]

circleFun <- function(center = c(0,0), r=1, npoints = 100){
  tt <- seq(0,2*pi,length.out = npoints)
  return(data.frame(x = center[1] + r * cos(tt), y = center[2] + r * sin(tt)))
}
circlesDF <- function(cdf, r=0.05) {cbind(rbindlist(apply(cdf[,.(V1,V2)], 1, circleFun, r=r)), Index=rep(cdf$Index, each=100))}

ggplot(m, aes(V1, V2)) + 
  geom_point(data=r, colour="lightgray", shape=16, size=1) +
  geom_path(data=circlesDF(centers, r=0.05), aes(x,y,group=Index), size=.25) +
  geom_path(data=circlesDF(centers, r=0.033), aes(x,y,group=Index), size=.25) +
  geom_path(data=circlesDF(centers, r=0.0165), aes(x,y,group=Index), size=.25) +
  geom_spoke(aes(angle = angle, radius=0.05*rad, colour=variable), size=1.5, lineend="butt") +
  geom_point(shape=20, size=1, colour="white") + coord_fixed()



# Transitions ############

r <- rep[Repo=="ns" & Job==3]
a <- merge(act, r[,c("Index",vars),with=F], by.x="Primitive", by.y="Index")
setorder(a, Task, Job, Seed, Time)

diff <- function(df) {
  cat(".")
  return(c(NA, parSapply(cl=NULL, 2:nrow(df), function(i){euclideanDist(df[i], df[i-1])})))
}
createCluster() ; clusterExport(NULL, "euclideanDist")
a[, Jump := diff(.SD), by=.(Seed,Job,Task), .SDcols=vars]

dists <- Dist(unique(r[,vars,with=F]), diag=F, upper=T)
dists <- as.matrix(dists)
diag(dists) <- NA
closestD <- mean(apply(dists, 1, min, na.rm=T))
allD <- mean(apply(dists, 1, mean, na.rm=T))
awayD <- mean(apply(dists, 1, max, na.rm=T))

ggplot(a[Jump>0], aes(Jump)) + geom_density(alpha=1, linetype="blank", fill="black") + facet_wrap(~ Task, scales="free_y", ncol=3) + 
  geom_vline(xintercept=c(closestD,allD,awayD), linetype="dashed", size=.25) + ylab("Frequency") +
  ggtitle("Size of jump (euclidean dist in behav. space) when changing to a different primitive") +
  xlab("Jump size. Dashed lines: Mean distance to nearest/all/furthest neighbour(s) in behav. space")


# Activation stats ##############

act.stats <- act[, .(Number=length(unique(Primitive)), 
                                  Duration=mean(rle(Primitive)$lengths), 
                                  MaxDuration=max(rle(Primitive)$lengths),
                                  Used=sum(sapply(.SD[, paste0("ArbitratorOut_",0:6), with=F], sd) > 0.10, na.rm=T)),
                 by=.(Seed, Job, Task)]

act.sum <- act.stats[, .(Used = mean(Used)), by=.(Task,Job)][, .(Mean=mean(Used),SE=se(Used)), by=.(Task)]
ggplot(act.sum, aes(Task, Mean)) + geom_bar(stat="identity") + ggtitle("Average number of outputs varied in one simulation (SD > 0.1)") +
  geom_errorbar(aes(ymin=Mean-SE,ymax=Mean+SE), width=.5) + ylab("Outputs varied (out of 7)") + ylim(0,7)

act.sum <- act.stats[, .(Number = mean(Number)), by=.(Task,Job)][, .(Mean=mean(Number),SE=se(Number)), by=.(Task)]
ggplot(act.sum, aes(Task, Mean)) + geom_bar(stat="identity") + ggtitle("Number of primitives used") +
  geom_errorbar(aes(ymin=Mean-SE,ymax=Mean+SE), width=.5) + ylab("Number of primitives used (out of 5000)")

act.sum <- act.stats[, .(Duration = mean(Duration)), by=.(Task,Job)][, .(Mean=mean(Duration),SE=se(Duration)), by=.(Task)]
ggplot(act.sum, aes(Task, Mean)) + geom_bar(stat="identity") + ggtitle("Average time in each primitive") +
  geom_errorbar(aes(ymin=Mean-SE,ymax=Mean+SE), width=.5) + ylab("Number of consecutive steps in each primitive")



# Dimensionality reduction ###############

subfit <- fit[Repo=="ns" & RepoJob==3 & (grepl("mds*",Reduction) | is.na(Reduction))]
sum <- lastGen(subfit)[, .(Fitness=mean(ScaledFitness), SE=se(ScaledFitness)), by=.(Task, Reduction)]
sum <- rbind(sum, sum[, .(Task="Average",Fitness=mean(Fitness),SE=se(Fitness)), by=.(Reduction)])

ggplot(sum, aes(Task, Fitness, fill=Reduction)) + geom_bar(stat="identity", position="dodge") + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.25, position=position_dodge(.9)) +
  ggtitle("Fitness achieved in each task, using different number of reduced dimensions") +
  scale_y_continuous(limits=c(0.4,1),oob=rescale_none)
  

m <- metaAnalysis(lastGen(subfit)[Reduction!="mds1" & Reduction!="mds2"], ScaledFitness ~ Reduction , ~ Task)
sapply(m, function(x) x$ttest$kruskal$p.value)

metaAnalysis(lastGen(subfit)[,.(MeanFitness=mean(ScaledFitness)), by=.(Task,Reduction)], MeanFitness ~ Reduction, paired=T)


# R² fit

baserep <- rep[Repo=="ns" & Job==3]
originalDists <- Dist(baserep[,vars,with=F], diag=T, upper=T)

rsquarefit <- function(x) {
  f <- fread(paste0("~/Dropbox/mase/src/mase/app/playground/rep/sdbc_ns_3_",x,".txt"))
  d <- Dist(f[,-1,with=F], diag=T, upper=T)
  return(cor(c(d), c(originalDists))^2)
}
fits <- data.table(Reduction=c("mds1","mds2","mds3","mds4","mds5"))
fits[, Rsquared := rsquarefit(Reduction), by=.(Reduction)]
ggplot(fits, aes(Reduction,Rsquared)) + geom_point() + geom_line(aes(group=0)) +
  ylab("Coefficient of determination R²") + ggtitle("Fit of SMACOF-MDS dimensionality reduction")

baserep[, variable := factor(variable, labels=c("Walls-Dist","Obstacle-Mean-Dist","Obstacle-Closest-Dist","Object-Mean-Dist","Object-Closest-Dist","Linear-Speed","Turn-Speed"))]

bvars <- baserep[,vars,with=F]
setnames(bvars, c("Walls-Dist","Obstacle-Mean-Dist","Obstacle-Closest-Dist","Object-Mean-Dist","Object-Closest-Dist","Linear-Speed","Turn-Speed"))
round(cor(bvars,method="pearson"), digits=2)



# Repertoire size reduction ###########

subfit <- fit[Repo=="ns" & RepoJob==3 & (grepl("pam*",Reduction) | is.na(Reduction))]
subfit[, Reduction := factor(Reduction, levels=c("pam50","pam100","pam500","pam1000","pam2500"))]
sum <- lastGen(subfit)[, .(Fitness=mean(ScaledFitness), SE=se(ScaledFitness)), by=.(Task, Reduction)]
sum <- rbind(sum, sum[, .(Task="Average",Fitness=mean(Fitness),SE=se(Fitness)), by=.(Reduction)])

ggplot(sum, aes(Task, Fitness, fill=Reduction)) + geom_bar(stat="identity", position="dodge") + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.25, position=position_dodge(.9)) +
  ggtitle("Fitness achieved in each task, using  subsets of primitives of different sizes") +
  scale_y_continuous(limits=c(0.6,1),oob=rescale_none)

m <- metaAnalysis(lastGen(subfit[Reduction!="pam50" & Reduction!="pam100"]), BestSoFar ~ Reduction , ~ Task)
sapply(m, function(x) x$ttest$kruskal$p.value)

metaAnalysis(lastGen(subfit)[,.(MeanFitness=mean(ScaledFitness)), by=.(Task,Reduction)], MeanFitness ~ Reduction, paired=T)


# View reduction

pams <- c("pam50","pam100","pam500","pam1000","pam2500")
all <- lapply(paste0("~/Dropbox/mase/src/mase/app/playground/rep/sdbc_ns_3_",pams,".txt"), fread, col.names=c("Index",vars))
names(all) <- pams
all[["All"]] <- rep[Repo=="ns" & Job==3, c("Index",vars), with=F]
all <- rbindlist(all,idcol="Reduction")
all[, Reduction := factor(Reduction, levels=c("pam50","pam100","pam500","pam1000","pam2500","All"))]

red <- reduceData(all, vars, method="Rtsne")
ggplot(red[Reduction!="All"], aes(x=V1, y=V2)) + 
  geom_point(data=red[Reduction=="All", .(V1,V2)], shape=16, size=1, colour="lightgray") +
  geom_point(shape=4, size=1, colour="red") + 
  facet_wrap(~ Reduction) + coord_fixed() + 
  theme(axis.text.x = element_blank(), axis.text.y = element_blank(), axis.ticks=element_blank()) + labs(x=NULL, y=NULL) +
  ggtitle("Primitives in the repertoire after reduction")







# OLD #################################################################


#Locking

subfit <- fit[Repo=="ns" & RepoJob==0 & is.na(Reduction)]
sum <- lastGen(subfit)[, .(Fitness=mean(RelativeFitness), SE=se(RelativeFitness)), by=.(Task, Locking)]
sum <- rbind(sum, sum[, .(Task="Average",Fitness=mean(Fitness),SE=0), by=.(Locking)])

ggplot(sum, aes(Task, Fitness, fill=Locking)) + geom_bar(stat="identity", position="dodge") + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.25, position=position_dodge(.9)) +
  ggtitle("Fitness achieved in each task, using different reduction techniques")

m <- metaAnalysis(lastGen(subfit), BestSoFar ~ Locking , ~ Task)
sapply(m, function(x) x$ttest$kruskal$p.value)

act.stats <- act[, .(Number=length(unique(Primitive)), 
                                   Duration=mean(rle(Primitive)$lengths), 
                                   MaxDuration=max(rle(Primitive)$lengths),
                                   Used=sum(sapply(.SD[, paste0("ArbitratorOut_",0:6), with=F], sd) > 0.10, na.rm=T)),
                 by=.(Seed, Job, Task, Locking)]

act.sum <- act.stats[, .(Number = mean(Number)), by=.(Task,Job,Locking)][, .(Mean=mean(Number),SE=se(Number)), by=.(Task,Locking)]
ggplot(act.sum, aes(Task, Mean,fill=Locking)) + geom_bar(stat="identity",position="dodge") + ggtitle("Number of primitives chosen") +
  geom_errorbar(aes(ymin=Mean-SE,ymax=Mean+SE), width=.5, position=position_dodge(.9)) + ylab("Number of primitives used (/5000)")

act.sum <- act.stats[, .(Duration = mean(Duration)), by=.(Task,Job,Locking)][, .(Mean=mean(Duration),SE=se(Duration)), by=.(Task,Locking)]
ggplot(act.sum, aes(Task, Mean,fill=Locking)) + geom_bar(stat="identity",position="dodge") + ggtitle("Average time in primitive (steps)") +
  geom_errorbar(aes(ymin=Mean-SE,ymax=Mean+SE), width=.5, position=position_dodge(.9)) + ylab("Number of consecutive steps in each primitive")

lock.stat <- act[!is.na(Locking), .(LockTime=sum(Locked==1)/.N),  by=.(Seed, Job, Task)][, .(LockTime = mean(LockTime)), by=.(Task,Job)][, .(Mean=mean(LockTime),SE=se(LockTime)), by=.(Task)]
ggplot(lock.stat, aes(Task, Mean)) + geom_bar(stat="identity") + ggtitle("Proportion of time in locked state") +
  geom_errorbar(aes(ymin=Mean-SE,ymax=Mean+SE), width=.5) + ylab("Locked time")

# NSLC vs NS

sub <- rep[Repo=="base" | Repo=="ns"]

reduced <- reduceData(unique(sub, by=vars), vars=vars, method="Rtsne", k=2)
plotReduced2D(reduced, color.var="Fitness") + facet_grid(Repo ~ Job)

qdscores <- sub[, qdscore(.SD,vars,d=0.5), by=.(Repo,Job)]
metaAnalysis(qdscores, QDscore ~ Repo)

subfit <- fit[(Repo=="base" | Repo=="ns") & Reduction=="mds3" & !is.na(Locking)]

sum <- lastGen(subfit)[, .(Fitness=mean(RelativeFitness)), by=.(Task, Repo, RepoJob)]
ggplot(sum, aes(paste(Repo,RepoJob),Fitness)) + geom_bar(aes(fill=Task), stat="identity") + 
  theme(axis.text.x=element_text(angle=22.5,hjust=1)) + labs(x="Setup", y="Fitness relative to direct (%)")

sum2 <- sum[, .(Fitness=mean(Fitness)), by=.(Task,Repo)]
ggplot(sum2, aes(Repo,Fitness)) + geom_bar(aes(fill=Task), stat="identity") + 
  theme(axis.text.x=element_text(angle=22.5,hjust=1)) + labs(x="Setup", y="Fitness relative to direct (%)")

metaAnalysis(lastGen(subfit), BestSoFar ~ Repo , ~ Task)








subrep <- rep[Repo %in% c("ns","nsslp","nsmlp5","nsmlp1010","nsneat")]
subrep[, Repo := factor(Repo, levels=c("slp","mlp5","base","neat"), labels=c("SLP","MLP-5","MLP-10","NEAT"))]

parallelCoordinates(subrep[Job==0], vars=vars) + facet_wrap(~ Repo)

red <- reduceData(unique(subrep[Job==0], by=vars), vars=vars, method="Rtsne", k=2)
plotReduced2D(red, color.var="Repo") + facet_wrap(~ Repo) + ggtitle("Behaviour space, one job (t-SNE)")

scores <- subrep[, qdscore(.SD, vars=vars, d=0.5), by=.(Repo,Job)]
ggplot(scores[, .(Mean=mean(N), SE=se(N)), by=.(Repo)], aes(Repo, Mean)) + geom_bar(stat="identity") + 
  geom_errorbar(aes(ymin=Mean-SE,ymax=Mean+SE), width=.5) + ylab("Number of regions reached")








setwd("~/exps/playground/rep10")

lapply(c("nsneat","nsnoobj","nsfew","nsnoobs","nsmlp5","nsslp","nsmlp5","nsmlp1010"), processRepo, red=NULL)
lapply(c("base","neat","ns"), processRepo)

processRepo("base", jobs=0, k=2)
processRepo("base", jobs=0, k=4)
processRepo("base", jobsk=5)







data <- loadFile("~/exps/playground/rep10/noobs/job.0.archive.stat")
plotVarsHist(data, vars)
parallelCoordinates(data, vars, alpha=.05)

setwd("~/labmag/exps/playground/rep10")
data <- loadData("*", "archive.stat")
data[, Fitness := Fitness / 10]
vars <- paste0("Behav_",0:6)

lplot <- lapply(split(data, by="Setup"), plotVarsHist, vars=vars)
plot_grid(plotlist=lplot, labels=names(lplot))

qdscore <- function(data, vars, d=0.25) {
  datacopy <- copy(data)
  bins <- paste0("bin",0:(length(vars)-1))
  datacopy[ , (bins) := lapply(.SD, function(x){round(x/d)}), .SDcols=vars]
  maxes <- datacopy[, .(Fitness=max(Fitness)), by=.(bin0,bin1,bin2,bin3,bin4,bin5,bin6)]
  qdscore <- sum(maxes$Fitness)
  return(list(QDscore=qdscore, N=nrow(maxes), MeanFitness=mean(maxes$Fitness)))
}

data[, qdscore(.SD,vars,d=0.5), by=.(Setup,Job)]


# R-squared fit
red <- fread("~/Dropbox/mase/src/mase/app/playground/rep/sdbc_neat_1_mds4.txt")
ori <- fread("~/exps/playground/rep9/neat/job.1.archive.stat")
dred <- Dist(red[,.(V2,V3,V4,V5)], diag=T, upper=T)
dori <- Dist(ori[,vars,with=F], diag=T, upper=T)
cor(c(dred), c(dori))^2


plotVarsHist(data, vars)
parallelCoordinates(data, vars, alpha=.05) + facet_wrap(~ Setup)

data <- loadData("*", "archive.stat")

d <- loadFile("~/exps/playground/rep9/neat/job.4.archive.stat")
coords <- fread("~/Dropbox/mase/src/mase/app/playground/rep/sdbc_neat_4_mds3.txt", col.names=c("Index","V1","V2","V3"))
m <- merge(coords, d, by="Index")
plotReduced3D(m, color.var="Fitness")

d <- loadFile("~/exps/playground/tasks9/pl_phototaxis_sdbc_neat_0_mds3_locking/job.0.postbest.xml.stat")[Seed==2]
coords <- fread("~/Dropbox/mase/src/mase/app/playground/rep/sdbc_neat_0_mds3.txt", col.names=c("Index","V1","V2","V3"))
m <- merge(d, coords, by.x="Primitive", by.y="Index")

lines3D(m$Coord_0, m$Coord_1, m$Coord_2, phi=20, theta=45, cex=0.6, bty="b2", colvar=d$Time, ticktype="detailed", type="b", xlim=c(0,1),ylim=c(0,1),zlim=c(0,1))
lines3D(m$V1, m$V2, m$V3, phi=20, theta=45, cex=0.6, bty="b2", colvar=d$Time, ticktype="detailed", type="b", xlim=c(0,1),ylim=c(0,1),zlim=c(0,1))


setwd("~/exps/playground/tasks9")
d <- loadData("*neat_0_mds3_locking", filename="postbest.xml.stat", auto.ids.names=c("Domain","Task","BC","Method","RepoJob","Reduction","Locking"))
coords <- fread("~/Dropbox/mase/src/mase/app/playground/rep/sdbc_neat_0_mds3.txt", col.names=c("Primitive","V1","V2","V3"))
d <- merge(d, coords, by="Primitive")

act <- d[Task!="coverage"]
act[, Progress := Time / max(Time), by=.(Task,Job,Seed)]
par(mfrow = c(3,3), mar=c(1, 1, 1, 1))#it goes c(bottom, left, top, right)
act[, scatter3D(.SD$V1, .SD$V2, .SD$V3, phi=20, theta=45, pch=16, cex=0.75, bty="b2", colvar=as.numeric(.SD$Job), xlim=range(coords$V1), ylim=range(coords$V2),zlim=range(coords$V3), main=.BY[[1]] ,ticktype="detailed"), by=.(Task)]
act[, scatter3D(.SD$ArbitratorOut_1, .SD$ArbitratorOut_2, .SD$ArbitratorOut_3, phi=20, theta=45, pch=16, cex=0.75, bty="b2", colvar=as.numeric(.SD$Job), xlim=c(0,1), ylim=c(0,1),zlim=c(0,1), main=.BY[[1]] ,ticktype="detailed"), by=.(Task)]


setwd("~/exps/playground/tasks9")
fit <- loadData("*", "postfitness.stat", fun=loadFitness, auto.ids.names=c("Domain","Task","BC","Method","RepoJob","Reduction","Locking"))
fit[, Reference := mean(.SD[is.na(Method) & Generation==max(Generation)]$BestSoFar), by=.(Task)]
fit[, RelativeFitness := ((BestSoFar-Reference) / Reference) * 100]
fit[, Conf := paste(BC,Method,Reduction,Locking, sep="-")]
fit <- fit[Task != "coverage"]

fit <- fit[Method=="neat"]

#fit <- fit[Task != "coverage" & Method=="neat" & RepoJob==1]

# NSLC vs NS
metaAnalysis(lastGen(fit)[!is.na(Method)], BestSoFar ~ Method, ~Task)

# NSLC vs direct
metaAnalysis(lastGen(fit)[is.na(Method) | Method=="neat"], BestSoFar ~ Method, ~Task)

# Repo job differences
metaAnalysis(lastGen(fit)[Method=="neat"], BestSoFar ~ RepoJob, ~Task)

sum <- lastGen(fit)[!is.na(Repo), .(Fitness=mean(RelativeFitness)), by=.(Task, Conf, RepoJob)]
ggplot(sum, aes(paste0(Conf,RepoJob),Fitness)) + geom_bar(aes(fill=Task), stat="identity") + 
  theme(axis.text.x=element_text(angle=22.5,hjust=1)) + labs(x="Setup", y="Fitness relative to direct (%)")

sum2 <- sum[, .(Mean=mean(Fitness),Min=min(Fitness), Max=max(Fitness)), by=.(Reduction)]
ggplot(sum2, aes(Reduction,Mean)) + geom_bar(stat="identity", fill="steelblue") + 
  theme(axis.text.x=element_text(angle=22.5,hjust=1)) + labs(x="Method", y="Fitness relative to direct (%) [Min,Max]") +
  geom_errorbar(aes(ymin=Min, ymax=Max),width=0.25)

sum <- lastGen(fit)[Method %in% c("neat","many") & Reduction=="mds3", .(Fitness=mean(RelativeFitness)), by=.(Task, Conf, RepoJob)]
sum <- lastGen(fit)[!is.na(Method), .(Fitness=mean(RelativeFitness)), by=.(Task, Conf, RepoJob)]
ggplot(sum, aes(paste(Conf,RepoJob),Fitness)) + geom_bar(aes(fill=Task), stat="identity") + 
  theme(axis.text.x=element_text(angle=22.5,hjust=1)) + labs(x="Setup", y="Fitness relative to direct (%)")

abs <- lastGen(fit)[, .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(Task, Conf)]
ggplot(abs, aes(Task,Mean,fill=Conf)) + geom_bar(stat="identity",position="dodge") +
  geom_errorbar(aes(ymin=Mean-SE, ymax=Mean+SE),width=0.25,position=position_dodge(.9))


sum2 <- sum[, .(Fitness=mean(Fitness)), by=.(Task,Conf)]
ggplot(sum2, aes(Conf,Fitness)) + geom_bar(aes(fill=Task), stat="identity") + 
  theme(axis.text.x=element_text(angle=22.5,hjust=1)) + labs(x="Setup", y="Fitness relative to direct (%)")

sum3 <- sum[, .(Mean=mean(Fitness),Min=min(Fitness), Max=max(Fitness)), by=.(Conf, RepoJob)]
ggplot(sum3, aes(paste(Conf,RepoJob),Mean)) + geom_bar(stat="identity", fill="steelblue") + 
  theme(axis.text.x=element_text(angle=22.5,hjust=1)) + labs(x="Method", y="Fitness relative to direct (%) [Min,Max]") +
  geom_errorbar(aes(ymin=Min, ymax=Max),width=0.25)

selected <- fit[is.na(BC) | (Method=="neat" & RepoJob==0)]
ggplot(lastGen(selected), aes(Method,BestSoFar)) + geom_boxplot() + facet_wrap(~ Task, scales="free_y")
ggplot(selected[, .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(Generation,Task,Method)], aes(Generation,Mean,group=Method)) + 
  geom_line(aes(colour=Method)) + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE, fill=Method), alpha = 0.1) + ylab("Fitness")  + facet_wrap(~ Task, scales="free_y")


sum <- lastGen(fit)[Method %in% c("neat","many") & Reduction=="mds3", .(Fitness=mean(RelativeFitness), .N), by=.(Task, Conf, RepoJob)]
ggplot(sum, aes(paste(Conf,RepoJob),Fitness)) + geom_bar(aes(fill=Task), stat="identity") + 
  theme(axis.text.x=element_text(angle=22.5,hjust=1)) + labs(x="Setup", y="Fitness relative to direct (%)")




fitnessBoxplots(fit) + ylim(0.3,1)


setwd("~/labmag/exps/playground/tasks8")
fit <- loadData(c("pl_avoidance","pl_coverage","pl_dynforaging","pl_dynphototaxis","pl_exploration","pl_freeforaging","pl_maze","pl_obsforaging","pl_phototaxis","pl_predator"), "postfitness.stat", fun=loadFitness)
fitnessBoxplots(fit) + ylim(0.3,1)



setwd("~/labmag/exps/playground/tasks8")
fit <- loadData("*", "postfitness.stat", fun=loadFitness)
ggplot(lastGen(fit), aes(ID2,BestSoFar)) + geom_boxplot() + facet_wrap(~ ID3)
ggplot(fit[, .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(Generation,ID2)], aes(Generation,Mean,group=ID2)) + 
  geom_line(aes(colour=ID2)) + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE, fill=ID2), alpha = 0.1) + ylab("Fitness")

sum <- lastGen(fit)[!is.na(BC), .(Fitness=mean(RelativeFitness)), by=.(Task, Method)]
ggplot(sum, aes(Method,Fitness)) + geom_bar(aes(fill=Task), stat="identity") + 
  theme(axis.text.x=element_text(angle=22.5,hjust=1)) + labs(x="Method", y="Fitness relative to direct (%)")



# Generalization




setwd("~/exps/playground/tasks5")
fit <- loadData(c("pl_phototaxis_nsmedian_2","direct_phototaxis"), "postfitness.stat", fun=loadFitness, auto.ids.names=c("Domain","Task","Repo","Reduction"))
ggplot(lastGen(fit), aes(Repo, BestSoFar)) + geom_boxplot()
fitnoobs <- loadData(c("pl_phototaxis_nsmedian_2","direct_phototaxis"), "noobsfitness.stat", fun=loadFitness, auto.ids.names=c("Domain","Task","Repo","Reduction"))
ggplot(lastGen(fitnoobs), aes(Repo, BestSoFar)) + geom_boxplot()
fitmanyobs <- loadData(c("pl_phototaxis_nsmedian_2","direct_phototaxis"), "manyobsfitness.stat", fun=loadFitness, auto.ids.names=c("Domain","Task","Repo","Reduction"))
ggplot(lastGen(fitmanyobs), aes(Repo, BestSoFar)) + geom_boxplot()


# test

setwd("~/labmag/exps/playground/rep7/")
data <- loadData("*", "archive.stat")
vars <- paste0("Behav_",0:6)
data[, Fitness := Fitness / 10]

qdscore <- function(data, vars, d=0.25) {
  datacopy <- copy(data)
  bins <- paste0("bin",0:(length(vars)-1))
  datacopy[ , (bins) := lapply(.SD, function(x){round(x/d)}), .SDcols=vars]
  maxes <- datacopy[, .(Fitness=max(Fitness)), by=.(bin0,bin1,bin2,bin3,bin4,bin5,bin6)]
  qdscore <- sum(maxes$Fitness)
  print(nrow(maxes))
  return(list(QDscore=qdscore, N=nrow(maxes), MeanFitness=mean(maxes$Fitness)))
}

data[, qdscore(.SD,vars,d=0.5), by=.(Setup,Job)]

red <- reduceData(unique(data,by=vars), vars=vars, method="Rtsne", k=2)



plotReduced2D(red, color.var="Fitness") + facet_wrap(~ Setup)
parallelCoordinates(data, vars, alpha=0.05) + facet_wrap(~ Setup)
data[, .(CorObs=cor(Behav_1,Behav_2),CorObj=cor(Behav_3,Behav_4)), by=.(Setup)]

red <- reduceData(data[Setup=="slp"], vars=vars, method="rpca", k=7) ; plotReduced2D(red, color.var="Fitness")
write.table(red[,.(Index,V1,V2,V3,V4)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbc_slp_direct_pca4.txt", row.names=F, col.names=F, sep=" ")

red <- reduceData(data[Setup=="mlp5"], vars=vars, method="rpca", k=7) ; plotReduced2D(red, color.var="Fitness")
write.table(red[,.(Index,V1,V2,V3,V4)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbc_mlp5_direct_pca4.txt", row.names=F, col.names=F, sep=" ")

red <- reduceData(data[Setup=="mlp10"], vars=vars, method="rpca", k=7) ; plotReduced2D(red, color.var="Fitness")
write.table(red[,.(Index,V1,V2,V3,V4)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbc_mlp10_direct_pca4.txt", row.names=F, col.names=F, sep=" ")

red <- reduceData(data[Setup=="neat" & Job==0], vars=vars, method="rpca", k=7) ; plotReduced2D(red, color.var="Fitness")
write.table(red[,.(Index,V1,V2,V3,V4)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbc_neat_direct_pca4.txt", row.names=F, col.names=F, sep=" ")

red <- reduceData(data[Setup=="neat" & Job==1], vars=vars, method="rpca", k=7) ; plotReduced2D(red, color.var="Fitness")
write.table(red[,.(Index,V1,V2,V3,V4)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbc_neat1_direct_pca4.txt", row.names=F, col.names=F, sep=" ")

red <- reduceData(data[Setup=="neat" & Job==2], vars=vars, method="rpca", k=7) ; plotReduced2D(red, color.var="Fitness")
write.table(red[,.(Index,V1,V2,V3,V4)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbc_neat2_direct_pca4.txt", row.names=F, col.names=F, sep=" ")

red <- reduceData(data[Setup=="neat" & Job==3], vars=vars, method="rpca", k=7) ; plotReduced2D(red, color.var="Fitness")
write.table(red[,.(Index,V1,V2,V3,V4)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbc_neat3_direct_pca4.txt", row.names=F, col.names=F, sep=" ")

red <- reduceData(data[Setup=="neat" & Job==4], vars=vars, method="rpca", k=7) ; plotReduced2D(red, color.var="Fitness")
write.table(red[,.(Index,V1,V2,V3,V4)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbc_neat4_direct_pca4.txt", row.names=F, col.names=F, sep=" ")

d <- unique(data[Setup=="neat" & Job==0], by=vars)
red2 <- reduceData(d, vars=vars, method="mds", k=2)
write.table(red2[,.(Index,V1,V2)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbc_neat_direct_mds2.txt", row.names=F, col.names=F, sep=" ")

red4 <- reduceData(d, vars=vars, method="mds", k=4)
write.table(red4[,.(Index,V1,V2,V3,V4)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbc_neat_direct_mds4.txt", row.names=F, col.names=F, sep=" ")

red3 <- reduceData(d, vars=vars, method="mds", k=3)
write.table(red3[,.(Index,V1,V2,V3)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbc_neat_direct_mds3.txt", row.names=F, col.names=F, sep=" ")

d <- loadFile("~/labmag/exps/playground/rep7/neat/job.0.archive.stat")
coords <- fread("~/Dropbox/mase/src/mase/app/playground/rep/sdbc_neat_direct_mds3.txt", col.names=c("Index","V1","V2","V3"))
m <- merge(coords, d, by="Index")

cls <- kmeans(m[,.(V1,V2,V3)], centers=2500, iter.max=1000, nstart=100)
m[, Cluster := cls$cluster]
sparse <- m[, .SD[which.max(Fitness)], by=.(Cluster)]
plotReduced2D(sparse,color.var="Fitness")
write.table(sparse[,.(Index,V1,V2,V3)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbc_neat_2500_mds3.txt", row.names=F, col.names=F, sep=" ")



d <- data[Setup=="neat"]
cls <- kmeans(d[,vars,with=F], centers=1000, iter.max=1000, nstart=100)
d[, Cluster := cls$cluster]
sparse <- d[, .SD[which.max(Fitness)], by=.(Cluster)]
red <- reduceData(sparse, vars=vars, method="rpca", k=7) ; plotReduced2D(red, color.var="Fitness")
write.table(red[,.(Index,V1,V2,V3,V4)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbc_neat_1000_pca4.txt", row.names=F, col.names=F, sep=" ")



rnew <- loadFile("~/exps/playground/rep6/sdbc_neat/job.0.archive.stat")
vars <- paste0("Behav_",0:4)
cls <- kmeans(rnew[,vars,with=F], centers=1000, iter.max=1000, nstart=100)
rnew[, Cluster := cls$cluster]
sparse <- rnew[, .SD[which.max(Fitness)], by=.(Cluster)]

red <- reduceData(sparse, vars=vars, method="rpca", k=5)
plotReduced2D(red, color.var="Fitness")

write.table(red[,.(Index,V1,V2)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbc_base_2000_pca2.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbc_base_2000_pca3.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbc_base_2000_pca4.txt", row.names=F, col.names=F, sep=" ")
write.table(sparse[,.(Index,Behav_0,Behav_1,Behav_2,Behav_3,Behav_4)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbc_base_2000_direct.txt", row.names=F, col.names=F, sep=" ")



red <- reduceData(rnew, vars=vars, method="rpca", k=5)
write.table(red[,.(Index,V1,V2)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbc_base_direct_pca2.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbc_base_direct_pca3.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbc_base_direct_pca4.txt", row.names=F, col.names=F, sep=" ")


rnew <- loadFile("~/exps/playground/rep6/as_base/job.0.archive.stat")
vars <- paste0("Behav_",0:8)

cls <- kmeans(rnew[,vars,with=F], centers=500, iter.max=1000, nstart=100)
rnew[, Cluster := cls$cluster]
sparse <- rnew[, .SD[which.max(Fitness)], by=.(Cluster)]

red <- reduceData(sparse, vars=vars, method="rpca", k=9)
plotReduced2D(red, color.var="Fitness")
parallelCoordinates(red, vars=paste0("V",1:5), color.var="Fitness")
parallelCoordinates(sparse, vars=paste0("Behav_",0:8), color.var="Fitness")

write.table(red[,.(Index,V1,V2,V3)], file="~/Dropbox/mase/src/mase/app/playground/rep/as_base_500_pca3.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4,V5)], file="~/Dropbox/mase/src/mase/app/playground/rep/as_base_500_pca5.txt", row.names=F, col.names=F, sep=" ")
write.table(sparse[,.(Index,Behav_0,Behav_1,Behav_2,Behav_3,Behav_4,Behav_5,Behav_6,Behav_7,Behav_8)], file="~/Dropbox/mase/src/mase/app/playground/rep/as_base_500_direct.txt", row.names=F, col.names=F, sep=" ")




# NSLC

setwd("~/exps/playground/rep5")

rep <- loadData(c("nslcmedian","nsmedian2"), "archive.stat")
red <- reduceData(rep, vars=paste0("Behav_",0:4), method="rpca", k=5)
plotReduced2D(red, color.var="Fitness") + facet_grid(Setup ~ Job)

rep[, mean(Fitness), by=.(Setup,Job)]


# 1. cluster into K clusters --> number of desired primitives
# 2. select the primitive from the cluster with the highest fitness

r <- loadFile("nslcmedian/job.0.archive.stat")
cls <- kmeans(r[,vars,with=F], centers=100)
r[, Cluster := cls$cluster]
sparse <- r[, .SD[which.max(Fitness)], by=.(Cluster)]

write.table(sparse[,.(Index,Behav_0,Behav_1,Behav_2,Behav_3,Behav_4)], file="~/Dropbox/mase/src/mase/app/playground/rep/nslcmedian_sparse.txt", row.names=F, col.names=F, sep=" ")

red <- reduceData(sparse, vars=vars, method="rpca", k=5)
write.table(red[,.(Index,V1,V2,V3)], file="~/Dropbox/mase/src/mase/app/playground/rep/nslcmedian_sparserpca3.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4)], file="~/Dropbox/mase/src/mase/app/playground/rep/nslcmedian_sparserpca4.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4,V5)], file="~/Dropbox/mase/src/mase/app/playground/rep/nslcmedian_sparserpca5.txt", row.names=F, col.names=F, sep=" ")

pam <- pam(r[,vars,with=F], k=100)
r[, Cluster := pam$clustering]
bests <- r[, .SD[which.max(Fitness)], by=.(Cluster)]
meds <- r[pam$id.med]

all <- rbind(cbind(r,Sparse="original"), cbind(bests,Sparse="bests"),cbind(meds,Sparse="medoids"))
red <- reduceData(all, vars=vars, method="rpca", k=5)
plotReduced2D(red, color.var="Fitness") + facet_wrap(~Sparse)
all[, mean(Fitness), by=.(Sparse)]


all <- rbind(cbind(r,Sparse="original"), cbind(sparse,Sparse="sparse100"))
red <- reduceData(all, vars=vars, method="rpca", k=5)
plotReduced2D(red, color.var="Fitness") + facet_wrap(~Sparse)

red <- reduceData(sparse, vars=vars, method="rpca", k=5)
plotReduced2D(red, color.var="Fitness")

r <- loadFile("nsmedian/job.0.archive.stat")
pam <- pam(r[,vars,with=F], k=100)
r[, Cluster := pam$clustering]

meds <- r[pam$id.med]
write.table(meds[,.(Index,Behav_0,Behav_1,Behav_2,Behav_3,Behav_4)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsmedian_sparse.txt", row.names=F, col.names=F, sep=" ")

red <- reduceData(meds, vars=vars, method="rpca", k=3)
write.table(red[,.(Index,V1,V2,V3)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsmedian_sparserpca3.txt", row.names=F, col.names=F, sep=" ")




require(FNN)
r1 <- loadFile("nslcmedian/job.0.archive.stat")
vars <- paste0("Behav_",0:4)

k <- get.knn(r1[,vars,with=F], k=15)
r1[, BetterThan := sum(Fitness > r1[k$nn.index[.GRP,], .(Fitness)]), by=.(Index)]
copy <- r1[BetterThan > 10]
all <- rbind(cbind(r1,Sparse="original"), cbind(copy,Sparse="better10"))

red <- reduceData(all, vars=vars, method="rpca")
plotReduced2D(red, color.var="Fitness") + facet_wrap(~Sparse)


kdiv <- function(d) {
  k <- knn.dist(d, k=9)
  return(data.table(k=1:9,dist=colMeans(k)))
}




setwd("~/exps/playground/tasks8")
fit <- loadData("*", "postfitness.stat", fun=loadFitness, auto.ids.names=c("Domain","Task","BC","RepoPar","Size","Reduction","Locking"))
fit[, Reference := mean(.SD[is.na(BC) & Generation==max(Generation)]$BestSoFar), by=.(Task)]
fit[, RelativeFitness := ((BestSoFar-Reference) / Reference) * 100]

fit[Size=="direct", Size := NA]
fit[Reduction=="direct", Reduction := NA]
fit[, Method := paste(BC,RepoPar,Size,Reduction,Locking, sep="-")]

sum <- lastGen(fit)[!is.na(BC), .(Fitness=mean(RelativeFitness)), by=.(Task, Method)]
ggplot(sum, aes(Method,Fitness)) + geom_bar(aes(fill=Task), stat="identity") + 
  theme(axis.text.x=element_text(angle=22.5,hjust=1)) + labs(x="Method", y="Fitness relative to direct (%)")

sum2 <- sum[, .(Mean=mean(Fitness),Min=min(Fitness), Max=max(Fitness)), by=.(Method)]
ggplot(sum2, aes(Method,Mean)) + geom_bar(stat="identity", fill="steelblue") + 
  theme(axis.text.x=element_text(angle=22.5,hjust=1)) + labs(x="Method", y="Fitness relative to direct (%) [Min,Max]") +
  geom_errorbar(aes(ymin=Min, ymax=Max),width=0.25)

selected <- fit[is.na(BC) | Method=="sdbc-neat-NA-pca4-locking"]
ggplot(lastGen(selected), aes(Method,BestSoFar)) + geom_boxplot() + facet_wrap(~ Task, scales="free_y")
ggplot(selected[, .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(Generation,Task,Method)], aes(Generation,Mean,group=Method)) + 
  geom_line(aes(colour=Method)) + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE, fill=Method), alpha = 0.1) + ylab("Fitness")  + facet_wrap(~ Task, scales="free_y")


fitneat <- fit[grepl("neat", RepoPar) & is.na(Size)]
metaAnalysis(lastGen(fitneat), BestSoFar ~ RepoPar, ~ Task)

# Task performance

setwd("~/exps/playground/tasks5")

fit <- loadData("*", "postfitness.stat", fun=loadFitness, auto.ids.names=c("Domain","Task","Repo","Reduction","Locking"))
fit[, Reference := mean(.SD[is.na(Repo) & Generation==max(Generation)]$BestSoFar), by=.(Task)]
fit[, RelativeFitness := ((BestSoFar-Reference) / Reference) * 100]
fit[is.na(Repo), Repo := "Direct"]
fit[, Reduction := factor(Reduction, levels=c("0","1","2","3","4","Direct"), labels=c("3","4","5","7","10","direct"))]

sum <- lastGen(fit)[Repo!="Direct", .(Fitness=mean(RelativeFitness)), by=.(Repo, Task, Reduction, Locking)]
ggplot(sum, aes(paste(Repo,Reduction,Locking),Fitness)) + 
  geom_bar(aes(fill=Task), stat="identity") + 
  theme(axis.text.x=element_text(angle=22.5,hjust=1)) + labs(x="Method", y="Fitness relative to direct (%)")

sum <- lastGen(fit)[Repo!="Direct", .(Fitness=mean(RelativeFitness)), by=.(Repo, Task, Reduction, Locking)]
sum <- sum[, .(Mean=mean(Fitness),Min=min(Fitness), Max=max(Fitness)), by=.(Repo,Reduction, Locking)]
ggplot(sum, aes(paste(Repo,Reduction,Locking,sep="-"),Mean)) + geom_bar(stat="identity", fill="steelblue") + 
  theme(axis.text.x=element_text(angle=22.5,hjust=1)) + labs(x="Method", y="Fitness relative to direct (%) [Min,Max]") +
  geom_errorbar(aes(ymin=Min, ymax=Max),width=0.25)


selected <- lastGen(fit)[is.na(Repo) | Repo=="base" | Repo=="nsmedian"]
ggplot(selected, aes(paste0(Repo,Reduction),BestSoFar)) + geom_boxplot() + facet_wrap(~ Task, scales="free_y")



lastGen(fit)[, , by=.(Method)]


# MAP-Elites vs NS comparison

repme <- loadFile("~/exps/playground/rep4/base/job.0.finalrep.stat")
repns <- loadFile("~/exps/playground/rep5/nsmedian/job.0.archive.stat")
b <- paste0("Behav_",0:4)
plotVarsHist(repme, vars=b)
plotVarsHist(repns, vars=b)

merged <- rbind(cbind(repme,Method="ME"), cbind(repns,Method="NS"), fill=T)
reduced <- reduceData(unique(merged, by=b), vars=b, method="Rtsne")
reduced <- reduceData(unique(merged, by=b), vars=b, method="rpca", k=5)
reduced <- reduceData(unique(merged, by=b), vars=b, method="sammon", k=2)

plotReduced2D(reduced) + facet_wrap(~ Method)


# Activations

act <- loadData("**", "postbest.xml.stat", filter=function(x){x[Seed < 3]}, auto.ids.names=c("Domain","Task","Repo","Reduction"))
act <- act[Repo != "memedian"]
act[, Reduction := factor(Reduction, levels=c("0","1","2","3","4","direct"), labels=c("3","4","5","7","10","direct"))]
act[, Progress := Time / max(Time), by=.(Task,Repo,Reduction,Job,Seed)]

sim.stats <- act[, .(Number=length(unique(Primitive)), 
         Duration=mean(rle(Primitive)$lengths), 
         MaxDuration=max(rle(Primitive)$lengths),
         Used=sum(sapply(.SD[, paste0("ArbitratorOut_",0:9), with=F], sd) > 0.1, na.rm=T),
         sd0=sd(ArbitratorOut_0),sd1=sd(ArbitratorOut_1),sd2=sd(ArbitratorOut_2),sd3=sd(ArbitratorOut_3),sd4=sd(ArbitratorOut_4),sd5=sd(ArbitratorOut_5),sd6=sd(ArbitratorOut_6),sd7=sd(ArbitratorOut_7),sd8=sd(ArbitratorOut_8),sd9=sd(ArbitratorOut_9)),
     by=.(Seed, Job, Task, Repo, Reduction)]

m <- melt(sim.stats, measure.vars=paste0("sd",0:9))
stat <- m[, .(Var=mean(value)), by=.(Repo,Reduction,variable)]
ggplot(stat, aes(variable, Var, group=Repo)) + geom_line(aes(colour=Repo)) + facet_wrap(~ Reduction, scales="free_x")

controller.means <- sim.stats[, .(Number=mean(Number), Duration=mean(Duration), MaxDuration=mean(MaxDuration), Used=mean(Used)), by=.(Job, Task, Repo, Reduction)]
ggplot(controller.means, aes(paste(Repo,Reduction), Used)) + geom_boxplot() + facet_wrap(~ Task) +
  theme(axis.text.x=element_text(angle=45,hjust=1))

ggplot(controller.means[, .(MeanUsed=mean(Used), SE=se(Used)), by=.(Repo,Reduction)], aes(Reduction,MeanUsed,group=Repo,colour=Repo)) + 
  geom_line() + geom_errorbar(aes(ymin=MeanUsed-SE, ymax=MeanUsed+SE), width=.25) + geom_point(shape=21, fill="white") + ylim(0,NA) 

ggplot(controller.means[, .(Number=mean(Number), SE=se(Number)), by=.(Repo,Reduction,Task)], aes(Reduction,Number,group=Repo,colour=Repo)) + 
  geom_line() + geom_errorbar(aes(ymin=Number-SE, ymax=Number+SE), width=.25) + geom_point(shape=21, fill="white") + facet_wrap(~ Task)


# NS-Median test case

m3 <- merge(act[Repo=="nsmedian" & Reduction=="3" & Seed==0], 
                fread("~/Dropbox/mase/src/mase/app/playground/rep/nsmedian_rpca3.txt", col.names=c("Primitive",paste0("B",0:2))),
                by="Primitive")

m4 <- merge(act[Repo=="nsmedian" & Reduction=="5" & Seed==0], 
            fread("~/Dropbox/mase/src/mase/app/playground/rep/nsmedian_rpca4.txt", col.names=c("Primitive",paste0("B",0:3))),
            by="Primitive")

m5 <- merge(act[Repo=="nsmedian" & Reduction=="5" & Seed==0], 
            fread("~/Dropbox/mase/src/mase/app/playground/rep/nsmedian_rpca5.txt", col.names=c("Primitive",paste0("B",0:4))),
            by="Primitive")

md <- merge(act[Repo=="nsmedian" & Reduction=="direct" & Seed==0], 
            fread("~/exps/playground/rep5/nsmedian/job.0.archive.stat"),
            by.x="Primitive", by.y="Index")

ggplot(melt(m3, measure.vars=paste0("B",0:2)), aes(variable, value, group=Time)) + geom_line(alpha=0.1) + facet_grid(Task ~ Job) + ggtitle("PCA 3")
ggplot(melt(m4, measure.vars=paste0("B",0:3)), aes(variable, value, group=Time)) + geom_line(alpha=0.1) + facet_grid(Task ~ Job) + ggtitle("PCA 4")
ggplot(melt(m5, measure.vars=paste0("B",0:4)), aes(variable, value, group=Time)) + geom_line(alpha=0.1) + facet_grid(Task ~ Job) + ggtitle("PCA 5")
ggplot(melt(md, measure.vars=paste0("Behav_",0:4,".y")), aes(variable, value, group=Time)) + geom_line(alpha=0.1) + facet_grid(Task ~ Job) + ggtitle("Direct")


#rep <- fread("~/Dropbox/mase/src/mase/app/playground/rep/nsmedian_rpca4.txt", col.names=c("Primitive",paste0("B",0:3)))
#merged <- merge(act[Repo=="nsmedian" & Reduction=="4" & Seed==0], rep, by="Primitive")
#ggplot(merged, aes(PC_0, PC_1)) + geom_point(data=rep, shape=20, colour="gray") + geom_point(aes(colour=Progress), shape=4) + facet_grid(Task ~ Job)




# Repertoires dimensionality reduction

rep <- loadFile("~/exps/playground/rep5/nsspirit/job.0.archive.stat")
toremove <- sapply(rep,function(x) length(unique(x))==1) & grepl("Behav_*", colnames(rep))
rep <- rep[, !toremove, with=F]

behav <- colnames(rep)[grep("Behav_*",colnames(rep))]
red <- reduceData(unique(rep, by=behav), behav, method="rpca", k=100,kmax=100)
plotReduced2D(red)

write.table(red[,.(Index,V1,V2,V3)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsspirit_rpca3.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsspirit_rpca4.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4,V5)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsspirit_rpca5.txt", row.names=F, col.names=F, sep=" ")

rep <- loadFile("~/labmag/exps/playground/rep5/nsmedian/job.0.archive.stat")
behav <- colnames(rep)[grep("Behav_*",colnames(rep))]

red <- reduceData(unique(rep, by=behav), behav, method="rpca", k=5)
plotReduced2D(red)

# kernel PCA test
kp <- kpca(~., data=rep[,behav,with=F], kernel="rbfdot", features=5, kpar=list(sigma = 0.2))
ggplot(as.data.table(kp@pcv), aes(V1,V2)) + geom_point()
ggplot(data.table(PC=1:5,Eigen=kp@eig), aes(PC,Eigen)) + geom_bar(stat="identity")


pc <- PcaHubert(rep[, behav, with=F], k=length(behav), kmax=length(behav), scale=F)
ggplot(data.table(PC=1:pc@k,SD=getSdev(pc)), aes(PC,SD)) + geom_bar(stat="identity")

write.table(red[,.(Index,V1,V2,V3)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsmedian_rpca3.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsmedian_rpca4.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4,V5)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsmedian_rpca5.txt", row.names=F, col.names=F, sep=" ")



rep <- loadFile("~/labmag/exps/playground/rep5/nsall/job.0.archive.stat")
behav <- colnames(rep)[grep("Behav_*",colnames(rep))]

red <- reduceData(unique(rep, by=behav), behav, method="rpca", k=100, kmax=100)
plotReduced2D(red)

write.table(red[,.(Index,V1,V2,V3)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsall_rpca3.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsall_rpca4.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4,V5)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsall_rpca5.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4,V5,V6,V7)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsall_rpca7.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4,V5,V6,V7,V8,V9,V10)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsall_rpca10.txt", row.names=F, col.names=F, sep=" ")



rep <- loadFile("~/labmag/exps/playground/rep5/nsas1/job.0.archive.stat")
behav <- colnames(rep)[grep("Behav_*",colnames(rep))]

red <- reduceData(unique(rep, by=behav), behav, method="rpca", k=length(behav), kmax=length(behav))
plotReduced2D(red)

write.table(red[,.(Index,V1,V2,V3)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsas1_rpca3.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsas1_rpca4.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4,V5)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsas1_rpca5.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4,V5,V6,V7)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsas1_rpca7.txt", row.names=F, col.names=F, sep=" ")


rep <- loadFile("~/labmag/exps/playground/rep5/nsas3/job.0.archive.stat")
behav <- colnames(rep)[grep("Behav_*",colnames(rep))]

red <- reduceData(unique(rep, by=behav), behav, method="rpca", k=length(behav), kmax=length(behav))
plotReduced2D(red)

write.table(red[,.(Index,V1,V2,V3)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsas3_rpca3.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsas3_rpca4.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4,V5)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsas3_rpca5.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4,V5,V6,V7)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsas3_rpca7.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4,V5,V6,V7,V8,V9,V10)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsas3_rpca10.txt", row.names=F, col.names=F, sep=" ")


# NS-ALL analysis

rep <- loadFile("~/exps/playground/rep5/nsall/job.0.archive.stat")
rep[, grep("Genome_*", colnames(rep)) := NULL]
rep[, Fitness := NULL]
rep[, Archive := NULL]

behav <- colnames(rep)[grep("Behav_*",colnames(rep))]
m <- melt(rep, measure.vars=behav)
setorder(m, Index)
m[, Sample := rep(1:100,each=5)]
m[, Feature := rep(paste0("Behav_",0:4),2000)]
cast <- dcast(m, formula=Index+Sample~Feature)

red <- reduceData(cast, vars=paste0("Behav_",0:4), method="rpca", k=2)
ggplot(red[Index > 1950], aes(x=V1, y=V2)) + geom_point(data=red[sample(.N,2000),!"Index",with=F], aes(V1,V2), colour="lightgray", size=1) +
  geom_point(shape=4, size=1.5, colour="red") + coord_fixed() + facet_wrap(~ Index)

rep2 <- loadFile("~/exps/playground/rep5/nsmedian/job.0.archive.stat")
red2 <- reduceData(rbind(cast,rep2,fill=T), vars=paste0("Behav_",0:4), method="rpca", k=2)

ggplot(red2[!is.na(Sample) & Index > 1950], aes(x=V1, y=V2)) + 
  geom_point(data=red2[is.na(Sample),!"Index",with=F], aes(V1,V2), colour="lightgray", size=1) +
  geom_point(shape=4, size=1.5, colour="red") + coord_fixed() + facet_wrap(~ Index)




setwd("~/exps/playground/tasks4")

run <- loadFile("~/exps/playground/tasks4/pl_avoidance_base/job.0.postbest.xml.stat")
rep <- loadFile("~/exps/playground/rep4/base/job.0.finalrep.stat")

r <- merge(run, rep, by.x="Primitive", by.y="Hash")
setnames(r, paste0("Behav_",0:4,".y"), paste0("Behav_",0:4))

run[, length(unique(Primitive)), by=.(Seed)]

mrun <- melt(r[, c("Seed","Time",paste0("Behav_",0:4)), with=F], id.vars=c("Seed","Time"))
ggplot(mrun, aes(variable, value, group=Time, colour=Time)) + geom_line(alpha=0.1) + facet_wrap(~ Seed)

mrep <- melt(rep[, c("Hash",paste0("Behav_",0:4)), with=F], id.vars="Hash")
ggplot(mrep, aes(variable, value, group=Hash)) + geom_line(alpha=0.1)


ggplot(mrun, aes(variable, value, group=Time)) + geom_line(data=mrep, alpha=0.1, aes(group=Hash)) + geom_line(alpha=0.1, colour="red") + facet_wrap(~ Seed)

setwd("~/exps/playground/tasks4")
fit <- loadData(c("direct*","*ns*","*base"), "postfitness.stat", fun=loadFitness, auto.ids.names=c("Domain","Task","Repo"))
fit[, Reference := mean(.SD[is.na(Repo) & Generation==max(Generation)]$BestSoFar), by=.(Task)]
fit[, RelativeFitness := (BestSoFar-Reference) / Reference]



setwd("~/exps/playground/tasks4")

fit <- loadData("*", "postfitness.stat", fun=loadFitness, auto.ids.names=c("Domain","Task","Repo"))
fit[, Reference := mean(.SD[is.na(Repo) & Generation==max(Generation)]$BestSoFar), by=.(Task)]
fit[, RelativeFitness := (BestSoFar-Reference) / Reference]

save(fit, file="~/Dropbox/WIP/fittasks4.rdata")

ggplot(fit[is.na(Subpop), .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(Task,Repo,Generation)], aes(Generation,Mean,group=Repo)) + 
  geom_line(aes(colour=Repo)) + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE, fill=Repo), alpha = 0.1) + ylab("Fitness")  + facet_wrap(~ Task, scales="free_y", ncol=2)

ggplot(lastGen(fit), aes(Repo,BestSoFar)) + geom_boxplot(aes(fill=Repo)) + facet_wrap(~ Task, scales="free_y", ncol=2)

sum <- lastGen(fit)[, .(Fitness=mean(RelativeFitness)), by=.(Repo, Task)]
ggplot(sum, aes(Repo,Fitness)) + geom_bar(aes(fill=Task), stat="identity")



sum <- lastGen(fit)[, .(RelativeFitness=mean(RelativeFitness),SE=se(RelativeFitness)), by=.(Task,Repo)]

ggplot(lastGen(fit)[is.na(Repo) | Repo=="base" | Repo=="ns"], aes(Task,BestSoFar)) + geom_boxplot(aes(fill=Repo))

ggplot(lastGen(fit)[is.na(Repo) | Repo=="base"], aes(Task,BestSoFar)) + geom_boxplot(aes(fill=Repo))
ggsave("~/Dropbox/WIP/boxplot.png", width=5, height=4)

ggplot(fit[is.na(Subpop) & (is.na(Repo) | Repo=="base"), .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(Task,Repo,Generation)], aes(Generation,Mean,group=Repo)) + 
  geom_line(aes(colour=Repo)) + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE, fill=Repo), alpha = 0.1) + ylab("Fitness")  + facet_wrap(~ Task, scales="free_y")
ggsave("~/Dropbox/WIP/lines.png", width=7, height=5)

groups <- list(BC=c("base","as"), Complexity=c("base","compslp","compmlp2","compmlp10"), Environment=c("base","envnoobs","envfixed","envmany"), Fitness=c("base","fitrandom","fitrecent"), Resolution=c("base","res02","res07","res10"), ReductionTS=c("base","basesam2","basesam3"), ReductionAS=c("as", "assam2","assam3"))
for(gname in names(groups)) {
  group <- groups[[gname]]
  g <- ggplot(sum[Repo %in% group], aes(Task,RelativeFitness,colour=Repo,group=Repo)) + geom_errorbar(aes(ymin=RelativeFitness-SE, ymax=RelativeFitness+SE),width=0.25) + geom_line() + geom_point(shape=21,fill="white") + ylim(NA,0) + ggtitle(gname)
  ggsave(paste0("~/Dropbox/WIP/",gname,".png"), width=5, height=4)
  print(g)
}

ggplot(lastGen(fit)[, .(RelativePerformance = mean(RelativeFitness)), by=.(Repo)], aes(Repo,RelativePerformance)) +
  geom_bar(position=position_dodge(), stat="identity")
ggsave("~/Dropbox/WIP/meanfitness.png", width=9, height=5)


mean <- lastGen(fit)[, .(Fitness=mean(BestSoFar)), by=.(Task,Repo)]
rank <- mean[, .(Repo,Rank=frankv(Fitness, order=-1)) , by=.(Task)]
ggplot(rank[Repo != "direct"], aes(Repo,Rank)) + geom_boxplot() + ylim(1,NA)
ggsave("~/Dropbox/WIP/rankfitness.png", width=9, height=5)


setwd("~/exps/playground/rep4")
data <- loadData("ns", "archive.stat", fun=loadFile)

r <- reduceData(data, paste0("Behav_",0:4), method="Rtsne", k=2)
plotReduced2D(r) + facet_wrap(~ Job)



setwd("~/labmag/exps/playground/rep4")

data <- loadData("ns100long", "archive.stat", fun=loadFile)
vars <- paste0("Behav_",0:499)

for(job in 0:3) {
  d <- unique(data[Job==job], by=vars)
  red <- reduceData(d, vars, method="sammon", k=2)
  g <- plotReduced2D(red)
  print(g)
}

data <- unique(data[Job==3], by=vars)

# Parallel coordinates
d <- data[, c("Index","Fitness",vars), with=F]
m <- melt(d, id.vars=c("Index","Fitness"))
ggplot(m, aes(variable, value, group=Index), alpha=0.1) + geom_line()

sammon2 <- reduceData(data, vars, method="sammon", k=2)
write.table(sammon2[,.(Index,V1,V2)], file="~/Dropbox/mase/src/mase/app/playground/rep/nssam2.txt", row.names=F, col.names=F, sep=" ")

sammon3 <- reduceData(data, vars, method="sammon", k=3)
write.table(sammon3[,.(Index,V1,V2,V3)], file="~/Dropbox/mase/src/mase/app/playground/rep/nssam3.txt", row.names=F, col.names=F, sep=" ")

sammon5 <- reduceData(data, vars, method="sammon", k=5)
write.table(sammon5[,.(Index,V1,V2,V3,V4,V5)], file="~/Dropbox/mase/src/mase/app/playground/rep/nslongsam5.txt", row.names=F, col.names=F, sep=" ")


data <- loadFile("as/job.0.finalrep.stat")
vars <- paste0("Behav_",0:8)

sammon2 <- reduceData(data, vars, method="sammon", k=2)
write.table(sammon2[,.(Hash,V1,V2)], file="~/Dropbox/mase/src/mase/app/playground/rep/assam2.txt", row.names=F, col.names=F, sep=" ")

sammon3 <- reduceData(data, vars, method="sammon", k=3)
write.table(sammon3[,.(Hash,V1,V2,V3)], file="~/Dropbox/mase/src/mase/app/playground/rep/assam3.txt", row.names=F, col.names=F, sep=" ")







setwd("~/exps/playground/rep3")


data <- loadData("base_*", "finalrep.stat", fun=loadFile, auto.ids.names=c("Env","Resolution"), jobs=0)

r <- reduceData(data, paste0("Behav_",0:4), method="sammon", k=2)
plotReduced2D(r, color.var="Fitness") + facet_wrap(~ Resolution)


setwd("~/exps/playground/rep2")

data <- loadFile("~/exps/testme/job.0.finalrep.stat")

data <- loadFile("sdbc/job.2.finalrep.stat")
vars <- paste0("Behav_",0:4)
gvars <- paste0("Genome_",0:51)

#data[, Variance := sqrt((1000 - Fitness) / 200)]

d <- rbind(cbind(Job=0, loadFile("sdbcl2var/job.0.finalrep.stat")),
           cbind(Job=1, loadFile("sdbcl2var/job.1.finalrep.stat")))

r <- reduceData(d, vars, method="Rtsne", k=2)
plotReduced2D(r, color.var="Fitness") + facet_wrap(~Job)
r <- reduceData(d, gvars, method="Rtsne", k=2)
plotReduced2D(r, color.var="Fitness") + facet_wrap(~Job)


plotVarsHist(data, vars)
plotVarsHist(data, vars, breaks=seq(-2,2,by=0.4))


normaliseCoords <- function(data) {(data - min(data))/(max(data)-min(data))}

ts2 <- tsne(dist(data[, vars, with=F]), k=2, epoch=10, max_iter=500)
ts2 <- normaliseCoords(ts2)
write.table(cbind(data[,.(Hash)], ts2), file="~/Dropbox/mase/src/mase/app/playground/rep/asl_tsne_2.txt", row.names=F, col.names=F, sep=" ")

ts3 <- tsne(dist(data[, vars, with=F]), k=3, epoch=10, max_iter=500)
ts3 <- normaliseCoords(ts3)
write.table(cbind(data[,.(Hash)], ts3), file="~/Dropbox/mase/src/mase/app/playground/rep/sdbcl_tsne_3.txt", row.names=F, col.names=F, sep=" ")

ts5 <- tsne(dist(data[, vars, with=F]), k=5, epoch=10, max_iter=500)
ts5 <- normaliseCoords(ts5)
write.table(cbind(data[,.(Hash)], ts5), file="~/Dropbox/mase/src/mase/app/playground/rep/as_tsne_5.txt", row.names=F, col.names=F, sep=" ")

sammon2 <- reduceData(data, vars, method="sammon", k=2)
write.table(sammon2[,.(Hash,V1,V2)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbcmedian_sam2.txt", row.names=F, col.names=F, sep=" ")

sammon3 <- reduceData(data, vars, method="sammon", k=3)
write.table(sammon3[,.(Hash,V1,V2,V3)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbcmedian_sam3.txt", row.names=F, col.names=F, sep=" ")


red <- reduceData(data, vars=vars, method="sammon", k=2)
plotReduced2D(red, color.var="Fitness")
red <- reduceData(data, vars=vars, method="tsne", k=2)
plotReduced2D(red, color.var="Fitness")
red <- reduceData(data, vars=vars, method="Rtsne", k=2)
plotReduced2D(red, color.var="Fitness")
red <- reduceData(data, vars=vars, method="pca", k=2)
plotReduced2D(red, color.var="Fitness")


d <- loadFile("rep/sdbcmeanhighres/job.0.finalrep.stat")
vars <- paste0("Behav_",0:4)
plotVarsHist(d, vars)
s <- sammonReduce(d, vars)
sammonPlot(s, "Fitness")

d <- loadFile("rep/sdbclast/job.0.finalrep.stat")
vars <- paste0("Behav_",0:9)
plotVarsHist(d, vars)
s <- sammonReduce(d, vars)
sammonPlot(s, "Fitness")


setwd("~/exps/playground/tasks2")

fit <- loadData("*", "postfitness.stat", fun=loadFitness, auto.ids.names=c("Method","Task"))

ggplot(fit[is.na(Subpop), .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(Task,Method,Generation)], aes(Generation,Mean,group=Method)) + 
  geom_line(aes(colour=Method)) + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE, fill=Method), alpha = 0.1) + ylab("Fitness")  + facet_wrap(~ Task, scales="free_y", ncol=2)

ggplot(lastGen(fit), aes(Method,BestSoFar)) + geom_boxplot(aes(fill=Method)) + facet_wrap(~ Task, scales="free_y", ncol=2)


setwd("~/labmag/exps/playground/tasks3")

fit <- loadData("*", "postfitness.stat", fun=loadFitness, auto.ids.names=c("BC","Task","Repo"))

ggplot(fit[is.na(Subpop), .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(Task,Repo,Generation)], aes(Generation,Mean,group=Repo)) + 
  geom_line(aes(colour=Repo)) + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE, fill=Repo), alpha = 0.1) + ylab("Fitness")  + facet_wrap(~ Task, scales="free_y", ncol=2)

ggplot(lastGen(fit), aes(Repo,BestSoFar)) + geom_boxplot(aes(fill=Repo)) + facet_wrap(~ Task, scales="free_y", ncol=2)



setwd("~/exps/playground/tasks")

fit <- loadData("**/*", "postfitness.stat", fun=loadFitness, auto.ids.names=c("Task","Repo","Reduction"))
fit[, Method := factor(paste(Repo,Reduction,sep="_"))]
fit[, Reference := mean(.SD[Repo=="direct" & Generation==max(Generation)]$BestSoFar), by=.(Task)]
fit[, RelativeFitness := (BestSoFar-Reference) / Reference]

ggplot(lastGen(fit), aes(Repo,RelativeFitness)) + geom_boxplot(aes(fill=Reduction))
ggplot(lastGen(fit)[, .(RelativePerformance = mean(RelativeFitness), SE=se(RelativeFitness)), by=.(Repo,Reduction)], aes(Repo,RelativePerformance,fill=Reduction)) +
  geom_bar(position=position_dodge(), stat="identity")

mean <- lastGen(fit)[, .(Fitness=mean(BestSoFar)), by=.(Task,Repo,Reduction,Method)]
rank <- mean[, .(Method,Repo,Reduction,Rank=frank(Fitness)) , by=.(Task)]

ggplot(rank, aes(Method,Rank)) + geom_boxplot()
ggplot(rank[Repo != "direct"], aes(Reduction,Rank)) + geom_boxplot() + facet_wrap(~ Repo)
ggplot(rank[Repo != "direct"], aes(Repo,Rank)) + geom_boxplot() + facet_wrap(~ Reduction)

mean <- lastGen(fit)[, .(Fitness=mean(BestSoFar)), by=.(Task,Repo,Reduction,Method)]


ggplot(lastGen(fit), aes(Repo,BestSoFar)) + geom_boxplot(aes(fill=Reduction)) + facet_wrap(~ Task, scales="free_y", ncol=2)


agg <- fit[is.na(Subpop), .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(Task,Method,Generation)]
ggplot(agg, aes(Generation,Mean,group=Method)) + geom_line(aes(colour=Method)) + facet_wrap(~Task, scales="free_y")

ggplot(lastGen(fit), aes(ID3,BestSoFar)) + geom_boxplot(aes(fill=ID2))
ggplot(lastGen(fit[ID2=="sdbc" | ID2=="sdbcl" | ID2=="sdbctsne3" | ID2=="sdbcltsne3"]), aes(ID3,BestSoFar)) + geom_boxplot(aes(fill=ID2))

metaAnalysis(lastGen(fit), BestSoFar ~ ID2, ~ ID3)
