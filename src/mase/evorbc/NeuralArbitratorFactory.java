/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import mase.controllers.AgentController;
import mase.controllers.AgentControllerIndividual;
import mase.controllers.GroupController;
import mase.controllers.HomogeneousGroupController;
import mase.controllers.NeuralControllerIndividual;
import mase.neat.NEATSubpop;

/**
 *
 * @author jorge
 */
public class NeuralArbitratorFactory extends ArbitratorFactory {

    private static final long serialVersionUID = 1L;

    public static final String P_LOCKING = "locking";
    private boolean locking;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        locking = state.parameters.getBoolean(base.push(P_LOCKING), DEFAULT_BASE.push(P_LOCKING), false);
        int outputs = repo.allPrimitives().iterator().next().coordinates.length + (locking ? 1 : 0);
        Parameter pOut = new Parameter(NEATSubpop.P_NEAT_BASE).push("OUTPUT.NODES");
        state.output.message("Forcing " + pOut + " to: " + outputs);
        state.parameters.set(pOut, outputs + "");

        Parameter nOut = new Parameter(NeuralControllerIndividual.DEFAULT_BASE).push("output");
        state.parameters.set(nOut, outputs + "");
        state.output.message("Forcing " + nOut + " to: " + outputs);
    }

    @Override
    public GroupController createController(EvolutionState state, Individual... inds) {
        if (inds.length != 1) {
            throw new UnsupportedOperationException("Only one individual is expected");
        }

        Repertoire clonedRepo = repo.deepCopy();

        AgentControllerIndividual aci = (AgentControllerIndividual) inds[0];
        AgentController arbitrator = aci.decodeController();

        ArbitratorController ac = new ArbitratorController(arbitrator, clonedRepo, mapFun, locking);
        return new HomogeneousGroupController(ac);
    }

}
