/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import mase.evaluation.SubpopEvaluationResult;
import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import net.jafama.FastMath;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class OnePreyIndividualEval extends MasonEvaluation {

    protected float diagonal;
    protected float[] avgDistance;
    protected int avgDistanceSteps;
    protected int[] voidSteps;
    protected float[] captured;
    protected float[] movement;
    protected float[] partnerAvgDist;
    protected SubpopEvaluationResult evaluation;

    @Override
    public void preSimulation() {
        super.preSimulation();
        PredatorPrey predSim = (PredatorPrey) sim;
        int nAgents = predSim.predators.size();
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
            voidSteps[i] = (int) Math.round(d / predSim.par.predatorSpeed);
        }
        diagonal = (float) FastMath.sqrtQuick(FastMath.pow2(((PredatorPrey) sim).field.width) * 2);
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
        PredatorPrey predSim = (PredatorPrey) sim;
        for (int i = 0; i < predSim.predators.size(); i++) {
            Predator pred = predSim.predators.get(i);
            captured[i] += pred.getCaptureCount();
            avgDistance[i] /= (diagonal / 2) * avgDistanceSteps;
            movement[i] /= currentEvaluationStep * predSim.par.predatorSpeed;
            partnerAvgDist[i] /= (predSim.predators.size() -1) * currentEvaluationStep * (diagonal / 2);
        }
    }

    @Override
    public EvaluationResult getResult() {
        if (evaluation == null) {
            VectorBehaviourResult[] res = new VectorBehaviourResult[avgDistance.length];
            for (int i = 0; i < res.length; i++) {
                float[] b = new float[]{captured[i], avgDistance[i], movement[i], partnerAvgDist[i]};
                res[i] = new VectorBehaviourResult(b);
            }
            evaluation = new SubpopEvaluationResult(res);
        }
        return evaluation;
    }
}
