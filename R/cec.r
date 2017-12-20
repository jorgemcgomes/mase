setwd("~/exps/mazefinal")
fixPostFitness("~/exps/mazefinal")


# Repertoire analysis
repos <- loadData("rep", "finalrep.stat")
ggplot(repos, aes(Behav_0,Behav_1,colour=Fitness)) + geom_point(size=.5) + facet_wrap(~ Job) + coord_fixed() +
  geom_spoke(aes(angle=CircularFitnessLog.2_0), radius=1)

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


data <- loadData("**", "postfitness.stat" ,fun=loadFitness, auto.ids.names=c("Method","Repo","Param"))
data[Method=="rbcselected", Param := factor(Param, labels=c("3","5","10","25","50","100"))]
data[, Param := factor(Param, ordered=T)]
data[, Blind := grepl("blind", Method)]
data[, Method := sub("blind", "", Method)]
data[, Method := factor(Method, levels=c("tr","rbcneat","rbcsub","rbcselected","gptrees","gptreespars"), labels=c("TR","NEAT-Coords","NEAT-Subset","NEAT-Selected","GP-DT","GP-DT(P)"))]
data[, Config := paste(Method,Blind,Param,sep="-")]
data[, Config := sub("-NA","",sub("TRUE","B",sub("-FALSE","",Config)))]
#gsub("NA, ","",dff$string)

fit_scale <- scale_y_continuous(limits=c(0,1),breaks=seq(0,1.25,0.25))

# All comparison
d <- data[Blind==F & (Method!="NEAT-Subset" | Param=="25") & Method != "NEAT-Selected"]
fitnessBoxplots(d, xvar="Config") + ylab("Best fitness") + fit_scale
bestSoFarFitnessEvals(d, xvar="Config") + fit_scale

# Rate of fitness increase
lags <- rbind(d[grepl("GP",Method), .(Evaluations=tail(Evaluations,n=-5), D=diff(BestSoFar,lag=5)), by=.(Method,Repo,Job)],
              d[!grepl("GP",Method), .(Evaluations=tail(Evaluations,n=-100), D=diff(BestSoFar,lag=100)), by=.(Method,Repo,Job)])
lags <- lags[, .(MeanD=mean(D)), by=.(Method,Evaluations)]
ggplot(lags, aes(Evaluations,MeanD,group=Method)) + geom_line(aes(colour=Method)) + 
  ylab("Average fitness increase in the last 20k evaluations (sqrt)") + 
  scale_y_sqrt(breaks=seq(0,1,0.1), minor_breaks=seq(0,1,0.025), limits=c(0,NA)) 

# NEAT-Subset number of primitives
d <- data[Blind==F & Method=="NEAT-Subset"]
fitnessBoxplots(d, xvar="Param") + labs(x="Number of primitives") + guides(fill=F) + ylab("Best fitness") + 
  ggtitle("Performance of NEAT-Sub for different number of primitives allowed") + fit_scale
metaAnalysis(lastGen(d), BestSoFar ~ Param)
bestSoFarFitnessEvals(d, xvar="Param") +
  ggtitle("Performance of NEAT-Sub for different number of primitives allowed") + fit_scale

# NEAT-Subset vs NEAT-Selected
d <- data[Blind==F & Method %in% c("NEAT-Subset","NEAT-Selected")]
ggplot(lastGen(d), aes(Param,BestSoFar,fill=Method)) + geom_boxplot() + 
  geom_point(position=position_jitterdodge(jitter.width=0.15, jitter.height=0), size=.5, colour="gray") + xlab("Number of primitives") +
  ylab("Best fitness") + ggtitle("Evolved (NEAT-Subset) vs previously selected (NEAT-Selected) set of primitives") + fit_scale
metaAnalysis(lastGen(d), BestSoFar ~ Method, ~ Param)
ggplot(d[, .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(Method,Param,Generation)], aes(Generation,Mean,group=Method)) + 
  geom_line(aes(colour=Method)) + ylab("Fitness") + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE, fill=Method), alpha = 0.1) + facet_wrap(~ Param)

# Blind comparison
d <- data[(Method=="NEAT-Coords" & (Param=="3" | is.na(Param))) | (Method=="NEAT-Subset" & Param=="25") | Method %in% c("GP-DT","TR","GP-DT(P)")]
ggplot(lastGen(d), aes(Method,BestSoFar,fill=Blind)) + geom_boxplot() + 
  geom_point(position=position_jitterdodge(jitter.width=0.15, jitter.height=0), size=.5, colour="gray") +
  ylab("Best fitness") + ggtitle("Performance of the methods when using or not the BC") + fit_scale

m <- metaAnalysis(lastGen(d[Method!="TR"]), BestSoFar ~ Blind, ~ Method) # performance degradation
sapply(m, function(x){as.numeric(x$ttest$holm[1,2])})
metaAnalysis(lastGen(d[Blind==T | Method=="TR"]), BestSoFar ~ Method) # best blind

# Blind Vanilla
fitnessBoxplots(data[Blind==T & Method=="NEAT-Coords"], xvar="Param") + labs(x="Number of dimensions") + guides(fill=F) + ylab("Best fitness") + 
  ggtitle("Performance of NEAT-Vanilla according to the dimensionality of the random BC") + fit_scale
metaAnalysis(lastGen(data)[Blind==T & Method=="NEAT-Vanilla"], BestSoFar ~ Param)

# Blind subset
fitnessBoxplots(data[Blind==T & Method=="NEAT-Sub"], xvar="Param") + labs(x="Number of primitives") + guides(fill=F) + ylab("Best fitness") + 
  ggtitle("Performance of NEAT-Sub (BLIND) for different number of primitives allowed")
metaAnalysis(lastGen(data)[Blind==T & Method=="NEAT-Sub"], BestSoFar ~ Param)


# Repo variability
d <- data[Blind==FALSE & Method != "TR"]
fitnessBoxplots(d, xvar="Repo") + facet_wrap(~ Config) + guides(fill=F) + fit_scale

# for each method, does the repertoire have a significant impact?
m <- metaAnalysis(lastGen(d),  BestSoFar ~ Repo, ~ Config)
sapply(m, function(x){x$ttest$kruskal$p.value})

# does any repertoire yield consistently superior results accross all methods?
means <- lastGen(d)[, .(Mean=mean(BestSoFar)), by=.(Repo,Config)]
metaAnalysis(means, Mean ~ Repo, paired=T)

# best solutions from each method
d <- lastGen(data)[Blind==F, .(Setup,Config,Repo,Job,BestSoFar)] # use only repo 0 and not blind
setorder(d, Config, -BestSoFar)
View(d)

# GP analysis
koza <- loadData("**", "koza.stat" ,fun=loadFile, auto.ids.sep="_", auto.ids.names=c("Method","Repo","Param"))
koza[, Blind := grepl("blind", Method)]
koza[, Method := sub("blind", "", Method)]
koza[, Method := factor(Method, levels=c("gptrees","gptreespars"), labels=c("GP-DT","GP-DT(P)"))]
koza[, Config := paste(Method,Blind,sep="-")]
koza[, Config := sub("TRUE","B",sub("-FALSE","",Config))]

ggplot(koza[Blind==F], aes(Generation,MeanSize, group=Config, colour=Config)) + stat_summary(fun.y=mean, geom="line") + ggtitle("Size")
ggplot(koza[Blind==F], aes(Generation,MeanDepth, group=Config, colour=Config)) + stat_summary(fun.y=mean, geom="line") + ggtitle("Depth")
ggplot(koza[Blind==F], aes(Generation,BestSizeGen, group=Config, colour=Config)) + stat_summary(fun.y=mean, geom="line")

m <- melt(koza[Blind==F, c("Generation","Config", colnames(koza)[grep("f\\..*", colnames(koza))]), with=F], id.vars=c("Generation","Config") )
#ggplot(m, aes(Generation,value, colour=variable)) + stat_summary(fun.y=mean, geom="line") + facet_wrap(~ Config)
ggplot(m, aes(variable,value)) + stat_summary(fun.y=mean, geom="bar") + ylab("Mean number of nodes") + xlab("Node type") + 
  facet_wrap(~ Config, scales="free_x", ncol=1) + ggtitle("Node types")

# search for the smallest best
m <- merge(koza[Blind==F, .(Config,Repo,Job,Generation,BestSizeGen)], data[, .(Config,Repo,Job,Generation,MaxFitness)], by=c("Config","Repo","Job","Generation"))
# all best-of-gens
ggplot(m, aes(BestSizeGen,MaxFitness,colour=Config)) + geom_point(shape=4)
# all best-of-runs
bestof <- m[, .SD[which.max(MaxFitness)] , by=.(Config,Repo,Job)]
ggplot(bestof, aes(BestSizeGen,MaxFitness,colour=Config)) + geom_point() + geom_text(aes(label=paste0(Repo,"/",Job)),vjust="bottom")

setorder(k, Config, BestSizeSoFar)
View(k)
# correlation of fitness and size
lapply(split(k,by="Config"), function(x){cor(x$BestSizeSoFar,x$BestSoFar)})




# the bests of each
repo <- loadData("rep", "finalrep.stat")
beststats <- loadData("**", "postbest.xml.stat" ,fun=loadFile, auto.ids.sep="_", auto.ids.names=c("Method","Repo","Param"), exclude.cols=T, colnames=c("Seed","Repetition","Time","Primitive"))
beststats[Method=="rbcselected", Param := factor(Param, labels=c("3","5","10","25","50","100"))]
beststats[, Param := factor(Param, ordered=T)]
beststats[, Blind := grepl("blind", Method)]
beststats[, Method := sub("blind", "", Method)]
beststats[, Method := factor(Method, levels=c("tr","rbcneat","rbcsub","rbcselected","gptrees","gptreespars"), labels=c("TR","NEAT-Coords","NEAT-Subset","NEAT-Selected","GP-DT","GP-DT(P)"))]
beststats[, Config := paste(Method,Blind,Param,sep="-")]
beststats[, Config := sub("-NA","",sub("TRUE","B",sub("-FALSE","",Config)))]

beststats <- merge(beststats, repo[,.(Job,Hash,Bin_0,Bin_1)], by.x=c("Primitive","Repo"),by.y=c("Hash","Job"))
setorder(beststats, Config, Job, Repetition, Time)

act.stats <- beststats[, .(Number=length(unique(Primitive)), Duration=mean(rle(Primitive)$lengths)), by=.(Config,Job,Repo,Repetition)]
act.sum <- act.stats[, .(Number = mean(Number), Duration=mean(Duration)), by=.(Config,Job,Repo)][, .(NumberMean=mean(Number),NumberSE=se(Number),DurationMean=mean(Duration),DurationSE=se(Duration)), by=.(Config)]

ggplot(beststats[Blind==FALSE, .N,.(Bin_0,Bin_1,Config)]) + geom_tile(aes(Bin_0, Bin_1,fill=N)) + facet_wrap(~ Config) + coord_fixed() + scale_fill_distiller(palette="Spectral")
ggplot(beststats[Blind==FALSE, .N,.(Bin_0,Bin_1,Config,Job)]) + geom_tile(aes(Bin_0, Bin_1,fill=N)) + facet_grid(Config ~ Job) + coord_fixed() + scale_fill_distiller(palette="spectral")




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
