/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.aggregation;

import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import sim.util.MutableDouble2D;

/**
 *
 * @author jorge
 */
public class AggregationBehaviourEval extends MasonEvaluation {

    private float[] centerMass;
    private VectorBehaviourResult vbr;

    @Override
    protected void preSimulation() {
        this.centerMass = new float[super.maxEvaluationSteps];
    }

    @Override
    protected void evaluate() {
        Aggregation agg = (Aggregation) sim;
        MutableDouble2D centre = new MutableDouble2D(0, 0);
        for (AggregationAgent aa : agg.agents) {
            centre.addIn(aa.getLocation());
        }
        centre.multiplyIn(1.0 / agg.agents.size());
        double dist = 0;
        for (AggregationAgent aa : agg.agents) {
            dist += centre.distance(aa.getLocation());
        }
        dist = dist / agg.agents.size() / agg.par.size;
        centerMass[super.currentEvaluationStep] = (float) dist;
    }

    @Override
    public EvaluationResult getResult() {
        if (vbr == null) {
            vbr = new VectorBehaviourResult(centerMass);
        }
        return vbr;
    }
}
