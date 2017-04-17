/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.controllers.GroupController;
import mase.mason.MasonSimulationProblem;
import mase.mason.ParamUtils;

/**
 *
 * @author jorge
 */
public class PlaygroundSimulator extends MasonSimulationProblem<Playground> {

    private static final long serialVersionUID = 1L;
    private PlaygroundParams par;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        par = new PlaygroundParams();
        ParamUtils.autoSetParameters(par, state, base, super.defaultBase(), true);
    }

    @Override
    protected Playground createSimState(GroupController gc, long seed) {
        return new Playground(gc, seed, par);
    }

}
