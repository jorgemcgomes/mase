library(ggplot2)
library(scales)
library(reshape)
library(gridExtra)
library(kohonen)
library(RColorBrewer)
library(parallel)
library(pbapply)
library(energy)
library(data.table)
library(ineq)
#library(plyr)

SOURCE_DIR = "/home/jorge/Dropbox/mase/"
source(file.path(SOURCE_DIR, "data.r"))
source(file.path(SOURCE_DIR, "analytic.r"))
source(file.path(SOURCE_DIR, "soms.r"))
#source(file.path(SOURCE_DIR, "generic.r"))


options("scipen"=100, "digits"=4)

DEF_WIDTH = 7
DEF_HEIGHT = 5
theme_set(theme_grey(base_size = 12)) 


# General purpose ##############################################################

# data in typicall wide format
# first column are the x values. the remaining columns are the y values.
plotMultiline <- function(data, ylim=c(0,1), legend="right", title=NULL, ylabel="Fitness", col="variable",lty="variable") {
    xlabel <- colnames(data)[1]
    data.long <- melt(data, id=xlabel)
    g <- ggplot(data=data.long, aes_string(x=xlabel, y="value", colour=col,lty=lty)) + geom_line() + theme(legend.position=legend) + ylab(ylabel) + guides(fill=guide_legend(title=NULL))
    if(!is.null(ylim)) {
        g <- g + ylim(ylim[1],ylim[2])
    }
    if(!is.null(title)) {
        g <- g + ggtitle(title)
    }
    return(g)
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
    if(is.null(ncol) & length(plotlist) > 1) {
        ncol <- 2
    }
    if(is.null(nrow)) {
        nrow <- ceiling(length(plotlist) / ncol)
    }
    plot <- do.call("arrangeGrob", c(plotlist, nrow=nrow, ncol=ncol, main=title))
    ggsave(file, plot, width=width * ncol, height=height * nrow, units="in", limitsize=FALSE,...)
    if(show) {
        file.show(file)
    }
}

# Fitness stats ################################################################

fitnessComparisonPlots <- function(..., snapshots=NULL, ttests=TRUE, jitter=TRUE, ylim=FALSE) {
    plots <- list()
    bestfar <- NULL
    bestgen <- NULL
    datalist <- list(...)
    fitlim <- NULL
    if(ylim) {
        fitlim <- datalist[[1]]$fitlim
    }
    for(data in datalist) {
        if(is.null(bestfar)) {
            bestfar <- data.frame(Generation=data$gens)
            bestgen <- data.frame(Generation=data$gens)
        }
        frame <- data.frame(Generation=data$gens)
        genframe <- data.frame(Generation=data$gens)
        for(j in data$jobs) {
            frame[[j]] <- data[[j]]$fitness$best.sofar
            genframe[[j]] <- data[[j]]$fitness$best.gen
        }
        bestfar[[data$expname]] <- rowMeans(frame[,-1])
        bestgen[[data$expname]] <- rowMeans(genframe[,-1])
    }
    plots[[length(plots)+1]] <- plotMultiline(bestfar, ylim=fitlim, title="Best so far")
    plots[[length(plots)+1]] <- plotMultiline(bestgen, ylim=fitlim, title="Best gen")
    
    if(is.null(snapshots)) {
        snapshots <- c(max(bestfar$gen))
    }
    if(ttests) {
        print(generational.ttest(datalist, snapshots))            
    }
    for(s in snapshots) {
        frame <- NULL
        names <- c()
        for(data in datalist) {
            for(job in data$jobs) {
                frame <- rbind(frame, c(data$expname, data[[job]]$fitness$best.sofar[[s]]))
            }
            names <- c(names, data$expname)
        }
        frame <- data.frame(exp=frame[,1], fit=as.numeric(frame[,2]))
        frame$exp <- factor(frame$exp, levels = names,ordered = TRUE)
        p <- ggplot(frame, aes(exp, fit)) + 
            geom_boxplot(aes(fill=factor(exp))) +
            ggtitle(paste("Generation",s)) + xlab("") +
            theme(axis.text.x = element_text(angle = 22.5, hjust = 1)) +
            guides(fill=guide_legend(title=NULL))
        if(jitter) {
            p <- p + geom_jitter(colour="darkgrey")
        }
        if(ylim) {
            p <- p + ylim(fitlim[1],fitlim[2])
        }
        plots[[length(plots)+1]] <- p
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


# Som stats ####################################################################

individualSomPlots <- function(som, data, mapping, ...) {
    plots <- list()
    for(j in data$jobs) {
        for(s in names(mapping[[j]])) {
            plots[[length(plots)+1]] <- somPlot(som, mapping[[j]][[s]], title=paste(j,s), ...)
        }
    }
    return(plots)
}

somPlot <- function(som, mapping, title="som", alpha=0.70, gradient.low="blue", gradient.high="red", colour.limits=c(), size.max=30, limit.min=0.00, limit.max=0.05) {
  almost <- which(mapping$count > 0 & mapping$count < limit.min)
  mapping$count[almost] <- limit.min
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

quickReport <- function(folder, jobs=NULL, snapshots=10, ...) {
    files <- list.files(folder, pattern="bests.tar.gz")
    if(is.null(jobs)) {
        jobs <- length(files)
    }
    
    data <- loadData(folder, jobs=jobs, load.behavs=F, load.clusters=F, load.weights=F, ...)
    frame <- data.frame(gen=data$gens)
    for(j in data$jobs) {
        frame[[j]] <- data[[j]]$fitness$best.sofar
    }
    snapshots <- c((0:(snapshots-1)) * floor(length(data$gens) / snapshots) + 1, length(data$gens))
     means <- c()
    sds <- c()
    mins <- c()
    maxs <- c()
     for(s in snapshots) {
         v <- as.numeric(frame[s,-1])
         means <- c(means, mean(v))
         sds <- c(sds, sd(v))
         mins <- c(mins, min(v))
         maxs <- c(maxs, max(v))
     }
    report <- data.frame(Gen=snapshots,Mean=means,SD=sds,Min=mins,Max=maxs)
    print(paste("Complete jobs:",length(data$jobs)))
    print(report)
}

fullStatistics <- function(..., fit.tests=FALSE, fit.ind=FALSE, fit.comp=FALSE, behav.mean=FALSE, som.ind=FALSE, som.sepind=FALSE, som.group=FALSE, som.alljobs=FALSE, 
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
    if(fit.tests) {
        print(generational.ttest(datalist, fit.comp.par$snapshots))            
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
    if(som.sepind) {
        subs <- datalist[[1]]$subpops
        for(s in subs) {
            cat("SOM ", s, "\n")
            args <- c(som.ind.par, list(variables=datalist[[1]]$vars.ind, subpops=s))
            ksoms$s <- do.call(buildSom, c(datalist, args))
            gc()
            cat("Plot ", s, "\n")
            if(show.only) {
                fitnessHeatmap(ksoms$s, show=T)
                f <- tempfile(fileext=".pdf")
                pdf(f) ; plot(ksoms$s) ; dev.off() ; file.show(f)      
            } else {
                fitnessHeatmap(ksoms$s, file=paste0(expset.name,".",s,".ind.heatmap.pdf"), show=F)
                pdf(paste0(expset.name,".",s,".codes.pdf")) ; plot(ksoms$s) ; dev.off() 
            }  
        }
        for(data in datalist) {
            cat(data$expname, "\n")
            gc()
            plots <- list()
            for(si in 1:length(subs)) {
                s <- subs[si]
                print(s)
                map <- mapIndividualSubpops(ksoms$s, data, s)
                for(ji in 1:length(data$jobs)) {
                    j <- data$jobs[ji]
                    print(j)
                    plots[[(ji-1)*length(subs)+si]] <- somPlot(ksoms$s, map[[j]][[s]], title=paste(j,s), ...)
                    gc()
                }
                rm(map) ; gc()
            }
            if(show.only) {
                plotListToPDF(plots, ncol=length(subs), file=NULL, show=T)
            } else {
                plotListToPDF(plots, ncol=length(subs), file=paste0(expset.name,".sepind.",data$expname,".pdf"), show=F)
            }
        }
    }
    if(som.group || som.alljobs) {
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
            if(som.group) {
              print(paste("Ploting", data$expname))
              if(show.only) {
                  fitnessSomPlot(ksoms$group, data, map, show=T)
              } else {
                  fitnessSomPlot(ksoms$group, data, map, file=paste0(expset.name,".group.",data$expname,".pdf"), show=F)
              }
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
                alljobsPlots[[length(alljobsPlots)+1]] <- somPlot(ksoms$group, allmap, title=data$expname)
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

fitnessRanking <- function(datalist, generation=NULL, name=NULL) {
  setlist <- list()
  for(data in datalist) {
    bests <- c()
    for(job in data$jobs) {
      bests <- c(bests, data[[job]]$fitness$best.sofar[generation])
    }
    setlist[[data$expname]] <- bests
  }
  m <- metaAnalysis(setlist)
  sum <- m$summary
  sum <- sum[with(sum, order(-mean)), ]
  sum$method <- rownames(sum)
  print(sum)
  
  toPlot <- sum
  toPlot$method <- reorder(toPlot$method,toPlot$mean)  
  
  g <- ggplot(toPlot, aes(x=method, y=mean)) +
    geom_bar(stat="identity",fill="#FF9999") + coord_flip() +
    geom_errorbar(aes(ymin=mean-se, ymax=mean+se),width=.2, position=position_dodge(.9)) +
    ggtitle(name)
  print(g)
  return(sum)
}

fitnessRanking2 <- function(datalist, generation=NULL, name=NULL) {
  setlist <- list()
  for(data in datalist) {
    bests <- c()
    for(job in data$jobs) {
      f <- data[[job]]$fitness$best.sofar[generation]
      if(f >= 1) {
        bests <- c(bests, 1)
      } else {
        bests <- c(bests,0)
      }
    }
    setlist[[data$expname]] <- bests
  }
  m <- metaAnalysis(setlist)
  sum <- m$summary
  sum <- sum[with(sum, order(-mean)), ]
  sum$method <- rownames(sum)
  print(sum)
  
  toPlot <- sum
  toPlot$method <- reorder(toPlot$method,toPlot$mean)   
  
  g <- ggplot(toPlot, aes(x=method, y=mean)) +
    geom_bar(stat="identity",fill="#FF9999") + coord_flip() + ggtitle(name) 
  print(g)
  
  return(sum)
}