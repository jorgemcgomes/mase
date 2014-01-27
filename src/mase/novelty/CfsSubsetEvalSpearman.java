/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty;

/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    CfsSubsetEval.java
 *    Copyright (C) 1999 University of Waikato, Hamilton, New Zealand
 *
 */
import weka.attributeSelection.CfsSubsetEval;

import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.Discretize;

import java.util.BitSet;
import org.apache.commons.math3.stat.ranking.NaturalRanking;

/**
 * <!-- globalinfo-start -->
 * CfsSubsetEval :<br/>
 * <br/>
 * Evaluates the worth of a subset of attributes by considering the individual
 * predictive ability of each feature along with the degree of redundancy
 * between them.<br/>
 * <br/>
 * Subsets of features that are highly correlated with the class while having
 * low intercorrelation are preferred.<br/>
 * <br/>
 * For more information see:<br/>
 * <br/>
 * M. A. Hall (1998). Correlation-based Feature Subset Selection for Machine
 * Learning. Hamilton, New Zealand.
 * <p/>
 * <!-- globalinfo-end -->
 *
 * <!-- technical-bibtex-start -->
 * BibTeX:
 * <pre>
 * &#64;phdthesis{Hall1998,
 *    address = {Hamilton, New Zealand},
 *    author = {M. A. Hall},
 *    school = {University of Waikato},
 *    title = {Correlation-based Feature Subset Selection for Machine Learning},
 *    year = {1998}
 * }
 * </pre>
 * <p/>
 * <!-- technical-bibtex-end -->
 *
 * <!-- options-start -->
 * Valid options are:
 * <p/>
 *
 * <pre> -M
 *  Treat missing values as a separate value.</pre>
 *
 * <pre> -L
 *  Don't include locally predictive attributes.</pre>
 *
 * <!-- options-end -->
 *
 * @author Mark Hall (mhall@cs.waikato.ac.nz)
 * @version $Revision: 6133 $
 * @see Discretize
 */
public class CfsSubsetEvalSpearman extends CfsSubsetEval {

    /**
     * for serialization
     */
    static final long serialVersionUID = 747878400813276317L;

    /**
     * The training instances
     */
    private Instances m_trainInstances;
    /**
     * Discretise attributes when class in nominal
     */
    private Discretize m_disTransform;
    /**
     * The class index
     */
    private int m_classIndex;
    /**
     * Is the class numeric
     */
    private boolean m_isNumeric;
    /**
     * Number of attributes in the training data
     */
    private int m_numAttribs;
    /**
     * Number of instances in the training data
     */
    private int m_numInstances;
    /**
     * Include locally predicitive attributes
     */
    private boolean m_locallyPredictive;
    /**
     * Holds the matrix of attribute correlations
     */
    //  private Matrix m_corr_matrix;
    private float[][] m_corr_matrix;
    /**
     * Standard deviations of attributes (when using pearsons correlation)
     */
    private double[] m_std_devs;
    /**
     * Threshold for admitting locally predictive features
     */
    private double m_c_Threshold;

    private double[][] ranks;

    /**
     * Generates a attribute evaluator. Has to initialize all fields of the
     * evaluator that are not being set via options.
     *
     * CFS also discretises attributes (if necessary) and initializes the
     * correlation matrix.
     *
     * @param data set of instances serving as training data
     * @throws Exception if the evaluator has not been generated successfully
     */
    public void buildEvaluator(Instances data)
            throws Exception {

        // can evaluator handle data?
        getCapabilities().testWithFail(data);

        m_trainInstances = new Instances(data);
        m_trainInstances.deleteWithMissingClass();
        m_classIndex = m_trainInstances.classIndex();
        m_numAttribs = m_trainInstances.numAttributes();
        m_numInstances = m_trainInstances.numInstances();
        m_isNumeric = m_trainInstances.attribute(m_classIndex).isNumeric();

        if (!m_isNumeric) {
            m_disTransform = new Discretize();
            m_disTransform.setUseBetterEncoding(true);
            m_disTransform.setInputFormat(m_trainInstances);
            m_trainInstances = Filter.useFilter(m_trainInstances, m_disTransform);
        }

        m_std_devs = new double[m_numAttribs];
        m_corr_matrix = new float[m_numAttribs][];
        for (int i = 0; i < m_numAttribs; i++) {
            m_corr_matrix[i] = new float[i + 1];
        }

        for (int i = 0; i < m_corr_matrix.length; i++) {
            m_corr_matrix[i][i] = 1.0f;
            m_std_devs[i] = 1.0;
        }

        for (int i = 0; i < m_numAttribs; i++) {
            for (int j = 0; j < m_corr_matrix[i].length - 1; j++) {
                m_corr_matrix[i][j] = -999;
            }
        }
        
        ranks = new double[m_numAttribs][];
        NaturalRanking ranking = new NaturalRanking();
        for(int i = 0 ; i < m_numAttribs ; i++) {
            double[] attrValues = m_trainInstances.attributeToDoubleArray(i);
            ranks[i] = ranking.rank(attrValues);
        }
    }

    /**
     * evaluates a subset of attributes
     *
     * @param subset a bitset representing the attribute subset to be evaluated
     * @return the merit
     * @throws Exception if the subset could not be evaluated
     */
    public double evaluateSubset(BitSet subset)
            throws Exception {
        double num = 0.0;
        double denom = 0.0;
        float corr;
        int larger, smaller;
        // do numerator
        for (int i = 0; i < m_numAttribs; i++) {
            if (i != m_classIndex) {
                if (subset.get(i)) {
                    if (i > m_classIndex) {
                        larger = i;
                        smaller = m_classIndex;
                    } else {
                        smaller = i;
                        larger = m_classIndex;
                    }
                    /*      int larger = (i > m_classIndex ? i : m_classIndex);
                     int smaller = (i > m_classIndex ? m_classIndex : i); */
                    if (m_corr_matrix[larger][smaller] == -999) {
                        corr = correlate(i, m_classIndex);
                        m_corr_matrix[larger][smaller] = corr;
                        num += (m_std_devs[i] * corr);
                    } else {
                        num += (m_std_devs[i] * m_corr_matrix[larger][smaller]);
                    }
                }
            }
        }

        // do denominator
        for (int i = 0; i < m_numAttribs; i++) {
            if (i != m_classIndex) {
                if (subset.get(i)) {
                    denom += (1.0 * m_std_devs[i] * m_std_devs[i]);

                    for (int j = 0; j < m_corr_matrix[i].length - 1; j++) {
                        if (subset.get(j)) {
                            if (m_corr_matrix[i][j] == -999) {
                                corr = correlate(i, j);
                                m_corr_matrix[i][j] = corr;
                                denom += (2.0 * m_std_devs[i] * m_std_devs[j] * corr);
                            } else {
                                denom += (2.0 * m_std_devs[i] * m_std_devs[j] * m_corr_matrix[i][j]);
                            }
                        }
                    }
                }
            }
        }

        if (denom < 0.0) {
            denom *= -1.0;
        }

        if (denom == 0.0) {
            return (0.0);
        }

        double merit = (num / Math.sqrt(denom));

        if (merit < 0.0) {
            merit *= -1.0;
        }

        return merit;
    }

    private float correlate(int att1, int att2) {
        return (float) num_num(att1, att2);
    }

    private double num_num(int att1, int att2) {
        int i;
        double r, diff1, diff2, num = 0.0, sx = 0.0, sy = 0.0;
        double mx = m_trainInstances.meanOrMode(m_trainInstances.attribute(att1));
        double my = m_trainInstances.meanOrMode(m_trainInstances.attribute(att2));

        for (i = 0; i < m_numInstances; i++) {
            diff1 = ranks[att1][i] - mx;
            diff2 = ranks[att2][i] - my;
            num += (diff1 * diff2);
            sx += (diff1 * diff1);
            sy += (diff2 * diff2);
        }

        if (sx != 0.0) {
            if (m_std_devs[att1] == 1.0) {
                m_std_devs[att1] = Math.sqrt((sx / m_numInstances));
            }
        }

        if (sy != 0.0) {
            if (m_std_devs[att2] == 1.0) {
                m_std_devs[att2] = Math.sqrt((sy / m_numInstances));
            }
        }

        if ((sx * sy) > 0.0) {
            r = (num / (Math.sqrt(sx * sy)));
            return ((r < 0.0) ? -r : r);
        } else {
            if (att1 != m_classIndex && att2 != m_classIndex) {
                return 1.0;
            } else {
                return 0.0;
            }
        }
    }

    private void addLocallyPredictive(BitSet best_group) {
        int i, j;
        boolean done = false;
        boolean ok = true;
        double temp_best = -1.0;
        float corr;
        j = 0;
        BitSet temp_group = (BitSet) best_group.clone();
        int larger, smaller;

        while (!done) {
            temp_best = -1.0;

            // find best not already in group
            for (i = 0; i < m_numAttribs; i++) {
                if (i > m_classIndex) {
                    larger = i;
                    smaller = m_classIndex;
                } else {
                    smaller = i;
                    larger = m_classIndex;
                }
                /*      int larger = (i > m_classIndex ? i : m_classIndex);
                 int smaller = (i > m_classIndex ? m_classIndex : i); */
                if ((!temp_group.get(i)) && (i != m_classIndex)) {
                    if (m_corr_matrix[larger][smaller] == -999) {
                        corr = correlate(i, m_classIndex);
                        m_corr_matrix[larger][smaller] = corr;
                    }

                    if (m_corr_matrix[larger][smaller] > temp_best) {
                        temp_best = m_corr_matrix[larger][smaller];
                        j = i;
                    }
                }
            }

            if (temp_best == -1.0) {
                done = true;
            } else {
                ok = true;
                temp_group.set(j);

        // check the best against correlations with others already
                // in group 
                for (i = 0; i < m_numAttribs; i++) {
                    if (i > j) {
                        larger = i;
                        smaller = j;
                    } else {
                        larger = j;
                        smaller = i;
                    }
                    /*  int larger = (i > j ? i : j);
                     int smaller = (i > j ? j : i); */
                    if (best_group.get(i)) {
                        if (m_corr_matrix[larger][smaller] == -999) {
                            corr = correlate(i, j);
                            m_corr_matrix[larger][smaller] = corr;
                        }

                        if (m_corr_matrix[larger][smaller] > temp_best - m_c_Threshold) {
                            ok = false;
                            break;
                        }
                    }
                }

                // if ok then add to best_group
                if (ok) {
                    best_group.set(j);
                }
            }
        }
    }

    /**
     * Calls locallyPredictive in order to include locally predictive attributes
     * (if requested).
     *
     * @param attributeSet the set of attributes found by the search
     * @return a possibly ranked list of postprocessed attributes
     * @throws Exception if postprocessing fails for some reason
     */
    public int[] postProcess(int[] attributeSet)
            throws Exception {
        int j = 0;

        if (!m_locallyPredictive) {
            //      m_trainInstances = new Instances(m_trainInstances,0);
            return attributeSet;
        }

        BitSet bestGroup = new BitSet(m_numAttribs);

        for (int i = 0; i < attributeSet.length; i++) {
            bestGroup.set(attributeSet[i]);
        }

        addLocallyPredictive(bestGroup);

        // count how many are set
        for (int i = 0; i < m_numAttribs; i++) {
            if (bestGroup.get(i)) {
                j++;
            }
        }

        int[] newSet = new int[j];
        j = 0;

        for (int i = 0; i < m_numAttribs; i++) {
            if (bestGroup.get(i)) {
                newSet[j++] = i;
            }
        }

        //    m_trainInstances = new Instances(m_trainInstances,0);
        return newSet;
    }

    protected void resetOptions() {
        m_trainInstances = null;
        m_locallyPredictive = true;
        m_c_Threshold = 0.0;
    }
}
