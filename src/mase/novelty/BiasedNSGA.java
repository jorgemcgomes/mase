/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty;

import java.util.List;

/**
 *
 * @author jorge
 */
public class BiasedNSGA extends NSGA2 {

    @Override
    protected void crowdingDistanceAssignement(List<Individual> I, double[] ranges) {
        if (I.size() == 1) {
            I.get(0).crowdingDistance = 0.5;
        } else {
            double minFit = Double.POSITIVE_INFINITY;
            double maxFit = Double.NEGATIVE_INFINITY;
            for (Individual i : I) {
                minFit = Math.min(minFit, i.objectives[0]);
                maxFit = Math.max(maxFit, i.objectives[0]);
            }
            for (Individual i : I) {
                i.crowdingDistance = maxFit == minFit ? 0.5 : (i.objectives[0] - minFit) / (maxFit - minFit);
            }
        }
    }
}
