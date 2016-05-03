neat <- loadData("soccer3/arcs*","neat.stat",fun=loadFile, colnames=c("Generation","Species","Neurons.Mean","Links.Mean","Recur.Mean","Neurons.Best","Links.Best","Recur.Best"))
ggplot(aggregate(Links.Mean ~ Setup + Generation, neat, mean), aes(Generation,Links.Mean,group=Setup)) + geom_line(aes(colour=Setup))


soc3 <- loadData("soccer3/*","postfitness.stat",fun=loadFitness)
bestSoFarFitness(soc3)
fitnessBoxplots(soc3)
rankByFitness(soc3)