/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty;

import ec.EvolutionState;
import ec.Population;
import ec.Subpopulation;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import mase.PostEvaluator;

/**
 *
 * @author jorge
 */
public class NSGA2 implements PostEvaluator {

    protected List<Individual>[] allInds; // this is used just for stats

    @Override
    public void setup(EvolutionState state, Parameter base) {
        // Nothing to do
    }

    @Override
    public void processPopulation(EvolutionState state) {
        Population pop = state.population;
        allInds = new ArrayList[pop.subpops.length]; // for stats only
        for (int i = 0; i < pop.subpops.length; i++) {
            // find ranges
            float noveltyMin = Float.POSITIVE_INFINITY;
            float noveltyMax = Float.NEGATIVE_INFINITY;
            float fitnessMin = Float.POSITIVE_INFINITY;
            float fitnessMax = Float.NEGATIVE_INFINITY;
            for (int j = 0; j < pop.subpops[i].individuals.length; j++) {
                NoveltyFitness nf = (NoveltyFitness) pop.subpops[i].individuals[j].fitness;
                fitnessMin = (float) Math.min(fitnessMin, nf.getFitnessScore());
                fitnessMax = (float) Math.max(fitnessMax, nf.getFitnessScore());
                noveltyMin = (float) Math.min(noveltyMin, nf.noveltyScore);
                noveltyMax = (float) Math.max(noveltyMax, nf.noveltyScore);
            }
            double[] ranges = new double[]{fitnessMax - fitnessMin, noveltyMax - noveltyMin};
            if(ranges[0] == 0) {
                ranges[0] = 0.001;
            }
            if(ranges[1] == 0) {
                ranges[1] = 0.001;
            }

            List<Individual> inds = new ArrayList<Individual>(pop.subpops[i].individuals.length);

            // Create individuals
            for (int j = 0; j < pop.subpops[i].individuals.length; j++) {
                NoveltyFitness nf = (NoveltyFitness) pop.subpops[i].individuals[j].fitness;
                inds.add(new Individual(j, new double[]{nf.getFitnessScore(), nf.getNoveltyScore()}));
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
        for (List<Individual> rank : rankedInds) {
            for (Individual ind : rank) {
                ind.score = rankedInds.size() - ind.rank + ind.crowdingDistance;
                NoveltyFitness nf = (NoveltyFitness) pop.individuals[ind.individualId].fitness;
                nf.setFitness(state, (float) ind.score, false);
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
                    if (p.dominates(q)) {
                        p.S.add(q);
                    } else if (q.dominates(p)) {
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
            I.get(0).crowdingDistance = 1;
            I.get(l - 1).crowdingDistance = 1;
            for (int i = 1; i < l - 1; i++) {
                I.get(i).crowdingDistance = I.get(i).crowdingDistance
                        + (I.get(i + 1).objectives[m] - I.get(i - 1).objectives[m]) / ranges[m];
            }
        }
    }

    protected static class Individual {

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

        boolean dominates(Individual other) {
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
    }
}
