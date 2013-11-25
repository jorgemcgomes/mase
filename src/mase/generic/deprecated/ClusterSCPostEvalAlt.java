/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic;

import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.util.Parameter;
import mase.evaluation.BehaviourResult;
import mase.novelty.NoveltyFitness;
import net.jafama.FastMath;

/**
 *
 * @author jorge
 */
public class ClusterSCPostEvalAlt extends ClusterSCPostEvalBalanced {

    protected double[] weightVector;
    protected int[] countVector;
    protected double[] totalCounts;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        super.doTfIdf = false;
    }

    @Override
    public void processPopulation(EvolutionState state) {
        super.processPopulation(state);

        // Calculate weights
        weightVector = new double[clusters.length];
        countVector = new int[clusters.length];
        totalCounts = new double[clusters.length];

        // normalize fitness scores to [0,1]
        double[] fitScores = new double[currentPop.size()];
        double max = Double.NEGATIVE_INFINITY, min = Double.POSITIVE_INFINITY;
        int index = 0;
        for (Subpopulation sub : state.population.subpops) {
            for (Individual ind : sub.individuals) {
                NoveltyFitness nf = (NoveltyFitness) ind.fitness;
                double fit = nf.getFitnessScore();
                max = Math.max(max, fit);
                min = Math.min(min, fit);
                fitScores[index++] = nf.getFitnessScore();
            }
        }
        for (int i = 0; i < fitScores.length; i++) {
            fitScores[i] = (fitScores[i] - min) / (max - min);
        }

        index = 0;
        for (SCResult scr : currentPop) {
            for (int c = 0; c < numClusters; c++) {
                if (scr.rawClusteredCount[c] > 0.0001) {
                    weightVector[c] += fitScores[index];
                    countVector[c]++;
                }
                totalCounts[c] += scr.rawClusteredCount[c];
            }
            index++;
        }

        for (int i = 0; i < numClusters; i++) {
            weightVector[i] = countVector[i] == 0 ? 0 : weightVector[i] / countVector[i];
        }

        // Adjust population characterisations
        for (SCResult scr : currentPop) {
            for (int i = 0; i < numClusters; i++) {
                // first normalize the counts and then apply weight
                scr.getBehaviour()[i] = totalCounts[i] > 0 ? (float) ((scr.rawClusteredCount[i] / totalCounts[i]) * weightVector[i]) : 0;
            }
        }

        // Adjust archive
        for (BehaviourResult br : archive) {
            SCResult scr = (SCResult) br;
            for (int i = 0; i < numClusters; i++) {
                // first normalize the counts and then apply weight
                scr.getBehaviour()[i] = totalCounts[i] > 0 ? (float) ((scr.rawClusteredCount[i] / totalCounts[i]) * weightVector[i]) : 0;
            }
        }
    }

}
