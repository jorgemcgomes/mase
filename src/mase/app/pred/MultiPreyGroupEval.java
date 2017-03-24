/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonSimState;
import sim.util.MutableDouble2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class MultiPreyGroupEval extends OnePreyGroupEval {

    private static final long serialVersionUID = 1L;

    protected double preyDispersion = 0;

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(null);
        this.preyDispersion = 0;
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(null);
        PredatorPrey simState = (PredatorPrey) sim;
        if (!simState.activePreys.isEmpty()) {
            MutableDouble2D centerMass = new MutableDouble2D(0, 0);
            for (Prey prey : simState.activePreys) {
                centerMass.addIn(prey.getLocation());
            }
            centerMass.multiplyIn(1.0 / simState.preys.size());
            double d = 0;
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
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(null);
        preyDispersion = Math.min(1, preyDispersion / currentEvaluationStep / (diagonal / 2));
    }

    @Override
    public EvaluationResult getResult() {
        if (evaluation == null) {
            evaluation = new VectorBehaviourResult((double)captured, time, predatorDispersion, preyDispersion);
        }
        return evaluation;
    }
}
