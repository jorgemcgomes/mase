#!/bin/sh

./evolve.sh -out /home/jorge/exps/bench/single_fitness_nn -file build/classes/mase/app/pred/benchmark.params -file build/classes/mase/gax.params -p pop.subpops=1 -force

./evolve.sh -out /home/jorge/exps/bench/single_ns_nn -file build/classes/mase/app/pred/benchmark.params -file build/classes/mase/novelty/novelty.params -file build/classes/mase/gax.params -p pop.subpops=1 -force

./evolve.sh -out /home/jorge/exps/bench/single_nsmo_nn -file build/classes/mase/app/pred/benchmark.params -file build/classes/mase/novelty/comb/nsga_scores.params -file build/classes/mase/gax.params -p pop.subpops=1 -force

./evolve.sh -out /home/jorge/exps/bench/single_nsnsga_nn -file build/classes/mase/app/pred/benchmark.params -file build/classes/mase/novelty/comb/true_nsga.params -file build/classes/mase/gax.params -p pop.subpops=1 -p ec.subpop.size=100 -force


./evolve.sh -out /home/jorge/exps/bench/ccea_fitness_nn -file build/classes/mase/app/pred/benchmark.params -file build/classes/mase/cooperative.params -file build/classes/mase/gax.params -p pop.subpops=3 -force

./evolve.sh -out /home/jorge/exps/bench/ccea_ns_nn -file build/classes/mase/app/pred/benchmark.params -file build/classes/mase/cooperative.params -file build/classes/mase/novelty/novelty.params -file build/classes/mase/gax.params -p pop.subpops=3 -force

./evolve.sh -out /home/jorge/exps/bench/ccea_nsmo_nn -file build/classes/mase/app/pred/benchmark.params -file build/classes/mase/cooperative.params -file build/classes/mase/novelty/comb/nsga_scores.params -file build/classes/mase/gax.params -p pop.subpops=3 -force

./evolve.sh -out /home/jorge/exps/bench/ccea_nsnsga_nn -file build/classes/mase/app/pred/benchmark.params -file build/classes/mase/cooperative.params -file build/classes/mase/novelty/comb/true_nsga.params -file build/classes/mase/gax.params -p pop.subpops=3 -p ec.subpop.size=100 -force

./evolve.sh -out /home/jorge/exps/bench/hyb_fitness_nn -file build/classes/mase/app/pred/benchmark.params -file build/classes/mase/spec/hybrid.params -file build/classes/mase/gax.params -p pop.subpops=3 -p exch.behaviour-index=2 -force



./evolve.sh -out /home/jorge/exps/bench/single_fitness_neat -file build/classes/mase/app/pred/benchmark.params -file build/classes/mase/neat.params -p pop.subpops=1

./evolve.sh -out /home/jorge/exps/bench/ccea_fitness_nn -file build/classes/mase/app/pred/benchmark.params -file build/classes/mase/cooperative.params -file build/classes/mase/neat.params -p pop.subpops=3 -force

