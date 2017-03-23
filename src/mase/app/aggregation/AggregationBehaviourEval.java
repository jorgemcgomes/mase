/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.aggregation;

import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import sim.util.MutableDouble2D;

/**
 *
 * @author jorge
 */
public class AggregationBehaviourEval extends MasonEvaluation {

    private double[] centerMass;
    private VectorBehaviourResult vbr;

    @Override
    protected void preSimulation(MasonSimState sim) {
        this.centerMass = new double[super.maxEvaluationSteps];
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        Aggregation agg = (Aggregation) sim;
        MutableDouble2D centre = new MutableDouble2D(0, 0);
        for (AggregationAgent aa : agg.agents) {
            centre.addIn(aa.getCenterLocation());
        }
        centre.multiplyIn(1.0 / agg.agents.size());
        double dist = 0;
        for (AggregationAgent aa : agg.agents) {
            dist += centre.distance(aa.getCenterLocation());
        }
        dist = dist / agg.agents.size() / agg.par.size;
        centerMass[super.currentEvaluationStep] = dist;
    }

    @Override
    public EvaluationResult getResult() {
        if (vbr == null) {
            vbr = new VectorBehaviourResult(centerMass);
        }
        return vbr;
    }
}
