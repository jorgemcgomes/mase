setwd("~/exps/playground/")

data <- loadFile("rep/sdbcmeanhighres/job.0.finalrep.stat")
vars <- paste0("Behav_",0:8)
vars <- paste0("Behav_",0:4)

plotVarsHist(data, vars)

normaliseCoords <- function(data) {(data - min(data))/(max(data)-min(data))}

ts2 <- tsne(dist(data[, vars, with=F]), k=2, epoch=10, max_iter=500)
ts2 <- normaliseCoords(ts2)
write.table(cbind(data[,.(Hash)], ts2), file="~/Dropbox/mase/src/mase/app/playground/rep/sdbc_tsne_2.txt", row.names=F, col.names=F, sep=" ")

ts3 <- tsne(dist(data[, vars, with=F]), k=3, epoch=10, max_iter=500)
ts3 <- normaliseCoords(ts3)
write.table(cbind(data[,.(Hash)], ts3), file="~/Dropbox/mase/src/mase/app/playground/rep/sdbcl_tsne_3.txt", row.names=F, col.names=F, sep=" ")

ts5 <- tsne(dist(data[, vars, with=F]), k=5, epoch=10, max_iter=500)
ts5 <- normaliseCoords(ts5)
write.table(cbind(data[,.(Hash)], ts5), file="~/Dropbox/mase/src/mase/app/playground/rep/as_tsne_5.txt", row.names=F, col.names=F, sep=" ")

sammon2 <- sammonReduce(data, vars)


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
agg <- fit[is.na(Subpop), .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(ID2,ID3,Generation)]
ggplot(agg, aes(Generation,Mean,group=ID2)) + geom_line(aes(colour=ID2)) + ylab("Fitness") + 
  geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE, fill=ID2), alpha = 0.1) + facet_wrap(~ID3, scales="free_y")

ggplot(lastGen(fit), aes(ID3,BestSoFar)) + geom_boxplot(aes(fill=ID2))
ggplot(lastGen(fit[ID2=="sdbc" | ID2=="sdbcl" | ID2=="sdbctsne3" | ID2=="sdbcltsne3"]), aes(ID3,BestSoFar)) + geom_boxplot(aes(fill=ID2))

metaAnalysis(lastGen(fit), BestSoFar ~ ID2, ~ ID3)
