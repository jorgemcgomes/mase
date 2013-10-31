/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic;

import ec.EvolutionState;
import ec.util.Parameter;
import java.util.HashMap;

/**
 *
 * @author jorge
 */
public class ClusterSCPostEvalBalanced extends ClusterSCPostEvaluator {

    /*
    Meaning: The elements from this generation should account for at least 
    min-learning-rate in the adjustment of the cluster centers
    */
    protected float minLearningRate;
    public static final String P_MIN_LEARNING_RATE = "min-learning-rate";

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.minLearningRate = state.parameters.getFloat(base.push(P_MIN_LEARNING_RATE),
                new Parameter(SCPostEvaluator.P_STATECOUNT_BASE).push(P_MIN_LEARNING_RATE));
    }

    @Override
    protected boolean updateClusters(EvolutionState state) {
        assignements.clear();

        HashMap<Integer, Integer> centerCache = new HashMap<Integer, Integer>(buffer.size() * 2);

        int[] genCounts = new int[clusters.length];
        // Cache the centers nearest to the elements of buffer
        for (Integer key : buffer.keySet()) {
            int cluster = closestCluster(buffer.get(key));
            centerCache.put(key, cluster);
            genCounts[cluster]++;
            counts[cluster]++;
        }
        
        // calculate per-center adjustement rates
        float[] adjWeights = new float[clusters.length];
        for(int i = 0 ; i < genCounts.length ; i++) {
            if(genCounts[i] > 0) {
                adjWeights[i] = Math.max(1f / counts[i], minLearningRate / genCounts[i]);
            }
        }
        
        // Update clusters
        for (Integer key : buffer.keySet()) {
            int c = centerCache.get(key); // get cached center
            double[] cluster = clusters[c];
            byte[] x = buffer.get(key);
            for (int i = 0; i < cluster.length; i++) {
                cluster[i] = (1 - adjWeights[i]) * cluster[i] + adjWeights[i] * x[i]; // gradient step
            }
        }

        // clear the structures
        buffer.clear();
        bufferCount.clear();

        return true;
    }
}
