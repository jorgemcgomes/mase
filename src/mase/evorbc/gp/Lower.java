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
public class Lower extends GPNode {

    private static final long serialVersionUID = 1L;

    @Override
    public String name() {
        return "Lower";
    }
    
    @Override
    public String toString() {
        return "<";
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
        Data d = (Data) input;
        children[0].eval(state, thread, input, stack, individual, problem);
        double v1 = d.doubleValue;
        
        children[1].eval(state,thread,input,stack,individual,problem);
        double v2 = d.doubleValue;

        d.boolValue = v1 < v2;
    }

    @Override
    public int expectedChildren() {
        return 2;
    }
    
    
    
}
