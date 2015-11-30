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


### GENERAL DATA PROCESSING FUNCTIONS ##############################

addTaskInfo <- function(data,expvar) {
  data[grepl( "st" , data[[expvar]]),"Task"] <- "Fix-Tog"
  data[grepl( "dt" , data[[expvar]]),"Task"] <- "Var-Tog"
  data[grepl( "ss" , data[[expvar]]),"Task"] <- "Fix-Sep"
  data[grepl( "ds" , data[[expvar]]),"Task"] <- "Var-Sep"
  data[grepl( "dm" , data[[expvar]]),"Task"] <- "Var-Mid"
  data[grepl( "good" , data[[expvar]]),"Type"] <- "Good"
  data[grepl( "bad" , data[[expvar]]),"Type"] <- "Bad"
  data[grepl( "Fit" , data[[expvar]]),"Method"] <- "Fit"
  data[grepl( "NS" , data[[expvar]]),"Method"] <- "NS"
  data[grepl( "MOEA" , data[[expvar]]),"Method"] <- "MOEA"
  data[grepl( "Staged" , data[[expvar]]),"Method"] <- "Staged"
  data[grepl( "Halted" , data[[expvar]]),"Method"] <- "Halted"
  data[grepl( "Inc" , data[[expvar]]),"Method"] <- "Inc"
  data$Task <- factor(data$Task, levels=c("Fix-Tog","Fix-Sep","Var-Tog","Var-Mid","Var-Sep"))
  data$Type <- factor(data$Type, levels=c("Good","Bad"))
  data$Method <- factor(data$Method, levels=c("Fit","MOEA","Staged","Halted","Inc","NS"))
  return(data)
}

splitJobs <- function(datalist, threshold, verbose=F) {
  all.split <- list()
  fl <- fitnessLevelReached(datalist, threshold)
  if(verbose) {
    print(fl)
  }
  for(d in datalist) {
    over <- as.logical(fl$data[d$expname,])
    good <- filterJobs(d, jobs=d$jobs[over], name=paste(task,"good",d$expname,sep="."))
    all.split[[good$expname]] <- good
    bad <- filterJobs(d, jobs=d$jobs[!over], name=paste(task,"bad",d$expname,sep="."))
    all.split[[bad$expname]] <- bad
  }
  return(all.split)
}

analyseVars <- function(datalist, vars) {
  result <- data.frame()
  for(data in datalist) {
    if(length(data$jobs) > 0) {
      df <- data.frame()
      for(job in data$jobs) {
        frame <- rbind(data[[job]][["sub.0"]], data[[job]][["sub.1"]])
        df <- rbind(df, subset(frame, select=c("gen",vars)))
      }
      m <- aggregate(. ~ gen, data=df, mean)
      result <- rbind(result, cbind(Exp=data$expname,m))      
    }
  }
  return(result)
}


### TIME WITHIN #############



time.within <- analyseVar(all.good.bad,"TimeWithin")
tw <- na.omit(melt(time.within, id="gen"))
tw <- addTaskInfo(tw,"variable")
save(tw, file="~/Dropbox/Work/Papers/NACO/plots/tw.rdata")

g <- ggplot(data=tw, aes(x=gen, y=value, colour=Task,lty=Type)) + ylab("Time within") + xlab("Generation") + stat_smooth(se=F,fill="grey75",n=500) #+ geom_line()
ggsave("~/Dropbox/Work/Papers/NACO/plots/timewithin.pdf", width=4.5,height=3)

#plotMultiline(smoothFrame(time.within,5), ylim=NULL,ylabel="Time within")

### TIME WITHIN x FITNESS

analyseVars <- function(datalist, vars) {
  result <- data.frame()
  for(data in datalist) {
    if(length(data$jobs) > 0) {
      df <- data.frame()
      for(job in data$jobs) {
        frame <- rbind(data[[job]][["sub.0"]], data[[job]][["sub.1"]])
        df <- rbind(df, subset(frame, select=c("gen",vars)))
      }
      m <- aggregate(. ~ gen, data=df, mean)
      result <- rbind(result, cbind(Exp=data$expname,m))      
    }
  }
  return(result)
}

behavTrajectory <- analyseVars(all.good.bad, c("fitness","TimeWithin"))
behavTrajectory <- addTaskInfo(behavTrajectory, "Exp")
ggplot(behavTrajectory,aes(x=TimeWithin,y=fitness)) + geom_path(aes(colour=Type)) + facet_grid(. ~ Task) + xlim(0,1) + ylim(0,6)

#ggplot(behavTrajectory,aes(x=TimeWithin,y=fitness)) + geom_point(aes(colour=Type)) + facet_grid(. ~ Task)
#ggplot(behavTrajectory,aes(x=TimeWithin,y=fitness)) + geom_point(aes(shape=Type,colour=gen)) + scale_colour_gradient() + facet_grid(. ~ Task)


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



### IPROVING CCEA - FITNESS LINES ###################

down_tog <- metaLoadData("down_tog/fit","down_tog/moea","down_tog/staged","down_tog/halted","down_tog/nst", names=c("Fit","MOEA","Staged","Halted","NS"), params=list(jobs=30, subpops=NULL, gens=0:499, fitlim=c(0,6),merge.subpops=T, fitness.file="refitness.stat",load.behavs=F))
down_sep <- metaLoadData("down_sep/fit","down_sep/moea","down_sep/staged","down_sep/halted","down_sep/nst", names=c("Fit","MOEA","Staged","Halted","NS"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=T, fitness.file="refitness.stat",load.behavs=F))
stable_sep <- metaLoadData("stable_sep/fit","stable_sep/moea","stable_sep/staged","stable_sep/halted","stable_sep/nst", names=c("Fit","MOEA","Staged","Halted","NS"), params=list(jobs=30, gens=0:499,subpops=NULL, fitlim=c(0,6),merge.subpops=T, fitness.file="refitness.stat",load.behavs=F))
down_mid <- metaLoadData("down_mid/fit","down_mid/moea","down_mid/staged","down_mid/halted","down_mid/nst", names=c("Fit","MOEA","Staged","Halted","NS"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=T, fitness.file="refitness.stat",load.behavs=F))

improv.fitness <- rbind(
  cbind(Task="Fix-Sep",fitnessStatistics(stable_sep)),
  cbind(Task="Var-Tog",fitnessStatistics(down_tog)),
  cbind(Task="Var-Mid",fitnessStatistics(down_mid)),
  cbind(Task="Var-Sep",fitnessStatistics(down_sep))
)
save(improv.fitness, file="~/Dropbox/Work/Papers/NACO/plots/improv.fitness.rdata")

ggplot(improv.fitness, aes(x=Generation,y=Bestfar.Mean,colour=Exp,lty=Exp)) + geom_line() + facet_grid(. ~ Task, scales="free")
ggsave("~/Dropbox/Work/Papers/NACO/plots/improv_lines.pdf", width=12,height=3)


### LINES SPLITTED ############

fitnessStats <- list()
for(d in names(all.split)) {
  fitnessStats[[d]] <- fitnessStatistics(all.split[[d]])
}
fitnessStats <- do.call("rbind", fitnessStats)
fitnessStats <- addTaskInfo(fitnessStats,"Exp")
ggplot(fitnessStats, aes(x=Generation,y=Bestgen.Mean,colour=Method,lty=Type)) + geom_line() + facet_grid(. ~ Task, scales="free")


### IMPROVING CCEA - FITNESS BOXPLOTS ######

improv.boxplots <- rbind(
  cbind(Task="Fix-Sep",fitnessBests(stable_sep)),
  cbind(Task="Var-Tog",fitnessBests(down_tog)),
  cbind(Task="Var-Mid",fitnessBests(down_mid)),
  cbind(Task="Var-Sep",fitnessBests(down_sep))
)
improv.boxplots$Exp <- factor(improv.boxplots$Exp, levels=c("Fit","MOEA","Staged","Halted","NS"))
save(improv.boxplots, file="~/Dropbox/Work/Papers/NACO/plots/improv.boxplots.rdata")

ggplot(improv.boxplots, aes(Exp, Fitness)) + geom_boxplot(aes(fill=Exp)) + facet_grid(. ~ Task) + 
  ylim(0,6) + theme(axis.text.x = element_text(angle = 45, hjust = 1)) + guides(fill=FALSE)
ggsave("~/Dropbox/Work/Papers/NACO/plots/improv_boxplot.pdf", width=6,height=3)

### IMPROVING CCEA - FITNESS BARS ##########

improv.frame <- rbind(
  cbind(Task="Fix-Sep",fitnessSummary(stable_sep)),
  cbind(Task="Var-Tog",fitnessSummary(down_tog)),
  cbind(Task="Var-Mid",fitnessSummary(down_mid)),
  cbind(Task="Var-Sep",fitnessSummary(down_sep))
)
improv.frame <- subset(improv.frame, method != "Inc")
improv.frame$method <- factor(improv.frame$method, levels=c("Fit","MOEA","Staged","Halted","NS"))
save(improv.frame, file="~/Dropbox/Work/Papers/NACO/plots/improv.frame.rdata")

ggplot(improv.frame, aes(x=Task, y=mean, fill=method)) + 
  geom_bar(position=position_dodge(), stat="identity") + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se),width=.25, position=position_dodge(.9)) + ylab("Fitness")
ggsave("~/Dropbox/Work/Papers/NACO/plots/improv_bars.pdf", width=6,height=3)



### IMPROVING CCEA - BEHAVIOURAL DIVERSITY ###########

# best-of-gens
down_tog <- metaLoadData("down_tog/fit","down_tog/nst","down_tog/moea","down_tog/staged","down_tog/halted","down_tog/inc", names=c("Fit","NS","MOEA","Staged","Halted","Inc"), params=list(jobs=30, subpops=NULL, gens=0:499, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.file="rebehaviours.stat",behavs.sample=1,vars.group=vars.group, vars.extra=vars.extra, vars.file=c(vars.group,rep(NA,10),vars.extra)))
down_sep <- metaLoadData("down_sep/fit","down_sep/nst","down_sep/moea","down_sep/staged","down_sep/halted","down_sep/inc", names=c("Fit","NS","MOEA","Staged","Halted","Inc"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.file="rebehaviours.stat",behavs.sample=1,vars.group=vars.group, vars.extra=vars.extra, vars.file=c(vars.group,rep(NA,10),vars.extra)))
stable_sep <- metaLoadData("stable_sep/fit","stable_sep/nst","stable_sep/moea","stable_sep/staged","stable_sep/halted","stable_sep/inc", names=c("Fit","NS","MOEA","Staged","Halted","Inc"), params=list(jobs=30, gens=0:499,subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.file="rebehaviours.stat",behavs.sample=1,vars.group=vars.group, vars.extra=vars.extra, vars.file=c(vars.group,rep(NA,10),vars.extra)))
down_mid <- metaLoadData("down_mid/fit","down_mid/nst","down_mid/moea","down_mid/staged","down_mid/halted","down_mid/inc", names=c("Fit","NS","MOEA","Staged","Halted","Inc"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.file="rebehaviours.stat",behavs.sample=1,vars.group=vars.group, vars.extra=vars.extra, vars.file=c(vars.group,rep(NA,10),vars.extra)))

# 10% sample of all
down_tog <- metaLoadData("down_tog/fit","down_tog/nst","down_tog/moea","down_tog/staged","down_tog/halted", names=c("Fit","NS","MOEA","Staged","Halted"), params=list(jobs=30, subpops=NULL, gens=0:499, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.file="behaviours.stat",behavs.sample=0.1,vars.group=vars.group, vars.ind=rep(NA,4), vars.extra=c(NA,NA)))
down_sep <- metaLoadData("down_sep/fit","down_sep/nst","down_sep/moea","down_sep/staged","down_sep/halted", names=c("Fit","NS","MOEA","Staged","Halted"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.file="behaviours.stat",behavs.sample=0.1,vars.group=vars.group, vars.ind=rep(NA,4), vars.extra=c(NA,NA)))
stable_sep <- metaLoadData("stable_sep/fit","stable_sep/nst","stable_sep/moea","stable_sep/staged","stable_sep/halted", names=c("Fit","NS","MOEA","Staged","Halted"), params=list(jobs=30, gens=0:499,subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.file="behaviours.stat",behavs.sample=0.1,vars.group=vars.group, vars.ind=rep(NA,4), vars.extra=c(NA,NA)))
down_mid <- metaLoadData("down_mid/fit","down_mid/nst","down_mid/moea","down_mid/staged","down_mid/halted", names=c("Fit","NS","MOEA","Staged","Halted"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.file="behaviours.stat",behavs.sample=0.1,vars.group=vars.group, vars.ind=rep(NA,4), vars.extra=c(NA,NA)))

# 25% sample of all
down_tog <- metaLoadData("down_tog/fit","down_tog/nst","down_tog/moea","down_tog/staged","down_tog/halted", names=c("Fit","NS","MOEA","Staged","Halted"), params=list(jobs=30, subpops=NULL, gens=0:499, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.file="behaviours.stat",behavs.sample=0.25,vars.group=vars.group, vars.ind=rep(NA,4), vars.extra=c(NA,NA)))
down_sep <- metaLoadData("down_sep/fit","down_sep/nst","down_sep/moea","down_sep/staged","down_sep/halted", names=c("Fit","NS","MOEA","Staged","Halted"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.file="behaviours.stat",behavs.sample=0.25,vars.group=vars.group, vars.ind=rep(NA,4), vars.extra=c(NA,NA)))
stable_sep <- metaLoadData("stable_sep/fit","stable_sep/nst","stable_sep/moea","stable_sep/staged","stable_sep/halted", names=c("Fit","NS","MOEA","Staged","Halted"), params=list(jobs=30, gens=0:499,subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.file="behaviours.stat",behavs.sample=0.25,vars.group=vars.group, vars.ind=rep(NA,4), vars.extra=c(NA,NA)))
down_mid <- metaLoadData("down_mid/fit","down_mid/nst","down_mid/moea","down_mid/staged","down_mid/halted", names=c("Fit","NS","MOEA","Staged","Halted"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.file="behaviours.stat",behavs.sample=0.25,vars.group=vars.group, vars.ind=rep(NA,4), vars.extra=c(NA,NA)))
# filter by good individuals
down_tog <- filterByFitness(down_tog,4)
down_sep <- filterByFitness(down_sep,4)
stable_sep <- filterByFitness(stable_sep,4)
down_mid <- filterByFitness(down_mid,4)
save(ds.div, file="~/Dropbox/Work/Papers/NACO/plots/ds.div.filtered.rdata")
save(ss.div, file="~/Dropbox/Work/Papers/NACO/plots/ss.div.filtered.rdata")
save(dm.div, file="~/Dropbox/Work/Papers/NACO/plots/dm.div.filtered.rdata")

dt.div <- diversity.group(down_tog)
ds.div <- diversity.group(down_sep)
ss.div <- diversity.group(stable_sep)
dm.div <- diversity.group(down_mid)

save(dt.div, file="~/Dropbox/Work/Papers/NACO/plots/dt.div.rdata")
save(ds.div, file="~/Dropbox/Work/Papers/NACO/plots/ds.div.rdata")
save(ss.div, file="~/Dropbox/Work/Papers/NACO/plots/ss.div.rdata")
save(dm.div, file="~/Dropbox/Work/Papers/NACO/plots/dm.div.rdata")
load("~/Dropbox/Work/Papers/NACO/plots/dt.div.rdata");load("~/Dropbox/Work/Papers/NACO/plots/ds.div.rdata");load("~/Dropbox/Work/Papers/NACO/plots/ss.div.rdata");load("~/Dropbox/Work/Papers/NACO/plots/dm.div.rdata")

improv.div.bests <- rbind(
  cbind(Method=rownames(ss.div$summary),ss.div$summary,Task="Fix-Sep"),
  cbind(Method=rownames(dt.div$summary),dt.div$summary,Task="Var-Tog"),
  cbind(Method=rownames(dm.div$summary),dm.div$summary,Task="Var-Mid"),
  cbind(Method=rownames(ds.div$summary),ds.div$summary,Task="Var-Sep")
  )
improv.div.bests$Method <- factor(improv.div.bests$Method, levels=c("Fit","MOEA","Staged","Halted","Inc","NS"))
save(improv.div.bests, file="~/Dropbox/Work/Papers/NACO/plots/improv.div.bests.rdata")
#save(improv.div.bests, file="~/Dropbox/Work/Papers/NACO/plots/improv.div.all.rdata")

#load("~/Dropbox/Work/Papers/NACO/plots/improv.div.all.rdata")
improv.div.bests <- subset(improv.div.bests, Method != "Inc")

ggplot(improv.div.bests, aes(x=Task, y=mean, fill=Method)) + 
  geom_bar(position=position_dodge(), stat="identity") + ylab("Behavioural diversity") +
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se),width=.25, position=position_dodge(.9))
ggsave("~/Dropbox/Work/Papers/NACO/plots/improv_diversity_bests.pdf", width=6,height=3)
#ggsave("~/Dropbox/Work/Papers/NACO/plots/improv_diversity_all.pdf", width=6,height=3)



### IMPROVING CCEA -- time within x fitness ######

all.split <- splitJobs(list(ss=stable_sep,dt=down_tog, dm=down_mid, ds=down_sep), 4)
all.split <- c(all.split$ss,all.split$dt,all.split$dm,all.split$ds)
improvTrajectory <- analyseVars(all.split, c("fitness","TimeWithin"))
improvTrajectory <- addTaskInfo(improvTrajectory, "Exp")
ggplot(improvTrajectory,aes(x=TimeWithin,y=fitness)) + geom_path(aes(colour=Type)) + facet_grid(Method ~ Task) + xlim(0,1) + ylim(0,6)





### IMPROVING CCEA - time within

# use best-of-generation indivduals

# split good and bad
all <- list(ss=stable_sep,dt=down_tog, dm=down_mid, ds=down_sep)
all.split <- splitJobs(list(ss=stable_sep,dt=down_tog, dm=down_mid, ds=down_sep), 4)

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
load("~/Dropbox/Work/Papers/NACO/plots/twlist.rdata")

# plot time within through gens
twframe <-  do.call("rbind", twlist)
twframe <- subset(twframe, Method != "Inc")
ggplot(data=twframe, aes(x=gen, y=value, colour=Method,lty=Type)) + ylim(0,1) + ylab("Time within") + xlab("Generation") + stat_smooth(se=F,fill="grey75",n=500) + facet_grid(. ~ Task, scale="free")
ggsave("~/Dropbox/Work/Papers/NACO/plots/improv_timewithin.pdf", width=10,height=3)

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

load("~/Dropbox/Work/Papers/NACO/plots/divframe.rdata")
divframe <- subset(divframe, Method != "Inc")

ggplot(divframe, aes(x=Task, y=mean, fill=Method)) + 
  geom_bar(position=position_dodge(), stat="identity") + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se),width=.25, position=position_dodge(.9)) +
  ylab("Behavioural diversity") + facet_grid(. ~ Type)
ggsave("~/Dropbox/Work/Papers/NACO/plots/improv_premature_convergence.pdf", width=7,height=3)

ggplot(divframe, aes(x=Method, y=mean, fill=Type)) + 
  geom_bar(position=position_dodge(), stat="identity") + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se),width=.25, position=position_dodge(.9)) +
  ylab("Behavioural diversity") + facet_grid(. ~ Task) + theme(axis.text.x = element_text(angle = 22.5, hjust = 1))
ggsave("~/Dropbox/Work/Papers/NACO/plots/improv_premature_convergence_tasks.pdf", width=9,height=3)



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



analyse("down_sep/staged", filename="rebehaviours.stat", exp.names=c("dm.staged"), vars.pre = c("gen"),vars.sub=paste("v",1:18), vars.post=c("tw"), analyse="tw", boxplots=F, all=T, ylim=c(0,1))
