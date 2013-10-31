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
import net.jafama.FastMath;

/**
 *
 * @author jorge
 */
public class ClusterSCPostEvaluator extends SCPostEvaluator {

    public static final String P_NUM_CLUSTERS = "k-clusters";
    protected int numClusters;
    protected double[][] clusters;
    protected int[] counts;
    protected KDTree<Integer> clusterTree;
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
        if (clusters == null) {
            initializeClusters(state);
            updateClusterTree();
        }

        // Rebuild the clusters with the evaluations from this generation
        if (updateClusters(state)) {
            // update individuals in novelty archive with the new clustering
            updateNoveltyArchive(state);
            // update tree
            updateClusterTree();
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
            byte[] s = buffer.get(key);
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
    protected boolean updateClusters(EvolutionState state) {
        assignements.clear();

        HashMap<Integer, Integer> centerCache = new HashMap<Integer, Integer>(buffer.size() * 2);

        // Cache the centers nearest to the elements of buffer
        for (Integer key : buffer.keySet()) {
            int cluster = closestCluster(buffer.get(key));
            centerCache.put(key, cluster);
        }

        // Update clusters
        for (Integer key : buffer.keySet()) {
            int c = centerCache.get(key); // get cached center
            counts[c]++; // update per-center counts
            float learningRate = 1.0f / counts[c]; // per-center learning rate
            double[] cluster = clusters[c];
            byte[] x = buffer.get(key);
            for (int i = 0; i < cluster.length; i++) {
                cluster[i] = (1 - learningRate) * cluster[i] + learningRate * x[i]; // gradient step
            }
        }

        // clear the structures
        buffer.clear();
        bufferCount.clear();
        return true;
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
