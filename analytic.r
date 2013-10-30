#### General purpose ###########################################################

metaAnalysis <- function(setlist, ...) {
    res <- data.frame(mean=NULL, sd=NULL, min=NULL, max=NULL)
    for(s in names(setlist)) {
        res[s,"mean"] <- mean(setlist[[s]])
        res[s,"sd"] <- sd(setlist[[s]])
        res[s,"min"] <- min(setlist[[s]])
        res[s,"max"] <- max(setlist[[s]])
    }
    tt <- batch.ttest(setlist, ...)
    return(list(summary=res, ttest=tt))
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
    for(i in 1:length(setlist)) {
        setlist[[i]] <- as.numeric(setlist[[i]])
    }
    matrix <- matrix(data = NA, nrow = length(setlist), ncol=length(setlist))
    rownames(matrix) <- names(setlist)
    colnames(matrix) <- names(setlist)
    for(i in 1:length(setlist)) {
        for(j in 1:length(setlist)) {
            if(i != j) {
                pvalue <- wilcox.test(as.numeric(setlist[[i]]), as.numeric(setlist[[j]]), ...)$p.value
                matrix[i,j] <- pvalue
            }
        }
    }
    return(matrix)
}


#### Behaviour diversity metrics ###############################################

euclideanDist <- function(x1, x2) {crossprod(x1-x2)} 

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

# levels: number of levels per variable to consider for patch formation
# only group variables are used, and assumes all the datas have the same vars
exploration.count <- function(datalist, levels=5, vars=datalist[[1]]$vars.group) {    
    pb <- txtProgressBar(min=1, max=length(datalist)*length(datalist[[1]]$subpops)*length(datalist[[1]]$jobs) * length(datalist[[1]]$gens), style=3)
    pbindex <- 1
    
    # subdivide the space in same-size patches
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
    
    # count the patches visited in each method, each evolutionary run, each subpop
    counts <- list()
    for(data in datalist) { # experiment
        exp <- data$expname
        counts[[exp]] <- list()
        for(j in data$jobs) { # jobs
            counts[[exp]][[j]] <- list()
            for(s in data$subpops) { # subpops
                counts[[exp]][[j]][[s]] <- list()
                for(g in data$gens) { # gens
                    setTxtProgressBar(pb,pbindex)
                    pbindex <- pbindex + 1
                    
                    counts[[exp]][[j]][[s]][[g+1]] <- rep(0, levels ^ length(vars))
                    frame <- subset(data[[j]][[s]], gen == g, select=vars)
                    for(v in vars) {
                        frame[[v]] <- round((frame[[v]] - minv[[v]]) / (maxv[[v]] - minv[[v]]) * (levels - 1))
                    }
                    for(r in 1:nrow(frame)) {
                        vec <- as.numeric(frame[r,])
                        id <- sum(vec * levels ^ ((length(vec)-1):0))
                        counts[[exp]][[j]][[s]][[g+1]][id] <- counts[[exp]][[j]][[s]][[g+1]][id] + 1
                    }
                }
            }
        }
    }
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

# Divergence from the total exploration of each experiment to the uniform distribution
uniformity.group <- function(count, threshold=100) {
    all.count <- merge.counts(count)
    print(sum(all.count))
    plot(sort(all.count[which(all.count > 0)], decreasing=T), type="p", pch=20, log="y")
    visited <- which(all.count > threshold)
    ideal <- rep(1 / length(visited), length(visited))
    setlist <- list()
    for(i in 1:length(count)) { # exps
        chis <- c()
        for(j in 1:length(count[[i]])) { # jobs
            job.counts <- merge.counts(count[[i]][[j]])[visited]
            job.counts <- job.counts / sum(job.counts)
            chis <- c(chis, 1 - jsd(job.counts, ideal))
        }
        setlist[[names(count)[i]]] <- chis
    }
    return(metaAnalysis(setlist))
}

uniformity.group.alt <- function(count, threshold=0.0001) {
    all.count <- merge.counts(count)
    plot(sort(all.count[which(all.count > 0)], decreasing=T), type="p", pch=20, log="y")
    visited <- which(all.count > 0)
    ideal <- rep(1 / length(visited), length(visited))
    setlist <- list()
    for(i in 1:length(count)) { # exps
        chis <- c()
        for(j in 1:length(count[[i]])) { # jobs
            job.counts <- merge.counts(count[[i]][[j]])[visited]
            job.counts <- job.counts / sum(job.counts)
            chis <- c(chis, sum(job.counts > threshold) / length(visited))
        }
        setlist[[names(count)[i]]] <- chis
    }
    return(metaAnalysis(setlist))
}

# Divergence from the exploration of each subpop to the uniform distribution
uniformity.ind <- function(count, threshold=100) {
    all.count <- merge.counts(count)
    plot(sort(all.count[which(all.count > 0)], decreasing=T), type="p", pch=20, log="y")
    visited <- which(all.count > threshold)
    ideal <- rep(1 / length(visited), length(visited))
    setlist <- list()
    for(i in 1:length(count)) { # exps
        chis <- c()
        for(j in 1:length(count[[i]])) { # jobs
            subchis <- c()
            for(s in 1:length(count[[i]][[j]])) { # subpops
                subcounts <- merge.counts(count[[i]][[j]][[s]])[visited]
                subcounts <- subcounts / sum(subcounts)
                subchis <- c(subchis, 1 - jsd(subcounts , ideal))
            }
            chis <- c(chis, mean(subchis))
        }
        setlist[[names(count)[i]]] <- chis
    }
    return(metaAnalysis(setlist))    
}

# Pairwise Jensen-Shannon divergence between the exploration of the subpops
uniformity.diff <- function(count, threshold=100) {
    all.count <- merge.counts(count)
    plot(sort(all.count[which(all.count > 0)], decreasing=T), type="p", pch=20, log="y")
    visited <- which(all.count > threshold)
    setlist <- list()
    for(i in 1:length(count)) { # exps
        chis <- c()
        for(j in 1:length(count[[i]])) { # jobs
            subchis <- c()
            for(s in 1:length(count[[i]][[j]])) { # subpops
                for(s2 in 1:length(count[[i]][[j]])) {
                    if(s < s2) {
                        subcounts <- merge.counts(count[[i]][[j]][[s]])[visited]
                        subcounts <- subcounts / sum(subcounts)
                        subcounts2 <- merge.counts(count[[i]][[j]][[s2]])[visited]
                        subcounts2 <- subcounts2 / sum(subcounts2)
                        subchis <- c(subchis, jsd(subcounts , subcounts2))
                    }
                }
            }
            chis <- c(chis, mean(subchis))
        }
        setlist[[names(count)[i]]] <- chis
    }
    return(metaAnalysis(setlist))      
}

uniformity.gen <- function(count, threshold=100) {
    all.count <- merge.counts(count)
    plot(sort(all.count[which(all.count > 0)], decreasing=T), type="p", pch=20, log="y")
    visited <- which(all.count > threshold)
    ideal <- rep(1 / length(visited), length(visited))
    setlist <- list()
    for(i in 1:length(count)) { # exps
        chis <- c()
        for(j in 1:length(count[[i]])) { # jobs
            subchis <- c()
            for(g in 1:length(count[[i]][[j]][[1]])) { # gens
                templist <- list()
                for(s in 1:length(count[[i]][[j]])) { # subpops
                    templist[[s]] <- count[[i]][[j]][[s]][[g]]
                }
                gen.count <- merge.counts(templist)[visited]
                gen.count <- gen.count / sum(gen.count)
                subchis <- c(subchis, 1 - jsd(gen.count , ideal))
            }
            chis <- c(chis, mean(subchis))
        }
        setlist[[names(count)[i]]] <- chis
    }
    return(metaAnalysis(setlist))    
}

uniformity.gen.2 <- function(count, threshold=100) {
    all.count <- merge.counts(count)
    plot(sort(all.count[which(all.count > 0)], decreasing=T), type="p", pch=20, log="y")
    visited <- which(all.count > threshold)
    ideal <- rep(1 / length(visited), length(visited))
    setlist <- list()
    for(i in 1:length(count)) { # exps
        chis <- c()
        for(j in 1:length(count[[i]])) { # jobs
            for(g in 1:length(count[[i]][[j]][[1]])) { # gens
                templist <- list()
                for(s in 1:length(count[[i]][[j]])) { # subpops
                    templist[[s]] <- count[[i]][[j]][[s]][[g]]
                }
                gen.count <- merge.counts(templist)[visited]
                gen.count <- gen.count / sum(gen.count)
                chis <- c(chis, jsd(gen.count , ideal))
            }
        }
        setlist[[names(count)[i]]] <- chis
    }
    return(metaAnalysis(setlist))    
}

# Jensen–Shannon divergence
jsd <- function(p, q) {
    m <- 0.5 * (p + q)
    jsd <- 0.5 * kl(p, m) + 0.5 * kl(q, m)
    return(jsd)
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
    means <- data.frame(gen=data$gens)
    maxes <- data.frame(gen=data$gens)
    for(j in data$jobs) {
        weights <- data[[j]]$weights
        m <- c()
        ma <- c()
        for(g in 1:nrow(weights)) {
            m <- c(m, mean(as.numeric(weights[g,-1])))
            ma <- c(ma, max(as.numeric(weights[g,-1]))) 
        }
        means[[j]] <- m
        maxes[[j]] <- ma
    }
    res <- data.frame(gen=data$gens)
    if(ncol(means) == 2) {
        res[["mean"]] <- means[,2]
        res[["max"]] <- maxes[,2]
    } else {
        res[["mean"]] <- rowMeans(means[,-1])
        res[["max"]] <- rowMeans(maxes[,-1])
    }
    return(res)
}
