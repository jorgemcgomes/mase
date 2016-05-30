#install.packages(c("ggplot2","scales","reshape","kohonen","parallel","doParallel","pbapply","data.table","plyr","doBy","pdist","RColorBrewer","formula.tools"))
library(ggplot2)
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

theme_set(theme_bw())
theme_update(plot.margin=unit(c(1,1,1,1),"mm"), legend.position="bottom")
#theme_set(theme_bw(base_size = 10))
#theme_update(plot.title = element_text(size=10))

#### Parallel #############################################################################

if(exists("cl")) stopCluster(cl)
cl <- makeCluster(detectCores(logical=T))
registerDoParallel(cl, cores=length(cl))
clusterEvalQ(cl, library(ggplot2))
clusterEvalQ(cl, library(data.table))
clusterEvalQ(cl, theme_set(theme_bw()))
clusterEvalQ(cl, theme_update(plot.margin=unit(c(1,1,1,1),"mm")))

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
# ids: list of vectors with additional ids to be given to each folder. Use "auto" for determining ids based on the filepath
# jobs: vector with job numbers to be loaded
# jobprefix: prefix used for jobs
# recursive: search folders recursively
# parallel: use multiple cores for data loading (no progress bar)
# fun: function to be called to load one file
# ...: parameters to fun
loadData <- function(folders, filename, names=NULL, ids=list(), auto.ids=T, auto.ids.names=NULL, auto.ids.sep="/", jobs=NULL, jobprefix="job", recursive=F, parallel=F, fun="loadFile", ...) {
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

  aux <- function(file, ...) {
    f <- do.call(fun, args=c(file[1],list(...)))
    job <- as.numeric(gsub("[^0-9]","",basename(file[1])))
    f$Job <- rep(job,nrow(f))
    for(id in names(file[-1])) {
      f[[id]] <- file[id]
    }
    return(f)
  }
  
  if(parallel) {
    clusterExport(cl, list("loadFile","loadWideFile","loadBehaviours","loadFitness"))
  }
  result <- ldply(allfiles, aux, ..., .progress="text", .parallel = parallel)
  setDT(result)
  for(id in names(ids)) set(result, j=id, value=factor(result[[id]],levels=unique(result[[id]])))
  set(result,j="Job", value=as.factor(result$Job))
  return(result)
}

# loads any file given the column names
loadFile <- function(file, separator=" ",colnames=NULL, exclude.na=T) {
  frame <- fread(file, header=F, sep=separator, stringsAsFactors=F)
  if(!is.null(colnames)) {
    setnames(frame,colnames)
    if(exclude.na) {
      set(frame, j=which(is.na(colnames)), value=NULL)
    }
  }
  return(frame)
}

loadWideFile <- function(file, separator=" ", pre=c(), repeating=c(), post=c(), key=head(pre,1)) {
  # TODO -- see loadFitnessFile
  
}

# sample: [0,1] percentage of behaviours to retain
# behaviour vars, following the four first fixed (Gen,Sub,Index,Fit)
# columns with NA will be excluded
# bestsOnly: keep only the best (highest fitness) individual of each generation and subpop. sample is ignored
loadBehaviours <- function(file, sample=1, vars=NULL, bestsOnly=F) {
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
  
  if(bestsOnly) {
    bestInGen <- function(data) {data[which.max(data$Fitness),]}
    frame <- ddply(frame, .(Generation,Subpop), bestInGen)
  } else if(sample < 1) {
    samp <- sample(1:nrow(frame), round(nrow(frame) * sample))
    samp <- samp[order(samp)]
    frame <- frame[samp,]
  }
  return(frame)
}

loadFitness <- function(file, loadSubs=F) {
  frame <- fread(file, header=F, sep=" ", stringsAsFactors=F)
  fixedvars <- c("Generation","Evaluations")
  df <- data.table(Generation=frame[[1]],Evaluations=frame[[2]],Subpop="Any",BestSoFar=frame[[ncol(frame)]], BestGen=frame[[ncol(frame)-1]], Mean=frame[[ncol(frame)-2]])
  if(loadSubs) {
    nsubs <- (ncol(frame) - 2) / 3 - 1
    substart <- 3
    for(sub in 0:(nsubs-1)) {
      d <- data.table(Generation=frame[[1]],Evaluations=frame[[2]],Subpop=sub,BestSoFar=frame[[sub*3+substart+2]], BestGen=frame[[sub*3+substart+1]], Mean=frame[[sub*3+substart]])
      df <- rbind(df,d)
    }
    df <- df[order(Generation, Subpop),]
  }
  set(df,j="Subpop", value=as.factor(df$Subpop))
  return(df)
}

# Convenience function to filter data to only last generation of each setup
lastGen <- function(data) {
  d <- ddply(data, .(Setup,Job), function(x) subset(x, Generation==max(x$Generation)))
  setDT(d)
  return(d)
}

#### Fitness plotting functions #####################################################################

bestSoFarFitness <- function(data, showSE=T) {
  agg <- summaryBy(BestSoFar ~ Setup + Generation, subset(data,Subpop=="Any"), FUN=c(mean,se))
  g <- ggplot(agg, aes(Generation,BestSoFar.mean,group=Setup)) + geom_line(aes(colour=Setup)) + ylab("Fitness") + 
    theme(legend.position="bottom")
  if(showSE) {
    g <- g + geom_ribbon(aes(ymax = BestSoFar.mean + BestSoFar.se, ymin = BestSoFar.mean - BestSoFar.se), alpha = 0.1)
  }
  return(g)
}

fitnessBoxplots <- function(data, generation=NULL, ttests=T) {
  data <- if(is.null(generation)) lastGen(data) else subset(data,Generation==generation)
  g <- ggplot(data, aes(x=Setup, y=BestSoFar,fill=Setup)) + geom_boxplot() +
    geom_point(position=position_jitterdodge(jitter.width=0.3, jitter.height=0), colour="gray") 
  if(ttests) print(fitnessTtests(data,generation=generation))
  return(g)
}

rankByFitness <- function(data, generation=NULL, ttests=T) {
  data <- if(is.null(generation)) lastGen(data) else subset(data,Generation==generation)
  agg <- summaryBy(BestSoFar ~ Setup, subset(data,Subpop=="Any"), FUN=c(mean,se))
  agg <- transform(agg, Setup = reorder(Setup, BestSoFar.mean))
  g <- ggplot(agg, aes(x=Setup, y=BestSoFar.mean, fill=Setup)) + 
    geom_bar(stat="identity") + geom_errorbar(aes(ymin=BestSoFar.mean-BestSoFar.se, ymax=BestSoFar.mean+BestSoFar.se),width=0.5) +
    theme(legend.position="none", axis.text.x = element_text(angle = 45, hjust = 1))
  if(ttests) print(fitnessTtests(data,generation=generation))
  return(g)
}

fitnessTtests <- function(data, generation=NULL) {
  data <- if(is.null(generation)) lastGen(data) else subset(data,Generation==generation)
  data <- subset(data, Subpop=="Any", select=c("Setup","BestSoFar"))
  d <- split(data, data$Setup)
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
  dists <- sapply(1:(nrow(data)-1), aux)
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

# calculate behavioural diversity per-run, based on vars
# if subpops==T, diversity is calculated within each subpop
diversity <- function(data, vars, subpops=F, parallel=T) {
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
# cluster: cluster data to avoid large amounts of repeated vectors
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


#### General purpose statistics ###########################################################

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

frameAnalysis <- function(frame, formula, summary=T, ttests=T, data=F, ...) {
  res <- list()
  if(summary) {
    res[["summary"]] <- summaryBy(formula, frame, FUN=c(length,mean,sd,se,min,max))
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
batch.ttest <- function(setlist, adjust.method="holm", ...) {
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
        matrix[i,j] <- wilcox.test(as.numeric(setlist[[i]]), as.numeric(setlist[[j]]), ...)$p.value
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
  kruskal <- kruskal.test(setlist)
  
  return(list(kruskal=kruskal, uncorrected=matrix, holm=adj.matrix))
}

