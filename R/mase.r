#install.packages(c("ggplot2","scales","reshape","kohonen","parallel","doParallel","pbapply","data.table","plyr","doBy","pdist","RColorBrewer","formula.tools","arules","MASS","tsne"))
#.libPaths(c("/home/jorge/R/x86_64-pc-linux-gnu-library/3.4/", .libPaths()))

library(ggplot2)
library(plot3D)
library(cowplot) # arrange multiple ggplots in a grid -- plot_grid

library(scales)
library(reshape)
library(kohonen)
library(parallel)
library(doParallel)
library(pbapply)
library(data.table)
library(plyr)
library(doBy)
library(pdist)
library(RColorBrewer)
library(formula.tools)
library(arules)
library(cluster)
library(amap)
library(parallelDist)

#theme_set(theme_bw())
theme_set(theme_bw(base_size = 8)) # 9?

theme_update(plot.margin=unit(c(0,0,0,0),"mm"), 
             legend.position="bottom", 
             plot.title=element_text(size=rel(1)), 
             strip.background=element_blank(), 
             strip.text=element_text(size=rel(.9)), 
             axis.title.x=element_text(size=rel(.9)),
             axis.title.y=element_text(size=rel(.9)),
             legend.title=element_text(size=rel(.9)),
             legend.key.height=unit(0.75,"line"),
             legend.margin=margin(0,0,0,0,unit="pt"))

# theme_update(plot.margin=unit(c(0.5,0.5,0.5,0.5),"mm"), legend.position="bottom", legend.margin=margin(-5,0,0,0,unit="pt"),
#             plot.title=element_text(size=rel(1)), legend.key.height=unit(0.75,"line"),
#             axis.title.x=element_text(size=rel(.9)), axis.title.y=element_text(size=rel(.9)), legend.title=element_text(size=rel(.9)),
#             strip.background=element_blank(), strip.text=element_text(size=rel(.9)))

# theme_set(theme_bw())
# theme_update(plot.margin=unit(c(0.5,0.5,0.5,0.5),"mm"), legend.position="bottom", legend.margin=margin(-5,0,0,0,unit="pt"),
#                plot.title=element_text(size=rel(1)), legend.key.height=unit(0.75,"line"),
#                strip.background=element_blank())


#### Parallel #############################################################################

createCluster <- function(cores=detectCores()) {
  if(exists("cl")) stopCluster(cl)
  cl <<- makeCluster(cores)
  setDefaultCluster(cl)
  registerDoParallel(cl, cores=length(cl))
  clusterEvalQ(cl, library(ggplot2))
  clusterEvalQ(cl, library(data.table))
  clusterEvalQ(cl, theme_set(theme_bw()))
  return(TRUE)
}

#### Data loading #########################################################################

# options: list of lists in the form of list(function1=list(options1),function2=list(options2))
#metaLoadData <- function(folders, filenames=NULL, functions, options, ...) {
  # TODO
  # for each function, get the options
  # if more than one options, use all sets
#}

# load from multiple files, using the same loading function and same options
metaLoadData <- function(folders, filenames=NULL, filename.ids=filenames, ids=list(), ...) {
  aux <- function(x) loadData(folders, filename=filenames[x], ids=c(ids,File=filename.ids[x]), ...)
  return(ldply(as.list(1:length(filenames)), aux, .progress="text"))
}


# Loads the content of files into a single dataframe
# folders: a vector of folders to be searched
# filename: the filename pattern of the files to be loaded
# names: vector with the setup names to be given for each of the folders. uses folder name if NULL
# ids: list of vectors with additional ids to be given to each folder. 
# auto.ids: determine ids based on the filepath
# auto.ids.names: column names for automatically determined ids (ID1,ID2,... if NULL). Do NOT use the "Setup" name
# jobs: vector with job numbers to be loaded
# jobprefix: prefix used for jobs
# recursive: search folders recursively
# parallel: use multiple cores for data loading (no progress bar)
# fun: function to be called to load one file. Must receive a file name and return a data.table
# ...: other parameters to pass to fun
# filter: function to apply to each loaded file (loaded by fun). Must receive a data.table and return a data.table
# filter.par: other parameters to pass to filter.fun
loadData <- function(folders, filename, names=NULL, ids=list(), auto.ids=T, auto.ids.names=NULL, auto.ids.sep="[_/]", jobs=NULL, jobprefix="job", recursive=F, parallel=F, fun=loadFile, ..., filter=NULL, filter.par=list()) {
  folders <- Sys.glob(folders) # expand
  if(recursive) {
    folders <- as.vector(sapply(folders,list.dirs))
    valid <- sapply(folders, function(x){length(list.files(x,pattern=jobprefix))>0})
    folders <- folders[valid]
  }
  cat("Found",length(folders),"folders\n")
  
  if(is.null(names)) names <- folders
  if(auto.ids) {
    auto <- tstrsplit(folders,auto.ids.sep)
    names(auto) <- if(is.null(auto.ids.names)) paste0("ID",1:length(auto)) else auto.ids.names
    ids <- c(ids,auto)
  }
  ids <- c(list(Setup=names),ids)

  allfiles <- list()
  for(i in 1:length(folders)) {
    if(is.null(jobs)) {
      files <- list.files(folders[[i]], pattern=paste(jobprefix,"\\d+",filename,sep="\\."),full.names=T)
    } else {
      files <- paste(jobprefix,jobs,filename,sep=".")
      files <- paste(folders[[i]],files,sep=.Platform$file.sep)
    }
    folderIds <- sapply(ids, function(x){rep_len(x,length(folders))[i]})
    allfiles <- c(allfiles, lapply(files, c, folderIds))
  }
  cat("Going to load",length(allfiles),"files\n")

  # file is a vector where the first position is the filepath and the other elements are the IDs for this file
  aux <- function(file, ...) {
    tryCatch({
      f <- do.call(fun, args=c(file[1],list(...)))
      if(!is.null(filter)) {
        f <- do.call(filter, args=c(list(f),filter.par))
      }
      f[,Job := as.numeric(gsub("[^0-9]","",basename(file[1])))]
      for(id in names(file[-1])) {
        f[,(id) := file[id]]
      }
      return(f)
    }, error = function(e) {
      message("Error loading file ", file[1], ":")
      message(e)
      return(NULL)
    })
  }

  if(parallel) {
    clusterExport(cl, list("fun","filter","aux"), envir=environment(NULL))
  }
  result <- llply(allfiles, aux, ..., .progress="text", .parallel=parallel, .paropts=list(.errorhandling="remove"))
  result <- rbindlist(result, fill=TRUE)

  cols <- c("Job",names(ids))
  result[,(cols) := lapply(.SD, factorReorder), .SDcols=cols]
  return(result)
}

# loads any file given the column names
loadFile <- function(file, separator=" ",colnames=NULL, exclude.cols=F, ignore.na.cols=F) {
  frame <- fread(file, sep=separator, stringsAsFactors=F, fill=T)
  if(!is.null(colnames)) {
    if(exclude.cols) {
      set(frame, j=which(!(colnames(frame) %in% colnames)), value=NULL)
    } else {
      setnames(frame,colnames)
      if(ignore.na.cols) {
        set(frame, j=which(is.na(colnames)), value=NULL)
      }
    }
  }
  return(frame)
}

# sample: [0,1] percentage of behaviours to retain
# behaviour vars, following the four first fixed (Gen,Sub,Index,Fit)
# columns with NA will be excluded
# bestsOnly: keep only the best (highest fitness) individual of each generation and subpop. sample is ignored
loadBehaviours <- function(file, sample=1, vars=NULL) {
  frame <- fread(file, header=F, sep=" ", stringsAsFactors=F)
  fixedvars <- c("Generation","Subpop","Index","Fitness")
  if(is.null(vars)) {
    vars <- paste0("B",1:(ncol(frame)-length(fixedvars)))
  }
  vars <- c(fixedvars,vars)
  # remove columns with NA in vars
  set(frame, j=which(is.na(vars)), value=NULL)
  setnames(frame, vars[!is.na(vars)])

  frame <- frame[,which(unlist(lapply(frame, function(x)!all(is.na(x))))),with=F] # remove columns with NA only
  frame <- na.omit(frame) # remove rows with NAs
  frame$Subpop <- factor(frame$Subpop)
  
  if(sample < 1) {
    frame <- frame[order(sample(nrow(frame)), nrow(frame) * sample),]
  }
  return(frame)
}

filterBests <- function(data, subpops=F) {
  bestInGen <- function(d) {d[which.max(d$Fitness),]}
  if(subpops) {
    return(data[, bestInGen(.SD), by=.(Generation,Subpop)])
  } else {
    return(data[, bestInGen(.SD), by=.(Generation)])
  }
}

loadFitness <- function(file, loadSubs=F) {
  frame <- fread(file, header=T, sep=" ", stringsAsFactors=F)
  if(!loadSubs) {
    frame <- frame[is.na(Subpop)]
  }
  frame[, Subpop := factor(Subpop)]
  return(frame)
}

factorDT <- function(dt, unique.limit=50) {
  cols <- c()
  for(col in colnames(dt)) {
    if(length(unique(dt[[col]])) <= unique.limit) cols <- c(cols,col)
  }
  copy <- copy(dt)
  if(length(cols) > 0) copy[,(cols) := lapply(.SD, factorReorder), .SDcols=cols]
  return(copy)
}

factorReorder <- function(col, natural.order=F) {
  num <- if(is.factor(col)) as.character(col) else col
  num[is.na(num)] <- Inf
  num <- as.numeric(num)
  if(sum(is.na(num)) > 0) { # there are non-numeric values in the column
    if(natural.order) { # use natural order
      return(factor(col))
    } else { # use order of appearance
      return(factor(col,levels=unique(col)))
    }
  } else {
    return(reorder(col, num))
  }
}

factorNum <- function(x) {
  return(as.numeric(as.character(x)))
}

# Convenience function to filter data to only last generation of each setup
lastGen <- function(data) {
  return(data[, .SD[.N],by=.(Setup,Job)])
}

# Fix postfitness stats by getting the Evaluations from fitness stat
# Only supports postfitness files with a single population
fixPostFitness <- function(folder, postname="postfitness.stat") {
  postfiles <- list.files(folder, pattern=postname, recursive=T, full.names=T)
  regfiles <- gsub(postname, "fitness.stat", postfiles)
  fixed <- 0; skipped <- 0 ; error <- 0
  for(i in 1:length(postfiles)) {
    cat("Fixing",postfiles[i],"\n")
    tryCatch({
    postfit <- fread(postfiles[i])
    if(tail(postfit, 1)[["Evaluations"]]==0) {
      evofit <- fread(regfiles[i])[is.na(Subpop)]
      if(nrow(postfit) == nrow(evofit)) {
        postfit[, Evaluations := evofit[,.(Evaluations)]]
        write.table(postfit, file=postfiles[i], quote=F, sep=" ", row.names=F, col.names=T)
        fixed <- fixed + 1
        cat("Fixed\n")
      } else {
        error <- error + 1
        cat("Number of rows does not match\n")
      }
    } else {
      skipped <- skipped + 1
      cat("Already fixed\n")
    }
    }, error = function(e) {
      message(e)
    })
  }
  cat("Fixed:",fixed,"Skipped:",skipped,"Error:",error,"\n")
}

# get the common start (longest common prefix) of the given array of strings
common.start <- function(x) {
  x<-sort(x) # sort the vector
  d_x<-strsplit(x[c(1,length(x))],"") # split the first and last element by character
  der_com<-match(FALSE,do.call("==",d_x))-1 # search for the first not common element and so, get the last matching one
  # if there is no matching element, return an empty vector, else return the common part
  ifelse(der_com==0,return(character(0)),return(substr(x[1],1,der_com)))
}

#### Fitness plotting functions #####################################################################

bestSoFarFitness <- function(data, showSE=T, xvar="Setup") {
  agg <- data[is.na(Subpop), .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=c(xvar,"Generation")]
  points <- agg[, .SD[seq(from=1,to=.N,length.out=10)], by=xvar]
  g <- ggplot(agg, aes_string("Generation","Mean",group=xvar,fill=xvar)) + geom_line(aes_string(colour=xvar)) + ylab("Fitness") +
    geom_point(data=points, aes_string(shape=xvar,colour=xvar),size=2) + scale_shape_manual(values=0:14)
  if(showSE) {g <- g + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE), alpha = 0.1)}
  return(g)
}

bestSoFarFitnessEvals <- function(data, showSE=T, xvar="Setup") {
  agg <- data[is.na(Subpop), .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=c(xvar,"Evaluations")]
  points <- agg[, .SD[seq(from=1,to=.N,length.out=10)], by=xvar]
  g <- ggplot(agg, aes_string("Evaluations","Mean",group=xvar,fill=xvar)) + geom_line(aes_string(colour=xvar)) + ylab("Fitness") +
    geom_point(data=points, aes_string(shape=xvar,colour=xvar),size=2)
  if(showSE) {g <- g + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE), alpha = 0.1)}
  return(g)
}

fitnessBoxplots <- function(data, generation=NULL, ttests=T, xvar="Setup") {
  data <- if(is.null(generation)) lastGen(data) else subset(data,Generation==generation)
  g <- ggplot(data[is.na(Subpop)], aes_string(x=xvar, y="BestSoFar",fill=xvar)) + geom_boxplot() +
    geom_point(position=position_jitterdodge(jitter.width=0.3, jitter.height=0), size=1, colour="gray") 
  if(ttests) print(fitnessTtests(data,generation=generation, splitvar=xvar))
  return(g)
}

fitnessViolins <- function(data, generation=NULL, ttests=T, xvar="Setup") {
  data <- if(is.null(generation)) lastGen(data) else data[Generation==generation]
  g <- ggplot(data[is.na(Subpop)], aes_string(x=xvar, y="BestSoFar",fill=xvar)) + geom_violin(size=.3, draw_quantiles=c(0.25,0.5,0.75), adjust=0.5)
  if(ttests) print(fitnessTtests(data,generation=generation, splitvar=xvar))  
  return(g)
}

rankByFitness <- function(data, generation=NULL, ttests=T, xvar="Setup") {
  data <- if(is.null(generation)) lastGen(data) else data[Generation==generation]
  agg <- data[is.na(Subpop), .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=xvar]
  agg[, (xvar) := reorder(agg[[xvar]], Mean)]
  g <- ggplot(agg, aes_string(x=xvar, y="Mean", fill=xvar)) + 
    geom_bar(stat="identity") + geom_errorbar(aes(ymin=Mean-SE, ymax=Mean+SE),width=0.5) +
    theme(legend.position="none", axis.text.x = element_text(angle = 45, hjust = 1))
  if(ttests) print(fitnessTtests(data,generation=generation, splitvar=xvar))
  return(g)
}

fitnessTtests <- function(data, generation=NULL, splitvar="Setup") {
  data <- if(is.null(generation)) lastGen(data) else subset(data,Generation==generation)
  data <- subset(data, is.na(Subpop), select=c(splitvar,"BestSoFar"))
  d <- split(data, data[[splitvar]])
  setlist <- lapply(d, function(x){x$BestSoFar})
  return(batch.ttest(setlist))
}

# ... options for loadData
quickReport <- function(folders, filename="postfitness.stat", ttests=T, snapshots=5, ...) {
  fitness <- loadData(folders, filename=filename, fun="loadFitness", ...)
  snapshots <- c((0:(snapshots-1)) * floor(max(fitness$Generation) / snapshots), max(fitness$Generation))
  sub <- subset(fitness, Generation %in% snapshots)
  agg <- summaryBy(BestSoFar ~ Setup + Generation, sub, FUN=c(mean,min,max,sd,length), var.names="f")
  print(agg)

  if(ttests) {
    print(fitnessTtests(sub))
  }
}

#### Fitness utility functions ################

bestSoFarEvaluations <- function(dt, step=10000) {
  dt[, Evaluations := floor(Evaluations / step) * step]
  return(dt[, .(BestSoFar=max(BestSoFar)), by=.(Evaluations)])
}


# data: fitness data
# thresholds: numeric vector with the fitness thresholds to be calculated
fitnessLevels <- function(data, thresholds, return.failed=F) {
  aux <- function(t) {
    w <- which(data[,BestSoFar] > t)
    return(if(length(w) > 0) data$Evaluations[min(w)] else Inf)
  }
  evals <- sapply(thresholds, aux)
  res <- data.table(Threshold=thresholds,Evaluations=evals)
  if(!return.failed) {
    res <- res[!is.infinite(Evaluations)]
  }
  return(res)
}


#### Behavioural analysis #################################################################

euclideanDist <- function(x1, x2) {sqrt(sum((x1 - x2) ^ 2))} 

# calculate the mean pairwise distance without using the dist function, to avoid using too much memory
meanDist <- function(data) {
  require(pdist)
  data <- as.matrix(data)
  aux <- function(index) {
    d <- pdist(data, indices.A=index, indices.B=((index+1):nrow(data)))
    return(sum(attr(d, "dist")))
  }
  clusterEvalQ(NULL, library(pdist))
  dists <- parSapply(cl, 1:(nrow(data)-1), aux)
  return(sum(dists) / (nrow(data) * nrow(data) / 2))
}

# calculate the mean pairwise distance between the two datasets (must have same variables)
meanDistSets <- function(data1, data2) {
  require(pdist)
  m1 <- as.matrix(data1)
  m2 <- as.matrix(data2)
  dist <- as.matrix(pdist(m1,m2))
  if(identical(data1,data2)) {
    dist[upper.tri(dist,diag=T)] <- NA
  }
  return(mean(dist,na.rm=T))
}

averageLinkageDistance <- function(data1, data2) {
  m1 <- as.matrix(data1)
  m2 <- as.matrix(data2)
  d <- parApply(NULL, data1, MARGIN=1, function(x){require(pdist);mean(pdist(x, m2))})
  return(mean(d))
}


# calculate behavioural diversity per-run, based on vars
# if subpops==T, diversity is calculated within each subpop
diversity <- function(data, vars, subpops=F, parallel=F) {
  clusterExport(cl, list("meanDist"))
  fun <- function(d) {c(Diversity=meanDist(subset(d,select=vars)))}
  split <- if(subpops) .(Setup,Job,Subpop) else .(Setup,Job)
  return(ddply(data, split, fun, .progress="text", .parallel=parallel))
}

# calculate behavioural diversity per-generation, based on vars
# interval is the number of generations that should be joined when calculating diversity
# if accum==T, diversity is calculated considering all behaviours up to that generation, else only the generations of that window are used
# if subpops==T, diversity is calculated within each subpop
diversityGens <- function(data, vars, interval, accum=T, subpops=F, parallel=T) {
  clusterExport(cl, list("meanDist"))
  split <- if(subpops) .(Setup,Job,Subpop) else .(Setup,Job)
  aux <- function(d) {
    steps <- seq(from=interval-1,to=max(d$Generation), by=interval)
    result <- data.frame()
    for(s in 1:length(steps)) {
      frame <- subset(d, Generation >= ifelse(accum | s == 1, 0, steps[s - 1]) & Generation <= steps[s], select=vars)
      result <- rbind(result, list(Step=steps[s], Diversity=meanDist(frame)))
    }
    return(result)
  }  
  return(ddply(data, split, aux, .progress="text", .parallel=parallel))
}

#### Behaviour plotting functions #########################################################

# prepare data for buildSom (optional)
# vars: vars to be used in the som
# sampleSize: number of vectors to be used in the som
# cluster: cluster data to avoid large amounts of repeated vectors -- how many centers?
preSomProcess <- function(data, vars=NULL, sampleSize=25000, cluster=0, ...) {
  data <- subset(data[sample(1:nrow(data),sampleSize),], select=c("Fitness",vars))
  if(cluster > 0) {
    require(Rclusterpp) # see https://cran.r-project.org/web/packages/Rclusterpp/vignettes/Rclusterpp.pdf
    h <- Rclusterpp.hclust(subset(data,select=-c(Fitness)), method="ward")
    c <- cutree(h, k=cluster)
    data <- data[!duplicated(c),] # get one arbitrary element from each cluster
  }
  return(data)
}

# data: behaviours, with only the variables to be considered + Fitness, and already sampled as needed
# returns som object
buildSom <- function(data, grid.size=10, grid.type="rectangular", scale=T) {
  somData <- as.matrix(subset(data,select=-c(Fitness)))
  if(scale) {
    somData <- scale(somData)
    print("Scale done")
  }
  som <- som(somData, rlen=1000, keep.data=FALSE, grid=somgrid(grid.size, grid.size, grid.type))
  gc()
  print("Som done")
  if(scale) {
    som$scaled.center <- attr(somData, "scaled:center")
    som$scaled.scale <- attr(somData, "scaled:scale")
  }
  som$map <- mapBehaviours(som, data)
  print("Mapping done")
  return(som)
}

# map a set of behaviours to an already built som
# data: the data to be mapped, with the variables to be considered + Fitness
mapBehaviours <- function(som, data, parallel=F) {
  m <- mapScale(som, data)
  aux <- function(i) {
    subFit <- data[m==i,]$Fitness
    row <- c(som$grid$pts[i,"x"], som$grid$pts[i,"y"], 
             Count=length(subFit), Frequency=length(subFit)/nrow(data), 
             Fitness.mean=mean(subFit), Fitness.min=min(subFit),
             Fitness.max=max(subFit), Fitness.q90=quantile(subFit, probs=0.9, names=F))
    return(row)
  }
  map <- ldply(as.list(1:nrow(som$grid$pts)), aux, .parallel=parallel)
  return(map)
}

# wrapper to som.map function, making the necessary data transformations and scaling if necessary
mapScale <- function(som, data) {
  data <- as.matrix(subset(data,select=colnames(som$codes)))
  if(!is.null(som$scaled.center)) {
    data <- scale(data, center=som$scaled.center, scale=som$scaled.scale)
  }
  return(map(som, data)$unit.classif)
}

# maxLimit: the maximum frequency
# maxQuantile is ignored if maxLimit is set
plotSomFrequency <- function(som, mapping, maxLimit=NULL, maxQuantile=0.975, palette="Greys", showMaxFitness=F) {
  if(is.null(maxLimit)) maxLimit <- quantile(mapping$Frequency, maxQuantile)
  mapping$Frequency[which(mapping$Frequency > maxLimit)] <- maxLimit
  mapping$Frequency[which(mapping$Frequency == 0)] <- NA
  mapping$Fitness.max[which(is.infinite(mapping$Fitness.max))] <- NA
  g <- ggplot(mapping, aes(x, y)) + 
    geom_tile(aes(fill=Frequency), colour="white") + 
    scale_fill_distiller(limits=c(0,maxLimit),type="seq", palette=palette, direction=1, na.value="white") + 
    scale_x_continuous(breaks = 1:max(mapping$x), expand = c(0, 0)) +
    scale_y_continuous(breaks = 1:max(mapping$y), expand = c(0, 0)) + coord_fixed(ratio = max(mapping$y) / max(mapping$x)) +
    xlab(NULL) + ylab(NULL)
  if(showMaxFitness) {
    g <- g + geom_label(aes(label = round(Fitness.max, 2)), na.rm=T)
  }
  return(g)
}

# maxLimit: the maximum frequency
# maxQuantile is ignored if maxLimit is set
# alpha: alpha [0,1] of each bubble -- useful for overlap
# maxSize: maximum size of each bubble
plotSomBubble <- function(som, mapping, maxLimit=NULL, maxQuantile=0.975, useSomFitness=T, palette="RdYlGn", alpha=0.75, maxSize=30) {
  if(is.null(maxLimit)) maxLimit <- quantile(mapping$Frequency, maxQuantile)
  if(useSomFitness) mapping$Fitness.max <- som$map$Fitness.max  # use the som fitness, not the fitness from this mapping
  mapping$Frequency[which(mapping$Frequency > maxLimit)] <- maxLimit
  g <- ggplot(mapping, aes(x, y)) + 
    geom_point(aes(size=Frequency, colour=Fitness.max), alpha=alpha) + 
    scale_colour_distiller(palette=palette, space="Lab", direction=1) + 
    scale_size(range=c(0,maxSize), limits=c(0,maxLimit)) +
    scale_x_continuous(breaks = 1:max(mapping$x), minor_breaks=NULL) +
    scale_y_continuous(breaks = 1:max(mapping$y), minor_breaks=NULL) +
    coord_fixed(ratio = max(mapping$y) / max(mapping$x))
  return(g)  
}

# code can take the values "segments", "stars" and "lines", or NULL
plotSom <- function(som, palette="YlOrRd", code=NULL) {
  colorScale <- brewer.pal(8,palette)
  ramp <- colorRampPalette(colorScale)
  colors <- ramp(1000)
  scale <- round(rescale(som$map$Fitness.max, to=c(1,1000)))
  cols <- colors[scale]
  print(plot(som, bgcol=cols, codeRendering=code, lwd=2))
  cat("Min: ", min(som$map$Fitness.max), " Max: ", max(som$map$Fitness.max))
  #print(plot(som, type="property", property=som$map$Fitness.max, ncolors=20, palette.name=ramp, heatkeywidth=.5))
}

identifyBests <- function(som, data, n=10, interactive=T) {
  if(interactive) {
    plotSom(som)
    ids <- identify(som)    
  } else {
    ids <- 1:nrow(som$grid$pts)
  }
  map <- mapScale(som,data)
  aux <- function(id) {
    submap <- data[map==id,]
    submap <- submap[order(Fitness,decreasing=T),]
    return(cbind(x=som$grid$pts[id,"x"], y=som$grid$pts[id,"y"], head(submap,n)))
  }
  return(ldply(as.list(ids), aux))
}


# Reduces the dimensionality of the given variables to k dimensions
# data: the frame that contains the data to be reduced
# vars: the vars to be reduced or NULL if all
# method: dimensionality reduction method. currently supports sammon, tsne, Rtsne (fast Barnes-Hut tsne)
# ...: to be passed to reduction method
# returns data with extra columns for the reduced data
reduceData <- function(data, vars=NULL, method=c("Rtsne","tsne","sammon","pca","rpca","mds"), k=2, normalise=T, ...) {
  oldCoords <- copy(data)
  if(!is.null(vars)) {
    oldCoords <- data[, vars, with=F]
  }
  oldCoords <- unique(oldCoords) # remove duplicates
  newCoords <- NULL
  if(method[1]=="sammon") {
    require(MASS)
    dists <- parDist(oldCoords)
    sam <- sammon(dists, k=k, ...)
    newCoords <- sam$points
  } else if(method[1]=="tsne") {
    require(tsne)
    dists <- parDist(oldCoords)
    newCoords <- tsne(dists, k=k, ...)
  } else if(method[1]=="Rtsne") {
    if(k==2 & require(Rtsne.multicore)) {
      print("Using Rtsne.multicore implementation")
      ts <- Rtsne.multicore(oldCoords, dims=2, num_threads=detectCores()*2, ...)
      newCoords <- ts$Y      
    } else {
      require(Rtsne)
      print("Using Rtsne single-core implementation")
      ts <- Rtsne(oldCoords, dims=k, ...)
      newCoords <- ts$Y      
    }
  } else if(method[1]=="pca") {
    pc <- prcomp(oldCoords, center=T, scale.=F, rank.=k)
    newCoords <- pc$x
  } else if(method[1]=="rpca") {
    require(rrcov)
    pc <- PcaHubert(oldCoords, k=k, scale=F, ...)
    print(summary(pc))
    s <- cumsum(pc@eigenvalues / sum(pc@eigenvalues))
    print(ggplot(data.table(PC=1:pc@k,CummulativeVariance=s), aes(PC,CummulativeVariance)) + geom_bar(stat="identity"))
    newCoords <- pc@scores
  } else if(method[1]=="mds") {
    require(smacof)
    dists <- parDist(oldCoords)
    md <- mds(dists, ndim=k, verbose=T, ...)
    newCoords <- md$conf
  } else {
    stop("unknown method:", method[1])
  }
  if(normalise) {
    newCoords <- scaleData(newCoords)
  }
  newCoords <- as.data.table(newCoords)
  colnames(newCoords) <- paste0("V",1:k)
  
  return(merge(data, cbind(oldCoords,newCoords), by=vars))
}

# to [0,1]
scaleData <- function(d) {
  (d - min(d)) / (max(d) - min(d))
}

# Plots the sammon mapping in 2D
# reduced: frame with the reduction done (sammonReduce)
# color.var: optional. numeric variable to be used to color the dots
plotReduced2D <- function(reduced, color.var=NULL) {
  g <- ggplot(reduced, aes(x=V1, y=V2)) + geom_point(aes_string(colour=color.var), shape=20, size=.5) + 
    coord_fixed() + theme(legend.position="right", axis.text.x = element_blank(), axis.text.y = element_blank(), axis.ticks=element_blank()) + labs(x=NULL, y=NULL)
  return(g)
}

plotReduced3D <- function(reduced, color.var=NULL, ...) {
  require(plot3D)
  if(is.null(color.var)) {
    scatter3D(reduced$V1, reduced$V2, reduced$V3, phi=20, theta=45, cex=0.6, bty="b2", ticktype="detailed", ...)
  } else {
    scatter3D(reduced$V1, reduced$V2, reduced$V3, phi=20, theta=45, cex=0.6, bty="b2", colvar=reduced[[color.var]], ticktype="detailed", ...)
  }
}

parallelCoordinates <- function(data, vars=NULL, color.var=NULL, points=T, lines=T, alpha=0.1) {
  data[, IDX := 1:.N]
  m <- melt(data, measure.vars=vars)
  g <- ggplot(m, aes(variable, value, group=IDX))
  if(lines) {
    g <- g + geom_line(aes_string(colour=color.var), alpha=alpha)
  }
  if(points) {
    g <- g + geom_point(aes_string(colour=color.var), alpha=alpha)
  }
  return(g)
}

#### General purpose statistics ###########################################################

# histogram with the behaviour dimensions
varsHist <- function(data, vars, breaks="Sturges") {
  setDT(data)
  aux <- function(v, i=interval) {
    h <- hist(v, breaks=breaks, plot=F)
    return(data.table(Break=h$breaks[1:length(h$counts)],Density=h$density, Counts=h$counts, Frequency=h$counts/sum(h$counts)))
  }
  h <- lapply(data[,vars,with=F],aux)
  return(rbindlist(h, idcol="Var"))
}

plotVarsHist <- function(...) {
  hist <- varsHist(...)
  g <- ggplot(hist, aes(Break,Frequency,group=Var,colour=Var)) + geom_line() + geom_point(aes(shape=Var))
  return(g)
}

which.median <- function(x) which.min(abs(x - median(x)))

which.mean <- function(x) which.min(abs(x - mean(x)))

se <- function(x){sd(x)/sqrt(length(x))}

# ex: metaAnalysis(lastGen(data), BestSoFar~Setup, ~ID1)
metaAnalysis <- function(frame, formula, split=NULL, ...) {
  if(is.null(split)) {
    return(frameAnalysis(frame,formula, ...))
  } else {
    res <- dlply(frame,split,frameAnalysis,formula, ...)
    return(res)
  }
}

# ex: frameAnalysis(lastGen(data), BestSoFar~Setup)
frameAnalysis <- function(frame, formula, summary=T, ttests=T, data=F, ...) {
  res <- list()
  if(summary) {
    res[["summary"]] <- summaryBy(formula, data=frame, FUN=c(length,mean,sd,se,min,max))
  }
  split <- splitBy(get.vars(formula)[-1], frame)
  split <- lapply(split, function(x) x[[get.vars(formula)[1]]])
  if(ttests) {
    res[["ttest"]] <- batch.ttest(split, ...)
  }
  if(data) {
    res[["data"]] <- split
  }
  return(res)
}

# ... : parameters to be passed to wilcox.test
batch.ttest <- function(setlist, adjust.method="holm", paired=F, ...) {
  for(n in names(setlist)) { # remove vectors with less than 3 elements
    setlist[[n]] <- if(length(setlist[[n]]) < 3) NULL else as.numeric(setlist[[n]])
  }
  if(length(setlist) < 2) { # there must be at least 2 sets to compare
    return(NULL)
  }
  
  # matrixes to store the pvalues
  matrix <- matrix(data = NA, nrow = length(setlist), ncol=length(setlist))
  rownames(matrix) <- names(setlist)
  colnames(matrix) <- names(setlist)
  adj.matrix <- matrix
  
  # Mann-Whitney tests
  pvalues <- c()
  for(i in 1:length(setlist)) {
    for(j in 1:length(setlist)) {
      if(i < j) {
        matrix[i,j] <- wilcox.test(as.numeric(setlist[[i]]), as.numeric(setlist[[j]]), paired=paired, ...)$p.value
        matrix[j,i] <- matrix[i,j]
        pvalues <- c(pvalues, matrix[i,j])
      }
    }
  }
  
  # Adjusted pvalues
  adjusted <- p.adjust(pvalues, method=adjust.method)
  index <- 1
  for(i in 1:length(setlist)) {
    for(j in 1:length(setlist)) {
      if(i < j) {
        adj.matrix[i,j] <- adjusted[index]
        adj.matrix[j,i] <- adj.matrix[i,j]
        index <- index + 1
      }
    }
  }
  
  # Kruskal-Wallis test
  if(paired) {
    fried <- friedman.test(t(as.data.frame(do.call(rbind, setlist))))
    return(list(friedman=fried, uncorrected=matrix, holm=adj.matrix))
  } else {
    kruskal <- kruskal.test(setlist)
    return(list(kruskal=kruskal, uncorrected=matrix, holm=adj.matrix))
  }
}

