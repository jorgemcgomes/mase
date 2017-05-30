/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import mase.controllers.AgentController;
import mase.stat.PersistentSolution;
import mase.stat.SolutionPersistence;
import mase.util.KdTree;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author jorge
 */
public class KdTreeRepertoire implements Repertoire {

    private static final long serialVersionUID = 1L;
    private transient KdTree<AgentController> tree;
    private Pair<Double, Double>[] bounds;

    private File repFile, coordsFile;
    private long repFileHash, coordsFileHash;

    //coords.get(solutions.get(0).getIndex()).length
    @Override
    public AgentController nearest(double[] coordinates) {
        ArrayList<KdTree.SearchResult<AgentController>> nearest = tree.nearestNeighbours(coordinates, 1);
        return nearest.get(0).payload;
    }

    @Override
    public Repertoire deepCopy() {
        // TODO: clone repo -- needed because the agentcontrollers can have state, and therefore
        // should not be used simultaneously in different evaluation threads
        // but this should be fine for stateless ACs (fixed values or non-recurrent NNs)
        return this;
    }

    @Override
    public void load(File repo, File coordinates) throws IOException {
        repFile = repo;
        repFileHash = FileUtils.checksumCRC32(repFile);
        if (coordinates != null) {
            coordsFile = coordinates;
            coordsFileHash = FileUtils.checksumCRC32(coordsFile);
        }

        List<PersistentSolution> solutions;
        try {
            solutions = SolutionPersistence.readSolutionsFromTar(repo);
        } catch (Exception ex) {
            Logger.getLogger(ArbitratorFactory.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        Map<Integer, double[]> coords = null;
        if (coordinates != null) {
            coords = fileCoordinates(coordinates);
            if (coords.size() != solutions.size()) {
                throw new IOException("Number of solutions in repertoire does not match number of coordinates");
            }
        } else {
            coords = encodedCoordinates(solutions);
        }

        int n = coords.get(solutions.get(0).getIndex()).length;

        bounds = new Pair[n];
        for (int i = 0; i < n; i++) {
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;
            for (double[] c : coords.values()) {
                min = Math.min(min, c[i]);
                max = Math.max(max, c[i]);
            }
            bounds[i] = Pair.of(min, max);
        }

        tree = new KdTree.Euclidean<>(n);
        for (int i = 0; i < solutions.size(); i++) {
            PersistentSolution sol = solutions.get(i);
            AgentController ac = sol.getController().getAgentControllers(1)[0];
            double[] c = coords.get(sol.getIndex());
            if (c == null) {
                throw new IOException("Coordinate not found for index " + sol.getIndex());
            }
            tree.addPoint(c, ac);
        }
    }

    private Map<Integer, double[]> encodedCoordinates(Collection<PersistentSolution> solutions) {
        Map<Integer, double[]> coords = new HashMap<>();
        for (PersistentSolution sol : solutions) {
            coords.put(sol.getIndex(), (double[]) sol.getUserData());
        }
        return coords;
    }

    private Map<Integer, double[]> fileCoordinates(File file) {
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
    public Pair<Double, Double>[] coordinateBounds() {
        return bounds;
    }

    @Override
    public int size() {
        return tree.size();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        long cr = FileUtils.checksumCRC32(repFile);
        if (repFileHash != cr) {
            throw new IOException("Repertoire file does not match the expected file");
        }
        if (coordsFile != null) {
            long cc = FileUtils.checksumCRC32(coordsFile);
            if (coordsFileHash != cc) {
                throw new IOException("Coordinates file does not match the expected file");
            }
        }
        load(repFile, coordsFile);
    }
}
