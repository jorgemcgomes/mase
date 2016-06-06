/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.allocation;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.vector.VectorSpecies;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import mase.SimulationProblem;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.ParamUtils;
import mase.mason.ParamUtils.Param;
import net.jafama.FastMath;
import org.apache.commons.math3.ml.distance.EuclideanDistance;

/**
 *
 * @author jorge
 */
public class AllocationProblem extends SimulationProblem {

    private static final long serialVersionUID = 1L;

    @Param()
    int numAgents;
    @Param()
    int[] types = null;
    @Param()
    int numTypes;
    @Param()
    double power;
    @Param()
    double clusterSize; // max 1
    @Param()
    int numClusters;
    @Param()
    double minSeparation = 0; // sqrt(dimensions)/3 if -1

    private final EuclideanDistance dist = new EuclideanDistance();
    double[][] typesLoc;
    int dimensions = -1;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        ParamUtils.autoSetParameters(this, state.parameters, base, defaultBase(), false);
        
        dimensions = state.parameters.getInt(new Parameter("vector.species.genome-size"), null);

        // calculate types, if not given
        if(types == null) {
            int div = numAgents / numTypes;
            int rem = numAgents % numTypes;
            types = new int[numTypes];
            for(int i = 0 ; i < numTypes ; i++) {
                types[i] = div;
                if(rem > 0) {
                    types[i]++;
                    rem--;
                }
            }
        } else {
            state.output.warning("Overriding numTypes with the given types");
        }
        
        // validate simulation parameters
        int checkSum = 0;
        for (int t : types) {
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
        typesLoc = new double[types.length][];
        if (numClusters == 0) {
            double[] min = new double[dimensions], max = new double[dimensions];
            Arrays.fill(min, 0);
            Arrays.fill(max, 1);
            typesLoc = generateDispersedPoints(rand, types.length, minSeparation, min, max);
        } else {
            double[] min = new double[dimensions], max = new double[dimensions];
            Arrays.fill(min, clusterSize / 2);
            Arrays.fill(max, 1 - clusterSize / 2);
            double[][] centers = generateDispersedPoints(rand, numClusters, minSeparation, min, max);
            int perCluster = types.length / numClusters;
            int remaining = types.length % numClusters;
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
                System.arraycopy(generated, 0, typesLoc, i * perCluster, generated.length);
            }
        }

        // debug: print types
        for (int i = 0; i < typesLoc.length; i++) {
            System.out.println(i + ": " + Arrays.toString(typesLoc[i]));
        }
        System.out.print("  ");
        for (int i = 0; i < typesLoc.length; i++) {
            System.out.printf("%6d", i);
        }
        System.out.println();
        for (int i = 0; i < typesLoc.length; i++) {
            System.out.printf("%2d", i);
            for (int j = 0; j < typesLoc.length; j++) {
                System.out.printf(" %.3f", dist.compute(typesLoc[i], typesLoc[j]));
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
                    if (dist.compute(points[i], candidate) < distThreshold) {
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

        double[][] distanceMatrix = new double[numAgents][typesLoc.length];
        double[][] distanceMatrixT = new double[typesLoc.length][numAgents];
        for (int i = 0; i < numAgents; i++) {
            AllocationAgent aa = (AllocationAgent) acs[i];
            for (int j = 0; j < typesLoc.length; j++) {
                distanceMatrix[i][j] = dist.compute(aa.getType(), typesLoc[j]);
                distanceMatrixT[j][i] = distanceMatrix[i][j];
            }
        }

        double payoff = 0;
        for (int i = 0; i < typesLoc.length; i++) {
            double[] dists = Arrays.copyOf(distanceMatrixT[i], distanceMatrixT[i].length);
            Arrays.sort(dists);
            for (int k = 0; k < types[i] && k < dists.length; k++) {
                payoff += distancePayoff(dists[k]);
            }
        }
        payoff /= numAgents;

        // fitness
        FitnessResult fr = new FitnessResult(payoff);

        // individual characterisation -- distance to each type
        List<EvaluationResult> vbrs = new ArrayList<>();
        for (double[] dists : distanceMatrix) {
            vbrs.add(new VectorBehaviourResult(dists));
        }
        SubpopEvaluationResult ser = new SubpopEvaluationResult(vbrs);

        // group characterisation -- number of agents closer to each type
        int[] closestType = new int[numAgents];
        for (int i = 0; i < numAgents; i++) {
            double minDist = Double.POSITIVE_INFINITY;
            for (int t = 0; t < typesLoc.length; t++) {
                if (distanceMatrix[i][t] < minDist) {
                    minDist = distanceMatrix[i][t];
                    closestType[i] = t;
                }
            }
        }
        double[] bc = new double[typesLoc.length];
        for (int a = 0; a < numAgents; a++) {
            bc[closestType[a]] += 1d / numAgents;
        }
        VectorBehaviourResult gr = new VectorBehaviourResult(bc);
        // alternative group char: min distance between an agent and that type

        return new EvaluationResult[]{fr, gr, ser};
    }

    private double distancePayoff(double dist) {
        double normDist = dist / FastMath.sqrt(dimensions);
        return FastMath.pow(1 - normDist, power);
    }

}
