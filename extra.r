fixRebehaviours <- function(folder, npops) {
  files <- list.files(folder,pattern="rebehaviours.stat", full.names=T)
  for(file in files) {
    print(file)
    # pre-processing: remove ":"
    system(paste0("cp ", file, " ", file,".bak"))
    system(paste0("sed -i 's/://' ",file))
    
    tab <- read.table(file, header=F, sep=" ", fill=T, blank.lines.skip=T, stringsAsFactors=T)
    newFrame <- as.data.frame(matrix(NA, ncol = ncol(tab)*npops, nrow = 1))
    tabIdx <- 1
    newIdx <- 1
    while(tabIdx <= nrow(tab)) {
      newFrame[newIdx,1:(ncol(tab))] <- tab[tabIdx,] # generation, subpop, index, fitness, group char, first ind char
      tabIdx <- tabIdx + 1
      newCol <- ncol(tab) + 1
      for(i in 1:(npops-1)) { # other ind chars
        x <- tab[tabIdx,]
        x <- x[!is.na(x)]
        newFrame[newIdx,newCol:(newCol+length(x)-1)] <- x
        newCol <- newCol + length(x)
        tabIdx <- tabIdx + 1
      }   
      newIdx <- newIdx + 1
    }
    newFrame <- newFrame[,colSums(is.na(newFrame))<nrow(newFrame)]
    write.table(newFrame, file=file, sep=" ", quote=F, row.names=F, col.names=F)
  }
}

# threshold = distance / (diagonal / 2) , diagonal/2 = 141.42/2 = 70.71
# countNear <- function(file, subpops, threshold) {
#   tab <- read.table(file, header=F, sep=" ",fill=T)
#   count <- c()
#   for(r in 1:nrow(tab)) {
#     near <- 0
#     for(i in 0:(subpops-1)) {
#       if(tab[r,11+i*4] < threshold) {
#         near <- near + 1
#       } 
#     }
#     count <- c(count, near)
#   }
#   return(count)
# }
# 
# folderCountNear <- function(folder, subpops, threshold) {
#   files <- list.files(folder,pattern="rebehaviours.stat", full.names=T)
#   jobs <- list()
#   for(file in files) {
#     jobs[[length(jobs)+1]] <- countNear(file, subpops, threshold)
#   }
#   jobs <- as.data.frame(jobs)
#   mean <- rowMeans(jobs)
#   return(mean)
# }

# threshold = distance / (diagonal / 2) , diagonal/2 = 141.42/2 = 70.71
countNearBest <- function(file, subpops, threshold) {
  tab <- read.table(file, header=F, sep=" ",fill=T)
  best <- which.max(tab[,4])
  near <- 0
  for(i in 0:(subpops-1)) {
    if(tab[best,11+i*4] < threshold) {
      near <- near + 1
    } 
  }
  return(near)
}

countNear <- function(folder, subpops, threshold) {
  files <- list.files(folder,pattern="rebehaviours.stat", full.names=T)
  jobs <- c()
  for(file in files) {
    jobs <- c(jobs, countNearBest(file,subpops,threshold))
  }
  return(jobs)
}