#!/usr/bin/Rscript

source("runsource.r")
OUTPUT_BASE <- "/home/jorge/exps"

runCommandLine("-out competitive/pred2/fit_neat_rand -p jobs=10 -p parent.0=src/mase/app/predcomp/predcomp.params -p parent.1=src/mase/neat/neat.params -p generations=300 -p pop.subpop.0=mase.neat.NEATSubpop -p pop.subpop.1=mase.neat.NEATSubpop -p eval.base.num-random-champions=10")

runCommandLine("-out competitive/pred2/fit_neat_novlast -p jobs=10 -p parent.0=src/mase/app/predcomp/predcomp.params -p parent.1=src/mase/neat/neat.params -p generations=300 -p pop.subpop.0=mase.neat.NEATSubpop -p pop.subpop.1=mase.neat.NEATSubpop -p eval.base.num-novel-champions=10 -p neat.species.fitness=mase.novelty.NoveltyFitness -p eval.base.novel-champions-mode=last")

runCommandLine("-out competitive/pred2/fit_neat_novrand -p jobs=10 -p parent.0=src/mase/app/predcomp/predcomp.params -p parent.1=src/mase/neat/neat.params -p generations=300 -p pop.subpop.0=mase.neat.NEATSubpop -p pop.subpop.1=mase.neat.NEATSubpop -p eval.base.num-novel-champions=10 -p neat.species.fitness=mase.novelty.NoveltyFitness -p eval.base.novel-champions-mode=random")

runCommandLine("-out competitive/pred2/fit_neat_novcent -p jobs=10 -p parent.0=src/mase/app/predcomp/predcomp.params -p parent.1=src/mase/neat/neat.params -p generations=300 -p pop.subpop.0=mase.neat.NEATSubpop -p pop.subpop.1=mase.neat.NEATSubpop -p eval.base.num-novel-champions=10 -p neat.species.fitness=mase.novelty.NoveltyFitness -p eval.base.novel-champions-mode=centroid")

