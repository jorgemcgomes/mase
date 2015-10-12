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
public class OnePreyIndividualEvalOriginal extends MasonEvaluation {

    protected double diagonal;
    protected double[] avgDistance;
    protected int avgDistanceSteps;
    protected int[] voidSteps;
    protected double[] captured;
    protected double[] movement;
    protected double[] partnerAvgDist;
    protected SubpopEvaluationResult evaluation;

    @Override
    public void preSimulation() {
        super.preSimulation();
        PredatorPrey predSim = (PredatorPrey) sim;
        int nAgents = predSim.predators.size();
        avgDistance = new double[nAgents];
        captured = new double[nAgents];
        movement = new double[nAgents];
        partnerAvgDist = new double[nAgents];
        avgDistanceSteps = 0;
        voidSteps = new int[nAgents];
        PredatorPrey simState = (PredatorPrey) sim;
        for (int i = 0; i < simState.predators.size(); i++) {
            Predator pred = simState.predators.get(i);
            Prey prey = simState.preys.get(0);
            double d = pred.distanceTo(prey);
            voidSteps[i] = (int) Math.round(d / predSim.par.predatorSpeed);
        }
        diagonal =  FastMath.sqrtQuick(FastMath.pow2(((PredatorPrey) sim).field.width) * 2);
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
            double closest = double.POSITIVE_INFINITY;
            for(Predator pOther : simState.predators) {
                if(pred != pOther) {
                    double dPred =  pred.distanceTo(pOther);
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
                double[] b = new double[]{captured[i], avgDistance[i], movement[i], partnerAvgDist[i]};
                res[i] = new VectorBehaviourResult(b);
            }
            evaluation = new SubpopEvaluationResult(res);
        }
        return evaluation;
    }
}
