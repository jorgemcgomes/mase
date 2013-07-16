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
import java.util.Arrays;
import mase.evaluation.BehaviourResult;
import mase.evaluation.VectorBehaviourResult;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

/**
 *
 * @author jorge
 */
public class WeightedNovelty extends NoveltyEvaluation {

    public static final String P_CORRELATION = "correlation";
    public static final String V_PEARSON = "pearson", V_SPEARMAN = "spearman";
    public static final String P_MIN_WEIGHT = "min-weight";
    public static final String P_SMOOTH = "smooth";
    protected double[] weights;
    protected double[] instantCorrelation;
    protected double[] smoothedCorrelation;
    protected int nIndividuals;
    protected String correlation;
    protected double smooth;
    protected double minWeight;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        String corr = state.parameters.getString(base.push(P_CORRELATION), null);
        if (corr.equalsIgnoreCase(V_PEARSON)) {
            this.correlation = V_PEARSON;
        } else if (corr.equalsIgnoreCase(V_SPEARMAN)) {
            this.correlation = V_SPEARMAN;
        } else {
            state.output.fatal("Unknown correlation method.", base.push(P_CORRELATION));
        }
        this.smooth = state.parameters.getDouble(base.push(P_SMOOTH), null);
        this.minWeight = state.parameters.getDouble(base.push(P_MIN_WEIGHT), null);
        this.nIndividuals = 0;
    }

    @Override
    public void processPopulation(EvolutionState state) {
        super.processPopulation(state);
        updateWeights(state, state.population);
    }

    /*
     * Only works if the BehaviourResult's are VectorBehaviourResult's.
     * Euclidean distance is used.
     */
    @Override
    protected float distance(BehaviourResult br1, BehaviourResult br2) {
        float[] v1 = (float[]) ((VectorBehaviourResult) br1).value();
        float[] v2 = (float[]) ((VectorBehaviourResult) br2).value();

        if (weights == null) {
            weights = new double[v1.length];
            Arrays.fill(weights, 1);
            instantCorrelation = new double[weights.length];
            Arrays.fill(instantCorrelation, 0);
            smoothedCorrelation = new double[weights.length];
            Arrays.fill(smoothedCorrelation, 0);
        }

        float d = 0;
        for (int i = 0; i < weights.length; i++) {
            d += Math.pow((v1[i] - v2[i]) * weights[i], 2);
        }
        return (float) Math.sqrt(d);
    }

    /*
     * Correlation is calculated based on the current population, and smoothed with previous weight vector.
     * What behaviour features are relevant for fitness might change throughout evolution.
     */
    protected void updateWeights(EvolutionState state, Population pop) {
        if (nIndividuals == 0) {
            for (Subpopulation sub : state.population.subpops) {
                nIndividuals += sub.individuals.length;
            }
        }

        int indIndex = 0;
        double[] fitnessScores = new double[nIndividuals];
        double[][] behaviourFeatures = new double[weights.length][nIndividuals];

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

        // compute correlation
        if (correlation == V_PEARSON) {
            PearsonsCorrelation pearson = new PearsonsCorrelation();
            for (int i = 0; i < instantCorrelation.length; i++) {
                instantCorrelation[i] = pearson.correlation(fitnessScores, behaviourFeatures[i]);
            }
        } else if (correlation == V_SPEARMAN) {
            SpearmansCorrelation spearman = new SpearmansCorrelation();
            for (int i = 0; i < instantCorrelation.length; i++) {
                instantCorrelation[i] = spearman.correlation(fitnessScores, behaviourFeatures[i]);
            }
        }

        // abs and smooth
        for (int i = 0; i < instantCorrelation.length; i++) {
            smoothedCorrelation[i] = Math.abs(instantCorrelation[i]) * (1 - smooth) + smoothedCorrelation[i] * smooth;
            weights[i] = smoothedCorrelation[i] + minWeight - smoothedCorrelation[i] * minWeight;
        }
    }
}
