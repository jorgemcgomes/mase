/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.foraging;

import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;

/**
 *
 * @author jorge
 */
public class ForagingGroupBehaviour extends MasonEvaluation {

    private static final long serialVersionUID = 1L;
    private double dispersion;
    private double averageProximity;
    private int timeWithin;
    private VectorBehaviourResult vbr;

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(null);
        dispersion = 0;
        averageProximity = 0;
        timeWithin = 0;
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(null);
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
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(null);
        ForagingTask ft = (ForagingTask) sim;
        double diag = ft.par.arenaSize.length();
        vbr = new VectorBehaviourResult(
                ft.items.size() / (double) ft.par.items.length,
                (double) timeWithin / currentEvaluationStep,
                 Math.min(1, dispersion / currentEvaluationStep / diag),
                 Math.min(1, averageProximity / currentEvaluationStep / diag)
        );
    }    
    
    @Override
    public EvaluationResult getResult() {
        return vbr;
    }

}
