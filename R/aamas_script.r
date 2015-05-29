theme_set(theme_bw())
DEF_HEIGHT=3
DEF_WIDTH=4.5

data <- metaLoadData("s5_f2_p1_hyb_t20","s5_f2_p1_base", names=c("Hyb-CCEA","CCEA"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 150000, length.out=300))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(300)), expset.name="S5_F2_P1_WEAK", show.only=F)
fitnessLevels(data, c(2.0,2.25,2.5))

data <- metaLoadData("s5_f1_p1_smart_hyb_t20","s5_f1_p1_smart_base", names=c("Hyb-CCEA","CCEA"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 150000, length.out=300))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(300)), expset.name="S5_F1_P1_EVAS", show.only=F)
fitnessLevels(data, c(2.0,2.25,2.5))

data <- metaLoadData("s7_f2_p2_hyb_t20","s7_f2_p2_base", names=c("Hyb-CCEA","CCEA"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(428)), expset.name="S7_F2_P2_WEAK", show.only=F)
fitnessLevels(data, c(2.75,3,3.25,3.5,3.75))

data <- metaLoadData("s7_f2_p1_smart_hyb_t20","s7_f2_p1_smart_base", names=c("Hyb-CCEA","CCEA"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(428)), expset.name="S7_F2_P1_EVAS", show.only=F)
fitnessLevels(data, c(1.5,1.75,2.0,2.25))

data <- metaLoadData("s10_f3_p2_hyb_t20","s10_f3_p2_base", names=c("Hyb-CCEA","CCEA"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 399000, length.out=400))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(400)), expset.name="S10_F3_P2_WEAK", show.only=F)
fitnessLevels(data, c(2.5,2.75,3,3.25,3.5))

data <- metaLoadData("s10_f3_p1_smart_hyb_t20","s10_f3_p1_smart_base", names=c("Hyb-CCEA","CCEA"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 399000, length.out=400))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(400)), expset.name="S10_F3_P1_EVAS", show.only=F)
fitnessLevels(data, c(1.25,1.5,1.75,2.0))



frame <- data.frame()
data <- metaLoadData("s5_f2_p1_hyb_t20","s5_f2_p1_base", names=c("Hyb-CCEA","CCEA"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 150000, length.out=300))))
frame <- rbind(frame, cbind(fitnessSummary(data),Setup="W5"))
data <- metaLoadData("s5_f1_p1_smart_hyb_t20","s5_f1_p1_smart_base", names=c("Hyb-CCEA","CCEA"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 150000, length.out=300))))
frame <- rbind(frame, cbind(fitnessSummary(data),Setup="E5"))
data <- metaLoadData("s7_f2_p2_hyb_t20","s7_f2_p2_base", names=c("Hyb-CCEA","CCEA"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))
frame <- rbind(frame, cbind(fitnessSummary(data),Setup="W7"))
data <- metaLoadData("s7_f2_p1_smart_hyb_t20","s7_f2_p1_smart_base", names=c("Hyb-CCEA","CCEA"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))
frame <- rbind(frame, cbind(fitnessSummary(data),Setup="E7"))
data <- metaLoadData("s10_f3_p2_hyb_t20","s10_f3_p2_base", names=c("Hyb-CCEA","CCEA"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 399000, length.out=400))))
frame <- rbind(frame, cbind(fitnessSummary(data),Setup="W10"))
data <- metaLoadData("s10_f3_p1_smart_hyb_t20","s10_f3_p1_smart_base", names=c("Hyb-CCEA","CCEA"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 399000, length.out=400))))
frame <- rbind(frame, cbind(fitnessSummary(data),Setup="E10"))

qplot(x=Setup, y=mean, fill=method, data=frame, geom="bar", stat="identity", position="dodge") + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.25, position=position_dodge(.9)) +
  coord_cartesian(ylim=c(1,4)) + ylab("Best fitness")


data_d <- metaLoadData("s7_f2_p2_hyb_m100","s7_f2_p2_hyb_m110","s7_f2_p2_hyb_m125_t10","s7_f2_p2_hyb_m150","s7_f2_p2_hyb_m175","s7_f2_p2_hyb_m200", names=c("100","110","125","150","175","200"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))
data_s <- metaLoadData("s7_f2_p1_smart_hyb_m100","s7_f2_p1_smart_hyb_m110","s7_f2_p1_smart_hyb_m125_t10","s7_f2_p1_smart_hyb_m150","s7_f2_p1_smart_hyb_m175","s7_f2_p1_smart_hyb_m200", names=c("100","110","125","150","175","200"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))
frame <- cbind(fitnessSummary(data_d), T=c("100","110","125","150","175","200"), Setup="W7")
frame <- rbind(frame, cbind(fitnessSummary(data_s), T=c("100","110","125","150","175","200"), Setup="E7"))

data_d <- metaLoadData("s7_f2_p2_hyb_t1","s7_f2_p2_hyb_t5","s7_f2_p2_hyb_m125_t10","s7_f2_p2_hyb_t20","s7_f2_p2_hyb_t40","s7_f2_p2_hyb_t60", names=c("01","05","10","20","40","60"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))
data_s <- metaLoadData("s7_f2_p1_smart_hyb_t1","s7_f2_p1_smart_hyb_t5","s7_f2_p1_smart_hyb_m125_t10","s7_f2_p1_smart_hyb_t20","s7_f2_p1_smart_hyb_t40","s7_f2_p1_smart_hyb_t60", names=c("01","05","10","20","40","60"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))
frame <- cbind(fitnessSummary(data_d), T=c("01","05","10","20","40","60"), Setup="W7")
frame <- rbind(frame, cbind(fitnessSummary(data_s), T=c("01","05","10","20","40","60"), Setup="E7"))

pd <- position_dodge(.0001) # move them .05 to the left and right
ggplot(frame, aes(x=T, y=mean, colour=Setup)) + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.25, position=pd) +
  geom_line(position=pd,aes(group=Setup,linetype=Setup)) +
  geom_point(position=pd) + ylab("Best fitness") + theme(legend.title=element_blank())

fullStatistics(data_d, fit.comp=T, fit.comp.par=list(snapshots=c(428)), expset.name="W7", show.only=T)
fullStatistics(data_s, fit.comp=T, fit.comp.par=list(snapshots=c(428)), expset.name="E7", show.only=T)


analyse("s5_f2_p1_hyb_t20","s5_f1_p1_smart_hyb_t20","s7_f2_p2_hyb_t20","s7_f2_p1_smart_hyb_t20","s10_f3_p2_hyb_t20","s10_f3_p1_smart_hyb_t20", exp.names=c("W5","E5","W7","E7","W10","E10"), filename="hybrid.stat", analyse=c("npops"), vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","foreigns","selfinds","forinds","allinds","meanage","maxage","meandisp","meandistother","merges","splits","remerges","totalmerges","totalsplits","totalremerges"), all=F, boxplots=F, t.tests=F, splits=10, print=T, plot=T, smooth=10, gens=seq(0,500))



data <- metaLoadData("s5_f2_p1_hyb_t20","s5_f2_p1_hyb_t20_homo", names=c("Hetero","Homo"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 150000, length.out=300))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(300)), expset.name="I_S5_F2_P1_WEAK", show.only=F)
fitnessLevels(data, c(1.5,1.75,2.0,2.25,2.5))

data <- metaLoadData("s5_f1_p1_smart_hyb_t20","s5_f1_p1_smart_hyb_t20_homo", names=c("Hetero","Homo"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 150000, length.out=300))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(300)), expset.name="I_S5_F1_P1_EVAS", show.only=F)
fitnessLevels(data, c(1.5,1.75,2.0,2.25,2.5))

data <- metaLoadData("s7_f2_p2_hyb_t20","s7_f2_p2_hyb_t20_homo", names=c("Hetero","Homo"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(428)), expset.name="I_S7_F2_P2_WEAK", show.only=F)
fitnessLevels(data, c(2,2.5,2.75,3,3.25,3.5,3.75,4))

data <- metaLoadData("s7_f2_p1_smart_hyb_t20","s7_f2_p1_smart_hyb_t20_homo", names=c("Hetero","Homo"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(428)), expset.name="I_S7_F2_P1_EVAS", show.only=F)
fitnessLevels(data, c(1.5,1.75,2.0,2.25,2.5))

data <- metaLoadData("s10_f3_p2_hyb_t20","s10_f3_p2_hyb_t20_homo", names=c("Hetero","Homo"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 399000, length.out=400))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(400)), expset.name="I_S10_F3_P2_WEAK", show.only=F)
fitnessLevels(data, c(2,2.5,2.75,3,3.25,3.5,3.75,4))

data <- metaLoadData("s10_f3_p1_smart_hyb_t20","s10_f3_p1_smart_hyb_t20_homo", names=c("Hetero","Homo"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 399000, length.out=400))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(400)), expset.name="I_S10_F3_P1_EVAS", show.only=F)
fitnessLevels(data, c(1.25,1.5,1.75,2.0,2.25,2.5))



for(setup in c("s5_f2_p1_hyb_t20","s5_f1_p1_smart_hyb_t20","s7_f2_p2_hyb_t20","s7_f2_p1_smart_hyb_t20","s10_f3_p2_hyb_t20","s10_f3_p1_smart_hyb_t20")) {
  r <- fitnessRelation(setup)
  cat("******* ",setup, "******* \n")
  print(metaAnalysis(r)) 
}


n = 0
heterogeneity("s5_f2_p1_base",n) ; heterogeneity("s5_f2_p1_hyb_t20",n)
heterogeneity("s5_f1_p1_smart_base",n) ; heterogeneity("s5_f1_p1_smart_hyb_t20",n)
heterogeneity("s7_f2_p2_base",n) ; heterogeneity("s7_f2_p2_hyb_t20",n)
heterogeneity("s7_f2_p1_smart_base",n) ; heterogeneity("s7_f2_p1_smart_hyb_t20",n)
heterogeneity("s10_f3_p2_base",n) ; heterogeneity("s10_f3_p2_hyb_t20",n)
heterogeneity("s10_f3_p1_smart_base",n) ; heterogeneity("s10_f3_p1_smart_hyb_t20",n)




# HOMOGENEOUS VS HETEROGENEOUS


data <- metaLoadData("s5_f2_p1_hyb_t20","s5_f2_p1_hyb_t20_homo","s5_f2_p1_base", names=c("Het-Hyb","Hom-Hyb","CCEA"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 150000, length.out=300))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(300)), expset.name="A_S5_F2_P1_WEAK", show.only=F)
fitnessLevels(data, c(1.25,1.5,1.75,2.0,2.25,2.5))

data <- metaLoadData("s5_f1_p1_smart_hyb_t20","s5_f1_p1_smart_hyb_t20_homo","s5_f1_p1_smart_base", names=c("Het-Hyb","Hom-Hyb","CCEA"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 150000, length.out=300))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(300)), expset.name="A_S5_F1_P1_EVAS", show.only=F)
fitnessLevels(data, c(1.25,1.5,1.75,2.0,2.25,2.5))

data <- metaLoadData("s7_f2_p2_hyb_t20","s7_f2_p2_hyb_t20_homo","s7_f2_p2_base", names=c("Het-Hyb","Hom-Hyb","CCEA"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(428)), expset.name="A_S7_F2_P2_WEAK", show.only=F)
fitnessLevels(data, c(2,2.5,2.75,3,3.25,3.5,3.75,4))

data <- metaLoadData("s7_f2_p1_smart_hyb_t20","s7_f2_p1_smart_hyb_t20_homo","s7_f2_p1_smart_base", names=c("Het-Hyb","Hom-Hyb","CCEA"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(428)), expset.name="A_S7_F2_P1_EVAS", show.only=F)
fitnessLevels(data, c(1.25,1.5,1.75,2.0,2.25,2.5))

data <- metaLoadData("s10_f3_p2_hyb_t20","s10_f3_p2_hyb_t20_homo","s10_f3_p2_base", names=c("Het-Hyb","Hom-Hyb","CCEA"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 399000, length.out=400))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(400)), expset.name="A_S10_F3_P2_WEAK", show.only=F)
fitnessLevels(data, c(2,2.5,2.75,3,3.25,3.5,3.75,4))

data <- metaLoadData("s10_f3_p1_smart_hyb_t20","s10_f3_p1_smart_hyb_t20_homo","s10_f3_p1_smart_base", names=c("Het-Hyb","Hom-Hyb","CCEA"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 399000, length.out=400))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(400)), expset.name="A_S10_F3_P1_EVAS", show.only=F)
fitnessLevels(data, c(1.25,1.5,1.75,2.0,2.25,2.5))

frame <- data.frame()
data <- metaLoadData("s5_f2_p1_hyb_t20","s5_f2_p1_hyb_t20_homo", names=c("Het-Hyb","Hom-Hyb"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 150000, length.out=300))))
frame <- rbind(frame, cbind(fitnessSummary(data),Setup="W5"))
data <- metaLoadData("s5_f1_p1_smart_hyb_t20","s5_f1_p1_smart_hyb_t20_homo", names=c("Het-Hyb","Hom-Hyb"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 150000, length.out=300))))
frame <- rbind(frame, cbind(fitnessSummary(data),Setup="E5"))
data <- metaLoadData("s7_f2_p2_hyb_t20","s7_f2_p2_hyb_t20_homo", names=c("Het-Hyb","Hom-Hyb"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))
frame <- rbind(frame, cbind(fitnessSummary(data),Setup="W7"))
data <- metaLoadData("s7_f2_p1_smart_hyb_t20","s7_f2_p1_smart_hyb_t20_homo", names=c("Het-Hyb","Hom-Hyb"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))
frame <- rbind(frame, cbind(fitnessSummary(data),Setup="E7"))
data <- metaLoadData("s10_f3_p2_hyb_t20","s10_f3_p2_hyb_t20_homo", names=c("Het-Hyb","Hom-Hyb"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 399000, length.out=400))))
frame <- rbind(frame, cbind(fitnessSummary(data),Setup="W10"))
data <- metaLoadData("s10_f3_p1_smart_hyb_t20","s10_f3_p1_smart_hyb_t20_homo", names=c("Het-Hyb","Hom-Hyb"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 399000, length.out=400))))
frame <- rbind(frame, cbind(fitnessSummary(data),Setup="E10"))

qplot(x=Setup, y=mean, fill=method, data=frame, geom="bar", stat="identity", position="dodge") + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.25, position=position_dodge(.9)) +
  coord_cartesian(ylim=c(1,4)) + ylab("Best fitness")

data <- metaLoadData("s5_f2_p1_hyb_t20","s5_f2_p1_hyb_t20_homo", names=c("Het-Hyb","Hom-Hyb"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 150000, length.out=300))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(300)), expset.name="A_S5_F2_P1_WEAK", show.only=T)
data <- metaLoadData("s5_f1_p1_smart_hyb_t20","s5_f1_p1_smart_hyb_t20_homo", names=c("Het-Hyb","Hom-Hyb"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 150000, length.out=300))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(300)), expset.name="A_S5_F2_P1_WEAK", show.only=T)
data <- metaLoadData("s7_f2_p2_hyb_t20","s7_f2_p2_hyb_t20_homo", names=c("Het-Hyb","Hom-Hyb"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(428)), expset.name="A_S7_F2_P2_WEAK", show.only=T)
data <- metaLoadData("s7_f2_p1_smart_hyb_t20","s7_f2_p1_smart_hyb_t20_homo", names=c("Het-Hyb","Hom-Hyb"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(428)), expset.name="A_S7_F2_P2_WEAK", show.only=T)
data <- metaLoadData("s10_f3_p2_hyb_t20","s10_f3_p2_hyb_t20_homo", names=c("Het-Hyb","Hom-Hyb"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 399000, length.out=400))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(400)), expset.name="A_S10_F3_P2_WEAK", show.only=T)
data <- metaLoadData("s10_f3_p1_smart_hyb_t20","s10_f3_p1_smart_hyb_t20_homo", names=c("Het-Hyb","Hom-Hyb"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 399000, length.out=400))))
fullStatistics(data, fit.comp=T, fit.comp.par=list(snapshots=c(400)), expset.name="A_S10_F3_P2_WEAK", show.only=T)



# NUM METAPOPS -- HETERO VS HOMO

splits <- c(1,101,251,2000)
analyse("s5_f2_p1_hyb_t20","s5_f2_p1_hyb_t20_homo", exp.names=c("Het","Hom"), filename="hybrid.stat", analyse=c("npops"), vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","foreigns","selfinds","forinds","allinds","meanage","maxage","meandisp","meandistother","merges","splits","remerges","totalmerges","totalsplits","totalremerges"), all=F, boxplots=F, t.tests=T, splits=splits, print=T, plot=F, smooth=10, interval=T,gens=seq(0,2000))
analyse("s5_f1_p1_smart_hyb_t20","s5_f1_p1_smart_hyb_t20_homo", exp.names=c("Het","Hom"), filename="hybrid.stat", analyse=c("npops"), vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","foreigns","selfinds","forinds","allinds","meanage","maxage","meandisp","meandistother","merges","splits","remerges","totalmerges","totalsplits","totalremerges"), all=F, boxplots=F, t.tests=T, splits=splits, print=T, plot=F, interval=T,smooth=10, gens=seq(0,2000))
analyse("s7_f2_p2_hyb_t20","s7_f2_p2_hyb_t20_homo", exp.names=c("Het","Hom"), filename="hybrid.stat", analyse=c("npops"), vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","foreigns","selfinds","forinds","allinds","meanage","maxage","meandisp","meandistother","merges","splits","remerges","totalmerges","totalsplits","totalremerges"), all=F, boxplots=F, t.tests=T, splits=splits, print=T, plot=F, smooth=10, interval=T,gens=seq(0,2000))
analyse("s7_f2_p1_smart_hyb_t20","s7_f2_p1_smart_hyb_t20_homo", exp.names=c("Het","Hom"), filename="hybrid.stat", analyse=c("npops"), vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","foreigns","selfinds","forinds","allinds","meanage","maxage","meandisp","meandistother","merges","splits","remerges","totalmerges","totalsplits","totalremerges"), all=F, boxplots=F, t.tests=T, splits=splits, print=T, plot=F, interval=T,smooth=10, gens=seq(0,2000))
analyse("s10_f3_p2_hyb_t20","s10_f3_p2_hyb_t20_homo", exp.names=c("Het","Hom"), filename="hybrid.stat", analyse=c("npops"), vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","foreigns","selfinds","forinds","allinds","meanage","maxage","meandisp","meandistother","merges","splits","remerges","totalmerges","totalsplits","totalremerges"), all=F, boxplots=F, t.tests=T, splits=splits, print=T, plot=F, smooth=10,interval=T, gens=seq(0,2000))
analyse("s10_f3_p1_smart_hyb_t20","s10_f3_p1_smart_hyb_t20_homo", exp.names=c("Het","Hom"), filename="hybrid.stat", analyse=c("npops"), vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","foreigns","selfinds","forinds","allinds","meanage","maxage","meandisp","meandistother","merges","splits","remerges","totalmerges","totalsplits","totalremerges"), all=F, boxplots=F, t.tests=T, splits=splits, print=T, plot=F,interval=T, smooth=10, gens=seq(0,2000))





data <- metaLoadData("s5_f2_p1_hyb_t20","s5_f2_p1_base", names=c("Hyb-CCEA","CCEA"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 150000, length.out=300))))
fitnessLevels(data, 2.5)
data <- metaLoadData("s5_f1_p1_smart_hyb_t20","s5_f1_p1_smart_base", names=c("Hyb-CCEA","CCEA"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 150000, length.out=300))))
fitnessLevels(data, 2.5)
data <- metaLoadData("s7_f2_p2_hyb_t20","s7_f2_p2_base", names=c("Hyb-CCEA","CCEA"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))
fitnessLevels(data, 3)
data <- metaLoadData("s7_f2_p1_smart_hyb_t20","s7_f2_p1_smart_base", names=c("Hyb-CCEA","CCEA"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 299300, length.out=428))))
fitnessLevels(data, 1.75)
data <- metaLoadData("s10_f3_p2_hyb_t20","s10_f3_p2_base", names=c("Hyb-CCEA","CCEA"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 399000, length.out=400))))
fitnessLevels(data, 2.75)
data <- metaLoadData("s10_f3_p1_smart_hyb_t20","s10_f3_p1_smart_base", names=c("Hyb-CCEA","CCEA"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 399000, length.out=400))))
fitnessLevels(data, 1.5)

splits <- c(1,2000)
analyse("s5_f2_p1_hyb_t20","s5_f1_p1_smart_hyb_t20","s7_f2_p2_hyb_t20","s7_f2_p1_smart_hyb_t20","s10_f3_p2_hyb_t20","s10_f3_p1_smart_hyb_t20", exp.names=c("W5","E5","W7","E7","W10","E10"), filename="hybrid.stat", analyse=c("npops"), vars.pre=c("gen"), vars.sub=c("npops","minsize","meansize","maxsize","foreigns","selfinds","forinds","allinds","meanage","maxage","meandisp","meandistother","merges","splits","remerges","totalmerges","totalsplits","totalremerges"), all=F, boxplots=F, t.tests=T, splits=splits, print=T, plot=F, smooth=10, interval=T,gens=seq(0,2000))



frame <- read.table("~/Dropbox/Papers/EvoRobot/diffs.csv", sep="\t",header=T)
frame$mean <- frame$mean * 100
frame <- within(frame, Setup <- factor(Setup, levels=c("W5","E5","W7","E7","W10","E10")))
qplot(x=Setup, y=mean, fill=Diff, data=frame, geom="bar", stat="identity", position="dodge") + 
  coord_cartesian(ylim=c(0,75)) + ylab("% Decrease from CCEA")

frame <- read.table("~/Dropbox/Papers/EvoRobot/evals.csv", sep="\t",header=T)
frame <- within(frame, Setup <- factor(Setup, levels=c("W5","E5","W7","E7","W10","E10")))
qplot(x=Setup, y=mean, fill=method, data=frame, geom="bar", stat="identity", position="dodge") + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.25, position=position_dodge(.9)) + ylab("Evaluations (x100)")

frame <- read.table("~/Dropbox/Papers/EvoRobot/metapops.csv", sep="\t",header=T)
frame <- within(frame, Setup <- factor(Setup, levels=c("W5","E5","W7","E7","W10","E10")))
qplot(x=Setup, y=mean, fill=method, data=frame, geom="bar", stat="identity", position="dodge") + 
  geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=.25, position=position_dodge(.9)) + ylab("Num. populations")




data <- metaLoadData("s5_f2_p1_hyb_t20", names=c("Hyb"), params=list(jobs=30, load.behavs=F, use.evals=TRUE, gens=round(seq(0, 150000, length.out=300))))
l <- list()
for(job in data[[1]]$jobs) {
  l[[job]] <- data[[1]][[job]]$hybrid[[2]]
}
f <- as.data.frame(l)
mean <- rowMeans(f)
