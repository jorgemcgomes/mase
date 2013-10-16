#!/usr/bin/Rscript

source("runsource.r")
OUTPUT_BASE <- "/home/jorge/exps"

runCommandLine("-out generic/sharing/rs_ls75 -p jobs=10 -p parent.0=src/mase/app/sharing/sharing.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/neat/neat.params -p neat.INPUT.NODES=8 -p neat.OUTPUT.NODES=3 -p pop.default-subpop.size=200 -p generations=300 -p eval.post.1.ns-blend=0.75")

runCommandLine("-out generic/sharing/rs_fit -p jobs=10 -p parent.0=src/mase/app/sharing/sharing.params -p parent.1=src/mase/neat/neat.params -p neat.INPUT.NODES=8 -p neat.OUTPUT.NODES=3 -p pop.default-subpop.size=200 -p generations=300")
    
runCommandLine("-out generic/sharing/rs_cl50_wls -p jobs=10 -p parent.0=src/mase/app/sharing/sharing.params -p parent.1=src/mase/generic/clusterweighted.params -p parent.2=src/mase/neat/neat.params -p neat.INPUT.NODES=8 -p neat.OUTPUT.NODES=3 -p pop.default-subpop.size=200 -p generations=300 -p problem.number-evals=3 -p problem.eval.2=mase.generic.SCEvaluator -p fitness.novelty-index=2 -p statecount.k-clusters=50")

runCommandLine("-out generic/sharing/rs_cl100_wls -p jobs=10 -p parent.0=src/mase/app/sharing/sharing.params -p parent.1=src/mase/generic/clusterweighted.params -p parent.2=src/mase/neat/neat.params -p neat.INPUT.NODES=8 -p neat.OUTPUT.NODES=3 -p pop.default-subpop.size=200 -p generations=300 -p problem.number-evals=3 -p problem.eval.2=mase.generic.SCEvaluator -p fitness.novelty-index=2 -p statecount.k-clusters=100")

runCommandLine("-out generic/sharing/rs_ls50 -p jobs=10 -p parent.0=src/mase/app/sharing/sharing.params -p parent.1=src/mase/novelty/linearscal.params -p parent.2=src/mase/neat/neat.params -p neat.INPUT.NODES=8 -p neat.OUTPUT.NODES=3 -p pop.default-subpop.size=200 -p generations=300 -p eval.post.1.ns-blend=0.5")

