/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.foraging;

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
public class ForagingSimulator extends MasonSimulationProblem<ForagingTask> {

    private static final long serialVersionUID = 1L;

    protected ForagingPar par;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        par = new ForagingPar();
        ParamUtils.autoSetParameters(par, state, base, super.defaultBase(), true);
    }

    @Override
    public ForagingTask createSimState(GroupController gc, long seed) {
        return new ForagingTask(seed, par, gc);
    }
}
