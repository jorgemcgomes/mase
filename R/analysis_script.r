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


setwd("~/exps/generic/sharing")
par <- list(fitlim=c(0,1), jobs=10, load.behavs=F, subpops=1)
data <- metaLoadData("rs11_fit","rs11_ls50","rs11_cl50_wls","rs11_cl50_wls_d4","rs11_cl100_wls", params=par)
fullStatistics(data, expset.name="shar.gen", show.only=T, fit.comp=T, fit.comp.par=list(snapshots=c(75,200,399)))


setwd("~/exps/generic/sharing")
par <- list(fitlim=c(0,1), jobs=10, load.behavs=F, subpops=1)
data <- metaLoadData("rs11_fit","rs11_ls50","rs11_cl50_wls_d4_idf","rs11_cl50_idf_f0","rs11_cl50_idf_f001","rs11_cl50_idf_f0001", params=par)
fullStatistics(data, expset.name="shar.gen", show.only=T, fit.comp=T, fit.comp.par=list(snapshots=c(100,200,399)))

setwd("~/exps/generic/sharing")
par <- list(fitlim=c(0,1), jobs=10, load.behavs=F, subpops=1)
data <- metaLoadData("rs11_fit","rs11_ls50","rs11_cl50_wls_d4_idf","rs11_cl50_wls_d3_idf", params=par)
fullStatistics(data, expset.name="shar.gen", show.only=T, fit.comp=T, fit.comp.par=list(snapshots=c(75,200,399)))


setwd("~/exps/generic/sharing")
par <- list(fitlim=c(0,1), jobs=10, load.behavs=F, subpops=1, load.weights=T)
data <- metaLoadData("rs11_cl50_wls_d4_idf","rs11_cl50_idf_f0","rs11_cl50_idf_f001","rs11_cl50_idf_f0001", params=par)
old <- weightAnalysis(data[[1]])
f0 <- weightAnalysis(data[[2]])
f001 <- weightAnalysis(data[[3]])
f0001 <- weightAnalysis(data[[4]])
plotMultiline(smoothFrame(data.frame(gen=data[[1]]$gens,old=old$mean,f001=f001$mean,f0001=f0001$mean,f0=f0$mean), window=10), ylim=NULL, ylabel="Fitness-correlation", title="Mean correlation RS-3")
plotMultiline(smoothFrame(data.frame(gen=data[[1]]$gens,old=old$max,f001=f001$max,f0001=f0001$max,f0=f0$max), window=10), ylim=NULL, ylabel="Fitness-correlation", title="Max correlation RS-3")

setwd("~/exps/generic/sharing")
par <- list(fitlim=c(0,1), jobs=10, load.behavs=F, subpops=1, load.clusters=T)
data <- metaLoadData("rs11_cl50_wls_d4_idf","rs11_cl50_idf_f0","rs11_cl50_idf_f001","rs11_cl50_idf_f0001", params=par)
old <- clusterAnalysis(data[[1]])
f0 <- clusterAnalysis(data[[2]])
f001 <- clusterAnalysis(data[[3]])
f0001 <- clusterAnalysis(data[[4]])
plotMultiline(smoothFrame(data.frame(gen=old$gen,old=old$mean,f001=f001$mean,f0001=f0001$mean,f0=f0$mean), window=10), ylim=NULL, ylabel="Mean change", title="Clusters change RS-3")
plotMultiline(smoothFrame(data.frame(gen=old$gen,old=old$max,f001=f001$max,f0001=f0001$max,f0=f0$max), window=10), ylim=NULL, ylabel="Max change", title="Clusters change RS-3")

setwd("~/exps/generic/sharing")
ts <- loadData("rs11_ls50", jobs=10, fitlim=c(0,1), load.behavs=T, subpops=1, behavs.sample=0.1, expname="TS", vars.group=c("survivors","energy","movement","distance"))
cl <- loadData("rs11_cl50_wls_d4_idf", jobs=10, fitlim=c(0,1), load.behavs=T, subpops=1, behavs.sample=0.1, expname="CL-Batch", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))    
clf0 <- loadData("rs11_cl50_idf_f0", jobs=10, fitlim=c(0,1), load.behavs=T, subpops=1, behavs.sample=0.1, expname="CL-F0", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))    
clf001 <- loadData("rs11_cl50_idf_f001", jobs=10, fitlim=c(0,1), load.behavs=T, subpops=1, behavs.sample=0.1, expname="CL-F001", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))    
clf0001 <- loadData("rs11_cl50_idf_f0001", jobs=10, fitlim=c(0,1), load.behavs=T, subpops=1, behavs.sample=0.1, expname="CL-F0001", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))    
fit <- loadData("rs11_fit", jobs=10, fitlim=c(0,1), load.behavs=T, behavs.sample=0.1, subpops=1, expname="Fit", vars.group=c("survivors","energy","movement","distance"))
datalist <- list(ts,cl,clf0,clf001,clf0001,fit)
fullStatistics(datalist, expset.name="shar.clf", show.only=F, fit.comp=T, fit.comp.par=list(snapshots=c(75,200,399)), som.group=T, som.alljobs=T)
counts <- exploration.count(list(ts,cl,clf0,clf001,clf0001,fit))
uniformity.group(counts)


setwd("~/exps/generic/sharing")
ts <- loadData("rs11_ls50", jobs=10, fitlim=c(0,1), load.behavs=T, subpops=1, behavs.sample=0.1, expname="TS", vars.group=c("survivors","energy","movement","distance"))
cl <- loadData("rs11_cl50_wls_d4_idf", jobs=10, fitlim=c(0,1), load.behavs=F,load.weights=T, load.clusters=T,  subpops=1, behavs.sample=0.1, expname="CL-Batch", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))    
clf001 <- loadData("rs11_cl50_idf_f001", jobs=10, fitlim=c(0,1), load.behavs=F,load.weights=T,  load.clusters=T, subpops=1, behavs.sample=0.1, expname="CL-F001", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))    
fit <- loadData("rs11_fit", jobs=10, fitlim=c(0,1), load.behavs=T, behavs.sample=0.1, subpops=1, expname="Fit", vars.group=c("survivors","energy","movement","distance"))
rep1 <- loadData("rs11_cl50_rep1", jobs=5, fitlim=c(0,1), load.behavs=F, load.weights=T, load.clusters=T, subpops=1, behavs.sample=0.1, expname="CL-Rep1", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))
rep5 <- loadData("rs11_cl50_rep5", jobs=5, fitlim=c(0,1), load.behavs=F,load.weights=T,  load.clusters=T, subpops=1, behavs.sample=0.1, expname="CL-Rep5", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))
rep10 <- loadData("rs11_cl50_rep10", jobs=5, fitlim=c(0,1), load.behavs=F,load.weights=T, load.clusters=T, subpops=1, behavs.sample=0.1, expname="CL-Rep10", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))
datalist <- list(ts,cl,clf001,fit,rep1,rep5,rep10)
fullStatistics(datalist, expset.name="shar.rep", show.only=F, fit.comp=T, fit.comp.par=list(snapshots=c(75,200,399)), som.group=T, som.alljobs=T)
w.batch <- weightAnalysis(cl)
w.f001 <- weightAnalysis(clf001)
w.rep1 <- weightAnalysis(rep1)
w.rep5 <- weightAnalysis(rep5)
w.rep10 <- weightAnalysis(rep10)
plotMultiline(smoothFrame(data.frame(gen=w.batch$gen,batch=w.batch$mean,f001=w.f001$mean,rep1=w.rep1$mean,rep5=w.rep5$mean,rep10=w.rep10$mean), window=10), ylim=NULL, ylabel="Fitness-correlation", title="Mean correlation RS-3")
c.batch <- clusterAnalysis(cl)
c.f001 <- clusterAnalysis(clf001)
c.rep1 <- clusterAnalysis(rep1)
c.rep5 <- clusterAnalysis(rep5)
c.rep10 <- clusterAnalysis(rep10)
plotMultiline(smoothFrame(data.frame(gen=c.batch$gen,batch=c.batch$mean,f001=c.f001$mean,rep1=c.rep1$mean,rep5=c.rep5$mean,rep10=c.rep10$mean), window=10), ylim=NULL, ylabel="Mean cluster change", title="Clusters change RS-3")
plotMultiline(smoothFrame(data.frame(gen=c.batch$gen,batch=c.batch$max,f001=c.f001$max,rep1=c.rep1$max,rep5=c.rep5$max,rep10=c.rep10$max), window=10), ylim=NULL, ylabel="Max cluster change", title="Clusters change RS-3")


setwd("~/exps/generic/sharing")
ts <- loadData("rs11_ls50", jobs=10, fitlim=c(0,1), load.behavs=T, subpops=1, behavs.sample=0.1, expname="TS", vars.group=c("survivors","energy","movement","distance"))
cl <- loadData("rs11_cl50_wls_d4_idf", jobs=10, fitlim=c(0,1), load.behavs=T,load.weights=T, load.clusters=T,  subpops=1, behavs.sample=0.1, expname="CL-Batch", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))    
clf001 <- loadData("rs11_cl50_idf_f001", jobs=10, fitlim=c(0,1), load.behavs=T,load.weights=T,  load.clusters=T, subpops=1, behavs.sample=0.1, expname="CL-F001", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))    
fit <- loadData("rs11_fit", jobs=10, fitlim=c(0,1), load.behavs=T, behavs.sample=0.1, subpops=1, expname="Fit", vars.group=c("survivors","energy","movement","distance"))
rep10 <- loadData("rs11_cl50_rep10", jobs=10, fitlim=c(0,1), load.behavs=T,load.weights=T, load.clusters=T, subpops=1, behavs.sample=0.1, expname="CL-Rep10", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))
datalist <- list(ts,fit,cl,clf001,rep10)
fullStatistics(datalist, expset.name="shar.rep2", show.only=F, fit.comp=T, fit.comp.par=list(snapshots=c(75,200,399)), som.group=T, som.alljobs=T)
w.batch <- weightAnalysis(cl)
w.f001 <- weightAnalysis(clf001)
w.rep10 <- weightAnalysis(rep10)
plotMultiline(smoothFrame(data.frame(gen=w.batch$gen,batch=w.batch$mean,f001=w.f001$mean,rep10=w.rep10$mean), window=10), ylim=NULL, ylabel="Fitness-correlation", title="Mean correlation RS-3")
c.batch <- clusterAnalysis(cl)
c.f001 <- clusterAnalysis(clf001)
c.rep10 <- clusterAnalysis(rep10)
plotMultiline(smoothFrame(data.frame(gen=c.batch$gen,batch=c.batch$mean,f001=c.f001$mean,rep10=c.rep10$mean), window=10), ylim=NULL, ylabel="Mean cluster change", title="Clusters change RS-3")
plotMultiline(smoothFrame(data.frame(gen=c.batch$gen,batch=c.batch$max,f001=c.f001$max,rep10=c.rep10$max), window=10), ylim=NULL, ylabel="Max cluster change", title="Clusters change RS-3")
counts <- exploration.count(datalist)
uniformity.group(counts)

setwd("~/exps/generic/keepaway")
ts <- loadData("ts_ls50_neat", jobs=10, fitlim=c(0,60), load.behavs=T, subpops=1, behavs.sample=0.2, expname="TS", vars.group=c("passes","steps","disp","passLength"))
fit <- loadData("fit_neat", jobs=10, fitlim=c(0,60), load.behavs=T, behavs.sample=0.2, subpops=1, expname="Fit", vars.group=c("passes","steps","disp","passLength"))
cl <- loadData("kw_cl50_wls_d4_idf", jobs=10, fitlim=c(0,60), load.behavs=T, load.clusters=T, load.weights=T, subpops=1, behavs.sample=0.2, expname="CL-Batch", vars.group=c("passes","steps","disp","passLength"), vars.file=c("passes","steps","disp","passLength",paste0("gen",1:50)))    
clf <- loadData("kw_cl50_wls_f001", jobs=10, fitlim=c(0,60), load.behavs=T, load.clusters=T, load.weights=T, subpops=1, behavs.sample=0.2, expname="CL-F001", vars.group=c("passes","steps","disp","passLength"), vars.file=c("passes","steps","disp","passLength",paste0("gen",1:50)))
clrep <- loadData("kw_cl50_rep10_2", jobs=c("job.5","job.6","job.7","job.8","job.9"), fitlim=c(0,60), load.behavs=T, load.clusters=T, load.weights=T, subpops=1, behavs.sample=0.2, expname="CL-Rep10", vars.group=c("passes","steps","disp","passLength"), vars.file=c("passes","steps","disp","passLength",paste0("gen",1:50)))
fullStatistics(list(ts,cl,fit,clf,clrep), expset.name="kw.gen", show.only=F, fit.comp=T, fit.comp.par=list(snapshots=c(100,250,499)), som.group=T, som.alljobs=T)
w.batch <- weightAnalysis(cl)
w.f001 <- weightAnalysis(clf)
w.rep <- weightAnalysis(clrep)
plotMultiline(smoothFrame(data.frame(gen=w.batch$gen,batch=w.batch$mean,f001=w.f001$mean, rep=w.rep$mean), window=10), ylim=NULL, ylabel="Fitness-correlation", title="Mean correlation KW")
# c.batch <- clusterAnalysis(cl)
# c.f001 <- clusterAnalysis(clf)
# c.rep <- clusterAnalysis(clrep)
# plotMultiline(smoothFrame(data.frame(gen=c.batch$gen,batch=c.batch$mean,f001=c.f001$mean,rep=c.rep$mean), window=10), ylim=NULL, ylabel="Mean cluster change", title="Clusters change KW")
# plotMultiline(smoothFrame(data.frame(gen=c.batch$gen,batch=c.batch$max,f001=c.f001$max, rep=c.rep$max), window=10), ylim=NULL, ylabel="Max cluster change", title="Clusters change KW")
counts <- exploration.count(list(ts,cl,clf,clrep,fit))
uniformity.group(counts)

setwd("~/exps/generic/aggregation")
ts <- loadData("ts_ls", jobs=10, fitlim=c(0,1), load.behavs=T, subpops=1, behavs.sample=0.2, expname="TS", vars.group=paste0("cm",1:20))
cl <- loadData("agg_cl50_wls_d4_idf", jobs=7, fitlim=c(0,1), load.behavs=T, load.weights=T, load.clusters=T, subpops=1, behavs.sample=0.2, expname="CL-Batch", vars.group=paste0("cm",1:20), vars.file=c(paste0("cm",1:20),paste0("gen",1:50)))    
clf <- loadData("agg_cl50_f001", jobs=10, fitlim=c(0,1), load.behavs=T, load.weights=T, load.clusters=T, subpops=1, behavs.sample=0.2, expname="CL-F001", vars.group=paste0("cm",1:20), vars.file=c(paste0("cm",1:20),paste0("gen",1:50)))    
clrep <- loadData("agg_cl50_rep10", jobs=10, fitlim=c(0,1), load.behavs=T, load.weights=T, load.clusters=T, subpops=1, behavs.sample=0.2, expname="CL-Rep10", vars.group=paste0("cm",1:20), vars.file=c(paste0("cm",1:20),paste0("gen",1:50)))    
fit <- loadData("fit", jobs=5, fitlim=c(0,1), load.behavs=T, behavs.sample=0.2, subpops=1, expname="Fit", vars.group=paste0("cm",1:20))
fullStatistics(list(ts,cl,clf,clrep,fit), expset.name="agg.gen", show.only=F, fit.comp=T, fit.comp.par=list(snapshots=c(75,150,249)), som.group=T, som.alljobs=T)
w.batch <- weightAnalysis(cl)
w.f001 <- weightAnalysis(clf)
w.rep <- weightAnalysis(clrep)
plotMultiline(smoothFrame(data.frame(gen=w.batch$gen,batch=w.batch$mean,f001=w.f001$mean, rep=w.rep$mean), window=10), ylim=NULL, ylabel="Fitness-correlation", title="Mean correlation Agg")


setwd("~/exps/generic/sharing")
ts <- loadData("rs11_ls50", jobs=10, fitlim=c(0,1), load.behavs=T, subpops=1, behavs.sample=0.1, expname="TS", vars.group=c("survivors","energy","movement","distance"))
cl50 <- loadData("rs11_cl50_wls_d4_idf", jobs=10, fitlim=c(0,1), load.behavs=T,load.weights=T, load.clusters=T,  subpops=1, behavs.sample=0.1, expname="CL-50", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))    
cl25 <- loadData("rs11_cl25", jobs=10, fitlim=c(0,1), load.behavs=T,load.weights=T,  load.clusters=T, subpops=1, behavs.sample=0.1, expname="CL-25", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:25)))    
cl100 <- loadData("rs11_cl100", jobs=10, fitlim=c(0,1), load.behavs=T,load.weights=T, load.clusters=T, subpops=1, behavs.sample=0.1, expname="CL-100", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:100)))
fit <- loadData("rs11_fit", jobs=10, fitlim=c(0,1), load.behavs=T, behavs.sample=0.1, subpops=1, expname="Fit", vars.group=c("survivors","energy","movement","distance"))
datalist <- list(ts,fit,cl50,cl25,cl100)
fullStatistics(datalist, expset.name="shar.nclusters", show.only=F, fit.comp=T, fit.comp.par=list(snapshots=c(75,200,399)), som.group=T, som.alljobs=T)
w.cl25 <- weightAnalysis(cl25)
w.cl50 <- weightAnalysis(cl50)
w.cl100 <- weightAnalysis(cl100)
plotMultiline(smoothFrame(data.frame(gen=w.cl25$gen,cl25=w.cl25$mean,cl50=w.cl50$mean,cl100=w.cl100$mean), window=10), ylim=NULL, ylabel="Fitness-correlation", title="Mean correlation RS-3")
plotMultiline(smoothFrame(data.frame(gen=w.cl25$gen,cl25=w.cl25$max,cl50=w.cl50$max,cl100=w.cl100$max), window=10), ylim=NULL, ylabel="Fitness-correlation", title="Max correlation RS-3")

data <- metaLoadData("rs11_cl50_bal25","rs11_cl50_bal25_d3", params=list(fitlim=c(0,1),jobs=10,load.behavs=F,subpops=1))
fullStatistics(data, expset.name="disc", fit.comp=T, fit.comp.par=list(snapshots=c(75,200,399)), show.only=T)

bal10 <- loadData("rs11_cl50_bal10", jobs=10, fitlim=c(0,1), load.behavs=F,load.weights=T,  load.clusters=T, subpops=1, behavs.sample=0.1, expname="BAL-10", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))    
w.bal10 <- weightAnalysis(bal10)
c.bal10 <- clusterAnalysis(bal10)
plot(w.bal10$mean, type="l")
plot(c.bal10$mean, type="l")

bal50 <- loadData("rs11_cl50_bal50", jobs=10, fitlim=c(0,1), load.behavs=F,load.weights=T,  load.clusters=T, subpops=1, behavs.sample=0.1, expname="BAL-10", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))    
w.bal50 <- weightAnalysis(bal50)
c.bal50 <- clusterAnalysis(bal50)
plot(w.bal50$mean, type="l")
plot(c.bal50$mean, type="l")


setwd("~/exps/generic/sharing")
fit <- loadData("rs11_fit", jobs=10, fitlim=c(0,1), load.behavs=T, behavs.sample=0.1, subpops=1, expname="Fit", vars.group=c("survivors","energy","movement","distance"))
ts <- loadData("rs11_ls50", jobs=10, fitlim=c(0,1), load.behavs=T, subpops=1, behavs.sample=0.1, expname="TS", vars.group=c("survivors","energy","movement","distance"))
cl <- loadData("rs11_cl50_wls_d4_idf", jobs=10, fitlim=c(0,1), load.behavs=T,load.weights=T, load.clusters=T, new.clusters=F,  subpops=1, behavs.sample=0.1, expname="CL-Batch", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))    
bal25 <- loadData("rs11_cl50_bal25", jobs=10, fitlim=c(0,1), load.behavs=T,load.weights=T, load.clusters=T, subpops=1, behavs.sample=0.1, expname="CL-Bal25", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))
rep10 <- loadData("rs11_cl50_rep10", jobs=10, fitlim=c(0,1), load.behavs=T,load.weights=T, load.clusters=T, new.clusters=F, subpops=1, behavs.sample=0.1, expname="CL-Rep10", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))
fullStatistics(list(ts,fit,cl,rep10,bal25), expset.name="shar.bal2", show.only=F, fit.comp=T, fit.comp.par=list(snapshots=c(75,200,399)), som.group=T, som.alljobs=T)

setwd("~/exps/generic/sharing")
fit <- loadData("rs11_fit", jobs=10, fitlim=c(0,1), load.behavs=F, behavs.sample=0.1, subpops=1, expname="Fit", vars.group=c("survivors","energy","movement","distance"))
ts <- loadData("rs11_ls50", jobs=10, fitlim=c(0,1), load.behavs=F, subpops=1, behavs.sample=0.1, expname="TS", vars.group=c("survivors","energy","movement","distance"))
cl <- loadData("rs11_cl50_wls_d4_idf", jobs=10, fitlim=c(0,1), load.behavs=F,load.weights=T, load.clusters=T, new.clusters=F,  subpops=1, behavs.sample=0.1, expname="CL-Batch", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))    
clf001 <- loadData("rs11_cl50_idf_f001", jobs=10, fitlim=c(0,1), load.behavs=F,load.weights=T, load.clusters=T, new.clusters=F, subpops=1, behavs.sample=0.1, expname="CL-F001", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))    
rep10 <- loadData("rs11_cl50_rep10", jobs=10, fitlim=c(0,1), load.behavs=F,load.weights=T, load.clusters=T, new.clusters=F, subpops=1, behavs.sample=0.1, expname="CL-Rep10", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))
bal10 <- loadData("rs11_cl50_bal10", jobs=10, fitlim=c(0,1), load.behavs=F,load.weights=T, load.clusters=T, subpops=1, behavs.sample=0.1, expname="CL-Bal10", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))
bal50 <- loadData("rs11_cl50_bal50", jobs=10, fitlim=c(0,1), load.behavs=F,load.weights=T, load.clusters=T, subpops=1, behavs.sample=0.1, expname="CL-Bal50", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))
bal5 <- loadData("rs11_cl50_bal5", jobs=10, fitlim=c(0,1), load.behavs=F,load.weights=T, load.clusters=T, subpops=1, behavs.sample=0.1, expname="CL-Bal5", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))
bal25 <- loadData("rs11_cl50_bal25", jobs=10, fitlim=c(0,1), load.behavs=F,load.weights=T, load.clusters=T, subpops=1, behavs.sample=0.1, expname="CL-Bal25", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))

datalist <- list(ts,fit,cl,clf001,rep10,bal10,bal50,bal5,bal25)
fullStatistics(datalist, expset.name="shar.bal", show.only=F, fit.comp=T, fit.comp.par=list(snapshots=c(75,200,399)), som.group=F, som.alljobs=F)
fullStatistics(list(ts,fit,cl,rep10,bal25), expset.name="shar.bal2", show.only=F, fit.comp=T, fit.comp.par=list(snapshots=c(75,200,399)), som.group=F, som.alljobs=F)
w.batch <- weightAnalysis(cl)
w.f001 <- weightAnalysis(clf001)
w.rep10 <- weightAnalysis(rep10)
w.bal10 <- weightAnalysis(bal10)
w.bal50 <- weightAnalysis(bal50)
w.bal5 <- weightAnalysis(bal5)
w.bal25 <- weightAnalysis(bal25)
plotMultiline(smoothFrame(data.frame(gen=w.batch$gen,batch=w.batch$mean,f001=w.f001$mean,rep10=w.rep10$mean,w.bal10=w.bal10$mean,w.bal50=w.bal50$mean,w.bal5=w.bal5$mean,w.bal25=w.bal25$mean), window=10), ylim=NULL, ylabel="Fitness-correlation", title="Mean correlation RS-3")
plotMultiline(smoothFrame(data.frame(gen=w.batch$gen,batch=w.batch$mean,rep10=w.rep10$mean,bal25=w.bal25$mean), window=10), ylim=NULL, ylabel="Fitness-correlation", title="Mean correlation RS-3")
c.batch <- clusterAnalysis(cl)
c.f001 <- clusterAnalysis(clf001)
c.rep10 <- clusterAnalysis(rep10)
c.bal10 <- clusterAnalysis(bal10)
c.bal50 <- clusterAnalysis(bal50)
c.bal5 <- clusterAnalysis(bal5)
c.bal25 <- clusterAnalysis(bal25)
plotMultiline(smoothFrame(data.frame(gen=c.batch$gen,batch=c.batch$mean,f001=c.f001$mean,rep10=c.rep10$mean, c.bal10=c.bal10$mean, c.bal50=c.bal50$mean, c.bal5=c.bal5$mean, c.bal25=c.bal25$mean), window=10), ylim=NULL, ylabel="Mean cluster change", title="Clusters change RS-3")
plotMultiline(smoothFrame(data.frame(gen=c.batch$gen,batch=c.batch$mean,rep10=c.rep10$mean, bal25=c.bal25$mean), window=10), ylim=NULL, ylabel="Mean cluster change", title="Clusters change RS-3")


setwd("~/exps/generic2/sharing/")
shar.data <- metaLoadData("fit","ls50","cl50_batch_d3","cl50_bal25_d3", params=list(fitlim=c(0,1),jobs=10,load.behavs=F,subpops=1))
fullStatistics(shar.data, expset.name="rs", fit.comp=T, fit.comp.par=list(snapshots=c(100,250,399)), show.only=T)

fit <- loadData("fit", jobs=10, fitlim=c(0,1), load.behavs=T, behavs.sample=0.1, subpops=1, expname="Fit", vars.group=c("survivors","energy","movement","distance"))
ts <- loadData("ls50", jobs=10, fitlim=c(0,1), load.behavs=T, subpops=1, behavs.sample=0.1, expname="TS", vars.group=c("survivors","energy","movement","distance"))
batch <- loadData("cl50_batch_d3", jobs=10, fitlim=c(0,1), load.behavs=T,load.weights=T, load.clusters=T, subpops=1, behavs.sample=0.1, expname="CL-Batch", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))    
bal25 <- loadData("cl50_bal25_d3", jobs=10, fitlim=c(0,1), load.behavs=T,load.weights=T, load.clusters=T, subpops=1, behavs.sample=0.1, expname="CL-Bal25", vars.group=c("survivors","energy","movement","distance"), vars.file=c("survivors","energy","movement","distance",paste0("gen",1:50)))
fullStatistics(list(fit,ts,batch,bal25), expset.name="rs", show.only=F, fit.comp=T, fit.comp.par=list(snapshots=c(100,250,399)), som.group=T, som.alljobs=T)
w.batch <- weightAnalysis(batch) ; w.bal25 <- weightAnalysis(bal25)
plotMultiline(smoothFrame(data.frame(gen=w.batch$gen,batch=w.batch$mean,bal25=w.bal25$mean), window=5), ylim=NULL, ylabel="Fitness-correlation", title="Mean correlation")
c.batch <- clusterAnalysis(batch) ; c.bal25 <- clusterAnalysis(bal25)
plotMultiline(smoothFrame(data.frame(gen=c.batch$gen,batch=c.batch$mean,bal25=c.bal25$mean), window=5), ylim=NULL, ylabel="Mean cluster change", title="Mean Clusters change")


setwd("~/exps/generic2/aggregation/")
agg.data <- metaLoadData("fit","ls50","cl50_batch_d3","cl50_bal25_d3", params=list(fitlim=c(0,1),jobs=10,load.behavs=F,subpops=1))
fullStatistics(agg.data, expset.name="agg", fit.comp=T, fit.comp.par=list(snapshots=c(50,100,199)), show.only=T)

fit <- loadData("fit", jobs=10, fitlim=c(0,1), load.behavs=T, behavs.sample=0.1, subpops=1, expname="Fit", vars.group=paste0("cm",1:20))
ts <- loadData("ls50", jobs=10, fitlim=c(0,1), load.behavs=T, subpops=1, behavs.sample=0.1, expname="TS", vars.group=paste0("cm",1:20))
batch <- loadData("cl50_batch_d3", jobs=10, fitlim=c(0,1), load.behavs=T, load.weights=T, load.clusters=T, subpops=1, behavs.sample=0.1, expname="CL-Batch", vars.group=paste0("cm",1:20), vars.file=c(paste0("cm",1:20),paste0("gen",1:50)))    
bal25 <- loadData("cl50_bal25_d3", jobs=10, fitlim=c(0,1), load.behavs=T, load.weights=T, load.clusters=T, subpops=1, behavs.sample=0.1, expname="CL-Bal25", vars.group=paste0("cm",1:20), vars.file=c(paste0("cm",1:20),paste0("gen",1:50)))    
fullStatistics(list(fit,ts,batch,bal25), expset.name="agg", show.only=F, fit.comp=T, fit.comp.par=list(snapshots=c(50,100,199)), som.group=T, som.alljobs=T)
w.batch <- weightAnalysis(batch) ; w.bal25 <- weightAnalysis(bal25)
plotMultiline(smoothFrame(data.frame(gen=w.batch$gen,batch=w.batch$mean,bal25=w.bal25$mean), window=5), ylim=NULL, ylabel="Fitness-correlation", title="Mean correlation")
c.batch <- clusterAnalysis(batch) ; c.bal25 <- clusterAnalysis(bal25)
plotMultiline(smoothFrame(data.frame(gen=c.batch$gen,batch=c.batch$mean,bal25=c.bal25$mean), window=5), ylim=NULL, ylabel="Mean cluster change", title="Mean Clusters change")


setwd("~/exps/generic2/keepaway/")
shar.data <- metaLoadData("fit","ls50","cl50_batch_d3","cl50_bal25_d3", params=list(fitlim=c(0,60),jobs=10,load.behavs=F,subpops=1))
fullStatistics(shar.data, expset.name="ks", fit.comp=T, fit.comp.par=list(snapshots=c(100,300,499)), show.only=T)

fit <- loadData("fit", jobs=10, fitlim=c(0,60), load.behavs=T, behavs.sample=0.1, subpops=1, expname="Fit", vars.group=c("passes","steps","disp","passLength"))
ts <- loadData("ls50", jobs=10, fitlim=c(0,60), load.behavs=T, subpops=1, behavs.sample=0.1, expname="TS", vars.group=c("passes","steps","disp","passLength"))
batch <- loadData("cl50_batch_d3", jobs=10, fitlim=c(0,60), load.behavs=T,load.weights=T, load.clusters=T, subpops=1, behavs.sample=0.1, expname="CL-Batch", vars.group=c("passes","steps","disp","passLength"), vars.file=c("passes","steps","disp","passLength",paste0("gen",1:50)))    
bal25 <- loadData("cl50_bal25_d3", jobs=10, fitlim=c(0,60), load.behavs=T,load.weights=T, load.clusters=T, subpops=1, behavs.sample=0.1, expname="CL-Bal25", vars.group=c("passes","steps","disp","passLength"), vars.file=c("passes","steps","disp","passLength",paste0("gen",1:50)))
fullStatistics(list(fit,ts,batch,bal25), expset.name="ks", show.only=F, fit.comp=T, fit.comp.par=list(snapshots=c(150,300,499)), som.group=T, som.alljobs=T)
w.batch <- weightAnalysis(batch) ; w.bal25 <- weightAnalysis(bal25)
plotMultiline(smoothFrame(data.frame(gen=w.batch$gen,batch=w.batch$mean,bal25=w.bal25$mean), window=5), ylim=NULL, ylabel="Fitness-correlation", title="Mean correlation")
c.batch <- clusterAnalysis(batch) ; c.bal25 <- clusterAnalysis(bal25)
plotMultiline(smoothFrame(data.frame(gen=c.batch$gen,batch=c.batch$mean,bal25=c.bal25$mean), window=5), ylim=NULL, ylabel="Mean cluster change", title="Mean Clusters change")



setwd("~/exps/generic2/keepaway/")
data.ks <- metaLoadData("fit","ls50","nsga","cl50_bal25_d3","cl50_bal25_d3_nsga", params=list(fitlim=c(0,60),jobs=10,load.behavs=F,subpops=1))
fullStatistics(data.ks, expset.name="ks.nsga", fit.comp=T, fit.comp.par=list(snapshots=c(150,300,499)), show.only=F)
setwd("~/exps/generic2/sharing/")
data.rs <- metaLoadData("fit","ls50","nsga","cl50_bal25_d3","cl50_bal25_d3_nsga","cl100_bal25_d3_nsga_el25", params=list(fitlim=c(0,1),jobs=10,load.behavs=F,subpops=1))
fullStatistics(data.rs, expset.name="rs.nsga", fit.comp=T, fit.comp.par=list(snapshots=c(100,250,399)), show.only=T)
setwd("~/exps/generic2/aggregation/")
data.agg <- metaLoadData("fit","ls50","nsga","cl50_bal25_d3","cl50_bal25_d3_nsga", params=list(fitlim=c(0,1),jobs=10,load.behavs=F,subpops=1))
fullStatistics(data.agg, expset.name="agg.nsga", fit.comp=T, fit.comp.par=list(snapshots=c(75,100,199)), show.only=F)


setwd("~/exps/generic2/keepaway/")
ts <- loadData("ls50", jobs=10, fitlim=c(0,60), subpops=1, load.noveltyind=T, expname="TS")
p <- pressureAnalysis(ts)
bal25 <- loadData("cl50_bal25_d3", jobs=10, fitlim=c(0,60), subpops=1, load.noveltyind=T, expname="Bal25")
p.b <- pressureAnalysis(bal25)
plotMultiline(smoothFrame(data.frame(Generation=p$gen,TS=p$diff, Gen=p.b$diff),10), ylim=NULL, title="KS pressure", ylabel="Novelty-fitness pressure")

setwd("~/exps/generic2/sharing/")
ts <- loadData("ls50", jobs=10, fitlim=c(0,60), subpops=1, load.noveltyind=T, expname="TS")
p <- pressureAnalysis(ts)
bal25 <- loadData("cl50_bal25_d3", jobs=10, fitlim=c(0,60), subpops=1, load.noveltyind=T, expname="Bal25")
p.b <- pressureAnalysis(bal25)
plotMultiline(smoothFrame(data.frame(Generation=p$gen,TS=p$diff, Gen=p.b$diff),10), ylim=NULL, title="RS pressure", ylabel="Novelty-fitness pressure")

setwd("~/exps/generic2/aggregation/")
ts <- loadData("ls50", jobs=10, fitlim=c(0,60), subpops=1, load.noveltyind=T, expname="TS")
p <- pressureAnalysis(ts)
bal25 <- loadData("cl50_bal25_d3", jobs=10, fitlim=c(0,60), subpops=1, load.noveltyind=T, expname="Bal25")
p.b <- pressureAnalysis(bal25)
plotMultiline(smoothFrame(data.frame(Generation=p$gen,TS=p$diff, Gen=p.b$diff),10), ylim=NULL, title="Agg pressure", ylabel="Novelty-fitness pressure")


setwd("~/exps/generic2/keepaway/")
kw.data <- metaLoadData("fit_species","nsga_species","cl50_bal25_d3_nsga/","cl50_bal25_d3_fixed","cl50_bal25_d3_fixed50", params=list(fitlim=c(0,60),jobs=10,load.behavs=F,subpops=1))
fullStatistics(shar.data, expset.name="ks.fix", fit.comp=T, fit.comp.par=list(snapshots=c(100,300,499)), show.only=F)

setwd("~/exps/generic2/sharing/")
shar.data <- metaLoadData("fit","nsga","cl50_bal25_d3_nsga/","cl50_bal25_d3_fixed","cl50_bal25_d3_fixed50", params=list(fitlim=c(0,1),jobs=10,load.behavs=F,subpops=1))
fullStatistics(shar.data, expset.name="rs.fix", fit.comp=T, fit.comp.par=list(snapshots=c(100,250,399)), show.only=F)

setwd("~/exps/generic2/indiana4")
ind.data <- metaLoadData("fit","nsga","cl50_bal25_d3","cl50_bal25_d3_fixed", params=list(fitlim=c(0,1),jobs=10,load.behavs=F,subpops=1))
fullStatistics(ind.data, expset.name="ind.fix", fit.comp=T, fit.comp.par=list(snapshots=c(75,150,249)), show.only=F)

setwd("~/exps/generic2/aggregation/")
data.agg <- metaLoadData("fit","nsga","cl50_bal25_d3_nsga","cl50_bal25_d3_fixed", params=list(fitlim=c(0,1),jobs=10,load.behavs=F,subpops=1))
fullStatistics(data.agg, expset.name="agg.fix", fit.comp=T, fit.comp.par=list(snapshots=c(50,100,199)), show.only=F)

# reuniao

setwd("~/exps/generic2/keepaway/")
kw.data <- metaLoadData("fit_species","nsga_species","cl50_bal25_d3_fixed", names=c("fit","ts","cl50"), params=list(fitlim=c(0,60),jobs=10,load.behavs=F,subpops=1))
fullStatistics(kw.data, expset.name="ks2", fit.comp=T, fit.comp.par=list(snapshots=c(100,300,499)), show.only=F)

setwd("~/exps/generic2/sharing/")
shar.data <- metaLoadData("fit","nsga","cl50_bal25_d3_fixed", names=c("fit","ts","cl50"), params=list(fitlim=c(0,1),jobs=10,load.behavs=F,subpops=1))
fullStatistics(shar.data, expset.name="rs2", fit.comp=T, fit.comp.par=list(snapshots=c(100,250,399)), show.only=F)

setwd("~/exps/generic2/indiana4")
ind.data <- metaLoadData("fit","nsga","cl50_bal25_d3_fixed", names=c("fit","ts","cl50"), params=list(fitlim=c(0,1),jobs=10,load.behavs=F,subpops=1))
fullStatistics(ind.data, expset.name="ind2", fit.comp=T, fit.comp.par=list(snapshots=c(75,150,249)), show.only=F)

setwd("~/exps/generic2/aggregation/")
data.agg <- metaLoadData("fit","nsga","cl50_bal25_d3_fixed", names=c("fit","ts","cl50"), params=list(fitlim=c(0,1),jobs=10,load.behavs=F,subpops=1))
fullStatistics(data.agg, expset.name="agg2", fit.comp=T, fit.comp.par=list(snapshots=c(50,100,199)), show.only=F)

setwd("~/exps/generic2/indiana3")
data <- metaLoadData("fit","nsga", params=list(fitlim=c(0,1),jobs=10,load.behavs=F,subpops=1))
fullStatistics(data, expset.name="rs", fit.comp=T, fit.comp.par=list(snapshots=c(50,100,199)), show.only=T)



###

setwd("~/exps/generic2/keepaway/")
kw.data <- metaLoadData("fit_species","nsga_species","cl50_bal25_d3_fixed","cl50_spearman_norm/","cl50_brown_all/","cl50_brown_norm", names=c("fit","ts","sp-a","sp-n","br-a","br-n"), params=list(fitlim=c(0,60),jobs=10,load.behavs=F,subpops=1))
fullStatistics(kw.data, expset.name="ks.w", fit.comp=T, fit.comp.par=list(snapshots=c(100,300,499)), show.only=F)

setwd("~/exps/generic2/indiana4")
ind.data <- metaLoadData("fit/","nsga","cl50_bal25_d3_fixed","cl50_spearman_norm/","cl50_brown_all/","cl50_brown_norm", names=c("fit","ts","sp-a","sp-n","br-a","br-n"), params=list(fitlim=c(0,1),jobs=10,load.behavs=F,subpops=1))
fullStatistics(ind.data, expset.name="ind.w", fit.comp=T, fit.comp.par=list(snapshots=c(75,150,249)), show.only=F)

###

setwd("~/exps/generic2/sharing/")
shar.data <- metaLoadData("fit2","nsga_fit2","cl50_brown_fit2", names=c("fit","ts","cl50"), params=list(fitlim=c(0,1),jobs=10,load.behavs=F,subpops=1))
fullStatistics(shar.data, expset.name="rs2", fit.comp=T, fit.comp.par=list(snapshots=c(100,250,399)), show.only=T)

setwd("~/exps/generic2/aggregation/")
data.agg <- metaLoadData("fit","nsga","cl50_brown", names=c("fit","ts","cl50"), params=list(fitlim=c(0,1),jobs=10,load.behavs=F,subpops=1))
fullStatistics(data.agg, expset.name="agg2", fit.comp=T, fit.comp.par=list(snapshots=c(50,100,199)), show.only=T)

#### 


setwd("~/exps/generic2/keepaway/")
kw.data <- metaLoadData("cl50_bal25_d3_fixed/","cl100_bal25_d3/","cl100_bal25_d3_tr25/","cl100_bal25_d3_tr50/","cl200_bal25_d3/", params=list(fitlim=c(0,60),jobs=10,load.behavs=F,subpops=1))
fullStatistics(kw.data, expset.name="ks.fix", fit.comp=T, fit.comp.par=list(snapshots=c(100,300,499)), show.only=T)


setwd("~/exps/generic2/keepaway/")
kw <- loadData("cl50_bal25_d3_fixed", jobs=10, fitlim=c(0,60), load.behavs=T, subpops=1, behavs.sample=0.1, expname="KW", vars.group=paste0("b",1:50), vars.file=c(NA,NA,NA,NA,paste0("b",1:50)))
plotListToPDF(individualFitnessPlots(kw), show=T)
kw <- filterJobs(kw, c("job.1","job.5","job.7"))
z.kw <- characterisationStats(kw)
plotMultiline(smoothFrame(z.kw,10), ylim=NULL, title="KS")

setwd("~/exps/generic2/sharing/")
rs <- loadData("cl50_bal25_d3_fixed", jobs=10, fitlim=c(0,1), load.behavs=T, subpops=1, behavs.sample=0.1, expname="RS", vars.group=paste0("b",1:50), vars.file=c(NA,NA,NA,NA,paste0("b",1:50)))
plotListToPDF(individualFitnessPlots(rs), show=T)
rs <- filterJobs(rs, c("job.0","job.3","job.5","job.7"))
z.rs <- characterisationStats(rs)
plotMultiline(smoothFrame(z.rs,10), ylim=NULL, title="RS")

setwd("~/exps/generic2/indiana4")
ind <- loadData("cl50_bal25_d3_fixed", jobs=10, fitlim=c(0,1), load.behavs=T, subpops=1, behavs.sample=0.1, expname="Ind", vars.group=paste0("b",1:50), vars.file=c(NA,NA,NA,NA,paste0("b",1:50)))
plotListToPDF(individualFitnessPlots(ind), show=T)
ind <- filterJobs(ind, c("job.2","job.3","job.6","job.7"))
z.ind <- characterisationStats(ind)
plotMultiline(smoothFrame(z.ind,10), ylim=NULL, title="Ind")

setwd("~/exps/generic2/aggregation/")
agg <- loadData("cl50_bal25_d3_fixed", jobs=10, fitlim=c(0,1), load.behavs=T, subpops=1, behavs.sample=0.1, expname="Agg", vars.group=paste0("b",1:50), vars.file=c(NA,NA,NA,NA,paste0("b",1:50)))
z.agg <- characterisationStats(agg)
plotMultiline(smoothFrame(z.agg,10), ylim=NULL, title="Agg")






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

setwd("~/exps/generic2/pred")
vars <- c("captured","time","predDisp","maxDisp","preyDisp")
data.fit <- loadData("fit_mp2", jobs=10, gens=0:249, load.behavs=T, subpops=3, behavs.sample=0.25, vars.group=vars)
data.nsga <- loadData("nsga_mp2", jobs=10, gens=0:249, load.behavs=T, subpops=3, behavs.sample=0.25, vars.group=vars)
data.cl <- loadData("cl50_mp2", jobs=10, gens=0:249, load.behavs=T, subpops=3, behavs.sample=0.25, vars.group=vars, vars.file=c(vars,paste0("B",1:50)))
datalist <- list(data.fit, data.nsga, data.cl)
fullStatistics(datalist, expset.name="pred.mp2", fit.comp=T, fit.comp.par=list(snapshots=c(75,150,249)), som.group=T, som.alljobs=T)



setwd("~/exps/generic2/ksh")
data <- metaLoadData("fit_noneat","nsga_noneat","nsga_b_noneat","cl50_noneat", params=list(jobs=10,load.behavs=F))
fullStatistics(data, expset.name="ksh", fit.comp=T, fit.comp.par=list(snapshots=c(100,300,499)))


setwd("~/exps/generic2/ksh")
data <- metaLoadData("fit","nsga_b/","nsga_semigen/", params=list(jobs=8,load.behavs=F, gens=0:299))
fullStatistics(data, expset.name="ksh", fit.comp=T, fit.comp.par=list(snapshots=c(100,200,299)), show.only=T)

setwd("~/exps/generic2/keepaway")
data <- metaLoadData("fit/","nsga","nsga_b/","cl50_brown_norm_biased/","nsga_semigen", params=list(jobs=10,load.behavs=F))
fullStatistics(data, expset.name="ksh", fit.comp=T, fit.comp.par=list(snapshots=c(100,300,499)), show.only=T)

setwd("~/exps/generic2/indiana4")
data <- metaLoadData("fit/","nsga","cl50_brown_norm","nsga_semigen", params=list(jobs=10,load.behavs=F))
fullStatistics(data, expset.name="ksh", fit.comp=T, fit.comp.par=list(snapshots=c(100,250,399)), show.only=T)

setwd("~/exps/generic2/keepaway")
data <- metaLoadData("fit/","nsga","nsga_b/","cl50_brown_norm_biased/","nsga_semigen", params=list(jobs=10,load.behavs=F))
fullStatistics(data, expset.name="ksh", fit.comp=T, fit.comp.par=list(snapshots=c(100,300,499)), show.only=T)



# competitive

data <- metaLoadData("fit_neat_rand_b","novcur_neat_rand", names=c("fit","nov"), params=list(jobs=10, vars.ind=c("time","walldist","agdist","mov"), subpops=2, load.behavs=T, behavs.sample=0.25))
fullStatistics(data, som.ind=TRUE, behav.mean=TRUE, show.only=F, expset.name="fit_vs_novcur")

data <- metaLoadData("fit_neat_rand_b","fit_neat_nov", names=c("fitrand","fitnov"), params=list(jobs=10, vars.ind=c("time","walldist","agdist","mov"), subpops=2, load.behavs=T, behavs.sample=0.25))
fullStatistics(data, som.ind=TRUE, behav.mean=TRUE, show.only=F, expset.name="fitrand_vs_fitnov")

analyse("nov_neat_rand","novcur_neat_rand","fit_neat_rand_b",filename="comp.stat", vars.pre=c("gen"), vars.sub=c("fit0","fit1","bestfit0","bestfit1"), analyse=c("bestfit0","bestfit1"), transform=list(bestfit0=c(-500,1)), smooth=5, splits=5)


analyse("novcur_neat_rand","fit_neat_rand_b","novcur_neat_rand25/","fit_neat_rand25/",filename="comp.stat", vars.pre=c("gen"), vars.sub=c("fit0","fit1","bestfit0","bestfit1"), analyse=c("bestfit0","bestfit1"), transform=list(bestfit0=c(-500,1)), smooth=0, splits=5)

setwd("~/exps//competitive/pred2")
analyse("nov_neat_rand","nov2_neat_rand",filename="compnov.stat", vars.pre=c("gen"), vars.sub=c("fit0","fit1","bestfit0","bestfit1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=10, plot=F)

analyse("nov_neat_rand","nov_neat_novrand","nov_neat_novcent",filename="compall.stat", vars.pre=c("gen"), vars.sub=c("fit0","fit1","bestfit0","bestfit1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=5)


setwd("~/exps//competitive/go2")
analyse("fit_neat_rand","nov2_neat_rand",filename="comprand.stat", vars.pre=c("gen"), vars.sub=c("fit0","fit1","bestfit0","bestfit1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=5, boxplots=T, print=F, all=F)
analyse("fit_neat_rand","nov2_neat_rand",filename="comprand.stat", vars.pre=c("gen"), vars.sub=c("fit0","fit1","bestfit0","bestfit1"), analyse=c("fit0","fit1"), smooth=2, splits=5, plot=F, boxplots=F, print=F, all=T)
data <- metaLoadData("fit_neat_rand","nov2_neat_rand", names=c("fitrand","novrand"), params=list(jobs=10, vars.ind=c("captured","lost","time","largest.group","number.groups"), subpops=2, load.behavs=T, behavs.sample=0.25))
fullStatistics(data, som.ind=TRUE, behav.mean=TRUE, show.only=F, expset.name="fit_vs_nov")

setwd("~/exps//competitive/pred2")
analyse("fit_neat_rand","nov_neat_rand",filename="compari.stat", vars.pre=c("gen"), vars.sub=c("fit0","fit1","bestfit0","bestfit1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=5, boxplots=T, print=F, all=F)
analyse("fit_neat_rand","nov_neat_rand",filename="compari.stat", vars.pre=c("gen"), vars.sub=c("fit0","fit1","bestfit0","bestfit1"), analyse=c("fit0","fit1"), smooth=2, splits=5, plot=F, boxplots=F, print=F, all=T)
data <- metaLoadData("fit_neat_rand","nov_neat_rand", names=c("fitrand","novrand"), params=list(jobs=10, vars.ind=c("time","walldist","agdist","mov"), subpops=2, load.behavs=T, behavs.sample=0.25))
fullStatistics(data, som.ind=TRUE, behav.mean=TRUE, show.only=F, expset.name="fit_vs_nov")

analyse("nov_neat_rand",filename="noveltypop.stat", vars.pre=c("gen"), vars.sub=c("size","mean.nov","max.nov","min.nov","mean.repo","max.repo","min.repo"), analyse=c("mean.repo.0","mean.repo.1"))



setwd("~/exps//competitive/go2")
analyse("fit_neat_rand/","div_neat_rand/",filename="comp.stat", vars.pre=c("gen"), vars.sub=c("fit0","fit1","bestfit0","bestfit1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=5, boxplots=T, print=F, all=F)
analyse("fit_neat_rand/","div_neat_rand/", filename="comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","fit1","bestfit0","bestfit1"), analyse=c("fit0","fit1"), smooth=2, splits=5, plot=F, boxplots=F, print=F, all=T)
data <- metaLoadData("fit_neat_rand","nov2_neat_rand","div_neat_rand", names=c("fit","nov","div"), params=list(jobs=10, vars.ind=c("captured","lost","time","largest.group","number.groups"), subpops=2, load.behavs=T, behavs.sample=0.25))
fullStatistics(data, som.ind=TRUE, behav.mean=TRUE, show.only=F, expset.name="arch")

analyse("div_neat_rand/", filename="noveltypop.stat",vars.pre=c("gen"), vars.sub=c("size","mean.nov","max.nov","min.nov","mean.repo","max.repo","min.repo"), analyse=c("mean.nov.0","mean.nov.1"), smooth=2, splits=5, plot=T, boxplots=T, print=F, all=T)



setwd("~/exps//competitive/pred2")
analyse("fit_neat_rand2/","nov_neat_rand2","div_neat_rand/",filename="comp.stat", vars.pre=c("gen"), vars.sub=c("fit0","fit1","bestfit0","bestfit1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=5, boxplots=T, print=F, all=F)
analyse("fit_neat_rand2/","nov_neat_rand2","div_neat_rand/",filename="comp.stat", vars.pre=c("gen"), vars.sub=c("fit0","fit1","bestfit0","bestfit1"), analyse=c("fit0","fit1"), smooth=2, splits=5, plot=F, boxplots=F, print=F, all=T)

setwd("~/exps//competitive/pred3")
analyse("fit_neat_rand/","div_neat_rand/",filename="comp.stat", vars.pre=c("gen"), vars.sub=c("fit0","fit1","bestfit0","bestfit1","diff0","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=5, boxplots=T, print=F, all=F, transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))
analyse("fit_neat_rand/","div_neat_rand/",filename="comp.stat", vars.pre=c("gen"), vars.sub=c("fit0","fit1","bestfit0","bestfit1","diff0","diff1"), analyse=c("fit0","fit1"), smooth=2, splits=5, plot=F, boxplots=F, print=F, all=T, transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))
analyse("fit_neat_rand/","div_neat_rand/",filename="comp.stat", vars.pre=c("gen"), vars.sub=c("fit0","fit1","bestfit0","bestfit1","diff0","diff1"), analyse=c("diff0","diff1"), smooth=0, splits=5, boxplots=T, print=F, all=F, transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))


setwd("~/exps//competitive/go2")

analyse("fit_neat_rand/","div_neat_rand/", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","fit1","bestfit0","bestfit1"), analyse=c("fit0","fit1"), smooth=2, splits=5, plot=T, boxplots=F, print=F, all=T)
analyse("fit_neat_rand/","div_neat_rand/", filename = "compind.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("fit0","fit1"), smooth=2, splits=5, plot=T, boxplots=F, print=F, all=T)

# ---------------------------------
# Reuniao 10/01

setwd("~/exps//competitive/pred3")
analyse("fit_neat_rand/","div_neat_rand","purediv_sel0","purediv","nsgadiv_sel0", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("fit0","fit1"), smooth=2, splits=5, plot=F, boxplots=F, print=F, all=T,transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))
analyse("fit_neat_rand/","div_neat_rand","purediv_sel0","purediv","nsgadiv_sel0", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("fit0","fit1"), smooth=5, splits=5, plot=T, boxplots=F, print=F, all=F,transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))
analyse("fit_neat_rand/","div_neat_rand","purediv_sel0","purediv","nsgadiv_sel0", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=5, plot=T, boxplots=T, print=F, all=F,transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))
analyse("fit_neat_rand/","div_neat_rand","purediv_sel0","purediv","nsgadiv_sel0", filename = "compind.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("fit0","fit1"), smooth=2, splits=5, plot=F, boxplots=F, print=F, all=T,transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))



setwd("~/exps//competitive/go2")
analyse("fit_neat_rand/","div_neat_rand","purediv_sel0","purediv","nsgadiv_sel0", filename = "compdiv.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("fit0","fit1"), smooth=5, splits=5, plot=T, boxplots=F, print=F, all=F)
analyse("fit_neat_rand/","div_neat_rand","purediv_sel0","purediv","nsgadiv_sel0", filename = "compdiv.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("fit0","fit1"), smooth=2, splits=5, plot=F, boxplots=F, print=F, all=T)
analyse("fit_neat_rand/","div_neat_rand","purediv_sel0","purediv","nsgadiv_sel0", filename = "compdiv.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=4, plot=T, boxplots=T, print=F, all=F)
analyse("fit_neat_rand/","div_neat_rand","purediv_sel0","purediv","nsgadiv_sel0", filename = "compind.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("fit0","fit1"), smooth=2, splits=5, plot=F, boxplots=F, print=F, all=T)


setwd("~/exps//competitive/ks")
analyse("purediv_sel0/","nsgadiv_sel0/", filename = "compsel",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("fit0","fit1"), smooth=5, splits=5, plot=T, boxplots=F, print=F, all=T)
analyse("purediv_sel0/","nsgadiv_sel0/", filename = "compsel",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=5, plot=T, boxplots=T, print=F, all=F)
analyse("fit_neat_rand/", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","fit1","bestfit0","bestfit1"), analyse=c("fit0","fit1"), smooth=0, splits=5, plot=T, boxplots=T, print=F, all=F)
analyse("nsgadiv_sel0/", filename = "compsel",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("fit0","fit1"), smooth=0, splits=5, plot=T, boxplots=T, print=F, all=F)


setwd("~/exps//competitive/pred3")
analyse("fit_ga_rand/","reeav","fit_reeav", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("fit0","fit1"), smooth=2, splits=5, plot=T, boxplots=F, print=F, all=T,transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))
analyse("fit_ga_rand/","reeav","fit_reeav", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=5, plot=T, boxplots=T, print=F, all=F,transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))

analyse("fit_rand","fit_nov","nsga_rand","nsga_nov", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=5, plot=T, boxplots=T, print=F, all=F,transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))
analyse("fit_rand","fit_nov","nsga_rand","nsga_nov", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("fit0","fit1"), smooth=5, splits=5, plot=T, boxplots=T, print=F, all=F,transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))

analyse("fit_nov_noreav","fit_nov","nsga_rand","nsga_rand_noreav", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=5, plot=T, boxplots=T, print=F, all=F,transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))
analyse("fit_nov_noreav","fit_nov","nsga_rand","nsga_rand_noreav", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("fit0","fit1"), smooth=5, splits=5, plot=T, boxplots=T, print=F, all=F,transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))


analyse("fit_rand","fit_nov","fit_nov_noreav","nsga_rand","nsga_rand_noreav","nsga_nov", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=5, plot=T, boxplots=T, print=F, all=F,transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))
analyse("fit_rand","fit_nov","fit_nov_noreav","nsga_rand","nsga_rand_noreav","nsga_nov", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("fit0","fit1"), smooth=10, splits=5, plot=T, boxplots=T, print=F, all=T,transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))


analyse("div_sel0", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("fit0","fit1"), smooth=2, splits=5, plot=T, boxplots=F, print=F, all=T,transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))
analyse("div_sel0", filename = "compind.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("fit0","fit1"), smooth=2, splits=5, plot=T, boxplots=F, print=F, all=T,transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))

analyse("fit_rand","nsga_rand_group", filename = "compgroup.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("fit0","fit1"), smooth=2, splits=5, plot=T, boxplots=F, print=F, all=T,transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))


setwd("~/exps//competitive/sel/pred")
analyse("fit_rand","nsga0_rand","nsga1_rand", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("fit0","fit1"), smooth=2, splits=5, plot=T, boxplots=F, print=F, all=T,transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))
data <- metaLoadData("fit_rand","nsga0_rand","nsga1_rand", names=c("fitrand","nsga0rand","nsga1rand"), params=list(jobs=10, vars.ind=c("time","walldist","agdist","mov"), subpops=2, load.behavs=T, behavs.sample=0.25))
fullStatistics(data, som.sepind=T, behav.mean=F, show.only=F, expset.name="fit_vs_nsga")

analyse("nsga0_rand","nsga0_novarch", filename = "comp0.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("fit0","fit1"), smooth=2, splits=5, plot=T, boxplots=F, print=F, all=T,transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))


setwd("~/exps//competitive/sel/ks")
analyse("fit_rand","nsga0_rand","nsga1_rand", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("fit0","fit1"), smooth=2, splits=5, plot=T, boxplots=F, print=F, all=T)

setwd("~/exps//competitive/sel/go")
analyse("fit_rand","nsga0_rand","nsga1_rand", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("fit0","fit1"), smooth=2, splits=5, plot=T, boxplots=F, print=F, all=T,transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))
analyse("fit_rand","nsga0_rand","nsga1_rand", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=5, plot=T, boxplots=T, print=F, all=F)


data <- loadData("fit_rand/", jobs=10, vars.ind=c("captured","lost","time","groupsize","groupnumber"), load.behavs=T, behavs.sample=0.25)


setwd("~/exps//competitive/ga/go")
analyse("fit_rand","fit_novarch","fit_novhall","nov_rand","nsga_rand","nsga_rand_noarch","nov0_rand","nov1_rand","nsga0_rand","nsga1_rand", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=1, plot=T, boxplots=T, print=F, all=F)
analyse("fit_rand","fit_novarch","fit_novhall", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=1, plot=T, boxplots=T, print=F, all=F)
analyse("nov_rand/","nsga_rand","nsga_rand_noarch", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=1, plot=T, boxplots=T, print=F, all=F)
analyse("nov_rand","nov0_rand","nov1_rand","nsga_rand","nsga0_rand","nsga1_rand", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=1, plot=T, boxplots=T, print=F, all=F)
analyse("fit_rand","nov0_rand","nov1_rand", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=1, plot=T, boxplots=T, print=F, all=F)
analyse("fit_rand","nsga0_rand","nsga1_rand", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=1, plot=T, boxplots=T, print=F, all=F)
analyse("fit_rand","nov0_rand","nov1_rand", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("fit0","fit1"), smooth=0, splits=1, plot=T, boxplots=F, print=F, all=T)

setwd("~/exps//competitive/ga/ks")
analyse("fit_rand","fit_novarch","fit_novhall","nsga_rand","nsga_rand_noarch","nov0_rand","nov1_rand","nsga0_rand","nsga1_rand", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=1, plot=T, boxplots=T, print=F, all=F)
analyse("fit_rand","fit_novarch","fit_novhall", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=1, plot=T, boxplots=T, print=F, all=F)
analyse("nov_rand/","nsga_rand","nsga_rand_noarch", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=1, plot=T, boxplots=T, print=F, all=F)
analyse("nsga_rand","nov0_rand","nov1_rand","nsga0_rand","nsga1_rand", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=1, plot=T, boxplots=T, print=F, all=F)
analyse("fit_rand","nov0_rand","nov1_rand", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=1, plot=T, boxplots=T, print=F, all=F)
analyse("fit_rand","nov0_rand","nov1_rand", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("fit0","fit1"), smooth=0, splits=1, plot=T, boxplots=F, print=F, all=T)

setwd("~/exps//competitive/ga/pred")
analyse("fit_rand","fit_novarch","fit_novhall","nsga_rand","nsga_rand_noarch","nov0_rand","nov1_rand","nsga0_rand","nsga1_rand", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=5, plot=T, boxplots=T, print=F, all=F, transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))
analyse("fit_rand","fit_novarch","fit_novhall", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=1, plot=T, boxplots=T, print=F, all=F, transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))
analyse("nov_rand","nsga_rand","nsga_rand_noarch", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=1, plot=T, boxplots=T, print=F, all=F, transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))
analyse("nsga_rand","nov0_rand","nov1_rand","nsga0_rand","nsga1_rand", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=1, plot=T, boxplots=T, print=F, all=F, transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))
analyse("fit_rand","nov0_rand","nov1_rand", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=1, plot=T, boxplots=T, print=F, all=F, transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))
analyse("fit_rand","nov0_rand","nov1_rand", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("fit0","fit1"), smooth=0, splits=1, plot=T, boxplots=F, print=F, all=T, transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))
analyse("fit_rand","nsga0_rand","nsga1_rand", filename = "comp.stat",vars.pre=c("gen"), vars.sub=c("fit0","bestfit0","diff0","fit1","bestfit1","diff1"), analyse=c("bestfit0","bestfit1"), smooth=0, splits=1, plot=T, boxplots=T, print=F, all=F, transform=list(fit0=c(-500,1),bestfit0=c(-500,1)))

correlation("fit_rand", jobs=20, files=c("comp.stat","compind.stat"), cols=c(2,2))
correlation("nsga0_rand", jobs=20, files=c("comp.stat","compind.stat"), cols=c(2,2))


setwd("~/exps//sys//rs")
data <- loadData("fit", jobs=10, vars.group=c("survivors","energy","mov","dist","s.alive","s.massmov","s.dispersion","s.masspos.x","s.masspos.y","s.indmov","s.energy","s.charging","s.dist","s.station","s.simlength"), vars.file=c("survivors","energy","mov","dist","VOID","s.alive","s.massmov","s.dispersion","s.masspos.x","s.masspos.y","s.indmov","s.energy","s.charging","s.dist","s.station","s.simlength") ,subpops=1, load.behavs=T, behavs.sample=0.05)
behaviourCorrelation(data, method="spearman")

setwd("~/exps//sys//ind")
data <- loadData("fit", jobs=10, vars.group=c("escaped","opentime","distance","dispersion","s.alive","s.massmov","s.dispersion","s.masspos.x","s.masspos.y","s.indmov","s.passing","s.distwall","s.distgate","s.gatestate","s.simlength"), vars.file=c("escaped","opentime","distance","dispersion","VOID","s.alive","s.massmov","s.dispersion","s.masspos.x","s.masspos.y","s.indmov","s.passing","s.distwall","s.distgate","s.gatestate","s.simlength") ,subpops=1, load.behavs=T, behavs.sample=0.05)
behaviourCorrelation(data, method="spearman")

setwd("~/exps/sys/ks")
data <- loadData("fit", jobs=10, vars.group=c("passes","simlength","dispersion","passlength","k.alive","k.massmov","k.dispersion","k.masspos.x","k.masspos.y","k.indmov","k.possession","k.distball","k.disttaker","t.alive","t.pos.x","t.pos.y","t.mov","t.distball","b.speed","s.simlength"), vars.file=c("passes","simlength","dispersion","passlength","VOID","k.alive","k.massmov","k.dispersion","k.masspos.x","k.masspos.y","k.indmov","k.possession","k.distball","k.disttaker","t.alive","t.pos.x","t.pos.y","t.mov","t.distball","b.speed","s.simlength") ,subpops=1, load.behavs=T, behavs.sample=0.05)
behaviourCorrelation(data, method="spearman")

setwd("~/exps/sys/agg")
data <- loadData("fit", jobs=10, vars.group=c(paste0("cm",1:20),"s.alive","s.massmov","s.dispersion","s.masspos.x","s.masspos.y","s.indmov","s.distwall","s.simlength"), vars.file=c(paste0("cm",1:20),"VOID","s.alive","s.massmov","s.dispersion","s.masspos.x","s.masspos.y","s.indmov","s.distwall","s.simlength") ,subpops=1, load.behavs=T, behavs.sample=0.1)
behaviourCorrelation(data, method="spearman")

setwd("~/exps//sys//ind")
data <- loadData("nov_reg", jobs=4, vars.group=c("escaped","opentime","distance","dispersion","s.alive","m.alive","s.massmov","m.massmov","s.dispersion","m.dispersion","s.masspos.x","m.masspos.x","s.masspos.y","m.masspos.y","s.indmov","m.indmov","s.passing","m.passing","s.distwall","m.distwall","s.distgate","m.distgate","s.gatestate","m.gatestate","s.simlength"), vars.file=c("escaped","opentime","distance","dispersion","VOID","s.alive","m.alive","s.massmov","m.massmov","s.dispersion","m.dispersion","s.masspos.x","m.masspos.x","s.masspos.y","m.masspos.y","s.indmov","m.indmov","s.passing","m.passing","s.distwall","m.distwall","s.distgate","m.distgate","s.gatestate","m.gatestate","s.simlength") ,subpops=1, load.behavs=T, behavs.sample=0.05)
behaviourCorrelation(data, method="brownian")
behaviourCorrelation(data, method="spearman")
behaviourCorrelation(data, method="pearson")

setwd("~/exps/sys/rs")
data <- metaLoadData("fit","ts","sys_mean","sys_mean2","sys_mean2_spear","sys_reg2","sys_frame2", params=list(jobs=5, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=F, expset.name="sys", fit.comp.par=list(snapshots=c(100,250,399)))

data <- metaLoadData("fit","ts","sys_mean2","sys_mean2", params=list(jobs=10, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="sys", fit.comp.par=list(snapshots=c(100,250,399)))

setwd("~/exps/sys/ind")
data <- metaLoadData("fit","ts","sys_mean","sys_mean2","sys_mean2_spear","sys_reg2","sys_frame2", params=list(jobs=5, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=F, expset.name="sys", fit.comp.par=list(snapshots=c(75,150,249)))

data <- metaLoadData("fit","sys_mean","sys_reg2", params=list(jobs=10, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="sys", fit.comp.par=list(snapshots=c(75,150,249)))


data <- loadData("~/exps//sys//ind//sys_reg2", jobs=1, , vars.group=c("escaped","opentime","distance","dispersion","s.alive","m.alive","s.massmov","m.massmov","s.dispersion","m.dispersion","s.masspos.x","m.masspos.x","s.masspos.y","m.masspos.y","s.indmov","m.indmov","s.passing","m.passing","s.distwall","m.distwall","s.distgate","m.distgate","s.gatestate","m.gatestate","s.simlength"), vars.file=c("escaped","opentime","distance","dispersion","VOID","s.alive","m.alive","s.massmov","m.massmov","s.dispersion","m.dispersion","s.masspos.x","m.masspos.x","s.masspos.y","m.masspos.y","s.indmov","m.indmov","s.passing","m.passing","s.distwall","m.distwall","s.distgate","m.distgate","s.gatestate","m.gatestate","s.simlength") ,subpops=1, load.behavs=T, behavs.sample=0.1)
behaviourCorrelation2(data)

data <- loadData("~/exps//sys//ind//sys_reg2_alt/", jobs=1, , vars.group=c("escaped","opentime","distance","dispersion","s.alive","m.alive","s.massmov","m.massmov","s.dispersion","m.dispersion","s.masspos.x","m.masspos.x","s.masspos.y","m.masspos.y","s.indmov","m.indmov","s.passing","m.passing","s.distwall","m.distwall","s.distgate","m.distgate","s.gatestate","m.gatestate","s.simlength"), vars.file=c("escaped","opentime","distance","dispersion","VOID","s.alive","m.alive","s.massmov","m.massmov","s.dispersion","m.dispersion","s.masspos.x","m.masspos.x","s.masspos.y","m.masspos.y","s.indmov","m.indmov","s.passing","m.passing","s.distwall","m.distwall","s.distgate","m.distgate","s.gatestate","m.gatestate","s.simlength") ,subpops=1, load.behavs=T, behavs.sample=0.1)
behaviourCorrelation2(data)

data <- loadData("~/exps//sys//ind//sys_reg2_alt2/", jobs=1, , vars.group=c("escaped","opentime","distance","dispersion","s.alive","m.alive","s.massmov","m.massmov","s.dispersion","m.dispersion","s.masspos.x","m.masspos.x","s.masspos.y","m.masspos.y","s.indmov","m.indmov","s.passing","m.passing","s.distwall","m.distwall","s.distgate","m.distgate","s.gatestate","m.gatestate","s.simlength"), vars.file=c("escaped","opentime","distance","dispersion","VOID","s.alive","m.alive","s.massmov","m.massmov","s.dispersion","m.dispersion","s.masspos.x","m.masspos.x","s.masspos.y","m.masspos.y","s.indmov","m.indmov","s.passing","m.passing","s.distwall","m.distwall","s.distgate","m.distgate","s.gatestate","m.gatestate","s.simlength") ,subpops=1, load.behavs=T, behavs.sample=0.1)
behaviourCorrelation2(data)

setwd("~/exps/sys/ind")
data <- metaLoadData("fit","ts","sys_mean/","sys_mean_cfs/","sys_reg_cfs/","sys_reg2","sys_mean_arch","sys_mean_cfs2/", params=list(jobs=10, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=F, expset.name="sys", fit.comp.par=list(snapshots=c(75,150,249)))

setwd("~/exps//sys//rs")
data <- metaLoadData("fit","ts","sys_mean/","sys_mean_cfs/","sys_reg_cfs/", params=list(jobs=10, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=F, expset.name="sys", fit.comp.par=list(snapshots=c(100,250,399)))

setwd("~/exps//sys//ks")
data <- metaLoadData("fit","ts","sys_mean/","sys_mean_cfs/","sys_reg/","sys_frames/", params=list(jobs=10, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=F, expset.name="sys", fit.comp.par=list(snapshots=c(100,300,499)))

setwd("~/exps/sys2//rs")
data <- metaLoadData("brownian","mutual_arch","mutual_k5","mutual_k10","mutual_kA","mutual_k10_reg/", params=list(jobs=10, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="sys", fit.comp.par=list(snapshots=c(100,250,399)))

setwd("~/exps/sys2//ind")
data <- metaLoadData("mutual_mean","mutual_mean_kf5","mutual_mean_kf10", params=list(jobs=10, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="sys", fit.comp.par=list(snapshots=c(75,150,249)))

setwd("~/exps/sys2//rs")
data <- metaLoadData("brownian","mutual_k5","mutual_k10","mutual_kf5","mutual_kf10", params=list(jobs=10, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="sys", fit.comp.par=list(snapshots=c(100,200,399)))


setwd("~/exps//competitive/ga//pred")
data <- metaLoadData("fit_rand","nsga0_rand","nsga1_rand", names=c("fitrand","nsga0rand","nsga1rand"), params=list(jobs=20, vars.ind=c("time","walldist","agdist","mov"), subpops=2, load.behavs=T, behavs.file="tbehaviours.stat", behavs.sample=1))
fullStatistics(data, som.ind=T, behav.mean=F, show.only=T, expset.name="fit_vs_nsga")

setwd("~/exps//competitive/ga//go")
data <- metaLoadData("fit_rand","nsga0_rand","nsga1_rand", names=c("fitrand","nsga0rand","nsga1rand"), params=list(jobs=20, vars.ind=c("captured","lost","length","groupSize","groupNumber"), subpops=2, load.behavs=T, behavs.file="tbehaviours.stat", behavs.sample=1))
fullStatistics(data, som.ind=T, behav.mean=F, show.only=T, expset.name="fit_vs_nsga")

setwd("~/exps/sys3//rs")
data <- metaLoadData("~/exps/sys/rs/fit/","~/exps/sys/rs/ts/","mutual_km10","~/exps/sys2/rs/mutual_kf5/","~/exps/sys2/rs/mutual_kf10/","~/exps/sys2/rs/mutual_k5/","~/exps/sys2/rs/mutual_k10/", params=list(jobs=10, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="sys", fit.comp.par=list(snapshots=c(100,200,399)))

setwd("~/exps/sys2//ind")
data <- metaLoadData("mutual_mean_kf5","mutual_mean_kf10", params=list(jobs=10, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="sys", fit.comp.par=list(snapshots=c(75,150,249)))

setwd("~/exps/sys3//ind")
data <- metaLoadData("mutual_km5","mutual_km10","mutual_km10_arch/", params=list(jobs=10, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="sys", fit.comp.par=list(snapshots=c(75,150,249)))

setwd("~/exps/")
data <- metaLoadData("sys/ind/fit/","sys/ind/ts/","sys2/ind/mutual_mean_kf10","sys2/ind/mutual_mean_kf5","sys3/ind/mutual_km5","sys3/ind/mutual_km10", params=list(jobs=10, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="sys", fit.comp.par=list(snapshots=c(75,150,249)))



setwd("~/exps/sys3/ind")
data <- metaLoadData("ga2_ts/","ga2_mutual_km10_ml_min/","ga2_mutual_km10_ml_min_k5/","ga2_mutual_km10_m_min_k5/","ga2_mutual_km10_l_min_k5/", params=list(jobs=10, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="Ind", fit.comp.par=list(snapshots=c(100,250,499)))

setwd("~/exps/sys3/rs")
data <- metaLoadData("ga2_ts/","ga2_mutual_km10_ml_min/","ga2_mutual_km10_ml_min_k5/","ga2_mutual_km10_m_min_k5/","ga2_mutual_km10_l_min_k5/", params=list(jobs=10, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="RS", fit.comp.par=list(snapshots=c(100,250,499)))

setwd("~/exps/sys3/pred")
data <- metaLoadData("ga2_ts/","ga2_mutual_km10_ml_min/","ga2_mutual_km10_ml_min_k5/","ga2_mutual_km10_m_min_k5/","ga2_mutual_km10_l_min_k5/", params=list(jobs=10, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="Pred", fit.comp.par=list(snapshots=c(100,250,499)))

data <- metaLoadData("ga_fit","ga2_ts/","ga_mutual_km10_ml","ga2_mutual_km10_ml_min", params=list(jobs=10, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="Pred", fit.comp.par=list(snapshots=c(100,250,499)))

data <- metaLoadData("ga2_mutual_km10_ml_min/","ga2_mutual_km10_ml_min_k5/", params=list(jobs=10, load.behavs=F, subpops=1))


setwd("~/exps/sys3/pred")
data <- metaLoadData("ga2_ts/","ga2_mutual_km10_ml_min/","ga2_mutual_km10_ml_min10/","ga2_mutual_km10_ml_min50/", params=list(jobs=10, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="Pred", fit.comp.par=list(snapshots=c(100,250,499)))




setwd("~/exps/sys3/ind")
data <- metaLoadData("ga_fit","ga_ts/","ga_nov_ts/","ga_mutual_km10_ml/","ga_nov_mutual_km10_ml/", params=list(jobs=10, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="sys", fit.comp.par=list(snapshots=c(100,250,499)))

setwd("~/exps/sys3/rs")
data <- metaLoadData("ga_fit","ga_ts/","ga_nov_ts/","ga_mutual_km10_ml/","ga_nov_mutual_km10_ml/", params=list(jobs=10, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="sys", fit.comp.par=list(snapshots=c(100,250,499)))

setwd("~/exps/sys3/ks3/")
data <- metaLoadData("ga_fit","ga_ts/","ga_mutual_km10_ml/","ga_mutual_km10_ml_i/", params=list(jobs=10, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="sys", fit.comp.par=list(snapshots=c(100,250,499)))

setwd("~/exps/sys3/rs")
data <- metaLoadData("ga_fit","ga_nov_ts/","ga_nov_mutual_km10_ml/","ga_nov_mutual_km10_ml_nosmooth/","ga_nov_mutual_km10_ml_smooth75/", params=list(jobs=10, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="sys", fit.comp.par=list(snapshots=c(100,250,499)))

setwd("~/exps/sys3/pred/")
data <- metaLoadData("ga_fit", params=list(jobs=10, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="sys", fit.comp.par=list(snapshots=c(100,250,499)))



setwd("~/exps/sys3/rs")
data <- metaLoadData("ga_mutual_km10_2/","ga_mutual_km10_2_flat/", params=list(jobs=10, load.behavs=T, behavs.sample=0.25, subpops=1, vars.file=c("alive","energy","mov","dist","na","alive", "x.avg", "y.avg", "ori.avg", "speed.avg", "energy.avg", "charging.avg", "dispersion", "res.dist",  "res.charging", "simlength"), vars.group=c("alive", "x.avg", "y.avg", "ori.avg", "speed.avg", "energy.avg", "charging.avg", "dispersion", "res.dist",  "res.charging", "simlength")))
fullStatistics(data, fit.comp=T, som.group=T, som.alljobs=T, show.only=T, expset.name="sys", fit.comp.par=list(snapshots=c(100,250,499)))

data1 <- metaLoadData("ga_fit/","ga_ts/", names=c("fit","ts"), params=list(jobs=10, load.behavs=T, behavs.sample=0.25, subpops=1, vars.group=c("alive","energy","mov","dist")))
data2 <- metaLoadData("ga_mutual_km10/","ga_mutual_km10_flat/", names=c("sys","sys_nw"),params=list(jobs=10, load.behavs=T, behavs.sample=0.25, subpops=1, vars.file=c("alive","energy","mov","dist","na","alive", "x.avg", "y.avg", "ori.avg", "speed.avg", "energy.avg", "charging.avg", "dispersion", "res.dist",  "res.charging", "simlength"), vars.group=c("alive","energy","mov","dist")))
data <- c(data1, data2)
fullStatistics(data, fit.comp=T, som.group=T, som.alljobs=T, show.only=F, expset.name="sys", fit.comp.par=list(snapshots=c(100,250,499)))

setwd("~/exps/sys3/ind")
data1 <- metaLoadData("ga_fit/","ga_ts/", names=c("fit","ts"), params=list(jobs=10, load.behavs=T, behavs.sample=0.25, subpops=1, vars.group=c("escaped","opentime","distgate","dispersion")))
data2 <- metaLoadData("ga_mutual_km10/","ga_mutual_km10_flat/", names=c("sys","sys_nw"),params=list(jobs=10, load.behavs=T, behavs.sample=0.25, subpops=1, vars.file=c("escaped","opentime","distgate","dispersion","na","alive","x","y","ori","speed","passing","dispersion","distgate", "distwall", "gate.open","simlength"), vars.group=c("escaped","opentime","distgate","dispersion")))
data <- c(data1, data2)
fullStatistics(data, fit.comp=F, som.group=T, som.alljobs=T, show.only=F, expset.name="sys", fit.comp.par=list(snapshots=c(100,250,499)))




setwd("~/exps/sysf/ind")
data <- loadData("ga_k10_ml_min25/", jobs=20, load.behavs=F, subpops=1, load.weights=T, weights.file="corr.stat")
weightAnalysis(data)

setwd("~/exps/sysf/rs")
data <- loadData("ga_k10_ml_min25/", jobs=20, load.behavs=F, subpops=1, load.weights=T, weights.file="corr.stat")
weightAnalysis(data)


setwd("~/exps/sysf/pred")
data <- loadData("ga_k10_ml_min25_2/", jobs=20, load.behavs=F, subpops=1, load.weights=T, weights.file="corr.stat")
fullStatistics(data, fit.comp=T, show.only=T, expset.name="Pred", fit.comp.par=list(snapshots=c(100,300,749)))
weightAnalysis(data)



setwd("~/exps/sysf/rs")
sysvars <- c("m.alive","f.alive","m.x","f.x","m.y","f.y","m.turn","f.turn","m.speed","f.speed","m.energy","f.energy","m.charging","f.charging","m.dispersion","f.dispersion","m.resdist","f.resdist","m.rescharging","f.rescharging","simlength")
data <- metaLoadData("ga_fit","ga_ts","ga_k10_ml_min25","ga_k10_ml_flat", params=list(jobs=10, load.behavs=T, subpops=1, vars.file=c("survivors","energy","mov","dist","NA",sysvars), vars.group=sysvars, behavs.sample=0.1))
fullStatistics(data, som.group=T, som.alljobs=T, expset.name="rs")

setwd("~/exps/sysf/ind")
sysvars <- c("m.alive",    "f.alive",	"m.x",	"f.x",	"m.y",	"f.y",	"m.turn",	"f.turn",	"m.speed",	"f.speed",	"m.passing",	"f.passing",	"m.dispersion",	"f.dispersion",	"m.distgate",	"f.distgate",	"m.distwall",	"f.distwall",	"m.gate.open",	"f.gate.open",	"simlength")
data <- metaLoadData("ga_fit","ga_ts","ga_k10_ml_min25","ga_k10_ml_flat", params=list(jobs=10, load.behavs=T, subpops=1, vars.file=c("A","B","C","D","NA",sysvars), vars.group=sysvars, behavs.sample=0.1))
fullStatistics(data, som.group=T, som.alljobs=T, expset.name="ind")

setwd("~/exps/sysf/pred")
data <- metaLoadData("ga_fit_2","ga_ts","ga_k10_ml_min25_2","ga_k10_ml_flat_2", names=c("Fit","NS-TS","NS-SW", "NS-SF"), params=list(jobs=20, load.behavs=F, subpops=1, fitlim=c(0,2)))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="Pred", fit.comp.par=list(snapshots=c(100,300,749)))

setwd("~/exps/sysf/ind")
data <- metaLoadData("ga_fit","ga_ts","ga_k10_ml_min25/","ga_k10_ml_flat/", names=c("Fit","NS-TS","NS-SW", "NS-SF"), params=list(jobs=20, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="Pred", fit.comp.par=list(snapshots=c(100,250,499)))

setwd("~/exps/sysf/rs")
data <- metaLoadData("ga_fit","ga_ts","ga_k10_ml_min25","ga_k10_ml_flat", names=c("Fit","NS-TS","NS-SW", "NS-SF"), params=list(jobs=20, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="Pred", fit.comp.par=list(snapshots=c(100,300,749)))



theme_set(theme_bw())
# final ---- fitness plots

setwd("~/exps/sysf/pred")
data <- metaLoadData("ga_fit_2","ga_ts","ga_k10_ml_min25_2","ga_k10_ml_flat_2", names=c("Fit","NS - TS","NS - Sys weighted", "NS - Sys flat"), params=list(jobs=20, load.behavs=F, subpops=1, fitlim=c(0,2)))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="Pred", fit.comp.par=list(snapshots=c(100,300,749)))

setwd("~/exps/sysf/ind")
data <- metaLoadData("ga_fit","ga_ts","ga_k10_ml_min25/","ga_k10_ml_flat/", names=c("Fit","NS - TS","NS - Sys weighted", "NS - Sys flat"), params=list(jobs=20, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="Pred", fit.comp.par=list(snapshots=c(100,250,499)))

setwd("~/exps/sysf/rs")
data <- metaLoadData("ga_fit","ga_ts","ga_k10_ml_min25","ga_k10_ml_flat", names=c("Fit","NS - TS","NS - Sys weighted", "NS - Sys flat"), params=list(jobs=20, load.behavs=F, subpops=1))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="Pred", fit.comp.par=list(snapshots=c(100,300,749)))

# final ---- som plots

setwd("~/exps/sysf/rs")
sysvars <- c("m.alive","f.alive","m.x","f.x","m.y","f.y","m.turn","f.turn","m.speed","f.speed","m.energy","f.energy","m.charging","f.charging","m.dispersion","f.dispersion","m.resdist","f.resdist","m.rescharging","f.rescharging","simlength")
data <- metaLoadData("ga_fit","ga_ts","ga_k10_ml_min25","ga_k10_ml_flat", params=list(jobs=10, load.behavs=T, subpops=1, vars.file=c("survivors","energy","mov","dist","NA",sysvars), vars.group=sysvars, behavs.sample=0.1))
fullStatistics(data, som.group=T, som.alljobs=T, expset.name="rs2")


theme_set(theme_bw()) ; DEF_WIDTH = 5 ; DEF_HEIGHT=3

setwd("~/exps/sysf/ind")
data <- metaLoadData("ga_fit","ga_ts","ga_k10_ml_min25","ga_k10_ml_flat", names=c("Fit","NS-TS","NS-SW", "NS-SF"), params=list(jobs=20, load.behavs=F, subpops=1, fitness.file="refitness.stat", offset=0))
fullStatistics(data, fit.comp=T, show.only=F, expset.name="Indiana", fit.comp.par=list(snapshots=c(100,250,499), jitter=F))

setwd("~/exps/sysf/pred")
data <- metaLoadData("ga_fit","ga_ts","ga_k10_ml_min25","ga_k10_ml_flat", names=c("Fit","NS-TS","NS-SW", "NS-SF"), params=list(jobs=20, load.behavs=F, subpops=1, fitness.file="refitness.stat", offset=0))
fullStatistics(data, fit.comp=T, show.only=F, expset.name="Pred", fit.comp.par=list(snapshots=c(100,300,749), jitter=F))

setwd("~/exps/sysf/rs")
data <- metaLoadData("ga_fit","ga_ts","ga_k10_ml_min25","ga_k10_ml_flat", names=c("Fit","NS-TS","NS-SW", "NS-SF"), params=list(jobs=20, load.behavs=F, subpops=1, fitness.file="refitness.stat", offset=0))
fullStatistics(data, fit.comp=T, show.only=F, expset.name="RS", fit.comp.par=list(snapshots=c(100,300,749), jitter=F))



setwd("~/exps/sysf/ind")
data <- metaLoadData("ga_fit","ga_ts","ga_k10_ml_min25","ga_k10_ml_flat","ga_cl50", names=c("Fit","NS-TS","NS-SW", "NS-SF","NS-CL"), params=list(jobs=10, load.behavs=F, subpops=1, fitness.file="refitness.stat", offset=0))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="Indiana", fit.comp.par=list(snapshots=c(100,250,499), jitter=F))

setwd("~/exps/sysf/pred")
data <- metaLoadData("ga_fit","ga_ts","ga_k10_ml_min25","ga_k10_ml_flat","ga_cl50", names=c("Fit","NS-TS","NS-SW", "NS-SF","NS-CL"), params=list(jobs=10, load.behavs=F, subpops=1, fitness.file="refitness.stat", offset=0))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="Pred", fit.comp.par=list(snapshots=c(100,300,749), jitter=F))

setwd("~/exps/sysf/rs")
data <- metaLoadData("ga_fit","ga_ts","ga_k10_ml_min25","ga_k10_ml_flat","ga_cl50", names=c("Fit","NS-TS","NS-SW", "NS-SF","NS-CL"), params=list(jobs=10, load.behavs=F, subpops=1, fitness.file="refitness.stat", offset=0))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="RS", fit.comp.par=list(snapshots=c(100,300,749), jitter=F))



# specialisation ---------------------------------------------------------------

setwd("~/exps/spec/pred5")
data <- metaLoadData("nospec","nospec_nov","spec_p","spec_p_nov","spec","spec_nov", params=list(jobs=10, subpops=5, load.behavs=T, behavs.sample=0.2, vars.group=c("captured","time","finalDist","predDisp"), vars.ind=c("i.captured","i.preyDist","i.movement","i.predatorDist")))
fullStatistics(data, fit.comp=T, show.only=F, expset.name="sp",fit.comp.par=list(snapshots=c(100,250,399)),som.group=T, som.alljobs=T, som.ind=T)

count <- exploration.count(data, vars=data$nospec$vars.ind)
uniformity.ind(count, NULL, threshold=50)
#uniformity.diff(count)
countg <- exploration.count(data, vars=data$nospec$vars.group)
uniformity.group(countg, threshold=50)

setwd("~/exps/spec/pred5_2")
data <- metaLoadData("nospec","nospec_nov","spec_p","spec", params=list(jobs=10, subpops=5, load.behavs=T, behavs.sample=0.2, vars.group=c("captured","time","finalDist","predDisp"), vars.ind=c("i.captured","i.preyDist","i.movement","i.predatorDist")))
fullStatistics(data, fit.comp=T, show.only=F, expset.name="sp",fit.comp.par=list(snapshots=c(100,250,399)),som.group=T, som.alljobs=T, som.ind=T)

fullStatistics(data, fit.comp=T, show.only=T, expset.name="sp",fit.comp.par=list(snapshots=c(100,250,399)))

count <- exploration.count(data, vars=data$nospec$vars.ind)
uniformity.ind(count, NULL, threshold=50)
countg <- exploration.count(data, vars=data$nospec$vars.group)
uniformity.group(countg, threshold=50)

setwd("~/exps/spec/pred5_2")
data <- metaLoadData("nospec","nospec_nov","fit_xover","nov_xover", params=list(jobs=10, subpops=5, load.behavs=T, behavs.sample=0.2, vars.group=c("captured","time","finalDist","predDisp"), vars.ind=c("i.captured","i.preyDist","i.movement","i.predatorDist")))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="sp",fit.comp.par=list(snapshots=c(100,250,399)))
count <- exploration.count(data, vars=data$nospec$vars.ind)
uniformity.ind(count, NULL, threshold=50)
countg <- exploration.count(data, vars=data$nospec$vars.group)
uniformity.group(countg, threshold=50)

setwd("~/exps/spec/pred5_2")
data <- metaLoadData("nospec_nov","nov_xover","nov_xover_50_50","nov_xover2_50", params=list(jobs=10, subpops=5, load.behavs=T, behavs.sample=0.2, vars.group=c("captured","time","finalDist","predDisp"), vars.ind=c("i.captured","i.preyDist","i.movement","i.predatorDist")))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="crossovers",fit.comp.par=list(snapshots=c(100,250,399)))
count <- exploration.count(data, vars=data$nospec$vars.ind)
uniformity.ind(count, NULL, threshold=50)
countg <- exploration.count(data, vars=data$nospec$vars.group)
uniformity.group(countg, threshold=50)

data <- metaLoadData("nospec","fit_xover","fit_xover_50_50", params=list(jobs=10, subpops=5, load.behavs=T, behavs.sample=0.2, vars.group=c("captured","time","finalDist","predDisp"), vars.ind=c("i.captured","i.preyDist","i.movement","i.predatorDist")))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="crossovers",fit.comp.par=list(snapshots=c(100,250,399)))
count <- exploration.count(data, vars=data$nospec$vars.ind)
uniformity.ind(count, NULL, threshold=50)
countg <- exploration.count(data, vars=data$nospec$vars.group)
uniformity.group(countg, threshold=50)


data <- metaLoadData("fit_sp_000/","fit_sp_010/","fit_sp_020/","fit_sp_030/","fit_sp_040/","fit_sp_050/","fit_sp_075/","fit_sp_100/", params=list(jobs=10, subpops=5, load.behavs=F, behavs.sample=0.2, vars.group=c("captured","time","finalDist","predDisp"), vars.ind=c("i.captured","i.preyDist","i.movement","i.predatorDist")))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="simt",fit.comp.par=list(snapshots=c(100,250,399)))
data <- metaLoadData("fit_sp_030_05/","fit_sp_030_10/","fit_sp_030/","fit_sp_030_50","fit_sp_030_75","fit_sp_030_100", params=list(jobs=10, subpops=5, load.behavs=F, behavs.sample=0.2, vars.group=c("captured","time","finalDist","predDisp"), vars.ind=c("i.captured","i.preyDist","i.movement","i.predatorDist")))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="elitet",fit.comp.par=list(snapshots=c(100,250,399)))
data <- metaLoadData("fit_sp_000","fit_sp_030_10/", params=list(jobs=10, subpops=5, load.behavs=F, behavs.sample=0.2, vars.group=c("captured","time","finalDist","predDisp"), vars.ind=c("i.captured","i.preyDist","i.movement","i.predatorDist")))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="comp",fit.comp.par=list(snapshots=c(100,250,399)))

data <- metaLoadData("fit_sp_000","fit_sp_030_10","test_fit_sp_030_10", params=list(jobs=10, subpops=5, load.behavs=F, behavs.sample=0.2, vars.group=c("captured","time","finalDist","predDisp"), vars.ind=c("i.captured","i.preyDist","i.movement","i.predatorDist")))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="comp",fit.comp.par=list(snapshots=c(100,250,399)))

data <- metaLoadData("fit_sp_000","fit_sp_030_10","test_fit_sp_030_10", params=list(jobs=10, subpops=1, load.behavs=F, fitness.file="refitness.stat", offset=0))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="comp",fit.comp.par=list(snapshots=c(100,250,399)))

data <- metaLoadData("fit_sp_030_10","fit_sp_m30_s40","fit_sp_m30_s50","test_fit_sp_030_10/", params=list(jobs=10, subpops=5, load.behavs=F, behavs.sample=0.2, vars.group=c("captured","time","finalDist","predDisp"), vars.ind=c("i.captured","i.preyDist","i.movement","i.predatorDist")))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="comp",fit.comp.par=list(snapshots=c(100,250,399)))

data <- metaLoadData("fit_sp_m20_s20","fit_sp_m20_s20_t5","fit_sp_m20_s30","fit_sp_m20_s40","fit_sp_m20_s50", params=list(jobs=10, subpops=5, load.behavs=F, behavs.sample=0.2, vars.group=c("captured","time","finalDist","predDisp"), vars.ind=c("i.captured","i.preyDist","i.movement","i.predatorDist")))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="comp",fit.comp.par=list(snapshots=c(100,250,399)))

analyse("fit_sp_m20_s20_t5","fit_sp_m20_s20","fit_sp_m20_s30","fit_sp_m20_s40","fit_sp_m20_s50", filename="specialisation.stat", analyse="merges", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits",paste0("A",1:20)), all=F, boxplots=F, t.tests=F, splits=0, smooth=10)
analyse("fit_sp_m20_s20_t5","fit_sp_m20_s20","fit_sp_m20_s30","fit_sp_m20_s40","fit_sp_m20_s50", filename="specialisation.stat", analyse="splits", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits",paste0("A",1:20)), all=F, boxplots=F, t.tests=F, splits=0, smooth=10)
analyse("fit_sp_m20_s20_t5","fit_sp_m20_s20","fit_sp_m20_s30","fit_sp_m20_s40","fit_sp_m20_s50", filename="specialisation.stat", analyse="npops", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits",paste0("A",1:20)), all=F, boxplots=F, t.tests=F, splits=0, smooth=10)
analyse("fit_sp_m20_s20_t5","fit_sp_m20_s20","fit_sp_m20_s30","fit_sp_m20_s40","fit_sp_m20_s50", filename="specialisation.stat", analyse="maxdisp", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits",paste0("A",1:20)), all=F, boxplots=F, t.tests=F, splits=0, smooth=10)


data <- metaLoadData("fit_sp_000","fit_sp_m20_s40", params=list(jobs=10, subpops=5, load.behavs=T, behavs.sample=0.2, vars.group=c("captured","time","finalDist","predDisp"), vars.ind=c("i.captured","i.preyDist","i.movement","i.predatorDist")))
countg <- exploration.count(data, vars=data$fit_sp_000$vars.group)
uniformity.group(countg, threshold=50)

data <- metaLoadData("fit_sp_000","fit_sp2_m15s15","fit_sp2_m15s20","fit_sp2_m15s25","fit_sp2_m25s25","fit_sp2_m25s35", params=list(jobs=10, subpops=5, load.behavs=F, behavs.sample=0.2, vars.group=c("captured","time","finalDist","predDisp"), vars.ind=c("i.captured","i.preyDist","i.movement","i.predatorDist")))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="comp",fit.comp.par=list(snapshots=c(100,250,399)))

data <- metaLoadData("fit_sp_000","fit_sp2_m15s20","dist_test","dist_test2", params=list(jobs=10, subpops=5, load.behavs=F, behavs.sample=0.2, vars.group=c("captured","time","finalDist","predDisp"), vars.ind=c("i.captured","i.preyDist","i.movement","i.predatorDist")))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="comp",fit.comp.par=list(snapshots=c(100,250,399)))
analyse("fit_sp2_m15s20","dist_test","dist_test2", filename="specialisation.stat", analyse="merges", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits",paste0("A",1:20)), all=F, boxplots=F, t.tests=F, splits=0, smooth=10)
analyse("fit_sp2_m15s20","dist_test","dist_test2", filename="specialisation.stat", analyse="splits", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits",paste0("A",1:20)), all=F, boxplots=F, t.tests=F, splits=0, smooth=10)
analyse("fit_sp2_m15s20","dist_test","dist_test2", filename="specialisation.stat", analyse="npops", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits",paste0("A",1:20)), all=F, boxplots=F, t.tests=F, splits=0, smooth=5)
analyse("fit_sp2_m15s20","dist_test","dist_test2", filename="specialisation.stat", analyse="maxdisp", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits",paste0("A",1:20)), all=F, boxplots=F, t.tests=F, splits=0, smooth=10)

setwd("~/exps/spec/pred5_2")
data <- metaLoadData("fit_sp_000","fit_sp3_m100s120","fit_sp3_m125s145","fit_sp3_m150s170","fit_sp3_m100s120_alt","fit_sp3_m100s120_shared", params=list(jobs=10, subpops=5, load.behavs=F, behavs.sample=0.2, vars.group=c("captured","time","finalDist","predDisp"), vars.ind=c("i.captured","i.preyDist","i.movement","i.predatorDist")))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="comp",fit.comp.par=list(snapshots=c(100,250,399)))
analyse("fit_sp3_m100s120","fit_sp3_m125s145","fit_sp3_m150s170","fit_sp3_m100s120_alt","fit_sp3_m100s120_shared", filename="specialisation.stat", analyse="merges", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits",paste0("A",1:20)), all=F, boxplots=F, t.tests=F, splits=0, smooth=10)
analyse("fit_sp3_m100s120","fit_sp3_m125s145","fit_sp3_m150s170","fit_sp3_m100s120_alt","fit_sp3_m100s120_shared", filename="specialisation.stat", analyse="splits", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits",paste0("A",1:20)), all=F, boxplots=F, t.tests=F, splits=0, smooth=10)
analyse("fit_sp3_m100s120","fit_sp3_m125s145","fit_sp3_m150s170","fit_sp3_m100s120_alt","fit_sp3_m100s120_shared", filename="specialisation.stat", analyse="npops", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits",paste0("A",1:20)), all=F, boxplots=F, t.tests=F, splits=0, smooth=5)

setwd("~/exps/spec/pred7")
data <- metaLoadData("fit","fit_sp3_m100s120", params=list(jobs=10, subpops=7, load.behavs=F, behavs.sample=0.2, vars.group=c("captured","time","finalDist","predDisp"), vars.ind=c("i.captured","i.preyDist","i.movement","i.predatorDist")))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="comp",fit.comp.par=list(snapshots=c(100,250,399)))
analyse("fit_sp3_m100s120", filename="specialisation.stat", analyse="npops", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits",paste0("A",1:28)), all=F, boxplots=F, t.tests=F, splits=0, smooth=5)

setwd("~/exps/spec/pred5")
data <- metaLoadData("fit","fit_m110s130_elites","fit_m110s130_full","fit_m110s130_one","fit_m110s130_partial", params=list(jobs=10, subpops=5, load.behavs=F, behavs.sample=0.2, vars.group=c("captured","time","finalDist","predDisp"), vars.ind=c("i.captured","i.preyDist","i.movement","i.predatorDist")))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="comp",fit.comp.par=list(snapshots=c(100,250,399)))
data <- metaLoadData("fit_m110s130_elites","fit_m110s130_elites_homo","fit_m110s130_elites_shared", params=list(jobs=10, subpops=5, load.behavs=F, behavs.sample=0.2, vars.group=c("captured","time","finalDist","predDisp"), vars.ind=c("i.captured","i.preyDist","i.movement","i.predatorDist")))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="comp",fit.comp.par=list(snapshots=c(100,250,399)))

analyse("fit_m110s130_elites","fit_m110s130_full","fit_m110s130_one","fit_m110s130_partial", filename="specialisation.stat", analyse="npops", vars.pre=c("gen","npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits"), vars.sub=c("metapop","diversity","mindistsame","meandistsame","maxdistsame","mindistother","meandistother","maxdistother"), all=F, boxplots=F, t.tests=F, splits=0, smooth=10)
analyse("fit_m110s130_elites","fit_m110s130_full","fit_m110s130_one","fit_m110s130_partial", filename="specialisation.stat", analyse="merges", vars.pre=c("gen","npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits"), vars.sub=c("metapop","diversity","mindistsame","meandistsame","maxdistsame","mindistother","meandistother","maxdistother"), all=F, boxplots=F, t.tests=F, splits=0, smooth=10)
analyse("fit_m110s130_elites","fit_m110s130_full","fit_m110s130_one","fit_m110s130_partial", filename="specialisation.stat", analyse="splits", vars.pre=c("gen","npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits"), vars.sub=c("metapop","diversity","mindistsame","meandistsame","maxdistsame","mindistother","meandistother","maxdistother"), all=F, boxplots=F, t.tests=F, splits=0, smooth=10)
analyse("fit_m110s130_elites","fit_m110s130_full","fit_m110s130_one","fit_m110s130_partial", filename="specialisation.stat", analyse="meandistother.mean", vars.pre=c("gen","npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits"), vars.sub=c("metapop","diversity","mindistsame","meandistsame","maxdistsame","mindistother","meandistother","maxdistother"), all=F, boxplots=F, t.tests=F, splits=0, smooth=10)
analyse("fit_m110s130_elites","fit_m110s130_full","fit_m110s130_one","fit_m110s130_partial", filename="specialisation.stat", analyse="diversity.mean", vars.pre=c("gen","npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits"), vars.sub=c("metapop","diversity","mindistsame","meandistsame","maxdistsame","mindistother","meandistother","maxdistother"), all=F, boxplots=F, t.tests=F, splits=0, smooth=10)

analyse("fit_m110s130_elites", filename="specialisation.stat", analyse="npops", vars.pre=c("gen","npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits"), vars.sub=c("metapop","diversity","mindistsame","meandistsame","maxdistsame","mindistother","meandistother","maxdistother"), all=F, boxplots=F, t.tests=F, splits=0, smooth=10)

analyse("fit_m110s130_elites","fit_m110s130_elites_homo","fit_m110s130_elites_shared", filename="specialisation.stat", analyse="npops", vars.pre=c("gen","npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits"), vars.sub=c("metapop","diversity","mindistsame","meandistsame","maxdistsame","mindistother","meandistother","maxdistother"), all=F, boxplots=F, t.tests=F, splits=0, smooth=10)
analyse("fit_m110s130_elites","fit_m110s130_elites_homo","fit_m110s130_elites_shared", filename="specialisation.stat", analyse="merges", vars.pre=c("gen","npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits"), vars.sub=c("metapop","diversity","mindistsame","meandistsame","maxdistsame","mindistother","meandistother","maxdistother"), all=F, boxplots=F, t.tests=F, splits=0, smooth=10)
analyse("fit_m110s130_elites","fit_m110s130_elites_homo","fit_m110s130_elites_shared", filename="specialisation.stat", analyse="meandistother.mean", vars.pre=c("gen","npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits"), vars.sub=c("metapop","diversity","mindistsame","meandistsame","maxdistsame","mindistother","meandistother","maxdistother"), all=F, boxplots=F, t.tests=F, splits=0, smooth=10)
analyse("fit_m110s130_elites","fit_m110s130_elites_homo","fit_m110s130_elites_shared", filename="specialisation.stat", analyse="diversity.mean", vars.pre=c("gen","npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits"), vars.sub=c("metapop","diversity","mindistsame","meandistsame","maxdistsame","mindistother","meandistother","maxdistother"), all=F, boxplots=F, t.tests=F, splits=0, smooth=10)

data <- metaLoadData("fit","fit_m110s130_elites","fit_m110s130_full", params=list(jobs=10, subpops=5, load.behavs=T, behavs.sample=0.2, vars.group=c("captured","time","finalDist","predDisp"), vars.ind=c("i.captured","i.preyDist","i.movement","i.predatorDist")))
countg <- exploration.count(data, vars=data$fit$vars.group)
uniformity.group(countg, threshold=50)
fullStatistics(data, fit.comp=T, show.only=F, expset.name="sp",fit.comp.par=list(snapshots=c(100,250,399)),som.group=T, som.alljobs=T, som.ind=T)

data <- metaLoadData("fit_m110s130_elites","fit_m110s130_group","fit", names=c("indspec","groupspec","fit"),params=list(jobs=10, subpops=5, load.behavs=F, behavs.sample=0.2, vars.group=c("captured","time","finalDist","predDisp"), vars.ind=c("i.captured","i.preyDist","i.movement","i.predatorDist"),  fitness.file="refitness.stat", offset=0))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="comp",fit.comp.par=list(snapshots=c(399)))
analyse("fit_m110s130_elites","fit_m110s130_group","fit", filename="specialisation.stat", analyse="meandistother.mean", vars.pre=c("gen","npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits"), vars.sub=c("metapop","diversity","mindistsame","meandistsame","maxdistsame","mindistother","meandistother","maxdistother"), all=F, boxplots=F, t.tests=F, splits=0, smooth=10)
analyse("fit_m110s130_elites","fit_m110s130_group","fit", filename="specialisation.stat", analyse="diversity.mean", vars.pre=c("gen","npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits"), vars.sub=c("metapop","diversity","mindistsame","meandistsame","maxdistsame","mindistother","meandistother","maxdistother"), all=F, boxplots=F, t.tests=F, splits=0, smooth=10)
analyse("fit_m110s130_elites","fit_m110s130_group", filename="specialisation.stat", analyse="npops", vars.pre=c("gen","npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits"), vars.sub=c("metapop","diversity","mindistsame","meandistsame","maxdistsame","mindistother","meandistother","maxdistother"), all=F, boxplots=F, t.tests=F, splits=0, smooth=10)


countg <- exploration.count(data, vars=data$fit$vars.group)
uniformity.group(countg, threshold=50)


data <- metaLoadData("fit","fit_m110s130_elites", names=c("Fit","MP-CCEA"), params=list(jobs=10, subpops=5, load.behavs=T, behavs.sample=0.2, vars.group=c("captured","time","finalDist","predDisp"), vars.ind=c("i.captured","i.preyDist","i.movement","i.predatorDist"), fitness.file="refitness.stat", offset=0))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="comp",fit.comp.par=list(snapshots=c(399)))
countg <- exploration.count(data, vars=data$Fit$vars.group)
uniformity.group(countg, threshold=100)
uniformity.group(countg, threshold=100, fitness.threshold=7)
analyse("fit_m110s130_elites", filename="specialisation.stat", analyse="npops", vars.pre=c("gen","npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits"), vars.sub=c("metapop","diversity","mindistsame","meandistsame","maxdistsame","mindistother","meandistother","maxdistother"), all=F, boxplots=F, t.tests=F, splits=0, smooth=5)
analyse("fit_m110s130_elites","fit", filename="specialisation.stat", analyse="meandistother.mean", vars.pre=c("gen","npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits"), vars.sub=c("metapop","diversity","mindistsame","meandistsame","maxdistsame","mindistother","meandistother","maxdistother"), all=F, boxplots=F, t.tests=F, splits=0, smooth=10)

setwd("~/exps/spec/pred5")
data1 <- metaLoadData("fit","fit_m110s130_elites", names=c("Fit","MP_110_130"), params=list(jobs=10, subpops=5, load.behavs=F))
data2 <- metaLoadData("fit_dyn_100_40_3","fit_dyn_110_40_3","fit_dyn_125_40_3", params=list(jobs=5, subpops=5, load.behavs=F))
data3 <- metaLoadData("fit_dyn_125_25_2","fit_dyn_125_25_3","fit_dyn_125_25_5","fit_dyn_125_40_2","fit_dyn_125_40_3","fit_dyn_125_40_5", params=list(jobs=5, subpops=5, load.behavs=F))
data4 <- metaLoadData("fit_dyn_125_25_3","fit_dyn_125_40_3","fit","fit_m110s130_elites", params=list(jobs=5, subpops=5, load.behavs=F))
fullStatistics(data2, fit.comp=T, show.only=T, expset.name="comp",fit.comp.par=list(snapshots=c(399)))

analyse("fit_dyn_100_40_3","fit_dyn_110_40_3","fit_dyn_125_40_3", filename="specialisation.stat", analyse="npops", vars.pre=c("gen","npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits"), vars.sub=c("metapop","diversity","mindistsame","meandistsame","maxdistsame","mindistother","meandistother","maxdistother"), all=F, boxplots=F, t.tests=F, splits=0, smooth=20)
analyse("fit_dyn_100_40_3","fit_dyn_110_40_3","fit_dyn_125_40_3", filename="specialisation.stat", analyse="merges", vars.pre=c("gen","npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits"), vars.sub=c("metapop","diversity","mindistsame","meandistsame","maxdistsame","mindistother","meandistother","maxdistother"), all=F, boxplots=F, t.tests=F, splits=0, smooth=20)
analyse("fit_dyn_100_40_3","fit_dyn_110_40_3","fit_dyn_125_40_3", filename="specialisation.stat", analyse="splits", vars.pre=c("gen","npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits"), vars.sub=c("metapop","diversity","mindistsame","meandistsame","maxdistsame","mindistother","meandistother","maxdistother"), all=F, boxplots=F, t.tests=F, splits=0, smooth=20)


analyse("fit_dyn_125_25_2","fit_dyn_125_25_3","fit_dyn_125_25_5","fit_dyn_125_40_2","fit_dyn_125_40_3","fit_dyn_125_40_5", filename="specialisation.stat", analyse="npops", vars.pre=c("gen","npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits"), vars.sub=c("metapop","diversity","mindistsame","meandistsame","maxdistsame","mindistother","meandistother","maxdistother"), all=F, boxplots=F, t.tests=F, splits=0, smooth=20)
analyse("fit_dyn_125_25_2","fit_dyn_125_25_3","fit_dyn_125_25_5","fit_dyn_125_40_2","fit_dyn_125_40_3","fit_dyn_125_40_5", filename="specialisation.stat", analyse="merges", vars.pre=c("gen","npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits"), vars.sub=c("metapop","diversity","mindistsame","meandistsame","maxdistsame","mindistother","meandistother","maxdistother"), all=F, boxplots=F, t.tests=F, splits=0, smooth=20)
analyse("fit_dyn_125_25_2","fit_dyn_125_25_3","fit_dyn_125_25_5","fit_dyn_125_40_2","fit_dyn_125_40_3","fit_dyn_125_40_5", filename="specialisation.stat", analyse="splits", vars.pre=c("gen","npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits"), vars.sub=c("metapop","diversity","mindistsame","meandistsame","maxdistsame","mindistother","meandistother","maxdistother"), all=F, boxplots=F, t.tests=F, splits=0, smooth=20)

analyse("fit_dyn_125_25_3","fit_dyn_125_40_3","fit_m110s130_elites", filename="specialisation.stat", analyse="npops", vars.pre=c("gen","npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits"), vars.sub=c("metapop","diversity","mindistsame","meandistsame","maxdistsame","mindistother","meandistother","maxdistother"), all=F, boxplots=F, t.tests=F, splits=0, smooth=20)
analyse("fit_dyn_125_25_3","fit_dyn_125_40_3","fit_m110s130_elites", filename="specialisation.stat", analyse="merges", vars.pre=c("gen","npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits"), vars.sub=c("metapop","diversity","mindistsame","meandistsame","maxdistsame","mindistother","meandistother","maxdistother"), all=F, boxplots=F, t.tests=F, splits=0, smooth=20)
analyse("fit_dyn_125_25_3","fit_dyn_125_40_3","fit_m110s130_elites", filename="specialisation.stat", analyse="splits", vars.pre=c("gen","npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","merges","splits"), vars.sub=c("metapop","diversity","mindistsame","meandistsame","maxdistsame","mindistother","meandistother","maxdistother"), all=F, boxplots=F, t.tests=F, splits=0, smooth=20)


data <- metaLoadData("fit_hybrid_m100_s5_t5_e100","fit_hybrid_m100_s10_t10_e100","fit_hybrid_m110_s5_t5_e100","fit_hybrid_m110_s10_t10_e100","fit_hybrid_m125_s5_t5_e100","fit_hybrid_m125_s10_t10_e100", params=list(jobs=10, subpops=5, load.behavs=F))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="hybrid",fit.comp.par=list(snapshots=c(499)))

analyse("fit_hybrid_m110_s10_t2_e100","fit_hybrid_m110_s10_t5_e100","fit_hybrid_m110_s10_t10_e100","fit_hybrid_m110_s5_t5_e100","fit_hybrid_m110_s20_t5_e100", filename="hybrid.stat", analyse="npops", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","mindistother","meandistother","maxdistother","maxdisp","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)
analyse("fit_hybrid_m110_s10_t2_e100","fit_hybrid_m110_s10_t5_e100","fit_hybrid_m110_s10_t10_e100","fit_hybrid_m110_s5_t5_e100","fit_hybrid_m110_s20_t5_e100", filename="hybrid.stat", analyse="merges", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","mindistother","meandistother","maxdistother","maxdisp","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)

data <- metaLoadData("fit_hybrid_m100_s5_t5_e100","fit_hybrid_m100_s10_t10_e100","fit_hybrid_m110_s10_t2_e100","fit_hybrid_m110_s10_t5_e100","fit_hybrid_m110_s10_t10_e100","fit_hybrid_m110_s5_t5_e100","fit_hybrid_m110_s10_t5_e100","fit_hybrid_m110_s20_t5_e100","fit_hybrid_m125_s5_t5_e100","fit_hybrid_m125_s10_t10_e100", params=list(jobs=10, subpops=5, load.behavs=F))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="hybrid",fit.comp.par=list(snapshots=c(499)))
DEF_WIDTH <- 9
analyse("fit_hybrid_m100_s5_t5_e100","fit_hybrid_m100_s10_t10_e100","fit_hybrid_m110_s10_t2_e100","fit_hybrid_m110_s10_t5_e100","fit_hybrid_m110_s10_t10_e100","fit_hybrid_m110_s5_t5_e100","fit_hybrid_m110_s10_t5_e100","fit_hybrid_m110_s20_t5_e100","fit_hybrid_m125_s5_t5_e100","fit_hybrid_m125_s10_t10_e100", filename="hybrid.stat", analyse="npops", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)
analyse("fit_hybrid_m100_s5_t5_e100","fit_hybrid_m100_s10_t10_e100","fit_hybrid_m110_s10_t2_e100","fit_hybrid_m110_s10_t5_e100","fit_hybrid_m110_s10_t10_e100","fit_hybrid_m110_s5_t5_e100","fit_hybrid_m110_s10_t5_e100","fit_hybrid_m110_s20_t5_e100","fit_hybrid_m125_s5_t5_e100","fit_hybrid_m125_s10_t10_e100", filename="hybrid.stat", analyse="merges", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)
analyse("fit_hybrid_m100_s5_t5_e100","fit_hybrid_m100_s10_t10_e100","fit_hybrid_m110_s10_t2_e100","fit_hybrid_m110_s10_t5_e100","fit_hybrid_m110_s10_t10_e100","fit_hybrid_m110_s5_t5_e100","fit_hybrid_m110_s10_t5_e100","fit_hybrid_m110_s20_t5_e100","fit_hybrid_m125_s5_t5_e100","fit_hybrid_m125_s10_t10_e100", filename="hybrid.stat", analyse="splits", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)
analyse("fit_hybrid_m100_s5_t5_e100","fit_hybrid_m100_s10_t10_e100","fit_hybrid_m110_s10_t2_e100","fit_hybrid_m110_s10_t5_e100","fit_hybrid_m110_s10_t10_e100","fit_hybrid_m110_s5_t5_e100","fit_hybrid_m110_s10_t5_e100","fit_hybrid_m110_s20_t5_e100","fit_hybrid_m125_s5_t5_e100","fit_hybrid_m125_s10_t10_e100", filename="hybrid.stat", analyse="meandisp", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)
analyse("fit_hybrid_m100_s5_t5_e100","fit_hybrid_m100_s10_t10_e100","fit_hybrid_m110_s10_t2_e100","fit_hybrid_m110_s10_t5_e100","fit_hybrid_m110_s10_t10_e100","fit_hybrid_m110_s5_t5_e100","fit_hybrid_m110_s10_t5_e100","fit_hybrid_m110_s20_t5_e100","fit_hybrid_m125_s5_t5_e100","fit_hybrid_m125_s10_t10_e100", filename="hybrid.stat", analyse="meandiff", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)
DEF_WIDTH <- 7


data <- metaLoadData("fit_hybrid_m110_s10_t10_e100","fit_hybrid3_m110_s3_t10_e100","fit_hybrid3_m110_s5_t10_e100","fit_hybrid3_m110_s7_t10_e100", params=list(jobs=10, subpops=5, load.behavs=F))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="hybrid",fit.comp.par=list(snapshots=c(499)))
analyse("fit_hybrid_m110_s10_t10_e100","fit_hybrid3_m110_s3_t10_e100","fit_hybrid3_m110_s5_t10_e100","fit_hybrid3_m110_s7_t10_e100", filename="hybrid.stat", analyse="npops", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)
analyse("fit_hybrid_m110_s10_t10_e100","fit_hybrid3_m110_s3_t10_e100","fit_hybrid3_m110_s5_t10_e100","fit_hybrid3_m110_s7_t10_e100", filename="hybrid.stat", analyse="merges", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)
analyse("fit_hybrid_m110_s10_t10_e100","fit_hybrid3_m110_s3_t10_e100","fit_hybrid3_m110_s5_t10_e100","fit_hybrid3_m110_s7_t10_e100", filename="hybrid.stat", analyse="splits", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)
analyse("fit_hybrid_m110_s10_t10_e100","fit_hybrid3_m110_s3_t10_e100","fit_hybrid3_m110_s5_t10_e100","fit_hybrid3_m110_s7_t10_e100", filename="hybrid.stat", analyse="meandisp", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)
analyse("fit_hybrid_m110_s10_t10_e100","fit_hybrid3_m110_s3_t10_e100","fit_hybrid3_m110_s5_t10_e100","fit_hybrid3_m110_s7_t10_e100", filename="hybrid.stat", analyse="meandiff", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)

data <- metaLoadData("fit_passive5","hybrid_passive5_m150","hybrid_passive5_m150_s7", params=list(jobs=10, subpops=5, load.behavs=F))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="hybrid",fit.comp.par=list(snapshots=c(499)))
analyse("hybrid_passive5_m150","hybrid_passive5_m150_s7", filename="hybrid.stat", analyse="npops", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)

setwd("~/exps/spec/herd/")
data <- metaLoadData("fit2_passive6/", params=list(jobs=10, subpops=6, load.behavs=F))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="hybrid",fit.comp.par=list(snapshots=c(499)))
analyse("hybrid_passive6_m125","hybrid_passive6_m150", filename="hybrid.stat", analyse="npops", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)


setwd("~/exps/spec/herd/")
data <- metaLoadData("fit_passive_neat","hybrid_passive_neat","hybrid_passive_neat_one","hybrid_passive_neat_prob","hybrid_passive_neat_rand", params=list(jobs=10, subpops=6, load.behavs=F))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="hybrid",fit.comp.par=list(snapshots=c(100,300,499)))
analyse("hybrid_passive_neat","hybrid_passive_neat_one","hybrid_passive_neat_prob","hybrid_passive_neat_rand", filename="hybrid.stat", analyse="npops", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)
analyse("hybrid_passive_neat","hybrid_passive_neat_one","hybrid_passive_neat_prob","hybrid_passive_neat_rand", filename="hybrid.stat", analyse="merges", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)
analyse("hybrid_passive_neat","hybrid_passive_neat_one","hybrid_passive_neat_prob","hybrid_passive_neat_rand", filename="hybrid.stat", analyse="splits", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)
analyse("hybrid_passive_neat","hybrid_passive_neat_one","hybrid_passive_neat_prob","hybrid_passive_neat_rand", filename="hybrid.stat", analyse="meandisp", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)
analyse("hybrid_passive_neat","hybrid_passive_neat_one","hybrid_passive_neat_prob","hybrid_passive_neat_rand", filename="hybrid.stat", analyse="meandiff", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)

setwd("~/exps/spec/herd/")
data <- metaLoadData("fit_passive_neat","hybrid_passive_neat","hybrid_passive_neat_el20", params=list(jobs=10, subpops=6, load.behavs=F))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="hybrid",fit.comp.par=list(snapshots=c(100,300,499)))
analyse("hybrid_passive_neat","hybrid_passive_neat_el20", filename="hybrid.stat", analyse="npops", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)
analyse("hybrid_passive_neat","hybrid_passive_neat_el20", filename="hybrid.stat", analyse="merges", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)
analyse("hybrid_passive_neat","hybrid_passive_neat_el20", filename="hybrid.stat", analyse="splits", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)
analyse("hybrid_passive_neat","hybrid_passive_neat_el20", filename="hybrid.stat", analyse="meandisp", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)
analyse("hybrid_passive_neat","hybrid_passive_neat_el20", filename="hybrid.stat", analyse="meandiff", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)

analyse("hybrid_passive_neat_el10","hybrid_passive_neat_el20","hybrid_passive_neat_el50","hybrid_passive_neat", filename="hybrid.stat", analyse="npops", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=F, smooth=15)
analyse("hybrid_passive_neat_el10","hybrid_passive_neat_el20","hybrid_passive_neat_el50","hybrid_passive_neat", filename="hybrid.stat", analyse="meandiff", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=F, smooth=15)

analyse("hybrid_passive_neat_el10_m140", filename="hybrid.stat", analyse="npops", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=F, smooth=15)

setwd("~/exps/spec/herd/")
data <- metaLoadData("fit_passive_neat","hybrid_passive_neat_el10","hybrid_passive_neat_el10_m140","hybrid_passive_neat_el10_m155","hybrid_passive_neat_el10_s10","hybrid_passive_neat_el10_s3","hybrid_passive_neat_el10_t20","hybrid_passive_neat_el10_t5", params=list(jobs=10, subpops=6, load.behavs=F))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="hybrid",fit.comp.par=list(snapshots=c(100,300,499)))
analyse("hybrid_passive_neat_el10","hybrid_passive_neat_el10_m140","hybrid_passive_neat_el10_m155","hybrid_passive_neat_el10_s10","hybrid_passive_neat_el10_s3","hybrid_passive_neat_el10_t20","hybrid_passive_neat_el10_t5", filename="hybrid.stat", analyse="npops", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=F, plot=T, smooth=15)

data <- metaLoadData("fit_passive_neat","hybrid_passive_neat_el10","hybrid_passive_neat_el10_s10","hybrid_passive_neat_el10_s3","hybrid_passive_neat_el10_t20", params=list(jobs=10, subpops=6, load.behavs=F))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="hybrid",fit.comp.par=list(snapshots=c(100,300,499)))
analyse("hybrid_passive_neat_el10","hybrid_passive_neat_el10_s10","hybrid_passive_neat_el10_s3","hybrid_passive_neat_el10_t20", filename="hybrid.stat", analyse="npops", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=F, plot=T, smooth=15)
analyse("hybrid_passive_neat_el10","hybrid_passive_neat_el10_s10","hybrid_passive_neat_el10_s3","hybrid_passive_neat_el10_t20", filename="hybrid.stat", analyse="merges", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=F, plot=T, smooth=15)
analyse("hybrid_passive_neat_el10","hybrid_passive_neat_el10_s10","hybrid_passive_neat_el10_s3","hybrid_passive_neat_el10_t20", filename="hybrid.stat", analyse="splits", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=F, plot=T, smooth=15)
analyse("hybrid_passive_neat_el10","hybrid_passive_neat_el10_s10","hybrid_passive_neat_el10_s3","hybrid_passive_neat_el10_t20", filename="hybrid.stat", analyse="meandisp", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=F, plot=T, smooth=15)
analyse("hybrid_passive_neat_el10","hybrid_passive_neat_el10_s10","hybrid_passive_neat_el10_s3","hybrid_passive_neat_el10_t20", filename="hybrid.stat", analyse="meandiff", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=F, plot=T, smooth=15)

data <- metaLoadData("fit_passive_neat","hybrid_passive_neat_el10_s10","hybrid_passive_neat_el10_s10_altnorm_m100","hybrid_passive_neat_el10_s10_altnorm_m125", params=list(jobs=10, subpops=6, load.behavs=F))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="hybrid",fit.comp.par=list(snapshots=c(100,300,499)))
analyse("hybrid_passive_neat_el10_s10","hybrid_passive_neat_el10_s10_altnorm_m100","hybrid_passive_neat_el10_s10_altnorm_m125", filename="hybrid.stat", analyse="npops", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","maxdisp","mindiff","meandiff","maxdiff","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=F, plot=T, smooth=15)

setwd("~/exps/gatest")
data <- metaLoadData("ga","gax","gax2","neat","gax_e5", params=list(jobs=10, subpops=3, load.behavs=F))
fullStatistics(data, fit.comp=T, show.only=T, expset.name="gatest",fit.comp.par=list(snapshots=c(50,100,300,499)))

setwd("~/exps")
data <- metaLoadData("herd5r2_fit","herd5r2_nov", params=list(jobs=10, subpops=5, load.behavs=T, behavs.sample=0.1, vars.ind=c("sheepDist","curralDist","fox1Dist","fox2Dist"), vars.group=c("sheepCurral","time","sheepFence","sheepFox")))
fullStatistics(data, fit.comp=F, som.group=T, som.alljobs=T, fit.comp.par=list(snapshots=c(500)), expset.name="herd", show.only=T)
fullStatistics(data, fit.comp=T,  fit.comp.par=list(snapshots=c(500)), expset.name="herd", show.only=T)

data <- metaLoadData("herd5r_fit","herd5r_nov", params=list(jobs=10, subpops=5, load.behavs=F, behavs.sample=0.1, vars.ind=c("sheepDist","curralDist","fox1Dist","fox2Dist"), vars.group=c("sheepCurral","time","sheepFence","sheepFox")))

setwd("~/exps/herding")
data <- metaLoadData("fit","nov","nov_ind","nov_indgroup", names=c("Fit","NS-T","NS-I","NS-Mix"), params=list(fitness.file="refitness.stat", offset=0, jobs=30, subpops=5, load.behavs=F, behavs.sample=0.1, vars.ind=c("sheepDist","curralDist","fox1Dist","fox2Dist"), vars.group=c("sheepCurral","time","sheepFence","sheepFox")))
fullStatistics(data, fit.comp=T, som.group=F, som.alljobs=F, som.group.par=list(grid.size=10,distance.filter=0.4), fit.comp.par=list(snapshots=c(500)), expset.name="herd", show.only=T)

setwd("~/exps/multirover")
data <- metaLoadData("fit","nov","nov_ind","nov_indgroup", names=c("Fit","NS-T","NS-I","NS-Mix"), params=list(fitness.file="refitness.stat", offset=0, jobs=30, subpops=2, load.behavs=F, behavs.sample=0.1, vars.ind=c("ind.mov","ind.prox","lowActive","highActive"), vars.group=c("captured","distance","movement","proximity")))
fullStatistics(data, fit.comp=T, som.group=F, som.alljobs=F, som.group.par=list(grid.size=10,distance.filter=0.5), fit.comp.par=list(snapshots=c(500)), expset.name="herd", show.only=T)

setwd("~/exps/herding")
data <- metaLoadData("fit","nov","nov_ind","nov_indgroup", names=c("Fit","NS-T","NS-I","NS-Mix"), params=list(fitness.file="refitness.stat", offset=0, jobs=30, subpops=5, load.behavs=T, behavs.sample=0.1, vars.ind=c("sheepDist","curralDist","fox1Dist","fox2Dist"), vars.group=c("sheepCurral","time","sheepFence","sheepFox")))

setwd("~/exps/multirover")
data <- metaLoadData("fit","nov","nov_ind","nov_indgroup", params=list(fitness.file="refitness.stat", offset=0, jobs=30, subpops=2, load.behavs=T, behavs.sample=0.1, vars.ind=c("ind.mov","ind.prox","lowActive","highActive"), vars.group=c("captured","distance","movement","proximity")))

q <- individuals.quantile(data,0.85)
count.g <- exploration.count(data)
uni.g <- uniformity(count.g,mode="jsd")
count.e <- exploration.count(data, min.fit=q)
uni.e <- uniformity(count.e, mode="jsd")
count.i <- exploration.count(data, vars=data[[1]]$vars.ind)
uni.i <- uniformity.ind(count.i, mode="jsd")
prop <- individuals.count(data, min.fit=q)



setwd("~/exps/hybrid/herdtest/")
data <- metaLoadData("s7_f2_p2_hyb","s7_f2_p2_base", params=list(jobs=10,load.behavs=F))
data <- metaLoadData("s7_f2_p1_smart_hyb","s7_f2_p1_smart_base", params=list(jobs=10,load.behavs=F))
data <- metaLoadData("s5_f2_p1_hyb","s5_f2_p1_base", params=list(jobs=10,load.behavs=F))
data <- metaLoadData("s5_f1_p1_smart_hyb","s5_f1_p1_smart_base", params=list(jobs=10,load.behavs=F))
data <- metaLoadData("s10_f2_p2_smart_hyb", params=list(jobs=7,load.behavs=F))

data <- metaLoadData("s7_f2_p2_hyb","s7_f2_p2_hyb2","s7_f2_p2_base", params=list(jobs=7,load.behavs=F))

fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(500)), expset.name="herd", show.only=T)

analyse("s7_f2_p2_hyb","s7_f2_p1_smart_hyb","s5_f2_p1_hyb","s5_f1_p1_smart_hyb", filename="hybrid.stat", analyse=c("npops"), vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","foreigns","selfinds","forinds","allinds","meanage","maxage","meandisp","meandistother","merges","splits","remerges","totalmerges","totalsplits","totalremerges"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)

analyse("s7_f2_p2_hyb", filename="hybrid.stat", analyse=c("allinds"), vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","foreigns","selfinds","forinds","allinds","meanage","maxage","meandisp","meandistother","merges","splits","remerges","totalmerges","totalsplits","totalremerges"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)




setwd("~/exps/hybrid/herd7/")

data <- metaLoadData("sm2_het_s5","sm2_het_s10","sm2_het_m50_s10","sm2_het_s10_rand","sm2_het_s5_e50","sm2_het_u", params=list(jobs=10,load.behavs=F))
analyse("sm2_het_s5","sm2_het_s10","sm2_het_m50_s10","sm2_het_s10_rand","sm2_het_s5_e50","sm2_het_u", filename="hybrid.stat", analyse=c("npops"), vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","foreigns","selfinds","forinds","allinds","meanage","maxage","meandisp","meandistother","merges","splits","remerges","totalmerges","totalsplits","totalremerges"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)
analyse("sm2_het_s5","sm2_het_s10","sm2_het_m50_s10","sm2_het_s10_rand","sm2_het_s5_e50","sm2_het_u", filename="hybrid.stat", analyse=c("allinds"), vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","foreigns","selfinds","forinds","allinds","meanage","maxage","meandisp","meandistother","merges","splits","remerges","totalmerges","totalsplits","totalremerges"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)
analyse("sm2_het_s5","sm2_het_s10","sm2_het_m50_s10","sm2_het_s10_rand","sm2_het_s5_e50","sm2_het_u", filename="hybrid.stat", analyse=c("totalmerges"), vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","foreigns","selfinds","forinds","allinds","meanage","maxage","meandisp","meandistother","merges","splits","remerges","totalmerges","totalsplits","totalremerges"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)
analyse("sm2_het_s5","sm2_het_s10","sm2_het_m50_s10","sm2_het_s10_rand","sm2_het_s5_e50","sm2_het_u", filename="hybrid.stat", analyse=c("totalsplits"), vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","foreigns","selfinds","forinds","allinds","meanage","maxage","meandisp","meandistother","merges","splits","remerges","totalmerges","totalsplits","totalremerges"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)
analyse("sm2_het_s5","sm2_het_s10","sm2_het_m50_s10","sm2_het_s10_rand","sm2_het_s5_e50","sm2_het_u", filename="hybrid.stat", analyse=c("totalremerges"), vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","foreigns","selfinds","forinds","allinds","meanage","maxage","meandisp","meandistother","merges","splits","remerges","totalmerges","totalsplits","totalremerges"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)

data <- metaLoadData("baseline","basic_het_m125","basic_het_m125_prob", params=list(jobs=9,load.behavs=F))
analyse("basic_het_m125","basic_het_m110_e100", filename="hybrid.stat", analyse=c("npops"), vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","foreigns","selfinds","forinds","allinds","meanage","maxage","meandisp","meandistother","merges","splits","remerges","totalmerges","totalsplits","totalremerges"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)



data <- metaLoadData("basic_hetero","safe_hetero","basic_homo","safe_homo", params=list(jobs=10,load.behavs=F))
data <- metaLoadData("safe_hetero_f1","safe_hetero","safe_hetero_f20","safe_hetero_f50", params=list(jobs=10,load.behavs=F))
data <- metaLoadData("safe_hetero_s3","safe_hetero","safe_homo_s3","safe_homo", params=list(jobs=10,load.behavs=F))
data <- metaLoadData("basic_hetero","safe_hetero", params=list(jobs=10,load.behavs=F))
data <- metaLoadData("basic_homo","safe_homo", params=list(jobs=10,load.behavs=F))
data <- metaLoadData("basic_homo_s3","basic_hetero_s3","safe_homo_s3","safe_hetero_s3", params=list(jobs=10,load.behavs=F))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(400)), expset.name="herd", show.only=T)

data <- metaLoadData("safemerge_het_m125","safemerge_het_m150","safemerge_het_m200", params=list(fitness.file="refitness.stat", offset=0,jobs=10,load.behavs=F))
data <- metaLoadData("safemerge_het_f1","safemerge_het_m150","safemerge_het_f25","safemerge_het_f50", params=list(fitness.file="refitness.stat", offset=0,jobs=10,load.behavs=F))
data <- metaLoadData("safemerge_het_m150","safemerge_het_m150_t5","safemerge_het_m150_t20", params=list(fitness.file="refitness.stat", offset=0,jobs=10,load.behavs=F))


analyse("safemerge_het_m125","safemerge_het_m150","safemerge_het_m200", filename="hybrid.stat", analyse="npops", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","mindistother","meandistother","maxdistother","maxdisp","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)
analyse("safemerge_het_f1","safemerge_het_m150","safemerge_het_f25","safemerge_het_f50", filename="hybrid.stat", analyse="npops", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","mindistother","meandistother","maxdistother","maxdisp","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)
analyse("safemerge_het_m150","safemerge_het_m150_t5","safemerge_het_m150_t20", filename="hybrid.stat", analyse="npops", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","mindistother","meandistother","maxdistother","maxdisp","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)


analyse("safemerge_het_m150","safe_hetero","basic_hetero", filename="hybrid.stat", analyse="npops", vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","mindisp","meandisp","mindistother","meandistother","maxdistother","maxdisp","merges","splits"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=15)

data <- metaLoadData("s10_f3_p2_base","s10_f3_p2_hyb", params=list(jobs=10, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 399000, length.out=400))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(400)), expset.name="herd", show.only=T)


data_d <- metaLoadData("s7_f2_p2_hyb_t1","s7_f2_p2_hyb_t5","s7_f2_p2_hyb_m125", params=list(jobs=20, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))
fullStatistics(data_d, fit.comp=T, fit.comp.par=list(snapshots=c(428)), expset.name="herd", show.only=T)

setwd("~/exps/AAMAS15//herdfinal/")
data_d <- metaLoadData("s7_f2_p2_hyb_m100","s7_f2_p2_hyb_m110","s7_f2_p2_hyb_m125","s7_f2_p2_hyb_m150","s7_f2_p2_hyb_m175", params=list(jobs=20, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))
data_d <- metaLoadData("s7_f2_p2_hyb_t1","s7_f2_p2_hyb_t5","s7_f2_p2_hyb_m125_t10","s7_f2_p2_hyb_t20","s7_f2_p2_hyb_t40", params=list(jobs=20, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))

data_s <- metaLoadData("s7_f2_p1_smart_hyb_m100","s7_f2_p1_smart_hyb_m110","s7_f2_p1_smart_hyb","s7_f2_p1_smart_hyb_m150","s7_f2_p1_smart_hyb_m175", params=list(jobs=20, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))
data_s <- metaLoadData("s7_f2_p1_smart_hyb_t1","s7_f2_p1_smart_hyb_t5","s7_f2_p1_smart_hyb_m125_t10","s7_f2_p1_smart_hyb_t20","s7_f2_p1_smart_hyb_t40", params=list(jobs=20, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))

frame <- cbind(fitnessSummary(data_d), T=c("100","110","125","150","175"), Setup="Dumb")
frame <- rbind(frame, cbind(fitnessSummary(data_s), T=c("100","110","125","150","175"), Setup="Smart"))

frame <- cbind(fitnessSummary(data_d), T=c("01","05","10","20","40"), Setup="Dumb")
frame <- rbind(frame, cbind(fitnessSummary(data_s), T=c("01","05","10","20","40"), Setup="Smart"))

pd <- position_dodge(.1) # move them .05 to the left and right
ggplot(frame, aes(x=T, y=mean, colour=Setup)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.25, position=pd) +
  geom_line(position=pd,aes(group=Setup)) +
  geom_point(position=pd) + ylab("Best fitness") + theme(legend.title=element_blank())

data <- metaLoadData("s5_f2_p1_base","s5_f2_p1_hyb","s5_f2_p1_hyb_t20", params=list(jobs=20, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 199500, length.out=400))))
data <- metaLoadData("s5_f1_p1_smart_base","s5_f1_p1_smart_hyb","s5_f1_p1_smart_hyb_t20", params=list(jobs=20, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 199500, length.out=400))))
data <- metaLoadData("s7_f2_p2_base","s7_f2_p2_hyb_m125_t10","s7_f2_p2_hyb_t20", params=list(jobs=20, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=400))))
data <- metaLoadData("s7_f2_p1_smart_base","s7_f2_p1_smart_hyb_m125_t10","s7_f2_p1_smart_hyb_t20", params=list(jobs=20, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=400))))
data <- metaLoadData("s10_f3_p1_smart_base","s10_f3_p1_smart_hyb_t20","s10_f3_p1_smart_hyb_t20_homo", params=list(jobs=20, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 399000, length.out=400))))
data <- metaLoadData("s10_f3_p2_base","s10_f3_p2_hyb_t20", params=list(jobs=20, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 399000, length.out=400))))

data <- metaLoadData("s5_f2_p1_base","s5_f2_p1_hyb","s5_f2_p1_hyb_t20","s5_f2_p1_hyb_homo","s5_f2_p1_hyb_t20_homo", params=list(jobs=10, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 199500, length.out=400))))
data <- metaLoadData("s5_f1_p1_smart_base","s5_f1_p1_smart_hyb","s5_f1_p1_smart_hyb_t20","s5_f1_p1_smart_hyb_homo","s5_f1_p1_smart_hyb_t20_homo", params=list(jobs=10, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 199500, length.out=400))))
data <- metaLoadData("s7_f2_p2_base","s7_f2_p2_hyb_m125_t10","s7_f2_p2_hyb_t20","s7_f2_p2_hyb_homo","s7_f2_p2_hyb_t20_homo", params=list(jobs=10, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=400))))
data <- metaLoadData("s7_f2_p1_smart_base","s7_f2_p1_smart_hyb_m125_t10","s7_f2_p1_smart_hyb_t20","s7_f2_p1_smart_hyb_homo","s7_f2_p1_smart_hyb_t20_homo", params=list(jobs=10, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=400))))
data <- metaLoadData("s10_f3_p1_smart_base","s10_f3_p1_smart_hyb","s10_f3_p1_smart_hyb_t20","s10_f3_p1_smart_hyb","s10_f3_p1_smart_hyb_t20_homo", params=list(jobs=10, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 399000, length.out=400))))

data <- metaLoadData("s5_f2_p1_base","s5_f2_p1_hyb_t20", params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 150000, length.out=300))))
data <- metaLoadData("s5_f1_p1_smart_base","s5_f1_p1_smart_hyb_t20", params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 150000, length.out=300))))
data <- metaLoadData("s7_f2_p2_base","s7_f2_p2_hyb_t20", params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=400))))
data <- metaLoadData("s7_f2_p1_smart_base","s7_f2_p1_smart_hyb_t20", params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=400))))
data <- metaLoadData("s10_f3_p1_smart_base","s10_f3_p1_smart_hyb_t20", params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 399000, length.out=400))))
data <- metaLoadData("s10_f3_p2_base","s10_f3_p2_hyb_t20", params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 399000, length.out=400))))

fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(300)), expset.name="herd", show.only=T)
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(400)), expset.name="herd", show.only=T)

fitnessLevels(data, c(1.5,2.5))
fitnessLevels(data, c(1.5,2.5,3.5))











data <- metaLoadData("s7_f2_p1_smart_base","s7_f2_p1_smart_hyb", params=list(jobs=10, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))

data <- metaLoadData("s5_f1_p1_smart_hyb","s5_f1_p1_smart_base", params=list(jobs=10, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 199500, length.out=400))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(400)), expset.name="herd", show.only=T)

data <- metaLoadData("s7_f2_p1_smart_hyb", params=list(jobs=10, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(428)), expset.name="herd", show.only=T)


datah <- loadData("s7_f2_p2_hyb_m125", jobs=10, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299000, length.out=400)))
datab <- loadData("s7_f2_p2_hyb_base", jobs=10, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299000, length.out=400)))
data <- list(datah,datab)
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(400)), expset.name="herd", show.only=T)

analyse("s5_f1_p1_smart_hyb_t20","s5_f2_p1_hyb_t20","s7_f2_p2_hyb_t20","s7_f2_p1_smart_hyb_t20","s10_f3_p2_hyb_t20", filename="hybrid.stat", analyse=c("npops"), vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","foreigns","selfinds","forinds","allinds","meanage","maxage","meandisp","meandistother","merges","splits","remerges","totalmerges","totalsplits","totalremerges"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=25)



setwd("~/exps/hybrid/nov/")
data <- metaLoadData("s7_f2_p1_smart_hyb_fit","s7_f2_p1_smart_hyb_nov","s7_f2_p1_smart_fit","s7_f2_p1_smart_nov", names=c("hyb.fit","hyb.nov","base.fit","base.nov"), params=list(jobs=10, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(428)), expset.name="S7_F2_P1_EVAS", show.only=T)
fitnessLevels(data, c(1.5,1.75,2.0,2.25))
analyse("s7_f2_p1_smart_hyb_fit","s7_f2_p1_smart_hyb_nov", exp.names=c("Fit","NS"), filename="hybrid.stat", analyse=c("npops"), vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","foreigns","selfinds","forinds","allinds","meanage","maxage","meandisp","meandistother","merges","splits","remerges","totalmerges","totalsplits","totalremerges"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=10, gens=seq(0,500))
data <- metaLoadData("s7_f2_p1_smart_hyb_fit","s7_f2_p1_smart_hyb_nov","s7_f2_p1_smart_fit","s7_f2_p1_smart_nov", names=c("hyb.fit","hyb.nov","base.fit","base.nov"), params=list(jobs=10, load.behavs=T, behavs.sample=0.1, subpops=7, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428)), vars.ind=c("curralDist","fox1Dist","fox2Dist","sheepDist"),vars.group=c("sheepCurral","steps","sheepFence","sheepFox")))
fullStatistics(data, expset.name="hybnov", show.only=T, som.group=T, som.alljobs=T, som.group.par=list(grid.size=10,distance.filter=0.4))




setwd("~/exps/maze/")
data <- metaLoadData("fit","nov_k15", params=list(jobs=10, load.behavs=F, use.evals=F))
data <- metaLoadData("nov_k1","nov_k7","nov_k15","nov_k30","nov_k100","nov_k200", params=list(jobs=10, subpops=1,load.behavs=F))
data <- metaLoadData("nov_s05","nov_s10","nov_k15","nov_s30","nov_s40","nov_s50","nov_s75", params=list(jobs=10, load.behavs=F, use.evals=F))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(500)), expset.name="maze", show.only=T)

data <- metaLoadData("nov_k1","nov_k7","nov_k15","nov_k30","nov_k100","nov_k200", params=list(jobs=10, load.behavs=T, subpops=1, vars.group=c("x","y"), use.evals=F))
data <- metaLoadData("nov_s05","nov_s10","nov_k15","nov_s30","nov_s40","nov_s50","nov_s75", params=list(jobs=10, load.behavs=T, behavs.sample=0.2,subpops=1, vars.group=c("x","y"), use.evals=F))

count <- exploration.count(data,levels=10)
uniformity(count, mode="jsd")

data <- metaLoadData("hard_fit","hard_nov_randarch","hard_nsga_randarch","hard_ls_randarch", params=list(jobs=20, load.behavs=F, use.evals=F))
data <- metaLoadData("hard_nov_noarch","hard_nov_novelarch","hard_nov_randarch", params=list(jobs=20, load.behavs=F, use.evals=F))
data <- metaLoadData("hard_ls_noarch","hard_ls_novelarch","hard_ls_randarch", params=list(jobs=20, load.behavs=F, use.evals=F))
data <- metaLoadData("hard_nsga_noarch","hard_nsga_novelarch","hard_nsga_randarch", params=list(jobs=20, load.behavs=F, use.evals=F))

data <- metaLoadData("med_fit","med_nov_randarch","med_nsga_randarch","med_ls_randarch", params=list(jobs=20, load.behavs=F, use.evals=F))
data <- metaLoadData("med_nov_noarch","med_nov_novelarch","med_nov_randarch", params=list(jobs=20, load.behavs=F, use.evals=F))
data <- metaLoadData("med_ls_noarch","med_ls_novelarch","med_ls_randarch", params=list(jobs=20, load.behavs=F, use.evals=F))
data <- metaLoadData("med_nsga_noarch","med_nsga_novelarch","med_nsga_randarch", params=list(jobs=20, load.behavs=F, use.evals=F))

data <- metaLoadData("hard_nov_noarch","hard_nov_novelarch","hard_nov_randarch", params=list(jobs=20, load.behavs=T, behavs.sample=0.2, subpops=1, vars.group=c("x","y"), vars.file=c("x","y",rep(NA,20))))
data <- metaLoadData("med_nov_noarch","med_nov_novelarch","med_nov_randarch", params=list(jobs=20, load.behavs=T, behavs.sample=0.2,subpops=1, vars.group=c("x","y"), vars.file=c("x","y",rep(NA,20))))
count <- exploration.count(data,levels=10)
uniformity(count, mode="jsd")

fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(500)), expset.name="maze", show.only=T)

data <- metaLoadData("sub_fit","sub_nov_randarch","sub_nsga_randarch","sub_ls_randarch", params=list(jobs=20, load.behavs=F, use.evals=F))
data <- metaLoadData("sub_nov_noarch","sub_nov_novelarch","sub_nov_randarch", params=list(jobs=20, load.behavs=F, use.evals=F))
data <- metaLoadData("sub_ls_noarch","sub_ls_novelarch","sub_ls_randarch", params=list(jobs=20, load.behavs=F, use.evals=F))
data <- metaLoadData("sub_nsga_noarch","sub_nsga_novelarch","sub_nsga_randarch", params=list(jobs=20, load.behavs=F, use.evals=F))

data <- metaLoadData("multi_fit","multi_nov_randarch","multi_nsga_randarch","multi_ls_randarch", params=list(jobs=20, load.behavs=F, use.evals=F))
data <- metaLoadData("multi_nov_noarch","multi_nov_novelarch","multi_nov_randarch", params=list(jobs=20, load.behavs=F, use.evals=F))
data <- metaLoadData("multi_ls_noarch","multi_ls_novelarch","multi_ls_randarch", params=list(jobs=20, load.behavs=F, use.evals=F))
data <- metaLoadData("multi_nsga_noarch","multi_nsga_novelarch","multi_nsga_randarch", params=list(jobs=20, load.behavs=F, use.evals=F))

fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(750)), expset.name="maze", show.only=T)

data <- metaLoadData("sub_fit","sub_nov_randarch","sub_nsga_randarch","sub_ls_randarch", params=list(jobs=10, load.behavs=T, behavs.sample=0.2, subpops=1, vars.group=c("x","y"), vars.file=c("x","y",rep(NA,20))))
data <- metaLoadData("multi_fit","multi_nov_randarch","multi_nsga_randarch","multi_ls_randarch", params=list(jobs=10, load.behavs=T, behavs.sample=0.2,subpops=1, vars.group=c("x","y"), vars.file=c("x","y",rep(NA,20))))
data <- metaLoadData("sub_nov_noarch","sub_nov_novelarch","sub_nov_randarch", params=list(jobs=10, load.behavs=T, behavs.sample=0.2, subpops=1, vars.group=c("x","y"), vars.file=c("x","y",rep(NA,20))))
data <- metaLoadData("multi_nov_noarch","multi_nov_novelarch","multi_nov_randarch", params=list(jobs=10, load.behavs=T, behavs.sample=0.2,subpops=1, vars.group=c("x","y"), vars.file=c("x","y",rep(NA,20))))

count <- exploration.count(data,levels=10)
uniformity(count, mode="jsd")


data <- metaLoadData("sub2_nov", params=list(jobs=10, load.behavs=F, use.evals=F))


setwd("~/exps/maze2/")
data <- metaLoadData("hard_fit","hard_nov_randarch","hard_nov_noarch","hard_nov_arch01","hard_nov_arch03","hard_nov_arch06", params=list(jobs=20, load.behavs=F,gens=0:499))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(500)), expset.name="maze", show.only=T)
data <- metaLoadData("multi_fit","multi_nov_randarch","multi_nov_noarch","multi_nov_arch01","multi_nov_arch03","multi_nov_arch06", params=list(jobs=20, load.behavs=F,gens=0:499))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(500)), expset.name="maze", show.only=T)
data <- metaLoadData("sub_fit","sub_nov","sub_nov_noarch","sub_nov_arch01","sub_nov_arch03","sub_nov_arch06", params=list(jobs=20, load.behavs=F))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(750)), expset.name="maze", show.only=T)

data <- metaLoadData("sub_fit","sub_nov","sub_nsga","sub_nov_long", params=list(jobs=20, load.behavs=F))
data <- metaLoadData("open_fit","open_nov","open_nsga","open_nov_long", params=list(jobs=20, load.behavs=F))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(750)), expset.name="maze", show.only=T)
data <- metaLoadData("hard_fit","hard_nov_randarch","hard_nov_long", params=list(jobs=20, load.behavs=F,gens=0:499))
data <- metaLoadData("multi_fit","multi_nov_randarch","multi_nov_long", params=list(jobs=20, load.behavs=F,gens=0:499))
data <- metaLoadData("med_fit","med_nov_randarch","med_nov_long", params=list(jobs=20, load.behavs=F,gens=0:499))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(500)), expset.name="maze", show.only=T)

data <- metaLoadData("sub_fit","sub_nov","sub_nov_noarch","sub_nov_arch01","sub_nov_arch03","sub_nov_arch06", params=list(jobs=10, load.behavs=T, behavs.sample=0.2,subpops=1, vars.group=c("x","y"), vars.file=c("x","y",rep(NA,20))))
count <- exploration.count(data,levels=10)
uniformity(count, mode="jsd")


data <- metaLoadData("hard_fit","hard_nov_randarch","hard_nov_noarch", params=list(jobs=20, load.behavs=T, behavs.sample=0.2,subpops=1, vars.group=c("x","y"), vars.file=c("x","y",rep(NA,20))))
data <- metaLoadData("multi_fit","multi_nov_randarch","multi_nov_noarch", params=list(jobs=20, load.behavs=T, behavs.sample=0.2,subpops=1, vars.group=c("x","y"), vars.file=c("x","y",rep(NA,20))))
data <- metaLoadData("med_fit","med_nov_randarch","med_nov_noarch", params=list(jobs=20, load.behavs=T, behavs.sample=0.2,subpops=1, vars.group=c("x","y"), vars.file=c("x","y",rep(NA,20))))
data <- metaLoadData("sub_fit","sub_nov","sub_nov_noarch", params=list(jobs=20, load.behavs=T, behavs.sample=0.2,subpops=1, vars.group=c("x","y"), vars.file=c("x","y",rep(NA,20))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(500)), expset.name="maze", show.only=T)
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(750)), expset.name="maze", show.only=T)

count <- exploration.count(data,levels=10)
uniformity(count, mode="jsd")

data <- metaLoadData("star_nov","star_nov_noarch",params=list(jobs=10, load.behavs=F, behavs.sample=0.2,subpops=1, vars.group=c("x","y"), vars.file=c("x","y",rep(NA,20))))
data <- metaLoadData("zigzag_nov","zigzag_nov_noarch",params=list(jobs=10, load.behavs=T, behavs.sample=0.2,subpops=1, vars.group=c("x","y"), vars.file=c("x","y",rep(NA,20))))
data <- metaLoadData("sub2_nov","sub2_nsga",params=list(jobs=10, load.behavs=T, behavs.sample=0.2,subpops=1, vars.group=c("x","y"), vars.file=c("x","y",rep(NA,20))))



############################################### ns params

allframe <- data.frame()
for(maze in c("hard","zigzag","subset","multi","star","open")) {
  setwd("~/exps/maze3/nspar")
  setwd(maze)
  all <- sub("./", "", list.dirs())[-1]
  #all <- all[grep("ga_el10_e",all)]
  #data <- do.call(metaLoadData,c(all,list(params=list(jobs=30, load.behavs=F))))
  #r <- fitnessLevelReached(data,1)
  #r <- fitnessLevels(data,level=1,use.max=1000)
  data <- do.call(metaLoadData,c(all,list(params=list(jobs=30, load.behavs=T, behavs.sample=0.2, vars.group=c("x","y")))))
  r <- uniformity(exploration.count(data,levels=10), mode="jsd")
  
  print(r$ttest)
  frame <- r$summary
  frame$method <- rownames(frame)
  allframe <- rbind(allframe, cbind(frame, Maze=maze))
}

allframe[grep("k15",allframe$method),"Knn"] <- "015"
allframe[grep("k1_",allframe$method),"Knn"] <- "001"
allframe[grep("k50",allframe$method),"Knn"] <- "050"
allframe[grep("k199",allframe$method),"Knn"] <- "199"
allframe[grep("k5_",allframe$method),"Knn"] <- "005"
allframe[grep("k100",allframe$method),"Knn"] <- "100"
allframe[grep("anone",allframe$method),"Archive"] <- "none"
allframe[grep("anovel",allframe$method),"Archive"] <- "novel"
allframe[grep("arandom",allframe$method),"Archive"] <- "random"

save(allframe, file="~/Dropbox/Papers/GECCO15/nspar_uniformity.rdata")

aggregate <- function(frame) {
  newframe <- data.frame()
  for(a in unique(frame$Archive)) {
    for(k in unique(frame$Knn)) {
      if(!is.na(a) & !is.na(k)) {
        sub <- subset(frame, k==Knn & a==Archive)
        val <- mean(sub$mean)
        row <- sub[1,]
        row["mean"] <- val
        newframe <- rbind(newframe, row)
      }
    }
  }
  return(newframe)
}
#frame <- subset(allframe, C=="short")
agg <- aggregate(allframe)
pd <- position_dodge() # move them .05 to the left and right
ggplot(agg, aes(x=Knn, y=mean, colour=Archive, shape=Archive)) + 
  geom_line(position=pd,aes(group=Archive)) +
  geom_point(position=pd,size=4) + ylab("Mean exploration uniformity") + xlab("Knn") 
  #geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.25, position=pd)


# ALL IN ONE BAR PLOT #####################
frame <- allframe
un <- unique(frame$method)
sums <- c()
for(u in un) {
  sums <- c(sums, sum(frame[which(frame$method==u),"mean"]))
}
#lvls <- rev(levels(reorder(un,sums,order=T)))
lvls <- levels(reorder(un,sums,order=T))
frame$method <- factor(frame$method, levels=lvls, ordered=T)
ggplot(frame, aes(x=method, y=mean, fill=Maze)) +
  geom_bar(stat="identity") + coord_flip() + xlab("Treatment") + ylab("Accumulated success ratio")

# INDIVIDUAL MAZE BAR PLOTS #################
for(maze in unique(allframe$Maze)) {
  d <- subset(allframe, Maze==maze)
  un <- unique(d$method)
  sums <- c()
  for(u in un) {
    sums <- c(sums, sum(d[which(d$method==u),"mean"]))
  }
  lvls <- levels(reorder(un,sums,order=T))
  d$method <- factor(d$method, levels=lvls, ordered=T)
  
  # plot
  g <- ggplot(d, aes(x=method, y=mean, fill=Maze)) +
    geom_bar(stat="identity") + coord_flip() + ggtitle(maze) +
    geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.25)
  print(g)
}

################################################# NEAT VS GA

allframe <- data.frame()
for(maze in c("hard","zigzag","subset","multi","star","open")) {
  ga <- file.path("~/exps/maze3/ea/mutation/",maze,"/ga_el10_m0.05")
  neat <- file.path("~/exps/maze3/nspar/",maze,"/ns_k15_arandom")
  
  data <- metaLoadData(ga,neat,names=c("GA","NEAT"),params=list(jobs=30, load.behavs=F))
  #r <- fitnessLevelReached(data,1)
  #r <- fitnessLevels(data,level=1,use.max=1000)
  data <- metaLoadData(ga,neat,names=c("GA","NEAT"),params=list(jobs=30, load.behavs=T, behavs.sample=0.2, vars.group=c("x","y")))
  r <- uniformity(exploration.count(data,levels=10), mode="jsd")
  
  print(r$ttest)
  frame <- r$summary
  frame$method <- rownames(frame)
  allframe <- rbind(allframe, cbind(frame, Maze=maze))
}

save(allframe, file="~/Dropbox/Papers/GECCO15/data/topology_uniformity.rdata")

ggplot(allframe, aes(x=Maze, y=mean, fill=method)) +
  geom_bar(stat="identity",position="dodge") + xlab("Maze") + ylab("Uniformity") +
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.25, position=position_dodge(.9))



################################################# mutation rate

allframe <- data.frame()
for(maze in c("hard","zigzag","subset","multi","star","open")) {
  setwd("~/exps/maze3/ea/mutation")
  setwd(maze)
  all <- sub("./", "", list.dirs())[-1]
  #data <- do.call(metaLoadData,c(all,list(params=list(jobs=30, load.behavs=F))))
  #r <- fitnessLevelReached(data,1)
  #r <- fitnessLevels(data,level=1,use.max=1000)
  data <- do.call(metaLoadData,c(all,list(params=list(jobs=30, load.behavs=T, behavs.sample=0.2, vars.group=c("x","y")))))
  r <- uniformity(exploration.count(data,levels=10), mode="jsd")
  
  print(r$ttest)
  frame <- r$summary
  frame$method <- rownames(frame)
  allframe <- rbind(allframe, cbind(frame, Maze=maze))
}

allframe[grep("ga_ns",allframe$method),"NE"] <- "GA-NS"
allframe[grep("ga_fit",allframe$method),"NE"] <- "GA-Fit"
allframe[grep("neat_ns",allframe$method),"NE"] <- "NEAT-NS"
allframe[grep("neat_fit",allframe$method),"NE"] <- "NEAT-Fit"
allframe[grep("m0.025",allframe$method),"M"] <- "0.025"
allframe[grep("m0.05",allframe$method),"M"] <- "0.05"
allframe[grep("m0.1",allframe$method),"M"] <- "0.1"
allframe[grep("m0.25",allframe$method),"M"] <- "0.25"
allframe[grep("m0.4",allframe$method),"M"] <- "0.4"
allframe[grep("m0.6",allframe$method),"M"] <- "0.6"
allframe[grep("m0.8",allframe$method),"M"] <- "0.8"

save(allframe, file="~/Dropbox/Papers/GECCO15/data/mutation_uniformity.rdata")

aggregate2 <- function(frame) {
  newframe <- data.frame()
  for(ne in unique(frame$NE)) {
    for(m in unique(frame$M)) {
        sub <- subset(frame, ne==NE & m==M)
        val <- mean(sub$mean)
        row <- sub[1,]
        row["mean"] <- val
        newframe <- rbind(newframe, row)
      }
  }
  return(newframe)
}
agg <- aggregate2(allframe)
pd <- position_dodge()
ggplot(agg, aes(x=M, y=mean, colour=NE, shape=NE)) + 
  geom_line(position=pd,aes(group=NE)) +
  geom_point(position=pd,size=4) + ylab("Mean exploration uniformity") + xlab("Mutation rate") 

############################### combination

allframe <- data.frame()
for(maze in c("hard","zigzag","subset","multi","star","open")) {
  setwd("~/exps/maze3/comb")
  setwd(maze)
  all <- sub("./", "", list.dirs())[-1]
  data <- do.call(metaLoadData,c(all,list(params=list(jobs=30, load.behavs=F))))
  #r <- fitnessLevelReached(data,1)
  #r <- fitnessLevels(data,level=1,use.max=1000)
  data <- do.call(metaLoadData,c(all,list(params=list(jobs=30, load.behavs=T, behavs.sample=0.2, vars.group=c("x","y")))))
  #r <- uniformity(exploration.count(data,levels=10), mode="jsd")
  r <- individuals.count(data, min.fit=0.95)
  print(r$ttest)
  frame <- r$summary
  frame$method <- rownames(frame)
  allframe <- rbind(allframe, cbind(frame, Maze=maze))
}
allframe$method <- factor(allframe$method, levels=c("fit","ns","ls50","ls75","pmcns","nsga2","nsga2_rank"))

save(allframe, file="~/Dropbox/Papers/GECCO15/data/comb_proportion.rdata")


ggplot(allframe, aes(x=Maze, y=mean, fill=method)) +
  geom_bar(stat="identity",position="dodge") + xlab("Maze") + ylab("High-fitness proportion") +
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.25, position=position_dodge(.9))

ggplot(allframe, aes(x=method, y=mean, fill=Maze)) +
  geom_bar(stat="identity") + xlab("Method") + ylab("Accumulated high-fitness proportion") +
  theme(axis.text.x = element_text(angle = 45, hjust = 1))



############################################## elites

allframe <- data.frame()
for(maze in c("hard","zigzag","subset","multi","star","open")) {
  setwd("~/exps/maze3/ea/elite")
  setwd(maze)
  all <- sub("./", "", list.dirs())[-1]
  data <- do.call(metaLoadData,c(all,list(params=list(jobs=30, load.behavs=F))))
  #r <- fitnessLevelReached(data,1)
  #r <- fitnessLevels(data,level=1,use.max=1000)
  data <- do.call(metaLoadData,c(all,list(params=list(jobs=30, load.behavs=T, behavs.sample=0.2, vars.group=c("x","y")))))
  r <- uniformity(exploration.count(data,levels=10), mode="jsd")
  
  print(r$ttest)
  frame <- r$summary
  frame$method <- rownames(frame)
  allframe <- rbind(allframe, cbind(frame, Maze=maze))
}

allframe[grep("ga",allframe$method),"NE"] <- "GA"
allframe[grep("neat",allframe$method),"NE"] <- "NEAT"
allframe[grep("e0",allframe$method),"E"] <- "0"
allframe[grep("e5",allframe$method),"E"] <- "1"
allframe[grep("e20",allframe$method),"E"] <- "2"
allframe[grep("e50",allframe$method),"E"] <- "3"
allframe[grep("e75",allframe$method),"E"] <- "4"
allframe[grep("e100",allframe$method),"E"] <- "5"
allframe[grep("s1",allframe$method),"E"] <- "0"
allframe[grep("s0.75",allframe$method),"E"] <- "1"
allframe[grep("s0.5",allframe$method),"E"] <- "2"
allframe[grep("s0.2",allframe$method),"E"] <- "3"
allframe[grep("s0.1",allframe$method),"E"] <- "4"
allframe[grep("s0.05",allframe$method),"E"] <- "5"

aggregate3 <- function(frame) {
  newframe <- data.frame()
  for(ne in unique(frame$NE)) {
    for(e in unique(frame$E)) {
      sub <- subset(frame, ne==NE & e==E)
      val <- sum(sub$mean)
      sd <- sqrt(sum(sub$sd ^ 2) / length(sub$sd))
      se <- sd / sqrt(30) 
      row <- sub[1,]
      row["mean"] <- val
      row["se"] <- se
      newframe <- rbind(newframe, row)
    }
  }
  return(newframe)
}
agg <- aggregate3(allframe)
ggplot(agg, aes(x=E, y=mean, colour=NE, shape=NE)) + 
  geom_line(position=position_dodge(.1),aes(group=NE)) +
  geom_point(position=pd,size=3) + ylab("Accumulated uniformity") + xlab("Elite size / survival threshold") +
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.25, position=position_dodge(.1)) +
  scale_x_discrete(labels=c("0\n1.0","5\n0.75","20\n0.5","50\n0.2","75\n0.1","100\n0.05"))


############################## growth rate

allframe <- data.frame()
for(maze in c("hard","zigzag","subset","multi","star","open")) {
  setwd("~/exps/maze3/growth")
  setwd(maze)
  all <- sub("./", "", list.dirs())[-1]
  data <- do.call(metaLoadData,c(all,list(params=list(jobs=30, load.behavs=F))))
  #r <- fitnessLevelReached(data,1)
  #r <- fitnessLevels(data,level=1,use.max=1000)
  data <- do.call(metaLoadData,c(all,list(params=list(jobs=30, load.behavs=T, behavs.sample=0.2, vars.group=c("x","y")))))
  r <- uniformity(exploration.count(data,levels=10), mode="jsd")
  
  print(r$ttest)
  frame <- r$summary
  frame$method <- rownames(frame)
  allframe <- rbind(allframe, cbind(frame, Maze=maze))
}

allframe[grep("anovel",allframe$method),"Archive"] <- "Novel"
allframe[grep("arandom",allframe$method),"Archive"] <- "Random"
allframe[grep("g0.01",allframe$method),"Growth"] <- "0.01"
allframe[grep("g0.03",allframe$method),"Growth"] <- "0.03"
allframe[grep("g0.07",allframe$method),"Growth"] <- "0.07"
write.csv(allframe, file="~/Dropbox/Papers/GECCO15/growth_uniformity.csv")

aggregate4 <- function(frame) {
  newframe <- data.frame()
  for(a in unique(frame$Archive)) {
    for(g in unique(frame$Growth)) {
      sub <- subset(frame, a==Archive & g==Growth)
      val <- sum(sub$mean)
      sd <- sqrt(sum(sub$sd ^ 2) / length(sub$sd))
      se <- sd / sqrt(30) 
      row <- sub[1,]
      row["mean"] <- val
      row["se"] <- se
      newframe <- rbind(newframe, row)
    }
  }
  return(newframe)
}
agg <- aggregate4(allframe)
ggplot(agg, aes(x=Growth, y=mean, colour=Archive, shape=Archive)) + 
  geom_line(position=position_dodge(.1),aes(group=Archive)) +
  geom_point(position=position_dodge(.1),size=3) + ylab("Accumulated uniformity") + xlab("Growth rate") +
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.25, position=position_dodge(.1))



# mutation correlations
load("mutation_success.rdata")
load("mutation_generations.rdata")
load("mutation_uniformity.rdata")
agg <- aggregate2(allframe)
cor(subset(agg, NE=="GA-Fit")$mean, subset(agg, NE=="GA-NS")$mean, method="pearson")
cor(subset(agg, NE=="NEAT-Fit")$mean, subset(agg, NE=="NEAT-NS")$mean, method="pearson")
cor(subset(agg, NE=="GA-NS")$mean, subset(agg, NE=="NEAT-NS")$mean, method="pearson")
cor(subset(agg, NE=="GA-Fit")$mean, subset(agg, NE=="NEAT-Fit")$mean, method="pearson")



# AQUATIC DRONES STUFF #############################################

setwd("~/labmag/exps/jbot/")
pred <- metaLoadData("predprey_rb/nsga","predprey_rb/fit","predprey_rb/hom_nsga","predprey_rb/hom_fit", names=c("NSGA","Fit","HomNSGA","HomFit"), params=list(jobs=10, subpops=NULL, merge.subpops=T, fitness.file="refitness.stat", fitlim=c(0,2), behavs.file="behaviours.stat", load.behavs=T, behavs.sample=0.2, vars.group=c("Captured","PreyDist","Time","Dispersion")))
foraging <- metaLoadData("foraging2/nsga","foraging2/fit","foraging2/hom_nsga","foraging2/hom_fit", names=c("NSGA","Fit","HomNSGA","HomFit"), params=list(jobs=10, subpops=NULL, merge.subpops=T, fitness.file="refitness.stat", fitlim=c(0,6), behavs.file="behaviours.stat", load.behavs=T, behavs.sample=0.2, vars.group=c("Items","Dispersion","Proximity")))
herding <- metaLoadData("herding_rb/nsga","herding_rb/fit","herding_rb/hom_nsga","herding_rb/hom_fit", names=c("NSGA","Fit","HomNSGA","HomFit"), params=list(jobs=10, subpops=NULL, merge.subpops=T, fitness.file="refitness.stat", fitlim=c(0,2), behavs.file="behaviours.stat", load.behavs=T, behavs.sample=0.2, vars.group=c("Time","SheepDist","ShephDispersion","ShephDist")))

fullStatistics(pred, fit.comp=T, show.only=F, som.group=F, som.alljobs=T, expset.name="pred",fit.comp.par=list(snapshots=c(250),jitter=F,ylim=F))
fullStatistics(foraging, fit.comp=T, show.only=F, som.group=F, som.alljobs=T, expset.name="foraging",fit.comp.par=list(snapshots=c(250),jitter=F,ylim=F))
fullStatistics(herding, fit.comp=T, show.only=F, som.group=F, som.alljobs=T, expset.name="herding",fit.comp.par=list(snapshots=c(250),jitter=F,ylim=F))

pred <- metaLoadData("predprey_rb/fit","predprey_rb/hom_fit", names=c("CCEA","Homo"), params=list(jobs=10, subpops=NULL, merge.subpops=T, fitness.file="refitness.stat", fitlim=c(0,2), behavs.file="behaviours.stat", load.behavs=F, behavs.sample=0.2, vars.group=c("Captured","PreyDist","Time","Dispersion")))
foraging <- metaLoadData("foraging2/fit","foraging2/hom_fit", names=c("CCEA","Homo"), params=list(jobs=10, subpops=NULL, merge.subpops=T, fitness.file="refitness.stat", fitlim=c(0,6), behavs.file="behaviours.stat", load.behavs=F, behavs.sample=0.2, vars.group=c("Items","Dispersion","Proximity")))
herding <- metaLoadData("herding_rb/fit","herding_rb/hom_fit", names=c("CCEA","Homo"), params=list(jobs=10, subpops=NULL, merge.subpops=T, fitness.file="refitness.stat", fitlim=c(0,6), behavs.file="behaviours.stat", load.behavs=F, behavs.sample=0.2, vars.group=c("Items","Dispersion","Proximity")))

fitnessJobs(pred)
fitnessJobs(foraging)
fitnessJobs(herding)


pred_het <- metaLoadData("predprey/fit","predprey/fit_nonoise","predprey/fit_actnoise","predprey/fit_sensnoise", names=c("all.noise","no.noise","act.noise","sens.noise"), params=list(jobs=10, subpops=NULL, merge.subpops=T, fitness.file="refitness.stat", fitlim=c(0,1.5), behavs.file="behaviours.stat", load.behavs=F, behavs.sample=0.2, vars.group=c("Captured","PreyDist","Time","Dispersion")))
fullStatistics(pred_het, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="pred_het",fit.comp.par=list(snapshots=c(250),jitter=F,ylim=T))

pred_hom <- metaLoadData("predprey/hom_fit","predprey/hom_fit_nonoise","predprey/hom_fit_actnoise","predprey/hom_fit_sensnoise", names=c("all.noise","no.noise","act.noise","sens.noise"), params=list(jobs=10, subpops=NULL, merge.subpops=T, fitness.file="refitness.stat", fitlim=c(0,1.5), behavs.file="behaviours.stat", load.behavs=F, behavs.sample=0.2, vars.group=c("Captured","PreyDist","Time","Dispersion")))
fullStatistics(pred_hom, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="pred_hom",fit.comp.par=list(snapshots=c(250),jitter=F,ylim=T))

forg_het <- metaLoadData("foraging/fit","foraging/fit_nonoise","foraging/fit_actnoise","foraging/fit_sensnoise", names=c("all.noise","no.noise","act.noise","sens.noise"), params=list(jobs=10, subpops=NULL, merge.subpops=T, fitness.file="refitness.stat", fitlim=c(0,5), behavs.file="behaviours.stat", load.behavs=F, behavs.sample=0.2, vars.group=c("Captured","PreyDist","Time","Dispersion")))
fullStatistics(forg_het, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="forg_het",fit.comp.par=list(snapshots=c(250),jitter=F,ylim=T))

forg_hom <- metaLoadData("foraging/hom_fit","foraging/hom_fit_nonoise","foraging/hom_fit_actnoise","foraging/hom_fit_sensnoise", names=c("all.noise","no.noise","act.noise","sens.noise"), params=list(jobs=10, subpops=NULL, merge.subpops=T, fitness.file="refitness.stat", fitlim=c(0,5), behavs.file="behaviours.stat", load.behavs=F, behavs.sample=0.2, vars.group=c("Captured","PreyDist","Time","Dispersion")))
fullStatistics(forg_het, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="forg_hom",fit.comp.par=list(snapshots=c(250),jitter=F,ylim=T))

pred <- metaLoadData("predprey/nsga","predprey/fit","predprey/hom_nsga","predprey/hom_fit", names=c("NSGA","Fit","HomNSGA","HomFit"), params=list(jobs=10, subpops=NULL, merge.subpops=T, fitness.file="refitness.stat", fitlim=c(0,1.5), behavs.file="behaviours.stat", load.behavs=T, behavs.sample=0.5, vars.group=c("Captured","PreyDist","Time","Dispersion")))
som <- fullStatistics(pred, fit.comp=T, show.only=F, som.group=T, som.alljobs=T, expset.name="pred",fit.comp.par=list(snapshots=c(250),jitter=F,ylim=T), som.group.par=list(distance.filter=0.4))


pred <- metaLoadData("predprey/nsga","predprey/fit","predprey/hom_nsga","predprey/hom_fit", names=c("NSGA","Fit","HomNSGA","HomFit"), params=list(jobs=10, subpops=NULL, merge.subpops=T, fitness.file="refitness.stat", fitlim=c(0,1.5), behavs.file="behaviours.stat", load.behavs=T, behavs.sample=0.5, vars.group=c("Captured","PreyDist","Time","Dispersion")))
som <- fullStatistics(pred, fit.comp=T, show.only=F, som.group=T, som.alljobs=T, expset.name="pred",fit.comp.par=list(snapshots=c(250),jitter=F,ylim=T), som.group.par=list(distance.filter=0.4))

repred <- metaLoadData("predprey/nsga","predprey/fit", names=c("NSGA","Fit"), params=list(jobs=10, subpops=NULL, merge.subpops=F, fitness.file="refitness.stat", fitlim=c(0,1.5), behavs.file="rebehaviours.stat", load.behavs=T, behavs.sample=1, vars.group=c("Captured","PreyDist","Time","Dispersion")))
som <- fullStatistics(repred, fit.comp=T, show.only=F, som.group=T, som.alljobs=T, expset.name="repred",fit.comp.par=list(snapshots=c(250),jitter=F,ylim=T), som.group.par=list(distance.filter=0.5,grid.size=6))
save(som, file="repred_som.rdata")
identifyBests(som$group, repred, "repred_bests.csv", n=10, fitness.threshold=0.8)


setwd("~/exps/ecal2/")
down_tog <- metaLoadData("down_tog/fit","down_tog/nst","down_tog/nsmix","down_tog/moea","down_tog/staged","down_tog/inc", names=c("Fit","NS-T","NS-M","MOEA","Staged","Inc"), params=list(jobs=10, gens=0:499, subpops=NULL, fitlim=c(0,6),merge.subpops=T, fitness.file="refitness.stat",load.behavs=F))
fullStatistics(down_tog, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="pred_het",fit.comp.par=list(snapshots=c(499),jitter=F,ylim=T))

down_sep <- metaLoadData("down_sep/fit","down_sep/nst","down_sep/nsmix","down_sep/moea","down_sep/staged","down_sep/inc", names=c("Fit","NS-T","NS-M","MOEA","Staged","Inc"), params=list(jobs=10, gens=0:499, subpops=NULL, fitlim=c(0,6),merge.subpops=T, fitness.file="refitness.stat",load.behavs=F))
fullStatistics(down_sep, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="pred_het",fit.comp.par=list(snapshots=c(499),jitter=F,ylim=T))

stable_sep <- metaLoadData("stable_sep/fit","stable_sep/nst","stable_sep/nsmix","stable_sep/moea","stable_sep/staged","stable_sep/inc", names=c("Fit","NS-T","NS-M","MOEA","Staged","Inc"), params=list(jobs=10, gens=0:499, subpops=NULL, fitlim=c(0,6),merge.subpops=T, fitness.file="refitness.stat",load.behavs=F))
fullStatistics(stable_sep, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="pred_het",fit.comp.par=list(snapshots=c(499),jitter=F,ylim=T))

up_sep <- metaLoadData("stable_sep/fit","stable_sep/inc", names=c("Fit","Inc"), params=list(jobs=10, gens=0:499, subpops=NULL, fitlim=c(0,6),merge.subpops=T, fitness.file="refitness.stat",load.behavs=F))
fullStatistics(up_sep, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="pred_het",fit.comp.par=list(snapshots=c(499),jitter=F,ylim=T))

stable_tog <- metaLoadData("stable_tog/fit", names=c("Fit"), params=list(jobs=10, gens=0:499, subpops=NULL, fitlim=c(0,6),merge.subpops=T, fitness.file="refitness.stat",load.behavs=F))
fullStatistics(stable_tog, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="pred_het",fit.comp.par=list(snapshots=c(499),jitter=F,ylim=T))

up_sep <- metaLoadData("up_sep/fit", names=c("Fit"), params=list(jobs=10, gens=0:499, subpops=NULL, fitlim=c(0,6),merge.subpops=T, fitness.file="refitness.stat",load.behavs=F))
fullStatistics(up_sep, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="pred_het",fit.comp.par=list(snapshots=c(499),jitter=F,ylim=T))

fit <- metaLoadData("down_tog/fit","down_sep/fit","stable_sep/fit","stable_tog/fit","up_sep/fit", names=c("Down-Tog","Down-Sep","Stable-Sep","Stable-Tog","Up-Sep"), params=list(jobs=10, gens=0:499, subpops=NULL, fitlim=c(0,6),merge.subpops=T, fitness.file="refitness.stat",load.behavs=F))
fullStatistics(fit, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="pred_het",fit.comp.par=list(snapshots=c(499),jitter=F,ylim=T))
