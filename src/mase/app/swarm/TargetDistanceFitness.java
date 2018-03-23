/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.swarm;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.mason.MasonSimState;

/**
 *
 * @author jorge
 */
public class TargetDistanceFitness extends SwarmFitness {

    public static final String P_TARGET_DISTANCE = "target-dist";
    private static final long serialVersionUID = 1L;
    private double targetDist;
    private double sum;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.targetDist = state.parameters.getDouble(base.push(P_TARGET_DISTANCE), null);
    }    
    
    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(sim);
        sum = 0;
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(sim);
        SwarmPlayground agg = (SwarmPlayground) sim;
        double distanceDelta = 0;
        for (SwarmAgent a : agg.agents) {
            double minDist = Double.POSITIVE_INFINITY;
            for (SwarmAgent b : agg.agents) {
                if (a != b) {
                	minDist = Math.min(minDist, a.distanceTo(b));
                }
            }
            distanceDelta += Math.abs(minDist - targetDist);
        }
        sum += distanceDelta / agg.agents.size() / agg.field.width;
    }

    @Override
    protected double getFinalTaskFitness(SwarmPlayground sim) {
        return 1 - sum / super.currentEvaluationStep;
    }
}
