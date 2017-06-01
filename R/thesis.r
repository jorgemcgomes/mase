# predator-prey real experiments

setwd("/media/jorge/Orico/predprey")
gvars <- c("Captured","PreyDist","Time","Dispersion")
data <- loadData(c("fit/","nsga/"), names=c("Fit","NS-Team"), filename="behaviours.stat", fun="loadBehaviours", vars=gvars)
data.bests <- data[, filterBests(.SD), .(Setup, Job)] 

div.bests <- diversity(data.bests, vars=gvars, parallel=F)
ggplot(div.bests, aes(Setup,Diversity)) + geom_boxplot()

data.sampled <- data[, .SD[order(sample(.N, .N*0.5))], by=.(Setup,Job)]
clusterExport(cl, "gvars")
div <- diversity(data.sampled, vars=gvars, parallel=T)
ggplot(div, aes(Setup,Diversity)) + geom_boxplot()

frame <- rbind(cbind(div.bests,Variable="BoG team dispersion"), cbind(div,Variable="All team dispersion"))
ggplot(frame, aes(Variable,Diversity)) + geom_boxplot(aes(fill=Setup)) + ylim(0,NA) + labs(x=NULL, fill="Method")




# NACO experiments

setwd("/media/jorge/Orico/naco/")

data <- loadData(c("stable_sep/*","down_tog/*","down_mid/*","down_sep_long/*"), filename="refitness.stat", fun=loadFitness, auto.ids.sep="/", auto.ids.names=c("Task","Method"))
data <- data[Method %in% c("fit","moea","nst","staged")]
data[, Method := factor(Method, levels=c("fit","staged","moea","nst"), labels=c("Fit","Inc","MOEA","NS"))]
data[, Task := factor(Task, labels=c("Fix-Sep","Var-Tog","Var-Mid","Var-Sep"))]
data <- data[!(Method=="Fit" & (Task=="Fix-Sep" | Task=="Var-Tog") & Generation > 499)]

ggplot(data[, .(BestSoFar=mean(BestSoFar)), by=.(Generation,Task,Method)], aes(Generation, BestSoFar)) + 
  geom_line(aes(group=Method,colour=Method,linetype=Method)) + facet_wrap(~ Task, scales="free_x") +
  labs(y="Fitness")
ggsave("~/Dropbox/Work/Papers/phd/fig/naco_improv_lines.pdf", width=5,height=5)

ggplot(lastGen(data), aes(Method, BestSoFar)) + 
  geom_boxplot(aes(fill=Method)) + facet_wrap(~ Task, nrow=1) + labs(y="Fitness") + ylim(0,6) + guides(fill=FALSE) +
  theme(axis.text.x = element_text(angle = 30, hjust = 1))
ggsave("~/Dropbox/Work/Papers/phd/fig/improv_boxplot.pdf", width=5,height=2.5)

load("~/Dropbox/Work/Papers/NACO/rdata/improv.div.rdata")
data <- as.data.frame(improv.div$data)
data[,"Task"] <- rownames(data)
data <- addTaskInfo(data,"Task")
d <- melt(data)
d <- subset(d, Method != "NInc")
ggplot(d, aes(Method,value)) + geom_boxplot(aes(fill=Method),outlier.size=1, lwd=.4) + facet_wrap(~ Task, nrow=1) + 
  ylab("Behavioural diversity") + guides(fill=FALSE) + ylim(0,NA) + theme(axis.text.x = element_text(angle = 30, hjust = 1))
ggsave("~/Dropbox/Work/Papers/phd/fig/improv_diversity_boxplot.pdf", width=5,height=2.5)
