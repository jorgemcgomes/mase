/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evaluation;

import ec.Evaluator;
import ec.EvolutionState;
import ec.Subpopulation;
import ec.util.Parameter;

/**
 *
 * @author jorge
 */
public class MetaEvaluator extends Evaluator {

    public static final String P_NUM_POST_EVAL = "num-post";
    public static final String P_POST_EVAL = "post";
    public static final String P_BASE_EVAL = "base";
    public static final String P_MAX_EVALUATIONS = "max-evaluations";

    private static final long serialVersionUID = 1L;
    private Evaluator baseEvaluator;
    private PostEvaluator[] postEvaluators;
    public int totalEvaluations;
    public int maxEvaluations;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        baseEvaluator = (Evaluator) state.parameters.getInstanceForParameter(base.push(P_BASE_EVAL), null, Evaluator.class);
        baseEvaluator.setup(state, base.push(P_BASE_EVAL));
        this.masterproblem = baseEvaluator.masterproblem;
        this.p_problem = baseEvaluator.p_problem;
    
        maxEvaluations = state.parameters.getIntWithDefault(base.push(P_MAX_EVALUATIONS), null, -1);
        totalEvaluations = 0;
        
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
        for(Subpopulation sub : state.population.subpops) {
            totalEvaluations += sub.individuals.length;
        }
        // necessary hack to load the elites in the CoevolutionaryEvaluator
        if (baseEvaluator instanceof CoevolutionaryEvaluator) {            
            ((CoevolutionaryEvaluator) baseEvaluator).afterCoevolutionaryEvaluation(state, state.population, null);
        }
    }

    @Override
    public boolean runComplete(EvolutionState state) {
        return baseEvaluator.runComplete(state) || (maxEvaluations != -1 && totalEvaluations >= maxEvaluations);
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
