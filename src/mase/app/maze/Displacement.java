/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.maze;

import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
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
        MazeTask mt = (MazeTask) sim;
        initial = mt.agent.getLocation();
    }
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(null);
        MazeTask mt = (MazeTask) sim;
        Double2D loc = mt.agent.getLocation();
        Double2D disp = loc.subtract(initial);
        vbr = new VectorBehaviourResult( disp.x,  disp.y);
    }

    
}
