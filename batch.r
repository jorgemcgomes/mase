#!/usr/bin/Rscript

source("runsource.r")

defaultCall(file="~/Dropbox/MASE/src/mase/generic/oneprey_gen_nov.params", out=paste0("op_gen_nov"), 
                params=c(jobs=10))

defaultCall(file="~/Dropbox/MASE/src/mase/generic/oneprey_gen_nov.params", out=paste0("op_gen_ts"), 
            params=c(jobs=10, "fitness.novelty-index"=3))

defaultCall(file="~/Dropbox/MASE/src/mase/generic/oneprey_gen_fit.params", out=paste0("op_gen_fit"), 
            params=c(jobs=10))