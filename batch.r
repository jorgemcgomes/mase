#!/usr/bin/Rscript

source("runsource.r")
OUTPUT_BASE <- "/home/jorge/exps"
    
runCommandLine("-out kw_cl5_neat -p jobs=10 -p parent.0=src/mase/app/keepaway/keepaway.params -p parent.1=src/mase/generic/clustergen.params -p parent.2=src/mase/neat/neat.params -p pop.default-subpop.size=300 -p generations=500 -p problem.number-evals=2 -p problem.eval.1=mase.generic.SCEvaluator -p fitness.novelty-index=1 -p neat.INPUT.NODES=9 -p neat.OUTPUT.NODES=4 -p eval.post.2.ns-blend=0.5 -p statecount.k-clusters=5")

runCommandLine("-out kw_cl10_neat -p jobs=10 -p parent.0=src/mase/app/keepaway/keepaway.params -p parent.1=src/mase/generic/clustergen.params -p parent.2=src/mase/neat/neat.params -p pop.default-subpop.size=300 -p generations=500 -p problem.number-evals=2 -p problem.eval.1=mase.generic.SCEvaluator -p fitness.novelty-index=1 -p neat.INPUT.NODES=9 -p neat.OUTPUT.NODES=4 -p eval.post.2.ns-blend=0.5 -p statecount.k-clusters=10")

runCommandLine("-out kw_cl20_neat -p jobs=10 -p parent.0=src/mase/app/keepaway/keepaway.params -p parent.1=src/mase/generic/clustergen.params -p parent.2=src/mase/neat/neat.params -p pop.default-subpop.size=300 -p generations=500 -p problem.number-evals=2 -p problem.eval.1=mase.generic.SCEvaluator -p fitness.novelty-index=1 -p neat.INPUT.NODES=9 -p neat.OUTPUT.NODES=4 -p eval.post.2.ns-blend=0.5 -p statecount.k-clusters=20")

runCommandLine("-out kw_cl50_neat -p jobs=10 -p parent.0=src/mase/app/keepaway/keepaway.params -p parent.1=src/mase/generic/clustergen.params -p parent.2=src/mase/neat/neat.params -p pop.default-subpop.size=300 -p generations=500 -p problem.number-evals=2 -p problem.eval.1=mase.generic.SCEvaluator -p fitness.novelty-index=1 -p neat.INPUT.NODES=9 -p neat.OUTPUT.NODES=4 -p eval.post.2.ns-blend=0.5 -p statecount.k-clusters=50")

runCommandLine("-out kw_cl100_neat -p jobs=10 -p parent.0=src/mase/app/keepaway/keepaway.params -p parent.1=src/mase/generic/clustergen.params -p parent.2=src/mase/neat/neat.params -p pop.default-subpop.size=300 -p generations=500 -p problem.number-evals=2 -p problem.eval.1=mase.generic.SCEvaluator -p fitness.novelty-index=1 -p neat.INPUT.NODES=9 -p neat.OUTPUT.NODES=4 -p eval.post.2.ns-blend=0.5 -p statecount.k-clusters=100")

runCommandLine("-out kw_cl500_neat -p jobs=10 -p parent.0=src/mase/app/keepaway/keepaway.params -p parent.1=src/mase/generic/clustergen.params -p parent.2=src/mase/neat/neat.params -p pop.default-subpop.size=300 -p generations=500 -p problem.number-evals=2 -p problem.eval.1=mase.generic.SCEvaluator -p fitness.novelty-index=1 -p neat.INPUT.NODES=9 -p neat.OUTPUT.NODES=4 -p eval.post.2.ns-blend=0.5 -p statecount.k-clusters=500")