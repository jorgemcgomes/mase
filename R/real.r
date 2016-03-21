setwd("~/Dropbox/Work/Papers/GECCO-Real/")

# b1: caught / not caught
# b2: average final distance of predators to prey
# b3: number of steps until prey is caught
# b4: average dispersion
# fitness: EC paper, if caught, 2 - steps/maxSteps, else, (initialDist-finalDist) / env. width
# capture distance = 2m ; env width = 75x75 ; inital dist to prey [20,35] ; steps=750

distsToPrey <- function(x) {
  preyloc <- tail(subset(x,robot=="prey",select=c("x","y")),1)
  dists <- c()
  for(p in c("pred0","pred1","pred2")) {
    predloc <- tail(subset(x,robot==p,select=c("x","y")),1)
    dists <- c(dists, euclideanDist(as.numeric(preyloc),as.numeric(predloc)))
  }
  return(dists)
}

caught <- function(log, captureDist=2) {
  dists <- as.numeric(dlply(log, .(step), function(x){min(distsToPrey(x))}))
  dists <- dists[!is.na(dists)]
  return(min(dists) < captureDist)
}

initialDist <- function(log) {
  sub <- subset(log, step==min(log$step))
  return(mean(distsToPrey(sub)))  
}

finalDist <- function(log) {
  return(mean(distsToPrey(log)))
}

steps <- function(log, captureDist=2) {
  dists <- ddply(log, .(step), function(x){min(distsToPrey(x))})
  indexes <- which(!is.na(dists$V1) & dists$V1 < captureDist)
  if(length(indexes) > 0) {
    return(dists$step[min(indexes)])
  } else {
    return(max(dists$step))
  }
}

cutCaptured <- function(log, captureDist=2) {
  s <- steps(log,captureDist)
  return(subset(log,step <= s))
}

cutEscape <- function(log, envwidth=75) {
  preysub <- subset(log, Type=="prey")
  cutIdx <- which(preysub$x > envwidth/2 | preysub$x < -envwidth / 2 | preysub$y > envwidth/2 | preysub$y < -envwidth / 2)
  if(length(cutIdx) > 0) {
    cutStep <- preysub$step[min(cutIdx)]
    return(subset(log, step <= cutStep))
  } else {
    return(log)
  }
}

averageDispersion <- function(log) {
  disp <- function(x) {
    p0loc <- as.numeric(head(subset(x,robot=="pred0",select=c("x","y")),1))
    p1loc <- as.numeric(head(subset(x,robot=="pred1",select=c("x","y")),1))
    p2loc <- as.numeric(head(subset(x,robot=="pred2",select=c("x","y")),1))
    d <- mean(c(euclideanDist(p0loc,p1loc),euclideanDist(p0loc,p2loc),euclideanDist(p1loc,p2loc)))
    return(d)
  }
  disps <- as.numeric(dlply(log, .(step), disp))
  return(mean(disps,na.rm=T))
}

fitness <- function(behavs, maxtime=996, envwidth=75) {
  for(r in 1:nrow(behavs)) {
    if(behavs$Captured[r]) {
      behavs$Fitness[r] <- 2 - behavs$Time[r] / maxtime
    } else {
      behavs$Fitness[r] <- max(0, (behavs$InitialDist[r] - behavs$PreyDist[r]) / envwidth)
    }
  }
  return(behavs)
}

### DATA LOADING #############

logs <- read.table("predprey_logs2.csv",header=F,sep=" ",col.names=c("Controller","Sample","NRobots","Date","Duration","step","robot","x","y","orientation"))
logs$robot <- factor(logs$robot, labels=c("pred0","pred1","pred2","prey"))
logs$Type <- "pred"
logs$Type[logs$robot=="prey"] <- "prey"
logs$Type <- factor(logs$Type)
logs$Controller <- factor(logs$Controller, labels=c("Fit","NS1","NS2","NS3","NS4"))
logs$Sample <- factor(logs$Sample)

cut <- as.data.table(ddply(logs, .(Controller,Sample), cutCaptured, .progress="text"))
cut <- as.data.table(ddply(cut, .(Controller,Sample), cutEscape, envwidth=80, .progress="text"))
#cut$y <- -cut$y # invert y

### BEHAV CHARACTERISATION ###########

bcaught <- ddply(cut, .(Controller,Sample), caught, .progress="text")
bsteps <- ddply(cut, .(Controller,Sample), function(x){max(x$step)}, .progress="text")
bfinal <- ddply(cut, .(Controller,Sample), finalDist, .progress="text")
bdisp <- ddply(cut, .(Controller,Sample), averageDispersion, .progress="text")
binitial <- ddply(cut, .(Controller,Sample), initialDist, .progress="text")

behavs <- data.frame(Controller=bcaught$Controller, Sample=bcaught$Sample, Captured=as.numeric(bcaught$V1), 
                     PreyDist=bfinal$V1,Time=bsteps$V1, Dispersion=bdisp$V1, InitialDist=binitial$V1)
behavs <- fitness(behavs, max(logs$step))

behavs$Time <- behavs$Time / max(logs$step)
behavs$PreyDist <- behavs$PreyDist / 75
behavs$Dispersion <- behavs$Dispersion / 75
behavs$InitialDist <- NULL

### JOIN WITH SIMULATION RESULTS ################

sim <- read.table("evaluation_sim.csv",sep=" ", header=T)
sim$Controller <- factor(sim$Controller, labels=levels(logs$Controller))
all <- rbind.fill(cbind(behavs,Environment="Real"),cbind(sim, Environment="Simulated"))

save(all, file="all.rdata")

### BEHAVIOR ANALYSIS #################

m <- melt(all)
agg <- aggregate(value ~ Controller + Environment + variable, m, mean)
ggplot(agg, aes(x=Controller,y=value,colour=Environment)) + geom_point(aes(shape=Environment),data=m,size=2) +
  geom_line(aes(group=Environment)) + facet_wrap(~ variable, scales="free_y")

d <- subset(m,variable=="Fitness")
ggplot(d, aes(x=Controller,y=value,colour=Environment)) + 
  geom_violin(data=subset(d,Environment=="Simulated"), adjust=.75) +
  geom_point(aes(shape=Environment),data=subset(d,Environment=="Real"),size=4,shape=8) + ylab("Fitness")
  #geom_line(aes(group=Environment),data=subset(agg,variable=="Fitness"))
ggsave("real_fitness.pdf", width=5, height=4)

d <- subset(m,variable!="Fitness")
ggplot(d, aes(x=Controller,y=value,colour=Environment)) + 
  geom_violin(data=subset(d,Environment=="Simulated"), adjust=.75) +
  geom_point(aes(shape=Environment),data=subset(d,Environment=="Real"),size=4,shape=8) +
  facet_wrap(~ variable, scales="free_y") + ylim(0,NA) + ylab("Behaviour feature value")
ggsave("real_behavs.pdf", width=6, height=6)


### ROBOT TRACES ##############

start <- unique(cut[step==0], by=c("Controller","Sample","robot"),fromLast=F)
final <- as.data.table(ddply(cut, .(Controller,Sample), function(x){subset(x,step==max(step))}))
final <- unique(final, by=c("Controller","Sample","robot"),fromLast=T)

ggplot(cut, aes(x,y,colour=Type)) + geom_path(aes(group=robot)) + facet_grid(Controller ~ Sample, labeller=label_both) + coord_fixed(ratio=1, xlim=c(-40,40),ylim=c(-40,40)) +
  geom_point(data=start,shape=20) + geom_point(data=final,shape=4,size=2) 
ggsave("alltraces.pdf", width=7, height=12)

expsamples <- list(Fit=0, NS1=1, NS2=0, NS3=0, NS4=0)
for(c in names(expsamples)) {
  s <- expsamples[[c]]
  g <- ggplot(cut[Controller==c & Sample==s], aes(x,y,colour=Type)) + geom_path(aes(group=robot)) + geom_point(data=start[Controller==c & Sample==s],shape=20,size=3) + 
    geom_point(data=final[Controller==c & Sample==s],shape=4,size=3) + coord_fixed(ratio=1) + ggtitle(c) + xlab(NULL) + ylab(NULL) + guides(colour=FALSE,shape=FALSE)
  ggsave(paste0("trace_",c,".pdf"), width=4, height=5)
  print(g)
}

### ROBOT VIDEO ####

video <- function(log, outfolder=paste0(log$Controller[1],"_",log$Sample[1])) {
  dir.create(outfolder)
  xrange <- c(min(log$x)-2, max(log$x)+2)
  yrange <- c(min(log$y)-2, max(log$y)+2)
  aux <- function(steplog) {
    step <- steplog$step[1]
    pos <- unique(as.data.table(steplog), by="robot",fromLast=T)
    pos$vx <- pos$x + cos(pos$orientation) * 1.5 
    pos$vy <- pos$y + sin(pos$orientation) * 1.5
    g <- ggplot(pos, aes(x,y,colour=Type)) + geom_point(shape=20,size=3) + geom_segment(aes(xend = vx, yend = vy)) +
      xlab(NULL) + ylab(NULL) + guides(colour=FALSE,shape=FALSE) + xlim(xrange) + ylim(yrange) +
      ggtitle(paste(formatC(step/10, digits=1, format="f", width=4),"s"))
    ggsave(paste0(outfolder,"/",step/2,".png"), plot=g, width=4, height=5)
  }
  dlply(log, .(step), aux, .parallel=T)
  return(NULL)
}

# ffmpeg -r 20 -i %d.png -b:v 10000k fit.mp4
dlply(cut, .(Controller,Sample), video, .progress = "text")
#video(cut[Controller=="Fit" & Sample==0], "testvideo")


### SOM ANALYSIS ##########

load("repred_som.rdata")

data <- loadData(c("predprey/fit/","predprey/nsga/"), names=c("Fit","NS-T"), filename="rebehaviours.stat", fun="loadBehaviours", vars=c("Captured","PreyDist","Time","Dispersion"))

plotSomFrequency(som, mapBehaviours(som,data[Setup=="Fit"]), showMaxFitness=T, maxLimit=0.2, palette="Greys") + ggtitle("Fit")
ggsave("som_fit.pdf", width=4, height=5)
plotSomFrequency(som, mapBehaviours(som,data[Setup=="NS-T"]), showMaxFitness=T, maxLimit=0.2) + ggtitle("NS-T")
ggsave("som_ns.pdf", width=4, height=5)

plotSomBubble(som, mapBehaviours(som,data[Setup=="Fit"]), useSomFitness = T)
plotSomBubble(som, mapBehaviours(som,data[Setup=="NS-T"]), useSomFitness = T)


### BEHAV DISTANCE ANALYSIS #####

centers <- aggregate(cbind(Fitness,Captured,PreyDist,Time,Dispersion) ~ Controller + Environment, subset(all,Environment=="Simulated"), mean)
dists <- all
vars <- c("Captured","PreyDist","Time","Dispersion")
for(i in 1:nrow(all)) {
  dists[i,"Self"] <- euclideanDist(as.numeric(dists[i,vars]), as.numeric(centers[centers$Controller==dists[i,"Controller"],vars]))
  for(c in unique(as.character(all$Controller))) {
    dists[i,c] <- euclideanDist(as.numeric(dists[i,vars]), as.numeric(centers[centers$Controller==c,vars]))
  }
}
aggdists <- summaryBy(Dist ~ Controller + Environment, dists, FUN=list(mean,sd))
aggdists <- aggregate(cbind(Self,Fit,NS1,NS2,NS3,NS4) ~ Controller + Environment, dists, mean)

real <- subset(all,Environment=="Real")
res <- data.frame()
for(c1 in unique(real$Controller)) {
  for(c2 in unique(real$Controller)) {
    d1 <- subset(real, Controller==c1, select=vars)
    d2 <- subset(real, Controller==c2, select=vars)
    res[c1,c2] <- meanDistSets(d1,d2)
  }
}


### SOM PROBABILITY ANALYSIS ####

mapped <- all
mapped$som <- mapScale(som,all)

simulated <- subset(mapped,Environment=="Simulated")
probabilities <- data.frame(Cell=1:36)
for(contr in unique(simulated$Controller)) {
  sub <- subset(simulated, Controller==contr)
  probs <- c()
  for(cell in probabilities$Cell) {
    count <- sum(sub$som == cell)
    probs[cell] <- count / nrow(sub)
  }
  probabilities[[contr]] <- probs
}

cross <- subset(mapped,Environment=="Real")
for(r in 1:nrow(cross)) {
  cross[r,"Prob"] <- probabilities[cross$som[r],as.character(cross$Controller)[r]]
}

agg <- aggregate(cbind(Fitness,Captured,PreyDist,Time,Dispersion) ~ Controller + Environment, all, mean)
agg$som <- mapScale(som,agg)
for(r in 1:nrow(agg)) {
  agg[r,"Prob"] <- probabilities[agg$som[r],as.character(agg$Controller)[r]]
}


### FITNESS ANALYSIS #####

data <- loadData(c("predprey/fit/","predprey/nsga/"), names=c("Fit","NS-T"), filename="refitness.stat", fun="loadFitness")
bestSoFarFitness(data) + ylim(0,1.25)
ggsave("fitness_gens.pdf", width=3, height=3)
ggplot(lastGen(data), aes(x=Setup, y=BestSoFar,fill=Setup)) + geom_boxplot() + geom_point(position=position_jitterdodge(jitter.width=0.3, jitter.height=0), colour="gray",size=1) + ylim(0,1.25)
ggsave("fitness_final.pdf", width=3, height=3)

data <- loadData(c("predprey/fit/","predprey/nsga/","predprey/hom_fit","predprey/hom_nsga"), names=c("Fit","NS-T","Hom-Fit","Hom-NS-T"), filename="refitness.stat", fun="loadFitness")
