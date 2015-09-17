/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import net.jafama.FastMath;
import sim.util.Double2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class OnePreyFitness extends MasonEvaluation {

    private float initialDistance, finalDistance;
    private FitnessResult fitnessResult;
    private float diagonal;

    @Override
    public FitnessResult getResult() {
        return fitnessResult;
    }

    @Override
    public void preSimulation() {
        PredatorPrey simState = (PredatorPrey) sim;
        Prey prey = simState.preys.get(0);
        initialDistance = 0;
        for (Predator pred : simState.predators) {
            initialDistance += prey.distanceTo(pred);
        }
        initialDistance /= simState.predators.size();
        diagonal = (float) FastMath.sqrtQuick(FastMath.pow2(simState.field.width) * 2);
    }

    @Override
    public void postSimulation() {
        PredatorPrey simState = (PredatorPrey) sim;

        Double2D preyLoc = simState.preys.get(0).getLocation();
        finalDistance = 0;
        for (Predator pred : simState.predators) {
            finalDistance += preyLoc.distance(pred.getLocation());
        }
        finalDistance /= simState.predators.size();

        // normalisation
        initialDistance = initialDistance / diagonal;
        finalDistance = Math.min(finalDistance, diagonal) / diagonal;
        float timeSpent = simState.schedule.getSteps() / (float) maxSteps;
        
        float score = 0;
        if (simState.getCaptureCount() == 1) {
            score = 2 - timeSpent  ; // 1..2
        } else {
            score = Math.max(initialDistance - finalDistance, 0); // 0..1
        }
        this.fitnessResult = new FitnessResult(score, FitnessResult.HARMONIC);
    }
}
