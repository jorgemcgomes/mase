setwd("~/Dropbox/Work/Papers/PPSN16/")

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

cutEscape <- function(log, envwidth) {
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

fitness <- function(behavs, maxtime, envwidth) {
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

logs <- read.table("real_predprey_logs.csv",header=T,sep=" ")
logs$robot <- factor(logs$robot, labels=c("pred0","pred1","pred2","prey"))
logs$Type <- "pred"
logs$Type[logs$robot=="prey"] <- "prey"
logs$Type <- factor(logs$Type)
logs$Controller <- factor(logs$Controller, levels=c(0,1,3,2,4), labels=c("Fit1","NS1","NS2","NS3","NS4"))
logs$Sample <- factor(logs$Sample)

write.table(logs,"real_logs.csv",row.names=F,col.names=T)

cut <- as.data.table(ddply(logs, .(Controller,Sample), cutCaptured, .progress="text"))
cut <- as.data.table(ddply(cut, .(Controller,Sample), cutEscape, envwidth=100, .progress="text"))
#cut$y <- -cut$y # invert y

### BEHAV CHARACTERISATION ###########

bcaught <- ddply(cut, .(Controller,Sample), caught, .progress="text")
bsteps <- ddply(cut, .(Controller,Sample), function(x){max(x$step)}, .progress="text")
bfinal <- ddply(cut, .(Controller,Sample), finalDist, .progress="text")
bdisp <- ddply(cut, .(Controller,Sample), averageDispersion, .progress="text")
binitial <- ddply(cut, .(Controller,Sample), initialDist, .progress="text")

behavs <- data.frame(Controller=bcaught$Controller, Sample=bcaught$Sample, Captured=as.numeric(bcaught$V1), 
                     PreyDist=bfinal$V1,Time=bsteps$V1, Dispersion=bdisp$V1, InitialDist=binitial$V1)
behavs <- fitness(behavs, max(logs$step), 100)

behavs$Time <- behavs$Time / max(logs$step)
behavs$PreyDist <- behavs$PreyDist / 100
behavs$Dispersion <- behavs$Dispersion / 100
behavs$InitialDist <- NULL

### JOIN WITH SIMULATION RESULTS ################

sim <- read.table("evaluation_sim_large.csv",sep=",", header=T) # same size and time, no prey randomness
sim$Controller <- factor(sim$Controller, levels=c(0,1,3,2,4), labels=levels(logs$Controller))
all <- rbind.fill(cbind(behavs,Environment="Real"),cbind(sim, Environment="Simulated"))

save(all, file="all.rdata")

### BEHAVIOR ANALYSIS #################

d <- melt(all)
d$variable <- factor(d$variable, levels=c("Fitness","Captured","Dispersion","Time","PreyDist"), labels=c("Fitness score","Behaviour: Prey captured","Behaviour: Predator dispersion","Behaviour: Trial length","Behaviour: Distance to prey"))
ggplot(d, aes(x=Controller,y=value,colour=Environment)) + 
  geom_violin(data=subset(d,Environment=="Simulated")) +
  geom_point(aes(shape=Environment),data=subset(d,Environment=="Real"),size=2,shape=8) +
  facet_wrap(~ variable, scales="free_y") + ylim(0,NA) + ylab("Score")
ggsave("real_features.pdf", width=8, height=6)

agg <- summaryBy(value ~ Controller + Environment + variable, melt(all), FUN=list(mean,sd))
agg <- subset(agg, variable!="Fitness")
ggplot(subset(agg,Environment=="Simulated"), aes(variable,value.mean)) + geom_line(aes(colour=Controller,group=Controller))
ggplot(subset(agg,Environment=="Real"), aes(variable,value.mean)) + geom_line(aes(colour=Controller,group=Controller))

# d <- subset(melt(all),variable=="Fitness")
# ggplot(d, aes(x=Controller,y=value,colour=Environment)) + 
#   geom_violin(data=subset(d,Environment=="Simulated"), adjust=.8) +
#   geom_point(aes(shape=Environment),data=subset(d,Environment=="Real"),size=2,shape=8) + ylab("Fitness")
# #geom_line(aes(group=Environment),data=subset(agg,variable=="Fitness"))
# ggsave("real_fitness.pdf", width=5, height=3)
# 
# d <- subset(melt(all),variable!="Fitness")
# ggplot(d, aes(x=Controller,y=value,colour=Environment)) + 
#   geom_violin(data=subset(d,Environment=="Simulated")) +
#   geom_point(aes(shape=Environment),data=subset(d,Environment=="Real"),size=2,shape=8) +
#   facet_wrap(~ variable, scales="free_y") + ylim(0,NA) + ylab("Behaviour feature value")
# ggsave("real_behavs.pdf", width=6, height=5)


### ROBOT TRACES ##############

start <- unique(cut, by=c("Controller","Sample","robot"),fromLast=F)
final <- unique(cut, by=c("Controller","Sample","robot"),fromLast=T)
traces <- ddply(cut, .(Controller,Sample), function(x){x$step <- x$step/max(x$step) ; return(x)})

ggplot(traces, aes(x,y,colour=Type)) + geom_path(aes(group=robot,alpha=step)) + facet_grid(Controller ~ Sample, labeller=label_both) + coord_fixed(ratio=1, xlim=c(-50,50),ylim=c(-50,50)) +
  geom_point(data=start,shape=15) + geom_point(data=final,shape=20,size=2)  + guides(colour=FALSE,shape=FALSE, alpha=FALSE)
ggsave("traces_all_trials.pdf", width=12, height=20)

sub <- cut[Controller=="Fit1" & Sample==2 | Controller=="NS1" & Sample==1 | Controller=="NS3" & Sample==0 | Controller=="NS2" & Sample==1 | Controller=="NS4" & Sample==0]
start <- unique(sub, by=c("Controller","Sample","robot"),fromLast=F)
final <- unique(sub, by=c("Controller","Sample","robot"),fromLast=T)
sub <- ddply(sub, .(Controller,Sample), function(x){x$step <- x$step/max(x$step) ; return(x)})
ggplot(sub, aes(x,y,colour=Type)) + geom_path(aes(group=robot,alpha=step)) + coord_fixed(ratio=1, xlim=c(-27,26),ylim=c(-46,12)) +
  geom_point(data=start,shape=15) + geom_point(data=final,shape=20)  + guides(colour=FALSE,shape=FALSE, alpha=FALSE) +
  facet_wrap(~ Controller) + xlab(NULL) + ylab(NULL)
ggsave("selectedtraces.pdf", width=6, height=4.8)

#expsamples <- list(Fit=2, NS1=1, NS2=0, NS3=1, NS4=0)
#for(c in names(expsamples)) {
#  s <- expsamples[[c]]
#  g <- ggplot(cut[Controller==c & Sample==s], aes(x,y,colour=Type)) + geom_path(aes(group=robot,alpha=step)) + geom_point(data=start[Controller==c & Sample==s],shape=20,size=3) + 
#    geom_point(data=final[Controller==c & Sample==s],shape=4,size=3) + 
#    coord_fixed(ratio=1) + ggtitle(c) + xlab(NULL) + ylab(NULL) + guides(colour=FALSE,shape=FALSE, alpha=FALSE)
#  ggsave(paste0("trace_",c,".pdf"), width=4, height=5)
#}


### ROBOT VIDEO ####

video <- function(log, outfolder=paste0(log$Controller[1],"_",log$Sample[1]), finalframes=60) {
  dir.create(outfolder)
  xrange <- c(min(log$x)-2, max(log$x)+2)
  yrange <- c(min(log$y)-2, max(log$y)+2)
  steps <- c(unique(log$step), rep(max(log$step),finalframes))
  aux <- function(snr) {
    s <- steps[snr]
    steplog <- subset(log, step <= s)
    pos <- unique(as.data.table(steplog), by="robot",fromLast=T)
    pos$c1x <- pos$x + cos(pos$orientation) * 0.355 
    pos$c1y <- pos$y + sin(pos$orientation) * 0.355
    pos$c2x <- pos$x + cos(pos$orientation + 2.44) * 0.309 
    pos$c2y <- pos$y + sin(pos$orientation + 2.44) * 0.309    
    pos$c3x <- pos$x + cos(pos$orientation - 2.44) * 0.309 
    pos$c3y <- pos$y + sin(pos$orientation - 2.44) * 0.309   
    g <- ggplot(pos, aes(x, y, colour=Type)) + 
      geom_segment(aes(x=c1x, y=c1y, xend = c2x, yend = c2y)) +
      geom_segment(aes(x=c1x, y=c1y, xend = c3x, yend = c3y)) +
      geom_segment(aes(x=c2x, y=c2y, xend = c3x, yend = c3y)) +
      xlab(NULL) + ylab(NULL) + guides(colour=FALSE,shape=FALSE) +
      coord_fixed(ratio=1, xlim=xrange, ylim=yrange) +
      geom_path(aes(group=robot,alpha=step),data=steplog) +
      ggtitle(paste(formatC(s/10, digits=1, format="f", width=4),"s")) +
      scale_alpha(range = c(0, 0.5)) + guides(alpha=FALSE,colour=FALSE)
    ggsave(paste0(outfolder,"/",snr,".png"), plot=g, width=4, height=4)
    return(NULL)
  }
  llply(1:length(steps), aux, .parallel=T)
  return(NULL)
}

# ffmpeg -r 20 -i %d.png -y -b:v 10000k fit.mp4
dlply(cut, .(Controller,Sample), video, .progress = "text")
#video(cut[Controller=="Fit" & Sample==0], "testvideo")

### SOM ANALYSIS ##########

load("repred_som.rdata")

data <- loadData(c("predprey/fit/","predprey/nsga/"), names=c("Fit","NS-T"), filename="rebehaviours.stat", fun="loadBehaviours", vars=c("Captured","PreyDist","Time","Dispersion"))

plotSomFrequency(som, mapBehaviours(som,data[Setup=="Fit"]), showMaxFitness=F, maxLimit=0.2, palette="Greys") + ggtitle("Fit")
ggsave("som_fit.pdf", width=3, height=4)
plotSomFrequency(som, mapBehaviours(som,data[Setup=="NS-T"]), showMaxFitness=F, maxLimit=0.2) + ggtitle("NS-T")
ggsave("som_ns.pdf", width=3, height=4)

plotSomBubble(som, mapBehaviours(som,data[Setup=="Fit"]), useSomFitness = T)
plotSomBubble(som, mapBehaviours(som,data[Setup=="NS-T"]), useSomFitness = T)

#theme(axis.line=element_blank(),axis.text.x=element_blank(),axis.text.y=element_blank(),axis.ticks=element_blank())

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

summaryBy(BestSoFar ~ Setup, lastGen(data), FUN=list(mean,sd))
