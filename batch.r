#!/usr/bin/Rscript

source("runsource.r")
OUTPUT_BASE <- "/home/jorge/exps"

runCommandLine("-out sysf/pred/ga_cl50 -p jobs=10 -p parent.0=src/mase/app/pred/oneprey_homo.params -p parent.1=src/mase/generic/clusterweighted_nsga.params -p parent.2=src/mase/generic/systematic/sysnov.params -p parent.2=src/mase/ga.params -p pop.default-subpop.size=200 -p generations=750 -p eval.post.2=mase.novelty.NSGA2 -p problem.number-evals=3 -p problem.eval.2=mase.generic.SCEvaluator -p fitness.novelty-index=2")

runCommandLine("-out sysf/ind/ga_cl50 -p jobs=10 -p parent.0=src/mase/app/indiana/indiana.params -p parent.1=src/mase/generic/clusterweighted_nsga.params -p parent.2=src/mase/generic/systematic/sysnov.params -p parent.2=src/mase/ga.params -p pop.default-subpop.size=200 -p generations=500 -p eval.post.2=mase.novelty.NSGA2 -p problem.number-evals=3 -p problem.eval.2=mase.generic.SCEvaluator -p fitness.novelty-index=2")

