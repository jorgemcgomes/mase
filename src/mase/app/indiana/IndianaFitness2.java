/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.indiana;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimulator;

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
        this.maxSteps = state.parameters.getInt(new Parameter(MasonSimulator.P_PROBLEM).push(MasonSimulator.P_MAX_STEPS), null);
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
        float time = ind.gate.openTime == -1 ? 0 : 1 - ind.gate.openTime / (float) maxSteps;
        float esc = (float) count / ind.agents.size();
        float w = 1 / (1 + (float) ind.agents.size());
        res = new FitnessResult(esc * (1 - w) + time * w);
    }

}
