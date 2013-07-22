#!/usr/bin/Rscript

source("runsource.r")

runCommandLine("-out k3_ph_med_nov50_indmult -p parent.0=src/mase/app/keepaway/keepaway_ph.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p generations=1000 -p eval.post.1.ns-blend=0.5 -p problem.taker-speed=0.5 -p fitness.novelty-index=2 -p eval.post.0.ns-archive-mode=multiple")
runCommandLine("-out k3_ph_med_nov50_indshare -p parent.0=src/mase/app/keepaway/keepaway_ph.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p generations=1000 -p eval.post.1.ns-blend=0.5 -p problem.taker-speed=0.5 -p fitness.novelty-index=2 -p eval.post.0.ns-archive-mode=shared")
runCommandLine("-out k3_ph_med_nov50_indnone -p parent.0=src/mase/app/keepaway/keepaway_ph.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p generations=1000 -p eval.post.1.ns-blend=0.5 -p problem.taker-speed=0.5 -p fitness.novelty-index=2 -p eval.post.0.ns-archive-mode=none")


#runCommandLine("-out k3_hom_hard_fit -p parent.0=src/mase/app/keepaway/keepaway.params -p parent.1=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p generations=1000 -p problem.taker-speed=1 -p problem.keeper.move-speed=1")