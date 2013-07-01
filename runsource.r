ECJ_HOME <- "/home/jorge/Dropbox/MASE"
CP <- paste0("\"",ECJ_HOME, "/build/classes:",ECJ_HOME,"/lib/*\"")
OUTPUT_BASE <- "/home/jorge/Dropbox/ECJ/experiments"

paramsToString <- function(params) {
    paramString <- ""
    for(k in names(params)) {
        paramString <- paste0(paramString, "-p ", k, "=", params[[k]], " ")
    }
    return(paramString)
}

callEvolve <- function(file, params=c()) {
    paramString <- paramsToString(params)
    command <- paste("java -cp", CP, "ec.Evolve", "-file", file, paramString)
    cat(command,"\n")
    system(command)
}

defaultCall <- function(file=NULL, outBase=OUTPUT_BASE, out=NULL, params=c()) {
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
    
    # set paths for statistics
    par <- c()
    con <- file(file, "rt")
    lines <- readLines(con)
    close(con)
    for(l in lines) {
        if(substr(l,1,1) != "#" & grepl("\\$",l)) {
            split <- strsplit(l, "=")[[1]]
            trim <- function (x) gsub("^\\s+|\\s+$", "", x)
            key <- trim(split[1])
            val <- gsub("\\$","",trim(split[2]))
            val <- file.path(out, val)    
            par[[key]] <- val
            cat(key,val,"\n")
        }
    }
    for(p in names(params)) {
        if(grepl("\\$",params[[p]])) {
            params[[p]] <- file.path(out, gsub("\\$","",params[[p]]))   
            cat(p,params[[p]],"\n")
        }
    }
    
    par <- c(par, params)
    file.copy(file, file.path(out, "config.params"))
    extra <- file(file.path(out, "config.extra"))
    writeLines(paramsToString(par), extra)
    close(extra)
    callEvolve(file, par)
}

runCommandLine <- function(args) {
    file <- NULL
    out <- NULL
    params <- c()
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
    defaultCall(file=file, out=out, params=params)
}
