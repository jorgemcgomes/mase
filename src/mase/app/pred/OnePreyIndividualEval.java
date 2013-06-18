/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import ec.EvolutionState;
import ec.util.Parameter;
import java.util.Arrays;
import mase.evaluation.AgentEvaluationResult;
import mase.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class OnePreyIndividualEval extends MasonEvaluation {

    protected float diagonal;
    protected int nAgents;
    protected float maxSpeed;
    protected float[] initialDistance;
    protected float[] avgDistance;
    protected int avgDistanceSteps;
    protected int[] voidSteps;
    protected float[] minDistance;
    protected float[] captured;
    protected float[] movement;
    protected float[] angle;
    protected float[] partnerAvgDist;
    protected float[] partnerMinDist;
    protected AgentEvaluationResult evaluation;

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
        minDistance = new float[nAgents];
        Arrays.fill(minDistance, Float.POSITIVE_INFINITY);
        captured = new float[nAgents];
        movement = new float[nAgents];
        angle = new float[nAgents];
        initialDistance = new float[nAgents];
        partnerAvgDist = new float[nAgents];
        partnerMinDist = new float[nAgents];
        avgDistanceSteps = 0;
        voidSteps = new int[nAgents];
        PredatorPrey simState = (PredatorPrey) sim;
        for (int i = 0; i < simState.predators.size(); i++) {
            Predator pred = simState.predators.get(i);
            Prey prey = simState.preys.get(0);
            initialDistance[i] = (float) pred.distanceTo(prey);
            voidSteps[i] = Math.round(initialDistance[i] / maxSpeed);
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
            minDistance[i] = (float) Math.min(minDistance[i], d);
            if(simState.schedule.getSteps() > voidSteps[i]) {
                avgDistance[i] += d;
                avgDistanceSteps++;
            }
            double[] sens = pred.getLastSensors();
            angle[i] += (sens[1] + 1) / 2; // normalize from -1,1 to 0,1
            movement[i] += pred.getCurrentSpeed();
            float closest = Float.POSITIVE_INFINITY;
            for(Predator pOther : simState.predators) {
                if(pred != pOther) {
                    float dPred = (float) pred.distanceTo(pOther);
                    closest = Math.min(closest, dPred);
                    partnerAvgDist[i] += dPred;
                }
            }
            partnerMinDist[i] += closest;
        }
    }

    @Override
    public void postSimulation() {
        super.postSimulation();
        PredatorPrey simState = (PredatorPrey) sim;
        for (int i = 0; i < simState.predators.size(); i++) {
            Predator pred = simState.predators.get(i);
            captured[i] += pred.getCaptureCount() * nAgents;
            avgDistance[i] /= (diagonal / 2) * avgDistanceSteps;
            minDistance[i] /= initialDistance[i];
            angle[i] /= currentEvaluationStep;
            movement[i] /= currentEvaluationStep * maxSpeed;
            partnerAvgDist[i] /= (nAgents -1) * currentEvaluationStep * (diagonal / 2);
            partnerMinDist[i] /= currentEvaluationStep * (diagonal / 2);
        }
    }

    @Override
    public EvaluationResult getResult() {
        if (evaluation == null) {
            VectorBehaviourResult[] res = new VectorBehaviourResult[nAgents];
            for (int i = 0; i < res.length; i++) {
                float[] b = new float[]{avgDistance[i], minDistance[i], angle[i], captured[i], movement[i], partnerAvgDist[i], partnerMinDist[i]};
                res[i] = new VectorBehaviourResult(b);
            }
            evaluation = new AgentEvaluationResult(res);
        }
        return evaluation;
    }
}
