setwd("~/exps/competitive/ga/go2")
setwd("~/exps/competitive/ga/pred2")
setwd("~/exps/competitive/ga/pred3")
setwd("~/exps/competitive/ga/go3")

analyse("fit","nsga","nov0","nov1","pmcns50","pmcns75","ls50","ls25",filename="compall.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit","captured","possession","surrounded"), analyse=c("bestfit.0","bestfit.1"), smooth=0, splits=1, boxplots=T, print=F, all=F, plot=T)
analyse("fit","pmcns50","pmcns75","ls50","ls25",filename="combest.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit","captured","possession","surrounded"), analyse=c("fit.0","fit.1"), smooth=5, splits=1, boxplots=F, print=F, all=T)




for(f in c("fit","nsga","nov0","nov1","pmcns50","ls50")) {
    transformTournamentFiles(f, "comp.stat", "tbehaviours.stat",3,3)
}

data <- metaLoadData("fit","nsga","nov0","nov1","pmcns50","ls50", params=list(jobs=10, vars.ind=c("captured","possession","surrounded"), subpops=2, load.behavs=T, behavs.sample=1, behavs.file="tbehaviours.stat"))
data <- metaLoadData("fit","nsga","nov0","nov1","pmcns50","ls50", params=list(jobs=10, vars.ind=c("steps","distance","movement"), subpops=2, load.behavs=T, behavs.sample=1, behavs.file="tbehaviours.stat"))

analyse("fit","nsga","nov0","nov1","pmcns50","ls50",filename="comp.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit","b1","b2","b3"), analyse=c("bestfit.0","bestfit.1"), smooth=0, splits=1, boxplots=T, print=T, all=F, plot=T)
analyse("fit","nsga","nov0","nov1","pmcns50","ls50",filename="comp.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit","b1","b2","b3"), analyse=c("fit.0"), smooth=5, splits=3, boxplots=F, print=T, all=F, plot=T)
analyse("fit","nsga","nov0","nov1","pmcns50","ls50",filename="comp.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit","b1","b2","b3"), analyse=c("fit.1"), smooth=5, splits=3, boxplots=F, print=T, all=F, plot=T)

data0 <- data
for(e in names(data0)) {
    data0[[e]][["subpops"]] <- c("sub.0")
    data0[[e]][["nsubs"]] <- 1
}
fullStatistics(data0, som.ind=TRUE, behav.mean=F, show.only=F, expset.name="comp0")

data1 <- data
for(e in names(data0)) {
    data1[[e]][["subpops"]] <- c("sub.1")
    data1[[e]][["nsubs"]] <- 1
}
fullStatistics(data1, som.ind=TRUE, behav.mean=F, show.only=F, expset.name="comp1")


count <- exploration.count(data, levels=5, vars=data[[1]]$vars.ind)
uniformity.ind(count, "sub.0" ,threshold=50)
uniformity.ind(count, "sub.1" ,threshold=50)










for(f in c("fit","nov","nov0","nov1","nsga","pmcns","alt25")) {
    transformTournamentFiles(f, "compmaster.stat", "masterbehaviours.stat",4,4)
}

# PPSN

theme_set(theme_bw())

setwd("~/exps/competitive/ga/pred5")
bvars <- c("time","dist","speed","walls")
DEF_WIDTH = 5 ; DEF_HEIGHT=3.5
analyse("fit","nov","nov0","nov1","alt25","pmcns", exp.names=c("Fit","NS-B","NS-0","NS-1","NS-Alt","PMCNS"), filename="compmaster.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit",bvars), analyse=c("bestfit.0"), smooth=0, splits=0, boxplots=F, print=F, all=F, plot=T, ylim=c(0.5,1), jitter=F)
analyse("fit","nov","nov0","nov1","alt25","pmcns", exp.names=c("Fit","NS-B","NS-0","NS-1","NS-Alt","PMCNS"),filename="compmaster.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit",bvars), analyse=c("bestfit.1"), smooth=0, splits=0, boxplots=F, print=F, all=F, plot=T, ylim=c(0.25,0.71), jitter=F)
DEF_WIDTH = 4 ; DEF_HEIGHT=4
analyse("fit","nov","nov0","nov1","alt25","pmcns", exp.names=c("Fit","NS-B","NS-0","NS-1","NS-Alt","PMCNS"), filename="compmaster.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit",bvars), analyse=c("bestfit.0"), smooth=0, splits=0, boxplots=T, print=F, all=F, plot=F, ylim=c(0.25,1), jitter=F)
analyse("fit","nov","nov0","nov1","alt25","pmcns", exp.names=c("Fit","NS-B","NS-0","NS-1","NS-Alt","PMCNS"),filename="compmaster.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit",bvars), analyse=c("bestfit.1"), smooth=0, splits=0, boxplots=T, print=F, all=F, plot=F, ylim=c(0.25,1), jitter=F)


analyse("fit","nov","nov0","nov1","alt25","pmcns", exp.names=c("Fit","NS-B","NS-0","NS-1","NS-Alt","PMCNS"),filename="compmaster.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit",bvars), analyse=c("fit.0"), smooth=10, splits=0, boxplots=F, print=F, all=F, plot=T)
analyse("fit","nov","nov0","nov1","alt25","pmcns", exp.names=c("Fit","NS-B","NS-0","NS-1","NS-Alt","PMCNS"),filename="compmaster.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit",bvars), analyse=c("fit.1"), smooth=10, splits=0, boxplots=F, print=F, all=F, plot=T)
analyse("fit","nov","nov0","nov1","alt25","pmcns", exp.names=c("Fit","NS-B","NS-0","NS-1","NS-Alt","PMCNS"),filename="compself.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit",bvars), analyse=c("fit.0"), smooth=10, splits=0, boxplots=F, print=F, all=F, plot=T)
analyse("fit","nov","nov0","nov1","alt25","pmcns", exp.names=c("Fit","NS-B","NS-0","NS-1","NS-Alt","PMCNS"),filename="compself.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit",bvars), analyse=c("fit.1"), smooth=10, splits=0, boxplots=F, print=F, all=F, plot=T)

analyse("fit","nov","nov0","nov1","alt25","pmcns", exp.names=c("Fit","NS-B","NS-0","NS-1","NS-Alt","PMCNS"), filename="compmaster.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit",bvars), analyse=c("bestfit.0"), smooth=0, splits=10, boxplots=T, print=T, all=F, plot=F, ylim=c(0.5,1), jitter=F)
analyse("fit","nov","nov0","nov1","alt25","pmcns", exp.names=c("Fit","NS-B","NS-0","NS-1","NS-Alt","PMCNS"),filename="compmaster.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit",bvars), analyse=c("bestfit.1"), smooth=0, splits=10, boxplots=T, print=T, all=F, plot=F, ylim=c(0.2,0.71), jitter=F)


data <- metaLoadData("fit","nov","nov0","nov1","alt25","pmcns",names=c("Fit","NS-B","NS-0","NS-1","NS-Alt","PMCNS"), params=list(jobs=30, vars.group=bvars, subpops=2, load.behavs=T, behavs.sample=1, behavs.file="masterbehaviours.stat"))

data0 <- data
for(e in names(data0)) {
    data0[[e]] <- filterSubs(data0[[e]], "sub.0")
}
count0 <- exploration.count(data0, levels=5, vars=bvars)
uniformity.ind(count0, "sub.0" ,threshold=50, fitness.threshold=0.8)
uniformity.ind(count0, "sub.0" ,threshold=50, fitness.threshold=0)
data1 <- data
for(e in names(data1)) {
    data1[[e]] <- filterSubs(data1[[e]], "sub.1")
}
count1 <- exploration.count(data1, levels=5, vars=bvars)
uniformity.ind(count1, "sub.1" ,threshold=50, fitness.threshold=0.3)
uniformity.ind(count1, "sub.1" ,threshold=50, fitness.threshold=0)

DEF_WIDTH = 7 ; DEF_HEIGHT = 5
som0 <- fullStatistics(data0, som.group=T, som.alljobs=T, behav.mean=F, show.only=F, expset.name="comp0", som.group.par=list(grid.size=10))
som1 <- fullStatistics(data1, som.group=T, som.alljobs=T, behav.mean=F, show.only=F, expset.name="comp1", som.group.par=list(grid.size=10))
save(som0, file="som0.data")
save(som1, file="som1.data")
identifyBests(som0$group, data0$PMCNS, n=10, "pmcnsbests0.stat")
identifyBests(som1$group, data1$PMCNS, n=10, "pmcnsbests1.stat")







analyse("fit_time","fit_wins",filename="compsample.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit",c("b1","b2","b3","b4")), analyse=c("fit.0"), smooth=0, splits=1, boxplots=T, print=F, all=F, plot=T)
analyse("fit_time","fit_wins",filename="compsample.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit",c("b1","b2","b3","b4")), analyse=c("fit.1"), smooth=0, splits=1, boxplots=T, print=F, all=F, plot=T)
transformTournamentFiles("fit_time", "compsample.stat", "samplebehaviours.stat",4,4)
transformTournamentFiles("fit_wins", "compsample.stat", "samplebehaviours.stat",4,4)
data <- metaLoadData("fit_time","fit_wins", params=list(jobs=10, vars.group=c("time","dist","speed","walls"), subpops=2, load.behavs=T, behavs.sample=1, behavs.file="samplebehaviours.stat"))
count <- exploration.count(data, levels=5, vars=c("time","dist","speed","walls"))
uniformity.ind(count, "sub.0" ,threshold=10)
uniformity.ind(count, "sub.1" ,threshold=10)
data0 <- data
for(e in names(data0)) {
    data0[[e]] <- filterSubs(data0[[e]], "sub.0")
}
fullStatistics(data0, som.group=T, som.alljobs=T, behav.mean=F, show.only=F, expset.name="comptest0", som.group.par=list(grid.size=10))
data1 <- data
for(e in names(data1)) {
    data1[[e]] <- filterSubs(data1[[e]], "sub.1")
}
fullStatistics(data1, som.group=T, som.alljobs=T, behav.mean=F, show.only=F, expset.name="comptest1", som.group.par=list(grid.size=10))









###############3
setwd("~/exps/competitive/ga/pred4")
bvars <- c("steps","distance","movement")

setwd("~/exps/competitive/ga/go4")
bvars <- c("captured","possession","surrounded")

analyse("fit","nov","nov0","nov1","nsga","pmcns","nov_alt25",filename="compmaster.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit",bvars), analyse=c("bestfit.0"), smooth=0, splits=1, boxplots=T, print=F, all=F, plot=T)
analyse("fit","nov","nov0","nov1","nsga","pmcns","nov_alt25",filename="compmaster.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit",bvars), analyse=c("bestfit.1"), smooth=0, splits=1, boxplots=T, print=F, all=F, plot=T)
analyse("fit","nov","nov0","nov1","nsga","pmcns","nov_alt25",filename="compmaster.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit",bvars), analyse=c("fit.0"), smooth=5, splits=1, boxplots=F, print=F, all=F, plot=T)
analyse("fit","nov","nov0","nov1","nsga","pmcns","nov_alt25",filename="compself.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit",bvars), analyse=c("fit.0"), smooth=5, splits=1, boxplots=F, print=F, all=F, plot=T)
analyse("fit","nov","nov0","nov1","nsga","pmcns","nov_alt25",filename="compmaster.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit",bvars), analyse=c("fit.1"), smooth=5, splits=1, boxplots=F, print=F, all=F, plot=T)
analyse("fit","nov","nov0","nov1","nsga","pmcns","nov_alt25",filename="compself.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit",bvars), analyse=c("fit.1"), smooth=5, splits=1, boxplots=F, print=F, all=F, plot=T)

diffs("fit","compself.stat","compmaster.stat")

for(f in c("fit","nov","nov0","nov1","nsga","pmcns","nov_alt25")) {
    transformTournamentFiles(f, "compmaster.stat", "masterbehaviours.stat",3,3)
}
data <- metaLoadData("fit","nov","nov0","nov1","nsga","pmcns","nov_alt25", params=list(jobs=20, vars.group=bvars, subpops=2, load.behavs=T, behavs.sample=1, behavs.file="masterbehaviours.stat"))
count <- exploration.count(data, levels=5, vars=bvars)
uniformity.ind(count, "sub.0" ,threshold=50)
uniformity.ind(count, "sub.1" ,threshold=50)
data0 <- data
for(e in names(data0)) {
    data0[[e]] <- filterSubs(data0[[e]], "sub.0")
}
fullStatistics(data0, som.group=T, som.alljobs=T, behav.mean=F, show.only=F, expset.name="comptest0", som.group.par=list(grid.size=10))
data1 <- data
for(e in names(data1)) {
    data1[[e]] <- filterSubs(data1[[e]], "sub.1")
}
fullStatistics(data1, som.group=T, som.alljobs=T, behav.mean=F, show.only=F, expset.name="comptest1", som.group.par=list(grid.size=10))


analyse("fit","nov","nov0","nov1","pmcns_s","pmcns",filename="compmaster.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit",bvars), analyse=c("bestfit.0"), smooth=0, splits=1, boxplots=T, print=F, all=F, plot=T)
analyse("fit","nov","nov0","nov1","pmcns_s","pmcns",filename="compmaster.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit",bvars), analyse=c("bestfit.1"), smooth=0, splits=1, boxplots=T, print=F, all=F, plot=T)
transformTournamentFiles("pmcns_s", "compmaster.stat", "masterbehaviours.stat",3,3)
data <- metaLoadData("fit","nov","nov0","nov1","pmcns_s","pmcns", params=list(jobs=20, vars.group=bvars, subpops=2, load.behavs=T, behavs.sample=1, behavs.file="masterbehaviours.stat"))
count <- exploration.count(data, levels=5, vars=bvars)
uniformity.ind(count, "sub.0" ,threshold=50)
uniformity.ind(count, "sub.1" ,threshold=50)
