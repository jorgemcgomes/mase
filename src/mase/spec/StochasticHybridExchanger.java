/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.spec;

import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.simple.SimpleFitness;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import mase.evaluation.BehaviourResult;
import net.jafama.FastMath;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

/**
 *
 * @author jorge
 */
public class StochasticHybridExchanger extends AbstractHybridExchanger {

    // algorithm parameter values
    public static final String P_MERGE_THRESHOLD = "merge-threshold";
    public static final String P_MAX_LOCKDOWN = "max-lockdown";

    // algorithm implementation modes
    public static final String P_ELITE_PORTION = "elite-portion";
    public static final String P_MERGE_MODE = "merge-mode";
    public static final String P_MERGE_PROPORTION = "merge-proportion";
    public static final String P_SPLIT_MODE = "split-mode";
    public static final String P_WEIGHTED_SILHOUETTE = "weighted-silhouette";
    public static final String P_THREADED_CALCULATION = "threaded-calculation";
    
    private static final long serialVersionUID = 1L;

    public enum PickMode {

        elite, probabilistic, random, first
    }

    public enum MergeProportion {

        equal, largest, proportionate

    }

    public enum SplitMode {

        one, half, random

    }

    double mergeThreshold;
    int maxLockdown;

    double elitePortion;
    boolean weightedSilhouette;
    PickMode mergeMode;
    MergeProportion mergeProportion;
    SplitMode splitMode;

    double[][] distanceMatrix;
    ExecutorService executor;
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        mergeThreshold = state.parameters.getDouble(base.push(P_MERGE_THRESHOLD), null);
        maxLockdown = state.parameters.getInt(base.push(P_MAX_LOCKDOWN), null);
        elitePortion = state.parameters.getDouble(base.push(P_ELITE_PORTION), null);
        mergeMode = PickMode.valueOf(state.parameters.getString(base.push(P_MERGE_MODE), null));
        mergeProportion = MergeProportion.valueOf(state.parameters.getString(base.push(P_MERGE_PROPORTION), null));
        splitMode = SplitMode.valueOf(state.parameters.getString(base.push(P_SPLIT_MODE), null));
        weightedSilhouette = state.parameters.getBoolean(base.push(P_WEIGHTED_SILHOUETTE), null, false);
        boolean threadedCalculation = state.parameters.getBoolean(base.push(P_THREADED_CALCULATION), null, false);
        if(threadedCalculation) {
            executor = Executors.newFixedThreadPool(state.evalthreads);            
        }
    }

    /* Similar to bottom-up (agglomerative) hierarchical clustering */
    @Override
    protected int mergeProcess(EvolutionState state) {
        distanceMatrix = computeMetapopDistances(state);

        Pair<MetaPopulation, MetaPopulation> nextMerge = findNextMerge(distanceMatrix, state);

        // Merge if they are similar
        if (nextMerge != null) {
            state.output.message("*** Merging " + nextMerge.getLeft() + " with " + nextMerge.getRight() + " ***");
            MetaPopulation mpNew = mergePopulations(nextMerge.getLeft(), nextMerge.getRight(), state);
            mpNew.lockDown = calculateLockDown(mpNew, state);
            metaPops.remove(nextMerge.getLeft());
            metaPops.remove(nextMerge.getRight());
            metaPops.add(mpNew);
            return 1;
        }
        return 0;
    }

    @Override
    protected void initializationProcess(EvolutionState state) {
        super.initializationProcess(state);
        for (MetaPopulation mp : metaPops) {
            mp.lockDown = calculateLockDown(mp, state);
        }
    }

    protected double[][] computeMetapopDistances(EvolutionState state) {
        // Retrieve agent behaviours, aggregated by MetaPopulation
        List<BehaviourResult> behavs = new ArrayList<>();
        List<Integer>[] alloc = new List[metaPops.size()];
        List<Double> weights = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < metaPops.size(); i++) {
            MetaPopulation mp = metaPops.get(i);
            if (mp.age < mp.lockDown) {
                continue;
            }
            alloc[i] = new ArrayList<>();
            Individual[] inds = getElitePortion(mp.inds, (int) Math.ceil(elitePortion * popSize));
            for (Individual ind : inds) {
                for (Integer a : mp.agents) {
                    behavs.add(getAgentBR(ind, a));
                    weights.add(weightedSilhouette ? ind.fitness.fitness() : 1d);
                    alloc[i].add(index++);
                }
            }
            // normalise weights
            if (weightedSilhouette) {
                double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
                for (int j : alloc[i]) {
                    double f = weights.get(j);
                    min = Math.min(min, f);
                    max = Math.max(max, f);
                }
                for (int j : alloc[i]) {
                    if (Math.abs(max - min) < 0.0001) {
                        weights.set(j, 1d);
                    } else {
                        weights.set(j, FastMath.pow3((weights.get(j) - min) / (max - min)));
                    }
                }
            }
        }

        RealMatrix behavDist = null;
        if(!behavs.isEmpty()) {
            if(executor != null) {
                behavDist = computeDistanceMatrixParallel(behavs);
            } else {
                behavDist = computeDistanceMatrix(behavs);
            }
        }

        double[][] mpDist = new double[metaPops.size()][metaPops.size()];
        for (int i = 0; i < metaPops.size(); i++) {
            for (int j = 0; j < metaPops.size(); j++) {
                MetaPopulation mpi = metaPops.get(i);
                MetaPopulation mpj = metaPops.get(j);
                if (i == j) {
                    mpDist[i][j] = Double.NaN;
                } else if (mpi.age < mpi.lockDown || mpj.age < mpj.lockDown) {
                    mpDist[i][j] = Double.POSITIVE_INFINITY;
                } else if (i < j) {
                    double wi = silhouetteWidth(alloc[i], alloc[j], behavDist, weights, state);
                    double wj = silhouetteWidth(alloc[j], alloc[i], behavDist, weights, state);
                    mpDist[i][j] = (wi + wj) / 2;
                } else {
                    mpDist[i][j] = mpDist[j][i];
                }
            }
        }
        return mpDist;
    }

    private double silhouetteWidth(List<Integer> own, List<Integer> others, RealMatrix distMatrix, List<Double> weights, EvolutionState state) {
        double totalWidth = 0;
        double count = 0;
        for (int i : own) {
            double ai = 0;
            for (int j : own) {
                if (i != j) {
                    ai += distMatrix.getEntry(i, j);
                }
            }
            ai /= (own.size() - 1);

            double bi = 0;
            for (int j : others) {
                bi += distMatrix.getEntry(i, j);
            }
            bi /= others.size();

            double si = (bi - ai) / Math.max(ai, bi);
            double f = weights.get(i);
            totalWidth += (si * f);
            count += f;
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

    protected Pair<MetaPopulation, MetaPopulation> findNextMerge(double[][] distanceMatrix, EvolutionState state) {
        MetaPopulation closeI = null, closeJ = null;
        double closest = 0;
        for (int i = 0; i < metaPops.size(); i++) {
            for (int j = i + 1; j < metaPops.size(); j++) {
                double d = distanceMatrix[i][j];
                if (metaPops.get(i).age >= metaPops.get(i).lockDown && metaPops.get(j).age >= metaPops.get(j).lockDown && (closeI == null || d < closest)) {
                    closeI = metaPops.get(i);
                    closeJ = metaPops.get(j);
                    closest = d;
                }
            }
        }
        if (closeI != null) {
            state.output.message("*** Closest: " + closeI + " " + closeJ + " -- " + closest + " ***");
        }
        if (closeI != null && closest <= mergeThreshold) {
            return Pair.of(closeI, closeJ);
        }
        return null;
    }

    protected MetaPopulation mergePopulations(MetaPopulation mp1, MetaPopulation mp2, EvolutionState state) {
        // Create new MetaPop
        MetaPopulation mpNew = new MetaPopulation();
        if (mp1.age > mp2.age) {
            mpNew.agents = new ArrayList<>(mp1.agents);
            mpNew.agents.addAll(mp2.agents);
        } else {
            mpNew.agents = new ArrayList<>(mp2.agents);
            mpNew.agents.addAll(mp1.agents);
        }

        if (mp1.agents.size() > mp2.agents.size()) {
            mpNew.pop = (Subpopulation) mp1.pop.emptyClone();
        } else {
            mpNew.pop = (Subpopulation) mp2.pop.emptyClone();
        }
        mpNew.inds = new Individual[mp1.inds.length];
        mergeIndividuals(mpNew, mp1, mp2, state);
        return mpNew;
    }

    protected void mergeIndividuals(MetaPopulation mpNew, MetaPopulation mp1, MetaPopulation mp2, EvolutionState state) {
        // The number of individuals to pick from each pop
        int from1 = 0;
        if (mergeProportion == MergeProportion.equal) {
            from1 = mpNew.inds.length / 2;
        } else if (mergeProportion == MergeProportion.largest) {
            from1 = mp1.agents.size() >= mp2.agents.size() ? mpNew.inds.length : 0;
        } else if (mergeProportion == MergeProportion.proportionate) {
            from1 = Math.round((float) mp1.agents.size() / (mp1.agents.size() + mp2.agents.size()) * mpNew.inds.length);
        }
        int from2 = mpNew.inds.length - from1;

        Individual[] picked1 = pickIndividuals(mp1.inds, from1, mergeMode, state);
        Individual[] picked2 = pickIndividuals(mp2.inds, from2, mergeMode, state);
        System.arraycopy(picked1, 0, mpNew.inds, 0, picked1.length);
        System.arraycopy(picked2, 0, mpNew.inds, picked1.length, picked2.length);
    }

    @Override
    protected int splitProcess(EvolutionState state) {
        // Find new splits
        MetaPopulation parent = findNextSplit(state);

        // Create new metapop
        if (parent != null) {
            MetaPopulation child = fork(parent, state);
            metaPops.add(child);
            parent.lockDown = calculateLockDown(parent, state);
            child.lockDown = parent.lockDown; // same lockdown to give them a chance to remerge before any further splits
            state.output.message("*** Spliting " + child + " from " + parent + " ***");
            return 1;
        }
        return 0;
    }

    protected MetaPopulation findNextSplit(EvolutionState state) {
        MetaPopulation chosen = null;
        for (MetaPopulation mp : metaPops) {
            if (mp.agents.size() > 1 && mp.age >= mp.lockDown && (chosen == null || mp.age > chosen.age)) {
                chosen = mp;
            }
        }
        if (chosen != null) {
            return chosen;
        }
        return null;
    }

    protected MetaPopulation fork(MetaPopulation parent, EvolutionState state) {
        List<Integer> forkAgents = new ArrayList<>();

        if (splitMode == SplitMode.one || parent.agents.size() <= 3) {
            forkAgents.add(parent.agents.get(0));
        } else if (splitMode == SplitMode.half) {
            int num = parent.agents.size() / 2;
            for (int i = 0; i < num; i++) {
                forkAgents.add(parent.agents.get(i));
            }
        } else if (splitMode == SplitMode.random) {
            int num = 1 + state.random[0].nextInt(parent.agents.size() / 2 - 1);
            for (int i = 0; i < num; i++) {
                forkAgents.add(parent.agents.get(i));
            }
        }

        parent.age = 0;
        parent.agents.removeAll(forkAgents);

        MetaPopulation child = new MetaPopulation();
        child.agents.addAll(forkAgents);
        child.pop = (Subpopulation) parent.pop.emptyClone();
        child.inds = new Individual[parent.inds.length];
        for (int k = 0; k < parent.inds.length; k++) {
            child.inds[k] = (Individual) parent.inds[k].clone();
        }
        return child;
    }

    protected int calculateLockDown(MetaPopulation pop, EvolutionState state) {
        return state.random[0].nextInt(maxLockdown);
    }

    protected Individual[] pickIndividuals(Individual[] pool, int num, PickMode mode, EvolutionState state) {
        Individual[] picked = new Individual[num];
        if (mode == PickMode.first) {
            System.arraycopy(pool, 0, picked, 0, num);
        } else if (mode == PickMode.elite) {
            Individual[] sorted = sortedCopy(pool);
            System.arraycopy(sorted, 0, picked, 0, num);
        } else if (mode == PickMode.probabilistic) {
            double total = 0;
            LinkedList<Individual> poolList = new LinkedList<>();
            for (Individual ind : pool) {
                poolList.add(ind);
                total += ((SimpleFitness) ind.fitness).fitness();
            }
            int index = 0;
            while (index < num) {
                double accum = 0;
                double rand = state.random[0].nextDouble() * total;
                Iterator<Individual> iter = poolList.iterator();
                while (iter.hasNext()) {
                    Individual ind = iter.next();
                    accum += ((SimpleFitness) ind.fitness).fitness();
                    if (accum >= rand) {
                        picked[index++] = ind;
                        iter.remove();
                        total -= ((SimpleFitness) ind.fitness).fitness();
                        break;
                    }
                }
            }
        } else if (mode == PickMode.random) {
            LinkedList<Individual> poolList = new LinkedList<>(Arrays.asList(pool));
            int index = 0;
            while (index < num) {
                int rand = state.random[0].nextInt(poolList.size());
                picked[index++] = poolList.get(rand);
                poolList.remove(rand);
            }
        } else {
            state.output.fatal("Unknown picking mode: " + mode);
        }
        return picked;
    }
}
