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
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.List;

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
    // TODO: this should really exchange individuals, not what it is currently doing
    public Population postBreedingExchangePopulation(EvolutionState state) {
        List<Individual>[] old = new List[state.population.subpops.size()];
        for(int i = 0 ; i < state.population.subpops.size() ; i++) {
            old[i] = new ArrayList(state.population.subpops.get(i).individuals);
        }
        
        for(int s = 0 ; s < old.length ; s++) {
            int replace = (int) Math.round(old[s].size() * foreignProportion);
            for(int i = 0 ; i < replace ; i++) {
                // pick population
                int pickIndex = -1;
                while(pickIndex == -1 || pickIndex == s) {
                    pickIndex = state.random[0].nextInt(old.length);
                }
                // pick individual
                Individual ind = old[pickIndex].get(state.random[0].nextInt(old[pickIndex].size()));
                // replace from the beginning -- the elites are copied to the end!
                state.population.subpops.get(s).individuals.set(i, (Individual) ind.clone());
            }
        }
        return state.population;
    }

    @Override
    public String runComplete(EvolutionState state) {
        return null;
    }
}
