/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import mase.controllers.GroupController;
import mase.evaluation.EvaluationResult;
import sim.display.GUIState;

/**
 *
 * @author jorge
 */
public abstract class MasonStandaloneProblem extends MasonSimulator {
    
    @Override
    public EvaluationResult[] evaluateSolution(GroupController gc, long seed) {
        MasonStandaloneSimulator sim = getSimulator();
        return sim.evaluateSolution(gc, seed, repetitions, maxSteps, evalFunctions);
    }
    
    public int getMaxSteps() {
        return maxSteps;
    }

    @Override
    public GUICompatibleSimState createSimState(GroupController gc, long seed) {
        return getSimulator().createSimState(gc, seed);
    }

    @Override
    public GUIState createSimStateWithUI(GroupController gc, long seed) {
        return getSimulator().createSimStateWithUI(gc, seed);
    }
        
    public abstract MasonStandaloneSimulator getSimulator();
    
}
