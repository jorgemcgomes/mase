/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.indiana;

import ec.EvolutionState;
import ec.util.Parameter;
import java.awt.Color;
import mase.GroupController;
import mase.app.indiana.Indiana.AgentPlacement;
import mase.mason.MaseSimState;
import mase.mason.Mason2dUI;
import mase.mason.MasonSimulator;
import sim.display.GUIState;

/**
 *
 * @author jorge
 */
public class IndianaSimulator extends MasonSimulator {

    protected IndianaParams par;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        par = new IndianaParams();
        Parameter df = defaultBase();
        par.agentSensorArcs = state.parameters.getInt(base.push(IndianaParams.P_AGENT_ARCS), df.push(IndianaParams.P_AGENT_ARCS));
        par.agentSensorRadius = state.parameters.getDouble(base.push(IndianaParams.P_AGENT_RADIUS), df.push(IndianaParams.P_AGENT_RADIUS));
        par.agentRotation = state.parameters.getDouble(base.push(IndianaParams.P_AGENT_ROTATION), df.push(IndianaParams.P_AGENT_ROTATION));
        par.agentSpeed = state.parameters.getDouble(base.push(IndianaParams.P_AGENT_SPEED), df.push(IndianaParams.P_AGENT_SPEED));
        par.discretization = state.parameters.getDouble(base.push(IndianaParams.P_DISCRETIZATION), df.push(IndianaParams.P_DISCRETIZATION));
        par.numAgents = state.parameters.getInt(base.push(IndianaParams.P_NUM_AGENTS), df.push(IndianaParams.P_NUM_AGENTS));
        par.size = state.parameters.getDouble(base.push(IndianaParams.P_SIZE), df.push(IndianaParams.P_SIZE));
        par.wallRadius = state.parameters.getDouble(base.push(IndianaParams.P_WALL_RADIUS), df.push(IndianaParams.P_WALL_RADIUS));
        par.wallRays = state.parameters.getInt(base.push(IndianaParams.P_WALL_RAYS), df.push(IndianaParams.P_WALL_RAYS));
        par.agentPlacement = AgentPlacement.valueOf(state.parameters.getString(base.push(IndianaParams.P_AGENT_PLACEMENT), df.push(IndianaParams.P_AGENT_PLACEMENT)));
        par.gateInterval = state.parameters.getInt(base.push(IndianaParams.P_GATE_INTERVAL), df.push(IndianaParams.P_GATE_INTERVAL));
        par.gateSize = state.parameters.getDouble(base.push(IndianaParams.P_GATE_SIZE), df.push(IndianaParams.P_GATE_SIZE));
    }

    @Override
    public MaseSimState createSimState(GroupController gc, long seed) {
        return new Indiana(seed, par, gc);
    }

    @Override
    public GUIState createSimStateWithUI(GroupController gc, long seed) {
        return new Mason2dUI(createSimState(gc, seed), "Indiana", 500, 500, Color.WHITE);
    }

}
