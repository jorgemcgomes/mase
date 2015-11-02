DEF_HEIGHT=3
DEF_WIDTH=4
theme_set(theme_bw())


generationalStats <- function(data, fun="sd", args=list(), threshold=0.1) {
  df <- list()
  dfc <- list()
  for(sub in data$subpops) {
    df[[sub]] <- data.frame()
    dfc[[sub]] <- data.frame()
  }
  for(job in data$jobs) {
    print(job)
    for(sub in data$subpops) {
      print(sub)
      d <- data[[job]][[sub]]
      for(g in data$gens) {
        gensub <- subset(d, gen==g)
        v <- do.call(fun, c(list(as.numeric(gensub[["fitness"]])),args))
        if(is.na(v)) {
          v <- 0
        }
        df[[sub]][g+1,job] <- v
        if(v < threshold) {
          dfc[[sub]][g+1,job] <- 1
        } else {
          dfc[[sub]][g+1,job] <- 0
        }
      }
    }
  }
  for(sub in data$subpops) {
    df[[sub]] <- rowMeans(df[[sub]])
    dfc[[sub]] <- rowMeans(dfc[[sub]])
  }
  df <- as.data.frame(df)
  dfc <- as.data.frame(dfc)
  res <- cbind(gen=data$gens,df,dfc)
  colnames(res) <- c("gen",paste0("v.",data$subpops),paste0("c.",data$subpops))
  return(res)
}

rsd <- function(vec){
  s <- sd(vec, na.rm=T)
  m <- mean(vec,na.rm=T)
  if(is.na(m) | m < 0.0001) {
    return(0)
  } else {
    return(s/m)
  }
}


setwd("~/labmag/exps/ecaln/")

vars.group <- c("Items","Within","Dispersion","AvgProximity")
vars.ind <- c("I1","I2","I3","I4")
vars.extra <- c("Height","TimeWithin")
fit <- metaLoadData("down_tog/fit","down_sep/fit","stable_sep/fit","stable_tog/fit", names=c("DownTog","DownSep","StableSep","StableTog"), params=list(jobs=15, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.sample=0.5, vars.group=vars.group, vars.ind=vars.ind, vars.extra=vars.extra))

### SEPARATE GOOD AND BAD RUNS ################

fl <- fitnessLevelReached(fit, 4)
dt.over <- as.logical(fl$data["DownTog",])
dt.good <- filterJobs(fit$DownTog, jobs=fit$DownTog$jobs[dt.over])
dt.bad <- filterJobs(fit$DownTog, jobs=fit$DownTog$jobs[!dt.over])
ds.over <- as.logical(fl$data["DownSep",])
ds.good <- filterJobs(fit$DownSep, jobs=fit$DownSep$jobs[ds.over])
ds.bad <- filterJobs(fit$DownSep, jobs=fit$DownSep$jobs[!ds.over])
ss.over <- as.logical(fl$data["StableSep",])
ss.good <- filterJobs(fit$StableSep, jobs=fit$StableSep$jobs[ss.over])
ss.bad <- filterJobs(fit$StableSep, jobs=fit$StableSep$jobs[!ss.over])
st.over <- as.logical(fl$data["StableTog",])
st.good <- filterJobs(fit$StableTog, jobs=fit$StableTog$jobs[st.over])
st.bad <- filterJobs(fit$StableTog, jobs=fit$StableTog$jobs[!st.over])


### TIME WITHIN ##############################

listmean <- function(l) {
  return(mean(as.numeric(l)))
}

analyseVar <- function(datalist, var) {
  allres <- list()
  for(data in datalist) {
    res <- list()
    for(g in data$gens) {
      res[[g+1]] <- list()
    }
    print(length(res))
    for(job in data$jobs) {
      print(job)
      for(sub in data$subpops) {
        for(g in data$gens) {
          s <- subset(data[[job]][[sub]], gen==g)[[var]]
          res[[g+1]] <- c(res[[g+1]],mean(s))
        }
      }
    }
    allres[[data$expname]] <- lapply(res, listmean)
  }
  return(allres)
}

a <- analyseVar(list(ss.good,ss.bad),"Within.1")
b <- c()
d <- as.data.frame(a)

plotMultiline(d, ylim=NULL,ylabel="Time within")

### FITNESS DIVERSITY ########################

st.good.rsd <- generationalStats(st.good, fun="rsd", threshold=0.1)
dt.good.rsd <- generationalStats(dt.good, fun="rsd", threshold=0.1)
dt.bad.rsd <- generationalStats(dt.bad, fun="rsd", threshold=0.1)
ss.good.rsd <- generationalStats(ss.good, fun="rsd", threshold=0.1)
ss.bad.rsd <- generationalStats(ss.bad, fun="rsd", threshold=0.1)
ds.good.rsd <- generationalStats(ds.good, fun="rsd", threshold=0.1)
ds.bad.rsd <- generationalStats(ds.bad, fun="rsd", threshold=0.1)

summary(st.good.rsd)
summary(dt.good.rsd) 
summary(dt.bad.rsd) 
summary(ss.good.rsd)
summary(ss.bad.rsd)
summary(ds.good.rsd)
summary(ds.bad.rsd)


### BEHAVIOURAL DIVERSITY #####################

div.group.all <- diversity.group(fit)






####################################################################################################3





data <- metaLoadData("~/exps/fl3/fit_down","~/exps/fl3/ns_down","~/exps/fl3/nsga_down", names=c("Fit","NS","NSGA"), params=list(jobs=20, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=T, behavs.sample=1, vars.ind=c("I/H","Proximity","Movement","Distance"),vars.group=c("Items","Within","Dispersion","AvgProximity")))

fit.sd <- generationalStats(fit, fun="sd")
plotMultiline(fit.sd, ylim=NULL)
ns.sd <- generationalStats(ns, fun="sd")
plotMultiline(ns.sd, ylim=NULL)

fit.mean <- generationalStats(fit, fun="mean")
plotMultiline(fit.mean, ylim=NULL)
ns.mean <- generationalStats(ns, fun="mean")
plotMultiline(ns.mean, ylim=NULL)

fitGood <- filterJobs(fit, jobs=c("job.0","job.1"))
fitGood.sd <- generationalStats(fitGood, fun="sd")
plotMultiline(fitGood.sd, ylim=NULL)
fitBad <- filterJobs(fit, jobs=c("job.2","job.3","job.8","job.9"))
fitBad.sd <- generationalStats(fitBad, fun="sd")
plotMultiline(fitBad.sd, ylim=NULL)

fullStatistics(data, fit.tests=T, show.only=T, fit.comp=F, som.group=T, som.alljobs=T, expset.name="down", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(500),jitter=F))

data <- metaLoadData("~/exps/fl3/fit_down","~/exps/fl3/ns_down","~/exps/fl3/nsga_down", names=c("Fit","NS","NSGA"), params=list(jobs=20, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=F, behavs.sample=1, vars.ind=c("I/H","Proximity","Movement","Distance"),vars.group=c("Items","Within","Dispersion","AvgProximity")))

data <- metaLoadData("~/exps/fl3/fit_stable","~/exps/fl3/ns_stable","~/exps/fl3/nsga_stable", names=c("Fit","NS","NSGA"), params=list(jobs=20, subpops=2, fitlim=c(0,6), load.behavs=F, behavs.sample=1, vars.ind=c("I/H","Proximity","Movement","Distance"),vars.group=c("Items","Within","Dispersion","AvgProximity")))
data <- metaLoadData("~/exps/fl3/fit_stable_sep","~/exps/fl3/ns_stable_sep","~/exps/fl3/nsga_stable_sep", names=c("Fit","NS","NSGA"), params=list(jobs=20, subpops=2, fitlim=c(0,6), load.behavs=F, behavs.sample=1, vars.ind=c("I/H","Proximity","Movement","Distance"),vars.group=c("Items","Within","Dispersion","AvgProximity")))

fullStatistics(data, fit.tests=T, show.only=T, fit.comp=T, som.group=F, som.alljobs=F, expset.name="down", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(500),jitter=F))

data <- metaLoadData("~/exps/fl3/fit_stable","~/exps/fl3/ns_stable","~/exps/fl3/fit_stable_inc", names=c("Fit","NS","Inc"), params=list(jobs=20, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=T, behavs.sample=0.2, vars.ind=c("I.H","Proximity","Movement","Distance"),vars.group=c("Items","Within","Dispersion","AvgProximity")))
fullStatistics(data, fit.tests=F, show.only=T, fit.comp=F, som.group=T, som.alljobs=T, som.ind=F, som.sepind=F, expset.name="stable", som.group.par=list(distance.filter=0.4),som.ind.par=list(distance.filter=0.4), fit.comp.par=list(snapshots=c(500),jitter=F))

fit.sd <- generationalStats(data$Fit, fun="sd")
plotMultiline(fit.sd, ylim=NULL)
ns.sd <- generationalStats(data$NS, fun="sd")
plotMultiline(ns.sd, ylim=NULL)

data <- metaLoadData("~/exps/fl3/stable/fit","~/exps/fl3/stable/ns","~/exps/fl3/stable/inc", names=c("Fit","NS","Inc"), params=list(jobs=20, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=F, behavs.sample=1, vars.ind=c("I/H","Proximity","Movement","Distance"),vars.group=c("Items","Within","Dispersion","AvgProximity")))
fullStatistics(data, fit.tests=T, show.only=T, fit.comp=T, som.group=F, som.alljobs=F, expset.name="down", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(500),jitter=F))
data <- metaLoadData("~/exps/fl3/down/fit","~/exps/fl3/down/ns","~/exps/fl3/down/inc","~/exps/fl3/down/staged","~/exps/fl3/down/staged_halted", names=c("Fit","NS","Inc","Staged","Halted"), params=list(jobs=20, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=F, behavs.sample=1, vars.ind=c("I/H","Proximity","Movement","Distance"),vars.group=c("Items","Within","Dispersion","AvgProximity")))
fullStatistics(data, fit.tests=T, show.only=T, fit.comp=T, som.group=F, som.alljobs=F, expset.name="down", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(500),jitter=F))
data <- metaLoadData("~/exps/fl3/down_sep/fit","~/exps/fl3/down_sep/ns","~/exps/fl3/down_sep/inc","~/exps/fl3/down_sep/staged","~/exps/fl3/down_sep/staged_halted", names=c("Fit","NS","Inc","Staged","Halted"), params=list(jobs=20, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=F, behavs.sample=1, vars.ind=c("I/H","Proximity","Movement","Distance"),vars.group=c("Items","Within","Dispersion","AvgProximity")))
fullStatistics(data, fit.tests=T, show.only=T, fit.comp=T, som.group=F, som.alljobs=F, expset.name="down", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(500),jitter=F))
data <- metaLoadData("~/exps/fl3/stable_sep/fit","~/exps/fl3/stable_sep/ns","~/exps/fl3/stable_sep/inc","~/exps/fl3/stable_sep/staged","~/exps/fl3/stable_sep/staged_halted", names=c("Fit","NS","Inc","Staged","Halted"), params=list(jobs=20, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=F, behavs.sample=1, vars.ind=c("I/H","Proximity","Movement","Distance"),vars.group=c("Items","Within","Dispersion","AvgProximity")))
fullStatistics(data, fit.tests=T, show.only=T, fit.comp=T, som.group=F, som.alljobs=F, expset.name="down", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(500),jitter=F))

setwd("~/exps/fl3/stable_sep")
setwd("~/exps/fl3/down")
setwd("~/exps/fl3/down_sep")
data <- metaLoadData("inc","inc10_10","inc5","inc10", names=c("Inc-5-10","Inc-10-10","Inc-5-20","Inc-10-20"), params=list(jobs=20, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=F, behavs.sample=1, vars.ind=c("I/H","Proximity","Movement","Distance"),vars.group=c("Items","Within","Dispersion","AvgProximity")))
fullStatistics(data, fit.tests=T, show.only=T, fit.comp=T, som.group=F, som.alljobs=F, expset.name="down", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(500),jitter=F))

analyse("fit","ns","inc5", filename="neat.stat", exp.names=c("Fit","NS","Inc"), vars.pre=c("gen"), vars.sub=c("species","avg.neurons","avg.conn","avg.recurr","best.neurons","best.conn","best.recurr"), analyse=c("avg.conn.0","avg.conn.1"), splits=5, boxplots=F, print=F)
analyse("inc10","inc10_10","inc5", filename="incremental.stat", exp.names=c("Inc10_20","Inc10_10","Inc5_20"), vars.pre=c("gen"), vars.sub=c("ratio","threshold","stage"), analyse=c("stage"), splits=5, boxplots=F, print=T)


data <- metaLoadData("~/exps/fl4/stable/fit","~/exps/fl4/stable/ns","~/exps/fl4/stable/inc", names=c("Fit","NS","Inc"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=F, behavs.sample=1, vars.ind=c("I/H","Proximity","Movement","Distance"),vars.group=c("Items","Within","Dispersion","AvgProximity")))
fullStatistics(data, fit.tests=T, show.only=T, fit.comp=T, som.group=F, som.alljobs=F, expset.name="down", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(750),jitter=T))
data <- metaLoadData("~/exps/fl4/down/fit","~/exps/fl4/down/ns","~/exps/fl4/down/inc","~/exps/fl4/down/staged","~/exps/fl4/down/halted", names=c("Fit","NS","Inc","Staged","Halted"), params=list(jobs=20, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=F, behavs.sample=1, vars.ind=c("I/H","Proximity","Movement","Distance"),vars.group=c("Items","Within","Dispersion","AvgProximity")))
fullStatistics(data, fit.tests=T, fit.ind=T, show.only=T, fit.comp=T, som.group=F, som.alljobs=F, expset.name="down", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(750),jitter=T))
data <- metaLoadData("~/exps/fl4/stable_sep/fit","~/exps/fl4/stable_sep/ns","~/exps/fl4/stable_sep/inc","~/exps/fl4/stable_sep/staged","~/exps/fl4/stable_sep/halted", names=c("Fit","NS","Inc","Staged","Halted"), params=list(jobs=20, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=F, behavs.sample=1, vars.ind=c("I/H","Proximity","Movement","Distance"),vars.group=c("Items","Within","Dispersion","AvgProximity")))
fullStatistics(data, fit.tests=T, show.only=T, fit.comp=T, som.group=F, som.alljobs=F, expset.name="down", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(750),jitter=T))
data <- metaLoadData("~/exps/fl4/down_sep/fit","~/exps/fl4/down_sep/ns","~/exps/fl4/down_sep/inc","~/exps/fl4/down_sep/staged","~/exps/fl4/down_sep/halted", names=c("Fit","NS","Inc","Staged","Halted"), params=list(jobs=20, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=F, behavs.sample=1, vars.ind=c("I/H","Proximity","Movement","Distance"),vars.group=c("Items","Within","Dispersion","AvgProximity")))
fullStatistics(data, fit.tests=T, show.only=T, fit.comp=T, som.group=F, som.alljobs=F, expset.name="down", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(750),jitter=T))

setwd("~/exps/fl4/down")
analyse("fit","ns", filename="neat.stat", exp.names=c("Fit","NS"), vars.pre=c("gen"), vars.sub=c("species","avg.neurons","avg.conn","avg.recurr","best.neurons","best.conn","best.recurr"), analyse=c("best.conn.0","best.conn.1"), splits=5, boxplots=F, print=F)

#analyse("fit","ns", filename="behaviours.stat", exp.names=c("Fit","NS"), vars.pre=c("gen"), vars.sub=c("pop","index","fitness","bg1","bg2","bg3","bg4","bi1","bi2","bi3","bi4","height","within"), analyse=c("height.0","within.0"), splits=5, boxplots=F, print=F)


vars.ind <- c("I/H","Proximity","Movement","Distance")
vars.group <- c("Items","Within","Dispersion","AvgProximity")
data <- metaLoadData("~/exps/fl4/down/fit","~/exps/fl4/down/ns", names=c("Fit","NS"), params=list(jobs=10, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=T, behavs.sample=0.5, vars.ind=vars.ind,vars.group=vars.group, vars.file=c(vars.group,vars.ind,NA,NA)))
fullStatistics(data, fit.tests=T, fit.ind=T, show.only=T, fit.comp=T, som.group=F, som.alljobs=F,som.ind=F, expset.name="down", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(500),jitter=F))

fitGood <- filterJobs(data$Fit, jobs=c("job.1","job.5"))
fitGood.sd <- generationalStats(fitGood, fun="mean")
plotMultiline(fitGood.sd, ylim=NULL)
fitBad <- filterJobs(data$Fit, jobs=c("job.3","job.6","job.7","job.9"))
fitBad.sd <- generationalStats(fitBad, fun="mean")
plotMultiline(fitBad.sd, ylim=NULL)

nsGood <- filterJobs(data$NS, jobs=c("job.0","job.2","job.4"))
nsGood.sd <- generationalStats(nsGood, fun="sd")
plotMultiline(smoothFrame(nsGood.sd,10), ylim=NULL)
nsBad <- filterJobs(data$NS, jobs=c("job.1","job.5"))
nsBad.sd <- generationalStats(nsBad, fun="sd")
plotMultiline(smoothFrame(nsBad.sd,10), ylim=NULL)

reduce <- function(vec, window=20) {
  indexes <- seq(from=1, to=length(vec), by=window)
  res <- c()
  for(i in 2:length(indexes)) {
    a <- mean(vec[indexes[i-1]:indexes[i]])
    res <- c(res,a)
  }
  return(res)
}

analyse("inc", filename="incremental.stat", exp.names=c("Inc"), vars.pre=c("gen"), vars.sub=c("ratio","aboveThreshold","fitnessThreshold","stage"), analyse=c("stage"), splits=5, boxplots=F, print=T)


setwd("~/exps/fl4/down")
data <- metaLoadData("fit", names=c("Fit"), params=list(jobs=20, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=F, behavs.sample=1, vars.ind=c("I/H","Proximity","Movement","Distance"),vars.group=c("Items","Within","Dispersion","AvgProximity")))
fullStatistics(data, fit.tests=F, fit.ind=T, show.only=T, fit.comp=F, som.group=F, som.alljobs=F, expset.name="down", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(750),jitter=T))
# good
analyse("fit", filename="rebehaviours.stat", jobs=paste0("job.",c(1,5,12,14,18)), exp.names=c("Fit"), vars.pre=c("gen","pop","index","fitness","bg1","bg2","bg3","bg4"), vars.sub=c("sep","bi1","bi2","bi3","bi4"), vars.post=c("height","within"), analyse=c("fitness","within"), transform=list(fitness=c(0,0.1666)), splits=5, boxplots=F, print=F)
# bad
analyse("fit", filename="rebehaviours.stat", jobs=paste0("job.",c(2,3,6,7,9,15,17,19)), exp.names=c("Fit"), vars.pre=c("gen","pop","index","fitness","bg1","bg2","bg3","bg4"), vars.sub=c("sep","bi1","bi2","bi3","bi4"), vars.post=c("height","within"), analyse=c("fitness","within"), transform=list(fitness=c(0,0.1666)), splits=5, boxplots=F, print=F)








# BASE CCEA
data <- metaLoadData("~/exps/fl4/stable/fit","~/exps/fl4/stable_sep/fit","~/exps/fl4/down/fit","~/exps/fl4/down_sep/fit","~/exps/fl4/baseline", names=c("Fix-Near","Fix-Sep","Var-Near","Var-Sep","Baseline"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=F, behavs.sample=1, vars.ind=c("I/H","Proximity","Movement","Distance"),vars.group=c("Items","Within","Dispersion","AvgProximity")))
DEF_HEIGHT <- 2.75
DEF_WIDTH <- 4.25
fullStatistics(data, fit.tests=T, show.only=T, fit.ind=F, fit.comp=T, som.group=F, som.alljobs=F, expset.name="Base", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(750),jitter=F,ylim=T))

# Time within analysis

setwd("~/exps/fl4")
analyse("stable/fit","stable_sep/fit","stable_sep/fit","down/fit","down/fit","down_sep/fit","down_sep/fit", jobs=list(
  NULL,
  c(6,7,8,10,11,13,19,21,22,25,28,29),
  c(5,9,14,16,17,18,20,23,24,26), 
  c(1,5,12,14,18,20,21,22,24,25,26,27,28),
  c(2,3,6,7,8,9,10,15,17,19,29),
  c(14,17,19,22,24),
  c(0,1,2,3,4,5,6,7,8,9,10,11,12,15,16,18,20,21,23,25,26,27,28,29)),
  exp.names=c("Fix-Near","Fix-Sep-Good","Fix-Sep-Bad","Var-Near-Good","Var-Near-Bad","Var-Sep-Good","Var-Sep-Bad"), 
  filename="rebehaviours.stat", vars.pre=c("gen","pop","index","fitness","bg1","bg2","bg3","bg4"), vars.sub=c("sep","bi1","bi2","bi3","bi4"), vars.post=c("height","within"), analyse="within", splits=1, boxplots=F, plot=F, all=F, print=T, interval=T, t.tests=T)


# Behaviour space analysis

vars.group <- c("Items","Within","Dispersion","AvgProximity")
setwd("~/exps/fl4")
data <- metaLoadData("stable/fit","stable_sep/fit","down/fit","down_sep/fit", names=c("ST","SO","VT","VO"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=T, behavs.sample=0.1, vars.group=vars.group, vars.file=c(vars.group,rep(NA,6))))
#data <- metaLoadData("stable/fit","stable_sep/fit","down/fit","down_sep/fit", names=c("ST","SO","VT","VO"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=T, behavs.bests=T, behavs.sample=1, vars.group=vars.group, vars.file=c(vars.group,rep(NA,6))))

st <- data$ST
vtGood <- filterJobs(data$VT, jobs=paste0("job.",c(1,5,12,14,18,20,21,22,24,25,26,27,28)), name="VT-Good")
vtBad <- filterJobs(data$VT, jobs=paste0("job.",c(2,3,6,7,8,9,10,15,17,19,29)), name="VT-Bad")
soGood <- filterJobs(data$SO, jobs=paste0("job.",c(6,7,8,10,11,13,19,21,22,25,28,29)),name="SO-Good")
soBad <- filterJobs(data$SO, jobs=paste0("job.",c(5,9,14,16,17,18,20,23,24,26)),name="SO-Bad")
voGood <- filterJobs(data$VO, jobs=paste0("job.",c(14,17,19,22,24)),name="VO-Good")
voBad <- filterJobs(data$VO, jobs=paste0("job.",c(0,1,2,3,4,5,6,7,8,9,10,11,12,15,16,18,20,21,23,25,26,27,28,29)),name="VO-Bad")

d <- diversity.group(list(st,vtGood,vtBad,soGood,soBad,voGood,voBad))

st.count <- exploration.count(list(st), vars=vars.group)
vt.count <- exploration.count(list(vtGood,vtBad), vars=vars.group)
so.count <- exploration.count(list(soGood,soBad), vars=vars.group)
vo.count <- exploration.count(list(voGood,voBad), vars=vars.group)
uniformity(st.count,mode="Gini") ; uniformity(vt.count,mode="Gini") ; uniformity(so.count,mode="Gini") ; uniformity(vo.count,mode="Gini")

# Fitness gradient analysis

rsd <- function(vec){
  s <- sd(vec, na.rm=T)
  m <- mean(vec,na.rm=T)
  if(is.na(m) | m < 0.0001) {
    return(0)
  } else {
    return(s/m)
  }
}

st.rsd <- generationalStats(st, fun="rsd", threshold=0.1)
vtGood.rsd <- generationalStats(vtGood, fun="rsd", threshold=0.1)
vtBad.rsd <- generationalStats(vtBad, fun="rsd", threshold=0.1)
soGood.rsd <- generationalStats(soGood, fun="rsd", threshold=0.1)
soBad.rsd <- generationalStats(soBad, fun="rsd", threshold=0.1)
voGood.rsd <- generationalStats(voGood, fun="rsd", threshold=0.1)
voBad.rsd <- generationalStats(voBad, fun="rsd", threshold=0.1)

summary(st.rsd)
summary(vtGood.rsd) 
summary(vtBad.rsd) 
summary(soGood.rsd)
summary(soBad.rsd)
summary(voGood.rsd)
summary(voBad.rsd)
# sum(vtGood.rsd$c.sub.0) ; sum(vtGood.rsd$c.sub.1)


# Improving coevolution -- fitness






st <- metaLoadData("fit","ns","inc", names=c("Base","NS","Env"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=T, behavs.sample=0.25, vars.group=vars.group, vars.file=c(vars.group,rep(NA,6))))
vt <- metaLoadData("fit","ns","inc","staged","halted", names=c("Base","NS","Env","Stag","NC-Stag"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=T, behavs.sample=0.25, vars.group=vars.group, vars.file=c(vars.group,rep(NA,6))))
so <- metaLoadData("fit","ns","inc","staged","halted", names=c("Base","NS","Env","Stag","NC-Stag"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=T, behavs.sample=0.25, vars.group=vars.group, vars.file=c(vars.group,rep(NA,6))))
vo <- metaLoadData("fit","ns","inc","staged","halted", names=c("Base","NS","Env","Stag","NC-Stag"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=T, behavs.sample=0.25, vars.group=vars.group, vars.file=c(vars.group,rep(NA,6))))

fullStatistics(st, fit.tests=T, show.only=T, fit.ind=F, fit.comp=T, som.group=F, som.alljobs=F, expset.name="ST", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(750),jitter=F,ylim=T))
fullStatistics(vt, fit.tests=T, show.only=T, fit.ind=F, fit.comp=T, som.group=F, som.alljobs=F, expset.name="VT", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(750),jitter=F,ylim=T))
fullStatistics(so, fit.tests=T, show.only=T, fit.ind=F, fit.comp=T, som.group=F, som.alljobs=F, expset.name="SO", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(750),jitter=F,ylim=T))
fullStatistics(vo, fit.tests=T, show.only=T, fit.ind=F, fit.comp=T, som.group=F, som.alljobs=F, expset.name="VO", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(750),jitter=F,ylim=T))




# Improving coevolution -- fitness 2

vars.group <- c("Items","Within","Dispersion","AvgProximity")
setwd("~/exps/fl4/down")
vt <- metaLoadData("fit","ns","staged","halted", names=c("B","N","I","J"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=F, behavs.sample=0.25, vars.group=vars.group, vars.file=c(vars.group,rep(NA,6))))
setwd("~/exps/fl4/stable_sep")
so <- metaLoadData("fit","ns","staged","halted", names=c("B","N","I","J"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=F, behavs.sample=0.25, vars.group=vars.group, vars.file=c(vars.group,rep(NA,6))))
setwd("~/exps/fl4/down_sep")
vo <- metaLoadData("fit","ns","staged","halted", names=c("B","N","I","J"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=F, behavs.sample=0.25, vars.group=vars.group, vars.file=c(vars.group,rep(NA,6))))

# line plots
DEF_HEIGHT <- 2.5
DEF_WIDTH <- 3.5
fullStatistics(vt, fit.tests=T, show.only=T, fit.ind=F, fit.comp=T, som.group=F, som.alljobs=F, expset.name="VT", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(750),jitter=F,ylim=T))
fullStatistics(so, fit.tests=T, show.only=T, fit.ind=F, fit.comp=T, som.group=F, som.alljobs=F, expset.name="SO", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(750),jitter=F,ylim=T))
fullStatistics(vo, fit.tests=T, show.only=T, fit.ind=F, fit.comp=T, som.group=F, som.alljobs=F, expset.name="VO", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(750),jitter=F,ylim=T))

# boxplots
DEF_HEIGHT <- 2.5
DEF_WIDTH <- 2.3

# Improving coevolution -- behaviour exploration

st.count <- exploration.count(st, vars=vars.group)
vt.count <- exploration.count(vt, vars=vars.group)
so.count <- exploration.count(so, vars=vars.group)
vo.count <- exploration.count(vo, vars=vars.group)
uniformity(st.count,mode="Gini")
uniformity(vt.count,mode="Gini")
uniformity(so.count,mode="Gini")
uniformity(vo.count,mode="Gini")

uniformity(st.count,mode="jsd")
uniformity(vt.count,mode="jsd")
uniformity(so.count,mode="jsd")
uniformity(vo.count,mode="jsd")



# Behaviour analysis 2

setwd("~/exps/fl4")
data <- metaLoadData("stable/fit","stable_sep/fit","down/fit","down_sep/fit", names=c("ST","SO","VT","VO"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=T, behavs.sample=0.25, vars.group=vars.group, vars.file=c(vars.group,rep(NA,6))))
vtGood <- filterJobs(data$VT, jobs=paste0("job.",c(1,5,12,14,18,20,21,22,24,25,26,27,28)), name="VT-Good")
vtBad <- filterJobs(data$VT, jobs=paste0("job.",c(2,3,6,7,8,9,10,15,17,19,29)), name="VT-Bad")
soGood <- filterJobs(data$SO, jobs=paste0("job.",c(6,7,8,10,11,13,19,21,22,25,28,29)),name="SO-Good")
soBad <- filterJobs(data$SO, jobs=paste0("job.",c(5,9,14,16,17,18,20,23,24,26)),name="SO-Bad")
voGood <- filterJobs(data$VO, jobs=paste0("job.",c(14,17,19,22,24)),name="VO-Good")
voBad <- filterJobs(data$VO, jobs=paste0("job.",c(0,1,2,3,4,5,6,7,8,9,10,11,12,15,16,18,20,21,23,25,26,27,28,29)),name="VO-Bad")

setwd("~/exps/fl4/stable")
st <- metaLoadData("fit","ns", names=c("Base","NS"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=T, behavs.sample=0.25, vars.group=vars.group, vars.file=c(vars.group,rep(NA,6))))
setwd("~/exps/fl4/down")
vt <- metaLoadData("fit","ns","staged","halted", names=c("Base","NS","Inc","NInc"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=T, behavs.sample=0.25, vars.group=vars.group, vars.file=c(vars.group,rep(NA,6))))
setwd("~/exps/fl4/stable_sep")
so <- metaLoadData("fit","ns","staged","halted", names=c("Base","NS","Inc","NInc"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=T, behavs.sample=0.25, vars.group=vars.group, vars.file=c(vars.group,rep(NA,6))))
setwd("~/exps/fl4/down_sep")
vo <- metaLoadData("fit","ns","staged","halted", names=c("Base","NS","Inc","NInc"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=T, behavs.sample=0.25, vars.group=vars.group, vars.file=c(vars.group,rep(NA,6))))

st.count <- exploration.count(st, vars=vars.group)
vt.count <- exploration.count(c(vt,list(vtGood,vtBad)), vars=vars.group)
so.count <- exploration.count(c(so,list(soGood,soBad)), vars=vars.group)
vo.count <- exploration.count(c(vo,list(voGood,voBad)), vars=vars.group)
uniformity(st.count,mode="Gini")
uniformity(vt.count,mode="Gini")
uniformity(so.count,mode="Gini")
uniformity(vo.count,mode="Gini")

# behaviour analysis 3 -- Camera ready

data <- metaLoadData("stable/fit","stable/ns","stable_sep/fit","stable_sep/staged","stable_sep/halted","stable_sep/ns","down/fit","down/staged/","down/halted","down/ns","down_sep/fit","down_sep/staged","down_sep/halted","down_sep/ns", names=c("ST.Fit","ST.NS","SO.Fit","SO.Inc","SO.NInc","SO.NS","VT.Fit","VT.Inc","VT.NInc","VT.NS","VO.Fit","VO.Inc","VO.NInc","VO.NS"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=T, behavs.sample=0.1, vars.group=vars.group, vars.file=c(vars.group,rep(NA,6))))
div.group.all <- diversity.group(data)
save(div.group.all, file="~/Dropbox/Work/Papers/ECAL/div.group.all")


# NS special

data <- metaLoadData("down/ns","down_sep/ns", names=c("VT.NS","VO.NS"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=F, behavs.sample=0.1, vars.group=vars.group, vars.file=c(vars.group,rep(NA,6))))

d <- data[["VO.NS"]]
failed <- c()
i <- 1
for(job in d$jobs) {
  if(d[[job]]$fitness$best.sofar[750] <= 3) {
    failed <- c(failed, i)
  }
  i <- i + 1
}

failed.vt <- div.group.all$data["VT.NS",failed]
failed.vo <- div.group.all$data["VO.NS",failed]

failed.vo <- c(0.889,0.884,0.882,0.82,0.885,0.908,0.836,0.915,0.845,0.883,0.89,0.83,0.887,0.91)
failed.vt <- c(0.938,0.91)


fitGood$expname <- "FitGood"
fitBad$expname <- "FitBad"
fitGood.sd <- generationalStats(fitGood, fun="sd", threshold=0.2)
fitBad.sd <- generationalStats(fitBad, fun="sd", threshold=0.2)
plotMultiline(fitGood.sd, ylim=NULL)
plotMultiline(fitBad.sd, ylim=NULL)
sum(fitGood.sd$c.sub.0) ; sum(fitGood.sd$c.sub.1) ; sum(fitBad.sd$c.sub.0) ; sum(fitBad.sd$c.sub.1)

mean(fitGood.sd$sub.0, na.rm=T)
mean(fitGood.sd$sub.1, na.rm=T)
mean(fitBad.sd$sub.0, na.rm=T)
mean(fitBad.sd$sub.1, na.rm=T)

count <- exploration.count(list(fitGood,fitBad), vars=vars.group)
uniformity(count,mode="gini")

data <- metaLoadData("fit","ns","inc","staged","halted", names=c("Fit","NS","Inc","Staged","Halted"), params=list(jobs=20, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=T, behavs.sample=0.5, vars.group=vars.group, vars.file=c(vars.group,rep(NA,6))))
count <- exploration.count(data, vars=vars.group)
uniformity(count,mode="jsd")
