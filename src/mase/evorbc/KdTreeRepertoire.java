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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import mase.controllers.AgentController;
import mase.evaluation.CompoundEvaluationResult;
import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.stat.PersistentSolution;
import mase.stat.SolutionPersistence;
import mase.util.KdTree;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author jorge
 */
public class KdTreeRepertoire implements Repertoire {

    private static final long serialVersionUID = 1L;
    public static final double FLOAT_DELTA = 0.001;
    
    // Cache to avoid many instances of the same repertoire when de-serializing many controllers using the same repertoire
    // Currently only supports one repo at a time (should be enough for most cases)
    // TODO: there WILL be problems if any controller modifies these structures for some reason
    private static transient KdTree<Integer> _cachedTree;
    private static transient Map<Integer, AgentController> _cachedPrimitives;
    private static transient long _cachedRepHash, _cachedCoordsHash;
    private static transient boolean _cachedIgnoreConstant;

    private transient KdTree<Integer> tree;
    private transient Map<Integer, AgentController> primitives;

    private Pair<Double, Double>[] bounds;
    private boolean ignoreConstant;
    private File repFile, coordsFile;
    private long repFileHash, coordsFileHash;

    @Override
    public Pair<Integer, AgentController> nearest(double[] coordinates) {
        ArrayList<KdTree.SearchResult<Integer>> nearest = tree.nearestNeighbours(coordinates, 1);
        int id = nearest.get(0).payload;
        return Pair.of(id, primitives.get(id));
    }

    @Override
    public Repertoire deepCopy() {
        KdTreeRepertoire newRep = new KdTreeRepertoire();
        newRep.tree = this.tree;
        newRep.bounds = this.bounds;
        newRep.repFile = this.repFile;
        newRep.coordsFile = this.coordsFile;
        newRep.repFileHash = this.repFileHash;
        newRep.coordsFileHash = this.coordsFileHash;
        newRep.ignoreConstant = this.ignoreConstant;
        
        newRep.primitives = new HashMap<>(this.primitives);
        for(Map.Entry<Integer,AgentController> e : newRep.primitives.entrySet()) {
            e.setValue(e.getValue().clone());
        }
        return newRep;
    }

    @Override
    public void load(File repo, File coordinates, boolean ignoreConstant) throws IOException {
        repFile = repo;
        repFileHash = FileUtils.checksumCRC32(repFile);
        if (coordinates != null) {
            coordsFile = coordinates;
            coordsFileHash = FileUtils.checksumCRC32(coordsFile);
        }
        this.ignoreConstant = ignoreConstant;

        List<PersistentSolution> solutions;
        try {
            solutions = SolutionPersistence.readSolutionsFromTar(repo);
        } catch (Exception ex) {
            Logger.getLogger(ArbitratorFactory.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        Map<Integer, double[]> coords;
        if (coordinates != null) {
            coords = fileCoordinates(coordinates);
            if(coords == null || coords.isEmpty()) {
                throw new IOException("Empty or invalid coordinate file");
            }
            if (coords.size() > solutions.size()) {
                throw new IOException("Number of coordinates is greater than the number of solutions in repertoire");
            }
        } else {
            coords = encodedCoordinates(solutions);
        }
        
        int n = coords.values().iterator().next().length;

        if(true) {
            // check which features are constant
            ArrayList<Integer> toRemove = new ArrayList<>();
            for(int i = 0 ; i < n ; i++) {
                boolean ignore = true;
                double v = coords.values().iterator().next()[i];
                for(double[] coord : coords.values()) {
                    if(Math.abs(coord[i] - v) > FLOAT_DELTA) {
                        ignore = false;
                        break;
                    }
                }
                if(ignore) {
                    toRemove.add(i);
                }
            }
            
            // remove constant features from the coordinates
            if(!toRemove.isEmpty()) {
                if(toRemove.size() == n) {
                    throw new IOException("Zero non-constant features");
                }
                int[] toRemoveA = ArrayUtils.toPrimitive(toRemove.toArray(new Integer[toRemove.size()]));
                System.out.println("Removing features: " + Arrays.toString(toRemoveA));
                for(Entry<Integer,double[]> e : coords.entrySet()) {
                    e.setValue(ArrayUtils.removeAll(e.getValue(), toRemoveA));
                }
                n = n - toRemove.size();
            }
        }
        
        // bounds might already have been computed if the object is being de-serialized
        if(bounds == null) {
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
        }


        tree = new KdTree.Euclidean<>(n);
        primitives = new HashMap<>();
        for (int i = 0; i < solutions.size(); i++) {
            PersistentSolution sol = solutions.get(i);
            AgentController ac = sol.getController().getAgentControllers(1)[0];
            double[] c = coords.get(sol.getIndex());
            if (c != null) {
                tree.addPoint(c, sol.getIndex());
                primitives.put(sol.getIndex(), ac);
            }
        }
        if (tree.size() != coords.size()) {
            throw new IOException("Some of the coordinates were not used");
        }
    }

    private Map<Integer, double[]> encodedCoordinates(Collection<PersistentSolution> solutions) {
        Map<Integer, double[]> coords = new HashMap<>();
        for (PersistentSolution sol : solutions) {
            if (sol.getUserData() instanceof double[]) {
                coords.put(sol.getIndex(), (double[]) sol.getUserData());
            } else {
                EvaluationResult[] evalResults = sol.getEvalResults();
                for (EvaluationResult e : evalResults) {
                    if (e instanceof CompoundEvaluationResult) {
                        e = ((CompoundEvaluationResult) e).getEvaluation(sol.getSubpop());
                    }
                    if (e instanceof VectorBehaviourResult) {
                        coords.put(sol.getIndex(), ((VectorBehaviourResult) e).getBehaviour());
                        break;
                    }
                }
            }
        }
        return coords;
    }

    private Map<Integer, double[]> fileCoordinates(File file) throws FileNotFoundException {
        Map<Integer, double[]> res = new HashMap<>();
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
        
        synchronized(KdTreeRepertoire.class) {
            if(_cachedTree != null && repFileHash == _cachedRepHash && coordsFileHash == _cachedCoordsHash && ignoreConstant == _cachedIgnoreConstant) {
                this.tree = _cachedTree;
                this.primitives = _cachedPrimitives;
            } else {
                load(repFile, coordsFile, ignoreConstant);
                _cachedRepHash = repFileHash;
                _cachedCoordsHash = coordsFileHash;
                _cachedTree = this.tree;
                _cachedPrimitives = this.primitives;
                _cachedIgnoreConstant = this.ignoreConstant;
            }
        }        
    }
}
