/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic;

import ec.EvolutionState;
import java.util.HashMap;

/**
 *
 * @author jorge
 */
public class ClusterSCPostEval2 extends ClusterSCPostEvaluator {

    @Override
    /**
     * As in "Web-Scale K-Means Clustering
     */
    protected void updateClusters(EvolutionState state) {
        if (clusters == null) {
            initializeClusters(state);
        }
        assignements.clear();
        
        HashMap<Integer, Integer> centerCache = new HashMap<Integer, Integer>(buffer.size() * 2);

        // Cache the centers nearest to the elements of buffer
        for (Integer key : buffer.keySet()) {
            int cluster = closestCluster(buffer.get(key));
            centerCache.put(key, cluster);
        }
        
        // Update clusters
        for(Integer key : buffer.keySet()) {
            int c = centerCache.get(key); // get cached center
            counts[c]++; // update per-center counts
            float learningRate = 1.0f / counts[c]; // per-center learning rate
            float[] cluster = clusters[c];
            byte[] x = buffer.get(key);
            for(int i = 0 ; i < cluster.length ; i++) {
                cluster[i] = (1 - learningRate) * cluster[i] + learningRate * x[i]; // gradient step
            }
        }
        
        // clear the structures
        buffer.clear();
        bufferCount.clear();

        // update individuals in novelty archive with the new clustering
        updateNoveltyArchive(state);

    }
}
