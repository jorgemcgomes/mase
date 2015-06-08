setwd("~/exps/EC/pred")
theme_set(theme_bw())
DEF_HEIGHT=3
DEF_WIDTH=4.5

diversity.group <- function(datalist) {
  pb <- txtProgressBar(min=1, max=length(datalist) * length(datalist[[1]]$jobs), style=3)
  index <- 1
  setlist <- list()
  cl <- makeCluster(8)
  clusterEvalQ(cl, library(pdist))
  for(data in datalist) {
    for(job in data$jobs) {
      frame <- data.frame()
      for(sub in data$subpops) {
        frame <- rbind(frame, subset(data[[job]][[sub]], select=data$vars.group))
      }
      v <- meanDists(frame, cl)
      setlist[[data$expname]] <- c(setlist[[data$expname]], v)
      index <- index + 1
      setTxtProgressBar(pb, index)
    }
  }
  stopCluster(cl)
  close(pb)
  return(metaAnalysis(setlist))
}

diversity.ind <- function(datalist) {
  pb <- txtProgressBar(min=1, max=length(datalist) * length(datalist[[1]]$jobs) * length(datalist[[1]]$subpops) , style=3)
  index <- 1
  setlist <- list()
  cl <- makeCluster(8)
  clusterEvalQ(cl, library(pdist))
  for(data in datalist) {
    for(job in data$jobs) {
      for(sub in data$subpops) {
        frame <- subset(data[[job]][[sub]], select=data$vars.ind)
        v <- meanDists(frame, cl)
        setlist[[data$expname]] <- c(setlist[[data$expname]], v)
        index <- index + 1
        setTxtProgressBar(pb, index)
      }
    }
  }
  stopCluster(cl)
  close(pb)
  return(metaAnalysis(setlist))  
}

meanDists <- function(data, cl) {
  data <- as.matrix(data)
  aux <- function(index) {
    d <- pdist(data, indices.A=index, indices.B=((index+1):nrow(data)))
    return(sum(attr(d, "dist")))
  }
  indexes <- sample.int(nrow(data)-1)
  dists <- parSapply(cl, indexes, aux)
  return(sum(dists) / (nrow(data) * nrow(data) / 2))
}

# threshold = distance / (diagonal / 2) , diagonal/2 = 141.42/2 = 70.71
countNear <- function(folder, subpops, threshold, mode="best") {
  files <- list.files(folder,pattern="rebehaviours.stat", full.names=T)
  jobs <- c()
  for(file in files) {
    if(mode=="best") {
      jobs <- c(jobs, countNearBest(file,subpops,threshold))
    } else if(mode=="mean") {
      jobs <- c(jobs, countNearMean(file,subpops,threshold))
    }
  }
  return(jobs)
}

countNearMean <- function(file, subpops, threshold) {
  tab <- read.table(file, header=F, sep=" ",fill=T)
  count <- c()
  for(r in 1:nrow(tab)) {
    near <- 0
    for(i in 0:(subpops-1)) {
      if(tab[r,11+i*4] < threshold) {
        near <- near + 1
      } 
    }
    count <- c(count, near)
  }
  return(mean(count))
}

countNearBest <- function(file, subpops, threshold) {
  tab <- read.table(file, header=F, sep=" ",fill=T)
  best <- which.max(tab[,4])
  near <- 0
  for(i in 0:(subpops-1)) {
    if(tab[best,11+i*4] < threshold) {
      near <- near + 1
    } 
  }
  return(near)
}

################### ECJ 2nd SUBMISSION #######################################################3

setwd("~/exps/EC/pred")

###### task difficulty - fitness ######################################

data <- metaLoadData("fit_e4","fit_e7","fit_e10","fit_e13", names=c("V4","V7","V10","V13"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F))
fullStatistics(data, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit",fit.comp.par=list(snapshots=c(500),jitter=F,ylim=T))

###### number of collaborators x setup - fitness #######################

data0 <- metaLoadData("fit_e4_p2","fit_e4","fit_e7","fit_e10","fit_e13", names=c("V4/2", "V4","V7","V10","V13"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F))
data2 <- metaLoadData("fit_e4_p2_r2","fit_e4_r2","fit_e7_r2","fit_e10_r2","fit_e13_r2", names=c("V4/2", "V4","V7","V10","V13"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F))
data5 <- metaLoadData("fit_e4_p2_r5","fit_e4_r5","fit_e7_r5","fit_e10_r5","fit_e13_r5", names=c("V4/2", "V4","V7","V10","V13"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F))
data10 <- metaLoadData("fit_e4_p2_r10","fit_e4_r10","fit_e7_r10","fit_e10_r10","fit_e13_r10", names=c("V4/2", "V4","V7","V10","V13"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F))

frame <- cbind(fitnessSummary(data0), N="0")
frame <- rbind(frame, cbind(fitnessSummary(data2), N="2"))
frame <- rbind(frame, cbind(fitnessSummary(data5), N="5"))
frame <- rbind(frame, cbind(fitnessSummary(data10), N="10"))
frame$method <- factor(frame$method, c("V4/2","V4","V7","V10","V13"))

pd <- position_dodge(.1) # move them .05 to the left and right
ggplot(frame, aes(x=N, y=mean, colour=method)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.25, position=pd) +
  geom_line(position=pd,aes(group=method)) + ylim(c(0,2)) +
  geom_point(position=pd) + ylab("Highest team performance") + theme(legend.title=element_blank())

##### TEAM CONVERGENCE -- COLLABORATIONS ###############################

data <- list()
data[["V4/2"]] <- metaLoadData("fit_e4_p2","fit_e4_p2_r2","fit_e4_p2_r5","fit_e4_p2_r10", names=c("Fit-N0","Fit-N2","Fit-N5","Fit-N10"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=T, behavs.bests=T, behavs.sample=1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data[["V4"]] <- metaLoadData("fit_e4","fit_e4_r2","fit_e4_r5","fit_e4_r10", names=c("Fit-N0","Fit-N2","Fit-N5","Fit-N10"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=T, behavs.bests=T, behavs.sample=1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data[["V7"]] <- metaLoadData("fit_e7","fit_e7_r2","fit_e7_r5","fit_e7_r10", names=c("Fit-N0","Fit-N2","Fit-N5","Fit-N10"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=T, behavs.bests=T, behavs.sample=1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data[["V10"]] <- metaLoadData("fit_e10","fit_e10_r2","fit_e10_r5","fit_e10_r10", names=c("Fit-N0","Fit-N2","Fit-N5","Fit-N10"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=T, behavs.bests=T, behavs.sample=1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data[["V13"]] <- metaLoadData("fit_e13","fit_e13_r2","fit_e13_r5","fit_e13_r10", names=c("Fit-N0","Fit-N2","Fit-N5","Fit-N10"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=T, behavs.bests=T, behavs.sample=1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))

count.col <- list()
for(setup in names(data)) {
  count.col[[setup]] <- diversity(data[[setup]])
}
save(count.col, file="~/Dropbox/Work/Papers/EC/collaborations_div.rdata")

frame <- data.frame()
for(setup in names(count.col)) {
  u <- count.col[[setup]]
  frame <- rbind(frame, cbind(u$summary, N=c("0","2","5","10"), Setup=setup))
  print(u$ttest)
}
pd <- position_dodge(.1) 
ggplot(frame, aes(x=N, y=mean, colour=Setup)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.5, position=pd) +
  geom_line(position=pd,aes(group=Setup)) +
  geom_point(position=pd) + ylab("Best-of-generation dispersion") + theme(legend.title=element_blank()) + scale_x_discrete(limits=c("0","2","5","10"))


frame <- data.frame(Setup=c(rep("V4",30),rep("V7",30),rep("V10",30),rep("V13",30)),
                    Value=c(count.col$V4$data[1,],count.col$V7$data[1,],count.col$V10$data[1,],count.col$V13$data[1,]))
frame$Setup <- factor(frame$Setup, levels=c("V4","V7","V10","V13"))
ggplot(frame, aes(Setup, Value)) + 
  geom_boxplot(aes(fill=Setup)) +
  ggtitle("Best-of-generation dispersion") + xlab("") +
  theme(axis.text.x = element_text(angle = 22.5, hjust = 1)) +
  guides(fill=guide_legend(title=NULL))

####### OVERCOMING PREMATURE CONVERGENCE -- FITNESS PLOTS

data4 <- metaLoadData("fit_e4","nsga_e4_group","nsga_e4_ind","nsga_e4_mix", names=c("F","T","I","M"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F))
data7 <- metaLoadData("fit_e7","nsga_e7_group","nsga_e7_ind","nsga_e7_mix", names=c("F","T","I","M"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F))
data10 <- metaLoadData("fit_e10","nsga_e10_group","nsga_e10_ind","nsga_e10_mix", names=c("F","T","I","M"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F))
data13 <- metaLoadData("fit_e13","nsga_e13_group","nsga_e13_ind","nsga_e13_mix", names=c("F","T","I","M"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat",fitlim=c(0,2), load.behavs=F))

DEF_WIDTH <- 3.25 ; DEF_HEIGHT <- 2.5
fullStatistics(data4, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit",fit.comp.par=list(snapshots=c(500),jitter=F,ylim=T))
fullStatistics(data7, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit",fit.comp.par=list(snapshots=c(500),jitter=F,ylim=T))
fullStatistics(data10, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit",fit.comp.par=list(snapshots=c(500),jitter=F,ylim=T))
fullStatistics(data13, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit",fit.comp.par=list(snapshots=c(500),jitter=F,ylim=T))


data4 <- metaLoadData("fit_e4","nsga_e4_group","nsga_e4_ind","nsga_e4_mix", names=c("Fit","NS-Team","NS-Ind","NS-Mix"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F))
data7 <- metaLoadData("fit_e7","nsga_e7_group","nsga_e7_ind","nsga_e7_mix", names=c("Fit","NS-Team","NS-Ind","NS-Mix"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F))
data10 <- metaLoadData("fit_e10","nsga_e10_group","nsga_e10_ind","nsga_e10_mix", names=c("Fit","NS-Team","NS-Ind","NS-Mix"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F))
data13 <- metaLoadData("fit_e13","nsga_e13_group","nsga_e13_ind","nsga_e13_mix", names=c("Fit","NS-Team","NS-Ind","NS-Mix"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat",fitlim=c(0,2), load.behavs=F))

fullStatistics(data4, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit",fit.comp.par=list(snapshots=c(500),jitter=F,ylim=T))
fullStatistics(data7, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit",fit.comp.par=list(snapshots=c(500),jitter=F,ylim=T))
fullStatistics(data10, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit",fit.comp.par=list(snapshots=c(500),jitter=F,ylim=T))
fullStatistics(data13, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit",fit.comp.par=list(snapshots=c(500),jitter=F,ylim=T))

# task difficulty x fitness x method

frame <- cbind(fitnessSummary(data4), V="4")
frame <- rbind(frame, cbind(fitnessSummary(data7), V="7"))
frame <- rbind(frame, cbind(fitnessSummary(data10), V="10"))
frame <- rbind(frame, cbind(fitnessSummary(data13), V="13"))
frame$method <- factor(frame$method, levels=c("Fit","NS-Team","NS-Ind","NS-Mix"))

pd <- position_dodge(.1) # move them .05 to the left and right
ggplot(frame, aes(x=V, y=mean, colour=method)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.5, position=pd) +
  geom_line(position=pd,aes(group=method)) + ylim(c(0,2)) +
  geom_point(position=pd) + ylab("Highest team performance") + theme(legend.title=element_blank())


##### BEHAVIOURAL METRICS -- NOVELTY ######################################

data <- metaLoadData("fit_e4","nsga_e4_group","nsga_e4_mix","nsga_e4_ind","fit_e7","nsga_e7_group","nsga_e7_mix","nsga_e7_ind","fit_e10","nsga_e10_group","nsga_e10_mix","nsga_e10_ind","fit_e13","nsga_e13_group","nsga_e13_mix","nsga_e13_ind", names=c("Fit.4","NS.Team.4","NS.Mix.4","NS.Ind.4","Fit.7","NS.Team.7","NS.Mix.7","NS.Ind.7","Fit.10","NS.Team.10","NS.Mix.10","NS.Ind.10","Fit.13","NS.Team.13","NS.Mix.13","NS.Ind.13"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=T, behavs.sample=0.1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
databests <- metaLoadData("fit_e4","nsga_e4_group","nsga_e4_mix","nsga_e4_ind","fit_e7","nsga_e7_group","nsga_e7_mix","nsga_e7_ind","fit_e10","nsga_e10_group","nsga_e10_mix","nsga_e10_ind","fit_e13","nsga_e13_group","nsga_e13_mix","nsga_e13_ind", names=c("Fit.4","NS.Team.4","NS.Mix.4","NS.Ind.4","Fit.7","NS.Team.7","NS.Mix.7","NS.Ind.7","Fit.10","NS.Team.10","NS.Mix.10","NS.Ind.10","Fit.13","NS.Team.13","NS.Mix.13","NS.Ind.13"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=T, behavs.bests=TRUE, behavs.sample=1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))

bests.group <- diversity.group(databests)
bests.ind <- diversity.ind(databests)
all.group <- diversity.group(data)
all.ind <- diversity.ind(data)

save(bests.group, file="~/Dropbox/Work/Papers/EC/div_group_bests.rdata")
save(bests.ind, file="~/Dropbox/Work/Papers/EC/div_ind_bests.rdata")
save(all.group, file="~/Dropbox/Work/Papers/EC/div_group_all.rdata")
save(all.ind, file="~/Dropbox/Work/Papers/EC/div_ind_all.rdata")

frame <- data.frame()
methods <- c("Fit","NS.Team","NS.Mix","NS.Ind") ; vs <- c("4","7","10","13")
for(v in vs) {
  names <- paste(methods,v, sep=".")
  frame <- rbind(frame, cbind(all.group$summary[names,], Type="Team exploration", V=v, Method=methods))
  frame <- rbind(frame, cbind(all.ind$summary[names,], Type="Individual exploration", V=v, Method=methods))
  frame <- rbind(frame, cbind(bests.group$summary[names,], Type="Best-of-gen team dispersion", V=v, Method=methods))
  frame <- rbind(frame, cbind(bests.ind$summary[names,], Type="Best-of-gen ind dispersion", V=v, Method=methods))
}
frame$Method <- factor(frame$Method, levels=c("Fit","NS.Team","NS.Ind","NS.Mix"))

plots <- list()
for(type in unique(frame$Type)) {
  pd <- position_dodge(.1)
  g <- ggplot(subset(frame, Type==type), aes(x=V, y=mean, colour=Method)) + 
    geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=0.5, position=pd) +
    geom_line(position=pd,aes(group=Method)) +
    geom_point(position=pd) + ylab(type) + theme(legend.title=element_blank())
  plots <- c(plots, list(g))
}
plotListToPDF(plots)
print(bests.group$ttest)
print(bests.ind$ttest)

pd <- position_dodge(.1)
ggplot(subset(frame, Type=="Best-of-gen team dispersion"), aes(x=V, y=mean, colour=Method)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=0.5, position=pd) +
  geom_line(position=pd,aes(group=Method)) + scale_y_continuous(breaks=pretty_breaks(n=6), limits=c(0,0.55)) +
  geom_point(position=pd) + ylab(type) + theme(legend.title=element_blank())

####### best-of-generation plots with REbehaviours ###############

data <- metaLoadData("fit_e4","fit_e7","fit_e10","fit_e13","nsga_e4_group","nsga_e7_group","nsga_e10_group","nsga_e13_group","nsga_e4_ind","nsga_e7_ind","nsga_e10_ind","nsga_e13_ind","nsga_e4_mix","nsga_e7_mix","nsga_e10_mix","nsga_e13_mix", 
                     names=c("Fit.4","Fit.7","Fit.10","Fit.13","NS.Team.4","NS.Team.7","NS.Team.10","NS.Team.13","NS.Ind.4","NS.Ind.7","NS.Ind.10","NS.Ind.13","NS.Mix.4","NS.Mix.7","NS.Mix.10","NS.Mix.13"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F))
which.median <- function(x) which.min(abs(x - median(x)))
rebests <- list()
unifs <- list()
maxfits <- list()
for(d in data) {
  fits <- c()
  for(job in d$jobs) {
    fits <- c(fits, d[[job]]$fitness$best.sofar[500])
  }
  medianjob <- which.median(bests.group$data[d$expname,])
  cat(d$expname, " " ,medianjob, "\n")
  rebests[[d$expname]] <- subset(fread(paste0(d$folder,"/job.",medianjob-1,".rebehaviours.stat")), select=c(4:8))
  unifs[[d$expname]] <- bests.group$data[d$expname,medianjob]
  maxfits[[d$expname]] <- fits[medianjob]
}

all <- data.frame()
for(n in names(rebests)) {all <- rbind(all, cbind(V=n, rebests[[n]]))}
all.b <- subset(all, select=3:6)
uni <- !duplicated(all.b)
resam <- sammon(dist(all.b[uni,]))
all[uni,"X"] <- resam$points[,1]
all[uni,"Y"] <- resam$points[,2]
save(all, file="~/Dropbox/Work/Papers/EC/sammon.rdata")

plots <- list()
for(v in names(rebests)) {
  sub <- cbind(subset(all, V==v),T="G")
  sub[1,"T"] <- "S"
  sub[500,"T"] <- "E"
  index.max <- which.max(sub[[2]])
  sub[index.max,"T"] <- "B"
  temp <- sub[499,] ; sub[499,] <- sub[index.max,] ; sub[index.max,] <- temp
  sub$T <- factor(sub$T, levels=c("G","S","E","B"))
  g <- ggplot(sub, aes(x=X, y=Y, shape=T, color=T, size=T)) + geom_point() + xlim(range(all$X,na.rm=T)) + ylim(range(all$Y,na.rm=T)) +
    scale_shape_manual(values=c(3,15,16,17)) + scale_colour_manual(values=c("black","turquoise","orange","red")) +
    scale_size_manual(values=c(1.5,4,4,4)) + 
    ggtitle(paste0(v, ", F=", format(maxfits[[v]],digits=3), " D=", format(unifs[[v]],digits=3))) + 
    theme(axis.text.x=element_blank(),axis.text.y=element_blank(),axis.ticks=element_blank(),axis.title.x=element_blank(),axis.title.y=element_blank(),legend.position="none")
  plots[[length(plots)+1]] <- g
}
plotListToPDF(plots, ncol=4, width=3, height=3.2)


################ DIVERSITY

data <- metaLoadData("fit_e4","nsga_e4_group", names=c("Fit","NS-Team"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=T, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
som <- buildSom(data[[1]],data[[2]], variables=data[[1]]$vars.group, sample.size=25000, distance.filter=0.4, grid.size=10, grid.type="rectangular", compute.fitness=TRUE, scale=TRUE, subpops=NULL)
save(som, file="~/Dropbox/Work/Papers/EC/som.rdata")

# out = 6.2x4.3in
m <- mapMergeSubpops(som, data[["Fit"]])
m2 <- mapMergeSubpops(som, data[["NS-Team"]])

# 4.5 x 3.6
somPlotHeatmap(som, m$job.0$all, limit.max=0.05, title="Fit")
somPlotHeatmap(som, m2$job.13$all, limit.max=0.05,title="NS-Team")

fitnessHeatmapPlots(som)
plot(som)

data <- loadData("nsga_e4_group", jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=T, behavs.sample=1, behavs.file="rebehaviours.stat", vars.ind=c(rep(NA, 12)),vars.group=c("GCap","GPreyD","GPredD","Time"))
identifyBests(som, data, outfile="~/Dropbox/Work/Papers/EC/newbests.csv")



####### SCALABILITY ################

data4 <- metaLoadData("nsga_p2v4","nsga_e4_group","nsga_p5v4","nsga_p7v4", names=c("P2","P3","P5","P7"), params=list(jobs=30, subpops=1, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F))
data7 <- metaLoadData("nsga_p2v7","nsga_e7_group","nsga_p5v7","nsga_p7v7", names=c("P2","P3","P5","P7"), params=list(jobs=30, subpops=1, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F))
data10 <- metaLoadData("nsga_p2v10","nsga_e10_group","nsga_p5v10","nsga_p7v10", names=c("P2","P3","P5","P7"), params=list(jobs=30, subpops=1, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F))
data13 <- metaLoadData("nsga_p2v13","nsga_e13_group","nsga_p5v13","nsga_p7v13",names=c("P2","P3","P5","P7"), params=list(jobs=30, subpops=1, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F))

fullStatistics(data13, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit",fit.comp.par=list(snapshots=c(500),jitter=F,ylim=T))

frame <- cbind(fitnessSummary(data4), V="4")
frame <- rbind(frame, cbind(fitnessSummary(data7), V="7"))
frame <- rbind(frame, cbind(fitnessSummary(data10), V="10"))
frame <- rbind(frame, cbind(fitnessSummary(data13), V="13"))

pd <- position_dodge(.1) # move them .05 to the left and right
ggplot(frame, aes(x=V, y=mean, colour=method)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.5, position=pd) +
  geom_line(position=pd,aes(group=method)) + ylim(c(0,2)) +
  geom_point(position=pd) + ylab("Best fitness") + xlab("V") + theme(legend.title=element_blank())

### scalability ALT

pd <- position_dodge(.1) # move them .05 to the left and right
ggplot(frame, aes(x=method, y=mean, colour=V)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.5, position=pd) +
  geom_line(position=pd,aes(group=V)) + ylim(c(0,2)) +
  geom_point(position=pd) + ylab("Best fitness") + xlab("Predators") + theme(legend.title=element_blank())

### scalability count near

m <- "mean"
countList <- list(
  p2v4=countNear("nsga_p2v4", 2, 0.085, mode=m),
  p3v4=countNear("nsga_e4_group", 3, 0.085, mode=m),
  p5v4=countNear("nsga_p5v4", 5, 0.085, mode=m),
  p7v4=countNear("nsga_p7v4", 7, 0.085, mode=m),
  p2v7=countNear("nsga_p2v7", 2, 0.15, mode=m),
  p3v7=countNear("nsga_e7_group", 3, 0.15, mode=m),
  p5v7=countNear("nsga_p5v7", 5, 0.15, mode=m),
  p7v7=countNear("nsga_p7v7", 7, 0.15, mode=m),
  p2v10=countNear("nsga_p2v10", 2, 0.21, mode=m),
  p3v10=countNear("nsga_e10_group", 3, 0.21, mode=m),
  p5v10=countNear("nsga_p5v10", 5, 0.21, mode=m),
  p7v10=countNear("nsga_p7v10", 7, 0.21, mode=m),
  p2v13=countNear("nsga_p2v13", 2, 0.275, mode=m),
  p3v13=countNear("nsga_e13_group", 3, 0.275, mode=m),
  p5v13=countNear("nsga_p5v13", 5, 0.275, mode=m),
  p7v13=countNear("nsga_p7v13", 7, 0.275, mode=m)
)
a <- metaAnalysis(countList)
frame <- a$summary
frame[,"V"] <- factor(rep(c(4,7,10,13), each=4))
frame[,"P"] <- factor(rep(c(2,3,5,7), times=4))

pd <- position_dodge(.1) # move them .05 to the left and right
ggplot(frame, aes(x=V, y=mean, colour=P)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.5, position=pd) +
  geom_line(position=pd,aes(group=P)) + scale_y_discrete(breaks = 1:6) +
  geom_point(position=pd) + ylab("Mean number of participants") + xlab("V") + theme(legend.title=element_blank())


###### PURE NS COMPARISON ##########

data4 <- metaLoadData("fit_e4","nsga_e4_group","nss_e4_group", names=c("Fit","NS+Fit","NS"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F))
data7 <- metaLoadData("fit_e7","nsga_e7_group","nss_e7_group", names=c("Fit","NS+Fit","NS"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F))
data10 <- metaLoadData("fit_e10","nsga_e10_group","nss_e10_group", names=c("Fit","NS+Fit","NS"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F))
data13 <- metaLoadData("fit_e13","nsga_e13_group","nss_e13_group", names=c("Fit","NS+Fit","NS"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat",fitlim=c(0,2), load.behavs=F))

fullStatistics(data4, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit",fit.comp.par=list(snapshots=c(500),jitter=F,ylim=T))


# task difficulty x fitness x method

frame <- cbind(fitnessSummary(data4), V="4")
frame <- rbind(frame, cbind(fitnessSummary(data7), V="7"))
frame <- rbind(frame, cbind(fitnessSummary(data10), V="10"))
frame <- rbind(frame, cbind(fitnessSummary(data13), V="13"))
frame$method <- factor(frame$method, levels=c("Fit","NS","NS+Fit"))

pd <- position_dodge(.1) # move them .05 to the left and right
ggplot(frame, aes(x=V, y=mean, colour=method)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.5, position=pd) +
  geom_line(position=pd,aes(group=method)) + ylim(c(0,2)) +
  geom_point(position=pd) + ylab("Highest team performance") + theme(legend.title=element_blank())

# exploration measures

data <- metaLoadData("nss_e4_group","nss_e7_group","nss_e10_group","nss_e13_group", names=c("NS.4","NS.7","NS.10","NS.13"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=T, behavs.sample=0.1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
databests <- metaLoadData("nss_e4_group","nss_e7_group","nss_e10_group","nss_e13_group", names=c("NS.4","NS.7","NS.10","NS.13"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=T, behavs.bests=TRUE, behavs.sample=1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))

nov.bests.group <- diversity.group(databests)
nov.all.group <- diversity.group(data)
save(nov.bests.group, file="~/Dropbox/Work/Papers/EC/nov.bests.group.rdata")
save(nov.all.group, file="~/Dropbox/Work/Papers/EC/nov.all.group.rdata")

frame <- rbind(nov.bests.group$summary, bests.group$summary[grep("Fit.|NS.Team",rownames(bests.group$summary)),])

frame[grep(".4",rownames(frame)),"V"] <- "4"
frame[grep(".7",rownames(frame)),"V"] <- "7"
frame[grep(".10",rownames(frame)),"V"] <- "10"
frame[grep(".13",rownames(frame)),"V"] <- "13"
frame[grep("NS.",rownames(frame)),"Method"] <- "NS"
frame[grep("NS.Team.",rownames(frame)),"Method"] <- "NS+Fit"
frame[grep("Fit.",rownames(frame)),"Method"] <- "Fit"
frame[["V"]] <- factor(frame[["V"]], levels=c("4","7","10","13"))

pd <- position_dodge(.1) # move them .05 to the left and right
ggplot(frame, aes(x=V, y=mean, colour=Method)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.5, position=pd) +
  geom_line(position=pd,aes(group=Method)) + scale_y_continuous(breaks=pretty_breaks(n=7), limits=c(0,0.7)) +
  geom_point(position=pd) + ylab("Best-of-generation dispersion") + theme(legend.title=element_blank())

frame <- rbind(nov.all.group$summary, all.group$summary[grep("Fit.|NS.Team",rownames(all.group$summary)),])
# *insert code above*
pd <- position_dodge(.1) # move them .05 to the left and right
ggplot(frame, aes(x=V, y=mean, colour=Method)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.5, position=pd) +
  geom_line(position=pd,aes(group=Method)) +
  geom_point(position=pd) + ylab("Team exploration") + theme(legend.title=element_blank())


### HERDING

setwd("~/exps/EC/herding")
data <- metaLoadData("fit","nsga_group","nsga_ind","nsga_mix", names=c("Fit","NS.Team","NS.Ind","NS.Mix"), params=list(fitness.file="refitness.stat", jobs=30, subpops=4, load.behavs=T, behavs.sample=0.1, vars.ind=c("sheepDist","curralDist","fox1Dist","fox2Dist"), vars.group=c("sheepCurral","time","sheepFence","sheepFox")))
databests <- metaLoadData("fit","nsga_group","nsga_ind","nsga_mix", names=c("Fit","NS.Team","NS.Ind","NS.Mix"), params=list(fitness.file="refitness.stat", jobs=30, subpops=4, load.behavs=T, behavs.bests=T, vars.ind=c("sheepDist","curralDist","fox1Dist","fox2Dist"), vars.group=c("sheepCurral","time","sheepFence","sheepFox")))

data <- metaLoadData("fit","nsga_group","nsga_ind","nsga_mix", names=c("Fit","NS.Team","NS.Ind","NS.Mix"), params=list(fitness.file="refitness.stat", jobs=30, fitlim=c(0,1.8), subpops=4, load.behavs=F))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(500), jitter=F, ylim=T), expset.name="herd", show.only=T)

herd.bests.group <- diversity.group(databests)
herd.all.group <- diversity.group(data)
herd.all.ind <- diversity.ind(data)
save(herd.bests.group, file="~/Dropbox/Work/Papers/EC/herd.bests.group.rdata")
save(herd.all.group, file="~/Dropbox/Work/Papers/EC/herd.all.group.rdata")
save(herd.all.ind, file="~/Dropbox/Work/Papers/EC/herd.all.ind.rdata")

### MULTI-ROVER

setwd("~/exps/EC/multirover")
data <- metaLoadData("fit","nsga_group","nsga_ind","nsga_mix", names=c("Fit","NS.Team","NS.Ind","NS.Mix"), params=list(fitness.file="refitness.stat", jobs=30, subpops=2, load.behavs=T, behavs.sample=0.1, vars.ind=c("ind.mov","ind.prox","lowActive","highActive"), vars.group=c("captured","distance","movement","proximity")))
databests <- metaLoadData("fit","nsga_group","nsga_ind","nsga_mix", names=c("Fit","NS.Team","NS.Ind","NS.Mix"), params=list(fitness.file="refitness.stat", jobs=30, subpops=2, load.behavs=T, behavs.bests=T, vars.ind=c("ind.mov","ind.prox","lowActive","highActive"), vars.group=c("captured","distance","movement","proximity")))

data <- metaLoadData("fit","nsga_group","nsga_ind","nsga_mix", names=c("Fit","NS.Team","NS.Ind","NS.Mix"), params=list(fitness.file="refitness.stat", jobs=30, subpops=2, load.behavs=F))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(500), jitter=F), expset.name="mr", show.only=T)

mr.bests.group <- diversity.group(databests)
mr.all.group <- diversity.group(data)
mr.all.ind <- diversity.ind(data)
save(mr.bests.group, file="~/Dropbox/Work/Papers/EC/mr.bests.group.rdata")
save(mr.all.group, file="~/Dropbox/Work/Papers/EC/mr.all.group.rdata")
save(mr.all.ind, file="~/Dropbox/Work/Papers/EC/mr.all.ind.rdata")


# HERDING AND MULTIROVER BEST-OF-GENERATION DISPERSION

# HERDING
setwd("~/exps/EC/herding")
jobs <- list(fit=0,nsga_group=0,nsga_ind=4,nsga_mix=0)

# MULTI-ROVER
setwd("~/exps/EC/multirover")
jobs <- list(fit=5,nsga_group=6,nsga_ind=4,nsga_mix=1)

all <- data.frame()
for(j in names(jobs)) {
  re <- subset(fread(paste0(j,"/job.",jobs[[j]],".rebehaviours.stat")), select=c(4:8))
  all <- rbind(all, cbind(V=j, re))
}
all.b <- subset(all, select=3:6)
uni <- !duplicated(all.b)
resam <- sammon(dist(all.b[uni,]))
all[uni,"X"] <- resam$points[,1] ; all[uni,"Y"] <- resam$points[,2]

save(all, file="~/Dropbox/Work/Papers/EC/herd_sammon.rdata")

save(all, file="~/Dropbox/Work/Papers/EC/mr_sammon.rdata")

plots <- list()
for(v in names(jobs)) {
  sub <- cbind(subset(all, V==v),T="G")
  sub[1,"T"] <- "S"
  sub[500,"T"] <- "E"
  index.max <- which.max(sub[[2]])
  sub[index.max,"T"] <- "B"
  temp <- sub[499,] ; sub[499,] <- sub[index.max,] ; sub[index.max,] <- temp
  sub$T <- factor(sub$T, levels=c("G","S","E","B"))
  g <- ggplot(sub, aes(x=X, y=Y, shape=T, color=T, size=T)) + geom_point() + xlim(range(all$X,na.rm=T)) + ylim(range(all$Y,na.rm=T)) +
    scale_shape_manual(values=c(3,15,16,17)) + scale_colour_manual(values=c("black","turquoise","orange","red")) +
    scale_size_manual(values=c(1.5,4,4,4)) + 
    ggtitle(paste0(v, ", F=", format(sub[[2]][499],digits=3))) + 
    theme(axis.text.x=element_blank(),axis.text.y=element_blank(),axis.ticks=element_blank(),axis.title.x=element_blank(),axis.title.y=element_blank(),legend.position="none")
  plots[[length(plots)+1]] <- g
}
plotListToPDF(plots, ncol=4, width=3, height=3.2)

data <- read.csv("~/Dropbox/Work/Papers/EC/otherexps.csv")
data$Type <- factor(data$Type, levels=c("BOD","TE","IE"))
data$Method <- factor(data$Method, levels=c("Fit","NST","NSI","NSM"))
ggplot(data=subset(data, Task=="MR"), aes(x=Type, y=Mean, fill=Method)) +
  geom_bar(stat="identity", position=position_dodge()) +
  geom_errorbar(aes(ymin=Mean-SE, ymax=Mean+SE), width=.5, position=position_dodge(.9))
ggplot(data=subset(data, Task=="Herd"), aes(x=Type, y=Mean, fill=Method)) +
  geom_bar(stat="identity", position=position_dodge()) +
  geom_errorbar(aes(ymin=Mean-SE, ymax=Mean+SE), width=.5, position=position_dodge(.9))

