/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.foraging;

import ec.EvolutionState;
import ec.Problem;
import ec.eval.MasterProblem;
import ec.util.Parameter;
import mase.evaluation.IncrementalEvolution;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class ForagingIncremental extends IncrementalEvolution {

    public static final String P_STARTING_RATIO = "starting-ratio";
    protected double startingRatio;
    protected double currentRatio;
    protected Double2D oriArenaSize;
    protected Double2D[] oriItems;
    protected double oriZone;
    
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        
        startingRatio = state.parameters.getDouble(base.push(P_STARTING_RATIO), 
                new Parameter(IncrementalEvolution.DEFAULT_BASE).push(P_STARTING_RATIO));
        
        ForagingSimulator fs = (ForagingSimulator) problem;
        oriArenaSize = fs.par.arenaSize;
        oriItems = fs.par.items;
        oriZone = fs.par.itemPlacementZone;
    }
    
    @Override
    public void changeStage(EvolutionState state, int stage) {
        super.changeStage(state, stage);
        double increment = (1 - startingRatio) / (numStages - 1);
        currentRatio = startingRatio + stage * increment;
        state.output.message("New size ratio: " + currentRatio);
        
        ForagingSimulator fs = (ForagingSimulator) problem;
        fs.par.arenaSize = new Double2D(oriArenaSize.x * currentRatio, oriArenaSize.y * currentRatio);
        fs.par.itemPlacementZone = oriZone * currentRatio;
        Double2D[] itemPos = new Double2D[oriItems.length];
        for(int i = 0 ; i < itemPos.length ; i++) {
            itemPos[i] = new Double2D(oriItems[i].x * currentRatio, oriItems[i].y * currentRatio);
        }
        fs.par.items = itemPos;
    }
    
}
