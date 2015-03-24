/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.Subpopulation;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import mase.evaluation.BehaviourResult;
import org.apache.commons.compress.archivers.ArchiveEntry;

/**
 * IMPORTANT: the novelty-index should point to the individual characterisation
 *
 * @author jorge
 */
public class GroupIndNoveltyEvaluation extends NoveltyEvaluation {

    public static final String P_GROUP_BEHAV_INDEX = "group-behav-index";
    public static final String P_GROUP_WEIGHT = "group-weight";
    protected List<ArchiveEntry> groupArchive;
    protected int groupBehaviourIndex;
    protected double groupWeight;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.groupArchive = new ArrayList<ArchiveEntry>(super.sizeLimit);
        this.groupBehaviourIndex = state.parameters.getInt(base.push(P_GROUP_BEHAV_INDEX), null);
        this.groupWeight = state.parameters.getDouble(base.push(P_GROUP_WEIGHT), null);
    }

    @Override
    protected void setNoveltyScores(EvolutionState state, Population pop) {
        // Calculate individual scores
        super.setNoveltyScores(state, pop);

        // Calculate group novelty scores 
        Map<Individual, Float> groupScores = new HashMap<Individual, Float>();
        for (Subpopulation subpop : pop.subpops) {
            // Set the individuals pool that will be used to compute novelty
            List<BehaviourResult> pool = new ArrayList<BehaviourResult>();
            // Archive
            for (ArchiveEntry e : groupArchive) {
                pool.add(e.getBehaviour());
            }
            // Current population
            for (Individual ind : subpop.individuals) {
                NoveltyFitness indFit = (NoveltyFitness) ind.fitness;
                pool.add(indFit.getBehaviour(groupBehaviourIndex));
            }
            KNNDistanceCalculator calc = useKDTree && pool.size() >= k * 2
                    ? new KDTreeCalculator()
                    : new BruteForceCalculator();
            calc.setPool(pool);

            // Calculate novelty for each individual
            for (Individual ind : subpop.individuals) {
                NoveltyFitness indFit = (NoveltyFitness) ind.fitness;
                BehaviourResult br = indFit.getBehaviour(groupBehaviourIndex);
                groupScores.put(ind, (float) calc.getDistance(br, k));
            }
        }

        // Normalise both group and individual scores
        double indMin = Double.POSITIVE_INFINITY, indMax = Double.NEGATIVE_INFINITY, groupMin = Double.POSITIVE_INFINITY, groupMax = Double.NEGATIVE_INFINITY;
        for (Subpopulation subpop : pop.subpops) {
            for (Individual ind : subpop.individuals) {
                double indScore = ((NoveltyFitness) ind.fitness).getNoveltyScore();
                double groupScore = groupScores.get(ind);
                indMin = Math.min(indMin, indScore);
                indMax = Math.max(indMax, indScore);
                groupMin = Math.min(groupMin, groupScore);
                groupMax = Math.max(groupMax, groupScore);
            }
        }

        // Mix individual scores and group scores
        for (Subpopulation subpop : pop.subpops) {
            for (Individual ind : subpop.individuals) {
                NoveltyFitness nf = (NoveltyFitness) ind.fitness;
                double indScore = indMin == indMax ? 0 : (nf.getNoveltyScore() - indMin) / (indMax - indMin);
                double groupScore = groupMin == groupMax ? 0 : (groupScores.get(ind) - groupMin) / (groupMax - groupMin);
                nf.noveltyScore = (1 - groupWeight) * indScore + groupWeight * groupScore;
                nf.setFitness(state, (float) nf.noveltyScore, false);
            }
        }
    }

    @Override
    protected void updateArchive(EvolutionState state, Population pop) {
        super.updateArchive(state, pop);

        for (Subpopulation subpop : pop.subpops) {
            LinkedHashSet<Individual> toAdd = new LinkedHashSet<Individual>();
            while (toAdd.size() < archiveGrowth * subpop.individuals.length) {
                int index = state.random[0].nextInt(subpop.individuals.length);
                toAdd.add(subpop.individuals[index]);
            }
            for (Individual ind : toAdd) {
                ArchiveEntry ar = new ArchiveEntry(state, ind, ((NoveltyFitness)ind.fitness).getBehaviour(groupBehaviourIndex));
                if (groupArchive.size() == sizeLimit) {
                    int index = state.random[0].nextInt(groupArchive.size());
                    groupArchive.set(index, ar);
                } else {
                    groupArchive.add(ar);
                }
            }
        }
    }
}
