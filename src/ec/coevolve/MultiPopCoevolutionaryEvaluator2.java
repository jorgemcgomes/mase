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
import java.util.Iterator;
import java.util.List;
import mase.MetaEvaluator;
import mase.PostEvaluator;
import mase.evaluation.ExpandedFitness;
import mase.evaluation.VectorBehaviourResult;
import mase.neat.NEATSubpop;
import mase.novelty.NoveltyEvaluation;
import mase.novelty.NoveltyEvaluation.ArchiveEntry;
import mase.novelty.NoveltyFitness;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
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
    public static final String P_NOVEL_CHAMPIONS = "num-novel-champions";
    public static final String P_NOVEL_CHAMPIONS_MODE = "novel-champions-mode";
    public static final String P_NOVEL_CHAMPIONS_ORIGIN = "novel-champions-origin";
    public static final String P_NOVEL_CHAMPIONS_CAP = "novel-champions-cap";
    public static final String P_NEAT_ELITE = "num-neat-elite";
    public static final String P_CURRENT_ELITE = "num-current-elite";
    public static final String P_FITNESS_ONLY_ELITE = "fitness-only-elite";
    
    public enum NovelChampionsMode {

        random, last, centroid, best;
    }

    public enum NovelChampionsOrigin {

        archive, halloffame
    }

    protected boolean eliteFitness;
    protected int lastChampions;
    protected int randomChampions;
    protected int novelChampions;
    protected int neatElite;
    protected int currentElite;
    protected float novelChampionsCap;
    protected NovelChampionsMode novelChampionsMode;
    protected NovelChampionsOrigin novelChampionsOrigin;
    protected List<Individual>[] hallOfFame;
    protected List<ArchiveEntry>[] archives;
    protected Individual[][] competitors;

    public Individual[][] getEliteIndividuals() {
        return eliteIndividuals;
    }
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        eliteFitness = state.parameters.getBoolean(base.push(P_FITNESS_ONLY_ELITE), null, false);

        lastChampions = state.parameters.getIntWithDefault(base.push(P_LAST_CHAMPIONS), null, 0);
        randomChampions = state.parameters.getIntWithDefault(base.push(P_RANDOM_CHAMPIONS), null, 0);
        novelChampions = state.parameters.getIntWithDefault(base.push(P_NOVEL_CHAMPIONS), null, 0);
        if (novelChampions > 0) {
            novelChampionsMode = NovelChampionsMode.valueOf(state.parameters.getString(base.push(P_NOVEL_CHAMPIONS_MODE), null));
            novelChampionsOrigin = NovelChampionsOrigin.valueOf(state.parameters.getString(base.push(P_NOVEL_CHAMPIONS_ORIGIN), null));
            novelChampionsCap = state.parameters.getFloat(base.push(P_NOVEL_CHAMPIONS_CAP), null);
        }
        neatElite = state.parameters.getIntWithDefault(base.push(P_NEAT_ELITE), null, 0);
        currentElite = state.parameters.getIntWithDefault(base.push(P_CURRENT_ELITE), null, 0);

        if (lastChampions > 0 || randomChampions > 0 || novelChampions > 0 || neatElite > 0 || currentElite > 0) {
            this.numElite = lastChampions + randomChampions + novelChampions + neatElite + currentElite;
            state.output.warnOnce("Parameter value was ignored. Value changed to: " + this.numElite, base.push(P_NUM_ELITE));
        } else {
            currentElite = this.numElite;
        }
    }

    @Override
    protected void beforeCoevolutionaryEvaluation(EvolutionState state, Population population, GroupedProblemForm prob) {
        super.beforeCoevolutionaryEvaluation(state, population, prob);
        if (state.generation == 0) {
            this.competitors = new Individual[state.population.subpops.length][];
            if (lastChampions > 0 || randomChampions > 0 || novelChampions > 0) {
                hallOfFame = new ArrayList[state.population.subpops.length];
                for (int i = 0; i < hallOfFame.length; i++) {
                    hallOfFame[i] = new ArrayList<Individual>();
                }
            }
            if (novelChampions > 0 && novelChampionsOrigin == NovelChampionsOrigin.archive) {
                for (PostEvaluator pe : ((MetaEvaluator) state.evaluator).getPostEvaluators()) {
                    if (pe instanceof NoveltyEvaluation) {
                        NoveltyEvaluation ne = (NoveltyEvaluation) pe;
                        archives = ne.getArchives();
                        break;
                    }
                }
                if (archives == null) {
                    state.output.fatal("NoveltyEvaluation is not being used. It is impossible to get the archives.");
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
    void loadElites(final EvolutionState state, int whichSubpop) {
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
        
        // Novel champions
        if (novelChampions > 0) {
            Individual[] behaviourElite = behaviourElite(state, whichSubpop);
            for (int i = 0; i < behaviourElite.length; i++) {
                eliteIndividuals[whichSubpop][index++] = (Individual) behaviourElite[i].clone();
                //System.out.println(whichSubpop + "\t" + ((ExpandedFitness) behaviourElite[i].fitness).getFitnessScore());
            }
            randChamps = randomChampions + (novelChampions - behaviourElite.length);
        }

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

        // NEAT Elite
        if (neatElite > 0) {
            NEATGeneticAlgorithm neat = ((NEATSubpop) subpop).getNEAT();
            if (!neat.getSpecies().specieList().isEmpty()) {
                HashMap<Integer, Individual> specieBests = new HashMap<Integer, Individual>(neat.getSpecies().specieList().size() * 2);
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
                    eliteIndividuals[whichSubpop][index++] = (Individual) specBests[i].clone();
                }
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
        if (eliteFitness) {
            return ((ExpandedFitness) a.fitness).getFitnessScore() > ((ExpandedFitness) b.fitness).getFitnessScore();
        } else {
            return a.fitness.betterThan(b.fitness);
        }
    }

    // Do the normal k-means clustering and then the picked individuals are the closest ones to the centroid
    protected Individual[] behaviourElite(EvolutionState state, int subpop) {
        // Generate the dataset
        ArrayList<IndividualClusterable> points = new ArrayList<IndividualClusterable>();
        if (novelChampionsOrigin == NovelChampionsOrigin.halloffame) {
            for (int i = 0; i < hallOfFame[subpop].size(); i++) {
                points.add(new IndividualClusterable(hallOfFame[subpop].get(i), i));
            }
        } else if (novelChampionsOrigin == NovelChampionsOrigin.archive) {
            for (ArchiveEntry ae : archives[subpop]) {
                points.add(new IndividualClusterable(ae.getIndividual(), ae.getGeneration()));
            }
        }

        // Cap -- only use the individuals with the highest fitness scores
        if (novelChampionsCap > 0) {
            // calculate the percentile
            DescriptiveStatistics ds = new DescriptiveStatistics();
            for (IndividualClusterable ic : points) {
                ds.addValue(ic.getFitness());
            }
            double percentile = ds.getPercentile(novelChampionsCap);

            // remove those below the percentile
            Iterator<IndividualClusterable> iter = points.iterator();
            while (iter.hasNext()) {
                IndividualClusterable next = iter.next();
                if (next.getFitness() < percentile) {
                    iter.remove();
                }
            }
        }

        // Check if there are enough points for clustering
        if (points.size() <= novelChampions) {
            Individual[] elite = new Individual[points.size()];
            for (int i = 0; i < elite.length; i++) {
                elite[i] = points.get(i).getIndividual();
            }
            return elite;
        }

        // Do the k-means clustering
        KMeansPlusPlusClusterer<IndividualClusterable> clusterer
                = new KMeansPlusPlusClusterer<IndividualClusterable>(novelChampions, 100);
        List<CentroidCluster<IndividualClusterable>> clusters = clusterer.cluster(points);

        // Return one from each cluster
        Individual[] elite = new Individual[novelChampions];
        for (int i = 0; i < clusters.size(); i++) {
            CentroidCluster<IndividualClusterable> cluster = clusters.get(i);
            List<IndividualClusterable> clusterPoints = cluster.getPoints();
            if (novelChampionsMode == NovelChampionsMode.random) {
                int randIndex = state.random[0].nextInt(clusterPoints.size());
                elite[i] = clusterPoints.get(randIndex).getIndividual();
            } else if (novelChampionsMode == NovelChampionsMode.last) {
                IndividualClusterable oldest = null;
                for (IndividualClusterable ic : clusterPoints) {
                    if (oldest == null || ic.age > oldest.age) {
                        oldest = ic;
                    }
                }
                elite[i] = oldest.getIndividual();
            } else if (novelChampionsMode == NovelChampionsMode.centroid) {
                DistanceMeasure dm = clusterer.getDistanceMeasure();
                double[] centroid = cluster.getCenter().getPoint();
                IndividualClusterable closest = null;
                double closestDist = Double.MAX_VALUE;
                for (IndividualClusterable ic : clusterPoints) {
                    double dist = dm.compute(centroid, ic.getPoint());
                    if (dist < closestDist) {
                        closestDist = dist;
                        closest = ic;
                    }
                }
                elite[i] = closest.getIndividual();
            } else if (novelChampionsMode == NovelChampionsMode.best) {
                IndividualClusterable best = null;
                float highestFit = Float.NEGATIVE_INFINITY;
                for (IndividualClusterable ic : clusterPoints) {
                    if (ic.getFitness() > highestFit) {
                        best = ic;
                        highestFit = ic.getFitness();
                    }
                }
                elite[i] = best.getIndividual();
            }
        }
        return elite;
    }

    protected static class IndividualClusterable implements Clusterable {

        private final double[] p;
        private final float fitness;
        private final Individual ind;
        private final int age;

        IndividualClusterable(Individual ind, int age) {
            this.ind = ind;
            NoveltyFitness nf = (NoveltyFitness) ind.fitness;
            VectorBehaviourResult br = (VectorBehaviourResult) nf.getNoveltyBehaviour();
            this.p = new double[br.getBehaviour().length];
            for (int i = 0; i < p.length; i++) {
                p[i] = br.getBehaviour()[i];
            }
            this.fitness = nf.getFitnessScore();
            this.age = age;
        }

        @Override
        public double[] getPoint() {
            return p;
        }

        public Individual getIndividual() {
            return ind;
        }

        public float getFitness() {
            return fitness;
        }

    }

}
