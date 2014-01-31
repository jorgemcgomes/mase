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
import net.jafama.FastMath;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.attributeSelection.ReliefFAttributeEval;
import weka.clusterers.SimpleKMeans;
import weka.clusterers.XMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Discretize;

/**
 *
 * @author jorge
 */
public class WeightedNovelty extends NoveltyEvaluation {

    public static enum SelectionMethod {

        all, truncation, tournament, roulette, normalised
    };

    public static enum CorrelationMethod {

        pearson, spearman, brownian, cfs, relief, mutualinfo
    }

    public static enum DiscretizationMethod {

        equalwidth, equalfreq, kmeans

    }

    public static final String P_CORRELATION = "correlation";
    public static final String P_SMOOTH = "smooth";
    public static final String P_SELECTION_PRESSURE = "selection-pressure";
    public static final String P_DIMENSION_SELECTION = "selection-method";
    public static final String P_WEIGHTS_ARCHIVE = "weights-archive";
    public static final String P_FITNESS_BINS = "fitness-bins";
    public static final String P_FITNESS_DISC = "fitness-disc";
    protected float[] weights;
    protected float[] instantCorrelation;
    protected float[] adjustedCorrelation;
    protected int nIndividuals;
    protected CorrelationMethod correlation;
    protected float smooth;
    protected SelectionMethod selection;
    protected float selectionPressure;
    protected boolean weightsArchive;
    protected int fitnessBins;
    protected DiscretizationMethod discMethod;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        String corr = state.parameters.getString(base.push(P_CORRELATION), null);
        this.correlation = CorrelationMethod.valueOf(corr);
        String m = state.parameters.getString(base.push(P_DIMENSION_SELECTION), null);
        selection = SelectionMethod.valueOf(m);
        if (selection == null) {
            state.output.fatal("Unknown selection method: " + m, base.push(P_DIMENSION_SELECTION));
        }
        this.selectionPressure = state.parameters.getFloat(base.push(P_SELECTION_PRESSURE), null);
        this.smooth = state.parameters.getFloat(base.push(P_SMOOTH), null);
        this.weightsArchive = state.parameters.getBoolean(base.push(P_WEIGHTS_ARCHIVE), null, false);
        this.nIndividuals = 0;
        if (this.correlation == CorrelationMethod.mutualinfo) {
            this.fitnessBins = state.parameters.getInt(base.push(P_FITNESS_BINS), null);
            this.discMethod = DiscretizationMethod.valueOf(state.parameters.getString(base.push(P_FITNESS_DISC), null));
        }
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
        List<float[]> instances = this.getInstances(state);

        /*
         Numerical correlation
         */
        if (correlation == CorrelationMethod.pearson || correlation == CorrelationMethod.spearman || correlation == CorrelationMethod.brownian) {
            // Assemble data -- transpose
            int indIndex = 0;
            double[][] behaviourFeatures = new double[weights.length + 1][instances.size()];
            for (float[] i : instances) {
                for (int k = 0; k < behaviourFeatures.length; k++) {
                    behaviourFeatures[k][indIndex] = i[k];
                }
                indIndex++;
            }

            // Compute correlation
            if (correlation == CorrelationMethod.pearson) {
                PearsonsCorrelation pearson = new PearsonsCorrelation();
                for (int i = 0; i < instantCorrelation.length; i++) {
                    instantCorrelation[i] = (float) pearson.correlation(behaviourFeatures[0], behaviourFeatures[i + 1]);
                }
            } else if (correlation == CorrelationMethod.spearman) {
                SpearmansCorrelation spearman = new SpearmansCorrelation();
                for (int i = 0; i < instantCorrelation.length; i++) {
                    instantCorrelation[i] = (float) spearman.correlation(behaviourFeatures[0], behaviourFeatures[i + 1]);
                }
            } else if (correlation == CorrelationMethod.brownian) {
                for (int i = 0; i < instantCorrelation.length; i++) {
                    instantCorrelation[i] = (float) distanceCorrelation(behaviourFeatures[0], behaviourFeatures[i + 1]);
                }
            }
            /*
             Weka-based correlation
             */
        } else if (correlation == CorrelationMethod.cfs || correlation == CorrelationMethod.relief || correlation == CorrelationMethod.mutualinfo) {
            /* Assemble data */
            FastVector atts = new FastVector(instantCorrelation.length + 1);
            atts.addElement(new Attribute("Fitness"));
            for (int i = 0; i < instantCorrelation.length; i++) {
                atts.addElement(new Attribute("A" + i));
            }
            Instances data = new Instances("Gen" + state.generation, atts, instances.size());
            data.setClassIndex(0);
            for (float[] i : instances) {
                double[] vals = new double[i.length];
                for (int k = 0; k < i.length; k++) {
                    vals[k] = i[k];
                }
                Instance inst = new Instance(1, vals);
                data.add(inst);
            }

            if (correlation == CorrelationMethod.cfs) {
                CfsSubsetEvalSpearman eval = new CfsSubsetEvalSpearman();
                eval.setLocallyPredictive(false);
                BestFirst search = new BestFirst();
                search.setDirection(new SelectedTag(0, BestFirst.TAGS_SELECTION));
                AttributeSelection as = new AttributeSelection();
                as.setEvaluator(eval);
                as.setSearch(search);
                try {
                    /* Apply the algorithm to the data set */
                    as.SelectAttributes(data);
                    int[] selected = as.selectedAttributes();
                    Arrays.fill(instantCorrelation, 0);
                    for (int i = 0; i < selected.length - 1; i++) {
                        instantCorrelation[selected[i] - 1] = 1;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (correlation == CorrelationMethod.relief) {
                ASEvaluation eval = new ReliefFAttributeEval();
                ASSearch search = new Ranker();
                AttributeSelection as = new AttributeSelection();
                as.setEvaluator(eval);
                as.setSearch(search);
                try {
                    /* Apply the algorithm to the data set */
                    as.SelectAttributes(data);
                    double[][] score = as.rankedAttributes();
                    for (int i = 0; i < score.length; i++) {
                        instantCorrelation[(int) score[i][0] - 1] = (float) Math.max(0, score[i][1]);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (correlation == CorrelationMethod.mutualinfo) {
                try {
                    /*
                     Discretize fitness class
                     */
                    Instances discretized = null;
                    //double[] splits = null; // DEBUG
                    if (discMethod == DiscretizationMethod.equalfreq || discMethod == DiscretizationMethod.equalwidth) {
                        data.setClassIndex(1);
                        Discretize disc = new Discretize();
                        disc.setAttributeIndicesArray(new int[]{0});
                        disc.setInputFormat(data);
                        disc.setBins(fitnessBins);
                        if (discMethod == DiscretizationMethod.equalwidth) {
                            disc.setUseEqualFrequency(false);
                            disc.setFindNumBins(fitnessBins == -1);
                        } else if (discMethod == DiscretizationMethod.equalfreq) {
                            disc.setUseEqualFrequency(true);
                        }
                        discretized = Filter.useFilter(data, disc);
                        discretized.setClassIndex(0);
                        //splits = disc.getCutPoints(0);
                    } else if (discMethod == DiscretizationMethod.kmeans) {
                        FastVector att = new FastVector(1);
                        att.addElement(new Attribute("Fitness"));
                        Instances fitnessData = new Instances("Fit" + state.generation, att, instances.size());
                        for (int i = 0; i < data.numInstances(); i++) {
                            fitnessData.add(new Instance(1, new double[]{data.instance(i).classValue()}));
                        }

                        SimpleKMeans clusterer = new SimpleKMeans();
                        clusterer.setNumClusters(fitnessBins);
                        clusterer.setSeed(state.random[0].nextInt());
                        clusterer.setPreserveInstancesOrder(true);
                        clusterer.buildClusterer(fitnessData);
                        int[] assignements = clusterer.getAssignments();
                        FastVector nominalValues = new FastVector(fitnessBins);
                        for (int i = 0; i < fitnessBins; i++) {
                            nominalValues.addElement("C" + i);
                        }

                        Attribute at = new Attribute("Fitness-disc", nominalValues);
                        data.insertAttributeAt(at, 0);
                        data.setClassIndex(0);
                        //splits = new double[fitnessBins];
                        //Arrays.fill(splits, Double.POSITIVE_INFINITY);
                        //int[] counts = new int[fitnessBins]; // DEBUG
                        for (int i = 0; i < data.numInstances(); i++) {
                            data.instance(i).setClassValue("C" + assignements[i]);
                            //splits[assignements[i]] = Math.min(splits[assignements[i]], data.instance(i).value(1));
                            //counts[assignements[i]]++;
                        }
                        //System.out.println(Arrays.toString(counts));
                        data.deleteAttributeAt(1);
                        discretized = data;
                    }
                    
                    //Arrays.sort(splits);
                    //System.out.println(Arrays.toString(splits));
                    
                    /*
                    Compute mutual information
                    */
                    ASEvaluation eval = new InfoGainAttributeEval();
                    ASSearch search = new Ranker();
                    AttributeSelection as = new AttributeSelection();
                    as.setEvaluator(eval);
                    as.setSearch(search);
                    as.SelectAttributes(discretized);
                    double[][] score = as.rankedAttributes();
                    for (int i = 0; i < score.length; i++) {
                        instantCorrelation[(int) score[i][0] - 1] = (float) score[i][1];
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
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
                weights[i] = selectionPressure == 1 ? adjustedCorrelation[i]
                        : (float) FastMath.pow(adjustedCorrelation[i], selectionPressure);
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
            float max = Float.NEGATIVE_INFINITY;
            for (int i = 0; i < weights.length; i++) {
                max = Math.max(max, adjustedCorrelation[i]);
            }
            for (int i = 0; i < weights.length; i++) {
                weights[i] = adjustedCorrelation[i] / max;
            }
        }
    }

    /*
     First index is the fitness score
     */
    protected List<float[]> getInstances(EvolutionState state) {
        ArrayList<float[]> list = new ArrayList<float[]>(state.population.subpops[0].individuals.length);
        for (Subpopulation sub : state.population.subpops) {
            for (Individual ind : sub.individuals) {
                NoveltyFitness nf = (NoveltyFitness) ind.fitness;
                VectorBehaviourResult vbr = (VectorBehaviourResult) nf.getNoveltyBehaviour();
                float[] v = (float[]) vbr.value();
                float[] f = new float[v.length + 1];
                f[0] = nf.getFitnessScore();
                System.arraycopy(v, 0, f, 1, v.length);
                list.add(f);
            }
        }
        if (weightsArchive) {
            for (ArchiveEntry ae : archives[0]) {
                VectorBehaviourResult vbr = (VectorBehaviourResult) ae.getBehaviour();
                float[] v = (float[]) vbr.value();
                float[] f = new float[v.length + 1];
                f[0] = ae.getFitness();
                System.arraycopy(v, 0, f, 1, v.length);
                list.add(f);
            }
        }
        return list;
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
