/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.spec;

import ec.EvolutionState;
import ec.Exchanger;
import ec.Individual;
import ec.Population;
import ec.Subpopulation;
import ec.coevolve.MultiPopCoevolutionaryEvaluator2;
import ec.simple.SimpleFitness;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import mase.MetaEvaluator;
import mase.evaluation.BehaviourResult;
import mase.evaluation.EvaluationResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.novelty.NoveltyFitness;

/**
 *
 * @author jorge
 */
public class HybridExchanger extends Exchanger {

    public static final String P_MERGE_THRESHOLD = "merge-threshold";
    public static final String P_MERGE_NORMALISE = "merge-normalise";
    public static final String P_MERGE_MODE = "merge-mode";
    public static final String P_MERGE_EQUAL = "merge-equal";
    public static final String P_SPLIT_THRESHOLD = "split-threshold";
    public static final String P_STABILITY_TIME = "stability-time";
    public static final String P_ELITE_PORTION = "elite-portion";

    double mergeThreshold;
    double splitThreshold;
    int stabilityTime;
    double elitePortion;
    MergeMode mergeMode;
    boolean mergeEqual;
    boolean mergeNormalise;

    int popSize;

    List<MetaPopulation> metaPops;
    List<Integer>[] allocations;

    double[][] distanceMatrix;
    int nAgents;

    public enum MergeMode {

        pickone, elites, probabilistic, random
    }

    @Override
    public void setup(EvolutionState state, Parameter base) {
        mergeThreshold = state.parameters.getDouble(base.push(P_MERGE_THRESHOLD), null);
        splitThreshold = state.parameters.getDouble(base.push(P_SPLIT_THRESHOLD), null);
        stabilityTime = state.parameters.getInt(base.push(P_STABILITY_TIME), null);
        elitePortion = state.parameters.getDouble(base.push(P_ELITE_PORTION), null);
        mergeMode = MergeMode.valueOf(state.parameters.getString(base.push(P_MERGE_MODE), null));
        allocations = new List[state.parameters.getInt(new Parameter("pop.subpops"), null)]; // TODO: bad fix
        mergeEqual = state.parameters.getBoolean(base.push(P_MERGE_EQUAL), null, false);
        mergeNormalise = state.parameters.getBoolean(base.push(P_MERGE_NORMALISE), null, true);
        for (int i = 0; i < allocations.length; i++) {
            allocations[i] = Collections.singletonList(i);
        }
        nAgents = allocations.length;
    }

    @Override
    public Population preBreedingExchangePopulation(EvolutionState state) {
        if (metaPops == null) {
            popSize = state.population.subpops.get(0).individuals.size();
            metaPops = new ArrayList<MetaPopulation>();
            initProcess(state);
        }

        mergeProcess(state);
        splitProcess(state);
        updateRepresentatives(state);

        /* Prepare for breeding */
        Population newPop = (Population) state.population.emptyClone();
        newPop.subpops = new Subpopulation[metaPops.size()];
        for (int i = 0; i < metaPops.size(); i++) {
            newPop.subpops[i] = metaPops.get(i).pop;
        }
        return newPop;
    }

    protected void initProcess(EvolutionState state) {
        for (int i = 0; i < state.population.subpops.size(); i++) {
            MetaPopulation pi = new MetaPopulation();
            pi.agents.add(i);
            pi.pop = state.population.subpops.get(i);
            metaPops.add(pi);
        }
    }

    protected void splitProcess(EvolutionState state) {
        // Find metapop with the highest split pressure
        MetaPopulation chosen = null;
        double highestPressure = 0;
        for (MetaPopulation mp : metaPops) {
            if (mp.agents.size() > 1) {
                double pressure = (mp.agents.size() / (double) nAgents) * mp.age;
                if (pressure > highestPressure) {
                    chosen = mp;
                    highestPressure = pressure;
                }
            }
        }
        if (chosen != null && highestPressure > splitThreshold) {
            int ag = chosen.agents.get(0);
            System.out.println("*** Spliting " + ag + " from " + chosen.toString() + " ***");
            chosen.agents.remove((Object) ag);
            chosen.age = 0;
            MetaPopulation mpNew = new MetaPopulation();
            mpNew.agents.add(ag);
            mpNew.pop = (Subpopulation) chosen.pop.emptyClone();
            for (int k = 0; k < chosen.pop.individuals.size(); k++) {
                mpNew.pop.individuals[k] = (Individual) chosen.pop.individuals[k].clone();
            }
            metaPops.add(mpNew);
        }
    }

    /* Similar to bottom-up (agglomerative) hierarchical clustering */
    protected void mergeProcess(EvolutionState state) {
        // Retrieve agent behaviours, aggregated by MetaPopulation
        List<BehaviourResult>[] mpBehavs = new List[metaPops.size()];
        for (int i = 0; i < metaPops.size(); i++) {
            MetaPopulation mp = metaPops.get(i);
            Individual[] inds = getElitePortion(mp.pop.individuals);
            mpBehavs[i] = new ArrayList<BehaviourResult>(mp.agents.size() * inds.length);
            for (Individual ind : inds) {
                for (Integer a : mp.agents) {
                    mpBehavs[i].add(getAgentBR(ind, a));
                }
            }
        }

        // Compute distance matrix
        distanceMatrix = distanceMatrix(mpBehavs, state);
        double[][] dm = mergeNormalise ? normalisedDistanceMatrix(distanceMatrix, state) : distanceMatrix;

        // Merge the most similar MetaPops
        MetaPopulation closeI = null, closeJ = null;
        double closest = 0;
        for (int i = 0; i < metaPops.size(); i++) {
            for (int j = i + 1; j < metaPops.size(); j++) {
                double d = dm[i][j];
                if (metaPops.get(i).age >= stabilityTime && metaPops.get(j).age >= stabilityTime && (closeI == null || d < closest)) {
                    closeI = metaPops.get(i);
                    closeJ = metaPops.get(j);
                    closest = d;
                }
            }
        }
        System.out.println(closeI + " " + closeJ + " " + closest);

        // Merge if they are similar
        if (closeI != null && closest < mergeThreshold) {
            System.out.println("*** Merging " + closeI + " with " + closeJ + " ***");
            MetaPopulation mpNew = mergePopulations(closeI, closeJ, state);
            metaPops.remove(closeI);
            metaPops.remove(closeJ);
            metaPops.add(mpNew);
        }
    }

    protected Individual[] getElitePortion(Individual[] inds) {
        if (elitePortion == 1) {
            return inds;
        } else {
            int size = (int) Math.ceil(inds.length * elitePortion);
            Individual[] indsCopy = Arrays.copyOf(inds, inds.length);
            Arrays.sort(indsCopy, new FitnessComparator());
            Individual[] elite = new Individual[size];
            System.arraycopy(indsCopy, 0, elite, 0, size);
            return elite;
        }
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

        // The number of individuals to pick from each pop
        int from1 = mergeEqual ? mpNew.pop.individuals.size() / 2 :
                Math.round((float) mp1.agents.size() / (mp1.agents.size() + mp2.agents.size()) * mpNew.pop.individuals.size());
        int from2 = mpNew.pop.individuals.size() - from1;

        if (mergeMode == MergeMode.pickone) {
            if (from1 >= from2) {
                System.arraycopy(mp1.pop.individuals, 0, mpNew.pop.individuals, 0, mp1.pop.individuals.size());
            } else {
                System.arraycopy(mp2.pop.individuals, 0, mpNew.pop.individuals, 0, mp2.pop.individuals.size());
            }
        } else if (mergeMode == MergeMode.elites) {
            int index = 0;
            Arrays.sort(mp1.pop.individuals, new FitnessComparator());
            Arrays.sort(mp2.pop.individuals, new FitnessComparator());
            // Pick the best
            for (int i = 0; i < from1; i++) {
                mpNew.pop.individuals[index++] = (Individual) mp1.pop.individuals.get(i)/*.clone()*/;
            }
            for (int i = 0; i < from2; i++) {
                mpNew.pop.individuals[index++] = (Individual) mp2.pop.individuals.get(i)/*.clone()*/;
            }
        } else if (mergeMode == MergeMode.probabilistic) {
            ArrayList<Individual> picked1 = probabilisticPick(mp1.pop.individuals, from1, state);
            ArrayList<Individual> picked2 = probabilisticPick(mp2.pop.individuals, from2, state);
            picked1.addAll(picked2);
            picked1.toArray(mpNew.pop.individuals);
        } else if (mergeMode == MergeMode.random) {
            ArrayList<Individual> picked1 = randomPick(mp1.pop.individuals, from1, state);
            ArrayList<Individual> picked2 = randomPick(mp2.pop.individuals, from2, state);
            picked1.addAll(picked2);
            picked1.toArray(mpNew.pop.individuals);
        }
        return mpNew;
    }

    private ArrayList<Individual> probabilisticPick(Individual[] pool, int num, EvolutionState state) {
        ArrayList<Individual> picked = new ArrayList<Individual>(num);
        double total = 0;
        LinkedList<Individual> poolList = new LinkedList<Individual>();
        for (Individual ind : pool) {
            poolList.add(ind);
            total += ((SimpleFitness) ind.fitness).fitness();
        }

        while (picked.size() < num) {
            double accum = 0;
            double rand = state.random[0].nextDouble() * total;
            Iterator<Individual> iter = poolList.iterator();
            while (iter.hasNext()) {
                Individual ind = iter.next();
                accum += ((SimpleFitness) ind.fitness).fitness();
                if (accum >= rand) {
                    picked.add(ind);
                    iter.remove();
                    total -= ((SimpleFitness) ind.fitness).fitness();
                    break;
                }
            }
        }
        return picked;
    }

    private ArrayList<Individual> randomPick(Individual[] pool, int num, EvolutionState state) {
        ArrayList<Individual> picked = new ArrayList<Individual>(num);
        LinkedList<Individual> poolList = new LinkedList<Individual>(Arrays.asList(pool));
        while (picked.size() < num) {
            int rand = state.random[0].nextInt(poolList.size());
            picked.add(poolList.get(rand));
            poolList.remove(rand);
        }
        return picked;
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
                //ndm[i][j] = dm[i][j] / Math.max(dm[i][i], dm[j][j]); ALT
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

    private BehaviourResult getAgentBR(Individual ind, int agent) {
        NoveltyFitness nf = (NoveltyFitness) ind.fitness;
        for (EvaluationResult er : nf.getEvaluationResults()) {
            if (er instanceof SubpopEvaluationResult) {
                SubpopEvaluationResult ser = (SubpopEvaluationResult) er;
                return (BehaviourResult) ser.getSubpopEvaluation(agent);
            }
        }
        return null;
    }

    protected void updateRepresentatives(EvolutionState state) {
        MultiPopCoevolutionaryEvaluator2 base = (MultiPopCoevolutionaryEvaluator2) ((MetaEvaluator) state.evaluator).getBaseEvaluator();
        Individual[][] elites = base.getEliteIndividuals();
        Individual[][] newElites = new Individual[metaPops.size()][elites[0].length];
        for (int i = 0; i < metaPops.size(); i++) {
            Individual[] inds = metaPops.get(i).pop.individuals;
            Arrays.sort(inds, new FitnessComparator());
            for (int j = 0; j < newElites[i].length; j++) {
                elites[i][j] = (Individual) inds[j].clone();
            }
        }
    }

    @Override
    public Population postBreedingExchangePopulation(EvolutionState state) {
        // Update pop after breeding
        for (int i = 0; i < metaPops.size(); i++) {
            metaPops.get(i).pop = state.population.subpops.get(i);
        }

        // Update age
        for (MetaPopulation mp : metaPops) {
            mp.age++;
            System.out.println(mp + " - " + mp.age);
        }

        // Update allocations
        allocations = new List[metaPops.size()];
        for (int i = 0; i < metaPops.size(); i++) {
            allocations[i] = metaPops.get(i).agents;
        }

        return state.population;
    }

    public List<Integer>[] getAllocations(EvolutionState state) {
        return allocations;
    }

    @Override
    public String runComplete(EvolutionState state) {
        return null;
    }

    protected class MetaPopulation {

        Subpopulation pop;
        List<Integer> agents;
        int age;

        MetaPopulation() {
            this.agents = new ArrayList<Integer>();
            this.age = 0;
        }

        @Override
        public String toString() {
            String s = "[";
            for (int i = 0; i < agents.size() - 1; i++) {
                s += agents.get(i) + ",";
            }
            if (agents.size() > 0) {
                s += agents.get(agents.size() - 1);
            }
            return s + "]";
        }

    }

    /*
     Uses final scores
     Sorts from the highest fitness to the lowest fitness
     */
    private class FitnessComparator implements Comparator<Individual> {

        @Override
        public int compare(Individual o1, Individual o2) {
            return Float.compare(o2.fitness.fitness(), o1.fitness.fitness());
        }
    }

}
