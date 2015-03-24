setwd("~/exps/pred")
theme_set(theme_bw())
DEF_HEIGHT=3.5
DEF_WIDTH=5

# DEF_HEIGHT=3
# DEF_WIDTH=4.5


########## Fitness-driven coevolution

# task difficulty - fitness

data <- metaLoadData("fit_e4","fit_e7","fit_e10","fit_e13", names=c("V=4","V=7","V=10","V=13"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
fullStatistics(data, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", som.group.par=list(distance.filter=0.25),fit.comp.par=list(snapshots=c(500),jitter=F))

# number of collaborators x setup - fitness

data0 <- metaLoadData("fit_e4_p2","fit_e4","fit_e7","fit_e10","fit_e13", names=c("V4.P2", "V4","V7","V10","V13"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data2 <- metaLoadData("fit_e4_p2_r2","fit_e4_r2","fit_e7_r2","fit_e10_r2","fit_e13_r2", names=c("V4.P2", "V4","V7","V10","V13"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data5 <- metaLoadData("fit_e4_p2_r5","fit_e4_r5","fit_e7_r5","fit_e10_r5","fit_e13_r5", names=c("V4.P2", "V4","V7","V10","V13"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data10 <- metaLoadData("fit_e4_p2_r10","fit_e4_r10","fit_e7_r10","fit_e10_r10","fit_e13_r10", names=c("V4.P2", "V4","V7","V10","V13"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))

frame <- cbind(fitnessSummary(data0), N="0")
frame <- rbind(frame, cbind(fitnessSummary(data2), N="2"))
frame <- rbind(frame, cbind(fitnessSummary(data5), N="5"))
frame <- rbind(frame, cbind(fitnessSummary(data10), N="10"))

pd <- position_dodge(.1) # move them .05 to the left and right
ggplot(frame, aes(x=N, y=mean, colour=method)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.25, position=pd) +
  geom_line(position=pd,aes(group=method)) +
  geom_point(position=pd) + ylab("Best fitness") + theme(legend.title=element_blank())

# number of collaborations x team exploration

data42 <- metaLoadData("fit_e4_p2","fit_e4_p2_r2","fit_e4_p2_r5","fit_e4_p2_r10", names=c("Fit-N0","Fit-N2","Fit-N5","Fit-N10"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=T, behavs.sample=0.1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data4 <- metaLoadData("fit_e4","fit_e4_r2","fit_e4_r5","fit_e4_r10", names=c("Fit-N0","Fit-N2","Fit-N5","Fit-N10"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=T, behavs.sample=0.1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data7 <- metaLoadData("fit_e7","fit_e7_r2","fit_e7_r5","fit_e7_r10", names=c("Fit-N0","Fit-N2","Fit-N5","Fit-N10"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=T, behavs.sample=0.1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data10 <- metaLoadData("fit_e10","fit_e10_r2","fit_e10_r5","fit_e10_r10", names=c("Fit-N0","Fit-N2","Fit-N5","Fit-N10"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=T, behavs.sample=0.1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data13 <- metaLoadData("fit_e13","fit_e13_r2","fit_e13_r5","fit_e13_r10", names=c("Fit-N0","Fit-N2","Fit-N5","Fit-N10"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=T, behavs.sample=0.1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))

count42 <- exploration.count(data42)
count4 <- exploration.count(data4)
count7 <- exploration.count(data7)
count10 <- exploration.count(data10)
count13 <- exploration.count(data13)

frame <- cbind(uniformity(count42,mode="jsd")$summary, N=c("0","2","5","10"), Setup="V4.P2")
frame <- rbind(frame, cbind(uniformity(count4,mode="jsd")$summary, N=c("0","2","5","10"), Setup="V4"))
frame <- rbind(frame, cbind(uniformity(count7,mode="jsd")$summary, N=c("0","2","5","10"), Setup="V7"))
frame <- rbind(frame, cbind(uniformity(count10,mode="jsd")$summary, N=c("0","2","5","10"), Setup="V10"))
frame <- rbind(frame, cbind(uniformity(count13,mode="jsd")$summary, N=c("0","2","5","10"), Setup="V13"))

pd <- position_dodge(.25) # move them .05 to the left and right
ggplot(frame, aes(x=N, y=mean, colour=Setup)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.5, position=pd) +
  geom_line(position=pd,aes(group=Setup)) +
  geom_point(position=pd) + ylab("Team exploration") + theme(legend.title=element_blank()) + scale_x_discrete(limits=c("0","2","5","10"))

# number of collaborations x individual exploration

count42 <- exploration.count(data42, vars=data42[[1]]$vars.ind)
count4 <- exploration.count(data4, vars=data4[[1]]$vars.ind)
count7 <- exploration.count(data7, vars=data7[[1]]$vars.ind)
count10 <- exploration.count(data10, vars=data10[[1]]$vars.ind)
count13 <- exploration.count(data13, vars=data13[[1]]$vars.ind)

frame <- cbind(uniformity.ind(count42,mode="jsd")$summary, N=c("0","2","5","10"), Setup="V4.P2")
frame <- rbind(frame, cbind(uniformity.ind(count4,mode="jsd")$summary, N=c("0","2","5","10"), Setup="V4"))
frame <- rbind(frame, cbind(uniformity.ind(count7,mode="jsd")$summary, N=c("0","2","5","10"), Setup="V7"))
frame <- rbind(frame, cbind(uniformity.ind(count10,mode="jsd")$summary, N=c("0","2","5","10"), Setup="V10"))
frame <- rbind(frame, cbind(uniformity.ind(count13,mode="jsd")$summary, N=c("0","2","5","10"), Setup="V13"))

pd <- position_dodge(.25) # move them .05 to the left and right
ggplot(frame, aes(x=N, y=mean, colour=Setup)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.5, position=pd) +
  geom_line(position=pd,aes(group=Setup)) +
  geom_point(position=pd) + ylab("Individual exploration") + theme(legend.title=element_blank()) + scale_x_discrete(limits=c("0","2","5","10"))


# number of collaborations x elite individuals

frame <- cbind(individuals.count(data42, min.fit=individuals.quantile(data42,0.9))$summary, N=c("0","2","5","10"), Setup="V4.P2")
frame <- rbind(frame, cbind(individuals.count(data4, min.fit=individuals.quantile(data4, 0.9))$summary, N=c("0","2","5","10"), Setup="V4"))
frame <- rbind(frame, cbind(individuals.count(data7, min.fit=individuals.quantile(data7, 0.9))$summary, N=c("0","2","5","10"), Setup="V7"))
frame <- rbind(frame, cbind(individuals.count(data10, min.fit=individuals.quantile(data10, 0.9))$summary, N=c("0","2","5","10"), Setup="V10"))
frame <- rbind(frame, cbind(individuals.count(data13, min.fit=individuals.quantile(data13, 0.9))$summary, N=c("0","2","5","10"), Setup="V13"))

pd <- position_dodge(.25) # move them .05 to the left and right
ggplot(frame, aes(x=N, y=mean, colour=Setup)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.5, position=pd) +
  geom_line(position=pd,aes(group=Setup)) +
  geom_point(position=pd) + ylab("Proportion of high fitness individuals") + theme(legend.title=element_blank()) + scale_x_discrete(limits=c("0","2","5","10"))


# number of collaborations x elite exploration

# count42.el <- exploration.count(data42, min.fit=1.25)
# count4.el <- exploration.count(data4, min.fit=1.5)
# count7.el <- exploration.count(data7, min.fit=0.75)
# count10.el <- exploration.count(data10, min.fit=0.3)
# count13.el <- exploration.count(data13, min.fit=0.3)
# 
# frame <- cbind(uniformity(count42.el,mode="jsd")$summary, N=c("0","2","5","10"), Setup="V4.P2")
# frame <- rbind(frame, cbind(uniformity(count4.el,mode="jsd")$summary, N=c("0","2","5","10"), Setup="V4"))
# frame <- rbind(frame, cbind(uniformity(count7.el,mode="jsd")$summary, N=c("0","2","5","10"), Setup="V7"))
# frame <- rbind(frame, cbind(uniformity(count10.el,mode="jsd")$summary, N=c("0","2","5","10"), Setup="V10"))
# frame <- rbind(frame, cbind(uniformity(count13.el,mode="jsd")$summary, N=c("0","2","5","10"), Setup="V13"))
# 
# ggplot(frame, aes(x=N, y=mean, colour=Setup)) + 
#   geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.25, position=pd) +
#   geom_line(position=pd,aes(group=Setup)) +
#   geom_point(position=pd) + ylab("Elite exploration") + theme(legend.title=element_blank()) + scale_x_discrete(limits=c("0","2","5","10"))



####### OVERCOMING PREMATURE CONVERGENCE

data4 <- metaLoadData("fit_e4","nov_e4","nov_e4_ind","nov_e4_indgroup", names=c("Fit","NS-Team","NS-Ind","NS-Mix"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data7 <- metaLoadData("fit_e7","nov_e7","nov_e7_ind","nov_e7_indgroup", names=c("Fit","NS-Team","NS-Ind","NS-Mix"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data10 <- metaLoadData("fit_e10","nov_e10","nov_e10_ind","nov_e10_indgroup", names=c("Fit","NS-Team","NS-Ind","NS-Mix"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data13 <- metaLoadData("fit_e13","nov_e13","nov_e13_ind","nov_e13_indgroup", names=c("Fit","NS-Team","NS-Ind","NS-Mix"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))

# task difficulty x fitness x method

frame <- cbind(fitnessSummary(data4), V="4")
frame <- rbind(frame, cbind(fitnessSummary(data7), V="7"))
frame <- rbind(frame, cbind(fitnessSummary(data10), V="10"))
frame <- rbind(frame, cbind(fitnessSummary(data13), V="13"))

pd <- position_dodge(.1) # move them .05 to the left and right
ggplot(frame, aes(x=V, y=mean, colour=method, ymin=0, ymax=2)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.5, position=pd) +
  geom_line(position=pd,aes(group=method)) +
  geom_point(position=pd) + ylab("Best fitness") + theme(legend.title=element_blank())

# per-generation fitness plots

fullStatistics(data4, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", som.group.par=list(distance.filter=0.25),fit.comp.par=list(snapshots=c(500),jitter=F))
fullStatistics(data7, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", som.group.par=list(distance.filter=0.25),fit.comp.par=list(snapshots=c(500),jitter=F))
fullStatistics(data10, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", som.group.par=list(distance.filter=0.25),fit.comp.par=list(snapshots=c(500),jitter=F))
fullStatistics(data13, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", som.group.par=list(distance.filter=0.25),fit.comp.par=list(snapshots=c(500),jitter=F))

# exploration

frame <- data.frame()
es <- c("4","7","10","13")
for(i in 1:4) {
  e <- es[i]
  data <- metaLoadData(paste0("fit_e",e),paste0("nov_e",e),paste0("nov_e",e,"_ind"),paste0("nov_e",e,"_indgroup"), names=c("Fit","NS-Team","NS-Ind","NS-Mix"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=T, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
  q <- individuals.quantile(data, 0.9)
  count <- exploration.count(data)
  frame <- rbind(frame, cbind(uniformity(count, mode="jsd")$summary, Type="Team", V=e, Method=names(data)))
#   count.el <- exploration.count(data, min.fit=q)
#   frame <- rbind(frame, cbind(uniformity(count.el, mode="jsd")$summary, Type="Elite", V=e, Method=names(data)))
  count.ind <- exploration.count(data, vars=data[[1]]$vars.ind)
  frame <- rbind(frame, cbind(uniformity.ind(count.ind, mode="jsd")$summary, Type="Individual", V=e, Method=names(data)))
  amount <- individuals.count(data, min.fit=q)
  frame <- rbind(frame, cbind(amount$summary, Type="Amount", V=e, Method=names(data)))
  rm(data) ; gc()
}

write.csv(frame, file="~/Dropbox/Papers/EC/data/exploration2_nsvariants.csv", quote=F)
frame <- read.csv("~/Dropbox/Papers/EC/data/exploration2_nsvariants.csv")
frame[["V"]] <- factor(frame[["V"]])

pd <- position_dodge(.1) # move them .05 to the left and right
ggplot(subset(frame, Type=="Team"), aes(x=V, y=mean, colour=Method)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=0.5, position=pd) +
  geom_line(position=pd,aes(group=Method)) +
  geom_point(position=pd) + ylab("Team exploration") + theme(legend.title=element_blank())
ggplot(subset(frame, Type=="Individual"), aes(x=V, y=mean, colour=Method)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=0.5, position=pd) +
  geom_line(position=pd,aes(group=Method)) +
  geom_point(position=pd) + ylab("Individual exploration") + theme(legend.title=element_blank())
ggplot(subset(frame, Type=="Amount"), aes(x=V, y=mean, colour=Method)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=0.5, position=pd) +
  geom_line(position=pd,aes(group=Method)) +
  geom_point(position=pd) + ylab("High-fitness proportion") + theme(legend.title=element_blank())
# ggplot(subset(frame, Type=="Elite"), aes(x=V, y=mean, colour=Method)) + 
#   geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=0.5, position=pd) +
#   geom_line(position=pd,aes(group=Method)) +
#   geom_point(position=pd) + ylab("Elite exploration") + theme(legend.title=element_blank())

########## Exploration-exploitation balance ####

data <- metaLoadData("fit_e7","nov_e7_l10","nov_e7_l20","nov_e7_l30","nov_e7_l40","nov_e7","nov_e7_l60","nov_e7_l70","nov_e7_l80","nov_e7_l90","nov_e7_l100", names=c("LS-0.0","LS-0.1","LS-0.2","LS-0.3","LS-0.4","LS-0.5","LS-0.6","LS-0.7","LS-0.8","LS-0.9","LS-1.0"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=T, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))

# fitness plot

frame <- cbind(fitnessSummary(data, snapshots=c(50)), Gen="50", Rho=seq(0,1,by=0.1))
frame <- rbind(frame, cbind(fitnessSummary(data, snapshots=c(100)), Gen="100", Rho=seq(0,1,by=0.1)))
frame <- rbind(frame, cbind(fitnessSummary(data, snapshots=c(300)), Gen="300", Rho=seq(0,1,by=0.1)))
frame <- rbind(frame, cbind(fitnessSummary(data, snapshots=c(500)), Gen="500", Rho=seq(0,1,by=0.1)))

pd <- position_dodge(.02) # move them .05 to the left and right
ggplot(frame, aes(x=Rho, y=mean, colour=Gen)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=0.1, position=pd) +
  geom_line(position=pd,aes(group=Gen)) +
  geom_point(position=pd) + ylab("Best fitness") + theme(legend.title=element_blank()) + scale_x_continuous(breaks=seq(0,1,by=0.1))

# exploration plot

count <- exploration.count(data)
count.ind <- exploration.count(data, vars=data[[1]]$vars.ind)
# count.el <- exploration.count(data, min.fit=individuals.quantile(data, 0.85))

q <- individuals.quantile(data, q=0.9)
frame <- cbind(uniformity(count, mode="jsd")$summary, Type="Team",Rho=seq(0,1,by=0.1))
uniformity.ind(count.ind)
frame <- rbind(frame, cbind(individuals.count(data, min.fit=q)$summary, Type="Proportion", Rho=seq(0,1,by=0.1)))


# frame <- rbind(frame, cbind(uniformity(count.el,mode="jsd")$summary, Type="Elite", Rho=seq(0,1,by=0.1)))
#frame <- rbind(frame, cbind(uniformity.ind(count.ind,mode="jsd")$summary, Type="Individual", Rho=seq(0,1,by=0.1)))

write.csv(frame, file="~/Dropbox/Papers/EC/data/rho2.csv", quote=F)

pd <- position_dodge(.02) # move them .05 to the left and right
ggplot(frame, aes(x=Rho, y=mean, colour=Type)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=0.1, position=pd) +
  geom_line(position=pd,aes(group=Type)) +
  geom_point(position=pd) + ylab("Exploration") + theme(legend.title=element_blank()) + scale_x_continuous(breaks=seq(0,1,by=0.1))


####### DIVERSITY OF SOLUTIONS ####################

data <- metaLoadData("fit_e4","nov_e4", names=c("Fit","NS-Team"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=T, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
soms <- fullStatistics(data, fit.comp=F, show.only=T, som.group=T, som.alljobs=T, som.ind=T, expset.name="nsfit_e4", som.ind.par=list(distance.filter=0.1) ,som.group.par=list(distance.filter=0.25),fit.comp.par=list(snapshots=c(500),jitter=F))
soms <- fullStatistics(data, fit.comp=F, show.only=T, som.group=T, som.alljobs=T, som.ind=F, expset.name="nsfit_e4", som.ind.par=list(distance.filter=0) ,som.group.par=list(distance.filter=0),fit.comp.par=list(snapshots=c(500),jitter=F))

# allMapCount <- function(map, data) {
#   allcount <- NULL
#   for(job in data$jobs) {
#     if(is.null(allcount)) {
#       allcount <- map[[job]]$all$count
#     } else {
#       allcount <- allcount + map[[job]]$all$count
#     }
#   }
#   allcount <- allcount / data$njobs
#   allmap <- map[[1]]$all # arbitrary one
#   allmap$count <- allcount
#   return(allmap)
# }
# 
# map <- mapMergeSubpops(soms$group, data[[1]])
# allmap <- allMapCount(map, data[[1]])
# somPlot(soms$group, allmap, limit.max=0.05, size.max=30, title="Fit")
# map2 <- mapMergeSubpops(soms$group, data[[2]])
# allmap2 <- allMapCount(map2, data[[2]])
# somPlot(soms$group, allmap2, limit.max=0.05, size.max=30, title="NS-Team")
# fitnessHeatmapPlots(soms$group)
# plot(soms$group)

# out = 6.2x4.3in
map <- mapMergeSubpops(soms$group, data[[1]])
somPlot(soms$group, map$job.0$all, limit.max=0.1, size.max=30, title="Fit")
map2 <- mapMergeSubpops(soms$group, data[[2]])
somPlot(soms$group, map2$job.3$all, limit.max=0.1, size.max=30, title="NS-Team")
fitnessHeatmapPlots(soms$group)
plot(soms$group)

data <- loadData("nov_e4", jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=T, behavs.sample=1, behavs.file="rebehaviours.stat", vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time"))
identifyBests(soms$group, data, outfile="~/Dropbox/Papers/EC/bests.csv")

####### SCALABILITY ################

# fitness x number of agents x method

data2 <- metaLoadData("fit_e7_p2","nov_e7_p2", names=c("Fit","NS-Team"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data3 <- metaLoadData("fit_e7","nov_e7", names=c("Fit","NS-Team"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data5 <- metaLoadData("fit_e7_p5","nov_e7_p5", names=c("Fit","NS-Team"), params=list(jobs=30, subpops=5, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data7 <- metaLoadData("fit_e7_p7","nov_e7_p7", names=c("Fit","NS-Team"), params=list(jobs=30, subpops=7, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))

frame <- cbind(fitnessSummary(data2), A="2")
frame <- rbind(frame, cbind(fitnessSummary(data3), A="3"))
frame <- rbind(frame, cbind(fitnessSummary(data5), A="5"))
frame <- rbind(frame, cbind(fitnessSummary(data7), A="7"))

pd <- position_dodge(.1) # move them .05 to the left and right
ggplot(frame, aes(x=A, y=mean, colour=method)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.5, position=pd) +
  geom_line(position=pd,aes(group=method)) +
  geom_point(position=pd) + ylab("Best fitness") + xlab("Number of predators") + theme(legend.title=element_blank())

#fullStatistics(data7, fit.comp=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", som.group.par=list(distance.filter=0.25),fit.comp.par=list(snapshots=c(500),jitter=F))

data2 <- metaLoadData("fit_e4_p2","nov_e4_p2", names=c("Fit","NS-Team"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data3 <- metaLoadData("fit_e7","nov_e7", names=c("Fit","NS-Team"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data5 <- metaLoadData("fit_e10_p5","nov_e10_p5", names=c("Fit","NS-Team"), params=list(jobs=30, subpops=5, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data7 <- metaLoadData("fit_e13_p7","nov_e13_p7", names=c("Fit","NS-Team"), params=list(jobs=30, subpops=7, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))

frame <- cbind(fitnessSummary(data2), A="2")
frame <- rbind(frame, cbind(fitnessSummary(data3), A="3"))
frame <- rbind(frame, cbind(fitnessSummary(data5), A="5"))
frame <- rbind(frame, cbind(fitnessSummary(data7), A="7"))

pd <- position_dodge(.1) # move them .05 to the left and right
ggplot(frame, aes(x=A, y=mean, colour=method)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.5, position=pd) +
  geom_line(position=pd,aes(group=method)) +
  geom_point(position=pd) + ylab("Best fitness") + xlab("Number of predators") + theme(legend.title=element_blank())

# scalability 2

a1 <- metaAnalysis(list(e7p2=countNear("nov_e7_p2",2,0.15),e7p3=countNear("nov_e7",3,0.15),e7p5=countNear("nov_e7_p5",5,0.15),e7p7=countNear("nov_e7_p7",7,0.15)))
a2 <- metaAnalysis(list(e4p2=countNear("nov_e4_p2",2,0.09),e7p3=countNear("nov_e7",3,0.15),e10p5=countNear("nov_e10_p5",5,0.21),e13p7=countNear("nov_e13_p7",7,0.27)))

frame <- cbind(a1$summary, A=c("2","3","5","7"), V="Fixed V")
frame <- rbind(frame, cbind(a2$summary, A=c("2","3","5","7"), V="Increasing V"))

pd <- position_dodge(.1) # move them .05 to the left and right
ggplot(frame, aes(x=A, y=mean, colour=V)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.5, position=pd) +
  geom_line(position=pd,aes(group=V)) +
  geom_point(position=pd) + ylab("Predators used in best solutions") + xlab("Total number of predators") + theme(legend.title=element_blank())


# data2 <- metaLoadData("nov_e7_p2","nov_e4_p2", names=c("Fixed V","Increasing"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
# data3 <- metaLoadData("nov_e7","nov_e7", names=c("Fixed V","Increasing"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
# data5 <- metaLoadData("nov_e7_p5","nov_e10_p5", names=c("Fixed V","Increasing"), params=list(jobs=30, subpops=5, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
# data7 <- metaLoadData("nov_e7_p7","nov_e13_p7", names=c("Fixed V","Increasing"), params=list(jobs=30, subpops=7, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
# 
# frame <- cbind(fitnessSummary(data2), A="2")
# frame <- rbind(frame, cbind(fitnessSummary(data3), A="3"))
# frame <- rbind(frame, cbind(fitnessSummary(data5), A="5"))
# frame <- rbind(frame, cbind(fitnessSummary(data7), A="7"))
# 
# pd <- position_dodge(.1) # move them .05 to the left and right
# ggplot(frame, aes(x=A, y=mean, colour=method)) + 
#   geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.5, position=pd) +
#   geom_line(position=pd,aes(group=method)) +
#   geom_point(position=pd) + ylab("Best fitness") + xlab("Total number of predators") + theme(legend.title=element_blank())



# Herding & Multi-rover task

setwd("~/exps/herding")
data <- metaLoadData("fit","nov","nov_ind","nov_indgroup", names=c("Fit","NS-T","NS-I","NS-Mix"), params=list(fitness.file="refitness.stat", offset=0, jobs=30, subpops=5, load.behavs=T, behavs.sample=0.2, vars.ind=c("sheepDist","curralDist","fox1Dist","fox2Dist"), vars.group=c("sheepCurral","time","sheepFence","sheepFox")))

setwd("~/exps/multirover")
data <- metaLoadData("fit","nov","nov_ind","nov_indgroup", names=c("Fit","NS-T","NS-I","NS-Mix"), params=list(fitness.file="refitness.stat", offset=0, jobs=30, subpops=2, load.behavs=T, behavs.sample=0.2, vars.ind=c("ind.mov","ind.prox","lowActive","highActive"), vars.group=c("captured","distance","movement","proximity")))

count.g <- exploration.count(data)
uni.g <- uniformity(count.g,mode="jsd")
count.i <- exploration.count(data, vars=data[[1]]$vars.ind)
uni.i <- uniformity.ind(count.i, mode="jsd")
q <- individuals.quantile(data, 0.9)
prop <- individuals.count(data, min.fit=q)

setwd("~/exps/herding")
data <- metaLoadData("fit","nov","nov_ind","nov_indgroup", names=c("Fit","NS-T","NS-I","NS-Mix"), params=list(fitness.file="refitness.stat", offset=0, jobs=30, subpops=5, load.behavs=F))
setwd("~/exps/multirover")
data <- metaLoadData("fit","nov","nov_ind","nov_indgroup", names=c("Fit","NS-T","NS-I","NS-Mix"), params=list(fitness.file="refitness.stat", offset=0, jobs=30, subpops=2, load.behavs=F))

fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(500), jitter=F), expset.name="herd", show.only=T)



# scalability 2


metaAnalysis(list(e7p2=countNear("nov_e7_p2",2,0.15),e7p3=countNear("nov_e7",3,0.15),e7p5=countNear("nov_e7_p5",5,0.15),e7p7=countNear("nov_e7_p7",7,0.15)))
metaAnalysis(list(e4p2=countNear("nov_e4_p2",2,0.09),e7p3=countNear("nov_e7",3,0.15),e10p5=countNear("nov_e10_p5",5,0.21),e13p7=countNear("nov_e13_p7",7,0.27)))
metaAnalysis(list(e4p2=countNear("fit_e4_p2",2,0.09),e7p3=countNear("fit_e7",3,0.15),e10p5=countNear("fit_e10_p5",5,0.21),e13p7=countNear("fit_e13_p7",7,0.27)))




# t-tests -- fitness

data <- metaLoadData("fit_e4_p2","fit_e4_p2_r2","fit_e4_p2_r5","fit_e4_p2_r10", params=list(jobs=30, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F))
fullStatistics(data, fit.tests=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", fit.comp.par=list(snapshots=c(500),jitter=F))
data <- metaLoadData("fit_e4","fit_e4_r2","fit_e4_r5","fit_e4_r10", params=list(jobs=30, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F))
fullStatistics(data, fit.tests=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", fit.comp.par=list(snapshots=c(500),jitter=F))
data <- metaLoadData("fit_e7","fit_e7_r2","fit_e7_r5","fit_e7_r10", params=list(jobs=30, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F))
fullStatistics(data, fit.tests=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", fit.comp.par=list(snapshots=c(500),jitter=F))
data <- metaLoadData("fit_e10","fit_e10_r2","fit_e10_r5","fit_e10_r10", params=list(jobs=30, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F))
fullStatistics(data, fit.tests=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", fit.comp.par=list(snapshots=c(500),jitter=F))
data <- metaLoadData("fit_e13","fit_e13_r2","fit_e13_r5","fit_e13_r10", params=list(jobs=30, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F))
fullStatistics(data, fit.tests=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", fit.comp.par=list(snapshots=c(500),jitter=F))


data <- metaLoadData("fit_e4","nov_e4","nov_e4_ind","nov_e4_indgroup", names=c("Fit","NS-Team","NS-Ind","NS-Mix"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F))
fullStatistics(data, fit.tests=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", fit.comp.par=list(snapshots=c(500),jitter=F))
data <- metaLoadData("fit_e7","nov_e7","nov_e7_ind","nov_e7_indgroup", names=c("Fit","NS-Team","NS-Ind","NS-Mix"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F))
fullStatistics(data, fit.tests=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", fit.comp.par=list(snapshots=c(500),jitter=F))
data <- metaLoadData("fit_e10","nov_e10","nov_e10_ind","nov_e10_indgroup", names=c("Fit","NS-Team","NS-Ind","NS-Mix"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F))
fullStatistics(data, fit.tests=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", fit.comp.par=list(snapshots=c(500),jitter=F))
data <- metaLoadData("fit_e13","nov_e13","nov_e13_ind","nov_e13_indgroup", names=c("Fit","NS-Team","NS-Ind","NS-Mix"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F))
fullStatistics(data, fit.tests=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", fit.comp.par=list(snapshots=c(500),jitter=F))


data <- metaLoadData("fit_e7_p2","nov_e7_p2", names=c("Fit","NS-Team"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
fullStatistics(data, fit.tests=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", fit.comp.par=list(snapshots=c(500),jitter=F))
data <- metaLoadData("fit_e7","nov_e7", names=c("Fit","NS-Team"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
fullStatistics(data, fit.tests=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", fit.comp.par=list(snapshots=c(500),jitter=F))
data <- metaLoadData("fit_e7_p5","nov_e7_p5", names=c("Fit","NS-Team"), params=list(jobs=30, subpops=5, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
fullStatistics(data, fit.tests=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", fit.comp.par=list(snapshots=c(500),jitter=F))
data <- metaLoadData("fit_e7_p7","nov_e7_p7", names=c("Fit","NS-Team"), params=list(jobs=30, subpops=7, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
fullStatistics(data, fit.tests=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", fit.comp.par=list(snapshots=c(500),jitter=F))

fullStatistics(data2, fit.tests=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", fit.comp.par=list(snapshots=c(500),jitter=F))
fullStatistics(data3, fit.tests=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", fit.comp.par=list(snapshots=c(500),jitter=F))
fullStatistics(data5, fit.tests=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", fit.comp.par=list(snapshots=c(500),jitter=F))
fullStatistics(data7, fit.tests=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", fit.comp.par=list(snapshots=c(500),jitter=F))



data <- metaLoadData("fit_e7","nov_e7_l10","nov_e7_l20","nov_e7_l30","nov_e7_l40","nov_e7","nov_e7_l60","nov_e7_l70","nov_e7_l80","nov_e7_l90","nov_e7_l100", names=c("LS-0.0","LS-0.1","LS-0.2","LS-0.3","LS-0.4","LS-0.5","LS-0.6","LS-0.7","LS-0.8","LS-0.9","LS-1.0"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F))
fullStatistics(data, fit.tests=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", fit.comp.par=list(snapshots=c(50,100,300,500),jitter=F))

data <- metaLoadData("nov_e7_l20","nov_e7_l30","nov_e7_l40","nov_e7","nov_e7_l60", params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F))
fullStatistics(data, fit.tests=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", fit.comp.par=list(snapshots=c(50,100,300,500),jitter=F))


data <- metaLoadData("nov_e13_p7","nov_e13","nov_e10_p5","nov_e10", names=c("NS-V13-P7","NS-V13-P3","NS-V10-P5","NS-V10-P3"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F))
fullStatistics(data, fit.comp=T, fit.tests=T, show.only=T, som.group=F, som.alljobs=F, expset.name="fit", fit.comp.par=list(snapshots=c(500),jitter=F))



# NEW ########################33

setwd("~/exps/EC")
data <- metaLoadData("fit_e4","fit_e10","nov_e4","nov_e10", names=c("Fit4","Fit10","NS4","NS10"), params=list(jobs=10, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=T, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
gfit4 <- groupDiversity(data$Fit4)
gfit10 <- groupDiversity(data$Fit10)
gns4 <- groupDiversity(data$NS4)
gns10 <- groupDiversity(data$NS10)
plotframe <- data.frame(gen=gfit4$gen, fit4=gfit4$mean, fit10=gfit10$mean, ns4=gns4$mean, ns10=gns10$mean)
plotMultiline(plotframe, ylim=NULL)

gfit4 <- groupDiversity.accum(data$Fit4, interval=10)
gfit10 <- groupDiversity.accum(data$Fit10, interval=10)
gns4 <- groupDiversity.accum(data$NS4, interval=10)
gns10 <- groupDiversity.accum(data$NS10, interval=10)
plotframe <- data.frame(gen=gfit4$gen, fit4=gfit4$mean, fit10=gfit10$mean, ns4=gns4$mean, ns10=gns10$mean)
plotMultiline(plotframe, ylim=NULL)

setwd("/media//jorge//e6a53fea-fa90-483a-975e-44cc084dc551//jorge//EXPERIMENTAL RESULTS/EC/pred/")
vars.group=c("GCap","GPreyD","GPredD","Time")
vars.ind=c("ICap","IPreyD","IPredD")

data <- metaLoadData("fit_e4","fit_e7","fit_e10","fit_e13", names=c("Fit4","Fit7","Fit10","Fit13"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=T, behavs.sample=0.2, vars.file=c(vars.group,NA,NA,NA),vars.group=vars.group))
data2 <- metaLoadData("nov_e4","nov_e7","nov_e10","nov_e13", names=c("Nov4","Nov7","Nov10","Nov13"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=T, behavs.sample=0.2, vars.file=c(vars.group,NA,NA,NA),vars.group=vars.group))

gfit4 <- groupDiversity.accum(data$Fit4, interval=25)
gfit7 <- groupDiversity.accum(data$Fit7, interval=25)
gfit10 <- groupDiversity.accum(data$Fit10, interval=25)
gfit13 <- groupDiversity.accum(data$Fit13, interval=25)
gnov4 <- groupDiversity.accum(data2$Nov4, interval=25)
gnov7 <- groupDiversity.accum(data2$Nov7, interval=25)
gnov10 <- groupDiversity.accum(data2$Nov10, interval=25)
gnov13 <- groupDiversity.accum(data2$Nov13, interval=25)

plotframe <- data.frame(gen=gfit4$gen, fit4=gfit4$mean, fit7=gfit7$mean,fit10=gfit10$mean,fit13=gfit13$mean,nov4=gnov4$mean, nov7=gnov7$mean,nov10=gnov10$mean,nov13=gnov13$mean)
plotMultiline(plotframe, ylim=NULL)
plotframe <- data.frame(gen=gfit4$gen, fit4=gfit4$mean, fit7=gfit7$mean,fit10=gfit10$mean,fit13=gfit13$mean)
plotMultiline(plotframe, ylim=NULL)

soms <- fullStatistics(data, fit.comp=F, show.only=T, som.group=T, som.alljobs=T, expset.name="fit", som.group.par=list(distance.filter=0.25),fit.comp.par=list(snapshots=c(500),jitter=F))

data <- metaLoadData("fit_e4","fit_e7","fit_e10","fit_e13", names=c("Fit4","Fit7","Fit10","Fit13"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=T, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
ifit4 <- indDiversity.accum(data$Fit4, interval=25)
ifit10 <- indDiversity.accum(data$Fit10, interval=25)

data <- metaLoadData("fit_e4","fit_e7","fit_e10","fit_e13", names=c("Fit4","Fit7","Fit10","Fit13"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=T, behavs.sample=0.2, vars.file=c(vars.group,NA,NA,NA),vars.group=vars.group))
data2 <- metaLoadData("nov_e4","nov_e7","nov_e10","nov_e13", names=c("Nov4","Nov7","Nov10","Nov13"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=T, behavs.sample=0.2, vars.file=c(vars.group,NA,NA,NA),vars.group=vars.group))
count <- exploration.count(c(data,data2), by.gen=10, accum=T)
count2 <- exploration.count(c(data,data2), by.gen=10, accum=F)
frame <- uniformity.gen(count, type="visit",t=0.0001)
frame2 <- uniformity.gen(count2, type="visit",t=0.0001)
plotframe <- data.frame(gen=frame$Gen, Fit4A=frame$Fit4, Fit4G=frame2$Fit4, Fit10A=frame$Fit10, Fit10G=frame2$Fit10, Nov4A=frame$Nov4, Nov4G=frame2$Nov4, Nov10A=frame$Nov10, Nov10G=frame2$Nov10)
plotMultiline(plotframe, ylim=NULL)

plotMultiline(frame[,c("Gen","Fit4","Fit7","Fit10","Fit13","Nov4","Nov7","Nov10","Nov13")], ylim=NULL)
plotMultiline(frame[,c("Gen","Fit4","Fit7","Fit10","Fit13")], ylim=NULL)
plotMultiline(frame[,c("Gen","Fit4","Fit7","Fit10","Fit13","Nov4","Nov7","Nov10","Nov13")], ylim=NULL)
plotMultiline(frame[,c("Gen","Fit4","Fit7","Fit10","Fit13")], ylim=NULL)


count.ind <- exploration.count(data, by.gen=50, accum=T, vars=data[[1]]$vars.ind)
s0 <- uniformity.gen(filterSubCount(count.ind,"sub.0"), type="visit")
s1 <- uniformity.gen(filterSubCount(count.ind,"sub.1"), type="visit")
s2 <- uniformity.gen(filterSubCount(count.ind,"sub.2"), type="visit")
frame <- data.frame(gen=s0$Gen, S0F4=s0$Fit4, S1F4=s1$Fit4, S2F4=s2$Fit4, S0F10=s0$Fit10, S1F10=s1$Fit10, S2F10=s2$Fit10)
plotMultiline(frame,ylim=NULL)


data <- metaLoadData("fit_e4","fit_e10", names=c("Fit4","Fit10"), params=list(jobs=5, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=T, behavs.sample=1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))

reduceToBests <- function(data) {
  for(job in data$jobs) {
    for(s in data$subpops) {
      bests <- data.frame()
      for(g in data$gens) {
        sub <- subset(data[[job]][[s]], gen==g)
        ind <- which.max(sub$fitness)
        bests <- rbind(bests, sub[ind,])
      }
      data[[job]][[s]] <- bests
    }
  }
  return(data)
}

data4 <- reduceToBests(data$Fit4)
sub0 <- subset(data4$job.0$sub.0, select=vars.ind)
sub1 <- subset(data4$job.0$sub.1, select=vars.ind)
sub2 <- subset(data4$job.0$sub.2, select=vars.ind)
suball <- rbind(sub0,sub1,sub2)
d <- dist(suball, method="euclidean")
sam <- sammon(d)
sub0.p <- sam$points[1:500,]
sub1.p <- sam$points[501:1000,]
sub2.p <- sam$points[1001:1500,]
plot(sub0.p, type="b", xlim=c(-0.75,1), ylim=c(-0.75,0.5))
plot(sub1.p, type="b", xlim=c(-0.75,1), ylim=c(-0.75,0.5))
plot(sub2.p, type="b", xlim=c(-0.75,1), ylim=c(-0.75,0.5))

data4 <- reduceToBests(data$Fit4)
sub0 <- subset(data4$job.0$sub.0, select=c("IPreyD","IPredD"))
sub1 <- subset(data4$job.0$sub.1, select=c("IPreyD","IPredD"))
sub2 <- subset(data4$job.0$sub.2, select=c("IPreyD","IPredD"))
plot(sub0,type="b",xlim=c(0,1),ylim=c(0,1))
plot(sub1,type="b",xlim=c(0,1),ylim=c(0,1))
plot(sub2,type="b",xlim=c(0,1),ylim=c(0,1))
suball <- cbind(sub0,sub1,sub2)
sam <- sammon(dist(suball))
plot(sam$points, type="b")

sub0 <- subset(data4$job.0$sub.0, select=vars.ind)
sub1 <- subset(data4$job.0$sub.1, select=vars.ind)
sub2 <- subset(data4$job.0$sub.2, select=vars.ind)
suball <- cbind(sub0,sub1,sub2)
d <- dist(suball, method="euclidean")
sam <- sammon(d)
plot(sam$points, type="b")

sub0 <- subset(data4$job.0$sub.0, select=vars.group)
sub1 <- subset(data4$job.0$sub.1, select=vars.group)
sub2 <- subset(data4$job.0$sub.2, select=vars.group)
suball <- rbind(sub0,sub1,sub2)
sam <- sammon(dist(suball))
plot(sam$points, type="p")

data <- fread("fit_e7/job.0.rebehaviours.stat")
data2 <- fread("nov_e7/job.0.rebehaviours.stat")
behavs <- subset(rbind(data,data2), select=c(5,6,7,8))
sam <- sammon(dist(behavs))
plot(sam$points[1:500,], type="b", pch=20, xlim=c(-0.75,1),ylim=c(-0.5,0.5))
plot(sam$points[501:1000,], type="b", pch=18,  xlim=c(-0.75,1),ylim=c(-0.5,0.5))

data4 <- reduceToBests(data$Fit4)
data10 <- reduceToBests(data$Fit10)
bests4 <- subset(rbind(data4$job.0$sub.0,data4$job.0$sub.1,data4$job.0$sub.2), select=vars.group)
bests10 <- subset(rbind(data10$job.0$sub.0,data10$job.0$sub.1,data10$job.0$sub.2), select=vars.group)
sam <- sammon(dist(rbind(bests4,bests10)))
plot(sam$points[1:1500,], type="p", pch=20, xlim=c(-0.75,1), ylim=c(-0.5,0.5))
plot(sam$points[1501:3000,], type="p", pch=18, xlim=c(-0.75,1),ylim=c(-0.5,0.5))

bests4 <- subset(rbind(data4$job.0$sub.0,data4$job.0$sub.1,data4$job.0$sub.2), select=vars.group)
bests10 <- subset(rbind(data10$job.0$sub.0,data10$job.0$sub.1,data10$job.0$sub.2), select=vars.group)
sam <- sammon(dist(rbind(bests4,bests10)))
plot(sam$points[1:500,], type="p", pch=20, xlim=c(-0.75,1), ylim=c(-0.5,0.5))
plot(sam$points[501:1000,], type="p", pch=20, xlim=c(-0.75,1), ylim=c(-0.5,0.5))
plot(sam$points[1001:1500,], type="p", pch=20, xlim=c(-0.75,1), ylim=c(-0.5,0.5))
plot(sam$points[1501:2000,], type="p", pch=18, xlim=c(-0.75,1), ylim=c(-0.5,0.5))
plot(sam$points[2001:2500,], type="p", pch=18, xlim=c(-0.75,1), ylim=c(-0.5,0.5))
plot(sam$points[2501:3000,], type="p", pch=18, xlim=c(-0.75,1),ylim=c(-0.5,0.5))

data <- metaLoadData("~/exps/EC/pred/nsga_e4/","/media/jorge/e6a53fea-fa90-483a-975e-44cc084dc551/jorge/EXPERIMENTAL RESULTS/EC/pred/nov_e4","~/exps/EC/pred/nsga_e7/","/media/jorge/e6a53fea-fa90-483a-975e-44cc084dc551/jorge/EXPERIMENTAL RESULTS/EC/pred/nov_e7","~/exps/EC/pred/nsga_e10/","/media/jorge/e6a53fea-fa90-483a-975e-44cc084dc551/jorge/EXPERIMENTAL RESULTS/EC/pred/nov_e10", names=c("NSGA.4","LS.4","NSGA.7","LS.7","NSGA.10","LS.10"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F, behavs.sample=1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
fullStatistics(data, fit.tests=T, show.only=T, fit.ind=F, fit.comp=T, som.group=F, som.alljobs=F, expset.name="V4", fit.comp.par=list(snapshots=c(500),jitter=T,ylim=T))

####### best-of-generation plots ###############
data <- metaLoadData("fit_e4","fit_e7","fit_e10","fit_e13", names=c("Fit4","Fit7","Fit10","Fit13"), params=list(jobs=1, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=T, behavs.sample=1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data4 <- reduceToBests(data$Fit4)
data7 <- reduceToBests(data$Fit7)
data10 <- reduceToBests(data$Fit10)
data13 <- reduceToBests(data$Fit13)
vars.group=c("GCap","GPreyD","GPredD","Time")
bests4 <- subset(rbind(data4$job.0$sub.0,data4$job.0$sub.1,data4$job.0$sub.2), select=vars.group)
bests7 <- subset(rbind(data7$job.0$sub.0,data7$job.0$sub.1,data7$job.0$sub.2), select=vars.group)
bests10 <- subset(rbind(data10$job.0$sub.0,data10$job.0$sub.1,data10$job.0$sub.2), select=vars.group)
bests13 <- subset(rbind(data13$job.0$sub.0,data13$job.0$sub.1,data13$job.0$sub.2), select=vars.group)
sam <- sammon(dist(rbind(bests4,bests7,bests10,bests13)))

color <- rgb(0,0,0,0.1)
plot(c(-0.5,1),c(-0.5,0.5), type="n", main="V4")
points(sam$points[1:1500,], pch=21, bg=color, col=NA)
points(sam$points[c(1,500,which.max(data4$job.0$sub.0$fitness)),], pch=4, col=c("blue","orange","red"),bg=NA,lwd=3)
plot(c(-0.5,1),c(-0.5,0.5), type="n", main="V7")
points(sam$points[1501:3000,], pch=21, bg=color, col=NA)
points(sam$points[1500+c(1,500,which.max(data7$job.0$sub.0$fitness)),], pch=4, col=c("blue","orange","red"), bg=NA,lwd=3)
plot(c(-0.5,1),c(-0.5,0.5), type="n", main="V10")
points(sam$points[3001:4500,], pch=21, bg=color, col=NA)
points(sam$points[3000+c(1,500,which.max(data10$job.0$sub.0$fitness)),], pch=4, col=c("blue","orange","red"), bg=NA,lwd=3)
plot(c(-0.5,1),c(-0.5,0.5), type="n", main="V13")
points(sam$points[4501:6000,], pch=21, bg=color, col=NA)
points(sam$points[4500+c(1,500,which.max(data13$job.0$sub.0$fitness)),], pch=4, col=c("blue","orange","red"),bg=NA,lwd=3)


setwd("~/exps/EC/pred")
data <- metaLoadData("nsga_e4_group","nov_e4","nsga_e4_mix50","nov_e4_indgroup","nsga_e4_ind","nov_e4_ind", names=c("NSGA.G","LS.G","NSGA.Mix","LS.Mix","NSGA.I","LS.I"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F, behavs.sample=1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data <- metaLoadData("nsga_e7_group","nov_e7","nsga_e7_mix50","nov_e7_indgroup","nsga_e7_ind","nov_e7_ind", names=c("NSGA.G","LS.G","NSGA.Mix","LS.Mix","NSGA.I","LS.I"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F, behavs.sample=1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data <- metaLoadData("nsga_e10_group","nov_e10","nsga_e10_mix50","nov_e10_indgroup","nsga_e10_ind","nov_e10_ind", names=c("NSGA.G","LS.G","NSGA.Mix","LS.Mix","NSGA.I","LS.I"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F, behavs.sample=1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data <- metaLoadData("nsga_e13_group","nov_e13","nsga_e13_mix50","nov_e13_indgroup","nsga_e13_ind","nov_e13_ind", names=c("NSGA.G","LS.G","NSGA.Mix","LS.Mix","NSGA.I","LS.I"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F, behavs.sample=1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))


data <- metaLoadData("nsga_e4_group","nsga_e4_mix75","nsga_e4_mix50","nsga_e4_mix25","nsga_e4_ind", names=c("NSGA.Group","NSGA.Mix25","NSGA.Mix50","NSGA.Mix75","NSGA.Ind"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F, behavs.sample=1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data <- metaLoadData("nsga_e7_group","nsga_e7_mix75","nsga_e7_mix50","nsga_e7_mix25","nsga_e7_ind", names=c("NSGA.Group","NSGA.Mix25","NSGA.Mix50","NSGA.Mix75","NSGA.Ind"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F, behavs.sample=1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data <- metaLoadData("nsga_e10_group","nsga_e10_mix75","nsga_e10_mix50","nsga_e10_mix25","nsga_e10_ind", names=c("NSGA.Group","NSGA.Mix25","NSGA.Mix50","NSGA.Mix75","NSGA.Ind"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F, behavs.sample=1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data <- metaLoadData("nsga_e13_group","nsga_e13_mix75","nsga_e13_mix50","nsga_e13_mix25","nsga_e13_ind", names=c("NSGA.Group","NSGA.Mix25","NSGA.Mix50","NSGA.Mix75","NSGA.Ind"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F, behavs.sample=1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))


data <- metaLoadData("nss_e4_group","nsf_e4_group","nsga_e4_group","fit_e4", names=c("NS.N","NS.F","NSGA","Fit"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F, behavs.sample=1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data <- metaLoadData("nss_e7_group","nsf_e7_group","nsga_e7_group","fit_e7", names=c("NS.N","NS.F","NSGA","Fit"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F, behavs.sample=1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data <- metaLoadData("nss_e10_group","nsf_e10_group","nsga_e10_group","fit_e10", names=c("NS.N","NS.F","NSGA","Fit"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F, behavs.sample=1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data <- metaLoadData("nss_e13_group","nsf_e13_group","nsga_e13_group","fit_e13", names=c("NS.N","NS.F","NSGA","Fit"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=F, behavs.sample=1, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))

fullStatistics(data, fit.tests=T, show.only=T, fit.ind=F, fit.comp=T, som.group=F, som.alljobs=F, expset.name="V4", fit.comp.par=list(snapshots=c(500),jitter=T,ylim=T))


data <- metaLoadData("nsga_e7_group","nov_e7","nsga_e7_ind","nov_e7_ind", names=c("NSGA.G","LS.G","NSGA.I","LS.I"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", fitlim=c(0,2), load.behavs=T, behavs.sample=0.25, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
count <- exploration.count(data)
uniformity.all(count)
count.ind <- exploration.count(data, vars=data[[1]]$vars.ind)
uniformity.all(count.ind)
