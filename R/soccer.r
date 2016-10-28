setwd("~/labmag/exps")

neat <- loadData("soccer3/arcs*","neat.stat",fun=loadFile, colnames=c("Generation","Species","Neurons.Mean","Links.Mean","Recur.Mean","Neurons.Best","Links.Best","Recur.Best"))
ggplot(aggregate(Links.Mean ~ Setup + Generation, neat, mean), aes(Generation,Links.Mean,group=Setup)) + geom_line(aes(colour=Setup))


soc3 <- loadData("soccer3/arcs*","postfitness.stat",fun=loadFitness)
soc3[, BestSoFar := BestSoFar-100]
soc3[, BestGen := BestGen-100]

fitnessBoxplots(soc3)
ggsave("soccer3_sensors_box.pdf")
rankByFitness(soc3)
ggsave("soccer3_sensors_bar.pdf")
bestSoFarFitness(soc3)

soc5 <- loadData("soccer5/arcs*","postfitness.stat",fun=loadFitness)
soc5[, BestSoFar := BestSoFar-100]
soc5[, BestGen := BestGen-100]

fitnessBoxplots(soc5)
ggsave("soccer5_sensors_box.pdf")
rankByFitness(soc5)
ggsave("soccer5_sensors_bar.pdf")
bestSoFarFitness(soc5)

soc3 <- loadData(c("soccer3/homo*","soccer3/ccea*"), "postfitness.stat", fun=loadFitness)
soc3[, BestSoFar := BestSoFar-100]
soc3[, BestGen := BestGen-100]

fitnessBoxplots(soc3)
ggsave("soccer3_algo_box.pdf")
rankByFitness(soc3)
ggsave("soccer3_algo_bar.pdf")
bestSoFarFitness(soc3)

soc5 <- loadData(c("soccer5/homo*","soccer5/ccea*"), "postfitness.stat", fun=loadFitness)
soc5[, BestSoFar := BestSoFar-100]
soc5[, BestGen := BestGen-100]

fitnessBoxplots(soc5)
ggsave("soccer5_algo_box.pdf")
rankByFitness(soc5)
ggsave("soccer5_algo_bar.pdf")
bestSoFarFitness(soc5)

setwd("~/labmag/exps/soccer")
d <- loadData("*", "postfitness.stat", fun=loadFitness)
fitnessBoxplots(d)
bestSoFarFitness(d)

setwd("~/labmag/exps/soc4")
d <- loadData("*", "postfitness.stat", fun=loadFitness)
fitnessBoxplots(d)
bestSoFarFitness(d)

fit <- loadData("*","fitness.stat",fun=loadFitness, filter=bestSoFarEvaluations, filter.par=list(step=500))
ggplot(fit[,.(Mean=mean(BestSoFar)),by=.(Evaluations,ID3)], aes(Evaluations,Mean)) + geom_line(aes(colour=ID3,group=ID3))

hyb.cols <- c("Generation","Evaluations","NumPops",NA,NA,NA,"MeanAge","MaxAge","Merges","Splits","TotalMerges","TotalSplits","MinDist","MeanDist",NA)
hyb <- loadData("*","hybrid.stat", fun=loadFile, colnames=hyb.cols)

ggplot(hyb, aes(Evaluations,NumPops)) + geom_smooth() + ylim(1,5)
ggplot(hyb, aes(Evaluations,MinDist)) + geom_smooth() + ylim(0,1)
ggplot(hyb, aes(Evaluations,TotalMerges)) + geom_smooth() + ylim(0,NA)


setwd("~/labmag/exps/soc5/")
d <- loadData("*", "postfitness.stat", fun=loadFitness)
ggplot(lastGen(d), aes(ID3,BestSoFar)) + geom_boxplot() + facet_wrap(~ ID2)

hyb <- loadData("wins_5*","hybrid.stat", fun=loadFile, colnames=hyb.cols)
ggplot(hyb, aes(Evaluations,NumPops,colour=Setup,group=Setup)) + geom_smooth() + ylim(1,5)
ggplot(hyb, aes(Evaluations,MinDist,colour=Setup,group=Setup)) + geom_smooth() + ylim(0,1)
ggplot(hyb, aes(Evaluations,TotalMerges,colour=Setup,group=Setup)) + geom_smooth() + ylim(0,NA)


setwd("~/labmag/exps/mr3/")
d <- loadData("*", "postfitness.stat", fun=loadFitness)
fitnessBoxplots(d)

setwd("~/labmag/exps/soc6/")
d <- loadData("*", "postfitness.stat", fun=loadFitness)
fitnessBoxplots(d)


fit <- loadData("*","fitness.stat",fun=loadFitness, filter=bestSoFarEvaluations, filter.par=list(step=500))
ggplot(fit[,.(Mean=mean(BestSoFar)),by=.(Evaluations,Setup)], aes(Evaluations,Mean)) + geom_line(aes(colour=Setup,group=Setup))


setwd("~/labmag/exps/mr3/")
fit <- loadData("*_0","fitness.stat",fun=loadFitness, filter=bestSoFarEvaluations, filter.par=list(step=1000))
fit <- loadData("*_1","fitness.stat",fun=loadFitness, filter=bestSoFarEvaluations, filter.par=list(step=1000))
ggplot(fit[,.(Mean=mean(BestSoFar)),by=.(Evaluations,Setup)], aes(Evaluations,Mean)) + geom_line(aes(colour=Setup,group=Setup))

setwd("~/labmag/exps/soc6/")
fit <- loadData("wins_9*","fitness.stat",fun=loadFitness, filter=bestSoFarEvaluations, filter.par=list(step=900))
ggplot(fit[,.(Mean=mean(BestSoFar)),by=.(Evaluations,Setup)], aes(Evaluations,Mean)) + geom_line(aes(colour=Setup,group=Setup))
ggplot(lastGen(fit), aes(Setup,BestSoFar)) + geom_boxplot()

fit <- loadData("wins_5*","fitness.stat",fun=loadFitness, filter=bestSoFarEvaluations, filter.par=list(step=500))
ggplot(fit[,.(Mean=mean(BestSoFar)),by=.(Evaluations,Setup)], aes(Evaluations,Mean)) + geom_line(aes(colour=Setup,group=Setup))
ggplot(lastGen(fit), aes(Setup,BestSoFar)) + geom_boxplot()

