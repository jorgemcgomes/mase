setwd("~/robot/exps/ecalf/")

data <- loadData(".", filename="postfitness.stat", fun="loadFitness", recursive=T)

agg <- summaryBy(BestSoFar ~ ID2 + ID3 + Generation, data, FUN=c(mean,se))
ggplot(agg, aes(Generation,BestSoFar.mean,group=ID3)) + geom_line(aes(colour=ID3)) + ylab("Fitness") +
  geom_ribbon(aes(ymax = BestSoFar.mean + BestSoFar.se, ymin = BestSoFar.mean - BestSoFar.se), alpha = 0.1) + facet_wrap(~ ID2)

ggplot(lastGen(data), aes(ID3,BestSoFar)) + geom_boxplot() + ylab("Fitness") + facet_wrap(~ ID2) + ylim(0,6)

setwd("/media/jorge/Orico/naco")

data <- loadData(c("stable_sep/nst*","stable_sep/moea*","down_tog/nst*","down_tog/moea*","down_mid/nst*","down_mid/moea*","down_sep_long/nst*","down_sep_long/moea*"), auto.ids.names=c("Task","EvoSetup"), filename="postfitness.stat", fun="loadFitness")

data <- data[!((Task=="down_tog" | Task=="stable_sep") & Generation >= 500)]
data <- data[!(Task=="down_sep_long" & Generation >= 700)]
data[, Evaluations := Generation * 300]
data[EvoSetup=="nst_pareto"|EvoSetup=="moea_pareto", Evaluations := Generation * 900]
data[EvoSetup=="nst_max", Evaluations := Generation * 600]
data[Task=="stable_sep" & EvoSetup=="moea_max", Evaluations := Generation * 600]
data[Task!="stable_sep" & EvoSetup=="moea_max", Evaluations := Generation * 900]
data <- data[EvoSetup != "nst_pareto" & EvoSetup != "moea_pareto"]
data$Task <- factor(data$Task, levels=c("stable_sep","down_tog","down_mid","down_sep_long"), labels=c("Fix-Sep","Var-Tog","Var-Mid","Var-Sep"))

data[EvoSetup %in% c("nst","nst_random","nst_pareto_one","nst_max"), Method := "NS"]
data[EvoSetup %in% c("moea","moea_random","moea_pareto_one","moea_max"), Method := "MOEA"]
data$Method <- factor(data$Method)
data[EvoSetup %in% c("nst","moea"), Representative := "Best"]
data[EvoSetup %in% c("nst_random","moea_random"), Representative := "Random"]
data[EvoSetup %in% c("nst_pareto_one","moea_pareto_one"), Representative := "Pareto"]
data[EvoSetup %in% c("nst_max","moea_max"), Representative := "Max"]
data$Representative <- factor(data$Representative, levels=c("Best","Random","Pareto","Max"))

agg <- summaryBy(BestSoFar ~ Task + Method + Representative + Generation, data, FUN=c(mean,se))
ggplot(agg, aes(Generation,BestSoFar.mean,group=Representative)) + geom_line(aes(colour=Representative)) + ylab("Fitness") + ylim(0,6) +
  geom_ribbon(aes(ymax = BestSoFar.mean + BestSoFar.se, ymin = BestSoFar.mean - BestSoFar.se), alpha = 0.1) + facet_wrap(~ Task + Method,scales="free_x",ncol=2)
ggplot(lastGen(data), aes(Method,BestSoFar)) + geom_boxplot() + ylab("Fitness") + facet_wrap(~ Task) + ylim(0,6) + ggtitle("Same number of generations")
metaAnalysis(lastGen(data), BestSoFar~Method, ~Task, summary=F)

evalcap <- data[((Task=="Var-Mid" | Task=="Var-Sep") & Evaluations <= 209700) | Evaluations <= 150000]

agg <- summaryBy(BestSoFar ~ Task + Method + Representative + Evaluations, evalcap, FUN=c(mean,se))
ggplot(agg, aes(Evaluations,BestSoFar.mean,group=Representative)) + geom_line(aes(colour=Representative)) + ylab("Fitness") + ylim(0,6) +
  geom_ribbon(aes(ymax = BestSoFar.mean + BestSoFar.se, ymin = BestSoFar.mean - BestSoFar.se), alpha = 0.1) + facet_wrap(~ Task + Method,scales="free_x",ncol=2)
ggplot(lastGen(evalcap), aes(Representative,BestSoFar)) + geom_boxplot() + ylab("Fitness") + facet_wrap(~ Task+Method,ncol=4) + ylim(0,6) + theme(axis.text.x=element_text(angle=22.5,vjust=0.5))
metaAnalysis(lastGen(evalcap), BestSoFar~Representative, ~Task+Method, summary=T)





data <- loadData(c("stable_tog/fit","stable_sep/fit","down_tog/fit","down_mid/fit","down_sep_long/fit","stable_tog/fit_random","stable_sep/fit_random","down_tog/fit_random","down_mid/fit_random","down_sep_long/fit_random"), auto.ids.names=c("Task","Method"), filename="postfitness.stat", fun="loadFitness")
data <- data[Generation < 700]
data$Method <- factor(data$Method, levels=c("fit","fit_random"), labels=c("Fit-Best","Fit-Random"))
data$Task <- factor(data$Task, levels=c("stable_tog","stable_sep","down_tog","down_mid","down_sep_long"), labels=c("Fix-Tog","Fix-Sep","Var-Tog","Var-Mid","Var-Sep"))

agg <- summaryBy(BestSoFar ~ Task + Method + Generation, data, FUN=c(mean,se))
ggplot(agg, aes(Generation,BestSoFar.mean,group=Method)) + geom_line(aes(colour=Method)) + ylab("Fitness") + ylim(0,6) +
  geom_ribbon(aes(ymax = BestSoFar.mean + BestSoFar.se, ymin = BestSoFar.mean - BestSoFar.se), alpha = 0.1) + facet_wrap(~ Task,ncol=2)
ggplot(lastGen(data), aes(Method,BestSoFar)) + geom_boxplot() + ylab("Fitness") + facet_wrap(~ Task) + ylim(0,6)
metaAnalysis(lastGen(data), BestSoFar~Method, ~Task, summary=T)



data <- loadData("*/moea", filename="refitness.stat", fun="loadFitness", recursive=F, jobs=0:9)[ID1 != "down_sep" & ID1 != "stable_mid"]
ggplot(data, aes(Generation,BestGen)) + geom_line() + ylab("Fitness") + facet_grid(ID1 ~ Job)

vars.group <- c("Items","Within","Dispersion","AvgProximity") ; vars.ind <- c("I1","I2","I3","I4") ; vars.extra <- c("Height","TimeWithin")
data <- loadData(c("*/fit","*/staged","*/halted","*/moea","*/nst"), filename="rebehaviours.stat", fun="loadBehaviours", vars=c(vars.group,rep(NA,10),vars.extra))
data <- data[ID1 != "down_sep" & ID1 != "stable_mid" & ID1 != "stable_tog"]
data <- data[!((ID1 =="down_tog" | ID1=="stable_sep") & Generation >= 500)]
data$Items <- 1 - data$Items

agg <- summaryBy(Items + Height + TimeWithin ~ Generation + ID1 + ID2, data, FUN=c(mean,se))
ggplot(agg, aes(Generation,Items.mean,group=ID2)) + geom_line(aes(colour=ID2)) + facet_wrap(~ ID1, scales="free_x") +
  geom_ribbon(aes(ymax = Items.mean + Items.se, ymin = Items.mean - Items.se), alpha = 0.1)
ggplot(agg, aes(Generation,TimeWithin.mean,group=ID2)) + geom_line(aes(colour=ID2)) + facet_wrap(~ ID1, scales="free_x") +
  geom_ribbon(aes(ymax = TimeWithin.mean + TimeWithin.se, ymin = TimeWithin.mean - TimeWithin.se), alpha = 0.1)
ggplot(agg, aes(Generation,Height.mean,group=ID2)) + geom_line(aes(colour=ID2)) + facet_wrap(~ ID1, scales="free_x") +
  geom_ribbon(aes(ymax = Height.mean + Height.se, ymin = Height.mean - Height.se), alpha = 0.1)


setwd("~/exps/")
data <- loadData("naco3", filename="postfitness.stat", fun="loadFitness", recursive=T)
bestSoFarFitness(data)
fitnessBoxplots(data)

data <- loadData("naco2", filename="postfitness.stat", fun="loadFitness", recursive=T)

agg <- summaryBy(BestSoFar ~ ID2 + ID3 + Generation, data, FUN=c(mean,se))
ggplot(agg, aes(Generation,BestSoFar.mean,group=ID3)) + geom_line(aes(colour=ID3)) + ylab("Fitness") + ylim(0,6) +
  geom_ribbon(aes(ymax = BestSoFar.mean + BestSoFar.se, ymin = BestSoFar.mean - BestSoFar.se), alpha = 0.1) + facet_wrap(~ ID2,scales="free_x")
ggplot(lastGen(data), aes(ID3,BestSoFar)) + geom_boxplot() + ylab("Fitness") + facet_wrap(~ ID2) + ylim(0,6)


data <- loadData("naco2/stable_sep/moea_random/", filename="behaviours.stat", fun="loadBehaviours", vars=c(rep(NA,8),vars.extra))

front <- function(gen, objectives=c("Fitness","TimeWithin","Height")) {
  sub <- subset(gen, select=objectives)
  f <- nondominated_points(-t(as.matrix(sub)))
  return(-t(f))
}
fronts <- ddply(data, .(Setup,Job,Subpop,Generation), front)

ggplot(fronts,aes(TimeWithin,Fitness)) + geom_point(aes(colour=Generation)) + facet_grid(Subpop ~ Job) + scale_colour_distiller(palette="Spectral")
  


#### FINAL REVISION #######


setwd("/media/jorge/Orico/naco")
fit <- loadData(c("**/gaga","**/neatga","**/ganeat","**/fit"),"postfitness.stat",fun=loadFitness, auto.ids.sep="/",auto.ids.names=c("Task","Method"))
fit <- fit[Task != "down_sep_long" & Task != "stable_mid"]
fit[, Task := factor(Task, levels=c("stable_tog","stable_sep","down_tog","down_mid","down_sep"), labels=c("Fix-Tog","Fix-Sep","Var-Tog","Var-Mid","Var-Sep"))]
fit[, Method := factor(Method, labels=c("GA-GA","NEAT-GA","GA-NEAT","NEAT-NEAT"))]

ggplot(lastGen(fit), aes(Task,BestSoFar)) + geom_boxplot(aes(fill=Method),outlier.size=1, lwd=.4) + ylim(0,6) + ylab("Fitness")
ggsave("~/Dropbox/Work/Papers/NACO/revision_submission/ga_boxplot.pdf", width=5,height=3.5)


load("~/Dropbox/Work/Papers/NACO/rdata/improv.div.rdata")

data <- as.data.frame(improv.div$data)
data[,"Task"] <- rownames(data)
data <- addTaskInfo(data,"Task")
d <- melt(data)
ggplot(d, aes(Task,value)) + geom_boxplot(aes(fill=Method),outlier.size=1, lwd=.4) + ylab("Behavioural diversity")
ggsave("~/Dropbox/Work/Papers/NACO/revision_submission/improv_diversity_boxplot.pdf", width=5,height=3.5)
