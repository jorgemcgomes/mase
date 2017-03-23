/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.generic.systematic.TaskDescription;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import net.jafama.FastMath;
import sim.util.MutableDouble2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class OnePreyGroupEval extends MasonEvaluation {

    private static final long serialVersionUID = 1L;

    protected double predatorDispersion;
    protected VectorBehaviourResult evaluation;
    protected double diagonal;
    protected double time;
    protected int captured;

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(null);
        predatorDispersion = 0;
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(null);     
        PredatorPrey pp = (PredatorPrey) sim;
        MutableDouble2D center = new MutableDouble2D(0,0);
        for(Predator p : pp.predators) {
            center.addIn(p.getCenterLocation());
        }
        center.multiplyIn(1.0 / pp.predators.size());
        for(Predator p : pp.predators) {
            predatorDispersion += center.distance(p.getCenterLocation());
        }
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(null);
        
        PredatorPrey simState = (PredatorPrey) sim;
        diagonal =  FastMath.sqrtQuick(FastMath.pow2(simState.field.width) * 2);
        
        predatorDispersion = Math.min(1, predatorDispersion / currentEvaluationStep / (diagonal / 2) / simState.predators.size());

        double finalDistance = 0;
        Prey prey = simState.preys.get(0);
        for(Predator pred : simState.predators) {
            finalDistance += prey.distanceTo(pred);
        }
        finalDistance = Math.min(1, finalDistance / (diagonal / 2) / simState.predators.size());
        
        time = currentEvaluationStep / (double) maxEvaluationSteps;
        captured = simState.captureCount;
        
        evaluation = new VectorBehaviourResult((double)captured, finalDistance, time, predatorDispersion);
    }

    @Override
    public EvaluationResult getResult() {
        return evaluation;
    }
}
