/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.herding;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;

/**
 *
 * @author jorge
 */
public class HerdingFitnessBootstrap extends MasonEvaluation {

    private static final long serialVersionUID = 1L;

    private FitnessResult res;
    private Map<Sheep, Double> initialDistances;
    private double[] closestShepherd;

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(null);
        Herding herd = (Herding) sim;
        initialDistances = new HashMap<>();
        for(Sheep s : herd.sheeps) {
            initialDistances.put(s, s.distanceTo(((Herding) sim).curralCenter));
        }
        closestShepherd = new double[herd.sheeps.size()];
        Arrays.fill(closestShepherd, Double.POSITIVE_INFINITY);
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(null);
        Herding herd = (Herding) sim;
        for(int i = 0 ; i < herd.sheeps.size() ; i++) {
            for(Shepherd shep : herd.shepherds) {
                closestShepherd[i] = Math.min(closestShepherd[i], herd.sheeps.get(i).distanceTo(shep));
            }
        }
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(null);
        Herding herd = (Herding) sim;
        double fitness = 0;
        for(Sheep sheep : herd.sheeps) {
            if (sheep.corraledTime > 0) { // sheep curraled
                fitness += 2 - sheep.corraledTime / (double) maxSteps;
            } else {
                fitness += Math.max(0, 1 - sheep.distanceTo(((Herding) sim).curralCenter) / initialDistances.get(sheep));
            }      
        }
        double closest = 0;
        for(double d : closestShepherd) {
            closest += d / herd.field.width;
        }
        fitness += (1 - closest / closestShepherd.length);
        res = new FitnessResult( fitness);
    }

    @Override
    public EvaluationResult getResult() {
        return res;
    }

}
