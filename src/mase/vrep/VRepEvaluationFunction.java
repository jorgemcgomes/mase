/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.vrep;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.evaluation.EvaluationFunction;
import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.evaluation.VectorBehaviourResult;

/**
 *
 * @author jorge
 */
public class VRepEvaluationFunction implements EvaluationFunction {

    public static final Parameter DEFAULT_BASE = new Parameter("vrep-eval");
    private static final long serialVersionUID = 1L;
    public static final String P_VALUES = "number-values";
    protected int numValues;
    protected double[] values;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        this.numValues = state.parameters.getInt(base.push(P_VALUES), defaultBase().push(P_VALUES));
        if(numValues < 1) {
            state.output.fatal("Must be >= 1", base.push(P_VALUES), defaultBase().push(P_VALUES));
        }
    }

    @Override
    public EvaluationResult getResult() {
        if(numValues == 1) {
            return new FitnessResult(values[0]);
        } else {
            return new VectorBehaviourResult(values);
        }
    }
    
    public void setValues(double[] v) {
        if(v.length != numValues) {
            throw new RuntimeException("Mismatch between number of received values (" + v.length + ") and expected (" + numValues +")");
        }
        values = v;
    }

    public int getNumberValues() {
        return numValues;
    }
    
    @Override
    public Parameter defaultBase() {
        return DEFAULT_BASE;
    }
    
     @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }   
}
