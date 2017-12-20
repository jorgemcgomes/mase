/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.maze;

import mase.evaluation.EvaluationFunction;
import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;

/**
 *
 * @author jorge
 */
public class CircularFitnessLog extends MasonEvaluation {

    private static final long serialVersionUID = 1L;
    private VectorBehaviourResult vbr;

    @Override
    public EvaluationResult getResult() {
        return vbr;
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        EvaluationFunction[] evals = sim.getCurrentEvalFunctions();
        for(EvaluationFunction e : evals) {
            if(e instanceof CircularFitness) {
                CircularFitness cf = (CircularFitness) e;
                vbr = new VectorBehaviourResult(cf.oriDelta,cf.targetOrientation, cf.oriError);
                return;
            }
        }
        System.out.println("CircularFitness not found! CircularFitnessLog is useless.");
    }    
    
}
