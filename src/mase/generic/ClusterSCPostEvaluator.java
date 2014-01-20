/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic;

import ec.EvolutionState;
import ec.util.Parameter;
import edu.wlu.cs.levy.CG.KDTree;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import mase.MetaEvaluator;
import mase.PostEvaluator;
import mase.evaluation.BehaviourResult;
import static mase.generic.SCPostEvaluator.mergeCountMap;
import mase.novelty.NoveltyEvaluation;
import mase.novelty.NoveltyEvaluation.ArchiveEntry;
import net.jafama.FastMath;

/**
 *
 * @author jorge
 */
public class ClusterSCPostEvaluator extends SCPostEvaluator {

    public static final String P_NUM_CLUSTERS = "k-clusters";
    public static final String P_ALLOWED_CHANGE = "allowed-change";
    public static final String P_MAX_FREQUENCY = "max-update-freq";
    protected int numClusters;
    protected double[][] clusters;
    protected int[] counts;
    protected KDTree<Integer> clusterTree;
    protected Map<Integer, Integer> assignements; // state-cluster assignments
    protected Map<Integer, Float> buffer; // counts of the above elements
    protected List<ArchiveEntry> archive;
    protected int updateFrequency;
    protected int maxUpdateFrequency;
    protected int lastUpdate;
    protected double allowedChange;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.numClusters = state.parameters.getInt(base.push(P_NUM_CLUSTERS),
                new Parameter(P_STATECOUNT_BASE).push(P_NUM_CLUSTERS));
        this.buffer = new HashMap<Integer, Float>(1000);
        this.assignements = new HashMap<Integer, Integer>(1000);
        this.updateFrequency = 1;
        this.lastUpdate = 0;
        this.allowedChange = state.parameters.getDouble(base.push(P_ALLOWED_CHANGE),
                new Parameter(P_STATECOUNT_BASE).push(P_ALLOWED_CHANGE));
        this.maxUpdateFrequency = state.parameters.getInt(base.push(P_MAX_FREQUENCY),
                new Parameter(P_STATECOUNT_BASE).push(P_MAX_FREQUENCY));
    }

    @Override
    public void processPopulation(EvolutionState state) {
        super.processPopulation(state); // Filter

        
        HashSet<Integer> genKeys = new HashSet<Integer>(1000);
        // Integrate the information from the evaluations of this generation
        for (SCResult scr : super.currentPop) {
            genKeys.addAll(scr.getCounts().keySet());
            mergeCountMap(buffer, scr.getCounts());
        }

        // Initialize
        if (clusters == null) {
            initializeClusters(state);
            updateClusterTree();
        }

        
        if (state.generation == 0 || state.generation - lastUpdate == updateFrequency) {
            // Make copy of the clusters
            double[][] clusterCopy = new double[numClusters][];
            for(int i = 0 ; i < numClusters ; i++) {
                clusterCopy[i] = Arrays.copyOf(clusters[i], clusters[i].length);
            }
            
            // Rebuild the clusters with the evaluations from this generation
            updateClusters(state);        
            
            // Calculate difference from previous
            double meanDiff = 0, maxDiff = 0;
            for(int i = 0 ; i < numClusters ; i++) {
                for(int j = 0 ; j < clusters[i].length ; j++) {
                    meanDiff += Math.abs(clusters[i][j] - clusterCopy[i][j]) / clusters[i].length;
                    maxDiff = Math.max(maxDiff, Math.abs(clusters[i][j] - clusterCopy[i][j]));
                }
            }
            meanDiff /= numClusters;
            
            // Update update-frequency
            if(meanDiff > allowedChange) {
                updateFrequency = Math.max(1, updateFrequency - 1);
            } else if(meanDiff < allowedChange) {
                updateFrequency = Math.min(maxUpdateFrequency, updateFrequency + 1);
            }
            this.lastUpdate = state.generation;
            
            state.output.message("Clusters updated. Mean diff: " + meanDiff + " | Max diff: " + maxDiff + " | New update freq: " + updateFrequency);
            
            // clear the assignements
            assignements.clear();
            // update tree
            updateClusterTree();
            // update individuals in novelty archive with the new clustering
            updateNoveltyArchive(state);
        }

        // Make the assignements of the states in the current population
        for (Integer id : genKeys) {
            if (!assignements.containsKey(id)) {
                assignements.put(id, closestCluster(globalKey.get(id)));
            }
        }

        // Calculate cluster state counts
        for (SCResult scr : super.currentPop) {
            computeClusteredCount(scr);
        }

        if (super.doTfIdf) {
            float[] clusterCount = new float[numClusters];
            int[] visited = new int[numClusters];
            for (SCResult scr : super.currentPop) {
                for (int i = 0; i < numClusters; i++) {
                    if (scr.rawClusteredCount[i] > 0) {
                        clusterCount[i] += scr.rawClusteredCount[i];
                        visited[i]++;
                    }
                }
            }
            /*for (BehaviourResult br : archive) {
                SCResult scr = (SCResult) br;
                for (int i = 0; i < numClusters; i++) {
                    if (scr.rawClusteredCount[i] > 0) {
                        clusterCount[i] += scr.rawClusteredCount[i];
                    }
                }
            }*/   
            
            // adjust population counts
            for (SCResult scr : super.currentPop) {
                for (int i = 0; i < numClusters; i++) {
                    if (scr.rawClusteredCount[i] > 0) {
                        scr.getBehaviour()[i] = clusterCount[i] > 0 ? scr.rawClusteredCount[i] / (clusterCount[i] / currentPop.size()) : 0;
                    }
                }
            }
            // adjust archive counts
            for (ArchiveEntry ar : archive) {
                SCResult scr = (SCResult) ar.getBehaviour();
                for (int i = 0; i < numClusters; i++) {
                    scr.getBehaviour()[i] = clusterCount[i] > 0 ? scr.rawClusteredCount[i] / (clusterCount[i] /currentPop.size()) : 0;
                }
            }
        }
    }

    /*
     Forgy method -- randomly chooses k observations from the data set and uses these as the initial means
     */
    protected void initializeClusters(EvolutionState state) {
        this.clusters = new double[numClusters][];
        this.counts = new int[numClusters];
        Object[] list = buffer.keySet().toArray();
        HashSet<Integer> randomKeys = new HashSet<Integer>(numClusters * 2);
        while (randomKeys.size() < numClusters) {
            int next = state.random[0].nextInt(list.length);
            if (!randomKeys.contains(next)) {
                randomKeys.add((Integer) list[next]);
            }
        }
        int clusterIndex = 0;
        for (Integer key : randomKeys) {
            byte[] s = globalKey.get(key);
            double[] cl = new double[s.length];
            for (int i = 0; i < s.length; i++) {
                cl[i] = s[i];
            }
            clusters[clusterIndex++] = cl;
        }
    }

    protected void computeClusteredCount(SCResult scr) {
        float[] clusterCount = new float[clusters.length];
        Arrays.fill(clusterCount, 0);
        for (Entry<Integer, Float> e : scr.getCounts().entrySet()) {
            int clusterIndex = assignements.get(e.getKey());
            clusterCount[clusterIndex] += e.getValue();
        }
        scr.setBehaviour(clusterCount);
        scr.rawClusteredCount = Arrays.copyOf(clusterCount, clusterCount.length);
    }

    protected int closestCluster(byte[] candidate) {
        double[] cand = new double[candidate.length];
        for (int i = 0; i < cand.length; i++) {
            cand[i] = candidate[i];
        }
        return clusterTree.nearest(cand);
    }

    protected double centerDistance(double[] cluster, byte[] candidate) {
        double d = 0;
        for (int i = 0; i < cluster.length; i++) {
            d += FastMath.pow(cluster[i] - candidate[i], 2);
        }
        return d;
    }

    /*
     * Returns TRUE if the clusters changed. FALSE otherwise.
     */
    protected void updateClusters(EvolutionState state) {
        HashMap<Integer, Integer> centerCache = new HashMap<Integer, Integer>(buffer.size() * 2);

        // Cache the centers nearest to the elements of buffer
        for (Integer key : buffer.keySet()) {
            int cluster = assignements.containsKey(key) ? assignements.get(key) : closestCluster(globalKey.get(key));
            centerCache.put(key, cluster);
        }

        // Update clusters
        for (Integer key : buffer.keySet()) {
            int c = centerCache.get(key); // get cached center
            counts[c]++; // update per-center counts
            float learningRate = 1.0f / counts[c]; // per-center learning rate
            double[] cluster = clusters[c];
            byte[] x = globalKey.get(key);
            for (int i = 0; i < cluster.length; i++) {
                cluster[i] = (1 - learningRate) * cluster[i] + learningRate * x[i]; // gradient step
            }
        }

        buffer.clear();
        buffer.clear();
    }

    protected void updateClusterTree() {
        clusterTree = new KDTree<Integer>(clusters[0].length);
        for (int i = 0; i < clusters.length; i++) {
            clusterTree.insert(clusters[i], i);
        }
    }

    protected void updateNoveltyArchive(EvolutionState state) {
        if (archive == null) {
            PostEvaluator[] evals = ((MetaEvaluator) state.evaluator).getPostEvaluators();
            for (PostEvaluator pe : evals) {
                if (pe instanceof NoveltyEvaluation) {
                    NoveltyEvaluation ne = (NoveltyEvaluation) pe;
                    archive = ne.getArchives()[0];
                }
            }
        }
        for (ArchiveEntry ar : archive) {
            SCResult scr = (SCResult) ar.getBehaviour();
            // make the cluster assignements
            for (Integer key : scr.getCounts().keySet()) {
                if (!assignements.containsKey(key)) {
                    assignements.put(key, closestCluster(globalKey.get(key)));
                }
            }
            // make cluster vector
            computeClusteredCount(scr);
        }
    }
}
