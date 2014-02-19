/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.aggregation;

import java.util.List;
import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import net.jafama.FastMath;

/**
 *
 * @author jorge
 */
public class AggregationFitnessMax extends MasonEvaluation {

    private FitnessResult res;
    
    @Override
    protected void postSimulation() {
        Aggregation agg = (Aggregation) sim;
        double maxDist = Double.NEGATIVE_INFINITY;
        List<AggregationAgent> list = agg.agents;
        for(int i = 0 ; i < list.size() ; i++) {
            for(int j = i + 1 ; j < list.size() ; j++) {
                double d = list.get(i).distanceTo(list.get(j));
                maxDist = Math.max(d, maxDist);
            }
        }
        double diag = agg.par.size * FastMath.sqrtQuick(2);
        res = new FitnessResult((float) (1 - maxDist / diag));
    }

    @Override
    public EvaluationResult getResult() {
        return res;
    }
}
