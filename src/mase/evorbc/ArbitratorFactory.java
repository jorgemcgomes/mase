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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        Map<Integer, double[]> coords = null;
        if (state.parameters.exists(base.push(P_REDUCED), DEFAULT_BASE.push(P_REDUCED))) {
            reducedFile = new File(state.parameters.getString(base.push(P_REDUCED), DEFAULT_BASE.push(P_REDUCED)));
            if (reducedFile.exists()) {
                coords = loadCoordinates(reducedFile);
                if (coords.size() != solutions.size()) {
                    state.output.fatal("Number of solutions in repertoire does not match number of coordinates");
                }
            } else {
                state.output.fatal("Coordinate file does not exist: " + reducedFile.getAbsolutePath());
            }
        } else {
            coords = new HashMap<>();
            for (int i = 0; i < solutions.size(); i++) {
                PersistentSolution sol = solutions.get(i);
                coords.put(sol.getIndex(), (double[]) solutions.get(i).getUserData());
            }
        }
        
        scaleCoordinates(coords);
        
        double prune = state.parameters.getDouble(base.push(P_PRUNE), DEFAULT_BASE.push(P_PRUNE));

        int pruned = 0;
        repo = new Euclidean<>(coords.get(solutions.get(0).getIndex()).length);
        for(int i = 0 ; i < solutions.size() ; i++) {
            PersistentSolution sol = solutions.get(i);
            if(valid(sol, prune)) {
                AgentController ac = sol.getController().getAgentControllers(1)[0];
                double[] c = coords.get(sol.getIndex());
                if(c == null) {
                    state.output.fatal("Coordinate not found for index " + sol.getIndex());
                }
                repo.addPoint(c, ac);
            } else {
                pruned++;
            }
        }

        state.output.message("Pruned: " + pruned + ". New size: " + repo.size());

    }
    
    protected void scaleCoordinates(Map<Integer, double[]> coords) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for(double[] c : coords.values()) {
            for(double v : c) {
                min = Math.min(min, v);
                max = Math.max(max, v);
            }
        }
        double range = max - min;
        for(double[] c : coords.values()) {
            for(int i = 0 ; i < c.length ; i++) {
                c[i] = (c[i] - min) / range;
            }
        }
    }
    
    protected boolean valid(PersistentSolution s, double prune) {
        return s.getFitness() > prune;
    }
    
    private Map<Integer, double[]> loadCoordinates(File file) {
        Map<Integer, double[]> res = new HashMap<>();
        try {
            Scanner sc = new Scanner(file);
            while (sc.hasNext()) {
                String line = sc.nextLine();
                String[] split = line.trim().split(" ");
                int index = Integer.parseInt(split[0]);
                double[] coords = new double[split.length - 1];
                for (int i = 0; i < coords.length; i++) {
                    coords[i] = Double.parseDouble(split[i + 1]);
                }
                res.put(index, coords);
            }
            return res;
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
