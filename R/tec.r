### Base ######

setwd("/media/jorge/Orico/allocationx_fix")
hyb.cols <- c("Generation","Evaluations","NumPops",NA,NA,NA,"MeanAge","MaxAge","Merges","Splits","TotalMerges","TotalSplits",NA,"MeanDist",NA)
ut_labeller <- labeller(UniqueTargets=function(v) {paste("Unique targets:",v)})
pops_scale <- scale_y_continuous(breaks=seq(1,10),minor_breaks=NULL,limits=c(1,10))

# fitnessDecay <- function(job, lookAhead=1) {
#   phased.diff <- ((c(job$BestGen[-1:-lookAhead],rep(NA,lookAhead)) - job$BestGen) / abs(job$BestGen)) * 100
#   res <- list(
#     Baseline = mean(phased.diff, na.rm=T),
#     AfterMerge = mean(phased.diff[which(job$Merges > 0)], na.rm=T),
#     AfterSplit = mean(phased.diff[which(job$Splits > 0)], na.rm=T)
#   )
#   return(res)
# }

### Method X number of targets #####

comp <- loadData("comparison_*", "fitness.stat", fun=loadFitness, auto.ids.names=c("Experiment","Method","UniqueTargets"),  filter=bestSoFarEvaluations, filter.par=list(step=1000))
comp[, Method := factor(Method,labels=c("CCEA-H","CCEA-PH","Hyb-CCEA","R-Exch","C-Exch"))]
comp[, Method := relevel(Method, "Hyb-CCEA")]
comp <- comp[Method != "R-Exch" & Method != "C-Exch"]

# highest fitnesses
metaAnalysis(lastGen(comp), BestSoFar~Method, ~UniqueTargets)
metaAnalysis(lastGen(comp), BestSoFar~UniqueTargets, ~Method)

#ggplot(comp[,.(Mean=mean(BestSoFar)),by=.(Evaluations,Method,UniqueTargets)], aes(Evaluations,Mean)) + geom_line(aes(group=Method,colour=Method)) + facet_wrap(~ UniqueTargets)
# evals until level -- note the failed runs with 10 targets
levels <- comp[, fitnessLevels(.SD,0.995), by=.(Job,Method,UniqueTargets)]
metaAnalysis(levels, Evaluations~Method, ~UniqueTargets)

ggplot(lastGen(comp), aes(UniqueTargets,BestSoFar,colour=Method)) + 
  geom_boxplot(size=.3,outlier.size=.4) + scale_color_brewer(palette="Set1") + theme(panel.grid.major.x = element_blank()) +
  labs(y="Highest fitness score achieved", x="Number of unique targets",color=NULL) + geom_vline(xintercept=c(1.5,2.5,3.5),size=.3,colour="darkgrey")
ggsave("~/Dropbox/Work/Papers/TEC/fig3/comparison_fitness.pdf", width=3.5, height=2)

ggplot(levels, aes(UniqueTargets,Evaluations/1000, colour=Method)) + 
  geom_boxplot(size=.3,outlier.size=.4) + scale_color_brewer(palette="Set1") + theme(panel.grid.major.x = element_blank()) +
  labs(y="Evaluations (x1000) to solution", x="Number of unique targets",color=NULL) + ylim(0,NA) + geom_vline(xintercept=c(1.5,2.5,3.5),size=.3,colour="darkgrey")
ggsave("~/Dropbox/Work/Papers/17-TEC/fig/comparison_evaluations2.pdf", width=3.5, height=2)

m <- merge(levels[Method=="Hyb-CCEA"],levels[Method=="CCEA-PH"], by=c("Job","UniqueTargets","Threshold"))
cor(m$Evaluations.x,m$Evaluations.y)

### Number of dimensions X number of targets #####

dims <- loadData("dimensions_*", "fitness.stat", fun=loadFitness, auto.ids.names=c("Experiment","UniqueTargets","Dimensions"),  filter=bestSoFarEvaluations, filter.par=list(step=1000))

metaAnalysis(lastGen(dims), BestSoFar~UniqueTargets, ~Dimensions)
metaAnalysis(lastGen(dims), BestSoFar~Dimensions, ~UniqueTargets)
lastGen(dims)[,.(cor(BestSoFar,factorNum(Dimensions),method="s")), by=.(UniqueTargets)]
lastGen(dims)[,.(cor2(BestSoFar,factorNum(Dimensions),method="s")), by=.(UniqueTargets)]

cor2 <- function(x , y, ...) {
  ct <- cor.test(x, y, ...)
  print(ct)
  return(ct$estimate)
}

ggplot(lastGen(dims), aes(Dimensions,BestSoFar,colour=UniqueTargets)) + 
  stat_summary(fun.y=median, geom="line", aes(group=UniqueTargets,linetype=UniqueTargets), size=.3) + scale_linetype_manual(values=c("solid","dashed","dotted","twodash")) +
  geom_boxplot(size=.3,outlier.size=.4, position="identity") +
  scale_color_brewer(palette="Set1") +ylim(0.985,1) +
  labs(colour="Number of unique targets", y="Highest fitness score")
ggsave("~/Dropbox/Work/Papers/TEC/fig3/dimensions_fitness.pdf", width=1.8, height=2.25) 

# ggplot(lastGen(mergefit), aes(MergeThreshold,BestSoFar,colour=UniqueTargets)) + 
#   stat_summary(fun.y=median, geom="line", aes(group=UniqueTargets), size=.2, position=pd) +
#   geom_boxplot(size=.3,outlier.size=.4, position=pd) +
#   scale_color_brewer(palette="Set1") + 
#   labs(x="Merge threshold", y="Highest fitness",colour="Unique targets")
# ggsave("~/Dropbox/Work/Papers/TEC/fig2/merge_fitness.pdf", width=3.5, height=2.5, scale=1) 


dimshyb <- loadData("dimensions_*","hybrid.stat", auto.ids.names=c("Experiment","UniqueTargets","Dimensions"),fun=loadFile, colnames=hyb.cols)

stat <- dimshyb[, .(NumPops = mean(NumPops)), by=.(Job,UniqueTargets,Dimensions)]
metaAnalysis(stat, NumPops~Dimensions, ~UniqueTargets)
metaAnalysis(stat[Dimensions!="10"], NumPops~Dimensions, ~UniqueTargets)

# stat_summary(fun.y=median, geom="line", aes(group=UniqueTargets), size=.2) 
ggplot(stat, aes(Dimensions,NumPops,colour=UniqueTargets)) + 
  stat_summary(fun.y=median, geom="line", aes(group=UniqueTargets,linetype=UniqueTargets), size=.3) + scale_linetype_manual(values=c("solid","dashed","dotted","twodash")) +
  geom_boxplot(size=.3,outlier.size=.4, position="identity") +
  pops_scale + scale_color_brewer(palette="Set1") + 
  labs(colour="Number of unique targets", y="Mean number of populations")
ggsave("~/Dropbox/Work/Papers/TEC/fig3/dimensions_populations_mean.pdf", width=1.75, height=2.25) 
ggsave("~/Dropbox/Work/Papers/TEC/fig3/dimensions_populations_extra.pdf", width=8, height=2.25) 



### Number of agents X number of targets #####

ag <- loadData("agents_*", "fitness.stat", fun=loadFitness, auto.ids.names=c("Experiment","UniqueTargets","Agents"),  filter=bestSoFarEvaluations, filter.par=list(step=5000))
metaAnalysis(lastGen(ag), BestSoFar~UniqueTargets+Agents)

# ggplot(lastGen(ag)[,.(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(UniqueTargets,Agents)], aes(UniqueTargets,Mean,group=Agents,colour=Agents)) + 
#   geom_line() + geom_point(size=1) + geom_errorbar(aes(ymin=Mean-SE, ymax=Mean+SE), width=.3) + geom_hline(yintercept=0.995,linetype=3) +
#   ylab("Maximum fitness score") + ylab("Fitness")
# ggsave("~/Dropbox/Work/Papers/TEC/fig/agents_fitness.pdf", width=3.5, height=3, scale=1) 
# 
# ggplot(lastGen(ag), aes(UniqueTargets,BestSoFar,fill=Agents)) + geom_boxplot()

# number of pops
aghyb <- loadData("agents_*","hybrid.stat", auto.ids.names=c("Experiment","UniqueTargets","Agents"),fun=loadFile, colnames=hyb.cols)

stat <- aghyb[, .(NumPops = mean(NumPops)), by=.(Job,UniqueTargets,Agents)]
#stat[, Ratio := NumPops / factorNum(UniqueTargets)]
#stat[, Difference := NumPops - factorNum(UniqueTargets)]
stat[,.(cor2(NumPops,factorNum(UniqueTargets),method="p"))]
metaAnalysis(stat, NumPops~Agents+UniqueTargets)

ggplot(stat[,.(NumPops=mean(NumPops),SE=se(NumPops)), by=.(UniqueTargets,Agents)], aes(UniqueTargets,NumPops,group=Agents,colour=Agents)) + 
  geom_line(size=0.3) + geom_point(aes(shape=Agents),size=1.5) + ylim(0,NA) + 
  ylab("Mean number of populations") + xlab("Unique targets") + scale_color_brewer(palette="Set1") 
ggsave("~/Dropbox/Work/Papers/TEC/fig3/agents_populations_mean.pdf", width=3.5, height=2.5, scale=1) 

# ggplot(stat, aes(UniqueTargets,NumPops,colour=Agents)) + 
#   stat_summary(fun.y=mean, geom="line", aes(group=Agents), size=.2) +
#   geom_boxplot(size=.2,outlier.size=.4,position="identity") +
#   scale_color_brewer(palette="Set1") + 
#   labs(x="Unique targets", y="Mean number of populations")
# ggsave("~/Dropbox/Work/Papers/TEC/fig2/agents_populations_mean.pdf", width=3.5, height=2.5, scale=1) 




### Initial composition X number of targets #####

initfit <- loadData("init*","fitness.stat", auto.ids.names=c("Experiment","UniqueTargets","InitialPops"),fun=loadFitness, filter=bestSoFarEvaluations, filter.par=list(step=1000))
metaAnalysis(lastGen(initfit), BestSoFar~InitialPops, ~UniqueTargets)

# ggplot(lastGen(initfit)[,.(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(InitialPops,UniqueTargets)], aes(UniqueTargets,Mean,group=InitialPops,colour=InitialPops)) + 
#   geom_line() + geom_point(size=1) + geom_errorbar(aes(ymin=Mean-SE, ymax=Mean+SE), width=.3) + ylim(NA,1) + geom_hline(yintercept=0.995,linetype=3) +
#   ylab("Maximum fitness score") + ylab("Number of unique targets")
# ggsave("~/Dropbox/Work/Papers/TEC/fig/init_fitness.pdf", width=3.5, height=3, scale=1) 

levels <- initfit[, fitnessLevels(.SD,0.995), by=.(Job,InitialPops,UniqueTargets)]
metaAnalysis(levels, Evaluations~InitialPops, ~UniqueTargets)
# ggplot(levels, aes(UniqueTargets,Evaluations)) + geom_boxplot(aes(fill=InitialPops)) +
#   ylab("Evaluations until fitness > 0.995") + xlab("Number of unique targets") + ylim(0,1500000)
# ggsave("~/Dropbox/Work/Papers/TEC/fig/init_evaluations.pdf", width=3.5, height=3, scale=1) 


inithyb <- loadData("init*","hybrid.stat",auto.ids.names=c("Experiment","UniqueTargets","InitialPops"),fun=loadFile, colnames=hyb.cols, parallel=F)
inithyb[, Evaluations := floor(Evaluations / 1000) * 1000]
ggplot(inithyb[, .(NumPops=mean(NumPops)), by=.(Evaluations,UniqueTargets,InitialPops)], aes(Evaluations/1000,NumPops)) + 
  geom_line(aes(colour=InitialPops,group=InitialPops),size=.2) +  scale_color_brewer(palette="Set1") +
  facet_wrap(~ UniqueTargets, labeller=as_labeller(paste("Unique targets:",c(1,3,5,10)))) + 
  pops_scale + xlim(0,500) + labs(colour="Number of initial populations",y="Mean number of populations",x="Evaluations (x1000)")
ggsave("~/Dropbox/Work/Papers/TEC/fig3/init_populations.pdf", width=3.5, height=3.5, scale=1) 
 
smoothed <- inithyb[, .(Evaluations,NumPops=predict(loess(NumPops~Evaluations, span=0.05))) , by=.(UniqueTargets,InitialPops)]
smoothed <- inithyb[, .(Evaluations=seq(from=0,to=max(Evaluations),by=1000),NumPops=predict(loess(NumPops~Evaluations, span=0.01), newdata=seq(from=0,to=max(Evaluations),by=1000))) , by=.(UniqueTargets,InitialPops)]
smoothed[NumPops < 1, NumPops := 1]

ggplot(smoothed, aes(Evaluations/1000,NumPops)) + 
  geom_line(aes(colour=InitialPops,group=InitialPops),size=.3) +  scale_color_brewer(palette="Set1") +
  facet_wrap(~ UniqueTargets, labeller=ut_labeller) + 
  pops_scale + xlim(0,500) + labs(colour="Number of initial populations",y="Mean number of populations",x="Evaluations (x1000)")
ggsave("~/Dropbox/Work/Papers/TEC/fig3/init_populations_smoothed.pdf", width=3.5, height=3.5, scale=1) 


stat <- inithyb[Evaluations > 1000000, .(NumPops = mean(NumPops)), by=.(Job,UniqueTargets,InitialPops)]
metaAnalysis(stat, NumPops~InitialPops, ~UniqueTargets)


### Merge threshold X Maturation X Unique targets ##############

setwd("/media/jorge/Orico/allocationx_par")

grid <- loadData("*","fitness.stat", auto.ids.names=c("Experiment","UniqueTargets","MergeThreshold","Maturation"),fun=loadFitness, filter=bestSoFarEvaluations, filter.par=list(step=1000))
#ggplot(lastGen(grid), aes(MergeThreshold,Maturation)) + geom_tile(aes(fill=BestSoFar)) + facet_wrap(~ UniqueTargets)
grid[, MergeThreshold := factor(MergeThreshold, labels=sub("0\\.","\\.",levels(MergeThreshold)))]

levels <- grid[factorNum(MergeThreshold)<1, fitnessLevels(.SD,0.995, return.failed=T), by=.(Job,MergeThreshold,Maturation,UniqueTargets)]
l <- levels[, .(Evaluations = if(sum(is.finite(Evaluations)) >= 20) mean(Evaluations[is.finite(Evaluations)]) else Inf), by=.(MergeThreshold,Maturation,UniqueTargets)]

ggplot(l, aes(MergeThreshold,Maturation)) + geom_tile(aes(fill=Evaluations/1000)) +facet_wrap(~ UniqueTargets, labeller=ut_labeller) +
  scale_fill_distiller(palette="YlOrRd", direction=1) + theme(legend.key.height=unit(0.3,"line")) +
  scale_x_discrete(expand = c(0, 0)) + scale_y_discrete(expand = c(0, 0)) + labs(x=expression(Merge~threshold~T[M]), y=expression(Maturation~limit~T[L]), fill="Evaluations (x1000)")
ggsave("~/Dropbox/Work/Papers/TEC/fig3/params_evaluations.pdf", width=3.5, height=3.5) 

ggplot(l[, .(Evaluations=mean(Evaluations)), by=.(MergeThreshold,Maturation)], aes(MergeThreshold,Maturation)) + geom_tile(aes(fill=Evaluations/1000)) +
  scale_fill_distiller(palette="YlOrRd", direction=1) + 
  scale_x_discrete(expand = c(0, 0)) + scale_y_discrete(expand = c(0, 0)) +
  labs(x=expression(Merge~threshold~T[M]), y=expression(Maturation~limit~T[L]), fill="Evaluations (x1000)")
ggsave("~/Dropbox/Work/Papers/TEC/fig3/params_evaluations_average.png", width=3.5, height=3.5) 
View(l[, .(Evaluations=mean(Evaluations)), by=.(MergeThreshold,Maturation)])

l[is.infinite(Evaluations), Evaluations := 999999999]
c <- l[UniqueTargets=="1", .(cor(Evaluations,factorNum(MergeThreshold),method="spearman")), by=.(Maturation,UniqueTargets)]
c[,.(mean(V1[-1]),sd(V1[-1]))]
c <- l[UniqueTargets=="10", .(cor(Evaluations,factorNum(MergeThreshold),method="spearman")), by=.(Maturation,UniqueTargets)]
c[,.(mean(V1,na.rm=T),sd(V1,na.rm=T))]
c <- l[UniqueTargets!="10" & UniqueTargets!="1", .(cor(Evaluations,factorNum(MergeThreshold),method="spearman")), by=.(Maturation,UniqueTargets)]
c[,.(mean(V1),sd(V1))]
c <- l[UniqueTargets!="1", .(cor(Evaluations,factorNum(MergeThreshold),method="spearman")), by=.(Maturation,UniqueTargets)]
c[,.(mean(V1,na.rm=T),sd(V1))]

l <- levels[, if(sum(is.finite(Evaluations)) >= 20) .SD, by=.(MergeThreshold,Maturation,UniqueTargets)]
metaAnalysis(l[factorNum(MergeThreshold) >= 0.2 & factorNum(MergeThreshold) <= 0.50 & factorNum(Maturation) >= 20 & factorNum(Maturation) <= 50], Evaluations ~ MergeThreshold + Maturation)




mean.pops <- function(dt) {dt[, .(NumPops=mean(NumPops))]}
gridhyb <- loadData("*","hybrid.stat", auto.ids.names=c("Experiment","UniqueTargets","MergeThreshold","Maturation"),fun=loadFile, colnames=hyb.cols, filter=mean.pops)
gridhyb[, MergeThreshold := factor(MergeThreshold, labels=sub("0\\.","\\.",levels(MergeThreshold)))]

ggplot(gridhyb[factorNum(MergeThreshold)<1,.(NumPops=mean(NumPops)),by=.(MergeThreshold,Maturation,UniqueTargets)], aes(MergeThreshold,Maturation)) + geom_tile(aes(fill=NumPops-factorNum(UniqueTargets))) +
  facet_wrap(~ UniqueTargets, labeller=ut_labeller) + scale_fill_distiller(palette="RdYlBu", limits=c(-9,9)) + #+ scale_fill_gradient2(low="#4575b4", mid="gray95", high="#d8372e") +
  scale_x_discrete(expand = c(0, 0)) + scale_y_discrete(expand = c(0, 0)) + theme(legend.key.height=unit(0.3,"line")) +
  labs(x=expression(Merge~threshold~T[M]), y=expression(Maturation~limit~T[L]), fill="Mean # populations - # unique targets")
ggsave("~/Dropbox/Work/Papers/TEC/fig3/params_pops.pdf", width=3.5, height=3.5) 

# ggplot(gridhyb[,.(NumPops=mean(NumPops)),by=.(MergeThreshold,Maturation,UniqueTargets)], aes(MergeThreshold,Maturation)) + geom_tile(aes(fill=NumPops)) +
#   facet_wrap(~ UniqueTargets) + scale_fill_distiller(palette="RdYlGn") +
#   scale_x_discrete(expand = c(0, 0)) + scale_y_discrete(expand = c(0, 0)) +
#   labs(x=expression(Merge~threshold~T[M]), y=expression(Maturation~limit~T[L]), fill="Mean num. populations")

m <- gridhyb[, .(NumPops=mean(NumPops)), by=.(MergeThreshold,Maturation,UniqueTargets)]
c <- m[, .(cor(NumPops,factorNum(Maturation),method="spearman")), by=.(MergeThreshold,UniqueTargets)]
c[,.(mean(V1),sd(V1))]


### Multirover task ##############

setwd("/media/jorge/Archive/hybccea_tec/multirover_fix")
fixPostFitness(".")

mrfit <- loadData("*","postfitness.stat",fun=loadFitness, filter=bestSoFarEvaluations, auto.ids.names=c("Exp","Agents","Method","Task"), filter.par=list(step=1000))
mrfit[, Task := factor(Task,labels=c("Multirover 2 item types","Multirover 5 item types"))]
mrfit[, Method := factor(Method,levels=c("hybashomo","hybtshomo","ccea"), labels=c("Hyb-CCEA-GC","Hyb-CCEA-TS","CCEA"))]

plotdata <- mrfit[,.(Mean=mean(BestSoFar),SE=se(BestSoFar)),by=.(Method,Task,Evaluations)]
ggplot(plotdata, aes(Evaluations/1000, Mean)) + 
  geom_line(aes(colour=Method),size=0.3) + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE, fill=Method), alpha = 0.2) + 
  geom_point(data=plotdata[, .SD[seq(from=1,to=.N,length.out=10)], by=.(Method,Task)], aes(shape=Method,colour=Method),size=0.8) + scale_shape_manual(values=0:14) +
  facet_wrap(~ Task, scales="free_x") + ylab("Highest fitness") + xlab("Evaluations (x1000)") +
  scale_color_brewer(palette="Set1") + scale_fill_brewer(palette="Set1")
ggsave("~/Dropbox/Work/Papers/TEC/fig3/mr_fitness.pdf", width=3.5, height=2, scale=1) 

metaAnalysis(lastGen(mrfit), BestSoFar ~ Method, ~ Task)

mrhyb <- loadData("*","hybrid.stat", auto.ids.names=c("Exp","Agents","Method","Task"),fun=loadFile, colnames=hyb.cols)
mrhyb[, Task := factor(Task,labels=c("Multirover 2 item types","Multirover 5 item types"))]
mrhyb[, Method := factor(Method,levels=c("hybashomo","hybtshomo","ccea"), labels=c("Hyb-CCEA-GC","Hyb-CCEA-TS","CCEA"))]

smoothed <- mrhyb[, .(Evaluations=seq(from=0,to=max(Evaluations),by=1000),NumPops=predict(loess(NumPops~Evaluations, span=0.1), newdata=seq(from=0,to=max(Evaluations),by=1000))) , by=.(Task,Method)]
smoothed[Evaluations==0, NumPops := 1]
ggplot(smoothed, aes(Evaluations/1000, NumPops,group=Method)) + 
  geom_line(aes(colour=Method), size=0.3) + 
  geom_point(data=smoothed[, .SD[seq(from=1,to=.N,length.out=10)], by=.(Method,Task)], aes(shape=Method,colour=Method),size=0.8) + scale_shape_manual(values=0:14) +
  scale_color_brewer(palette="Set1") +
  ylab("Mean number of populations") + xlab("Evaluations (x1000)") + facet_wrap(~ Task, scales="free_x") +
  scale_y_continuous(minor_breaks=NULL,breaks=1:10, limits=c(1,10))
ggsave("~/Dropbox/Work/Papers/TEC/fig3/mr_pops.pdf", width=3.5, height=2, scale=1) 

metaAnalysis(mrhyb[,.(NumPops=mean(NumPops)),by=.(Task,Method,Job)], NumPops ~ Method, ~ Task)


ggplot(lastGen(mrhyb[Method=="Hyb-CCEA-GC"]), aes(Task, TotalMerges)) + geom_boxplot()


### Soccer task ###############

setwd("~/exps/soccer_fix/")
fixPostFitness(".")

socfit <- loadData("*","postfitness.stat",fun=loadFitness, filter=bestSoFarEvaluations, auto.ids.names=c("Task","Agents","Method"), filter.par=list(step=1000))
socfit[, Task := factor(Task, labels=c("Soccer-80%","Soccer-100%"))]
socfit[, Method := factor(Method,levels=c("hybashomo","hybtshomo","ccea"), labels=c("Hyb-CCEA-GC","Hyb-CCEA-TS","CCEA"))]

plotdata <- socfit[,.(Mean=mean(BestSoFar),SE=se(BestSoFar)),by=.(Method,Task,Evaluations)]
ggplot(plotdata, aes(Evaluations/1000, Mean, group=Method)) + 
  geom_line(aes(colour=Method),size=0.3) + 
  geom_point(data=plotdata[, .SD[seq(from=1,to=.N,length.out=10)], by=.(Method,Task)], aes(shape=Method,colour=Method),size=0.8) + scale_shape_manual(values=0:14) +
  geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE, fill=Method), alpha = 0.25) + 
  facet_wrap(~ Task, scales="free_x") + ylab("Highest fitness") + xlab("Evaluations (x1000)") +
  scale_color_brewer(palette="Set1") + scale_fill_brewer(palette="Set1") 
ggsave("~/Dropbox/Work/Papers/TEC/fig3/soc_fitness.pdf", width=3.5, height=2, scale=1) 


metaAnalysis(lastGen(socfit), BestSoFar ~ Method, ~ Task)

sochyb <- loadData("*","hybrid.stat", auto.ids.names=c("Task","Agents","Method"),fun=loadFile, colnames=hyb.cols)
sochyb[, Task := factor(Task, labels=c("Soccer-80%","Soccer-100%"))]
sochyb[, Method := factor(Method,labels=c("Hyb-CCEA-GC","Hyb-CCEA-TS"))]

smoothed <- sochyb[, .(Evaluations=seq(from=0,to=max(Evaluations),by=1000),NumPops=predict(loess(NumPops~Evaluations, span=0.1), newdata=seq(from=0,to=max(Evaluations),by=1000))) , by=.(Task,Method)]
smoothed[Evaluations==0, NumPops := 1]

ggplot(smoothed, aes(Evaluations/1000, NumPops,group=Method)) + 
  geom_line(aes(colour=Method), size=0.3) + 
  geom_point(data=smoothed[, .SD[seq(from=1,to=.N,length.out=10)], by=.(Method,Task)], aes(shape=Method,colour=Method),size=0.8) + scale_shape_manual(values=0:14) +
  scale_color_brewer(palette="Set1") +
  ylab("Mean number of populations") + xlab("Evaluations (x1000)") + facet_wrap(~ Task, scales="free_x") +
  scale_y_continuous(minor_breaks=NULL,breaks=1:5, limits=c(1,5))
ggsave("~/Dropbox/Work/Papers/TEC/fig3/soc_pops.pdf", width=3.5, height=2, scale=1) 

metaAnalysis(sochyb[,.(NumPops=mean(NumPops)),by=.(Task,Method,Job)], NumPops ~ Method, ~ Task)


### Heterogeneity analysis

longify <- function(data) {
  popids <- sapply(data, function(x){length(unique(x))==1 & is.integer(x)})
  n <- max(data[, popids, with=F])+1
  starts <- which(popids & data[1]==0)
  print(starts)
  nb1 <- (starts[2] - starts[1]) / n - 1
  nb2 <- ((ncol(data)+1) - starts[2]) / n - 1
  cat(n, nb1, nb2)
  new <- data[, transformEntry(.SD, n, nb1, nb2), by=1:nrow(data)][,-1]
  return(new)
}

transformEntry <- function(line, n, nb1, nb2) {
  prelude <- line[,1:4]
  behav <- line[,5:length(line)]
  offset <- n * (nb1 + 1)
  index <- 1
  result <- list()
  for(i in 0:(n-1)) {
    aux <- c(behav[,(2+i*(nb1+1)):(nb1+1+i*(nb1+1))], behav[,(2+offset+i*(nb2+1)):(nb2+1+offset+i*(nb2+1))])
    result[[length(result)+1]] <- aux
  }
  df <- cbind(prelude,rbindlist(result))
  colnames(df) <- c(names(prelude),paste0("TS",1:nb1),paste0("AS",1:nb2))
  df[, Agent := 0:(n-1)]
  return(df)
}

clusterExport(cl, list("longify","transformEntry"))

setwd("/media/jorge/Orico/multirover_fix/")
b0 <- loadData("*_0","postbehaviours.stat",fun=loadBehaviours, auto.ids.names=c("Exp","Agents","Method","Task"), filter=longify, parallel=T)
b1 <- loadData("*_1","postbehaviours.stat",fun=loadBehaviours, auto.ids.names=c("Exp","Agents","Method","Task"), filter=longify, parallel=T)
mrb <- rbind(b0, b1, use.names=T, fill=T)

# best solutions in each run
sample <- mrb[Method != "hybtshomo" & Job %in% (0:4), .SD[which.max(Fitness):(which.max(Fitness)+9)] , by=.(Method,Job,Task)]
rmr <- reduceData(sample, vars=paste0("AS",1:16), method="tsne")
ggplot(red, aes(x=V1, y=V2)) + geom_point(aes(colour=Job,shape=Job), size=2) + 
  coord_fixed() + facet_grid(Task ~ Method)

# best-of-generation solutions
sample <- mrb[Generation %% 20 == 0 & Method != "hybtshomo" & Job %in% (0:4)]
red <- reduceData(sample, vars=paste0("AS",1:16), method="Rtsne")
ggplot(red[Task=="0"], aes(x=V1, y=V2)) + geom_point(aes(colour=factor(Agent)),shape=4, size=1.5) + 
  coord_fixed() + facet_grid(Method ~ Job)
ggplot(red[Task=="1"], aes(x=V1, y=V2)) + geom_point(aes(colour=factor(Agent)),shape=4, size=1.5) + 
  coord_fixed() + facet_grid(Method ~ Job)

# task-specific behaviour analysis
sample <- mrb[, .SD[which.max(Fitness):(which.max(Fitness)+9)] , by=.(Method,Job,Task)]
comp0 <- sample[Task=="0", .(R1=sum(TS1>0.67), R2=sum(TS2>0.67), NR=sum(TS1<=0.67 & TS2<=0.67), Fitness=Fitness[1]) , by=.(Method,Job)]
comp1 <- sample[Task=="1", .(R1=sum(TS1>1.66), R2=sum(TS2>1.66), R3=sum(TS3>1.66), R4=sum(TS4>1.66), R5=sum(TS5>1.66), NR=sum(TS1<=1.66 & TS2<=1.66 & TS3<=1.66 & TS4<=1.66 & TS5<=1.66), Fitness=Fitness[1]) , by=.(Method,Job)]
setorder(comp0, Method,-Fitness)
setorder(comp1, Method,-Fitness)
write.table(comp0, file="~/Dropbox/Work/Papers/17-TEC/revision/mr2_composition.csv", row.names=F)
write.table(comp1, file="~/Dropbox/Work/Papers/17-TEC/revision/mr5_composition.csv", row.names=F)

# same as in barplots (not very good)
sub <- comp0[, .SD[1:5, -"Fitness"][,Job := 1:5], by=.(Method)]
m <- melt(sub, id.vars=c("Method","Job"),variable.name="Specialisation")
ggplot(m, aes(Job,value)) + geom_bar(aes(fill=Specialisation), stat="identity", position="dodge") + facet_wrap(~ Method)
sub <- comp1[, .SD[1:5, -"Fitness"][,Job := 1:5], by=.(Method)]
m <- melt(sub, id.vars=c("Method","Job"),variable.name="Specialisation")
ggplot(m, aes(Job,value)) + geom_bar(aes(fill=Specialisation), stat="identity", position="dodge") + facet_wrap(~ Method)



# knn distances
kdiv <- function(d) {
  k <- knn.dist(d, k=9)
  return(data.table(k=1:9,dist=colMeans(k)))
}

sample <- mrb[Method != "hybtshomo"]
vars <- paste0("AS",1:16)
kdists <- sample[, kdiv(.SD[,vars,with=F]), by=.(Method,Job,Task,Generation)]

ggplot(kdists[,.(MeanDist=mean(dist)),by=.(Method,Task,k)], aes(k,MeanDist,colour=Method)) + geom_line(aes(group=Method)) + geom_point() + facet_wrap(~ Task)


#Low-res detector: A rover that activates its red rock detection sensors at low-res for a period of simulation time greater than any other action.
#Med-res detector: A rover that activates its red rock detection sensors at med-res for a period of simulation time greater than any other action.
#Hi-res detector: A rover that activates its red rock detection sensors at hi-res for a period of simulation time greater than any other action.
#Spec threshold = 0.5. > half the time in the same action


setwd("/media/jorge/Orico/soccer_fix/")
socb <- loadData("*","postbehaviours.stat",fun=loadBehaviours, auto.ids.names=c("Task","Agents","Method"), filter=longify, parallel=T)

# best-of-generation solutions
sample <- socb[Job %in% (0:4) & Generation %% 20 == 0 & Method != "hybtshomo"]
rsoc <- reduceData(sample, vars=paste0("AS",1:18), method="Rtsne")
ggplot(rsoc[Task=="easy"], aes(x=V1, y=V2)) + geom_point(aes(colour=factor(Agent)),shape=4, size=1.5) + 
  coord_fixed() + facet_grid(Method ~ Job)
ggplot(rsoc[Task=="wins"], aes(x=V1, y=V2)) + geom_point(aes(colour=factor(Agent)),shape=4, size=1.5) + 
  coord_fixed() + facet_grid(Method ~ Job)
ggplot(rsoc[Method=="hybashomo"], aes(x=V1, y=V2)) + geom_point(aes(colour=factor(Agent)),shape=4, size=1.5) + 
  coord_fixed() + facet_grid(Task ~ Job)


# best solutions in each run
sample <- socb[Method != "hybtshomo" & Job %in% (0:4), .SD[which.max(Fitness):(which.max(Fitness)+4)] , by=.(Method,Job,Task)]
rsoc <- reduceData(sample, vars=paste0("AS",1:18), method="tsne")
ggplot(rsoc, aes(x=V1, y=V2)) + geom_point(aes(colour=Job,shape=Job), size=2) + 
  coord_fixed() + facet_grid(Task ~ Method)

# task-specific behaviour analysis
sample <- socb[, .SD[which.max(Fitness):(which.max(Fitness)+4)] , by=.(Method,Job,Task)]
# scored in at least 10% of the matches
comp <- sample[, .(Defender=sum(TS1>0.5 & TS3 < 0.1), Attacker=sum(TS1<0.5 & TS3<0.1), Scorer=sum(TS3>=0.1), Fitness=Fitness[1]) , by=.(Task,Method,Job)]

setorder(comp, Task,Method,-Fitness)

write.table(comp, file="~/Dropbox/Work/Papers/17-TEC/revision/soccer_composition.csv", row.names=F)

# same as in barplots (not very good)
sub <- comp[, .SD[1:5, -"Fitness"][,Job := 1:5], by=.(Task,Method)]
m <- melt(sub, id.vars=c("Task","Method","Job"),variable.name="Specialisation")
ggplot(m, aes(Job,value)) + geom_bar(aes(fill=Specialisation), stat="identity", position="dodge") + facet_grid(Task ~ Method)




# metapop analysis

dprox <- function(row, threshold=c(-1,0.2), pops=10) {
  bds <- as.numeric(row[paste0("BD",0:(pops-1))])
  gds <- as.numeric(row[paste0("GD",0:(pops-1))])
  target <- gds[!is.na(bds)& bds > threshold[1] & bds <= threshold[2]]
  meantarget <- ifelse(length(target)>0, mean(target, na.rm=T), NA)
  return(meantarget)
}

dcount <- function(row, threshold=c(-1,0.2), pops=10) {
  bds <- as.numeric(row[paste0("BD",0:(pops-1))])
  return(sum(!is.na(bds) & bds > threshold[1] & bds <= threshold[2]))
}

meancor <- function(data, pops=10) {
  aux <- function(x) {cor(data[[paste0("GD",x)]], data[[paste0("BD",x)]], method="spearman", use="pairwise.complete.obs")}
  return(mean(sapply(0:(pops-1), aux), na.rm=T))
}
meancorr <- function(data, pops=10) {
  aux <- function(x) {cor(data[[paste0("GD",x)]]/data[["SelfG"]], data[[paste0("BD",x)]], method="spearman", use="pairwise.complete.obs")}
  return(mean(sapply(0:(pops-1), aux), na.rm=T))
}

setwd("/media/jorge/Orico/multirover_extra")
f <- loadData("*", filename="metapop.stat", colnames=c("Generation","Evaluations","Subpop","Origin","Agents","Age","Lockdown",paste0("BD",0:9),paste0("GD",0:9)), auto.ids.names=c("Exp","Agents","Method","Task"))
f[, Task := factor(Task,labels=c("Multirover 2 item types","Multirover 5 item types"))]
f[, Method := factor(Method,levels=c("hybashomo","hybtshomo","ccea"), labels=c("Hyb-CCEA-GC","Hyb-CCEA-TS","CCEA"))]

# copy the self distance to other variable
f[, SelfG := get(paste0("GD",.BY[[1]])), by=Subpop] 
# mean to all other genetic
f[, OtherG := rowMeans(.SD[, setdiff(paste0("GD",0:9), paste0("GD",.BY[[1]])), with=F], na.rm=T), by=Subpop]
# mean to close
f[, CloseG := apply(.SD, 1, dprox, c(-Inf,0.2))]
# mean to distant
f[, DistantG := apply(.SD, 1, dprox, c(0.2,Inf))]
# mean to all other behavior
#f[, OtherB := rowMeans(.SD[, paste0("BD",0:9), with=F], na.rm=T), by=Subpop]
# how many other populations in the behaviour distance interval
#f[, CountB := apply(.SD, 1, dcount, c(-Inf,0.2))]

f[, OtherRel := OtherG / SelfG]
f[, CloseRel := CloseG / SelfG]
f[, DistantRel := DistantG / SelfG]

mrdists <- f[, lapply(.SD, mean, na.rm=T), by=.(Task,Method,Job), .SDcols=c("SelfG","OtherG","CloseG", "DistantG","OtherRel","CloseRel","DistantRel")] 
mrcors <- f[, .(Cor=meancorr(.SD)), by=.(Task, Method, Job)]

save(mrdists, file="~/Dropbox/Work/Papers/17-TEC/revision/mrdists.rdata")
save(mrcors, file="~/Dropbox/Work/Papers/17-TEC/revision/mrcors.rdata")

ggplot(mrcors, aes(Task,Cor,fill=Method)) + geom_boxplot()


setwd("/media/jorge/Orico/soccer_extra")
f <- loadData("*", filename="metapop.stat", colnames=c("Generation","Evaluations","Subpop","Origin","Agents","Age","Lockdown",paste0("BD",0:4),paste0("GD",0:4)), auto.ids.names=c("Task","Agents","Method"))
f[, Task := factor(Task, labels=c("Soccer-80%","Soccer-100%"))]
f[, Method := factor(Method,levels=c("hybashomo","hybtshomo","ccea"), labels=c("Hyb-CCEA-GC","Hyb-CCEA-TS","CCEA"))]

# copy the self distance to other variable
f[, SelfG := get(paste0("GD",.BY[[1]])), by=Subpop] 
# mean to all other genetic
f[, OtherG := rowMeans(.SD[, setdiff(paste0("GD",0:4), paste0("GD",.BY[[1]])), with=F], na.rm=T), by=Subpop]
# mean to close
f[, CloseG := apply(.SD, 1, dprox, threshold=c(-Inf,0.2), pops=5)]
# mean to distant
f[, DistantG := apply(.SD, 1, dprox, threshold=c(0.2,Inf), pops=5)]
# mean to all other behavior
#f[, OtherB := rowMeans(.SD[, paste0("BD",0:4), with=F], na.rm=T), by=Subpop]
# how many other populations in the behaviour distance interval
#f[, CountB := apply(.SD, 1, dcount, threshold=c(-Inf,0.2), pops=5)]

f[, OtherRel := OtherG / SelfG]
f[, CloseRel := CloseG / SelfG]
f[, DistantRel := DistantG / SelfG]

socdists <- f[, lapply(.SD, mean, na.rm=T), by=.(Task,Method,Job), .SDcols=c("SelfG","OtherG","CloseG", "DistantG","OtherRel","CloseRel","DistantRel")] 
soccors <- f[, .(Cor=meancorr(.SD, pops=5)), by=.(Task, Method, Job)]

save(socdists, file="~/Dropbox/Work/Papers/17-TEC/revision/socdists.rdata")
save(soccors, file="~/Dropbox/Work/Papers/17-TEC/revision/soccors.rdata")

ggplot(soccors, aes(Task,Cor,fill=Method)) + geom_boxplot()

load("~/Dropbox/Work/Papers/17-TEC/revision/soccors.rdata")
load("~/Dropbox/Work/Papers/17-TEC/revision/mrcors.rdata")
cors <- rbind(mrcors, soccors)
tab <- cors[, .(Correlation=mean(Cor)), by=.(Task,Method)]
setorder(tab, Task, Method)
View(tab)

# merges / splits analysis

setwd("/media/jorge/Orico/multirover_extra")
f <- loadData("*", filename="metapop.stat", colnames=c("Generation","Evaluations","Subpop","Origin","Agents","Age","Lockdown",paste0("BD",0:9),paste0("GD",0:9)), auto.ids.names=c("Exp","Agents","Method","Task"))
f[, Task := factor(Task,labels=c("Multirover 2 item types","Multirover 5 item types"))]
f[, Method := factor(Method,levels=c("hybashomo","hybtshomo","ccea"), labels=c("Hyb-CCEA-GC","Hyb-CCEA-TS","CCEA"))]

setwd("/media/jorge/Orico/soccer_extra")
f2 <- loadData("*", filename="metapop.stat", colnames=c("Generation","Evaluations","Subpop","Origin","Agents","Age","Lockdown",paste0("BD",0:4),paste0("GD",0:4)), auto.ids.names=c("Task","Agents","Method"))
f2[, Task := factor(Task, labels=c("Soccer-80%","Soccer-100%"))]
f2[, Method := factor(Method,levels=c("hybashomo","hybtshomo","ccea"), labels=c("Hyb-CCEA-GC","Hyb-CCEA-TS","CCEA"))]

data <- rbind(f, f2, fill=T)

# lasting change:
# true merge between 2 separate pops
# lasting split
# split that results in further split

shiftsize <- function(d) {
  comp <- unique(d[, .(Generation,N)], by="Generation")
  comp[, NextSize := c(N[-1],NA)]
  merged <- merge(d, comp[,.(Generation,NextSize)], by="Generation")
  return(merged$NextSize)
}

data[, N := .N, by=.(Task,Method,Job,Generation)]
data[, NextSize := shiftsize(.SD), by=.(Task,Method,Job)]

sum <- data[Method!="CCEA", .(NewMerges=sum(Origin=="M" & Age==0), 
                           NewSplits=sum(Origin=="S" & Age == Lockdown + 1) + sum(!is.na(NextSize) & Origin=="S" & Age==Lockdown-1 & NextSize==N+1) / 2, 
                           SplitRemerges=sum(Origin=="R" & Age==0)), by=.(Task, Method, Job)]

sum[, .(NewMerges=mean(NewMerges),NewMerges.SD=sd(NewMerges),NewSplits=mean(NewSplits),NewSplits.SD=sd(NewSplits),SplitRemerges=mean(SplitRemerges),SplitRemerges.SD=sd(SplitRemerges)), by=.(Task,Method)]
ggplot(melt(sum, measure.vars=c("NewMerges","NewSplits","SplitRemerges")), aes(Method, value)) + geom_boxplot(aes(fill=variable)) + facet_wrap(~ Task)

metaAnalysis(sum, NewMerges ~ Method, ~ Task)
metaAnalysis(sum, NewSplits ~ Method, ~ Task)
metaAnalysis(sum, SplitRemerges ~ Method, ~ Task)


# sum[, TotalOps := NewMerges + NewSplits + SplitRemerges]
# sum[, NewMerges := NewMerges / TotalOps]
# sum[, NewSplits := NewSplits / TotalOps]
# sum[, SplitRemerges := SplitRemerges / TotalOps]
# m <- melt(sum, measure.vars=c("NewMerges","NewSplits","SplitRemerges"))
# ggplot(m, aes(Method, value)) + geom_boxplot(aes(fill=variable)) + facet_wrap(~ Task)













### Soccer NS ###############

setwd("~/labmag/exps/socns")
fixPostFitness(".")

socfit <- loadData("*","postfitness.stat",fun=loadFitness, filter=bestSoFarEvaluations, auto.ids.names=c("Task","Agents","Method"), filter.par=list(step=1000))
socfit[, Task := factor(Task, labels=c("Soccer-80%","Soccer-100%"))]
socfit[, Method := factor(Method,labels=c("Fit-CCEA","Fit-Hyb-CCEA","NS-CCEA","NS-Hyb-CCEA","NSTS-CCEA"))]

ggplot(socfit[,.(Mean=mean(BestSoFar),SE=se(BestSoFar)),by=.(Method,Task,Evaluations)], aes(Evaluations/1000, Mean, group=Method)) + 
  geom_line(aes(colour=Method),size=0.3) + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE, fill=Method), alpha = 0.2) + 
  facet_wrap(~ Task, scales="free_x") + ylab("Highest fitness") + xlab("Evaluations (x1000)") +
  scale_color_brewer(palette="Set1") + scale_fill_brewer(palette="Set1") 

sochyb <- loadData("*","hybrid.stat", auto.ids.names=c("Task","Agents","Method"),fun=loadFile, colnames=hyb.cols)
sochyb[, Task := factor(Task, labels=c("Soccer-80%","Soccer-100%"))]
sochyb[, Method := factor(Method,labels=c("Fit-Hyb-CCEA","NS-Hyb-CCEA"))]

smoothed <- sochyb[, .(Evaluations,NumPops=predict(loess(NumPops~Evaluations, span=0.1))) , by=.(Task,Method)]
ggplot(smoothed, aes(Evaluations/1000, NumPops,group=Method)) + 
  geom_line(aes(colour=Method), size=0.3) + 
  scale_colour_manual(values=brewer.pal(3,"Set1")[-1]) +
  ylab("Mean number of populations") + xlab("Evaluations (x1000)") + facet_wrap(~ Task, scales="free_x") +
  scale_y_continuous(minor_breaks=NULL,breaks=1:5, limits=c(1,5))















### TESTS / GARBAGE ############

### OLD ######################################################

### Merge threshold X target setup #####

pd = position_dodge(.5)
mergefit <- loadData("merge_*_1.0_*","fitness.stat", auto.ids.names=c("Experiment","UniqueTargets","Dispersion","MergeThreshold"),fun=loadFitness, filter=bestSoFarEvaluations, filter.par=list(step=1000))
# ggplot(lastGen(mergefit), aes(MergeThreshold,BestSoFar,colour=UniqueTargets)) + 
#   stat_summary(fun.y=median, geom="line", aes(group=UniqueTargets), size=.2, position=pd) +
#   geom_boxplot(size=.3,outlier.size=.4, position=pd) +
#   scale_color_brewer(palette="Set1") + 
#   labs(x="Merge threshold", y="Highest fitness",colour="Unique targets")
# ggsave("~/Dropbox/Work/Papers/TEC/fig2/merge_fitness.pdf", width=3.5, height=2.5, scale=1) 

metaAnalysis(lastGen(mergefit), BestSoFar ~ MergeThreshold, ~UniqueTargets)

levels <- mergefit[, fitnessLevels(.SD,0.995), by=.(Job,MergeThreshold,UniqueTargets)][,if(.N>=15).SD, by=.(MergeThreshold,UniqueTargets)]
ggplot(levels, aes(MergeThreshold,Evaluations/1000,colour=UniqueTargets)) + 
  stat_summary(fun.y=median, geom="line", aes(group=UniqueTargets), size=.2, position=pd) +
  geom_boxplot(size=.3,outlier.size=.4, width=.6, position=pd) +
  scale_color_brewer(palette="Set1") + ylim(0,1500) +
  labs(x=expression(Merge~threshold~(T[M])), y="Evaluations (x1000) until solution",colour="Unique targets")
ggsave("~/Dropbox/Work/Papers/TEC/fig3/merge_evaluations.pdf", width=3.5, height=2.5, scale=1) 

metaAnalysis(levels, Evaluations ~ MergeThreshold, ~UniqueTargets)
levels[, cor(Evaluations,factorNum(MergeThreshold),method="spearman") , by=.(UniqueTargets)]

mergehyb <- loadData("merge_*_1.0_*","hybrid.stat", auto.ids.names=c("Experiment","UniqueTargets","Dispersion","MergeThreshold"),fun=loadFile, colnames=hyb.cols)
stat <- mergehyb[, .(NumPops=mean(NumPops)), by=.(Job,UniqueTargets,MergeThreshold)] 
ggplot(stat, aes(MergeThreshold,NumPops,colour=UniqueTargets)) +
  stat_summary(fun.y=median, geom="line", aes(group=UniqueTargets), size=.2, position=pd) +
  geom_boxplot(size=.3,outlier.size=.2, width=.6, position=pd) +
  scale_color_brewer(palette="Set1") + pops_scale +
  labs(x=expression(Merge~threshold~(T[M])), y="Mean number of populations",colour="Unique targets")
ggsave("~/Dropbox/Work/Papers/TEC/fig3/merge_populations.pdf", width=3.5, height=2.5, scale=1) 

metaAnalysis(stat, NumPops ~ MergeThreshold, ~UniqueTargets)
cor(stat$NumPops, factorNum(stat$MergeThreshold), method="spearman")

metaAnalysis(stat[factorNum(MergeThreshold) > 0.35], NumPops ~ MergeThreshold, ~UniqueTargets)


### Split threshold X target setup #####

pd = position_dodge(.5)
splitfit <- loadData("split_*","fitness.stat", auto.ids.names=c("Experiment","UniqueTargets","MaxLockdown"),fun=loadFitness, filter=bestSoFarEvaluations, filter.par=list(step=1000))
# ggplot(lastGen(splitfit), aes(MaxLockdown,BestSoFar,colour=UniqueTargets)) + 
#   stat_summary(fun.y=median, geom="line", aes(group=UniqueTargets), size=.2, position=pd) +
#   geom_boxplot(size=.3,outlier.size=.4, position=pd) +
#   scale_color_brewer(palette="Set1") + 
#   labs(x="Max. lockdown", y="Highest fitness",colour="Unique targets")
# ggsave("~/Dropbox/Work/Papers/TEC/fig2/split_fitness.pdf", width=3.5, height=2.5, scale=1) 

levels <- splitfit[, fitnessLevels(.SD,0.995), by=.(Job,MaxLockdown,UniqueTargets)][,if(.N>=15).SD, by=.(MaxLockdown,UniqueTargets)]
ggplot(levels, aes(MaxLockdown,Evaluations/1000,colour=UniqueTargets)) + 
  stat_summary(fun.y=median, geom="line", aes(group=UniqueTargets), size=.2, position=pd) +
  geom_boxplot(size=.3,outlier.size=.4, width=.6, position=pd) +
  scale_color_brewer(palette="Set1") + ylim(0,1500) +
  labs(x=expression(Maturation~limit~(T[L])), y="Evaluations (x1000) until solution",colour="Unique targets")
ggsave("~/Dropbox/Work/Papers/TEC/fig3/split_evaluations.pdf", width=3.5, height=2.5, scale=1) 

splithyb <- loadData("split*","hybrid.stat", auto.ids.names=c("Experiment","UniqueTargets","MaxLockdown"),fun=loadFile, colnames=hyb.cols)
ggplot(splithyb[, .(NumPops=mean(NumPops)), by=.(Job,UniqueTargets,MaxLockdown)], aes(MaxLockdown,NumPops,colour=UniqueTargets)) +
  stat_summary(fun.y=median, geom="line", aes(group=UniqueTargets), size=.2, position=pd) +
  geom_boxplot(size=.3,outlier.size=.2, width=.6, position=pd) +
  scale_color_brewer(palette="Set1") + pops_scale +
  labs(x=expression(Maturation~limit~(T[L])), y="Mean number of populations",colour="Unique targets")
ggsave("~/Dropbox/Work/Papers/TEC/fig3/split_populations.pdf", width=3.5, height=2.5, scale=1) 


mergefit[, Targets := factor(paste0("N=",UniqueTargets,",D=",Dispersion))]
mergefit[, Targets := factor(Targets, levels=levels(Targets)[c(1,2,3,4,5,8,7,6)])]

metaAnalysis(lastGen(mergefit), BestSoFar~MergeThreshold, ~Targets)

# fitness
ggplot(lastGen(mergefit)[,.(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(Targets,MergeThreshold)], aes(MergeThreshold,Mean,group=Targets,colour=Targets)) + 
  geom_line() + geom_point(size=1)  + ylab("Maximum fitness") + xlab("Merge threshold") + geom_hline(yintercept = 0.995, linetype=3)
ggsave("~/Dropbox/Work/Papers/TEC/fig/merge_fitness.pdf", width=3.5, height=3, scale=1) 

# evaluations
levels <- mergefit[, fitnessLevels(.SD,0.995), by=.(Job,MergeThreshold,Targets)]
levels.sum <- levels[, .(.N, Mean=mean(Evaluations), SE=se(Evaluations)), by=.(MergeThreshold,Targets)]
metaAnalysis(levels, Evaluations~MergeThreshold, ~Targets)
ggplot(levels.sum[N>=10], aes(MergeThreshold,Mean,group=Targets,colour=Targets)) + geom_line() +
  geom_point(size=1) + geom_errorbar(aes(ymin=Mean-SE, ymax=Mean+SE), width=.2) +
  ylab("Evaluations until fitness > 0.995") + xlab("Number of unique targets") + guides(colour=guide_legend(nrow=2)) + ylim(0,NA)

mergehyb <- loadData("merge*","hybrid.stat", auto.ids.names=c("Experiment","UniqueTargets","Dispersion","MergeThreshold"),fun=loadFile, colnames=hyb.cols)
mergehyb[, Targets := factor(paste0("N=",UniqueTargets,",D=",Dispersion))]
mergehyb[, Targets := factor(Targets, levels=levels(Targets)[c(1,2,3,4,5,8,7,6)])]

stat <- mergehyb[, .(NumPops = mean(NumPops)), by=.(Job,Targets,UniqueTargets,Dispersion,MergeThreshold)]
metaAnalysis(stat, NumPops~MergeThreshold, ~Targets)

ggplot(stat[,.(Mean=mean(NumPops),SE=se(NumPops)), by=.(Targets,MergeThreshold)], aes(MergeThreshold,Mean,group=Targets,colour=Targets)) + 
  geom_line() + geom_point(size=1) + #geom_errorbar(aes(ymin=Mean-SE, ymax=Mean+SE), width=.3) +
  ylab("Mean number of populations") + xlab("Merge treshold") + pops_scale
ggsave("~/Dropbox/Work/Papers/TEC/fig/merge_populations_abs.pdf", width=3.5, height=3, scale=1) 


### Split threshold X number of targets #####

splitfit <- loadData("split*","fitness.stat", auto.ids.names=c("Experiment","UniqueTargets","MaxLockdown"),fun=loadFitness, filter=bestSoFarEvaluations, filter.par=list(step=1000))
metaAnalysis(lastGen(splitfit), BestSoFar~MaxLockdown, ~UniqueTargets)

# fitness
ggplot(lastGen(splitfit)[,.(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(UniqueTargets,MaxLockdown)], aes(MaxLockdown,Mean,group=UniqueTargets,colour=UniqueTargets)) + 
  geom_line() + geom_point(size=1)  + ylab("Maximum fitness") + xlab("Max. lockdown time") + geom_hline(yintercept=0.995,linetype=3)
ggsave("~/Dropbox/Work/Papers/TEC/fig/split_fitness.pdf", width=3.5, height=3, scale=1) 

ggplot(lastGen(splitfit), aes(MaxLockdown,BestSoFar)) + geom_boxplot(aes(fill=UniqueTargets)) + ylab("Maximum fitness") + xlab("Max. lockdown time") + geom_hline(yintercept=0.995,linetype=3)


# evaluations
levels <- splitfit[, fitnessLevels(.SD,0.995), by=.(Job,MaxLockdown,UniqueTargets)]
levels.sum <- levels[, .(.N, Mean=mean(Evaluations), SE=se(Evaluations)), by=.(MaxLockdown,UniqueTargets)]
metaAnalysis(levels, Evaluations~MaxLockdown, ~UniqueTargets)
ggplot(levels.sum[N>=10], aes(MaxLockdown,Mean,group=UniqueTargets,colour=UniqueTargets)) + geom_line() +
  geom_point(size=1) + geom_errorbar(aes(ymin=Mean-SE, ymax=Mean+SE), width=.2) +
  ylab("Evaluations until fitness > 0.995") + xlab("Number of unique targets") + guides(colour=guide_legend(nrow=2)) + ylim(0,NA)

splithyb <- loadData("split*","hybrid.stat", auto.ids.names=c("Experiment","UniqueTargets","MaxLockdown"),fun=loadFile, colnames=hyb.cols)

stat <- splithyb[, .(NumPops = mean(NumPops)), by=.(Job,UniqueTargets,UniqueTargets,MaxLockdown)]
stat[, Ratio := NumPops / as.numeric(as.character(UniqueTargets))]
metaAnalysis(stat, NumPops~MaxLockdown, ~UniqueTargets)

ggplot(stat[,.(Mean=mean(NumPops)), by=.(UniqueTargets,MaxLockdown)], aes(MaxLockdown,Mean,group=UniqueTargets,colour=UniqueTargets)) + 
  geom_hline(aes(yintercept=factorNum(UniqueTargets),colour=UniqueTargets),linetype=3) +
  geom_line() + geom_point(size=1) + #geom_errorbar(aes(ymin=Mean-SE, ymax=Mean+SE), width=.3) +
  ylab("Mean number of pops") + xlab("Max. lockdown time") + pops_scale
ggsave("~/Dropbox/Work/Papers/TEC/fig/split_populations_mean.pdf", width=3.5, height=3, scale=1) 


splithyb[, Evaluations := floor(Evaluations / 1000) * 1000]
ggplot(splithyb[, .(NumPops=mean(NumPops)), by=.(Evaluations,UniqueTargets,MaxLockdown)], aes(Evaluations/1000,NumPops)) + 
  geom_line(aes(colour=MaxLockdown,group=MaxLockdown),size=0.2) + 
  facet_wrap(~ UniqueTargets, labeller=label_both) + 
  geom_hline(aes(yintercept=factorNum(UniqueTargets)), linetype=3) +
  pops_scale + labs(colour="Number of initial populations",y="Mean number of populations",x="Evaluations (x1000)")


fixPostFitness("~/labmag/exps/soclong/")

socfit <- loadData("~/hpc/exps/soclong/*","postfitness.stat",fun=loadFitness, filter=bestSoFarEvaluations, filter.par=list(step=1000))
ggplot(socfit[,.(Mean=mean(BestSoFar),SE=se(BestSoFar)),by=.(Evaluations,ID9)], aes(Evaluations, Mean, group=ID9)) + 
  geom_line(aes(colour=ID9)) + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE, fill=ID9), alpha = 0.15) + ylim(0,0.85)
ggplot(lastGen(socfit), aes(ID9,BestSoFar)) + geom_boxplot() + geom_jitter() + ylim(0,1)


testdists <- function(m, n, tests=1000) {
  aux <- function() {
    data <- t(replicate(m, runif(n)))
    d <- as.matrix(dist(data))
    c <- apply(d, 1, function(x){min(x[x!=0])})
    return(mean(c)) # mean distance to nearest neighbor
  }
  r <- replicate(tests, aux())
  cat("Mean:",mean(r),"SD:",sd(r))
}

socfit <- loadData("easy_5_hybashomo","fitness.stat",fun=loadFitness, filter=bestSoFarEvaluations, auto.ids.names=c("Task","Agents","Method"), filter.par=list(step=1000))
socfit <- loadData("easy_5_hybashomo_fix","fitness.stat",fun=loadFitness, filter=bestSoFarEvaluations, auto.ids.names=c("Task","Agents","Method","NIL"), filter.par=list(step=1000))
ggplot(socfit[,.(Mean=mean(BestSoFar),SE=se(BestSoFar)),by=.(Method,Evaluations)], aes(Evaluations/1000, Mean, group=Method)) + 
  geom_line(aes(colour=Method),size=0.3) + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE, fill=Method), alpha = 0.2) + 
  ylab("Highest fitness") + xlab("Evaluations (x1000)") +
  scale_color_brewer(palette="Set1") + scale_fill_brewer(palette="Set1") 


