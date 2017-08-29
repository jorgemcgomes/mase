/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.me;

import ec.BreedingSource;
import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author jorge
 */
public class MEBreedingSource extends BreedingSource {

    public static final Parameter DEFAULT_BASE = new Parameter("me");
    private static final long serialVersionUID = 1L;
    
    @Override
    public Parameter defaultBase() {
        return DEFAULT_BASE;
    }

    @Override
    public boolean produces(EvolutionState state, Population newpop, int subpopulation, int thread) {
        return newpop.subpops.get(subpopulation) instanceof MESubpopulation;
    }

    @Override
    public void prepareToProduce(EvolutionState state, int subpopulation, int thread) {
        // nothing to do here
    }
    
    @Override
    public int produce(int min, int max, int subpopulation, ArrayList<Individual> inds, EvolutionState state, int thread, HashMap<String, Object> misc) {
        MESubpopulation subpop = (MESubpopulation) state.population.subpops.get(subpopulation);
        Integer[] binKeys = subpop.getRepertoire().keySet().toArray(new Integer[0]);
        int randomBin = state.random[thread].nextInt(binKeys.length);
        Collection<Individual> values = subpop.getRepertoire().get(binKeys[randomBin]);
        inds.add((Individual) values.iterator().next().clone());
        return 1;    
    }   

    @Override
    public int typicalIndsProduced() {
        return 1;
    }

    @Override
    public void finishProducing(EvolutionState s, int subpopulation, int thread) {
        // nothing to do here
    }

}
