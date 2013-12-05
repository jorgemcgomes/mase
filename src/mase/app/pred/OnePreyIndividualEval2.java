/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.evaluation.SubpopEvaluationResult;
import mase.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class OnePreyIndividualEval2 extends MasonEvaluation {

    protected float diagonal;
    protected int nAgents;
    protected float maxSpeed;
    protected float[] avgDistance;
    protected int avgDistanceSteps;
    protected int[] voidSteps;
    protected float[] captured;
    protected float[] movement;
    protected float[] partnerAvgDist;
    protected SubpopEvaluationResult evaluation;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.nAgents = state.parameters.getInt(base.pop().pop().push(PredParams.P_NPREDATORS), null);
        this.maxSpeed = state.parameters.getFloat(base.pop().pop().push(PredParams.P_PREDATOR_SPEED), null);
        float size = state.parameters.getInt(base.pop().pop().push(PredParams.P_SIZE), null);
        this.diagonal = (float) Math.sqrt(Math.pow(size, 2) * 2);
    }

    @Override
    public void preSimulation() {
        super.preSimulation();
        avgDistance = new float[nAgents];
        captured = new float[nAgents];
        movement = new float[nAgents];
        partnerAvgDist = new float[nAgents];
        avgDistanceSteps = 0;
        voidSteps = new int[nAgents];
        PredatorPrey simState = (PredatorPrey) sim;
        for (int i = 0; i < simState.predators.size(); i++) {
            Predator pred = simState.predators.get(i);
            Prey prey = simState.preys.get(0);
            double d = pred.distanceTo(prey);
            voidSteps[i] = (int) Math.round(d / maxSpeed);
        }
    }

    @Override
    public void evaluate() {
        super.evaluate();
        PredatorPrey simState = (PredatorPrey) sim;
        for (int i = 0; i < simState.predators.size(); i++) {
            Predator pred = simState.predators.get(i);
            Prey prey = simState.preys.get(0);
            double d = pred.distanceTo(prey);
            if(simState.schedule.getSteps() > voidSteps[i]) {
                avgDistance[i] += d;
                avgDistanceSteps++;
            }
            movement[i] += pred.getSpeed();
            float closest = Float.POSITIVE_INFINITY;
            for(Predator pOther : simState.predators) {
                if(pred != pOther) {
                    float dPred = (float) pred.distanceTo(pOther);
                    closest = Math.min(closest, dPred);
                    partnerAvgDist[i] += dPred;
                }
            }
        }
    }

    @Override
    public void postSimulation() {
        super.postSimulation();
        PredatorPrey simState = (PredatorPrey) sim;
        for (int i = 0; i < simState.predators.size(); i++) {
            Predator pred = simState.predators.get(i);
            captured[i] += pred.getCaptureCount();
            avgDistance[i] /= (diagonal / 2) * avgDistanceSteps;
            movement[i] /= currentEvaluationStep * maxSpeed;
            partnerAvgDist[i] /= (nAgents -1) * currentEvaluationStep * (diagonal / 2);
        }
    }

    @Override
    public EvaluationResult getResult() {
        if (evaluation == null) {
            VectorBehaviourResult[] res = new VectorBehaviourResult[nAgents];
            for (int i = 0; i < res.length; i++) {
                float[] b = new float[]{captured[i], avgDistance[i], movement[i], partnerAvgDist[i]};
                res[i] = new VectorBehaviourResult(b);
            }
            evaluation = new SubpopEvaluationResult(res);
        }
        return evaluation;
    }
}
