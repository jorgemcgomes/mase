/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.controllers;

import mase.ControllerDecoder;
import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class NeuralGroupDecoder implements ControllerDecoder {

    protected EvolutionState state;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        this.state = state;
    }

    @Override
    public StaticGroupController decodeController(Individual[] ind) {
        NeuralAgentController[] ncs = new NeuralAgentController[ind.length];
        for (int i = 0; i < ind.length; i++) {
            if (!(ind[i] instanceof NeuralControllerIndividual)) {
                state.output.fatal("Individual was expected to be a subclass of NeuralControllerIndividual. Found: " + ind[i].getClass().getName());
            }
            ncs[i] = new NeuralAgentController(((NeuralControllerIndividual) ind[i]).decodeNetwork());
        }
        return new StaticGroupController(ncs);
    }
}
