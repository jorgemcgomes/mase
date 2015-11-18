DEF_HEIGHT=3.5
DEF_WIDTH=5
theme_set(theme_bw())

setwd("~/exps/ecalf/")
setwd("/media/jorge/Orico/ecalf/")
vars.group <- c("Items","Within","Dispersion","AvgProximity") ; vars.ind <- c("I1","I2","I3","I4") ; vars.extra <- c("Height","TimeWithin")

fit <- metaLoadData("down_tog/fit","down_sep/fit","stable_sep/fit","stable_tog/fit","down_mid/fit", names=c("DownTog","DownSep","StableSep","StableTog","DownMid"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.sample=1, vars.group=rep(NA,4), vars.ind=rep(NA,4), vars.extra=rep(NA,2)))
save(fit,file="~/Dropbox/Work/Papers/NACO/plots/fit_only.rdata")
load("~/Dropbox/Work/Papers/NACO/plots/fit_only.rdata")

fit <- metaLoadData("down_tog/fit","down_sep/fit","down_mid/fit","stable_sep/fit","stable_tog/fit", names=c("DownTog","DownSep","DownMid","StableSep","StableTog"), params=list(jobs=30, subpops=2, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat", behavs.file="rebehaviours.stat",load.behavs=T,behavs.sample=1, vars.group=vars.group, vars.extra=vars.extra, vars.file=c(vars.group,rep(NA,10),vars.extra)))

### FITNESS PLOTS #############################

fit <- metaLoadData("down_tog/fit","down_sep/fit","down_mid/fit","stable_sep/fit","stable_tog/fit", names=c("DownTog","DownSep","DownMid","StableSep","StableTog"), params=list(jobs=30, subpops=2, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat", behavs.file="rebehaviours.stat",load.behavs=F,behavs.sample=1, vars.group=vars.group, vars.extra=vars.extra, vars.file=c(vars.group,rep(NA,10),vars.extra)))
fullStatistics(fit, fit.tests=T, show.only=T, fit.comp=T, som.group=F, som.alljobs=F, expset.name="Fit", fit.comp.par=list(snapshots=c(699),jitter=F,ylim=T))

### SUCCESS RATIO #############################

fl <- fitnessLevelReached(fit, 4)
dt.over <- as.logical(fl$data["DownTog",])
ds.over <- as.logical(fl$data["DownSep",])
ss.over <- as.logical(fl$data["StableSep",])
st.over <- as.logical(fl$data["StableTog",])
dm.over <- as.logical(fl$data["DownMid",])
all.good.bad <- list(
  dt.good = filterJobs(fit$DownTog, jobs=fit$DownTog$jobs[dt.over], name="dt.good"),
  dt.bad = filterJobs(fit$DownTog, jobs=fit$DownTog$jobs[!dt.over], name="dt.bad"),
  ds.good = filterJobs(fit$DownSep, jobs=fit$DownSep$jobs[ds.over], name="ds.good"),
  ds.bad = filterJobs(fit$DownSep, jobs=fit$DownSep$jobs[!ds.over], name="ds.bad"),
  ss.good = filterJobs(fit$StableSep, jobs=fit$StableSep$jobs[ss.over], name="ss.good"),
  ss.bad = filterJobs(fit$StableSep, jobs=fit$StableSep$jobs[!ss.over], name="ss.bad"),
  st.good = filterJobs(fit$StableTog, jobs=fit$StableTog$jobs[st.over], name="st.good"),
  st.bad = filterJobs(fit$StableTog, jobs=fit$StableTog$jobs[!st.over], name="st.bad"),
  dm.good = filterJobs(fit$DownMid, jobs=fit$DownMid$jobs[dm.over], name="dm.good"),
  dm.bad = filterJobs(fit$DownMid, jobs=fit$DownMid$jobs[!dm.over], name="dm.bad")
)

### TIME WITHIN ##############################

addTaskInfo <- function(data,expvar) {
  data[grepl( "st" , data[[expvar]]),"Task"] <- "Fix-Tog"
  data[grepl( "dt" , data[[expvar]]),"Task"] <- "Var-Tog"
  data[grepl( "ss" , data[[expvar]]),"Task"] <- "Fix-Sep"
  data[grepl( "ds" , data[[expvar]]),"Task"] <- "Var-Sep"
  data[grepl( "dm" , data[[expvar]]),"Task"] <- "Var-Mid"
  data[grepl( "good" , data[[expvar]]),"Type"] <- "Good"
  data[grepl( "bad" , data[[expvar]]),"Type"] <- "Bad"
  data[grepl( "Fit" , data[[expvar]]),"Method"] <- "Fit"
  data[grepl( "NS_T" , data[[expvar]]),"Method"] <- "NS_T"
  data[grepl( "MOEA" , data[[expvar]]),"Method"] <- "MOEA"
  data[grepl( "Staged" , data[[expvar]]),"Method"] <- "Staged"
  data[grepl( "Halted" , data[[expvar]]),"Method"] <- "Halted"
  data[grepl( "Inc" , data[[expvar]]),"Method"] <- "Inc"
  data$Task <- factor(data$Task, levels=c("Fix-Tog","Fix-Sep","Var-Tog","Var-Mid","Var-Sep"))
  data$Type <- factor(data$Type, levels=c("Good","Bad"))
  data$Method <- factor(data$Method, levels=c("Fit","MOEA","Staged","Halted","Inc","NS_T"))
  return(data)
}

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
    for(job in data$jobs) {
      print(job)
      frame <- rbind(data[[job]][["sub.0"]], data[[job]][["sub.1"]])
      for(g in data$gens) {
        s <- subset(frame, gen==g)[[var]]
        res[[g+1]] <- c(res[[g+1]],mean(s))
      }
    }
    allres[[data$expname]] <- lapply(res, listmean)
    allres[[data$expname]] <- as.vector(allres[[data$expname]], mode="numeric")
  }
  allres <- cbind(gen=datalist[[1]]$gens, as.data.frame(allres))
  return(allres)
}

time.within <- analyseVar(all.good.bad,"TimeWithin")
tw <- na.omit(melt(time.within, id="gen"))
tw <- addTaskInfo(tw,"variable")
save(tw, file="~/Dropbox/Work/Papers/NACO/plots/tw.rdata")

g <- ggplot(data=tw, aes(x=gen, y=value, colour=Task,lty=Type)) + ylab("Time within") + xlab("Generation") + stat_smooth(se=F,fill="grey75",n=500) #+ geom_line()
ggsave("~/Dropbox/Work/Papers/NACO/plots/timewithin.pdf", width=4.5,height=3)

#plotMultiline(smoothFrame(time.within,5), ylim=NULL,ylabel="Time within")

### FITNESS DIVERSITY ########################

fitnessGenStats <- function(datalist, ...) {
  result <- data.frame()
  for(d in datalist) {
    if(length(d$jobs) > 0) {
      print(d$expname)
      res <- fitnessGenStats.aux(d, ...)
      result <- rbind(result, cbind(Exp=d$expname,res))    
    }
  }
  return(result)
}

fitnessGenStats.aux <- function(data, fun="sd", args=list(), threshold=0.1) {
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
        df[[sub]][g+1,job] <- ifelse(is.na(v), 0, v)
        dfc[[sub]][g+1,job] <- ifelse(v < threshold, 1, 0)
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

#res <- generationalStats(all.good.bad$ss.good, fun="rsd", threshold=0.1)
rsd.result <- fitnessGenStats(all.good.bad, fun="rsd", threshold=0.1)
rsd.result <- addTaskInfo(rsd.result, "Exp")
save(rsd.result, file="~/Dropbox/Work/Papers/NACO/plots/rsd.result.rdata")

ggplot(data=rsd.result, aes(x=gen, y=v.sub.0, colour=Task,lty=Type)) + ylab("RSD") + ylim(0,1.5) + xlab("Generation") + stat_smooth(se=F) + ggtitle("Ground robot pop.")
ggsave("~/Dropbox/Work/Papers/NACO/plots/rsd1.pdf", width=5,height=3.5)
ggplot(data=rsd.result, aes(x=gen, y=v.sub.1, colour=Task,lty=Type)) + ylab("RSD") + ylim(0,1.5) + xlab("Generation") + stat_smooth(se=F) + ggtitle("Aerial robot pop.")
ggsave("~/Dropbox/Work/Papers/NACO/plots/rsd2.pdf", width=5,height=3.5)

sd.result <- fitnessGenStats(all.good.bad, fun="sd", threshold=0.1)
sd.result <- addTaskInfo(sd.result, "Exp")
save(sd.result, file="~/Dropbox/Work/Papers/NACO/plots/sd.result.rdata")

ggplot(data=sd.result, aes(x=gen, y=v.sub.0, colour=Task,lty=Type)) + ylab("SD") + ylim(0,2) + xlab("Generation") + stat_smooth(se=F) + ggtitle("Ground robot pop.")
ggsave("~/Dropbox/Work/Papers/NACO/plots/sd1.pdf", width=5,height=3.5)
ggplot(data=sd.result, aes(x=gen, y=v.sub.1, colour=Task,lty=Type)) + ylab("SD") + ylim(0,2) + xlab("Generation") + stat_smooth(se=F) + ggtitle("Aerial robot pop.")
ggsave("~/Dropbox/Work/Papers/NACO/plots/sd2.pdf", width=5,height=3.5)




### BEHAVIOURAL DIVERSITY #####################

# use only the best-of-generation individuals

d.all <- diversity.group(all.good.bad)
d.accum <- diversity.group.gens(all.good.bad,25,accum=T)
d.accum <- addTaskInfo(d.accum,"Exp")
d.windows <- diversity.group.gens(all.good.bad,25,accum=F)
d.windows <- addTaskInfo(d.windows,"Exp")
save(d.accum, file="~/Dropbox/Work/Papers/NACO/plots/d.accum.rdata")
save(d.windows, file="~/Dropbox/Work/Papers/NACO/plots/d.windows.rdata")

g <- ggplot(data=d.accum, aes(x=Step, y=Mean, colour=Task,lty=Type)) + ylab("Accumulated behavioural diversity") + xlab("Generation") + geom_line() #+ geom_ribbon(aes(ymin=Mean-SE, ymax=Mean+SE),alpha=0.2)
ggsave(g, "~/Dropbox/Work/Papers/NACO/plots/fitdiversity.pdf", width=5,height=3)



### IPROVING CCEA - FITNESS ###################

down_tog <- metaLoadData("down_tog/fit","down_tog/nst","down_tog/moea","down_tog/staged","down_tog/halted","down_tog/inc", names=c("Fit","NS-T","MOEA","Staged","Halted","Inc"), params=list(jobs=30, subpops=NULL, gens=0:499, fitlim=c(0,6),merge.subpops=T, fitness.file="refitness.stat",load.behavs=F))
down_sep <- metaLoadData("down_sep/fit","down_sep/nst","down_sep/moea","down_sep/staged","down_sep/halted","down_sep/inc", names=c("Fit","NS-T","MOEA","Staged","Halted","Inc"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=T, fitness.file="refitness.stat",load.behavs=F))
stable_sep <- metaLoadData("stable_sep/fit","stable_sep/nst","stable_sep/moea","stable_sep/staged","stable_sep/halted","stable_sep/inc", names=c("Fit","NS-T","MOEA","Staged","Halted","Inc"), params=list(jobs=30, gens=0:499,subpops=NULL, fitlim=c(0,6),merge.subpops=T, fitness.file="refitness.stat",load.behavs=F))
down_mid <- metaLoadData("down_mid/fit","down_mid/nst","down_mid/moea","down_mid/staged","down_mid/halted","down_mid/inc", names=c("Fit","NS-T","MOEA","Staged","Halted","Inc"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=T, fitness.file="refitness.stat",load.behavs=F))

fullStatistics(down_tog, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="downtog",fit.comp.par=list(snapshots=c(499),jitter=T,ylim=T))
fullStatistics(down_sep, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="downsep",fit.comp.par=list(snapshots=c(699),jitter=T,ylim=T))
fullStatistics(stable_sep, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="stablesep",fit.comp.par=list(snapshots=c(499),jitter=T,ylim=T))
fullStatistics(down_mid, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="downmid",fit.comp.par=list(snapshots=c(699),jitter=T,ylim=T))

fitnessLevelReached(down_tog,4)
fitnessLevelReached(down_sep,4)
fitnessLevelReached(stable_sep,4)


### IMPROVING CCEA - BEHAVIOURAL DIVERSITY ###########

# best-of-gens
down_tog <- metaLoadData("down_tog/fit","down_tog/nst","down_tog/moea","down_tog/staged","down_tog/halted","down_tog/inc", names=c("Fit","NS_T","MOEA","Staged","Halted","Inc"), params=list(jobs=30, subpops=NULL, gens=0:499, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.file="rebehaviours.stat",behavs.sample=1,vars.group=vars.group, vars.extra=vars.extra, vars.file=c(vars.group,rep(NA,10),vars.extra)))
down_sep <- metaLoadData("down_sep/fit","down_sep/nst","down_sep/moea","down_sep/staged","down_sep/halted","down_sep/inc", names=c("Fit","NS_T","MOEA","Staged","Halted","Inc"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.file="rebehaviours.stat",behavs.sample=1,vars.group=vars.group, vars.extra=vars.extra, vars.file=c(vars.group,rep(NA,10),vars.extra)))
stable_sep <- metaLoadData("stable_sep/fit","stable_sep/nst","stable_sep/moea","stable_sep/staged","stable_sep/halted","stable_sep/inc", names=c("Fit","NS_T","MOEA","Staged","Halted","Inc"), params=list(jobs=30, gens=0:499,subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.file="rebehaviours.stat",behavs.sample=1,vars.group=vars.group, vars.extra=vars.extra, vars.file=c(vars.group,rep(NA,10),vars.extra)))
down_mid <- metaLoadData("down_mid/fit","down_mid/nst","down_mid/moea","down_mid/staged","down_mid/halted","down_mid/inc", names=c("Fit","NS_T","MOEA","Staged","Halted","Inc"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.file="rebehaviours.stat",behavs.sample=1,vars.group=vars.group, vars.extra=vars.extra, vars.file=c(vars.group,rep(NA,10),vars.extra)))

# 10% sample of all
down_tog <- metaLoadData("down_tog/fit","down_tog/nst","down_tog/moea","down_tog/staged","down_tog/halted","down_tog/inc", names=c("Fit","NS_T","MOEA","Staged","Halted","Inc"), params=list(jobs=30, subpops=NULL, gens=0:499, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.file="behaviours.stat",behavs.sample=0.1,vars.group=vars.group, vars.ind=rep(NA,4), vars.extra=c(NA,NA)))
down_sep <- metaLoadData("down_sep/fit","down_sep/nst","down_sep/moea","down_sep/staged","down_sep/halted","down_sep/inc", names=c("Fit","NS_T","MOEA","Staged","Halted","Inc"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.file="behaviours.stat",behavs.sample=0.1,vars.group=vars.group, vars.ind=rep(NA,4), vars.extra=c(NA,NA)))
stable_sep <- metaLoadData("stable_sep/fit","stable_sep/nst","stable_sep/moea","stable_sep/staged","stable_sep/halted","stable_sep/inc", names=c("Fit","NS_T","MOEA","Staged","Halted","Inc"), params=list(jobs=30, gens=0:499,subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.file="behaviours.stat",behavs.sample=0.1,vars.group=vars.group, vars.ind=rep(NA,4), vars.extra=c(NA,NA)))
down_mid <- metaLoadData("down_mid/fit","down_mid/nst","down_mid/moea","down_mid/staged","down_mid/halted","down_mid/inc", names=c("Fit","NS_T","MOEA","Staged","Halted","Inc"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.file="behaviours.stat",behavs.sample=0.1,vars.group=vars.group, vars.ind=rep(NA,4), vars.extra=c(NA,NA)))

dt.div <- diversity.group(down_tog)
ds.div <- diversity.group(down_sep)
ss.div <- diversity.group(stable_sep)
dm.div <- diversity.group(down_mid)

improv.div.bests <- rbind(
  cbind(Method=rownames(ss.div$summary),ss.div$summary,Task="Fix-Sep"),
  cbind(Method=rownames(dt.div$summary),dt.div$summary,Task="Var-Tog"),
  cbind(Method=rownames(dm.div$summary),dm.div$summary,Task="Var-Mid"),
  cbind(Method=rownames(ds.div$summary),ds.div$summary,Task="Var-Sep")
  )
improv.div.bests$Method <- factor(improv.div.bests$Method, levels=c("Fit","MOEA","Staged","Halted","Inc","NS_T"))
save(improv.div.bests, file="~/Dropbox/Work/Papers/NACO/plots/improv.div.bests.rdata")
#save(improv.div.bests, file="~/Dropbox/Work/Papers/NACO/plots/improv.div.all.rdata")

ggplot(improv.div.bests, aes(x=Task, y=mean, fill=Method)) + 
  geom_bar(position=position_dodge(), stat="identity") + ylab("Behavioural diversity") +
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se),width=.25, position=position_dodge(.9))
ggsave("~/Dropbox/Work/Papers/NACO/plots/improv_diversity_bests.pdf", width=6,height=3)
#ggsave("~/Dropbox/Work/Papers/NACO/plots/improv_diversity_all.pdf", width=6,height=3)

### IMPROVING CCEA - time within

# use best-of-generation indivduals

# split good and bad
all <- list(ss=stable_sep,dt=down_tog, dm=down_mid, ds=down_sep)
all.split <- list()
for(task in names(all)) {
  datalist <- all[[task]]
  fl <- fitnessLevelReached(datalist, 4)
  print(fl)
  newlist <- list()
  for(d in datalist) {
    print(d$expname)
    over <- as.logical(fl$data[d$expname,])
    good <- filterJobs(d, jobs=d$jobs[over], name=paste(task,"good",d$expname,sep="."))
    newlist[[good$expname]] <- good
    bad <- filterJobs(d, jobs=d$jobs[!over], name=paste(task,"bad",d$expname,sep="."))
    newlist[[bad$expname]] <- bad
  }
  all.split[[task]] <- newlist
}

# time within
twlist <- list()
for(task in names(all.split)) {
  print(task)
  datalist <- all.split[[task]]
  time.within <- analyseVar(datalist,"TimeWithin")
  tw <- na.omit(melt(time.within, id="gen"))
  tw <- addTaskInfo(tw,"variable")
  twlist[[task]] <- tw
}
save(twlist, file="~/Dropbox/Work/Papers/NACO/plots/twlist.rdata")

# plot time within through gens
for(task in names(twlist)) {
  tw <- twlist[[task]]
  g <- ggplot(data=tw, aes(x=gen, y=value, colour=Method,lty=Type)) + ylim(0,1) + ylab("Time within") + xlab("Generation") + stat_smooth(se=F,fill="grey75",n=500) + ggtitle(task)
  ggsave(paste0("~/Dropbox/Work/Papers/NACO/plots/tw_",task,".pdf"), width=5,height=3.5)
  print(g)
}

# bars with average time within
tw.frame <- data.frame()
for(task in names(twlist)) {
  tw <- twlist[[task]]
  tw.frame <- rbind(tw.frame,tw)
}
tw.means <- aggregate(value ~ Task + Type + Method, data=tw.frame, mean)
tw.sd <- aggregate(value ~ Task + Type + Method, data=tw.frame, sd)
tw.stats <- cbind(tw.means,sd=tw.sd$value)
ggplot(subset(tw.stats,Type=="Good"), aes(x=Task, y=value, fill=Method)) + 
  geom_bar(position=position_dodge(), stat="identity") + ylab("Time within")
ggplot(subset(tw.stats,Type=="Bad"), aes(x=Task, y=value, fill=Method)) + 
  geom_bar(position=position_dodge(), stat="identity") + ylab("Time within") + 


### IMPROVING CCEA -- PREMATURE CONVERGENCE

# split in good/bad runs
# calculate behav diversity of bests

divframe <- data.frame()
for(datalist in all.split) {
  div <- diversity.group(datalist)
  d <- cbind(exp=rownames(div$summary),div$summary)
  d <- addTaskInfo(d,"exp")
  divframe <- rbind(divframe,d)
}
save(divframe, file="~/Dropbox/Work/Papers/NACO/plots/divframe.rdata")
ggplot(divframe, aes(x=Task, y=mean, fill=Method)) + 
  geom_bar(position=position_dodge(), stat="identity") + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se),width=.25, position=position_dodge(.9)) +
  ylab("Behavioural diversity") + facet_grid(. ~ Type)
ggsave("~/Dropbox/Work/Papers/NACO/plots/improv_premature_convergence.pdf", width=7,height=3)




### INCREMENTAL EVOLUTION -- STAGE ANALYSIS ##########################
DEF_WIDTH = 5.5 ; DEF_HEIGHT = 3.5
analyse("stable_sep/staged","down_tog/staged","down_mid/staged","down_sep/staged", filename="incremental.stat", exp.names=c("Fix-Sep","Var-Tog","Var-Mid","Var-Sep"), vars.pre = c("gen"),vars.sub=c("above","above.threshold","fitness.threshold","stage"), analyse="stage", boxplots=F, all=F, ylim=c(0,2))
analyse("stable_sep/halted","down_tog/halted","down_mid/halted","down_sep/halted", filename="incremental.stat", exp.names=c("Fix-Sep","Var-Tog","Var-Mid","Var-Sep"), vars.pre = c("gen"),vars.sub=c("above","above.threshold","fitness.threshold","stage"), analyse="stage", boxplots=F, all=F, ylim=c(0,2))
analyse("stable_sep/inc","down_tog/inc","down_mid/inc","down_sep/inc", filename="incremental.stat", exp.names=c("Fix-Sep","Var-Tog","Var-Mid","Var-Sep"), vars.pre = c("gen"),vars.sub=c("above","above.threshold","fitness.threshold","stage"), analyse="stage", boxplots=F, all=F, ylim=c(0,5))


### GA VS NEAT #####################################################

stable_tog  <- metaLoadData("stable_tog/fit","stable_tog/gaga","stable_tog/ganeat","stable_tog/neatga", names=c("NEAT.NEAT","GA.GA","GA.NEAT","NEAT.GA"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=F))
down_tog <- metaLoadData("down_tog/fit","down_tog/gaga","down_tog/ganeat","down_tog/neatga", names=c("NEAT.NEAT","GA.GA","GA.NEAT","NEAT.GA"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=F))
down_sep <- metaLoadData("down_sep/fit","down_sep/gaga","down_sep/ganeat","down_sep/neatga", names=c("NEAT.NEAT","GA.GA","GA.NEAT","NEAT.GA"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=F))
stable_sep <- metaLoadData("stable_sep/fit","stable_sep/gaga","stable_sep/ganeat","stable_sep/neatga", names=c("NEAT.NEAT","GA.GA","GA.NEAT","NEAT.GA"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=F))
down_mid <- metaLoadData("down_mid/fit","down_mid/gaga","down_mid/ganeat","down_mid/neatga", names=c("NEAT.NEAT","GA.GA","GA.NEAT","NEAT.GA"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=F))

fullStatistics(stable_tog, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="pred_het",fit.comp.par=list(snapshots=c(699),jitter=T,ylim=T))
fullStatistics(down_tog, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="pred_het",fit.comp.par=list(snapshots=c(699),jitter=T,ylim=T))
fullStatistics(down_sep, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="pred_het",fit.comp.par=list(snapshots=c(699),jitter=T,ylim=T))
fullStatistics(stable_sep, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="pred_het",fit.comp.par=list(snapshots=c(699),jitter=T,ylim=T))
fullStatistics(down_mid, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="pred_het",fit.comp.par=list(snapshots=c(699),jitter=T,ylim=T))

fitness.frame <- rbind(
  cbind(Task="Fix-Tog",fitnessSummary(stable_tog)),
  cbind(Task="Fix-Sep",fitnessSummary(stable_sep)),
  cbind(Task="Var-Tog",fitnessSummary(down_tog)),
  cbind(Task="Var-Mid",fitnessSummary(down_mid)),
  cbind(Task="Var-Sep",fitnessSummary(down_sep))
  )
fitness.frame$method <- factor(fitness.frame$method, levels=c("GA.GA","NEAT.GA","GA.NEAT","NEAT.NEAT"))
save(fitness.frame, file="~/Dropbox/Work/Papers/NACO/plots/fitness.frame.rdata")

ggplot(fitness.frame, aes(x=Task, y=mean, fill=method)) + 
  geom_bar(position=position_dodge(), stat="identity") + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se),width=.25, position=position_dodge(.9)) + ylab("Fitness")
ggsave("~/Dropbox/Work/Papers/NACO/plots/ga.pdf", width=6,height=3)



## bars where x = task and colour = method
## or one line plot for each task




####################################################################################################3

down_sep <- metaLoadData("down_sep/fit","down_sep/nst","down_sep/moea","down_sep/staged","down_sep/halted","down_sep/inc","down_sep/stmoea", names=c("Fit","NS-T","MOEA","Staged","Halted","Inc","StMOEA"), params=list(jobs=10, subpops=NULL, fitlim=c(0,6),merge.subpops=T, fitness.file="refitness.stat",load.behavs=F))
stable_sep <- metaLoadData("stable_sep/fit","stable_sep/nst","stable_sep/moea","stable_sep/staged","stable_sep/halted","stable_sep/inc","stable_sep/stmoea", names=c("Fit","NS-T","MOEA","Staged","Halted","Inc","StMOEA"), params=list(jobs=10, gens=0:499,subpops=NULL, fitlim=c(0,6),merge.subpops=T, fitness.file="refitness.stat",load.behavs=F))
fullStatistics(down_sep, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="pred_het",fit.comp.par=list(snapshots=c(699),jitter=T,ylim=T))
fullStatistics(stable_sep, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="pred_het",fit.comp.par=list(snapshots=c(499),jitter=T,ylim=T))


down_tog <- metaLoadData("down_tog/fit","down_tog/gaga","down_tog/neatga","down_tog/ganeat", names=c("NEAT.NEAT","GA.GA","NEAT.GA","GA.NEAT"), params=list(jobs=10, subpops=NULL, fitlim=c(0,6),merge.subpops=T, fitness.file="refitness.stat",load.behavs=F))
fullStatistics(down_tog, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="pred_het",fit.comp.par=list(snapshots=c(499),jitter=T,ylim=T))



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
