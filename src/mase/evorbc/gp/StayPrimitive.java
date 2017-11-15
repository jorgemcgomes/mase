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
public class StayPrimitive extends GPNode {

    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "S";
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
        Data d = (Data) input;
        GPArbitratorController gpac = (GPArbitratorController) d.ac;
        d.primitive = gpac.getLastPrimitive() == null ? Integer.MAX_VALUE : gpac.getLastPrimitive().id;
    }
    
}
