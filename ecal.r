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
data <- metaLoadData("~/exps/fl4/stable/fit","~/exps/fl4/stable_sep/fit","~/exps/fl4/down/fit","~/exps/fl4/down_sep/fit","~/exps/fl4/baseline", names=c("ST","SO","VT","VO","BL"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=F, behavs.sample=1, vars.ind=c("I/H","Proximity","Movement","Distance"),vars.group=c("Items","Within","Dispersion","AvgProximity")))
fullStatistics(data, fit.tests=T, show.only=T, fit.ind=F, fit.comp=T, som.group=F, som.alljobs=F, expset.name="Base", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(750),jitter=F))

# Time within analysis

setwd("~/exps/fl4")
analyse("stable/fit", filename="rebehaviours.stat", exp.names=c("ST"), vars.pre=c("gen","pop","index","fitness","bg1","bg2","bg3","bg4"), vars.sub=c("sep","bi1","bi2","bi3","bi4"), vars.post=c("height","within"), analyse="within", splits=5, boxplots=F, plot=F, all=F, print=T)
analyse("stable_sep/fit", filename="rebehaviours.stat", jobs=c(6,7,8,10,11,13,19,21,22,25,28,29), exp.names=c("SO-Good"), vars.pre=c("gen","pop","index","fitness","bg1","bg2","bg3","bg4"), vars.sub=c("sep","bi1","bi2","bi3","bi4"), vars.post=c("height","within"), analyse="within", splits=5, boxplots=F, plot=F, all=F, print=T)
analyse("stable_sep/fit", filename="rebehaviours.stat", jobs=c(5,9,14,16,17,18,20,23,24,26), exp.names=c("SO-Bad"), vars.pre=c("gen","pop","index","fitness","bg1","bg2","bg3","bg4"), vars.sub=c("sep","bi1","bi2","bi3","bi4"), vars.post=c("height","within"), analyse="within", splits=5, boxplots=F, plot=F, all=F, print=T)
analyse("down/fit", filename="rebehaviours.stat", jobs=c(1,5,12,14,18,20,21,22,24,25,26,27,28), exp.names=c("VT-Good"), vars.pre=c("gen","pop","index","fitness","bg1","bg2","bg3","bg4"), vars.sub=c("sep","bi1","bi2","bi3","bi4"), vars.post=c("height","within"), analyse="within", splits=5, boxplots=F, plot=F, all=F, print=T)
analyse("down/fit", filename="rebehaviours.stat", jobs=c(2,3,6,7,8,9,10,15,17,19,29), exp.names=c("VT-Bad"), vars.pre=c("gen","pop","index","fitness","bg1","bg2","bg3","bg4"), vars.sub=c("sep","bi1","bi2","bi3","bi4"), vars.post=c("height","within"), analyse="within", splits=5, boxplots=F, plot=F, all=F, print=T)
analyse("down_sep/fit", filename="rebehaviours.stat", jobs=c(14,17,19,22,24), exp.names=c("VO-Good"), vars.pre=c("gen","pop","index","fitness","bg1","bg2","bg3","bg4"), vars.sub=c("sep","bi1","bi2","bi3","bi4"), vars.post=c("height","within"), analyse="within", splits=5, boxplots=F, plot=F, all=F, print=T)
analyse("down_sep/fit", filename="rebehaviours.stat", jobs=c(0,1,2,3,4,5,6,7,8,9,10,11,12,15,16,18,20,21,23,25,26,27,28,29), exp.names=c("VO-Bad"), vars.pre=c("gen","pop","index","fitness","bg1","bg2","bg3","bg4"), vars.sub=c("sep","bi1","bi2","bi3","bi4"), vars.post=c("height","within"), analyse="within", splits=5, boxplots=F, plot=F, all=F, print=T)


# Behaviour space analysis

vars.group <- c("Items","Within","Dispersion","AvgProximity")
setwd("~/exps/fl4")
data <- metaLoadData("stable/fit","stable_sep/fit","down/fit","down_sep/fit", names=c("ST","SO","VT","VO"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=T, behavs.sample=1, vars.group=vars.group, vars.file=c(vars.group,rep(NA,6))))

st <- data$ST
vtGood <- filterJobs(data$VT, jobs=paste0("job.",c(1,5,12,14,18,20,21,22,24,25,26,27,28)), name="VT-Good")
vtBad <- filterJobs(data$VT, jobs=paste0("job.",c(2,3,6,7,8,9,10,15,17,19,29)), name="VT-Bad")
soGood <- filterJobs(data$SO, jobs=paste0("job.",c(6,7,8,10,11,13,19,21,22,25,28,29)),name="SO-Good")
soBad <- filterJobs(data$SO, jobs=paste0("job.",c(5,9,14,16,17,18,20,23,24,26)),name="SO-Bad")
voGood <- filterJobs(data$VO, jobs=paste0("job.",c(14,17,19,22,24)),name="VO-Good")
voBad <- filterJobs(data$VO, jobs=paste0("job.",c(0,1,2,3,4,5,6,7,8,9,10,11,12,15,16,18,20,21,23,25,26,27,28,29)),name="VO-Bad")

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

vars.group <- c("Items","Within","Dispersion","AvgProximity")
setwd("~/exps/fl4/stable")
st <- metaLoadData("fit","ns","inc", names=c("Base","NS","Env"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=T, behavs.sample=0.25, vars.group=vars.group, vars.file=c(vars.group,rep(NA,6))))
setwd("~/exps/fl4/down")
vt <- metaLoadData("fit","ns","inc","staged","halted", names=c("Base","NS","Env","Stag","NC-Stag"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=T, behavs.sample=0.25, vars.group=vars.group, vars.file=c(vars.group,rep(NA,6))))
setwd("~/exps/fl4/stable_sep")
so <- metaLoadData("fit","ns","inc","staged","halted", names=c("Base","NS","Env","Stag","NC-Stag"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=T, behavs.sample=0.25, vars.group=vars.group, vars.file=c(vars.group,rep(NA,6))))
setwd("~/exps/fl4/down_sep")
vo <- metaLoadData("fit","ns","inc","staged","halted", names=c("Base","NS","Env","Stag","NC-Stag"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", fitlim=c(0,6), load.behavs=T, behavs.sample=0.25, vars.group=vars.group, vars.file=c(vars.group,rep(NA,6))))

fullStatistics(st, fit.tests=T, show.only=T, fit.ind=F, fit.comp=T, som.group=F, som.alljobs=F, expset.name="ST", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(750),jitter=F,ylim=T))
fullStatistics(vt, fit.tests=T, show.only=T, fit.ind=F, fit.comp=T, som.group=F, som.alljobs=F, expset.name="VT", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(750),jitter=F,ylim=T))
fullStatistics(so, fit.tests=T, show.only=T, fit.ind=F, fit.comp=T, som.group=F, som.alljobs=F, expset.name="SO", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(750),jitter=F,ylim=T))
fullStatistics(vo, fit.tests=T, show.only=T, fit.ind=F, fit.comp=T, som.group=F, som.alljobs=F, expset.name="VO", som.group.par=list(distance.filter=0.25), fit.comp.par=list(snapshots=c(750),jitter=F,ylim=T))


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



# NS special

failed.vo <- c(0.889,0.884,0.882,0.82,0.885,0.908,0.836,0.915,0.845,0.883,0.89,0.83,0.887,0.91,0.77)
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
