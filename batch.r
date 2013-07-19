#!/usr/bin/Rscript

source("runsource.r")

runCommandLine("./evolve.r -out k3_ph_med_nov50 -p parent.0=src/mase/app/keepaway/keepaway_ph.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=3 -p jobs=5 -p eval.post.1.ns-blend=0.7 -p problem.taker-speed=0.5")