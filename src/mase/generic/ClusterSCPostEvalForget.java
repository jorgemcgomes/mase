/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic;

import ec.EvolutionState;
import ec.util.Parameter;
import java.util.Map;

/**
 *
 * @author jorge
 */
public class ClusterSCPostEvalForget extends ClusterSCPostEvaluator {

    public static final String P_UPDATE_INTERVAL = "update-interval";
    protected int updateInterval;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.updateInterval = state.parameters.getInt(base.push(P_UPDATE_INTERVAL),
                new Parameter(SCPostEvaluator.P_STATECOUNT_BASE).push(P_UPDATE_INTERVAL));
    }

    @Override
    protected void updateClusters(EvolutionState state) {
        if (state.generation % updateInterval == 0) {
            // initialisation step
            initializeClusters(state);
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
                    for(int j = 0 ; j < e.getValue().length ; j++) {
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
                    for(int j = 0 ; j < totals[0].length ; j++) {
                        clusters[c][j] = totals[c][j] / totalCounts[c];
                    }
                }
            }

            // clear the structures
            buffer.clear();
            bufferCount.clear();

            // update individuals in novelty archive with the new clustering
            updateNoveltyArchive(state);
        }
    }
}
