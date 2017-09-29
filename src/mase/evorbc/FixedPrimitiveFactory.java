/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.controllers.HomogeneousGroupController;
import mase.evorbc.Repertoire.Primitive;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author jorge
 */
public class FixedPrimitiveFactory extends ArbitratorFactory {

    private static final long serialVersionUID = 1L;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        int outputs = repo.allPrimitives().iterator().next().coordinates.length;
        Parameter pOut = new Parameter("vector.species.genome-size");
        state.output.message("Forcing " + pOut + " to: " + outputs);        
        state.parameters.set(pOut, outputs + "");
    }

    
    @Override
    public GroupController createController(EvolutionState state, Individual... inds) {
        if (inds.length != 1) {
            throw new UnsupportedOperationException("Only one individual is expected");
        }

        DoubleVectorIndividual v = (DoubleVectorIndividual) inds[0]; // needs to be in range [0,1]!
        double[] out = v.genome;
        double[] coords = mapFun.outputToCoordinates(out);
        Primitive primitive = repo.nearest(coords);
        AgentController ac = primitive.ac.clone();
        return new HomogeneousGroupController(ac);
    }

}
