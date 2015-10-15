/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.foraging;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.mason.MasonStandaloneProblem;
import mase.mason.MasonStandaloneSimulator;
import mase.mason.ParamUtils;

/**
 *
 * @author jorge
 */
public class ForagingStandaloneProblem extends MasonStandaloneProblem {

    protected ForagingStandaloneSimulator sim;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        ForagingPar par = new ForagingPar();
        ParamUtils.autoSetParameters(par, state.parameters, base, super.defaultBase(), true);
        sim = new ForagingStandaloneSimulator(par);
    }

    @Override
    public MasonStandaloneSimulator getSimulator() {
        return sim;
    }
}
