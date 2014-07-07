/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.herding;

import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;

/**
 *
 * @author jorge
 */
public class HerdingGroupEval extends MasonEvaluation {

    private VectorBehaviourResult vbr;
    private float sheepFence;
    private float sheepFox;

        @Override
    protected void preSimulation() {
        super.preSimulation();
        sheepFence = 0;
        sheepFox = 0;
    }
    
    @Override
    protected void evaluate() {
        super.evaluate();
        
        Herding herd = (Herding) sim;
        Sheep s = herd.sheeps.get(0);
        
        sheepFence += herd.fence.closestDistance(s.getLocation());
        for(Fox f : herd.foxes) {
            sheepFox += s.distanceTo(f);
        }        
    }

    @Override
    protected void postSimulation() {
        super.postSimulation();
                        Herding herd = (Herding) sim;
        Sheep s = herd.sheeps.get(0);
        double sheepCurral = herd.curral.closestDistance(s.getLocation());
        
        vbr = new VectorBehaviourResult(
                (float) (sheepCurral / herd.par.arenaSize),
                (float) currentEvaluationStep / maxEvaluationSteps,
                (float) (sheepFence / currentEvaluationStep / (herd.par.arenaSize / 2)),
                (float) (sheepFox / currentEvaluationStep / herd.par.numFoxes / herd.par.arenaSize)
        );
    }

    @Override
    public EvaluationResult getResult() {
        return vbr;
    }
    
}
