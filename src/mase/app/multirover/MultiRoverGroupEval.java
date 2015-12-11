/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.multirover;

import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

/**
 *
 * @author jorge
 */
public class MultiRoverGroupEval extends MasonEvaluation {

    private static final long serialVersionUID = 1L;

    private VectorBehaviourResult br;
    private double dispersion;
    private double proximity;
    private Double2D[] lastPosition;
    private double movement;

    @Override
    public EvaluationResult getResult() {
        return br;
    }

    @Override
    protected void preSimulation() {
        super.preSimulation();
        dispersion = 0;
        proximity = 0;
        
        MultiRover mr = (MultiRover) sim;
        movement = 0;
        lastPosition = new Double2D[mr.rovers.size()];
        int i = 0;
        for(Rover r : mr.rovers) {
            lastPosition[i++] = r.getLocation();
        }
    }

    @Override
    protected void postSimulation() {
        MultiRover mr = (MultiRover) sim;
        br = new VectorBehaviourResult(mr.scores[2] / (double) mr.par.rocks.length,
                 (dispersion / mr.rovers.size() / currentEvaluationStep / (mr.par.size / 2)),
                 (movement / mr.rovers.size() / currentEvaluationStep / mr.par.linearSpeed),
                 (proximity / mr.rovers.size() / currentEvaluationStep / (mr.par.size / 4))
        );
    }

    @Override
    protected void evaluate() {
        MultiRover mr = (MultiRover) sim;
        MutableDouble2D cm = new MutableDouble2D();
        for (Rover r : mr.rovers) {
            cm.addIn(r.getLocation());
        }

        cm.multiplyIn(1.0 / mr.rovers.size());
        
        int i = 0;
        for (Rover r : mr.rovers) {
            dispersion += r.distanceTo(new Double2D(cm));

            double closest = Double.POSITIVE_INFINITY;
            for (RedRock rock : mr.rocks) {
                Double2D p = mr.field.getObjectLocation(rock);
                closest = Math.min(closest, r.distanceTo(p));
            }

            if (!Double.isInfinite(closest)) {
                proximity += closest;
            }
            
            movement += r.getLocation().distance(lastPosition[i]);
            lastPosition[i] = r.getLocation();
            i++;
        }
    }
}
