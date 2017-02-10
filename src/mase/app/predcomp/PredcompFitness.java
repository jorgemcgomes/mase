/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.predcomp;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import mase.mason.MasonSimulationProblem;

/**
 *
 * @author jorge
 */
public class PredcompFitness extends MasonEvaluation {

    private SubpopEvaluationResult ser;
    private int maxSteps;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.maxSteps = state.parameters.getInt(base.pop().pop().push(MasonSimulationProblem.P_MAX_STEPS), null);
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        ser = new SubpopEvaluationResult(
                new FitnessResult( maxSteps - sim.schedule.getSteps(), FitnessResult.ARITHMETIC),
                new FitnessResult( sim.schedule.getSteps(), FitnessResult.ARITHMETIC)
        );
    }

    @Override
    public EvaluationResult getResult() {
        return ser;
    }
}
