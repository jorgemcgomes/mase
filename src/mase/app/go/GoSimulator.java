/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.go;

import ec.EvolutionState;
import ec.util.Parameter;
import java.awt.Color;
import mase.app.go.Go.ControllerMode;
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

    public static final String P_CONTROLLER_MODE = "controller-mode";
    public static final String P_BOARD_SIZE = "board-size";
    private ControllerMode mode;
    private int size;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        Parameter df = defaultBase();
        this.size = state.parameters.getInt(df.push(P_BOARD_SIZE), null);
        this.mode = ControllerMode.valueOf(state.parameters.getString(df.push(P_CONTROLLER_MODE), null));
    }
    
    
    
    @Override
    public MaseSimState createSimState(GroupController gc, long seed) {
        return new Go(seed, gc, mode, size);
    }

    @Override
    public GUIState createSimStateWithUI(GroupController gc, long seed) {
        return new Mason2dUI(createSimState(gc, seed), "Go", 500, 500, Color.RED);
    }
}
