/*
 Copyright 2006 by Sean Luke and George Mason University
 Licensed under the Academic Free License version 3.0
 See the file "LICENSE" for more information
 */
package mase.evaluation;

import ec.*;
import ec.coevolve.GroupedProblemForm;
import ec.coevolve.MultiPopCoevolutionaryEvaluator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Multi-threaded version.
 * Maintains the original functionality, with just one difference: the randomly
 * chosen collaborators from the current or previous population are the same
 * for evaluating all individuals of a given population
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class MultiPopCoevolutionaryEvaluatorThreaded extends MultiPopCoevolutionaryEvaluator {

    private static final long serialVersionUID = 1L;

    public Individual[][] getEliteIndividuals() {
        return eliteIndividuals;
    }

    public void setEliteIndividuals(Individual[][] elite) {
        this.eliteIndividuals = elite;
    }    
    
    @Override
    protected void afterCoevolutionaryEvaluation(EvolutionState state, Population population, GroupedProblemForm prob) {
        if(!(state.evaluator instanceof MetaEvaluator)) {
            // Do not do this until explicitly asked to (forceAfterCoevolutionaryEvaluation)
            // Delayed to allow for PostEvaluators in MetaEvaluator to influence the elites selection
            // If the evaluator is not a MetaEvaluator, this works as usual
            super.afterCoevolutionaryEvaluation(state, population, prob);
        }
    }
    
    public void forceAfterCoevolutionaryEvaluation(EvolutionState state, Population population, GroupedProblemForm prob) {
        super.afterCoevolutionaryEvaluation(state, population, prob);
    }
    
    @Override
    public void performCoevolutionaryEvaluation(final EvolutionState state,
            final Population population,
            final GroupedProblemForm prob) {

        // initialization
        int evaluations = 0;
        inds = new Individual[population.subpops.length];
        updates = new boolean[population.subpops.length];

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

        // build subpopulation array to pass in each time
        int[] subpops = new int[population.subpops.length];
        for (int j = 0; j < subpops.length; j++) {
            subpops[j] = j;
        }

        LinkedList<ProblemEvaluation> evals = new LinkedList<>();

        // handle shuffled
        if (numShuffled > 0) {
            int[/*numShuffled*/][/*subpop*/][/*shuffledIndividualIndexes*/] ordering;
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
                    evals.add(new ProblemEvaluation(inds, updates, false, subpops));
                    evaluations++;
                }
            }

            // take care of the shuffled right now because it can cause concurrency issues
            // with the next phase of the evaluation
            // each individual is evaluated only once, so this distribution is fine
            distributeAndEvaluate(state, prob, evals);
            evals.clear();
        }

        /*
         * Functionality was changed here. In the super class, each individual is
         * evaluated with a different random choice of collaborators. Here, the same
         * collaborators are used to evaluate every individual of a given population.
         */
        Individual[][] collaborators = new Individual[state.population.subpops.length][];
        for (int p = 0; p < population.subpops.length; p++) {
            // includes elites, randoms, and previous
            collaborators[p] = loadCollaborators(state, p);
        }

        // for each subpopulation
        for (int j = 0; j < state.population.subpops.length; j++) {
            // now do elites and randoms
            if (!shouldEvaluateSubpop(state, j, 0)) {
                continue;  // don't evaluate this subpopulation
            }
            // for each individual
            for (int i = 0; i < state.population.subpops[j].individuals.length; i++) {
                Individual individual = state.population.subpops[j].individuals[i];

                // Test against all the collaborators
                for (int k = 0; k < collaborators[j].length; k++) {
                    for (int ind = 0; ind < inds.length; ind++) {
                        if (ind == j) {
                            inds[ind] = individual;
                            updates[ind] = true;
                        } else {
                            inds[ind] = collaborators[ind][k];
                            updates[ind] = false;
                        }
                    }
                    evals.add(new ProblemEvaluation(inds, updates, false, subpops));
                    evaluations++;
                }
            }
        }
        // TODO: Although unlikely, concurrency problems are possible if the same individual finishes
        // evaluation in multiple threads simultaneously.
        // The distribution strategy tries to mitigate this, but still...
        distributeAndEvaluate(state, prob, evals);

        // now shut down the selection methods
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

        state.output.message("Evaluations: " + evaluations);
    }

    protected Individual[] loadCollaborators(EvolutionState state, int whichSubpop) {
        ArrayList<Individual> tInds = new ArrayList<>();
        tInds.addAll(Arrays.asList(eliteIndividuals[whichSubpop]));
        for (int i = 0; i < numCurrent; i++) {
            tInds.add(produceCurrent(whichSubpop, state, 0));
        }
        for (int i = 0; i < numPrev; i++) {
            tInds.add(producePrevious(whichSubpop, state, 0));
        }
        return tInds.toArray(new Individual[tInds.size()]);
    }

    protected void distributeAndEvaluate(EvolutionState state, GroupedProblemForm prob, LinkedList<ProblemEvaluation> eval) {
        if (state.evalthreads == 1) {
            GroupedProblemForm p = (GroupedProblemForm) ((Problem) prob).clone();
            ((Problem) p).prepareToEvaluate(state, 0);
            while (!eval.isEmpty()) {
                ProblemEvaluation e = eval.removeFirst();
                p.evaluate(state, e.inds, e.updates, e.countVictoriesOnly, e.subpops, 0);
            }
            ((Problem) p).finishEvaluating(state, 0);
        } else {
            Thread[] t = new Thread[state.evalthreads];
            int n = eval.size() / t.length;
            // start up the threads
            for (int y = 0; y < state.evalthreads; y++) {
                EvaluatorThread r = new EvaluatorThread();
                r.threadnum = y;
                r.state = state;
                r.p = (GroupedProblemForm) ((Problem) prob).clone();
                LinkedList<ProblemEvaluation> temp = new LinkedList<>();
                if (y < state.evalthreads - 1) {
                    for (int i = 0; i < n; i++) {
                        temp.add(eval.removeFirst());
                    }
                } else { // the remaining
                    while (!eval.isEmpty()) {
                        temp.add(eval.removeFirst());
                    }
                }
                r.evals = temp;
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
    }

    protected static class ProblemEvaluation {

        Individual[] inds;
        boolean[] updates;
        boolean countVictoriesOnly;
        int[] subpops;

        public ProblemEvaluation(Individual[] inds, boolean[] updateFitness, boolean countVictoriesOnly, int[] subpops) {
            this.inds = Arrays.copyOf(inds, inds.length);
            this.updates = Arrays.copyOf(updateFitness, updateFitness.length);
            this.countVictoriesOnly = countVictoriesOnly;
            this.subpops = Arrays.copyOf(subpops, subpops.length);
        }
    }

    protected static class EvaluatorThread implements Runnable {

        EvolutionState state;
        Collection<ProblemEvaluation> evals;
        int threadnum;
        GroupedProblemForm p;

        @Override
        public void run() {
            ((Problem) p).prepareToEvaluate(state, threadnum);
            for (ProblemEvaluation e : evals) {
                p.evaluate(state, e.inds, e.updates, e.countVictoriesOnly, e.subpops, threadnum);
            }
            ((Problem) p).finishEvaluating(state, threadnum);
        }
    }


    
    
}
