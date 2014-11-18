fitnessLevels <- function(data, levels, use.evals=T) {
  frame <- data.frame()
  for(lvl in levels) {
    dataset <- list()
    for(exp in names(data)) {
      metric <- c()
      for(job in data[[exp]]$jobs) {
        l <- fitnessLevelAux(data[[exp]][[job]][["fitness"]], lvl, use.evals)
        metric <- c(metric, l)
      }
      dataset[[exp]] <- metric[which(!is.na(metric))]
      frame[as.character(lvl),paste0(exp,".hits")] <- length(which(!is.na(metric)))
      frame[as.character(lvl),paste0(exp,".mean")] <- mean(metric, na.rm=TRUE)
    }
    cat("LEVEL ", lvl, "\n")
    print(metaAnalysis(dataset))
  }
  #frame <- as.data.frame(frame)
  return(frame)
}

fitnessLevelAux <- function(fitness, level, use.evals) {
  gen <- NA
  for(r in 1:nrow(fitness)) {
    if(fitness[r,"best.sofar"] >= level) {
      if(use.evals) {
        gen <- fitness[r,"gen"]
      } else {
        gen <- fitness[r,"old.gen"]
      }      
      break
    }
  }
  return(gen)
}

heterogeneity <- function(folder, n=0) {
  files <- list.files(folder, pattern="hetero.stat", full.names=T)
  metric <- c()
  p20 <- c()
  p30 <- c()
  for(f in files) {
    t <- read.table(f, sep=" ", header=F, col.names=c("gen","fitness","pops","p20","p30"))
    if(n > 0) {
      t <- t[order(t$fitness, decreasing=T)[1:n],]
    }
    metric <- c(metric, mean(t[,"pops"]))
    p20 <- c(p20, mean(t[,"p20"]))
    p30 <- c(p30, mean(t[,"p30"]))
  }
  cat(folder," Npops:\n")
  print(summary(metric))
  cat("Participant <20:\n")
  print(summary(p20))
  cat("Participant <30:\n")
  print(summary(p30))
}


fitnessRelation <- function(folder) {
  fit.files <- list.files(folder, pattern="[.]fitness[.]stat", full.names=T)
  hyb.files <- list.files(folder, pattern="[.]hybrid[.]stat", full.names=T)
  res <- list()
  for(i in 1:length(fit.files)) {
    fit.frame <- read.table(fit.files[[i]], sep=" ",header=F, col.names=c("gen","evals","mean","max","bestfar",NA))
    hyb.frame <- read.table(hyb.files[[i]], sep=" ",header=F, col.names=c("gen","npops","minsize","meansize","maxsize","foreigns","selfinds","forinds","allinds","meanage","maxage","meandisp","meandistother","merges","splits","remerges","totalmerges","totalsplits","totalremerges"))
    for(g in 2:nrow(fit.frame)) {
      fit.diff <- (fit.frame[g,"max"] - fit.frame[g-1,"max"]) / abs(fit.frame[g-1,"max"])
      if(hyb.frame[g-1,"merges"] > 0) {
        res$merges <- c(res$merges, fit.diff)        
      } else if(hyb.frame[g-1,"splits"] > 0) {
        res$splits <- c(res$splits, fit.diff)
      } else if(hyb.frame[g-1,"merges"] == 0 & hyb.frame[g-1,"splits"] == 0) {
        res$others <- c(res$others, fit.diff)
      }
    }
  }
  return(res)
}