metaLoadData <- function(..., params, names=NULL) {
    folderList <- list(...)
    if(is.null(names)) {
        names <- folderList
    }
    datas <- list()
    for(i in 1:length(folderList)) {
        cat(folderList[[i]])
        datas[[length(datas)+1]] <- do.call(loadData, c(folder=folderList[[i]], expname=names[[i]], params))
    }
    names(datas) <- names
    return(datas)
}

loadData <- function(folder, jobs=1, fitlim=c(0,1), vars.ind=c(), vars.group=c(), 
                     vars.file=c(vars.group, vars.ind), vars.transform=list(),
                     subpops=3, expname=folder, gens=NULL, load.behavs=TRUE, 
                     behavs.sample=1, fitness.file="fitness.stat", behavs.file="behaviours.stat") {
    data <- NULL
    data$fitlim <- fitlim
    if(is.character(jobs)) {
        data$jobs <- jobs
    } else {
        data$jobs <- paste0("job.",0:(jobs-1))
    }
    data$njobs <- length(data$jobs)
    data$vars.ind <- vars.ind
    data$vars.group <- vars.group
    data$subpops <- paste0("sub.",0:(subpops-1))
    data$nsubs <- length(data$subpops)
    data$folder <- folder
    data$expname <- expname
    data$gens <- gens
    
    progress <- txtProgressBar(min = 0, max = data$njobs, initial = 0, char = "=", style = 3)
    prog <- 0
    for(j in data$jobs) {
        gc()
        data[[j]] <- list()
        ext <- read.table(file.path(folder, paste0(j,".",fitness.file)), header=FALSE, sep=" ")
        if(is.null(data$gens)) {
            data$gens <- ext[[1]]
        } else {
            ext <- ext[which(ext[[1]] %in% data$gens),]
        }
        data[[j]]$fitness <- data.frame(gen=data$gens, best.sofar=ext[[ncol(ext)-1]], best.gen=ext[[ncol(ext)-2]], mean=ext[[ncol(ext)-3]])    
        
        if(load.behavs) {
            tab <- read.table(file.path(folder,paste0(j,".",behavs.file)), header=FALSE, sep=" ", stringsAsFactors=FALSE)
            fixedvars <- c("gen","subpop","index","fitness")
            colnames(tab) <- c(fixedvars,vars.file)
            for(s in 0:(data$nsubs-1)) {
                sub <- subset(tab, subpop == s & gen %in% data$gens, select=c(fixedvars, data$vars.ind, data$vars.group))
                if(behavs.sample < 1) {  # sample
                    sub <- sub[sample(1:nrow(sub), round(nrow(sub) * behavs.sample)),]
                } 
                # apply transformations
                for(v in names(vars.transform)) {
                    if(v %in% colnames(sub)) {
                        sub[,v] <- parSapply(NULL, sub[,v], transform, vars.transform[[v]])
                    }
                }
                data[[j]][[data$subpops[s+1]]] <- sub           
            }
        }
        
        prog <- prog + 1
        setTxtProgressBar(progress, prog)
    }
    return(data)
}

transform <- function(v, t) {
    return(min(max((v + t[1]) * t[2], t[3]), t[4]))
}

filterJobs <- function(data, jobs=c()) {
    for(j in data$jobs) {
        if(!(j %in% jobs)) {
            data[[j]] <- NULL
        }
    }
    data$njobs <- length(jobs)
    data$jobs <- jobs
    return(data)
}

reduceMean <- function(data) {
    progress <- txtProgressBar(min = 0, max = data$njobs, initial = 0, char = "=", style = 3)
    prog <- 0
    for(j in data$jobs) {
        if(length(data$vars.ind) > 0) {
            data[[j]]$mean <- list()
            for(s in data$subpops) {
                res <- data.frame()
                for(g in data$gens) {
                    sub <- subset(data[[j]][[s]], gen==g, select=c("gen",data$vars.ind))
                    sub <- colMeans(sub)
                    res <- rbind(res, sub)
                }
                colnames(res) <- c("gen",data$vars.ind)
                data[[j]]$mean[[s]] <- res
            }
        }
        if(length(data$vars.group) > 0) {
            res <- data.frame()
            temp <- data.frame()
            for(s in data$subpops) {
                temp <- rbind(temp, data[[j]][[s]])
            }
            for(g in data$gens) {
                sub <- subset(temp, gen==g, select=c("gen",data$vars.group))
                sub <- colMeans(sub)
                res <- rbind(res, sub)
            }
            colnames(res) <- c("gen",data$vars.group)            
            data[[j]]$group.mean <- res
        }
        
        prog <- prog + 1
        setTxtProgressBar(progress, prog)
    }
    return(data)
}

sampleData <- function(dataList, sample.size) {
    sample <- data.frame()
    data.sample.size <- sample.size / length(dataList)
    for(data in dataList) {
        each.sample <- round(data.sample.size / data$njobs / data$nsubs)
        for(j in data$jobs) {
            for(s in data$subpops) {
                subsample <- data[[j]][[s]]
                subsample <- subsample[sample(1:nrow(subsample), each.sample),]
                sample <- rbind(sample, subsample)
            }
        }
    }
    return(sample)
}

smoothFrame <- function(frame, window) {
    if(window > 0) {
        for(c in 2:ncol(frame)) {
            frame[[c]] <- smooth(frame[[c]], window=window)
        }
    }
    return(frame)
} 

smooth <- function(data, window=5, weighted=TRUE) {
    if(is.data.frame(data)) {
        for(c in 2:ncol(data)) { # skip gen column
            data[,c] <- smooth(data[,c], window=window, weighted=weighted)
        }
        return(data)
    }
    
    newData <- c()
    for(i in 1:length(data)) {
        backInclusive <- max(i-window,1):i
        wBack <- rev(((window+1):1)[1:length(backInclusive+1)])
        front <- c()
        wFront <- c()
        if(i < length(data)) {
            front <- (i+1):min(i+window,length(data))
            wFront <- (window:1)[1:length(front)]
        }
        if(weighted) {
            newData[i] <- weighted.mean(data[c(backInclusive, front)], c(wBack,wFront))
        } else {
            newData[i] <- mean(data[c(backInclusive, front)])
        }
    }
    return(newData)
}

# assumes that all frames in framelist have the same columns and same number of rows
frameFusion <- function(framelist) {
    result <- NULL
    for(i in 1:ncol(framelist[[1]])) {
        temp <- data.frame(framelist[[1]][[i]])
        for(j in 2:length(framelist)) {
            temp <- cbind(temp, framelist[[j]][[i]])
        }
        r <- rowMeans(temp)
        if(is.null(result)) {
            result <- data.frame(r)
        } else {
            result <- cbind(result, r)
        }
    }
    colnames(result) <- colnames(framelist[[1]])
    return(result)
}