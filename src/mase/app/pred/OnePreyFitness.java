/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimulator;
import sim.util.Double2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class OnePreyFitness extends MasonEvaluation {

    private float initialDistance, finalDistance;
    private FitnessResult fitnessResult;
    private int maxSteps;
    private float diagonal;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.maxSteps = state.parameters.getInt(base.pop().pop().push(MasonSimulator.P_MAX_STEPS), null);
        float size = state.parameters.getInt(base.pop().pop().push(PredParams.P_SIZE), null);
        this.diagonal = (float) Math.sqrt(Math.pow(size, 2) * 2);
    }

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
            score = 3 - finalDistance - timeSpent  ; // 1..3
        } else {
            score = Math.max(initialDistance - finalDistance, 0); // 0..1
        }
        this.fitnessResult = new FitnessResult(score);
    }
}
