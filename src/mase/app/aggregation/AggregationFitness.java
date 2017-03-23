/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.aggregation;

import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import sim.util.MutableDouble2D;

/**
 *
 * @author jorge
 */
public class AggregationFitness extends MasonEvaluation {

    private FitnessResult res;
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        Aggregation agg = (Aggregation) sim;
        MutableDouble2D centre = new MutableDouble2D(0,0);
        for(AggregationAgent aa : agg.agents) {
            centre.addIn(aa.getCenterLocation());
        }
        centre.multiplyIn(1.0 / agg.agents.size());
        double dist = 0;
        for(AggregationAgent aa : agg.agents) {
            dist += centre.distance(aa.getCenterLocation());
        }
        dist = dist / agg.agents.size() / agg.par.size;
        res = new FitnessResult(Math.max(0, 1 -  dist));
    }

    @Override
    public EvaluationResult getResult() {
        return res;
    }
}
