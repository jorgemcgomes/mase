setwd("~/exps/aloparameters/")
fitness <- loadData("*","fitness.stat",fun=loadFitness,auto.ids.sep="_")
ggplot(lastGen(fitness), aes(x=ID3, y=BestSoFar, fill=ID4)) + geom_boxplot() + facet_wrap(~ ID2)




f2 <- f
f2$Evaluations <- discretize(f2$Evaluations, categories=50, ordered=T)
agg <- summaryBy(BestSoFar ~ Setup + Evaluations + ID1 + ID2, subset(f2,Subpop=="Any"), FUN=c(mean,se))
ggplot(agg, aes(Evaluations,BestSoFar.mean,group=ID2)) + geom_line(aes(colour=ID2)) + 
  ylab("Fitness") + facet_wrap(~ ID1)

f2 <- f
f2$Evaluations <- round(f2$Evaluations / 1000)
agg <- aggregate(BestSoFar ~ Evaluations + Setup + Job + ID1 + ID2, f2, FUN=mean)
agg <- summaryBy(BestSoFar ~ Setup + Evaluations + ID1 + ID2, agg, FUN=c(mean,se))
ggplot(agg, aes(Evaluations,BestSoFar.mean,group=ID2)) + geom_line(aes(colour=ID2)) + 
  ylab("Fitness") + facet_wrap(~ ID1)
fitnessBoxplots(f2)

ggplot(lastGen(f2), aes(ID2, BestSoFar,fill=ID2)) + geom_boxplot() + facet_wrap(~ ID1)



setwd("labmag/exps/alo2")
f <- loadData("merge*","fitness.stat",fun=loadFitness, auto.ids.sep="_")

### FITNESS

f2 <- f
f2$Evaluations <- round(f2$Evaluations / 10000)
agg <- summaryBy(BestSoFar ~ Setup + Job + Evaluations, f2, FUN=max, id=~ID2+ID3, keep.names=T)
agg <- summaryBy(BestSoFar ~ Setup + Evaluations, id=~ID2+ID3, agg, FUN=c(mean,se))
ggplot(agg, aes(Evaluations,BestSoFar.mean,group=ID3)) + geom_line(aes(colour=ID3)) + 
  xlab("Evaluations (x10000)") + ylab("Fitness") + facet_wrap(~ ID2) +
  geom_ribbon(aes(ymax = BestSoFar.mean + BestSoFar.se, ymin = BestSoFar.mean - BestSoFar.se), alpha = 0.1)
ggplot(lastGen(f), aes(x=ID3, y=BestSoFar,fill=ID3)) + geom_boxplot() +
  geom_point(position=position_jitterdodge(jitter.width=0.3, jitter.height=0), colour="gray") + facet_wrap(~ ID2)

ccea <- loadData("../alo2/ccea*","fitness.stat",fun=loadFitness, auto.ids.sep="_")
fitnessBoxplots(ccea)
bestSoFarFitness(ccea)

### TOTAL NUMBER OF MERGES AND SPLITS

hybrid <- loadData("merge*","hybrid.stat",auto.ids.sep="_",fun=loadFile, 
                   colnames=c("Generation","NumPops","MinInPop","MeanInPop","MaxInPop","NumForeigns","EvaluatedOwn","EvaluatedForeign","EvaluatedAll",
                              "MeanAge","MaxAge","MeanDisp","MeanDistOthers","Merges","Splits","Remerges",
                              "TotalMerges","TotalSplits","TotalRemerges"))

agg <- summaryBy(NumPops ~ Generation + Setup, id=~ID2+ID3, hybrid, FUN=mean, keep.names=T)
ggplot(agg, aes(Generation,NumPops,group=ID3)) + geom_line(aes(colour=ID3)) + 
  xlab("Generations") + ylab("Fitness") + facet_wrap(~ ID2)

d <- subset(lastGen(hybrid), select=c("ID2","ID3","Job","TotalSplits","TotalMerges"))
d.melt <- melt(d, measure.vars=c("TotalSplits","TotalMerges"))
d.sum <- summaryBy(value ~ ID2 + ID3 + variable, d.melt, FUN=c(mean,se))
ggplot(d.sum, aes(ID3, value.mean, fill=variable)) + geom_bar(position=position_dodge(), stat="identity") +
  geom_errorbar(aes(ymin=value.mean-value.se, ymax=value.mean+value.se), width=.2, position=position_dodge(.9)) +
  facet_wrap(~ ID2)

### FITNESS DECAY AFTER MERGE/SPLIT

fitnessDecay <- function(job, lookAhead=c(1,5,10)) {
  res <- list()
  for(d in lookAhead) {
    diff <- c(job$BestGen[-1:-d],rep(NA,d)) - job$BestGen
    res[[paste0("Baseline",d)]] <- mean(diff, na.rm=T)
  }
  for(d in lookAhead) {
    diff <- c(job$BestGen[-1:-d],rep(NA,d)) - job$BestGen
    merges <- diff[which(job$Merges > 0)]
    res[[paste0("Merge",d)]] <- mean(merges, na.rm=T)
  }
  for(d in lookAhead) {
    diff <- c(job$BestGen[-1:-d],rep(NA,d)) - job$BestGen
    splits <- diff[which(job$Splits > 0)]
    res[[paste0("Split",d)]] <- mean(splits, na.rm=T)
  }
  return(as.data.table(res))
}

hybfit <- merge(f, hybrid, by=c("ID1","ID2","ID3","Setup","Job","Generation"))
hybfit <- hybfit[, .(ID2,ID3,Job,Generation,BestGen,Mean,Merges,Splits)]

decays <- as.data.table(ddply(hybfit, .(ID2,ID3,Job), fitnessDecay))
decays.melt <- melt(decays)
decays.sum <- summaryBy(value~ID2+ID3+variable, na.omit(decays.melt), FUN=c(mean,se))

ggplot(decays.sum, aes(ID3, value.mean, fill=variable)) + geom_bar(position=position_dodge(), stat="identity") +
  geom_errorbar(aes(ymin=value.mean-value.se, ymax=value.mean+value.se), width=.2, position=position_dodge(.9)) +
  facet_wrap(~ ID2) + xlab("Merge threshold") + ylab("Fitness difference after the operation")


##### merge parameters #####################

fitness <- loadData("merge*","fitness.stat",fun=loadFitness,auto.ids.sep="_",jobs=0:14, parallel=T)
hybrid <- loadData("merge*","hybrid.stat",auto.ids.sep="_",fun=loadFile, 
                   colnames=c("Generation","NumPops",NA,NA,NA,NA,NA,NA,"Evaluations",
                              "MeanAge",NA,NA,"MeanDistOthers","Merges","Splits",NA,
                              "TotalMerges","TotalSplits",NA), jobs=0:14, parallel=T)

# best so far fitness
f2 <- fitness
f2$Evaluations <- round(f2$Evaluations / 10000)
agg <- summaryBy(BestSoFar ~ Setup + Job + Evaluations, f2, FUN=max, id=~ID2+ID3+ID4, keep.names=T)
agg <- summaryBy(BestSoFar ~ Setup + Evaluations, id=~ID2+ID3+ID4, agg, FUN=c(mean,se))
ggplot(agg, aes(Evaluations,BestSoFar.mean,group=ID4)) + geom_line(aes(colour=ID4)) + 
  xlab("Evaluations (x10000)") + ylab("Fitness") + facet_grid(ID2 ~ ID3) +
  geom_ribbon(aes(ymax = BestSoFar.mean + BestSoFar.se, ymin = BestSoFar.mean - BestSoFar.se), alpha = 0.1)


# best fitness
ggplot(lastGen(fitness), aes(x=ID3, y=BestSoFar,fill=ID4)) + geom_boxplot() + facet_wrap(~ ID2) +
  xlab("Merge threshold") + ylab("Best fitness")

# rank
agg <- summaryBy(BestSoFar ~ ID3+ID4, lastGen(fitness), FUN=c(mean,se))
agg$Setup <- paste0("M_",agg$ID3,"_E_",agg$ID4)
agg <- transform(agg, Setup = reorder(Setup, BestSoFar.mean))
ggplot(agg, aes(x=Setup, y=BestSoFar.mean, fill=Setup)) + 
  geom_bar(stat="identity") + geom_errorbar(aes(ymin=BestSoFar.mean-BestSoFar.se, ymax=BestSoFar.mean+BestSoFar.se),width=0.5) +
  theme(legend.position="none", axis.text.x = element_text(angle = 45, hjust = 1))


# mean number of pops
sum <- summaryBy(NumPops ~ Setup + Job, id=~ID2+ID3+ID4, hybrid, FUN=mean, keep.names=T)
sum <- summaryBy(NumPops ~ Setup, id=~ID2+ID3+ID4, sum, FUN=c(mean,se))
ggplot(sum, aes(ID3,NumPops.mean,fill=ID4)) + geom_bar(position=position_dodge(), stat="identity") + 
  geom_errorbar(aes(ymin=NumPops.mean-NumPops.se, ymax=NumPops.mean+NumPops.se), width=.2, position=position_dodge(.9)) +
  xlab("Merge threshold") + ylab("Mean number of populations") + facet_wrap(~ ID2)

# merges + splits
last <- subset(lastGen(hybrid))
last$Ops <- last$TotalSplits + last$TotalMerges
last.sum <- summaryBy(Ops ~ ID2 + ID3 + ID4 + variable, last, FUN=c(mean,se))
ggplot(last.sum, aes(ID3, Ops.mean, fill=ID4)) + geom_bar(position=position_dodge(), stat="identity") +
  geom_errorbar(aes(ymin=Ops.mean-Ops.se, ymax=Ops.mean+Ops.se), width=.2, position=position_dodge(.9)) +
  xlab("Merge threshold") + ylab("Total number of merges+splits") + facet_wrap(~ ID2)

# decay
hybfit <- merge(fitness, hybrid, by=c("ID1","ID2","ID3","ID4","Setup","Job","Generation"))
hybfit <- hybfit[, .(ID2,ID3,ID4,Job,Generation,BestGen,Mean,Merges,Splits)]

# after merge
decays <- as.data.table(ddply(hybfit, .(ID2,ID3,ID4,Job), fitnessDecay))
decays.sum <- summaryBy(Merge1~ID2+ID3+ID4, na.omit(decays), FUN=c(mean,se))
ggplot(decays.sum, aes(ID3, Merge1.mean, fill=ID4)) + geom_bar(position=position_dodge(), stat="identity") +
  geom_errorbar(aes(ymin=Merge1.mean-Merge1.se, ymax=Merge1.mean+Merge1.se), width=.2, position=position_dodge(.9)) +
  xlab("Merge threshold") + ylab("Fitness decay ONE gen after merge") + facet_wrap(~ ID2)


setwd("~/exps/alo_parameters/")
fitness <- loadData("merge*","fitness.stat",fun=loadFitness,auto.ids.sep="_",jobs=0:14, parallel=F)
fitness[, ID2 := factor(ID2,levels=c("0.1","0.25","0.50","0.75","1.00"),labels=c("T10S0.1","T10S0.25","T10S0.50","T10S0.75","T10S1"))]
ggplot(lastGen(fitness), aes(x=paste0(ID3,ID4), y=BestSoFar)) + geom_boxplot() + facet_wrap(~ ID2) + xlab("Hybrid setup") + ylab("Best fitness") +
  theme(legend.position="none", axis.text.x = element_text(angle = 45, hjust = 1))



setwd("~/exps/alo_parameters2/")
fitness <- loadData("*_*_*_*_elite_5_*","fitness.stat",fun=loadFitness,auto.ids.sep="_",parallel=F)
fitness[, ID2 := factor(ID2,levels=c("0.10","0.25","0.50","0.75","10","5","3","1"),labels=c("T10S0.1","T10S0.25","T10S0.50","T10S0.75","T10S1","T5S1","T3S1","T1S1"))]
fitness[, ID7 := factor(ID7,levels=c("0","1","2"),labels=c("5D","10D","15D"))]

ggplot(lastGen(fitness), aes(x=paste0(ID3,ID4), y=BestSoFar)) + geom_boxplot() + facet_grid(ID7 ~ ID2) + xlab("Hybrid setup") + ylab("Best fitness") +
  theme(legend.position="none", axis.text.x = element_text(angle = 45, hjust = 1))


hybrid <- loadData("*_*_*_*_elite_5_*","hybrid.stat",auto.ids.sep="_",fun=loadFile, 
                   colnames=c("Generation","NumPops",NA,NA,NA,NA,NA,NA,"Evaluations",
                              "MeanAge",NA,NA,NA,"Merges","Splits",NA,"TotalMerges","TotalSplits",NA), parallel=F)
hybrid[, ID2 := factor(ID2,levels=c("0.10","0.25","0.50","0.75","10","5","3","1"),labels=c("T10S0.1","T10S0.25","T10S0.50","T10S0.75","T10S1","T5S1","T3S1","T1S1"))]
hybrid[, ID7 := factor(ID7,levels=c("0","1","2"),labels=c("5D","10D","15D"))]
sum <- summaryBy(NumPops ~ Setup + Job, id=~ID2+ID3+ID4+ID7, hybrid, FUN=mean, keep.names=T)
ggplot(sum, aes(x=paste0(ID3,ID4), y=NumPops)) + geom_boxplot() + facet_grid(ID7 ~ ID2) + xlab("Hybrid setup") + ylab("Mean number of pops") +
  theme(legend.position="none", axis.text.x = element_text(angle = 45, hjust = 1))


fitnessLevelAchieved <- function(data, threshold) {
  w <- which(data$BestSoFar > threshold)
  if(length(w) > threshold) {
    return(data[min(w),])
  }
  return(NULL)
}
l <- ddply(fitness,.(Setup,Job), fitnessLevelAchieved, 0.99, .progress="text")
ggplot(l, aes(x=paste0(ID3,ID4), y=Evaluations)) + geom_boxplot() + facet_grid(ID7 ~ ID2) + xlab("Hybrid setup") + ylab("Evaluations to threshold") +
  theme(legend.position="none", axis.text.x = element_text(angle = 45, hjust = 1))


setwd("~/exps/alo_parameters2/")
fitness <- loadData("*_*_1.25_0.20_elite_*_*","fitness.stat",fun=loadFitness,auto.ids.sep="_",parallel=F)
fitness[, ID2 := factor(ID2,levels=c("0.10","0.25","0.50","0.75","10","5","3","1"),labels=c("T10S0.1","T10S0.25","T10S0.50","T10S0.75","T10S1","T5S1","T3S1","T1S1"))]
fitness[, ID7 := factor(ID7,levels=c("0","1","2"),labels=c("5D","10D","15D"))]

ggplot(lastGen(fitness), aes(x=ID6, y=BestSoFar)) + geom_boxplot() + facet_grid(ID7 ~ ID2) + xlab("Hybrid setup") + ylab("Mean number of pops") +
  theme(legend.position="none", axis.text.x = element_text(angle = 45, hjust = 1))
l <- ddply(fitness,.(Setup,Job), fitnessLevelAchieved, 0.98, .progress="text")
ggplot(l, aes(x=ID6, y=Evaluations)) + geom_boxplot() + facet_grid(ID7 ~ ID2) + xlab("Hybrid setup") + ylab("Evaluations to threshold") +
  theme(legend.position="none", axis.text.x = element_text(angle = 45, hjust = 1))


setwd("~/exps/aloparameters3/")
fitness <- loadData("*","fitness.stat",fun=loadFitness,auto.ids.sep="_",parallel=F)
fitness[, ID2 := factor(ID2,levels=c("0.10","0.25","0.50","0.75","10","5","3","1"),labels=c("T10S0.1","T10S0.25","T10S0.50","T10S0.75","T10S1","T5S1","T3S1","T1S1"))]
fitness[, ID3 := factor(ID3,labels=c("5/1.7","10/3.3","20/6.7","30/10"))]
fitness[, ID4 := factor(ID4,levels=c("0","1","2"),labels=c("5D","10D","15D"))]

ggplot(lastGen(fitness), aes(x=ID3, y=BestSoFar)) + geom_boxplot() + facet_grid(ID4 ~ ID2) + xlab("Hybrid setup") + ylab("Best fitness") +
  theme(legend.position="none", axis.text.x = element_text(angle = 45, hjust = 1))

l <- ddply(fitness,.(Setup,Job), fitnessLevelAchieved, 0.98, .progress="text")
ggplot(l, aes(x=ID3, y=Evaluations)) + geom_boxplot() + facet_grid(ID4 ~ ID2) + xlab("Hybrid setup") + ylab("Evaluations to threshold") +
  theme(legend.position="none", axis.text.x = element_text(angle = 45, hjust = 1))


setwd("~/exps/alo_parameters2/")
fitness <- loadData("*_*_1.25_0.20_*_5_*","fitness.stat",fun=loadFitness,auto.ids.sep="_",parallel=F)
fitness[, ID2 := factor(ID2,levels=c("0.10","0.25","0.50","0.75","10","5","3","1"),labels=c("T10S0.1","T10S0.25","T10S0.50","T10S0.75","T10S1","T5S1","T3S1","T1S1"))]
fitness[, ID7 := factor(ID7,levels=c("0","1","2"),labels=c("5D","10D","15D"))]

ggplot(lastGen(fitness), aes(x=ID5, y=BestSoFar)) + geom_boxplot() + facet_grid(ID7 ~ ID2) + xlab("Hybrid setup") + ylab("Merge mode") +
  theme(legend.position="none", axis.text.x = element_text(angle = 45, hjust = 1))
l <- ddply(fitness,.(Setup,Job), fitnessLevelAchieved, 0.98, .progress="text")
ggplot(l, aes(x=ID5, y=Evaluations)) + geom_boxplot() + facet_grid(ID7 ~ ID2) + xlab("Hybrid setup") + ylab("Evaluations to threshold") +
  theme(legend.position="none", axis.text.x = element_text(angle = 45, hjust = 1))

metaAnalysis(lastGen(l), BestSoFar ~ Setup, ~ ID7 + ID2)


setwd("~/exps/alo_parameters4/")
fitness <- loadData("*","fitness.stat",fun=loadFitness,auto.ids.sep="_",parallel=F)
fitness[, ID2 := factor(ID2,levels=c("0.10","0.25","0.50","0.75","10","5","3","1"),labels=c("T10S0.1","T10S0.25","T10S0.50","T10S0.75","T10S1","T5S1","T3S1","T1S1"))]
fitness[, ID5 := factor(ID5,levels=c("0","1","2"),labels=c("5D","10D","15D"))]
ggplot(lastGen(fitness), aes(x=ID3, y=BestSoFar, fill=ID4)) + geom_boxplot() + facet_grid(ID5 ~ ID2) + xlab("Hybrid setup") + ylab("Merge mode") +
  theme(legend.position="none", axis.text.x = element_text(angle = 45, hjust = 1))

l <- ddply(fitness,.(Setup,Job), fitnessLevelAchieved, 0.98, .progress="text")
ggplot(l, aes(x=ID3, y=Evaluations, fill=ID4)) + geom_boxplot() + facet_grid(ID5 ~ ID2) + xlab("Hybrid setup") + ylab("Merge mode") +
  theme(legend.position="none", axis.text.x = element_text(angle = 45, hjust = 1))

hybrid <- loadData("*","hybrid.stat",auto.ids.sep="_",fun=loadFile, 
                   colnames=c("Generation","NumPops",NA,NA,NA,NA,NA,NA,"Evaluations",
                              "MeanAge",NA,NA,NA,"Merges","Splits",NA,"TotalMerges","TotalSplits",NA), parallel=F)
hybrid[, ID2 := factor(ID2,levels=c("0.10","0.25","0.50","0.75","10","5","3","1"),labels=c("T10S0.1","T10S0.25","T10S0.50","T10S0.75","T10S1","T5S1","T3S1","T1S1"))]
hybrid[, ID5 := factor(ID5,levels=c("0","1","2"),labels=c("5D","10D","15D"))]
sum <- summaryBy(NumPops ~ Setup + Job, id=~ID2+ID3+ID4+ID5, hybrid, FUN=mean, keep.names=T)
ggplot(sum, aes(x=ID3, y=NumPops, fill=ID4)) + geom_boxplot() + facet_grid(ID5 ~ ID2) + xlab("Hybrid setup") + ylab("Num pops") +
  theme(legend.position="none", axis.text.x = element_text(angle = 45, hjust = 1))



### MERGE DISTANCE AND THRESHOLD ###############################

setwd("~/exps/alo_parameters5/")
fitness <- loadData("*","fitness.stat",fun=loadFitness,auto.ids.sep="_",parallel=F)
fitness[, ID2 := factor(ID2,levels=c("0.10","0.25","0.50","0.75","10","5","3","1"),labels=c("T10S0.1","T10S0.25","T10S0.50","T10S0.75","T10S1","T5S1","T3S1","T1S1"))]
fitness[, ID4 := factor(ID4,levels=c("0","1","2"),labels=c("Weighted","Elite20","Flat"))]

ggplot(lastGen(fitness), aes(x=ID3, y=BestSoFar, fill=ID4)) + geom_boxplot() + facet_wrap(~ ID2)

l <- ddply(fitness,.(Setup,Job), fitnessLevelAchieved, 0.98, .progress="text")
ggplot(l, aes(x=ID3, y=Evaluations, fill=ID4)) + geom_boxplot() + facet_wrap(~ ID2, scales="free_y")

hybrid <- loadData("*","hybrid.stat",auto.ids.sep="_",fun=loadFile, colnames=c("Generation","Evaluations","NumPops",NA,NA,NA,"MeanAge","MaxAge","Merges","Splits","TotalMerges","TotalSplits",NA,"MeanDist",NA), parallel=F)
hybrid[, ID2 := factor(ID2,levels=c("0.10","0.25","0.50","0.75","10","5","3","1"),labels=c("T10S0.1","T10S0.25","T10S0.50","T10S0.75","T10S1","T5S1","T3S1","T1S1"))]
hybrid[, ID4 := factor(ID4,levels=c("0","1","2"),labels=c("Weighted","Elite20","Flat"))]
sum <- summaryBy(NumPops ~ Setup + Job, id=~ID2+ID3+ID4, hybrid, FUN=mean, keep.names=T)
ggplot(sum, aes(x=ID3, y=NumPops, fill=ID4)) + geom_boxplot() + facet_wrap(~ ID2)


hybfit <- merge(fitness, hybrid, by=c("ID1","ID2","ID3","ID4","Setup","Job","Generation"))
hybfit <- hybfit[, .(ID2,ID3,ID4,Job,Generation,BestGen,Merges,Splits)]

decays <- ddply(hybfit, .(ID2,ID3,ID4,Job), fitnessDecay, 1, .progress="text")
setDT(decays)
decays.melt <- melt(na.omit(decays))
decays.sum <- summaryBy(value~ID2+ID3+ID4+variable, decays.melt, FUN=c(mean,se))

ggplot(subset(decays.sum,variable=="Split1"), aes(ID3, value.mean, fill=ID4)) + geom_bar(position=position_dodge(), stat="identity") +
  geom_errorbar(aes(ymin=value.mean-value.se, ymax=value.mean+value.se), width=.2, position=position_dodge(.9)) +
  facet_wrap(~ ID2, scales="free_y") + xlab("Merge threshold") + ylab("Fitness difference after the operation")



### MAX LOCKDOWN PARAMETER #####################################

setwd("~/exps/alo_parameters6/")
fitness <- loadData("*","fitness.stat",fun=loadFitness,auto.ids.sep="_",parallel=F)
fitness[, ID2 := factor(ID2,levels=c("0.10","0.25","0.50","0.75","10","5","3","1"),labels=c("T10S0.1","T10S0.25","T10S0.50","T10S0.75","T10S1","T5S1","T3S1","T1S1"))]
fitness[, ID3 := factor(ID3,levels=c("1","5","10","20","35","50"))]
fitness[, ID4 := factor(ID4,levels=c("0","1","2"),labels=c("5D","10D","15D"))]

ggplot(lastGen(fitness), aes(x=ID3, y=BestSoFar, fill=ID4)) + geom_boxplot() + facet_wrap(~ ID2)

l <- ddply(fitness,.(Setup,Job), fitnessLevelAchieved, 0.98, .progress="text")
sum <- summaryBy(Evaluations ~ Setup, id=~ID2+ID3+ID4, l, FUN=c(mean,se))
pd <- position_dodge(0)
ggplot(sum, aes(x=ID3, y=Evaluations.mean, colour=ID4, group=ID4)) +
  geom_errorbar(aes(ymin=Evaluations.mean-Evaluations.se, ymax=Evaluations.mean+Evaluations.se), width=.5, position=pd) +
  geom_line(position=pd) + geom_point(position=pd) + facet_wrap(~ ID2, scales="free_y") + xlab("Max lockdown")

hybrid <- loadData("*","hybrid.stat",auto.ids.sep="_",fun=loadFile, colnames=c("Generation","Evaluations","NumPops",NA,NA,NA,"MeanAge","MaxAge","Merges","Splits","TotalMerges","TotalSplits",NA,"MeanDist",NA), parallel=F)
hybrid[, ID2 := factor(ID2,levels=c("0.10","0.25","0.50","0.75","10","5","3","1"),labels=c("T10S0.1","T10S0.25","T10S0.50","T10S0.75","T10S1","T5S1","T3S1","T1S1"))]
hybrid[, ID3 := factor(ID3,levels=c("1","5","10","20","35","50"))]
hybrid[, ID4 := factor(ID4,levels=c("0","1","2"),labels=c("5D","10D","15D"))]

sum <- summaryBy(NumPops ~ Setup + Job, id=~ID2+ID3+ID4, hybrid, FUN=mean, keep.names=T)
sum <- summaryBy(NumPops ~ Setup, id=~ID2+ID3+ID4, sum, FUN=c(mean,se))
pd <- position_dodge(0)
ggplot(sum, aes(x=ID3, y=NumPops.mean, colour=ID4, group=ID4)) +
  geom_errorbar(aes(ymin=NumPops.mean-NumPops.se, ymax=NumPops.mean+NumPops.se), width=.5, position=pd) +
  geom_line(position=pd) + geom_point(position=pd) + facet_wrap(~ ID2) + xlab("Max lockdown")




### CHANGE CHECK ##################

fitness <- loadData("~/exps/aloparameters5/*_0.2_1","fitness.stat",fun=loadFitness,auto.ids.sep="_",parallel=F)


fitness <- loadData("~/exps/aloparameters7/*_0.2_0","fitness.stat",fun=loadFitness,auto.ids.sep="_",parallel=F)

fitness[, ID2 := factor(ID2,levels=c("0.10","0.25","0.50","0.75","10","5","3","1"),labels=c("T10S0.1","T10S0.25","T10S0.50","T10S0.75","T10S1","T5S1","T3S1","T1S1"))]
ggplot(lastGen(fitness), aes(x=ID2, y=BestSoFar)) + geom_boxplot()
l <- ddply(fitness,.(Setup,Job), fitnessLevelAchieved, 0.98, .progress="text")
ggplot(l, aes(x=ID2, y=Evaluations/1000)) + geom_boxplot() + ylim(0,1000)



### FINAL PARAMS #################

fitnessLevelAchieved <- function(data, threshold) {
  w <- which(data$BestSoFar > threshold)
  if(length(w) > threshold) {
    return(data[min(w)])
  } else {
    return(tail(data,1))
  }
}

setwd("~/exps/aloparamsfinal/")
fitness <- loadData("*","fitness.stat",fun=loadFitness,auto.ids.sep="_", parallel=T, filter=tail,filter.par=list(n=1))
fitness[, ID2 := factor(ID2,levels=c("0.25","0.50","0.75","10","5","3","1"),labels=c("T10S0.25","T10S0.50","T10S0.75","T10S1","T5S1","T3S1","T1S1"))]
fitness[, ID3 := factor(ID3,levels=c("5","10","20","30"))]
fitness[, ID5 := factor(ID5,levels=c("1","5","10","20","35","50"))]

sum <- fitness[, .(BestMean=mean(BestSoFar)), by=.(ID2,ID3,ID4,ID5)]
ggplot(sum, aes(ID4,ID5)) + geom_tile(aes(fill = BestMean), colour="white") + 
  scale_fill_distiller(type="seq", palette="Reds", direction=1, na.value="white") +
  facet_grid(ID3 ~ ID2) + xlab("Merge threshold") + ylab("Max lockdown") 

ggplot(fitness, aes(ID4,BestSoFar)) + geom_boxplot(aes(fill = ID5)) + facet_grid(ID3 ~ ID2) 

ggplot(fitness[ID4=="0.15"], aes(ID5,BestSoFar)) + geom_boxplot(aes(fill = ID3)) + facet_wrap(~ ID2) 


agg <- fitness[, .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(ID4,ID5)]
agg[, Setup := reorder(paste(ID4,ID5),Mean)]

agg <- fitness[, .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(ID4)]
agg[, Setup := reorder(ID4,Mean)]

agg <- fitness[, .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(ID5)]
agg[, Setup := reorder(ID5,Mean)]

ggplot(agg, aes(x=Setup, y=Mean)) + geom_bar(stat="identity") + 
  geom_errorbar(aes(ymin=Mean-SE, ymax=Mean+SE),width=0.5) +
  theme(legend.position="none", axis.text.x = element_text(angle = 45, hjust = 1))

evals <- loadData("*","fitness.stat",fun=loadFitness,auto.ids.sep="_", parallel=T, filter=fitnessLevelAchieved,filter.par=list(0.9))
evals[, ID2 := factor(ID2,levels=c("0.25","0.50","0.75","10","5","3","1"),labels=c("T10S0.25","T10S0.50","T10S0.75","T10S1","T5S1","T3S1","T1S1"))]
evals[, ID3 := factor(ID3,levels=c("5","10","20","30"))]
evals[, ID5 := factor(ID5,levels=c("1","5","10","20","35","50"))]

sum <- evals[, .(EvalMean=mean(Evaluations)), by=.(ID2,ID3,ID4,ID5)]
ggplot(sum, aes(ID4,ID5)) + geom_tile(aes(fill = EvalMean), colour="white") + 
  scale_fill_distiller(type="seq", palette="Reds", direction=1, na.value="white") +
  facet_grid(ID3 ~ ID2) + xlab("Merge threshold") + ylab("Max lockdown") 

ggplot(evals, aes(ID4,Evaluations)) + geom_bar(aes(fill = ID5), stat="identity",position="dodge") + facet_grid(ID3 ~ ID2) 


hyb.cols <- c("Generation","Evaluations","NumPops",NA,NA,NA,"MeanAge","MaxAge","Merges","Splits","TotalMerges","TotalSplits",NA,"MeanDist",NA)
hybrid <- loadData("*","hybrid.stat",auto.ids.sep="_",fun=loadFile, colnames=hyb.cols, filter=function(df) {df[, .(NumPops = mean(NumPops))]}, parallel=T)
hybrid[, ID2 := factor(ID2,levels=c("0.25","0.50","0.75","10","5","3","1"),labels=c("T10S0.25","T10S0.50","T10S0.75","T10S1","T5S1","T3S1","T1S1"))]
hybrid[, ID3 := factor(ID3,levels=c("5","10","20","30"))]
hybrid[, ID5 := factor(ID5,levels=c("1","5","10","20","35","50"))]

sum <- hybrid[, .(NumPopsMean=mean(NumPops)), by=.(ID2,ID3,ID4,ID5)]
ggplot(sum, aes(ID4,ID5)) + geom_tile(aes(fill = NumPopsMean), colour="white") + 
  scale_fill_distiller(type="seq", palette="Reds", direction=1, na.value="white") +
  facet_grid(ID3 ~ ID2) + xlab("Merge threshold") + ylab("Max lockdown") 


setwd("~/exps/aloparamsfinalelite/")
fitness <- loadData("*","fitness.stat",fun=loadFitness,auto.ids.sep="_", parallel=F, filter=tail,filter.par=list(n=1))
fitness[, ID2 := factor(ID2,levels=c("0.25","0.50","0.75","10","5","3","1"),labels=c("T10S0.25","T10S0.50","T10S0.75","T10S1","T5S1","T3S1","T1S1"))]

sum <- fitness[, .(BestMean=mean(BestSoFar)), by=.(ID2,ID4,ID6)]
ggplot(sum, aes(ID4,ID6)) + geom_tile(aes(fill = BestMean), colour="white") + 
  scale_fill_distiller(type="seq", palette="Reds", direction=1, na.value="white") +
  facet_wrap(~ ID2)

hybrid <- loadData("*","hybrid.stat",auto.ids.sep="_",fun=loadFile, colnames=hyb.cols, filter=function(df) {df[, .(NumPops = mean(NumPops))]}, parallel=T)
hybrid[, ID2 := factor(ID2,levels=c("0.25","0.50","0.75","10","5","3","1"),labels=c("T10S0.25","T10S0.50","T10S0.75","T10S1","T5S1","T3S1","T1S1"))]
sum <- hybrid[, .(NumPopsMean=mean(NumPops)), by=.(ID2,ID4,ID6)]
ggplot(sum, aes(ID4,ID6)) + geom_tile(aes(fill = NumPopsMean), colour="white") + 
  scale_fill_distiller(type="seq", palette="Reds", direction=1, na.value="white") +
  facet_wrap(~ ID2) + xlab("Merge threshold") + ylab("Max lockdown") 

evals <- loadData("*","fitness.stat",fun=loadFitness,auto.ids.sep="_", parallel=T, filter=fitnessLevelAchieved,filter.par=list(0.95))
evals[, ID2 := factor(ID2,levels=c("0.25","0.50","0.75","10","5","3","1"),labels=c("T10S0.25","T10S0.50","T10S0.75","T10S1","T5S1","T3S1","T1S1"))]
sum <- evals[, .(EvalMean=mean(Evaluations)), by=.(ID2,ID4,ID6)]
ggplot(sum, aes(ID4,ID6)) + geom_tile(aes(fill = EvalMean), colour="white") + 
  scale_fill_distiller(type="seq", palette="Reds", direction=1, na.value="white") +
  facet_wrap(~ ID2) + xlab("Merge threshold") + ylab("Max lockdown") 



setwd("~/exps/allocation")

fitness <- loadData(c("random/*","biased/*","conditional/*"),"fitness.stat",fun=loadFitness,auto.ids.sep="[_/]", parallel=F, filter=tail,filter.par=list(n=1))
ggplot(fitness[, .(BestMean=mean(BestSoFar)), by=.(ID1,ID3,ID4,ID5)], aes(ID5,BestMean)) + geom_line(aes(colour=ID1,group=ID1)) + facet_grid(ID4 ~ ID3)

fitness <- loadData(c("ccea/*","cceahyb/*","random/*_0.25","biased/*_0.25","conditional/*_0.25","hybrid/clusters_*_0.15_20"),"fitness.stat",fun=loadFitness,auto.ids.sep="[_/]", parallel=F, filter=bestSoFarEvaluations)

ggplot(lastGen(fitness)[, .(BestMean=mean(BestSoFar)), by=.(ID1,ID3,ID4)], aes(ID1,BestMean)) + geom_boxplot() + facet_grid(ID3 ~ ID4)

agg <- fitness[, .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(Setup,ID1,ID3,ID4,Evaluations)]
ggplot(agg, aes(Evaluations,Mean,group=ID1)) + geom_line(aes(colour=ID1)) + facet_grid(ID3 ~ ID4, labeller=label_both)


setwd("~/exps/allocation2")
fitness <- loadData("**/*","fitness.stat",fun=loadFitness,auto.ids.sep="[_/]", parallel=F, filter=bestSoFarEvaluations, filter.par=list(step=1000))
ggplot(lastGen(fitness)[, .(BestMean=mean(BestSoFar)), by=.(ID1,ID3,ID4)], aes(ID1,BestMean)) + geom_boxplot() + facet_grid(ID3 ~ ID4)
agg <- fitness[, .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(Setup,ID1,ID3,ID4,Evaluations)]
ggplot(agg, aes(Evaluations,Mean,group=ID1)) + geom_line(aes(colour=ID1)) + facet_grid(ID3 ~ ID4, labeller=label_both, scales="free")
ggplot(agg, aes(Evaluations,Mean,group=ID3)) + geom_line(aes(colour=ID3)) + facet_grid(ID4 ~ ID1, labeller=label_both, scales="free")

hybrid <- loadData("hybrid/*","hybrid.stat",auto.ids.sep="[_/]",fun=loadFile, colnames=hyb.cols, parallel=F)
ggplot(hybrid[, .(MeanPops=mean(NumPops)), by=.(ID3,ID4)], aes(ID3,MeanPops)) + geom_bar(stat="identity") + facet_wrap(~ ID4, scales="free")


fitness <- loadData("comparison/**/*","fitness.stat",fun=loadFitness,auto.ids.sep="[_/]", parallel=F, filter=bestSoFarEvaluations, filter.par=list(step=1000))
levels <- fitness[, fitnessLevels(.SD,seq(0.95,1,0.01)), by=.(ID2,ID4,ID5,Job)]
levels[which(is.infinite(Evaluations)), Evaluations := NA]

agg <- levels[, .(Mean=mean(Evaluations,na.rm=T)), by=.(ID2,ID4,ID5,Threshold)]
ggplot(agg, aes(Threshold,Mean)) + geom_bar(aes(fill=ID2),stat="identity",position="dodge") + facet_grid(ID4 ~ ID5, scales="free_y")



fitness <- loadData("pops/**/*","fitness.stat",jobs=0:22,fun=loadFitness,auto.ids.sep="[_/]", parallel=F, filter=bestSoFarEvaluations, filter.par=list(step=10000))
agg <- fitness[, .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(Setup,ID2,ID4,ID5,Evaluations)]
ggplot(agg, aes(Evaluations,Mean,group=ID2)) + geom_line(aes(colour=ID2)) + facet_wrap(~ ID5 + ID4, labeller=label_both, scales="free_x")

fitness <- loadData("init/**/*","fitness.stat",jobs=0:22,fun=loadFitness,auto.ids.sep="[_/]", parallel=F, filter=bestSoFarEvaluations, filter.par=list(step=10000))
agg <- fitness[, .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(Setup,ID4,ID5,Evaluations)]
ggplot(agg, aes(Evaluations,Mean,group=ID5)) + geom_line(aes(colour=ID5)) + facet_wrap(~ ID4, labeller=label_both)



fitnessLevels <- function(data, thresholds) {
  aux <- function(t) {
    w <- which(data[,BestSoFar] > t)
    return(if(length(w) > 0) data$Evaluations[min(w)] else Inf)
  }
  evals <- sapply(thresholds, aux)
  return(data.table(Threshold=thresholds,Evaluations=evals))
}

levels <- fitness[, fitnessLevels(.SD,seq(0.95,1,0.01)), by=.(ID2,ID4,ID5,Job)]
levels[which(is.infinite(Evaluations)), Evaluations := NA]

agg <- levels[, .(Mean=mean(Evaluations,na.rm=T)), by=.(ID2,ID4,ID5,Threshold)]
ggplot(agg, aes(Threshold,Mean)) + geom_bar(aes(fill=ID2),stat="identity",position="dodge") + facet_grid(ID4 ~ ID5, scales="free_y")

setwd("~/labmag/exps/herding/")
fitness <- loadData("*","fitness.stat",fun=loadFitness,auto.ids.sep="_", parallel=F, filter=bestSoFarEvaluations, filter.par=list(step=1000))
agg <- fitness[, .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(ID1,ID2,Evaluations)]
ggplot(agg, aes(Evaluations,Mean,group=ID1)) + geom_line(aes(colour=ID1)) + facet_wrap(~ ID2, scales="free", ncol=2)

ggplot(lastGen(fitness), aes(ID1,BestSoFar)) + geom_boxplot() + facet_wrap(~ ID2, scales="free",ncol=2)

hybrid <- loadData("hyb*","hybrid.stat",auto.ids.sep="_",fun=loadFile, colnames=hyb.cols, parallel=F)
ggplot(hybrid[, .(MeanPops=mean(NumPops)), by=.(ID1,ID2)], aes(ID1,MeanPops)) + geom_bar(stat="identity") + facet_wrap(~ ID2, scales="free")
ggplot(hybrid[, .(MeanDist=mean(MeanDist)), by=.(ID1,ID2)], aes(ID1,MeanDist)) + geom_bar(stat="identity") + facet_wrap(~ ID2, scales="free")
ggplot(lastGen(hybrid)[, .(MeanTotalMerges=mean(TotalMerges)), by=.(ID1,ID2)], aes(ID1,MeanTotalMerges)) + geom_bar(stat="identity") + facet_wrap(~ ID2, scales="free")
ggplot(lastGen(hybrid)[, .(MeanTotalSplits=mean(TotalSplits)), by=.(ID1,ID2)], aes(ID1,MeanTotalSplits)) + geom_bar(stat="identity") + facet_wrap(~ ID2, scales="free")



# DRAFT

setwd("~/exps/allocation2")
hyb.cols <- c("Generation","Evaluations","NumPops",NA,NA,NA,"MeanAge","MaxAge","Merges","Splits","TotalMerges","TotalSplits",NA,"MeanDist",NA)


fitness <- loadData("comparison/hybrid/*","fitness.stat",jobs=0:22,fun=loadFitness,auto.ids.sep="[_/]", parallel=F, auto.ids.names=c("Experiment","Method","Task","UniqueTargets","Dimensions"),  filter=bestSoFarEvaluations, filter.par=list(step=1000))
ggplot(lastGen(fitness)[, .(BestMean=mean(BestSoFar)), by=.(UniqueTargets,Dimensions)], aes(Dimensions,BestMean)) + geom_bar(aes(fill=UniqueTargets), stat="identity",position="dodge")

hybrid <- loadData("comparison/hybrid/*","hybrid.stat",jobs=0:22,auto.ids.sep="[_/]", auto.ids.names=c("Experiment","Method","Task","UniqueTargets","Dimensions"),fun=loadFile, colnames=hyb.cols, parallel=F)
ggplot(hybrid[Evaluations>=900000, .(MeanPops=mean(NumPops)), by=.(UniqueTargets,Dimensions)], aes(Dimensions,MeanPops)) + geom_bar(aes(fill=UniqueTargets), stat="identity",position="dodge") + ggtitle("Mean number of populations in the final generations")

hybrid[, Evaluations := floor(Evaluations / 1000) * 1000]
ggplot(hybrid[, .(NumPops=mean(NumPops)), by=.(Evaluations,UniqueTargets,Dimensions)], aes(Evaluations,NumPops)) + geom_line(aes(colour=UniqueTargets,group=UniqueTargets)) + facet_wrap(~ Dimensions, labeller=label_both) + ylim(1,10) + ylab("Mean number of populations")


fitness <- loadData("pops/hybrid/*","fitness.stat",jobs=0:22,fun=loadFitness,auto.ids.sep="[_/]", parallel=F, auto.ids.names=c("Experiment","Method","Task","Agents","UniqueTargets"),  filter=bestSoFarEvaluations, filter.par=list(step=5000))
ggplot(lastGen(fitness)[, .(BestMean=mean(BestSoFar)), by=.(Agents,UniqueTargets)], aes(Agents,BestMean)) + geom_bar(aes(fill=UniqueTargets), stat="identity",position="dodge")

hybrid <- loadData("pops/hybrid/*","hybrid.stat",jobs=0:22,auto.ids.sep="[_/]", auto.ids.names=c("Experiment","Method","Task","Agents","UniqueTargets"),fun=loadFile, colnames=hyb.cols, parallel=F)
hybrid[, Evaluations := floor(Evaluations / 5000) * 5000]
ggplot(hybrid[, .(NumPops=mean(NumPops)), by=.(Evaluations,UniqueTargets,Agents)], aes(Evaluations,NumPops)) + geom_line(aes(colour=UniqueTargets,group=UniqueTargets)) + facet_wrap(~ Agents, labeller=label_both, scales="free") + ylab("Mean number of populations")


fitness <- loadData("init/hybrid/*","fitness.stat",jobs=0:22,fun=loadFitness,auto.ids.sep="[_/]", parallel=F, auto.ids.names=c("Experiment","Method","Task","UniqueTargets","InitialPopulations"), filter=bestSoFarEvaluations, filter.par=list(step=1000))
ggplot(lastGen(fitness)[, .(BestMean=mean(BestSoFar)), by=.(InitialPopulations,UniqueTargets)], aes(UniqueTargets,BestMean)) + geom_bar(aes(fill=InitialPopulations), stat="identity",position="dodge")

hybrid <- loadData("init/hybrid/*","hybrid.stat",jobs=0:22,auto.ids.sep="[_/]", auto.ids.names=c("Experiment","Method","Task","UniqueTargets","InitialPopulations"),fun=loadFile, colnames=hyb.cols, parallel=F)
hybrid[, Evaluations := floor(Evaluations / 1000) * 1000]
hybrid[, InitialPopulations := factor(InitialPopulations, labels=c("1","3","5","10"))]
ggplot(hybrid[, .(NumPops=mean(NumPops)), by=.(Evaluations,UniqueTargets,InitialPopulations)], aes(Evaluations,NumPops)) + geom_line(aes(colour=InitialPopulations,group=InitialPopulations)) + facet_wrap(~ UniqueTargets, labeller=label_both, scales="free") + ylab("Mean number of populations")

fitness <- loadData("comparison/**/*","fitness.stat",jobs=0:22,fun=loadFitness,auto.ids.sep="[_/]", parallel=F, auto.ids.names=c("Experiment","Method","Task","UniqueTargets","Dimensions"), filter=bestSoFarEvaluations, filter.par=list(step=1000))

fit <- fitness[Dimensions=="20"]
ggplot(fit[,.(Mean=mean(BestSoFar)), by=.(Evaluations,Method,UniqueTargets)], aes(Evaluations,Mean)) + geom_line(aes(colour=Method,group=Method)) + facet_wrap(~ UniqueTargets, labeller=label_both)

levels <- fitness[, fitnessLevels(.SD,seq(0.9,0.99,0.01)), by=.(Job,Method,UniqueTargets,Dimensions)]
levels[which(is.infinite(Evaluations)), Evaluations := NA]
agg <- levels[, if(length(Evaluations) > 10) mean(Evaluations,na.rm=T) else NA, by=.(Method,UniqueTargets,Dimensions,Threshold)]
ggplot(agg, aes(UniqueTargets,V1)) + geom_bar(aes(fill=Method), stat="identity",position="dodge") + facet_grid(Dimensions ~ Threshold, labeller=label_both)

filt <- agg[(Threshold==0.99 & Dimensions=="5")|(Threshold==0.99 & Dimensions=="10")|(Threshold==0.98 & Dimensions=="20")|(Threshold==0.96 & Dimensions=="30")|(Threshold==0.93 & Dimensions=="50")]

ggplot(filt, aes(UniqueTargets,V1)) + geom_bar(aes(fill=Method), stat="identity",position="dodge") + 
  facet_wrap(~ Dimensions + Threshold, labeller=label_both) + ylab("Evaluations until fitness levels achieved (less is better)")
ggplot(lastGen(fitness)[,.(Mean=mean(BestSoFar)), by=.(Method,Dimensions,UniqueTargets)], aes(UniqueTargets,Mean)) + geom_bar(aes(fill=Method), stat="identity",position="dodge") + facet_wrap(~ Dimensions, labeller=label_both) + scale_y_continuous(limits=c(0.75,1),oob = rescale_none) + ylab("Mean of highest fitness achieved in each run")

ggplot(filt, aes(UniqueTargets,V1,colour=Method,group=Method)) + geom_line() + geom_point() + 
  facet_wrap(~ Dimensions + Threshold, labeller=label_both) + ylab("Evaluations until fitness levels achieved (less is better)")
ggplot(lastGen(fitness)[,.(Mean=mean(BestSoFar)), by=.(Method,Dimensions,UniqueTargets)], aes(UniqueTargets,Mean,colour=Method,group=Method)) + geom_line() + geom_point() + facet_wrap(~ Dimensions, labeller=label_both) + ylab("Mean of highest fitness achieved in each run (higher is better)")


setwd("/media/jorge/Orico/allocation2")
split <- loadData("split/*_20_*","fitness.stat",fun=loadFitness,auto.ids.sep="[_/]", parallel=F, auto.ids.names=c("Experiment","Task","UniqueTargets","Dimensions","MaxLockdown"), filter=bestSoFarEvaluations, filter.par=list(step=1000))

ggplot(split[,.(Mean=mean(BestSoFar)), by=.(Evaluations,UniqueTargets,Dimensions,MaxLockdown)], aes(Evaluations,Mean)) + geom_line(aes(colour=MaxLockdown,group=MaxLockdown)) + facet_wrap(~ UniqueTargets, labeller=label_both)
ggplot(lastGen(split)[, .(BestMean=mean(BestSoFar)), by=.(UniqueTargets,Dimensions,MaxLockdown)], aes(MaxLockdown,BestMean)) + geom_line(aes(colour=Dimensions,group=Dimensions)) + facet_wrap(~ UniqueTargets)

hybsplit <- loadData("split/*_20_*","hybrid.stat",auto.ids.sep="[_/]", auto.ids.names=c("Experiment","Task","UniqueTargets","Dimensions","MaxLockdown"), fun=loadFile, colnames=hyb.cols, parallel=F)
hybsplit[, Evaluations := floor(Evaluations / 1000) * 1000]
ggplot(hybsplit[, .(NumPops=mean(NumPops)), by=.(Evaluations,UniqueTargets,Dimensions,MaxLockdown)], aes(Evaluations,NumPops)) + geom_line(aes(colour=MaxLockdown,group=MaxLockdown)) + facet_grid(UniqueTargets ~ Dimensions, labeller=label_both) + ylab("Mean number of populations")

merge <- loadData("merge/*_20_*","fitness.stat",fun=loadFitness,auto.ids.sep="[_/]", parallel=F, auto.ids.names=c("Experiment","Task","UniqueTargets","Dispersion","Dimensions","MergeThreshold"), filter=bestSoFarEvaluations, filter.par=list(step=10000))

levels <- merge[, fitnessLevels(.SD,seq(0.9,0.99,0.01)), by=.(Job,UniqueTargets,Dispersion,MergeThreshold)]
agg <- levels[, if(length(Evaluations) > 10) mean(Evaluations,na.rm=T) else NA, by=.(UniqueTargets,Dispersion,MergeThreshold,Threshold)]
ggplot(agg, aes(Threshold,V1)) + geom_bar(aes(fill=MergeThreshold), position="dodge",stat="identity") + facet_wrap(~ Dispersion + UniqueTargets, labeller=label_both)


ggplot(merge[,.(Mean=mean(BestSoFar)), by=.(Evaluations,UniqueTargets,Dispersion,MergeThreshold)], aes(Evaluations,Mean)) + geom_line(aes(colour=MergeThreshold,group=MergeThreshold)) + facet_wrap(~ Dispersion + UniqueTargets, labeller=label_both)

hybmerge <- loadData("merge/*_20_*","hybrid.stat",auto.ids.sep="[_/]", auto.ids.names=c("Experiment","Task","UniqueTargets","Dispersion","Dimensions","MergeThreshold"), fun=loadFile, colnames=hyb.cols, parallel=F)
hybmerge[, Evaluations := floor(Evaluations / 1000) * 1000]
ggplot(hybmerge[, .(NumPops=mean(NumPops)), by=.(Evaluations,UniqueTargets,Dispersion,MergeThreshold)], aes(Evaluations,NumPops)) + geom_line(aes(colour=MergeThreshold,group=MergeThreshold)) + facet_wrap(~ Dispersion + UniqueTargets, labeller=label_both) + ylab("Mean number of populations")

setwd("/media/jorge/Orico/allocationx")
fitness <- loadData("comparison/**/*","fitness.stat",fun=loadFitness,auto.ids.sep="[_/]", parallel=F, auto.ids.names=c("Experiment","Method","Task","UniqueTargets","Dimensions"), filter=bestSoFarEvaluations, filter.par=list(step=1000))

levels <- fitness[, fitnessLevels(.SD,seq(0.9,0.99,0.01)), by=.(Job,Method,UniqueTargets,Dimensions)]
agg <- levels[, if(length(Evaluations) > 10) mean(Evaluations,na.rm=T) else NA, by=.(Method,UniqueTargets,Dimensions,Threshold)]
ggplot(agg, aes(UniqueTargets,V1)) + geom_bar(aes(fill=Method), stat="identity",position="dodge") + facet_grid(Dimensions ~ Threshold, labeller=label_both)

filt <- agg[(Threshold==0.99 & Dimensions=="5")|(Threshold==0.99 & Dimensions=="10")|(Threshold==0.99 & Dimensions=="20")|(Threshold==0.99 & Dimensions=="30")|(Threshold==0.98 & Dimensions=="50")]

ggplot(filt, aes(UniqueTargets,V1)) + geom_bar(aes(fill=Method), stat="identity",position="dodge") + 
  facet_wrap(~ Dimensions + Threshold, labeller=label_both) + ylab("Evaluations until fitness levels achieved (less is better)")
ggplot(lastGen(fitness)[,.(Mean=mean(BestSoFar)), by=.(Method,Dimensions,UniqueTargets)], aes(UniqueTargets,Mean)) + geom_bar(aes(fill=Method), stat="identity",position="dodge") + facet_wrap(~ Dimensions, labeller=label_both) + scale_y_continuous(limits=c(0.75,1),oob = rescale_none) + ylab("Mean of highest fitness achieved in each run")

ggplot(filt, aes(UniqueTargets,V1,colour=Method,group=Method)) + geom_line() + geom_point() + 
  facet_wrap(~ Dimensions + Threshold, labeller=label_both) + ylab("Evaluations until fitness levels achieved (less is better)")
ggplot(lastGen(fitness)[,.(Mean=mean(BestSoFar)), by=.(Method,Dimensions,UniqueTargets)], aes(UniqueTargets,Mean,colour=Method,group=Method)) + geom_line() + geom_point() + facet_wrap(~ Dimensions, labeller=label_both) + ylab("Mean of highest fitness achieved in each run (higher is better)")

