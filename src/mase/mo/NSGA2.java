/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mo;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.Subpopulation;
import ec.util.Parameter;
import java.io.Serializable;
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
    private static final long serialVersionUID = 1L;

    protected List<NSGAIndividual>[] indsRank;
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
    
    public String[] getIncludeScores() {
        return include;
    }
    
    public List<NSGAIndividual>[] getIndividualsRanking() {
        return indsRank;
    }

    @Override
    public void processPopulation(EvolutionState state) {
        Population pop = state.population;
        indsRank = new ArrayList[pop.subpops.size()];
        
        for (int i = 0; i < pop.subpops.size(); i++) {
            List<NSGAIndividual> inds = new ArrayList<>(pop.subpops.get(i).individuals.size());

            float[] maxVals = new float[include.length];
            Arrays.fill(maxVals, Float.NEGATIVE_INFINITY);
            float[] minVals = new float[include.length];
            Arrays.fill(minVals, Float.POSITIVE_INFINITY);
            for (int k = 0 ; k < pop.subpops.get(i).individuals.size() ; k++) {
                ExpandedFitness ef = (ExpandedFitness) pop.subpops.get(i).individuals.get(k).fitness;
                double[] vals = new double[include.length];
                for(int j = 0 ; j < include.length ; j++) {
                    vals[j] = ef.scores().get(include[j]);
                    maxVals[j] = Math.max((float)vals[j], maxVals[j]);
                    minVals[j] = Math.min((float)vals[j], minVals[j]);
                }
                inds.add(new NSGAIndividual(pop.subpops.get(i).individuals.get(k), vals));
            }
            double[] ranges = new double[include.length];
            for(int j = 0 ; j < ranges.length ; j++) {
                ranges[j] = maxVals[j] - minVals[j];
                if(ranges[j] == 0) {
                    ranges[j] = 0.001f;
                }
            }

            // Sort by rank
            List<List<NSGAIndividual>> ranked = fastNondominatedSort(inds);
            // Calculate crowding distance
            for (List<NSGAIndividual> rank : ranked) {
                //System.out.println(rank.size());
                crowdingDistanceAssignement(rank, ranges);
            }

            assignFitnessScores(pop.subpops.get(i), state, ranked);

            indsRank[i] = new ArrayList<>();
            for (List<NSGAIndividual> rank : ranked) {
                for (NSGAIndividual ind : rank) {
                    indsRank[i].add(ind);
                }
            }
        }
    }

    protected void assignFitnessScores(Subpopulation pop, EvolutionState state, List<List<NSGAIndividual>> rankedInds) {
        // Calculate score
        if (ordinalRanking) {
            List<NSGAIndividual> all = new ArrayList<>();
            for (List<NSGAIndividual> rank : rankedInds) {
                all.addAll(rank);
            }
            Collections.sort(all);
            int index = 0;
            for (int i = 0; i < all.size(); i++) {
                NSGAIndividual ind = all.get(i);
                if (i > 0 && ind.compareTo(all.get(i - 1)) == 0) {
                    // Assign the same score to individuals that are tied
                    ind.score = index;
                } else {
                    index = i;
                    ind.score = index;
                }
                ExpandedFitness nf = (ExpandedFitness) ind.popIndividual.fitness;
                nf.setFitness(state, (float) ind.score, false);
            }
        } else {
            for (List<NSGAIndividual> rank : rankedInds) {
                for (NSGAIndividual ind : rank) {
                    double rankScore = rankedInds.size() - ind.rank;
                    double distScore = Double.isInfinite(ind.crowdingDistance) ? 1 : ind.crowdingDistance / include.length;
                    ind.score = rankScore + distScore;
                    ExpandedFitness nf = (ExpandedFitness) ind.popIndividual.fitness;
                    nf.setFitness(state, (float) ind.score, false);
                }
            }
        }
    }

    /*
     * Returns the fronts and assign the nondomination rank to each individual
     */
    protected List<List<NSGAIndividual>> fastNondominatedSort(List<NSGAIndividual> inds) {
        List<List<NSGAIndividual>> F = new ArrayList<>();
        List<NSGAIndividual> F1 = new ArrayList<>();
        for (NSGAIndividual p : inds) {
            for (NSGAIndividual q : inds) {
                if (p != q) {
                    if (p.paretoDominates(q)) {
                        p.S.add(q);
                    } else if (q.paretoDominates(p)) {
                        p.n = p.n + 1;
                    }
                }
            }
            if (p.n == 0) {
                F1.add(p);
            }
        }
        F.add(F1);
        int i = 0;
        while (!F.get(i).isEmpty()) {
            List<NSGAIndividual> H = new ArrayList<>();
            for (NSGAIndividual p : F.get(i)) {
                for (NSGAIndividual q : p.S) {
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
            for (NSGAIndividual ind : F.get(i)) {
                ind.rank = i + 1;
            }
        }
        return F;
    }

    protected void crowdingDistanceAssignement(List<NSGAIndividual> I, double[] ranges) {
        int mTotal = I.get(0).objectives.length;
        int l = I.size();
        for (NSGAIndividual i : I) {
            i.crowdingDistance = 0;
        }
        for (int m = 0; m < mTotal; m++) {
            final int mm = m;
            // Sort according to objective m
            Collections.sort(I, new Comparator<NSGAIndividual>() {
                @Override
                public int compare(NSGAIndividual ind1, NSGAIndividual ind2) {
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

    public static class NSGAIndividual implements Comparable<NSGAIndividual>, Serializable {

        private static final long serialVersionUID = 1L;

        double[] objectives;
        int rank;
        double crowdingDistance;
        double score;
        int n;
        List<NSGAIndividual> S;
        Individual popIndividual;

        NSGAIndividual(Individual ind, double[] objectives) {
            this.popIndividual = ind;
            this.objectives = objectives;
            this.S = new ArrayList<>();
            this.n = 0;
        }

        public boolean paretoDominates(NSGAIndividual other) {
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
        public int compareTo(NSGAIndividual o) {
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
        
        public int getRank() {
            return rank;
        }
        
        public double getCrowdingDistance() {
            return crowdingDistance;
        }      
        
        public Individual getIndividual() {
            return popIndividual;
        }
    }
}
