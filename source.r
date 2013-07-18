library(ggplot2)
library(scales)
library(reshape)
library(gridExtra)
library(kohonen)
library(RColorBrewer)
library(parallel)
library(pbapply)


options("scipen"=100, "digits"=4)

DEF_WIDTH = 7
DEF_HEIGHT = 5

# General purpose ##############################################################

# data in typicall wide format
# first column are the x values. the remaining columns are the y values.
plotMultiline <- function(data, ylim=c(0,1), legend="right", title=NULL) {
    xlabel <- colnames(data)[1]
    data.long <- melt(data, id=xlabel)
    g <- ggplot(data=data.long, aes_string(x=xlabel, y="value", colour="variable")) + geom_line() + theme(legend.position=legend)
    if(!is.null(ylim)) {
        g <- g + ylim(ylim[1],ylim[2])
    }
    if(!is.null(title)) {
        g <- g + ggtitle(title)
    }
    return(g)
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

plotToPDF <- function(plot, width=DEF_WIDTH, height=DEF_HEIGHT, file=NULL, show=TRUE, ...) {
    if(is.null(file)) {
        file <- tempfile(fileext=".pdf")
    }
    ggsave(file, plot, width=width, height=height, units="in", limitsize=FALSE,...)
    if(show) {
        file.show(file)
    }
}

plotListToPDF <- function(plotlist, title="", nrow=NULL, ncol=NULL, width=DEF_WIDTH, height=DEF_HEIGHT, file=NULL, show=TRUE, ...) {
    if(is.null(file)) {
        file <- tempfile(fileext=".pdf")
    }
    if(is.null(ncol) & is.null(nrow)) {
        ncol <- nrow <- ceiling(sqrt(length(plotlist)))
    } else if(is.null(ncol)) {
        ncol <- ceiling(length(plotlist) / nrow)
    } else if(is.null(nrow)) {
        nrow <- ceiling(length(plotlist) / ncol)
    }
    plot <- do.call("arrangeGrob", c(plotlist, nrow=nrow, ncol=ncol, main=title))
    ggsave(file, plot, width=width * ncol, height=height * nrow, units="in", limitsize=FALSE,...)
    if(show) {
        file.show(file)
    }
}


# Data manipulation ############################################################

metaLoadData <- function(..., params) {
    folderList <- list(...)
    datas <- list()
    for(f in folderList) {
        datas[[length(datas)+1]] <- do.call(loadData, c(folder=f, params))
    }
    names(datas) <- folderList
    return(datas)
}

loadData <- function(folder, jobs=1, fitlim=c(0,1), vars.ind=c(), vars.group=c(), 
                     vars.file=c(vars.ind, vars.group), vars.transform=list(),
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

extractBehaviours <- function(...) {
    datas <- list(...)
    behav <- NULL
    for(d in datas) {
        print(d$expname)
        for(j in d$jobs) {
            print(j)
            for(s in d$subpops) {
                print(s)
                if(is.null(behav)) {
                    behav <- d[[j]][[s]]
                } else {
                    behav <- rbind(behav, d[[j]][[s]])
                }
            }
        }
    }
    return(behav)
}

expandGeneric <- function(data, var, keyfile) {
    cl <- makeCluster(8)
    # build key
    keyf <- read.table(keyfile, header=F, sep=" ")
    keys <- as.character(keyf[,1])

    # update vars
#     if(var %in% data$var.group) {
#         data$var.group <- c(data$var.group, keys)
#     } else if(var %in% data$var.ind) {
#         data$var.ind <- c(data$var.ind, keys)
#     } else {
#         return(data)
#     }
    
    # build vectors
    for(j in data$jobs) {
        for(s in data$subpops) {
            # fill all positions with zeros 
            temp <- data.frame(behavs=data[[j]][[s]][[var]], stringsAsFactors=FALSE)
            for(k in keys) {
                temp[[k]] <- 0
            }
            
            # split the work
            division <- rep(1:7, each=floor(nrow(temp)/8))
            division <- c(division, rep(8, nrow(temp) - length(division)))
            
            # do the work
            expandAux <- function(subframe) {
                for(r in 1:nrow(subframe)) {
                    split <- strsplit(subframe[r,1], "[;>]")[[1]]
                    for(i in 1:(length(split)/2)) {
                        # if there are clusters, an intermediate mapping must be used here, to map split[i] to the correct index
                        subframe[r,split[i*2-1]] <- as.numeric(split[i*2])
                    }
                }
                return(subframe)
            }
            res <- parLapply(cl, split(temp,division), expandAux)
            res <- rbind(res)
            return(res)
        }
    }
    stopCluster(cl)
    return(data)
}

# Generic generational data analysis

analyse <- function(..., filename="", exp.names=NULL, vars.pre=c(), vars.sub=c(), vars.post=c(), analyse=NULL, gens=NULL, 
                    checkpoints=NULL, splits=NULL, t.tests=TRUE, plot=TRUE, print=TRUE, smooth=0) {
    
    # data loading
    print("Loading data...")
    expsfolders <- list(...)
    data <- list()
    for(i in 1:length(expsfolders)) {
        f <- expsfolders[[i]]
        index <- ifelse(is.null(exp.names), basename(f), exp.names[i])
        data[[index]] <- loadExp(f, filename, vars.pre, vars.sub, vars.post, gens)
    }
    
    if(is.null(gens)) {
        gens <- data[[1]][[1]]$gen
    }
    
    if(analyse %in% vars.sub) {
        analyse <- paste(analyse, "mean", sep=".")
    }
        
    # mean plots
    print("Assembling plot data...")
    plotframe <- data.frame(gen=gens)
    for(exp in names(data)) {
        mean <- list()
        for(job in names(data[[exp]])) {
            mean[[job]] <- data[[exp]][[job]][[analyse]] 
        }
        plotframe[[exp]] <- rowMeans(as.data.frame(mean))
    }
    if(smooth > 0) {
        plotframe <- smooth(plotframe, window=smooth)
    }
    print("Plotting...")
    g <- plotMultiline(plotframe, title=analyse, ylim=NULL)
    plotToPDF(g, show=T)
}

loadExp <- function(folder, filename, ...) {
    files <- list.files(folder, pattern=filename, full.names=T)
    exp <- list()
    for(f in files) {
        jobname <- basename(f)
        exp[[jobname]] <- loadFile(f, ...)
    }
    return(exp)
}

loadFile <- function(file, vars.pre, vars.sub, vars.post, gens) {
    f <- read.table(file, header=F, sep=" ")
    subs <- (ncol(f) - length(vars.pre) - length(vars.post)) / length(vars.sub)
    names <- vars.pre
    for(i in 0:(subs-1)) {
        names <- c(names, paste(vars.sub,i,sep="."))
    }
    names <- c(names, vars.post)
    colnames(f) <- names
    if(!is.null(gens)) {
        f <- f[which(f$gen %in% gens),]
    }
    f <- addSubStats(f, vars.sub)
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

# Fitness stats ################################################################

batch.ttest <- function(sets, names, ...) {
    for(i in 1:length(sets)) {
        sets[[i]] <- as.numeric(sets[[i]])
    }
    matrix <- matrix(data = NA, nrow = length(sets), ncol=length(sets))
    rownames(matrix) <- names
    colnames(matrix) <- names
    for(i in 1:length(sets)) {
        for(j in 1:length(sets)) {
            if(i != j) {
                pvalue <- wilcox.test(as.numeric(sets[[i]]), as.numeric(sets[[j]]), ...)$p.value
                matrix[i,j] <- pvalue
            }
        }
    }
    return(matrix)
}

fitnessComparisonPlots <- function(..., snapshots=NULL, ttests=TRUE) {
    plots <- list()
    bestfar <- NULL
    datalist <- list(...)
    fitlim <- datalist[[1]]$fitlim
    for(data in datalist) {
        if(is.null(bestfar)) {
            bestfar <- data.frame(gen=data$gens)
        }
        frame <- data.frame(gen=data$gens)
        for(j in data$jobs) {
            frame[[j]] <- data[[j]]$fitness$best.sofar
        }
        avg.best <- rowMeans(frame[,-1])
        bestfar[[data$expname]] <- avg.best
    }
    plots[[length(plots)+1]] <- plotMultiline(bestfar, ylim=NULL, title="Best so far")
    
    if(is.null(snapshots)) {
        snapshots <- c(max(bestfar$gen))
    }
    for(s in snapshots) {
        frame <- NULL
        sets <- list()
        sets.names <- c()
        for(data in datalist) {
            set <- c()
            for(job in data$jobs) {
                frame <- rbind(frame, c(data$expname, data[[job]]$fitness$best.sofar[[s]]))
                set <- c(set, data[[job]]$fitness$best.sofar[[s]])
            }
            sets[[length(sets)+1]] <- set
            sets.names <- c(sets.names, data$expname)
        }
        if(ttests) {
            print(paste("Snapshot",s))
            print(batch.ttest(sets, sets.names))            
        }
        
        frame <- data.frame(exp=frame[,1], fit=as.numeric(frame[,2]))
        plots[[length(plots)+1]] <- ggplot(frame, aes(factor(exp), fit)) + 
            geom_boxplot(aes(fill=factor(exp))) + ylim(fitlim[1],fitlim[2]) + 
            geom_jitter(colour="darkgrey") + ggtitle(paste("Generation",s)) + 
            theme(axis.text.x = element_text(angle = 22.5, hjust = 1))
    }
    return(plots)
}

fitnessMetricsPlots <- function(data) {
    bestfar <- NULL
    for(j in data$jobs) {
        if(is.null(bestfar)) {
            bestfar <- data.frame(gen=data$gens)
        }
        bestfar[[j]] <- data[[j]]$fitness$best.sofar
    }
    all <- plotMultiline(bestfar, ylim=data$fitlim, title=data$expname)
    
    bestfar.mean <- rowMeans(bestfar[,-1])
    mean <- plotMultiline(data.frame(gen=data$gens, bestfar=bestfar.mean), ylim=data$fitlim, title=data$expname)
    
    bests <- data.frame(fit=as.numeric(bestfar[nrow(bestfar),-1]))
    boxplot <- ggplot(bests, aes(x="Best fitness", y=fit)) + geom_boxplot() + ylim(data$fitlim[1],data$fitlim[2]) + geom_jitter() + ggtitle(data$expname)
    
    return(list(all,mean,boxplot)) 
}

individualFitnessPlots <- function(data) {
    plots <- list()
    for(j in data$jobs) {
        plots[[length(plots)+1]] <- plotMultiline(data[[j]]$fitness, ylim=data$fitlim, title=paste(j, data$expname))
    }
    return(plots)
}

# Behaviour stats ##############################################################

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

meanBehaviourPlots <- function(data, vars.ind=data$vars.ind, vars.group=data$vars.group, smooth=10) {
    plots <- list()
    for(j in data$jobs) {
        if(length(vars.ind) > 0) {
            for(s in data$subpops) {
                d <- smoothFrame(data[[j]]$mean[[s]][,c("gen",vars.ind)], smooth)
                plots[[length(plots)+1]] <- plotMultiline(d, ylim=c(0,1),title=paste("Ind", j, s, data$expname))
            }
        }
        if(length(vars.group) > 0) {
            d <- smoothFrame(data[[j]]$group.mean[,c("gen",vars.group)], smooth)
            plots[[length(plots)+1]] <- plotMultiline(d, ylim=c(0,1),title=paste("Group", j, data$expname))
        }
    }
    return(plots)
}

smoothFrame <- function(frame, window) {
    if(window > 0) {
        for(c in 2:ncol(frame)) {
            frame[[c]] <- smooth(frame[[c]], window=window)
        }
    }
    return(frame)
} 

intraPopDiversity <- function(data, vars=data$vars.group) {
    result <- list()
    euclideanDist <- function(x1, x2) {crossprod(x1-x2)} 
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
    euclideanDist <- function(x1, x2) {crossprod(x1-x2)} 
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

# Som stats ####################################################################

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

buildSom <- function(..., variables=NULL, sample.size=50000, grid.size=20, grid.type="rectangular", compute.fitness=TRUE, scale=TRUE) {
    dataList <- list(...)
    sample <- sampleData(dataList, sample.size)
    
    rm(dataList)
    gc()
    
    sample[is.na(sample)] <- 0.5 # rarely a NA can appear in the sample
    trainData <- sample[,variables]
    if(scale) {
        trainData <- scale(trainData)
    }
    
    print("Sampling done...")
    
    som <- som(as.matrix(trainData), keep.data=FALSE, grid=somgrid(grid.size, grid.size, grid.type))
    gc()
    
    print("Som done...")
    
    if(scale) {
        som$scaled.center <- attr(trainData, "scaled:center")
        som$scaled.scale <- attr(trainData, "scaled:scale")
    }
    if(compute.fitness) {
        som$fitness.avg <- fitnessMapAvg(som, sample)
        som$fitness.max <- fitnessMapMax(som, sample)
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

mapIndividualSubpops <- function(som, data, ...) {
    mapping <- lapply(data$jobs, mapIndividualSubpopsAux, som, data, ...)
    names(mapping) <- data$jobs
    return(mapping)
}

mapMergeSubpops <- function(som, data, ...) {
    mapping <- lapply(data$jobs, mapMergeSubpopsAux, som, data, ...)
    names(mapping) <- data$jobs
    return(mapping)  
}

mapIndividualSubpopsAux <- function(job, som, data, gen.from=data$gens[1], gen.to=data$gens[length(data$gens)], norm=NULL) {
    mapping <- list()
    for(s in data$subpops) {
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

individualSomPlots <- function(som, data, mapping, ...) {
    plots <- list()
    for(j in data$jobs) {
        for(s in names(mapping[[j]])) {
            plots[[length(plots)+1]] <- somPlot(som, mapping[[j]][[s]], title=paste(j,s), ...)
        }
    }
    return(plots)
}

somPlot <- function(som, mapping, title="som", alpha=0.70, gradient.low="blue", gradient.high="red", colour.limits=c(), size.max=25, limit.max=0.03) {
    g <- ggplot(mapping, aes(somx, somy)) + 
        geom_point(aes(size=count, colour=fitness.max), alpha=alpha) + 
        scale_colour_gradient(limits=colour.limits, low=gradient.low, high=gradient.high, space="Lab") + 
        scale_size(range=c(0,size.max), oob=squish, limits=c(0,limit.max)) + 
        ggtitle(title)
    return(g)
}

fitnessHeatmapPlots <- function(som, gradient.low="white", gradient.high="steelblue") {
    d <- data.frame(x = som$grid$pts[,1], y = som$grid$pts[,2], fitness.max = som$fitness.max, fitness.avg = som$fitness.avg)
    limits <- c(min(som$fitness.avg), max(som$fitness.max))
    p1 <- ggplot(d, aes(x, y)) + geom_tile(aes(fill = fitness.avg), colour="white") + 
        scale_fill_gradient(low=gradient.low, high=gradient.high, limits=limits) + 
        ggtitle("Avg fitness")
    p2 <- ggplot(d, aes(x, y)) + geom_tile(aes(fill = fitness.max), colour="white") + 
        scale_fill_gradient(low=gradient.low, high=gradient.high, limits=limits) + 
        ggtitle("Max fitness")
    return(list(p1,p2))
}

explorationVideo <- function(som, data, mergeSubpops=TRUE, accumulate=TRUE, interval=10, fps=5, out=paste(data$expname,mergeSubpops,accumulate,data$jobs[1],"mp4",sep=".")) {
    if(data$njobs != 1) {
        print("data must have exactly one job")
        return(NULL)
    }
    norm <- NULL
    if(accumulate) {
        norm <- nrow(data[[data$jobs[1]]][[data$subpops[1]]])
    }
    tmpdir <- file.path(tempdir(), "video")
    if(file.exists(tmpdir)) {
        unlink(tmpdir, recursive=TRUE)
    }
    dir.create(tmpdir)
    pb <- txtProgressBar(min=1, max=length(data$gens), style=3)
    
    i <- 0
    for(g in 1:length(data$gens)) {
        if(g %% interval == 0 | g == length(data$gens)) {
            m <- NULL
            from <- data$gens[g-interval+1]
            if(accumulate) {
                from <- data$gens[1]
            }
            if(mergeSubpops) {
                m <- mapMergeSubpops(som, data, gen.from=from, gen.to=data$gens[g], norm=norm)
            } else {
                m <- mapIndividualSubpops(som, data, gen.from=from, gen.to=data$gens[g], norm=norm)
            }
            
            plots <- individualSomPlots(som, data, m)
            name <- sprintf("%05d.png", i)
            plotListToPDF(plots, nrow=1, title=paste("Gen",data$gens[g]), file=file.path(tmpdir,name), show=F)
            i <- i+1
        }
        setTxtProgressBar(pb, g)
    }
    out <- file.path(getwd(),out)
    if(file.exists(out)) {
        unlink(out)
    }
    
    # make video with imagemagick
    cmd <- paste0("mogrify -path ",tmpdir, " -resize 50x50% ", tmpdir, "/*.png") ; cat(cmd) ; system(cmd)
    #  -morph 5
    #cmd <- paste0("convert ", tmpdir, "/*.png -clone 5 ", tmpdir, "/clone%05d.png") ; print(cmd) ; system(cmd)
    cmd <- paste0("ffmpeg -r ",fps," -qscale 2 -i ", tmpdir, "/%05d.png -r 30 \"", out, "\"") ; cat(cmd) ; system(cmd)
}


# High level analysis functions ################################################

fitnessMeanBehaviour <- function(data, ...) {
    mean.list <- meanBehaviourPlots(data)
    fitness.list <- individualFitnessPlots(data)
    n <- length(mean.list) / data$njobs
    plots <- list()
    for(j in 1:data$njobs) {
        plots[[length(plots)+1]] <- fitness.list[[j]]
        for(s in 1:n) {
            plots[[length(plots)+1]] <- mean.list[[(j-1)*n + s]]
        }
    }
    plotListToPDF(plots, ncol=n+1, title=data$expname, ...)    
}

fitnessHeatmap <- function(som, ...) {
    list <- fitnessHeatmapPlots(som)
    plotListToPDF(list, nrow=1, ...) 
}

somPlots <- function(som, data, mapping, ...) {
    plots <- individualSomPlots(som, data, mapping, ...)
    plotListToPDF(plots, ncol=(length(plots)/data$njobs), title=data$expname)
}

fitnessMetrics <- function(data, ...) {
    metrics <- fitnessMetricsPlots(data)
    plotListToPDF(metrics, ncol=length(metrics), title=data$expname, ...)
}

fitnessStats <- function(data, ...) {
    metrics <- fitnessMetricsPlots(data)
    all <- individualFitnessPlots(data)
    plotListToPDF(c(metrics,all), ncol=length(metrics), title=data$expname, ...)
}

fitnessSomPlot <- function(som, data, mapping, ...) {
    fitness <- individualFitnessPlots(data)
    soms <- individualSomPlots(som, data, mapping)
    n <- length(soms) / data$njobs
    plots <- list()
    for(j in 1:data$njobs) {
        plots[[length(plots)+1]] <- fitness[[j]]
        for(s in 1:n) {
            plots[[length(plots)+1]] <- soms[[(j-1)*n + s]]
        }
    }
    plotListToPDF(plots, ncol=n+1, title=data$expname, ...)
}

# Batch analysis functions #####################################################

fullStatistics <- function(..., fit.ind=FALSE, fit.comp=FALSE, behav.mean=FALSE, som.ind=FALSE, som.group=FALSE, som.alljobs=FALSE,
                           fit.ind.par=list(), fit.comp.par=list(), behav.mean.par=list(), som.ind.par=list(), som.group.par=list(), 
                           expset.name="", show.only=FALSE) {
    datalist <- list(...)
    if(length(datalist) == 1 & is.null(datalist[[1]]$folder)) {
        datalist <- datalist[[1]]
    }
    ksoms <- NULL
    args <- NULL
    if(fit.ind) {
        print("Fitness individual experiment plots")
        for(data in datalist) {
            print(data$expname)
            if(show.only) {
                do.call(fitnessStats, c(list(data, show=T), fit.ind.par))
            } else {
                do.call(fitnessStats, c(list(data, file=paste0(expset.name,".fitness.",data$expname,".pdf"), show=F), fit.ind.par))
            }
        }
        gc()
    }
    if(fit.comp) {
        print("Fitness comparison plot")
        plots <- do.call(fitnessComparisonPlots, c(datalist, fit.comp.par))
        if(show.only) {
            plotListToPDF(plots, show=T)
        } else {
            plotListToPDF(plots, file=paste0(expset.name,".fitness.comparison.pdf"), show=F)
        }        
        rm(plots) ; gc()
    }
    if(som.ind) {
        print("Som individual variables plots")
        args <- c(som.ind.par, list(variables=datalist[[1]]$vars.ind))
        print("Building som")
        ksoms$ind <- do.call(buildSom, c(datalist, args))
        gc()
        print("Som plots")
        if(show.only) {
            fitnessHeatmap(ksoms$ind, show=T)
            f <- tempfile(fileext=".pdf")
            pdf(f) ; plot(ksoms$ind) ; dev.off() ; file.show(f)      
        } else {
            fitnessHeatmap(ksoms$ind, file=paste0(expset.name,".ind.heatmap.pdf"), show=F)
            pdf(paste0(expset.name,".ind.codes.pdf")) ; plot(ksoms$ind) ; dev.off() 
        }  
        print("Mapping plots")
        for(data in datalist) {
            gc()
            print(paste("Mapping", data$expname))
            map <- mapIndividualSubpops(ksoms$ind, data)
            print(paste("Ploting", data$expname))
            if(show.only) {
                fitnessSomPlot(ksoms$ind, data, map, show=T)
            } else {
                fitnessSomPlot(ksoms$ind, data, map, file=paste0(expset.name,".ind.",data$expname,".pdf"), show=F)
            }
        }
        rm(map) ; gc()
    }
    if(som.group) {
        print("Som group variables plots")
        args <- c(som.group.par, list(variables=datalist[[1]]$vars.group))
        print("Building som")
        ksoms$group <- do.call(buildSom, c(datalist, args))
        gc()
        print("Som plots")
        if(show.only) {
            fitnessHeatmap(ksoms$group, show=T)
            f <- tempfile(fileext=".pdf")
            pdf(f) ; plot(ksoms$group) ; dev.off() ; file.show(f)      
        } else {
            fitnessHeatmap(ksoms$group, file=paste0(expset.name,".group.heatmap.pdf"), show=F)
            pdf(paste0(expset.name,".group.codes.pdf")) ; plot(ksoms$group) ; dev.off() 
        }          
        
        print("Mapping plots")
        alljobsPlots <- list()
        for(data in datalist) {
            gc()
            print(paste("Mapping", data$expname))
            map <- mapMergeSubpops(ksoms$group, data)
            print(paste("Ploting", data$expname))
            if(show.only) {
                fitnessSomPlot(ksoms$group, data, map, show=T)
            } else {
                fitnessSomPlot(ksoms$group, data, map, file=paste0(expset.name,".group.",data$expname,".pdf"), show=F)
            }
            if(som.alljobs) { 
                allcount <- NULL
                for(job in data$jobs) {
                    if(is.null(allcount)) {
                        allcount <- map[[job]]$all$count
                    } else {
                        allcount <- allcount + map[[job]]$all$count
                    }
                }
                allcount <- allcount / data$njobs
                allmap <- map[[1]]$all # arbitrary one
                allmap$count <- allcount
                alljobsPlots[[length(alljobsPlots)+1]] <- somPlot(ksoms$group, allmap, title=data$expname, colour.limits=data$fitlim)
            }
        }
        if(som.alljobs) {
            print("All group plot")    
            if(show.only) {
                plotListToPDF(alljobsPlots, show=T)
            } else {
                plotListToPDF(alljobsPlots, file=paste0(expset.name,".group.comparison.pdf"), show=F)
            }            
        }
        rm(map, alljobsPlots) ; gc()
    }
    if(behav.mean) {
        print("Mean behaviour plots")
        for(data in datalist) {
            gc()
            print(paste("Reducing",data$expname))
            reduced <- reduceMean(data)
            print(paste("Ploting",data$expname))
            if(show.only) {
                fitnessMeanBehaviour(reduced, show=T)
            } else {
                fitnessMeanBehaviour(reduced, file=paste0(expset.name,".mean.",data$expname,".pdf"), show=F)
            }
        }
        rm(reduced) ; gc()
    }
    return(ksoms)
}