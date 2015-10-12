/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.sharing;

import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;

/**
 *
 * @author jorge
 */
public class RSBehaviourEval extends MasonEvaluation {

    private double survivors;
    private double averageEnergy;
    private double averageMovement;
    private double averageDistance;
    private int totalEvals = 0;
    private VectorBehaviourResult vbr;

    @Override
    protected void preSimulation() {
        totalEvals = 0;
        averageMovement = 0;
        averageEnergy = 0;
        averageDistance = 0;
    }

    @Override
    protected void evaluate() {
        ResourceSharing rs = (ResourceSharing) sim;
        for (RSAgent a : rs.agents) {
            if (a.isAlive()) {
                averageMovement += a.getSpeed();
                averageEnergy += a.energyLevel;
                averageDistance += rs.resource.getLocation().distance(a.getLocation());
                totalEvals++;
            }
        }
    }

    @Override
    protected void postSimulation() {
        ResourceSharing rs = (ResourceSharing) sim;
        survivors = 0;
        for (RSAgent a : rs.agents) {
            if (a.isAlive()) {
                survivors++;
            }
        }
        survivors /= rs.agents.size();
        
        vbr = new VectorBehaviourResult(survivors,  (averageEnergy / totalEvals / rs.par.maxEnergy),
                 (averageMovement / totalEvals / rs.par.agentSpeed),
                 Math.min(1, averageDistance / totalEvals / (rs.par.size / 2)));
    }

    @Override
    public EvaluationResult getResult() {
        return vbr;
    }
}
