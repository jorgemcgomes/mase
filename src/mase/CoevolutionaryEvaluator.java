/*
 Copyright 2006 by Sean Luke and George Mason University
 Licensed under the Academic Free License version 3.0
 See the file "LICENSE" for more information
 */
package mase;

import ec.*;
import ec.coevolve.GroupedProblemForm;
import ec.coevolve.MultiPopCoevolutionaryEvaluator;
import ec.util.Parameter;
import ec.util.QuickSort;
import ec.util.SortComparator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import mase.evaluation.ExpandedFitness;
import mase.novelty.NoveltyFitness;

/**
 * Multi-threaded version
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class CoevolutionaryEvaluator extends MultiPopCoevolutionaryEvaluator {

    public static final String P_LAST_CHAMPIONS = "num-last-champions";
    public static final String P_RANDOM_CHAMPIONS = "num-random-champions";
    public static final String P_CURRENT_ELITE = "num-current-elite";
    public static final String P_ELITE_MODE = "elite-mode";
    public static final String P_MAX_EVALUATIONS = "max-evaluations";

    public enum EliteMode {

        fitness, novelty, score

    }

    protected EliteMode eliteMode;
    public int totalEvaluations;
    public int maxEvaluations;
    protected int lastChampions;
    protected int randomChampions;
    protected int currentElite;
    protected List<Individual>[] hallOfFame;
    protected Individual[][] competitors;

    public Individual[][] getEliteIndividuals() {
        return eliteIndividuals;
    }

    public void setEliteIndividuals(Individual[][] elite) {
        this.eliteIndividuals = elite;
    }

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        maxEvaluations = state.parameters.getIntWithDefault(base.push(P_MAX_EVALUATIONS), null, -1);
        totalEvaluations = 0;

        eliteMode = EliteMode.valueOf(state.parameters.getString(base.push(P_ELITE_MODE), null));

        lastChampions = state.parameters.getIntWithDefault(base.push(P_LAST_CHAMPIONS), null, 0);
        randomChampions = state.parameters.getIntWithDefault(base.push(P_RANDOM_CHAMPIONS), null, 0);
        currentElite = state.parameters.getIntWithDefault(base.push(P_CURRENT_ELITE), null, 0);

        if (lastChampions > 0 || randomChampions > 0 || currentElite > 0) {
            this.numElite = lastChampions + randomChampions + currentElite;
            state.output.warnOnce("Parameter value was ignored. Value changed to: " + this.numElite, base.push(P_NUM_ELITE));
        } else {
            currentElite = this.numElite;
        }
    }

    @Override
    public boolean runComplete(EvolutionState state) {
        return maxEvaluations == -1 ? false : totalEvaluations >= maxEvaluations;
    }

    @Override
    protected void beforeCoevolutionaryEvaluation(EvolutionState state, Population population, GroupedProblemForm prob) {
        super.beforeCoevolutionaryEvaluation(state, population, prob);
        if (state.generation == 0) {
            if (lastChampions > 0 || randomChampions > 0) {
                hallOfFame = new ArrayList[state.population.subpops.length];
                for (int i = 0; i < hallOfFame.length; i++) {
                    hallOfFame[i] = new ArrayList<Individual>();
                }
            }
        }
    }

    @Override
    public void evaluatePopulation(final EvolutionState state) {
        // determine who needs to be evaluated
        boolean[] preAssessFitness = new boolean[state.population.subpops.length];
        boolean[] postAssessFitness = new boolean[state.population.subpops.length];
        for (int i = 0; i < state.population.subpops.length; i++) {
            postAssessFitness[i] = shouldEvaluateSubpop(state, i, 0);
            preAssessFitness[i] = postAssessFitness[i] || (state.generation == 0);  // always prepare (set up trials) on generation 0
            totalEvaluations += state.population.subpops[i].individuals.length;
        }

        // first generation initialization
        beforeCoevolutionaryEvaluation(state, state.population, (GroupedProblemForm) p_problem);

        // do evaluation        
        ((GroupedProblemForm) p_problem).preprocessPopulation(state, state.population, preAssessFitness, false);
        performCoevolutionaryEvaluation(state, state.population, (GroupedProblemForm) p_problem);
        ((GroupedProblemForm) p_problem).postprocessPopulation(state, state.population, postAssessFitness, false);

        // other methods become responsible of calling afterCoevolutionaryEvaluation
    }

    @Override
    public void performCoevolutionaryEvaluation(final EvolutionState state,
            final Population population,
            final GroupedProblemForm prob) {

        /* initialization */
        inds = new Individual[population.subpops.length];
        updates = new boolean[population.subpops.length];

        // build subpopulation array to pass in each time
        int[] subpops = new int[population.subpops.length];
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

        this.competitors = new Individual[state.population.subpops.length][];
        for (int p = 0; p < population.subpops.length; p++) {
            loadCompetitors(state, p);
        }

        /* subpops evaluation -- multi-threaded */
        int numinds[][] = new int[state.evalthreads][population.subpops.length];
        int from[][] = new int[state.evalthreads][population.subpops.length];
        // figure out multi-thread distribution
        for (int y = 0; y < state.evalthreads; y++) {
            for (int x = 0; x < population.subpops.length; x++) {
                if (shouldEvaluateSubpop(state, x, 0)) {
                    // figure numinds
                    if (y < state.evalthreads - 1) { // not last one
                        numinds[y][x] = population.subpops[x].individuals.length / state.evalthreads;
                    } else { // in case we're slightly off in division
                        numinds[y][x]
                                = population.subpops[x].individuals.length / state.evalthreads
                                + (population.subpops[x].individuals.length
                                - (population.subpops[x].individuals.length
                                / state.evalthreads) // note integer division
                                * state.evalthreads);
                    }
                    // figure from
                    from[y][x] = (population.subpops[x].individuals.length / state.evalthreads) * y;
                }
            }
        }

        // distribute the work
        if (state.evalthreads == 1) {
            evalPopChunk(state, population, numinds[0], from[0], 0, subpops, (GroupedProblemForm) ((Problem) prob).clone());
        } else {
            Thread[] t = new Thread[state.evalthreads];
            // start up the threads
            for (int y = 0; y < state.evalthreads; y++) {
                EvaluatorThread r = new EvaluatorThread();
                r.threadnum = y;
                r.numinds = numinds[y];
                r.from = from[y];
                r.state = state;
                r.pop = population;
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

        /* finalization */
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
        public Population pop;
        public int threadnum;
        public GroupedProblemForm p;
        public int[] subpops;

        @Override
        public void run() {
            evalPopChunk(state, pop, numinds, from, threadnum, subpops, p);
        }
    }

    protected void evalPopChunk(EvolutionState state, Population pop, int[] numinds, int[] from, int threadnum, int[] subpops, GroupedProblemForm prob) {
        ((Problem) prob).prepareToEvaluate(state, threadnum);
        boolean[] tUpdates = new boolean[pop.subpops.length];
        Individual[] tInds = new Individual[pop.subpops.length];

        for (int p = 0; p < pop.subpops.length; p++) {
            // start evaluatin'!
            int upperbound = from[p] + numinds[p];
            for (int x = from[p]; x < upperbound; x++) {
                Individual individual = pop.subpops[p].individuals[x];
                // Test against the competitors
                for (int k = 0; k < competitors[p].length; k++) {
                    for (int ind = 0; ind < tInds.length; ind++) {
                        if (ind == p) {
                            tInds[ind] = individual;
                            tUpdates[ind] = true;
                        } else {
                            tInds[ind] = competitors[ind][k];
                            tUpdates[ind] = false;
                        }
                    }
                    prob.evaluate(state, tInds, tUpdates, false, subpops, 0);
                }
            }
        }
        ((Problem) prob).finishEvaluating(state, threadnum);
    }

    void loadCompetitors(EvolutionState state, int whichSubpop) {
        ArrayList<Individual> tInds = new ArrayList<Individual>();
        for (Individual ind : eliteIndividuals[whichSubpop]) {
            tInds.add(ind);
        }
        for (int i = 0; i < numCurrent; i++) {
            tInds.add(produceCurrent(whichSubpop, state, 0));
        }
        for (int i = 0; i < numPrev; i++) {
            tInds.add(producePrevious(whichSubpop, state, 0));
        }
        Individual[] tIndsA = new Individual[tInds.size()];
        tInds.toArray(tIndsA);
        competitors[whichSubpop] = tIndsA;
    }

    @Override
    protected void loadElites(final EvolutionState state, int whichSubpop) {
        Subpopulation subpop = state.population.subpops[whichSubpop];

        // Update hall of fame
        if (hallOfFame != null) {
            int best = 0;
            Individual[] oldinds = subpop.individuals;
            for (int x = 1; x < oldinds.length; x++) {
                if (betterThan(oldinds[x], oldinds[best])) {
                    best = x;
                }
            }
            hallOfFame[whichSubpop].add((Individual) subpop.individuals[best].clone());
        }

        int index = 0;

        // Last champions
        if (lastChampions > 0) {
            for (int i = 1; i <= lastChampions && i <= hallOfFame[whichSubpop].size(); i++) {
                eliteIndividuals[whichSubpop][index++]
                        = (Individual) hallOfFame[whichSubpop].get(hallOfFame[whichSubpop].size() - i).clone();
            }
        }

        double randChamps = randomChampions;

        // Random champions
        if (randChamps > 0) {
            // Choose random positions
            ArrayList<Integer> pos = new ArrayList<Integer>(hallOfFame[whichSubpop].size());
            for (int i = 0; i < hallOfFame[whichSubpop].size(); i++) {
                pos.add(i);
            }
            Collections.shuffle(pos);
            for (int i = 0; i < pos.size() && i < randChamps; i++) {
                eliteIndividuals[whichSubpop][index++]
                        = (Individual) hallOfFame[whichSubpop].get(pos.get(i)).clone();
            }
        }

        // Fill remaining with the elite of the current pop
        int toFill = numElite - index;
        if (toFill == 1) { // Just one to place
            Individual best = subpop.individuals[0];
            for (int x = 1; x < subpop.individuals.length; x++) {
                if (betterThan(subpop.individuals[x], best)) {
                    best = subpop.individuals[x];
                }
            }
            eliteIndividuals[whichSubpop][index++] = (Individual) best.clone();
        } else if (toFill > 1) {
            Individual[] orderedPop = Arrays.copyOf(subpop.individuals, subpop.individuals.length);
            QuickSort.qsort(orderedPop, new EliteComparator2());
            // load the top N individuals
            for (int j = 0; j < toFill; j++) {
                eliteIndividuals[whichSubpop][index++] = (Individual) orderedPop[j].clone();
            }
        }
    }

    // sorts the opposite way on purpose -- we want the best to be first in the array
    class EliteComparator2 implements SortComparator {

        @Override
        public boolean lt(Object a, Object b) {
            return betterThan((Individual) a, (Individual) b);
        }

        @Override
        public boolean gt(Object a, Object b) {
            return betterThan((Individual) b, (Individual) a);
        }

    }

    protected boolean betterThan(Individual a, Individual b) {
        if (eliteMode == EliteMode.fitness) {
            double fa = ((ExpandedFitness) a.fitness).getFitnessScore();
            double fb = ((ExpandedFitness) b.fitness).getFitnessScore();
            if(fa != fb) {
                return fa > fb;
            } else {
                return a.fitness.betterThan(b.fitness);
            }
        } else if (eliteMode == EliteMode.novelty) {
            return ((NoveltyFitness) a.fitness).getNoveltyScore() > ((NoveltyFitness) b.fitness).getNoveltyScore();
        } else {
            return a.fitness.betterThan(b.fitness);
        }
    }
}
