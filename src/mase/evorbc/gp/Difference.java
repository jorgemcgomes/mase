/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc.gp;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

/**
 *
 * @author jorge
 */
public class Difference extends GPNode {

    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "-";
    }

    @Override
    public String name() {
        return "Diff";
    }
    
    

    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
        Data d = (Data) input;
        children[0].eval(state, thread, input, stack, individual, problem);
        double v1 = d.doubleValue;

        children[1].eval(state, thread, input, stack, individual, problem);
        double v2 = d.doubleValue;

        d.doubleValue = (v1 - v2) / 2; // keep the same range [-1,1] as the original values
    }

    @Override
    public int expectedChildren() {
        return 2;
    }

}
