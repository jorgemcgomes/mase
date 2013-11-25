/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.Subpopulation;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mase.evaluation.BehaviourResult;
import mase.evaluation.VectorBehaviourResult;
import static mase.novelty.NoveltyEvaluation.V_NONE;
import net.jafama.FastMath;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

/**
 *
 * @author jorge
 */
public class WeightedNoveltyRepo extends WeightedNovelty {

    protected List<Float> archiveFitness;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.smooth = 0;
        this.archiveFitness = new ArrayList<Float>();
    }

    /*
     * Correlation is calculated based on the current population, and smoothed with previous weight vector.
     * What behaviour features are relevant for fitness might change throughout evolution.
     */
    @Override
    protected void updateWeights(EvolutionState state, Population pop) {
        List<BehaviourResult> archive = archives.get(0);
        int indIndex = 0;
        double[] fitnessScores = new double[nIndividuals + archive.size()];
        double[][] behaviourFeatures = new double[weights.length][nIndividuals + archive.size()];

        for (Subpopulation sub : pop.subpops) {
            for (Individual ind : sub.individuals) {
                NoveltyFitness nf = (NoveltyFitness) ind.fitness;
                VectorBehaviourResult vbr = (VectorBehaviourResult) nf.getNoveltyBehaviour();
                float[] v = (float[]) vbr.value();
                for (int i = 0; i < v.length; i++) {
                    behaviourFeatures[i][indIndex] = v[i]; // fill by columns
                }
                fitnessScores[indIndex] = nf.getFitnessScore();
                indIndex++;
            }
        }
        for (int i = 0 ; i < archive.size() ; i++) {
            VectorBehaviourResult vbr = (VectorBehaviourResult) archive.get(i);
            float[] v = (float[]) vbr.value();
            for (int j = 0; j < v.length; j++) {
                behaviourFeatures[j][indIndex] = v[j]; // fill by columns
            }
            fitnessScores[indIndex] = archiveFitness.get(i);
            indIndex++;
        }

        // compute correlation
        if (correlation == V_PEARSON) {
            PearsonsCorrelation pearson = new PearsonsCorrelation();
            for (int i = 0; i < instantCorrelation.length; i++) {
                instantCorrelation[i] = (float) pearson.correlation(fitnessScores, behaviourFeatures[i]);
            }
        } else if (correlation == V_SPEARMAN) {
            SpearmansCorrelation spearman = new SpearmansCorrelation();
            for (int i = 0; i < instantCorrelation.length; i++) {
                instantCorrelation[i] = (float) spearman.correlation(fitnessScores, behaviourFeatures[i]);
            }
        }

        // calculate base weight -- absolute value and smooth
        for (int i = 0; i < instantCorrelation.length; i++) {
            if (Float.isNaN(instantCorrelation[i])) {
                instantCorrelation[i] = 0;
            }
            adjustedCorrelation[i] = Math.abs(instantCorrelation[i]) * (1 - smooth) + adjustedCorrelation[i] * smooth;
        }

        if (selection == SelectionMethod.all) {
            for (int i = 0; i < adjustedCorrelation.length; i++) {
                weights[i] = (float) FastMath.pow(adjustedCorrelation[i], selectionPressure);
            }
        } else if (selection == SelectionMethod.truncation) {
            float[] v = Arrays.copyOf(adjustedCorrelation, adjustedCorrelation.length);
            Arrays.sort(v);
            int nElites = (int) Math.ceil(selectionPressure * adjustedCorrelation.length);
            double cutoff = v[adjustedCorrelation.length - nElites];
            for (int i = 0; i < adjustedCorrelation.length; i++) {
                weights[i] = adjustedCorrelation[i] >= cutoff ? adjustedCorrelation[i] : 0;
            }
        } else if (selection == SelectionMethod.tournament) {
            Arrays.fill(weights, 0);
            for (int i = 0; i < adjustedCorrelation.length; i++) {
                int idx = makeTournament(adjustedCorrelation);
                weights[idx] += adjustedCorrelation[idx];
            }
        }
    }

    @Override
        protected void updateArchive(EvolutionState state, Population pop) {
        if (archiveMode != V_NONE) {
            for (int i = 0; i < pop.subpops.length; i++) {
                List<BehaviourResult> archive = archives.get(i);
                for (int j = 0; j < pop.subpops[i].individuals.length; j++) {
                    Individual ind = pop.subpops[i].individuals[j];
                    if (state.random[0].nextDouble() < addProb) {
                        BehaviourResult br = (BehaviourResult) ((NoveltyFitness) ind.fitness).getNoveltyBehaviour();
                        float fit = ((NoveltyFitness) ind.fitness).getFitnessScore();
                        if (archive.size() == sizeLimit) {
                            int index = state.random[0].nextInt(archive.size());
                            archive.set(index, br);
                            archiveFitness.set(index, fit);
                        } else {
                            archive.add(br);
                            archiveFitness.add(fit);
                        }
                    }
                }
            }
        }
    }
}
