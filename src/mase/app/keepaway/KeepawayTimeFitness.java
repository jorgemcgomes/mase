/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.keepaway;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimulator;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class KeepawayTimeFitness extends MasonEvaluation {

    private FitnessResult fitnessResult;
    private int maxSteps;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.maxSteps = state.parameters.getInt(base.pop().pop().push(MasonSimulator.P_MAX_STEPS), null);
    }

    @Override
    protected void postSimulation() {
        fitnessResult = new FitnessResult(sim.schedule.getSteps() / (float) maxSteps);
    }

    @Override
    public EvaluationResult getResult() {
        return fitnessResult;
    }
}
