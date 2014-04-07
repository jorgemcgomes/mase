#!/usr/bin/Rscript

source("runsource.r")
OUTPUT_BASE <- "/home/jorge/exps"

runCommandLine("-out spec/pred5_2/fit_sp_030_05 -p jobs=10 -p parent.0=src/mase/app/pred/oneprey.params -p parent.1=src/mase/spec/spec2.params -p parent.2=src/mase/cooperative.params -p parent.3=src/mase/gax.params -p pop.default-subpop.size=100 -p generations=400 -p problem.eval.0=mase.app.pred.OnePreyFitness3 -p vector.species.fitness=mase.novelty.NoveltyFitness -p fitness.novelty-index=2 -p problem.n-predators=5 -p pop.subpops=5 -p problem.escape-distance=7 -p eval.base.num-current=0 -p eval.base.num-current-elite=1 -p problem.max-steps=400 -p exch.similarity-threshold=0.3 -p exch.elite-portion=0.05")

runCommandLine("-out spec/pred5_2/fit_sp_020_05 -p jobs=10 -p parent.0=src/mase/app/pred/oneprey.params -p parent.1=src/mase/spec/spec2.params -p parent.2=src/mase/cooperative.params -p parent.3=src/mase/gax.params -p pop.default-subpop.size=100 -p generations=400 -p problem.eval.0=mase.app.pred.OnePreyFitness3 -p vector.species.fitness=mase.novelty.NoveltyFitness -p fitness.novelty-index=2 -p problem.n-predators=5 -p pop.subpops=5 -p problem.escape-distance=7 -p eval.base.num-current=0 -p eval.base.num-current-elite=1 -p problem.max-steps=400 -p exch.similarity-threshold=0.2 -p exch.elite-portion=0.05")