ECJ_HOME <- "/home/jorge/ToDrop/MASE/mase"
CP <- paste0("\"",ECJ_HOME, "/build/classes:",ECJ_HOME,"/lib/*\"")
OUTPUT_BASE <- "/home/jorge/ToDrop/experiments"

callEvolve <- function(file, extraString="") {
    command <- paste("java -cp", CP, "ec.Evolve", "-file", file, extraString)
    cat(command,"\n")
    system(command)
}

paramsToString <- function(params) {
    paramString <- ""
    for(k in names(params)) {
        paramString <- paste0(paramString, "-p ", k, "=", params[[k]], " ")
    }
    return(paramString)
}

# loads the parameters from the file
fileToParams <- function(file) {
    par <- list()
    con <- file(file, "rt")
    lines <- readLines(con)
    close(con)
    trim <- function (x) gsub("^\\s+|\\s+$", "", x)
    for(l in lines) {
        if(nchar(l) > 1 & substr(l,1,1) != "#") {
            split <- strsplit(l, "=")[[1]]
            key <- trim(split[1])
            val <- trim(split[2])
            par[[key]] <- val
        }
    }
    return(par)
}

# deals with the config file inheritance
loadParams <- function(par, dir) {
    # check parents
    for(i in 0:100) {
        pi <- paste0("parent.",i)
        if(pi %in% names(par)) {
            parent <- par[[pi]]
            parent <- normalizePath(file.path(dir, parent))
            print(parent)
            
            # load parent
            parentPar <- loadParams(fileToParams(parent), dirname(parent))
            par <- mergePar(parentPar, par)
            par[[pi]] <- NULL
        } else {
            break
        }
    }    
    return(par)
}

# join parameters of bottom and top, with the parameters of top replacing those on bottom
mergePar <- function(bottom, top) {
    for(k in names(top)) {
        bottom[k] <- top[k]
    }
    return(bottom)
}

defaultCall <- function(file=NULL, outBase=OUTPUT_BASE, out=NULL, params=list(), consoleCall=NULL) {
    # prepare output folder
    if(is.null(out)) {
        date <- Sys.time()
        form <- format(date, format="%y_%m_%d_%H_%M_%S")
        out <- file.path(outBase, form)
    } else {
        out <- file.path(outBase, out)
    }
    if(!file.exists(out)) {
        dir.create(out)
    }
    
    # load parameters
    par <- list()
    if(is.null(file)) {
        par <- loadParams(params, ".")
        params <- list()
    } else {
        par <- loadParams(fileToParams(file), dirname(file))
    }

    # correct stats paths
    for(k in names(par)) {
        if(grepl("\\$",par[k])) {
            val <- gsub("\\$","",par[k])
            val <- file.path(out, val)
            par[k] <- val
        }
    }
    
    # write config file
    lines <- c(paste("#",consoleCall))
    max <- 0
    for(k in c(names(par), names(params))) { #
        max = max(max, nchar(k))
    }   
    for(k in names(par)) {
        lines <- c(lines, paste(formatC(k, width=-max), "=", par[[k]]))
    }
    if(length(params) > 0) {
        lines <- c(lines, "# Command-line parameters")
        for(k in names(params)) {
            lines <- c(lines, paste(formatC(k, width=-max), "=", params[[k]]))
        }        
    }
    configPath <- file.path(out, "config.params")
    config <- file(configPath)
    writeLines(lines, config)
    close(config)
    
    # call evolve with the assembled config file
    callEvolve(configPath)
}

runCommandLine <- function(args, originalCall) {
    file <- NULL
    out <- NULL
    params <- list()
    for(i in 1:length(args)) {
        if(args[i] == "-file") {
            file <- args[i+1]
        } else if(args[i] == "-out") {
            out <- args[i+1]
        } else if(args[i] == "-p") {
            split <- strsplit(args[i+1], "=")[[1]]
            params[[split[1]]] <- split[2]
        }
    }
    defaultCall(file=file, out=out, params=params, consoleCall=originalCall)
}
