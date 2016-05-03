library(ggplot2)
library(plyr)
library(data.table)
theme_set(theme_bw())
theme_update(plot.margin=unit(c(1,1,1,1),"mm"), legend.position="bottom")

### INTRUDER DETECTION EVOROBOT #########

video <- function(log, outfolder=NULL, area=data.frame(x=c(),y=c()), points=data.frame(x=c(),y=c()), pathframes=300, finalframes=60, parallel=F) {
  dir.create(outfolder)
  
  # re-center in area
  xshift <- (max(area$x) + min(area$x)) / 2
  yshift <- (max(area$y) + min(area$y)) / 2
  area$x <- area$x - xshift
  area$y <- area$y - yshift    
  log$x <- log$x - xshift
  log$y <- log$y - yshift
  points$x <- points$x - xshift
  points$y <- points$y - yshift         
  
  xrange <- c(min(log$x)-2, max(log$x)+2)
  yrange <- c(min(log$y)-2, max(log$y)+2)
  steps <- c(unique(log$step), rep(max(log$step),finalframes))
  aux <- function(snr) {
    s <- steps[snr]
    steplog <- subset(log, step <= s & step >= s - pathframes)
    pos <- unique(as.data.table(steplog), by="robot",fromLast=T)
    g <- ggplot(pos, aes(x, y, colour=Type)) + 
      geom_polygon(data=area, linetype="dashed", colour="black", fill=NA) + # area
      geom_point(data=points, colour="black", size=4, shape=15) + # points
      geom_point(size=0.75) + # robot positions
      geom_path(aes(group=robot,alpha=step),data=steplog) + # robot trace
      scale_alpha(range = c(0, 0.5)) +
      xlab(NULL) + ylab(NULL) + guides(colour=FALSE,shape=FALSE,alpha=FALSE) +
      coord_fixed(ratio=1, xlim=xrange, ylim=yrange) +
      ggtitle(paste(formatC(s/10, digits=1, format="f", width=5),"s"))
    ggsave(paste0(outfolder,"/",snr,".png"), plot=g, width=4, height=4)
    return(NULL)
  }
  llply(1:length(steps), aux, .parallel=parallel)
  return(NULL)
}

logs <- read.table("~/Dropbox/Work/Papers/EvoRobot/hybrid_logs.csv",header=F,sep=" ",col.names=c("Sample","step","robot","x","y","orientation"))
logs$Type <- "robot"
logs$Type[logs$robot==5] <- "intruder"
logs$Type <- factor(logs$Type)
logs$Sample <- factor(logs$Sample, labels=c(0,1,2))

area0 <- data.frame(x=c(-42.66098885447718,57.33901114546461,57.33901114563923,-42.6609888545936), y=c(-26.660249043256044,-26.660249043256044,-126.66024905070662,-126.66024905070662))
base0 <- data.frame(x=7.339011145581026, y=-6.660469439812005)
video(subset(logs,Sample==0), "/home/jorge/traces_temp/test", area0, base0)

area1 <- data.frame(x=c(-58.21058127254946,41.78941872745054,41.78941872739233,-58.21058127260767),y=c(-23.302409977652133,-23.302409975789487,-123.30240998324007,-123.30240998417139))
base1 <- data.frame(x=-8.210581272374839, y=-3.302630371414125)
video(subset(logs,Sample==1), "/home/jorge/traces_temp/intruder_1", area1, base1)

area2 <- data.frame(x=c(-52.20697671256494,47.793023287376855,47.79302328743506,-52.20697671250673),y=c(-18.52322393655777,-18.523223937489092,-118.52322394400835,-118.5232239421457))
base2 <- data.frame(x=-2.206976712332107, y=1.4765556724742055)
video(subset(logs,Sample==2), "/home/jorge/traces_temp/intruder_2", area2, base2)

# ffmpeg -r 20 -i %d.png -y -b:v 10000k video.mp4
