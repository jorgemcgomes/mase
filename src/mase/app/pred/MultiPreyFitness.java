/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class MultiPreyFitness extends MasonEvaluation {

    private FitnessResult fitnessResult;

    @Override
    public FitnessResult getResult() {
        return fitnessResult;
    }

    @Override
    public void postSimulation() {
        PredatorPrey predSim = (PredatorPrey) sim;
        int captureCount = predSim.getCaptureCount();
        double captured = captureCount / (double) predSim.preys.size();
        double time = captureCount < predSim.preys.size() ? 1 : sim.schedule.getSteps() / (double) maxSteps;
        fitnessResult = new FitnessResult(0.9f * captured + 0.1f * (1 - time));
    }
}
