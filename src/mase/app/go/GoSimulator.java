/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.go;

import java.awt.Color;
import mase.controllers.GroupController;
import mase.mason.MaseSimState;
import mase.mason.Mason2dUI;
import mase.mason.MasonSimulator;
import sim.display.GUIState;

/**
 *
 * @author Jorge
 */
public class GoSimulator extends MasonSimulator {

    @Override
    public MaseSimState createSimState(GroupController gc, long seed) {
        return new Go(seed, gc);
    }

    @Override
    public GUIState createSimStateWithUI(GroupController gc, long seed) {
        return new Mason2dUI(createSimState(gc, seed), "Go", 500, 500, Color.RED);
    }
}
