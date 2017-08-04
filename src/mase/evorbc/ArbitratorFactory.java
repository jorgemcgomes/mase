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
import mase.controllers.NeuralControllerIndividual;
import mase.neat.NEATSubpop;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author jorge
 */
public class ArbitratorFactory implements ControllerFactory {

    public static final Parameter DEFAULT_BASE = new Parameter("evorbc");
    public static final String P_REPERTOIRE = "repertoire";
    public static final String P_LOCKING = "locking";
    public static final String P_COORDINATES = "coordinates";
    public static final String V_DIRECT = "direct";
    private static final long serialVersionUID = 1L;
    private Repertoire repo;
    private MappingFunction mapFun;
    private boolean locking;

    @Override
    /*
     * TODO: the mappingfunction and repertoire should also be configurable and setupable
     */
    public void setup(EvolutionState state, Parameter base) {        
        locking = state.parameters.getBoolean(base.push(P_LOCKING), DEFAULT_BASE.push(P_LOCKING), false);
        File repoFile = new File(state.parameters.getString(base.push(P_REPERTOIRE), DEFAULT_BASE.push(P_REPERTOIRE)));
        File coordsFile = null;
        String fName = state.parameters.getString(base.push(P_COORDINATES), DEFAULT_BASE.push(P_COORDINATES));
        if (!fName.equalsIgnoreCase(V_DIRECT)) {
            if (!fName.endsWith(".txt")) {
                fName += ".txt";
            }
            coordsFile = new File(repoFile.getParentFile(), repoFile.getName().replace(".tar.gz", "_") + fName);
            if (!coordsFile.exists()) {
                state.output.fatal("Given coordinate file does not exist: " + coordsFile.getAbsolutePath() + ".txt.\n"
                        + "The coordinate file should be in the same folder as the repertoire.");
            }
        }

        repo = new KdTreeRepertoire();
        try {
            repo.load(repoFile, coordsFile);
        } catch (IOException ex) {
            state.output.fatal("Error loading repertoire: " + ex.getMessage());
        }
        state.output.message("Loaded repertoire with " + repo.size() + " controllers");
        state.output.message("Coordinate bounds: " + boundsToString(repo.coordinateBounds()));

        int outputs = repo.coordinateBounds().length + (locking ? 1 : 0);
        Parameter pOut = new Parameter(NEATSubpop.P_NEAT_BASE).push("OUTPUT.NODES");
        state.output.message("Forcing " + pOut + " to: " + outputs);        
        state.parameters.set(pOut, outputs + "");

        Parameter nOut = new Parameter(NeuralControllerIndividual.DEFAULT_BASE).push("output");
        state.parameters.set(nOut, outputs + "");
        state.output.message("Forcing " + nOut + " to: " + outputs);        
        
        mapFun = new CartesianMappingFunction(repo.coordinateBounds());
    }
    
    private String boundsToString(Pair<Double,Double>[] bounds) {
        String s = "";
        for(int i = 0 ; i < bounds.length ; i++) {
            s += i + ":["+bounds[i].getLeft()+","+bounds[i].getRight()+"] ";
        }
        return s;
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
