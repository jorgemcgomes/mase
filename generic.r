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