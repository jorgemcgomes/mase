#!/usr/bin/Rscript

source("runsource.r")

runCommandLine("-out kw_ph_mcn15 -p parent.0=src/mase/app/keepaway/keepaway_ph.params -p parent.1=src/mase/novelty/mcn.params -p parent.2=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p eval.post.1.novelty-threshold=1.5")
runCommandLine("-out kw_ph_nov50 -p parent.0=src/mase/app/keepaway/keepaway_ph.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p eval.post.1.ns-blend=0.5")

runCommandLine("-out kw_ph_hard_mcn10 -p parent.0=src/mase/app/keepaway/keepaway_ph.params -p parent.1=src/mase/novelty/mcn.params -p parent.2=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p eval.post.1.novelty-threshold=1.0 -p problem.taker-speed=1")
runCommandLine("-out kw_ph_hard_mcn15 -p parent.0=src/mase/app/keepaway/keepaway_ph.params -p parent.1=src/mase/novelty/mcn.params -p parent.2=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p eval.post.1.novelty-threshold=1.5 -p problem.taker-speed=1")
runCommandLine("-out kw_ph_hard_nov50 -p parent.0=src/mase/app/keepaway/keepaway_ph.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p problem.taker-speed=1 -p eval.post.1.ns-blend=0.5")