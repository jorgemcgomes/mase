/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.spec;

import ec.EvolutionState;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author jorge
 */
public class DynamicSpecialisationExchanger extends SpecialisationExchanger {

    public static final String P_FINAL_THRESHOLD = "final-threshold";
    public static final String P_THRESHOLD_DEVIATION = "threshold-deviation";
    public static final String P_THRESHOLD_CHANGE = "threshold-change";

    double finalThreshold;
    double thresholdDeviation;
    double thresholdChange;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        finalThreshold = state.parameters.getDouble(base.push(P_FINAL_THRESHOLD), null);
        thresholdDeviation = state.parameters.getDouble(base.push(P_THRESHOLD_DEVIATION), null);
        thresholdChange = state.parameters.getDouble(base.push(P_THRESHOLD_CHANGE), null);
    }

    @Override
    protected void splitProcess(EvolutionState state) {
        splits = 0;
        List<MetaPopulation> created = new ArrayList<MetaPopulation>();
        for (MetaPopulation mp : metaPops) {
            if (mp.populations.size() > 1) {
                // Find the biggest distance between populations of the same MetaPopulation
                int maxI = -1, maxJ = -1;
                for (Integer i : mp.populations) {
                    for (Integer j : mp.populations) {
                        if (j > i && (maxI == -1 || distanceMatrix[i][j] > distanceMatrix[maxI][maxJ])) {
                            maxI = i;
                            maxJ = j;
                        }
                    }
                }
                
                double threshold = Math.max(finalThreshold, finalThreshold + thresholdDeviation - mp.age * thresholdChange); 

                // Check if it needs to be split
                if (distanceMatrix[maxI][maxJ] > threshold) {
                    // Determine which one will leave the metapopulation
                    int exitPop = maxJ;
                    if (mp.populations.size() > 2) {
                        double maxIdist = 0, maxJdist = 0;
                        for (Integer k : mp.populations) {
                            maxIdist += distanceMatrix[k][maxI];
                            maxJdist += distanceMatrix[k][maxJ];
                        }
                        exitPop = maxIdist > maxJdist ? maxI : maxJ;
                    }

                    // Do the split -- remove subpop from current metapopulation
                    System.out.println("Spliting " + exitPop + " from " + mp.toString() + " D: " + distanceMatrix[maxI][maxJ] + " T: " + threshold);
                    mp.populations.remove((Object) exitPop);
                    mp.age = 0;

                    // Create new metapopulation with subpop and the same individuals as the former metapop
                    MetaPopulation mpj = new MetaPopulation();
                    mpj.individuals = state.population.subpops[exitPop].individuals;
                    mpj.populations.add(exitPop);
                    created.add(mpj);
                    splits++;
                }

            }
        }
        metaPops.addAll(created);
    }

    @Override
    protected void mergeProcess(EvolutionState state) {
        Iterator<MetaPopulation> iter = metaPops.iterator();
        while (iter.hasNext()) {
            MetaPopulation next = iter.next();
            // The population is alone: candidate to merging
            if (next.populations.size() == 1) {
                // Find the closest metapopulation to merge with
                int subpop = next.populations.get(0); // get the single subpopulation
                MetaPopulation closest = null;
                double distance = Double.POSITIVE_INFINITY;
                for (MetaPopulation mp : metaPops) {
                    if (mp != next) { // can not merge with itself
                        double d = maxDistance(subpop, mp);
                        if (d < distance) {
                            distance = d;
                            closest = mp;
                        }
                    }
                }
                

                // One metapop was found, merge with it
                if (closest != null) {
                    double threshold = Math.min(finalThreshold, finalThreshold - thresholdDeviation + Math.min(closest.age, next.age) * thresholdChange); 
                    if(distance < threshold) {
                        iter.remove(); // delete current metapop
                        // Integrate in the closest
                        System.out.println("Merging " + next.toString() + " with " + closest.toString() + " D: " + distance + " T: " + threshold);
                        closest.populations.add(subpop);
                        closest.waitingIndividuals.add(next.individuals);
                        closest.age = 0;
                    }
                }
            }
        }
    }

}
