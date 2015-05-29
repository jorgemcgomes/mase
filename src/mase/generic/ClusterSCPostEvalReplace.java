/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.MetaEvaluator;
import mase.PostEvaluator;
import mase.novelty.weighted.WeightedNovelty;

/**
 *
 * @author jorge
 */
public class ClusterSCPostEvalReplace extends ClusterSCPostEvaluator {

    private WeightedNovelty wnov;
    protected int replaceRate;
    public static final String P_REPLACE_RATE = "replace-rate";

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.replaceRate = state.parameters.getInt(base.push(P_REPLACE_RATE),
                new Parameter(SCPostEvaluator.P_STATECOUNT_BASE).push(P_REPLACE_RATE));
    }

    @Override
    protected void updateClusters(EvolutionState state) {
        if (wnov == null) {
            for (PostEvaluator pe : ((MetaEvaluator) state.evaluator).getPostEvaluators()) {
                if (pe instanceof WeightedNovelty) {
                    wnov = (WeightedNovelty) pe;
                }
            }
            if (wnov == null) {
                state.output.fatal("ClusterSCPostEvalReplace requires WeightedNovelty to be used");
            }
        }

        boolean replaced = false;
        if (state.generation > 0 && state.generation % replaceRate == 0) {
            replaced = true;
            // find the center with the lowest weight
            float[] weights = wnov.getWeights();
            int minW = 0;
            for (int i = 1; i < weights.length; i++) {
                if (weights[i] < weights[minW]) {
                    minW = i;
                }
            }

            // randomly choose the parent
            // compute the total weight of all items together
            float totalWeight = 0;
            for (int i = 0; i < weights.length; i++) {
                if (i != minW) {
                    totalWeight += weights[i];
                }
            }
            // pick the index
            int father = -1;
            float random = state.random[0].nextFloat() * totalWeight;
            for (int i = 0; i < weights.length; i++) {
                if (i != minW) {
                    random -= weights[i];
                    if (random <= 0) {
                        father = i;
                        break;
                    }
                }
            }

            // find the nearest parent's nearest neighbour
            int mother = -1;
            float minDist = Float.POSITIVE_INFINITY;
            for (int i = 0; i < weights.length; i++) {
                if (i != minW && i != father) {
                    float d = dist(clusters[father], clusters[i]);
                    if (d < minDist) {
                        mother = i;
                        minDist = d;
                    }
                }
            }

            // create the new center
            for (int i = 0; i < clusters[minW].length; i++) {
                clusters[minW][i] = (clusters[father][i] + clusters[mother][i]) / 2;
            }
            counts[minW] = 1;
            // get initial correlation from the parents
            wnov.getAdjustedCorrelation()[minW] = (weights[father] + weights[mother]) / 2; 
            state.output.message("Removed cluster " + minW + " and added new from " + father + " and " + mother);
        }
    }

    private float dist(double[] a, double[] b) {
        float d = 0;
        for (int i = 0; i < a.length; i++) {
            d += Math.pow(a[i] - b[i], 2);
        }
        return d;
    }

}
