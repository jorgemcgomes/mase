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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mase.evaluation.BehaviourResult;

/**
 *
 * @author jorge
 */
public class GroupIndNoveltyEvaluation extends NoveltyEvaluation {

    public static final String P_GROUP_BEHAV_INDEX = "group-behav-index";
    protected List<ArchiveEntry> groupArchive;
    protected int groupBehaviourIndex;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.groupArchive = new ArrayList<ArchiveEntry>(super.sizeLimit);
        this.groupBehaviourIndex = state.parameters.getInt(base.push(P_GROUP_BEHAV_INDEX), null);
    }

    @Override
    protected void setNoveltyScores(EvolutionState state, Population pop) {
        super.setNoveltyScores(state, pop);

        // Calculate group novelty scores 
        Map<Individual, Float> groupScores = new HashMap<Individual, Float>();
        for (Subpopulation subpop : pop.subpops) {
            for (Individual ind : subpop.individuals) {
                BehaviourResult brInd = ((NoveltyFitness) ind.fitness).getBehaviour(groupBehaviourIndex);
                ArrayList<Float> distances = new ArrayList<Float>(groupArchive.size() + subpop.individuals.length);
                // from subpop
                if (useCurrent) {
                    for (Individual i : subpop.individuals) {
                        if (ind != i) {
                            distances.add(distance(((NoveltyFitness) i.fitness).getBehaviour(groupBehaviourIndex), brInd));
                        }
                    }
                }
                // from repo
                for (ArchiveEntry ar : groupArchive) {
                    distances.add(distance(ar.getBehaviour(), brInd));
                }
                // sort the distances
                Collections.sort(distances);
                // average to k nearest
                float score = 0;
                for (int i = 0; i < k && i < distances.size(); i++) {
                    score += distances.get(i);
                }
                groupScores.put(ind, score / k);
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
                nf.noveltyScore = indScore + groupScore;
                nf.setFitness(state, (float) nf.noveltyScore, false);
            }
        }
    }

    @Override
    protected void updateArchive(EvolutionState state, Population pop) {
        super.updateArchive(state, pop);

        for (Subpopulation subpop : pop.subpops) {
            for (Individual ind : subpop.individuals) {
                if (state.random[0].nextDouble() < addProb) {
                    ArchiveEntry ar = new ArchiveEntry(state, ind, ((NoveltyFitness) ind.fitness).getBehaviour(groupBehaviourIndex));
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

}
