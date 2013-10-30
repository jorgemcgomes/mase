/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic;

import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.util.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import mase.EvaluationResult;
import mase.ExpandedFitness;
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
    protected Map<Integer, Integer> assignements;
    protected Map<Integer, byte[]> buffer;
    protected Map<Integer, Float> bufferCount;
    protected List<BehaviourResult> archive;
    //protected KDTree clusterTree;

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

        if (super.doTfIdf) {
            // cluster count totals
            float[] clusterCount = new float[numClusters];
            for (Subpopulation sub : state.population.subpops) {
                for (Individual ind : sub.individuals) {
                    for (EvaluationResult er : ((ExpandedFitness) ind.fitness).getEvaluationResults()) {
                        if (er instanceof SCResult) {
                            SCResult r = (SCResult) er;
                            for (int i = 0; i < numClusters; i++) {
                                clusterCount[i] += r.rawClusteredCount[i];
                            }
                        }
                    }
                }
            }

            // adjust population counts
            for (Subpopulation sub : state.population.subpops) {
                for (Individual ind : sub.individuals) {
                    for (EvaluationResult er : ((ExpandedFitness) ind.fitness).getEvaluationResults()) {
                        if (er instanceof SCResult) {
                            SCResult r = (SCResult) er;
                            for (int i = 0; i < numClusters; i++) {
                                if (r.rawClusteredCount[i] > 0) {
                                    r.getBehaviour()[i] = r.rawClusteredCount[i] / clusterCount[i];
                                }
                            }
                        }
                    }
                }
            }

            // adjust archive counts
            for (BehaviourResult br : archive) {
                SCResult scr = (SCResult) br;
                for (int i = 0; i < numClusters; i++) {
                    if (scr.getBehaviour()[i] > 0) {
                        scr.getBehaviour()[i] = scr.getBehaviour()[i] / clusterCount[i];
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
        scr.rawClusteredCount = Arrays.copyOf(clusterCount, clusterCount.length);
    }

    // TODO: replace with space partitioning tree
    protected int closestCluster(byte[] candidate) {
        double closestDist = Double.POSITIVE_INFINITY;
        int closestCluster = -1;
        for (int i = 0; i < clusters.length; i++) {
            float[] c = clusters[i];
            double dist = distance(c, candidate);
            if (dist < closestDist) {
                closestDist = dist;
                closestCluster = i;
            }
        }
        return closestCluster;
    }

    protected double distance(float[] clusters, byte[] candidate) {
        double d = 0;
        for (int i = 0; i < clusters.length; i++) {
            d += Math.pow(clusters[i] - candidate[i], 2);
        }
        return d;
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
    private void updateCluster(int cl, byte[] candidate, float freq) {
        for (int i = 0; i < Math.round(freq); i++) {
            counts[cl]++;
            for (int j = 0; j < candidate.length; j++) {
                clusters[cl][j] += (1.0 / counts[cl]) * (candidate[j] - clusters[cl][j]);
            }
        }
    }

    protected void initializeClusters(EvolutionState state) {
        this.clusters = new float[numClusters][];
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
            float[] cl = new float[s.length];
            for (int i = 0; i < s.length; i++) {
                cl[i] = s[i];
            }
            clusters[clusterIndex++] = cl;
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
