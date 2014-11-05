/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.spec;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.Subpopulation;
import ec.simple.SimpleFitness;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import mase.evaluation.BehaviourResult;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author jorge
 */
public class BasicHybridExchanger extends AbstractHybridExchanger {

    public static final String P_MERGE_THRESHOLD = "merge-threshold";
    public static final String P_MERGE_MODE = "merge-mode";
    public static final String P_MERGE_PROPORTION = "merge-proportion";
    public static final String P_STABILITY_TIME = "stability-time";
    public static final String P_ELITE_PORTION = "elite-portion";
    public static final String P_SPLIT_MODE = "split-mode";

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
    PickMode mergeMode;
    MergeProportion mergeProportion;
    SplitMode splitMode;
    int stabilityTime;
    double elitePortion;
    double[][] distanceMatrix;
    int merges, splits, remerges;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        mergeThreshold = state.parameters.getDouble(base.push(P_MERGE_THRESHOLD), null);
        stabilityTime = state.parameters.getInt(base.push(P_STABILITY_TIME), null);
        elitePortion = state.parameters.getDouble(base.push(P_ELITE_PORTION), null);
        mergeMode = PickMode.valueOf(state.parameters.getString(base.push(P_MERGE_MODE), null));
        mergeProportion = MergeProportion.valueOf(state.parameters.getString(base.push(P_MERGE_PROPORTION), null));
        splitMode = SplitMode.valueOf(state.parameters.getString(base.push(P_SPLIT_MODE), null));
    }

    @Override
    protected void importForeignPreBreed(EvolutionState state) {
    }

    @Override
    protected void importForeignPostBreed(EvolutionState state) {
    }

    @Override
    public Population preBreedingExchangePopulation(EvolutionState state) {
        merges = 0;
        splits = 0;
        remerges = 0;
        return super.preBreedingExchangePopulation(state);
    }

    /* Similar to bottom-up (agglomerative) hierarchical clustering */
    @Override
    protected void mergeProcess(EvolutionState state) {
        distanceMatrix = computeMetapopDistances(state);

        Pair<MetaPopulation, MetaPopulation> nextMerge = findNextMerge(distanceMatrix, state);

        // Merge if they are similar
        if (nextMerge != null) {
            state.output.message("*************************** Merging " + nextMerge.getLeft() + " with " + nextMerge.getRight() + " ***************************");
            merges++;
            MetaPopulation mpNew = mergePopulations(nextMerge.getLeft(), nextMerge.getRight(), state);
            metaPops.remove(nextMerge.getLeft());
            metaPops.remove(nextMerge.getRight());
            metaPops.add(mpNew);
        }
    }

    protected double[][] computeMetapopDistances(EvolutionState state) {
        // Retrieve agent behaviours, aggregated by MetaPopulation
        List<BehaviourResult>[] mpBehavs = new List[metaPops.size()];
        for (int i = 0; i < metaPops.size(); i++) {
            MetaPopulation mp = metaPops.get(i);
            Individual[] inds = getElitePortion(mp.inds, (int) Math.ceil(elitePortion * popSize));
            mpBehavs[i] = new ArrayList<BehaviourResult>(mp.agents.size() * inds.length);
            for (Individual ind : inds) {
                for (Integer a : mp.agents) {
                    mpBehavs[i].add(getAgentBR(ind, a));
                }
            }
        }

        // Compute distance matrix
        double[][] dm = distanceMatrix(mpBehavs, state);
        dm = normalisedDistanceMatrix(dm, state);
        return dm;
    }

    protected Pair<MetaPopulation, MetaPopulation> findNextMerge(double[][] distanceMatrix, EvolutionState state) {
        MetaPopulation closeI = null, closeJ = null;
        double closest = 0;
        for (int i = 0; i < metaPops.size(); i++) {
            for (int j = i + 1; j < metaPops.size(); j++) {
                double d = distanceMatrix[i][j];
                if (metaPops.get(i).age >= stabilityTime && metaPops.get(j).age >= stabilityTime && (closeI == null || d < closest)) {
                    closeI = metaPops.get(i);
                    closeJ = metaPops.get(j);
                    closest = d;
                }
            }
        }
        if (closeI != null) {
            state.output.message("Closest: " + closeI + " " + closeJ + " -- " + closest);
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
            mpNew.agents = new ArrayList<Integer>(mp1.agents);
            mpNew.agents.addAll(mp2.agents);
        } else {
            mpNew.agents = new ArrayList<Integer>(mp2.agents);
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
    protected void splitProcess(EvolutionState state) {
        // Find new splits
        MetaPopulation parent = findNextSplit(state);

        // Create new metapop
        if (parent != null) {
            MetaPopulation child = fork(parent, state);
            metaPops.add(child);
            state.output.message("*************************** Spliting " + child + " from " + parent + " ***************************");

        }
    }

    protected MetaPopulation findNextSplit(EvolutionState state) {
        MetaPopulation chosen = null;
        double highestPressure = 0;
        for (MetaPopulation mp : metaPops) {
            if (mp.agents.size() > 1) {
                //double pressure = (mp.agents.size() / (double) nAgents) * mp.age;
                double pressure = mp.age / 2.0 + (mp.age / 2.0) * ((mp.agents.size() - 2.0) / (nAgents - 2.0));
                if (pressure > highestPressure) {
                    chosen = mp;
                    highestPressure = pressure;
                }
            }
        }
        if (chosen != null && highestPressure > stabilityTime) {
            return chosen;
        }
        return null;
    }

    protected MetaPopulation fork(MetaPopulation parent, EvolutionState state) {
        List<Integer> forkAgents = new ArrayList<Integer>();

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

        splits++;
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

    protected double[][] distanceMatrix(List<BehaviourResult>[] behavs, EvolutionState state) {
        double[][] dm = new double[behavs.length][behavs.length];
        for (int i = 0; i < behavs.length; i++) {
            for (int j = 0; j < behavs.length; j++) {
                if (j >= i) {
                    dm[i][j] = pairwiseDistance(behavs[i], behavs[j], state);
                } else {
                    dm[i][j] = dm[j][i];
                }
            }
        }
        return dm;
    }

    protected double[][] normalisedDistanceMatrix(double[][] dm, EvolutionState state) {
        double[][] ndm = new double[dm.length][dm.length];
        for (int i = 0; i < dm.length; i++) {
            for (int j = 0; j < dm.length; j++) {
                ndm[i][j] = dm[i][j] / ((dm[i][i] + dm[j][j]) / 2);
            }
        }
        return ndm;
    }

    protected double pairwiseDistance(List<BehaviourResult> brs1, List<BehaviourResult> brs2, EvolutionState state) {
        // all to all
        int count = 0;
        double total = 0;
        for (BehaviourResult brs11 : brs1) {
            for (BehaviourResult brs21 : brs2) {
                if (brs11 != brs21) {
                    total += brs11.distanceTo(brs21);
                    count++;
                }
            }
        }
        return total / count;
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
            LinkedList<Individual> poolList = new LinkedList<Individual>();
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
            LinkedList<Individual> poolList = new LinkedList<Individual>(Arrays.asList(pool));
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
