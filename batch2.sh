#!/bin/bash

./voyager.sh -out /home/jorge/exps/EC/pred/nsga_p2v10 -p jobs=30 -p parent.0=build/classes/mase/app/pred/oneprey.params -p parent.1=build/classes/mase/novelty/mo/nsga_ga.params -p parent.2=build/classes/mase/cooperative.params -p parent.3=build/classes/mase/gax.params -p pop.default-subpop.size=75 -p generations=500 -p pop.subpops=2 -p problem.n-predators=2 -p problem.escape-distance=10

./voyager.sh -out /home/jorge/exps/EC/pred/nsga_p2v13 -p jobs=30 -p parent.0=build/classes/mase/app/pred/oneprey.params -p parent.1=build/classes/mase/novelty/mo/nsga_ga.params -p parent.2=build/classes/mase/cooperative.params -p parent.3=build/classes/mase/gax.params -p pop.default-subpop.size=75 -p generations=500 -p pop.subpops=2 -p problem.n-predators=2 -p problem.escape-distance=13

./voyager.sh -out /home/jorge/exps/EC/pred/nsga_p5v4 -p jobs=30 -p parent.0=build/classes/mase/app/pred/oneprey.params -p parent.1=build/classes/mase/novelty/mo/nsga_ga.params -p parent.2=build/classes/mase/cooperative.params -p parent.3=build/classes/mase/gax.params -p pop.default-subpop.size=75 -p generations=500 -p pop.subpops=5 -p problem.n-predators=5 -p problem.escape-distance=4

./voyager.sh -out /home/jorge/exps/EC/pred/nsga_p5v13 -p jobs=30 -p parent.0=build/classes/mase/app/pred/oneprey.params -p parent.1=build/classes/mase/novelty/mo/nsga_ga.params -p parent.2=build/classes/mase/cooperative.params -p parent.3=build/classes/mase/gax.params -p pop.default-subpop.size=75 -p generations=500 -p pop.subpops=5 -p problem.n-predators=5 -p problem.escape-distance=13

./voyager.sh -out /home/jorge/exps/EC/pred/nsga_p7v4 -p jobs=30 -p parent.0=build/classes/mase/app/pred/oneprey.params -p parent.1=build/classes/mase/novelty/mo/nsga_ga.params -p parent.2=build/classes/mase/cooperative.params -p parent.3=build/classes/mase/gax.params -p pop.default-subpop.size=75 -p generations=500 -p pop.subpops=7 -p problem.n-predators=7 -p problem.escape-distance=4

./voyager.sh -out /home/jorge/exps/EC/pred/nsga_p7v10 -p jobs=30 -p parent.0=build/classes/mase/app/pred/oneprey.params -p parent.1=build/classes/mase/novelty/mo/nsga_ga.params -p parent.2=build/classes/mase/cooperative.params -p parent.3=build/classes/mase/gax.params -p pop.default-subpop.size=75 -p generations=500 -p pop.subpops=7 -p problem.n-predators=7 -p problem.escape-distance=10

