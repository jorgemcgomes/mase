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
import net.jafama.FastMath;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

/**
 *
 * @author jorge
 */
public class WeightedNovelty extends NoveltyEvaluation {

    public static enum SelectionMethod {

        all, truncation, tournament, roulette, normalised
    };

    public static final String P_CORRELATION = "correlation";
    public static final String V_PEARSON = "pearson", V_SPEARMAN = "spearman", V_BROWNIAN = "brownian";
    public static final String P_SMOOTH = "smooth";
    public static final String P_SELECTION_PRESSURE = "selection-pressure";
    public static final String P_DIMENSION_SELECTION = "selection-method";
    protected float[] weights;
    protected float[] instantCorrelation;
    protected float[] adjustedCorrelation;
    protected int nIndividuals;
    protected String correlation;
    protected float smooth;
    protected SelectionMethod selection;
    protected float selectionPressure;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        String corr = state.parameters.getString(base.push(P_CORRELATION), null);
        if (corr.equalsIgnoreCase(V_PEARSON)) {
            this.correlation = V_PEARSON;
        } else if (corr.equalsIgnoreCase(V_SPEARMAN)) {
            this.correlation = V_SPEARMAN;
        } else if (corr.equalsIgnoreCase(V_BROWNIAN)) {
            this.correlation = V_BROWNIAN;
        } else {
            state.output.fatal("Unknown correlation method.", base.push(P_CORRELATION));
        }
        String m = state.parameters.getString(base.push(P_DIMENSION_SELECTION), null);
        selection = SelectionMethod.valueOf(m);
        if (selection == null) {
            state.output.fatal("Unknown selection method: " + m, base.push(P_DIMENSION_SELECTION));
        }
        this.selectionPressure = state.parameters.getFloat(base.push(P_SELECTION_PRESSURE), null);
        this.smooth = state.parameters.getFloat(base.push(P_SMOOTH), null, 0.5);
        this.nIndividuals = 0;
    }

    @Override
    public void processPopulation(EvolutionState state) {
        if (weights == null) {
            for (Subpopulation sub : state.population.subpops) {
                nIndividuals += sub.individuals.length;
            }
            int len = ((float[]) ((VectorBehaviourResult) ((NoveltyFitness) state.population.subpops[0].individuals[0].fitness).getNoveltyBehaviour()).value()).length;
            weights = new float[len];
            Arrays.fill(weights, 1);
            instantCorrelation = new float[weights.length];
            Arrays.fill(instantCorrelation, 0);
            adjustedCorrelation = new float[weights.length];
            Arrays.fill(adjustedCorrelation, 0);
        }
        updateWeights(state, state.population);
        super.processPopulation(state);
    }

    /*
     * Only works if the BehaviourResult's are VectorBehaviourResult's.
     */
    @Override
    protected float distance(BehaviourResult br1, BehaviourResult br2) {
        float[] v1 = (float[]) ((VectorBehaviourResult) br1).value();
        float[] v2 = (float[]) ((VectorBehaviourResult) br2).value();

        float[] w1 = new float[v1.length];
        float[] w2 = new float[v2.length];
        for (int i = 0; i < v1.length; i++) {
            w1[i] = v1[i] * weights[i];
            w2[i] = v2[i] * weights[i];
        }

        float d = ((VectorBehaviourResult) br1).vectorDistance(w1, w2);
        return d;
    }

    /*
     * Correlation is calculated based on the current population, and smoothed with previous weight vector.
     * What behaviour features are relevant for fitness might change throughout evolution.
     */
    protected void updateWeights(EvolutionState state, Population pop) {
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
        } else if (correlation == V_BROWNIAN) {
            for (int i = 0; i < instantCorrelation.length; i++) {
                instantCorrelation[i] = (float) distanceCorrelation(fitnessScores, behaviourFeatures[i]);
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
        } else if (selection == SelectionMethod.normalised) {
            float min = Float.POSITIVE_INFINITY;
            float max = Float.NEGATIVE_INFINITY;
            for (int i = 0; i < weights.length; i++) {
                min = Math.min(min, adjustedCorrelation[i]);
                max = Math.max(max, adjustedCorrelation[i]);
            }
            for (int i = 0; i < weights.length; i++) {
                weights[i] = (adjustedCorrelation[i] - min) / (max - min);
            }
        }
    }

    protected double distanceCorrelation(double[] x, double[] y) {
        double[][] A = distMatrix(x);
        double[][] B = distMatrix(y);
        centreMatrix(A);
        centreMatrix(B);

        double[][] AB = mult(A, B);
        double[][] AA = mult(A, A);
        double[][] BB = mult(B, B);

        double Cxy = FastMath.sqrt(mean(AB));
        double Vx = FastMath.sqrt(mean(AA));
        double Vy = FastMath.sqrt(mean(BB));

        double R = Cxy / FastMath.sqrt(Vx * Vy);
        return R;
    }

    private double mean(double[][] m) {
        double sum = 0;
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m.length; j++) {
                sum += m[i][j];
            }
        }
        return sum / (m.length * m.length);
    }

    private double[][] mult(double[][] A, double[][] B) {
        double[][] res = new double[A.length][A.length];
        for (int i = 0; i < res.length; i++) {
            for (int j = 0; j < res.length; j++) {
                res[i][j] = A[i][j] * B[i][j];
            }
        }
        return res;
    }

    private void centreMatrix(double[][] m) {
        double grandMean = 0;
        double[] colMeans = new double[m.length];
        double[] rowMeans = new double[m.length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m.length; j++) {
                grandMean += m[i][j];
                rowMeans[i] += m[i][j];
                colMeans[j] += m[i][j];
            }
        }
        grandMean /= m.length * m.length;
        for (int i = 0; i < m.length; i++) {
            colMeans[i] /= m.length;
            rowMeans[i] /= m.length;
        }
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m.length; j++) {
                m[i][j] = m[i][j] - rowMeans[i] - colMeans[j] + grandMean;
            }
        }
    }

    private double[][] distMatrix(double[] v) {
        double[][] res = new double[v.length][v.length];
        for (int i = 0; i < v.length; i++) {
            for (int j = 0; j < v.length; j++) {
                res[i][j] = Math.abs(v[i] - v[j]);
            }
        }
        return res;
    }

    protected int makeTournament(float[] weights) {
        int k = (int) selectionPressure;
        int[] players = new int[k];
        for (int i = 0; i < k; i++) {
            players[i] = (int) (Math.random() * weights.length);
        }
        int best = 0;
        for (int i = 1; i < k; i++) {
            if (weights[players[i]] > weights[best]) {
                best = players[i];
            }
        }
        return best;
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
