/*
 Copyright 2006 by Sean Luke and George Mason University
 Licensed under the Academic Free License version 3.0
 See the file "LICENSE" for more information
 */
package ec.coevolve;

import ec.*;
import ec.util.Parameter;
import ec.util.QuickSort;
import ec.util.SortComparatorL;
import java.util.ArrayList;
import mase.ExpandedFitness;

/**
 * Multi-threaded version
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class MultiPopCoevolutionaryEvaluator2 extends MultiPopCoevolutionaryEvaluator {

    public static final String P_OLD_ELITES = "old-elites";
    public static final String P_FITNESS_ONLY_ELITE = "fitness-only-elite";
    protected boolean eliteFitness;
    protected boolean oldElites;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        eliteFitness = state.parameters.getBoolean(base.push(P_FITNESS_ONLY_ELITE), null, false);
        oldElites = state.parameters.getBoolean(base.push(P_OLD_ELITES), null, false);
    }

    @Override
    public void evaluatePopulation(final EvolutionState state) {
        // determine who needs to be evaluated
        boolean[] preAssessFitness = new boolean[state.population.subpops.length];
        boolean[] postAssessFitness = new boolean[state.population.subpops.length];
        for (int i = 0; i < state.population.subpops.length; i++) {
            postAssessFitness[i] = shouldEvaluateSubpop(state, i, 0);
            preAssessFitness[i] = postAssessFitness[i] || (state.generation == 0);  // always prepare (set up trials) on generation 0
        }

        // do evaluation
        beforeCoevolutionaryEvaluation(state, state.population, (GroupedProblemForm) p_problem);

        ((GroupedProblemForm) p_problem).preprocessPopulation(state, state.population, preAssessFitness, false);
        performCoevolutionaryEvaluation(state, state.population, (GroupedProblemForm) p_problem);
        ((GroupedProblemForm) p_problem).postprocessPopulation(state, state.population, postAssessFitness, false);
        
        // other methods become responsible of calling afterCoevolutionaryEvaluation
    }

    @Override
    public void performCoevolutionaryEvaluation(final EvolutionState state,
            final Population population,
            final GroupedProblemForm prob) {

        /* initialization -- not modified */
        inds = new Individual[population.subpops.length];
        updates = new boolean[population.subpops.length];

        // build subpopulation array to pass in each time
        int[] subpops = new int[state.population.subpops.length];
        for (int j = 0; j < subpops.length; j++) {
            subpops[j] = j;
        }

        // we start by warming up the selection methods
        if (numCurrent > 0) {
            for (int i = 0; i < selectionMethodCurrent.length; i++) {
                selectionMethodCurrent[i].prepareToProduce(state, i, 0);
            }
        }
        if (numPrev > 0) {
            for (int i = 0; i < selectionMethodPrev.length; i++) {
                // do a hack here
                Population currentPopulation = state.population;
                state.population = previousPopulation;
                selectionMethodPrev[i].prepareToProduce(state, i, 0);
                state.population = currentPopulation;
            }
        }

        /* shuffling -- single threaded -- not modified */
        if (numShuffled > 0) {
            int[/*numShuffled*/][/*subpop*/][/*shuffledIndividualIndexes*/] ordering = null;
            // build shuffled orderings
            ordering = new int[numShuffled][state.population.subpops.length][state.population.subpops[0].individuals.length];
            for (int c = 0; c < numShuffled; c++) {
                for (int m = 0; m < state.population.subpops.length; m++) {
                    for (int i = 0; i < state.population.subpops[0].individuals.length; i++) {
                        ordering[c][m][i] = i;
                    }
                    if (m != 0) {
                        shuffle(state, ordering[c][m]);
                    }
                }
            }

            // for each individual
            for (int i = 0; i < state.population.subpops[0].individuals.length; i++) {
                for (int k = 0; k < numShuffled; k++) {
                    for (int ind = 0; ind < inds.length; ind++) {
                        inds[ind] = state.population.subpops[ind].individuals[ordering[k][ind][i]];
                        updates[ind] = true;
                    }
                    prob.evaluate(state, inds, updates, false, subpops, 0);
                }
            }
        }

        /* subpops evaluation -- multi-threaded */
        // figure out multi-thread distribution
        int numinds[][] = new int[state.evalthreads][state.population.subpops.length];
        int from[][] = new int[state.evalthreads][state.population.subpops.length];

        for (int y = 0; y < state.evalthreads; y++) {
            for (int x = 0; x < state.population.subpops.length; x++) {
                if (shouldEvaluateSubpop(state, x, 0)) {
                    // figure numinds
                    if (y < state.evalthreads - 1) { // not last one
                        numinds[y][x] = state.population.subpops[x].individuals.length / state.evalthreads;
                    } else { // in case we're slightly off in division
                        numinds[y][x]
                                = state.population.subpops[x].individuals.length / state.evalthreads
                                + (state.population.subpops[x].individuals.length
                                - (state.population.subpops[x].individuals.length
                                / state.evalthreads) // note integer division
                                * state.evalthreads);
                    }
                    // figure from
                    from[y][x] = (state.population.subpops[x].individuals.length / state.evalthreads) * y;
                }
            }
        }

        // distribute the work
        if (state.evalthreads == 1) {
            evalPopChunk(state, numinds[0], from[0], 0, subpops, (GroupedProblemForm) ((Problem) prob).clone());
        } else {
            Thread[] t = new Thread[state.evalthreads];
            // start up the threads
            for (int y = 0; y < state.evalthreads; y++) {
                EvaluatorThread r = new EvaluatorThread();
                r.threadnum = y;
                r.numinds = numinds[y];
                r.from = from[y];
                r.state = state;
                r.subpops = subpops;
                r.p = (GroupedProblemForm) ((Problem) prob).clone();
                t[y] = new Thread(r);
                t[y].start();
            }

            // gather the threads
            for (int y = 0; y < state.evalthreads; y++) {
                try {
                    t[y].join();
                } catch (InterruptedException e) {
                    state.output.fatal("Whoa! The main evaluation thread got interrupted!  Dying...");
                }
            }
        }

        /* finalization -- not modified */
        if (numCurrent > 0) {
            for (int i = 0; i < selectionMethodCurrent.length; i++) {
                selectionMethodCurrent[i].finishProducing(state, i, 0);
            }
        }
        if (numPrev > 0) {
            for (int i = 0; i < selectionMethodPrev.length; i++) {
                // do a hack here
                Population currentPopulation = state.population;
                state.population = previousPopulation;
                selectionMethodPrev[i].finishProducing(state, i, 0);
                state.population = currentPopulation;
            }
        }
    }

    @Override
    public void afterCoevolutionaryEvaluation(EvolutionState state, Population population, GroupedProblemForm prob) {
        super.afterCoevolutionaryEvaluation(state, population, prob);
    }

    class EvaluatorThread implements Runnable {

        public int[] numinds;
        public int[] from;
        public EvolutionState state;
        public int threadnum;
        public GroupedProblemForm p;
        public int[] subpops;

        @Override
        public void run() {
            evalPopChunk(state, numinds, from, threadnum, subpops, p);
        }
    }

    protected void evalPopChunk(EvolutionState state, int[] numinds, int[] from, int threadnum, int[] subpops, GroupedProblemForm prob) {
        ((Problem) prob).prepareToEvaluate(state, threadnum);
        boolean[] tUpdates = new boolean[state.population.subpops.length];
        Individual[] tInds = new Individual[state.population.subpops.length];

        for (int pop = 0; pop < state.population.subpops.length; pop++) {
            // start evaluatin'!
            int upperbound = from[pop] + numinds[pop];
            for (int x = from[pop]; x < upperbound; x++) {
                Individual individual = state.population.subpops[pop].individuals[x];
                // Test against all the elites
                for (int k = 0; k < eliteIndividuals[pop].length; k++) {
                    for (int ind = 0; ind < tInds.length; ind++) {
                        if (ind == pop) {
                            tInds[ind] = individual;
                            tUpdates[ind] = true;
                        } else {
                            tInds[ind] = eliteIndividuals[ind][k];
                            tUpdates[ind] = false;
                        }
                    }
                    prob.evaluate(state, tInds, tUpdates, false, subpops, 0);
                }

                // test against random selected individuals of the current population
                for (int k = 0; k < numCurrent; k++) {
                    for (int ind = 0; ind < tInds.length; ind++) {
                        if (ind == pop) {
                            tInds[ind] = individual;
                            tUpdates[ind] = true;
                        } else {
                            tInds[ind] = produceCurrent(ind, state, 0);
                            tUpdates[ind] = false;
                        }
                    }
                    prob.evaluate(state, tInds, tUpdates, false, subpops, 0);
                }

                // Test against random individuals of previous population
                for (int k = 0; k < numPrev; k++) {
                    for (int ind = 0; ind < tInds.length; ind++) {
                        if (ind == pop) {
                            tInds[ind] = individual;
                            tUpdates[ind] = true;
                        } else {
                            tInds[ind] = producePrevious(ind, state, 0);
                            tUpdates[ind] = false;
                        }
                    }
                    prob.evaluate(state, tInds, tUpdates, false, subpops, 0);
                }
            }
        }
        ((Problem) prob).finishEvaluating(state, threadnum);
    }

    @Override
    void loadElites(final EvolutionState state, int whichSubpop) {
        Subpopulation subpop = state.population.subpops[whichSubpop];

        if (numElite == 1) {
            int best = 0;
            Individual[] oldinds = subpop.individuals;
            for (int x = 1; x < oldinds.length; x++) {
                if (betterThan(oldinds[x], oldinds[best])) {
                    best = x;
                }
            }
            eliteIndividuals[whichSubpop][0] = (Individual) (state.population.subpops[whichSubpop].individuals[best].clone());
        } else if (!oldElites && numElite > 0) { // elites from current population
            int[] orderedPop = new int[subpop.individuals.length];
            for (int x = 0; x < subpop.individuals.length; x++) {
                orderedPop[x] = x;
            }
            QuickSort.qsort(orderedPop, new EliteComparator2(subpop.individuals));
            // load the top N individuals
            for (int j = 0; j < numElite; j++) {
                eliteIndividuals[whichSubpop][j] = (Individual) (state.population.subpops[whichSubpop].individuals[orderedPop[j]].clone());
            }
        } else if (oldElites && numElite > 0) { // elites from previous generations
            int best = 0;
            for (int x = 1; x < subpop.individuals.length; x++) {
                if (betterThan(subpop.individuals[x], subpop.individuals[best])) {
                    best = x;
                }
            }
            eliteIndividuals[whichSubpop][(state.generation) % numElite]
                    = (Individual) subpop.individuals[best].clone();
            if (state.generation < numElite - 1) { // need to fill the rest of the elites
                int[] orderedPop = new int[subpop.individuals.length];
                for (int x = 0; x < subpop.individuals.length; x++) {
                    orderedPop[x] = x;
                }
                QuickSort.qsort(orderedPop, new EliteComparator2(subpop.individuals));

                // fill the rest of the elites vector
                for (int j = 1; j < numElite - state.generation; j++) {
                    eliteIndividuals[whichSubpop][j + state.generation]
                            = (Individual) (state.population.subpops[whichSubpop].individuals[orderedPop[j]].clone());
                }
            }

            /*System.out.println(whichSubpop + " -------");
             for (int i = 0; i < numElite; i++) {
             System.out.print(eliteIndividuals[whichSubpop][i].fitness.fitness() + " ");
             }
             System.out.println();*/
        }
    }

    // sorts the opposite way on purpose -- we want the best to be first in the array
    class EliteComparator2 implements SortComparatorL {

        Individual[] inds;

        public EliteComparator2(Individual[] inds) {
            super();
            this.inds = inds;
        }

        @Override
        public boolean lt(long a, long b) {
            return betterThan(inds[(int) a], inds[(int) b]);
        }

        @Override
        public boolean gt(long a, long b) {
            return betterThan(inds[(int) b], inds[(int) a]);
        }
    }

    private boolean betterThan(Individual a, Individual b) {
        if (eliteFitness) {
            return ((ExpandedFitness) a.fitness).getFitnessScore() > ((ExpandedFitness) b.fitness).getFitnessScore();
        } else {
            return a.fitness.betterThan(b.fitness);
        }
    }
}
