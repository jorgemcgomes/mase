/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import sim.util.MutableDouble2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class MultiPreyGroupEval extends OnePreyGroupEvalOriginal {

    protected float preyDispersion = 0;

    @Override
    public void preSimulation() {
        super.preSimulation();
        this.preyDispersion = 0;
    }

    @Override
    public void evaluate() {
        super.evaluate();
        PredatorPrey simState = (PredatorPrey) sim;
        if (!simState.activePreys.isEmpty()) {
            MutableDouble2D centerMass = new MutableDouble2D(0, 0);
            for (Prey prey : simState.activePreys) {
                centerMass.addIn(prey.getLocation());
            }
            centerMass.multiplyIn(1.0 / simState.preys.size());
            float d = 0;
            for (Prey prey : simState.activePreys) {
                d += centerMass.distance(prey.getLocation());
            }
            preyDispersion += d / simState.activePreys.size();
        } else {
            // This does not happen often. Add the value mean to not affect the measure.
            preyDispersion += preyDispersion / currentEvaluationStep;
        }
    }

    @Override
    public void postSimulation() {
        super.postSimulation();
        preyDispersion = Math.min(1, preyDispersion / currentEvaluationStep / (diagonal / 2));
    }

    @Override
    public EvaluationResult getResult() {
        if (evaluation == null) {
            evaluation = new VectorBehaviourResult(new float[]{captured, simTime, predatorDispersion, preyDispersion});
        }
        return evaluation;
    }
}
