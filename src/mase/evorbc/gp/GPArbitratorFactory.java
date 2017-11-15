/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc.gp;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPTree;
import ec.util.Parameter;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.controllers.HomogeneousGroupController;
import mase.evorbc.ArbitratorFactory;
import mase.evorbc.Repertoire;

/**
 *
 * @author jorge
 */
public class GPArbitratorFactory extends ArbitratorFactory {

    private static final long serialVersionUID = 1L;
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        // TODO: check to ensure that the GP tree is compatible with the number of robot's sensors
        // int outputs = repo.allPrimitives().iterator().next().coordinates.length;
    }

    @Override
    public GroupController createController(EvolutionState state, Individual... inds) {
        if (inds.length != 1) {
            throw new UnsupportedOperationException("Only one individual is expected");
        }

        Repertoire clonedRepo = repo.deepCopy();
        GPIndividual ind = (GPIndividual) inds[0];
        AgentController ac = new GPArbitratorController((GPTree) ind.trees[0], clonedRepo);
        return new HomogeneousGroupController(ac);
    }    
    
}
