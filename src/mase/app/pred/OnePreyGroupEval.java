/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.generic.systematic.TaskDescription;
import mase.mason.MasonEvaluation;
import net.jafama.FastMath;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class OnePreyGroupEval extends MasonEvaluation {

    protected double predatorDispersion;
    protected VectorBehaviourResult evaluation;
    protected double diagonal;
    protected double time;
    protected int captured;

    @Override
    public void preSimulation() {
        super.preSimulation();
        predatorDispersion = 0;
    }

    @Override
    public void evaluate() {
        super.evaluate();        
        TaskDescription td = ((PredatorPrey) sim).getTaskDescription();
        predatorDispersion += td.distanceFunction().distance(td.groups()[0], td.groups()[0]);
    }

    @Override
    public void postSimulation() {
        super.postSimulation();
        
        PredatorPrey simState = (PredatorPrey) sim;
        TaskDescription td = simState.getTaskDescription();
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
        
        evaluation = new VectorBehaviourResult(captured, finalDistance, time, predatorDispersion);
    }

    @Override
    public EvaluationResult getResult() {
        return evaluation;
    }
}
