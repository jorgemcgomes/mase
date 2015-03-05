/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.foraging;

import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;

/**
 *
 * @author jorge
 */
public class ForagingGroupBehaviour extends MasonEvaluation {

    private double dispersion;
    private double averageProximity;
    private int timeWithin;
    private VectorBehaviourResult vbr;

    @Override
    protected void preSimulation() {
        super.preSimulation();
        dispersion = 0;
        averageProximity = 0;
        timeWithin = 0;
    }

    @Override
    protected void evaluate() {
        super.evaluate();
        ForagingTask ft = (ForagingTask) sim;
        dispersion += ft.landBot.getLocation().distance(ft.flyingBot.getLocation());

        double dFlying = 10000, dLand = 10000;
        for (Item it : ft.items) {
            dFlying = Math.min(ft.flyingBot.getLocation().distance(it.getLocation()), dFlying);
            dLand = Math.min(ft.landBot.getLocation().distance(it.getLocation()), dLand);
        }
        averageProximity += (dFlying + dLand) / 2;
        
        boolean within = false;
        Object[] lastRead = ft.flyingBot.botArcs.getClosestObjects();
        for(Object o : lastRead) {
            if(o != null) {
                within = true ; break;
            }
        }
        if(within) {
            timeWithin++;
        }
    }

    @Override
    protected void postSimulation() {
        super.postSimulation();
        ForagingTask ft = (ForagingTask) sim;
        double diag = ft.par.arenaSize.length();
        vbr = new VectorBehaviourResult(
                ft.items.size() / (float) ft.par.items.length,
                (float) timeWithin / currentEvaluationStep,
                (float) Math.min(1, dispersion / currentEvaluationStep / diag),
                (float) Math.min(1, averageProximity / currentEvaluationStep / diag)
        );
    }    
    
    @Override
    public EvaluationResult getResult() {
        return vbr;
    }

}
