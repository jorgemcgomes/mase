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
    public static final String P_VALUE_INDEX = "value-index";
    protected int[] valueIndex;
    protected double[] values;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        String s = state.parameters.getString(base.push(P_VALUE_INDEX), defaultBase().push(P_VALUE_INDEX));
        String[] split = s.split("[;,]");
        valueIndex = new int[split.length];
        for(int i = 0 ; i < split.length ; i++) {
            valueIndex[i] = Integer.parseInt(split[i]);
        }
    }

    @Override
    public EvaluationResult getResult() {
        if(values.length == 1) {
            return new FitnessResult(values[0]);
        } else {
            return new VectorBehaviourResult(values);
        }
    }
    
    public void setValues(double[] all) {
        values = new double[valueIndex.length];
        int index = 0;
        for(int i : valueIndex) {
            if(i > all.length) {
                throw new RuntimeException("Index outside the range of results vector: " + i + "(length " + all.length +")");
            }
            values[index++] = all[i];
        }
    }

    public int[] getIndexes() {
        return valueIndex;
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
