/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mo;

import ec.EvolutionState;
import ec.Population;
import ec.Subpopulation;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import mase.evaluation.PostEvaluator;
import mase.evaluation.ExpandedFitness;

/**
 *
 * @author jorge
 */
public class NSGA2 implements PostEvaluator {

    protected List<Individual>[] allInds; // this is used just for stats
    public static final String P_ORDINAL_RANKING = "ordinal-ranking";
    public static final String P_INCLUDE_SCORES = "scores";
    protected boolean ordinalRanking;
    protected String[] include;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        ordinalRanking = state.parameters.getBoolean(base.push(P_ORDINAL_RANKING), null, false);
        String str = state.parameters.getString(base.push(P_INCLUDE_SCORES), null);
        include = str.split("[,;\\s\\-]+");
    }

    @Override
    public void processPopulation(EvolutionState state) {
        Population pop = state.population;
        allInds = new ArrayList[pop.subpops.length]; // for stats only
        
        for (int i = 0; i < pop.subpops.length; i++) {
            List<Individual> inds = new ArrayList<Individual>(pop.subpops[i].individuals.length);

            float[] maxVals = new float[include.length];
            Arrays.fill(maxVals, Float.NEGATIVE_INFINITY);
            float[] minVals = new float[include.length];
            Arrays.fill(minVals, Float.POSITIVE_INFINITY);
            for (int k = 0 ; k < pop.subpops[i].individuals.length ; k++) {
                ExpandedFitness ef = (ExpandedFitness) pop.subpops[i].individuals[k].fitness;
                double[] vals = new double[include.length];
                for(int j = 0 ; j < include.length ; j++) {
                    vals[j] = ef.scores().get(include[j]);
                    maxVals[j] = Math.max((float)vals[j], maxVals[j]);
                    minVals[j] = Math.min((float)vals[j], minVals[j]);
                }
                inds.add(new Individual(k, vals));
            }
            double[] ranges = new double[include.length];
            for(int j = 0 ; j < ranges.length ; j++) {
                ranges[j] = maxVals[j] - minVals[j];
                if(ranges[j] == 0) {
                    ranges[j] = 0.001f;
                }
            }

            // Sort by rank
            List<List<Individual>> ranked = fastNondominatedSort(inds);
            // Calculate crowding distance
            for (List<Individual> rank : ranked) {
                //System.out.println(rank.size());
                crowdingDistanceAssignement(rank, ranges);
            }

            assignFitnessScores(pop.subpops[i], state, ranked);

            // for stats only
            allInds[i] = new ArrayList<Individual>();
            for (List<Individual> rank : ranked) {
                for (Individual ind : rank) {
                    allInds[i].add(ind);
                }
            }
        }
    }

    protected void assignFitnessScores(Subpopulation pop, EvolutionState state, List<List<Individual>> rankedInds) {
        // Calculate score
        if (ordinalRanking) {
            List<Individual> all = new ArrayList<Individual>();
            for (List<Individual> rank : rankedInds) {
                all.addAll(rank);
            }
            Collections.sort(all);
            int index = 0;
            for (int i = 0; i < all.size(); i++) {
                Individual ind = all.get(i);
                if (i > 0 && ind.compareTo(all.get(i - 1)) == 0) {
                    // Assign the same score to individuals that are tied
                    ind.score = index;
                } else {
                    index = i;
                    ind.score = index;
                }
                ExpandedFitness nf = (ExpandedFitness) pop.individuals[ind.individualId].fitness;
                nf.setFitness(state, (float) ind.score, false);
            }
        } else {
            for (List<Individual> rank : rankedInds) {
                for (Individual ind : rank) {
                    double rankScore = rankedInds.size() - ind.rank;
                    double distScore = Double.isInfinite(ind.crowdingDistance) ? 1 : ind.crowdingDistance / 2;
                    ind.score = rankScore + distScore;
                    ExpandedFitness nf = (ExpandedFitness) pop.individuals[ind.individualId].fitness;
                    nf.setFitness(state, (float) ind.score, false);
                }
            }
        }
    }

    /*
     * Returns the fronts and assign the nondomination rank to each individual
     */
    protected List<List<Individual>> fastNondominatedSort(List<Individual> inds) {
        List<List<Individual>> F = new ArrayList<List<Individual>>();
        List<Individual> F1 = new ArrayList<Individual>();
        for (Individual p : inds) {
            for (Individual q : inds) {
                if (p != q) {
                    if (p.paretoDominates(q)) {
                        p.S.add(q);
                    } else if (q.paretoDominates(p)) {
                        p.n = p.n + 1;
                    }
                }
            }
            //System.out.println("dom by: " + p.dominatedBy.size() + " | doms: " + p.S.size());
            if (p.n == 0) {
                F1.add(p);
            }
        }
        F.add(F1);
        int i = 0;
        while (!F.get(i).isEmpty()) {
            List<Individual> H = new ArrayList<Individual>();
            for (Individual p : F.get(i)) {
                for (Individual q : p.S) {
                    q.n = q.n - 1;
                    if (q.n == 0) {
                        H.add(q);
                    }
                }
            }
            i = i + 1;
            F.add(H);
        }

        // Last one may be empty
        if (F.get(F.size() - 1).isEmpty()) {
            F.remove(F.size() - 1);
        }

        // assign ranks
        for (i = 0; i < F.size(); i++) {
            for (Individual ind : F.get(i)) {
                ind.rank = i + 1;
            }
        }
        return F;
    }

    protected void crowdingDistanceAssignement(List<Individual> I, double[] ranges) {
        int mTotal = I.get(0).objectives.length;
        int l = I.size();
        for (Individual i : I) {
            i.crowdingDistance = 0;
        }
        for (int m = 0; m < mTotal; m++) {
            final int mm = m;
            // Sort according to objective m
            Collections.sort(I, new Comparator<Individual>() {
                @Override
                public int compare(Individual ind1, Individual ind2) {
                    return Double.compare(ind1.objectives[mm], ind2.objectives[mm]);
                }
            });
            I.get(0).crowdingDistance = Double.POSITIVE_INFINITY;
            I.get(l - 1).crowdingDistance = Double.POSITIVE_INFINITY;
            for (int i = 1; i < l - 1; i++) {
                I.get(i).crowdingDistance = I.get(i).crowdingDistance
                        + (I.get(i + 1).objectives[m] - I.get(i - 1).objectives[m]) / ranges[m];
            }
        }
    }

    protected static class Individual implements Comparable<Individual> {

        double[] objectives;
        int rank;
        double crowdingDistance;
        double score;
        int n;
        List<Individual> S;
        int individualId;

        Individual(int individualId, double[] objectives) {
            this.individualId = individualId;
            this.objectives = objectives;
            this.S = new ArrayList<Individual>();
            this.n = 0;
        }

        boolean paretoDominates(Individual other) {
            boolean oneBetter = false;
            for (int i = 0; i < this.objectives.length; i++) {
                if (this.objectives[i] < other.objectives[i]) {
                    return false;
                } else if (this.objectives[i] > other.objectives[i]) {
                    oneBetter = true;
                }
            }
            return oneBetter;
        }

        @Override
        public int compareTo(Individual o) {
            if (this.rank < o.rank) {
                return 1;
            } else if (this.rank > o.rank) {
                return -1;
            } else {
                if (this.crowdingDistance > o.crowdingDistance) {
                    return 1;
                } else if (this.crowdingDistance < o.crowdingDistance) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }
}
