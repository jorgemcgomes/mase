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
import mase.mason.GUICompatibleSimState;
import mase.mason.GUIState2D;
import mase.mason.MasonSimulator;
import sim.display.GUIState;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class ForagingSimulator extends MasonSimulator {

    protected ForagingPar par;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        Parameter df = super.defaultBase();
        par = new ForagingPar();

        par.useFlyingRobot = state.parameters.getBoolean(df.push(ForagingPar.P_USE_FLYING_ROBOT), null, true);
        par.flyingAngAcc = state.parameters.getDouble(df.push(ForagingPar.P_FLYING_ANG_ACC), null);
        par.flyingAngSpeed = state.parameters.getDouble(df.push(ForagingPar.P_FLYING_ANG_SPEED), null);
        par.flyingLinearAcc = state.parameters.getDouble(df.push(ForagingPar.P_FLYING_LINEAR_ACC), null);
        par.flyingLinearSpeed = state.parameters.getDouble(df.push(ForagingPar.P_FLYING_LINEAR_SPEED), null);
        par.flyingRadius = state.parameters.getDouble(df.push(ForagingPar.P_FLYING_RADIUS), null);
        par.flyingArcs = state.parameters.getInt(df.push(ForagingPar.P_FLYING_ARCS), null);
        par.flyingVisionAngle = state.parameters.getDouble(df.push(ForagingPar.P_FLYING_VISION_ANGLE), null);

        par.flyingStartPos = parsePoint(state.parameters.getString(df.push(ForagingPar.P_FLYING_START_POS), null));
        par.flyingStartOri = state.parameters.getDouble(df.push(ForagingPar.P_FLYING_START_ORI), null);
        par.flyingStartHeight = state.parameters.getDouble(df.push(ForagingPar.P_FLYING_START_HEIGHT), null);
        par.flyingVerticalMovement = state.parameters.getBoolean(df.push(ForagingPar.P_FLYING_VERTICAL_MOVEMENT), null, false);
        par.flyingMaxHeight = state.parameters.getDouble(df.push(ForagingPar.P_FLYING_MAX_HEIGHT), null);

        par.landLinearSpeed = state.parameters.getDouble(df.push(ForagingPar.P_LAND_LINEAR_SPEED), null);
        par.landRadius = state.parameters.getDouble(df.push(ForagingPar.P_LAND_RADIUS), null);
        par.landArcs = state.parameters.getInt(df.push(ForagingPar.P_LAND_ARCS), null);
        par.landSensingRange = state.parameters.getDouble(df.push(ForagingPar.P_LAND_SENSING_RANGE), null);
        par.landTurnSpeed = state.parameters.getDouble(df.push(ForagingPar.P_LAND_TURN_SPEED), null);
        par.landVisionAngle = state.parameters.getDouble(df.push(ForagingPar.P_LAND_VISION_ANGLE), null);

        par.itemPlacementZone = state.parameters.getDouble(df.push(ForagingPar.P_PLACEMENT_ZONE), null);
        par.itemRadius = state.parameters.getDouble(df.push(ForagingPar.P_ITEM_RADIUS), null);
        String str = state.parameters.getString(df.push(ForagingPar.P_ITEMS), null);
        String[] pts = str.split("-");
        par.items = new Double2D[pts.length];
        for (int i = 0; i < pts.length; i++) {
            par.items[i] = parsePoint(pts[i]);
        }
        par.arenaSize = parsePoint(state.parameters.getString(df.push(ForagingPar.P_ARENA_SIZE), null));

        par.landStartPos = parsePoint(state.parameters.getString(df.push(ForagingPar.P_LAND_START_POS), null));
        par.landStartOri = state.parameters.getDouble(df.push(ForagingPar.P_LAND_START_ORI), null);
    }

    private Double2D parsePoint(String s) {
        String[] coords = s.split(",");
        return new Double2D(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));
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
