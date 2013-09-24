setwd("~/exps/")
par <- list(fitlim=c(0,30), jobs=10, load.behavs=F)
all <- metaLoadData("k3_hom_easy_fit","k3_hom_easy_nov50","k3_hom_med_fit","k3_hom_med_nov50","k3_hom_hard_fit","k3_hom_hard_nov50", params=par)
fullStatistics(all, expset.name="kw.hom", show.only=F, fit.comp=T, fit.comp.par=list(snapshots=c(250,500,750,999)))

setwd("~/Dropbox/exps_AAMAS/")




par <- list(fitlim=c(0,2), jobs=5, load.behavs=T, subpops=3, behavs.sample=0.25, vars.group=c("g.captured","g.time","g.finaldist","g.dispersion"), vars.ind=c("i.captured","i.preydist","i.movement","i.partnerdist"))
data <- metaLoadData("pred3_fit","pred3_nov50","pred3_nov50_indshare","pred3_nov50_indmult", params=par, names=c("fit","nov-group","nov-indshare","nov-indmult"))

par <- list(fitlim=c(0,2), jobs=5, load.behavs=T, subpops=5, behavs.sample=0.1, vars.group=c("g.captured","g.time","g.finaldist","g.dispersion"), vars.ind=c("i.captured","i.preydist","i.movement","i.partnerdist"))
data.p5 <- metaLoadData("pred5m_fit","pred5m_nov50", params=par, names=c("fit","nov-group"))
par <- list(fitlim=c(0,2), jobs=5, load.behavs=T, subpops=3, behavs.sample=0.1, vars.group=c("g.captured","g.time","g.finaldist","g.dispersion"), vars.ind=c("i.captured","i.preydist","i.movement","i.partnerdist"))
data.p3 <- metaLoadData("pred3_fit","pred3_nov50", params=par, names=c("fit","nov-group"))
par <- list(fitlim=c(0,30), jobs=5, load.behavs=T, subpops=3, behavs.sample=0.1, vars.group=c("g.passes","g.steps","g.dispersion","g.pass-length"), vars.ind=c("i.passes","i.pass-length","i.keeper-dist","i.movement"))
data.km <- metaLoadData("k3_ph_med_fit","k3_ph_med_nov50", params=par, names=c("fit","nov-group"))
data.kh <- metaLoadData("k3_ph_hard_fit","k3_ph_hard_nov50", params=par, names=c("fit","nov-group"))

count <- exploration.count(data.p3, levels=5)


count.p3 <- exploration.count(data.p3, levels=5)
count.p5 <- exploration.count(data.p5, levels=5)
count.km <- exploration.count(data.km, levels=5)
count.kh <- exploration.count(data.kh, levels=5)

counti.p3 <- exploration.count(data.p3, levels=5, vars=data.p3[[1]]$vars.ind)
counti.p5 <- exploration.count(data.p5, levels=5, vars=data.p5[[1]]$vars.ind)
counti.km <- exploration.count(data.km, levels=5, vars=data.km[[1]]$vars.ind)
counti.kh <- exploration.count(data.kh, levels=5, vars=data.kh[[1]]$vars.ind)

exploration.uniformity(count.p3, threshold=100)
exploration.uniformity(count.p5, threshold=100)
exploration.uniformity(count.km, threshold=100)
exploration.uniformity(count.kh, threshold=100)

setwd("~/Dropbox/exps_AAMAS/")
par <- list(fitlim=c(0,2), jobs=20, load.behavs=T, subpops=5, behavs.sample=0.25, vars.group=c("g.captured","g.time","g.finaldist","g.dispersion"), vars.ind=c("i.captured","i.preydist","i.movement","i.partnerdist"))
data <- metaLoadData("pred5m_fit","pred5m_nov50","pred5m_nov50_indshare","pred5m_nov50_indmult", params=par, names=c("fit","nov-group","nov-indshare","nov-indmult"))
fullStatistics(data, expset.name="p5", fit.comp=T, fit.comp.par=list(snapshots=c(100,250,499)), fit.ind=T, som.group=T, som.alljobs=T, som.ind=T, behav.mean=T, show.only=F)
gc()
par <- list(fitlim=c(0,2), jobs=20, load.behavs=T, subpops=3, behavs.sample=0.25, vars.group=c("g.captured","g.time","g.finaldist","g.dispersion"), vars.ind=c("i.captured","i.preydist","i.movement","i.partnerdist"))
data <- metaLoadData("pred3_fit","pred3_nov50","pred3_nov50_indshare","pred3_nov50_indmult", params=par, names=c("fit","nov-group","nov-indshare","nov-indmult"))
fullStatistics(data, expset.name="p3", fit.comp=T, fit.comp.par=list(snapshots=c(100,250,499)), fit.ind=T, som.group=T, som.alljobs=T, som.ind=T, behav.mean=T, show.only=F)
gc()
par <- list(fitlim=c(0,30), jobs=20, load.behavs=T, subpops=3, behavs.sample=0.25, vars.group=c("g.passes","g.steps","g.dispersion","g.pass-length"), vars.ind=c("i.passes","i.pass-length","i.keeper-dist","i.movement"))
data <- metaLoadData("k3_ph_med_fit","k3_ph_med_nov50","k3_ph_med_nov50_indshare","k3_ph_med_nov50_indmult", params=par, names=c("fit","nov-group","nov-indshare","nov-indmult"))
fullStatistics(data, expset.name="ke", fit.comp=T, fit.comp.par=list(snapshots=c(333,666,999)), fit.ind=T, som.group=T, som.alljobs=T, som.ind=T, behav.mean=T, show.only=F)
gc()
data <- metaLoadData("k3_ph_hard_fit","k3_ph_hard_nov50","k3_ph_hard_nov50_indshare","k3_ph_hard_nov50_indmult", params=par, names=c("fit","nov-group","nov-indshare","nov-indmult"))
fullStatistics(data, expset.name="kh", fit.comp=T, fit.comp.par=list(snapshots=c(333,666,999)), fit.ind=T, som.group=T, som.alljobs=T, som.ind=T, behav.mean=T, show.only=F)
gc()

par <- list(fitlim=c(0,2), jobs=20, load.behavs=T, subpops=5, behavs.sample=0.15, vars.group=c("g.captured","g.time","g.finaldist","g.dispersion"), vars.ind=c("i.captured","i.preydist","i.movement","i.partnerdist"))
data <- metaLoadData("pred5m_fit","pred5m_nov50","pred5m_nov50_indshare","pred5m_nov50_indmult", params=par, names=c("fit","nov-group","nov-indshare","nov-indmult"))
fullStatistics(data, expset.name="p5", som.group=T, som.alljobs=T, show.only=F)

nov.div <- groupDiversity(data$pred5m_nov50)
fit.div <- groupDiversity(data$pred5m_fit)
indmult.div <- groupDiversity(data$pred5m_nov50_indmult)
indshare.div <- groupDiversity(data$pred5m_nov50_indshare)
plotMultiline(smooth(data.frame(gen=nov.div$gen,nov=nov.div$mean, fit=fit.div$mean,indmult=indmult.div$mean,indhsare=indshare.div$mean)), ylim=NULL)
    

par <- list(fitlim=c(0,2), jobs=10, load.behavs=F)

predm <- metaLoadData("pred3m_nov50", "pred3m_fit","pred5m_nov50", "pred5m_fit", params=par)
fullStatistics(predm, fit.comp=T, fit.comp.par=list(snapshots=c(100,250,499)), show.only=T)

par <- list(fitlim=c(0,2), jobs=10, load.behavs=T, subpops=3, behavs.sample=0.1, vars.group=c("g.captured","g.time","g.finaldist","g.dispersion"), vars.ind=c("i.captured","i.preydist","i.movement","i.partnerdist"))

pred5 <- metaLoadData("pred5_nov50", "pred5_fit", params=par)
fullStatistics(pred5, fit.comp=T, fit.comp.par=list(snapshots=c(50,150,249)), show.only=T)

pred3m <- metaLoadData("pred3m_nov50", "pred3m_fit", params=par)
fullStatistics(pred3m, fit.comp=T, fit.ind=T, fit.comp.par=list(snapshots=c(100,250,499)), show.only=T)

pred5h <- metaLoadData("pred5h_nov50", params=par)
fullStatistics(pred5h, fit.comp=T, fit.comp.par=list(snapshots=c(50,150,249)), show.only=T)




par <- list(fitlim=c(0,30), jobs=10, load.behavs=T, subpops=3, behavs.sample=0.1, vars.group=c("g.passes","g.steps","g.dispersion","g.pass-length"), vars.ind=c("i.passes","i.pass-length","i.keeper-dist","i.movement"))
k.med <- metaLoadData("k3_ph_med_fit","k3_ph_med_nov50","k3_ph_med_nov50_indmult","k3_ph_med_nov50_indshare","k3_ph_med_nov50_indnone", params=par, names=c("fit","nov","nov.ind.mult","nov.ind.shared","nov.ind.none"))
fullStatistics(k.med, expset.name="kmed", fit.comp=T, fit.comp.par=list(snapshots=c(333,666,999)), fit.ind=T, som.group=T, som.alljobs=T, som.ind=T, behav.mean=T)

k.hard <- metaLoadData("k3_ph_hard_fit","k3_ph_hard_nov50", params=par, names=c("fit","nov"))
fullStatistics(k.hard, expset.name="khard", fit.comp=T, fit.comp.par=list(snapshots=c(333,666,999)), fit.ind=T, som.group=T, som.alljobs=T, som.ind=T, behav.mean=T)



setwd("~/predator/")
data.op.nov50 <- loadData("oneprey_nov50", 
                          jobs=10, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                          vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion","g.maxdispersion"), vars.file=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin","g.captured","g.time","g.finaldist","g.avgdist","g.dispersion","g.maxdispersion"), subpops=3, expname="NS-50", load.behavs=TRUE, behavs.sample=0.25)
sample <- sampleData(list(data.op.nov50), 100000)
cor(sample[,c("fitness","g.captured","g.time","g.finaldist","g.avgdist","g.dispersion","g.maxdispersion")], method="spearman")
                fitness g.captured  g.time g.finaldist g.avgdist g.dispersion g.maxdispersion
fitness          1.0000     0.6852 -0.2951     -0.8997   -0.7049      -0.6959         -0.6819
g.captured       0.6852     1.0000 -0.4193     -0.6293   -0.4278      -0.3590         -0.3497
g.time          -0.2951    -0.4193  1.0000      0.2638    0.1941       0.3546          0.4129
g.finaldist     -0.8997    -0.6293  0.2638      1.0000    0.8619       0.8041          0.7724
g.avgdist       -0.7049    -0.4278  0.1941      0.8619    1.0000       0.8735          0.8206
g.dispersion    -0.6959    -0.3590  0.3546      0.8041    0.8735       1.0000          0.9745
g.maxdispersion -0.6819    -0.3497  0.4129      0.7724    0.8206       0.9745          1.0000
cor(sample[,c("fitness","avgdist","mindist","angle","captured","movement","partneravg","partnermin")], method="spearman")
            fitness avgdist mindist    angle captured movement partneravg partnermin
fitness     1.00000 -0.6693 -0.7971 -0.03108  0.53830 -0.01836   -0.68383   -0.63726
avgdist    -0.66931  1.0000  0.8565  0.15139 -0.35477 -0.11317    0.82976    0.86875
mindist    -0.79713  0.8565  1.0000  0.16689 -0.56313 -0.13075    0.73531    0.73822
angle      -0.03108  0.1514  0.1669  1.00000 -0.04276 -0.16979    0.03624    0.06585
captured    0.53830 -0.3548 -0.5631 -0.04276  1.00000  0.21694   -0.24674   -0.22129
movement   -0.01836 -0.1132 -0.1307 -0.16979  0.21694  1.00000    0.05415    0.02872
partneravg -0.68383  0.8298  0.7353  0.03624 -0.24674  0.05415    1.00000    0.94793
partnermin -0.63726  0.8688  0.7382  0.06585 -0.22129  0.02872    0.94793    1.00000

setwd("~/exps")

par <- list(fitlim=c(0,2), jobs=10, load.behavs=T, subpops=3, behavs.sample=0.1, vars.group=c("g.captured","g.time","g.finaldist","g.dispersion"), vars.ind=c("i.captured","i.preydist","i.movement","i.partnerdist"))
all <- metaLoadData("pred3_nov50","pred3_fit", params=par, names=c("nov","fit"))
fullStatistics(all, expset.name="p3", show.only=F, fit.comp=T, fit.comp.par=list(snapshots=c(100,250,499)), fit.ind=T, behav.mean=T, som.ind=T, som.group=T, som.alljobs=T)
soms <- fullStatistics(all, expset.name="p3", show.only=T, som.group=T)
explorationVideo(soms$group, filterJobs(all[[1]], "job.1"), mergeSubpops=T, accumulate=F,  interval=10, fps=3)
explorationVideo(soms$group, filterJobs(all[[2]], "job.0"), mergeSubpops=T, accumulate=F,  interval=10, fps=3)

par <- list(fitlim=c(0,30), jobs=10, load.behavs=T, subpops=3, behavs.sample=0.1, vars.group=c("g.passes","g.steps","g.dispersion","g.pass-length"), vars.ind=c("i.passes","i.pass-length","i.keeper-dist","i.movement"))

k.med <- metaLoadData("k3_ph_med_nov50","k3_ph_med_fit", params=par)
fullStatistics(k.med, expset.name="kmed", fit.comp=T, fit.comp.par=list(snapshots=c(250,500,750,999)), fit.ind=T, som.group=T, som.alljobs=T, som.ind=T, behav.mean=T )
sample <- sampleData(k.med, 10000)
cor(sample[,c("fitness","pass-length","steps","dispersion","passes")])

s <- fullStatistics(k.med, expset.name="kmed", som.group=T, som.alljobs=T)


par <- list(fitlim=c(0,30), jobs=10, load.behavs=F)

# Homogeneous vs heterogeneous
all <- metaLoadData("k3_hom_med_fit","k3_hom_med_nov50","k3_ph_hard_fit","k3_ph_hard_nov50", params=par)
fullStatistics(all, expset.name="k3", show.only=T, fit.comp=T, fit.comp.par=list(snapshots=c(250,500,750,999)))

# Het easy vs het hard
all <- metaLoadData("k3_ph_med_fit","k3_ph_med_nov50","k3_ph_hard_fit","k3_ph_hard_nov50", params=par)
fullStatistics(all, expset.name="k3", show.only=T, fit.comp=T, fit.comp.par=list(snapshots=c(250,500,750,999)))


all <- metaLoadData("k3_ph_med_nov50_indmult","k3_ph_med_nov50_indshare","k3_ph_med_nov50_indnone", params=par)
fullStatistics(all, expset.name="k3", show.only=T, fit.comp=T, fit.ind=F, fit.comp.par=list(snapshots=c(250,500,750,999)))

all <- metaLoadData("k3_hom_easy_fit","k3_hom_easy_nov50","k3_hom_med_fit","k3_hom_med_nov50","k3_hom_hard_fit","k3_hom_hard_nov50", params=par)
fullStatistics(all, expset.name="k3", show.only=T, fit.comp=T, fit.ind=F, fit.comp.par=list(snapshots=c(250,500,750,999)))

all <- metaLoadData("k3_ph_easy_fit","k3_ph_easy_nov50","k3_ph_medh_fit","k3_ph_medh_nov50","k3_ph_med_fit","k3_ph_med_nov50","k3_ph_hard_fit","k3_ph_hard_nov50", params=par)
fullStatistics(all, expset.name="k3", show.only=T, fit.comp=T, fit.comp.par=list(snapshots=c(250,500,750,999)))

all <- metaLoadData("k3_ph_med_fit","k3_ph_med_fit_noelite","k3_ph_med_fit_nocurrent", params=list(fitlim=c(0,25), jobs=10, load.behavs=F, gens=0:499))
fullStatistics(all, expset.name="k3", show.only=T, fit.comp=T, fit.comp.par=list(snapshots=c(100,250,499)))

all <- metaLoadData("k3_hom_easy_nov50","k3_hom_med_nov50", params=par)
fullStatistics(all, expset.name="k3", show.only=T, fit.comp=T, fit.comp.par=list(snapshots=c(250,500,750,999)))

all <- metaLoadData("k3_ph_med_fit","k3_ph_med_fit_mut10","k3_ph_med_fit_mut20", params=list(fitlim=c(0,25), jobs=10, load.behavs=F, gens=0:499))
fullStatistics(all, expset.name="k3", show.only=T, fit.comp=T, fit.comp.par=list(snapshots=c(100,250,499)))


par <- list(fitlim=c(0,25), jobs=5, load.behavs=T, behavs.sample=0.1, vars.group=c("Passes","Steps","Dispersion","Movement","TakerDist"), subpops=3)
all <- metaLoadData("k2_ph_easier_fit","k2_ph_easier_nov50","k2_ph_med_fit","k2_ph_med_nov50","k2_ph_med_nov70","k2_ph_medhard_fit","k2_ph_medhard_nov50","k2_ph_hard_fit","k2_ph_hard_nov50", params=par)
fullStatistics(all, expset.name="kw_new", show.only=T, fit.comp=T, fit.comp.par=list(snapshots=c(100,250,499)), fit.ind=F)

easy <- metaLoadData("k2_ph_easier_fit","k2_ph_easier_nov50", params=par)
fullStatistics(easy, expset.name="kw_easy", show.only=T, fit.comp=T, fit.comp.par=list(snapshots=c(100,250,499)))

med <- metaLoadData("k2_ph_med_fit","k2_ph_med_nov50","k2_ph_med_nov70", params=par)
fullStatistics(med, expset.name="kw_med", show.only=F, fit.comp=T, fit.comp.par=list(snapshots=c(100,250,499)), fit.ind=T, behav.mean=T, som.group=T, som.alljobs=T)

medhard <- metaLoadData("k2_ph_medhard_fit","k2_ph_medhard_nov50", params=par)
fullStatistics(medhard, expset.name="kw_medhard", show.only=T, fit.comp=T, fit.comp.par=list(snapshots=c(100,250,499)))

hard <- metaLoadData("k2_ph_hard_fit","k2_ph_hard_nov50", params=par)
fullStatistics(hard, expset.name="kw_hard", show.only=T, fit.comp=T, fit.comp.par=list(snapshots=c(100,250,499)))









ph <- metaLoadData("k2_ph_medhard_nov50","k2_ph_medhard_fit", params=list(fitlim=c(0,25), jobs=5, load.behavs=F))
fullStatistics(ph, expset.name="kw_new", show.only=T, fit.comp=T, fit.comp.par=list(snapshots=c(100,250,499)), fit.ind=T)

d <- loadData("kw_ph_lp_nov50", fitlim=c(0,6), jobs=1, load.behavs=T, behavs.sample=0.1, vars.group=c("Passes","Steps","Dispersion","Movement","TakerDist"), subpops=3)
s <- sampleData(list(d), 20000)
summary(s)

phnew <- metaLoadData("kw_phnew_fit","kw_phnew_nov50","kw_phnew_nov75", params=list(fitlim=c(0,10), jobs=10, load.behavs=T, behavs.sample=0.1, vars.group=c("Passes","Steps","Dispersion","Movement","TakerDist"), subpops=3))
fullStatistics(phnew, expset.name="kw_small", fit.ind=T, fit.comp=T, fit.comp.par=list(snapshots=c(250,500,750,999)), behav.mean=T, som.group=T, som.alljobs=T, show.only=F)

sample <- sampleData(phnew, 100000)
cor(sample[,c(-1,-2,-3)], method="spearman")

phs <- loadData("kw_phnew_nov50short", jobs=5, load.behavs=T, behavs.sample=0.1, vars.group=c("Passes","Steps","Dispersion"), subpops=3)
sample <- sampleData(list(phs), 100000)
summary(sample)
cor(sample[,c(-1,-2,-3)], method="pearson")

ph3e <- metaLoadData("kw_ph3_fit","kw_ph3_nov50","kw_ph3_mcn15", params=list(fitlim=c(0,20), jobs=10, load.behavs=T, behavs.sample=0.1, vars.group=c("Passes","Steps","Dispersion","Movement","TakerDist"), subpops=3))
ph3h <- metaLoadData("kw_ph3_hard_fit","kw_ph3_hard_nov50","kw_ph3_hard_mcn15", params=list(fitlim=c(0,20), jobs=10, load.behavs=T, behavs.sample=0.1, vars.group=c("Passes","Steps","Dispersion","Movement","TakerDist"), subpops=3))
fullStatistics(ph3e, expset.name="KW_PH3_E", fit.comp=T, fit.comp.par=list(snapshots=c(100,250,499)), fit.ind=T, behav.mean=T, som.group=T, som.alljobs=T)
fullStatistics(ph3h, expset.name="KW_PH3_H", fit.comp=T, fit.comp.par=list(snapshots=c(100,250,499)), fit.ind=T, behav.mean=T, som.group=T, som.alljobs=T)



ph5fit <- loadData("kw_ph5_fit", jobs=10, load.behavs=F, fitlim=c(0,10))
ph5mcn <- loadData("kw_ph5_mcn15", jobs=9, load.behavs=F, fitlim=c(0,10))
fullStatistics(ph5fit, ph5mcn, expset.name="KW_PH5", fit.comp=T, fit.comp.par=list(snapshots=c(100,250,499)))
fullStatistics(ph5mcn, expset.name="KW_PH5", fit.ind=T)


data <- read.table("~/GECCO/Energy/Centroid_70/2013-01-19_00-29-44/population.csv", sep="\t", header=FALSE, fill=TRUE, stringsAsFactors=FALSE)
data <- data[-1,-1]
data[,1] <- as.numeric(data[,1])
corrs <- c()
for(i in 2:ncol(data)) {
    corrs <- c(corrs, cor(data[,1], data[,i], method="pearson"))
}
plot(corrs, xlab="Feature index", ylab="Correlation", main="RS - Corr of SAS features with fitness (Pearson)", ylim=c(-1,1))
corrs <- c()
for(i in 2:ncol(data)) {
    corrs <- c(corrs, cor(data[,1], data[,i], method="spearman"))
}
plot(corrs, xlab="Feature index", ylab="Correlation", main="RS - Corr of SAS features with fitness (Spearman)", ylim=c(-1,1))

data <- read.table("~/GECCO/Aggregation/Centroid_70_10/2013-01-23_15-30-11/population.csv", sep="\t", header=FALSE, fill=TRUE, stringsAsFactors=FALSE)
data <- data[-1,-1]
data[,1] <- as.numeric(data[,1])
corrs <- c()
for(i in 2:ncol(data)) {
    corrs <- c(corrs, cor(data[,1], data[,i], method="pearson"))
}
plot(corrs, xlab="Feature index", ylab="Correlation", main="AGG - Corr of SAS features with fitness (Pearson)", ylim=c(-1,1))
corrs <- c()
for(i in 2:ncol(data)) {
    corrs <- c(corrs, cor(data[,1], data[,i], method="spearman"))
}
plot(corrs, xlab="Feature index", ylab="Correlation", main="AGG - Corr of SAS features with fitness (Spearman)", ylim=c(-1,1))


setwd("~/exps/")

datas <- metaLoadData("kw_ph_fit","kw_ph_nov50","kw_ph_nov70","kw_ph_mcn10","kw_ph_mcn15", params=list(fitlim=c(0,30), jobs=10, load.behavs=F, behavs.sample=0.1, vars.group=c("Passes","Steps","Dispersion","Movement","TakerDist"), subpops=3))
fullStatistics(datas, fit.comp.par=list(snapshots=c(100,250,499)), expset.name="KW_PH1", fit.ind=F, fit.comp=T, behav.mean=F, som.ind=F, som.group=F, som.alljobs=F, show.only=F)
analyse("kw_ph_fit","kw_ph_nov50","kw_ph_nov70","kw_ph_mcn10","kw_ph_mcn15", filename="fitness.stat", vars.pre=c("gen"),vars.sub=c("meansub","bestgensub","bestfarsub"), vars.post=c("meanall","bestall","bestfarall"), analyse="bestall", smooth=10)
analyse("kw_ph_mcn10","kw_ph_mcn15", filename="mcn.stat", vars.pre=c("gen"),vars.sub=c("novmean","novsd","novmax","boosted"), analyse="boosted", smooth=10)


datas <- metaLoadData("kw_ph_fit","kw_ph_nov50","kw_ph_nov70","kw_ph_mcn10","kw_ph_mcn15", params=list(fitlim=c(0,30), jobs=10, load.behavs=F))
do.call(fitnessComparisonPlots, c(datas, list(snapshots=c(100,250,499))))

hard <- loadData("kw_ph_hard_mcn10", fitlim=c(0,30), jobs=10, load.behavs=F)
fitnessComparisonPlots(hard, snapshots=c(100,250,499))

mcn15 <- loadData("kw_ph3_mcn15", fitlim=c(0,20), jobs=10, load.behavs=F)
fit <- loadData("kw_ph3_fit", fitlim=c(0,20), jobs=8, load.behavs=F)
fullStatistics(mcn15, fit, fit.comp.par=list(snapshots=c(100,250,499)), expset.name="KW_PH3", fit.comp=T)


data.hom <- loadData("kw_hom_nov_k7", expname="Keepaway", jobs=10, subpops=3, load.behavs=T, vars.group=c("Passes","Steps","Dispersion","Movement","TakerDist"), fitlim=c(0,40), behavs.sample=0.1)
bs <- extractBehaviours(data.hom)
cor(bs[,c("fitness",data.hom$vars.group)])


datas <- metaLoadData("kw_hom_fit","kw_hom_nov","kw_het_fit","kw_het_nov", params=list(fitlim=c(0,50), jobs=10, load.behavs=F))
do.call(fitnessComparisonPlots, c(datas, list(snapshots=c(100,250,499))))
datas2 <- metaLoadData("kw_hom_fit","kw_hom_nov","kw_hom_fit_rnd","kw_hom_nov_rnd", params=list(fitlim=c(0,50), jobs=10, load.behavs=F))
do.call(fitnessComparisonPlots, c(datas2, list(snapshots=c(100,250,499))))
datas3 <- metaLoadData("kw_hom_fit","kw_hom_fit_cro","kw_hom_fit_k7","kw_hom_fit_cro_k7","kw_hom_nov","kw_hom_nov_cro","kw_hom_nov_k7","kw_hom_nov_cro_k7", params=list(fitlim=c(0,50), jobs=10, load.behavs=F))
do.call(fitnessComparisonPlots, c(datas3, list(snapshots=c(100,250,499))))
datas4 <- metaLoadData("kw_het_fit","kw_het_fit_k7","kw_het_nov","kw_het_nov_k7", params=list(fitlim=c(0,50), jobs=10, load.behavs=F))
do.call(fitnessComparisonPlots, c(datas4, list(snapshots=c(100,250,499))))
individualFitnessPlots(datas4[[4]])

setwd("~/Dropbox/ECJ/experiments")
data.novts <- loadData("op_gen_ts", expname="TS", jobs=10, fitlim=c(0,3), subpops=3, load.behavs=F)
data.novgen <- loadData("op_gen_nov", expname="Gen", jobs=10, fitlim=c(0,3), subpops=3, load.behavs=F)
data.fit <- loadData("op_gen_fit", expname="Fit", jobs=10, fitlim=c(0,3), subpops=3, load.behavs=F)

data.gen70 <- loadData("op_gen_nov", expname="Gen 70", jobs=10, fitlim=c(0,3), subpops=3, load.behavs=F)
data.gen50 <- loadData("op_gen_nov50", expname="Gen 50", jobs=10, fitlim=c(0,3), subpops=3, load.behavs=F)
data.gen30 <- loadData("op_gen_nov30", expname="Gen 30", jobs=10, fitlim=c(0,3), subpops=3, load.behavs=F)
data.ts70 <- loadData("op_gen_ts", expname="TS 70", jobs=10, fitlim=c(0,3), subpops=3, load.behavs=F)
data.ts50 <- loadData("op_gen_ts50", expname="TS 50", jobs=10, fitlim=c(0,3), subpops=3, load.behavs=F)
data.ts30 <- loadData("op_gen_ts30", expname="TS 30", jobs=10, fitlim=c(0,3), subpops=3, load.behavs=F)
fullStatistics(data.gen70, data.gen50, data.gen30, data.ts70, data.ts50, data.ts30,
               fit.comp.par=list(snapshots=c(50,100,200,300,400,499)), expset.name="gen", fit.ind=T, fit.comp=T, behav.mean=F, som.ind=F, som.group=F, show.only=F)

data.kw <- loadData("keepaway_10", expname="Keepaway", jobs=10, load.behavs=F, fitlim=c(0,20))
fullStatistics(data.kw, fit.comp.par=list(snapshots=c(50,100,200,300,400,499)), expset.name="Keepaway", fit.ind=T, fit.comp=T, behav.mean=F, som.ind=F, som.group=F, show.only=F)

fullStatistics(data.novts, data.novgen, data.fit,
               fit.comp.par=list(snapshots=c(50,100,200,300,400,499)), expset.name="gen", fit.ind=T, fit.comp=T, behav.mean=F, som.ind=F, som.group=F, show.only=F)


data.fit <- loadData("op_gen_fit", expname="Fit", jobs=1, fitlim=c(0,3), subpops=3, load.behavs=T, behavs.sample=0.1,
                     vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"),
                     vars.group=c("g.gen","g.captured","g.time","g.finaldist","g.avgdist","g.avgdisp","g.maxdisp"), 
                     vars.file=c("g.gen","avgdist","mindist","angle","captured","movement","partneravg","partnermin","g.captured","g.time","g.finaldist","g.avgdist","g.avgdisp","g.maxdisp"))




# simple MCN

setwd("~/Dropbox/ECJ/novelty_results")
data.op.mcn150.extra <- loadData("oneprey_mcn_t1.5_k7", expname="MCN150-E", jobs=10, fitlim=c(0,3),  vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                           vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.avgdisp","g.maxdisp"), subpops=3, load.behavs=F, behavs.sample=0.25)
data.op.mcn150.simple <- loadData("op_mcn150_simple", expname="MCN150-S", jobs=10, fitlim=c(0,3), vars.file=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin","g.captured","g.time","g.finaldist"), 
                           vars.ind=c("avgdist","angle","captured","movement"), vars.group=c("g.captured","g.time","g.finaldist"), subpops=3, load.behavs=F, behavs.sample=0.25)
data.op.ns70.simple <- loadData("oneprey_simple_70", jobs=10, fitlim=c(0,3), 
                             vars.file=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin","g.captured","g.time","g.finaldist"), 
                             vars.ind=c("avgdist","angle","captured","movement"), vars.group=c("g.captured","g.time","g.finaldist"), 
                             subpops=3, expname="NS-70-S", load.behavs=F, behavs.sample=0.25)
data.op.ns70.extra <- loadData("oneprey_nov70",
                          jobs=10, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                          vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.avgdisp","g.maxdisp"), subpops=3, expname="NS-70-E", load.behavs=F, behavs.sample=0.25)
fullStatistics(data.op.mcn150.extra, data.op.mcn150.simple, data.op.ns70.extra,data.op.ns70.simple,
               fit.comp.par=list(snapshots=c(50,100,200,300,400,499)), expset.name="op.mcn.simple", fit.ind=F, fit.comp=T, behav.mean=F, som.ind=F, som.group=F, show.only=F)





data.oph.fw5 <- loadData("oneprey_hard_nov70", 
                          jobs=10, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                          vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion","g.maxdispersion"), subpops=3, expname="FW-05", load.behavs=F, behavs.sample=0.25)
data.oph.fw10 <- loadData("oph_mcn_t1.5_fw10", 
                          jobs=10, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                          vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion","g.maxdispersion"), subpops=3, expname="FW-10", load.behavs=F, behavs.sample=0.25)
data.oph.el5 <- loadData("oph_mcn_t1.5_elman5", 
                          jobs=10, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                          vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion","g.maxdispersion"), subpops=3, expname="EL-05", load.behavs=F, behavs.sample=0.25)
data.oph.el7 <- loadData("oph_mcn_t1.5_elman7", 
                         jobs=10, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                         vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion","g.maxdispersion"), subpops=3, expname="EL-07", load.behavs=F, behavs.sample=0.25)
fullStatistics(data.oph.fw5, data.oph.fw10, data.oph.el5,data.oph.el7,
               fit.comp.par=list(snapshots=c(50,100,200,300,400,499)), expset.name="oph.ann", fit.ind=T, fit.comp=F, behav.mean=F, som.ind=F, som.group=F, show.only=F)

data.op.mcn150 <- loadData("oneprey_mcn_t1.5_k7", expname="MCN-150", jobs=10, fitlim=c(0,3),  vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                                 vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.avgdisp","g.maxdisp"), subpops=3, load.behavs=T, behavs.sample=0.25)
data.op.mcn100 <- loadData("oneprey_mcn_t1_k7", expname="MCN-100", jobs=10, fitlim=c(0,3),  vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                           vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.avgdisp","g.maxdisp"), subpops=3, load.behavs=T, behavs.sample=0.25)
data.op.mcn75 <- loadData("oneprey_mcn_t0.75_k7", expname="MCN-075", jobs=10, fitlim=c(0,3),  vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                           vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.avgdisp","g.maxdisp"), subpops=3, load.behavs=T, behavs.sample=0.25)
data.op.nov70 <- loadData("oneprey_nov70",
                          jobs=10, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                          vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.avgdisp","g.maxdisp"), subpops=3, expname="NS-70", load.behavs=T, behavs.sample=0.25)
data.op.nov50 <- loadData("oneprey_nov50",
                          jobs=10, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                          vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.avgdisp","g.maxdisp"), subpops=3, expname="NS-70", load.behavs=T, behavs.sample=0.25)
data.op.fit <- loadData("oneprey_fit", 
                        jobs=10, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                        vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.avgdisp","g.maxdisp"), subpops=3, expname="Fit", load.behavs=T, behavs.sample=0.25)

md <- function(x) frameFusion(interPopDiversity(x))$all.avg
df <- data.frame(gen=data.op.mcn100$gens, mcn75=md(data.op.mcn75), mcn100=md(data.op.mcn100), mcn150=md(data.op.mcn150), ns70=md(data.op.nov70),fit=md(data.op.fit), ns50=md(data.op.nov50))
plotMultiline(smooth(df, window=25), ylim=NULL, title="Inter pop diversity - ind vars")

md <- function(x) frameFusion(intraPopDiversity(x))$all.avg
df2 <- data.frame(gen=data.op.mcn100$gens, mcn75=md(data.op.mcn75), mcn100=md(data.op.mcn100), mcn150=md(data.op.mcn150), ns70=md(data.op.nov70),fit=md(data.op.fit), ns50=md(data.op.nov50))
plotMultiline(smooth(df2, window=25), ylim=NULL, title="Intra pop diversity - group vars")

md <- function(x) frameFusion(intraPopDiversity(x, vars=data.op.fit$vars.ind))$all.avg
df3 <- data.frame(gen=data.op.mcn100$gens, mcn75=md(data.op.mcn75), mcn100=md(data.op.mcn100), mcn150=md(data.op.mcn150), ns70=md(data.op.nov70),fit=md(data.op.fit), ns50=md(data.op.nov50))
plotMultiline(smooth(df3, window=25), ylim=NULL, title="Intra pop diversity - ind vars")

md <- function(x) frameFusion(interPopDiversity(x, vars=data.op.fit$vars.group))$all.avg
df4 <- data.frame(gen=data.op.mcn100$gens, mcn75=md(data.op.mcn75), mcn100=md(data.op.mcn100), mcn150=md(data.op.mcn150), ns70=md(data.op.nov70),fit=md(data.op.fit), ns50=md(data.op.nov50))
plotMultiline(smooth(df, window=25), ylim=NULL, title="Inter pop diversity - group vars")


df <- data.frame(gen=data.mpe.fit$gens, fit=md(data.mpe.fit), ns70=md(data.mpe.nov70), 
                 ns50=md(data.mpe.nov50), mcn100=md(data.mpe.mcn100), mcn150=md(data.mpe.mcn150))
dfs <- smooth(df, window=10)
plotMultiline(dfs, ylim=NULL)
analyse("mpe_mcn_t1","mpe_mcn_t1.5","multiprey_easy_nov50", "multiprey_easy_nov70",  filename="fitness.stat", vars.pre=c("gen"), vars.sub=c("meanfit","bestfit","bestfar"), vars.post=c("popmeanfit","popbestfit","popbestfitfar"), gens=NULL, analyse="popmeanfit", smooth=10)
analyse("mpe_mcn_t1","mpe_mcn_t1.5","multiprey_easy_nov50", "multiprey_easy_nov70",  filename="fitness.stat", vars.pre=c("gen"), vars.sub=c("meanfit","bestfit","bestfar"), vars.post=c("popmeanfit","popbestfit","popbestfitfar"), gens=NULL, analyse="popbestfit", smooth=10)


md2 <- function(x) frameFusion(intraPopDiversity(x))$all.sd
df2 <- data.frame(gen=data.op.fit$gens, fit=md(data.op.fit), ns70=md(data.op.nov70), 
                 mcn050=md(data.op.mcn.050.7), mcn100=md(data.op.mcn.100.7),
                 mcn150=md(data.op.mcn.150.7), mcn175=md(data.op.mcn.175.7))
plotMultiline(df2, ylim=NULL)

analyse("oneprey_mcn_t1_k7", "oneprey_mcn_t1.25_k7","oneprey_mcn_t1.5_k7","oneprey_mcn_t1.75_k7", filename="mcnovelty.stat", vars.gen=c("gen"), vars.sub=c("nov.mean","nov.sd","nov.max","boosted"), gens=NULL, analyse="boosted.mean")
analyse("oneprey_mcn_t1_k7", "oneprey_mcn_t1.25_k7","oneprey_mcn_t1.5_k7","oneprey_mcn_t1.75_k7", filename="fitness.stat", vars.gen=c("gen"), vars.sub=c("meanfit","bestfit","bestfar"), vars.post=c("popmeanfit","popbestfit","popbestfitfar"), gens=NULL, analyse="popmeanfit")

analyse("oneprey_mcn_t0.25_k7","oneprey_mcn_t0.5_k7","oneprey_mcn_t0.75_k7","oneprey_mcn_t1_k7", "oneprey_mcn_t1.25_k7","oneprey_mcn_t1.5_k7","oneprey_mcn_t1.75_k7", filename="mcnovelty.stat", vars.pre=c("gen"), vars.sub=c("nov.mean","nov.sd","nov.max","boosted"), gens=NULL, analyse="boosted.mean", smooth=10)
analyse("oneprey_mcn_t0.25_k7","oneprey_mcn_t0.5_k7","oneprey_mcn_t0.75_k7","oneprey_mcn_t1_k7", "oneprey_mcn_t1.25_k7","oneprey_mcn_t1.5_k7","oneprey_mcn_t1.75_k7","~/Dropbox/ECJ/novelty_results/oneprey_fit","~/Dropbox/ECJ/novelty_results/oneprey_nov70",  filename="fitness.stat", vars.pre=c("gen"), vars.sub=c("meanfit","bestfit","bestfar"), vars.post=c("popmeanfit","popbestfit","popbestfitfar"), gens=NULL, analyse="popmeanfit", smooth=10)
analyse("oneprey_mcn_t0.25_k7","oneprey_mcn_t0.5_k7","oneprey_mcn_t0.75_k7","oneprey_mcn_t1_k7", "oneprey_mcn_t1.25_k7","oneprey_mcn_t1.5_k7","oneprey_mcn_t1.75_k7","~/Dropbox/ECJ/novelty_results/oneprey_fit","~/Dropbox/ECJ/novelty_results/oneprey_nov70",  filename="fitness.stat", vars.pre=c("gen"), vars.sub=c("meanfit","bestfit","bestfar"), vars.post=c("popmeanfit","popbestfit","popbestfitfar"), gens=NULL, analyse="popbestfit", smooth=10)



setwd("~/Dropbox/ECJ/novelty_results/")
data.op.nov70 <- loadData("oneprey_nov70", 
                          jobs="job.5", fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                          vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion","g.maxdispersion"), subpops=3, expname="NS-70", load.behavs=T, behavs.sample=0.25)
res.nov <- intraPopDiversity(data.op.nov70)
data.op.fit <- loadData("oneprey_fit", 
                        jobs="job.1", fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                        vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion"), subpops=3, expname="Fit", load.behavs=T, behavs.sample=0.25)
res.fit <- intraPopDiversity(data.op.fit)
plotMultiline(res.nov[[1]][,c("gen","sub.0.sd","sub.1.sd","sub.2.sd")], title="Nov")
plotMultiline(res.fit[[1]][,c("gen","sub.0.sd","sub.1.sd","sub.2.sd")], title="Fit")

setwd("~/Dropbox/ECJ/novelty_results/")
data.op.nov70 <- loadData("oneprey_nov70", gens=0:299,
                          jobs=10, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                          vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion","g.maxdispersion"), subpops=3, expname="NS-70", load.behavs=T, behavs.sample=0.25)
data.op.fit <- loadData("oneprey_fit", gens=0:299,
                        jobs=10, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                        vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion","g.maxdispersion"), subpops=3, expname="Fit", load.behavs=T, behavs.sample=0.25)
setwd("~/Dropbox/ECJ/experiments/")
data.mcn <- loadData("oneprey_mcn_1.5", 
                          jobs=2, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                          vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion","g.maxdispersion"), subpops=3, expname="MCN", load.behavs=T, behavs.sample=0.5)
soms <- fullStatistics(data.mcn, data.op.nov70, data.op.fit,
               fit.comp.par=list(snapshots=c(50,100,200,299)), expset.name="op.mcn", fit.ind=F, fit.comp=F, behav.mean=F, som.ind=T, som.group=T, show.only=F)
explorationVideo(soms$group, filterJobs(data.mcn, "job.0"), mergeSubpops=T, accumulate=F, interval=5, fps=3)
explorationVideo(soms$group, filterJobs(data.mcn, "job.0"), mergeSubpops=F, accumulate=F, interval=5, fps=3)

# Minimal Novelty Criteria results

setwd("~/Dropbox/ECJ/experiments/")
lb <- T ; bs <- 0.25 ; f <- c(0,1) ; vg <- c("captured","duration","pred.avgdisp","pred.maxdisp","prey.avgdisp")
data.mpe.mcn150 <- loadData("mpe_mcn_t1.5", expname="MCN-T1.5", jobs=10, fitlim=f, vars.group=vg, subpops=3, load.behavs=lb, behavs.sample=bs)
data.mpe.mcn100 <- loadData("mpe_mcn_t1", expname="MCN-T1.0", jobs=10, fitlim=f, vars.group=vg, subpops=3, load.behavs=lb, behavs.sample=bs)
data.mpe.fit <- loadData("~/Dropbox/ECJ/novelty_results/multiprey_easy_fit", expname="Fit", jobs=10, fitlim=f, vars.group=vg, subpops=3,  load.behavs=lb, behavs.sample=bs, fitness.file="extended.stat")
data.mpe.nov70 <- loadData("~/Dropbox/ECJ/novelty_results/multiprey_easy_nov70", expname="NS-70", jobs=10, fitlim=f, vars.group=vg, subpops=3,  load.behavs=lb, behavs.sample=bs)
data.mpe.nov50 <- loadData("~/Dropbox/ECJ/novelty_results/multiprey_easy_nov50", expname="NS-50", jobs=10, fitlim=f, vars.group=vg, subpops=3,  load.behavs=lb, behavs.sample=bs)
fullStatistics(data.mpe.fit,data.mpe.mcn100,data.mpe.mcn150,data.mpe.nov70,data.mpe.nov50,
               fit.comp.par=list(snapshots=c(50,100,150,200,249)), expset.name="mpe.mcn", fit.ind=F, fit.comp=F, behav.mean=T, som.ind=F, som.group=F, som.alljobs=F)


data.op.nov70 <- loadData("oneprey_nov70", 
                          jobs=10, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                          vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion","g.maxdispersion"), subpops=3, expname="NS-70", load.behavs=F, behavs.sample=0.25)
data.op.fit <- loadData("oneprey_fit", 
                        jobs=10, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                        vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion"), subpops=3, expname="Fit", load.behavs=F, behavs.sample=0.25)

lb <- F ; bs <- 0.25 ; f <- c(0,3) ; vi <- c("avgdist","mindist","angle","captured","movement","partneravg","partnermin") ; vg <- c("g.captured","g.time","g.finaldist","g.avgdist","g.avgdisp","g.maxdisp")
data.op.mcn.150.2 <- loadData("oneprey_mcn_t1.5_k2", expname="MCN T1.5 K2", jobs=10, fitlim=f, vars.ind=vi, vars.group=vg, subpops=3, load.behavs=lb, behavs.sample=bs)
data.op.mcn.125.2 <- loadData("oneprey_mcn_t1.25_k2", expname="MCN T1.25 K2", jobs=10, fitlim=f, vars.ind=vi, vars.group=vg, subpops=3, load.behavs=lb, behavs.sample=bs)
data.op.mcn.100.2 <- loadData("oneprey_mcn_t1_k2", expname="MCN T1 K2", jobs=10, fitlim=f, vars.ind=vi, vars.group=vg, subpops=3, load.behavs=lb, behavs.sample=bs)
data.op.mcn.175.2 <- loadData("oneprey_mcn_t1.75_k2", expname="MCN T1.75 K2", jobs=4, fitlim=f, vars.ind=vi, vars.group=vg, subpops=3, load.behavs=lb, behavs.sample=bs)

lb <- T ; bs <- 0.2 ; f <- c(0,3) ; vi <- c("avgdist","mindist","angle","captured","movement","partneravg","partnermin") ; vg <- c("g.captured","g.time","g.finaldist","g.avgdist","g.avgdisp","g.maxdisp")
data.op.mcn.150.7 <- loadData("oneprey_mcn_t1.5_k7", expname="MCN T1.5", jobs=10, fitlim=f, vars.ind=vi, vars.group=vg, subpops=3, load.behavs=lb, behavs.sample=bs)
data.op.mcn.125.7 <- loadData("oneprey_mcn_t1.25_k7", expname="MCN T1.25", jobs=10, fitlim=f, vars.ind=vi, vars.group=vg, subpops=3, load.behavs=lb, behavs.sample=bs)
data.op.mcn.100.7 <- loadData("oneprey_mcn_t1_k7", expname="MCN T1", jobs=10, fitlim=f, vars.ind=vi, vars.group=vg, subpops=3, load.behavs=lb, behavs.sample=bs)
data.op.mcn.175.7 <- loadData("oneprey_mcn_t1.75_k7", expname="MCN T1.75", jobs=10, fitlim=f, vars.ind=vi, vars.group=vg, subpops=3, load.behavs=lb, behavs.sample=bs)
data.op.mcn.075.7 <- loadData("oneprey_mcn_t0.75_k7", expname="MCN T0.75", jobs=10, fitlim=f, vars.ind=vi, vars.group=vg, subpops=3, load.behavs=lb, behavs.sample=bs)
data.op.mcn.050.7 <- loadData("oneprey_mcn_t0.5_k7", expname="MCN T0.50", jobs=10, fitlim=f, vars.ind=vi, vars.group=vg, subpops=3, load.behavs=lb, behavs.sample=bs)
data.op.mcn.025.7 <- loadData("oneprey_mcn_t0.25_k7", expname="MCN T0.25", jobs=10, fitlim=f, vars.ind=vi, vars.group=vg, subpops=3, load.behavs=lb, behavs.sample=bs)
data.op.nov70 <- loadData("~/Dropbox/ECJ/novelty_results/oneprey_nov70", expname="NS-70", jobs=10, fitlim=f, vars.ind=vi, vars.group=vg, subpops=3,  load.behavs=lb, behavs.sample=bs)
data.op.fit <- loadData("~/Dropbox/ECJ/novelty_results/oneprey_fit", expname="Fit", jobs=10, fitlim=f, vars.ind=vi, vars.group=vg, subpops=3,  load.behavs=lb, behavs.sample=bs)

soms2 <- fullStatistics(data.op.mcn.150.7,data.op.mcn.100.7,
                        expset.name="op.mcn.videos", fit.ind=F, fit.comp=F, behav.mean=F, som.ind=F, som.group=T, som.alljobs=F, show.only=T)
explorationVideo(soms2$group, filterJobs(data.op.mcn.100.7, "job.2"), mergeSubpops=T, accumulate=F, interval=5, fps=5)
explorationVideo(soms2$group, filterJobs(data.op.mcn.150.7, "job.6"), mergeSubpops=T, accumulate=F, interval=5, fps=5)

fullStatistics(data.op.mcn.150.2,data.op.mcn.150.7,data.op.mcn.125.2,data.op.mcn.125.7,data.op.mcn.100.2,data.op.mcn.100.7,data.op.mcn.175.2,data.op.mcn.175.7,
               fit.comp.par=list(snapshots=c(50,100,200,300,400,499)), expset.name="op.mcn", fit.ind=T, fit.comp=T, behav.mean=F, som.ind=F, som.group=F)

soms2 <- fullStatistics(data.op.mcn.150.7,data.op.mcn.125.7,data.op.mcn.100.7,data.op.mcn.175.7,data.op.mcn.075.7,data.op.mcn.050.7,data.op.mcn.025.7,data.op.nov70,data.op.fit,
               fit.comp.par=list(snapshots=c(50,100,200,300,400,499)), expset.name="op.mcn7", fit.ind=F, fit.comp=F, behav.mean=F, som.ind=T, som.group=F, som.alljobs=F)
explorationVideo(soms2$ind, filterJobs(data.op.mcn.100.7, "job.2"), mergeSubpops=F, accumulate=F, interval=5, fps=5)
explorationVideo(soms$group, filterJobs(data.op.mcn.100.7, "job.2"), mergeSubpops=T, accumulate=F, interval=5, fps=5)

# Fitness-only elite results

setwd("~/Dropbox/ECJ/novelty_results/")
data.op.nov100 <- loadData("oneprey_nov100", 
                           jobs=20, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                           vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion","g.maxdispersion"), subpops=3, expname="NS-100", load.behavs=F, behavs.sample=0.25)
data.op.nov70 <- loadData("oneprey_nov70", 
                          jobs=20, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                          vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion","g.maxdispersion"), subpops=3, expname="NS-70", load.behavs=F, behavs.sample=0.25)
data.op.el.nov100 <- loadData("oneprey_fitelite_100", 
                           jobs=20, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                           vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion","g.maxdispersion"), subpops=3, expname="NS-EL-100", load.behavs=F, behavs.sample=0.25)
data.op.el.nov70 <- loadData("oneprey_fitelite_70", 
                          jobs=20, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                          vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion","g.maxdispersion"), subpops=3, expname="NS-EL-70", load.behavs=F, behavs.sample=0.25)
data.op.fit <- loadData("oneprey_fit", 
                        jobs=20, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                        vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion"), subpops=3, expname="Fit", load.behavs=F, behavs.sample=0.25)
fullStatistics(data.op.nov100,data.op.nov70,data.op.el.nov100,data.op.el.nov70,data.op.fit,
                       fit.comp.par=list(snapshots=c(50,100,200,300,400,499)), expset.name="op.elite", fit.ind=F, fit.comp=T, behav.mean=F, som.ind=F, som.group=F)

# Simple results

setwd("~/Dropbox/ECJ/novelty_results/")

data.op.simple70 <- loadData("oneprey_simple_70", jobs=10, fitlim=c(0,3), 
                             vars.file=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin","g.captured","g.time","g.finaldist"), 
                             vars.ind=c("avgdist","angle","captured","movement"), vars.group=c("g.captured","g.time","g.finaldist"), 
                             subpops=3, expname="Simple-70", load.behavs=T, behavs.sample=0.5)
data.op.extra70 <- loadData("oneprey_nov70", jobs=10, fitlim=c(0,3), 
                            vars.file=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin","g.captured","g.time","g.finaldist","g.avgdist","g.avgdisp","g.maxdisp"), 
                            vars.ind=c("avgdist","angle","captured","movement"), vars.group=c("g.captured","g.time","g.finaldist"), 
                            subpops=3, expname="Extra-70", load.behavs=T, behavs.sample=0.5)
data.op.fit <- loadData("oneprey_fit", jobs=10, fitlim=c(0,3), 
                        vars.file=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin","g.captured","g.time","g.finaldist","g.avgdist","g.avgdisp","g.maxdisp"), 
                        vars.ind=c("avgdist","angle","captured","movement"), vars.group=c("g.captured","g.time","g.finaldist"), 
                        subpops=3, expname="Fit", load.behavs=T, behavs.sample=0.5)

soms <- fullStatistics(data.op.simple70,data.op.extra70,data.op.fit,
               fit.comp.par=list(snapshots=c(50,100,200,300,400,499)), expset.name="op.simple", fit.ind=F, fit.comp=F, behav.mean=F, som.ind=T, som.group=T)
explorationVideo(soms$group, filterJobs(data.op.simple70, "job.0"), mergeSubpops=T, accumulate=F, interval=5, fps=5)
explorationVideo(soms$group, filterJobs(data.op.fit, "job.1"), mergeSubpops=T, accumulate=F, interval=5, fps=5)
explorationVideo(soms$ind, filterJobs(data.op.simple70, "job.0"), mergeSubpops=F, accumulate=F, interval=5, fps=5)
explorationVideo(soms$ind, filterJobs(data.op.fit, "job.1"), mergeSubpops=F, accumulate=F, interval=5, fps=5)

data.mpe.simple70 <- loadData("multiprey_easy_simple_70", 
                             jobs=10, fitlim=c(0,1), vars.group=c("captured","duration","prey.avgdisp"), 
                             subpops=3, expname="Simple-70", load.behavs=T, behavs.sample=0.25)
data.mpe.extra70 <- loadData("multiprey_easy_nov70", vars.file=c("captured","duration","pred.avgdisp","pred.maxdisp","prey.avgdisp"),
                           jobs=10, fitlim=c(0,1), vars.group=c("captured","duration","prey.avgdisp"), 
                           subpops=3, expname="Extra-70", load.behavs=T, behavs.sample=0.25)
data.mpe.fit <- loadData("multiprey_easy_fit", vars.file=c("captured","duration","pred.avgdisp","pred.maxdisp","prey.avgdisp"),
                         jobs=10, fitlim=c(0,1), vars.group=c("captured","duration","prey.avgdisp"), 
                         vars.transform=list("pred.avgdisp"=c(0,2,0,1),"pred.maxdisp"=c(0,2,0,1)), subpops=3, 
                         expname="Fit", fitness.file="extended.stat", load.behavs=T, behavs.sample=0.25)

soms <- fullStatistics(data.mpe.simple70,data.mpe.extra70,data.mpe.fit,
               fit.comp.par=list(snapshots=c(50,100,150,200,249)), expset.name="mpe.simple", fit.ind=T, fit.comp=F, behav.mean=T, som.ind=F, som.group=T)
explorationVideo(soms$group, filterJobs(data.mpe.simple70, "job.4"), mergeSubpops=T, accumulate=F, interval=5, fps=5)
explorationVideo(soms$group, filterJobs(data.mpe.fit, "job.1"), mergeSubpops=T, accumulate=F, interval=5, fps=5)

data.mph.simple70 <- loadData("multiprey_hard_simple_70", 
                              jobs=10, fitlim=c(0,1), vars.group=c("captured","duration","prey.avgdisp"), 
                              subpops=3, expname="Simple-70", load.behavs=T, behavs.sample=1)
data.mph.extra70 <- loadData("multiprey_hard_nov70", vars.file=c("captured","duration","pred.avgdisp","pred.maxdisp","prey.avgdisp"),
                           jobs=10, fitlim=c(0,1), vars.group=c("captured","duration","prey.avgdisp"), 
                           subpops=3, expname="Extra-70", load.behavs=T, behavs.sample=0.25)
data.mph.fit <- loadData("multiprey_hard_fit", vars.file=c("captured","duration","pred.avgdisp","pred.maxdisp","prey.avgdisp"),
                         jobs=10, fitlim=c(0,1), vars.group=c("captured","duration","prey.avgdisp"), 
                         vars.transform=list("pred.avgdisp"=c(0,2,0,1),"pred.maxdisp"=c(0,2,0,1)), subpops=3, 
                         expname="Fit", fitness.file="extended.stat", load.behavs=T, behavs.sample=0.25)

fullStatistics(data.mph.simple70,data.mph.extra70,data.mph.fit,
               fit.comp.par=list(snapshots=c(50,100,150,200,249)), expset.name="mph.simple", fit.ind=T, fit.comp=F, behav.mean=T, som.ind=F, som.group=T)

# LN results

setwd("~/Dropbox/ECJ/novelty_results/")
lim <- c(0,3) ; loadb <- TRUE ; sampleb <- 0.5 ; sn <- 3
ind <- c("avgdist","mindist","angle","captured","movement","partneravg","partnermin")
group <- c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion","g.maxdispersion")
data.fit <- loadData("/home/jorge/Dropbox/ECJ/novelty_results/oneprey_fit", jobs=10, fitlim=lim, vars.ind=ind, vars.group=group, subpops=sn, expname="fit", load.behavs=loadb, behavs.sample=sampleb)
data.ln02g <- loadData("oneprey_ln02g", jobs=10, fitlim=lim, vars.ind=ind, vars.group=group, subpops=sn, expname="LN-G-02", load.behavs=loadb, behavs.sample=sampleb)
data.ln05g <- loadData("oneprey_ln05g", jobs=10, fitlim=lim, vars.ind=ind, vars.group=group, subpops=sn, expname="LN-G-05", load.behavs=loadb, behavs.sample=sampleb)
data.ln10g <- loadData("oneprey_ln10g", jobs=10, fitlim=lim, vars.ind=ind, vars.group=group, subpops=sn, expname="LN-G-10", load.behavs=loadb, behavs.sample=sampleb)
data.ln02i <- loadData("oneprey_ln02i", jobs=10, fitlim=lim, vars.ind=ind, vars.group=group, subpops=sn, expname="LN-I-02", load.behavs=loadb, behavs.sample=sampleb)
data.ln05i <- loadData("oneprey_ln05i", jobs=10, fitlim=lim, vars.ind=ind, vars.group=group, subpops=sn, expname="LN-I-05", load.behavs=loadb, behavs.sample=sampleb)
soms <- fullStatistics(data.fit, data.ln05g,
               fit.comp.par=list(snapshots=c(50,100,200,300,400,499)), expset.name="ln.op", fit.ind=FALSE, fit.comp=FALSE, behav.mean=FALSE, som.ind=TRUE, som.group=TRUE)

explorationVideo(soms$group, filterJobs(data.ln05g, "job.3"), mergeSubpops=T, accumulate=F, interval=5, fps=5)
explorationVideo(soms$group, filterJobs(data.fit, "job.1"), mergeSubpops=T, accumulate=F, interval=5, fps=5)
explorationVideo(soms$ind, filterJobs(data.ln05g, "job.3"), mergeSubpops=F, accumulate=F, interval=5, fps=5)
explorationVideo(soms$ind, filterJobs(data.fit, "job.1"), mergeSubpops=F, accumulate=F, interval=5, fps=5)

# STO results

setwd("~/Dropbox/ECJ/novelty_results/")
lim <- c(0,3) ; loadb <- TRUE ; sampleb <- 0.1 ; sn <- 3
ind <- c("avgdist","mindist","angle","captured","movement","partneravg","partnermin")
group <- c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion")
data.op.sto0 <- loadData("oneprey_fit", jobs=20, fitlim=lim, vars.ind=ind, vars.group=group, subpops=sn, expname="STO-000", load.behavs=loadb, behavs.sample=sampleb)
data.op.sto10 <- loadData("oneprey_sto10", jobs=10, fitlim=lim, vars.ind=ind, vars.group=group, subpops=sn, expname="STO-010", load.behavs=loadb, behavs.sample=sampleb)
data.op.sto20 <- loadData("oneprey_sto20", jobs=10, fitlim=lim, vars.ind=ind, vars.group=group, subpops=sn, expname="STO-020", load.behavs=loadb, behavs.sample=sampleb)
data.op.sto35 <- loadData("oneprey_sto35", jobs=10, fitlim=lim, vars.ind=ind, vars.group=group, subpops=sn, expname="STO-035", load.behavs=loadb, behavs.sample=sampleb)
data.op.sto50 <- loadData("oneprey_sto50", jobs=10, fitlim=lim, vars.ind=ind, vars.group=group, subpops=sn, expname="STO-050", load.behavs=loadb, behavs.sample=sampleb)
data.op.sto70 <- loadData("oneprey_sto70", jobs=10, fitlim=lim, vars.ind=ind, vars.group=group, subpops=sn, expname="STO-070", load.behavs=loadb, behavs.sample=sampleb)
data.op.sto90 <- loadData("oneprey_sto90", jobs=10, fitlim=lim, vars.ind=ind, vars.group=group, subpops=sn, expname="STO-090", load.behavs=loadb, behavs.sample=sampleb)
data.op.sto100 <- loadData("oneprey_nov100", jobs=20, fitlim=lim, vars.ind=ind, vars.group=group, subpops=sn, expname="STO-100", load.behavs=loadb, behavs.sample=sampleb)
fullStatistics(data.op.sto0, data.op.sto10, data.op.sto20, data.op.sto35, data.op.sto50, data.op.sto70, data.op.sto90, data.op.sto100,
               fit.comp.par=list(snapshots=c(50,100,200,300,400,499)), expset.name="sto.op", fit.ind=FALSE, fit.comp=FALSE, behav.mean=TRUE, som.ind=TRUE, som.group=TRUE)



# novelty results 2

setwd("~/Dropbox/ECJ/novelty_results/")

data.op.nov100 <- loadData("oneprey_nov100", 
                     jobs=20, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                     vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion"), subpops=3, expname="NS-100", load.behavs=TRUE, behavs.sample=0.25)
data.op.nov70 <- loadData("oneprey_nov70", 
                       jobs=20, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                       vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion"), subpops=3, expname="NS-70", load.behavs=TRUE, behavs.sample=0.25)
data.op.nov50 <- loadData("oneprey_nov50", 
                         jobs=10, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                         vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion"), subpops=3, expname="NS-50", load.behavs=TRUE, behavs.sample=0.25)
data.op.nov25 <- loadData("oneprey_nov25", 
                          jobs=10, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                          vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion"), subpops=3, expname="NS-25", load.behavs=TRUE, behavs.sample=0.25)
data.op.fit <- loadData("oneprey_fit", 
                     jobs=20, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                     vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion"), subpops=3, expname="Fit", load.behavs=TRUE, behavs.sample=0.25)

fullStatistics(data.op.fit, data.op.nov25, data.op.nov50, data.op.nov70, data.op.nov100, 
               fit.ind=TRUE, fit.comp=TRUE, behav.mean=TRUE, som.ind=TRUE, som.group=TRUE,
               fit.comp.par=list(snapshots=c(50,100,200,300,400,499)), expset.name="oneprey")
rm(data.op.fit, data.op.nov25, data.op.nov50, data.op.nov70, data.op.nov100) ; gc()

data.mpe.nov25 <- loadData("multiprey_easy_nov25", 
                       jobs=10, fitlim=c(0,1), vars.group=c("captured","duration","pred.avgdisp","pred.maxdisp","prey.avdisp"), 
                       subpops=3, expname="NS-25", load.behavs=TRUE, behavs.sample=0.25)
data.mpe.nov50 <- loadData("multiprey_easy_nov50", 
                       jobs=10, fitlim=c(0,1), vars.group=c("captured","duration","pred.avgdisp","pred.maxdisp","prey.avdisp"), 
                       subpops=3, expname="NS-50", load.behavs=TRUE, behavs.sample=0.25)
data.mpe.nov70 <- loadData("multiprey_easy_nov70", 
                       jobs=10, fitlim=c(0,1), vars.group=c("captured","duration","pred.avgdisp","pred.maxdisp","prey.avdisp"), 
                       subpops=3, expname="NS-70", load.behavs=TRUE, behavs.sample=0.25)
data.mpe.fit <- loadData("multiprey_easy_fit", 
                     jobs=20, fitlim=c(0,1), vars.group=c("captured","duration","pred.avgdisp","pred.maxdisp","prey.avdisp"), 
                     vars.transform=list("pred.avgdisp"=c(0,2,0,1),"pred.maxdisp"=c(0,2,0,1)), subpops=3, 
                     expname="Fit", fitness.file="extended.stat", load.behavs=TRUE, behavs.sample=0.25)

fullStatistics(data.mpe.nov25, data.mpe.nov50, data.mpe.nov70, data.mpe.fit, 
               fit.ind=TRUE, fit.comp=TRUE, behav.mean=TRUE, som.ind=FALSE, som.group=TRUE,
               fit.comp.par=list(snapshots=c(50,100,150,200,249)), expset.name="mpe")
rm(data.mpe.nov25, data.mpe.nov50, data.mpe.nov70, data.mpe.fit) ; gc()

data.mph.nov25 <- loadData("multiprey_hard_nov25", 
                           jobs=10, fitlim=c(0,1), vars.group=c("captured","duration","pred.avgdisp","pred.maxdisp","prey.avdisp"), 
                           subpops=3, expname="NS-25", load.behavs=TRUE, behavs.sample=0.25)
data.mph.nov50 <- loadData("multiprey_hard_nov50", 
                           jobs=10, fitlim=c(0,1), vars.group=c("captured","duration","pred.avgdisp","pred.maxdisp","prey.avdisp"), 
                           subpops=3, expname="NS-50", load.behavs=TRUE, behavs.sample=0.25)
data.mph.nov70 <- loadData("multiprey_hard_nov70", 
                           jobs=10, fitlim=c(0,1), vars.group=c("captured","duration","pred.avgdisp","pred.maxdisp","prey.avdisp"), 
                           subpops=3, expname="NS-70", load.behavs=TRUE, behavs.sample=0.25)
data.mph.fit <- loadData("multiprey_hard_fit", 
                         jobs=20, fitlim=c(0,1), vars.group=c("captured","duration","pred.avgdisp","pred.maxdisp","prey.avdisp"), 
                         vars.transform=list("pred.avgdisp"=c(0,2,0,1),"pred.maxdisp"=c(0,2,0,1)), subpops=3, 
                         expname="Fit", fitness.file="extended.stat", load.behavs=TRUE, behavs.sample=0.25)

fullStatistics(data.mph.nov25, data.mph.nov50, data.mph.nov70, data.mph.fit, 
               fit.ind=TRUE, fit.comp=TRUE, behav.mean=TRUE, som.ind=FALSE, som.group=TRUE,
               fit.comp.par=list(snapshots=c(50,100,150,200,249)), expset.name="mph")
rm(data.mph.nov25, data.mph.nov50, data.mph.nov70, data.mph.fit) ; gc()

# predator results

setwd("~/Dropbox/ECJ/predator_results/")

data.ose <- loadData("oneprey_short_easy", 
                 jobs=30, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                 vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion"), subpops=3, expname="One prey short easy")

data.ole <- loadData("oneprey_long_easy", 
                 jobs=10, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                 vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion"), subpops=3, expname="One prey long easy")

data.olh <- loadData("oneprey_long_hard", 
                 jobs=10, fitlim=c(0,3), vars.ind=c("avgdist","mindist","angle","captured","movement","partneravg","partnermin"), 
                 vars.group=c("g.captured","g.time","g.finaldist","g.avgdist","g.dispersion"), subpops=3, expname="One prey long hard")

data.m3e <- loadData("multiprey_3_easy", jobs=20, fitlim=c(0,1), 
                     vars.group=c("captured","duration","pred.avgdisp","pred.maxdisp","prey.avdisp"), subpops=3, expname="Multi prey 3 easy")

data.m3h <- loadData("multiprey_3_hard", jobs=20, fitlim=c(0,1), 
                     vars.group=c("captured","duration","pred.avgdisp","pred.maxdisp","prey.avdisp"), subpops=3, expname="Multi prey 3 hard")

data.m4h <- loadData("multiprey_4_hard", jobs=20, fitlim=c(0,1), 
                     vars.group=c("captured","duration","pred.avgdisp","pred.maxdisp","prey.avdisp"), subpops=3, expname="Multi prey 4 hard")

som.ose <- buildSom(data.ose, sample.size=50000)
map.ose <- mapMergeSubpops(som.ose, data.ose)
fitnessMetrics(data.ose, file="ose_fitness.pdf", show=FALSE)
fitnessSomPlot(som.ose, data.ose, map.ose, file="ose_som.pdf", show=FALSE)
fitnessHeatmap(som.ose, data.ose, file="ose_heat.pdf", show=FALSE)
pdf("ose_codes.pdf") ; plot(som.ose) ; dev.off()
som.ose.ind <- buildSom(data.ose, sample.size=50000, variables=data.ose$vars.ind)
map.ose.ind <- mapIndividualSubpops(som.ose.ind, data.ose)
fitnessSomPlot(som.ose.ind, data.ose, map.ose.ind, file="ose_som_ind.pdf", show=FALSE)
fitnessHeatmap(som.ose.ind, data.ose, file="ose_heat_ind.pdf", show=FALSE)
pdf("ose_codes_ind.pdf") ; plot(som.ose.ind) ; dev.off()
data.ose <- reduceMean(data.ose)
fitnessMeanBehaviour(data.ose, file="ose_means.pdf", show=FALSE)

som.ole <- buildSom(data.ole, sample.size=50000)
map.ole <- mapMergeSubpops(som.ole, data.ole)
fitnessMetrics(data.ole, file="ole_fitness.pdf", show=FALSE)
fitnessSomPlot(som.ole, data.ole, map.ole, file="ole_som.pdf", show=FALSE)
fitnessHeatmap(som.ole, data.ole, file="ole_heat.pdf", show=FALSE)
pdf("ole_codes.pdf") ; plot(som.ole) ; dev.off()
som.ole.ind <- buildSom(data.ole, sample.size=50000, variables=data.ole$vars.ind)
map.ole.ind <- mapIndividualSubpops(som.ole.ind, data.ole)
fitnessSomPlot(som.ole.ind, data.ole, map.ole.ind, file="ole_som_ind.pdf", show=FALSE)
fitnessHeatmap(som.ole.ind, data.ole, file="ole_heat_ind.pdf", show=FALSE)
pdf("ole_codes_ind.pdf") ; plot(som.ole.ind) ; dev.off()
data.ole <- reduceMean(data.ole)
fitnessMeanBehaviour(data.ole, file="ole_means.pdf", show=FALSE)


som.olh <- buildSom(data.olh, sample.size=50000)
map.olh <- mapMergeSubpops(som.olh, data.olh)
fitnessMetrics(data.olh, file="olh_fitness.pdf", show=FALSE)
fitnessSomPlot(som.olh, data.olh, map.olh, file="olh_som.pdf", show=FALSE)
fitnessHeatmap(som.olh, data.olh, file="olh_heat.pdf", show=FALSE)
pdf("olh_codes.pdf") ; plot(som.olh) ; dev.off()
som.olh.ind <- buildSom(data.olh, sample.size=50000, variables=data.olh$vars.ind)
map.olh.ind <- mapIndividualSubpops(som.olh.ind, data.olh)
fitnessSomPlot(som.olh.ind, data.olh, map.olh.ind, file="olh_som_ind.pdf", show=FALSE)
fitnessHeatmap(som.olh.ind, data.olh, file="olh_heat_ind.pdf", show=FALSE)
pdf("olh_codes_ind.pdf") ; plot(som.olh.ind) ; dev.off()
data.olh <- reduceMean(data.olh)
fitnessMeanBehaviour(data.olh, file="olh_means.pdf", show=FALSE)

som.m3e <- buildSom(data.m3e, sample.size=50000)
map.m3e <- mapMergeSubpops(som.m3e, data.m3e)
fitnessMetrics(data.m3e, file="m3e_fitness.pdf", show=FALSE)
fitnessSomPlot(som.m3e, data.m3e, map.m3e, file="m3e_som.pdf", show=FALSE)
fitnessHeatmap(som.m3e, data.m3e, file="m3e_heat.pdf", show=FALSE)
pdf("m3e_codes.pdf") ; plot(som.m3e) ; dev.off()

som.m3h <- buildSom(data.m3h, sample.size=50000)
map.m3h <- mapMergeSubpops(som.m3h, data.m3h)
fitnessMetrics(data.m3h, file="m3h_fitness.pdf", show=FALSE)
fitnessSomPlot(som.m3h, data.m3h, map.m3h, file="m3h_som.pdf", show=FALSE)
fitnessHeatmap(som.m3h, data.m3h, file="m3h_heat.pdf", show=FALSE)
pdf("m3h_codes.pdf") ; plot(som.m3h) ; dev.off()

som.m4h <- buildSom(data.m4h, sample.size=50000)
map.m4h <- mapMergeSubpops(som.m4h, data.m4h)
fitnessMetrics(data.m4h, file="m4h_fitness.pdf", show=FALSE)
fitnessSomPlot(som.m4h, data.m4h, map.m4h, file="m4h_som.pdf", show=FALSE)
fitnessHeatmap(som.m4h, data.m4h, file="m4h_heat.pdf", show=FALSE)
pdf("m4h_codes.pdf") ; plot(som.m4h) ; dev.off()

# others ######################################################################

data.nov25 <- loadData("~/Dropbox/ECJ/experiments/multiprey_easy_nov25", jobs=10, fitlim=c(0,1), vars.group=c("captured","duration","pred.avgdisp","pred.maxdisp","prey.avdisp"), subpops=3, expname="M3E nov 25%", load.behavs=F)
data.nov50 <- loadData("~/Dropbox/ECJ/experiments/multiprey_easy_nov50", jobs=10, fitlim=c(0,1), vars.group=c("captured","duration","pred.avgdisp","pred.maxdisp","prey.avdisp"), subpops=3, expname="M3E nov 50%", load.behavs=F)
data.nov70 <- loadData("~/Dropbox/ECJ/novelty_results/multiprey_easy_nov", jobs=10, fitlim=c(0,1), vars.group=c("captured","duration","pred.avgdisp","pred.maxdisp","prey.avdisp"), subpops=3, expname="M3E nov 70%", load.behavs=F)
data.fit <- loadData("~/Dropbox/ECJ/predator_results/multiprey_3_easy", jobs=20, fitlim=c(0,1), vars.group=c("captured","duration","pred.avgdisp","pred.maxdisp","prey.avdisp"), vars.transform=list("pred.avgdisp"=c(0,2,0,1),"pred.maxdisp"=c(0,2,0,1)), subpops=3, expname="Fit", fitness.file="extended.stat", load.behavs=F)


fitnessComparisonPlots(data.nov25, data.nov50, data.nov70, data.fit, snapshots=c(20,75,150,200,249))

data.fit <- loadData("~/Dropbox/ECJ/predator_results/multiprey_3_easy", jobs=20, fitlim=c(0,1), vars.group=c("captured","duration","pred.avgdisp","pred.maxdisp","prey.avdisp"), vars.transform=list("pred.avgdisp"=c(0,2,0,1),"pred.maxdisp"=c(0,2,0,1)), subpops=3, expname="M3E fit", fitness.file="extended.stat")
fitnessComparisonPlots(data.nov, data.fit, snapshots=c(20,75,150,200,249))
som.m3e <- buildSom(data.nov, data.fit, sample.size=50000, variables=data.nov$vars.group)
map.nov <- mapMergeSubpops(som.m3e, data.nov)
map.fit <- mapMergeSubpops(som.m3e, data.fit)
fitnessSomPlot(som.m3e, data.nov, map.nov, file="m3e_nov.pdf", show=F)
fitnessSomPlot(som.m3e, data.fit, map.fit, file="m3e_fit.pdf", show=F)
pdf("m3e_codes.pdf") ; plot(som.m3e) ; dev.off()
fitnessHeatmap(som.m3e, file="m3e_heatmap.pdf", show=F)

data.nov <- loadData("~/Dropbox/ECJ/novelty_results/multiprey_hard_nov", jobs=7, fitlim=c(0,1), vars.group=c("captured","duration","pred.avgdisp","pred.maxdisp","prey.avdisp"), subpops=3, expname="M3H nov 70%")
data.fit <- loadData("~/Dropbox/ECJ/predator_results/multiprey_3_hard", jobs=20, fitlim=c(0,1), vars.group=c("captured","duration","pred.avgdisp","pred.maxdisp","prey.avdisp"), vars.transform=list("pred.avgdisp"=c(0,2,0,1),"pred.maxdisp"=c(0,2,0,1)), subpops=3, expname="M3H fit", fitness.file="extended.stat")
fitnessComparisonPlots(data.nov, data.fit, snapshots=c(20,75,150,200,249))
som.m3h <- buildSom(data.nov, data.fit, sample.size=50000, variables=data.nov$vars.group)
map.nov <- mapMergeSubpops(som.m3h, data.nov)
map.fit <- mapMergeSubpops(som.m3h, data.fit)
fitnessSomPlot(som.m3h, data.nov, map.nov, file="m3h_nov.pdf", show=F)
fitnessSomPlot(som.m3h, data.fit, map.fit, file="m3h_fit.pdf", show=F)
pdf("m3h_codes.pdf") ; plot(som.m3h) ; dev.off()
fitnessHeatmap(som.m3h, file="m3h_heatmap.pdf", show=F)
                           
                           