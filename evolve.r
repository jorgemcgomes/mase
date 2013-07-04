#!/usr/bin/Rscript

source("runsource.r")
cmdArgs <- commandArgs(TRUE)
call <- paste(c("./evolve.r",cmdArgs), collapse=" ")
runCommandLine(cmdArgs, call)