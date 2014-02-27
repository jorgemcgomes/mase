setwd("~/exps/competitive/ga/go2")
setwd("~/exps/competitive/ga/pred2")

analyse("fit","nsga","nov0","nov1","pmcns50","pmcns75","ls50","ls25",filename="compall.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit","captured","possession","surrounded"), analyse=c("bestfit.0","bestfit.1"), smooth=0, splits=1, boxplots=T, print=F, all=F, plot=F)
analyse("fit","pmcns50","pmcns75","ls50","ls25",filename="combest.stat", vars.pre=c("gen"), vars.sub=c("fit","bestfit","captured","possession","surrounded"), analyse=c("fit.0","fit.1"), smooth=5, splits=1, boxplots=F, print=F, all=T)
data <- metaLoadData("pmcns50","pmcns75","ls50","ls25", params=list(jobs=5, vars.ind=c("captured","possession","surrounded"), subpops=2, load.behavs=T, behavs.sample=0.25))
fullStatistics(data, som.ind=TRUE, behav.mean=F, show.only=F, expset.name="comp")


for(f in c("fit","ls25","ls50","nov0","nov1","nsga","pmcns50","pmcns75")) {
    transformTournamentFiles(f, "compall.stat", "tbehaviours.stat",3,3)
}

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


data <- metaLoadData("fit","ls25","ls50","nov0","nov1","nsga","pmcns50","pmcns75", params=list(jobs=10, vars.ind=c("captured","possession","surrounded"), subpops=2, load.behavs=T, behavs.sample=1, behavs.file="tbehaviours.stat"))
fullStatistics(data, som.ind=TRUE, behav.mean=F, show.only=F, expset.name="comp")
count <- exploration.count(data, levels=5, vars=c("captured","possession","surrounded"))
uniformity.ind(count, "sub.0" ,threshold=50)
uniformity.ind(count, "sub.1" ,threshold=50)

data <- metaLoadData("fit","ls25","ls50","nov0","nov1","nsga","pmcns50","pmcns75", params=list(jobs=10, vars.ind=c("steps","distance","movement"), subpops=2, load.behavs=T, behavs.sample=1, behavs.file="tbehaviours.stat"))
fullStatistics(data, som.ind=TRUE, behav.mean=F, show.only=F, expset.name="comp")
count <- exploration.count(data, levels=5, vars=c("steps","distance","movement"))
uniformity.ind(count, "sub.0" ,threshold=50)
uniformity.ind(count, "sub.1" ,threshold=50)