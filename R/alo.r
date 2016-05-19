setwd("labmag/exps/alo")
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
