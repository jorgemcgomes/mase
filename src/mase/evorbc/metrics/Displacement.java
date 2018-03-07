/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc.metrics;

import mase.app.maze.MazeTask;
import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import mase.mason.generic.SmartAgentProvider;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class Displacement extends MasonEvaluation {
    
    private static final long serialVersionUID = 1L;
    private VectorBehaviourResult vbr;
    private Double2D initial;

    @Override
    public EvaluationResult getResult() {
        return vbr;
    }

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(sim); 
        SmartAgentProvider smp = (SmartAgentProvider) sim;
        initial = smp.getSmartAgents().get(0).getLocation();
    }
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(null);
        SmartAgentProvider smp = (SmartAgentProvider) sim;
        Double2D loc = smp.getSmartAgents().get(0).getLocation();
        Double2D disp = loc.subtract(initial);
        vbr = new VectorBehaviourResult( disp.x,  disp.y);
    }

    
}
