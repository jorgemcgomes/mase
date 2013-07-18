#!/usr/bin/Rscript

source("runsource.r")

runCommandLine("-out kw_ph3_hard_fit -p parent.0=src/mase/app/keepaway/keepaway_ph.params -p parent.1=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p problem.taker-speed=1")

runCommandLine("-out kw_ph3_hard_nov50 -p parent.0=src/mase/app/keepaway/keepaway_ph.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p eval.post.1.ns-blend=0.5 -p problem.taker-speed=1")

runCommandLine("-out kw_ph5_fit -p parent.0=src/mase/app/keepaway/keepaway_ph.params -p parent.1=src/mase/coevolution.params -p pop.subpops=5 -p jobs=10 -p problem.n-keepers=5")