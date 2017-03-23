/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.indiana;

import ec.EvolutionState;
import ec.Problem;
import ec.util.Parameter;
import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import mase.mason.MasonSimulationProblem;
import sim.util.MutableDouble2D;

/**
 *
 * @author jorge
 */
public class IndianaBehaviourEval extends MasonEvaluation {

    private VectorBehaviourResult res;
    private double avgDist = 0;
    private double avgDisp = 0;
    private int maxSteps;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.maxSteps = state.parameters.getInt(new Parameter(MasonSimulationProblem.P_PROBLEM).push(MasonSimulationProblem.P_MAX_STEPS), null);
    }

    @Override
    protected void preSimulation(MasonSimState sim) {
        this.avgDisp = 0;
        this.avgDist = 0;
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        Indiana ind = (Indiana) sim;
        MutableDouble2D centre = new MutableDouble2D(0, 0);
        for (IndianaAgent a : ind.agents) {
            avgDist += a.getCenterLocation().distance(ind.gate.getCenter());
            centre.addIn(a.getCenterLocation());
        }
        centre.multiplyIn(1.0 / ind.agents.size());
        for (IndianaAgent a : ind.agents) {
            avgDisp += centre.distance(a.getCenterLocation());
        }
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        int count = 0;
        Indiana ind = (Indiana) sim;
        for (IndianaAgent a : ind.agents) {
            if (a.escaped) {
                count++;
            }
        }
        res = new VectorBehaviourResult(
                (double) count / ind.agents.size(),
                ind.gate.openTime == -1 ? 1 : (double) ind.gate.openTime / maxSteps,
                 (avgDist / ind.agents.size() / currentEvaluationStep / ind.par.size),
                 (avgDisp / ind.agents.size() / currentEvaluationStep / (ind.par.size / 2)));
    }

    @Override
    public EvaluationResult getResult() {
        return res;
    }
}
