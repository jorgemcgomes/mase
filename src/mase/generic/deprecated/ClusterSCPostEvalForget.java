/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic.deprecated;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.generic.ClusterSCPostEvaluator;
import mase.generic.SCPostEvaluator;

/**
 *
 * @author jorge
 */
public class ClusterSCPostEvalForget extends ClusterSCPostEvaluator {

    protected float learningRate;
    public static final String P_LEARNING_RATE = "learning-rate";

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.learningRate = state.parameters.getFloat(base.push(P_LEARNING_RATE),
                new Parameter(SCPostEvaluator.P_STATECOUNT_BASE).push(P_LEARNING_RATE));
    }

    @Override
    protected void updateClusters(EvolutionState state) {
        if (clusters == null) {
            initializeClusters(state);
        }
        assignements.clear();

        // do the incremental update
        for (Integer key : buffer.keySet()) {
            byte[] s = buffer.get(key);
            int c = closestCluster(s);
            counts[c]++;
            float rate = Math.max(1f / counts[c], learningRate);
            for (int j = 0; j < s.length; j++) {
                clusters[c][j] += rate * (s[j] - clusters[c][j]);
            }
        }

        // clear the structures
        buffer.clear();
        bufferCount.clear();

        // update individuals in novelty archive with the new clustering
        updateNoveltyArchive(state);

    }
    /*if (clusters == null) {
     initializeClusters(state);
     }
     assignements.clear();

     // update step
     int numChanges;
     for (int i = 0; i < 1000; i++) {
     float[][] totals = new float[numClusters][clusters[0].length];
     int[] totalCounts = new int[numClusters];

     // Assignement step
     numChanges = 0;
     for (Map.Entry<Integer, byte[]> e : buffer.entrySet()) {
     Integer a = assignements.get(e.getKey());
     int c = closestCluster(e.getValue());
     assignements.put(e.getKey(), c);
     if (a == null || (int) a != c) {
     numChanges++;
     }
     for (int j = 0; j < e.getValue().length; j++) {
     totals[c][j] += e.getValue()[j];
     }
     totalCounts[c]++;
     }

     // Terminate if there were no changes in the assignements
     if (numChanges == 0) {
     System.out.println("Clustering converged with " + i + " steps");
     break;
     }

     // Update step
     for (int c = 0; c < numClusters; c++) {
     for (int j = 0; j < totals[0].length; j++) {
     clusters[c][j] = totals[c][j] / totalCounts[c];
     }
     }
     }

     // clear the structures
     buffer.clear();
     bufferCount.clear();

     // update individuals in novelty archive with the new clustering
     updateNoveltyArchive(state);*/
}
