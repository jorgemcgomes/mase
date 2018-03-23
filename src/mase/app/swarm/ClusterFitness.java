/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.swarm;

import ec.EvolutionState;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * https://github.com/BioMachinesLab/drones/blob/master/JBotAquatic/src/evaluation/ClusterFitness.java
 *
 * @author jorge
 */
public class ClusterFitness extends SwarmFitness {

    public static final String P_CLUSTER_DISTANCE = "cluster-dist";
    private static final long serialVersionUID = 1L;
    private double clusterDist;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.clusterDist = state.parameters.getDouble(base.push(P_CLUSTER_DISTANCE), null);
    }

    @Override
    protected double getFinalTaskFitness(SwarmPlayground sim) {
        List<List<SwarmAgent>> clusters = cluster(sim.agents, clusterDist);
        return 1d / clusters.size();
    }

    protected static List<List<SwarmAgent>> cluster(List<SwarmAgent> agents, double clusterDistance) {
        List<List<SwarmAgent>> clusters = new ArrayList<>(agents.size());
        
        // start with one cluster for each robot
        for (SwarmAgent r : agents) {
            List<SwarmAgent> cluster = new ArrayList<>();
            cluster.add(r);
            clusters.add(cluster);
        }

        boolean mergedSome = true;
        // stop when the clusters cannot be further merged
        while (mergedSome) {
            mergedSome = false;
            // find two clusters to merge
            for (int i = 0; i < clusters.size() && !mergedSome; i++) {
                for (int j = i + 1; j < clusters.size() && !mergedSome; j++) {
                    List<SwarmAgent> c1 = clusters.get(i);
                    List<SwarmAgent> c2 = clusters.get(j);
                    // check if the two clusters have (at least) one individual close to the other
                    for (int ri = 0; ri < c1.size() && !mergedSome; ri++) {
                        for (int rj = 0; rj < c2.size() && !mergedSome; rj++) {
                            SwarmAgent r1 = c1.get(ri);
                            SwarmAgent r2 = c2.get(rj);
                            if (r1.distanceTo(r2) <= clusterDistance) {
                                // do the merge
                                mergedSome = true;
                                clusters.get(i).addAll(clusters.get(j));
                                clusters.remove(j);
                            }
                        }
                    }
                }
            }
        }
        return clusters;
    }

}
