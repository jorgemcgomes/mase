options(digits=3, scipen=100)
theme_set(theme_bw())

processBatch <- function(base, filter=NULL, mazes=c("hard","zigzag","subset","multi","star","open"), success=T, generations=T, uniformity=T, highfit=T) {
  allframe <- data.frame()
  tests <- list()
  alldata <- data.frame()
  for(maze in mazes) {
    setwd(file.path(base,maze))
    print(getwd())
    all <- sub("./", "", list.dirs())[-1]
    if(!is.null(filter)) {
      all <- all[grep(filter,all)]
    }
    data <- NULL
    if(uniformity | highfit) {
      data <- do.call(metaLoadData,c(all,list(params=list(jobs=30, load.behavs=T, behavs.sample=0.2, vars.group=c("x","y")))))
    } else {
      data <- do.call(metaLoadData,c(all,list(params=list(jobs=30, load.behavs=F))))
    }
    tests[[maze]] <- list()
    if(success) {
      r <- fitnessLevelReached(data,1)
      allframe <- rbind(allframe, cbind(r$summary, Maze=maze, Method=rownames(r$summary), Analysis="Success"))
      alldata <- rbind(alldata, cbind(Maze=maze, Analysis="Success", Method=rownames(r$data), r$data))
      tests[[maze]][["Success"]] <- r$ttest
    }
    if(generations) {
      r <- fitnessLevels(data,level=1,use.max=1000)
      allframe <- rbind(allframe, cbind(r$summary, Maze=maze, Method=rownames(r$summary), Analysis="Generations"))
      alldata <- rbind(alldata, cbind(Maze=maze, Analysis="Generations", Method=rownames(r$data), r$data))
      tests[[maze]][["Generations"]] <- r$ttest
    }
    if(uniformity) {
      r <- uniformity(exploration.count(data,levels=10), mode="jsd")
      allframe <- rbind(allframe, cbind(r$summary, Maze=maze, Method=rownames(r$summary), Analysis="Uniformity"))
      alldata <- rbind(alldata, cbind(Maze=maze, Analysis="Uniformity", Method=rownames(r$data), r$data))
      tests[[maze]][["Uniformity"]] <- r$ttest
    }
    if(highfit) {
      r <- individuals.count(data, min.fit=1)
      allframe <- rbind(allframe, cbind(r$summary, Maze=maze, Method=rownames(r$summary), Analysis="Highfit"))
      alldata <- rbind(alldata, cbind(Maze=maze, Analysis="Highfit", Method=rownames(r$data), r$data))
      tests[[maze]][["Highfit"]] <- r$ttest
    }
  }
  return (list(frame=allframe,test=tests,data=alldata))
}

aggregate <- function(frame) {
  newframe <- data.frame()
  for(m in unique(frame$Method)) {
    sub <- subset(frame, m==Method)
    row <- sub[1,]
    row["mean"] <- mean(sub$mean)
    row["min"] <- min(sub$min)
    row["max"] <- max(sub$max)
    row["sd"] <- sqrt(sum(sub$sd^2))
    row["se"] <- row["sd"] / sqrt(nrow(sub)*30)
    newframe <- rbind(newframe, row)
  }
  return(newframe)
}

linePlots <- function(frame, xvar, lvar, ylims) {
  pd <- position_dodge()
  for(a in unique(frame$Analysis)) {
    sub <- subset(frame, a==Analysis)
    sub <- aggregate(sub)
    g <- ggplot(sub, aes_string(x=xvar, y="mean", colour=lvar, shape=lvar)) + 
      #geom_errorbar(aes(ymin=mean-se, ymax=mean+se), width=0.5, position=pd) +
      geom_line(position=pd,aes_string(group=lvar)) + ylab(paste("Mean",a)) +
      geom_point(position=pd,size=3)
    if(!is.null(ylims[[a]])) {
      g <- g + scale_y_continuous(breaks=pretty_breaks(n=7), limits=ylims[[a]])
    } else {
      g <- g + scale_y_continuous(breaks=pretty_breaks(n=7))
    }
    print(g)
  }
}

barPlots <- function(frame, sort=T, horizontal=T) {
  for(a in unique(frame$Analysis)) {
    sub <- subset(frame, a==Analysis)
    if(sort) {
      agg <- aggregate(sub)
      lvls <- levels(reorder(agg$Method,agg$mean,order=T))
      sub$Method <- factor(sub$Method, levels=lvls, ordered=T)
    }
    #sub$Maze <- factor(sub$Maze, levels=c("zigzag","star","subset","multi","hard","open"))
    g <- ggplot(sub, aes(x=Method, y=mean, fill=Maze, order=Maze)) +
      geom_bar(stat="identity") + xlab("Treatment") + ylab(a) 
    if(horizontal) {
      g <- g + coord_flip()
    }
    print(g)
  }
}

metaTest <- function(tests, level=0.05, method="uncorrected", filter=NULL) {
  agg <- list()
  for(maze in tests) {
    for(an in names(maze)) {
      f <- maze[[an]][[method]]
      if(is.null(agg[[an]])) {
        agg[[an]] <- f
        for(r in 1:nrow(f)) {
          for(c in 1:ncol(f)) {
            if(r != c) {
              agg[[an]][r,c] <- 0
            }
          }
        }
      }
      for(r in 1:nrow(f)) {
        for(c in 1:r) {
            agg[[an]][r,c] <- agg[[an]][r,c] + ifelse(is.nan(f[r,c]) | f[r,c] > level, 0, 1)
            agg[[an]][c,r] <- agg[[an]][r,c] 
        }
      }
    }
  }
  if(!is.null(filter)) {
    for(i in 1:length(agg)) {
      f <- agg[[i]]
      pass <- grep(filter, colnames(f))
      agg[[i]] <- f[pass,pass]
    }
  }
  return(agg)
}

aggregateTest <- function(data, filter=NULL) {
  if(!is.null(filter)) {
    data <- data[grep(filter, data$Method),]
  }
  
  res <- list()
  for(a in unique(data$Analysis)) {
    setlist <- list()
    for(m in unique(data$Method)) {
      sub <- subset(data, Method==m & Analysis==a)
      sub <- sub[,4:ncol(sub)]
      setlist[[m]] <- as.numeric(as.vector(as.matrix(sub)))
    }
    res[[a]] <- metaAnalysis(setlist)
    res[[a]][["data"]] <- NULL
  }
  return(res)
}

batch <- processBatch("~/exps/maze3/ea/crossover/", filter=NULL, success=T, generations=T, uniformity=T, highfit=T)
batch$frame[grep("neat_fit",batch$frame$Method),"NE"] <- "NEAT-Fit"
batch$frame[grep("neat_ns",batch$frame$Method),"NE"] <- "NEAT-NS"
batch$frame[grep("ga_fit",batch$frame$Method),"NE"] <- "GA-Fit"
batch$frame[grep("ga_ns",batch$frame$Method),"NE"] <- "GA-NS"
batch$frame[grep("c0",batch$frame$Method),"Crossover"] <- "0.0"
batch$frame[grep("c0.1",batch$frame$Method),"Crossover"] <- "0.1"
batch$frame[grep("c0.2",batch$frame$Method),"Crossover"] <- "0.2"
batch$frame[grep("c0.4",batch$frame$Method),"Crossover"] <- "0.4"
batch$frame[grep("c0.8",batch$frame$Method),"Crossover"] <- "0.8"
save(batch, file="~/Dropbox/Papers/GECCO15/data/crossover.rdata")

batch <- processBatch("~/exps/maze3/nspar/", filter=NULL, success=T, generations=T, uniformity=T, highfit=T)
batch$frame[grep("k15",batch$frame$Method),"Knn"] <- "015"
batch$frame[grep("k1_",batch$frame$Method),"Knn"] <- "001"
batch$frame[grep("k50",batch$frame$Method),"Knn"] <- "050"
batch$frame[grep("k199",batch$frame$Method),"Knn"] <- "199"
batch$frame[grep("k5_",batch$frame$Method),"Knn"] <- "005"
batch$frame[grep("k100",batch$frame$Method),"Knn"] <- "100"
batch$frame[grep("anone",batch$frame$Method),"Archive"] <- "none"
batch$frame[grep("anovel",batch$frame$Method),"Archive"] <- "novel"
batch$frame[grep("arandom",batch$frame$Method),"Archive"] <- "random"
save(batch, file="~/Dropbox/Papers/GECCO15/data/nspar.rdata")

batch <- processBatch("~/exps/maze3/comb/", filter=NULL, success=T, generations=T, uniformity=T, highfit=T)
save(batch, file="~/Dropbox/Papers/GECCO15/data/comb.rdata")

batch <- processBatch("~/exps/maze3/ea/mutation/", filter=NULL, success=T, generations=T, uniformity=T, highfit=T)
batch$frame[grep("ga_ns",batch$frame$Method),"NE"] <- "GA-NS"
batch$frame[grep("ga_fit",batch$frame$Method),"NE"] <- "GA-Fit"
batch$frame[grep("neat_ns",batch$frame$Method),"NE"] <- "NEAT-NS"
batch$frame[grep("neat_fit",batch$frame$Method),"NE"] <- "NEAT-Fit"
batch$frame[grep("m0.025",batch$frame$Method),"Mutation"] <- "0.025"
batch$frame[grep("m0.05",batch$frame$Method),"Mutation"] <- "0.05"
batch$frame[grep("m0.1",batch$frame$Method),"Mutation"] <- "0.1"
batch$frame[grep("m0.25",batch$frame$Method),"Mutation"] <- "0.25"
batch$frame[grep("m0.4",batch$frame$Method),"Mutation"] <- "0.4"
batch$frame[grep("m0.6",batch$frame$Method),"Mutation"] <- "0.6"
batch$frame[grep("m0.8",batch$frame$Method),"Mutation"] <- "0.8"
save(batch, file="~/Dropbox/Papers/GECCO15/data/mutation.rdata")

agg <- aggregate(subset(batch$frame, Analysis=="Uniformity"))
cor(subset(agg, NE=="GA-Fit")$mean, subset(agg, NE=="GA-NS")$mean, method="pearson")
cor(subset(agg, NE=="NEAT-Fit")$mean, subset(agg, NE=="NEAT-NS")$mean, method="pearson")

cor(subset(agg, NE=="GA-NS")$mean, c(0.025, 0.05, 0.1, 0.25, 0.4, 0.6, 0.8) , method="pearson")
cor(subset(agg, NE=="NEAT-NS")$mean, c(0.025, 0.05, 0.1, 0.25, 0.4, 0.6, 0.8), method="pearson")


cor(subset(agg, NE=="GA-NS")$mean, subset(agg, NE=="NEAT-NS")$mean, method="pearson")
cor(subset(agg, NE=="GA-Fit")$mean, subset(agg, NE=="NEAT-Fit")$mean, method="pearson")

batch <- processBatch("~/exps/maze3/ea/elite/", filter=NULL, success=T, generations=T, uniformity=T, highfit=T)
batch$frame[grep("ga_ns",batch$frame$Method),"NE"] <- "GA-NS"
batch$frame[grep("ga_fit",batch$frame$Method),"NE"] <- "GA-Fit"
batch$frame[grep("neat_ns",batch$frame$Method),"NE"] <- "NEAT-NS"
batch$frame[grep("neat_fit",batch$frame$Method),"NE"] <- "NEAT-Fit"
batch$frame[grep("e0",batch$frame$Method),"Elitism"] <- "0"
batch$frame[grep("e5",batch$frame$Method),"Elitism"] <- "1"
batch$frame[grep("e20",batch$frame$Method),"Elitism"] <- "2"
batch$frame[grep("e50",batch$frame$Method),"Elitism"] <- "3"
batch$frame[grep("e75",batch$frame$Method),"Elitism"] <- "4"
batch$frame[grep("e100",batch$frame$Method),"Elitism"] <- "5"
batch$frame[grep("s1",batch$frame$Method),"Elitism"] <- "0"
batch$frame[grep("s0.75",batch$frame$Method),"Elitism"] <- "1"
batch$frame[grep("s0.5",batch$frame$Method),"Elitism"] <- "2"
batch$frame[grep("s0.2",batch$frame$Method),"Elitism"] <- "3"
batch$frame[grep("s0.1",batch$frame$Method),"Elitism"] <- "4"
batch$frame[grep("s0.05",batch$frame$Method),"Elitism"] <- "5"
save(batch, file="~/Dropbox/Papers/GECCO15/data/elite.rdata")

batch <- processBatch("~/exps/maze3/growth/", filter=NULL, success=T, generations=T, uniformity=T, highfit=T)
batch$frame[grep("anovel",batch$frame$Method),"Archive"] <- "novel"
batch$frame[grep("arandom",batch$frame$Method),"Archive"] <- "random"
batch$frame[grep("g0.01",batch$frame$Method),"Growth"] <- "0.01"
batch$frame[grep("g0.03",batch$frame$Method),"Growth"] <- "0.03"
batch$frame[grep("g0.07",batch$frame$Method),"Growth"] <- "0.07"
save(batch, file="~/Dropbox/Papers/GECCO15/data/growth.rdata")

batch <- processBatch("~/exps/maze3/comb2/", filter=NULL, success=T, generations=T, uniformity=T, highfit=T)
save(batch, file="~/Dropbox/Papers/GECCO15/data/comb2.rdata")

batch <- processBatch("~/exps/maze3/ea/mutation2/", filter=NULL, success=T, generations=T, uniformity=T, highfit=T)
batch$frame[grep("ga_ns",batch$frame$Method),"NE"] <- "GA-NS"
batch$frame[grep("ga_fit",batch$frame$Method),"NE"] <- "GA-Fit"
batch$frame[grep("neat_ns",batch$frame$Method),"NE"] <- "NEAT-NS"
batch$frame[grep("neat_fit",batch$frame$Method),"NE"] <- "NEAT-Fit"
batch$frame[grep("m0.025",batch$frame$Method),"Mutation"] <- "0.025"
batch$frame[grep("m0.05",batch$frame$Method),"Mutation"] <- "0.05"
batch$frame[grep("m0.1",batch$frame$Method),"Mutation"] <- "0.1"
batch$frame[grep("m0.25",batch$frame$Method),"Mutation"] <- "0.25"
batch$frame[grep("m0.4",batch$frame$Method),"Mutation"] <- "0.4"
batch$frame[grep("m0.6",batch$frame$Method),"Mutation"] <- "0.6"
batch$frame[grep("m0.8",batch$frame$Method),"Mutation"] <- "0.8"
save(batch, file="~/Dropbox/Papers/GECCO15/data/mutation2.rdata")

batch <- processBatch("~/exps/maze3/growth/", filter=NULL, success=T, generations=T, uniformity=T, highfit=T)
batch$frame[grep("anovel",batch$frame$Method),"Archive"] <- "novel"
batch$frame[grep("arandom",batch$frame$Method),"Archive"] <- "random"
batch$frame[grep("g0.01",batch$frame$Method),"Growth"] <- "0.01"
batch$frame[grep("g0.03",batch$frame$Method),"Growth"] <- "0.03"
batch$frame[grep("g0.05",batch$frame$Method),"Growth"] <- "0.05"
batch$frame[grep("g0.07",batch$frame$Method),"Growth"] <- "0.07"
batch$frame[grep("k5",batch$frame$Method),"Knn"] <- "5"
batch$frame[grep("k15",batch$frame$Method),"Knn"] <- "15"
batch$frame[grep("k100",batch$frame$Method),"Knn"] <- "100"
batch$frame[grep("k5_anovel",batch$frame$Method),"Treatment"] <- "N-k5"
batch$frame[grep("k15_anovel",batch$frame$Method),"Treatment"] <- "N-k15"
batch$frame[grep("k100_anovel",batch$frame$Method),"Treatment"] <- "N-k100"
batch$frame[grep("k5_arandom",batch$frame$Method),"Treatment"] <- "R-k5"
batch$frame[grep("k15_arandom",batch$frame$Method),"Treatment"] <- "R-k15"
batch$frame[grep("k100_arandom",batch$frame$Method),"Treatment"] <- "R-k100"

linePlots(subset(batch$frame), xvar="Growth", lvar="Treatment")
aggregateTest(batch$data, filter="k5_anovel")
aggregateTest(batch$data, filter="k15_anovel")
aggregateTest(batch$data, filter="k100_anovel")
aggregateTest(batch$data, filter="k5_arandom")
aggregateTest(batch$data, filter="k15_arandom")
aggregateTest(batch$data, filter="k100_arandom")


# figure 3
load("~/Dropbox/Work/Papers/GECCO15/data/nspar.rdata")
linePlots(subset(batch$frame, Method != "fit"), xvar="Knn", lvar="Archive", ylims=list(Success=c(0.3,0.75),Uniformity=c(0.35,0.65)))

# figure 4
load("~/Dropbox/Work/Papers/GECCO15/data/growth.rdata")
linePlots(subset(batch$frame, Method != "fit"), xvar="Growth", lvar="Treatment", ylims=list(Success=c(0.3,0.75),Uniformity=c(0.35,0.65)))
