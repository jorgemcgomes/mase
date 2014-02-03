/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.keepaway;

import ec.EvolutionState;
import ec.util.Parameter;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import mase.controllers.GroupController;
import mase.mason.MaseSimState;
import mase.mason.Mason2dUI;
import mase.mason.MasonSimulator;
import sim.display.GUIState;
import sim.util.Double2D;

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

        // World
        par.size = state.parameters.getDouble(df.push(KeepawayParams.P_SIZE), null);
        par.ringSize = state.parameters.getDouble(df.push(KeepawayParams.P_RING_SIZE), null);
        par.discretization = state.parameters.getDouble(df.push(KeepawayParams.P_DISCRETIZATION), null);
        par.ballDecay = state.parameters.getDouble(df.push(KeepawayParams.P_BALL_DECAY), null);
        par.collisions = state.parameters.getBoolean(df.push(KeepawayParams.P_COLLISIONS), null, false);
        par.ballPlacement = state.parameters.getInt(df.push(KeepawayParams.P_BALL_PLACEMENT), null, 0);

        // Keepers
        par.numKeepers = state.parameters.getInt(df.push(KeepawayParams.P_NUM_KEEPERS), null);
        par.color = new Color[par.numKeepers];
        par.moveSpeed = new double[par.numKeepers];
        par.passSpeed = new double[par.numKeepers];
        for (int i = 0; i < par.numKeepers; i++) {
            String c = state.parameters.getString(df.push(KeepawayParams.P_KEEPER).push(i + "").push(KeepawayParams.P_COLOR),
                    df.push(KeepawayParams.P_KEEPER).push(KeepawayParams.P_COLOR));
            try {
                par.color[i] = (Color) Color.class.getDeclaredField(c).get(null);
            } catch (Exception ex) {
                Logger.getLogger(KeepawaySimulator.class.getName()).log(Level.SEVERE, null, ex);
            }
            par.moveSpeed[i] = state.parameters.getDouble(df.push(KeepawayParams.P_KEEPER).push(i + "").push(KeepawayParams.P_MOVE_SPEED),
                    df.push(KeepawayParams.P_KEEPER).push(KeepawayParams.P_MOVE_SPEED));
            par.passSpeed[i] = state.parameters.getDouble(df.push(KeepawayParams.P_KEEPER).push(i + "").push(KeepawayParams.P_PASS_SPEED),
                    df.push(KeepawayParams.P_KEEPER).push(KeepawayParams.P_PASS_SPEED));
        }
        par.sortKeepers = state.parameters.getBoolean(df.push(KeepawayParams.P_SORT_KEEPERS), null, false);
        par.keepersPlacement = state.parameters.getDouble(df.push(KeepawayParams.P_KEEPERS_PLACEMENT), null);

        // Takers
        par.takerSpeed = state.parameters.getDouble(df.push(KeepawayParams.P_TAKER_SPEED), null);
        par.takersPlacement = state.parameters.getDouble(df.push(KeepawayParams.P_TAKERS_PLACEMENT), null);

        if (par.keepersPlacement == 0) {
            // Initial keeper position
            // place keepers evenly distributed around the circle
            Double2D center = new Double2D(par.size / 2, par.size / 2);
            par.keeperStartPos = new Double2D[par.numKeepers];
            par.keeperStartAngle = new double[par.numKeepers];
            Double2D up = new Double2D(0, par.ringSize / 2);
            double rot = Math.PI * 2 / par.numKeepers;
            for (int i = 0; i < par.numKeepers; i++) {
                Double2D v = up.rotate(rot * i);
                par.keeperStartPos[i] = v.add(center);
                par.keeperStartAngle[i] = v.negate().angle();
            }
        }
    }

    @Override
    public MaseSimState createSimState(GroupController gc, long seed) {
        return new Keepaway(seed, par, gc);
    }

    @Override
    public GUIState createSimStateWithUI(GroupController gc, long seed) {
        return new Mason2dUI(createSimState(gc, seed), "Keepaway", 500, 500, new Color(0, 150, 0));
    }

}
