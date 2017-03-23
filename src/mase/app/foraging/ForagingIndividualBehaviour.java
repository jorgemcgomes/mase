/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.foraging;

import mase.evaluation.EvaluationResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class ForagingIndividualBehaviour extends MasonEvaluation {

    private static final long serialVersionUID = 1L;

    private double landFlyingDistance;
    private double landProximity, flyingProximity;
    private double landMovement, flyingMovement;
    private double flyingHeight;
    private Double2D landLast, flyingLast;
    private SubpopEvaluationResult ser;

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(null);
        landProximity = 0;
        flyingProximity = 0;
        landFlyingDistance = 0;
        flyingHeight = 0;
        landMovement = 0;
        flyingMovement = 0;
        ForagingTask ft = (ForagingTask) sim;
        landLast = ft.landBot.getCenterLocation();
        flyingLast = ft.flyingBot.getCenterLocation();
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(null);
        ForagingTask ft = (ForagingTask) sim;
        landFlyingDistance += ft.landBot.getCenterLocation().distance(ft.flyingBot.getCenterLocation());

        double dLand = 10000, dFlying = 10000;
        for (Item it : ft.items) {
            dLand = Math.min(ft.flyingBot.getCenterLocation().distance(it.getCenterLocation()), dLand);
            dFlying = Math.min(ft.landBot.getCenterLocation().distance(it.getCenterLocation()), dFlying);
        }
        landProximity += dLand;
        flyingProximity += dFlying;

        landMovement += landLast.distance(ft.landBot.getCenterLocation());
        flyingMovement += flyingLast.distance(ft.flyingBot.getCenterLocation());
        landLast = ft.landBot.getCenterLocation();
        flyingLast = ft.flyingBot.getCenterLocation();
        
        flyingHeight += ft.flyingBot.effector.getHeight();
    }
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(null);
        ForagingTask ft = (ForagingTask) sim;
        double diag = ft.par.arenaSize.length();
        ser = new SubpopEvaluationResult(
                new VectorBehaviourResult(
                         ft.items.size() / (double) ft.par.items.length,
                         Math.min(1, landProximity / currentEvaluationStep / diag),
                         Math.min(1, landMovement / currentEvaluationStep / (ft.par.landLinearSpeed * updateFrequency)),
                         Math.min(1, landFlyingDistance / currentEvaluationStep / diag)),
                new VectorBehaviourResult(
                         Math.min(1.5, flyingHeight / currentEvaluationStep / ft.par.flyingMaxHeight),
                         Math.min(1, flyingProximity / currentEvaluationStep / diag),
                         Math.min(1, flyingMovement / currentEvaluationStep / (ft.par.flyingLinearSpeed * updateFrequency)),
                         Math.min(1, landFlyingDistance / currentEvaluationStep / diag))
        );
    }    

    @Override
    public EvaluationResult getResult() {
        return ser;
    }

}
