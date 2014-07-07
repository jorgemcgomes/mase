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
    maxFitness <- rep(0, levels ^ length(vars))
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
                    frame <- subset(data[[j]][[s]], gen == g, select=c("fitness",vars))
                    for(v in vars) {
                        frame[[v]] <- round((frame[[v]] - minv[[v]]) / (maxv[[v]] - minv[[v]]) * (levels - 1))
                    }
                    for(r in 1:nrow(frame)) {
                        vec <- as.numeric(frame[r,vars])
                        id <- sum(vec * levels ^ ((length(vec)-1):0))
                        counts[[exp]][[j]][[s]][[g+1]][id] <- counts[[exp]][[j]][[s]][[g+1]][id] + 1
                        maxFitness[id] <- max(maxFitness[id], frame[r,"fitness"])
                    }
                }
            }
        }
    }
    counts$maxFitness <- maxFitness
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

merge.counts.sub <- function(counts, targetsub) {
    countsum <- NULL
    for(exp in counts) {
        for(job in exp) {
            for(sub in names(job)) {
                if(is.null(targetsub) || sub == targetsub) {
                    for(i in job[[sub]]) {
                        if(is.null(countsum)) {
                            countsum <- i
                        } else {
                            countsum <- countsum + i
                        }
                    }
                }
            }
        }
    }
    return(countsum)
}

# Divergence from the total exploration of each experiment to the uniform distribution
uniformity.group <- function(count, threshold=100, fitness.threshold=0) {
    all.count <- merge.counts(count)
    plot(sort(all.count[which(all.count > 0)], decreasing=T), type="p", pch=20, log="y")
    visited <- which(all.count > threshold & count$maxFitness > fitness.threshold)
    cat("All:", length(all.count), "| Visited:", length(visited),"\n")
    ideal <- rep(1 / length(visited), length(visited))
    setlist <- list()
    for(i in 1:(length(count)-1)) { # exps: -1 to discard the maxFitness
        chis <- c()
        for(j in 1:length(count[[i]])) { # jobs
            job.counts <- merge.counts(count[[i]][[j]])[visited]
            if(sum(job.counts) > 0) {
                job.counts <- job.counts / sum(job.counts)
            }
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
uniformity.ind <- function(count, sub, threshold=100, fitness.threshold=0) {
    all.count <- merge.counts.sub(count, sub)
    plot(sort(all.count[which(all.count > 0)], decreasing=T), type="p", pch=20, log="y")
    visited <- which(all.count > threshold & count$maxFitness > fitness.threshold)
    cat("All:", length(all.count), "| Visited:", length(visited),"\n")
    print(summary(all.count[visited]))
    ideal <- rep(1 / length(visited), length(visited))
    setlist <- list()
    for(i in 1:(length(count)-1)) { # exps
        chis <- c()
        for(j in 1:length(count[[i]])) { # jobs
            subchis <- c()
            for(s in names(count[[i]][[j]])) { # subpops
                if(is.null(sub) || sub == s) {
                    subcounts <- merge.counts(count[[i]][[j]][[s]])[visited]
                    subcounts <- subcounts / sum(subcounts)
                    subchis <- c(subchis, 1 - jsd(subcounts , ideal))
                }
            }
            chis <- c(chis, mean(subchis))
        }
        setlist[[names(count)[i]]] <- chis
    }
    return(metaAnalysis(setlist))    
}

uniformity.ind.visited <- function(count, sub, threshold=1) {
    all.count <- merge.counts.sub(count, sub)
    plot(sort(all.count[which(all.count > 0)], decreasing=T), type="p", pch=20, log="y")
    visited <- which(all.count > threshold)
    cat("All:", length(all.count), "| Visited:", length(visited),"\n")
    print(summary(all.count[visited]))
    setlist <- list()
    for(i in 1:length(count)) { # exps
        chis <- c()
        for(j in 1:length(count[[i]])) { # jobs
            subchis <- c()
            for(s in names(count[[i]][[j]])) { # subpops
                if(is.null(sub) || sub == s) {
                    subcounts <- merge.counts(count[[i]][[j]][[s]])[visited]
                    above <- length(which(subcounts > threshold))
                    subchis <- c(subchis, above / length(visited))
                }
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
                    splits=10, t.tests=T, plot=T, boxplots=T, all=T, print=F, smooth=0, transform=list(), ylim=NULL, jitter=T) {
    
    # data loading
    print("Loading data...")
    expsfolders <- list(...)
    data <- list()
    for(i in 1:length(expsfolders)) {
        f <- expsfolders[[i]]
        index <- ifelse(is.null(exp.names), basename(f), exp.names[i])
        data[[index]] <- loadExp(f, filename, vars.pre, vars.sub, vars.post, gens, transform=transform)
    }
    
    if(is.null(gens)) {
        gens <- data[[1]][[1]]$gen
    }
    if(splits == 0) {
        splits <- length(gens)
    } else {
        splits <- c((0:(splits-1)) * floor(length(gens) / splits) + 1, length(gens))
    }
    
    # find max and min
    if(is.null(ylim)) {
        min <- +Inf
        max <- -Inf
        for(exp in names(data)) {
            for(job in names(data[[exp]])) {
                for(a in analyse) {
                    min <- min(min, min(data[[exp]][[job]][[a]]))
                    max <- max(max, max(data[[exp]][[job]][[a]]))
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
                d <- data[[exp]][[job]][[a]]
                mean[[job]] <- d
                for(s in splits) {
                    splitsets[[s]][[exp]] <- c(splitsets[[s]][[exp]], d[[s]])
                }
            }  
            plotframe[[paste(exp,a,sep=".")]] <- rowMeans(as.data.frame(mean))
        }

        # t-tests
        if(t.tests) {
            for(s in splits) {
                cat("\nVar:",a,"\tGen:",s,"\n")
                print(batch.ttest(splitsets[[s]]))
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
}

loadExp <- function(folder, filename, transform=list(), ...) {
    files <- list.files(folder, pattern=filename, full.names=T)
    exp <- list()
    for(f in files) {
        print(f)
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

# pressureAnalysisRun <- function(frame) {
#     totalFit <- c()
#     totalNov <- c()
#     totalDiff <- c()
#     gens <- unique(frame$gen)
#     for(g in gens) {
#         subgen <- subset(frame, gen == g, select=c("nov","fit","score"))
#         novRange <- range(subgen[["nov"]])
#         fitRange <- range(subgen[["fit"]])
#         normNov <- (subgen[["nov"]] - novRange[1]) / (novRange[2] - novRange[1])
#         normFit <- (subgen[["fit"]] - fitRange[1]) / (fitRange[2] - fitRange[1])
#         totalFit <- c(totalFit, mean(normFit))
#         totalNov <- c(totalNov, mean(normNov))
#         totalDiff <- c(totalDiff, weighted.mean(normNov - normFit, subgen[["score"]]))
#     }
#     res <- data.frame(gen=gens, fit=totalFit, nov=totalNov, diff=totalDiff)
#     return(res)
# }

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