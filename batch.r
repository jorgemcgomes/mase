#!/usr/bin/Rscript

source("runsource.r")

runCommandLine("-out pred5m_nov50_indshare -p parent.0=src/mase/app/pred/oneprey.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=5 -p problem.n-predators=5 -p problem.escape-distance=7 -p jobs=10 -p generations=500 -p eval.post.1.ns-blend=0.5 -p eval.post.0.ns-archive-mode=shared -p fitness.novelty-index=2")
runCommandLine("-out pred5m_nov50_indmult -p parent.0=src/mase/app/pred/oneprey.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=5 -p problem.n-predators=5 -p problem.escape-distance=7 -p jobs=10 -p generations=500 -p eval.post.1.ns-blend=0.5 -p eval.post.0.ns-archive-mode=multiple -p fitness.novelty-index=2")

runCommandLine("-out k3_ph_hard_nov50_indshare -p parent.0=src/mase/app/keepaway/keepaway_ph.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p generations=1000 -p eval.post.1.ns-blend=0.5 -p problem.taker-speed=0.7 -p fitness.novelty-index=2 -p eval.post.0.ns-archive-mode=shared")
runCommandLine("-out k3_ph_hard_nov50_indmult -p parent.0=src/mase/app/keepaway/keepaway_ph.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=3 -p jobs=10 -p generations=1000 -p eval.post.1.ns-blend=0.5 -p problem.taker-speed=0.7 -p fitness.novelty-index=2 -p eval.post.0.ns-archive-mode=multiple")