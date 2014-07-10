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

    protected float predatorDispersion;
    protected VectorBehaviourResult evaluation;

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
        float diagonal = (float) FastMath.sqrtQuick(FastMath.pow2(simState.field.width) * 2);
        
        predatorDispersion = Math.min(1, predatorDispersion / currentEvaluationStep / (diagonal / 2) / simState.predators.size());

        float finalDistance = 0;
        Prey prey = simState.preys.get(0);
        for(Predator pred : simState.predators) {
            finalDistance += prey.distanceTo(pred);
        }
        finalDistance = Math.min(1, finalDistance / (diagonal / 2) / simState.predators.size());
        
        float time = currentEvaluationStep / (float) maxEvaluationSteps;
        
        evaluation = new VectorBehaviourResult(simState.captureCount, finalDistance, time, predatorDispersion);
    }

    @Override
    public EvaluationResult getResult() {
        return evaluation;
    }
}
