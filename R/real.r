library(plyr)
library(ggplot2)
library(reshape)
library(kohonen)

setwd("~/Dropbox/Work/Papers/GECCO-Real/")
euclideanDist <- function(x1, x2) {sqrt(sum((x1 - x2) ^ 2))} 
theme_set(theme_bw())

# b1: caught / not caught
# b2: average final distance of predators to prey
# b3: number of steps until prey is caught
# b4: average dispersion
# fitness: EC paper, if caught, 2 - steps/maxSteps, else, (initialDist-finalDist) / env. width
# capture distance = 2m ; env width = 75x75 ; inital dist to prey [20,35] ; steps=750

distsToPrey <- function(x) {
  preyloc <- head(subset(x,robot=="prey",select=c("x","y")),1)
  dists <- c()
  for(p in c("pred0","pred1","pred2")) {
    predloc <- head(subset(x,robot==p,select=c("x","y")),1)
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
  sub <- subset(log, step==max(log$step))
  return(mean(distsToPrey(sub)))
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

logs <- read.table("predprey_logs.csv",header=T,sep=" ")
logs$robot <- factor(logs$robot, labels=c("pred0","pred1","pred2","prey"))
logs$robottype <- "pred"
logs$robottype[logs$robot=="prey"] <- "prey"
logs$robottype <- factor(logs$robottype)
#logs <- subset(logs, step <=750)
cut <- ddply(logs, .(experiment,repetition), cutCaptured, .progress="text")

### BEHAV CHARACTERISATION ###########

bcaught <- ddply(cut, .(experiment,repetition), caught, .progress="text")
bsteps <- ddply(cut, .(experiment,repetition), function(x){max(x$step)}, .progress="text")
bfinal <- ddply(cut, .(experiment,repetition), finalDist, .progress="text")
bdisp <- ddply(cut, .(experiment,repetition), averageDispersion, .progress="text")
binitial <- ddply(cut, .(experiment,repetition), initialDist, .progress="text")

behavs <- data.frame(Controller=bcaught$experiment, Sample=bcaught$repetition, Captured=as.numeric(bcaught$V1), 
                     Time=bsteps$V1, PreyDist=bfinal$V1, Dispersion=bdisp$V1, InitialDist=binitial$V1)
behavs <- fitness(behavs, max(logs$step))

behavs$Time <- behavs$Time / max(logs$step)
behavs$PreyDist <- behavs$PreyDist / 75
behavs$Dispersion <- behavs$Dispersion / 75

### JOIN WITH SIMULATION RESULTS ################

sim <- read.table("simulated_behaviours.csv",sep=",", header=T)
sim$Sample <- 0

all <- rbind(cbind(subset(behavs,select=c("Controller","Sample","Fitness","Captured","PreyDist","Time","Dispersion")),Environment="Real"),
             cbind(sim, Environment="Simulated"))
#all$controller <- factor(all$controller,levels=c("0","1","2","4","3"),labels=c("Fit","NS1","NS2","NS3","NS4"))
all$Controller <- factor(all$Controller, labels=c("Fit","NS1","NS2","NS3","NS4"))
all$Sample <- factor(all$Sample)


### BEHAVIOR ANALYSIS #################

m <- melt(all)
agg <- aggregate(value ~ controller + environment + variable, m, mean)
ggplot(agg, aes(x=controller,y=value,colour=environment)) + geom_point(aes(shape=environment),data=m,size=2) +
  geom_line(aes(group=environment)) + facet_wrap(~ variable, scales="free_y")


### ROBOT TRACES ##############

ggplot(cut, aes(x,y,colour=robottype)) + geom_path(aes(group=robot)) + facet_grid(repetition ~ experiment) + coord_fixed(xlim=c(-25,30),ylim=c(-45,25)) +
  geom_point(data=subset(cut,step==0),shape=20) + geom_point(data=ddply(cut, .(experiment,repetition), function(x){subset(x,step==max(step))}),shape=4,size=2) 


### SOM ANALYSIS ##########

mapScale <- function(som, data) {
  data <- as.matrix(subset(data,select=colnames(som$codes)))
  if(!is.null(som$scaled.center)) {
    data <- scale(data, center=som$scaled.center, scale=som$scaled.scale)
  }
  return(map(som, data)$unit.classif)
}

agg <- aggregate(cbind(Fitness,Captured,PreyDist,Time,Dispersion) ~ Controller + Environment, all, mean)
load("repred_som.rdata")
mapScale(som$group,agg)
