setwd("~/exps/playground/rep2")

data <- loadFile("~/exps/testme/job.0.finalrep.stat")

data <- loadFile("sdbc/job.2.finalrep.stat")
vars <- paste0("Behav_",0:4)
gvars <- paste0("Genome_",0:51)

#data[, Variance := sqrt((1000 - Fitness) / 200)]

d <- rbind(cbind(Job=0, loadFile("sdbcl2var/job.0.finalrep.stat")),
           cbind(Job=1, loadFile("sdbcl2var/job.1.finalrep.stat")))

r <- reduceData(d, vars, method="Rtsne", k=2)
plotReduced2D(r, color.var="Fitness") + facet_wrap(~Job)
r <- reduceData(d, gvars, method="Rtsne", k=2)
plotReduced2D(r, color.var="Fitness") + facet_wrap(~Job)


plotVarsHist(data, vars)
plotVarsHist(data, vars, breaks=seq(-2,2,by=0.4))


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

sammon2 <- reduceData(data, vars, method="sammon", k=2)
write.table(sammon2[,.(Hash,V1,V2)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbcmedian_sam2.txt", row.names=F, col.names=F, sep=" ")

sammon3 <- reduceData(data, vars, method="sammon", k=3)
write.table(sammon3[,.(Hash,V1,V2,V3)], file="~/Dropbox/mase/src/mase/app/playground/rep/sdbcmedian_sam3.txt", row.names=F, col.names=F, sep=" ")


red <- reduceData(data, vars=vars, method="sammon", k=2)
plotReduced2D(red, color.var="Fitness")
red <- reduceData(data, vars=vars, method="tsne", k=2)
plotReduced2D(red, color.var="Fitness")
red <- reduceData(data, vars=vars, method="Rtsne", k=2)
plotReduced2D(red, color.var="Fitness")
red <- reduceData(data, vars=vars, method="pca", k=2)
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


setwd("~/exps/playground/tasks2")

fit <- loadData("*", "postfitness.stat", fun=loadFitness, auto.ids.names=c("Method","Task"))

ggplot(fit[is.na(Subpop), .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(Task,Method,Generation)], aes(Generation,Mean,group=Method)) + 
  geom_line(aes(colour=Method)) + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE, fill=Method), alpha = 0.1) + ylab("Fitness")  + facet_wrap(~ Task, scales="free_y", ncol=2)

ggplot(lastGen(fit), aes(Method,BestSoFar)) + geom_boxplot(aes(fill=Method)) + facet_wrap(~ Task, scales="free_y", ncol=2)


setwd("~/labmag/exps/playground/tasks3")

fit <- loadData("*", "postfitness.stat", fun=loadFitness, auto.ids.names=c("BC","Task","Repo"))

ggplot(fit[is.na(Subpop), .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(Task,Repo,Generation)], aes(Generation,Mean,group=Repo)) + 
  geom_line(aes(colour=Repo)) + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE, fill=Repo), alpha = 0.1) + ylab("Fitness")  + facet_wrap(~ Task, scales="free_y", ncol=2)

ggplot(lastGen(fit), aes(Repo,BestSoFar)) + geom_boxplot(aes(fill=Repo)) + facet_wrap(~ Task, scales="free_y", ncol=2)



setwd("~/exps/playground/tasks")

fit <- loadData("**/*", "postfitness.stat", fun=loadFitness, auto.ids.names=c("Task","Repo","Reduction"))
fit[, Method := factor(paste(Repo,Reduction,sep="_"))]
fit[, Reference := mean(.SD[Repo=="direct" & Generation==max(Generation)]$BestSoFar), by=.(Task)]
fit[, RelativeFitness := (BestSoFar-Reference) / Reference]

ggplot(lastGen(fit), aes(Repo,RelativeFitness)) + geom_boxplot(aes(fill=Reduction))
ggplot(lastGen(fit)[, .(RelativePerformance = mean(RelativeFitness), SE=se(RelativeFitness)), by=.(Repo,Reduction)], aes(Repo,RelativePerformance,fill=Reduction)) +
  geom_bar(position=position_dodge(), stat="identity")

mean <- lastGen(fit)[, .(Fitness=mean(BestSoFar)), by=.(Task,Repo,Reduction,Method)]
rank <- mean[, .(Method,Repo,Reduction,Rank=frank(Fitness)) , by=.(Task)]

ggplot(rank, aes(Method,Rank)) + geom_boxplot()
ggplot(rank[Repo != "direct"], aes(Reduction,Rank)) + geom_boxplot() + facet_wrap(~ Repo)
ggplot(rank[Repo != "direct"], aes(Repo,Rank)) + geom_boxplot() + facet_wrap(~ Reduction)

mean <- lastGen(fit)[, .(Fitness=mean(BestSoFar)), by=.(Task,Repo,Reduction,Method)]


ggplot(lastGen(fit), aes(Repo,BestSoFar)) + geom_boxplot(aes(fill=Reduction)) + facet_wrap(~ Task, scales="free_y", ncol=2)


agg <- fit[is.na(Subpop), .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(Task,Method,Generation)]
ggplot(agg, aes(Generation,Mean,group=Method)) + geom_line(aes(colour=Method)) + facet_wrap(~Task, scales="free_y")

ggplot(lastGen(fit), aes(ID3,BestSoFar)) + geom_boxplot(aes(fill=ID2))
ggplot(lastGen(fit[ID2=="sdbc" | ID2=="sdbcl" | ID2=="sdbctsne3" | ID2=="sdbcltsne3"]), aes(ID3,BestSoFar)) + geom_boxplot(aes(fill=ID2))

metaAnalysis(lastGen(fit), BestSoFar ~ ID2, ~ ID3)
