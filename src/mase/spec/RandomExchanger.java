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
        for(Subpopulation sub : state.population.subpops) {
            int replace = (int) Math.round(sub.individuals.length * foreignProportion);
            for(int i = 0 ; i < replace ; i++) {
                // pick population
                Subpopulation pickSub = null;
                while(pickSub == null || pickSub == sub) {
                    pickSub = state.population.subpops[state.random[0].nextInt(state.population.subpops.length)];
                }
                // pick individual
                Individual ind = pickSub.individuals[state.random[0].nextInt(pickSub.individuals.length)];
                // replace
                sub.individuals[sub.individuals.length - replace - 1] = (Individual) ind.clone();
            }
        }
        return state.population;
    }

    @Override
    public String runComplete(EvolutionState state) {
        return null;
    }
}
