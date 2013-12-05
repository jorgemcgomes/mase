/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class MultiPreyGroupEvalSimple extends MultiPreyGroupEval {

    @Override
    public EvaluationResult getResult() {
        if (evaluation == null) {
            evaluation = new VectorBehaviourResult(new float[]{captured, simTime, preyDispersion});
        }
        return evaluation;
    }
}
