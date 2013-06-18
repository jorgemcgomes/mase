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

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class MultiPreyFitness extends MasonEvaluation {

    private FitnessResult fitnessResult;
    private int maxSteps;
    private int nPreys;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.maxSteps = state.parameters.getInt(base.pop().pop().push(MasonSimulator.P_MAX_STEPS), null);
        this.nPreys = state.parameters.getInt(base.pop().pop().push(PredParams.P_NPREYS), null);
    }

    @Override
    public FitnessResult getResult() {
        return fitnessResult;
    }

    @Override
    public void postSimulation() {
        int captureCount = ((PredatorPrey) super.sim).getCaptureCount();
        float captured = captureCount / (float) nPreys;
        float time = captureCount < nPreys ? 1 : sim.schedule.getSteps() / (float) maxSteps;
        fitnessResult = new FitnessResult(0.9f * captured + 0.1f * (1 - time));
    }
}
