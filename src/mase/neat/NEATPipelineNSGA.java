/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.neat;

import ec.EvolutionState;
import ec.Individual;
import java.util.Arrays;
import java.util.Comparator;

/**
 * This requires the use of NSGA2 PostEvaluator!!! neat.COMPATABILITY.THRESHOLD
 * = 99999999 neat.COMPATABILITY.CHANGE = 0 neat.SPECIE.COUNT = 1
 * neat.SURVIVAL.THRESHOLD = 1 The population size should be halved The "copy
 * best" in NEATSpecies produceOffspring method must be removed
 *
 *
 * @author jorge
 */
public class NEATPipelineNSGA extends NEATPipeline {

    private int[] originalPopSizes;

    @Override
    public int produce(int min, int max, int start, int subpopulation, Individual[] inds, EvolutionState state, int thread) {
        // save the original population sizes
        if (originalPopSizes == null) {
            originalPopSizes = new int[state.population.subpops.length];
            for (int i = 0; i < originalPopSizes.length; i++) {
                originalPopSizes[i] = state.population.subpops[i].individuals.length;
            }
        }

        // Build archive P -- select the best half of each population
        Individual[] subInds = state.population.subpops[subpopulation].individuals;
        if (state.generation > 0) {
            // Sort the population individuals in descending order
            Arrays.sort(subInds, new Comparator<Individual>() {

                @Override
                public int compare(Individual o1, Individual o2) {
                    return Double.compare(o2.fitness.fitness(), o1.fitness.fitness());
                }

            });
            // Reduce the population to the best half
            state.population.subpops[subpopulation].individuals = Arrays.copyOf(subInds, originalPopSizes[subpopulation]);
        }
        
        // Save this best half
        for (int j = 0; j < originalPopSizes[subpopulation]; j++) {
            inds[j] = (Individual) state.population.subpops[subpopulation].individuals[j].clone();
        }
        
        // Breed the populations (now composed only by the best half)
        // The result is placed next to the "archive" half, saved above
        int n = super.produce(0, 0, originalPopSizes[subpopulation], subpopulation, inds, state, thread);
        if(n != originalPopSizes[subpopulation]) {
            state.output.fatal("Unexpected number of individuals produced. Expected: " + originalPopSizes[subpopulation] + " Got: " + n);
        }
        return originalPopSizes[subpopulation] * 2;
    }
}
