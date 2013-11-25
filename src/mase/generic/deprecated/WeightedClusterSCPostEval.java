/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic;

import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import mase.novelty.NoveltyFitness;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

/**
 *
 * @author jorge
 */
public class WeightedClusterSCPostEval extends ClusterSCPostEvalBalanced {

    @Override
    protected void initializeClusters(EvolutionState state) {
        // initialization should also be biased. the probability of being chosen
        // is proportional to the weight

        this.clusters = new double[numClusters][];
        this.counts = new int[numClusters];
        Integer[] list = new Integer[buffer.size()];
        buffer.keySet().toArray(list);
        HashSet<Integer> randomKeys = new HashSet<Integer>(numClusters * 2);
        HashMap<Integer, Double> pointWeight = stateCorrelations(state);
        double totalWeight = 0;
        for (Double d : pointWeight.values()) {
            totalWeight += d;
        }

        while (randomKeys.size() < numClusters) {
            int next = -1;
            double rand = state.random[0].nextDouble() * totalWeight;
            for(int i = 0 ; i < list.length ; i++) {
                rand -= pointWeight.get(list[i]);
                if(rand <= 0.0) {
                    next = list[i];
                    break;
                }
            }
            if (!randomKeys.contains(next)) {
                randomKeys.add(next);
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

    private HashMap<Integer, Double> stateCorrelations(EvolutionState state) {
        SpearmansCorrelation spearman = new SpearmansCorrelation();
        HashMap<Integer, Double> pointWeight = new HashMap<Integer, Double>(buffer.size() * 2);
        double[] fitScores = new double[super.currentPop.size()];
        int index = 0;
        for (Subpopulation sub : state.population.subpops) {
            for (Individual ind : sub.individuals) {
                NoveltyFitness nf = (NoveltyFitness) ind.fitness;
                fitScores[index++] = nf.getFitnessScore();
            }
        }
        for (Integer k : buffer.keySet()) {
            double[] cs = new double[super.currentPop.size()];
            for (int i = 0; i < super.currentPop.size(); i++) {
                Float count = currentPop.get(i).getCounts().get(k);
                cs[i] = count == null ? 0 : count;
            }
            double c = Math.abs(spearman.correlation(fitScores, cs));
            pointWeight.put(k, c);
        }
        return pointWeight;
    }

    @Override
    protected void updateClusters(EvolutionState state) {
        // the update exerted by each data point is weighted by the importance
        // of the point. irrelevant points are not taken into account in the
        // clustering process

        // Compute the weights of the individual states
        HashMap<Integer, Double> pointWeight = stateCorrelations(state);

        // Cache the centers nearest to the elements of buffer
        HashMap<Integer, Integer> centerCache = new HashMap<Integer, Integer>(buffer.size() * 2);
        int[] genCounts = new int[clusters.length];
        double[] weightTotals = new double[clusters.length];
        for (Integer key : buffer.keySet()) {
            int cluster = assignements.containsKey(key) ? assignements.get(key) : closestCluster(globalKey.get(key));
            centerCache.put(key, cluster);
            genCounts[cluster]++;
            counts[cluster]++;
            weightTotals[cluster] += pointWeight.get(key);
        }

        // Normalize weights
        for (Entry<Integer, Double> e : pointWeight.entrySet()) {
            int closest = centerCache.get(e.getKey());
            e.setValue(e.getValue() * genCounts[closest] / weightTotals[closest]);
        }

        // Calculate per-cluster adjustement rates
        float[] adjWeights = new float[clusters.length];
        for (int i = 0; i < genCounts.length; i++) {
            if (genCounts[i] > 0) {
                adjWeights[i] = Math.max(1f / counts[i], minLearningRate / genCounts[i]);
            }
        }

        // Update clusters
        for (Integer key : buffer.keySet()) {
            int c = centerCache.get(key); // get closest cluster
            double[] cluster = clusters[c];
            double learningRate = adjWeights[c] * pointWeight.get(key);
            if (learningRate >= 1) {
                System.out.println("Warning: " + learningRate);
            }
            byte[] x = globalKey.get(key); // new data point
            for (int i = 0; i < cluster.length; i++) {
                cluster[i] += learningRate * (x[i] - cluster[i]); // gradient step
            }
        }

        buffer.clear();
        buffer.clear();

    }

}
