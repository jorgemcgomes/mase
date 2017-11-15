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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mase.evaluation.EvaluationResult;
import mase.controllers.GroupController;
import mase.MaseProblem;
import sim.display.GUIState;
import sim.engine.Steppable;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class MasonSimulationProblem<T extends MasonSimState> extends MaseProblem {

    private static final long serialVersionUID = 1L;

    public static final String P_MAX_STEPS = "max-steps";
    public static final String P_REPETITIONS = "repetitions";
    public static final String P_SIMSTATE = "simstate";
    public static final String P_PARAMS = "params";
    protected int maxSteps;
    protected int repetitions;
    protected Class<MasonSimState> simClass;
    protected Object params;
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
        if (repetitions > 1 && sameSeed) {
            state.output.warning("Using multiple repetitions, but the random seed is fixed.");
        }

        if (state.parameters.exists(base.push(P_SIMSTATE), defaultBase().push(P_SIMSTATE))) {
            simClass = state.parameters.getClassForParameter(base.push(P_SIMSTATE), defaultBase().push(P_SIMSTATE), MasonSimState.class);
            try {
                simClass.getConstructor(long.class);
            } catch (NoSuchMethodException e) {
                state.output.fatal("Provided class " + simClass.getName() + " does not implement a single-long constructor (seed)");
            }
            params = setupParams(state, base);
        } else if (getClass() == MasonSimulationProblem.class) {
            state.output.fatal("simstate class not given and class not extended", base.push(P_SIMSTATE), defaultBase().push(P_SIMSTATE));
        } else {
            state.output.warning("simstate class not given. the createSimState method must be overriden", base.push(P_SIMSTATE), defaultBase().push(P_SIMSTATE));
        }

        loggers = new ArrayList<>();
    }

    public Object setupParams(EvolutionState state, Parameter base) {
        if (state.parameters.exists(base.push(P_PARAMS), defaultBase().push(P_PARAMS))) {
            Object parObject = state.parameters.getInstanceForParameter(base.push(P_PARAMS), defaultBase().push(P_PARAMS), Object.class);
            ParamUtils.autoSetParameters(parObject, state, base, defaultBase(), true);
            return parObject;
        } else {
            state.output.warning("Parameter class not specified. Not doing the auto-load and setup of parameters. "
                    + "The implementing problem must take care of that if needed.", base.push(P_PARAMS), defaultBase().push(P_PARAMS));
        }
        return null;
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
        s.setEvalFunctionsPrototypes(evalFunctions);
        s.setLoggers(loggers);
        return s;
    }

    public GUIState getSimStateUI(T state) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int size = (int) screenSize.getHeight() - 200;
        return new GUIState2D(state, state.getClass().getName(), size, size, Color.WHITE);
    }

    protected T createSimState(GroupController gc, long seed) {
        try {
            Constructor<MasonSimState> constructor = simClass.getConstructor(long.class);
            MasonSimState newInstance = constructor.newInstance(seed);
            newInstance.setGroupController(gc);
            newInstance.setParams(params);
            return (T) newInstance;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(MasonSimulationProblem.class.getName()).log(Level.SEVERE, null, ex);
        }
        throw new RuntimeException("Unable to create sim state");
    }
}
