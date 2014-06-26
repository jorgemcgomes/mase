/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimulator;
import net.jafama.FastMath;
import sim.util.MutableDouble2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class OnePreyGroupEval extends MasonEvaluation {

    protected float diagonal;
    protected float predatorDispersion;
    protected float finalDistance;
    protected float captured;
    protected float simTime;
    protected VectorBehaviourResult evaluation;

    @Override
    public void preSimulation() {
        super.preSimulation();
        predatorDispersion = 0;
        diagonal = (float) FastMath.sqrtQuick(FastMath.pow2(((PredatorPrey) sim).field.width) * 2);
    }

    @Override
    public void evaluate() {
        super.evaluate();
        PredatorPrey simState = (PredatorPrey) sim;
        MutableDouble2D centerMass = new MutableDouble2D(0, 0);
        for (Predator pred : simState.predators) {
            centerMass.addIn(pred.getLocation());
        }
        centerMass.multiplyIn(1.0 / simState.predators.size());
        double disp = 0;
        for (Predator pred : simState.predators) {
            disp += centerMass.distance(pred.getLocation());
        }
        predatorDispersion += disp;
    }

    @Override
    public void postSimulation() {
        super.postSimulation();
        PredatorPrey simState = (PredatorPrey) sim;
        predatorDispersion = Math.min(1, predatorDispersion / currentEvaluationStep / (diagonal / 2) / simState.predators.size());
        Prey p = simState.preys.get(0);
        finalDistance = 0;
        for (Predator pred : simState.predators) {
            finalDistance += p.distanceTo(pred);
        }
        finalDistance = Math.min(1, finalDistance / (diagonal / 2) / simState.predators.size());
        captured = simState.getCaptureCount() / (float) simState.preys.size();
        simTime = simState.schedule.getSteps() / (float) maxSteps;
    }

    @Override
    public EvaluationResult getResult() {
        if (evaluation == null) {
            evaluation = new VectorBehaviourResult(new float[]{captured, simTime, finalDistance, predatorDispersion});
        }
        return evaluation;
    }
}
