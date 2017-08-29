/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

import ec.Evaluator;
import ec.EvolutionState;
import ec.util.Parameter;

/**
 * A wrapper for another Evaluator, that adds the support of PostEvaluators
 * @author jorge
 */
public class MetaEvaluator extends Evaluator {

    public static final String P_NUM_POST_EVAL = "num-post";
    public static final String P_POST_EVAL = "post";
    public static final String P_BASE_EVAL = "base";

    private static final long serialVersionUID = 1L;
    protected Evaluator baseEvaluator;
    protected PostEvaluator[] postEvaluators;
    public long lastEvaluationTime = 0;
    public long lastPostEvaluationTime = 0;

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
        long t1 = System.currentTimeMillis();
        baseEvaluator.evaluatePopulation(state);
        long t2 = System.currentTimeMillis();        
        for (PostEvaluator postEval : postEvaluators) {
            postEval.processPopulation(state);
        }
        long t3 = System.currentTimeMillis();
        lastEvaluationTime = t2 - t1;
        lastPostEvaluationTime = t3 - t2;
        // necessary to load the elites in the CoevolutionaryEvaluator
        if (baseEvaluator instanceof MultiPopCoevolutionaryEvaluatorThreaded) {            
            ((MultiPopCoevolutionaryEvaluatorThreaded) baseEvaluator).forceAfterCoevolutionaryEvaluation(state, state.population, null);
        }
    }

    @Override
    public String runComplete(EvolutionState state) {
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
