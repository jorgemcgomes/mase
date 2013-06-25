/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.EvaluationResult;
import mase.ExpandedFitness;
import mase.evaluation.AgentEvaluationResult;
import mase.evaluation.BehaviourResult;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class NoveltyFitness extends ExpandedFitness {
    
    public static final String P_NOVELTY_EVAL_INDEX = "novelty-index";    
    protected int noveltyIndex;
    protected double noveltyScore;
    protected int repoComparisons;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base); 
        this.noveltyIndex = state.parameters.getIntWithDefault(base.push(P_NOVELTY_EVAL_INDEX), defaultBase().push(P_NOVELTY_EVAL_INDEX), 1);
    }
    
    public BehaviourResult getNoveltyBehaviour() {
        EvaluationResult er = super.evalResults[noveltyIndex];
        BehaviourResult br;
        if(er instanceof AgentEvaluationResult) {
            AgentEvaluationResult aer = (AgentEvaluationResult) er;
            br = (BehaviourResult) aer.getAgentEvaluation(super.getCorrespondingSubpop());
        } else {
            br = (BehaviourResult) er;
        }
        return br;
    }
    
}
