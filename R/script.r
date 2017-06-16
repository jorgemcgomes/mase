# Task performance

setwd("~/exps/playground/tasks5")

fit <- loadData("*", "postfitness.stat", fun=loadFitness, auto.ids.names=c("Domain","Task","Repo","Reduction"))
fit[, Reference := mean(.SD[is.na(Repo) & Generation==max(Generation)]$BestSoFar), by=.(Task)]
fit[, RelativeFitness := ((BestSoFar-Reference) / Reference) * 100]
fit[is.na(Repo), Repo := "Direct"]
fit[, Reduction := factor(Reduction, levels=c("0","1","2","3","4","Direct"), labels=c("3","4","5","7","10","direct"))]

sum <- lastGen(fit)[Repo!="Direct", .(Fitness=mean(RelativeFitness)), by=.(Repo, Task, Reduction)]
ggplot(sum, aes(paste(Repo,Reduction),Fitness)) + 
  geom_bar(aes(fill=Task), stat="identity") + 
  theme(axis.text.x=element_text(angle=22.5,hjust=1)) + labs(x="Method", y="Fitness relative to direct (%)")



sum <- lastGen(fit)[Repo!="Direct", .(Fitness=mean(RelativeFitness)), by=.(Repo, Task, Reduction)]
sum <- sum[, .(Mean=mean(Fitness),Min=min(Fitness), Max=max(Fitness)), by=.(Repo,Reduction)]
ggplot(sum, aes(paste(Repo,Reduction,sep="-"),Mean)) + geom_bar(stat="identity", fill="steelblue") + 
  theme(axis.text.x=element_text(angle=22.5,hjust=1)) + labs(x="Method", y="Fitness relative to direct (%) [Min,Max]") +
  geom_errorbar(aes(ymin=Min, ymax=Max),width=0.25)


selected <- lastGen(fit)[is.na(Repo) | Repo=="base" | Repo=="nsmedian"]
ggplot(selected, aes(paste0(Repo,Reduction),BestSoFar)) + geom_boxplot() + facet_wrap(~ Task, scales="free_y")



# MAP-Elites vs NS comparison

repme <- loadFile("~/exps/playground/rep4/base/job.0.finalrep.stat")
repns <- loadFile("~/exps/playground/rep5/nsmedian/job.0.archive.stat")
b <- paste0("Behav_",0:4)
plotVarsHist(repme, vars=b)
plotVarsHist(repns, vars=b)

merged <- rbind(cbind(repme,Method="ME"), cbind(repns,Method="NS"), fill=T)
reduced <- reduceData(unique(merged, by=b), vars=b, method="Rtsne")
reduced <- reduceData(unique(merged, by=b), vars=b, method="rpca", k=5)
reduced <- reduceData(unique(merged, by=b), vars=b, method="sammon", k=2)

plotReduced2D(reduced) + facet_wrap(~ Method)


# Activations

act <- loadData("**", "postbest.xml.stat", filter=function(x){x[Seed < 3]}, auto.ids.names=c("Domain","Task","Repo","Reduction"))
act <- act[Repo != "memedian"]
act[, Reduction := factor(Reduction, levels=c("0","1","2","3","4","direct"), labels=c("3","4","5","7","10","direct"))]
act[, Progress := Time / max(Time), by=.(Task,Repo,Reduction,Job,Seed)]

sim.stats <- act[, .(Number=length(unique(Primitive)), 
         Duration=mean(rle(Primitive)$lengths), 
         MaxDuration=max(rle(Primitive)$lengths),
         Used=sum(sapply(.SD[, paste0("ArbitratorOut_",0:9), with=F], sd) > 0.1, na.rm=T),
         sd0=sd(ArbitratorOut_0),sd1=sd(ArbitratorOut_1),sd2=sd(ArbitratorOut_2),sd3=sd(ArbitratorOut_3),sd4=sd(ArbitratorOut_4),sd5=sd(ArbitratorOut_5),sd6=sd(ArbitratorOut_6),sd7=sd(ArbitratorOut_7),sd8=sd(ArbitratorOut_8),sd9=sd(ArbitratorOut_9)),
     by=.(Seed, Job, Task, Repo, Reduction)]

m <- melt(sim.stats, measure.vars=paste0("sd",0:9))
stat <- m[, .(Var=mean(value)), by=.(Repo,Reduction,variable)]
ggplot(stat, aes(variable, Var, group=Repo)) + geom_line(aes(colour=Repo)) + facet_wrap(~ Reduction, scales="free_x")

controller.means <- sim.stats[, .(Number=mean(Number), Duration=mean(Duration), MaxDuration=mean(MaxDuration), Used=mean(Used)), by=.(Job, Task, Repo, Reduction)]
ggplot(controller.means, aes(paste(Repo,Reduction), Used)) + geom_boxplot() + facet_wrap(~ Task) +
  theme(axis.text.x=element_text(angle=45,hjust=1))

ggplot(controller.means[, .(MeanUsed=mean(Used), SE=se(Used)), by=.(Repo,Reduction)], aes(Reduction,MeanUsed,group=Repo,colour=Repo)) + 
  geom_line() + geom_errorbar(aes(ymin=MeanUsed-SE, ymax=MeanUsed+SE), width=.25) + geom_point(shape=21, fill="white") + ylim(0,NA) 

ggplot(controller.means[, .(Number=mean(Number), SE=se(Number)), by=.(Repo,Reduction,Task)], aes(Reduction,Number,group=Repo,colour=Repo)) + 
  geom_line() + geom_errorbar(aes(ymin=Number-SE, ymax=Number+SE), width=.25) + geom_point(shape=21, fill="white") + facet_wrap(~ Task)


# NS-Median test case

m3 <- merge(act[Repo=="nsmedian" & Reduction=="3" & Seed==0], 
                fread("~/Dropbox/mase/src/mase/app/playground/rep/nsmedian_rpca3.txt", col.names=c("Primitive",paste0("B",0:2))),
                by="Primitive")

m4 <- merge(act[Repo=="nsmedian" & Reduction=="5" & Seed==0], 
            fread("~/Dropbox/mase/src/mase/app/playground/rep/nsmedian_rpca4.txt", col.names=c("Primitive",paste0("B",0:3))),
            by="Primitive")

m5 <- merge(act[Repo=="nsmedian" & Reduction=="5" & Seed==0], 
            fread("~/Dropbox/mase/src/mase/app/playground/rep/nsmedian_rpca5.txt", col.names=c("Primitive",paste0("B",0:4))),
            by="Primitive")

md <- merge(act[Repo=="nsmedian" & Reduction=="direct" & Seed==0], 
            fread("~/exps/playground/rep5/nsmedian/job.0.archive.stat"),
            by.x="Primitive", by.y="Index")

ggplot(melt(m3, measure.vars=paste0("B",0:2)), aes(variable, value, group=Time)) + geom_line(alpha=0.1) + facet_grid(Task ~ Job) + ggtitle("PCA 3")
ggplot(melt(m4, measure.vars=paste0("B",0:3)), aes(variable, value, group=Time)) + geom_line(alpha=0.1) + facet_grid(Task ~ Job) + ggtitle("PCA 4")
ggplot(melt(m5, measure.vars=paste0("B",0:4)), aes(variable, value, group=Time)) + geom_line(alpha=0.1) + facet_grid(Task ~ Job) + ggtitle("PCA 5")
ggplot(melt(md, measure.vars=paste0("Behav_",0:4,".y")), aes(variable, value, group=Time)) + geom_line(alpha=0.1) + facet_grid(Task ~ Job) + ggtitle("Direct")


#rep <- fread("~/Dropbox/mase/src/mase/app/playground/rep/nsmedian_rpca4.txt", col.names=c("Primitive",paste0("B",0:3)))
#merged <- merge(act[Repo=="nsmedian" & Reduction=="4" & Seed==0], rep, by="Primitive")
#ggplot(merged, aes(PC_0, PC_1)) + geom_point(data=rep, shape=20, colour="gray") + geom_point(aes(colour=Progress), shape=4) + facet_grid(Task ~ Job)




# Repertoires dimensionality reduction

rep <- loadFile("~/exps/playground/rep5/nsspirit/job.0.archive.stat")
toremove <- sapply(rep,function(x) length(unique(x))==1) & grepl("Behav_*", colnames(rep))
rep <- rep[, !toremove, with=F]

behav <- colnames(rep)[grep("Behav_*",colnames(rep))]
red <- reduceData(unique(rep, by=behav), behav, method="rpca", k=100,kmax=100)
plotReduced2D(red)

write.table(red[,.(Index,V1,V2,V3)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsspirit_rpca3.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsspirit_rpca4.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4,V5)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsspirit_rpca5.txt", row.names=F, col.names=F, sep=" ")

rep <- loadFile("~/labmag/exps/playground/rep5/nsmedian/job.0.archive.stat")
behav <- colnames(rep)[grep("Behav_*",colnames(rep))]

red <- reduceData(unique(rep, by=behav), behav, method="rpca", k=5)
plotReduced2D(red)

# kernel PCA test
kp <- kpca(~., data=rep[,behav,with=F], kernel="rbfdot", features=5, kpar=list(sigma = 0.2))
ggplot(as.data.table(kp@pcv), aes(V1,V2)) + geom_point()
ggplot(data.table(PC=1:5,Eigen=kp@eig), aes(PC,Eigen)) + geom_bar(stat="identity")


pc <- PcaHubert(rep[, behav, with=F], k=length(behav), kmax=length(behav), scale=F)
ggplot(data.table(PC=1:pc@k,SD=getSdev(pc)), aes(PC,SD)) + geom_bar(stat="identity")

write.table(red[,.(Index,V1,V2,V3)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsmedian_rpca3.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsmedian_rpca4.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4,V5)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsmedian_rpca5.txt", row.names=F, col.names=F, sep=" ")



rep <- loadFile("~/labmag/exps/playground/rep5/nsall/job.0.archive.stat")
behav <- colnames(rep)[grep("Behav_*",colnames(rep))]

red <- reduceData(unique(rep, by=behav), behav, method="rpca", k=100, kmax=100)
plotReduced2D(red)

write.table(red[,.(Index,V1,V2,V3)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsall_rpca3.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsall_rpca4.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4,V5)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsall_rpca5.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4,V5,V6,V7)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsall_rpca7.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4,V5,V6,V7,V8,V9,V10)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsall_rpca10.txt", row.names=F, col.names=F, sep=" ")



rep <- loadFile("~/labmag/exps/playground/rep5/nsas1/job.0.archive.stat")
behav <- colnames(rep)[grep("Behav_*",colnames(rep))]

red <- reduceData(unique(rep, by=behav), behav, method="rpca", k=length(behav), kmax=length(behav))
plotReduced2D(red)

write.table(red[,.(Index,V1,V2,V3)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsas1_rpca3.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsas1_rpca4.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4,V5)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsas1_rpca5.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4,V5,V6,V7)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsas1_rpca7.txt", row.names=F, col.names=F, sep=" ")


rep <- loadFile("~/labmag/exps/playground/rep5/nsas3/job.0.archive.stat")
behav <- colnames(rep)[grep("Behav_*",colnames(rep))]

red <- reduceData(unique(rep, by=behav), behav, method="rpca", k=length(behav), kmax=length(behav))
plotReduced2D(red)

write.table(red[,.(Index,V1,V2,V3)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsas3_rpca3.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsas3_rpca4.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4,V5)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsas3_rpca5.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4,V5,V6,V7)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsas3_rpca7.txt", row.names=F, col.names=F, sep=" ")
write.table(red[,.(Index,V1,V2,V3,V4,V5,V6,V7,V8,V9,V10)], file="~/Dropbox/mase/src/mase/app/playground/rep/nsas3_rpca10.txt", row.names=F, col.names=F, sep=" ")



setwd("~/exps/playground/tasks4")

run <- loadFile("~/exps/playground/tasks4/pl_avoidance_base/job.0.postbest.xml.stat")
rep <- loadFile("~/exps/playground/rep4/base/job.0.finalrep.stat")

r <- merge(run, rep, by.x="Primitive", by.y="Hash")
setnames(r, paste0("Behav_",0:4,".y"), paste0("Behav_",0:4))

run[, length(unique(Primitive)), by=.(Seed)]

mrun <- melt(r[, c("Seed","Time",paste0("Behav_",0:4)), with=F], id.vars=c("Seed","Time"))
ggplot(mrun, aes(variable, value, group=Time, colour=Time)) + geom_line(alpha=0.1) + facet_wrap(~ Seed)

mrep <- melt(rep[, c("Hash",paste0("Behav_",0:4)), with=F], id.vars="Hash")
ggplot(mrep, aes(variable, value, group=Hash)) + geom_line(alpha=0.1)


ggplot(mrun, aes(variable, value, group=Time)) + geom_line(data=mrep, alpha=0.1, aes(group=Hash)) + geom_line(alpha=0.1, colour="red") + facet_wrap(~ Seed)

setwd("~/exps/playground/tasks4")
fit <- loadData(c("direct*","*ns*","*base"), "postfitness.stat", fun=loadFitness, auto.ids.names=c("Domain","Task","Repo"))
fit[, Reference := mean(.SD[is.na(Repo) & Generation==max(Generation)]$BestSoFar), by=.(Task)]
fit[, RelativeFitness := (BestSoFar-Reference) / Reference]



setwd("~/exps/playground/tasks4")

fit <- loadData("*", "postfitness.stat", fun=loadFitness, auto.ids.names=c("Domain","Task","Repo"))
fit[, Reference := mean(.SD[is.na(Repo) & Generation==max(Generation)]$BestSoFar), by=.(Task)]
fit[, RelativeFitness := (BestSoFar-Reference) / Reference]

save(fit, file="~/Dropbox/WIP/fittasks4.rdata")

ggplot(fit[is.na(Subpop), .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(Task,Repo,Generation)], aes(Generation,Mean,group=Repo)) + 
  geom_line(aes(colour=Repo)) + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE, fill=Repo), alpha = 0.1) + ylab("Fitness")  + facet_wrap(~ Task, scales="free_y", ncol=2)

ggplot(lastGen(fit), aes(Repo,BestSoFar)) + geom_boxplot(aes(fill=Repo)) + facet_wrap(~ Task, scales="free_y", ncol=2)

sum <- lastGen(fit)[, .(Fitness=mean(RelativeFitness)), by=.(Repo, Task)]
ggplot(sum, aes(Repo,Fitness)) + geom_bar(aes(fill=Task), stat="identity")



sum <- lastGen(fit)[, .(RelativeFitness=mean(RelativeFitness),SE=se(RelativeFitness)), by=.(Task,Repo)]

ggplot(lastGen(fit)[is.na(Repo) | Repo=="base" | Repo=="ns"], aes(Task,BestSoFar)) + geom_boxplot(aes(fill=Repo))

ggplot(lastGen(fit)[is.na(Repo) | Repo=="base"], aes(Task,BestSoFar)) + geom_boxplot(aes(fill=Repo))
ggsave("~/Dropbox/WIP/boxplot.png", width=5, height=4)

ggplot(fit[is.na(Subpop) & (is.na(Repo) | Repo=="base"), .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(Task,Repo,Generation)], aes(Generation,Mean,group=Repo)) + 
  geom_line(aes(colour=Repo)) + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE, fill=Repo), alpha = 0.1) + ylab("Fitness")  + facet_wrap(~ Task, scales="free_y")
ggsave("~/Dropbox/WIP/lines.png", width=7, height=5)

groups <- list(BC=c("base","as"), Complexity=c("base","compslp","compmlp2","compmlp10"), Environment=c("base","envnoobs","envfixed","envmany"), Fitness=c("base","fitrandom","fitrecent"), Resolution=c("base","res02","res07","res10"), ReductionTS=c("base","basesam2","basesam3"), ReductionAS=c("as", "assam2","assam3"))
for(gname in names(groups)) {
  group <- groups[[gname]]
  g <- ggplot(sum[Repo %in% group], aes(Task,RelativeFitness,colour=Repo,group=Repo)) + geom_errorbar(aes(ymin=RelativeFitness-SE, ymax=RelativeFitness+SE),width=0.25) + geom_line() + geom_point(shape=21,fill="white") + ylim(NA,0) + ggtitle(gname)
  ggsave(paste0("~/Dropbox/WIP/",gname,".png"), width=5, height=4)
  print(g)
}

ggplot(lastGen(fit)[, .(RelativePerformance = mean(RelativeFitness)), by=.(Repo)], aes(Repo,RelativePerformance)) +
  geom_bar(position=position_dodge(), stat="identity")
ggsave("~/Dropbox/WIP/meanfitness.png", width=9, height=5)


mean <- lastGen(fit)[, .(Fitness=mean(BestSoFar)), by=.(Task,Repo)]
rank <- mean[, .(Repo,Rank=frankv(Fitness, order=-1)) , by=.(Task)]
ggplot(rank[Repo != "direct"], aes(Repo,Rank)) + geom_boxplot() + ylim(1,NA)
ggsave("~/Dropbox/WIP/rankfitness.png", width=9, height=5)


setwd("~/exps/playground/rep4")
data <- loadData("ns", "archive.stat", fun=loadFile)

r <- reduceData(data, paste0("Behav_",0:4), method="Rtsne", k=2)
plotReduced2D(r) + facet_wrap(~ Job)



setwd("~/labmag/exps/playground/rep4")

data <- loadData("ns100long", "archive.stat", fun=loadFile)
vars <- paste0("Behav_",0:499)

for(job in 0:3) {
  d <- unique(data[Job==job], by=vars)
  red <- reduceData(d, vars, method="sammon", k=2)
  g <- plotReduced2D(red)
  print(g)
}

data <- unique(data[Job==3], by=vars)

# Parallel coordinates
d <- data[, c("Index","Fitness",vars), with=F]
m <- melt(d, id.vars=c("Index","Fitness"))
ggplot(m, aes(variable, value, group=Index), alpha=0.1) + geom_line()

sammon2 <- reduceData(data, vars, method="sammon", k=2)
write.table(sammon2[,.(Index,V1,V2)], file="~/Dropbox/mase/src/mase/app/playground/rep/nssam2.txt", row.names=F, col.names=F, sep=" ")

sammon3 <- reduceData(data, vars, method="sammon", k=3)
write.table(sammon3[,.(Index,V1,V2,V3)], file="~/Dropbox/mase/src/mase/app/playground/rep/nssam3.txt", row.names=F, col.names=F, sep=" ")

sammon5 <- reduceData(data, vars, method="sammon", k=5)
write.table(sammon5[,.(Index,V1,V2,V3,V4,V5)], file="~/Dropbox/mase/src/mase/app/playground/rep/nslongsam5.txt", row.names=F, col.names=F, sep=" ")


data <- loadFile("as/job.0.finalrep.stat")
vars <- paste0("Behav_",0:8)

sammon2 <- reduceData(data, vars, method="sammon", k=2)
write.table(sammon2[,.(Hash,V1,V2)], file="~/Dropbox/mase/src/mase/app/playground/rep/assam2.txt", row.names=F, col.names=F, sep=" ")

sammon3 <- reduceData(data, vars, method="sammon", k=3)
write.table(sammon3[,.(Hash,V1,V2,V3)], file="~/Dropbox/mase/src/mase/app/playground/rep/assam3.txt", row.names=F, col.names=F, sep=" ")







setwd("~/exps/playground/rep3")


data <- loadData("base_*", "finalrep.stat", fun=loadFile, auto.ids.names=c("Env","Resolution"), jobs=0)

r <- reduceData(data, paste0("Behav_",0:4), method="sammon", k=2)
plotReduced2D(r, color.var="Fitness") + facet_wrap(~ Resolution)


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
