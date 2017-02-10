/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.herding;

import java.util.HashMap;
import java.util.Map;
import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class HerdingFitness extends MasonEvaluation {

    private static final long serialVersionUID = 1L;

    private FitnessResult res;
    private Map<Sheep, Double> initialDistances;
    private Double2D gate;

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(null);
        Herding herd = (Herding) sim;
        gate = new Double2D(herd.par.arenaSize, herd.par.arenaSize / 2);
        initialDistances = new HashMap<>();
        for(Sheep s : herd.sheeps) {
            initialDistances.put(s, s.distanceTo(gate));
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
                fitness += Math.max(0, 1 - sheep.distanceTo(gate) / initialDistances.get(sheep));
            }      
        }
        res = new FitnessResult( fitness, FitnessResult.ARITHMETIC);
    }

    @Override
    public EvaluationResult getResult() {
        return res;
    }

}
