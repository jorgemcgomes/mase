setwd("~/exps/mazefinal")
fixPostFitness("~/exps/mazefinal")

data <- loadData("**", "postfitness.stat" ,fun=loadFitness, auto.ids.names=c("Method","Repo","Param"))
data[Method=="rbcselected", Param := factor(Param, labels=c("3","5","10","15","30"))]
data[, Param := factor(formatC(as.numeric(as.character(Param)), width=2, flag="0"))]
data[, Blind := grepl("blind", Method)]
data[, Method := sub("blind", "", Method)]
data[, Method := factor(Method, levels=c("tr","rbcneat","rbcsub","rbcselected","gptreeslopr"), labels=c("TR","NEAT-Coords","NEAT-Subset","NEAT-Selected","GP-DT"))]
data[, Config := paste(Method,Blind,Param,sep="-")]
data[, Config := sub("-NA","",sub("TRUE","B",sub("-FALSE","",Config)))]
#gsub("NA, ","",dff$string)


fit_scale <- scale_y_continuous(limits=c(0,1.25),breaks=seq(0,1.25,0.25))

# All comparison
d <- data[Blind==F & (Method!="NEAT-Subset" | Param=="15") & Method != "NEAT-Selected"]
fitnessBoxplots(d, xvar="Config") + ylab("Best fitness") + fit_scale
bestSoFarFitnessEvals(d, xvar="Config") + fit_scale

# Rate of fitness increase
lags <- rbind(d[Method=="GP-DT", .(Evaluations=tail(Evaluations,n=-5), D=diff(BestSoFar,lag=5)), by=.(Method,Repo,Job)],
              d[Method!="GP-DT", .(Evaluations=tail(Evaluations,n=-100), D=diff(BestSoFar,lag=100)), by=.(Method,Repo,Job)])
lags <- lags[, .(MeanD=mean(D)), by=.(Method,Evaluations)]
ggplot(lags, aes(Evaluations,MeanD,group=Method)) + geom_line(aes(colour=Method)) + 
  ylab("Average fitness increase in the last 20k evaluations (sqrt)") + 
  scale_y_sqrt(breaks=seq(0,1,0.1), minor_breaks=seq(0,1,0.025), limits=c(0,NA)) 


# NEAT-Subset number of primitives
d <- data[Blind==F & Method=="NEAT-Subset"]
fitnessBoxplots(d, xvar="Param") + labs(x="Number of primitives") + guides(fill=F) + ylab("Best fitness") + 
  ggtitle("Performance of NEAT-Sub for different number of primitives allowed") + fit_scale
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
d <- data[(Method=="NEAT-Coords" & (Param=="03" | Param=="NA")) | (Method=="NEAT-Subset" & Param=="15") | Method %in% c("GP-DT","TR")]
ggplot(lastGen(d), aes(Method,BestSoFar,fill=Blind)) + geom_boxplot() + 
  geom_point(position=position_jitterdodge(jitter.width=0.15, jitter.height=0), size=.5, colour="gray") +
  ylab("Best fitness") + ggtitle("Performance of the methods when using or not the BC") + fit_scale

m <- metaAnalysis(lastGen(subset[Method!="TR"]), BestSoFar ~ Blind, ~ Method) # performance degradation
sapply(m, function(x){as.numeric(x$ttest$holm[1,2])})
metaAnalysis(lastGen(subset[Blind==T | Method=="TR"]), BestSoFar ~ Method) # best blind

# Blind Vanilla
fitnessBoxplots(data[Blind==T & Method=="NEAT-Vanilla"], xvar="Param") + labs(x="Number of dimensions") + guides(fill=F) + ylab("Best fitness") + 
  ggtitle("Performance of NEAT-Vanilla according to the dimensionality of the random BC") + fit_scale
metaAnalysis(lastGen(data)[Blind==T & Method=="NEAT-Vanilla"], BestSoFar ~ Param)

# Blind subset
fitnessBoxplots(data[Blind==T & Method=="NEAT-Sub"], xvar="Param") + labs(x="Number of primitives") + guides(fill=F) + ylab("Best fitness") + 
  ggtitle("Performance of NEAT-Sub (BLIND) for different number of primitives allowed")
metaAnalysis(lastGen(data)[Blind==T & Method=="NEAT-Sub"], BestSoFar ~ Param)


# Repo variability
d <- data[Blind==FALSE & Method != "TR" & (Method!="NEAT-Sub" | Param=="15")]
fitnessBoxplots(d, xvar="Repo") + facet_wrap(~ Config) + guides(fill=F) + fit_scale

# for each method, does the repertoire have a significant impact?
m <- metaAnalysis(lastGen(d),  BestSoFar ~ Repo, ~ Config)
sapply(m, function(x){x$ttest$kruskal$p.value})

# does any repertoire yield consistently superior results accross all methods?
means <- lastGen(d)[, .(Mean=mean(BestSoFar)), by=.(Repo,Config)]
friedman.test(Mean ~ Repo | Config, data=means)


# best solutions from each method
d <- lastGen(data)[Repo=="0" & Blind==F, .(Setup,Config,Job,BestSoFar)] # use only repo 0 and not blind
setorder(d, Config, -BestSoFar)
View(d)

# GP analysis
koza <- loadData("**", "koza.stat" ,fun=loadFile, auto.ids.sep="_", auto.ids.names=c("Method","Repo","Param"))
koza[, Blind := grepl("blind", Method)]
koza[, Method := sub("blind", "", Method)]
koza[, Method := factor(Method, levels=c("gptreeslo","gpgraphlo","gptreeslopr","gpgraphlopr"), labels=c("GP-DT","GP-DG","GP-DT(P)","GP-DG(P)"))]
koza[, Config := paste(Method,Blind,sep="-")]
koza[, Config := sub("TRUE","B",sub("-FALSE","",Config))]

ggplot(koza[Blind==F], aes(Generation,MeanSize, group=Config, colour=Config)) + stat_summary(fun.y=mean, geom="line") + ggtitle("Size")
ggplot(koza[Blind==F], aes(Generation,BestSizeGen, group=Config, colour=Config)) + stat_summary(fun.y=mean, geom="line")
ggplot(koza[Blind==F], aes(Generation,MeanDepth, group=Config, colour=Config)) + stat_summary(fun.y=mean, geom="line") + ggtitle("Depth")

m <- melt(koza[Blind==F, c("Generation","Config", colnames(koza)[grep("f\\..*", colnames(koza))]), with=F], id.vars=c("Generation","Config") )
#ggplot(m, aes(Generation,value, colour=variable)) + stat_summary(fun.y=mean, geom="line") + facet_wrap(~ Config)
ggplot(m, aes(variable,value)) + stat_summary(fun.y=mean, geom="bar") + ylab("Mean number of nodes") + xlab("Node type") + 
  facet_wrap(~ Config, scales="free_x", ncol=1) + ggtitle("Node types")


# search for the smallest best
k <- lastGen(koza[Blind==F & Repo=="0", .(Setup,Config,Job,BestSizeSoFar,BestFitnessSoFar)])
setorder(k, Config, BestSizeSoFar)
View(k)


# the bests of each
repo <- loadData("rep", "finalrep.stat")
beststats <- loadData("rbcneat_*", "postbest.xml.stat", auto.ids.names=c("Method","Repo"))
beststats <- merge(beststats[, .(Repo,Job,Repetition,Time,Primitive)], repo[,.(Job,Hash,Bin_0,Bin_1)], by.x=c("Primitive","Repo"),by.y=c("Hash","Job"))
most.used <- beststats[, .N, by=.(Repo,Primitive,Bin_0,Bin_1)]
setorder(most.used, Repo, -N)
lapply(split(most.used, by="Repo"), function(dt){print(paste(dt$Primitive[1:3],collapse=","),paste(dt$Primitive[1:5],collapse=","),
                                                       paste(dt$Primitive[1:10],collapse=","),paste(dt$Primitive[1:15], collapse=","),
                                                     paste(dt$Primitive[1:30],collapse=","), sep=";")})



beststats <- loadData("**", "postbest.xml.stat" ,fun=loadFile, auto.ids.sep="_", auto.ids.names=c("Method","Repo","Param"))
beststats[, Param := factor(formatC(as.numeric(as.character(Param)), width=2, flag="0"))]
beststats[, Blind := grepl("blind", Method)]
beststats[, Method := sub("blind", "", Method)]
beststats[, Method := factor(Method, levels=c("tr","rbcneat","rbcsub","gptreeslopr"), labels=c("TR","NEAT-Vanilla","NEAT-Sub","GP-DT"))]
beststats[, Config := paste(Method,Blind,Param,sep="-")]
beststats[, Config := sub("-NA","",sub("TRUE","B",sub("-FALSE","",Config)))]

beststats <- merge(beststats, repo[,.(Hash,Bin_0,Bin_1)], by.x="Primitive",by.y="Hash")
setorder(beststats, Config, Job, Seed, Repetition, Time)
set(beststats, j=c(grep("ArbitratorOut_|SensorInput_|ActuatorOut_",colnames(beststats))), value=NULL )

act.stats <- beststats[, .(Number=length(unique(Primitive)), Duration=mean(rle(Primitive)$lengths)), by=.(Config,Job,Repetition)]
act.sum <- act.stats[, .(Number = mean(Number), Duration=mean(Duration)), by=.(Config,Job)][, .(NumberMean=mean(Number),NumberSE=se(Number),DurationMean=mean(Duration),DurationSE=se(Duration)), by=.(Config)]

ggplot(beststats[Blind==FALSE & (Method!="NEAT-Sub" | Param=="15"),.N,.(Bin_0,Bin_1,Config)]) + geom_tile(aes(Bin_0, Bin_1,fill=N)) + facet_wrap(~ Config) + coord_fixed()
ggplot(beststats[Blind==FALSE & (Method!="NEAT-Sub" | Param=="15"),.N,.(Bin_0,Bin_1,Config,Job)]) + geom_tile(aes(Bin_0, Bin_1,fill=N)) + facet_grid(Config ~ Job) + coord_fixed()

# most used by NEAT-Vanilla
most.used <- beststats[Config=="NEAT-Vanilla", .N, by=.(Primitive,Bin_0,Bin_1)]
setorder(most.used, -N)
View(most.used)
paste(most.used$N[1:30],collapse=",")
