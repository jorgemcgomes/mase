/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;
import mase.app.pred.PredatorPrey;
import mase.app.pred.Prey;


import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.generic.systematic.EntityGroup;
import mase.generic.systematic.TaskDescription;
import mase.mason.MasonEvaluation;
import net.jafama.FastMath;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class OnePreyGroupEvalSys extends MasonEvaluation {

    protected float predatorDispersion;
    protected float predatorY;
    protected VectorBehaviourResult evaluation;

    @Override
    public void preSimulation() {
        super.preSimulation();
        predatorDispersion = 0;
        predatorY = 0;
    }

    @Override
    public void evaluate() {
        super.evaluate();        
        TaskDescription td = ((PredatorPrey) sim).getTaskDescription();
        predatorDispersion += td.distanceFunction().distance(td.groups()[0], td.groups()[0]);
        for(Predator pred : ((PredatorPrey) sim).predators) {
            predatorY += pred.getLocation().y;
        }
    }

    @Override
    public void postSimulation() {
        super.postSimulation();
        
        PredatorPrey simState = (PredatorPrey) sim;
        TaskDescription td = simState.getTaskDescription();
        float diagonal = (float) FastMath.sqrtQuick(FastMath.pow2(simState.field.width) * 2);
        
        predatorDispersion = Math.min(1, predatorDispersion / currentEvaluationStep / (diagonal / 2) / simState.predators.size());
        predatorY = (float) (predatorY / simState.predators.size() / simState.field.height / currentEvaluationStep);
        predatorY = Math.max(0, Math.min(predatorY, 1));
        
        float finalDistance = 0;
        Prey prey = simState.preys.get(0);
        for(Predator pred : simState.predators) {
            finalDistance += prey.distanceTo(pred);
        }
        finalDistance = Math.min(1, finalDistance / (diagonal / 2) / simState.predators.size());
        
        float boundDistance = (float) td.distanceFunction().distance(td.groups()[0], td.groups()[2]);
        boundDistance = Math.min(1, boundDistance / (diagonal / 2));
        
        evaluation = new VectorBehaviourResult(finalDistance, predatorDispersion, boundDistance, predatorY);
    }

    @Override
    public EvaluationResult getResult() {
        return evaluation;
    }
}
