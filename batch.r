#!/usr/bin/Rscript

source("runsource.r")

runCommandLine("-out kw_phnew_nov50 -p parent.0=src/mase/app/keepaway/keepaway_ph2.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p eval.post.1.ns-blend=0.5 -p generations=1000")

runCommandLine("-out kw_phnew_fit -p parent.0=src/mase/app/keepaway/keepaway_ph2.params -p parent.1=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p generations=1000")

runCommandLine("-out kw_phnew_nov75 -p parent.0=src/mase/app/keepaway/keepaway_ph2.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p eval.post.1.ns-blend=0.75 -p generations=1000")