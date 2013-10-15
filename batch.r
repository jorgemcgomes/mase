#!/usr/bin/Rscript

source("runsource.r")
OUTPUT_BASE <- "/home/jorge/exps"

runCommandLine("-out generic/aggregation/ts_ls -p jobs=5 -p parent.0=src/mase/app/aggregation/aggregation.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/neat/neat.params -p neat.INPUT.NODES=9 -p neat.OUTPUT.NODES=3 -p pop.default-subpop.size=200 -p generations=250")

runCommandLine("-out generic/aggregation/fit -p jobs=5 -p parent.0=src/mase/app/aggregation/aggregation.params -p parent.1=src/mase/neat/neat.params -p neat.INPUT.NODES=9 -p neat.OUTPUT.NODES=3 -p pop.default-subpop.size=200 -p generations=250")
    
runCommandLine("-out generic/aggregation/cl_w_ls -p jobs=5 -p parent.0=src/mase/app/aggregation/aggregation.params -p parent.1=src/mase/generic/clusterweighted.params -p parent.2=src/mase/neat/neat.params -p neat.INPUT.NODES=9 -p neat.OUTPUT.NODES=3 -p pop.default-subpop.size=200 -p generations=250 -p problem.number-evals=3 -p problem.eval.2=mase.generic.SCEvaluator -p fitness.novelty-index=2 -p statecount.k-clusters=50")    
