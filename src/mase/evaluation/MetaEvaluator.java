/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

import ec.Evaluator;
import ec.EvolutionState;
import ec.util.Parameter;

/**
 *
 * @author jorge
 */
public class MetaEvaluator extends Evaluator {

    public static final String P_NUM_POST_EVAL = "num-post";
    public static final String P_POST_EVAL = "post";
    public static final String P_BASE_EVAL = "base";
    private static final long serialVersionUID = 1L;
    private Evaluator baseEvaluator;
    private PostEvaluator[] postEvaluators;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        baseEvaluator = (Evaluator) state.parameters.getInstanceForParameter(base.push(P_BASE_EVAL), null, Evaluator.class);
        baseEvaluator.setup(state, base.push(P_BASE_EVAL));
        this.masterproblem = baseEvaluator.masterproblem;
        this.p_problem = baseEvaluator.p_problem;
        
        int numPosts = state.parameters.getIntWithDefault(base.push(P_NUM_POST_EVAL), null, 0);
        postEvaluators = new PostEvaluator[numPosts];
        for (int i = 0; i < numPosts; i++) {
            Parameter b = base.push(P_POST_EVAL).push("" + i);
            postEvaluators[i] = (PostEvaluator) state.parameters.getInstanceForParameter(b, null, PostEvaluator.class);
            postEvaluators[i].setup(state, b);
        }
    }

    @Override
    public void evaluatePopulation(EvolutionState state) {
        baseEvaluator.evaluatePopulation(state);
        for (PostEvaluator postEval : postEvaluators) {
            postEval.processPopulation(state);
        }
        // necessary hack
        if (baseEvaluator instanceof CoevolutionaryEvaluator) {            
            ((CoevolutionaryEvaluator) baseEvaluator).afterCoevolutionaryEvaluation(state, state.population, null);
        }
    }

    @Override
    public boolean runComplete(EvolutionState state) {
        return baseEvaluator.runComplete(state);
    }

    @Override
    public void initializeContacts(EvolutionState state) {
        baseEvaluator.initializeContacts(state);
    }

    @Override
    public void reinitializeContacts(EvolutionState state) {
        baseEvaluator.reinitializeContacts(state);
    }

    @Override
    public void closeContacts(EvolutionState state, int result) {
        baseEvaluator.closeContacts(state, result);
    }

    public Evaluator getBaseEvaluator() {
        return baseEvaluator;
    }

    public PostEvaluator[] getPostEvaluators() {
        return postEvaluators;
    }
}
