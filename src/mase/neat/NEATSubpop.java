/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.neat;

import ec.EvolutionState;
import ec.Subpopulation;
import ec.util.Parameter;
import org.neat4j.neat.core.NEATGeneticAlgorithm;

/**
 *
 * @author jorge
 */
public class NEATSubpop extends Subpopulation {

    private NEATGeneticAlgorithm neat;

    // parameters
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        // load neat parameters

    }

    public NEATGeneticAlgorithm getNEAT() {
        return neat;
    }

    @Override
    public void populate(EvolutionState state, int thread) {
        neat.createPopulation();
    }

    public void breed() {   
        // TODO
        neat.runEpoch();
        // TODO
    }
}
