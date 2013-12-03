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

setwd("~/exps/g0eneric2/keepaway")
data <- metaLoadData("fit/","nsga","nsga_b/","cl50_brown_norm_biased/","nsga_semigen", params=list(jobs=10,load.behavs=F))
fullStatistics(data, expset.name="ksh", fit.comp=T, fit.comp.par=list(snapshots=c(100,300,499)), show.only=T)
