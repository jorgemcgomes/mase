/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic;

import ec.EvolutionState;
import ec.util.Parameter;
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

/**
 *
 * @author jorge
 */
public class ClusterSCPostEvaluator extends SCPostEvaluator {

    public static final String P_NUM_CLUSTERS = "k-clusters";
    protected int numClusters;
    protected float[][] clusters;
    protected int[] counts;
    protected Map<Integer, Integer> assignements; // state-cluster assignments
    protected Map<Integer, byte[]> buffer; // elements to be added to clusters
    protected Map<Integer, Float> bufferCount; // counts of the above elements
    protected List<BehaviourResult> archive;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.numClusters = state.parameters.getInt(base.push(P_NUM_CLUSTERS),
                new Parameter(P_STATECOUNT_BASE).push(P_NUM_CLUSTERS));
        this.buffer = new HashMap<Integer, byte[]>(1000);
        this.assignements = new HashMap<Integer, Integer>(1000);
        this.bufferCount = new HashMap<Integer, Float>(1000);
    }

    @Override
    public void processPopulation(EvolutionState state) {
        super.processPopulation(state); // Filter

        HashMap<Integer, byte[]> genKey = new HashMap<Integer, byte[]>(1000);
        // Integrate the information from the evaluations of this generation
        for (SCResult scr : super.currentPop) {
            genKey.putAll(scr.getStates());
            buffer.putAll(scr.getStates());
            mergeCountMap(bufferCount, scr.getCounts());
        }
        
        // Initialize
        if(clusters == null) {
            initializeClusters(state);
        }

        // Rebuild the clusters with the evaluations from this generation
        if (updateClusters(state)) {
            // update individuals in novelty archive with the new clustering
            updateNoveltyArchive(state);
        }

        // Make the assignements
        for (Entry<Integer, byte[]> e : genKey.entrySet()) {
            if (!assignements.containsKey(e.getKey())) {
                assignements.put(e.getKey(), closestCluster(e.getValue()));
            }
        }

        // Calculate cluster state counts
        for (SCResult scr : super.currentPop) {
            computeClusteredCount(scr);
        }

        if (super.doTfIdf) {
            // cluster count totals
            float[] clusterCount = new float[numClusters];
            for (SCResult scr : super.currentPop) {
                for (int i = 0; i < numClusters; i++) {
                    clusterCount[i] += scr.rawClusteredCount[i];
                }
            }

            // adjust population counts
            for (SCResult scr : super.currentPop) {
                for (int i = 0; i < numClusters; i++) {
                    if (scr.rawClusteredCount[i] > 0) {
                        scr.getBehaviour()[i] = scr.rawClusteredCount[i] / clusterCount[i];
                    }
                }
            }

            // adjust archive counts
            for (BehaviourResult br : archive) {
                SCResult scr = (SCResult) br;
                for (int i = 0; i < numClusters; i++) {
                    if (scr.getBehaviour()[i] > 0) {
                        scr.getBehaviour()[i] = scr.rawClusteredCount[i] / clusterCount[i];
                    }
                }
            }
        }
    }
    
    /*
    Forgy method -- randomly chooses k observations from the data set and uses these as the initial means
    */
    protected void initializeClusters(EvolutionState state) {
        this.clusters = new float[numClusters][];
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
            byte[] s = buffer.get(key);
            float[] cl = new float[s.length];
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

    // TODO: replace with space partitioning tree?
    protected int closestCluster(byte[] candidate) {
        double closestDist = Double.POSITIVE_INFINITY;
        int closestCluster = -1;
        for (int i = 0; i < clusters.length; i++) {
            float[] c = clusters[i];
            double dist = centerDistance(c, candidate);
            if (dist < closestDist) {
                closestDist = dist;
                closestCluster = i;
            }
        }
        return closestCluster;
    }

    protected double centerDistance(float[] cluster, byte[] candidate) {
        double d = 0;
        for (int i = 0; i < cluster.length; i++) {
            d += Math.pow(cluster[i] - candidate[i], 2);
        }
        return d;
    }

    /*
     * Returns TRUE if the clusters changed. FALSE otherwise.
     */
    protected boolean updateClusters(EvolutionState state) {
        assignements.clear();

        // do the incremental update
        for (Integer key : buffer.keySet()) {
            byte[] candidate = buffer.get(key);
            int c = closestCluster(candidate);
            counts[c]++;
            for (int j = 0; j < candidate.length; j++) {
                clusters[c][j] += (1.0 / counts[c]) * (candidate[j] - clusters[c][j]);
            }
        }

        // clear the structures
        buffer.clear();
        bufferCount.clear();
        return true;
    }


    protected void updateNoveltyArchive(EvolutionState state) {
        if (archive == null) {
            PostEvaluator[] evals = ((MetaEvaluator) state.evaluator).getPostEvaluators();
            for (PostEvaluator pe : evals) {
                if (pe instanceof NoveltyEvaluation) {
                    NoveltyEvaluation ne = (NoveltyEvaluation) pe;
                    archive = ne.getArchives().get(0);
                }
            }
        }
        for (BehaviourResult br : archive) {
            SCResult scr = (SCResult) br;
            // make the cluster assignements
            for (Entry<Integer, byte[]> e : scr.getStates().entrySet()) {
                if (!assignements.containsKey(e.getKey())) {
                    assignements.put(e.getKey(), closestCluster(e.getValue()));
                }
            }
            // make cluster vector
            computeClusteredCount(scr);
        }
    }
}
