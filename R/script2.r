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
processRepo("base", jobs=0:9)
processRepo("rand", jobs=0:9)
processRepo("few", jobs=0:9)
processRepo("noobs", jobs=0:9)
processRepo("noobj", jobs=0:9)
processRepo("fixed", jobs=0:9)
processRepo("none", jobs=0:9)


setwd("~/exps/playground/rep11")
processRepo("diffdrive", jobs=0)

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

# load files
setwd("~/exps/playground")
fitraw <- loadData("tasks10/*", "fitness.stat", fun=loadFitness, auto.ids.sep="_", auto.ids.names=c("Domain","Task","BC","Repo","RepoJob","Reduction"))
fit <- loadData("tasks10/*", "postfitness.stat", fun=loadFitness, auto.ids.sep="_", auto.ids.names=c("Domain","Task","BC","Repo","RepoJob","Reduction"))
fit <- merge(fit, fitraw[, .(Generation,Job,Setup,RawMin=MinFitness,RawMean=MeanFitness,RawMax=MaxFitness,RawBest=BestSoFar)], by=c("Generation","Job","Setup"), all.x=T)
rm(fitraw) ; gc()

# fix data
fit <- fit[Reduction != "nonconstant"]
fit[, ScaledFitness := (BestSoFar - min(RawMin, na.rm=T)) / (max(BestSoFar)-min(RawMin,na.rm=T)) , by=.(Task)]
fit[, Task := factor(Task, levels=c("freeforaging","obsforaging","dynforaging","simplephototaxis","phototaxis","exploration","maze","avoidance","predator","dynphototaxis"), labels=c("Foraging","Foraging-O","Foraging-D","Phototaxis","Phototaxis-O","Exploration","Maze","Avoidance","Prey","Tracking"))]
fit <- fit[Task != "Foraging-D"]
fit[Reduction=="direct", Reduction := NA]

# convenience definitions
vars <- paste0("Behav_", 0:6)
options(digits = 5, scipen=10)

# repertoire data (already reduced)
load("finalreps.rdata")
load("baserep.rdata")

# activation data
act <- loadData("tasks10/pl_*_sdbc_nsneat_0_direct", "postbest.xml.stat", auto.ids.sep="_", auto.ids.names=c("Domain","Task","BC","Repo","RepoJob","Reduction"))
act[, Task := factor(Task, levels=c("freeforaging","obsforaging","simplephototaxis","phototaxis","exploration","maze","avoidance","predator","dynphototaxis"), labels=c("Foraging","Foraging-O","Phototaxis","Phototaxis-O","Exploration","Maze","Avoidance","Prey","Tracking"))]
act <- merge(act, rep[Repo=="nsneat", c("Job","Index",vars,"V1","V2"), with=F], by.x=c("Primitive","RepoJob"), by.y=c("Index","Job"))
setorder(act, RepoJob, Task, Job, Seed, Time)


# Repertoire dimensionality reduction (one time only) ################

rep <- loadData("rep10/*", "archive.stat", auto.ids.names=c("NA","Repo"))
hrep <- loadFile("~/exps/playground/rep10/nsneat/job.0.behaviours.stat", colnames=c("Generation","Subpop","Index",vars,"Fitness"))

# Only fair to compare those that were evolved with the same environment conditions
toreduce <- rbind(rep[Repo=="nsneat" | Repo=="rand"], hrep, fill=T)
toreduce <- reduceData(toreduce, vars=vars, method="rpca", k=2)

rep <- merge(rep, toreduce[is.na(Generation),.(Repo,Job,Index,V1,V2)], by=c("Repo","Job","Index"), all.x=T)
hrep <- merge(hrep, toreduce[!is.na(Generation),.(Generation,Index,V1,V2)], by=c("Generation","Index"), all.x=T)

plotReduced2D(rep) + facet_grid(Repo ~ Job)

save(rep, file="finalreps.rdata")
save(hrep, file="baserep.rdata")



# Repertoire inspection ##############

# Behaviour space exploration through gens
hrep.plus <- rbind(hrep, rep[Repo=="nsneat" & Job==0], fill=T)
hrep.plus[, Generation := Generation + 1]
gens <- c(1,5,10,25,50,100,250,500)

flist <- lapply(gens, function(g){hrep.plus[Generation<=g]})
names(flist) <- paste("Gen",gens)
flist[["Repertoire"]] <- hrep.plus[is.na(Generation)]
f <- rbindlist(flist, idcol="id")
f[, id := factor(id, levels=names(flist))]
ggplot(f, aes(x=V1, y=V2)) + geom_point(shape=20, size=.2) + coord_fixed() + labs(x="PC1", y="PC2") + facet_wrap(~ id) +
  scale_x_continuous(breaks=seq(0,1,by=0.2)) + scale_y_continuous(breaks=seq(0,1,by=0.2))
ggsave("~/Dropbox/Work/Papers/17-SWEVO/rep_evolution.png", width=4.7, height=3.2)


# Plot region codes

require(cluster)
fullcircle <- function(center=c(0,0), r=1, npoints=100){
  tt <- seq(0,2*pi,length.out = npoints)
  return(data.frame(x = center[1] + r * cos(tt), y = center[2] + r * sin(tt)))
}
circleSector <- function(center=c(0,0), r=1, fromangle=0, toangle=0, npoints=50) {
  tt <- seq(fromangle,toangle,length.out = npoints)
  curve <- data.frame(x = center[1] + r * cos(tt), y = center[2] + r * sin(tt))
  curve <- rbind(center, curve, center)
  return(curve)
}
circlescale <- function(centers, r=0.05) {cbind(rbindlist(apply(centers[,.(V1,V2)], 1, circleFun, r=r)), Index=rep(centers$Index, each=100))}

repbase <- rep[Repo=="nsneat" & Job==0]
cluster <- clara(repbase[,.(V1,V2)], k=25, samples=50, sampsize=500, pamLike=T) 
centers <- repbase[cluster$i.med, c("Index","V1","V2",vars), with=F]

m <- melt(centers, measure.vars=vars)
m[, variable := factor(variable, labels=c("Walls-Dist","Obstacle-Mean-Dist","Obstacle-Closest-Dist","Object-Mean-Dist","Object-Closest-Dist","Linear-Speed","Turn-Speed"))]
m[, angle := ((as.numeric(variable) - 1) / length(levels(variable))) * -2 * pi + pi/2]
m[, rad := (value - min(value)) / (max(value)-min(value)) , by=.(variable)] # normalize each var to [0,1]
polydata <- m[, circleSector(center=c(V1,V2), r=0.05*rad, fromangle=angle-pi/length(vars), toangle=angle+pi/length(vars)), by=.(Index,variable)]

ggplot(polydata, aes(x, y)) + 
  geom_point(data=repbase, aes(V1,V2), colour="lightgray", shape=16, size=.5) +
  geom_polygon(aes(fill=variable,group=paste(Index,variable))) +
  geom_path(data=circlesDF(centers, r=0.05), aes(group=Index), size=.25) +
  geom_spoke(data=m, aes(V1,V2,angle=angle+pi/length(vars), radius=0.05), size=.25) +
  guides(fill=guide_legend(nrow=3)) + coord_fixed() + labs(x="PC1",y="PC2",fill="Feature")

ggsave("~/Dropbox/Work/Papers/17-SWEVO/rep_codes.pdf", width=4, height=3.2)


ggplot(centers, aes(V1, V2)) + 
  geom_point(data=repbase, colour="lightgray", shape=16, size=.5) +
  geom_label(aes(label=Index), size=2) + coord_fixed() + labs(colour=NULL, x="PC1",y="PC2")

ggsave("~/Dropbox/Work/Papers/17-SWEVO/rep_codes_index.pdf", width=4, height=2.5)


# Base comparison ##################

subfit <- fit[Repo %in% c("nsneat","rand",NA) & is.na(Reduction)]
subfit[, Repo := factor(Repo, levels=c("nsneat","rand"), labels=c("EvoRBC","EvoRBC(R)"))]
subfit[is.na(Repo), Repo := "Tabula-rasa"]
sum <- lastGen(subfit)[, .(Fitness=mean(ScaledFitness), SE=se(ScaledFitness)), by=.(Task, Repo)]
sum <- rbind(sum, sum[, .(Task="Average",Fitness=mean(Fitness),SE=se(Fitness)), by=.(Repo)])

# Barplots
ggplot(sum, aes(Task, Fitness,fill=Repo)) + geom_bar(stat="identity", position="dodge") + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.5, size=.25, position=position_dodge(.9)) +
  labs(y="Scaled fitness",fill="Method") +
  theme(axis.text.x = element_text(angle = 22.5, hjust = 1)) 

ggsave("~/Dropbox/Work/Papers/17-SWEVO/base_comparison.pdf", width=4.7, height=3)

# Boxplots
ggplot(lastGen(subfit), aes(Task,ScaledFitness,colour=Repo)) + geom_boxplot(outlier.size=1) + 
  geom_vline(xintercept=seq(1.5,9.5,by=1), size=.25) + ylim(0,1)

# Statistical tests
m <- metaAnalysis(lastGen(subfit), ScaledFitness ~ Repo, ~ Task)
sapply(m, function(x) x$ttest$holm[1,2]) # EvoRBC vs TR (mann-whitney, holm corrected)
sapply(m, function(x) x$ttest$holm[3,2]) # EvoRBC vs EvoRBC(R) (mann-whitney, holm corrected)
metaAnalysis(lastGen(subfit)[,.(MeanFitness=mean(ScaledFitness)), by=.(Task,Repo)], MeanFitness ~ Repo, paired=T) # Paired tests

# Best fitness through generations
genmean <- subfit[Repo!="EvoRBC(R)", .(Fitness=mean(ScaledFitness), SE=se(ScaledFitness)), by=.(Generation,Task,Repo)]
ggplot(genmean, aes(Generation, Fitness, group=Repo)) + geom_ribbon(aes(ymin=Fitness-SE,ymax=Fitness+SE,fill=Repo), alpha=.15) +
  geom_line(aes(colour=Repo)) + facet_wrap(~ Task, scales="free_y", ncol=3) + labs(y="Scaled Fitness", colour="Method", fill="Method")

ggsave("~/Dropbox/Work/Papers/17-SWEVO/base_comparison_gens.pdf", width=4.7, height=4)



# Repo variability (base) ################

# behaviour space coverage comparison
ggplot(rep[Repo=="nsneat"], aes(x=V1, y=V2)) + geom_point(shape=20, size=.5) + facet_wrap(~ Job, ncol=3) + coord_fixed() + 
  theme(axis.text.x = element_blank(), axis.text.y = element_blank(), axis.ticks=element_blank()) + labs(x=NULL, y=NULL)

ggsave("~/Dropbox/Work/Papers/17-SWEVO/repo_variability.png", width=4.7, height=4)


# which primitives belong to a bin that has not been visited by the other repo under comparison, and vice versa
bred[, Bin := do.call(paste, lapply(.SD/.5, round)), by=.(Job,Index), .SDcols=vars] # assign bins by discretizing BC
visited <- unique(bred, by=c("Job","Bin")) # remove repeated
rarity <- visited[, .(Rarity=10-.N), by=.(Bin)]
bred <- merge(bred, rarity, by="Bin")
ggplot(bred, aes(x=V1, y=V2, colour=Rarity)) + geom_point(shape=16, size=1) + facet_wrap(~ Job) +
  coord_fixed() + theme(axis.text.x = element_blank(), axis.text.y = element_blank(), axis.ticks=element_blank()) + labs(x=NULL, y=NULL) +
  ggtitle("Behaviour space coverage (t-SNE)") + scale_color_distiller(palette="Spectral")

# fitness comparison
subfit <- fit[Repo=="nsneat" & is.na(Reduction)]
m <- metaAnalysis(lastGen(subfit), ScaledFitness ~ RepoJob, ~Task) 
sapply(m, function(x) x$ttest$kruskal$p.value) # Kruskall-Wallis task-by-task
metaAnalysis(lastGen(subfit)[,.(MeanFitness=mean(ScaledFitness)), by=.(Task,RepoJob)], MeanFitness ~ RepoJob, paired=T) # Paired test

# Boxplots
ggplot(lastGen(subfit), aes(Task,ScaledFitness,colour=RepoJob)) + geom_boxplot(outlier.size=.25, size=.25) + 
  geom_vline(xintercept=seq(1.5,9.5,by=1), size=.25) + labs(y="Scaled fitness", colour="Repertoire")
ggsave("~/Dropbox/Work/Papers/17-SWEVO/repo_variability_box.pdf", width=4.7, height=3)

# line plots
ggplot(lastGen(subfit)[,.(MeanFitness=mean(ScaledFitness)), by=.(Task,RepoJob)], aes(Task,MeanFitness,colour=RepoJob,group=RepoJob)) + 
  geom_line() + geom_label(aes(label=RepoJob), size=2, label.padding=unit(0.1,"lines")) + labs(y="Scaled fitness") + guides(colour=F)
ggsave("~/Dropbox/Work/Papers/17-SWEVO/repo_variability_lines.pdf", width=4.7, height=3)

# stacked barplot (useless)
ggplot(lastGen(fit[(Repo=="nsneat" | is.na(Repo)) & is.na(Reduction)])[, .(Fitness=mean(ScaledFitness)), by=.(Task, RepoJob)], aes(RepoJob, Fitness,fill=Task)) + 
  geom_bar(stat="identity") + 
  labs(y="Scaled fitness",fill="Method") +
  theme(axis.text.x = element_text(angle = 22.5, hjust = 1)) 



# Usage of the repertoire visualization ################

#https://stackoverflow.com/questions/21193138/transparency-and-alpha-levels-for-ggplot2-stat-density2d-with-maps-and-layers-in
ggplot(act[RepoJob==0], aes(V1,V2,colour=Job)) + coord_fixed() +
  geom_point(data=rep[Repo=="nsneat" & Job==0], colour="lightgray", shape=16, size=.5) +
  geom_density2d(h=0.2,bins=5) + facet_wrap(~ Task, ncol=3) +
  theme(axis.text.x = element_blank(), axis.text.y = element_blank(), axis.ticks=element_blank()) + labs(x=NULL, y=NULL)

ggplot(act[RepoJob==0], aes(V1,V2)) + coord_fixed() +
  geom_point(data=rep[Repo=="nsneat" & Job==0], colour="lightgray", shape=16, size=.5) +
  stat_density2d(geom="polygon", h=0.2,bins=10, aes(fill=..level..), alpha=.6) + facet_wrap(~ Task, ncol=3) +
  scale_fill_distiller(palette="Spectral") + xlim(range(act$V1)+c(-0.05,0.05)) + ylim(range(act$V2)+c(-0.05,0.05)) +
  theme(axis.text.x = element_blank(), axis.text.y = element_blank(), axis.ticks=element_blank()) + labs(x=NULL, y=NULL, fill="Density")

ggsave("~/Dropbox/Work/Papers/17-SWEVO/rep_usage.png", width=4.7, height=4)

ggplot(act[RepoJob==0], aes(V1,V2)) + coord_fixed() +
  geom_point(data=rep[Repo=="nsneat" & Job==0], colour="lightgray", shape=16, size=.5) +
  stat_density2d(geom="polygon", h=0.2,bins=20, aes(fill=..level..), alpha=.6) +
  scale_fill_distiller(palette="Spectral") + xlim(range(act$V1)+c(-0.05,0.05)) + ylim(range(act$V2)+c(-0.05,0.05)) +
  theme(axis.text.x = element_blank(), axis.text.y = element_blank(), axis.ticks=element_blank()) + labs(x=NULL, y=NULL, fill="Density")

ggsave("~/Dropbox/Work/Papers/17-SWEVO/rep_usage_all.png", width=3.5, height=2.5)


# Usage of the repertoire stats ############

aggregated <- act[, .N, by=c("Task","Job",vars)]
d <- function(a, b) {
    dists <- pdist(a[,-"N"], b[,-"N"])@dist
    w <- rep(a$N, each=nrow(b)) * rep(b$N, nrow(a))
    if(identical(a,b)) {
      w[seq(from=1,to=length(dists),by=nrow(a)+1)] <- 0
    }
    meandist <- sum((dists * w)) / sum(as.numeric((w)))
    return(meandist)    
}

# Intra-solution distance
intrasol <- aggregated[, d(.SD,.SD), by=.(Task,Job)]
intrasol[, .(Mean=mean(V1),SD=sd(V1)), by=.(Task) ]

# Inter-run distance
aux <- function(x){
  s <- split(x, by="Job",keep.by=F)
  o <- outer(s, s, Vectorize(d))
  return(o[lower.tri(o)])
}
interrun <- aggregated[, aux(.SD), by=.(Task)]
interrun[, .(Mean=mean(V1),SD=sd(V1)), by=.(Task) ]

# Inter-task distance
aggregated2 <- act[, .N, by=c("Task",vars)]
tasksplit <- split(aggregated2, by="Task",keep.by=F)
intertask <- outer(tasksplit, tasksplit, Vectorize(d))
intertask[lower.tri(intertask)] <- NA

interd <- as.data.table(intertask, keep.rownames=T)
interd <- melt(interd, id.vars="rn")
ggplot(interd, aes(factor(rn,levels=levels(fit$Task)),factor(variable,levels=levels(fit$Task)))) + geom_tile(aes(fill=value)) + geom_label(aes(label=round(value,digits=2)),label.size=0.1,size=2) +
  scale_fill_distiller(type="seq", palette="YlOrRd", direction=1, na.value="white") + coord_fixed() +
  theme(axis.text.x = element_text(angle = 22.5, hjust = 1)) + labs(x="Task",y="Task",fill="Mean distance")
ggsave("~/Dropbox/Work/Papers/17-SWEVO/usage_distance.pdf", width=4, height=4)

m <- mds(as.dist(intertask), ndim=2, type="ordinal")
ggplot(as.data.table(m$conf, keep.rownames=T), aes(D1,D2)) + geom_point() + geom_text(aes(label=rn), vjust="bottom") + coord_fixed()


# Environment #####################

subfit <- fit[is.na(Reduction) & Repo %in% c("nsneat","fixed","noobj","noobs","none")]
subfit[, Repo := factor(Repo, levels=c("nsneat","fixed","noobs","noobj","none"), labels=c("Base","Fixed","No-obstacles","No-POIs","Only-walls"))]
sum <- lastGen(subfit)[, .(Fitness=mean(ScaledFitness), SE=se(ScaledFitness)), by=.(Task, Repo)]
sum <- rbind(sum, sum[, .(Task="Average",Fitness=mean(Fitness),SE=se(Fitness)), by=.(Repo)])

ggplot(sum, aes(Task, Fitness, fill=Repo)) + geom_bar(stat="identity", position="dodge") + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.5, position=position_dodge(.9), size=.25) +
  labs(y="Scaled fitness", fill="Repertoire evo. environment") +
  theme(axis.text.x = element_text(angle = 22.5, hjust = 1)) 

ggsave("~/Dropbox/Work/Papers/17-SWEVO/environments.pdf", width=4.7, height=3)


m <- metaAnalysis(lastGen(subfit)[!is.na(Repo)], ScaledFitness ~ Repo, ~Task)
sapply(m, function(x) x$ttest$kruskal$p.value)
sapply(m, function(x) x$ttest$holm[1,5]) # base vs fixed
sapply(m, function(x) x$ttest$holm[1,4]) # base vs no-obstacles
sapply(m, function(x) x$ttest$holm[1,3]) # base vs no-poi
sapply(m, function(x) x$ttest$holm[1,2]) # base vs only-walls
sapply(m, function(x) x$ttest$holm[2,3]) # no-poi vs only-walls

# paired comparison without tabula-rasa
metaAnalysis(lastGen(subfit)[!is.na(Repo),.(MeanFitness=mean(ScaledFitness)), by=.(Task,Repo)], MeanFitness ~ Repo, paired=T)



# Ignore constant dimensions ##################

subfit <- fit[(Reduction=="nonconstant" | is.na(Reduction)) & Repo %in% c("noobj","noobs","none") & RepoJob==0]
sum <- lastGen(subfit)[, .(Fitness=mean(ScaledFitness), SE=se(ScaledFitness)), by=.(Task, Repo, Reduction)]
sum <- rbind(sum, sum[, .(Task="Average",Fitness=mean(Fitness),SE=se(Fitness)), by=.(Repo, Reduction)])

ggplot(sum, aes(Task, Fitness, fill=Reduction)) + geom_bar(stat="identity", position="dodge") + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.5, position=position_dodge(.9), size=.25) +
  labs(y="Scaled fitness", fill="Repertoire evo. environment") + facet_wrap(~ Repo) +
  theme(axis.text.x = element_text(angle = 22.5, hjust = 1)) 

m <- metaAnalysis(lastGen(subfit), ScaledFitness ~ Reduction, ~ Task + Repo)
sapply(m, function(x) x$ttest$holm[1,2])



# Activation stats ##############

act.stats <- act[, .(Number=length(unique(Primitive)), 
                     Duration=mean(rle(Primitive)$lengths), 
                     MaxDuration=max(rle(Primitive)$lengths),
                     Used=sum(sapply(.SD[, paste0("ArbitratorOut_",0:6), with=F], sd) > 0.10, na.rm=T)),
                 by=.(Seed, Job, Task)]

act.stats[, .(Number=sprintf("& %.2f & (%.1f) &", mean(Number), sd(Number)), Duration=sprintf("%.2f & (%.1f) &", mean(Duration), sd(Duration)), Dimensions=sprintf("%.2f & (%.1f) \\", mean(Used), sd(Used))), by=.(Task)]

metaAnalysis(act.stats, Number ~ Task)

act.sum <- act.stats[, .(Used = mean(Used)), by=.(Task,Job)][, .(Mean=mean(Used),SE=se(Used)), by=.(Task)]
ggplot(act.sum, aes(Task, Mean)) + geom_bar(stat="identity") +
  geom_errorbar(aes(ymin=Mean-SE,ymax=Mean+SE), width=.5) + ylab("Mean number of outputs varied (SD > 0.1)") + 
  scale_y_continuous(breaks=0:7, limits=c(0,7)) + theme(axis.text.x = element_text(angle = 22.5, hjust = 1)) 
ggsave("~/Dropbox/Work/Papers/17-SWEVO/act_outputs_varied.pdf", width=4, height=2.5)

act.sum <- act.stats[, .(Number = mean(Number)), by=.(Task,Job)][, .(Mean=mean(Number),SE=se(Number)), by=.(Task)]
ggplot(act.sum, aes(Task, Mean)) + geom_bar(stat="identity") +
  geom_errorbar(aes(ymin=Mean-SE,ymax=Mean+SE), width=.5) + ylab("Mean number of primitives used (out of 5000)") +
  theme(axis.text.x = element_text(angle = 22.5, hjust = 1))
ggsave("~/Dropbox/Work/Papers/17-SWEVO/act_primitives_used.pdf", width=4, height=2.5)

act.sum <- act.stats[, .(Duration = mean(Duration)), by=.(Task,Job)][, .(Mean=mean(Duration),SE=se(Duration)), by=.(Task)]
ggplot(act.sum, aes(Task, Mean)) + geom_bar(stat="identity") +
  geom_errorbar(aes(ymin=Mean-SE,ymax=Mean+SE), width=.5) + ylab("Number of consecutive steps in each primitive") +
  theme(axis.text.x = element_text(angle = 22.5, hjust = 1))
ggsave("~/Dropbox/Work/Papers/17-SWEVO/act_duration.pdf", width=4, height=2.5)



# Transitions ############

diff <- function(df) {
  cat(".")
  return(c(NA, parSapply(cl=NULL, 2:nrow(df), function(i){euclideanDist(df[i], df[i-1])})))
}
createCluster() ; clusterExport(NULL, "euclideanDist")
act[, Jump := diff(.SD), by=.(Seed,Job,Task), .SDcols=vars]

dists <- parDist(as.matrix(unique(rep[Repo=="nsneat" & Job==0, vars,with=F])), diag=F, upper=T)
dists <- as.matrix(dists)
diag(dists) <- NA
closestD <- mean(apply(dists, 1, min, na.rm=T))
allD <- mean(apply(dists, 1, mean, na.rm=T))
awayD <- mean(apply(dists, 1, max, na.rm=T))

ggplot(act[Jump>0], aes(Jump)) + geom_density(alpha=1, linetype="blank", fill="black") + facet_wrap(~ Task, scales="free_y", ncol=3) + 
  geom_vline(xintercept=c(closestD,allD,awayD), linetype="dashed", size=.25) + 
  ylab("Frequency") + xlab("Jump size in behaviour space")
ggsave("~/Dropbox/Work/Papers/17-SWEVO/act_jumps.pdf", width=4.7, height=4)




# Dimensionality reduction ###############

subfit <- fit[Repo=="nsneat" & RepoJob==0 & (grepl("mds*",Reduction) | is.na(Reduction))]
subfit[, Reduction := factor(Reduction, labels=paste0("k=",1:5))]
subfit[is.na(Reduction), Reduction := "None"]
sum <- lastGen(subfit)[, .(Fitness=mean(ScaledFitness), SE=se(ScaledFitness)), by=.(Task, Reduction)]
sum <- rbind(sum, sum[, .(Task="Average",Fitness=mean(Fitness),SE=se(Fitness)), by=.(Reduction)])

ggplot(sum, aes(Task, Fitness, fill=Reduction)) + geom_bar(stat="identity", position="dodge") + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.5, size=.25, position=position_dodge(.9)) +
  theme(axis.text.x = element_text(angle = 22.5, hjust = 1)) + labs(fill="Dimensionality reduction", y="Scaled fitness")

ggsave("~/Dropbox/Work/Papers/17-SWEVO/reduction.pdf", width=4.7, height=3)

subfit[is.na(Reduction), Reduction := "None"]
m <- metaAnalysis(lastGen(subfit)[Reduction!="k=1" & Reduction!="k=2"], ScaledFitness ~ Reduction , ~ Task)
sapply(m, function(x) x$ttest$kruskal$p.value)
m <- metaAnalysis(lastGen(subfit)[Reduction!="k=1" & Reduction!="k=2" & Reduction != "k=3"], ScaledFitness ~ Reduction , ~ Task)
sapply(m, function(x) x$ttest$kruskal$p.value)

metaAnalysis(lastGen(subfit)[,.(MeanFitness=mean(ScaledFitness)), by=.(Task,Reduction)], MeanFitness ~ Reduction, paired=T)


# R² fit

baserep <- rep[Repo=="nsneat" & Job==0]
setorder(baserep, Index)
originalDists <- Dist(baserep[,vars,with=F], diag=T, upper=T)

rsquarefit <- function(x) {
  f <- fread(paste0("~/Dropbox/mase/src/mase/app/playground/rep/sdbc_nsneat_0_",x,".txt"))
  setorder(f,V1)
  d <- Dist(f[,-"V1"], diag=T, upper=T)
  return(cor(c(d), c(originalDists))^2)
}
fits <- data.table(Reduction=paste0("mds",1:5))
fits[, Rsquared := rsquarefit(Reduction), by=.(Reduction)]
fits[, Reduction := factor(Reduction, labels=paste0("k=",1:5))]
ggplot(fits, aes(Reduction,Rsquared)) + geom_point() + geom_line(aes(group=0)) + ylab("Coefficient of determination R²")

ggsave("~/Dropbox/Work/Papers/17-SWEVO/reduction_fit.pdf", width=3.5, height=2.5)



# Repertoire size reduction ###########

subfit <- fit[Repo=="nsneat" & RepoJob==0 & (grepl("pam*",Reduction) | is.na(Reduction))]
subfit[, Reduction := factor(Reduction, levels=c("pam50","pam100","pam500","pam1000","pam2500"), labels=c("size=50","size=100","size=500","size=1000","size=2500"))]
subfit[is.na(Reduction), Reduction := "size=5000"]
sum <- lastGen(subfit)[, .(Fitness=mean(ScaledFitness), SE=se(ScaledFitness)), by=.(Task, Reduction)]
sum <- rbind(sum, sum[, .(Task="Average",Fitness=mean(Fitness),SE=se(Fitness)), by=.(Reduction)])

ggplot(sum, aes(Task, Fitness, fill=Reduction)) + geom_bar(stat="identity", position="dodge") + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.5, size=.25, position=position_dodge(.9)) +
  theme(axis.text.x = element_text(angle = 22.5, hjust = 1)) + labs(y="Scaled fitness", fill="Size reduction")
ggsave("~/Dropbox/Work/Papers/17-SWEVO/size.pdf", width=4.7, height=3)

subfit[is.na(Reduction), Reduction := "none"]
m <- metaAnalysis(lastGen(subfit), BestSoFar ~ Reduction , ~ Task)
sapply(m, function(x) x$ttest$kruskal$p.value)
m <- metaAnalysis(lastGen(subfit[Reduction!="pam50" & Reduction!="pam100"]), BestSoFar ~ Reduction , ~ Task)
sapply(m, function(x) x$ttest$kruskal$p.value)

metaAnalysis(lastGen(subfit)[,.(MeanFitness=mean(ScaledFitness)), by=.(Task,Reduction)], MeanFitness ~ Reduction, paired=T)


# View reduction

pams <- paste0("pam",c(50,100,500,1000,2500))
all <- lapply(paste0("~/Dropbox/mase/src/mase/app/playground/rep/sdbc_nsneat_0_",pams,".txt"), fread, col.names=c("Index",vars))
names(all) <- pams
all <- rbindlist(all,idcol="Reduction")

baserep <- rep[Repo=="nsneat" & Job==0, .(Index,V1,V2)]
all <- merge(all, baserep, by="Index")
all <- rbind(all, cbind(baserep, Reduction="All"), fill=T)
all[, Reduction := factor(Reduction, levels=c("pam50","pam100","pam500","pam1000","pam2500","All"))]

ggplot(all[Reduction!="All"], aes(V1, V2)) + 
  geom_point(data=all[Reduction=="All",.(V1,V2)], shape=16, size=.5, colour="lightgray") +
  geom_point(shape=4, size=.5, colour="red") + 
  facet_wrap(~ Reduction, ncol=2) + coord_fixed() + 
  theme(axis.text.x = element_blank(), axis.text.y = element_blank(), axis.ticks=element_blank()) + labs(x=NULL, y=NULL)

ggsave("~/Dropbox/Work/Papers/17-SWEVO/size_viz.png", width=4, height=4)


# Generalisation

basefit <- loadData("tasks10/*", "postfitness.stat", fun=loadFitness, auto.ids.sep="_", auto.ids.names=c("Domain","Task","BC","Repo","RepoJob","Reduction"))
basefit <- basefit[(is.na(Repo) | (Repo=="nsneat" & Reduction=="direct" & RepoJob==0)) & Task != "dynforaging"]
v1fit <- loadData("tasks10/*", "var1fitness.stat", fun=loadFitness, auto.ids.sep="_", auto.ids.names=c("Domain","Task","BC","Repo","RepoJob","Reduction"))
v2fit <- loadData("tasks10/*", "var2fitness.stat", fun=loadFitness, auto.ids.sep="_", auto.ids.names=c("Domain","Task","BC","Repo","RepoJob","Reduction"))

allfits <- rbind(cbind(basefit,Variant="Base"), cbind(v1fit,Variant="Var1"), cbind(v2fit,Variant="Var2"))


# Barplots
sum <- allfits[Generation==249, .(Fitness=mean(BestSoFar),SE=se(BestSoFar)), by=.(Repo,Task,Variant)]
ggplot(sum[is.na(Repo)], aes(Task, Fitness,fill=Variant)) + geom_bar(stat="identity", position="dodge") + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.5, size=.25, position=position_dodge(.9)) +
  theme(axis.text.x = element_text(angle = 22.5, hjust = 1)) 

ggplot(sum, aes(paste(Task,Variant), Fitness,fill=Repo)) + geom_bar(stat="identity", position="dodge") + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.5, size=.25, position=position_dodge(.9)) +
  theme(axis.text.x = element_text(angle = 22.5, hjust = 1)) 




