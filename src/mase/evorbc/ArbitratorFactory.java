/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
import org.apache.commons.math3.util.MathArrays;
import smile.mds.SammonMapping;
import smile.neighbor.KDTree;

/**
 *
 * @author jorge
 */
public class ArbitratorFactory implements ControllerFactory {

    public static final Parameter DEFAULT_BASE = new Parameter("evorbc");
    public static final String P_REPERTOIRE = "repertoire";
    public static final String P_DIMENSION_REDUCTION = "reduction";
    public static final String P_PRUNE = "prune";
    private static final long serialVersionUID = 1L;
    private transient KDTree<AgentController> repo;
    private File repoFile;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        repoFile = state.parameters.getFile(base.push(P_REPERTOIRE), DEFAULT_BASE.push(P_REPERTOIRE));
        List<PersistentSolution> solutions;
        try {
            solutions = SolutionPersistence.readSolutionsFromTar(repoFile);
        } catch (Exception ex) {
            Logger.getLogger(ArbitratorFactory.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        state.output.message("Solutions found in repertoire: " + solutions.size());

        double prune = state.parameters.getDouble(base.push(P_PRUNE), DEFAULT_BASE.push(P_PRUNE));
        List<PersistentSolution> pruned = prune(solutions, prune);
        state.output.message("Pruned: " + (solutions.size() - pruned.size()) + ". New size: " + pruned.size());
        
        int reduction = state.parameters.getInt(base.push(P_DIMENSION_REDUCTION), DEFAULT_BASE.push(P_DIMENSION_REDUCTION));        
        if (reduction <= 0) {
            repo = noReduction(state, pruned);
        } else {
            repo = reduction(state, pruned, reduction);
        }

        // TODO: also setup mapping FUN
    }
    
    protected List<PersistentSolution> prune(List<PersistentSolution> all, double prune) {
        List<PersistentSolution> pruned = new ArrayList<>(all);
        Iterator<PersistentSolution> iterator = pruned.iterator();
        while(iterator.hasNext()) {
            PersistentSolution next = iterator.next();
            if(next.getFitness() < prune) {
                iterator.remove();
            }
        }
        return pruned;
    }

    private KDTree<AgentController> noReduction(EvolutionState state, List<PersistentSolution> solutions) {
        double[][] keys = new double[solutions.size()][];
        AgentController[] values = new AgentController[solutions.size()];
        for (int i = 0; i < solutions.size(); i++) {
            PersistentSolution s = solutions.get(i);
            keys[i] = (double[]) s.getUserData();
            values[i] = s.getController().getAgentControllers(1)[0];
        }
        KDTree<AgentController> tree = new KDTree<>(keys, values);
        tree.setIdenticalExcluded(false);
        return tree;
    }

    // TODO: if multiple processes write the cache at the same time, that might be a problem
    private KDTree<AgentController> reduction(EvolutionState state, List<PersistentSolution> solutions, int n) {
        double[][] coordinates = reduce(state, solutions, n);
        AgentController[] values = new AgentController[solutions.size()];
        for (int i = 0; i < solutions.size(); i++) {
            values[i] = solutions.get(i).getController().getAgentControllers(1)[0];
        }
        KDTree<AgentController> tree = new KDTree<>(coordinates, values);
        tree.setIdenticalExcluded(false);
        return tree;
    }

    private double[][] reduce(EvolutionState state, List<PersistentSolution> solutions, int n) {
        File cache = new File(repoFile.getAbsolutePath() + "." + n + ".txt");
        if (cache.exists()) {
            state.output.message("Loading mapping from: " + cache.getAbsolutePath());
            double[][] loaded = loadFromCache(cache);
            if (loaded != null && loaded.length == solutions.size()) {
                return loaded;
            } else {
                state.output.warning("Cache does not match in size or corrupt. Going to discard and make new mapping.");
            }
        }
        List<double[]> vals = new ArrayList<>();
        for (PersistentSolution s : solutions) {
            double[] k = (double[]) s.getUserData();
            vals.add(k);
        }
        double[][] dists = distanceMatrix(vals);
        SammonMapping map = new SammonMapping(dists, n);
        state.output.message("Stress: " + map.getStress());
        double[][] coordinates = map.getCoordinates();
        coordinates = normaliseCoordinates(coordinates);
        writeToCache(state, cache, coordinates);
        return coordinates;
    }

    private void writeToCache(EvolutionState state, File file, double[][] coords) {
        try (BufferedWriter bfw = new BufferedWriter(new FileWriter(file))) {
            for (int i = 0; i < coords.length; i++) {
                for (int j = 0; j < coords[i].length; j++) {
                    bfw.write(coords[i][j] + " ");
                }
                bfw.newLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(ArbitratorFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        state.output.message("Cache wrote to " + file.getAbsolutePath());
    }

    private double[][] loadFromCache(File file) {
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

    private double[][] distanceMatrix(List<double[]> values) {
        double[][] d = new double[values.size()][values.size()];
        for (int i = 0; i < values.size(); i++) {
            for (int j = 0; j < values.size(); j++) {
                if (i == j) {
                    d[i][j] = 0;
                } else { // missing optimisation
                    d[i][j] = MathArrays.distance(values.get(i), values.get(j));
                }
            }
        }
        return d;
    }

    // normalise to [0,1]
    private double[][] normaliseCoordinates(double[][] coords) {
        double[][] norm = new double[coords.length][coords[0].length];
        for (int i = 0; i < coords.length; i++) {
            for (int j = 0; j < coords[i].length; j++) {
                norm[i][j] = (coords[i][j] + 1) / 2;
            }
        }
        return norm;
    }

    @Override
    public GroupController createController(EvolutionState state, Individual... inds) {
        if (inds.length != 1) {
            throw new UnsupportedOperationException("Only one individual is expected");
        }

        // TODO: clone repo -- needed because the agentcontrollers can have state, and therefore
        // should not be used simultaneously in different evaluation threads
        // but this should be fine for stateless ACs (fixed values or non-recurrent NNs)
        KDTree<AgentController> clonedRepo = repo;

        AgentControllerIndividual aci = (AgentControllerIndividual) inds[0];
        AgentController arbitrator = aci.decodeController();

        ArbitratorController ac = new ArbitratorController(arbitrator, clonedRepo);
        return new HomogeneousGroupController(ac);
    }

}
