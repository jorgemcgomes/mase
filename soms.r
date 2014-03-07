buildSom <- function(..., variables=NULL, sample.size=50000, grid.size=20, grid.type="rectangular", compute.fitness=TRUE, scale=TRUE, subpops=NULL) {
    dataList <- list(...)
    sample <- sampleData(dataList, sample.size, subpops)

    rm(dataList)
    gc()
    
    sample[is.na(sample)] <- 0.5 # rarely a NA can appear in the sample
    
    # test
    baseSample <- sample[,variables]
    print(nrow(baseSample))
    trainData <- data.frame()
    for(i in 1:nrow(baseSample)) {
        if(i %% 1000 == 0) {
            cat(i,nrow(trainData),"\n")
        }
        s <- baseSample[i,]
        ok <- T
        if(nrow(trainData) > 0) {
            for(j in 1:nrow(trainData)) {
                if(euclideanDist(as.numeric(s),as.numeric(trainData[j,])) < 0.005) {
                    ok <- F
                    break
                } 
            }
        }
        if(ok) {
            #print(s)
            trainData <- rbind(trainData, as.numeric(s))
            
        }
    }
    colnames(trainData) <- variables
    View(trainData)
    # item
    
    #trainData <- sample[,variables]
    if(scale) {
        trainData <- scale(trainData)
    }
    
    print("Sampling done...")
    
    som <- som(as.matrix(trainData), keep.data=FALSE, grid=somgrid(grid.size, grid.size, grid.type))
    gc()
    som$fitmin <- min(sample$fitness)
    som$fitmax <- max(sample$fitness)
    
    print("Som done...")
    
    if(scale) {
        som$scaled.center <- attr(trainData, "scaled:center")
        som$scaled.scale <- attr(trainData, "scaled:scale")
    }
    if(compute.fitness) {
        som$fitness.avg <- fitnessMapAvg(som, sample)
        #som$fitness.max <- fitnessMapMax(som, sample)
        som$fitness.max <- fitnessMapQuantile(som, sample, q=0.9)
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