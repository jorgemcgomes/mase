/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.herding;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.controllers.GroupController;
import mase.mason.MasonSimulationProblem;
import mase.mason.ParamUtils;

/**
 *
 * @author jorge
 */
public class HerdingSimulator extends MasonSimulationProblem<Herding> {

    private static final long serialVersionUID = 1L;
    private HerdingParams par;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        par = new HerdingParams();    
        ParamUtils.autoSetParameters(par, state, base, super.defaultBase(), true);
    }

    @Override
    public Herding createSimState(GroupController gc, long seed) {
        return new Herding(seed, par, gc);
    }
}
