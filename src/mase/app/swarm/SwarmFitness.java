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
import mase.mason.world.MultilineObject;
import mase.mason.world.WorldObject;

/**
 *
 * @author jorge
 */
public class SwarmFitness extends MasonEvaluation<FitnessResult> {

    private static final long serialVersionUID = 1L;
    public static final String P_COUNT_COLLISIONS = "count-collisions";
    public static final String P_MAX_COLLISIONS = "max-collisions";
    
    private enum CollisionMode {
        none, walls, agents, all
    }
    private int collisionCount;
    private double maxCollisions;
    private double meanCollisionsPerAgent;
    private CollisionMode mode;
    private FitnessResult fr;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        state.output.warning("Forcing the update frequency to one", base.push(P_FREQUENCY));
        super.updateFrequency = 1;
        this.mode = CollisionMode.valueOf(state.parameters.getStringWithDefault(base.push(P_COUNT_COLLISIONS), null, CollisionMode.none.name()));
        this.maxCollisions = mode != CollisionMode.none ? state.parameters.getDouble(base.push(P_MAX_COLLISIONS), null) : -1;
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
            WorldObject collision = sa.getCollidingObject();
            if((mode == CollisionMode.all && sa.isInCollision()) || 
                    (mode == CollisionMode.walls && collision instanceof MultilineObject) || 
                    (mode == CollisionMode.agents && collision instanceof SwarmAgent)) {
                collisionCount++;
            }
        }
        meanCollisionsPerAgent = collisionCount / (double) sw.agents.size();
        if(maxCollisions >= 0 && meanCollisionsPerAgent > maxCollisions * maxEvaluationSteps) {
            sw.kill(); // no point in continuing, the fitness will be zero
        }
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        SwarmPlayground sw = (SwarmPlayground) sim;
        double collisionFactor = 1;
        if(maxCollisions == 0) { // no collisions tolerated
            collisionFactor = meanCollisionsPerAgent > 0 ? 0 : 1;
        } else if(maxCollisions < 0) { // do not use maxcollisions
            collisionFactor = 1 - meanCollisionsPerAgent / super.currentEvaluationStep;
        } else { 
            collisionFactor = Math.max(0, 1 - meanCollisionsPerAgent / (maxCollisions * maxEvaluationSteps));
        }
        double customFitness = getFinalTaskFitness(sw);
        fr = new FitnessResult(collisionFactor * customFitness);
    }
    
    protected double getFinalTaskFitness(SwarmPlayground sim) {
        return 1;
    }
    
    @Override
    public FitnessResult getResult() {
        return fr;
    }
        
}
