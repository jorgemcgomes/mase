/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.evaluation.EvaluationFunction;
import mase.evaluation.EvaluationResult;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public abstract class MasonEvaluation<T extends EvaluationResult> implements EvaluationFunction<T>, Steppable {

    public static final String P_DEFAULT = "mason-eval";
    public static final String P_FREQUENCY = "update-freq";
    private static final long serialVersionUID = 1L;
    protected int currentEvaluationStep;
    protected int maxEvaluationSteps;
    protected int updateFrequency;
    protected int maxSteps;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        this.updateFrequency = state.parameters.getIntWithDefault(base.push(P_FREQUENCY), defaultBase().push(P_FREQUENCY), 1);
        if (updateFrequency < 0) {
            state.output.fatal("Update frequency must be >= 0", base.push(P_FREQUENCY), defaultBase().push(P_FREQUENCY));
        }
        this.maxSteps = state.parameters.getInt(base.pop().pop().push(MasonSimulationProblem.P_MAX_STEPS), null);
        this.maxEvaluationSteps = updateFrequency == 0 ? 0 : maxSteps / updateFrequency;
    }

    @Override
    public Parameter defaultBase() {
        return new Parameter(P_DEFAULT);
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }

    @Override
    public final void step(SimState sim) {
        evaluate((MasonSimState) sim);
        this.currentEvaluationStep++;
    }

    /**
     * Is called right before the simulation, after all initialization
     * procedures are done
     */
    protected void preSimulation(MasonSimState sim) {
        this.currentEvaluationStep = 0;
    }    
    
    /**
     * Does the evaluation every step (or at the specificied frequency)
     */
    protected void evaluate(MasonSimState sim) {
    }

    /**
     * Is called after the simulation is finished
     */
    protected void postSimulation(MasonSimState sim) {
    }
}
