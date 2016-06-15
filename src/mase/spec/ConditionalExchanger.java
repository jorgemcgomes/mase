/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.spec;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.Subpopulation;
import ec.util.Parameter;
import java.util.Arrays;

/**
 * Only import individuals from similar populations (distance bellow a
 * threshold) In case there are multiple similar, divide the space equally
 *
 * @author jorge
 */
public class ConditionalExchanger extends BiasedRandomExchanger {

    private static final long serialVersionUID = 1L;
    public static final String P_SIMILARITY_THRESHOLD = "similarity-threshold";
    private double similarityThreshold;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.similarityThreshold = state.parameters.getDouble(base.push(P_SIMILARITY_THRESHOLD), null);
    }

    @Override
    public Population postBreedingExchangePopulation(EvolutionState state) {
        double[][] dists = computeDistances(state);
        Individual[][] old = new Individual[state.population.subpops.length][];
        for(int i = 0 ; i < state.population.subpops.length ; i++) {
            old[i] = Arrays.copyOf(state.population.subpops[i].individuals, state.population.subpops[i].individuals.length);
        }

        for (int i = 0; i < state.population.subpops.length; i++) {
            Subpopulation sub = state.population.subpops[i];
            int count = 0;
            for (int j = 0; j < dists[i].length; j++) {
                if (i != j && dists[i][j] < similarityThreshold) {
                    count++;
                }
            }
            if (count > 0) {
                int each = (int) (sub.individuals.length * foreignProportion / count);
                int index = 0;
                for (int j = 0; j < dists[i].length; j++) {
                    if (i != j && dists[i][j] < similarityThreshold) {
                        for(int k = 0 ; k < each ; k++) {
                            // pick individual
                            Individual ind = old[j][state.random[0].nextInt(old[j].length)];
                            // replace from the beginning -- the elites are copied to the end!
                            sub.individuals[index++] = (Individual) ind.clone();                            
                        }
                    }
                }
            }
        }
        return state.population;
    }

}
