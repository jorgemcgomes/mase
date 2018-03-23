/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.swarm;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;

/**
 *
 * @author jorge
 */
public class CollisionsFitness extends MasonEvaluation<FitnessResult> {

    private static final long serialVersionUID = 1L;
    private int collisionCount;
    private FitnessResult fr;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        state.output.warning("Forcing the update frequency to one", base.push(P_FREQUENCY));
        super.updateFrequency = 1;
    }
            
    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(sim);
        this.collisionCount = 0;
    }

    
    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(sim); 
        SwarmPlayground sw = (SwarmPlayground) sim;
        for(SwarmAgent sa : sw.agents) {
            if(sa.isInCollision()) {
                collisionCount++;
            }
        }
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        SwarmPlayground sw = (SwarmPlayground) sim;
        fr = new FitnessResult(1 - (double) collisionCount / super.currentEvaluationStep / (double) sw.agents.size());
    }
    
    
    @Override
    public FitnessResult getResult() {
        return fr;
    }
        
}
