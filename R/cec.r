load("~/Dropbox/Work/Papers/18-CEC/expdatalong.rdata")
fit_scale <- scale_y_continuous(limits=c(0,1.2),breaks=seq(0,2,0.2))

# DATA LOADING (only if rdata is not available) #################

# Correctly adds the meta-data to the experiments
beautify <- function(data) {
  data[Method=="rbcselected", Param := factor(Param, labels=c("3","5","10","25","50","100"))]
  data[, Param := factor(Param, ordered=T)]
  data[, Blind := grepl("blind", Method)]
  data[, Method := sub("blind", "", Method)]
  data[, Method := factor(Method, levels=c("tr","rbcneat","rbcsub","rbcselected","gptrees","gptreespars"), labels=c("NEAT-TR","NEAT-EvoRBC","NEAT-Subset","NEAT-Selected","GP-DT","GP-DT(P)"))]
  data[, Config := paste(Method,Blind,Param,sep="-")]
  data[, Config := factor(sub("-NA","",sub("TRUE","B",sub("-FALSE","",Config))))]
}

setwd("~/exps/mazefinal")
fixPostFitness("~/exps/mazefinal")

repos <- loadData("rep", "finalrep.stat")

evofitness <- loadData("**", "fitness.stat" ,fun=loadFitness, auto.ids.names=c("Method","Repo","Param"))
beautify(evofitness)

fitness <- loadData("**", "postfitness.stat" ,fun=loadFitness, auto.ids.names=c("Method","Repo","Param"))
beautify(fitness)

koza <- loadData("**", "koza.stat" ,fun=loadFile, auto.ids.sep="_", auto.ids.names=c("Method","Repo","Param"))
beautify(koza)

trees <- loadData("**", "postbest.xml.gp.stat" ,fun=loadFile, auto.ids.sep="_", auto.ids.names=c("Method","Repo","Param"))
beautify(trees)

beststats <- loadData("**", "postbest.xml.stat" ,fun=loadFile, auto.ids.sep="_", auto.ids.names=c("Method","Repo","Param"), exclude.cols=T, colnames=c("Seed","Repetition","Time","Primitive"))
beautify(beststats)

neat <- loadData("**", "neat.stat" ,fun=loadFile, auto.ids.sep="_", auto.ids.names=c("Method","Repo","Param"))
beautify(neat)

save(repos, evofitness, fitness, koza, beststats, trees, neat, file="~/Dropbox/Work/Papers/18-CEC/expdatalong.rdata")


# Repertoire analysis ##################################

# Compare all repos
ggplot(repos, aes(Behav_0,Behav_1,colour=Fitness)) + geom_point(size=.5) + facet_wrap(~ Job) + coord_fixed() +
  geom_spoke(aes(angle=CircularFitnessLog.2_0), radius=1)
repo.stats <- repos[, .(.N, MeanFitness=mean(Fitness)), by=.(Job)]
repo.stats[, .(N=mean(N),Nmin=min(N),Nmax=max(N),Fit=mean(MeanFitness))]

# Plot the selected repo
ggplot(repos[Job==0], aes(Behav_0,Behav_1)) + geom_point(size=1) + coord_fixed() +
  geom_spoke(aes(angle=CircularFitnessLog.2_0), radius=1) +
  scale_x_continuous(breaks=seq(-21,21,2), minor_breaks=NULL) + scale_y_continuous(breaks=seq(-21,21,2), minor_breaks=NULL) +
  labs(x="x displacement (cm)",y="y displacement (cm)") + geom_rect(xmin=-4,xmax=4,ymin=-4,ymax=4,color="Blue",alpha=0) +
  geom_polygon(data=data.table(X=c(-1.5,2.5,-1.5),Y=c(-2,0,2)), aes(x=X,y=Y), alpha=0,color="Blue")
ggsave("~/Dropbox/Work/Papers/18-CEC/repo.pdf", width=3.5, height=2.5)



# Base maze fitness comparison ############################
d <- fitness[Blind==F & ((Method=="NEAT-Subset" & Param=="25") | Method %in% c("NEAT-TR","NEAT-EvoRBC","GP-DT"))]

ggplot(lastGen(d), aes(x=Method, y=BestSoFar, fill=Method)) + geom_boxplot(outlier.size=.5, size=.25) + 
  fit_scale + labs(y="Highest fitness achieved") + guides(fill=FALSE) + coord_flip()
ggsave("~/Dropbox/Work/Papers/18-CEC/comparison_box.pdf", width=3.5, height=1)
metaAnalysis(lastGen(d), BestSoFar ~ Config)

agg <- d[, mean(BestSoFar), by=.(Method,Evaluations)]
ggplot(agg, aes(Evaluations/1000,V1,group=Method,colour=Method)) + geom_line() + labs(y="Fitness",x="Evaluations (x1000)") +
  geom_point(data=agg[, .SD[seq(from=1,to=.N,length.out=10)], by=Method], aes(shape=Method),size=1) + ylim(0,NA) +
  theme(legend.position="bottom", legend.margin=margin(t=-10,l=-10))
ggsave("~/Dropbox/Work/Papers/18-CEC/comparison_time.pdf", width=3.5, height=2)

# best solutions evolved by each method
bests <- lastGen(d)
setorder(bests, Config, -BestSoFar)
View(bests)

# NEAT-Subset investigation ##################################
d <- fitness[Blind==F & Method=="NEAT-Subset"]

ggplot(lastGen(d), aes(x=Param, y=BestSoFar, fill=Param)) + geom_boxplot(outlier.size=.5, size=.25) + 
  labs(y="Highest fitness achieved") + guides(fill=FALSE) + labs(x="Size of the subset") + scale_y_continuous(breaks=seq(0,1,0.1))
ggsave("~/Dropbox/Work/Papers/18-CEC/neatsubset_box.pdf", width=3.5, height=1.25)
metaAnalysis(lastGen(d)[Param!="3"], BestSoFar ~ Param)
metaAnalysis(lastGen(d), BestSoFar ~ Param)

agg <- d[, mean(BestSoFar), by=.(Param,Evaluations)]
ggplot(agg, aes(Evaluations/1000,V1,group=Param,colour=Param)) + geom_line() + labs(y="Fitness",x="Evaluations (x1000)") +
  geom_point(data=agg[, .SD[seq(from=1,to=.N,length.out=10)], by=Param], aes(shape=Param),size=1) + ylim(0,NA) + theme(legend.position="right")
ggsave("~/Dropbox/Work/Papers/18-CEC/neatsubset_bestsofar.pdf", width=3.5, height=3.5)

# NEAT-Subset vs NEAT-Selected 
d <- fitness[Blind==F & Method %in% c("NEAT-Subset","NEAT-Selected")]
ggplot(lastGen(d), aes(Param,BestSoFar,fill=Method)) + geom_boxplot(outlier.size=.5, size=.25) + 
  labs(x="Size of the subset",y="Highest fitness achieved") + fit_scale
ggsave("~/Dropbox/Work/Papers/18-CEC/subset_selected.pdf", width=3.5, height=3)
sapply(metaAnalysis(lastGen(d), BestSoFar ~ Method, ~ Param), function(x){as.numeric(x$ttest$holm[1,2])})

# Arbitrator stats #################################

d <- beststats[Blind==F & Method %in% c("NEAT-EvoRBC","NEAT-Subset","GP-DT")]
d <- merge(d, repos[,.(Job,Hash,Bin_0,Bin_1,Behav_0,Behav_1)], by.x=c("Primitive","Repo"),by.y=c("Hash","Job"))
setorder(d, Config, Repo, Job, Repetition, Time)
auxdiff <- function(df) {c(NA, sqrt(rowSums(as.data.table(lapply(df, function(x){diff(x, lag=1) ^ 2})))))}
d[, Jump := auxdiff(.SD), by=.(Config,Method,Param,Repo,Job,Repetition), .SDcols=c("Behav_0","Behav_1")]

sim.stats <- d[, .(Number=length(unique(Primitive)), Duration=mean(rle(Primitive)$lengths), Switches=length(rle(Primitive)$values), JumpSize=mean(Jump[Jump!=0], na.rm=T)), by=.(Config,Method,Param,Repo,Job,Repetition)]
solution.stats <- sim.stats[, lapply(.SD, mean, na.rm=T), by=.(Config,Method,Param,Repo,Job)]
method.stats <- solution.stats[, c(lapply(.SD, mean, na.rm=T), lapply(.SD, sd)), by=.(Config,Method,Param), .SDcols=sapply(solution.stats, is.numeric)]
View(method.stats)


# Tree stats #######################################

t <- trees[Blind==F & Method=="GP-DT"]
nodes.melt <- melt(t[, c("Job", colnames(t)[grep("f\\..*", colnames(t))]), with=F], id.vars=c("Job") )
nodes.melt[, variable := sub("f\\.","",variable)]
nodes.melt[is.na(value), value := 0]
nodes.melt[variable=="C", variable := "Constant"]
nodes.melt[variable=="P", variable := "Primitive"]

ggplot(nodes.melt[, .(Mean=mean(value),SE=se(value)), by=.(variable)], aes(variable,Mean)) + 
  geom_bar(stat="identity", position="dodge") + geom_errorbar(aes(ymin=Mean-SE,ymax=Mean+SE), width=.5, size=.25, position=position_dodge(.9)) +
  labs(y="Mean number of nodes in tree", x="Node type") + scale_y_continuous(breaks=seq(0,100,5), minor_breaks=seq(0,100,1)) + coord_flip()
ggsave("~/Dropbox/Work/Papers/18-CEC/gp_best_functions.pdf", width=3.5, height=1.5)

ggplot(t, aes(Size,Fitness)) + geom_point(size=.5) + labs(x="Size", y="Fitness") +
  geom_text(aes(label=paste0(Repo,"/",Job)),vjust="bottom", nudge_y=0.005,alpha=.5,size=2)
ggsave("~/Dropbox/Work/Papers/18-CEC/gp_bests.pdf", width=3.5, height=2)

# Best trees stats
t[, .(Size=mean(Size),SizeSD=sd(Size),SizeMin=min(Size),SizeMax=max(Size),Depth=mean(Depth),DepthSD=sd(Depth))]
cor(t$Fitness, t$Size, method="pearson")
cor(t$Fitness, t$Depth, method="pearson")
cor(t$Fitness, t$f.P, method="pearson")


# Blind comparison #################################

d <- fitness[(Method=="NEAT-EvoRBC" & (Param=="3" | is.na(Param))) | (Method=="NEAT-Subset" & Param=="25") | Method %in% c("GP-DT","NEAT-TR")]
d[, PlotNames := Method]
d[Blind==T, PlotNames := paste0(PlotNames,"-B")]
d[, PlotNames := factor(PlotNames,levels=c("NEAT-TR","NEAT-EvoRBC","NEAT-EvoRBC-B","NEAT-Subset","NEAT-Subset-B","GP-DT","GP-DT-B"))]
ggplot(lastGen(d), aes(PlotNames, BestSoFar,fill=Blind)) + 
  geom_boxplot(outlier.size=.5, size=.25) + ylab("Best fitness") + fit_scale + guides(fill=F) +
  theme(axis.text.x=element_text(angle=22.5,hjust=1)) + labs(x="Method", y="Highest fitness achieved")
ggsave("~/Dropbox/Work/Papers/18-CEC/blind_box.pdf", width=3.5, height=2)

# performance degradation
sapply(metaAnalysis(lastGen(d[Method!="NEAT-TR"]), BestSoFar ~ Blind, ~ Method), function(x){as.numeric(x$ttest$holm[1,2])})
# best blind
metaAnalysis(lastGen(d[Blind==T | Method=="NEAT-TR"]), BestSoFar ~ Method)

# Blind NEAT-Coords
fitnessBoxplots(fitness[Blind==T & Method=="NEAT-EvoRBC"], xvar="Param") + labs(x="Number of dimensions") + guides(fill=F) + 
  ylab("Best fitness") + fit_scale
metaAnalysis(lastGen(fitness)[Blind==T & Method=="NEAT-EvoRBC"], BestSoFar ~ Param)









# Deprecated ######################################################################################

# Repo variability #################################
d <- fitness[Blind==FALSE & Method != "TR"]
fitnessBoxplots(d, xvar="Repo") + facet_wrap(~ Config) + guides(fill=F) + fit_scale

# for each method, does the repertoire have a significant impact?
m <- metaAnalysis(lastGen(d),  BestSoFar ~ Repo, ~ Config)
sapply(m, function(x){x$ttest$kruskal$p.value})

# does any repertoire yield consistently superior results accross all methods?
means <- lastGen(d)[, .(Mean=mean(BestSoFar)), by=.(Repo,Config)]
metaAnalysis(means, Mean ~ Repo, paired=T)


# GP analysis ########################################
d <- koza[Method=="GP-DT"] # do not use the parsimony variant

# Tree statistics over time
ggplot(d[Blind==F], aes(Generation,MeanSize, group=Config, colour=Config)) + stat_summary(fun.y=mean, geom="line") + ggtitle("Size")
ggplot(d[Blind==F], aes(Generation,MeanDepth, group=Config, colour=Config)) + stat_summary(fun.y=mean, geom="line") + ggtitle("Depth")
ggplot(d[Blind==F], aes(Generation,BestSizeSoFar, group=Config, colour=Config)) + stat_summary(fun.y=mean, geom="line") + ggtitle("Size of best so far")
ggplot(d[Blind==F], aes(Generation,BestSizeGen, group=Config, colour=Config)) + stat_summary(fun.y=mean, geom="line") + ggtitle("Size of the best in generation")

# frequency of usage
freq <- beststats[Method != "NEAT-Subset" | Param=="25", .N, by=.(Bin_0,Bin_1,Config,Repo,Job,Repetition)]
freq.stats <- freq[, .(Count=sum(N)), by=.(Bin_0,Bin_1,Config)]
freq.stats[, Frequency := Count * 100 / sum(Count), by=.(Config)]
ggplot(freq.stats, aes(Bin_0,Bin_1)) + geom_tile(aes(fill=Frequency)) + facet_wrap(~ Config) + theme(legend.position="right") +
  coord_fixed() + scale_fill_distiller(palette="Spectral") + labs(x="X (Back/Front)", y="Y (Left/Right)", fill="Frequency (%)")
ggsave("~/Dropbox/Work/Papers/18-CEC/gp_bests_frequency.pdf", width=7, height=1.85)

# alternative: occurrences
freq.stats <- freq[,.(Counts=sum(N)), by=.(Bin_0,Bin_1,Config,Repo,Job)]
freq.stats <- freq.stats[, .N, by=.(Config,Bin_0,Bin_1)]
ggplot(freq.stats, aes(Bin_0,Bin_1)) + geom_tile(aes(fill=N)) + facet_wrap(~ Config) + theme(legend.position="right") +
  coord_fixed() + scale_fill_distiller(palette="Spectral") + labs(x="X (Back/Front)", y="Y (Left/Right)", fill="Frequency (%)")
ggsave("~/Dropbox/Work/Papers/18-CEC/gp_bests_occurrences.pdf", width=7, height=1.85)

# 3D plots for repertoire usage
x <- seq(from=min(freq$Bin_0), to=max(freq$Bin_0))
y <- seq(from=min(freq$Bin_1), to=max(freq$Bin_1))
g <- expand.grid(Bin_0=x, Bin_1=y)

gm <- as.data.table(merge(g, freq.stats[Config=="NEAT-Coords"], all.x=T))
coords.mat <- matrix(gm$Frequency, nrow=length(x), ncol=length(y), byrow=T)
gm <- as.data.table(merge(g, freq.stats[Config=="NEAT-Subset-25"], all.x=T))
subset.mat <- matrix(gm$Frequency, nrow=length(x), ncol=length(y), byrow=T)
gm <- as.data.table(merge(g, freq.stats[Config=="GP-DT"], all.x=T))
gp.mat <- matrix(gm$Frequency, nrow=length(x), ncol=length(y), byrow=T)

hist3D(x, y, coords.mat, ticktype="detailed", main="NEAT-Coords", bty="b2", colkey=list(side=1, length=.5), border="black", clim=c(0,60))
hist3D(x, y, subset.mat, ticktype="detailed", main="NEAT-Subset", bty="b2", colkey=list(side=1, length=.5), border="black", clim=c(0,60))
hist3D(x, y, gp.mat, ticktype="detailed", main="GP-DT", bty="b2", colkey=list(side=1, length=.5), border="black", clim=c(0,60))

# most used by NEAT-Vanilla
most.used <- beststats[Config=="NEAT-Coords", .N, by=.(Repo,Primitive, Bin_0, Bin_1)]
ggplot(most.used) + geom_tile(aes(Bin_0, Bin_1,fill=N)) + facet_wrap(~ Repo) + coord_fixed() + scale_fill_distiller(palette="Spectral")
setorder(most.used, Repo, -N)
lapply(split(most.used, by="Repo"), function(dt){paste(sapply(c(3,5,10,25,50,100), function(n){paste(dt$Primitive[1:n],collapse=",")}), collapse=";")})

# random coordinates for blind neat-coords
generateRandomCoords <- function(repfolder, srcdir="/home/jorge/Dropbox/mase/src/mase/app/maze/rep2/", jobs=0:9, k=3) {
  builddir <- gsub("src","build/classes",srcdir)
  for(j in jobs) {
    d <- fread(paste0(repfolder,"/job.",j,".finalrep.stat"))
    rand <- data.table(Index=d$Hash)
    for(n in 1:k) {
      rand[[paste0("V",n)]] <- runif(nrow(rand))
    }
    write.table(rand, file=paste0(srcdir,"job.",j,".finalrep_k",k,".txt"), row.names=F, col.names=F, sep=" ")
    write.table(rand, file=paste0(builddir,"job.",j,".finalrep_k",k,".txt"), row.names=F, col.names=F, sep=" ")
  }
}
generateRandomCoords("rep","/home/jorge/Dropbox/mase/src/mase/app/maze/rep2/", k=2)
generateRandomCoords("rep","/home/jorge/Dropbox/mase/src/mase/app/maze/rep2/", k=3)
generateRandomCoords("rep","/home/jorge/Dropbox/mase/src/mase/app/maze/rep2/", k=5)

# View distance in the genome space to the nearest neighbours in the behaviour space
distanceToNN <- function(rep) {
  # find the value columns that are actually used
  allv <- colnames(rep)[grep("Behav_\\d+",colnames(rep))]
  cols <- allv[!is.na(rep[1,allv,with=F])]
  aux <- function(row) {
    x <- as.numeric(row["Bin_0"])
    y <- as.numeric(row["Bin_1"])
    neighbours <- rep[(Bin_0 != x | Bin_1 != y) & abs(Bin_0-x) <= 1 & abs(Bin_1-y) <= 1]
    v <- as.numeric(row[cols])
    nv <- neighbours[, cols, with=F]
    dists <- apply(nv, 1, euclideanDist, v)
    return(mean(dists))
  }
  return(apply(rep,1,aux))
}
d <- repos[, .(Bin_0,Bin_1,Distance=distanceToNN(.SD)), by=.(Job)]
ggplot(d, aes(Bin_0,Bin_1)) + geom_tile(aes(fill=Distance)) + facet_wrap(~ Job) +
  scale_fill_distiller(type="seq", palette="Spectral", na.value="white") + coord_fixed() +
  scale_x_discrete(expand = c(0, 0)) + scale_y_discrete(expand = c(0, 0))

