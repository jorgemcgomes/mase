/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.multirover;

import mase.evaluation.EvaluationResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class MultiRoverIndEval extends MasonEvaluation {

    private static final long serialVersionUID = 1L;
    
    private SubpopEvaluationResult br;
    private double dispersion;
    private double[] proximity;
    private Double2D[] lastPosition;
    private double[] movement;
    private int[] type1Active;
    private int[] type2Active;

    @Override
    public EvaluationResult getResult() {
        return br;
    }

    @Override
    protected void preSimulation() {
        super.preSimulation();
        MultiRover mr = (MultiRover) sim;

        dispersion = 0;
        proximity = new double[mr.rovers.size()];
        movement = new double[mr.rovers.size()];
        lastPosition = new Double2D[mr.rovers.size()];
        type1Active = new int[mr.rovers.size()];
        type2Active = new int[mr.rovers.size()];
        
        int i = 0;
        for(Rover r : mr.rovers) {
            lastPosition[i++] = r.getLocation();
        }
    }

    @Override
    protected void postSimulation() {
        MultiRover mr = (MultiRover) sim;
        VectorBehaviourResult[] res = new VectorBehaviourResult[mr.rovers.size()];
        
        for(int i = 0 ; i < mr.rovers.size() ; i++) {
            res[i] = new VectorBehaviourResult( (movement[i] / currentEvaluationStep / mr.par.linearSpeed),
                     (proximity[i] / currentEvaluationStep / (mr.par.size / 4)),
                    (double) type1Active[i] / currentEvaluationStep,
                    (double) type2Active[i] / currentEvaluationStep
            );
        }
        this.br = new SubpopEvaluationResult(res);
    }

    @Override
    protected void evaluate() {
        MultiRover mr = (MultiRover) sim;
        /*MutableDouble2D cm = new MutableDouble2D();
        for (Rover r : mr.rovers) {
            cm.addIn(r.getLocation());
        }

        cm.multiplyIn(1.0 / mr.rovers.size());*/
        
        int i = 0;
        for (Rover r : mr.rovers) {
            //dispersion += r.distanceTo(new Double2D(cm));

            double closest = Double.POSITIVE_INFINITY;
            for (Rock rock : mr.rocks) {
                Double2D p = mr.field.getObjectLocation(rock);
                closest = Math.min(closest, r.distanceTo(p));
            }

            if (!Double.isInfinite(closest)) {
                proximity[i] += closest;
            }
            
            movement[i] += r.getLocation().distance(lastPosition[i]);
            lastPosition[i] = r.getLocation();
            
            type1Active[i] += r.getActuatorType() == 0 ? 1 : 0;
            type2Active[i] += r.getActuatorType() == 1 ? 1 : 0;
            
            i++;
        }
    }
}
