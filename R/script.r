setwd("~/exps/playground/rep")

data <- loadFile("sdbc/job.0.finalrep.stat")
vars <- paste0("Behav_",0:8)
vars <- paste0("Behav_",0:4)

plotVarsHist(data, vars)

normaliseCoords <- function(data) {(data - min(data))/(max(data)-min(data))}

ts2 <- tsne(dist(data[, vars, with=F]), k=2, epoch=10, max_iter=500)
ts2 <- normaliseCoords(ts2)
write.table(cbind(data[,.(Hash)], ts2), file="~/Dropbox/mase/src/mase/app/playground/rep/asl_tsne_2.txt", row.names=F, col.names=F, sep=" ")

ts3 <- tsne(dist(data[, vars, with=F]), k=3, epoch=10, max_iter=500)
ts3 <- normaliseCoords(ts3)
write.table(cbind(data[,.(Hash)], ts3), file="~/Dropbox/mase/src/mase/app/playground/rep/sdbcl_tsne_3.txt", row.names=F, col.names=F, sep=" ")

ts5 <- tsne(dist(data[, vars, with=F]), k=5, epoch=10, max_iter=500)
ts5 <- normaliseCoords(ts5)
write.table(cbind(data[,.(Hash)], ts5), file="~/Dropbox/mase/src/mase/app/playground/rep/as_tsne_5.txt", row.names=F, col.names=F, sep=" ")

sammon2 <- sammonReduce(data, vars)
sammon2 <- normaliseCoords(sammon2[,.(X,Y)])
write.table(cbind(data[,.(Hash)], sammon2), file="~/Dropbox/mase/src/mase/app/playground/rep/sdbcl_sammon_2.txt", row.names=F, col.names=F, sep=" ")

sammon3 <- reduceData(data, vars, method="sammon", k=3)
write.table(sammon3[,.(Hash,V1,V2,V3)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbc_sammon_3.txt", row.names=F, col.names=F, sep=" ")


red <- reduceData(data, vars=vars, method="sammon", k=2)
plotReduced2D(red, color.var="Fitness")
red <- reduceData(data, vars=vars, method="tsne", k=2)
plotReduced2D(red, color.var="Fitness")
red <- reduceData(data, vars=vars, method="Rtsne", k=2)
plotReduced2D(red, color.var="Fitness")

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




setwd("~/exps/playground/tasks")

fit <- loadData("**/*", "postfitness.stat", fun=loadFitness, auto.ids.names=c("Task","Repo","Reduction"))
fit[, Method := factor(paste(Repo,Reduction,sep="_"))]
fit[, Min := min(BestSoFar), by=.(Task)]
fit[, Reference := mean(.SD[Repo=="direct" & Generation==max(Generation)]$BestSoFar), by=.(Task)]
fit[, RelativeFitness := (BestSoFar-Min) / (Reference-Min)]

ggplot(lastGen(fit), aes(Repo,RelativeFitness)) + geom_boxplot(aes(fill=Reduction))
ggplot(lastGen(fit)[, .(RelativePerformance = mean(RelativeFitness), SE=se(RelativeFitness)), by=.(Repo,Reduction)], aes(Repo,RelativePerformance,fill=Reduction)) +
  geom_bar(position=position_dodge(), stat="identity")

mean <- lastGen(fit)[, .(Fitness=mean(BestSoFar)), by=.(Task,Repo,Reduction,Method)]
rank <- mean[, .(Method,Repo,Reduction,Rank=frank(Fitness)) , by=.(Task)]

ggplot(rank, aes(Method,Rank)) + geom_boxplot()
ggplot(rank[Repo != "direct"], aes(Reduction,Rank)) + geom_boxplot() + facet_wrap(~ Repo)
ggplot(rank[Repo != "direct"], aes(Repo,Rank)) + geom_boxplot() + facet_wrap(~ Reduction)

mean <- lastGen(fit)[, .(Fitness=mean(BestSoFar)), by=.(Task,Repo,Reduction,Method)]


ggplot(lastGen(fit), aes(Repo,BestSoFar)) + geom_boxplot(aes(fill=Reduction)) + facet_wrap(~ Task, scales="free_y")


agg <- fit[is.na(Subpop), .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(Task,Method,Generation)]
ggplot(agg, aes(Generation,Mean,group=Method)) + geom_line(aes(colour=Method)) + facet_wrap(~Task, scales="free_y")

ggplot(lastGen(fit), aes(ID3,BestSoFar)) + geom_boxplot(aes(fill=ID2))
ggplot(lastGen(fit[ID2=="sdbc" | ID2=="sdbcl" | ID2=="sdbctsne3" | ID2=="sdbcltsne3"]), aes(ID3,BestSoFar)) + geom_boxplot(aes(fill=ID2))

metaAnalysis(lastGen(fit), BestSoFar ~ ID2, ~ ID3)
