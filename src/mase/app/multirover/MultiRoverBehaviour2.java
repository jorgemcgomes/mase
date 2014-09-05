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
public class MultiRoverBehaviour2 extends MasonEvaluation {

    private VectorBehaviourResult br;
    private double activeTime;
    private double dispersion;

    @Override
    public EvaluationResult getResult() {
        return br;
    }

    @Override
    protected void preSimulation() {
        super.preSimulation();
        activeTime = 0;
        dispersion = 0;
    }

    @Override
    protected void postSimulation() {
        MultiRover mr = (MultiRover) sim;
        br = new VectorBehaviourResult(mr.scores[2] / (float) mr.par.numRocks, (float) activeTime / mr.rovers.size() / maxSteps, (float) (dispersion / mr.rovers.size() / maxSteps / mr.par.size));
    }

    @Override
    protected void evaluate() {
        MultiRover mr = (MultiRover) sim;
        MutableDouble2D cm = new MutableDouble2D();
        for (Rover r : mr.rovers) {
            if (r.getActuatorType() != 0) {
                activeTime++;
            }
            cm.addIn(r.getLocation());
        }
        
        cm.multiplyIn(1.0 / mr.rovers.size());
        for(Rover r : mr.rovers) {
            dispersion += r.distanceTo(new Double2D(cm));
        }
    }
}
