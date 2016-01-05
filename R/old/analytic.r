#### General purpose ###########################################################

metaAnalysis <- function(setlist, ...) {
    res <- data.frame(mean=NULL, sd=NULL, min=NULL, max=NULL)
    for(s in names(setlist)) {
        res[s,"n"] <- length(setlist[[s]])
        res[s,"mean"] <- mean(setlist[[s]])
        res[s,"sd"] <- sd(setlist[[s]])
        res[s,"se"] <- res[s,"sd"] / sqrt(length(setlist[[s]]))
        res[s,"min"] <- min(setlist[[s]])
        res[s,"max"] <- max(setlist[[s]])
    }
    tt <- batch.ttest(setlist, ...)
    
    maxlen <- max(as.numeric(lapply(setlist, length)))
    norm <- lapply(setlist, function(v){c(v,rep(NA,maxlen-length(v)))})
    dd <- t(as.data.frame(norm))
    return(list(summary=res, ttest=tt,data=dd))
}


#### Generational t-tests ######################################################

generational.ttest <- function(datalist, snapshots, ...) {
    result <- list()
    names <- c()
    for(data in datalist) {
        names <- c(names, data$expname)
    }
    for(s in snapshots) {
        setlist <- list()
        for(data in datalist) {
            set <- c()
            for(job in data$jobs) {
                set <- c(set, data[[job]]$fitness$best.sofar[[s]])
            }
            setlist[[data$expname]] <- set
        }
        result[[paste("Gen",s)]] <- batch.ttest(setlist, ...)
    }
    return(result)
}

batch.ttest <- function(setlist, ...) {
    for(n in names(setlist)) {
      if(length(setlist[[n]]) < 3) {
        setlist[[n]] <- NULL
      } else {
        setlist[[n]] <- as.numeric(setlist[[n]])
      }
    }
    if(length(setlist) < 2) {
      return(NULL)
    } 
    matrix <- matrix(data = NA, nrow = length(setlist), ncol=length(setlist))
    rownames(matrix) <- names(setlist)
    colnames(matrix) <- names(setlist)
    
    adj.matrix <- matrix
    tests <- c()
    for(i in 1:length(setlist)) {
        for(j in 1:length(setlist)) {
            if(i > j) {
                pvalue <- wilcox.test(as.numeric(setlist[[i]]), as.numeric(setlist[[j]]), ...)$p.value
                matrix[i,j] <- pvalue
                tests <- c(tests, pvalue)
            }
        }
    }
    
    adjusted <- p.adjust(tests, method="holm")
    index <- 1
    for(i in 1:length(setlist)) {
      for(j in 1:length(setlist)) {
        if(i > j) {
          adj.matrix[i,j] <- adjusted[index]
          index <- index + 1
        }
      }
    }
    
    kruskal <- kruskal.test(setlist)
    
    result <- NULL
    result$kruskal <- kruskal
    result$uncorrected <- matrix
    result$holm <- adj.matrix
    return(result)
}

#### Fitness analysis ##################################################

fitnessSummary <- function(datalist, snapshots=NULL) {
  frame <- data.frame()
  for(data in datalist) {
    if(is.null(snapshots)) {
      snapshots <- c(length(data$gens))
    }
    for(s in snapshots) {
      fits <- c()
      for(job in data$jobs) {
        fits <- c(fits, data[[job]]$fitness$best.sofar[s])
      }
      index <- nrow(frame) + 1
      frame[index, "method"] <- data$expname
      frame[index, "gen"] <- s
      frame[index, "n"] <- length(fits)
      frame[index, "mean"] <- mean(fits)
      frame[index, "sd"] <- sd(fits)
      frame[index, "se"] <- sd(fits) / sqrt(length(fits))
    }
  }
  return(frame)
}

se <- function(x, na.rm=F){
  if(na.rm) {
    x <- x[!is.na(x)]
  }
  sqrt(var(x)/length(x))
}

fitnessStatistics <- function(datalist) {
  resultFrame <- data.frame()
  for(data in datalist) {
    frame <- data.frame(gen=data$gens)
    genframe <- data.frame(gen=data$gens)
    for(j in data$jobs) {
      frame <- cbind(frame,data[[j]]$fitness$best.sofar)
      genframe <- cbind(frame,data[[j]]$fitness$best.gen)
    }
    frame <- frame[,-1] # delete gens column
    genframe <- genframe[,-1]
    resultFrame <- rbind(resultFrame, data.frame(
      Exp=data$expname,
      Generation=data$gens,
      Bestfar.Mean=apply(frame,1,mean),
      Bestfar.SD=apply(frame,1,sd),
      Bestfar.SE=apply(frame,1,se),
      Bestgen.Mean=apply(genframe,1,mean),
      Bestgen.SD=apply(genframe,1,sd),
      Bestgen.SE=apply(genframe,1,se)
    ))
  }
  return(resultFrame)
}

fitnessBests <- function(datalist, ttests=F, ...) {
  resultList <- list()
  for(data in datalist) {
    for(j in data$jobs) {
      f <- tail(data[[j]]$fitness$best.sofar,1)
      resultList[[length(resultList)+1]] <- c(data$expname,j,f)
    }
  }
  df <- do.call(rbind.data.frame, resultList)
  colnames(df) <- c("Exp","Job","Fitness")
  df$Fitness <- as.numeric(as.character(df$Fitness))

  if(ttests) {
    setlist <- list()
    for(e in unique(df$Exp)) {
      setlist[[e]] <- subset(df, Exp==e)$Fitness
    }
    m <- metaAnalysis(setlist, ...)
    print(m$summary)
    print(m$ttest)
  }  
  return(df)  
}

fitnessLevels <- function(data, ...) {
  setlist <- list()
  for(exp in names(data)) {
    metric <- c()
    for(job in data[[exp]]$jobs) {
      l <- fitnessLevelAux(data[[exp]][[job]]$fitness, ...)
      metric <- c(metric, l)
    }
    setlist[[exp]] <- metric[which(!is.na(metric))]
  }
  return(metaAnalysis(setlist))
}

fitnessLevelAux <- function(fitness, level, use.max=NULL) {
  for(r in 1:nrow(fitness)) {
    if(fitness[r,"best.sofar"] >= level) {
      return(fitness[r,"gen"])
    }
    if(r == nrow(fitness)) {
      if(is.null(use.max)) {
        return(NA)
      } else {
        return(use.max)
      }
    }
  }
}

fitnessLevelReached <- function(datalist, level) {
  setlist <- list()
  for(data in datalist) {      
    fits <- c()
    for(job in data$jobs) {
      fitness.list <- data[[job]]$fitness$best.sofar
      f <- fitness.list[length(fitness.list)]
      if(f >= level) {
        fits <- c(fits,1)
      } else {
        fits <- c(fits,0)
      }
    }
    setlist[[data$expname]] <- fits
  }
  return(metaAnalysis(setlist))
}


#### Behaviour analysis #################################################################

analyseVars <- function(datalist, vars) {
  result <- data.frame()
  for(data in datalist) {
    if(length(data$jobs) > 0) {
      df <- data.frame()
      for(job in data$jobs) {
        for(sub in data$subpops) {
          df <- rbind(df, subset(data[[job]][[sub]], select=c("gen",vars)))
        }
      }
      m <- aggregate(. ~ gen, data=df, mean)
      result <- rbind(result, cbind(Exp=data$expname,m))      
    }
  }
  return(result)
}


#### Behaviour diversity based on mean distance #########################################

euclideanDist <- function(x1, x2) {sqrt(sum((x1 - x2) ^ 2))} 

diversity.group <- function(datalist, min.number=10) {
  setlist <- list()
  cl <- makeCluster(detectCores(logical=T))
  clusterEvalQ(cl, library(pdist))
  for(data in datalist) {
    print(data$expname)
    for(job in data$jobs) {
      print(job)
      frame <- data.frame()
      for(sub in data$subpops) {
        frame <- rbind(frame, subset(data[[job]][[sub]], select=data$vars.group))
      }
      if(nrow(frame) > min.number) {
        v <- meanDists(frame, cl)
        setlist[[data$expname]] <- c(setlist[[data$expname]], v)
      }
    }
  }
  stopCluster(cl)
  return(metaAnalysis(setlist))
}

diversity.group.gens <- function(datalist, ...) {
  cl <- makeCluster(detectCores(logical=T))
  clusterEvalQ(cl, library(pdist))
  all.results <- data.frame()
  for(data in datalist) {
    if(length(data$jobs) > 0) {
      print(data$expname)
      res <- diversity.group.gens.aux(data, cl, ...)
      all.results <- rbind(all.results, cbind(Exp=data$expname,res))
    }
  }
  stopCluster(cl)
  return(all.results)
}

diversity.group.gens.aux <- function(data, cl, interval, accum=T) {
  steps <- seq(from=interval-1,to=tail(data$gens,1), by=interval)
  result <- data.frame()
  for(job in data$jobs) {
    print(job)
    for(s in 1:length(steps)) {
      frame <- data.frame()
      for(sub in data$subpops) {
        frame <- rbind(frame, subset(data[[job]][[sub]], 
                                     gen >= ifelse(accum | s == 1, 0, steps[s - 1]) & gen <= steps[s], 
                                     select=data$vars.group))
      }
      v <- meanDists(frame, cl)
      result[s,job] <- v
    }
  }
  sumRes <- data.frame(Step=steps)
  sumRes[["Mean"]] <- rowMeans(result)
  sumRes[["SD"]] <- apply(result, 1, sd)
  sumRes[["SE"]] <- sumRes[["SD"]] / sqrt(ncol(result))
  return(sumRes)
}

diversity.ind <- function(datalist) {
  pb <- txtProgressBar(min=1, max=length(datalist) * length(datalist[[1]]$jobs) * length(datalist[[1]]$subpops) , style=3)
  index <- 1
  setlist <- list()
  cl <- makeCluster(8)
  clusterEvalQ(cl, library(pdist))
  for(data in datalist) {
    for(job in data$jobs) {
      for(sub in data$subpops) {
        frame <- subset(data[[job]][[sub]], select=data$vars.ind)
        v <- meanDists(frame, cl)
        setlist[[data$expname]] <- c(setlist[[data$expname]], v)
        index <- index + 1
        setTxtProgressBar(pb, index)
      }
    }
  }
  stopCluster(cl)
  close(pb)
  return(metaAnalysis(setlist))  
}



### OTHER BEHAVIOURAL ANALYSIS #############################################################

# threshold = distance / (diagonal / 2) , diagonal/2 = 141.42/2 = 70.71
countNear <- function(folder, subpops, threshold, mode="best") {
  files <- list.files(folder,pattern="rebehaviours.stat", full.names=T)
  jobs <- c()
  for(file in files) {
    if(mode=="best") {
      jobs <- c(jobs, countNearBest(file,subpops,threshold))
    } else if(mode=="mean") {
      jobs <- c(jobs, countNearMean(file,subpops,threshold))
    }
  }
  return(jobs)
}

countNearMean <- function(file, subpops, threshold) {
  tab <- read.table(file, header=F, sep=" ",fill=T)
  count <- c()
  for(r in 1:nrow(tab)) {
    near <- 0
    for(i in 0:(subpops-1)) {
      if(tab[r,11+i*4] < threshold) {
        near <- near + 1
      } 
    }
    count <- c(count, near)
  }
  return(mean(count))
}

countNearBest <- function(file, subpops, threshold) {
  tab <- read.table(file, header=F, sep=" ",fill=T)
  best <- which.max(tab[,4])
  near <- 0
  for(i in 0:(subpops-1)) {
    if(tab[best,11+i*4] < threshold) {
      near <- near + 1
    } 
  }
  return(near)
}

metaGroupDiversity <- function(datalist) {
    setlist <- list()
    for(data in datalist) {
        div <- groupDiversity(data)
        means <- c()
        for(j in data$jobs) {
            means <- c(means, mean(div[[j]]))
        }
        setlist[[data$expname]] <- means
    }
    return(metaAnalysis(setlist))
}

groupDiversity.all <- function(data, min.fit=0) {
  result <- data.frame(gen=data$gens)
  pb <- txtProgressBar(min=1, max=length(data$jobs), style=3)
  pbindex <- 1
  
  chis <- c()
  
  for(j in data$jobs) {
    all <- data.frame()
    for(s in data$subpops) {
      all <- rbind(all, data[[j]][[s]])
    }
    all <- subset(all, fitness >= min.fit, select=data$vars.group)
    if(nrow(all) > 0) {
      c <- cov(all)
      tr <- 0
      for(a in 1:nrow(c)) {
        tr <- tr + c[a,a]
      }
      chis <- c(chis, tr)
      
#       centre <- colMeans(all)
#       dists <- apply(all, 1, euclideanDist, centre)
#       chis <- c(chis, sqrt(mean(dists)))
    }
    setTxtProgressBar(pb,pbindex)
    pbindex <- pbindex + 1
  }
  print(mean(chis))
  print(sd(chis))
}

groupDiversity <- function(data) {
    result <- data.frame(gen=data$gens)
    pb <- txtProgressBar(min=1, max=length(data$jobs)*length(data$gens), style=3)
    pbindex <- 1
    
    for(j in data$jobs) {
        div <- c()
        for(g in data$gens) {
            setTxtProgressBar(pb,pbindex)
            pbindex <- pbindex + 1
            
            all <- NULL
            for(s in data$subpops) {
                sub <- subset(data[[j]][[s]], gen==g, select=data$vars.group)
                if(is.null(all)) {
                    all <- sub
                } else {
                    all <- rbind(all, sub)
                }
            }
            centre <- colMeans(all)
            dists <- apply(all, 1, euclideanDist, centre)
            div <- c(div, mean(dists))
        }
        result[[j]] <- div
    }
    result[["mean"]] <- rowMeans(result[,-1])
    return(result)
}

groupDiversity.accum <- function(data, interval=10) {
  result <- data.frame(gen=data$gens[data$gens %% interval == 0])
  pb <- txtProgressBar(min=1, max=length(data$jobs)*length(result$gen), style=3)
  pbindex <- 1
  
  for(j in data$jobs) {
    div <- c()
    for(g in result$gen) {
      setTxtProgressBar(pb,pbindex)
      pbindex <- pbindex + 1
      
      all <- NULL
      for(s in data$subpops) {
        sub <- subset(data[[j]][[s]], gen <= g, select=data$vars.group)
        all <- rbind(all, sub)
      }
      
      count <- exploration.count.aux(all, c(0,0,0,0), c(1,1,1,1), 5)
      div <- c(div, ineq(count,type="Gini"))
      #div <- c(div, sum(count > 0))
      #centre <- colMeans(all)
      #dists <- apply(all, 1, euclideanDist, centre)
      #div <- c(div, mean(dists))
    }
    result[[j]] <- div
  }
  result[["mean"]] <- rowMeans(result[,-1])
  return(result)
}

indDiversity.accum <- function(data, interval=10) {
  result <- data.frame(gen=data$gens[data$gens %% interval == 0])
  pb <- txtProgressBar(min=1, max=length(data$jobs)*length(result$gen), style=3)
  pbindex <- 1
  
  for(j in data$jobs) {
    div <- c()
    for(g in result$gen) {
      setTxtProgressBar(pb,pbindex)
      pbindex <- pbindex + 1
      
      exp <- c()
      for(s in data$subpops) {
        sub <- subset(data[[j]][[s]], gen <= g, select=data$vars.ind)
        count <- exploration.count.aux(sub, c(0,0,0), c(1,1,1), 5)
        exp <- c(exp, sum(count > 0))
      }
      div <- c(div, mean(exp))
    }
    result[[j]] <- div
  }
  result[["mean"]] <- rowMeans(result[,-1])
  return(result)
}

intraPopDiversity <- function(data, vars=data$vars.group) {
    result <- list()
    pb <- txtProgressBar(min=1, max=length(data$jobs)*length(data$subpops), style=3)
    pbindex <- 1
    
    for(j in data$jobs) {
        result[[j]] <- data.frame(gen=data$gens)
        result[[j]]$all.avg <- rep(0, length(data$gens))
        result[[j]]$all.sd <- rep(0, length(data$gens))
        for(s in data$subpops) {
            setTxtProgressBar(pb,pbindex)
            pbindex <- pbindex + 1
            t.mean <- c()
            t.sd <- c()
            for(g in data$gens) {
                sub <- subset(data[[j]][[s]], gen==g, select=vars)
                centre <- colMeans(sub)
                dists <- apply(sub, 1, euclideanDist, centre)
                t.mean <- c(t.mean, mean(dists))
                t.sd <- c(t.sd, sd(dists))
            }
            result[[j]][[paste0(s,".avg")]] <- t.mean
            result[[j]][[paste0(s,".sd")]] <- t.sd
            
            result[[j]]$all.avg <- result[[j]]$all.avg + t.mean / data$nsubs
            result[[j]]$all.sd <- result[[j]]$all.sd + t.sd / data$nsubs
        }
    }
    return(result)
}

interPopDiversity <- function(data, vars=data$vars.ind) {
    result <- list()
    pb <- txtProgressBar(min=1, max=length(data$jobs)*length(data$subpops)*length(data$gens), style=3)
    pbindex <- 1
    
    for(j in data$jobs) {
        result[[j]] <- list()
        result[[j]]$gen <- data$gens
        result[[j]]$all.avg <- c()
        result[[j]]$all.sd <- c()
        
        for(g in data$gens) {
            subdatas <- list()
            subcentres <- list()
            for(s in data$subpops) {
                subdatas[[s]] <- subset(data[[j]][[s]], gen==g, select=vars)
                subcentres[[s]] <- colMeans(subdatas[[s]])
            }
            t.mean <- c()
            t.sd <- c()
            for(s in data$subpops) {
                setTxtProgressBar(pb,pbindex)
                pbindex <- pbindex + 1
                dists <- c()
                for(s2 in data$subpops[!data$subpops == s]) {
                    dists <- c(dists, apply(subdatas[[s]], 1, euclideanDist, subcentres[[s2]]))
                }
                t.mean <- c(t.mean, mean(dists))
                t.sd <- c(t.sd, sd(dists))
            }
            result[[j]]$all.avg <- c(result[[j]]$all.avg, mean(t.mean))
            result[[j]]$all.sd <- c(result[[j]]$all.sd, mean(t.sd))
        }
        result[[j]] <- as.data.frame(result[[j]])
    }
    return(result)    
}



#### Behaviour exploration uniformity ##########################################

individuals.quantile <- function(datalist, q) {
  fitnesses <- c()
  for(data in datalist) {
    for(j in data$jobs) {
      for(s in data$subpops) {
        fitnesses <- c(fitnesses, data[[j]][[s]][["fitness"]])
      }
    }
  }
  res <- quantile(fitnesses, q)
  print(res)
  return(res)
}

individuals.count <- function(datalist, min.fit=-Inf, max.fit=Inf) {
  setlist <- list()
  for(data in datalist) {
    chis <- c()
    for(j in data$jobs) {
      all <- 0
      hit <- 0
      for(s in data$subpops) {
        subset <- subset(data[[j]][[s]], fitness >= min.fit & fitness <= max.fit)
        all <- all + nrow(data[[j]][[s]])
        hit <- hit + nrow(subset)
      }
      chis <- c(chis, hit / all)
    }
    setlist[[data$expname]] <- chis
  }
  a <- metaAnalysis(setlist)
  return(a)
}

exploration.count <- function(datalist, levels=5, vars=datalist[[1]]$vars.group, min.fit=-Inf, max.fit=Inf, by.gen=NULL, accum=T, minv=NULL, maxv=NULL) {     
  pb <- txtProgressBar(min=1, max=length(datalist)*length(datalist[[1]]$subpops)*length(datalist[[1]]$jobs), style=3)
  pbindex <- 1
  
  # subdivide the space in same-size patches
  if(is.null(minv) || is.null(maxv)) {
    maxv <- list()
    minv <- list()
    for(data in datalist) {
      for(j in data$jobs) {
        for(s in data$subpops) {
          for(v in vars) {
            maxv[[v]] <- max(maxv[[v]], data[[j]][[s]][[v]])
            minv[[v]] <- min(minv[[v]], data[[j]][[s]][[v]])
          }
        }
      }
    }
  }
  
  cat("\n") ; print(minv) ; print(maxv)
  
  # count the patches visited in each method, each evolutionary run, each subpop
  counts <- list()
  totalCount <- 0
  size <- 0
  
  for(data in datalist) { # experiment
    exp <- data$expname
    counts[[exp]] <- list()
    for(j in data$jobs) { # jobs
      counts[[exp]][[j]] <- list()
      for(s in data$subpops) { # subpops
        if(is.null(by.gen)) {
          counts[[exp]][[j]][[s]] <- exploration.count.aux(subset(data[[j]][[s]], select=vars), minv, maxv, levels)
          totalCount <- totalCount + counts[[exp]][[j]][[s]]
        } else {
          counts[[exp]][[j]][[s]] <- list()
          g <- 0
          while(g < max(data$gens)) {
            if(accum) {
              c <- exploration.count.aux(subset(data[[j]][[s]], gen < g + by.gen, select=vars), minv, maxv, levels)
            } else {
              c <- exploration.count.aux(subset(data[[j]][[s]], gen >= g & gen < g + by.gen, select=vars), minv, maxv, levels)
            }
            counts[[exp]][[j]][[s]][[as.character(g)]] <- c
            totalCount <- totalCount + c
            g <- g + by.gen
          }
        }        
        setTxtProgressBar(pb,pbindex)
        pbindex <- pbindex + 1
      }
    }
  }
  counts$totalCount <- totalCount  
  return(counts)
}

behav.id <- function(vector, levels) {
  sum(vector * levels)
}

exploration.count.aux <- function(data, minv, maxv, levels) {
  for(c in 1:ncol(data)) {
    data[[c]][data[[c]] > maxv[[c]]] <- maxv[[c]]
    data[[c]][data[[c]] < minv[[c]]] <- minv[[c]]
    data[[c]] <- round((data[[c]] - minv[[c]]) / (maxv[[c]] - minv[[c]]) * (levels - 1))
  }
  lev <- levels ^ ((ncol(data)-1):0)
  ids <- apply(data, 1, behav.id, lev)
  len <- levels ^ ncol(data)
  counts <- sapply(1:len, function(x) {sum(ids == x)})
  return(counts)  
}

merge.counts <- function(counts) {
    if(is.list(counts)) {
        countsum <- NULL
        for(c in counts) {
            if(is.null(countsum)) {
                countsum <- merge.counts(c)
            } else {
                countsum <- countsum + merge.counts(c)
            }
        }
        return(countsum)
    } else {
        return(counts)
    }
}

filterSubCount <- function(counts, sub=NULL) {
  for(i in 1:(length(counts)-1)) { # exps
    for(j in 1:length(counts[[i]])) { # jobs
      for(s in names(counts[[i]][[j]])) {
        if(s != sub) {
          counts[[i]][[j]][[s]] <- NULL
        }
      }
    }
  }
  return(counts)
}

uniformity.all <- function(count, t=0.0001, totalCount=count$totalCount, ...) {
  threshold <- t * sum(totalCount)
  cat("Threshold:",threshold, "\n")
  
  plot(sort(totalCount[which(totalCount > 0)], decreasing=T), type="p", pch=20, log="y")
  visited <- which(totalCount > threshold)
  cat("All:", length(totalCount), "| Visited:", length(which(totalCount > 0))  ,"| Filtered:", length(visited),"\n")
  
  setlist <- list()
  for(i in 1:(length(count)-1)) { # exps
    chis <- c()
    for(j in 1:length(count[[i]])) { # jobs
      merge <- merge.counts(count[[i]][[j]])
      chis <- c(chis, count.uniformity(merge[visited], ...))
    }
    setlist[[names(count)[i]]] <- chis
  }
  a <- metaAnalysis(setlist)
  return(a)
}

uniformity.ind <- function(count, subs=names(count[[1]][[1]]), ...) {
  res <- list()
  for(sub in subs) {
    f <- filterSubCount(count, sub)
    u <- uniformity.all(f, ...)$data
    for(n in rownames(u)) {
      res[[n]] <- c(res[[n]], u[n,])
    }
  }
  return(metaAnalysis(res))
}

uniformity.gen <- function(count, t=0.0001, ...) {
  threshold <- t * sum(count$totalCount)
  cat("Threshold:",threshold,"\n")
  plot(sort(count$totalCount[which(count$totalCount > 0)], decreasing=T), type="p", pch=20, log="y")
  visited <- which(count$totalCount > threshold)
  cat("All:", length(count$totalCount), "| Visited:", length(which(count$totalCount > 0))  ,"| Filtered:", length(visited),"\n")  
  
  result <- list()
  result[["Gen"]] <- as.numeric(names(count[[1]][[1]][[1]]))
  for(i in 1:(length(count)-1)) { # exps
    tempresult <- data.frame()
    for(j in 1:length(count[[i]])) { # jobs
      for(g in 1:length(count[[i]][[j]][[1]])) { # gens
        templist <- list()
        for(s in 1:length(count[[i]][[j]])) { # subpops
          templist[[s]] <- count[[i]][[j]][[s]][[g]]
        }
        tempresult[g,j] <- count.uniformity(merge.counts(templist)[visited], ...)
      }
    }
    name <- names(count)[i]
    result[[name]] <- apply(tempresult, 1, mean)
    result[[paste0(name,".sd")]] <- apply(tempresult, 1, sd)
  }
  return(as.data.frame(result))    
}

count.uniformity <- function(vector, type="jsd") {
  if(sum(vector) == 0) {
    return(0)
  } else if(type == "visit") {
    return(sum(vector > 0) / length(vector))
  } else if(type == "jsd") {
    ideal <- rep(1 / length(vector), length(vector))
    vector <- vector / sum(vector)
    return(1 - jsd(vector, ideal))
  } else if(type == "diffs") {
    ideal <- rep(1 / length(vector), length(vector))
    vector <- vector / sum(vector)
    worst <- c(rep(0, length(vector)-1),1)
    totalDiff <- sum(abs(ideal - vector))
    maxDiff <- sum(abs(ideal - worst))
    return (1 - totalDiff / maxDiff)
  } else if(type == "chisq") {
    chisq <- as.numeric(chisq.test(vector)$statistic)
    return(chisq)
  } else {
    return (ineq(vector,type=type))
  }
}

# Jensen–Shannon distance
jsd <- function(p, q) {
  m <- 0.5 * (p + q)
  jsd <- 0.5 * kl(p, m) + 0.5 * kl(q, m)
  return(sqrt(jsd))
}

# Kullback-Leibler divergence
kl <- function(p, q) {
  res <- 0
  for(i in 1:length(p)) {
    if(p[i] != 0) {
      res <- res + log2(p[i] / q[i]) * p[i]
    }
  }
  return(res)
}

#### Generic generational data analysis ########################################

analyse.raw <- function(..., filename="", exp.names=NULL, vars.pre=c(), vars.sub=c(), vars.post=c(), analyse=NULL, gens=NULL, jobs=NULL, transform=list(), include.jobs=F) {
  # data loading
  print("Loading data...")
  expsfolders <- list(...)
  data <- list()
  for(i in 1:length(expsfolders)) {
    f <- expsfolders[[i]]
    index <- ifelse(is.null(exp.names), basename(f), exp.names[i])
    data[[index]] <- loadExp(f, filename, vars.pre, vars.sub, vars.post, gens, transform=transform, jobs=jobs[[i]])
  }
  
  if(is.null(gens)) {
    for(d in data) {
      for(j in d) {
        if(length(j[["gen"]]) > length(gens)) {
          gens <- j[["gen"]]
        }
      }
    }
  }

  # assemble data
  resframe <- data.frame()
  for(a in analyse) {  
    for(exp in names(data)) {
      mean <- list()
      for(job in names(data[[exp]])) {
        d <- data[[exp]][[job]][[a]][gens+1]
        mean[[job]] <- d
      }  
      df <- as.data.frame(mean)
      varframe <- data.frame(
        Generation=gens,
        Var=a,
        Exp=exp,
        Mean=apply(df,1,mean,na.rm=T),
        SD=apply(df,1,sd,na.rm=T),
        SE=apply(df,1,se,na.rm=T)
      )
      if(include.jobs) {
        varframe <- cbind(varframe,df)        
      }
      resframe <- rbind(resframe,varframe)
    }
  }
  return(resframe)
}

analyse <- function(..., filename="", exp.names=NULL, vars.pre=c(), vars.sub=c(), vars.post=c(), analyse=NULL, gens=NULL, jobs=NULL,
                    splits=10, interval=F, t.tests=F, plot=T, boxplots=T, all=T, print=T, smooth=0, transform=list(), ylim=NULL, jitter=T) {
    
    # data loading
    print("Loading data...")
    expsfolders <- list(...)
    data <- list()
    for(i in 1:length(expsfolders)) {
        f <- expsfolders[[i]]
        index <- ifelse(is.null(exp.names), basename(f), exp.names[i])
        data[[index]] <- loadExp(f, filename, vars.pre, vars.sub, vars.post, gens, transform=transform, jobs=jobs[[i]])
    }
    
    if(is.null(gens)) {
      for(d in data) {
        for(j in d) {
          if(length(j[["gen"]]) > length(gens)) {
            gens <- j[["gen"]]
          }
        }
      }
    }

    if(length(splits) == 1) {
      if(splits == 0) {
          splits <- length(gens)
      } else {
          splits <- c((0:(splits-1)) * floor(length(gens) / splits) + 1, length(gens))
      }
    }
    
    # find max and min
    if(is.null(ylim)) {
        min <- +Inf
        max <- -Inf
        for(exp in data) {
            for(job in exp) {
                for(a in analyse) {
                    min <- min(min, min(job[[a]]))
                    max <- max(max, max(job[[a]]))
                }
            }
        }
        ylim <- c(min,max)
    }
            
    # assemble plot data
    plotframe <- data.frame(gen=gens)
    for(a in analyse) {  
        splitsets <- list()
        for(s in splits) {splitsets[[s]] <- list()}
        for(exp in names(data)) {
            mean <- list()
            for(job in names(data[[exp]])) {
                d <- data[[exp]][[job]][[a]][gens+1]
                d <- c(d, rep(NA,length(gens)-length(d)))
                mean[[job]] <- d
                for(s in 1:(length(splits))) {
                    split <- splits[s]
                    if(interval & s != length(splits)) {
                      splitsets[[split]][[exp]] <- c(splitsets[[split]][[exp]], mean(d[splits[s]:splits[s+1]],na.rm=T))
                    } else {
                      splitsets[[split]][[exp]] <- c(splitsets[[split]][[exp]], d[split])
                    }
                }
            }  
            df <- as.data.frame(mean)
            plotframe[[paste(exp,a,sep=".")]] <- rowMeans(df, na.rm=T)
        }

        # t-tests
        if(t.tests) {
            for(s in splits) {
                cat("\n____________________Var:",a,"\tGen:",s,"____________________\n")
                print(metaAnalysis(splitsets[[s]]))
            }
        }
        if(print) {
          printframe <- data.frame()
          for(s in splits) {
            for(exp in names(data)) {
              printframe[paste0("",s), paste0(exp,".mean")] <- mean(splitsets[[s]][[exp]])
              printframe[paste0("",s), paste0(exp,".sd")] <- sd(splitsets[[s]][[exp]])
            }
          }
          print(printframe)
        }
    }
    

    
    # mean plots    
    if(plot) {
        if(smooth > 0) {
            plotframe <- smooth(plotframe, window=smooth)
        }
        g <- plotMultiline(plotframe, title=paste0("Mean plots - Smooth=",smooth), ylim=NULL, ylabel="Value")
        plotToPDF(g, show=T)
    }
    
    if(all) {
        for(exp in names(data)) {
            plotlist <- list()
            for(job in names(data[[exp]])) {
                frame <- data[[exp]][[job]][,c("gen",analyse)]
                if(smooth > 0) {
                    frame <- smooth(frame, window=smooth)
                }
                g <- plotMultiline(frame, title=job, ylim=ylim, ylabel="Value")
                plotlist[[length(plotlist)+1]] <- g
            }
            plotListToPDF(plotlist, show=T, title=paste0("Individual job plots - ",exp," - Smooth=",smooth))
        }
    }
    
    if(boxplots) {
        plots <- list()
        for(s in splits) {
            names <- c()
            frame <- NULL
            for(a in analyse) {
                for(exp in names(data)) {
                    n <- paste(a,exp,sep=".")
                    names <- c(names, n)
                    for(job in names(data[[exp]])) {
                        frame <- rbind(frame, c(n, data[[exp]][[job]][[a]][s]))
                    }
                }
            }
            frame <- data.frame(exp=frame[,1], v=as.numeric(frame[,2]))
            print(frame$exp)
            print(names(data))
            frame$exp <- factor(frame$exp, levels = names,ordered = TRUE)
            p <- ggplot(frame, aes(exp, v)) + 
                geom_boxplot(aes(fill=factor(exp))) +
                ggtitle(paste("Generation",s)) + xlab("") + ylab("Value") +
                theme(axis.text.x = element_text(angle = 45, hjust = 1)) + ylim(ylim[1], ylim[2])
            if(jitter) {
                p <- p + geom_jitter(colour="darkgrey")
            }
            plots[[length(plots)+1]] <- p
        }
        if(length(plots) > 1) {
            plotListToPDF(plots, show=T, title="Boxplots")
        } else {
            plotToPDF(plots[[1]], show=T)
        }
        
    }
    return(plotframe)
}

loadExp <- function(folder, filename, transform=list(), jobs=NULL, ...) {
    files <- list.files(folder, pattern=filename, full.names=T)
    if(!is.null(jobs)) {
      jobnames <- paste0("job\\.",jobs,"\\.")
      files <- files[grep(paste(jobnames,collapse="|"), files)]
    }
    print(files)
    exp <- list()
    for(f in files) {
        jobname <- basename(f)
        e <- loadFile(f, ...)
        for(cn in colnames(e)) {
            if(cn %in% names(transform)) {
                e[[cn]] <- transform(e[[cn]], transform[[cn]])
            }
        }
        exp[[jobname]] <- e
        #print(summary(e))
    }
    return(exp)
}

loadFile <- function(file, vars.pre, vars.sub, vars.post, gens) {
    f <- read.table(file, header=F, sep=" ", stringsAsFactors=F)
    subs <- (ncol(f) - length(vars.pre) - length(vars.post)) / length(vars.sub)
    names <- vars.pre
    if(subs > 1) {
        for(i in 0:(subs-1)) {
            names <- c(names, paste(vars.sub,i,sep="."))
        }
    } else {
        names <- c(names, vars.sub)
    }
    names <- c(names, vars.post)
    colnames(f) <- names
    if(!is.null(gens)) {
        f <- f[which(f$gen %in% gens),]
    }
    if(subs > 1) {
        f <- addSubStats(f, vars.sub)
    }
    return(f)
}

addSubStats <- function(f, vars.sub) {
    names <- colnames(f)
    for(v in vars.sub) {
        match <- names[sapply(names, function(x) grepl(v, x))]
        m <- f[,match]
        f[[paste(v,"mean",sep=".")]] <- rowMeans(m)
        f[[paste(v,"max",sep=".")]] <- apply(m, 1, max)
        f[[paste(v,"min",sep=".")]] <- apply(m, 1, min)
    }
    return(f)
}

#### Cluster analysis ##########################################################

clusterAnalysis <- function(data) {
    means <- data.frame(gen=data$gens[-1])
    maxes <- data.frame(gen=data$gens[-1])
    
    for(j in data$jobs) {
        print(j)
        m <- c()
        ma <- c()
        clusters <- data[[j]]$clusters
        oldcl <- subset(clusters, gen == data$gens[1])[data$clustervars]
        for(g in data$gens[-1]) {
            diffs <- c()
            cl <- subset(clusters, gen == g)[data$clustervars]
            for(r in 1:nrow(cl)) {
                diffs <- c(diffs, euclideanDist(as.numeric(cl[r,]),as.numeric(oldcl[r,])))
            }
            m <- c(m, mean(diffs))
            ma <- c(ma, max(diffs))
            oldcl <- cl
        } 
        means[[j]] <- m
        maxes[[j]] <- ma
    }
    res <- data.frame(gen=data$gens[-1])
    if(ncol(means) == 2) {
        res[["mean"]] <- means[,2]
        res[["max"]] <- maxes[,2]
    } else {
        res[["mean"]] <- rowMeans(means[,-1])
        res[["max"]] <- rowMeans(maxes[,-1])
    }
    return(res)
}

#### Weight analysis ###########################################################

weightAnalysis <- function(data) {
    means <- data.frame()
    maxes <- data.frame()
    for(j in data$jobs) {
        weights <- data[[j]]$weights
        for(w in colnames(weights)[-1]) {
            means[j,w] <- mean(weights[,w])
            maxes[j,w] <- max(weights[,w])
        }
    }
    res <- data.frame()
    for(w in colnames(means)) {
        res[w,"mean"] <- mean(means[,w])
        res[w,"mean.sd"] <- sd(means[,w])
        res[w,"max"] <- mean(maxes[,w])
        res[w,"max.sd"] <- sd(maxes[,w])
    }
    
    # bar plot with mean and max for each weight
    index <- 1
    plotframe <- data.frame()
    for(w in rownames(res)) {
        plotframe[index, "feature"] <- w
        plotframe[index, "value"] <- res[w,"mean"]
        plotframe[index, "type"] <- "mean"
        index <- index + 1
        plotframe[index, "feature"] <- w
        plotframe[index, "value"] <- res[w,"max"]
        plotframe[index, "type"] <- "max"        
        index <- index + 1
    }
    g <- ggplot(plotframe, aes(x = reorder(feature, value), y=value, fill=factor(type))) + geom_bar(stat="identity", position="dodge") + ggtitle(data$expname) + ylab("Weight") + xlab("Feature") + guides(fill=guide_legend(title=NULL))
    plot(g)
    
    res <- res[order(-res$mean),]
    return(res)
}

behaviourCorrelation <- function(data, method="brownian") {
    frame <- data.frame()
    prog <- txtProgressBar(min=0, max=length(data$jobs)*length(data$vars.group), style=3)
    i <- 0
    for(j in data$jobs) {
        fit <- data[[j]][["sub.0"]]$fitness
        #p <- as.numeric(quantile(fit, 0.5))
        #elite <- which(fit > p)
        #fit <- fit[elite]
        
        frame[j, "mean.fit"] <- mean(fit)
        frame[j, "max.fit"] <- max(fit)
        for(v in data$vars.group) {
            var <- data[[j]][["sub.0"]][[v]]
            cor <- NULL
            if(method == "brownian") {
                cor <- dcor(fit, var)
            } else {
                cor <- cor(fit, var, method=method)
            }
            frame[j, v] <- cor 
            gc()
            
            i <- i+1
            setTxtProgressBar(prog, i)
        }
    }
    close(prog)
    for(v in data$vars.group) {
        frame["mean", v] <- mean(frame[data$jobs,v])
        frame["sd", v] <- sd(frame[data$jobs,v])
    }
    return(frame)
}

behaviourCorrelation2 <- function(data) {
    prog <- txtProgressBar(min=0, max=length(data$jobs), style=3)
    i <- 0
    for(j in data$jobs) {
        m <- as.matrix(data[[j]][["sub.0"]][,data$vars.group])
        c <- cor(m, method="spearman")
        View(c)
        i <- i + 1
        setTxtProgressBar(prog, i)
    }
    close(prog)
}

#### Selection pressure ########################################################

pressureAnalysis <- function(data, ...) {
    fits <- data.frame(gen=data$gens)
    novs <- data.frame(gen=data$gens)
    diffs <- data.frame(gen=data$gens)
    for(j in data$jobs) {
        print(j)
        a <- pressureAnalysisRun(data[[j]]$noveltyind, ...)
        fits[[j]] <- a$fit
        novs[[j]] <- a$nov
        diffs[[j]] <- a$diff
    }
    res <- data.frame(gen=data$gens)
    if(ncol(fits) == 2) {
        res[["fit"]] <- fits[,2]
        res[["nov"]] <- novs[,2]
        res[["diff"]] <- diffs[,2]
    } else {
        res[["fit"]] <- rowMeans(fits[,-1])
        res[["nov"]] <- rowMeans(novs[,-1])
        res[["diff"]] <- rowMeans(diffs[,-1])
    }    
    return(res)
}

pressureAnalysisRun <- function(frame, top=0.25) {
    totalFit <- c()
    totalNov <- c()
    totalDiff <- c()
    gens <- unique(frame$gen)
    for(g in gens) {
        subgen <- subset(frame, gen == g, select=c("nov","fit","score"))
        novRange <- range(subgen[["nov"]])
        fitRange <- range(subgen[["fit"]])
        nTop <- ceiling(top * nrow(subgen))
        topIndx <- order(subgen[["score"]], decreasing=T)[1:nTop]
        normNov <- (subgen[topIndx,"nov"] - novRange[1]) / (novRange[2] - novRange[1])
        normFit <- (subgen[topIndx,"fit"] - fitRange[1]) / (fitRange[2] - fitRange[1])
        totalFit <- c(totalFit, mean(normFit))
        totalNov <- c(totalNov, mean(normNov))
        totalDiff <- c(totalDiff, mean(normNov - normFit))
    }
    res <- data.frame(gen=gens, fit=totalFit, nov=totalNov, diff=totalDiff)
    return(res)
}

pressureAnalysisDiff <- function(file) {
    f <- read.table(file, header=F, sep=" ", stringsAsFactors=F)
    colnames(f) <- c("gen","sub","ind","novelty","fitness","repo","score")
    gens <- unique(f[["gen"]])
    res <- c()
    for(g in gens) {
        subgen <- subset(f, gen == g, select=c("novelty","fitness","score"))
        minNov <- min(subgen[["novelty"]])
        maxNov <- max(subgen[["novelty"]])
        minFit <- min(subgen[["fitness"]])
        maxFit <- max(subgen[["fitness"]])
        t <- c()
        for(r in 1:nrow(subgen)) {
            t <- c(t, (subgen[r,"novelty"] - minNov) / (maxNov-minNov) - (subgen[r,"fitness"]-minFit)/(maxFit-minFit))
        }
        res <- c(res, weighted.mean(t, subgen[["score"]]))
    }
    return(res)
}

pressureAnalysisSD <- function(file, top=0.2) {
    f <- read.table(file, header=F, sep=" ", stringsAsFactors=F)
    colnames(f) <- c("gen","sub","ind","novelty","fitness","repo","score")
    gens <- unique(f[["gen"]])
    sdFit <- c()
    sdNov <- c()
    for(g in gens) {
        print(g)
        subgen <- subset(f, gen == g, select=c("novelty","fitness","score"))
        minNov <- min(subgen[["novelty"]])
        maxNov <- max(subgen[["novelty"]])
        minFit <- min(subgen[["fitness"]])
        maxFit <- max(subgen[["fitness"]])
        
        topIndx <- order(subgen[["score"]], decreasing=T)[1:ceiling(top * nrow(subgen))]
        normNov <- c()
        normFit <- c()
        for(i in topIndx) {
            normNov <- c(normNov, (subgen[i,"novelty"] - minNov) / (maxNov-minNov))
            normFit <- c(normFit, (subgen[i,"fitness"]-minFit)/(maxFit-minFit))
        }
                
        sdFit <- c(sdFit, sd(normFit))
        sdNov <- c(sdNov, sd(normNov))
    }
    res <- data.frame(gen=gens, fit=sdFit, nov=sdNov)
    return(res)    
}



pressureAnalysisRate <- function(file, top=0.2) {
    f <- read.table(file, header=F, sep=" ", stringsAsFactors=F)
    colnames(f) <- c("gen","sub","ind","novelty","fitness","repo","score")
    gens <- unique(f[["gen"]])
    res <- c()
    for(g in gens) {
        print(g)
        subgen <- subset(f, gen == g, select=c("novelty","fitness","score"))
        minNov <- min(subgen[["novelty"]])
        maxNov <- max(subgen[["novelty"]])
        
        topIndx <- order(subgen[["score"]], decreasing=T)[1:ceiling(top * nrow(subgen))]
        novRate <- c()
        for(i in topIndx) {
            normNov <- (subgen[i,"novelty"] - minNov) / (maxNov-minNov)
            novRate <- c(novRate, (normNov * 0.5) / subgen[i,"score"])
        }
        res <- c(res, mean(novRate))
    }
    res <- data.frame(gen=gens, novRate=res)
    return(res)    
}


###############

characterisationStats <- function(data) {
    zeros <- data.frame(gen=data$gens)
    for(j in data$jobs) {
        print(j)
        z <- c()
        for(g in data$gens) {
            behavs <- subset(data[[j]][["sub.0"]], gen==g)
            best <- which.max(behavs[["fitness"]])
            char <- behavs[best,data$vars.group]
            z <- c(z, sum(char != 0))
        }
        zeros[[j]] <- z
    }
    res <- data.frame(gen=data$gens)
    if(ncol(zeros) == 2) {
        res[["zeros"]] <- zeros[,2]
    } else {
        res[["zeros"]] <- rowMeans(zeros[,-1])
    }    
    return(res)    
}

##############

correlation <- function(folder, jobs=10, files=NULL, cols=NULL, method="pearson") {
    corrs <- c()
    for(j in 0:(jobs-1)) {
        d1 <- read.table(file.path(folder, paste0("job.",j,".",files[1])), header=F, stringsAsFactors=F, sep=" ")
        d2 <- read.table(file.path(folder, paste0("job.",j,".",files[2])), header=F, stringsAsFactors=F, sep=" ")
        c1 <- d1[,cols[1]]
        c2 <- d2[,cols[2]]
        corrs <- c(corrs, abs(cor(c1, c2, method=method)))
    }
    cat("MEAN: ", mean(corrs), " ; SD: ", sd(corrs), " ; MIN: ", min(corrs), " ; MAX: ", max(corrs), "\n")
    #cat(corrs, "\n")
}




diffs <- function(folder, self.name, all.name) {
    h <- c("gen","best0","bestfar0","a1","a2","a3","best1","bestfar1","a4","a5","a6")
    selfs <- list.files(folder, pattern=self.name, full.names=T)
    alls <- list.files(folder, pattern=all.name, full.names=T)
    meandiff0 <- NULL
    meandiff1 <- NULL
    gen <- NULL
    for(i in 1:length(selfs)) {
        s <- read.table(selfs[i], sep=" ", header=F, col.names=h)
        a <- read.table(alls[i], sep=" ", header=F, col.names=h)
        diffs0 <- a$best0 - s$best0
        diffs1 <- a$best1 - s$best1
        if(is.null(meandiff0)) {
            gen <- s$gen
            meandiff0 <- diffs0
            meandiff1 <- diffs1
        } else {
            meandiff0 <- meandiff0 + diffs0
            meandiff1 <- meandiff1 + diffs1
        }
    }
    meandiff0 <- meandiff0 / length(selfs)
    meandiff1 <- meandiff1 / length(selfs)
    frame <- data.frame(gen=gen, diff0=meandiff0, diff1=meandiff1)
    plotMultiline(frame, ylim=NULL, ylabel="Subjective fitness error")
}

diffs2 <- function(folder, all.name, bvars=3) {
    h <- c("gen","best0","bestfar0",paste0("b",1:bvars),"best1","bestfar1",paste0("b",(bvars+1):(bvars+bvars)))
    selfs <- list.files(folder, pattern="fitness.stat", full.names=T)
    alls <- list.files(folder, pattern=all.name, full.names=T)
    meandiff0 <- NULL
    meandiff1 <- NULL
    corrs0 <- c()
    corrs1 <- c()
    gen <- NULL
    for(i in 1:length(selfs)) {
        s <- read.table(selfs[i], sep=" ", header=F, col.names=c("gen","mean0","best0","bestfar0","mean1","best1","bestfar1","a1","a2","a3","a4"))
        a <- read.table(alls[i], sep=" ", header=F, col.names=c("gen","best0","bestfar0",paste0("b",1:bvars),"best1","bestfar1",paste0("b",(bvars+1):(bvars+bvars))))
        diffs0 <- s$best0 - a$best0
        diffs1 <- s$best1 - a$best1 
        if(is.null(meandiff0)) {
            gen <- s$gen
            meandiff0 <- diffs0
            meandiff1 <- diffs1
        } else {
            meandiff0 <- meandiff0 + diffs0
            meandiff1 <- meandiff1 + diffs1
        }
        corrs0 <- c(corrs0, cor(s$best0,a$best0))
        corrs1 <- c(corrs1, cor(s$best1,a$best1))
    }
    meandiff0 <- meandiff0 / length(selfs)
    meandiff1 <- meandiff1 / length(selfs)
    frame <- data.frame(gen=gen, diff0=meandiff0, diff1=meandiff1)
    plotMultiline(frame, ylim=NULL, ylabel="Subjective fitness error")
    print(summary(corrs0))
    print(summary(corrs1))
}


