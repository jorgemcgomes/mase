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

generateRandomCoords <- function(folder, jobs=0:9, k=3, srcdir="/home/jorge/Dropbox/mase/src/mase/app/maze/repf/", builddir="/home/jorge/Dropbox/mase/build/classes/mase/app/maze/repf/") {
  for(j in jobs) {
    d <- fread(paste0(folder,"/job.",j,".finalrep.stat"))
    rand <- data.table(Index=d$Hash)
    for(n in 1:k) {
      rand[[paste0("V",n)]] <- runif(nrow(rand))
    }
    write.table(rand, file=paste0(srcdir,"job.",j,".finalrep_k",k,".txt"), row.names=F, col.names=F, sep=" ")
    write.table(rand, file=paste0(builddir,"job.",j,".finalrep_k",k,".txt"), row.names=F, col.names=F, sep=" ")
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
processRepo("randmlp", jobs=0:9)

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

#fit <-loadData("tasks10/pl_maze_sdbc_nsneat_*_direct",  "postfitness.stat", fun=loadFitness, auto.ids.sep="_", auto.ids.names=c("Domain","Task","BC","Repo","RepoJob","Reduction"))
#refit <-loadData("tasks12/pl_maze_sdbc_nsneat_*_direct",  "postfitness.stat", fun=loadFitness, auto.ids.sep="_", auto.ids.names=c("Domain","Task","BC","Repo","RepoJob","Reduction"))
#joined <- rbind(cbind(fit,Version="original"), cbind(refit,Version="rerun"))
#metaAnalysis(lastGen(joined), BestSoFar ~ Version, ~ RepoJob)
#lastGen(joined)[, .(mean(BestSoFar)), by=.(RepoJob,Version)]


# DATA LOAD ######################

# load files
setwd("~/exps/playground")
fitraw <- loadData("tasks10/*", "fitness.stat", fun=loadFitness, auto.ids.sep="_", auto.ids.names=c("Domain","Task","BC","Repo","RepoJob","Reduction"))
fit <- loadData("tasks10/*", "postfitness.stat", fun=loadFitness, auto.ids.sep="_", auto.ids.names=c("Domain","Task","BC","Repo","RepoJob","Reduction"))
fit <- merge(fit, fitraw[, .(Generation,Job,Setup,RawMin=MinFitness,RawMean=MeanFitness,RawMax=MaxFitness,RawBest=BestSoFar)], by=c("Generation","Job","Setup"), all.x=T)
rm(fitraw) ; gc()

# fix data
fit <- fit[is.na(Reduction) | Reduction != "nonconstant"]
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

ract <- loadData("tasks10/pl_*_sdbc_rand_0_direct", "postbest.xml.stat", auto.ids.sep="_", auto.ids.names=c("Domain","Task","BC","Repo","RepoJob","Reduction"))
ract[, Task := factor(Task, levels=c("freeforaging","obsforaging","simplephototaxis","phototaxis","exploration","maze","avoidance","predator","dynphototaxis"), labels=c("Foraging","Foraging-O","Phototaxis","Phototaxis-O","Exploration","Maze","Avoidance","Prey","Tracking"))]
ract <- merge(ract, rep[Repo=="rand", c("Job","Index",vars,"V1","V2"), with=F], by.x=c("Primitive","RepoJob"), by.y=c("Index","Job"))

act <- rbind(act,ract) ; rm(ract)
setorder(act, Repo, RepoJob, Task, Job, Seed, Time)


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



# Repertoire comparison with the two randoms ####

newrep <- loadData("rep10/*", "archive.stat", auto.ids.names=c("NA","Repo"))
newrep <- newrep[Repo %in% c("nsneat","rand","randmlp")]
reduced <- reduceData(newrep, vars=vars, method="rpca", k=2)
reduced[, Repo := factor(Repo, levels=c("rand","randmlp","nsneat"), labels=c("Random-SLP","Random-MLP","NS-NEAT"))]

ggplot(reduced[Job==0], aes(x=V1, y=V2)) + geom_point(size=0.35, stroke=0) + coord_fixed() + labs(x="PC1", y="PC2") + facet_wrap(~ Repo) +
  scale_x_continuous(breaks=seq(0,1,by=0.2)) + scale_y_continuous(breaks=seq(0,1,by=0.2))
ggsave("~/Dropbox/Work/Papers/17-SWEVO/rep_comparison.png", width=4.7, height=1.25)


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
ggplot(f, aes(x=V1, y=V2)) + geom_point(stroke=0, size=0.35) + coord_fixed() + labs(x="PC1", y="PC2") + facet_wrap(~ id) +
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

subfit <- fit[Repo %in% c("nsneat","rand","randmlp",NA) & is.na(Reduction)]
subfit[is.na(Repo), Repo := "TR"]
subfit[, Repo := factor(Repo, levels=c("rand","randmlp","nsneat","TR"), labels=c("EvoRBC-II (R-SLP)","EvoRBC-II (R-MLP)","EvoRBC-II","Tabula-rasa"))]
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
sapply(m, function(x) x$ttest$holm[2,1]) # EvoRBC vs TR (mann-whitney, holm corrected)
sapply(m, function(x) x$ttest$holm[2,4]) # EvoRBC vs EvoRBC(R-MLP)
sapply(m, function(x) x$ttest$holm[3,2]) # EvoRBC vs EvoRBC(R-SLP)
sapply(m, function(x) x$ttest$holm[4,1]) # EvoRBC(R-MLP) vs TR
sapply(m, function(x) x$ttest$holm[3,1]) # EvoRBC(R-SLP) vs TR


metaAnalysis(lastGen(subfit)[,.(MeanFitness=mean(ScaledFitness)), by=.(Task,Repo)], MeanFitness ~ Repo, paired=T) # Paired tests

sapply(m, function(x) x$ttest$holm[2,1]) # EvoRBC vs TR (mann-whitney, holm corrected)
sapply(m, function(x) x$ttest$holm[2,4]) # EvoRBC vs EvoRBC(R2) (mann-whitney, holm corrected)
sapply(m, function(x) x$ttest$holm[1,4]) # EvoRBC(R2) vs TR (mann-whitney, holm corrected)


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
ggplot(act[RepoJob==0 & Repo=="nsneat"], aes(V1,V2,colour=Job)) + coord_fixed() +
  geom_point(data=rep[Repo=="nsneat" & Job==0], colour="lightgray", shape=16, size=.5) +
  geom_density2d(h=0.2,bins=5) + facet_wrap(~ Task, ncol=3) +
  theme(axis.text.x = element_blank(), axis.text.y = element_blank(), axis.ticks=element_blank()) + labs(x=NULL, y=NULL)

ggplot(act[RepoJob==0 & Repo=="nsneat"], aes(V1,V2)) + coord_fixed() +
  geom_point(data=rep[Repo=="nsneat" & Job==0], colour="lightgray", shape=16, size=.5) +
  stat_density2d(geom="polygon", h=0.2,bins=10, aes(fill=..level..), alpha=.6) + facet_wrap(~ Task, ncol=3) +
  scale_fill_distiller(palette="Spectral") + xlim(range(act$V1)+c(-0.05,0.05)) + ylim(range(act$V2)+c(-0.05,0.05)) +
  theme(axis.text.x = element_blank(), axis.text.y = element_blank(), axis.ticks=element_blank()) + labs(x=NULL, y=NULL, fill="Density")

ggsave("~/Dropbox/Work/Papers/17-SWEVO/rep_usage.png", width=4.7, height=4)

ggplot(act[RepoJob==0 & Repo=="rand"], aes(V1,V2)) + coord_fixed() +
  geom_point(data=rep[Repo=="rand" & Job==0], colour="lightgray", shape=16, size=.5) +
  stat_density2d(geom="polygon", h=0.2,bins=10, aes(fill=..level..), alpha=.6) + facet_wrap(~ Task, ncol=3) +
  scale_fill_distiller(palette="Spectral") + xlim(range(act$V1)+c(-0.05,0.05)) + ylim(range(act$V2)+c(-0.05,0.05)) +
  theme(axis.text.x = element_blank(), axis.text.y = element_blank(), axis.ticks=element_blank()) + labs(x=NULL, y=NULL, fill="Density")

ggsave("~/Dropbox/Work/Papers/17-SWEVO/rep_usage_rand.png", width=4.7, height=4)


ggplot(act[RepoJob==0], aes(V1,V2)) + coord_fixed() +
  geom_point(data=rep[Repo=="nsneat" & Job==0], colour="lightgray", shape=16, size=.5) +
  stat_density2d(geom="polygon", h=0.2,bins=20, aes(fill=..level..), alpha=.6) +
  scale_fill_distiller(palette="Spectral") + xlim(range(act$V1)+c(-0.05,0.05)) + ylim(range(act$V2)+c(-0.05,0.05)) +
  theme(axis.text.x = element_blank(), axis.text.y = element_blank(), axis.ticks=element_blank()) + labs(x=NULL, y=NULL, fill="Density")

ggsave("~/Dropbox/Work/Papers/17-SWEVO/rep_usage_all.png", width=3.5, height=2.5)


# Usage of the repertoire stats ############

aggregated <- act[Repo=="nsneat", .N, by=c("Task","Job",vars)]
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

act.stats <- act[Repo=="nsneat", .(Number=length(unique(Primitive)), 
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




setwd("~/exps/mazemw/")
fixPostFitness("~/exps/mazemw")

data <- loadData("**", "postfitness.stat" ,fun=loadFitness, auto.ids.sep="_", auto.ids.names=c("Method","MaxSize","LogicalLowerP","PrimitiveIfP","SelectorTerminalNonP"))
data[, LogicalLowerP := factor(LogicalLowerP, labels=c("0.2-0.6","0.1-0.8","0.05-0.9"))]
data[, PrimitiveIfP := factor(PrimitiveIfP, labels=c("0.5-0.5","0.25-0.75","0.75-0.25"))]
data[, SelectorTerminalNonP := factor(SelectorTerminalNonP, labels=c("0.5-0.5","0.25-0.75","0.75-0.25"))]
data[, Setup := paste0("S",MaxSize,"/L",LogicalLowerP,"/P",PrimitiveIfP,"/T",SelectorTerminalNonP)]

bests <- lastGen(data)[,.(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(Setup,MaxSize,LogicalLowerP,PrimitiveIfP,SelectorTerminalNonP)]

ggplot(bests[SelectorTerminalNonP=="0.5-0.5" & MaxSize=="25"], aes(LogicalLowerP,PrimitiveIfP)) + geom_tile(aes(fill=Mean))
ggplot(bests[LogicalLowerP=="0.2-0.6" & PrimitiveIfP=="0.25-0.75"], aes(MaxSize,SelectorTerminalNonP)) + geom_tile(aes(fill=Mean))


fitnessBoxplots(data)
bestSoFarFitnessEvals(data)
rankByFitness(data)

koza <- loadData("**", "koza.stat" ,fun=loadFile, auto.ids.sep="_", auto.ids.names=c("Method","MaxSize","LogicalLowerP","PrimitiveIfP","SelectorTerminalNonP"))
koza[, LogicalLowerP := factor(LogicalLowerP, labels=c("0.2-0.6","0.1-0.8","0.05-0.9"))]
koza[, PrimitiveIfP := factor(PrimitiveIfP, labels=c("0.5-0.5","0.25-0.75","0.75-0.25"))]
koza[, SelectorTerminalNonP := factor(SelectorTerminalNonP, labels=c("0.5-0.5","0.25-0.75","0.75-0.25"))]
koza[, Setup := paste0("S",MaxSize,"/L",LogicalLowerP,"/P",PrimitiveIfP,"/T",SelectorTerminalNonP)]

avgsizes <- lastGen(koza)[,.(Mean=mean(V3),SE=se(V3)), by=.(Setup,MaxSize,LogicalLowerP,PrimitiveIfP,SelectorTerminalNonP)]
ggplot(avgsizes,aes(Setup,Mean)) + geom_bar(stat="identity")

#ggplot(koza, aes(V1,V7,group=Setup,colour=Setup)) + stat_summary(fun.y="mean", geom="line") + ggtitle("Average size in gen")
#ggplot(koza, aes(V1,V8,group=Setup,colour=Setup)) + stat_summary(fun.y="mean", geom="line") + ggtitle("Average size so far")
#ggplot(koza, aes(V1,V9,group=Setup,colour=Setup)) + stat_summary(fun.y="mean", geom="line") + ggtitle("Size of best in gen")
#ggplot(koza, aes(V1,V10,group=Setup,colour=Setup)) + stat_summary(fun.y="mean", geom="line") + ggtitle("Size of best so far")


#ggplot(koza[, .(Size=mean(V5)), by=.(V1,Setup)], aes(V1,Size,group=Setup,colour=Setup)) + geom_line()



setwd("~/exps/mazemw3/")
#fixPostFitness("~/labmag/exps/mazemw/")
setwd("~/Dropbox/gpresults")
load("fitness_data.rdata")
load("koza.rdata")

data <- loadData("**", "postfitness.stat" ,fun=loadFitness, auto.ids.sep="_", auto.ids.names=c("Method","MaxDepth","MaxSize","Parsimony","Selector"))
data[, Parsimony := factor(Parsimony, labels=c("Yes","No"))]
data[, Selector := factor(Selector, labels=c("0.5","0.25","0.75"))] # prob. of selecting terminal
data[, Selector := relevel(Selector, "0.25")]
data[, Setup := paste0("D",MaxDepth,"/S",MaxSize,"/P",Parsimony,"/T",Selector)]
save(data, file="~/Dropbox/gpresults/fitness_data.rdata")

bests <- lastGen(data)[,.(Mean=mean(BestSoFar)), by=.(Setup,MaxDepth,MaxSize,Parsimony,Selector)]
ggplot(bests, aes(MaxDepth,MaxSize)) + geom_tile(aes(fill=Mean)) + facet_grid(Parsimony ~ Selector) + scale_fill_distiller(palette="Spectral")
ggsave("fitness.svg")

fitnessBoxplots(data)
bestSoFarFitnessEvals(data)
rankByFitness(data)

koza <- loadData("**", "koza.stat" ,fun=loadFile, auto.ids.sep="_", auto.ids.names=c("Method","MaxDepth","MaxSize","Parsimony","Selector"))
koza[, Parsimony := factor(Parsimony, labels=c("Yes","No"))]
koza[, Selector := factor(Selector, labels=c("0.5","0.25","0.75"))] # prob. of selecting terminal
koza[, Selector := relevel(Selector, "0.25")]
koza[, Setup := paste0("D",MaxDepth,"/S",MaxSize,"/P",Parsimony,"/T",Selector)]
save(koza, file="~/Dropbox/gpresults/koza.rdata")

kozastats <- lastGen(koza)[,.(AverageSize=mean(V41),BestSize=mean(V43)), by=.(Setup,MaxDepth,MaxSize,Parsimony,Selector)]
ggplot(kozastats, aes(MaxDepth,MaxSize)) + geom_tile(aes(fill=AverageSize)) + facet_grid(Parsimony ~ Selector) + scale_fill_distiller(palette="Spectral")
ggsave("avg_size.svg")
ggplot(kozastats, aes(MaxDepth,MaxSize)) + geom_tile(aes(fill=BestSize)) + facet_grid(Parsimony ~ Selector) + scale_fill_distiller(palette="Spectral")
ggsave("best_size.svg")

kozastats2 <- koza[,.(Depth=mean(V5),PrimitivesT=mean(V39),Ifs=mean(V25)), by=.(Setup,MaxDepth,MaxSize,Parsimony,Selector)]
ggplot(kozastats2, aes(MaxDepth,MaxSize)) + geom_tile(aes(fill=Depth)) + facet_grid(Parsimony ~ Selector) + scale_fill_distiller(palette="Spectral")
ggsave("avg_depth.svg")
ggplot(kozastats2, aes(MaxDepth,MaxSize)) + geom_tile(aes(fill=PrimitivesT)) + facet_grid(Parsimony ~ Selector) + scale_fill_distiller(palette="Spectral")
ggsave("primitive_terminals.svg")

nodenames <- as.character(koza[1, seq(10,38,2), with=F])
setnames(koza, seq(11,39,2), nodenames)
beststats <- koza[Setup=="D10/S10/PYes/T0.75", c("Job","V1",nodenames), with=F]
m <- melt(beststats, id.vars=c("Job","V1"), variable.name="Function",value.name="Count")
means <- m[, .(Count=mean(Count)), by=.(Job,Function)][, .(MeanCount=mean(Count),SE=se(Count)), by=.(Function)]


#, lapply(.SD, mean) , by=.(Job), .SDcols=paste0("V",seq(11,39,2))]
setwd("~/exps/mazemw3/")
data <- loadData("**", "postfitness.stat" ,fun=loadFitness)
fitnessBoxplots(data)
bestSoFarFitness(data)
rankByFitness(data)


setwd("~/exps/mazemw4/")
#fixPostFitness("~/exps/mazemw")

data <- loadData("**", "postfitness.stat" ,fun=loadFitness, auto.ids.sep="_", auto.ids.names=c("Task","FS","BuildSize","MaxDepth","MaxSize"))
data[, MaxDepth := factor(MaxDepth, labels=c("5","10","17"))]
data[, MaxSize := factor(MaxSize, labels=c("25","50","100","250"))]
data[, Setup := paste0(FS,"/B",BuildSize,"/D",MaxDepth,"/S",MaxSize)]

rankByFitness(data)

bests <- lastGen(data)[,.(Mean=mean(BestSoFar)), by=.(Setup,FS,BuildSize,MaxDepth,MaxSize)]
ggplot(bests, aes(MaxDepth,MaxSize)) + geom_tile(aes(fill=Mean)) + facet_grid(FS ~ BuildSize) + 
  scale_fill_distiller(palette="Spectral") + geom_label(aes(label=round(Mean,2)), size=3) + ggtitle("Best fitness")
ggsave("~/Dropbox/gpresults/mw4_fitness.svg")

koza <- loadData("**", "koza.stat" ,fun=loadFile, auto.ids.sep="_", auto.ids.names=c("Task","FS","BuildSize","MaxDepth","MaxSize"))
koza[, MaxDepth := factor(MaxDepth, labels=c("5","10","17"))]
koza[, MaxSize := factor(MaxSize, labels=c("25","50","100","250"))]
koza[, Setup := paste0(FS,"/B",BuildSize,"/D",MaxDepth,"/S",MaxSize)]

kozastats <- koza[,.(Depth=mean(V5),Size=mean(V8)), by=.(Setup,FS,BuildSize,MaxDepth,MaxSize)]
ggplot(kozastats, aes(MaxDepth,MaxSize)) + geom_tile(aes(fill=Depth)) + facet_grid(FS ~ BuildSize) + 
  scale_fill_distiller(palette="Spectral") + geom_label(aes(label=round(Depth,1)), size=3) + ggtitle("Average depth")
ggsave("~/Dropbox/gpresults/mw4_avgdepth.svg")
ggplot(kozastats, aes(MaxDepth,MaxSize)) + geom_tile(aes(fill=Size)) + facet_grid(FS ~ BuildSize) + 
  scale_fill_distiller(palette="Spectral") + geom_label(aes(label=round(Size,1)), size=3) + ggtitle("Average size")
ggsave("~/Dropbox/gpresults/mw4_avgsize.svg")


kozastats2 <- rbind(lastGen(koza)[FS=="gp",.(BestSize=mean(V43)), by=.(Setup,FS,BuildSize,MaxDepth,MaxSize)],
                   lastGen(koza)[FS=="gptrees",.(BestSize=mean(V37)), by=.(Setup,FS,BuildSize,MaxDepth,MaxSize)])
ggplot(kozastats2, aes(MaxDepth,MaxSize)) + geom_tile(aes(fill=BestSize)) + facet_grid(FS ~ BuildSize) + 
  scale_fill_distiller(palette="Spectral") + geom_label(aes(label=round(BestSize,1)), size=3) + ggtitle("Best size")
ggsave("~/Dropbox/gpresults/mw4_bestsize.svg")





setwd("~/exps/mazemw9/")
fixPostFitness("~/exps/mazemw9")

data <- loadData("**", "postfitness.stat" ,fun=loadFitness, auto.ids.names=c("Method"))
data[!is.na(Repo), Job := paste0(Repo,".",Job)]
data[, Setup := Method]

fitnessBoxplots(data)
rankByFitness(data)
bestSoFarFitnessEvals(data)


koza <- loadData("**", "koza.stat" ,fun=loadFile, auto.ids.sep="_", auto.ids.names=c("Method"))
ggplot(koza, aes(Generation,MeanSize)) + stat_summary(fun.y=mean, geom="line")

m <- melt(koza[, c("Job","Generation", colnames(koza)[grep("f\\..*", colnames(koza))]), with=F], id.vars=c("Job","Generation") )
ggplot(m, aes(Generation,value, colour=variable)) + stat_summary(fun.y=mean, geom="line")


koza[!is.na(Repo), Job := paste0(Repo,".",Job)]
koza[, Setup := Method]

kozastats <- koza[,.(Depth=mean(MeanDepth),Size=mean(MeanSize)), by=.(Method)]

setwd("~/exps/mazemw10/")
data <- loadData("**", "postfitness.stat" ,fun=loadFitness, auto.ids.names=c("Method","ERCMut","PrimitiveSD"))
rankByFitness(data)
bests <- lastGen(data)[,.(Mean=mean(BestSoFar)), by=.(ERCMut,PrimitiveSD)]
ggplot(bests, aes(ERCMut,PrimitiveSD)) + geom_tile(aes(fill=Mean)) + 
  scale_fill_distiller(palette="Spectral") + geom_label(aes(label=round(Mean,2)), size=3) + ggtitle("Best fitness")





setwd("~/labmag/exps/mazemw6/")

data <- loadData("**", "postfitness.stat" ,fun=loadFitness, auto.ids.names=c("Method","PrimitiveSD","ConstantSD"))
bests <- lastGen(data)[,.(Mean=mean(BestSoFar)), by=.(Setup,PrimitiveSD,ConstantSD)]
ggplot(bests, aes(PrimitiveSD,ConstantSD)) + geom_tile(aes(fill=Mean)) + 
  scale_fill_distiller(palette="Spectral") + geom_label(aes(label=round(Mean,2)), size=3) + ggtitle("Best fitness")

rankByFitness(data)



bests <- lastGen(data)[,.(Mean=mean(BestSoFar)), by=.(Setup,FS,BuildSize,MaxDepth,MaxSize)]
ggplot(bests, aes(MaxDepth,MaxSize)) + geom_tile(aes(fill=Mean)) + facet_grid(FS ~ BuildSize) + 
  scale_fill_distiller(palette="Spectral") + geom_label(aes(label=round(Mean,2)), size=3) + ggtitle("Best fitness")
ggsave("~/Dropbox/gpresults/mw4_fitness.svg")

koza <- loadData("**", "koza.stat" ,fun=loadFile, auto.ids.sep="_", auto.ids.names=c("Task","FS","BuildSize","MaxDepth","MaxSize"))
koza[, MaxDepth := factor(MaxDepth, labels=c("5","10","17"))]
koza[, MaxSize := factor(MaxSize, labels=c("25","50","100","250"))]
koza[, Setup := paste0(FS,"/B",BuildSize,"/D",MaxDepth,"/S",MaxSize)]

kozastats <- koza[,.(Depth=mean(V5),Size=mean(V8)), by=.(Setup,FS,BuildSize,MaxDepth,MaxSize)]
ggplot(kozastats, aes(MaxDepth,MaxSize)) + geom_tile(aes(fill=Depth)) + facet_grid(FS ~ BuildSize) + 
  scale_fill_distiller(palette="Spectral") + geom_label(aes(label=round(Depth,1)), size=3) + ggtitle("Average depth")
ggsave("~/Dropbox/gpresults/mw4_avgdepth.svg")
ggplot(kozastats, aes(MaxDepth,MaxSize)) + geom_tile(aes(fill=Size)) + facet_grid(FS ~ BuildSize) + 
  scale_fill_distiller(palette="Spectral") + geom_label(aes(label=round(Size,1)), size=3) + ggtitle("Average size")
ggsave("~/Dropbox/gpresults/mw4_avgsize.svg")


kozastats2 <- rbind(lastGen(koza)[FS=="gp",.(BestSize=mean(V43)), by=.(Setup,FS,BuildSize,MaxDepth,MaxSize)],
                    lastGen(koza)[FS=="gptrees",.(BestSize=mean(V37)), by=.(Setup,FS,BuildSize,MaxDepth,MaxSize)])
ggplot(kozastats2, aes(MaxDepth,MaxSize)) + geom_tile(aes(fill=BestSize)) + facet_grid(FS ~ BuildSize) + 
  scale_fill_distiller(palette="Spectral") + geom_label(aes(label=round(BestSize,1)), size=3) + ggtitle("Best size")
ggsave("~/Dropbox/gpresults/mw4_bestsize.svg")




##############################

setwd("~/exps/mazemw12/")
fixPostFitness("~/exps/mazemw12")

data <- loadData("**", "postfitness.stat" ,fun=loadFitness, auto.ids.names=c("Method","Setting","Mut"))
rankByFitness(data[Method=="gpg" & Setting=="ercmut"])
rankByFitness(data[Method=="gpt" & Setting=="ercmut"])
rankByFitness(data[Method=="gpg" & Setting=="sd"])
rankByFitness(data[Method=="gpt" & Setting=="sd"])

data <- loadData("**", "fitness.stat" ,fun=loadFitness, auto.ids.names=c("Method","Setting","Mut"))
data[, Gap := MaxFitness - MeanFitness]
ggplot(data, aes(Generation,Gap, group=Mut, colour=Mut)) + stat_summary(fun.y=mean, geom="line") + facet_grid(Setting ~ Method)



setwd("~/exps/mazemw11/")
data <- loadData("gp*", "fitness.stat" ,fun=loadFitness, auto.ids.names=c("Method","Repo"))
data[, Gap := MaxFitness - MeanFitness]
ggplot(data, aes(Generation,Gap, group=Method, colour=Method)) + stat_summary(fun.y=mean, geom="line")


setwd("~/exps/mazemw11/")
fixPostFitness("~/exps/mazemw11")

data <- loadData("**", "postfitness.stat" ,fun=loadFitness, auto.ids.names=c("Method","Repo"))

fitnessBoxplots(data, xvar="Method")
fitnessViolins(data, xvar="Method")
bestSoFarFitnessEvals(data, xvar="Method")
rankByFitness(data, xvar="Method")

koza <- loadData("**", "koza.stat" ,fun=loadFile, auto.ids.sep="_", auto.ids.names=c("Method","Repo"))

ggplot(koza, aes(Generation,MeanSize, group=Method, colour=Method)) + stat_summary(fun.y=mean, geom="line")
ggplot(koza, aes(Generation,BestSizeGen, group=Method, colour=Method)) + stat_summary(fun.y=mean, geom="line")
ggplot(koza, aes(Generation,MeanDepth, group=Method, colour=Method)) + stat_summary(fun.y=mean, geom="line") 

m <- melt(koza[, c("Generation","Method", colnames(koza)[grep("f\\..*", colnames(koza))]), with=F], id.vars=c("Generation","Method") )
ggplot(m, aes(Generation,value, colour=variable)) + stat_summary(fun.y=mean, geom="line") + facet_wrap(~ Method)
ggplot(m, aes(variable,value)) + stat_summary(fun.y=mean, geom="bar") + ylab("Mean number of nodes") + xlab("Node type") + facet_wrap(~ Method)

setwd("~/exps/mazemw13/")
data <- loadData("**", "postfitness.stat" ,fun=loadFitness, auto.ids.names=c("Method","Mut"))
rankByFitness(data)

setwd("~/exps/mazemw14/")
data <- loadData("**", "postfitness.stat" ,fun=loadFitness, auto.ids.names=c("Method","CrossoverP","SelectorTP","IfElseP"))
data[, CrossoverP := factor(CrossoverP, levels=c("3","2","0","1"), labels=c("0.1","0.3","0.5","0.7"))]
data[, SelectorTP := factor(SelectorTP, levels=c("1","0","2"), labels=c("0.25","0.5","0.75"))]
data[, Conf := paste0(Method,"/C",CrossoverP,"/ST",SelectorTP)]
rankByFitness(data, xvar="Conf")

means <- lastGen(data)[, .(Fitness=mean(BestSoFar),SE=se(BestSoFar)), by=.(Method,CrossoverP,SelectorTP)]
ggplot(means, aes(CrossoverP,Fitness,colour=SelectorTP,group=SelectorTP)) + geom_point() + geom_line() +
  geom_errorbar(aes(ymin=Fitness-SE, ymax=Fitness+SE), width=.25) + facet_wrap(~ Method)

metaAnalysis(lastGen(data), BestSoFar ~ CrossoverP, ~ Method)
metaAnalysis(lastGen(data), BestSoFar ~ SelectorTP, ~ Method)

setwd("~/exps/mazesub/")
data <- loadData("**", "postfitness.stat" ,fun=loadFitness, auto.ids.names=c("Method","NumPrimitives"))
fitnessBoxplots(data)





########## MAZE FINAL ##############################

setwd("~/exps/mazefinal")
fixPostFitness("~/exps/mazefinal")
data <- loadData("**", "postfitness.stat" ,fun=loadFitness, auto.ids.names=c("Method","Repo","Param"))
data[, Param := factor(formatC(as.numeric(as.character(Param)), width=2, flag="0"))]
data[, Blind := grepl("blind", Method)]
data[, Method := sub("blind", "", Method)]
data[, Method := factor(Method, levels=c("tr","rbcneat","rbcsub","rbcselected","gptreeslopr"), labels=c("TR","NEAT-Vanilla","NEAT-Sub","NEAT-SubSel","GP-DT"))]
data[, Config := paste(Method,Blind,Param,sep="-")]
data[, Config := sub("-NA","",sub("TRUE","B",sub("-FALSE","",Config)))]
#gsub("NA, ","",dff$string)


fit_scale <- scale_y_continuous(limits=c(0,1.25),breaks=seq(0,1.25,0.25))

# NEAT-Sub
fitnessBoxplots(data[Blind==F & Method=="NEAT-Sub"], xvar="Param") + labs(x="Number of primitives") + guides(fill=F) + ylab("Best fitness") + 
  ggtitle("Performance of NEAT-Sub for different number of primitives allowed") + fit_scale
metaAnalysis(lastGen(data)[Blind==F & Method=="NEAT-Sub"], BestSoFar ~ Param)
bestSoFarFitnessEvals(data[Blind==F & Method=="NEAT-Sub"], xvar="Param") + labs(colour="Number of primitives", fill="Number of primitives") +
  ggtitle("Performance of NEAT-Sub for different number of primitives allowed") + fit_scale

# All comparison
fitnessBoxplots(data[Blind==F & (Method!="NEAT-Sub" | Param=="15")], xvar="Config") + ylab("Best fitness") + fit_scale
metaAnalysis(lastGen(data)[Blind==F & (Method!="NEAT-Sub" | Param=="15")], BestSoFar ~ Config)
bestSoFarFitnessEvals(data[Blind==F & (Method!="NEAT-Sub" | Param=="15")], xvar="Config") + fit_scale

# Blind Vanilla
fitnessBoxplots(data[Blind==T & Method=="NEAT-Vanilla"], xvar="Param") + labs(x="Number of dimensions") + guides(fill=F) + ylab("Best fitness") + 
  ggtitle("Performance of NEAT-Vanilla according to the dimensionality of the random BC") + fit_scale
metaAnalysis(lastGen(data)[Blind==T & Method=="NEAT-Vanilla"], BestSoFar ~ Param)

# Blind subset
fitnessBoxplots(data[Blind==T & Method=="NEAT-Sub"], xvar="Param") + labs(x="Number of primitives") + guides(fill=F) + ylab("Best fitness") + 
  ggtitle("Performance of NEAT-Sub (BLIND) for different number of primitives allowed")
metaAnalysis(lastGen(data)[Blind==T & Method=="NEAT-Sub"], BestSoFar ~ Param)

# Blind comparison
subset <- data[(Method=="NEAT-Vanilla" & (Param=="03" | Param=="NA")) | (Method=="NEAT-Sub" & Param=="15") | !(Method %in% c("NEAT-Vanilla","NEAT-Sub"))]
ggplot(lastGen(subset), aes(Method,BestSoFar,fill=Blind)) + geom_boxplot() + 
  geom_point(position=position_jitterdodge(jitter.width=0.15, jitter.height=0), size=.5, colour="gray") +
  ylab("Best fitness") + ggtitle("Performance of the methods when using or not the BC") + fit_scale

m <- metaAnalysis(lastGen(subset[Method!="TR"]), BestSoFar ~ Blind, ~ Method) # performance degradation
sapply(m, function(x){as.numeric(x$ttest$holm[1,2])})

metaAnalysis(lastGen(subset[Blind==T | Method=="TR"]), BestSoFar ~ Method) # best blind

# Repo variability
d <- data[Blind==FALSE & Method != "TR" & (Method!="NEAT-Sub" | Param=="15")]
fitnessBoxplots(d, xvar="Repo") + facet_wrap(~ Config) + guides(fill=F) + fit_scale

# for each method, does the repertoire have a significant impact?
m <- metaAnalysis(lastGen(d),  BestSoFar ~ Repo, ~ Config)
sapply(m, function(x){x$ttest$kruskal$p.value})

# does any repertoire yield consistently superior results accross all methods?
means <- lastGen(d)[, .(Mean=mean(BestSoFar)), by=.(Repo,Config)]
friedman.test(Mean ~ Repo | Config, data=means)


# best solutions from each method
d <- lastGen(data)[Repo=="0" & Blind==F, .(Setup,Config,Job,BestSoFar)] # use only repo 0 and not blind
setorder(d, Config, -BestSoFar)
View(d)

# GP analysis
koza <- loadData("**", "koza.stat" ,fun=loadFile, auto.ids.sep="_", auto.ids.names=c("Method","Repo","Param"))
koza[, Blind := grepl("blind", Method)]
koza[, Method := sub("blind", "", Method)]
koza[, Method := factor(Method, levels=c("gptreeslo","gpgraphlo","gptreeslopr","gpgraphlopr"), labels=c("GP-DT","GP-DG","GP-DT(P)","GP-DG(P)"))]
koza[, Config := paste(Method,Blind,sep="-")]
koza[, Config := sub("TRUE","B",sub("-FALSE","",Config))]

ggplot(koza[Blind==F], aes(Generation,MeanSize, group=Config, colour=Config)) + stat_summary(fun.y=mean, geom="line") + ggtitle("Size")
ggplot(koza[Blind==F], aes(Generation,BestSizeGen, group=Config, colour=Config)) + stat_summary(fun.y=mean, geom="line")
ggplot(koza[Blind==F], aes(Generation,MeanDepth, group=Config, colour=Config)) + stat_summary(fun.y=mean, geom="line") + ggtitle("Depth")

m <- melt(koza[Blind==F, c("Generation","Config", colnames(koza)[grep("f\\..*", colnames(koza))]), with=F], id.vars=c("Generation","Config") )
#ggplot(m, aes(Generation,value, colour=variable)) + stat_summary(fun.y=mean, geom="line") + facet_wrap(~ Config)
ggplot(m, aes(variable,value)) + stat_summary(fun.y=mean, geom="bar") + ylab("Mean number of nodes") + xlab("Node type") + 
  facet_wrap(~ Config, scales="free_x", ncol=1) + ggtitle("Node types")


# search for the smallest best
k <- lastGen(koza[Blind==F & Repo=="0", .(Setup,Config,Job,BestSizeSoFar,BestFitnessSoFar)])
setorder(k, Config, BestSizeSoFar)
View(k)


repo <- loadFile("~/exps/mazefinal/rep/job.0.finalrep.stat")
beststats <- loadData("**", "postbest.xml.stat" ,fun=loadFile, auto.ids.sep="_", auto.ids.names=c("Method","Repo","Param"))
beststats[, Param := factor(formatC(as.numeric(as.character(Param)), width=2, flag="0"))]
beststats[, Blind := grepl("blind", Method)]
beststats[, Method := sub("blind", "", Method)]
beststats[, Method := factor(Method, levels=c("tr","rbcneat","rbcsub","gptreeslopr"), labels=c("TR","NEAT-Vanilla","NEAT-Sub","GP-DT"))]
beststats[, Config := paste(Method,Blind,Param,sep="-")]
beststats[, Config := sub("-NA","",sub("TRUE","B",sub("-FALSE","",Config)))]

beststats <- merge(beststats, repo[,.(Hash,Bin_0,Bin_1)], by.x="Primitive",by.y="Hash")
setorder(beststats, Config, Job, Seed, Repetition, Time)
set(beststats, j=c(grep("ArbitratorOut_|SensorInput_|ActuatorOut_",colnames(beststats))), value=NULL )

act.stats <- beststats[, .(Number=length(unique(Primitive)), Duration=mean(rle(Primitive)$lengths)), by=.(Config,Job,Repetition)]
act.sum <- act.stats[, .(Number = mean(Number), Duration=mean(Duration)), by=.(Config,Job)][, .(NumberMean=mean(Number),NumberSE=se(Number),DurationMean=mean(Duration),DurationSE=se(Duration)), by=.(Config)]

ggplot(beststats[Blind==FALSE & (Method!="NEAT-Sub" | Param=="15"),.N,.(Bin_0,Bin_1,Config)]) + geom_tile(aes(Bin_0, Bin_1,fill=N)) + facet_wrap(~ Config) + coord_fixed()
ggplot(beststats[Blind==FALSE & (Method!="NEAT-Sub" | Param=="15"),.N,.(Bin_0,Bin_1,Config,Job)]) + geom_tile(aes(Bin_0, Bin_1,fill=N)) + facet_grid(Config ~ Job) + coord_fixed()

# most used by NEAT-Vanilla
most.used <- beststats[Config=="NEAT-Vanilla", .N, by=.(Primitive,Bin_0,Bin_1)]
setorder(most.used, -N)
View(most.used)
paste(most.used$N[1:30],collapse=",")
