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
public class OnePreyIndividualEvalSys extends MasonEvaluation {

    protected double[] agentY;
    protected double[] partnerAvgDist;
    protected SubpopEvaluationResult evaluation;

    @Override
    public void preSimulation() {
        super.preSimulation();
        PredatorPrey predSim = (PredatorPrey) sim;
        int nAgents = predSim.predators.size();
        agentY = new double[nAgents];
        partnerAvgDist = new double[nAgents];

    }

    @Override
    public void evaluate() {
        super.evaluate();
        PredatorPrey simState = (PredatorPrey) sim;
        for (int i = 0; i < simState.predators.size(); i++) {
            Predator pred = simState.predators.get(i);
            agentY[i] += pred.getLocation().y;
            for (Predator pOther : simState.predators) {
                if (pred != pOther) {
                    partnerAvgDist[i] += pred.distanceTo(pOther);
                }
            }
        }
    }

    @Override
    public void postSimulation() {
        super.postSimulation();
        PredatorPrey predSim = (PredatorPrey) sim;
        double diagonal =  FastMath.sqrtQuick(FastMath.pow2(predSim.field.width) * 2);

        VectorBehaviourResult[] res = new VectorBehaviourResult[predSim.predators.size()];
        Prey prey = predSim.preys.get(0);
        for (int i = 0; i < predSim.predators.size(); i++) {
            Predator pred = predSim.predators.get(i);
            agentY[i] =  (agentY[i] / predSim.field.height / currentEvaluationStep);
            agentY[i] = Math.max(0, Math.min(agentY[i], 1));
            partnerAvgDist[i] = Math.min(1, partnerAvgDist[i] / (diagonal / 2) / currentEvaluationStep / (predSim.predators.size() - 1));

            double preyDist =  Math.min(1, pred.distanceTo(prey) / (diagonal / 2));
            double boundsDist =  predSim.boundaries.closestDistance(pred.getLocation());
            boundsDist = Math.min(1, boundsDist / (diagonal / 2));

            res[i] = new VectorBehaviourResult(preyDist, partnerAvgDist[i], boundsDist, agentY[i]);
        }
        evaluation = new SubpopEvaluationResult(res);

    }

    @Override
    public EvaluationResult getResult() {
        return evaluation;
    }
}
