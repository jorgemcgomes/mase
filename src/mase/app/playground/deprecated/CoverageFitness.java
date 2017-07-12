/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import mase.mason.world.CircularObject;

/**
 *
 * @author jorge
 */
public class CoverageFitness extends MasonEvaluation<FitnessResult> {

    private static final long serialVersionUID = 1L;

    private Set<CircularObject> toVisit;
    private FitnessResult res;
    
    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(sim);
        Playground pl = (Playground) sim;
        toVisit = new HashSet<>(pl.objects);
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(sim);
        Playground pl = (Playground) sim;
        for(CircularObject ob : pl.objects) {
            if(pl.agent.distanceTo(ob) == 0) {
                toVisit.remove(ob);
                ob.setColor(Color.RED);
            }
        }
        if(toVisit.isEmpty()) {
            sim.kill();
        }
    }    

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        Playground pl = (Playground) sim;
        res = new FitnessResult((pl.objects.size() - toVisit.size()) / (double) pl.objects.size());
    }
    
    @Override
    public FitnessResult getResult() {
        return res;
    }


    
    
    
}
