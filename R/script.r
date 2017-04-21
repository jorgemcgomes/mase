setwd("~/exps/playground/")

data <- loadFile("rep/as/job.0.finalrep.stat")
vars <- paste0("Behav_",0:8)
plotVarsHist(d, vars)
s <- sammonReduce(d, vars)
sammonPlot(s, "Fitness")

require(tsne)

sub <- data[, vars, with=F]
dists <- dist(sub)
ts <- tsne(dists)
data[, X := ts[,1]]
data[, Y := ts[,2]]
ggplot(data, aes(x=X, y=Y)) + geom_point(aes_string(colour="Fitness"), shape=4, size=1.5) + 
  coord_fixed() + theme(legend.position="right")

ts3 <- tsne(dists, k=3)

ts <- normaliseCoords(ts)
ts3 <- normaliseCoords(ts3)

write.table(normaliseCoords(s[,.(X,Y)]), file="as_sammon_2.txt", row.names=F, col.names=F, sep=" ")
write.table(normaliseCoords(ts), file="as_tsne_2.txt", row.names=F, col.names=F, sep=" ")
write.table(normaliseCoords(ts3), file="as_tsne_3.txt", row.names=F, col.names=F, sep=" ")


normaliseCoords <- function(data) {
  return((data - min(data))/(max(data)-min(data)))
}

d <- loadFile("rep/sdbcmeanhighres/job.0.finalrep.stat")
vars <- paste0("Behav_",0:4)
plotVarsHist(d, vars)
s <- sammonReduce(d, vars)
sammonPlot(s, "Fitness")

d <- loadFile("rep/sdbclast/job.0.finalrep.stat")
vars <- paste0("Behav_",0:9)
plotVarsHist(d, vars)
s <- sammonReduce(d, vars)
sammonPlot(s, "Fitness")


fit <- loadData("tasks/*", "postfitness.stat", fun=loadFitness)
bestSoFarFitness(fit[ID2=="direct"])

