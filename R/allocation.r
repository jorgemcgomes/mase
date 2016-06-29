### Base ######

setwd("~/exps/allocationx")
hyb.cols <- c("Generation","Evaluations","NumPops",NA,NA,NA,"MeanAge","MaxAge","Merges","Splits","TotalMerges","TotalSplits",NA,"MeanDist",NA)

### Number of dimensions X Method ######

dimfit <- loadData("dimensions*","fitness.stat",jobs=0:10,fun=loadFitness,auto.ids.sep="[_/]", parallel=F, auto.ids.names=c("Experiment","Method","UniqueTargets","Dimensions"),  filter=bestSoFarEvaluations, filter.par=list(step=1000))
dimfit[, Method := factor(Method,labels=c("CCEA-Het","CCEA-Opt","HybCCEA","RandomExch","ConditionalExch"))]

ggplot(lastGen(dimfit), aes(UniqueTargets,BestSoFar)) + geom_boxplot(aes(fill=Method)) + facet_wrap(~ Dimensions)
levels <- dimfit[, fitnessLevels(.SD,0.975), by=.(Job,Method,UniqueTargets,Dimensions)]
ggplot(levels, aes(UniqueTargets,Evaluations)) + geom_boxplot(aes(fill=Method)) + facet_wrap(~ Dimensions, labeller=label_both)
ggplot(levels[, if(.N > 5) mean(Evaluations), by=.(Method,Dimensions,UniqueTargets)], aes(UniqueTargets,V1)) + geom_bar(aes(fill=Method), stat="identity",position="dodge") + facet_wrap(~ Dimensions, labeller=label_both)

dimhyb <- loadData("dimensions_2*","hybrid.stat",jobs=0:10, auto.ids.names=c("Experiment","Method","UniqueTargets","Dimensions"),fun=loadFile, colnames=hyb.cols, parallel=F)
dimhyb[, Evaluations := floor(Evaluations / 10000) * 10000]

ggplot(dimhyb[, .(NumPops=mean(NumPops)), by=.(Evaluations,UniqueTargets,Dimensions)], aes(Evaluations,NumPops)) + geom_line(aes(colour=UniqueTargets,group=UniqueTargets)) + facet_wrap(~ Dimensions, labeller=label_both) + ylim(1,10) + ylab("Mean number of populations")

min <- dimhyb[, .(NumPops=min(.SD[Evaluations > max(Evaluations)*0.9, NumPops])), by=.(Job,UniqueTargets,Dimensions)]
ggplot(min[, .(Mean=mean(NumPops),SE=se(NumPops)), by=.(UniqueTargets,Dimensions)], aes(Dimensions,Mean)) + 
  geom_bar(aes(fill=UniqueTargets), stat="identity",position="dodge") + 
  ggtitle("Minimum number of populations in the final generations")


### Number of agents X Method ######

agfit <- loadData("agents*","fitness.stat",jobs=0:10,fun=loadFitness,auto.ids.sep="[_/]", parallel=F, auto.ids.names=c("Experiment","Method","UniqueTargets","Agents"),  filter=bestSoFarEvaluations, filter.par=list(step=5000))
agfit[, Method := factor(Method,labels=c("CCEA-Het","CCEA-Opt","HybCCEA","RandomExch","ConditionalExch"))]

ggplot(lastGen(agfit), aes(UniqueTargets,BestSoFar)) + geom_boxplot(aes(fill=Method)) + facet_wrap(~ Agents, scales="free_x")
levels <- agfit[, fitnessLevels(.SD,0.975), by=.(Job,Method,UniqueTargets,Agents)]
ggplot(levels, aes(UniqueTargets,Evaluations)) + geom_boxplot(aes(fill=Method)) + facet_wrap(~ Agents, labeller=label_both, scales="free")
ggplot(levels[, if(.N > 5) mean(Evaluations), by=.(Method,Agents,UniqueTargets)], aes(UniqueTargets,V1)) + geom_bar(aes(fill=Method), stat="identity",position="dodge") + facet_wrap(~ Agents, labeller=label_both, scales="free")

aghyb <- loadData("agents_2*","hybrid.stat",jobs=0:10, auto.ids.names=c("Experiment","Method","UniqueTargets","Agents"),fun=loadFile, colnames=hyb.cols, parallel=F)
aghyb[, Evaluations := floor(Evaluations / 10000) * 10000]

ggplot(aghyb[, .(NumPops=mean(NumPops)), by=.(Evaluations,UniqueTargets,Agents)], aes(Evaluations,NumPops)) + geom_line(aes(colour=UniqueTargets,group=UniqueTargets)) + facet_wrap(~ Agents, labeller=label_both, scales="free") + ylab("Mean number of populations") + ylim(1,NA)

min <- aghyb[, .(NumPops=min(.SD[Evaluations > max(Evaluations)*0.9, NumPops])), by=.(Job,UniqueTargets,Agents)]
ggplot(min[, .(Mean=mean(NumPops),SE=se(NumPops)), by=.(UniqueTargets,Agents)], aes(Agents,Mean)) + 
  geom_bar(aes(fill=UniqueTargets), stat="identity",position="dodge") + 
  ggtitle("Minimum number of populations in the final generations")


### Hyb-CCEA initial allocations ######

inithyb <- loadData("init*","hybrid.stat",jobs=0:15, auto.ids.names=c("Experiment","UniqueTargets","InitialPops"),fun=loadFile, colnames=hyb.cols, parallel=F)
inithyb[, Evaluations := floor(Evaluations / 10000) * 10000]
ggplot(inithyb[, .(NumPops=mean(NumPops)), by=.(Evaluations,UniqueTargets,InitialPops)], aes(Evaluations,NumPops)) + geom_line(aes(colour=InitialPops,group=InitialPops)) + facet_wrap(~ UniqueTargets, labeller=label_both, scales="free") + ylab("Mean number of populations") + ylim(1,NA)

ggplot(inithyb, aes(Evaluations,NumPops)) + geom_smooth(aes(colour=InitialPops,group=InitialPops), se=F, size=1) + facet_wrap(~ UniqueTargets, labeller=label_both) + ylab("Mean number of populations") + ylim(1,10)


### Hyb-CCEA merge threshold ######

mergefit <- loadData("merge*","fitness.stat",jobs=0:14,fun=loadFitness,auto.ids.sep="[_/]", parallel=F, auto.ids.names=c("Experiment","UniqueTargets","Dispersion","MergeThreshold"),  filter=bestSoFarEvaluations, filter.par=list(step=1000))
ggplot(lastGen(mergefit), aes(MergeThreshold,BestSoFar)) + geom_boxplot() + facet_wrap(~ UniqueTargets + Dispersion)

levels <- mergefit[, fitnessLevels(.SD,0.98), by=.(Job,MergeThreshold,UniqueTargets,Dispersion)]
ggplot(levels[, if(.N > 10) mean(Evaluations), by=.(MergeThreshold,UniqueTargets,Dispersion)], aes(paste(UniqueTargets,Dispersion),V1)) + geom_bar(aes(fill=MergeThreshold), stat="identity",position="dodge")

mergehyb <- loadData("merge*","hybrid.stat",jobs=0:14, auto.ids.names=c("Experiment","UniqueTargets","Dispersion","MergeThreshold"),fun=loadFile, colnames=hyb.cols, parallel=F)
mergehyb[, Evaluations := floor(Evaluations / 10000) * 10000]
ggplot(mergehyb[, .(NumPops=mean(NumPops)), by=.(Evaluations,UniqueTargets,Dispersion,MergeThreshold)], aes(Evaluations,NumPops)) + geom_line(aes(colour=MergeThreshold,group=MergeThreshold)) + facet_wrap(~ UniqueTargets + Dispersion, labeller=label_both) + ylab("Mean number of populations") + ylim(1,NA)

min <- mergehyb[, .(NumPops=min(.SD[Evaluations > max(Evaluations)*0.9, NumPops])), by=.(Job,UniqueTargets,Dispersion,MergeThreshold)]
ggplot(min[, .(Mean=mean(NumPops), SE=se(NumPops)), by=.(UniqueTargets,Dispersion,MergeThreshold)], aes(paste(UniqueTargets,Dispersion),Mean)) + 
  geom_bar(aes(fill=MergeThreshold), stat="identity",position="dodge") + 
  ggtitle("Minimum number of populations in the final generations")


### Hyb-CCEA split threshold ######

splitfit <- loadData("split*","fitness.stat",jobs=0:14,fun=loadFitness,auto.ids.sep="[_/]", parallel=F, auto.ids.names=c("Experiment","UniqueTargets","SplitThreshold"),  filter=bestSoFarEvaluations, filter.par=list(step=1000))
ggplot(lastGen(splitfit), aes(SplitThreshold,BestSoFar)) + geom_boxplot() + facet_wrap(~ UniqueTargets)

splithyb <- loadData("split*","hybrid.stat",jobs=0:14, auto.ids.names=c("Experiment","UniqueTargets","SplitThreshold"),fun=loadFile, colnames=hyb.cols, parallel=F)

ggplot(splithyb, aes(Evaluations,NumPops)) + geom_smooth(aes(colour=SplitThreshold,group=SplitThreshold), se=F, size=.75) + facet_wrap(~ UniqueTargets, labeller=label_both) + ylab("Mean number of populations") + ylim(1,10)

splithyb[, Evaluations := floor(Evaluations / 10000) * 10000]
ggplot(splithyb[, .(NumPops=mean(NumPops)), by=.(Evaluations,UniqueTargets,SplitThreshold)], aes(Evaluations,NumPops)) + geom_smooth(aes(colour=SplitThreshold,group=SplitThreshold)) + facet_wrap(~ UniqueTargets, labeller=label_both) + ylab("Mean number of populations") + ylim(1,NA)


