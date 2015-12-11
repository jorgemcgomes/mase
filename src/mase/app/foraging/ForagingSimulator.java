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
public class ForagingSimulator extends MasonSimulationProblem {

    private static final long serialVersionUID = 1L;

    protected ForagingPar par;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        par = new ForagingPar();
        ParamUtils.autoSetParameters(par, state.parameters, base, super.defaultBase(), true);
    }

    @Override
    public MasonSimState createSimState(GroupController gc, long seed) {
        return new ForagingTask(seed, par, gc);
    }

    @Override
    public GUIState createSimStateWithUI(GroupController gc, long seed) {
        double w = par.arenaSize.x;
        double h = par.arenaSize.y;
        double ratio = 500 / Math.min(w, h);
        return new GUIState2D(createSimState(gc, seed), "Foraging",
                (int) Math.round(w * ratio), (int) Math.round(h * ratio), Color.WHITE);
    }

}
