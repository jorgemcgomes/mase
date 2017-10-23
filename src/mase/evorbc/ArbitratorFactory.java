/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.controllers.ControllerFactory;

/**
 *
 * @author jorge
 */
public abstract class ArbitratorFactory implements ControllerFactory {

    public static final Parameter DEFAULT_BASE = new Parameter("evorbc");
    public static final String P_REPERTOIRE_IMPL = "repertoire-impl";
    public static final String P_MAPPING_FUNCTION = "mapping-fun";
    private static final long serialVersionUID = 1L;
    protected Repertoire repo;
    protected MappingFunction mapFun;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        if (state.parameters.exists(base.push(P_REPERTOIRE_IMPL), DEFAULT_BASE.push(P_REPERTOIRE_IMPL))) {
            repo = (Repertoire) state.parameters.getInstanceForParameter(
                    base.push(P_REPERTOIRE_IMPL), DEFAULT_BASE.push(P_REPERTOIRE_IMPL), Repertoire.class);
        } else {
            state.output.warning("No repertoire implementation given. Using the default KdTreeRepertoire", 
                    base.push(P_REPERTOIRE_IMPL), DEFAULT_BASE.push(P_REPERTOIRE_IMPL));
            repo = new KdTreeRepertoire();
        }         
        repo.setup(state, base);
        
        if (state.parameters.exists(base.push(P_MAPPING_FUNCTION), DEFAULT_BASE.push(P_MAPPING_FUNCTION))) {
            mapFun = (MappingFunction) state.parameters.getInstanceForParameter(
                    base.push(P_MAPPING_FUNCTION), DEFAULT_BASE.push(P_MAPPING_FUNCTION), MappingFunction.class);
        } else {
            state.output.warning("No MappingFunction given. Using the default CartesianMappingFunction", 
                    base.push(P_MAPPING_FUNCTION), DEFAULT_BASE.push(P_MAPPING_FUNCTION));
            mapFun = new CartesianMappingFunction();
        }
        mapFun.setup(state, base);
        mapFun.additionalSetup(state, repo);
    }

    public Repertoire getRepertoire() {
        return repo;
    }

    public MappingFunction getMappingFunction() {
        return mapFun;
    }

}
