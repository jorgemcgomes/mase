/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.neat;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import java.util.Arrays;
import java.util.Comparator;

/**
 * This requires the use of NSGA2 PostEvaluator!!! 
 * neat.COMPATABILITY.THRESHOLD = 99999999
 * neat.COMPATABILITY.CHANGE = 0
 * neat.SPECIE.COUNT = 1
 * neat.SURVIVAL.THRESHOLD = 1
 * The population size should be halved
 * The "copy best" in NEATSpecies produceOffspring method must be removed
 * 
 *
 * @author jorge
 */
public class NEATBreederNSGA extends NEATBreeder {

    private int[] originalPopSizes;

    @Override
    public Population breedPopulation(EvolutionState state) {
        // save the original populatio sizes
        if (originalPopSizes == null) {
            originalPopSizes = new int[state.population.subpops.length];
            for (int i = 0; i < originalPopSizes.length; i++) {
                originalPopSizes[i] = state.population.subpops[i].individuals.length;
            }
        }

        // Build archive P -- select the best half of each population
        Individual[][] archives = new Individual[state.population.subpops.length][];
        for (int i = 0; i < state.population.subpops.length; i++) {
            Individual[] subInds = state.population.subpops[i].individuals;
            if (state.generation > 0) {
                // Sort the population individuals in descending order
                Arrays.sort(subInds, new Comparator<Individual>() {

                    @Override
                    public int compare(Individual o1, Individual o2) {
                        return Float.compare(o2.fitness.fitness(), o1.fitness.fitness());
                    }

                });
                // Reduce the population to the best half
                state.population.subpops[i].individuals = Arrays.copyOf(subInds, originalPopSizes[i]);
            }
            // Copy this best half to the archive
            archives[i] = new Individual[originalPopSizes[i]];
            for (int j = 0; j < archives[i].length; j++) {
                archives[i][j] = (Individual) state.population.subpops[i].individuals[j].clone();
            }
        }

        // Breed the populations (now composed only by the best half)
        Population pop = super.breedPopulation(state);

        // Join the recently bred population with the archive
        for (int i = 0; i < pop.subpops.length; i++) {
            Individual[] inds = Arrays.copyOf(pop.subpops[i].individuals, originalPopSizes[i] * 2);
            System.arraycopy(archives[i], 0, inds, originalPopSizes[i], archives[i].length);
            pop.subpops[i].individuals = inds;
        }

        return pop;
    }
}
