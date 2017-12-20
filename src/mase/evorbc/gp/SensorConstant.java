/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc.gp;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.ERC;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.util.Parameter;
import java.util.Locale;

/**
 *
 * @author jorge
 */
public class SensorConstant extends ERC {

    public static final String P_MUTATION_SD = "mutation-sd";
    public static final String V_RESET = "reset";
    public static final double MIN_VALUE = -1;
    public static final double MAX_VALUE = 1;
    private static final long serialVersionUID = 1L;
    private double value;
    private double sd;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        if (state.parameters.getString(base.push(RepoPrimitive.P_MUTATION_SD), defaultBase().push(RepoPrimitive.P_MUTATION_SD)).equalsIgnoreCase(V_RESET)) {
            sd = Double.NaN;
        } else {
            sd = state.parameters.getDouble(base.push(RepoPrimitive.P_MUTATION_SD), defaultBase().push(RepoPrimitive.P_MUTATION_SD));
        }
    }

    @Override
    public void resetNode(EvolutionState state, int thread) {
        value = state.random[thread].nextDouble() * (MAX_VALUE - MIN_VALUE) + MIN_VALUE;
    }

    @Override
    public void mutateERC(EvolutionState state, int thread) {
        if (Double.isNaN(sd)) {
            this.resetNode(state, thread);
        } else {
            //System.out.println("< " + value);
            double v;
            do {
                v = value + state.random[thread].nextGaussian() * sd;
            } while (v < MIN_VALUE || v > MAX_VALUE);
            value = v;
            //System.out.println("> " + value);
        }
    }

    @Override
    public boolean nodeEquals(GPNode node) {
        return node instanceof SensorConstant && value == ((SensorConstant) node).value;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "%.2f", value);
    }

    @Override
    public String encode() {
        return toString();
    }

    @Override
    public String name() {
        return "C";
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
        Data d = (Data) input;
        d.doubleValue = value;
    }
    
    public double getValue() {
        return value;
    }

}
