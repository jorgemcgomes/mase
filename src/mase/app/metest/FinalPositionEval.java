/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.metest;

import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class FinalPositionEval extends MasonEvaluation {

    private VectorBehaviourResult vbr;
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        LocomotionTask lt = (LocomotionTask) sim;
        Double2D finalPos = lt.agent.getLocation();
        vbr = new VectorBehaviourResult(finalPos.x / lt.size, finalPos.y / lt.size);
    }

    @Override
    public EvaluationResult getResult() {
        return vbr;
    }
}
