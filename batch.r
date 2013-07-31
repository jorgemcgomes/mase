#!/usr/bin/Rscript

source("runsource.r")
OUTPUT_BASE <- "/home/jorge/Dropbox/exps_AAMAS/"
    
runCommandLine("./evolve.r -out k3_ph_hard_fit -p jobs=20 -p current-job=10 -p parent.0=src/mase/app/keepaway/keepaway_ph.params -p parent.1=src/mase/coevolution.params -p pop.subpops=3 -p generations=1000 -p problem.taker-speed=0.7")
runCommandLine("./evolve.r -out k3_ph_hard_nov50 -p jobs=20 -p current-job=10 -p parent.0=src/mase/app/keepaway/keepaway_ph.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=3 -p generations=1000 -p eval.post.1.ns-blend=0.5 -p problem.taker-speed=0.7")
runCommandLine("./evolve.r -out k3_ph_hard_nov50_indmult -p jobs=20 -p current-job=10 -p parent.0=src/mase/app/keepaway/keepaway_ph.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=3 -p generations=1000 -p eval.post.1.ns-blend=0.5 -p problem.taker-speed=0.7 -p fitness.novelty-index=2 -p eval.post.0.ns-archive-mode=multiple")
runCommandLine("./evolve.r -out k3_ph_hard_nov50_indshare -p jobs=20 -p current-job=10 -p parent.0=src/mase/app/keepaway/keepaway_ph.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=3 -p generations=1000 -p eval.post.1.ns-blend=0.5 -p problem.taker-speed=0.7 -p fitness.novelty-index=2 -p eval.post.0.ns-archive-mode=shared")

runCommandLine("./evolve.r -out pred3_fit -p jobs=20 -p current-job=10 -p parent.0=src/mase/app/pred/oneprey.params -p parent.1=src/mase/coevolution.params -p pop.subpops=3 -p generations=500")
runCommandLine("./evolve.r -out pred3_nov50 -p jobs=20 -p current-job=10 -p parent.0=src/mase/app/pred/oneprey.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=3 -p generations=500 -p eval.post.1.ns-blend=0.5")
runCommandLine("./evolve.r -out pred3_nov50_indmult -p jobs=20 -p current-job=10 -p parent.0=src/mase/app/pred/oneprey.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=3 -p generations=500 -p eval.post.1.ns-blend=0.5 -p eval.post.0.ns-archive-mode=multiple -p fitness.novelty-index=2")
runCommandLine("./evolve.r -out pred3_nov50_indshare -p jobs=20 -p current-job=10 -p parent.0=src/mase/app/pred/oneprey.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/coevolution.params -p pop.subpops=3 -p generations=500 -p eval.post.1.ns-blend=0.5 -p eval.post.0.ns-archive-mode=shared -p fitness.novelty-index=2")