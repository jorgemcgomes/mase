### DATA LOADING AND CLEANUP #############################################################

vars <- paste0("Behav_",0:6)
tasknames <- c("foraging","obsforaging","phototaxis","obsphototaxis","exploration","maze","avoidance","prey","tracking")

beautify <- function(data) {
  data[, Task := factor(Task, levels=tasknames, labels=c("Foraging","Foraging-O","Phototaxis","Phototaxis-O","Exploration","Maze","Avoidance","Prey","Tracking"))]
  if("Method" %in% colnames(data)) {
    data[!is.na(Repo) & Repo!="base", Config := paste(Method,Repo,sep="-")]
    data[is.na(Config), Config := Method]
    data[, Config := factor(Config)]
  }
}

setwd("~/exps/playground2/rep")

# behaviour data already reduced to 2D
load("~/Dropbox/Work/Papers/17-SWEVO/reducedrepos.rdata")

# load repertoire data and reduce behaviour space (instead of the line above)
repos <- loadData(c("base","randmlp"), filename="archive.stat", auto.ids.sep="_", auto.ids.names=c("Repo"))
evorepos <- loadData("base", filename="behaviours.stat", fun=loadBehaviours, auto.ids.names=c("Repo"), jobs=9)
setnames(evorepos, paste0("PlaygroundSDBC.0_",0:6), vars)
red <- reduceData(rbind(evorepos,repos,fill=T), vars=vars, method="rpca", k=7)
ggsave("~/Dropbox/Work/Papers/17-SWEVO/reduced_variance.pdf")
repos[, c("V1","V2") := red[is.na(Generation),.(V1,V2)]]
evorepos[, c("V1","V2") := red[!is.na(Generation),.(V1,V2)]]
rm(red) ; gc()
#save(repos, evorepos, file="~/Dropbox/Work/Papers/17-SWEVO/reducedrepos.rdata")

# load fitness of primitives evaluated in the tasks
repofit <- rbindlist(lapply(tasknames, function(t){loadData("**", paste0(t,"fitness.stat") ,fun=loadFile, ids=list(Task=t), auto.ids.names=c("Repo"))}))
setnames(repofit, "Generation", "Index")
#repofit <- merge(repofit, repos, by=c("Index","Job"))
beautify(repofit)

# load fitness of evolutionary processes
setwd("~/exps/playground2/tasks")
fit <- loadData("*", "postfitness.stat", fun=loadFitness, auto.ids.sep="_", auto.ids.names=c("Method","Task","Repo","RepoJob"))
beautify(fit)

# load tree stats of the best-of-run controllers
trees <- loadData("**", "postbest.xml.gp.stat" ,fun=loadFile, auto.ids.sep="_", auto.ids.names=c("Method","Task","Repo","RepoJob"))
beautify(trees)

# load primitive selection stats of the best-of-run controllers
act <- loadData("**", "postbest.xml.stat", auto.ids.sep="_", auto.ids.names=c("Method","Task","Repo","RepoJob"), keep.cols=c("Seed","Repetition","Time","Primitive"))
act <- act[Method != "direct" & Method != "directrec"]
beautify(act)
#act <- merge(act, repos, by.x=c("Primitive","RepoJob"), by.y=c("Index","Job"))
act.stats <- act[, .(Number=length(unique(Primitive)), Duration=mean(rle(Primitive)$lengths), 
                     MaxDuration=max(rle(Primitive)$lengths), MostFrequent=max(.SD[,.N,by=.(Primitive)]$N)*100/.N),
               by=.(Repetition, Seed, Job, RepoJob, Method, Config, Task)]


### REPERTOIRE INSPECTION PLOTS ##############################################

# Repertoire evolution plots (for one job)
gens <- c(1,10,50,100,250,500)
selected <- evorepos[Repo=="base" & Job==9]
selected[, Generation := Generation + 1]
flist <- lapply(gens, function(g){selected[Generation<=g]})
names(flist) <- paste("Gen",gens)
flist[["NS-Evolved repertoire"]] <- repos[Repo=="base" & Job==9]
flist[["Randomly generated repertoire"]] <- repos[Repo=="randmlp" & Job==9]
f <- rbindlist(flist, idcol="id")
f[, id := factor(id, levels=names(flist))]
ggplot(f, aes(x=V1, y=V2)) + geom_point(stroke=0, size=.4) + coord_fixed() + labs(x="PC1", y="PC2") + facet_wrap(~ id) +
  scale_x_continuous(breaks=seq(0,1,by=0.2)) + scale_y_continuous(breaks=seq(0,1,by=0.2))
ggsave("~/Dropbox/Work/Papers/17-SWEVO/rep_evolution.png", width=4.7, height=3.9)

# Plot behaviour space characterisations
require(cluster)
selected <- repos[Repo=="base"] # used for showing the point cloud behind
cluster <- clara(selected[,.(V1,V2)], k=30, samples=50, sampsize=500) 
centers <- selected[cluster$i.med, c("Job","Index","V1","V2",vars), with=F]
centers[, Index := paste0(Job,"-",Index)]

# points for the larger circles that serve as scale
circlescale <- function(centers, r=0.05) {
  cbind(rbindlist(apply(centers[,.(V1,V2)], 1, full.circle, r=r, npoints=100)), Index=rep(centers$Index, each=100))
}
m <- melt(centers, measure.vars=vars)
m[, variable := factor(variable, labels=c("Walls-Dist","Obstacle-Mean-Dist","Obstacle-Closest-Dist","Object-Mean-Dist","Object-Closest-Dist","Linear-Speed","Turn-Speed"))]
m[, angle := ((as.numeric(variable) - 1) / length(levels(variable))) * -2 * pi + pi/2]
m[, rad := (value - min(value)) / (max(value)-min(value)) , by=.(variable)] # normalize each var to [0,1]
polydata <- m[, circle.sector(center=c(V1,V2), r=0.05*rad, fromangle=angle-pi/length(vars), toangle=angle+pi/length(vars)), by=.(Index,variable)]

#./run.sh mase.mason.MasonTracer -gc ~/exps/playground2/rep/base/job.0.finalarchive.tar.gz -s 500 -o ~/Dropbox/Work/Papers/17-SWEVO/trac2 -index 997 371 380 197 798 881 785 988 -p problem.randomPosition=false -seed 24
# from repertoire 0
traced <-  c(a=988,b=997,c=376,d=696,e=785,f=71)
traced.f <- repos[Repo=="base" & Job == 0 & Index %in% traced]
traced.f[, Label := names(sort(traced))]

# finally, we have everything needed for the plot
ggplot(polydata, aes(x, y)) + 
  geom_point(data=selected, aes(V1,V2), colour="gray90", shape=16, size=.5) +
  geom_polygon(aes(fill=variable,group=paste(Index,variable))) +
  geom_path(data=circlescale(centers, r=0.05), aes(group=Index), size=.2) +
  geom_path(data=circlescale(centers, r=0.025), aes(group=Index), size=.2) +
  geom_spoke(data=m, aes(V1,V2,angle=angle+pi/length(vars), radius=0.05), size=.2) +
  #geom_point(data=traced.f, aes(x=V1,y=V2), colour="blue", shape=16) +
  geom_text(data=traced.f, aes(x=V1,y=V2,label=Label), colour="blue", fontface="bold") + # hjust="right", nudge_x=-0.01
  guides(fill=guide_legend(nrow=3)) + coord_fixed() + labs(x="PC1",y="PC2",fill="Feature") +
  theme(legend.position="right") + guides(fill=guide_legend(ncol=1))
ggsave("~/Dropbox/Work/Papers/17-SWEVO/rep_codes.png", width=4.7, height=2.4)

# plot which primitives (from which repertoire) were used as cluster centers
ggplot(centers, aes(V1, V2)) + 
  geom_point(data=selected, colour="lightgray", shape=16, size=1) +
  geom_label(aes(label=Index), size=2) + coord_fixed() + labs(colour=NULL, x="PC1",y="PC2") +
  ggtitle(paste(selected$Setup[0], selected$Job[0]))
ggsave("~/Dropbox/Work/Papers/17-SWEVO/rep_bests_index.png", width=4, height=2.5)

# Quality of the repertoires -- fitness achieved in each task by the primitives
q.repos <- repofit[Repo %in% c("base","randmlp"), .(Fitness=max(MaxFitness)), by=.(Task,Job,Repo)]
q.tr <- lastGen(fit)[Config=="direct", .(Fitness=BestSoFar, Repo="tr"), by=.(Task,Job)]
q.rand <- repofit[Repo=="randmlp", .(Fitness=MaxFitness, Repo="baseline"), by=.(Task,Job)]
plotdata <- rbind(q.repos,q.tr,q.rand)
plotdata[, Repo := factor(Repo, levels=c("tr","base","randmlp","baseline"), labels=c("Tabula-rasa","NS-evolved repertoire","Random-MLP repertoire","Baseline"))]

#q.tr5 <- fit[Config=="direct" & Generation==10, .(Fitness=BestSoFar, Repo="tr5"), by=.(Task,Job)]
#plotdata <- rbind(q.repos,q.tr,q.tr5,q.rand)
#plotdata[, Repo := factor(Repo, levels=c("tr","tr5","base","randmlp","baseline"), labels=c("Tabula-rasa","Tabula-rasa(5)","NS-evolved repertoire","Random-MLP repertoire","Baseline"))]

ggplot(plotdata[,.(Fitness=mean(Fitness),SE=se(Fitness)), by=.(Repo,Task)], aes(x=Repo,y=Fitness, fill=Repo)) + 
  geom_bar(stat="identity", position="dodge") + facet_wrap(~ Task, scales="free_y", nrow=2) + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.5, size=.25, position=position_dodge(.9)) +
  theme(axis.title.x=element_blank(), axis.text.x=element_blank(), axis.ticks.x=element_blank()) + labs(y="Fitness in task", fill="Setup")
ggsave("~/Dropbox/Work/Papers/17-SWEVO/repo_quality.pdf", width=4.7, height=3)

metaAnalysis(plotdata[,.(Fitness=mean(Fitness)), by=.(Repo,Task)], Fitness ~ Repo, paired=T)
m <- metaAnalysis(plotdata, Fitness ~ Repo, ~Task, paired=F)
sapply(m, function(x) x$ttest$holm[1,3]) # tr vs evolved repo
sapply(m, function(x) x$ttest$holm[1,2]) # evolved vs random repos


# Usefulness heatmap -- which primitives worked best for solving each task?
base <- repofit[Repo=="base"]
merged <- merge(base, repos[Repo=="base"], by=c("Index","Job"))
merged[, Bin1 := round(V1*20)/20]
merged[, Bin2 := round(V2*20)/20]
sum <- merged[, .(Fitness=mean(MaxFitness)), by=.(Task,Bin1,Bin2)]
sum[, ScaledFitness := (Fitness - min(Fitness)) / (max(Fitness)-min(Fitness)), by=.(Task)]

ggplot(sum, aes(Bin1,Bin2,fill=ScaledFitness)) + 
  scale_x_continuous(expand=c(0,0)) + scale_y_continuous(expand=c(0,0)) + labs(x="PC1", y="PC2", fill="Fitness in task (scaled)") +
  geom_tile() + facet_wrap(~ Task) + scale_fill_distiller(palette="Spectral") + coord_fixed()
ggsave("~/Dropbox/Work/Papers/17-SWEVO/repo_usefulness.pdf", width=4.7, height=4.2)

# Generation environment
bests <- repofit[Repo %in% c("few","fixed","none","noobj","noobs","base"), .(BestFitness=max(BestSoFar), MeanFitness=mean(BestSoFar)), by=.(Task,Repo,Job)]
bests[, Repo := factor(Repo, levels=c("base","fixed","few","noobs","noobj","none"), labels=c("Base","Fixed","Few","No-obstacles","No-POIs","Only-walls"))]
ggplot(bests[,.(Fitness=mean(BestFitness),SE=se(BestFitness)), by=.(Repo,Task)], aes(x=Repo,y=Fitness, fill=Repo)) + 
  geom_bar(stat="identity", position="dodge") + facet_wrap(~ Task, scales="free_y", nrow=2) + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.5, size=.25, position=position_dodge(.9)) +
  theme(axis.title.x=element_blank(), axis.text.x=element_blank(), axis.ticks.x=element_blank()) +
  labs(y="Highest fitness in task", fill="Repertoire evo. environment")
ggsave("~/Dropbox/Work/Papers/17-SWEVO/environments.pdf", width=4.7, height=3)

metaAnalysis(bests[,.(Fitness=mean(BestFitness)), by=.(Repo,Task)], Fitness ~ Repo, paired=T)
m <- metaAnalysis(bests, BestFitness ~ Repo, ~Task, paired=F)
sapply(m, function(x) x$ttest$kruskal$p.value)
sapply(m, function(x) x$ttest$holm[1,3]) # fixed
sapply(m, function(x) x$ttest$holm[1,6]) # no-obstacles
sapply(m, function(x) x$ttest$holm[1,2]) # few
sapply(m, function(x) x$ttest$holm[1,5]) # no-pois
sapply(m, function(x) x$ttest$holm[1,4]) # only-walls

### EVORBC-II ANALYSIS PLOTS ######################################

# Highest fitness plot with GP and baselines
directrepo <- repofit[Repo=="base", .(Fitness=max(MaxFitness),Setup="NS-evolved repertoire"), by=.(Task,Job)]
tabularasa <- lastGen(fit)[Config=="direct", .(Fitness=BestSoFar,Setup="Tabula-rasa"), by=.(Task,Job)]
evorbc2 <- lastGen(fit)[Config=="gp", .(Fitness=BestSoFar,Setup="EvoRBC-II"), by=.(Task,Job)]
baseline <- repofit[Repo=="randmlp", .(Fitness=MaxFitness,Setup="Baseline"), by=.(Task,Job)]
plotdata <- rbind(directrepo,tabularasa,evorbc2,baseline)
plotdata[, Setup := factor(Setup,levels=c("Tabula-rasa","EvoRBC-II","NS-evolved repertoire","Baseline"))]
ggplot(plotdata[,.(Fitness=mean(Fitness),SE=se(Fitness)), by=.(Setup,Task)], aes(x=Setup,y=Fitness, fill=Setup)) + 
  geom_bar(stat="identity", position="dodge") + facet_wrap(~ Task, scales="free_y", nrow=2) + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.5, size=.25, position=position_dodge(.9)) +
  theme(axis.title.x=element_blank(), axis.text.x=element_blank(), axis.ticks.x=element_blank()) +
  labs(y="Highest fitness achieved")
ggsave("~/Dropbox/Work/Papers/17-SWEVO/base_comparison.pdf", width=4.7, height=3)


#metaAnalysis(plotdata[,.(Fitness=mean(Fitness)), by=.(Setup,Task)], Fitness ~ Setup, paired=T)
m <- metaAnalysis(plotdata, Fitness ~ Setup, ~Task, paired=F)
sapply(m, function(x) x$ttest$kruskal$p.value)
sapply(m, function(x) x$ttest$holm[2,3]) # TR vs evorbc
sapply(m, function(x) x$ttest$holm[1,3]) # Repo vs evorbc

# relative difference
means <- dcast(plotdata[, mean(Fitness), by=.(Setup,Task)], Task ~ Setup)
means[, EvoRBC.TR.Diff := (`EvoRBC-II`-`Tabula-rasa`) / `Tabula-rasa` * 100]
means[, Repo.TR.Diff := (`NS-evolved repertoire`-`Tabula-rasa`) / `Tabula-rasa` * 100]

# Fitness X evaluations -- EvoRBC-I, EvoRBC-II, TR
genmean <- fit[Config %in% c("gp","direct"), .(Fitness=mean(BestSoFar), SE=se(BestSoFar)), by=.(Task,Config,Evaluations)]
ggplot(genmean, aes(Evaluations, Fitness, group=Config)) + geom_ribbon(aes(ymin=Fitness-SE,ymax=Fitness+SE,fill=Config), alpha=.15) +
  geom_line(aes(colour=Config)) + facet_wrap(~ Task, scales="free_y", ncol=3) + labs(y="Scaled Fitness", colour="Method", fill="Method")
ggsave("~/Dropbox/Work/Papers/17-SWEVO/fitness_evo.pdf", width=4.7, height=4.5)

# Repertoire usage by the arbitrator
m <- merge(act[Config=="gp"], repos[Repo=="base"], by.x=c("Primitive","RepoJob"),by.y=c("Index","Job"))
ggplot(m, aes(V1,V2)) + coord_fixed() +
  geom_point(data=repos[Repo=="base" & Job==9], colour="lightgray", shape=16, size=.5) +
  stat_density2d(geom="polygon", h=0.2,bins=10, aes(fill=..level..), alpha=.6) + facet_wrap(~ Task, ncol=3) +
  scale_fill_distiller(palette="Spectral") + xlim(range(m$V1)+c(-0.05,0.05)) + ylim(range(m$V2)+c(-0.05,0.05)) +
  theme(axis.text.x = element_blank(), axis.text.y = element_blank(), axis.ticks=element_blank()) + labs(x=NULL, y=NULL, fill="Density")
ggsave("~/Dropbox/Work/Papers/17-SWEVO/rep_usage.png", width=4.7, height=4.3)

# Primitive activation stats (plots)
act.sum <- act.stats[Config=="gp", .(NumPrimitives=mean(Number), MeanDuration=mean(Duration), MostFrequent=mean(MostFrequent)), by=.(Config, Task)]
setorder(act.sum, Task)
ggplot(act.sum, aes(Task,NumPrimitives,fill=Config)) + geom_bar(stat="identity", position="dodge")
ggplot(act.sum, aes(Task,MeanDuration,fill=Config)) + geom_bar(stat="identity", position="dodge") 
ggplot(act.sum, aes(Task,MostFrequent,fill=Config)) + geom_bar(stat="identity", position="dodge") 


# Primitive activation stats (table)
ro <- function(x){sprintf("%.1f", as.numeric(x))}
act.sum <- act.stats[Config=="gp", .(NumPrimitives=mean(Number), MeanDuration=mean(Duration), MostFrequent=mean(MostFrequent)), by=.(Task,Job,RepoJob)]
act.sum <- act.sum[, .(NumPrimitives=mean(NumPrimitives), SDNum=sd(NumPrimitives), MeanDuration=mean(MeanDuration), SDDur=sd(MeanDuration), MostFrequent=mean(MostFrequent), SDFreq=sd(MostFrequent)), by=.(Task)]
tree.sum <- trees[Config=="gp", .(TreeSplits=mean(f.P-1),SDSplits=sd(f.P-1)), by=.(Task)]
act.sum <- merge(act.sum, tree.sum, by="Task")
setorder(act.sum, Task)
apply(act.sum, 1, function(x) cat(x[["Task"]]," & ",ro(x[["TreeSplits"]])," & (",ro(x[["SDSplits"]]), ") & ",ro(x[["NumPrimitives"]])," & (",ro(x[["SDNum"]]), ") & ",ro(x[["MeanDuration"]])," & (", ro(x[["SDDur"]]), ") &", ro(x[["MostFrequent"]])," & (", ro(x[["SDFreq"]]), ") \\\\\n", sep=""))

metaAnalysis(act.stats[Config=="gp"], Number ~ Task)
metaAnalysis(act.stats[Config=="gp"], Duration ~ Task)
metaAnalysis(trees[Config=="gp"], f.P ~ Task)

# Selected trees for vizualization
bests <- trees[Config=="gp"]
bests[, TaskThreshold := quantile(Fitness,0.9), by=.(Task)]
selected <- bests[Fitness >= TaskThreshold]
setorder(selected, Task,Size)

# ./run.sh mase.mason.MasonTracer -gc ~/exps/playground2/tasks/gp_phototaxis_base_0/job.7.postbest.xml -s 500 -seed 0 -o ~/Dropbox/Work/Papers/17-SWEVO/trace_phototaxis.svg
# ./run.sh mase.mason.MasonTracer -gc ~/exps/playground2/tasks/gp_obsphototaxis_base_5/job.2.postbest.xml -s 500 -seed 0 -o ~/Dropbox/Work/Papers/17-SWEVO/trace_obsphototaxis.svg
# ./run.sh mase.mason.MasonTracer -gc ~/exps/playground2/tasks/gp_prey_base_7/job.6.postbest.xml -s 500 -seed 1 -o ~/Dropbox/Work/Papers/17-SWEVO/trace_prey.svg
# ./run.sh mase.mason.MasonTracer -gc ~/exps/playground2/tasks/gp_foraging_base_4/job.0.postbest.xml -s 500 -seed 1 -o ~/Dropbox/Work/Papers/17-SWEVO/trace_foraging.svg
# ./run.sh mase.evorbc.gp.DecisionTreeViz ~/exps/playground2/tasks/gp_phototaxis_base_0/job.7.postbest.xml
# ./run.sh mase.evorbc.gp.DecisionTreeViz ~/exps/playground2/tasks/gp_obsphototaxis_base_5/job.2.postbest.xml
# ./run.sh mase.evorbc.gp.DecisionTreeViz ~/exps/playground2/tasks/gp_prey_base_7/job.6.postbest.xml
# ./run.sh mase.evorbc.gp.DecisionTreeViz ~/exps/playground2/tasks/gp_foraging_base_4/job.0.postbest.xml

# locomotion repertoire
locorep <- fread("~/exps/playground2/rep/loco/job.1.finalrep.stat")
ggplot(locorep, aes(Behav_0,Behav_1)) + geom_point(size=.5) + coord_fixed() +
  geom_spoke(aes(angle=CircularFitnessLog.2_0), size=.3, radius=1) +
  scale_x_continuous(breaks=seq(-100,100,5), minor_breaks=seq(-100.5,100.5,1)) + scale_y_continuous(breaks=seq(-100,100,5), minor_breaks=seq(-100.5,100.5,1)) +
  theme(panel.grid.major=element_blank()) +
  labs(x="Front-back displacement (cm)",y="Left-right displacement (cm)") + geom_polygon(data=full.circle(r=4), aes(x=x,y=y), alpha=.2, fill="Blue") +
  geom_polygon(data=data.table(X=c(-1.5,2.5,-1.5),Y=c(-2,0,2)), aes(x=X,y=Y), alpha=.3,fill="Blue")
ggsave("~/Dropbox/Work/Papers/17-SWEVO/locomotion_repertoire.pdf", width=3.5, height=1.7)

# locomotion vs closed-loop
comp <- lastGen(fit)[Config %in% c("gp","gp-loco")]
comp[, Config := factor(Config, labels=c("EvoRBC-II","EvoRBC"))]
m <- metaAnalysis(comp, BestSoFar ~ Config, ~Task, paired=F)
sapply(m, function(x) x$ttest$holm[1,2])

t <- trees[Config %in% c("gp","gp-loco")]
t[, Config := factor(Config, labels=c("EvoRBC-II","EvoRBC"))]
m <- metaAnalysis(t, f.P ~ Config, ~ Task)
sapply(m, function(x) x$ttest$holm[1,2])

act.sum <- act.stats[Config %in% c("gp","gp-loco"), .(NumPrimitives=mean(Number), SENum=se(Number), MeanDuration=mean(Duration), SEDur=se(Duration)), by=.(Config, Task)]
act.sum[, Config := factor(Config, labels=c("EvoRBC-II","EvoRBC"))]
m <- metaAnalysis(act.stats[Config %in% c("gp","gp-loco")], Number ~ Config, ~ Task)
sapply(m, function(x) x$ttest$holm[1,2])
m <- metaAnalysis(act.stats[Config %in% c("gp","gp-loco")], Duration ~ Config, ~ Task)
sapply(m, function(x) x$ttest$holm[1,2])

gf <- ggplot(comp[,.(Fitness=mean(BestSoFar),SE=se(BestSoFar)), by=.(Config,Task)], aes(x=Task,y=Fitness, fill=Config)) + 
  geom_bar(stat="identity", position="dodge") + labs(y="a) Highest fitness achieved") + ggtitle("a) Fitness") +
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.5, size=.25, position=position_dodge(.9)) +
  theme(axis.text.x = element_text(angle = 22.5, hjust = 1), plot.margin=margin(0,2,2,0,"mm"))
gt <- ggplot(t[,.(Splits=mean(f.P-1),SE=se(f.P-1)), by=.(Config,Task)], aes(Task,Splits,fill=Config)) +
  geom_bar(stat="identity", position="dodge") + labs(y="Number of splits in arbitrator tree") +
  geom_errorbar(aes(ymin=Splits-SE,ymax=Splits+SE), width=.5, size=.25, position=position_dodge(.9)) +
  theme(axis.text.x = element_text(angle = 22.5, hjust = 1), plot.margin=margin(0,0,2,2,"mm")) + ggtitle("b) Tree splits")
gn <- ggplot(act.sum, aes(Task,NumPrimitives,fill=Config)) + geom_bar(stat="identity", position="dodge") +
  geom_errorbar(aes(ymin=NumPrimitives-SENum,ymax=NumPrimitives+SENum), width=.5, size=.25, position=position_dodge(.9)) +
  ylab("Mean number of primitives used") + theme(axis.text.x = element_text(angle = 22.5, hjust = 1), plot.margin=margin(2,2,0,0,"mm")) + ggtitle("c) Primitives used")
gd <- ggplot(act.sum, aes(Task,MeanDuration,fill=Config)) + geom_bar(stat="identity", position="dodge") +
  geom_errorbar(aes(ymin=MeanDuration-SEDur,ymax=MeanDuration+SEDur), width=.5, size=.25, position=position_dodge(.9)) +
  ylab("Mean duration of each primitive (steps)") + theme(axis.text.x = element_text(angle = 22.5, hjust = 1), plot.margin=margin(2,0,0,2,"mm")) + ggtitle("d) Primitive duration")
plot_grid(gf, gt, gn, gd)
ggsave("~/Dropbox/Work/Papers/17-SWEVO/locomotion_comparison.pdf", width=4.7, height=4.5)




# Other experiments that did not make it to the paper ###################

# repertoire evo environment time
bests <- repofit[Repo %in% c("veryshort","short","base","long","verylong"), .(BestFitness=max(BestSoFar), MeanFitness=mean(BestSoFar)), by=.(Task,Repo,Job)]
bests[, Repo := factor(Repo, levels=c("veryshort","short","base","long","verylong"), labels=c("T50","T100","T200","T500","T1000"))]
ggplot(bests[,.(Fitness=mean(BestFitness),SE=se(BestFitness)), by=.(Repo,Task)], aes(x=Repo,y=Fitness, fill=Repo)) + 
  geom_bar(stat="identity", position="dodge") + facet_wrap(~ Task, scales="free_y", nrow=2) + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.5, size=.25, position=position_dodge(.9)) +
  theme(axis.title.x=element_blank(), axis.text.x=element_blank(), axis.ticks.x=element_blank()) +
  labs(y="Highest fitness in task", fill="Repertoire evo. environment")
ggsave("~/Dropbox/Work/Papers/17-SWEVO/repo_evo_time.pdf", width=4.7, height=3)


# Repertoire variability (primitives in tasks)
directrepo <- repofit[Repo=="base", .(Fitness=max(MaxFitness)), by=.(Task,Job)]
ggplot(directrepo[,.(Fitness=mean(Fitness)), by=.(Job,Task)], aes(x=Job,y=Fitness, fill=Job)) + 
  geom_bar(stat="identity", position="dodge") + facet_wrap(~ Task, scales="free_y", nrow=2) +
  labs(y="Highest fitness in task", x="Repertoire") + guides(fill=F)

metaAnalysis(directrepo, Fitness ~ Job, paired=T)

# Repertoire variability (arbitrators in tasks)
evorbc2 <- lastGen(fit)[Config=="gp"]
ggplot(evorbc2[,.(Fitness=mean(BestSoFar),SE=se(BestSoFar)), by=.(RepoJob,Task)], aes(x=RepoJob,y=Fitness, fill=RepoJob)) + 
  geom_bar(stat="identity", position="dodge") + facet_wrap(~ Task, scales="free_y", nrow=2) + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.5, size=.25, position=position_dodge(.9))

metaAnalysis(evorbc2[,.(Fitness=mean(BestSoFar)), by=.(RepoJob,Task)], Fitness ~ RepoJob, paired=T)
m <- metaAnalysis(evorbc2, BestSoFar ~ RepoJob, ~Task, paired=F)
sapply(m, function(x) x$ttest$kruskal$p.value)

# tabula-rasa recursion or not
comp <- lastGen(fit)[Config %in% c("direct","directrec")]
ggplot(comp[,.(Fitness=mean(BestSoFar),SE=se(BestSoFar)), by=.(Config,Task)], aes(x=Config,y=Fitness, fill=Config)) + 
  geom_bar(stat="identity", position="dodge") + facet_wrap(~ Task, scales="free_y", nrow=2) + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.5, size=.25, position=position_dodge(.9))
m <- metaAnalysis(comp, BestSoFar ~ Config, ~Task, paired=F)
sapply(m, function(x) x$ttest$holm[1,2])

# evorbc with neural vs GP arbitrator
comp <- lastGen(fit)[Config %in% c("evorbc","gp","evorbc-loco","gp-loco")]
ggplot(comp[,.(Fitness=mean(BestSoFar),SE=se(BestSoFar)), by=.(Config,Task)], aes(x=Config,y=Fitness, fill=Config)) + 
  geom_bar(stat="identity", position="dodge") + facet_wrap(~ Task, scales="free_y", nrow=2) + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.5, size=.25, position=position_dodge(.9))
m <- metaAnalysis(comp, BestSoFar ~ Config, ~Task, paired=F)
sapply(m, function(x) x$ttest$holm[1,3]) # neural vs gp (base)
sapply(m, function(x) x$ttest$holm[2,4]) # neural vs gp (loco)

# behaviour characterisation
comp <- lastGen(fit)[Config %in% c("gp","gp-as")]
ggplot(comp[,.(Fitness=mean(BestSoFar),SE=se(BestSoFar)), by=.(Config,Task)], aes(x=Config,y=Fitness, fill=Config)) + 
  geom_bar(stat="identity", position="dodge") + facet_wrap(~ Task, scales="free_y", nrow=2) + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.5, size=.25, position=position_dodge(.9)) + guides(fill=F)
m <- metaAnalysis(comp, BestSoFar ~ Config, ~Task, paired=F)
sapply(m, function(x) x$ttest$holm[1,2])

bc <- repofit[Repo %in% c("base","as"), .(Fitness=max(MaxFitness)), by=.(Task,Job,Repo)]
ggplot(bc[,.(Fitness=mean(Fitness), SE=se(Fitness)), by=.(Repo,Task)], aes(x=Repo,y=Fitness, fill=Repo)) + 
  geom_bar(stat="identity", position="dodge") + facet_wrap(~ Task, scales="free_y", nrow=2) +
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.5, size=.25, position=position_dodge(.9)) +
  labs(y="Highest fitness in task", x="Repertoire") + guides(fill=F)

# arbitrator with evolved repertoire vs with random repertoire
comp <- lastGen(fit)[Config %in% c("gp","gp-randmlp")]
ggplot(comp[,.(Fitness=mean(BestSoFar),SE=se(BestSoFar)), by=.(Config,Task)], aes(x=Config,y=Fitness, fill=Config)) + 
  geom_bar(stat="identity", position="dodge") + facet_wrap(~ Task, scales="free_y", nrow=2) + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.5, size=.25, position=position_dodge(.9)) + guides(fill=F)
m <- metaAnalysis(comp, BestSoFar ~ Config, ~Task, paired=F)
sapply(m, function(x) x$ttest$holm[1,2])

# frequency with neural-evorbc-2
setwd("~/exps/playground2/exploratory")
fit.freq <- loadData("evorbc_*_base_0_*", "postfitness.stat", fun=loadFitness, auto.ids.sep="_", auto.ids.names=c("Method","Task","Repo","RepoJob","Frequency"))
beautify(fit.freq)
ggplot(lastGen(fit.freq)[,.(Fitness=mean(BestSoFar),SE=se(BestSoFar)), by=.(Frequency,Task)], aes(x=Frequency,y=Fitness, fill=Frequency)) + 
  geom_bar(stat="identity", position="dodge") + facet_wrap(~ Task, scales="free_y", nrow=2) + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.5, size=.25, position=position_dodge(.9)) + guides(fill=F) +
  labs(x="Steps between each arbitrator decision")
ggsave("~/Dropbox/Work/Papers/17-SWEVO/neural_evorbc2_freq.pdf", width=4.7, height=3)


# directly rewarding primitive stability 

# Temporary / garbage stuff ################################# 
bests <- repofit[Repo %in% c("base","sdbc2","sdbc2sd"), .(BestFitness=max(BestSoFar), MeanFitness=mean(BestSoFar)), by=.(Task,Repo,Job)]
tabularasa <- lastGen(fit)[Config=="direct", .(BestFitness=BestSoFar,Repo="Tabula-rasa"), by=.(Task,Job)]
plotdata <- rbind(bests,tabularasa, fill=T)
ggplot(plotdata[,.(Fitness=mean(BestFitness),SE=se(BestFitness)), by=.(Repo,Task)], aes(x=Repo,y=Fitness, fill=Repo)) + 
  geom_bar(stat="identity", position="dodge") + facet_wrap(~ Task, scales="free_y", nrow=2) + 
  geom_errorbar(aes(ymin=Fitness-SE,ymax=Fitness+SE), width=.5, size=.25, position=position_dodge(.9)) +
  theme(axis.title.x=element_blank(), axis.text.x=element_blank(), axis.ticks.x=element_blank()) +
  labs(y="Highest fitness in task", fill="Repertoire evo. environment")



