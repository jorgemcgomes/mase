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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import mase.controllers.AgentController;
import mase.controllers.AgentControllerIndividual;
import mase.controllers.ControllerFactory;
import mase.controllers.GroupController;
import mase.controllers.HomogeneousGroupController;
import mase.stat.PersistentSolution;
import mase.stat.SolutionPersistence;
import mase.util.KdTree;
import mase.util.KdTree.Euclidean;

/**
 *
 * @author jorge
 */
public class ArbitratorFactory implements ControllerFactory {

    public static final Parameter DEFAULT_BASE = new Parameter("evorbc");
    public static final String P_REPERTOIRE = "repertoire";
    public static final String P_REDUCED = "reduced";
    public static final String P_PRUNE = "prune";
    private static final long serialVersionUID = 1L;
    private transient KdTree<AgentController> repo;
    private File repoFile;
    private File reducedFile;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        repoFile = new File(state.parameters.getString(base.push(P_REPERTOIRE), DEFAULT_BASE.push(P_REPERTOIRE)));
        List<PersistentSolution> solutions;
        try {
            solutions = SolutionPersistence.readSolutionsFromTar(repoFile);
        } catch (Exception ex) {
            Logger.getLogger(ArbitratorFactory.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        state.output.message("Solutions found in repertoire: " + solutions.size());

        double[][] coords = null;
        if (state.parameters.exists(base.push(P_REDUCED), DEFAULT_BASE.push(P_REDUCED))) {
            reducedFile = new File(state.parameters.getString(base.push(P_REDUCED), DEFAULT_BASE.push(P_REDUCED)));
            if (reducedFile.exists()) {
                coords = loadCoordinates(reducedFile);
                if (coords.length != solutions.size()) {
                    state.output.fatal("Number of solutions in repertoire does not match number of coordinates");
                }
            } else {
                state.output.fatal("Coordinate file does not exist: " + reducedFile.getAbsolutePath());
            }
        } else {
            coords = new double[solutions.size()][];
            for (int i = 0; i < solutions.size(); i++) {
                coords[i] = (double[]) solutions.get(i).getUserData();
            }
        }
        
        double prune = state.parameters.getDouble(base.push(P_PRUNE), DEFAULT_BASE.push(P_PRUNE));

        int pruned = 0;
        repo = new Euclidean<>(coords[0].length);
        for(int i = 0 ; i < solutions.size() ; i++) {
            PersistentSolution sol = solutions.get(i);
            if(valid(sol, prune)) {
                AgentController ac = sol.getController().getAgentControllers(1)[0];
                repo.addPoint(coords[i], ac);
            } else {
                pruned++;
            }
        }

        state.output.message("Pruned: " + pruned + ". New size: " + repo.size());

    }
    
    protected boolean valid(PersistentSolution s, double prune) {
        return s.getFitness() > prune;
    }
    
    private double[][] loadCoordinates(File file) {
        try {
            List<double[]> read = new ArrayList<>();
            Scanner sc = new Scanner(file);
            while (sc.hasNext()) {
                String line = sc.nextLine();
                String[] split = line.trim().split(" ");
                double[] splitValues = new double[split.length];
                for (int i = 0; i < splitValues.length; i++) {
                    splitValues[i] = Double.parseDouble(split[i]);
                }
                read.add(splitValues);
            }
            double[][] matrix = read.toArray(new double[read.size()][]);
            return matrix;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ArbitratorFactory.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public GroupController createController(EvolutionState state, Individual... inds) {
        if (inds.length != 1) {
            throw new UnsupportedOperationException("Only one individual is expected");
        }

        // TODO: clone repo -- needed because the agentcontrollers can have state, and therefore
        // should not be used simultaneously in different evaluation threads
        // but this should be fine for stateless ACs (fixed values or non-recurrent NNs)
        KdTree<AgentController> clonedRepo = repo;

        AgentControllerIndividual aci = (AgentControllerIndividual) inds[0];
        AgentController arbitrator = aci.decodeController();

        ArbitratorController ac = new ArbitratorController(arbitrator, clonedRepo);
        return new HomogeneousGroupController(ac);
    }

    // normalise to [0,1]
    /*private double[][] normaliseCoordinates(double[][] coords) {
        double[][] norm = new double[coords.length][coords[0].length];
        for (int i = 0; i < coords.length; i++) {
            for (int j = 0; j < coords[i].length; j++) {
                norm[i][j] = (coords[i][j] + 1) / 2;
            }
        }
        return norm;
    }*/
}
