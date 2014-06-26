/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.sharing;

import ec.EvolutionState;
import ec.util.Parameter;
import java.awt.Color;
import mase.controllers.GroupController;
import mase.mason.GUICompatibleSimState;
import mase.mason.GUIState2D;
import mase.mason.MasonSimulator;
import sim.display.GUIState;

/**
 *
 * @author jorge
 */
public class ResourceSharingSimulator extends MasonSimulator {

    protected RSParams par;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        par = new RSParams();
        Parameter df = defaultBase();
        par.agentRadius = state.parameters.getDouble(base.push(RSParams.P_AGENT_RADIUS), df.push(RSParams.P_AGENT_RADIUS));
        par.agentRotation = state.parameters.getDouble(base.push(RSParams.P_AGENT_ROTATION), df.push(RSParams.P_AGENT_ROTATION));
        par.agentSpeed = state.parameters.getDouble(base.push(RSParams.P_AGENT_SPEED), df.push(RSParams.P_AGENT_SPEED));
        par.agentSensorRange = state.parameters.getDouble(base.push(RSParams.P_AGENT_SENSOR_RANGE), df.push(RSParams.P_AGENT_SENSOR_RANGE));
        par.agentSensorArcs = state.parameters.getInt(base.push(RSParams.P_AGENT_SENSOR_ARCS), df.push(RSParams.P_AGENT_SENSOR_ARCS));
        par.maxEnergy = state.parameters.getDouble(base.push(RSParams.P_MAX_ENERGY), df.push(RSParams.P_MAX_ENERGY));
        par.minEnergyDecay = state.parameters.getDouble(base.push(RSParams.P_MIN_ENERGY_DECAY), df.push(RSParams.P_MIN_ENERGY_DECAY));
        par.maxEnergyDecay = state.parameters.getDouble(base.push(RSParams.P_MAX_ENERGY_DECAY), df.push(RSParams.P_MAX_ENERGY_DECAY));
        par.rechargeRate = state.parameters.getDouble(base.push(RSParams.P_RECHARGE_RATE), df.push(RSParams.P_RECHARGE_RATE)); 
        par.resourceRadius = state.parameters.getDouble(base.push(RSParams.P_RESOURCE_RADIUS), df.push(RSParams.P_RESOURCE_RADIUS)); 
        par.numAgents = state.parameters.getInt(base.push(RSParams.P_NUM_AGENTS), df.push(RSParams.P_NUM_AGENTS));
        par.discretization = state.parameters.getDouble(base.push(RSParams.P_DISCRETIZATION), df.push(RSParams.P_DISCRETIZATION));
        par.size = state.parameters.getDouble(base.push(RSParams.P_SIZE), df.push(RSParams.P_SIZE));
        par.rechargeDelay = state.parameters.getInt(base.push(RSParams.P_RECHARGE_DELAY), df.push(RSParams.P_RECHARGE_DELAY));
    }

    @Override
    public GUICompatibleSimState createSimState(GroupController gc, long seed) {
        return new ResourceSharing(seed, par, gc);
    }

    @Override
    public GUIState createSimStateWithUI(GroupController gc, long seed) {
        return new GUIState2D(createSimState(gc, seed), "Aggregation", 500, 500, Color.WHITE);
    }
}
