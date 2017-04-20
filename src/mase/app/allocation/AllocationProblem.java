/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.allocation;

import ec.EvolutionState;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import mase.MaseProblem;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.evaluation.CompoundEvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.ParamUtils;
import mase.mason.ParamUtils.Param;
import net.jafama.FastMath;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author jorge
 */
public class AllocationProblem extends MaseProblem {

    private static final long serialVersionUID = 1L;

    @Param()
    int numAgents;
    @Param()
    int[] uniqueTypes = null;
    @Param()
    int numUniqueTypes;
    @Param()
    double clusterSize; // max 1
    @Param()
    int numClusters;
    @Param()
    double minSeparation = 0; // sqrt(dimensions)/3 if -1

    private static final EuclideanDistance DIST = new EuclideanDistance();
    private double[] nulify;
    double[][] types;
    int dimensions = -1;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        ParamUtils.autoSetParameters(this, state, base, defaultBase(), false);
        
        nulify = new double[numAgents];
        Arrays.fill(nulify, Double.NaN);    

        dimensions = state.parameters.getInt(new Parameter("vector.species.genome-size"), null);

        // calculate types, if not given
        if (uniqueTypes == null) {
            int div = numAgents / numUniqueTypes;
            int rem = numAgents % numUniqueTypes;
            uniqueTypes = new int[numUniqueTypes];
            for (int i = 0; i < numUniqueTypes; i++) {
                uniqueTypes[i] = div;
                if (rem > 0) {
                    uniqueTypes[i]++;
                    rem--;
                }
            }
        } else {
            state.output.warning("Overriding numTypes with the given types");
        }

        // validate simulation parameters
        int checkSum = 0;
        for (int t : uniqueTypes) {
            checkSum += t;
        }
        if (checkSum != numAgents) {
            state.output.fatal("Optimal allocation " + checkSum + " does not match the number of agents " + numAgents);
        }
        if (numClusters > 0 && (clusterSize < 0 || clusterSize > 1)) {
            state.output.fatal("Invalid cluster size [0,1]: " + clusterSize);
        }
        if (minSeparation == -1) {
            minSeparation = FastMath.sqrt(dimensions) / 3;
            state.output.warning("Using default min separation sqrt(dimensions)/3: " + minSeparation);
        }

        // randomly create target points
        Random rand = new Random((int) state.job[0]);
        double[][] unique = new double[uniqueTypes.length][];
        if (numClusters == 0) {
            double[] min = new double[dimensions], max = new double[dimensions];
            Arrays.fill(min, 0);
            Arrays.fill(max, 1);
            unique = generateDispersedPoints(rand, uniqueTypes.length, minSeparation, min, max);
        } else {
            double[] min = new double[dimensions], max = new double[dimensions];
            Arrays.fill(min, clusterSize / 2);
            Arrays.fill(max, 1 - clusterSize / 2);
            double[][] centers = generateDispersedPoints(rand, numClusters, minSeparation, min, max);
            int perCluster = uniqueTypes.length / numClusters;
            int remaining = uniqueTypes.length % numClusters;
            for (int i = 0; i < centers.length; i++) {
                for (int j = 0; j < dimensions; j++) {
                    min[j] = centers[i][j] - clusterSize / 2;
                    max[j] = centers[i][j] + clusterSize / 2;
                }
                int size = perCluster;
                if (remaining > 0) {
                    size++;
                    remaining--;
                }
                double[][] generated = generateDispersedPoints(rand, size, 0, min, max);
                System.arraycopy(generated, 0, unique, i * perCluster, generated.length);
            }
        }

        types = new double[numAgents][];
        int index = 0;
        for (int i = 0; i < unique.length; i++) {
            for (int j = 0; j < uniqueTypes[i]; j++) {
                types[index++] = unique[i];
            }
        }

        // debug: print types
        for (int i = 0; i < types.length; i++) {
            System.out.println(i + ": " + Arrays.toString(types[i]));
        }
        System.out.print("  ");
        for (int i = 0; i < types.length; i++) {
            System.out.printf("%6d", i);
        }
        System.out.println();
        for (int i = 0; i < types.length; i++) {
            System.out.printf("%2d", i);
            for (int j = 0; j < types.length; j++) {
                System.out.printf(" %.3f", DIST.compute(types[i], types[j]));
            }
            System.out.println();
        }
    }

    private double[][] generateDispersedPoints(Random rand, int num, double distThreshold, double[] min, double[] max) {
        double[][] points = new double[num][];
        int filled = 0;
        while (filled < num) {
            // generate random
            double[] candidate = new double[dimensions];
            for (int i = 0; i < candidate.length; i++) {
                candidate[i] = min[i] + rand.nextDouble() * (max[i] - min[i]);
            }
            // check if it is not similar to a previous one
            boolean ok = true;
            if (distThreshold > 0) {
                for (int i = 0; i < filled; i++) {
                    if (DIST.compute(points[i], candidate) < distThreshold) {
                        ok = false;
                        break;
                    }
                }
            }
            // add it
            if (ok) {
                points[filled] = candidate;
                filled++;
            }
        }
        return points;
    }

    @Override
    public EvaluationResult[] evaluateSolution(GroupController gc, long seed) {
        AgentController[] acs = gc.getAgentControllers(numAgents);
        RealMatrix distanceMatrix = new Array2DRowRealMatrix(numAgents, types.length);
        for (int i = 0; i < numAgents; i++) {
            AllocationAgent aa = (AllocationAgent) acs[i];
            for (int j = 0; j < types.length; j++) {
                distanceMatrix.setEntry(i, j, DIST.compute(aa.getLocation(), types[j]));
            }
        }

        DescriptiveStatistics pd = pairDistances(distanceMatrix);
        
        // fitness
        FitnessResult fr = new FitnessResult(1 - pd.getMean()  / FastMath.sqrt(dimensions));

        // individual characterisation -- distance to each type
        List<EvaluationResult> vbrs = new ArrayList<>();
        for (double[] dists : distanceMatrix.getData()) {
            vbrs.add(new VectorBehaviourResult(dists));
        }
        CompoundEvaluationResult ser = new CompoundEvaluationResult(vbrs);
        
        // aux characterisation -- min, mean, max, sd pair distances
        VectorBehaviourResult aux = new VectorBehaviourResult(pd.getMin(), pd.getMean(), pd.getMax(), pd.getStandardDeviation());
        
        return new EvaluationResult[]{fr, aux, ser};
    }
    
    private DescriptiveStatistics pairDistances(RealMatrix distMatrix) {
        DescriptiveStatistics ds = new DescriptiveStatistics();
        distMatrix = distMatrix.copy();
  
        for(int k = 0 ; k < numAgents ; k++) {
            // find closest pair
            double min = Double.POSITIVE_INFINITY;
            int minI = -1, minJ = -1;
            for(int i = 0 ; i < numAgents ; i++) {
                for(int j = 0 ; j < numAgents ; j++) {
                    double d = distMatrix.getEntry(i, j);
                    if(!Double.isNaN(d) && d < min) {
                        min = d;
                        minI = i;
                        minJ = j;
                    }
                }
            }
            ds.addValue(min);
            distMatrix.setRow(minI, nulify);
            distMatrix.setColumn(minJ, nulify);
        }
        return ds;
    }
}
