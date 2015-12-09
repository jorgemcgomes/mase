/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic.deprecated;

import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.util.Parameter;
import edu.wlu.cs.levy.CG.KDTree;
import edu.wlu.cs.levy.CG.KeySizeException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import mase.EvaluationResult;
import mase.ExpandedFitness;
import mase.MetaEvaluator;
import mase.PostEvaluator;
import mase.evaluation.BehaviourResult;
import mase.generic.SCPostEvaluator;
import mase.generic.SCResult;
import mase.novelty.NoveltyEvaluation;

/**
 *
 * @author jorge
 */
public class ClusterSCPostEvaluatorTree extends SCPostEvaluator {

    public static final String P_NUM_CLUSTERS = "k-clusters";
    protected int numClusters;
    protected double[][] clusters;
    protected int[] counts;
    protected Map<Integer, Integer> assignements;
    protected Map<Integer, byte[]> buffer;
    protected Map<Integer, Float> bufferCount;
    protected List<BehaviourResult> archive;
    protected KDTree<Integer> clusterTree;

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
        super.processPopulation(state); // filter

        HashMap<Integer, byte[]> genKey = new HashMap<Integer, byte[]>(1000);

        // Integrate the information from the evaluations of this generation
        for (Subpopulation sub : state.population.subpops) {
            for (Individual ind : sub.individuals) {
                for (EvaluationResult er : ((ExpandedFitness) ind.fitness).getEvaluationResults()) {
                    if (er instanceof SCResult) {
                        SCResult scr = (SCResult) er;
                        genKey.putAll(scr.getStates());
                        buffer.putAll(scr.getStates());
                        mergeCountMap(bufferCount, scr.getCounts());
                    }
                }
            }
        }

        // Rebuild the clusters
        updateClusters(state);

        // Make the assignements
        for (Entry<Integer, byte[]> e : genKey.entrySet()) {
            if (!assignements.containsKey(e.getKey())) {
                assignements.put(e.getKey(), closestCluster(e.getValue()));
            }
        }

        // Calculate cluster state count
        for (Subpopulation sub : state.population.subpops) {
            for (Individual ind : sub.individuals) {
                for (EvaluationResult er : ((ExpandedFitness) ind.fitness).getEvaluationResults()) {
                    if (er instanceof SCResult) {
                        computeClusteredCount((SCResult) er);
                    }
                }
            }
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
    }

    protected int closestCluster(byte[] candidate) {
        double[] cdouble = new double[candidate.length];
        for(int i = 0 ; i < candidate.length ; i++) {
            cdouble[i] = candidate[i];
        }
        try {
            return clusterTree.nearest(cdouble);
        } catch (KeySizeException ex) {
            Logger.getLogger(ClusterSCPostEvaluatorTree.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    protected void updateClusters(EvolutionState state) {
        if (clusters == null) {
            initializeClusters(state);
        }
        assignements.clear();

        // do the incremental update
        for (Integer key : buffer.keySet()) {
            int cluster = closestCluster(buffer.get(key));
            updateCluster(cluster, buffer.get(key), bufferCount.get(key));
            // TODO make the assignement of key to cluster?
        }

        // clear the structures
        buffer.clear();
        bufferCount.clear();

        // update individuals in novelty archive with the new clustering
        updateNoveltyArchive(state);
    }

    /*
     * Complete memory -- all samples weight the same
     */
    protected void updateCluster(int cl, byte[] candidate, float freq) {
        try {
            clusterTree.delete(clusters[cl]);
            for (int i = 0; i < Math.round(freq); i++) {
                counts[cl]++;
                for (int j = 0; j < candidate.length; j++) {
                    clusters[cl][j] += (1.0 / counts[cl]) * (candidate[j] - clusters[cl][j]);
                }
            }
            clusterTree.insert(clusters[cl], cl);
        } catch (Exception ex) {
            Logger.getLogger(ClusterSCPostEvaluatorTree.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    protected void initializeClusters(EvolutionState state) {
        this.clusters = new double[numClusters][];
        this.counts = new int[numClusters];
        // Forgy method -- randomly chooses k observations from the data set and uses these as the initial means
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
            //System.out.println(Arrays.toString(cl));
            clusters[clusterIndex++] = cl;
        }
        clusterTree = new KDTree<Integer>(clusters[0].length);
        for(int i = 0 ; i < numClusters ; i++) {
            try {
                clusterTree.insert(clusters[i], i);
            } catch (Exception ex) {
                Logger.getLogger(ClusterSCPostEvaluatorTree.class.getName()).log(Level.SEVERE, null, ex);
            }
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
