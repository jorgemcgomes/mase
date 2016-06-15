/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.spec;

import ec.EvolutionState;
import ec.Exchanger;
import ec.Individual;
import ec.Population;
import ec.Subpopulation;
import ec.util.Parameter;
import java.util.Arrays;

/**
 * After breeding, randomly remove the foreign-proportion, and import individuals from other subpops
 * @author jorge
 */
public class RandomExchanger extends Exchanger {

    public static final String P_FOREIGN_PROPORTION = "foreign-proportion";
    private static final long serialVersionUID = 1L;
    protected double foreignProportion;
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        foreignProportion = state.parameters.getDouble(base.push(P_FOREIGN_PROPORTION), null);
    }    
    
    @Override
    public Population preBreedingExchangePopulation(EvolutionState state) {
        // nothing to do here
        return state.population;
    }

    @Override
    public Population postBreedingExchangePopulation(EvolutionState state) {
        Individual[][] old = new Individual[state.population.subpops.length][];
        for(int i = 0 ; i < state.population.subpops.length ; i++) {
            old[i] = Arrays.copyOf(state.population.subpops[i].individuals, state.population.subpops[i].individuals.length);
        }
        
        for(int s = 0 ; s < old.length ; s++) {
            int replace = (int) Math.round(old[s].length * foreignProportion);
            for(int i = 0 ; i < replace ; i++) {
                // pick population
                int pickIndex = -1;
                while(pickIndex == -1 || pickIndex == s) {
                    pickIndex = state.random[0].nextInt(old.length);
                }
                // pick individual
                Individual ind = old[pickIndex][state.random[0].nextInt(old[pickIndex].length)];
                // replace from the beginning -- the elites are copied to the end!
                state.population.subpops[s].individuals[i] = (Individual) ind.clone();
            }
        }
        return state.population;
    }

    @Override
    public String runComplete(EvolutionState state) {
        return null;
    }
}
