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

data42 <- metaLoadData("fit_e4_p2","fit_e4_p2_r2","fit_e4_p2_r5","fit_e4_p2_r10", names=c("Fit-N0","Fit-N2","Fit-N5","Fit-N10"), params=list(jobs=30, subpops=2, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=T, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data4 <- metaLoadData("fit_e4","fit_e4_r2","fit_e4_r5","fit_e4_r10", names=c("Fit-N0","Fit-N2","Fit-N5","Fit-N10"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=T, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data7 <- metaLoadData("fit_e7","fit_e7_r2","fit_e7_r5","fit_e7_r10", names=c("Fit-N0","Fit-N2","Fit-N5","Fit-N10"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=T, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data10 <- metaLoadData("fit_e10","fit_e10_r2","fit_e10_r5","fit_e10_r10", names=c("Fit-N0","Fit-N2","Fit-N5","Fit-N10"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=T, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data13 <- metaLoadData("fit_e13","fit_e13_r2","fit_e13_r5","fit_e13_r10", names=c("Fit-N0","Fit-N2","Fit-N5","Fit-N10"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=T, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))

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

# number of collaborations x elite individuals

frame <- cbind(individuals.count(data42, min.fit=individuals.quantile(data42,0.85)$summary, N=c("0","2","5","10"), Setup="V4.P2")
frame <- rbind(frame, cbind(individuals.count(data4, min.fit=individuals.quantile(data4, 0.85))$summary, N=c("0","2","5","10"), Setup="V4"))
frame <- rbind(frame, cbind(individuals.count(data7, min.fit=individuals.quantile(data7, 0.85))$summary, N=c("0","2","5","10"), Setup="V7"))
frame <- rbind(frame, cbind(individuals.count(data10, min.fit=individuals.quantile(data10, 0.85))$summary, N=c("0","2","5","10"), Setup="V10"))
frame <- rbind(frame, cbind(individuals.count(data13, min.fit=individuals.quantile(data13, 0.85))$summary, N=c("0","2","5","10"), Setup="V13"))

pd <- position_dodge(.25) # move them .05 to the left and right
ggplot(frame, aes(x=N, y=mean, colour=Setup)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.5, position=pd) +
  geom_line(position=pd,aes(group=Setup)) +
  geom_point(position=pd) + ylab("Proportion of high fitness individuals") + theme(legend.title=element_blank()) + scale_x_discrete(limits=c("0","2","5","10"))


# number of collaborations x elite exploration

count42.el <- exploration.count(data42, min.fit=1.25)
count4.el <- exploration.count(data4, min.fit=1.5)
count7.el <- exploration.count(data7, min.fit=0.75)
count10.el <- exploration.count(data10, min.fit=0.3)
count13.el <- exploration.count(data13, min.fit=0.3)

frame <- cbind(uniformity(count42.el,mode="jsd")$summary, N=c("0","2","5","10"), Setup="V4.P2")
frame <- rbind(frame, cbind(uniformity(count4.el,mode="jsd")$summary, N=c("0","2","5","10"), Setup="V4"))
frame <- rbind(frame, cbind(uniformity(count7.el,mode="jsd")$summary, N=c("0","2","5","10"), Setup="V7"))
frame <- rbind(frame, cbind(uniformity(count10.el,mode="jsd")$summary, N=c("0","2","5","10"), Setup="V10"))
frame <- rbind(frame, cbind(uniformity(count13.el,mode="jsd")$summary, N=c("0","2","5","10"), Setup="V13"))

ggplot(frame, aes(x=N, y=mean, colour=Setup)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.25, position=pd) +
  geom_line(position=pd,aes(group=Setup)) +
  geom_point(position=pd) + ylab("Elite exploration") + theme(legend.title=element_blank()) + scale_x_discrete(limits=c("0","2","5","10"))



####### OVERCOMING PREMATURE CONVERGENCE

data4 <- metaLoadData("fit_e4","nov_e4","nov_e4_ind","nov_e4_indgroup", names=c("Fit","NS-Team","NS-Ind","NS-Mix"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data7 <- metaLoadData("fit_e7","nov_e7","nov_e7_ind","nov_e7_indgroup", names=c("Fit","NS-Team","NS-Ind","NS-Mix"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data10 <- metaLoadData("fit_e10","nov_e10","nov_e10_ind","nov_e10_indgroup", names=c("Fit","NS-Team","NS-Ind","NS-Mix"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data13 <- metaLoadData("fit_e13","nov_e13","nov_e13_ind","nov_e13_indgroup", names=c("Fit","NS-Team","NS-Ind","NS-Mix"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=F, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))

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
  q <- individuals.quantile(data, 0.85)
  count <- exploration.count(data)
  frame <- rbind(frame, cbind(uniformity(count, mode="jsd")$summary, Type="Team", V=e, Method=names(data)))
  count.el <- exploration.count(data, min.fit=q)
  frame <- rbind(frame, cbind(uniformity(count.el, mode="jsd")$summary, Type="Elite", V=e, Method=names(data)))
  count.ind <- exploration.count(data, vars=data[[1]]$vars.ind)
  frame <- rbind(frame, cbind(uniformity.ind(count.ind, mode="jsd")$summary, Type="Individual", V=e, Method=names(data)))
  amount <- individuals.count(data, min.fit=q)
  frame <- rbind(frame, cbind(amount$summary, Type="Amount", V=e, Method=names(data)))
  rm(data) ; gc()
}

write.csv(frame, file="~/Dropbox/Papers/EC/exploration_nsvariants.csv", quote=F)

pd <- position_dodge(.1) # move them .05 to the left and right
ggplot(subset(frame, Type=="Team"), aes(x=V, y=mean, colour=Method)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=0.5, position=pd) +
  geom_line(position=pd,aes(group=Method)) +
  geom_point(position=pd) + ylab("Team exploration") + theme(legend.title=element_blank())
ggplot(subset(frame, Type=="Elite"), aes(x=V, y=mean, colour=Method)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=0.5, position=pd) +
  geom_line(position=pd,aes(group=Method)) +
  geom_point(position=pd) + ylab("Elite exploration") + theme(legend.title=element_blank())
ggplot(subset(frame, Type=="Individual"), aes(x=V, y=mean, colour=Method)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=0.5, position=pd) +
  geom_line(position=pd,aes(group=Method)) +
  geom_point(position=pd) + ylab("Individual exploration") + theme(legend.title=element_blank())
ggplot(subset(frame, Type=="Amount"), aes(x=V, y=mean, colour=Method)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=0.5, position=pd) +
  geom_line(position=pd,aes(group=Method)) +
  geom_point(position=pd) + ylab("High-fitness proportion") + theme(legend.title=element_blank())


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
count.el <- exploration.count(data, min.fit=individuals.quantile(data, 0.85))
count.ind <- exploration.count(data, vars=data[[1]]$vars.ind)

frame <- cbind(uniformity(count, mode="jsd")$summary, Type="Team",Rho=seq(0,1,by=0.1))
frame <- rbind(frame, cbind(uniformity(count.el,mode="jsd")$summary, Type="Elite", Rho=seq(0,1,by=0.1)))
#frame <- rbind(frame, cbind(uniformity.ind(count.ind,mode="jsd")$summary, Type="Individual", Rho=seq(0,1,by=0.1)))
frame <- rbind(frame, cbind(individuals.count(data, min.fit=0.271)$summary, Type="Amount", Rho=seq(0,1,by=0.1)))

write.csv(frame, file="~/Dropbox/Papers/EC/rho.csv", quote=F)

pd <- position_dodge(.02) # move them .05 to the left and right
ggplot(frame, aes(x=Rho, y=mean, colour=Type)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=0.1, position=pd) +
  geom_line(position=pd,aes(group=Type)) +
  geom_point(position=pd) + ylab("Exploration") + theme(legend.title=element_blank()) + scale_x_continuous(breaks=seq(0,1,by=0.1))


####### DIVERSITY OF SOLUTIONS ####################

data <- metaLoadData("fit_e4","nov_e4", names=c("Fit","NS-Team"), params=list(jobs=30, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=T, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
soms <- fullStatistics(data, fit.comp=F, show.only=T, som.group=T, som.alljobs=T, som.ind=T, expset.name="nsfit_e4", som.ind.par=list(distance.filter=0.1) ,som.group.par=list(distance.filter=0.25),fit.comp.par=list(snapshots=c(500),jitter=F))

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


######## garbage ########

data4 <- metaLoadData("fit_e4","nov_e4", names=c("Fit","NS-Team"), params=list(jobs=5, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=T, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data7 <- metaLoadData("fit_e7","nov_e7", names=c("Fit","NS-Team"), params=list(jobs=5, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=T, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data10 <- metaLoadData("fit_e10","nov_e10", names=c("Fit","NS-Team"), params=list(jobs=5, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=T, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))
data13 <- metaLoadData("fit_e13","nov_e13", names=c("Fit","NS-Team"), params=list(jobs=5, subpops=3, fitness.file="refitness.stat", offset=0, fitlim=c(0,2), load.behavs=T, behavs.sample=0.2, vars.ind=c("ICap","IPreyD","IPredD"),vars.group=c("GCap","GPreyD","GPredD","Time")))

