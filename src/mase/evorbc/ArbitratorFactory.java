/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
import mase.controllers.AgentController;
import mase.controllers.AgentControllerIndividual;
import mase.controllers.ControllerFactory;
import mase.controllers.GroupController;
import mase.controllers.HomogeneousGroupController;

/**
 *
 * @author jorge
 */
public class ArbitratorFactory implements ControllerFactory {

    public static final Parameter DEFAULT_BASE = new Parameter("evorbc");
    public static final String P_REPERTOIRE = "repertoire";
    public static final String P_REDUCED = "reduced";
    private static final long serialVersionUID = 1L;
    private Repertoire repo;
    private MappingFunction mapFun;

    @Override
    /*
     * TODO: the mappingfunction and repertoire should also be configurable and setupable
     */
    public void setup(EvolutionState state, Parameter base) {
        File repoFile = new File(state.parameters.getString(base.push(P_REPERTOIRE), DEFAULT_BASE.push(P_REPERTOIRE)));
        File coordsFile = null;
        if (state.parameters.exists(base.push(P_REDUCED), DEFAULT_BASE.push(P_REDUCED))) {
            File f = new File(state.parameters.getString(base.push(P_REDUCED), DEFAULT_BASE.push(P_REDUCED)));
            if (f.exists()) {
                coordsFile = f;
            } else {
                state.output.fatal("Given coordinate file does not exist: " + f.getAbsolutePath());
            }
        }
        
        repo = new KdTreeRepertoire();
        try {
            repo.load(repoFile, coordsFile);
        } catch (IOException ex) {
            state.output.fatal("Error loading repertoire: " + ex.getMessage());
        }
        state.output.message("Loaded repertoire with " + repo.size() + " controllers");
        state.output.message("Coordinate bounds: " + repo.coordinateBounds());

        mapFun = new CartesianMappingFunction(repo.coordinateBounds());
    }



    @Override
    public GroupController createController(EvolutionState state, Individual... inds) {
        if (inds.length != 1) {
            throw new UnsupportedOperationException("Only one individual is expected");
        }

        Repertoire clonedRepo = repo.deepCopy();

        AgentControllerIndividual aci = (AgentControllerIndividual) inds[0];
        AgentController arbitrator = aci.decodeController();

        ArbitratorController ac = new ArbitratorController(arbitrator, clonedRepo, mapFun);
        return new HomogeneousGroupController(ac);
    }
}
