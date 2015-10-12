/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.sharing;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;

/**
 *
 * @author jorge
 */
public class RSFitness extends MasonEvaluation {

    public static final String P_ENERGY_WEIGHT = "energy-weight";
    private double averageEnergy;
    private double survivors;
    private double weight;
    private FitnessResult result;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.weight = state.parameters.getDouble(base.push(P_ENERGY_WEIGHT), defaultBase().push(P_ENERGY_WEIGHT));
    }

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
        double res =  (averageEnergy * weight + survivors * (1 - weight));
        //System.out.println(survivors + " , " + averageEnergy + " = " + res);
        result = new FitnessResult(res);
    }
    
    

    @Override
    public EvaluationResult getResult() {
        return result;
    }
    
}
