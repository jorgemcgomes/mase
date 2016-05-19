neat <- loadData("soccer3/arcs*","neat.stat",fun=loadFile, colnames=c("Generation","Species","Neurons.Mean","Links.Mean","Recur.Mean","Neurons.Best","Links.Best","Recur.Best"))
ggplot(aggregate(Links.Mean ~ Setup + Generation, neat, mean), aes(Generation,Links.Mean,group=Setup)) + geom_line(aes(colour=Setup))


soc3 <- loadData("soccer3/*","postfitness.stat",fun=loadFitness)
bestSoFarFitness(soc3)
fitnessBoxplots(soc3)
rankByFitness(soc3)

soc3 <- loadData(c("soccer3/homo*","soccer3/ccea*"), "postfitness.stat", fun=loadFitness)
soc5 <- loadData(c("soccer5/homo*","soccer5/ccea*"), "postfitness.stat", fun=loadFitness)

bestSoFarFitness(soc5)
fitnessBoxplots(soc5)
rankByFitness(soc5)
