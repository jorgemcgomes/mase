/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.aggregation;

import ec.EvolutionState;
import ec.util.Parameter;
import java.awt.Color;
import mase.controllers.GroupController;
import mase.mason.MasonSimState;
import mase.mason.GUIState2D;
import mase.mason.MasonSimulationProblem;
import sim.display.GUIState;

/**
 *
 * @author jorge
 */
public class AggregationSimulator extends MasonSimulationProblem<Aggregation> {

    protected AggregationParams par;
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        par = new AggregationParams();
        Parameter df = defaultBase();
        par.agentArcs = state.parameters.getInt(base.push(AggregationParams.P_AGENT_ARCS), df.push(AggregationParams.P_AGENT_ARCS));
        par.agentRadius = state.parameters.getDouble(base.push(AggregationParams.P_AGENT_RADIUS), df.push(AggregationParams.P_AGENT_RADIUS));
        par.agentRotation = state.parameters.getDouble(base.push(AggregationParams.P_AGENT_ROTATION), df.push(AggregationParams.P_AGENT_ROTATION));
        par.agentSpeed = state.parameters.getDouble(base.push(AggregationParams.P_AGENT_SPEED), df.push(AggregationParams.P_AGENT_SPEED));
        par.discretization = state.parameters.getDouble(base.push(AggregationParams.P_DISCRETIZATION), df.push(AggregationParams.P_DISCRETIZATION));
        par.numAgents = state.parameters.getInt(base.push(AggregationParams.P_NUM_AGENTS), df.push(AggregationParams.P_NUM_AGENTS));
        par.size = state.parameters.getDouble(base.push(AggregationParams.P_SIZE), df.push(AggregationParams.P_SIZE));
        par.wallRadius = state.parameters.getDouble(base.push(AggregationParams.P_WALL_RADIUS), df.push(AggregationParams.P_WALL_RADIUS));
        par.wallRays = state.parameters.getInt(base.push(AggregationParams.P_WALL_RAYS), df.push(AggregationParams.P_WALL_RAYS));
    }

    @Override
    public Aggregation createSimState(GroupController gc, long seed) {
        return new Aggregation(seed, par, gc);
    }
}
