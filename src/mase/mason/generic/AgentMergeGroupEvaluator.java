/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.generic;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.evaluation.EvaluationResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;

/**
 *
 * @author jorge
 */
public class AgentMergeGroupEvaluator extends MasonEvaluation {

    public static final String P_AGENT_EVAL = "behaviour-index";
    private static final long serialVersionUID = 1L;
    private int agEvalIndex = -1;
    private EvaluationResult res = null;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        agEvalIndex = state.parameters.getInt(base.push(P_AGENT_EVAL), null);
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        // Get result from the other
        SubpopEvaluationResult ser = (SubpopEvaluationResult) sim.currentEvals[agEvalIndex].getResult();
        // merge
        EvaluationResult[] ers = ser.getAllEvaluations().toArray(new EvaluationResult[0]);
        res = ers[0].mergeEvaluations(ers);
    }

    @Override
    public EvaluationResult getResult() {
        return res;
    }
}
