/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import ec.EvolutionState;
import ec.util.Parameter;
import java.util.logging.Level;
import java.util.logging.Logger;
import mase.evaluation.EvaluationFunction;
import sim.engine.SimState;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public abstract class MasonEvaluation implements EvaluationFunction {

    public static final String P_COMBINATION = "combination";
    public static final String P_DEFAULT = "mason-eval";
    public static final String P_FREQUENCY = "update-freq";
    protected SimState sim;
    protected int currentEvaluationStep;
    protected int maxEvaluationSteps;
    protected int updateFrequency;
    protected int maxSteps;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        updateFrequency = state.parameters.getInt(base.push(P_FREQUENCY), defaultBase().push(P_FREQUENCY));
        if (updateFrequency < 0) {
            state.output.fatal("Update frequency must be >= 0", base.push(P_FREQUENCY), defaultBase().push(P_FREQUENCY));
        }
        this.maxSteps = state.parameters.getInt(base.pop().pop().push(MasonSimulator.P_MAX_STEPS), null);
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
            Logger.getLogger(MasonEvaluation.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public void setSimulationModel(SimState sim) {
        this.sim = sim;
    }

    /**
     * Should be called right before the simulation, after all initialization
     * procedures are done
     */
    protected void preSimulation() {
        this.currentEvaluationStep = 0;
    }

    protected final void evaluationStep() {
        if (updateFrequency > 0 && sim.schedule.getSteps() % updateFrequency == 0) {
            evaluate();
            this.currentEvaluationStep++;
        }
    }

    /**
     * Should be called to do the actual evaluation
     */
    protected void evaluate() {
    }

    /**
     * Should be called after the simulation is finished
     */
    protected void postSimulation() {
    }
}
