/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.spec;

import ec.EvolutionState;
import ec.Individual;
import java.util.ArrayList;
import java.util.List;
import mase.evaluation.BehaviourResult;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author jorge
 */
public class BasicHybridExchangerDunn extends BasicHybridExchanger {
    private static final long serialVersionUID = 1L;


    /* Similar to bottom-up (agglomerative) hierarchical clustering */
    @Override
    protected void mergeProcess(EvolutionState state) {
        List<BehaviourResult>[] mpBehavs = new List[metaPops.size()];
        for (int i = 0; i < metaPops.size(); i++) {
            MetaPopulation mp = metaPops.get(i);
            Individual[] inds = getElitePortion(mp.inds, (int) Math.ceil(elitePortion * popSize));
            mpBehavs[i] = new ArrayList<BehaviourResult>(mp.agents.size() * inds.length);
            for (Individual ind : inds) {
                for (Integer a : mp.agents) {
                    mpBehavs[i].add(getAgentBR(ind, a));
                }
            }
        }
        distanceMatrix = distanceMatrix(mpBehavs, state);
        Pair<MetaPopulation, MetaPopulation> nextMerge = metaPops.size() >= 3 ? findNextMerge(distanceMatrix, state) : null;

        // Merge if they are similar
        if (nextMerge != null) {
            state.output.message("*************************** Merging " + nextMerge.getLeft() + " with " + nextMerge.getRight() + " ***************************");
            merges++;
            MetaPopulation mpNew = mergePopulations(nextMerge.getLeft(), nextMerge.getRight(), state);
            metaPops.remove(nextMerge.getLeft());
            metaPops.remove(nextMerge.getRight());
            metaPops.add(mpNew);
        }
    }

    @Override
    protected Pair<MetaPopulation, MetaPopulation> findNextMerge(double[][] distanceMatrix, EvolutionState state) {
        List<BehaviourResult>[] mpBehavs = new List[metaPops.size()];
        for (int i = 0; i < metaPops.size(); i++) {
            MetaPopulation mp = metaPops.get(i);
            Individual[] inds = getElitePortion(mp.inds, (int) Math.ceil(elitePortion * popSize));
            mpBehavs[i] = new ArrayList<BehaviourResult>(mp.agents.size() * inds.length);
            for (Individual ind : inds) {
                for (Integer a : mp.agents) {
                    mpBehavs[i].add(getAgentBR(ind, a));
                }
            }
        }

        double currentDunn = dunnIndex(distanceMatrix);
        System.out.println("Current dunn: " + currentDunn);
        Pair<MetaPopulation, MetaPopulation> next = null;

        for (int i = 0; i < metaPops.size(); i++) {
            for (int j = 0; j < metaPops.size(); j++) {
                MetaPopulation mpi = metaPops.get(i);
                MetaPopulation mpj = metaPops.get(j);
                if (i != j && mpi.age > stabilityTime && mpj.age > stabilityTime) {
                    List<BehaviourResult>[] hypothesis = new List[metaPops.size() - 1];
                    List<BehaviourResult> merged = new ArrayList<BehaviourResult>();
                    merged.addAll(mpBehavs[i]);
                    merged.addAll(mpBehavs[j]);
                    int index = 0;
                    hypothesis[index++] = merged;
                    for (int k = 0; k < mpBehavs.length; k++) {
                        if (k != i && k != j) {
                            hypothesis[index++] = mpBehavs[k];
                        }
                    }
                    double[][] dists = distanceMatrix(hypothesis, state);
                    double dunn = dunnIndex(dists);

                    System.out.println("Hypothesis merge " + mpi + " with " + mpj + ": " + dunn);

                    if (dunn > currentDunn) {
                        currentDunn = dunn;
                        next = Pair.of(mpi, mpj);
                    }
                }
            }
        }
        return next;
    }

    private double dunnIndex(double[][] distances) {
        double maxIntraCluster = Double.NEGATIVE_INFINITY;
        double minInterCluster = Double.POSITIVE_INFINITY;
        for (int i = 0; i < distances.length; i++) {
            maxIntraCluster = Math.max(distances[i][i], maxIntraCluster);
            for (int j = 0; j < distances.length; j++) {
                if (i != j) {
                    minInterCluster = Math.min(minInterCluster, distances[i][j]);
                }
            }
        }

        return minInterCluster / maxIntraCluster;
    }
}
