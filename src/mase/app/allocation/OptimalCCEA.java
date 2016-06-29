/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.allocation;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.spec.AbstractHybridExchanger;

/**
 *
 * @author jorge
 */
public class OptimalCCEA extends AbstractHybridExchanger {
    
    private static final long serialVersionUID = 1L;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        if(!(state.evaluator.p_problem instanceof AllocationProblem)) {
            state.output.fatal("Problem is not an AllocationProblem as expected!");
        }
        AllocationProblem prob = (AllocationProblem) state.evaluator.p_problem;
        state.output.warning("Ignoring initial allocation and using number of unique types instead: " + prob.numUniqueTypes, base.push(P_INITIAL_ALLOCATION));
        this.setInitialAllocation(prob.numUniqueTypes);  
    }    
}
