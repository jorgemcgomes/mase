#!/usr/bin/Rscript

source("runsource.r")

runCommandLine("-out k3_ph_med_fit -p parent.0=src/mase/app/keepaway/keepaway_ph.params -p parent.1=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p generations=1000 -p problem.taker-speed=0.5")
runCommandLine("-out k3_ph_med_fit_mut10 -p parent.0=src/mase/app/keepaway/keepaway_ph.params -p parent.1=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p generations=500 -p problem.taker-speed=0.5 -p vector.species.mutation-prob=0.10")

runCommandLine("-out k3_hom_easy_nov50 -p parent.0=src/mase/app/keepaway/keepaway.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p generations=1000 -p eval.post.1.ns-blend=0.5 -p problem.taker-speed=0.5 -p problem.keeper.move-speed=1")
runCommandLine("-out k3_hom_med_nov50 -p parent.0=src/mase/app/keepaway/keepaway.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p generations=1000 -p eval.post.1.ns-blend=0.5 -p problem.taker-speed=0.75 -p problem.keeper.move-speed=1")
runCommandLine("-out k3_hom_hard_nov50 -p parent.0=src/mase/app/keepaway/keepaway.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p generations=1000 -p eval.post.1.ns-blend=0.5 -p problem.taker-speed=1 -p problem.keeper.move-speed=1")

runCommandLine("-out k3_hom_easy_fit -p parent.0=src/mase/app/keepaway/keepaway.params -p parent.1=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p generations=1000 -p problem.taker-speed=0.5 -p problem.keeper.move-speed=1")
runCommandLine("-out k3_hom_med_fit -p parent.0=src/mase/app/keepaway/keepaway.params -p parent.1=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p generations=1000 -p problem.taker-speed=0.75 -p problem.keeper.move-speed=1")
runCommandLine("-out k3_hom_hard_fit -p parent.0=src/mase/app/keepaway/keepaway.params -p parent.1=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p generations=1000 -p problem.taker-speed=1 -p problem.keeper.move-speed=1")