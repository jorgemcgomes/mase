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
    public static final String P_CORR_EXPONENT = "corr-exponent";
    protected float[] weights;
    protected float[] instantCorrelation;
    protected float[] adjustedCorrelation;
    protected int nIndividuals;
    protected String correlation;
    protected float smooth;
    protected float minWeight;
    protected float corrExponent;

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
        this.smooth = state.parameters.getFloat(base.push(P_SMOOTH), null, 0.5);
        this.minWeight = state.parameters.getFloat(base.push(P_MIN_WEIGHT), null, 0);
        this.corrExponent = state.parameters.getFloat(base.push(P_CORR_EXPONENT), null, 1);
        this.nIndividuals = 0;
    }

    @Override
    public void processPopulation(EvolutionState state) {
        super.processPopulation(state);
        updateWeights(state, state.population);
    }

    /*
     * Only works if the BehaviourResult's are VectorBehaviourResult's.
     */
    @Override
    protected float distance(BehaviourResult br1, BehaviourResult br2) {
        float[] v1 = (float[]) ((VectorBehaviourResult) br1).value();
        float[] v2 = (float[]) ((VectorBehaviourResult) br2).value();

        if (weights == null) { // TODO: mudar de sitio?
            weights = new float[v1.length];
            Arrays.fill(weights, 1);
            instantCorrelation = new float[weights.length];
            Arrays.fill(instantCorrelation, 0);
            adjustedCorrelation = new float[weights.length];
            Arrays.fill(adjustedCorrelation, 0);
        }
        
        float[] w1 = new float[v1.length];
        float[] w2 = new float[v2.length];
        for(int i = 0 ; i < v1.length ; i++) {
            w1[i] = v1[i] * weights[i];
            w2[i] = v2[i] * weights[i];
            
        }
        
        float d = ((VectorBehaviourResult) br1).vectorDistance(w1, w2);
        /*if(Float.isNaN(d) || Float.isInfinite(d)) {
            System.out.println("Wrong distance: " + d + " V1: " + Arrays.toString(v1) + " | V2: " + Arrays.toString(v2) + " | W: " + Arrays.toString(weights));
        }
        if(d < 0.0001 && d > -0.0001) {
            System.out.println("Zero distance: " + d + " V1: " + Arrays.toString(v1) + " | V2: " + Arrays.toString(v2) + " | W: " + Arrays.toString(weights));
        }*/
        return d;
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
                instantCorrelation[i] = (float) pearson.correlation(fitnessScores, behaviourFeatures[i]);
            }
        } else if (correlation == V_SPEARMAN) {
            SpearmansCorrelation spearman = new SpearmansCorrelation();
            for (int i = 0; i < instantCorrelation.length; i++) {
                instantCorrelation[i] = (float) spearman.correlation(fitnessScores, behaviourFeatures[i]);
            }
        }

        // abs and smooth
        for (int i = 0; i < instantCorrelation.length; i++) {
            if(Float.isNaN(instantCorrelation[i])) {
                instantCorrelation[i] = 0;
            }
            adjustedCorrelation[i] = Math.abs(instantCorrelation[i]) * (1 - smooth) + adjustedCorrelation[i] * smooth;
            adjustedCorrelation[i] = (float) Math.pow(adjustedCorrelation[i], corrExponent);
            weights[i] = adjustedCorrelation[i] + minWeight - adjustedCorrelation[i] * minWeight;
        }
    }

    public float[] getWeights() {
        return weights;
    }

    public float[] getInstantCorrelation() {
        return instantCorrelation;
    }

    public float[] getAdjustedCorrelation() {
        return adjustedCorrelation;
    }
}
