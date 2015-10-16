/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.indiana;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimulationProblem;

/**
 *
 * @author jorge
 */
public class IndianaFitness2 extends MasonEvaluation {

    private FitnessResult res;
    private int maxSteps;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.maxSteps = state.parameters.getInt(new Parameter(MasonSimulationProblem.P_PROBLEM).push(MasonSimulationProblem.P_MAX_STEPS), null);
    }

    @Override
    public EvaluationResult getResult() {
        return res;
    }

    @Override
    protected void postSimulation() {
        Indiana ind = (Indiana) sim;
        int count = 0;
        for (IndianaAgent a : ind.agents) {
            if (a.escaped) {
                count++;
            }
        }
        double time = ind.gate.openTime == -1 ? 0 : 1 - ind.gate.openTime / (double) maxSteps;
        double esc = (double) count / ind.agents.size();
        double w = 1 / (1 + (double) ind.agents.size());
        res = new FitnessResult(esc * (1 - w) + time * w);
    }

}
