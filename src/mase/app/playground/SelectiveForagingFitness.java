/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import ec.EvolutionState;
import ec.util.Parameter;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import mase.app.playground.ForagingPlayground.ItemRemover.ForagingHook;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import mase.mason.world.CircularObject;
import mase.mason.world.MultilineObject;

/**
 *
 * @author jorge
 */
public class SelectiveForagingFitness extends MasonEvaluation<FitnessResult> {

    private static final long serialVersionUID = 1L;

    private FitnessResult result;
    protected Set<CircularObject> forbidden;
    protected int forbiddenForaged;
    public static final String P_DISTANCE = "distance", P_OVER = "over";
    protected double distance;
    protected boolean over;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base); 
        distance = state.parameters.getDouble(base.push(P_DISTANCE), null);
        over = state.parameters.getBoolean(base.push(P_DISTANCE), null, false);
    }

    @Override
    public FitnessResult getResult() {
        return result;
    }
    
    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(sim);
        // Determine which items are good to forage and which are not
        ForagingPlayground fp = (ForagingPlayground) sim;
        forbidden = new HashSet<>();
        for(CircularObject o : fp.objects) {
            double d = fp.walls.distanceTo(o.getLocation());
            if((d < distance && over) || (d > distance && !over)) {
                forbidden.add(o);
                continue;
            }
            for(MultilineObject mo : fp.obstacles) {
                d = mo.distanceTo(o.getLocation());
                if((d < distance && over) || (d > distance && !over)) {
                    forbidden.add(o);
                    break;
                }
            }
        }
        
        for(CircularObject o : forbidden) {
            o.setColor(Color.RED);
        }
        forbiddenForaged = 0;
        fp.itemRemover.addForagingHook(new ForagingHook() {
            @Override
            public void foraged(ForagingPlayground sim, PlaygroundAgent ag, CircularObject foraged) {
                if(forbidden.contains(foraged)) {
                    forbiddenForaged++;
                }
            }
            
        });
    }
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        // Reward the goods and penalize the bads
        ForagingPlayground pl = (ForagingPlayground) sim;
        int goodForaged = pl.objects.size() - pl.itemRemover.aliveObjects.size() - forbiddenForaged;
        double f = 1 + (goodForaged - forbiddenForaged) / (double) pl.objects.size();
        result = new FitnessResult(f);
    }
    
}
