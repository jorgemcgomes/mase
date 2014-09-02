/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.multirover;

import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;

/**
 *
 * @author jorge
 */
public class MultiRoverBehaviour extends MasonEvaluation {

    private VectorBehaviourResult br;
    private double activeTime;
    
    @Override
    public EvaluationResult getResult() {
        return br;
    }

        @Override
    protected void preSimulation() {
        super.preSimulation();
        activeTime = 0;
    }
    
    @Override
    protected void postSimulation() {
        MultiRover mr = (MultiRover) sim;
        br = new VectorBehaviourResult(mr.scores[0] / 2f, mr.scores[1] / 2f, mr.scores[2] / 2f, (float) activeTime / mr.rovers.size() / maxSteps);
    }

    @Override
    protected void evaluate() {
        MultiRover mr = (MultiRover) sim;
        for(Rover r : mr.rovers) {
            if(r.getActuatorType() != 0) {
                activeTime++;
            }
        }
    }   
}
