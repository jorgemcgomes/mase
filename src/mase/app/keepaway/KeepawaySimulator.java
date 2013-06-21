/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.keepaway;

import ec.EvolutionState;
import ec.util.Parameter;
import java.awt.Color;
import mase.GroupController;
import mase.mason.Mason2dUI;
import mase.mason.MasonSimulator;
import mase.mason.PortrayableSimState;
import sim.display.GUIState;

/**
 *
 * @author Jorge
 */
public class KeepawaySimulator extends MasonSimulator {

    protected KeepawayParams par;
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        Parameter df = defaultBase();
        par = new KeepawayParams();
        par.ballDecay = state.parameters.getDouble(base.push(KeepawayParams.P_BALL_DECAY), df.push(KeepawayParams.P_BALL_DECAY));
        par.ballSpeed = state.parameters.getDouble(base.push(KeepawayParams.P_BALL_SPEED), df.push(KeepawayParams.P_BALL_SPEED));
        par.discretization = state.parameters.getDouble(base.push(KeepawayParams.P_DISCRETIZATION), df.push(KeepawayParams.P_DISCRETIZATION));
        par.keeperSpeed = state.parameters.getDouble(base.push(KeepawayParams.P_KEEPER_SPEED), df.push(KeepawayParams.P_KEEPER_SPEED));
        par.ringSize = state.parameters.getDouble(base.push(KeepawayParams.P_RING_SIZE), df.push(KeepawayParams.P_RING_SIZE));
        par.size = state.parameters.getDouble(base.push(KeepawayParams.P_SIZE), df.push(KeepawayParams.P_SIZE));
        par.takerSpeed = state.parameters.getDouble(base.push(KeepawayParams.P_TAKER_SPEED), df.push(KeepawayParams.P_TAKER_SPEED));
        par.numKeepers = state.parameters.getInt(base.push(KeepawayParams.P_NUM_KEEPERS), df.push(KeepawayParams.P_NUM_KEEPERS));
    }

    @Override
    public PortrayableSimState createSimState(GroupController gc, long seed) {
        return new Keepaway(seed, par, gc);
    }

    @Override
    public GUIState createSimStateWithUI(GroupController gc, long seed) {
        return new Mason2dUI(createSimState(gc, seed), "Keepaway", 500, 500, new Color(0, 150, 0));
    }  
}
