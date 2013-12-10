#!/usr/bin/Rscript

source("runsource.r")
cmdArgs <- commandArgs(TRUE)
app <- cmdArgs[1]
cmdArgs <- tail(cmdArgs, -1)
appCall(app, cmdArgs)