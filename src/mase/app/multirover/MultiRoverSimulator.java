/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.multirover;

import ec.EvolutionState;
import ec.util.Parameter;
import java.awt.Color;
import mase.controllers.GroupController;
import mase.mason.GUIState2D;
import mase.mason.MasonSimulator;
import sim.display.GUIState;

/**
 *
 * @author jorge
 */
public class MultiRoverSimulator extends MasonSimulator {
    
        private MRParams par;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        par = new MRParams();
        /* Mandatory parameters */
        base = defaultBase();
        par.discretization = state.parameters.getDouble(base.push(MRParams.P_DISCRETIZATION), null);
        par.numAgents = state.parameters.getInt(base.push(MRParams.P_NUM_AGENTS), null);
        par.numRocks = state.parameters.getInt(base.push(MRParams.P_NUM_ROCKS), null);
        par.rockRadius = state.parameters.getDouble(base.push(MRParams.P_ROCK_RADIUS), null);
        par.rotationSpeed = state.parameters.getDouble(base.push(MRParams.P_ROTATION_SPEED), null);
        par.sensorArcs = state.parameters.getInt(base.push(MRParams.P_SENSOR_ARCS), null);
        par.separation = state.parameters.getDouble(base.push(MRParams.P_SEPARATION), null);
        par.size = state.parameters.getDouble(base.push(MRParams.P_SIZE), null);
        par.speed = state.parameters.getDouble(base.push(MRParams.P_SPEED), null);
        par.sensorRange = state.parameters.getInt(base.push(MRParams.P_SENSOR_RANGE), null);
        par.minActivationTime = state.parameters.getInt(base.push(MRParams.P_MIN_ACTIVATION_TIME), null);
    }

    @Override
    public MultiRover createSimState(GroupController gc, long seed) {
        return new MultiRover(seed, par, gc);
    }

    @Override
    public GUIState createSimStateWithUI(GroupController cs, long seed) {
        return new GUIState2D(createSimState(cs, seed), "Predator-prey", 500, 500, Color.WHITE);
    }
    
}
