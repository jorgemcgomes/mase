setwd("~/exps/generic/keepaway")

par <- list(fitlim=c(0,60), jobs=10, load.behavs=F, subpops=1)

data <- metaLoadData("cl10_ls_w","cl10_ls","cl10_w","cl50_ls_w","cl50_ls","cl50_w","cl100_ls_w","cl100_ls","cl100_w", params=par)
fullStatistics(data, expset.name="kw.cl", show.only=F, fit.comp=T, fit.comp.par=list(snapshots=c(100,300,499)))

data <- metaLoadData("fit_neat","ts_ls50_neat","cl10_ls_w","cl50_ls_w","cl100_ls_w", params=par)
fullStatistics(data, expset.name="kw.gen", show.only=F, fit.comp=T, fit.comp.par=list(snapshots=c(100,300,499)))


setwd("~/exps/generic/aggregation")
par <- list(fitlim=c(0,1), jobs=5, load.behavs=F, subpops=1)
data <- metaLoadData("fit","ts_ls","cl50_w_ls", params=par)
fullStatistics(data, expset.name="agg.gen", show.only=F, fit.comp=T, fit.comp.par=list(snapshots=c(75,150,249)))

setwd("~/exps/generic/sharing")
par <- list(fitlim=c(0,1), jobs=10, load.behavs=F, subpops=1)
data <- metaLoadData("rs9_fit","rs9_ls50","rs9_cl50_wls", params=par)
fullStatistics(data, expset.name="shar.gen", show.only=T, fit.comp=T, fit.comp.par=list(snapshots=c(75,150,299)))

data <- metaLoadData("rs9_ls50","rs9_ls75","rs9_cl50_wls","rs9_cl50_wls75", params=par)
fullStatistics(data, expset.name="shar.gen", show.only=T, fit.comp=T, fit.comp.par=list(snapshots=c(75,150,299)))

setwd("~/exps/generic/aggregation")

ts <- loadData("ts_ls", jobs=10, fitlim=c(0,1), load.behavs=F)
cl <- loadData("cl50_w_ls", jobs=8, fitlim=c(0,1), load.behavs=F)    
fit <- loadData("fit", jobs=5, fitlim=c(0,1), load.behavs=F)
fullStatistics(list(ts,cl,fit), expset.name="agg.gen", show.only=T, fit.comp=T, fit.comp.par=list(snapshots=c(75,150,249)))

setwd("~/exps/generic/sharing")
ts <- loadData("rs9_ls50", jobs=10, fitlim=c(0,1), subpops=1, load.behavs=T, vars.ind=c(), vars.group=c("survivors","energy","movement","distance"), behavs.sample=0.2)
cl <- loadData("rs9_cl50_wls", jobs=10, fitlim=c(0,1), subpops=1, load.behavs=T, vars.ind=c(), vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance","SC"),behavs.sample=0.2)    
fit <- loadData("rs9_fit", jobs=10, fitlim=c(0,1), subpops=1, load.behavs=T, vars.ind=c(), vars.group=c("survivors","energy","movement","distance"), behavs.sample=0.2)
fullStatistics(list(ts,cl,fit), expset.name="shar.gen.b", show.only=F, som.group=T, som.alljobs=T)



setwd("~/exps")
par <- list(fitlim=c(0,60), jobs=10, load.behavs=F, subpops=3)
data <- metaLoadData("kw_fit_neat","kw_ts_neat","kw_ts75_neat","kw_gen_neat","kw_gen75_neat","kw_fit_ga","kw_ts_ga","kw_ts75_ga","kw_gen_ga","kw_gen75_ga", params=par)
fullStatistics(data, expset.name="kw.hom", show.only=F, fit.comp=T, fit.comp.par=list(snapshots=c(100,300,499)))

data <- metaLoadData("kw_gen_neat","kw_cl5_neat","kw_cl10_neat","kw_cl20_neat","kw_cl50_neat","kw_cl100_neat","kw_cl500_neat", params=par)
fullStatistics(data, expset.name="kw.gen", show.only=F, fit.comp=T, fit.comp.par=list(snapshots=c(100,300,499)))

data <- metaLoadData("kw_ts_neat","kw_fit_neat","kw_gen_neat","kw_cl10_ls_w","kw_cl10_ls","kw_cl10_w","kw_cl10_neat","kw_cl50_ls_w","kw_cl50_ls","kw_cl50_w","kw_cl50_neat","kw_cl100_ls_w","kw_cl100_ls","kw_cl100_w","kw_cl100_neat", params=par)
fullStatistics(data, expset.name="kw.cl", show.only=F, fit.comp=T, fit.comp.par=list(snapshots=c(100,300,499)))

data <- metaLoadData("kw_cl10_ls_w","kw_cl10_ls","kw_cl10_w","kw_cl50_ls_w","kw_cl50_ls","kw_cl50_w","kw_cl100_ls_w","kw_cl100_ls","kw_cl100_w", params=par)
fullStatistics(data, expset.name="kw.cl", show.only=F, fit.comp=T, fit.comp.par=list(snapshots=c(100,300,499)))

data <- metaLoadData("kw_cl10_neat","kw_cl10_ls","kw_cl50_ls","kw_cl50_neat","kw_cl100_ls","kw_cl100_neat", params=par)
fullStatistics(data, expset.name="kw.cl", show.only=T, fit.comp=T, fit.comp.par=list(snapshots=c(100,300,499)))


setwd("~/Dropbox/exps_AAMAS/")

par <- list(fitlim=c(0,2), jobs=20, load.behavs=T, subpops=3, behavs.sample=0.25, vars.group=c("g.captured","g.time","g.finaldist","g.dispersion"), vars.ind=c("i.captured","i.preydist","i.movement","i.partnerdist"))
data <- metaLoadData("pred3_fit","pred3_nov50","pred3_nov50_indshare","pred3_nov50_indmult", params=par, names=c("fit","nov-group","nov-indshare","nov-indmult"))
gc()

count <- exploration.count(data, levels=5, vars=par$vars.group)
uniformity.group(count, threshold=100)
uniformity.gen(count, threshold=100)
count <- exploration.count(data, levels=5, vars=par$vars.ind)
uniformity.ind(count, threshold=100)
uniformity.diff(count, threshold=100)
rm(data) ; rm(count) ; gc()

par <- list(fitlim=c(0,2), jobs=20, load.behavs=T, subpops=5, behavs.sample=0.25, vars.group=c("g.captured","g.time","g.finaldist","g.dispersion"), vars.ind=c("i.captured","i.preydist","i.movement","i.partnerdist"))
data <- metaLoadData("pred5m_fit","pred5m_nov50","pred5m_nov50_indshare","pred5m_nov50_indmult", params=par, names=c("fit","nov-group","nov-indshare","nov-indmult"))
gc()

count <- exploration.count(data, levels=5, vars=par$vars.group)
uniformity.group(count, threshold=100)
uniformity.gen(count, threshold=100)
count <- exploration.count(data, levels=5, vars=par$vars.ind)
uniformity.ind(count, threshold=100)
uniformity.diff(count, threshold=100)
rm(data) ; rm(count) ; gc()

par <- list(fitlim=c(0,30), jobs=20, load.behavs=T, subpops=3, behavs.sample=0.25, vars.group=c("g.passes","g.steps","g.dispersion","g.pass-length"), vars.ind=c("i.passes","i.pass-length","i.keeper-dist","i.movement"))
data <- metaLoadData("k3_ph_med_fit","k3_ph_med_nov50","k3_ph_med_nov50_indshare","k3_ph_med_nov50_indmult", params=par, names=c("fit","nov-group","nov-indshare","nov-indmult"))
gc()

count <- exploration.count(data, levels=5, vars=par$vars.group)
uniformity.group(count, threshold=100)
uniformity.gen(count, threshold=100)
count <- exploration.count(data, levels=5, vars=par$vars.ind)
uniformity.ind(count, threshold=100)
uniformity.diff(count, threshold=100)
rm(data) ; rm(count) ; gc()

data <- metaLoadData("k3_ph_hard_fit","k3_ph_hard_nov50","k3_ph_hard_nov50_indshare","k3_ph_hard_nov50_indmult", params=par, names=c("fit","nov-group","nov-indshare","nov-indmult"))
gc()

count <- exploration.count(data, levels=5, vars=par$vars.group)
uniformity.group(count, threshold=100)
uniformity.gen(count, threshold=100)
count <- exploration.count(data, levels=5, vars=par$vars.ind)
uniformity.ind(count, threshold=100)
uniformity.diff(count, threshold=100)
rm(data) ; rm(count) ; gc()


setwd("~/Dropbox/exps_AAMAS/")

par <- list(fitlim=c(0,2), jobs=20, load.behavs=T, subpops=3, behavs.sample=0.25, vars.group=c("g.captured","g.time","g.finaldist","g.dispersion"), vars.ind=c("i.captured","i.preydist","i.movement","i.partnerdist"))
data <- metaLoadData("pred3_fit","pred3_nov50","pred3_nov50_indmult", params=par, names=c("fit","nov-group","nov-indmult"))
count <- exploration.count(data, levels=5, vars=par$vars.group)
uniformity.gen.2(count, threshold=100)
rm(data) ; rm(count) ; gc()

par <- list(fitlim=c(0,2), jobs=20, load.behavs=T, subpops=5, behavs.sample=0.25, vars.group=c("g.captured","g.time","g.finaldist","g.dispersion"), vars.ind=c("i.captured","i.preydist","i.movement","i.partnerdist"))
data <- metaLoadData("pred5m_fit","pred5m_nov50","pred5m_nov50_indmult", params=par, names=c("fit","nov-group","nov-indmult"))
count <- exploration.count(data, levels=5, vars=par$vars.group)
uniformity.gen.2(count, threshold=100)
rm(data) ; rm(count) ; gc()

par <- list(fitlim=c(0,30), jobs=20, load.behavs=T, subpops=3, behavs.sample=0.25, vars.group=c("g.passes","g.steps","g.dispersion","g.pass-length"), vars.ind=c("i.passes","i.pass-length","i.keeper-dist","i.movement"))
data <- metaLoadData("k3_ph_med_fit","k3_ph_med_nov50","k3_ph_med_nov50_indmult", params=par, names=c("fit","nov-group","nov-indmult"))
count <- exploration.count(data, levels=5, vars=par$vars.group)
uniformity.gen.2(count, threshold=100)
rm(data) ; rm(count) ; gc()

data <- metaLoadData("k3_ph_hard_fit","k3_ph_hard_nov50","k3_ph_hard_nov50_indmult", params=par, names=c("fit","nov-group","nov-indmult"))
count <- exploration.count(data, levels=5, vars=par$vars.group)
uniformity.gen.2(count, threshold=100)
rm(data) ; rm(count) ; gc()