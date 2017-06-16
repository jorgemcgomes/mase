/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.generic;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import mase.evaluation.BehaviourResult;
import mase.evaluation.EvaluationResult;
import net.jafama.FastMath;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author jorge
 */
public class SpiritResult implements BehaviourResult<float[][]> {

    private static final long serialVersionUID = 1L;

    protected int nSensorStates, nActions;
    protected double distNorm;
    protected double filterThreshold;

    // Intermediate temporary data
    // TODO: more scalable solution using a sparse matrix
    // This is done like this for maximum time efficiency, not space
    protected int[/*sensor index*/][/*action index*/] counts;
    protected Set<Integer> visited;

    // Permanent info kept after merging
    protected int[] visitedSorted;
    protected float[/*index matching visited sorted*/][/*action index*/] frequency;

    // Logging stuff
    protected boolean logging = false;
    protected int[] totalsBeforeFilter;
    protected int grandTotalBeforeFilter;
    protected int visitedBeforeFilter, visitedAfterFilter;

    public SpiritResult(int nSensorStates, int nActions) {
        this.nSensorStates = nSensorStates;
        this.nActions = nActions;
        this.distNorm = FastMath.sqrt(nSensorStates * nActions);
        this.counts = new int[nSensorStates][nActions];
        this.visited = new HashSet<>();
    }

    public void allowLoging(boolean allow) {
        this.logging = allow;
    }

    public void setFilterThreshold(double threshold) {
        this.filterThreshold = threshold;
    }

    public void addEntry(int sensorIndex, int actionIndex) {
        counts[sensorIndex][actionIndex]++;
        visited.add(sensorIndex);
    }

    /**
     * State-count style, based on shared states
     *
     * @param other
     * @return
     */
    @Override
    public double distanceTo(BehaviourResult other) {
        SpiritResult oth = (SpiritResult) other;
        double uniformFreq = 1d / nActions;

        // all that need to be compared
        int thisIdx = 0;
        int otherIdx = 0;
        double diff = 0;
        while (thisIdx < this.visitedSorted.length || otherIdx < oth.visitedSorted.length) {
            // both visited the state
            if (thisIdx < this.visitedSorted.length && otherIdx < oth.visitedSorted.length && this.visitedSorted[thisIdx] == oth.visitedSorted[otherIdx]) {
                for (int a = 0; a < nActions; a++) {
                    diff += FastMath.pow2(this.frequency[thisIdx][a] - oth.frequency[otherIdx][a]);
                }
                thisIdx++;
                otherIdx++;
                // only this visited the state -- the other has reached the end or this is first than the other
            } else if (thisIdx < this.visitedSorted.length && (otherIdx == oth.visitedSorted.length || this.visitedSorted[thisIdx] < oth.visitedSorted[otherIdx])) {
                for (int a = 0; a < nActions; a++) {
                    diff += FastMath.pow2(this.frequency[thisIdx][a] - uniformFreq);
                }
                thisIdx++;
                // only other visited the state    
            } else {
                for (int a = 0; a < nActions; a++) {
                    diff += FastMath.pow2(oth.frequency[otherIdx][a] - uniformFreq);
                }
                otherIdx++;
            }
        }
        return FastMath.sqrt(diff) / distNorm;
        //diff /= (nSensorStates * nActions);
        //return diff;
    }

    @Override
    public float[][] value() {
        return frequency;
    }

    @Override
    public EvaluationResult<float[][]> mergeEvaluations(Collection<EvaluationResult<float[][]>> results) {
        SpiritResult newSR = new SpiritResult(nSensorStates, nActions);

        for (EvaluationResult<float[][]> e : results) {
            SpiritResult sr = (SpiritResult) e;
            // compare matrix sizes
            if (sr.nSensorStates != newSR.nSensorStates || sr.nActions != newSR.nActions) {
                throw new RuntimeException("Trying to merge two SPIRIT results that do not match");
            }

            // add matrixes
            for (int s : sr.visited) {
                for (int a = 0; a < nActions; a++) {
                    newSR.counts[s][a] += sr.counts[s][a];
                }
            }
            newSR.visited.addAll(sr.visited);
        }

        // total visits per sensor state
        int[] totals = new int[nSensorStates];
        int grandTotal = 0;
        for (int s : newSR.visited) {
            for (int t : newSR.counts[s]) {
                totals[s] += t;
                grandTotal += t;
            }
        }

        if (logging) {
            newSR.visitedBeforeFilter = newSR.visited.size();
            newSR.grandTotalBeforeFilter = grandTotal;
            newSR.totalsBeforeFilter = Arrays.copyOf(totals, totals.length);
        }

        // filter
        int countThreshold = (int) Math.round(grandTotal * filterThreshold);
        Iterator<Integer> iter = newSR.visited.iterator();
        while (iter.hasNext()) {
            int s = iter.next();
            if (totals[s] < countThreshold) {
                iter.remove();
            }
        }

        if (logging) {
            newSR.visitedAfterFilter = newSR.visited.size();
        }

        // sort visited states and move to array for efficiency
        newSR.visitedSorted = ArrayUtils.toPrimitive(newSR.visited.toArray(new Integer[newSR.visited.size()]));
        newSR.visited = null;
        Arrays.sort(newSR.visitedSorted);

        // compute frequencies -- only store non-uniform frequencies
        newSR.frequency = new float[newSR.visitedSorted.length][nActions];
        for (int i = 0; i < newSR.visitedSorted.length; i++) {
            for (int a = 0; a < nActions; a++) {
                newSR.frequency[i][a] = newSR.counts[newSR.visitedSorted[i]][a] / (float) totals[newSR.visitedSorted[i]];
            }
        }
        newSR.counts = null;
        return newSR;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String uniformFreq = String.format(Locale.ENGLISH, "%.5f", 1d / nActions);
        int sIdx = 0;
        for (int i = 0; i < nSensorStates; i++) {
            if (i == visitedSorted[sIdx]) {
                for (int a = 0; a < nActions; a++) {
                    sb.append(" ").append(String.format(Locale.ENGLISH, "%.5f", frequency[sIdx][a]));
                }
                sIdx++;
            } else {
                for (int a = 0; a < nActions; a++) {
                    sb.append(" ").append(uniformFreq);
                }
            }
        }
        return sb.toString().trim();
    }

}
