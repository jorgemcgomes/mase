/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.foraging;

import java.awt.Color;
import mase.controllers.GroupController;
import mase.mason.GUICompatibleSimState;
import mase.mason.GUIState2D;
import mase.mason.MasonStandaloneSimulator;
import sim.display.GUIState;

/**
 *
 * @author jorge
 */
public class ForagingStandaloneSimulator extends MasonStandaloneSimulator {
    
    protected ForagingPar par;
    
    public ForagingStandaloneSimulator(ForagingPar par) {
        this.par = par;
    }

    @Override
    public GUICompatibleSimState createSimState(GroupController gc, long seed) {
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
