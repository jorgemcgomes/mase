/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty;

import mase.evaluation.EvaluationResult;
import mase.evaluation.ExpandedFitness;
import mase.evaluation.SubpopEvaluationResult;
import mase.evaluation.BehaviourResult;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class NoveltyFitness extends ExpandedFitness {
    
    public static final String NOVELTY_SCORE = "novelty";
    
    public BehaviourResult getBehaviour(int index) {
        EvaluationResult er = super.evalResults[index];
        BehaviourResult br;
        if (er instanceof SubpopEvaluationResult) {
            SubpopEvaluationResult aer = (SubpopEvaluationResult) er;
            br = (BehaviourResult) aer.getSubpopEvaluation(super.getCorrespondingSubpop());
        } else {
            br = (BehaviourResult) er;
        }
        return br;
    }
    
    public float getNoveltyScore() {
        return scores.get(NOVELTY_SCORE);
    }
}
