/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

import mase.EvaluationResult;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class AgentEvaluationResult implements EvaluationResult {

    private EvaluationResult[] evals;

    public AgentEvaluationResult(EvaluationResult[] evals) {
        this.evals = evals;
    }

    public EvaluationResult getAgentEvaluation(int index) {
        return evals[index];
    }
    
    public EvaluationResult[] getAllEvaluations() {
        return evals;
    }

    @Override
    public Object value() {
        return evals;
    }

    @Override
    public AgentEvaluationResult mergeEvaluations(EvaluationResult[] results) {
        EvaluationResult[] merged = new EvaluationResult[((AgentEvaluationResult) results[0]).evals.length];
        for (int a = 0; a < merged.length; a++) {
            EvaluationResult[] agentEvals = new EvaluationResult[results.length];
            for (int i = 0; i < results.length; i++) {
                agentEvals[i] = ((AgentEvaluationResult) results[i]).evals[a];
            }
            merged[a] = agentEvals[0].mergeEvaluations(agentEvals);
        }
        return new AgentEvaluationResult(merged);
    }

    @Override
    public String toString() {
        String str = "";
        for(int i = 0 ; i < evals.length ; i++) {
            str += i+": " + evals[i].toString() + "\n";
        }
        return str;
    }
    
    
}
