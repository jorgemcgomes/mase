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
public class Or extends GPNode {

    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "OR";
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
        Data d = (Data) input;
        children[0].eval(state, thread, input, stack, individual, problem);
        if(!d.boolValue) { // if false, check the other one, otherwise it stays true
            children[1].eval(state,thread,input,stack,individual,problem);  
        }
    }
    
    @Override
    public int expectedChildren() {
        return 2;
    }    
}
