#!/bin/bash

./evolve.sh -out /home/jorge/exps/pred/fit_p2v10 -p jobs=30 -p parent.0=build/classes/mase/app/pred/oneprey.params -p parent.1=build/classes/mase/cooperative.params -p parent.2=build/classes/mase/gax.params -p pop.default-subpop.size=150 -p generations=500 -p pop.subpops=2 -p problem.escape-distance=10 -p problem.n-predators=2 

./evolve.sh -out /home/jorge/exps/pred/fit_p2v13 -p jobs=30 -p parent.0=build/classes/mase/app/pred/oneprey.params -p parent.1=build/classes/mase/cooperative.params -p parent.2=build/classes/mase/gax.params -p pop.default-subpop.size=150 -p generations=500 -p pop.subpops=2 -p problem.escape-distance=13 -p problem.n-predators=2 

./evolve.sh -out /home/jorge/exps/pred/fit_p5v4 -p jobs=30 -p parent.0=build/classes/mase/app/pred/oneprey.params -p parent.1=build/classes/mase/cooperative.params -p parent.2=build/classes/mase/gax.params -p pop.default-subpop.size=150 -p generations=500 -p pop.subpops=5 -p problem.escape-distance=4 -p problem.n-predators=5

./evolve.sh -out /home/jorge/exps/pred/fit_p5v13 -p jobs=30 -p parent.0=build/classes/mase/app/pred/oneprey.params -p parent.1=build/classes/mase/cooperative.params -p parent.2=build/classes/mase/gax.params -p pop.default-subpop.size=150 -p generations=500 -p pop.subpops=5 -p problem.escape-distance=13 -p problem.n-predators=5

./evolve.sh -out /home/jorge/exps/pred/fit_p7v4 -p jobs=30 -p parent.0=build/classes/mase/app/pred/oneprey.params -p parent.1=build/classes/mase/cooperative.params -p parent.2=build/classes/mase/gax.params -p pop.default-subpop.size=150 -p generations=500 -p pop.subpops=7 -p problem.escape-distance=4 -p problem.n-predators=7

./evolve.sh -out /home/jorge/exps/pred/fit_p7v10 -p jobs=30 -p parent.0=build/classes/mase/app/pred/oneprey.params -p parent.1=build/classes/mase/cooperative.params -p parent.2=build/classes/mase/gax.params -p pop.default-subpop.size=150 -p generations=500 -p pop.subpops=7 -p problem.escape-distance=10 -p problem.n-predators=7

