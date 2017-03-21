### Base ######

setwd("~/exps/allocationx_fix")
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
comp <- comp[Method != "R-Exch"]

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
ggsave("~/Dropbox/Work/Papers/TEC/fig3/comparison_evaluations.pdf", width=3.5, height=2)

m <- merge(levels[Method=="Hyb-CCEA"],levels[Method=="CCEA-PH"], by=c("Job","UniqueTargets","Threshold"))
cor(m$Evaluations.x,m$Evaluations.y)

### Number of dimensions X number of targets #####

dims <- loadData("dimensions_*", "fitness.stat", fun=loadFitness, auto.ids.names=c("Experiment","UniqueTargets","Dimensions"),  filter=bestSoFarEvaluations, filter.par=list(step=1000))

metaAnalysis(lastGen(dims), BestSoFar~UniqueTargets, ~Dimensions)

ggplot(lastGen(dims), aes(Dimensions,BestSoFar,colour=UniqueTargets)) + 
  stat_summary(fun.y=median, geom="line", aes(group=UniqueTargets,linetype=UniqueTargets), size=.3) + scale_linetype_manual(values=c("solid","dashed","dotted","twodash")) +
  geom_boxplot(size=.3,outlier.size=.4, position="identity") +
  scale_color_brewer(palette="Set1") +ylim(0.985,1) +
  labs(colour="Number of unique targets", y="Highest fitness score")
ggsave("~/Dropbox/Work/Papers/TEC/fig3/dimensions_fitness.pdf", width=1.8, height=2.25) 

# thesis
ggplot(lastGen(dims), aes(Dimensions,BestSoFar,colour=UniqueTargets)) + 
  stat_summary(fun.y=median, geom="line", aes(group=UniqueTargets,linetype=UniqueTargets), size=.3) + scale_linetype_manual(values=c("solid","dashed","dotted","twodash")) +
  geom_boxplot(size=.3,outlier.size=.4, width=.5, position="identity") +
  scale_color_brewer(palette="Set1") +ylim(0.985,1) +
  labs(colour="Number of unique targets", y="Highest fitness score")
ggsave("~/Dropbox/Work/Papers/TEC/fig_thesis/dimensions_fitness_wide.pdf", width=2.2, height=2.25) 


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

# thesis
ggplot(stat, aes(Dimensions,NumPops,colour=UniqueTargets)) + 
  stat_summary(fun.y=median, geom="line", aes(group=UniqueTargets,linetype=UniqueTargets), size=.3) + scale_linetype_manual(values=c("solid","dashed","dotted","twodash")) +
  geom_boxplot(size=.3,outlier.size=.4, width=.5, position="identity") +
  pops_scale + scale_color_brewer(palette="Set1") + 
  labs(colour="Number of unique targets", y="Mean number of populations")
ggsave("~/Dropbox/Work/Papers/TEC/fig_thesis/dimensions_populations_mean.pdf", width=2.2, height=2.25) 


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
stat[,.(cor(NumPops,factorNum(UniqueTargets),method="p"))]
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

setwd("~/exps/allocationx_par")

grid <- loadData("*","fitness.stat", auto.ids.names=c("Experiment","UniqueTargets","MergeThreshold","Maturation"),fun=loadFitness, filter=bestSoFarEvaluations, filter.par=list(step=1000))
#ggplot(lastGen(grid), aes(MergeThreshold,Maturation)) + geom_tile(aes(fill=BestSoFar)) + facet_wrap(~ UniqueTargets)
grid[, MergeThreshold := factor(MergeThreshold, labels=sub("0\\.","\\.",levels(MergeThreshold)))]

ggplot(lastGen(grid), aes(MergeThreshold,Maturation)) + geom_tile(aes(fill=BestSoFar)) +facet_wrap(~ UniqueTargets, labeller=ut_labeller) +
  scale_fill_distiller(palette="YlOrRd", direction=1, limits=c(0.7,1)) + theme(legend.key.height=unit(0.3,"line")) +
  scale_x_discrete(expand = c(0, 0)) + scale_y_discrete(expand = c(0, 0)) +
  labs(x=expression(Merge~threshold~T[M]), y=expression(Maturation~limit~T[L]), fill="Highest fitness scores")
ggsave("~/Dropbox/Work/Papers/TEC/fig_thesis/params_fitness.pdf", width=3.5, height=3.5) 


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

l <- levels[, if(sum(is.finite(Evaluations)) >= 15) .SD, by=.(MergeThreshold,Maturation,UniqueTargets)]
metaAnalysis(l[factorNum(MergeThreshold) >= 0.2 & factorNum(MergeThreshold) <= 0.50 & factorNum(Maturation) >= 20 & factorNum(Maturation) <= 50], Evaluations ~ MergeThreshold + Maturation)


#ggplot(levels[,if(.N>=15).SD, by=.(MergeThreshold,Maturation,UniqueTargets)], aes(MergeThreshold,Maturation)) + geom_tile(aes(fill=Evaluations/1000)) +facet_wrap(~ UniqueTargets)
#ggplot(levels[,.(Evaluations=mean(Evaluations,na.rm=T)),by=.(MergeThreshold,Maturation)], aes(MergeThreshold,Maturation)) + geom_tile(aes(fill=Evaluations/1000))

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

setwd("~/exps/multirover_fix")
fixPostFitness(".")

mrfit <- loadData("*","postfitness.stat",fun=loadFitness, filter=bestSoFarEvaluations, auto.ids.names=c("Exp","Agents","Method","Task"), filter.par=list(step=1000))
mrfit[, Task := factor(Task,labels=c("Multirover 2 rock types","Multirover 5 rock types"))]
mrfit[, Method := factor(Method,levels=c("hybashomo","hybtshomo","ccea"), labels=c("Hyb-CCEA-GC","Hyb-CCEA-TS","CCEA"))]

plotdata <- mrfit[,.(Mean=mean(BestSoFar),SE=se(BestSoFar)),by=.(Method,Task,Evaluations)]
ggplot(plotdata, aes(Evaluations/1000, Mean)) + 
  geom_line(aes(colour=Method),size=0.3) + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE, fill=Method), alpha = 0.2) + 
  geom_point(data=plotdata[, .SD[seq(from=1,to=.N,length.out=10)], by=.(Method,Task)], aes(shape=Method,colour=Method),size=0.8) + scale_shape_manual(values=0:14) +
  facet_wrap(~ Task, scales="free_x") + ylab("Highest fitness") + xlab("Evaluations (x1000)") +
  scale_color_brewer(palette="Set1") + scale_fill_brewer(palette="Set1")
ggsave("~/Dropbox/Work/Papers/TEC/fig3/mr_fitness.pdf", width=3.5, height=2, scale=1) 

metaAnalysis(lastGen(mrfit), BestSoFar ~ Method, ~ Task)

lastGen(mrfit)[, .SD[c(which.max(BestSoFar),which.median(BestSoFar))], by=.(Method,Task)]

mrhyb <- loadData("*","hybrid.stat", auto.ids.names=c("Exp","Agents","Method","Task"),fun=loadFile, colnames=hyb.cols)
mrhyb[, Task := factor(Task,labels=c("Multirover 2 rock types","Multirover 5 rock types"))]
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

lastGen(socfit)[, .SD[c(which.max(BestSoFar),which.median(BestSoFar))], by=.(Method,Task)]

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




### Soccer NS ###############

setwd("~/exps/socns")
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



behavs <- loadData("easy_5_nstsccea", "behaviours.stat" ,fun=loadBehaviours, vars=c("i.distgoal","i.distball","i.scored","g.scored","g.suffered","g.distball","g.ownposs","g.oppposs"))

d <- preSomProcess(behavs, vars=c("g.scored","g.suffered","g.distball","g.ownposs","g.oppposs"), cluster=1000)
s <- buildSom(d)
m <- mapBehaviours(s, behavs)
plotSomFrequency(s, )







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


## NS 

setwd("~/exps/socns/")

d <- loadData(c("easy*","med*","hard*"),"postfitness.stat", fun=loadFitness)
ggplot(d[, .(Mean = mean(BestSoFar), SE=se(BestSoFar)), by=.(ID1,ID2,Generation)], aes(Generation,Mean,group=ID2)) + geom_line(aes(colour=ID2)) + ylab("Fitness") +
  geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE, fill=ID2), alpha = 0.1) + facet_wrap(~ ID1, scales="free")

ggplot(lastGen(d), aes(ID2,BestSoFar)) + geom_boxplot(aes(colour=ID2)) + ylab("Fitness") + facet_wrap(~ ID1, scales="free")

metaAnalysis(lastGen(d), BestSoFar ~ ID2, ~ ID1)

ts <- c("ownscore","oppscore","time","ballgoaldist","possession")
d <- loadData("veryeasy_nsga","behaviours.stat", fun=loadBehaviours, vars=ts, sample=0.25)
plotVarsHist(d, ts, breaks=10)

its <- c("i.dgoal","i.dball","i.scored") ; its.na <- rep(NA,3)
gts <- c("g.ownscore","g.oppscore","g.dballgoal","g.dballteam","g.dballopp") ; gts.na <- rep(NA,5)
ig <- paste0("ig.",1:18) ; ig.na <- rep(NA,18)
gg <- paste0("gg.",1:18) ; gg.na <- rep(NA,18)
d <- loadData("veryeasy_5_fit","behaviours.stat", fun=loadBehaviours, vars=c(its.na,gts,ig.na,gg.na), sample=0.2)



View(d[ID3=="nsts"])
div <- diversity(d[ID3=="nsts"], c("g.ownscore","g.oppscore","g.dballgoal","g.dballteam","g.dballopp"), parallel=F)

somd <- preSomProcess(d, vars=gts, cluster=1000)
s <- buildSom(somd, grid.size=8)
m <- d[, mapBehaviours(s, .SD), by=.(ID3,Job)]
plotSomFrequency(s, m, showMaxFitness=F) + facet_grid(ID3 ~ Job)

div <- diversity(d, c("g.ownscore","g.oppscore","g.dballgoal","g.dballteam","g.dballopp"))
