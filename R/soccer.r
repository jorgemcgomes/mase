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

setwd("~/exps/soccerwins2/")
d <- loadData("*", "fitness.stat", fun=loadFitness)
