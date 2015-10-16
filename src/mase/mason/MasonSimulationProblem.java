/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import ec.EvolutionState;
import ec.util.Parameter;
import java.awt.Color;
import mase.evaluation.EvaluationResult;
import mase.controllers.GroupController;
import mase.SimulationProblem;
import sim.display.GUIState;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public abstract class MasonSimulationProblem extends SimulationProblem {

    public static final String P_MAX_STEPS = "max-steps";
    protected int maxSteps;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        base = defaultBase();
        maxSteps = state.parameters.getInt(base.push(P_MAX_STEPS), null);
    }

    @Override
    public EvaluationResult[] evaluateSolution(GroupController gc, long seed) {
        MasonSimState simState = createSimState(gc, seed);
        return simState.evaluate(repetitions, maxSteps, evalFunctions);
    }
    
    public int getMaxSteps() {
        return maxSteps;
    }

    public abstract MasonSimState createSimState(GroupController gc, long seed);

    public GUIState createSimStateWithUI(GroupController gc, long seed) {
        return new GUIState2D(createSimState(gc, seed), this.getClass().getName(), 500,500, Color.WHITE);
    }
}
