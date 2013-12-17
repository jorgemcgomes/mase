/*
 Copyright 2006 by Sean Luke and George Mason University
 Licensed under the Academic Free License version 3.0
 See the file "LICENSE" for more information
 */
package ec.coevolve;

import ec.*;
import ec.util.Parameter;
import ec.util.QuickSort;
import ec.util.SortComparator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import mase.evaluation.ExpandedFitness;
import mase.neat.NEATSubpop;
import org.neat4j.neat.core.NEATChromosome;
import org.neat4j.neat.core.NEATGeneticAlgorithm;
import org.neat4j.neat.ga.core.Chromosome;

/**
 * Multi-threaded version
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class MultiPopCoevolutionaryEvaluator2 extends MultiPopCoevolutionaryEvaluator {

    public static final String P_LAST_CHAMPIONS = "num-last-champions";
    public static final String P_RANDOM_CHAMPIONS = "num-random-champions";
    public static final String P_NEAT_ELITE = "num-neat-elite";
    public static final String P_CURRENT_ELITE = "num-current-elite";

    public static final String P_FITNESS_ONLY_ELITE = "fitness-only-elite";
    protected boolean eliteFitness;
    protected int lastChampions;
    protected int randomChampions;
    protected int neatElite;
    protected int currentElite;
    protected ArrayList<Individual>[] hallOfFame;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        eliteFitness = state.parameters.getBoolean(base.push(P_FITNESS_ONLY_ELITE), null, false);

        lastChampions = state.parameters.getIntWithDefault(base.push(P_LAST_CHAMPIONS), null, 0);
        randomChampions = state.parameters.getIntWithDefault(base.push(P_RANDOM_CHAMPIONS), null, 0);
        neatElite = state.parameters.getIntWithDefault(base.push(P_NEAT_ELITE), null, 0);
        currentElite = state.parameters.getIntWithDefault(base.push(P_CURRENT_ELITE), null, 0);

        if (lastChampions > 0 || randomChampions > 0 || neatElite > 0 || currentElite > 0) {
            this.numElite = lastChampions + randomChampions + neatElite + currentElite;
            state.output.warnOnce("Parameter value was ignored. Value changed to: " + this.numElite, base.push(P_NUM_ELITE));
        } else {
            currentElite = this.numElite;
        }
    }

    @Override
    public void evaluatePopulation(final EvolutionState state) {
        // initialize
        if (hallOfFame == null && (lastChampions > 0 || randomChampions > 0)) {
            hallOfFame = new ArrayList[state.population.subpops.length];
            for (int i = 0; i < hallOfFame.length; i++) {
                hallOfFame[i] = new ArrayList<Individual>();
            }
        }

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
        int index = 0;
        String[] marks = new String[numElite];

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

        // Last champions
        if (lastChampions > 0) {
            for (int i = 1; i <= lastChampions && i <= hallOfFame[whichSubpop].size() ; i++) {
                eliteIndividuals[whichSubpop][index++]
                        = (Individual) hallOfFame[whichSubpop].get(hallOfFame[whichSubpop].size() - i).clone();
            }
        }

        // Random champions
        if (randomChampions > 0) {
            // Choose random positions
            ArrayList<Integer> pos = new ArrayList<Integer>(hallOfFame[whichSubpop].size());
            for (int i = 0; i < hallOfFame[whichSubpop].size(); i++) {
                pos.add(i);
            }
            Collections.shuffle(pos);
            for (int i = 0; i < pos.size() && i < randomChampions; i++) {
                marks[index] = "R";
                eliteIndividuals[whichSubpop][index++]
                        = (Individual) hallOfFame[whichSubpop].get(pos.get(i)).clone();
            }
        }

        // NEAT Elite
        if (neatElite > 0) {
            // Issue: the population suffers speciation BEFORE breeding
            NEATGeneticAlgorithm neat = ((NEATSubpop) subpop).getNEAT();
            if (!neat.getSpecies().specieList().isEmpty()) {
                HashMap<Integer, Individual> specieBests = new HashMap<Integer,Individual>(neat.getSpecies().specieList().size() * 2);
                Chromosome[] genoTypes = neat.population().genoTypes();
                for (int i = 0; i < genoTypes.length; i++) {
                    int specie = ((NEATChromosome) genoTypes[i]).getSpecieId();
                    if (!specieBests.containsKey(specie) 
                            || betterThan(subpop.individuals[i], specieBests.get(specie))) {
                        specieBests.put(specie, subpop.individuals[i]);
                    }
                }
                Individual[] specBests = new Individual[specieBests.size()];
                specieBests.values().toArray(specBests);
                QuickSort.qsort(specBests, new EliteComparator2());
                for (int i = 0; i < specBests.length && i < neatElite; i++) {
                    marks[index] = "N";
                    eliteIndividuals[whichSubpop][index++] = (Individual) specBests[i].clone();
                }
            }
        }

        // Fill remaining with the elite of the current pop
        int toFill = numElite - index ;
        if (toFill == 1) { // Just one to place
            Individual best = subpop.individuals[0];
            for (int x = 1; x < subpop.individuals.length; x++) {
                if (betterThan(subpop.individuals[x], best)) {
                    best = subpop.individuals[x];
                }
            }
            marks[index] = "C";
            eliteIndividuals[whichSubpop][index++] = (Individual) best.clone();
        } else if (toFill > 1) {
            Individual[] orderedPop = Arrays.copyOf(subpop.individuals, subpop.individuals.length);
            QuickSort.qsort(orderedPop, new EliteComparator2());
            // load the top N individuals
            for (int j = 0; j < toFill; j++) {
                marks[index] = "C";
                eliteIndividuals[whichSubpop][index++] = (Individual) orderedPop[j].clone();
            }
        }

       /* System.out.println(whichSubpop + " -------");
        System.out.println(Arrays.toString(marks));
        for (int i = 0; i < numElite; i++) {
            System.out.print(eliteIndividuals[whichSubpop][i].fitness.fitness() + " ");
        }
        System.out.println();*/
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

    private boolean betterThan(Individual a, Individual b) {
        if (eliteFitness) {
            return ((ExpandedFitness) a.fitness).getFitnessScore() > ((ExpandedFitness) b.fitness).getFitnessScore();
        } else {
            return a.fitness.betterThan(b.fitness);
        }
    }
}
