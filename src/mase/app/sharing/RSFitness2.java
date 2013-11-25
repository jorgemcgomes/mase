/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.sharing;

import mase.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;

/**
 *
 * @author jorge
 */
public class RSFitness2 extends MasonEvaluation {

    private double averageEnergy;
    private double survivors;
    private FitnessResult result;

    @Override
    protected void evaluate() {
        ResourceSharing rs = (ResourceSharing) sim;
        for(RSAgent a : rs.agents) {
            averageEnergy += a.energyLevel;
        }
    }

    @Override
    protected void postSimulation() {
        ResourceSharing rs = (ResourceSharing) sim;
        int count = 0;
        for(RSAgent a : rs.agents) {
            if(a.energyLevel > 0.0001) {
                count++;
            }
        }
        survivors = count / (double) rs.par.numAgents;
        averageEnergy = averageEnergy / rs.par.numAgents / rs.par.maxEnergy / currentEvaluationStep;
        float w = 1 / (1 + (float) rs.par.numAgents);
        float res = (float) (averageEnergy * w + survivors * (1 - w));
        result = new FitnessResult(res);
    }
    
    

    @Override
    public EvaluationResult getResult() {
        return result;
    }
    
}
