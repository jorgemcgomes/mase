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
import sim.util.MutableDouble2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class OnePreyGroupEval extends MasonEvaluation {

    protected float diagonal;
    protected int nAgents;
    protected int maxSteps;
    protected float avgDistance;
    protected float predatorDispersion;
    protected float maxPredatorDispersion;
    protected float finalDistance;
    protected float captured;
    protected float simTime;
    protected VectorBehaviourResult evaluation;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.nAgents = state.parameters.getInt(base.pop().pop().push(PredParams.P_NPREDATORS), null);
        float size = state.parameters.getInt(base.pop().pop().push(PredParams.P_SIZE), null);
        this.diagonal = (float) Math.sqrt(Math.pow(size, 2) * 2);
        this.maxSteps = state.parameters.getInt(base.pop().push(MasonSimulator.P_MAX_STEPS), base.pop().pop().push(MasonSimulator.P_MAX_STEPS));
    }

    @Override
    public void preSimulation() {
        super.preSimulation();
        avgDistance = 0;
        predatorDispersion = 0;
        maxPredatorDispersion = 0;
    }

    @Override
    public void evaluate() {
        super.evaluate();
        PredatorPrey simState = (PredatorPrey) sim;
        Prey prey = simState.preys.get(0);
        MutableDouble2D centerMass = new MutableDouble2D(0, 0);
        for (Predator pred : simState.predators) {
            avgDistance += pred.distanceTo(prey);
            centerMass.addIn(pred.getLocation());
        }
        centerMass.multiplyIn(1.0 / simState.predators.size());
        double disp = 0;
        for (Predator pred : simState.predators) {
            disp += centerMass.distance(pred.getLocation());
        }
        maxPredatorDispersion = (float) Math.max(maxPredatorDispersion, disp);
        predatorDispersion += disp;
    }

    @Override
    public void postSimulation() {
        super.postSimulation();
        PredatorPrey simState = (PredatorPrey) sim;
        avgDistance = Math.min(1, avgDistance / currentEvaluationStep / (diagonal / 2) / simState.predators.size());
        predatorDispersion = Math.min(1, predatorDispersion / currentEvaluationStep / (diagonal / 2) / simState.predators.size());
        maxPredatorDispersion = Math.min(1, maxPredatorDispersion / (diagonal / 2) / simState.predators.size());

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
            evaluation = new VectorBehaviourResult(new float[]{captured, simTime, finalDistance, avgDistance, predatorDispersion, maxPredatorDispersion});
        }
        return evaluation;
    }
}
