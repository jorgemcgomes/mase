setwd("~/exps/swarm")

# Load direct evolution fitness stats
fitness <- loadData("tasksfinal/**", file="postfitness.stat", fun=loadFitness, auto.ids.names=c("Approach","Task"))

# Load fitness of the repertoires in the tasks
lAux <- function(t){loadData("repfinal/**", paste0(t,"fitness.stat") ,fun=loadFile, ids=list(Task=t), auto.ids.names=c("Approach","Repo"))}
repofit <- rbindlist(lapply(as.character(unique(fitness$Task)), lAux))
setnames(repofit, "Generation", "Index")

fixTaskNames <- function(dt){dt[, Task := factor(Task,levels=c("agg","cluster","coverage","bordercoverage","dispersion","phototaxis","dynphototaxis","flocking"),labels=c("Aggregation","Clustering","Coverage","Border coverage", "Dispersion","Phototaxis","Dynamic phototaxis","Flocking"))]}

# Calculate the highest fitness found in each repertoire for each task
bests <- repofit[, .(Fitness=max(MaxFitness)), by=.(Task,Repo,Job)]

# Load the repertoire behaviour data
bc <- paste0("Behav_",0:15)
cols <- loadData(c("repfinal/**"), filename="collection.stat", auto.ids.names=c("Approach","Repo"))
cols <- cols[Repo=="qdfitlong25"]  # the only one shown in the paper
cols <- reduceData(cols, vars=bc, method="rpca",k=2)

# Repertoire stats
plotReduced2D(cols, color.var="Fitness") + facet_wrap(~ Job)
repo.stats <- cols[, .(MinDist=min(dist(.SD[,bc,with=F])), MeanFit=mean(Fitness), SDFit=sd(Fitness), .N), by=.(Repo,Job)]
repo.stats[, .(MinDist=mean(MinDist),MeanFit=mean(MeanFit),SDFit=mean(SDFit),MeanN=mean(N),SDN=sd(N)), by=.(Repo)]

# Repertoire evolution (not shown in paper)
col.evo <- loadData("repfinal/qdfitlong25", file="collector.stat")
c.stat <- col.evo[, .(MeanSize=mean(Size),SESize=sd(Size),MeanFitness=mean(MeanFitness),SEFitness=sd(MeanFitness)), by=.(Generation)]
ggplot(c.stat, aes(Generation,MeanSize)) + geom_line() + geom_ribbon(aes(ymin=MeanSize-SESize,ymax=MeanSize+SESize), alpha=.2) + ylab("Repertoire size")
ggsave("~/Dropbox/Work/Papers/18-ANTS/size.pdf", width=2.4, height=1.5)
ggplot(c.stat, aes(Generation,MeanFitness)) + geom_line() + geom_ribbon(aes(ymin=MeanFitness-SEFitness,ymax=MeanFitness+SEFitness), alpha=.2) + ylab("Mean quality")
ggsave("~/Dropbox/Work/Papers/18-ANTS/quality.pdf", width=2.4, height=1.5)

# Fitness comparison
bests.sum <- bests[Repo%in%c("qdfitlong25","rand"), .(Mean=mean(Fitness),SE=se(Fitness)), by=.(Task,Repo)]
bests.sum[, Repo := factor(Repo, labels=c("Evolved repertoire", "Random repertoire"))]
fixTaskNames(bests.sum)
tr.sum <- fitness[, .(Mean=mean(BestSoFar),SE=se(BestSoFar),Repo="Direct evolution"), by=.(Task,Generation)]
fixTaskNames(tr.sum)
POINTS = 6 # number of points to show in plot (over the lines)
tr.points <- tr.sum[, .SD[seq(from=1,to=.N,length.out=POINTS)], by=.(Task,Repo)]
bests.points <- bests.sum[rep(1:.N,POINTS)][,Generation:=seq(from=0,to=max(tr.sum$Generation),length.out=POINTS),by=.(Task,Repo)]
ggplot(rbind(tr.sum,bests.points), aes(Generation,Mean,group=Repo)) + 
  geom_line(aes(colour=Repo),size=.35) + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE,fill=Repo), alpha = 0.2) + 
  facet_wrap(~ Task, scales="free_y", nrow=2) + geom_point(data=rbind(tr.points,bests.points), size=1, aes(shape=Repo,colour=Repo)) +
  labs(y="Fitness", fill="Approach",colour="Approach", shape="Approach")
ggsave("~/Dropbox/Work/Papers/18-ANTS/fitness.pdf", width=4.8, height=2.5)

# Statistical comparison
joined <- rbind(bests[Repo%in%c("qdfitlong25","rand")], lastGen(fitness)[, .(Task,Repo="Direct",Job,Fitness=BestSoFar)])
metaAnalysis(joined, Fitness ~ Repo, ~Task)

# Fitness heatmap
base <- merge(repofit, cols, by=c("Index","Job","Repo","Setup"))
fixTaskNames(base)
base[, Bin1 := round(V1*15)/15]
base[, Bin2 := round(V2*15)/15]
bined <- base[, .(Fitness=mean(MaxFitness),.N), by=.(Task,Bin1,Bin2,Job)]
bined <- bined[, .(Fitness=mean(Fitness),.N), by=.(Task,Bin1,Bin2)][N==length(unique(base$Job))]
bined[, Fitness := (Fitness - min(Fitness)) / (max(Fitness)-min(Fitness)), by=.(Task)]
ggplot(bined, aes(Bin1,Bin2,fill=Fitness)) + 
  scale_x_continuous(expand=c(0,0)) + scale_y_continuous(expand=c(0,0)) + labs(x="PC1", y="PC2", fill="Fitness in task (scaled)") +
  geom_tile() + facet_wrap(~ Task, nrow=2) + scale_fill_distiller(palette="Spectral") + coord_fixed()
ggsave("~/Dropbox/Work/Papers/18-ANTS/heatmap.pdf", width=4.8, height=2.7)











###############################################################################
### GARBAGE AND NON-USED STUFF BELOW ##########################################
###############################################################################

# interpolated variant
require(sp)
require(automap)
require(gstat)
coordinates(base) = ~V1+V2 # Set spatial coordinates to create a Spatial object
plot(base) # plot points (debug)
# Expand points to grid
v1.range <- range(base$V1)
v2.range <- range(base$V2)
grd <- expand.grid(V1 = seq(from = v1.range[1], to = v1.range[2], by = 0.01), V2 = seq(from = v2.range[1], to = v2.range[2], by = 0.01))  
coordinates(grd) <- ~V1+V2
gridded(grd) <- TRUE
# Plot the points over the interpolation grid (debug)
plot(grd, cex = 1.5, col = "grey")
points(base, pch = 1, col = "red", cex = 1)
# inverse distance weighted interpolation
t <- base[base$Repo=="qdfitlong25" & base$Task=="Flocking",]
idw <- krige(formula = MaxFitness ~ 1, locations=base, newdata = grd)  # apply idw model for the data
ggplot(as.data.table(idw), aes(V1,V2,fill=var1.pred)) + 
  scale_x_continuous(expand=c(0,0)) + scale_y_continuous(expand=c(0,0)) + labs(x="PC1", y="PC2", fill="Fitness in task (scaled)") +
  geom_tile() + scale_fill_distiller(palette="Spectral") + coord_fixed()


# Repo STATS
st <- cols[, .(MinDist=min(dist(.SD[,bc,with=F])),MeanDist=mean(dist(.SD[,bc,with=F])),SDDist=sd(dist(.SD[,bc,with=F])), MeanFit=mean(Fitness), SDFit=sd(Fitness), .N), by=.(Repo,Job)]
st[, .(MeanFit=mean(MeanFit),SDFit=mean(SDFit),MeanN=mean(N),SDN=sd(N)), by=.(Repo)]




plotReduced2D(cols[as.numeric(Job)<5], color.var="Fitness") + facet_grid(Repo ~ Job)


# t-tests
m <- metaAnalysis(bests, Fitness ~ Repo, ~Task)
sapply(m, function(x) x$ttest$holm[1,2])

# point plots
tr <- lastGen(fitness)[, .(Fitness=BestSoFar,Repo="TR"), by=.(Task,Job)]
ggplot(rbind(bests,tr), aes(Repo,Fitness)) + geom_point(aes(colour=Repo)) + facet_wrap(~ Task, scales="free")


# Exploration plots
plotReduced2D(cols) + facet_grid(Repo ~ Job)
plotReduced2D(cols[Repo!="randmlp"], color.var="Fitness") + facet_grid(Repo ~ Job)




# 1d usefulness
cols <- reduceData(cols, vars=bc, method="Rtsne",k=1)
merged <- merge(repofit, cols, by=c("Index","Job","Repo","Setup"))
merged <- merged[Job==0]
merged[, MaxFitness := (MaxFitness-min(MaxFitness))/(max(MaxFitness)-min(MaxFitness)), by=.(Task)]
ggplot(merged, aes(V1,MaxFitness,group=Task)) + geom_point(aes(colour=Task))


# number of good
ranked <- base[, cbind(.SD[order(-MaxFitness), .(MaxFitness)],Rank=1:nrow(.SD)), by=.(Task,Repo,Job)]
ranked.stat <- ranked[, .(Mean=mean(MaxFitness),SE=se(MaxFitness), .N), by=.(Task,Repo,Rank)]
ranked.stat <- ranked.stat[N==length(unique(base$Job))]
ggplot(ranked.stat, aes(Rank,Mean,group=Repo)) + geom_line(aes(colour=Repo)) + 
  geom_ribbon(aes(fill=Repo, ymin=Mean-SE, ymax=Mean+SE),alpha=.2) + facet_wrap(~ Task, scales="free")

ranked.stat <- ranked[, .(Mean=mean(MaxFitness), .N), by=.(Task,Repo,Rank)]
ranked.stat <- ranked.stat[N==length(unique(base$Job))]
ranked.stat[, Mean := (Mean-min(Mean))/(max(Mean)-min(Mean)), by=.(Task)]
ggplot(ranked.stat, aes(Rank,Mean,group=Task,colour=Task)) + geom_line() + labs(x="Behaviour rank by fitness", y="Scaled fitness")
ggsave("~/Dropbox/Work/Papers/18-ANTS/density.pdf", width=4, height=3)



# Continuity quantification -- spearman correlation between behaviour distance and fitness distance
continuity <- function(data, vars) {
  behav.d <- as.matrix(dist(data[,vars,with=F]))
  behav.d <- behav.d[lower.tri(behav.d)]
  fitness.d <- as.matrix(dist(data[,"MaxFitness",with=F]))  
  fitness.d <- fitness.d[lower.tri(fitness.d)]
  return(cor(behav.d, fitness.d, method="spearman"))
}
cont <- merged[, continuity(.SD,vars=paste0("Behav_",0:15)), by=.(Repo,Job,Task)]
ggplot(cont[Repo=="sdbc"], aes(Task,V1)) + geom_boxplot() + labs(y="Spearman's r") +
  ggtitle("Correlation between fitness distance and behaviour distance") + ylim(0,1)

# Fitness discontinuity
distanceToNN <- function(rep, k=10) {
  require(FastKNN)
  dists <- as.matrix(dist(rep[,grep("Behav",colnames(rep)),with=F]))
  aux <- function(row) {
    nearest <- k.nearest.neighbors(row,dists,k=k)
    #return(mean(dists[row,nearest])) # this yields the distance in behaviour space, not very useful
    return(mean(abs(rep$MaxFitness[row]-rep$MaxFitness[nearest])))
  }
  return(sapply(1:nrow(rep),aux))
}
base[, NNDistance := distanceToNN(.SD,k=1), by=.(Task,Repo,Job)]

ggplot(base[, .(NNDistance=mean(NNDistance)), by=.(Task,Bin1,Bin2)], aes(Bin1,Bin2,fill=NNDistance)) + 
  scale_x_continuous(expand=c(0,0)) + scale_y_continuous(expand=c(0,0)) + labs(x="PC1", y="PC2", fill="Fitness discontinuity") +
  geom_tile() + facet_wrap(~ Task) + scale_fill_distiller(palette="Spectral") + coord_fixed()



# repertoire history movie
h <- fread("~/exps/swarm/repfinal/qdfitlong25/job.1.collectorhist.stat")
h <- reduceData(h, vars=bc, method="rpca")

genplots <- lapply(c(0,100,250,500,750,999), genplot, data=h, outdir=NULL, changes=F)
plot_grid(plotlist=genplots)

genplot <- function(gen, data, outdir=".", minfitness=0.9, changes=T) {
  currentcol <- unique(data[Generation <= gen], by=paste0("Behav_",0:15),fromLast=T)[Direction=="in" | Direction=="new"] 
  #currentcol[, Fitness := pmax(Fitness,minfitness)]
  newgen <- data[Generation==gen & Direction=="new"]
  inout <- cbind(data[Generation==gen & Direction=="out",.(Out1=V1,Out2=V2)],data[Generation==gen & Direction=="in",.(In1=V1,In2=V2,Fitness)])
  g <- ggplot(currentcol, aes(V1,V2,color=Fitness)) + geom_point() + 
    xlim(range(data$V1)) + ylim(range(data$V2)) + labs(x="PC1", y="PC2") +
    ggtitle(paste0("Generation: ",gen, " Size: ", nrow(currentcol))) +
    scale_color_distiller(palette="Spectral",limits=c(minfitness,max(data$Fitness))) + coord_fixed()
  if(changes) {
    g <- g + geom_segment(data=inout,aes(x=Out1,y=Out2,xend=In1,yend=In2),arrow=arrow(type="closed",length=unit(4,"pt")),colour="black") +
      geom_point(data=newgen,shape=8,size=4,colour="black") 
  }  
  if(!is.null(outdir)) {
    ggsave(g, file=file.path(outdir,paste0("gen_",gen,".png")))
  } else {
    return(g)
  }
}
collection.history <- function(data, outfile) {
  createCluster()
  dir <- tempdir() # temp folder for the plots
  parSapply(cl=NULL, 0:max(h$Generation), genplot, data=h, outdir=dir)
  system(paste0("ffmpeg -r 5 -i ",dir,"/gen_%d.png -vcodec libx264 ",outfile))
  unlink(dir,recursive=T) # cleanup the temp folder
}
collection.history(h,"~/Dropbox/qd25.mp4")


# Repo fitness viz
ggplot(repos[Repo=="sdbc"], aes(V1,V2,colour=Fitness)) + geom_point() + facet_wrap(~ Job)


ggplot(fitness[, .(Mean=mean(BestSoFar),SE=se(BestSoFar)), by=.(Task,Generation)], aes(Generation,Mean)) + 
  geom_line() + ylab("Fitness") + geom_ribbon(aes(ymax=Mean+SE, ymin=Mean-SE), alpha = 0.1) + facet_wrap(~ Task, scales="free")

ggplot(bests, aes(Repo,Fitness)) + geom_point(aes(colour=Repo)) + facet_wrap(~ Task, scales="free")

tr <- lastGen(fitness)[, .(Fitness=BestSoFar,Repo="TR"), by=.(Task,Job)]
baseline <- repofit[Repo=="randmlp", .(Fitness=MaxFitness,Repo="Baseline"), by=.(Task,Job)]
comp <- rbind(bests,tr,baseline)
ggplot(comp, aes(Repo,Fitness)) + geom_boxplot(aes(colour=Repo)) + facet_wrap(~ Task, scales="free")


# Correlation between fitness (behaviour invariance) and fitness in tasks
cors <- merged[Repo=="sdbc", mean(cor(MaxFitness, Fitness)), by=.(Task)]



cols <- loadData(c("rep2/nscol2","rep2/nslccol2","rep2/ns1col2","rep2/ns1lccol2"), filename="collection.stat", auto.ids.names=c("Approach","Repo"))
cols <- reduceData(cols, vars=paste0("Behav_",0:15), method="rpca")
plotReduced2D(cols, color.var="Fitness") + facet_grid(Job ~ Repo)

reps[, .(MeanFit=mean(Fitness)), by=.(Repo,Job)]


