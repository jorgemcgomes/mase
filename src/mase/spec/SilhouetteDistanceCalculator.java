/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.spec;

import ec.EvolutionState;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import mase.evaluation.BehaviourResult;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

/**
 *
 * @author jorge
 */
public class SilhouetteDistanceCalculator implements DistanceCalculator {

    public static final String P_THREADED = "threaded";
    private static final long serialVersionUID = 1L;
    private ExecutorService executor;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        boolean threadedCalculation = state.parameters.getBoolean(base.push(P_THREADED), null, false);
        if (threadedCalculation) {
            executor = Executors.newFixedThreadPool(state.evalthreads);
        }
    }

    @Override
    public double[][] computeDistances(List<BehaviourResult>[] list, EvolutionState state) {
        List<BehaviourResult> all = new ArrayList<>();
        int[][] alloc = new int[list.length][];
        int index = 0;

        for (int i = 0; i < list.length; i++) {
            alloc[i] = new int[list[i].size()];
            all.addAll(list[i]);
            for (int j = 0; j < list[i].size(); j++) {
                alloc[i][j] = index++;
            }
        }

        RealMatrix behavDist = null;
        if (!all.isEmpty()) {
            if (executor != null) {
                behavDist = computeDistanceMatrixParallel(all);
            } else {
                behavDist = computeDistanceMatrix(all);
            }
        }

        double[][] mpDist = new double[list.length][list.length];
        for (int i = 0; i < mpDist.length; i++) {
            for (int j = 0; j < mpDist.length; j++) {
                if (i == j) {
                    mpDist[i][j] = Double.NaN;
                } else if (list[i].isEmpty() || list[j].isEmpty()) {
                    mpDist[i][j] = Double.POSITIVE_INFINITY;
                } else if (i < j) {
                    double wi = silhouetteWidth(alloc[i], alloc[j], behavDist, state);
                    double wj = silhouetteWidth(alloc[j], alloc[i], behavDist, state);
                    mpDist[i][j] = (wi + wj) / 2;
                } else {
                    mpDist[i][j] = mpDist[j][i];
                }
            }
        }
        return mpDist;
    }

    private double silhouetteWidth(int[] own, int[] others, RealMatrix distMatrix, EvolutionState state) {
        double totalWidth = 0;
        int count = 0;
        for (int i : own) {
            double ai = 0;
            for (int j : own) {
                if (i != j) {
                    ai += distMatrix.getEntry(i, j);
                }
            }
            ai /= (own.length - 1);

            double bi = 0;
            for (int j : others) {
                bi += distMatrix.getEntry(i, j);
            }
            bi /= others.length;

            double si = (bi - ai) / Math.max(ai, bi);
            totalWidth += si;
            count++;
        }

        return totalWidth / count;
    }

    private RealMatrix computeDistanceMatrix(List<BehaviourResult> brs) {
        RealMatrix mat = new BlockRealMatrix(brs.size(), brs.size());
        for (int i = 0; i < brs.size(); i++) {
            for (int j = i + 1; j < brs.size(); j++) {
                double d = brs.get(i).distanceTo(brs.get(j));
                mat.setEntry(i, j, d);
                mat.setEntry(j, i, d);
            }
        }
        return mat;
    }

    private RealMatrix computeDistanceMatrixParallel(List<BehaviourResult> brs) {
        RealMatrix mat = new BlockRealMatrix(brs.size(), brs.size());
        Collection<Callable<Object>> div = new ArrayList<>();
        for (int i = 0; i < brs.size(); i++) {
            div.add(new DistanceMatrixCalculator(mat, brs, i, i));
        }
        try {
            executor.invokeAll(div);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        return mat;
    }

    private static class DistanceMatrixCalculator implements Callable<Object> {

        private final RealMatrix matrix;
        private final List<BehaviourResult> brs;
        private final int fromRow, toRow;

        DistanceMatrixCalculator(RealMatrix matrix, List<BehaviourResult> brs, int fromRow, int toRow) {
            this.matrix = matrix;
            this.brs = brs;
            this.fromRow = fromRow;
            this.toRow = toRow;
        }

        @Override
        public Object call() {
            for (int i = fromRow; i <= toRow; i++) {
                for (int j = i + 1; j < brs.size(); j++) {
                    double d = brs.get(i).distanceTo(brs.get(j));
                    matrix.setEntry(i, j, d);
                    matrix.setEntry(j, i, d);
                }
            }
            return null;
        }

    }

}
