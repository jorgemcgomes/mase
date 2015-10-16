/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.predcomp;

import ec.EvolutionState;
import ec.util.Parameter;
import java.awt.Color;
import mase.app.predcomp.PredcompParams.ORIENTATION;
import mase.controllers.GroupController;
import mase.mason.MasonSimState;
import mase.mason.GUIState2D;
import mase.mason.MasonSimulationProblem;
import sim.display.GUIState;

/**
 *
 * @author jorge
 */
public class PredcompSimulator extends MasonSimulationProblem {

    private PredcompParams par;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        Parameter df = defaultBase();
        par = new PredcompParams();
        par.discretization = state.parameters.getDouble(df.push(PredcompParams.P_DISCRETIZATION), null);
        par.predatorSpeed = state.parameters.getDouble(df.push(PredcompParams.P_PREDATOR_SPEED), null);
        par.preySpeed = state.parameters.getDouble(df.push(PredcompParams.P_PREY_SPEED), null);
        par.proximityRange = state.parameters.getDouble(df.push(PredcompParams.P_PROXIMITY_RANGE), null);
        par.size = state.parameters.getDouble(df.push(PredcompParams.P_SIZE), null);
        par.viewAngle = state.parameters.getDouble(df.push(PredcompParams.P_VIEW_ANGLE), null);
        par.visionRange = state.parameters.getDouble(df.push(PredcompParams.P_VISION_RANGE), null);
        par.proximitySensors = state.parameters.getInt(df.push(PredcompParams.P_PROXIMITY_SENSORS), null);
        par.visionNeurons = state.parameters.getInt(df.push(PredcompParams.P_VISION_NEURONS), null);
        par.orientation = ORIENTATION.valueOf(state.parameters.getString(df.push(PredcompParams.P_INITIAL_ORIENTATIONS), null));
    }

    @Override
    public MasonSimState createSimState(GroupController gc, long seed) {
        Predcomp sim = new Predcomp(seed, par, gc);
        return sim;
    }

    @Override
    public GUIState createSimStateWithUI(GroupController gc, long seed) {
        return new GUIState2D(createSimState(gc, seed), "Competitive predator", 500, 500, Color.WHITE);
    }

}
