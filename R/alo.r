setwd("~/labmag/exps/alo_parameters/")
f <- loadData("*/**","fitness.stat",fun=loadFitness)

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


setwd("~/exps/alo_parameters3/")
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



