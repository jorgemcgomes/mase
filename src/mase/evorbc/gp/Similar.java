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
public class Similar extends ERC {
    
    private static final long serialVersionUID = 1L;
    private double sd;
    private double threshold;
    public static final String P_GAUSSIAN_SD = "gaussian-sd";
    public static final double MIN_VALUE = 0;
    public static final double MAX_VALUE = 1;
    
    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
        Data d = (Data) input;
        children[0].eval(state, thread, input, stack, individual, problem);
        double v1 = d.doubleValue;
        
        children[1].eval(state,thread,input,stack,individual,problem);
        double v2 = d.doubleValue;

        d.boolValue = Math.abs(v1 - v2) < threshold;
    }

    @Override
    public int expectedChildren() {
        return 2;
    }    

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        sd = state.parameters.getDouble(base.push(P_GAUSSIAN_SD), defaultBase().push(P_GAUSSIAN_SD));
    }   

    @Override
    public void resetNode(EvolutionState state, int thread) {
        threshold = state.random[thread].nextDouble() * (MAX_VALUE - MIN_VALUE) + MIN_VALUE;
    }

    public void mutateNode(EvolutionState state, int thread) {
        double v;
        do {
            v = threshold + state.random[thread].nextGaussian() * sd;
        } while (v < MIN_VALUE || v > MAX_VALUE);
        threshold = v;
    }

    @Override
    public boolean nodeEquals(GPNode node) {
        return node instanceof Similar && threshold == ((Similar) node).threshold;
    }

    @Override
    public String encode() {
        return toString();
    }
    
    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "≈%.2f≈", threshold);
    }    
    
    @Override
    public String name() {
        return "Approx";
    }    
}
