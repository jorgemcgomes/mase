/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import ec.EvolutionState;
import ec.util.Parameter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import mase.evaluation.EvaluationResult;
import mase.controllers.GroupController;
import mase.MaseProblem;
import sim.display.GUIState;
import sim.engine.Steppable;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public abstract class MasonSimulationProblem<T extends MasonSimState> extends MaseProblem {

    private static final long serialVersionUID = 1L;

    public static final String P_MAX_STEPS = "max-steps";
    public static final String P_REPETITIONS = "repetitions";
    protected int maxSteps;
    protected int repetitions;
    protected List<Steppable> loggers;
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        base = defaultBase();
        maxSteps = state.parameters.getInt(base.push(P_MAX_STEPS), null);
  
        repetitions = state.parameters.getIntWithDefault(base.push(P_REPETITIONS), null, 1);
        if (repetitions < 1) {
            state.output.fatal("Parameter invalid value. Must be > 0.", base.push(P_REPETITIONS));
        }
        if(repetitions > 1 && sameSeed) {
            state.output.warning("Using multiple repetitions, but the random seed is fixed.");
        }      
        
        loggers = new ArrayList<>();
    }
    
    @Override
    public EvaluationResult[] evaluateSolution(GroupController gc, long seed) {
        MasonSimState simState = getSimState(gc, seed);
        return simState.evaluate(repetitions);
    }
    
    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }
    
    public int getMaxSteps() {
        return maxSteps;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    public int getRepetitions() {
        return repetitions;
    }
    
    public List<Steppable> loggers() {
        return loggers;
    }
    
    public T getSimState(GroupController gc, long seed) {
        T s = createSimState(gc, seed);
        s.setMaxSteps(maxSteps);
        s.setEvalFunctions(evalFunctions);
        s.setLoggers(loggers);
        return s;
    }
    
    public GUIState getSimStateUI(T state) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int size = (int) screenSize.getHeight() - 200;
        return new GUIState2D(state, state.getClass().getName(), size, size, Color.WHITE);
    }
  
    protected abstract T createSimState(GroupController gc, long seed);
}
