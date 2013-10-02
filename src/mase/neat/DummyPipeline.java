/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.neat;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;

/**
 *
 * @author jorge
 */
public class DummyPipeline extends BreedingPipeline {

    @Override
    public void setup(EvolutionState state, Parameter base) {
        // Do nothing
    }

    @Override
    public int numSources() {
        return 0;
    }

    @Override
    public int produce(int min, int max, int start, int subpopulation, Individual[] inds, EvolutionState state, int thread) {
        return 0;
    }

    @Override
    public Parameter defaultBase() {
        return new Parameter("ec");
    }
    
}
