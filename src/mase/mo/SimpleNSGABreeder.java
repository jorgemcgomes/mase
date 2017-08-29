/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mo;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.simple.SimpleBreeder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author jorge
 */
public class SimpleNSGABreeder extends SimpleBreeder {
    private static final long serialVersionUID = 1L;

    private int[] originalPopSizes;

    @Override
    public Population breedPopulation(EvolutionState state) {
        // save the original populatio sizes
        if (originalPopSizes == null) {
            originalPopSizes = new int[state.population.subpops.size()];
            for (int i = 0; i < originalPopSizes.length; i++) {
                originalPopSizes[i] = state.population.subpops.get(i).individuals.size();
            }
        }

        // Build archive P -- select the best half of each population
        List<Individual>[] archives = new List[state.population.subpops.size()];
        for (int i = 0; i < state.population.subpops.size(); i++) {
            ArrayList<Individual> subInds = state.population.subpops.get(i).individuals;
            if (state.generation > 0) {
                // Sort the population individuals in descending order
                Collections.sort(subInds, new Comparator<Individual>() {

                    @Override
                    public int compare(Individual o1, Individual o2) {
                        return Double.compare(o2.fitness.fitness(), o1.fitness.fitness());
                    }

                });
                // Reduce the population to the best half
                state.population.subpops.get(i).individuals = new ArrayList<>(subInds.subList(0, originalPopSizes[i]));
            }
            // Copy this best half to the archive
            archives[i] = new ArrayList(originalPopSizes[i]);
            for (int j = 0; j < originalPopSizes[i]; j++) {
                archives[i].add((Individual) state.population.subpops.get(i).individuals.get(j).clone());
            }
        }

        // Breed the populations (now composed only by the best half)
        Population pop = super.breedPopulation(state);

        // Join the recently bred population with the archive
        for (int i = 0; i < pop.subpops.size(); i++) {
            pop.subpops.get(i).individuals.addAll(archives[i]);
        }

        return pop;
    }
}
