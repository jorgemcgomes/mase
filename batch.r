#!/usr/bin/Rscript

source("runsource.r")
OUTPUT_BASE <- "/home/jorge/exps"

runCommandLine("-out generic/keepaway/kw_cl50_wls_d4_idf -p jobs=1 -p parent.0=src/mase/app/keepaway/keepaway.params -p parent.1=src/mase/generic/clusterweighted.params -p parent.2=src/mase/neat/neat.params -p neat.INPUT.NODES=9 -p neat.OUTPUT.NODES=4 -p pop.default-subpop.size=300 -p generations=500 -p problem.number-evals=3 -p problem.eval.2=mase.generic.SCEvaluator -p fitness.novelty-index=2 -p statecount.k-clusters=50 -p statecount.discretisation=4 -p statecount.do-tf-idf=true -p statecount.do-filter=false  -p stat.child.15=mase.stat.PlaceHolder -p current-job=0")

