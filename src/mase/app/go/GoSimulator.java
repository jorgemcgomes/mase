/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.go;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.app.go.Go.ControllerMode;
import mase.controllers.GroupController;
import mase.mason.MasonSimulationProblem;

/**
 *
 * @author Jorge
 */
public class GoSimulator extends MasonSimulationProblem<Go> {

    public static final String P_CONTROLLER_MODE = "controller-mode";
    public static final String P_BOARD_SIZE = "board-size";
    private static final long serialVersionUID = 1L;
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
    public Go createSimState(GroupController gc, long seed) {
        return new Go(seed, gc, mode, size);
    }
}
