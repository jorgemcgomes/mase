#!/usr/bin/Rscript

source("runsource.r")
OUTPUT_BASE <- "/home/jorge/exps"

runCommandLine("-out generic/sharing/rs11_cl50_bal50 -p jobs=10 -p parent.0=src/mase/app/sharing/sharing_f.params -p parent.1=src/mase/generic/clusterweighted.params -p parent.2=src/mase/neat/neat.params -p eval.post.0=mase.generic.ClusterSCPostEval3 -p statecount.min-learning-rate=0.5 -p neat.INPUT.NODES=8 -p neat.OUTPUT.NODES=3 -p pop.default-subpop.size=200 -p generations=400 -p problem.number-evals=3 -p problem.eval.2=mase.generic.SCEvaluator -p fitness.novelty-index=2 -p statecount.k-clusters=50 -p statecount.discretisation=4 -p statecount.do-tf-idf=true -p statecount.do-filter=false -p stat.child.15=mase.stat.PlaceHolder")

runCommandLine("-out generic/sharing/rs11_cl50_bal5 -p jobs=10 -p parent.0=src/mase/app/sharing/sharing_f.params -p parent.1=src/mase/generic/clusterweighted.params -p parent.2=src/mase/neat/neat.params -p eval.post.0=mase.generic.ClusterSCPostEval3 -p statecount.min-learning-rate=0.05 -p neat.INPUT.NODES=8 -p neat.OUTPUT.NODES=3 -p pop.default-subpop.size=200 -p generations=400 -p problem.number-evals=3 -p problem.eval.2=mase.generic.SCEvaluator -p fitness.novelty-index=2 -p statecount.k-clusters=50 -p statecount.discretisation=4 -p statecount.do-tf-idf=true -p statecount.do-filter=false -p stat.child.15=mase.stat.PlaceHolder")