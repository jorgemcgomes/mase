DEF_HEIGHT=3.5
DEF_WIDTH=5
theme_set(theme_bw())

setwd("~/exps/ecalf/")
vars.group <- c("Items","Within","Dispersion","AvgProximity") ; vars.ind <- c("I1","I2","I3","I4") ; vars.extra <- c("Height","TimeWithin")

### GENERAL DATA PROCESSING FUNCTIONS ##############################

addTaskInfo <- function(data,expvar=NULL) {
  exp <- NULL
  if(is.null(expvar)) exp <- rownames(data) else exp <- data[[expvar]]
  data[grepl( "st" , exp),"Task"] <- "Fix-Tog"
  data[grepl( "dt" , exp),"Task"] <- "Var-Tog"
  data[grepl( "ss" , exp),"Task"] <- "Fix-Sep"
  data[grepl( "ds" , exp),"Task"] <- "Var-Sep"
  data[grepl( "dm" , exp),"Task"] <- "Var-Mid"
  data[grepl( "good" , exp),"Type"] <- "Successful"
  data[grepl( "bad" , exp),"Type"] <- "Failed"
  data[grepl( "fit" , exp),"Method"] <- "Fit"
  data[grepl( "ns" , exp),"Method"] <- "NS"
  data[grepl( "moea" , exp),"Method"] <- "MOEA"
  data[grepl( "staged" , exp),"Method"] <- "Inc"
  data[grepl( "halted" , exp),"Method"] <- "NInc"
  data[grepl( "inc" , exp),"Method"] <- "Env"
  data$Task <- factor(data$Task, levels=c("Fix-Tog","Fix-Sep","Var-Tog","Var-Mid","Var-Sep"))
  data$Type <- factor(data$Type, levels=c("Successful","Failed"))
  data$Method <- factor(data$Method, levels=c("Fit","Inc","NInc","MOEA","Env","NS"))
  data <- data[, colSums(is.na(data)) != nrow(data)]
  return(data)
}

### FITNESS BASIC CCEA ################

fit <- metaLoadData("down_tog/fit","down_sep/fit","down_mid/fit","stable_sep/fit","stable_tog/fit", names=c("dt.fit","ds.fit","dm.fit","ss.fit","st.fit"), params=list(jobs=30, subpops=2, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat", load.behavs=F))

basic.fitness <- fitnessStatistics(fit)
basic.fitness <- addTaskInfo(basic.fitness,"Exp")
ggplot(basic.fitness, aes(Generation,Bestfar.Mean,group=Task)) + geom_line(aes(colour=Task,lty=Task)) + ylim(0,6) + ylab("Fitness") +
  geom_ribbon(aes(ymax = Bestfar.Mean + Bestfar.SE, ymin = Bestfar.Mean - Bestfar.SE), alpha = 0.1)
ggsave("~/Dropbox/Work/Papers/NACO/plots/basic_lines.pdf", width=4.5,height=3)

basic.boxplots <- fitnessBests(fit)
basic.boxplots <- addTaskInfo(basic.boxplots,"Exp")
ggplot(basic.boxplots, aes(Task, Fitness)) + geom_boxplot(aes(fill=Task)) +
  ylim(0,6) + theme(axis.text.x = element_text(angle = 45, hjust = 1)) + guides(fill=FALSE)
ggsave("~/Dropbox/Work/Papers/NACO/plots/basic_boxplots.pdf", width=2.5,height=3)


### FITNESS GRADIENTS ################

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

fit <- metaLoadData("down_tog/fit","down_sep/fit","stable_sep/fit","stable_tog/fit","down_mid/fit", names=c("DownTog","DownSep","StableSep","StableTog","DownMid"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.sample=1, vars.group=rep(NA,4), vars.ind=rep(NA,4), vars.extra=rep(NA,2)))
all.good.bad <- splitJobs(fit, 4)

rsd.result <- fitnessGenStats(all.good.bad, fun="rsd", threshold=0.1)
rsd.result <- addTaskInfo(rsd.result, "Exp")
save(rsd.result, file="~/Dropbox/Work/Papers/NACO/plots/rsd.result.rdata")

load("~/Dropbox/Work/Papers/NACO/plots/rsd.result.rdata")
melted <- melt(rsd.result, id.vars=c("Exp","gen","Task","Type","Method"))
melted <- subset(melted, variable == "v.sub.0" | variable == "v.sub.1")
melted$variable <- factor(melted$variable, levels=c("v.sub.0","v.sub.1"), labels=c("Ground robot pop.","Aerial robot pop."))
ggplot(data=melted, aes(x=gen, y=value, colour=Task,lty=Type)) + ylab("RSD") + xlab("Generation") + 
  stat_smooth(se=F) + facet_grid(. ~ variable) + ylim(0,1.5) + theme(legend.position="bottom")
ggsave("~/Dropbox/Work/Papers/NACO/plots/fitness_diversity.pdf", width=5,height=3.75)


### Basic CCEA Time within x fitness ##############

fit <- metaLoadData("down_tog/fit","down_sep/fit","down_mid/fit","stable_sep/fit","stable_tog/fit", names=c("dt.fit","ds.fit","dm.fit","ss.fit","st.fit"), params=list(jobs=30, subpops=2, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat", behavs.file="rebehaviours.stat",load.behavs=T,behavs.sample=1, vars.group=vars.group, vars.extra=vars.extra, vars.file=c(vars.group,rep(NA,10),vars.extra)))
fit.split <- splitJobs(fit,F,4)
behavTrajectory <- analyseVars(fit.split, c("fitness","TimeWithin"))
behavTrajectory <- addTaskInfo(behavTrajectory, "Exp")
ggplot(behavTrajectory,aes(x=TimeWithin,y=fitness)) + geom_path(aes(colour=Type)) + facet_grid(. ~ Task) + 
  xlim(0,1) + ylim(0,6) + xlab("Time within range") + ylab("Number of items collected")

behavTrajectory$gen[behavTrajectory$Type=="Failed"] <- -behavTrajectory$gen[behavTrajectory$Type=="Failed"]
ggplot(behavTrajectory,aes(x=TimeWithin,y=fitness)) + geom_path(data=subset(behavTrajectory,Type=="Successful"),aes(colour=gen))+ 
  geom_path(data=subset(behavTrajectory,Type=="Failed"),aes(colour=gen)) + facet_grid(. ~ Task) + 
  xlim(0,1) + ylim(0,6) + xlab("Time within range") + ylab("Number of items collected") + 
  scale_color_gradientn(colours=c("blue4","skyblue1","pink1","red"), values=c(1,0.5001,0.4999,0)) +
  theme(axis.text.x = element_text(angle = 90, vjust=0.5))

ggsave("~/Dropbox/Work/Papers/NACO/plots/basic_fitness_tw.pdf", width=10,height=2.5)


### Basic CCEA behavioural diversity #############

fit <- metaLoadData("down_tog/fit","down_sep/fit","down_mid/fit","stable_sep/fit","stable_tog/fit", names=c("dt.fit","ds.fit","dm.fit","ss.fit","st.fit"), params=list(jobs=30, subpops=2, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat", behavs.file="rebehaviours.stat",load.behavs=T,behavs.sample=1, vars.group=vars.group, vars.extra=vars.extra, vars.file=c(vars.group,rep(NA,10),vars.extra)))
d.accum <- diversity.group.gens(all.good.bad,25,accum=T)
d.accum <- addTaskInfo(d.accum,"Exp")
save(d.accum, file="~/Dropbox/Work/Papers/NACO/plots/d.accum.rdata")
load("~/Dropbox/Work/Papers/NACO/plots/d.accum.rdata")

ggplot(data=d.accum, aes(x=Step, y=Mean, colour=Task,lty=Type)) + ylab("Accumulated behavioural diversity") + xlab("Generation") + geom_line() #+ geom_ribbon(aes(ymin=Mean-SE, ymax=Mean+SE),alpha=0.2)
ggsave("~/Dropbox/Work/Papers/NACO/plots/fitdiversity.pdf", width=5,height=3)


### Improv fitness lines ###############

stable_sep <- metaLoadData("stable_sep/fit","stable_sep/moea","stable_sep/staged","stable_sep/halted","stable_sep/nst", names=c("ss.fit","ss.moea","ss.staged","ss.halted","ss.nst"), params=list(jobs=30, gens=0:499,subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=F))
down_tog <- metaLoadData("down_tog/fit","down_tog/moea","down_tog/staged","down_tog/halted","down_tog/nst", names=c("dt.fit","dt.moea","dt.staged","dt.halted","dt.nst"), params=list(jobs=30, subpops=NULL, gens=0:499, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=F))
down_mid <- metaLoadData("down_mid/fit","down_mid/moea","down_mid/staged","down_mid/halted","down_mid/nst", names=c("dm.fit","dm.moea","dm.staged","dm.halted","dm.nst"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=F))
down_sep <- metaLoadData("down_sep_long/fit","down_sep_long/moea","down_sep_long/staged","down_sep_long/halted","down_sep_long/nst", names=c("ds.fit","ds.moea","ds.staged","ds.halted","ds.nst"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=F))

all.improv <- c(stable_sep,down_tog,down_mid,down_sep)

improv.fitness <- fitnessStatistics(all.improv)
improv.fitness <- addTaskInfo(improv.fitness,"Exp")

ggplot(improv.fitness, aes(x=Generation,y=Bestfar.Mean,colour=Method,lty=Method)) + geom_line() + 
  facet_wrap(~ Task, scales="free_x", ncol=2) + ylab("Fitness") + theme(legend.position="bottom")
ggsave("~/Dropbox/Work/Papers/NACO/plots/improv_lines.pdf", width=5,height=5)


### Improv boxplots ##################

improv.boxplots <- fitnessBests(all.improv)
improv.boxplots <- addTaskInfo(improv.boxplots,"Exp")

ggplot(improv.boxplots, aes(Method, Fitness)) + geom_boxplot(aes(fill=Method)) + facet_grid(. ~ Task) + 
  ylim(0,6) + theme(axis.text.x = element_text(angle = 45, hjust = 1)) + guides(fill=FALSE)
ggsave("~/Dropbox/Work/Papers/NACO/plots/improv_boxplot.pdf", width=5,height=3)

for(t in unique(improv.boxplots$Task)) {
  setlist <- list()
  for(m in unique(improv.boxplots$Method)) {
    sub <- subset(improv.boxplots, Method==m & Task==t,select="Fitness")
    setlist[[m]] <- sub$Fitness
  }
  print(t)
  print(metaAnalysis(setlist))
}

### Improv behavioural diversity all ##############

# 10% sample of all
stable_sep <- metaLoadData("stable_sep/fit","stable_sep/moea","stable_sep/staged","stable_sep/halted","stable_sep/nst", names=c("ss.fit","ss.moea","ss.staged","ss.halted","ss.nst"), params=list(jobs=30, gens=0:499,subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.sample=0.1,vars.group=vars.group, vars.ind=rep(NA,4), vars.extra=c(NA,NA)))
down_tog <- metaLoadData("down_tog/fit","down_tog/moea","down_tog/staged","down_tog/halted","down_tog/nst", names=c("dt.fit","dt.moea","dt.staged","dt.halted","dt.nst"), params=list(jobs=30, subpops=NULL, gens=0:499, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.sample=0.1,vars.group=vars.group, vars.ind=rep(NA,4), vars.extra=c(NA,NA)))
down_mid <- metaLoadData("down_mid/fit","down_mid/moea","down_mid/staged","down_mid/halted","down_mid/nst", names=c("dm.fit","dm.moea","dm.staged","dm.halted","dm.nst"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.sample=0.1,vars.group=vars.group, vars.ind=rep(NA,4), vars.extra=c(NA,NA)))
down_sep <- metaLoadData("down_sep_long/fit","down_sep_long/moea","down_sep_long/staged","down_sep_long/halted","down_sep_long/nst", names=c("ds.fit","ds.moea","ds.staged","ds.halted","ds.nst"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.sample=0.1,vars.group=vars.group, vars.ind=rep(NA,4), vars.extra=c(NA,NA)))
all.improv <- c(stable_sep,down_tog,down_mid,down_sep)
all.improv.split <- splitJobs(all.improv, F, 4)

improv.div.split <- diversity.group(all.improv.split)
save(improv.div.split, file="~/Dropbox/Work/Papers/NACO/plots/improv.div.split.rdata")
load("~/Dropbox/Work/Papers/NACO/plots/improv.div.split.rdata")

improv.div <- diversity.group(all.improv)
save(improv.div, file="~/Dropbox/Work/Papers/NACO/plots/improv.div.rdata")
load("~/Dropbox/Work/Papers/NACO/plots/improv.div.rdata")

improv.div.frame <- addTaskInfo(improv.div$summary)

ggplot(improv.div.frame, aes(x=Task, y=mean, fill=Method)) + 
  geom_bar(position=position_dodge(), stat="identity") + ylab("Behavioural diversity") +
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se),width=.25, position=position_dodge(.9)) +
  theme(legend.position="bottom")
ggsave("~/Dropbox/Work/Papers/NACO/plots/improv_diversity.pdf", width=5,height=3.5)


### IMPROVING CCEA -- time within x fitness ######

stable_sep <- metaLoadData("stable_sep/fit","stable_sep/moea","stable_sep/staged","stable_sep/halted","stable_sep/nst", names=c("ss.fit","ss.moea","ss.staged","ss.halted","ss.nst"), params=list(jobs=30, gens=0:499,subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat", behavs.file="rebehaviours.stat",load.behavs=T,behavs.sample=1, vars.group=vars.group, vars.extra=vars.extra, vars.file=c(vars.group,rep(NA,10),vars.extra)))
down_tog <- metaLoadData("down_tog/fit","down_tog/moea","down_tog/staged","down_tog/halted","down_tog/nst", names=c("dt.fit","dt.moea","dt.staged","dt.halted","dt.nst"), params=list(jobs=30, subpops=NULL, gens=0:499, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat", behavs.file="rebehaviours.stat",load.behavs=T,behavs.sample=1, vars.group=vars.group, vars.extra=vars.extra, vars.file=c(vars.group,rep(NA,10),vars.extra)))
down_mid <- metaLoadData("down_mid/fit","down_mid/moea","down_mid/staged","down_mid/halted","down_mid/nst", names=c("dm.fit","dm.moea","dm.staged","dm.halted","dm.nst"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat", behavs.file="rebehaviours.stat",load.behavs=T,behavs.sample=1, vars.group=vars.group, vars.extra=vars.extra, vars.file=c(vars.group,rep(NA,10),vars.extra)))
down_sep <- metaLoadData("down_sep_long/fit","down_sep_long/moea","down_sep_long/staged","down_sep_long/halted","down_sep_long/nst", names=c("ds.fit","ds.moea","ds.staged","ds.halted","ds.nst"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat", behavs.file="rebehaviours.stat",load.behavs=T,behavs.sample=1, vars.group=vars.group, vars.extra=vars.extra, vars.file=c(vars.group,rep(NA,10),vars.extra)))

all.improv <- c(stable_sep,down_tog,down_mid,down_sep)
all.improv.split <- splitJobs(all.improv, F, 4)

improvTrajectory <- analyseVars(all.improv.split, c("fitness","TimeWithin"))
improvTrajectory <- addTaskInfo(improvTrajectory, "Exp")

improvTrajectory$gen[improvTrajectory$Task=="Fix-Sep"] <- improvTrajectory$gen[improvTrajectory$Task=="Fix-Sep"] / 500
improvTrajectory$gen[improvTrajectory$Task=="Var-Tog"] <- improvTrajectory$gen[improvTrajectory$Task=="Var-Tog"] / 500
improvTrajectory$gen[improvTrajectory$Task=="Var-Mid"] <- improvTrajectory$gen[improvTrajectory$Task=="Var-Mid"] / 700
improvTrajectory$gen[improvTrajectory$Task=="Var-Sep"] <- improvTrajectory$gen[improvTrajectory$Task=="Var-Sep"] / 1000
improvTrajectory$gen[improvTrajectory$Type=="Failed"] <- -improvTrajectory$gen[improvTrajectory$Type=="Failed"]

ggplot(improvTrajectory,aes(x=TimeWithin,y=fitness)) + geom_path(data=subset(improvTrajectory,Type=="Successful"),aes(colour=gen))+ 
  geom_path(data=subset(improvTrajectory,Type=="Failed"),aes(colour=gen)) + facet_grid(Method ~ Task) + 
  xlim(0,1) + ylim(0,6) + xlab("Time within range") + ylab("Number of items collected") + 
  scale_color_gradientn(colours=c("blue4","skyblue1","pink1","red"), values=c(1,0.5001,0.4999,0)) +
  theme(axis.text.x = element_text(angle = 90, vjust=0.5))
ggsave("~/Dropbox/Work/Papers/NACO/plots/improv_fitness_tw.pdf", width=10,height=10)


### Incremental Evo Stages ###############

a <- analyse.raw("stable_sep/staged","down_tog/staged","down_mid/staged","down_sep_long/staged", filename="incremental.stat", exp.names=c("ss.staged","dt.staged","dm.staged","ds.staged"), vars.pre = c("gen"),vars.sub=c("above","above.threshold","fitness.threshold","stage"), analyse="stage", include.jobs=T)
b <- analyse.raw("stable_sep/halted","down_tog/halted","down_mid/halted","down_sep_long/halted", filename="incremental.stat", exp.names=c("ss.halted","dt.halted","dm.halted","ds.halted"), vars.pre = c("gen"),vars.sub=c("above","above.threshold","fitness.threshold","stage"), analyse="stage", include.jobs=T)
stages <- rbind(a,b)
stages <- addTaskInfo(stages,"Exp")
stages$Type <- NULL
stages <- na.omit(stages)
stages$Mean <- stages$Mean + 1

ggplot(stages, aes(Generation,Mean,group=Task)) + geom_line(aes(colour=Task,lty=Task)) + ylab("Stage number") +
  geom_ribbon(aes(ymax = Mean + SE, ymin = Mean - SE), alpha = 0.1) + facet_grid(. ~ Method, scale="free") + xlim(0,750) +
  scale_y_continuous(breaks=c(1,2,3)) + theme(legend.position="bottom") #theme(legend.justification=c(1,0), legend.position=c(1,0))
ggsave("~/Dropbox/Work/Papers/NACO/plots/stages.pdf", width=5,height=3.5)

ggplot(stages, aes(Generation,Mean,group=Method)) + geom_line(aes(colour=Method,lty=Method)) + ylab("Stage number") +
  geom_ribbon(aes(ymax = Mean + SE, ymin = Mean - SE), alpha = 0.1) + facet_wrap(~ Task, scales="free", ncol=2) +
  scale_y_continuous(breaks=c(1,2,3)) + theme(legend.position="bottom") #theme(legend.justification=c(1,0), legend.position=c(1,0))
ggsave("~/Dropbox/Work/Papers/NACO/plots/stages_alt.pdf", width=5,height=5)


all.list <- list()
for(m in unique(stages$Method)) {
  m.list <- list()
  for(t in unique(stages$Task)) {
    if(t != "Var-Tog") {
      st <- ifelse(t=="Fix-Sep",0,1)
      s <- subset(stages, Method==m & Task==t)
      vals <- c()
      for(c in 7:36) {
        gens <- sum(s[[c]] == st,na.rm=T)
        vals <- c(vals, gens)
      }
      all.list[[m]] <- c(all.list[[m]],vals)
      m.list[[t]] <- c(m.list[[t]],vals)
    }
  }
  print(m.list)
  print(metaAnalysis(m.list))
}
print(metaAnalysis(all.list))



### NEAT-GA ################################

stable_tog  <- metaLoadData("stable_tog/fit","stable_tog/gaga","stable_tog/ganeat","stable_tog/neatga", names=c("NEAT.NEAT","GA.GA","GA.NEAT","NEAT.GA"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=F))
down_tog <- metaLoadData("down_tog/fit","down_tog/gaga","down_tog/ganeat","down_tog/neatga", names=c("NEAT.NEAT","GA.GA","GA.NEAT","NEAT.GA"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=F))
down_sep <- metaLoadData("down_sep/fit","down_sep/gaga","down_sep/ganeat","down_sep/neatga", names=c("NEAT.NEAT","GA.GA","GA.NEAT","NEAT.GA"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=F))
stable_sep <- metaLoadData("stable_sep/fit","stable_sep/gaga","stable_sep/ganeat","stable_sep/neatga", names=c("NEAT.NEAT","GA.GA","GA.NEAT","NEAT.GA"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=F))
down_mid <- metaLoadData("down_mid/fit","down_mid/gaga","down_mid/ganeat","down_mid/neatga", names=c("NEAT.NEAT","GA.GA","GA.NEAT","NEAT.GA"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=F))

fitness.frame <- rbind(
  cbind(Task="Fix-Tog",fitnessSummary(stable_tog)),
  cbind(Task="Fix-Sep",fitnessSummary(stable_sep)),
  cbind(Task="Var-Tog",fitnessSummary(down_tog)),
  cbind(Task="Var-Mid",fitnessSummary(down_mid)),
  cbind(Task="Var-Sep",fitnessSummary(down_sep))
)
fitness.frame$method <- factor(fitness.frame$method, levels=c("GA.GA","NEAT.GA","GA.NEAT","NEAT.NEAT"), labels=c("GA-GA","NEAT-GA","GA-NEAT","NEAT-NEAT"))
#save(fitness.frame, file="~/Dropbox/Work/Papers/NACO/plots/fitness.frame.rdata")

ggplot(fitness.frame, aes(x=Task, y=mean, fill=method)) + 
  geom_bar(position=position_dodge(), stat="identity") + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se),width=.25, position=position_dodge(.9)) + ylab("Fitness") +
  theme(legend.position="bottom") + guides(fill = guide_legend(title="Method"))
ggsave("~/Dropbox/Work/Papers/NACO/plots/ga.pdf", width=5,height=3.5)

### Population sizes ####################

popsizes  <- metaLoadData("stable_tog/fit","stable_tog/fit_150_100","stable_tog/fit_150_50","stable_tog/fit_100_150","stable_tog/fit_50_150",
                            "stable_sep/fit","stable_sep/fit_150_100","stable_sep/fit_150_50","stable_sep/fit_100_150","stable_sep/fit_50_150",
                            "down_tog/fit","down_tog/fit_150_100","down_tog/fit_150_50","down_tog/fit_100_150","down_tog/fit_50_150",
                            "down_mid/fit","down_mid/fit_150_100","down_mid/fit_150_50","down_mid/fit_100_150","down_mid/fit_50_150",
                            "down_sep/fit","down_sep/fit_150_100","down_sep/fit_150_50","down_sep/fit_100_150","down_sep/fit_50_150",
                            names=c("st.g150.a150","st.g150.a100","st.g150.a50","st.g100.a150","st.g50.a150",
                                    "ss.g150.a150","ss.g150.a100","ss.g150.a50","ss.g100.a150","ss.g50.a150",
                                    "dt.g150.a150","dt.g150.a100","dt.g150.a50","dt.g100.a150","dt.g50.a150",
                                    "dm.g150.a150","dm.g150.a100","dm.g150.a50","dm.g100.a150","dm.g50.a150",
                                    "ds.g150.a150","ds.g150.a100","ds.g150.a50","ds.g100.a150","ds.g50.a150"),
                            params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=F))

fitnessBests(popsizes, T)

fit.pops <- fitnessSummary(popsizes)
fit.pops$Setup <- substring(fit.pops$method, 4)
fit.pops <- addTaskInfo(fit.pops,"method")
fit.pops[fit.pops$Setup=="g150.a150","PopSize"] <- 150
fit.pops[fit.pops$Setup=="g150.a100","PopSize"] <- 100
fit.pops[fit.pops$Setup=="g150.a50","PopSize"] <- 50
fit.pops[fit.pops$Setup=="g100.a150","PopSize"] <- 100
fit.pops[fit.pops$Setup=="g50.a150","PopSize"] <- 50
fit.pops[grepl("g150", fit.pops$Setup),"VaryingPopSize"] <- "Ground robot pop. size = 150"
fit.pops[grepl("a150", fit.pops$Setup),"VaryingPopSize"] <- "Aerial robot pop. size = 150"
a <- fit.pops[fit.pops$Setup=="g150.a150",]
a$VaryingPopSize <- "Ground robot pop. size = 150"
fit.pops <- rbind(fit.pops,a)

pd <- position_dodge(5)
ggplot(fit.pops, aes(x=PopSize, y=mean, colour=Task, group=Task)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se),width=20, position=pd) + 
  geom_line(aes(lty=Task)) +  theme(legend.position="bottom") +
  geom_point(position=pd, size=1.5, shape=21, fill="white") +
  facet_grid(. ~ VaryingPopSize, scale="free") + ylim(0,6) + 
  ylab("Fitness") + xlab("pop. size") + scale_x_reverse(breaks=c(150,100,50))
ggsave("~/Dropbox/Work/Papers/NACO/plots/popsizes.pdf", width=5,height=3.5)




### GARBAGE ###############

# old vs new comparison

test_down_sep <- metaLoadData("down_sep/fit","down_sep/moea","down_sep/staged","down_sep/halted","down_sep/nst","down_sep_long/fit","down_sep_long/moea","down_sep_long/staged","down_sep_long/halted","down_sep_long/nst", names=c("ds.fit","ds.moea","ds.staged","ds.halted","ds.nst","dsl.fit","dsl.moea","dsl.staged","dsl.halted","dsl.nst"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, gens=0:699,fitness.file="refitness.stat",load.behavs=F))
fullStatistics(test_down_sep, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="pred_het",fit.comp.par=list(snapshots=c(699),jitter=T,ylim=T))

# temp improv div

down_sep <- metaLoadData("down_sep_long/fit","down_sep_long/moea","down_sep_long/staged","down_sep_long/halted","down_sep_long/nst", names=c("ds.fit","ds.moea","ds.staged","ds.halted","ds.nst"), params=list(jobs=30, subpops=NULL, fitlim=c(0,6),merge.subpops=F, fitness.file="refitness.stat",load.behavs=T,behavs.sample=0.1,vars.group=vars.group, vars.ind=rep(NA,4), vars.extra=c(NA,NA)))
d <- diversity.group(down_sep)
save(d, file="~/Dropbox/Work/Papers/NACO/plots/div.down_sep_long.rdata")
d.frame <- addTaskInfo(d$summary)
improv.div <- improv.div[improv.div$Task != "Var-Sep", ]
improv.div <- rbind(improv.div,d.frame)
save(improv.div, file="~/Dropbox/Work/Papers/NACO/plots/improv.div.new.rdata")

rownames(improv.div.bests) <- c("ss.fit","ss.nst","ss.moea","ss.staged","ss.halted","ss.inc","dt.fit","dt.nst","dt.moea","dt.staged","dt.halted","dt.inc","dm.fit","dm.nst","dm.moea","dm.staged","dm.halted","dm.inc","ds.fit","ds.nst","ds.moea","ds.staged","ds.halted","ds.inc")
improv.div <- improv.div.bests
improv.div$Task <- NULL
improv.div$Method <- NULL
improv.div <- addTaskInfo(improv.div)
improv.div.frame <- subset(improv.div, Method!="Env")

# pop sizes other


ggplot(fit.pops, aes(x=Setup, y=mean, fill=Task)) + geom_bar(stat="identity") + ylab("Fitness") +
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se),width=.25, position=position_dodge(.9)) +
  theme(legend.position="bottom")


pd <- position_dodge(.1)
ggplot(fit.pops, aes(x=PopSize, y=mean, colour=VaryingPopSize, group=VaryingPopSize)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se),width=.25, position=pd) + ylab("Fitness") +
  geom_line() + 
  geom_point(position=pd, size=3, shape=21, fill="white") +
  facet_grid(. ~ Task) + ylim(0,6) +
  scale_x_reverse()

ggplot(fit.pops, aes(x=Generation,y=Bestfar.Mean,colour=Setup,lty=Setup)) + geom_line() + 
  facet_wrap(~ Task, scales="free_x", ncol=2) + ylab("Fitness") + theme(legend.position="bottom")

ggplot(fit.pops, aes(Setup, Fitness)) + geom_boxplot(aes(fill=Setup)) + facet_grid(. ~ Task) + 
  ylim(0,6) + theme(axis.text.x = element_text(angle = 45, hjust = 1)) + guides(fill=FALSE)
