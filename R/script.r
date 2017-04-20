setwd("~/exps/playgroundrepas/")
d <- loadFile("job.0.finalrep.stat")

vars <- paste0("Behav_",0:7)
plotVarsHist(d, vars)

s <- sammonReduce(d, vars)
sammonPlot(s, "Fitness")
