### Base ######

setwd("~/exps/allocationx")
hyb.cols <- c("Generation","Evaluations","NumPops",NA,NA,NA,"MeanAge","MaxAge","Merges","Splits","TotalMerges","TotalSplits",NA,"MeanDist",NA)
pops_scale <- scale_y_continuous(breaks=seq(1,10),minor_breaks=NULL,limits=c(1,10))

fitnessDecay <- function(job, lookAhead=1) {
  phased.diff <- ((c(job$BestGen[-1:-lookAhead],rep(NA,lookAhead)) - job$BestGen) / abs(job$BestGen)) * 100
  res <- list(
    Baseline = mean(phased.diff, na.rm=T),
    AfterMerge = mean(phased.diff[which(job$Merges > 0)], na.rm=T),
    AfterSplit = mean(phased.diff[which(job$Splits > 0)], na.rm=T)
  )
  return(res)
}

### Method X number of targets #####

comp <- loadData("comparison_*", "fitness.stat", fun=loadFitness, auto.ids.names=c("Experiment","Method","UniqueTargets"),  filter=bestSoFarEvaluations, filter.par=list(step=1000))
comp[, Method := factor(Method,labels=c("CCEA-H","CCEA-PH","HybCCEA","R-Exch","C-Exch"))]

# highest fitnesses
metaAnalysis(lastGen(comp), BestSoFar~Method, ~UniqueTargets)
metaAnalysis(lastGen(comp), BestSoFar~UniqueTargets, ~Method)

ggplot(comp[,.(Mean=mean(BestSoFar)),by=.(Evaluations,Method,UniqueTargets)], aes(Evaluations,Mean)) + geom_line(aes(group=Method,colour=Method)) + facet_wrap(~ UniqueTargets)

#ggplot(lastGen(comp)[,.(Mean=mean(BestSoFar),SE=se(BestSoFar)),by=.(Method,UniqueTargets)], aes(UniqueTargets,Mean,group=Method,colour=Method)) + 
#  geom_line(size=.3) + geom_point(size=.5) + geom_linerange(aes(ymin=Mean-SE, ymax=Mean+SE), size=.3) +
#  ylab("Highest fitness score achieved") + xlab("Number of unique targets") + ylim(0.85,1) + guides(colour=guide_legend(nrow=2))
#ggsave("~/Dropbox/Work/Papers/TEC/fig2/comparison_fitness.pdf", width=1.75, height=3, scale=1)

ggplot(lastGen(comp), aes(UniqueTargets,BestSoFar,colour=Method)) + 
  geom_boxplot(size=.3,outlier.size=.4) + scale_color_brewer(palette="Set1") +
  labs(y="Highest fitness score achieved", x="Number of unique targets",color=NULL)
ggsave("~/Dropbox/Work/Papers/TEC/fig2/comparison_fitness.pdf", width=3.5, height=2.25, scale=1)



# evals until level -- note the failed runs with 10 targets
levels <- comp[, fitnessLevels(.SD,0.995), by=.(Job,Method,UniqueTargets)]
levels.sum <- levels[, .(.N, Mean=mean(Evaluations), SE=se(Evaluations)), by=.(Method,UniqueTargets)]

metaAnalysis(levels, Evaluations~Method, ~UniqueTargets)

ggplot(levels.sum[N>=10], aes(UniqueTargets,Mean,group=Method,colour=Method)) + geom_line(size=.3) +
  geom_point(size=.5) + geom_errorbar(aes(ymin=Mean-SE, ymax=Mean+SE), width=.2, size=.3) +
  ylab("Evaluations (x1000) to solution") + xlab("Number of unique targets") + guides(colour=guide_legend(nrow=2)) + ylim(0,NA)
ggsave("~/Dropbox/Work/Papers/TEC/fig2/comparison_evaluations.pdf", width=1.75, height=3, scale=1) 

ggplot(levels, aes(UniqueTargets,Evaluations/1000, colour=Method)) + 
  geom_boxplot(size=.3,outlier.size=.4) + scale_color_brewer(palette="Set1") +
  labs(y="Evaluations (x1000) to solution", x="Number of unique targets",color=NULL) + ylim(0,NA)
ggsave("~/Dropbox/Work/Papers/TEC/fig2/comparison_evaluations.pdf", width=3.5, height=2.25, scale=1)


m <- merge(levels[Method=="Hyb-CCEA"],levels[Method=="CCEA-Opt"], by=c("Job","UniqueTargets","Threshold"))
cor(m$Evaluations.x,m$Evaluations.y)

### Number of dimensions X number of targets #####

dims <- loadData("dimensions_*", "fitness.stat", fun=loadFitness, auto.ids.names=c("Experiment","UniqueTargets","Dimensions"),  filter=bestSoFarEvaluations, filter.par=list(step=1000))

ggplot(lastGen(dims)[,.(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(UniqueTargets,Dimensions)], aes(Dimensions,Mean,group=UniqueTargets,colour=UniqueTargets)) + 
  geom_line() + geom_point(size=1) + geom_errorbar(aes(ymin=Mean-SE, ymax=Mean+SE), width=.3) + geom_hline(yintercept=0.995,linetype=3) +
  ylab("Maximum fitness score")
ggsave("~/Dropbox/Work/Papers/TEC/fig/dimensions_fitness.pdf", width=3.5, height=3, scale=1) 

metaAnalysis(lastGen(dims), BestSoFar~UniqueTargets, ~Dimensions)

dimshyb <- loadData("dimensions_*","hybrid.stat", auto.ids.names=c("Experiment","UniqueTargets","Dimensions"),fun=loadFile, colnames=hyb.cols)

stat <- dimshyb[, .(NumPops = mean(NumPops)), by=.(Job,UniqueTargets,Dimensions)]
metaAnalysis(stat, NumPops~Dimensions, ~UniqueTargets)

ggplot(stat[,.(NumPops=mean(NumPops),SE=se(NumPops)), by=.(UniqueTargets,Dimensions)], aes(Dimensions,NumPops,group=UniqueTargets,colour=UniqueTargets)) + 
  geom_hline(aes(yintercept=as.numeric(as.character(UniqueTargets)),colour=UniqueTargets),linetype=3) + geom_line() + geom_point(size=1) + pops_scale + geom_errorbar(aes(ymin=NumPops-SE, ymax=NumPops+SE), width=.3) +
  ylab("Mean number of populations") + labs(colour="Number of unique targets")
ggsave("~/Dropbox/Work/Papers/TEC/fig/dimensions_populations_mean.pdf", width=3.5, height=3, scale=1) 

# stat_summary(fun.y=median, geom="line", aes(group=UniqueTargets), size=.2) 
ggplot(stat, aes(Dimensions,NumPops,colour=UniqueTargets)) + 
  geom_boxplot(size=.3,outlier.size=.4, position="identity") +
  pops_scale + scale_color_brewer(palette="Set1") + 
  labs(colour="Number of unique targets", y="Mean number of populations")
ggsave("~/Dropbox/Work/Papers/TEC/fig2/dimensions_populations_mean.pdf", width=1.75, height=2.5, scale=1) 

ggplot(lastGen(dims), aes(Dimensions,BestSoFar,colour=UniqueTargets)) + 
  geom_boxplot(size=.3,outlier.size=.4, position="identity") +
  scale_color_brewer(palette="Set1") +ylim(0.985,1) +
  labs(colour="Number of unique targets", y="Highest fitness score")
ggsave("~/Dropbox/Work/Papers/TEC/fig2/dimensions_fitness.pdf", width=1.75, height=2.5, scale=1) 


### Number of agents X number of targets #####

ag <- loadData("agents_*", "fitness.stat", fun=loadFitness, auto.ids.names=c("Experiment","UniqueTargets","Agents"),  filter=bestSoFarEvaluations, filter.par=list(step=5000))
metaAnalysis(lastGen(ag), BestSoFar~UniqueTargets+Agents)

ggplot(lastGen(ag)[,.(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(UniqueTargets,Agents)], aes(UniqueTargets,Mean,group=Agents,colour=Agents)) + 
  geom_line() + geom_point(size=1) + geom_errorbar(aes(ymin=Mean-SE, ymax=Mean+SE), width=.3) + geom_hline(yintercept=0.995,linetype=3) +
  ylab("Maximum fitness score") + ylab("Fitness")
ggsave("~/Dropbox/Work/Papers/TEC/fig/agents_fitness.pdf", width=3.5, height=3, scale=1) 

ggplot(lastGen(ag), aes(UniqueTargets,BestSoFar,fill=Agents)) + geom_boxplot()


# number of pops
aghyb <- loadData("agents_*","hybrid.stat", auto.ids.names=c("Experiment","UniqueTargets","Agents"),fun=loadFile, colnames=hyb.cols)

stat <- aghyb[, .(NumPops = mean(NumPops)), by=.(Job,UniqueTargets,Agents)]
stat[, Ratio := NumPops / factorNum(UniqueTargets)]
stat[, Difference := NumPops - factorNum(UniqueTargets)]

ggplot(stat[,.(NumPops=mean(NumPops),SE=se(NumPops)), by=.(UniqueTargets,Agents)], aes(UniqueTargets,NumPops,group=Agents,colour=Agents)) + 
  geom_line() + geom_point(size=1) + ylim(0,NA) + 
  ylab("Mean number of populations") + xlab("Unique targets") + scale_color_brewer(palette="Set1") 
ggsave("~/Dropbox/Work/Papers/TEC/fig2/agents_populations_mean.pdf", width=3.5, height=3, scale=1) 

ggplot(stat, aes(UniqueTargets,NumPops,colour=Agents)) + 
  stat_summary(fun.y=mean, geom="line", aes(group=Agents), size=.2) +
  geom_boxplot(size=.2,outlier.size=.4,position="identity") +
  scale_color_brewer(palette="Set1") + 
  labs(x="Unique targets", y="Mean number of populations")
ggsave("~/Dropbox/Work/Papers/TEC/fig2/agents_populations_mean.pdf", width=3.5, height=2.5, scale=1) 



metaAnalysis(stat, NumPops~Agents+UniqueTargets)

ggplot(stat[,.(NumPops=mean(NumPops),SE=se(NumPops)), by=.(UniqueTargets,Agents)], aes(factorNum(UniqueTargets),NumPops,group=Agents,colour=Agents)) + 
  geom_abline(slope=1,intercept=0,linetype=3) + geom_line() + geom_point(size=1) + ylim(0,NA) + 
  ylab("Mean number of populations") + xlab("Unique targets") + coord_fixed()
ggsave("~/Dropbox/Work/Papers/TEC/fig/agents_populations_mean.pdf", width=3.5, height=3.5, scale=1) 

stat[,.(cor(NumPops,factorNum(UniqueTargets),method="p"))]

ggplot(stat[,.(NumPops=mean(NumPops),SE=se(NumPops)), by=.(UniqueTargets,Agents)], aes(Agents,UniqueTargets)) + 
  geom_tile(aes(fill=NumPops)) + scale_fill_distiller(palette="Greys",direction=1) + geom_label(aes(label=round(NumPops,1)),size=2) +
  labs(fill="Mean number of populations", x="Number of agents", y="Unique targets")
ggsave("~/Dropbox/Work/Papers/TEC/fig/agents_populations_heat.pdf", width=3.5, height=3, scale=1) 


### Initial composition X number of targets #####

initfit <- loadData("init*","fitness.stat", auto.ids.names=c("Experiment","UniqueTargets","InitialPops"),fun=loadFitness, filter=bestSoFarEvaluations, filter.par=list(step=1000))
metaAnalysis(lastGen(initfit), BestSoFar~InitialPops, ~UniqueTargets)

ggplot(lastGen(initfit)[,.(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(InitialPops,UniqueTargets)], aes(UniqueTargets,Mean,group=InitialPops,colour=InitialPops)) + 
  geom_line() + geom_point(size=1) + geom_errorbar(aes(ymin=Mean-SE, ymax=Mean+SE), width=.3) + ylim(NA,1) + geom_hline(yintercept=0.995,linetype=3) +
  ylab("Maximum fitness score") + ylab("Number of unique targets")
ggsave("~/Dropbox/Work/Papers/TEC/fig/init_fitness.pdf", width=3.5, height=3, scale=1) 

levels <- initfit[, fitnessLevels(.SD,0.995), by=.(Job,InitialPops,UniqueTargets)]
metaAnalysis(levels, Evaluations~InitialPops, ~UniqueTargets)
ggplot(levels, aes(UniqueTargets,Evaluations)) + geom_boxplot(aes(fill=InitialPops)) +
  ylab("Evaluations until fitness > 0.995") + xlab("Number of unique targets")
ggsave("~/Dropbox/Work/Papers/TEC/fig/init_evaluations.pdf", width=3.5, height=3, scale=1) 


inithyb <- loadData("init*","hybrid.stat",auto.ids.names=c("Experiment","UniqueTargets","InitialPops"),fun=loadFile, colnames=hyb.cols, parallel=F)
inithyb[, Evaluations := floor(Evaluations / 1000) * 1000]
ggplot(inithyb[, .(NumPops=mean(NumPops)), by=.(Evaluations,UniqueTargets,InitialPops)], aes(Evaluations/1000,NumPops)) + 
  geom_line(aes(colour=InitialPops,group=InitialPops),size=.2) +  scale_color_brewer(palette="Set1") +
  facet_wrap(~ UniqueTargets, labeller=label_both) + 
  pops_scale + xlim(0,500) + labs(colour="Number of initial populations",y="Mean number of populations",x="Evaluations (x1000)")
ggsave("~/Dropbox/Work/Papers/TEC/fig2/init_populations.pdf", width=3.5, height=3.5, scale=1) 

stat <- inithyb[Evaluations > 1000000, .(NumPops = mean(NumPops)), by=.(Job,UniqueTargets,InitialPops)]
metaAnalysis(stat, NumPops~InitialPops, ~UniqueTargets)



### Merge threshold X target setup #####

pd = position_dodge(.5)
mergefit <- loadData("merge_*_1.0_*","fitness.stat", auto.ids.names=c("Experiment","UniqueTargets","Dispersion","MergeThreshold"),fun=loadFitness, filter=bestSoFarEvaluations, filter.par=list(step=1000))
ggplot(lastGen(mergefit), aes(MergeThreshold,BestSoFar,colour=UniqueTargets)) + 
  stat_summary(fun.y=median, geom="line", aes(group=UniqueTargets), size=.2, position=pd) +
  geom_boxplot(size=.3,outlier.size=.4, position=pd) +
  scale_color_brewer(palette="Set1") + 
  labs(x="Merge threshold", y="Highest fitness",colour="Unique targets")
ggsave("~/Dropbox/Work/Papers/TEC/fig2/merge_fitness.pdf", width=3.5, height=2.5, scale=1) 

metaAnalysis(lastGen(mergefit), BestSoFar ~ MergeThreshold, ~UniqueTargets)

levels <- mergefit[, fitnessLevels(.SD,0.995), by=.(Job,MergeThreshold,UniqueTargets)]
ggplot(levels, aes(MergeThreshold,Evaluations/1000,colour=UniqueTargets)) + 
  stat_summary(fun.y=median, geom="line", aes(group=UniqueTargets), size=.2, position=pd) +
  geom_boxplot(size=.3,outlier.size=.4, width=.6, position=pd) +
  scale_color_brewer(palette="Set1") + ylim(0,NA) +
  labs(x="Merge threshold", y="Evaluations (x1000) until solution",colour="Unique targets")
ggsave("~/Dropbox/Work/Papers/TEC/fig2/merge_evaluations.pdf", width=3.5, height=2.5, scale=1) 

metaAnalysis(levels, Evaluations ~ MergeThreshold, ~UniqueTargets)
levels[, cor(Evaluations,factorNum(MergeThreshold),method="spearman") , by=.(UniqueTargets)]

mergehyb <- loadData("merge_*_1.0_*","hybrid.stat", auto.ids.names=c("Experiment","UniqueTargets","Dispersion","MergeThreshold"),fun=loadFile, colnames=hyb.cols)
stat <- mergehyb[, .(NumPops=mean(NumPops)), by=.(Job,UniqueTargets,MergeThreshold)] 
ggplot(stat, aes(MergeThreshold,NumPops,colour=UniqueTargets)) +
  stat_summary(fun.y=median, geom="line", aes(group=UniqueTargets), size=.2, position=pd) +
  geom_boxplot(size=.3,outlier.size=.2, width=.6, position=pd) +
  scale_color_brewer(palette="Set1") + pops_scale +
  labs(x="Merge threshold", y="Mean number of populations",colour="Unique targets")
ggsave("~/Dropbox/Work/Papers/TEC/fig2/merge_populations.pdf", width=3.5, height=2.5, scale=1) 

metaAnalysis(stat, NumPops ~ MergeThreshold, ~UniqueTargets)
cor(stat$NumPops, factorNum(stat$MergeThreshold), method="spearman")

metaAnalysis(stat[factorNum(MergeThreshold) > 0.35], NumPops ~ MergeThreshold, ~UniqueTargets)


### Split threshold X target setup #####

pd = position_dodge(.5)
splitfit <- loadData("split_*","fitness.stat", auto.ids.names=c("Experiment","UniqueTargets","MaxLockdown"),fun=loadFitness, filter=bestSoFarEvaluations, filter.par=list(step=1000))
ggplot(lastGen(splitfit), aes(MaxLockdown,BestSoFar,colour=UniqueTargets)) + 
  stat_summary(fun.y=median, geom="line", aes(group=UniqueTargets), size=.2, position=pd) +
  geom_boxplot(size=.3,outlier.size=.4, position=pd) +
  scale_color_brewer(palette="Set1") + 
  labs(x="Max. lockdown", y="Highest fitness",colour="Unique targets")
ggsave("~/Dropbox/Work/Papers/TEC/fig2/split_fitness.pdf", width=3.5, height=2.5, scale=1) 

levels <- splitfit[, fitnessLevels(.SD,0.995), by=.(Job,MaxLockdown,UniqueTargets)]
ggplot(levels, aes(MaxLockdown,Evaluations/1000,colour=UniqueTargets)) + 
  stat_summary(fun.y=median, geom="line", aes(group=UniqueTargets), size=.2, position=pd) +
  geom_boxplot(size=.3,outlier.size=.4, width=.6, position=pd) +
  scale_color_brewer(palette="Set1") + ylim(0,NA) +
  labs(x="Max. lockdown", y="Evaluations (x1000) until solution",colour="Unique targets")
ggsave("~/Dropbox/Work/Papers/TEC/fig2/split_evaluations.pdf", width=3.5, height=2.5, scale=1) 

splithyb <- loadData("split*","hybrid.stat", auto.ids.names=c("Experiment","UniqueTargets","MaxLockdown"),fun=loadFile, colnames=hyb.cols)
ggplot(splithyb[, .(NumPops=mean(NumPops)), by=.(Job,UniqueTargets,MaxLockdown)], aes(MaxLockdown,NumPops,colour=UniqueTargets)) +
  stat_summary(fun.y=median, geom="line", aes(group=UniqueTargets), size=.2, position=pd) +
  geom_boxplot(size=.3,outlier.size=.2, width=.6, position=pd) +
  scale_color_brewer(palette="Set1") + pops_scale +
  labs(x="Max. lockdown", y="Mean number of populations",colour="Unique targets")
ggsave("~/Dropbox/Work/Papers/TEC/fig2/split_populations.pdf", width=3.5, height=2.5, scale=1) 


## old



mergefit[, Targets := factor(paste0("N=",UniqueTargets,",D=",Dispersion))]
mergefit[, Targets := factor(Targets, levels=levels(Targets)[c(1,2,3,4,5,8,7,6)])]

metaAnalysis(lastGen(mergefit), BestSoFar~MergeThreshold, ~Targets)

# fitness
ggplot(lastGen(mergefit)[,.(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(Targets,MergeThreshold)], aes(MergeThreshold,Mean,group=Targets,colour=Targets)) + 
  geom_line() + geom_point(size=1)  + ylab("Maximum fitness") + xlab("Merge threshold") + geom_hline(yintercept = 0.995, linetype=3)
ggsave("~/Dropbox/Work/Papers/TEC/fig/merge_fitness.pdf", width=3.5, height=3, scale=1) 

# evaluations
levels <- mergefit[, fitnessLevels(.SD,0.995), by=.(Job,MergeThreshold,Targets)]
levels.sum <- levels[, .(.N, Mean=mean(Evaluations), SE=se(Evaluations)), by=.(MergeThreshold,Targets)]
metaAnalysis(levels, Evaluations~MergeThreshold, ~Targets)
ggplot(levels.sum[N>=10], aes(MergeThreshold,Mean,group=Targets,colour=Targets)) + geom_line() +
  geom_point(size=1) + geom_errorbar(aes(ymin=Mean-SE, ymax=Mean+SE), width=.2) +
  ylab("Evaluations until fitness > 0.995") + xlab("Number of unique targets") + guides(colour=guide_legend(nrow=2)) + ylim(0,NA)

mergehyb <- loadData("merge*","hybrid.stat", auto.ids.names=c("Experiment","UniqueTargets","Dispersion","MergeThreshold"),fun=loadFile, colnames=hyb.cols)
mergehyb[, Targets := factor(paste0("N=",UniqueTargets,",D=",Dispersion))]
mergehyb[, Targets := factor(Targets, levels=levels(Targets)[c(1,2,3,4,5,8,7,6)])]

stat <- mergehyb[, .(NumPops = mean(NumPops)), by=.(Job,Targets,UniqueTargets,Dispersion,MergeThreshold)]
metaAnalysis(stat, NumPops~MergeThreshold, ~Targets)

ggplot(stat[,.(Mean=mean(NumPops),SE=se(NumPops)), by=.(Targets,MergeThreshold)], aes(MergeThreshold,Mean,group=Targets,colour=Targets)) + 
  geom_line() + geom_point(size=1) + #geom_errorbar(aes(ymin=Mean-SE, ymax=Mean+SE), width=.3) +
  ylab("Mean number of populations") + xlab("Merge treshold") + pops_scale
ggsave("~/Dropbox/Work/Papers/TEC/fig/merge_populations_abs.pdf", width=3.5, height=3, scale=1) 


### Split threshold X number of targets #####

splitfit <- loadData("split*","fitness.stat", auto.ids.names=c("Experiment","UniqueTargets","MaxLockdown"),fun=loadFitness, filter=bestSoFarEvaluations, filter.par=list(step=1000))
metaAnalysis(lastGen(splitfit), BestSoFar~MaxLockdown, ~UniqueTargets)

# fitness
ggplot(lastGen(splitfit)[,.(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(UniqueTargets,MaxLockdown)], aes(MaxLockdown,Mean,group=UniqueTargets,colour=UniqueTargets)) + 
  geom_line() + geom_point(size=1)  + ylab("Maximum fitness") + xlab("Max. lockdown time") + geom_hline(yintercept=0.995,linetype=3)
ggsave("~/Dropbox/Work/Papers/TEC/fig/split_fitness.pdf", width=3.5, height=3, scale=1) 

ggplot(lastGen(splitfit), aes(MaxLockdown,BestSoFar)) + geom_boxplot(aes(fill=UniqueTargets)) + ylab("Maximum fitness") + xlab("Max. lockdown time") + geom_hline(yintercept=0.995,linetype=3)


# evaluations
levels <- splitfit[, fitnessLevels(.SD,0.995), by=.(Job,MaxLockdown,UniqueTargets)]
levels.sum <- levels[, .(.N, Mean=mean(Evaluations), SE=se(Evaluations)), by=.(MaxLockdown,UniqueTargets)]
metaAnalysis(levels, Evaluations~MaxLockdown, ~UniqueTargets)
ggplot(levels.sum[N>=10], aes(MaxLockdown,Mean,group=UniqueTargets,colour=UniqueTargets)) + geom_line() +
  geom_point(size=1) + geom_errorbar(aes(ymin=Mean-SE, ymax=Mean+SE), width=.2) +
  ylab("Evaluations until fitness > 0.995") + xlab("Number of unique targets") + guides(colour=guide_legend(nrow=2)) + ylim(0,NA)

splithyb <- loadData("split*","hybrid.stat", auto.ids.names=c("Experiment","UniqueTargets","MaxLockdown"),fun=loadFile, colnames=hyb.cols)

stat <- splithyb[, .(NumPops = mean(NumPops)), by=.(Job,UniqueTargets,UniqueTargets,MaxLockdown)]
stat[, Ratio := NumPops / as.numeric(as.character(UniqueTargets))]
metaAnalysis(stat, NumPops~MaxLockdown, ~UniqueTargets)

ggplot(stat[,.(Mean=mean(NumPops)), by=.(UniqueTargets,MaxLockdown)], aes(MaxLockdown,Mean,group=UniqueTargets,colour=UniqueTargets)) + 
  geom_hline(aes(yintercept=factorNum(UniqueTargets),colour=UniqueTargets),linetype=3) +
  geom_line() + geom_point(size=1) + #geom_errorbar(aes(ymin=Mean-SE, ymax=Mean+SE), width=.3) +
  ylab("Mean number of pops") + xlab("Max. lockdown time") + pops_scale
ggsave("~/Dropbox/Work/Papers/TEC/fig/split_populations_mean.pdf", width=3.5, height=3, scale=1) 


splithyb[, Evaluations := floor(Evaluations / 1000) * 1000]
ggplot(splithyb[, .(NumPops=mean(NumPops)), by=.(Evaluations,UniqueTargets,MaxLockdown)], aes(Evaluations/1000,NumPops)) + 
  geom_line(aes(colour=MaxLockdown,group=MaxLockdown),size=0.2) + 
  facet_wrap(~ UniqueTargets, labeller=label_both) + 
  geom_hline(aes(yintercept=factorNum(UniqueTargets)), linetype=3) +
  pops_scale + labs(colour="Number of initial populations",y="Mean number of populations",x="Evaluations (x1000)")

### Multirover task ##############

setwd("~/exps/multirover")
mrfit <- loadData("*","postfitness.stat",fun=loadFitness, filter=bestSoFarEvaluations, auto.ids.names=c("Exp","Agents","Method","Task"), filter.par=list(step=1000))
mrfit[, Method := factor(Method,labels=c("CCEA","HybCCEA-AS","HybCCEA-TS"))]
mrfit[, Task := factor(Task,labels=c("Multirover 2 item types","Multirover 5 item types"))]
ggplot(mrfit[,.(Mean=mean(BestSoFar),SE=se(BestSoFar)),by=.(Method,Task,Evaluations)], aes(Evaluations, Mean, group=Method)) + 
  geom_line(aes(colour=Method)) + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE), alpha = 0.1) + 
  facet_wrap(~ Task, labeller=label_both) + ylab("Highest fitness")
ggsave("~/Dropbox/Work/Papers/TEC/fig/mr_fitness.pdf", width=3.5, height=3, scale=1) 

ggplot(lastGen(mrfit), aes(Task, BestSoFar, fill=Method)) + geom_boxplot() + ylab("Highest fitness") + ylim(0,30)
ggsave("~/Dropbox/Work/Papers/TEC/fig/mr_boxplot.pdf", width=3.5, height=3, scale=1) 

metaAnalysis(lastGen(mrfit), BestSoFar~Method, ~Task)

mrhyb <- loadData("*","hybrid.stat", auto.ids.names=c("Exp","Agents","Method","Task"),fun=loadFile, colnames=hyb.cols)
mrhyb[, Evaluations := floor(Evaluations / 1000) * 1000]
mrhyb[, Method := factor(Method,labels=c("HybCCEA-AS","HybCCEA-TS"))]
mrhyb[, Task := factor(Task,labels=c("Multirover 2 item types","Multirover 5 item types"))]

ggplot(mrhyb, aes(Evaluations/1000,NumPops,group=Method)) + 
  geom_smooth(aes(colour=Method,fill=Method), alpha=0.2, size=0.2, method="loess", level=0.99, span=0.05, method.args=list(degree=1)) + 
  ylab("Mean number of populations") + xlab("Evaluations (x1000)") + facet_wrap(~ Task, labeller=label_both) + scale_y_continuous(minor_breaks=NULL,breaks=1:10, limits=c(1,NA))
ggsave("~/Dropbox/Work/Papers/TEC/fig/mr_numpops.pdf", width=3.5, height=2.5, scale=1) 


metaAnalysis(mrhyb[, .(NumPops = mean(NumPops)), by=.(Job,Task,Method)], NumPops~Method, ~Task)


### Soccer task ###############

fixPostFitness("~/hpc/exps/soclong/")

socfit <- loadData("~/hpc/exps/soclong/*","postfitness.stat",fun=loadFitness, filter=bestSoFarEvaluations, filter.par=list(step=1000))
ggplot(socfit[,.(Mean=mean(BestSoFar),SE=se(BestSoFar)),by=.(Evaluations,ID9)], aes(Evaluations, Mean, group=ID9)) + 
  geom_line(aes(colour=ID9)) + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE, fill=ID9), alpha = 0.15) + ylim(0,0.85)
ggplot(lastGen(socfit), aes(ID9,BestSoFar)) + geom_boxplot() + geom_jitter() + ylim(0,1)


socfit <- loadData("~/exps/soclong/wins_5*","postfitness.stat",fun=loadFitness, filter=bestSoFarEvaluations, filter.par=list(step=1000))
ggplot(socfit[,.(Mean=mean(BestSoFar),SE=se(BestSoFar)),by=.(Evaluations,ID8)], aes(Evaluations, Mean, group=ID8)) + 
  geom_line(aes(colour=ID8)) + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE, fill=ID8), alpha = 0.15) + ylim(0,0.85)
ggplot(lastGen(socfit), aes(ID8,BestSoFar)) + geom_boxplot() + geom_jitter() + ylim(0,1)
metaAnalysis(lastGen(socfit), BestSoFar~ID8)


sochyb <- loadData("~/exps/soclong/easy_5*","hybrid.stat", fun=loadFile, colnames=hyb.cols)
sochyb[, Evaluations := floor(Evaluations/10000)*10000]
stat <- sochyb[, .(NumPops = mean(NumPops)), by=.(Job,ID8,Evaluations)]
ggplot(stat[, .(Mean=mean(NumPops),SE=se(NumPops)), by=.(Evaluations,ID8)], aes(Evaluations/1000,Mean,group=ID8)) + 
  geom_line(aes(colour=ID8)) + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE), alpha = 0.1) 
  
geom_smooth(aes(colour=Method,fill=Method), alpha=0.2, size=0.2, method="loess", level=0.99, span=0.05, method.args=list(degree=1)) + 
  ylab("Mean number of populations") + xlab("Evaluations (x1000)") + facet_wrap(~ Agents, labeller=label_both, scales="free_x") + scale_y_continuous(minor_breaks=NULL,breaks=1:10, limits=c(1,NA))


setwd("~/exps/soccer/")
socfit <- loadData("*","postfitness.stat",fun=loadFitness, filter=bestSoFarEvaluations, auto.ids.names=c("Exp","Agents","Method"), filter.par=list(step=1000))
socfit[, Method := factor(Method,labels=c("CCEA","HybCCEA-AS","HybCCEA-TS"))]
socfit[, Task := factor(paste("Soccer",Agents,"agents"))]
ggplot(socfit[,.(Mean=mean(BestSoFar),SE=se(BestSoFar)),by=.(Method,Agents,Evaluations)], aes(Evaluations, Mean, group=Method)) + 
  geom_line(aes(colour=Method)) + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE), alpha = 0.1) + 
  facet_wrap(~ Agents, labeller=label_both, scales="free_x") + ylab("Highest fitness")
ggsave("~/Dropbox/Work/Papers/TEC/fig/soc_fitness.pdf", width=3.5, height=3, scale=1) 

ggplot(lastGen(socfit), aes(Agents, BestSoFar, fill=Method)) + geom_boxplot() + ylab("Highest fitness")
ggsave("~/Dropbox/Work/Papers/TEC/fig/soc_boxplot.pdf", width=3.5, height=3, scale=1) 

metaAnalysis(lastGen(socfit), BestSoFar~Method, ~Agents)

sochyb <- loadData("*","hybrid.stat", auto.ids.names=c("Exp","Agents","Method"),fun=loadFile, colnames=hyb.cols)
sochyb[, Method := factor(Method,labels=c("HybCCEA-AS","HybCCEA-TS"))]
sochyb[, Task := factor(paste("Soccer",Agents,"agents"))]

ggplot(sochyb, aes(Evaluations/1000,NumPops,group=Method)) + 
  geom_smooth(aes(colour=Method,fill=Method), alpha=0.2, size=0.2, method="loess", level=0.99, span=0.05, method.args=list(degree=1)) + 
  ylab("Mean number of populations") + xlab("Evaluations (x1000)") + facet_wrap(~ Agents, labeller=label_both, scales="free_x") + scale_y_continuous(minor_breaks=NULL,breaks=1:10, limits=c(1,NA))
ggsave("~/Dropbox/Work/Papers/TEC/fig/soc_numpops.pdf", width=3.5, height=2.5, scale=1) 


stat <- sochyb[, .(NumPops = mean(NumPops)), by=.(Job,Agents,Method,Evaluations)]
ggplot(stat[, .(Mean=mean(NumPops),SE=se(NumPops)), by=.(Evaluations,Method,Agents)], aes(Evaluations/1000,Mean,group=Method)) + 
  geom_line(aes(colour=Method)) + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE), alpha = 0.1) + 
  ylab("Mean number of populations") + xlab("Evaluations (x1000)") + scale_y_continuous(minor_breaks=NULL,breaks=1:10) + facet_wrap(~ Agents, labeller=label_both, scales="free_x")
ggsave("~/Dropbox/Work/Papers/TEC/fig/soc_numpops.pdf", width=3.5, height=2.5, scale=1) 

metaAnalysis(sochyb[, .(NumPops = mean(NumPops)), by=.(Job,Agents,Method)], NumPops~Method, ~Agents)

### Joined

joined <- rbind(mrfit,socfit)
ggplot(lastGen(joined), aes(Task, BestSoFar, fill=Method)) + geom_boxplot(outlier.size=0.5,size=0.2) + 
  ylab("Highest fitness") + facet_wrap(~ Domain, scales="free") + xlab("Task variant") + ylim(0,NA)
ggsave("~/Dropbox/Work/Papers/TEC/fig/mrsoc_boxplot.pdf", width=3.5, height=3, scale=1) 

ggplot(joined[,.(Mean=mean(BestSoFar),SE=se(BestSoFar)),by=.(Method,Task,Evaluations)], aes(Evaluations/1000, Mean, group=Method)) + 
  geom_line(aes(colour=Method),size=0.3) + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE,fill=Method), alpha = 0.2) + 
  scale_color_brewer(palette="Set1") + scale_fill_brewer(palette="Set1") +
  facet_wrap(~ Task, scales="free") + ylab("Highest fitness") + xlab("Evaluations (x1000)")
ggsave("~/Dropbox/Work/Papers/TEC/fig2/mrsoc_lines.pdf", width=3.5, height=3.5, scale=1) 

joined <- rbind(mrhyb,sochyb)
ggplot(joined, aes(Evaluations/1000,NumPops,group=Method)) + 
  geom_smooth(aes(colour=Method,fill=Method), alpha=0.2, size=0.2, method="loess", span=0.05) + 
  scale_color_brewer(palette="Set1") + scale_fill_brewer(palette="Set1") +
  ylab("Mean number of populations") + xlab("Evaluations (x1000)") + facet_wrap(~ Task) + scale_y_continuous(minor_breaks=NULL,breaks=1:10, limits=c(1,NA))
ggsave("~/Dropbox/Work/Papers/TEC/fig2/mrsoc_pops.pdf", width=3.5, height=3.5, scale=1) 

smoothed <- joined[, .(Evaluations,NumPops=predict(loess(NumPops~Evaluations, span=0.1))) , by=.(Task,Method)]
ggplot(smoothed, aes(Evaluations/1000, NumPops,group=Method)) + 
  geom_line(aes(colour=Method), size=0.3) + 
  scale_color_brewer(palette="Set1") + scale_fill_brewer(palette="Set1") +
  ylab("Mean number of populations") + xlab("Evaluations (x1000)") + facet_wrap(~ Task, scales="free") + 
  scale_y_continuous(minor_breaks=NULL,breaks=1:10, limits=c(1,NA))
ggsave("~/Dropbox/Work/Papers/TEC/fig2/mrsoc_pops.pdf", width=3.5, height=3.5, scale=1) 

j <- joined[Task=="Multirover 2 item types" & Method=="HybCCEA-AS"]
s <- lowess(j$Evaluations,j$NumPops)
y <- predict(s,se=T)

y <- predict(loess(NumPops~Evaluations, j, span=0.01),se=T)

### TESTS / GARBAGE ############

testdists <- function(m, n, tests=1000) {
  aux <- function() {
    data <- t(replicate(m, runif(n)))
    d <- as.matrix(dist(data))
    c <- apply(d, 1, function(x){min(x[x!=0])})
    return(mean(c)) # mean distance to nearest neighbor
  }
  r <- replicate(tests, aux())
  cat("Mean:",mean(r),"SD:",sd(r))
}
