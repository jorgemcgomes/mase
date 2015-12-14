/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.herding;

import ec.EvolutionState;
import ec.util.Parameter;
import java.awt.Color;
import mase.controllers.GroupController;
import mase.mason.MasonSimState;
import mase.mason.GUIState2D;
import mase.mason.MasonSimulationProblem;
import mase.mason.ParamUtils;
import sim.display.GUIState;

/**
 *
 * @author jorge
 */
public class HerdingSimulator extends MasonSimulationProblem {

    private static final long serialVersionUID = 1L;
    private HerdingParams par;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        par = new HerdingParams();        
        ParamUtils.autoSetParameters(par, state.parameters, base, super.defaultBase(), true);
    }

    @Override
    public MasonSimState createSimState(GroupController gc, long seed) {
        return new Herding(seed, par, gc);
    }

    @Override
    public GUIState createSimStateWithUI(GroupController gc, long seed) {
        return new GUIState2D(createSimState(gc, seed), "Herding", 500, 500, Color.WHITE);
    }

}
