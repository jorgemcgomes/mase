#!/usr/bin/Rscript

source("runsource.r")
OUTPUT_BASE <- "/home/jorge/exps"
    
runCommandLine("-out kw_ts_ga -p jobs=10 -p parent.0=src/mase/app/keepaway/keepaway.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/homogeneous.params -p pop.default-subpop.size=300 -p generations=500 -p eval.post.1.ns-blend=0.5 -p current-job=3")

runCommandLine("-out kw_ts_neat -p jobs=10 -p parent.0=src/mase/app/keepaway/keepaway.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/neat/neat.params -p neat.INPUT.NODES=9 -p neat.OUTPUT.NODES=4 -p pop.default-subpop.size=300 -p generations=500 -p eval.post.1.ns-blend=0.5 -p current-job=1")

runCommandLine("-out kw_fit_neat -p jobs=10 -p parent.0=src/mase/app/keepaway/keepaway.params -p parent.1=src/mase/neat/neat.params -p neat.INPUT.NODES=9 -p neat.OUTPUT.NODES=4 -p pop.default-subpop.size=300 -p generations=500 -p current-job=4")