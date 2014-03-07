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
                     subpops=1, expname=folder, gens=NULL, load.behavs=TRUE, 
                     load.clusters=FALSE, new.clusters=TRUE, load.weights=FALSE, load.noveltyind=FALSE,
                     behavs.sample=1, fitness.file="fitness.stat", behavs.file="behaviours.stat",
                     clusters.file="genclusters.stat", weights.file="weights.stat",
                     noveltyind.file="noveltyind.stat", offset=1) {
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
        
        # Fitness
        ext <- read.table(file.path(folder, paste0(j,".",fitness.file)), header=FALSE, sep=" ")
        if(is.null(data$gens)) {
            data$gens <- ext[[1]]
        } else {
            ext <- ext[which(ext[[1]] %in% data$gens),]
        }
        data[[j]]$fitness <- data.frame(gen=data$gens, best.sofar=ext[[ncol(ext)-offset]], best.gen=ext[[ncol(ext)-offset-1]], mean=ext[[ncol(ext)-offset-2]])    
        
        # Behaviours
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
        
        # Clusters
        if(load.clusters) {
            cl <- read.table(file.path(folder, paste0(j,".",clusters.file)), header=FALSE, sep=" ", stringsAsFactors=F)
            if(new.clusters) {
                data$clustervars <- paste0("c",1:(ncol(cl)-3))
                colnames(cl) <- c("gen","count","frequency",data$clustervars)
            } else {
                data$clustervars <- paste0("c",1:(ncol(cl)-2))
                colnames(cl) <- c("gen","frequency",data$clustervars)
            }
            data[[j]]$clusters <- cl
        }
        
        # Weights
        if(load.weights) {
            wg <- read.table(file.path(folder, paste0(j,".",weights.file)), header=FALSE, sep=" ", stringsAsFactors=F)
            colnames(wg) <- c("gen",paste0("w",1:(ncol(wg)-1)))
            data[[j]]$weights <- wg
        }
        
        # Individual novelty stats
        if(load.noveltyind) {
            ni <- read.table(file.path(folder, paste0(j,".",noveltyind.file)), header=FALSE, sep=" ", stringsAsFactors=F)
            colnames(ni) <- c("gen","subpop","ind","fit","nov","repocomp","score")
            data[[j]]$noveltyind <- ni
        }
        
        prog <- prog + 1
        setTxtProgressBar(progress, prog)
    }
    return(data)
}

transform <- function(v, t) {
    return((v + t[1]) * t[2])
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

filterSubs <- function(data, subs) {
    data[["subpops"]] <- subs
    data[["nsubs"]] <- length(subs)
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

sampleData <- function(dataList, sample.size, subpops=NULL) {
    sample <- data.frame()
    data.sample.size <- sample.size / length(dataList)
    for(data in dataList) {
        subs <- subpops
        if(is.null(subs)) {
            subs <- data$subpops
        }
        each.sample <- round(data.sample.size / data$njobs / length(subs))
        for(j in data$jobs) {
            for(s in subs) {
                subsample <- data[[j]][[s]]
                n <- min(nrow(subsample), each.sample)
                subsample <- subsample[sample(1:nrow(subsample), n),]
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

transformTournamentFiles <- function(folder, infile, outfile, vars.0, vars.1) {
    files <- list.files(folder, pattern=infile, full.names=T)
    for(f in files) {
        print(f)
        t <- read.table(f, header=F, sep=" ", stringsAsFactors=F)
        df <- data.frame()
        r <- 1
        for(g in 1:nrow(t)) {
            df[r,1] <- t[g,1]
            df[r,2] <- 0
            df[r,3] <- 0
            df[r,4] <- t[g,2]
            for(i in 1:vars.0) {
                df[r,4+i] <- t[g,3+i]
            }
            r <- r+1
            df[r,1] <- t[g,1]
            df[r,2] <- 1
            df[r,3] <- 0
            df[r,4] <- t[g, vars.0 + 4]
            for(i in 1:vars.1) {
                df[r,4+i] <- t[g,5+vars.0+i]
            }
            r <- r+1
        }
        
        o <- gsub(infile, outfile, f)
        write.table(df, file=o, row.names=F, col.names=F, sep=" ", quote=F)
    }
}
