buildSom <- function(..., variables=NULL, sample.size=25000, distance.filter=0.25, grid.size=10, grid.type="rectangular", compute.fitness=TRUE, scale=TRUE, subpops=NULL) {
    dataList <- list(...)
    sample <- sampleData(dataList, sample.size, subpops)
    sample[is.na(sample)] <- 0.5 # rarely a NA can appear in the sample
    trainData <- sample[,variables]
    rm(dataList)
    gc()
    print("Sampling done")        
    
    if(scale) {
        trainData <- scale(trainData)
        print("Scaling done")
    }
    colnames(trainData) <- variables
    
    somData <- NULL
    if(distance.filter > 0) {
      somData <- data.frame()
      pb <- txtProgressBar(min=1, max=nrow(trainData),style=3)
      for(i in 1:nrow(trainData)) {
        setTxtProgressBar(pb, i)      
        s <- trainData[i,]
        ok <- T
        if(nrow(somData) > 0) {
          for(j in 1:nrow(somData)) {
            if(euclideanDist(as.numeric(s),as.numeric(somData[j,])) < distance.filter) {
              ok <- F
              break
            } 
          }
        }
        if(ok) {
          somData <- rbind(somData, as.numeric(s))
          if(nrow(somData) %% 50 == 0) {
            print(nrow(somData))
          }
        }
      }
      close(pb)
      colnames(somData) <- variables
      print("Filtering done")
    } else {
      somData <- trainData
    }
    
    
    som <- som(as.matrix(somData), keep.data=FALSE, grid=somgrid(grid.size, grid.size, grid.type))
    gc()
    som$fitmin <- min(sample$fitness)
    som$fitmax <- max(sample$fitness)
    
    print("Som done")
    
    if(scale) {
        som$scaled.center <- attr(trainData, "scaled:center")
        som$scaled.scale <- attr(trainData, "scaled:scale")
    }
    if(compute.fitness) {
        som$fitness.avg <- fitnessMapAvg(som, sample)
        som$fitness.max <- fitnessMapQuantile(som, sample, q=0.9)
        print("Average unit fitness done")
    }
    som$count <- countMap(som, sample)
    
    print("Done.")
    return(som)
}

map2 <- function(som, data) {
    if(!is.null(som$scaled.center)) {
        data <- scale(data, center=som$scaled.center, scale=som$scaled.scale)
    }
    return(map(som, data))
}

countMap <- function(som, data) {
    variables <- colnames(som$codes)
    len = som$grid$xdim * som$grid$ydim
    res <- mat.or.vec(len,1)
    data <- as.matrix(data[,variables])
    m <- map2(som, data)$unit.classif
    for(i in 1:len) {
        res[i] <- sum(m == i)
    }
    return(res)
}

fitnessMapAvg <- function(som, data) {
    variables <- colnames(som$codes)
    len = som$grid$xdim * som$grid$ydim
    accum <- mat.or.vec(len,1)
    count <- mat.or.vec(len,1)
    m <- map2(som, as.matrix(data[,variables]))$unit.classif
    for(i in 1:length(m)) {
        accum[m[i]] <- accum[m[i]] + data[i,"fitness"]
        count[m[i]] <- count[m[i]] + 1
    }
    res <- accum / count
    res[is.nan(res)] <- 0
    return(res)
}

fitnessMapMax <- function(som, data) {
    variables <- colnames(som$codes)
    len = som$grid$xdim * som$grid$ydim
    res <- rep(0, len)
    m <- map2(som, as.matrix(data[,variables]))$unit.classif
    for(i in 1:length(m)) {
        res[m[i]] <- max(res[m[i]], data[i,"fitness"])
    }
    return(res)
}

fitnessMapQuantile <- function(som, data, q=0.75) {
    variables <- colnames(som$codes)
    len = som$grid$xdim * som$grid$ydim
    temp <- list()
    temp[[len]] <- 0
    m <- map2(som, as.matrix(data[,variables]))$unit.classif
    for(i in 1:length(m)) {
        temp[[m[i]]] <- c(temp[[m[i]]], data[i,"fitness"])
    }
    for(i in 1:len) {
        if(is.null(temp[[i]])) {
            temp[[i]] <- 0
        } else {
            temp[[i]] <- quantile(temp[[i]], probs=q, names=F)
        }
    }
    return(as.vector(temp, mode="numeric"))    
}

mapIndividualSubpops <- function(som, data, subs=NULL,...) {
    if(is.null(subs)) {
        subs <- data$subpops
    }
    mapping <- lapply(data$jobs, mapIndividualSubpopsAux, som, data, subs, ...)
    names(mapping) <- data$jobs
    return(mapping)
}

mapIndividualSubpopsAux <- function(job, som, data, subs, gen.from=data$gens[1], gen.to=data$gens[length(data$gens)], norm=NULL) {
    mapping <- list()
    for(s in subs) {
        d <- subset(data[[job]][[s]], gen >= gen.from & gen <= gen.to)
        count <- countMap(som, d)
        if(is.null(norm)) {
            norm <- nrow(d)
        }
        count <- count / norm
        mapping[[s]] <- data.frame(somx=som$grid$pts[,1], somy=som$grid$pts[,2], count=count, fitness.avg=som$fitness.avg, fitness.max=som$fitness.max)
    }    
    return(mapping)
}

mapMergeSubpops <- function(som, data, ...) {
    mapping <- lapply(data$jobs, mapMergeSubpopsAux, som, data, ...)
    names(mapping) <- data$jobs
    return(mapping)  
}

mapMergeSubpopsAux <- function(job, som, data, gen.from=data$gens[1], gen.to=data$gens[length(data$gens)], norm=NULL) {
    count <- rep(0, nrow(som$grid$pts))
    for(s in data$subpops) {
        d <- subset(data[[job]][[s]], gen >= gen.from & gen <= gen.to)
        if(is.null(norm)) {
            norm <- nrow(d)
        }
        count <- count + countMap(som, d) / norm
    }
    count <- count / data$nsubs
    d <- data.frame(somx=som$grid$pts[,1], somy=som$grid$pts[,2], count=count, fitness.avg=som$fitness.avg, fitness.max=som$fitness.max)    
    return(list(all=d))
}

identifyBests <- function(som, data, n=10, outfile) {
    # create identification data frame
    variables <- colnames(som$codes)
    idframe <- data.frame(job=rep(NA,100000), sub=rep(NA,100000), gen=rep(NA,100000), id=rep(NA,100000), fitness=rep(NA,100000))
    for(var in variables) {
        idframe[[var]] <- rep(NA,100000)
    }
    index <- 1
    for(job in data$jobs) {
        print(job)
        for(sub in data$subpops) {
            subset <- data[[job]][[sub]]
            for(r in 1:nrow(subset)) {
                row <- subset[r,]
                rowT <- c(job, sub, row["gen"], row["index"], row["fitness"], row[variables])
                idframe[index,] <- rowT
                index <- index + 1
            }
        }
    }
    idframe <- idframe[complete.cases(idframe),]
    
    # map data to som
    m <- map2(som, idframe[,variables])$unit.classif
    
    # pick bests
    bestsframe <- data.frame(x=rep(NA,100000), y=rep(NA,100000))
    for(col in colnames(idframe)) {
        bestsframe[[col]] <- rep(NA,100000)
    }
    index <- 1
    for(i in 1:nrow(som$grid$pts)) {
        mapped <- idframe[m == i,]
        mapped <- mapped[order(mapped$fitness, decreasing=T),]
        if(nrow(mapped) > 0) {
            for(j in 1:min(nrow(mapped),n)) {
                row <- c(som$grid$pts[i,1], som$grid$pts[i,2], mapped[j,])
                bestsframe[index,] <- row
                index <- index + 1
            }
        }
    }
    bestsframe <- bestsframe[complete.cases(bestsframe),]
    write.table(bestsframe, file=outfile, row.names=F, col.names=T)
}