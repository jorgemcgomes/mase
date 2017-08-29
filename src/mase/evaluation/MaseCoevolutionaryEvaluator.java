/*
 Copyright 2006 by Sean Luke and George Mason University
 Licensed under the Academic Free License version 3.0
 See the file "LICENSE" for more information
 */
package mase.evaluation;

import ec.*;
import ec.coevolve.GroupedProblemForm;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import mase.mo.NSGA2;
import mase.mo.NSGA2.NSGAIndividual;

/**
 * Multi-threaded version
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class MaseCoevolutionaryEvaluator extends MultiPopCoevolutionaryEvaluatorThreaded {

    public static final String P_LAST_CHAMPIONS = "num-last-champions";
    public static final String P_RANDOM_CHAMPIONS = "num-random-champions";
    public static final String P_CURRENT_ELITE = "num-current-elite";
    public static final String P_ELITE_MODE = "elite-score";
    public static final String V_SELECTION_SCORE = "score";
    public static final String P_PARETO_ELITE = "num-pareto-front";

    private static final long serialVersionUID = 1L;

    protected String[] eliteScore;
    protected int lastChampions;
    protected int randomChampions;
    protected int currentElite;
    protected int paretoFront;
    protected List<Individual>[] hallOfFame;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);

        String st = state.parameters.getString(base.push(P_ELITE_MODE), null);
        if (st.equalsIgnoreCase(V_SELECTION_SCORE)) {
            eliteScore = new String[]{null};
        } else {
            eliteScore = st.split(",");
        }

        lastChampions = state.parameters.getIntWithDefault(base.push(P_LAST_CHAMPIONS), null, 0);
        randomChampions = state.parameters.getIntWithDefault(base.push(P_RANDOM_CHAMPIONS), null, 0);
        currentElite = state.parameters.getIntWithDefault(base.push(P_CURRENT_ELITE), null, 0);
        paretoFront = state.parameters.getIntWithDefault(base.push(P_PARETO_ELITE), null, 0);

        if (lastChampions > 0 || randomChampions > 0 || currentElite > 0 || paretoFront > 0) {
            this.numElite = lastChampions + randomChampions + paretoFront + currentElite;
            state.output.warnOnce("Parameter value was ignored. Value changed to: " + this.numElite, base.push(P_NUM_ELITE));
        } else {
            currentElite = this.numElite;
        }
    }


    @Override
    protected void beforeCoevolutionaryEvaluation(EvolutionState state, Population population, GroupedProblemForm prob) {
        super.beforeCoevolutionaryEvaluation(state, population, prob);
        if (state.generation == 0) {
            if (lastChampions > 0 || randomChampions > 0) {
                hallOfFame = new ArrayList[state.population.subpops.size()];
                for (int i = 0; i < hallOfFame.length; i++) {
                    hallOfFame[i] = new ArrayList<>();
                }
            }
        }
    }

    @Override
    protected void loadElites(final EvolutionState state, int whichSubpop) {
        Subpopulation subpop = state.population.subpops.get(whichSubpop);

        // Update hall of fame if it exists
        if (hallOfFame != null) {
            int best = 0;
            List<Individual> oldinds = subpop.individuals;
            for (int x = 1; x < oldinds.size(); x++) {
                if (betterThan(oldinds.get(x), oldinds.get(best), eliteScore[0])) {
                    best = x;
                }
            }
            hallOfFame[whichSubpop].add((Individual) subpop.individuals.get(best).clone());
        }

        int index = 0;

        // Add last champions
        if (lastChampions > 0) {
            for (int i = 1; i <= lastChampions && i <= hallOfFame[whichSubpop].size(); i++) {
                eliteIndividuals[whichSubpop][index++]
                        = (Individual) hallOfFame[whichSubpop].get(hallOfFame[whichSubpop].size() - i).clone();
            }
        }

        // Add random champions
        if (randomChampions > 0) {
            // Choose random positions from the Hall of Fame
            ArrayList<Integer> pos = new ArrayList<>(hallOfFame[whichSubpop].size());
            for (int i = 0; i < hallOfFame[whichSubpop].size(); i++) {
                pos.add(i);
            }
            Collections.shuffle(pos);
            // Add the individuals
            for (int i = 0; i < pos.size() && i < randomChampions; i++) {
                eliteIndividuals[whichSubpop][index++]
                        = (Individual) hallOfFame[whichSubpop].get(pos.get(i)).clone();
            }
        }

        // Add individuals from pareto front
        if (paretoFront > 0) {
            // get the NSGA PostEvaluator
            NSGA2 nsga = null;
            for(PostEvaluator pe : ((MetaEvaluator) state.evaluator).getPostEvaluators()) {
                if(pe instanceof NSGA2) {
                    nsga = (NSGA2) pe;
                    break;
                }
            }
            
            int added = 0;
            int currentRank = 1;
            List<NSGAIndividual> ranking = nsga.getIndividualsRanking()[whichSubpop];
            while(added < paretoFront) {
                // get all individuals from currentRank
                ArrayList<Individual> rank = new ArrayList<>();
                for(NSGAIndividual ind : ranking) {
                    if(ind.getRank() == currentRank) {
                        rank.add(ind.getIndividual());
                    }
                }
                
                // pick random individuals from currentRank
                Collections.shuffle(rank);
                for(int i = 0 ; i < rank.size() && added < paretoFront ; i++) {
                    eliteIndividuals[whichSubpop][index++] = (Individual) rank.get(i).clone();
                    added++;
                }
                
                // if there arent enough inds in this rank, move to the next one
                currentRank++;
            }            
        }

        // Fill remaining with the elite of the current pop
        int toFill = numElite - index;
        // the number of individuals left to fill is less than or equal to the number of elite scores
        // no need to do any sorting, since it is going to be the single best
        if (toFill <= eliteScore.length) {
            for (int i = 0; i < toFill && index < numElite; i++) {
                Individual best = subpop.individuals.get(0);
                for (int x = 1; x < subpop.individuals.size(); x++) {
                    if (betterThan(subpop.individuals.get(x), best, eliteScore[i])) {
                        best = subpop.individuals.get(x);
                    }
                }
                eliteIndividuals[whichSubpop][index++] = (Individual) best.clone();
            }
        // more than one individual per score are need, therefore we sort them and fill with the top
        } else {
            // Sort the individuals according to the multiple objectives
            List<Individual>[] sorted = new List[eliteScore.length];
            for(int i = 0 ; i < eliteScore.length ; i++) {
                List<Individual> indsList = new ArrayList<>(subpop.individuals);
                final String currentScore = eliteScore[i];
                Collections.sort(indsList, new Comparator<Individual>() {
                    @Override
                    public int compare(Individual o1, Individual o2) {
                        double s1 = ((ExpandedFitness) o1.fitness).getScore(currentScore);
                        double s2 = ((ExpandedFitness) o2.fitness).getScore(currentScore);
                        return Double.compare(s2, s1);
                    }
                });
                sorted[i] = indsList;
            }
            // load the top individuals
            for(int i = 0 ; i < toFill ; i++) {
                eliteIndividuals[whichSubpop][index++] = (Individual) sorted[i % sorted.length].get(i / sorted.length).clone();
            }
        }
    }

    protected boolean betterThan(Individual a, Individual b, String score) {
        if (score == null) {
            return a.fitness.betterThan(b.fitness);
        } else {
            double sa = ((ExpandedFitness) a.fitness).getScore(score);
            double sb = ((ExpandedFitness) b.fitness).getScore(score);
            return sa > sb;
        }
    }
}
