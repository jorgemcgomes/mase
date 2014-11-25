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
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import mase.MetaEvaluator;
import mase.evaluation.BehaviourResult;
import mase.evaluation.EvaluationResult;
import mase.evaluation.ExpandedFitness;
import mase.evaluation.SubpopEvaluationResult;
import mase.novelty.NoveltyFitness;

/**
 *
 * @author jorge
 */
public class SpecialisationExchanger extends Exchanger {

    public static final String P_ELITE_PORTION = "elite-portion";
    public static final String P_SHARED_REPRESENTATIVES = "shared-representatives";
    public static final String P_HOMOGENEOUS_START = "homogeneous-start";
    public static final String P_MERGE_MODE = "merge-mode";
    public static final String P_STABILITY_THRESHOLD = "stability-threshold";
    public static final String P_MERGE_THRESHOLD = "merge-threshold";
    public static final String P_SPLIT_THRESHOLD = "split-threshold";
    
    double elitePortion;
    double mergeThreshold;
    double splitThreshold;
    int stabilityThreshold;
    boolean sharedReps;
    boolean homogeneousStart;
    MergeMode mergeMode;
    
    public enum MergeMode {
        pickone, elites, partialrandom, fullrandom
    }

    double[][] originalMatrix;
    double[][] distanceMatrix;
    List<MetaPopulation> metaPops;
    List<MetaPopulation> breedMetaPops;
    Subpopulation[] prototypeSubs;
    int popSize;

    // stats
    int splits;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        elitePortion = state.parameters.getDouble(base.push(P_ELITE_PORTION), null);
        mergeThreshold = state.parameters.getDouble(base.push(P_MERGE_THRESHOLD), null);
        splitThreshold = state.parameters.getDouble(base.push(P_SPLIT_THRESHOLD), null);
        stabilityThreshold = state.parameters.getInt(base.push(P_STABILITY_THRESHOLD), null);
        sharedReps = state.parameters.getBoolean(base.push(P_SHARED_REPRESENTATIVES), null, false);
        homogeneousStart = state.parameters.getBoolean(base.push(P_HOMOGENEOUS_START), null, false);
        mergeMode = MergeMode.valueOf(state.parameters.getString(base.push(P_MERGE_MODE), null));
    }

    /*
     Merge the populations for breeding
     */
    @Override
    public Population preBreedingExchangePopulation(EvolutionState state) {
        // First generation initialisation
        if (metaPops == null) {
            popSize = state.population.subpops[0].individuals.length;
            metaPops = new ArrayList<MetaPopulation>();
            breedMetaPops = new ArrayList<MetaPopulation>();
            prototypeSubs = new Subpopulation[state.population.subpops.length];
            for (int i = 0; i < prototypeSubs.length; i++) {
                prototypeSubs[i] = (Subpopulation) state.population.subpops[i].emptyClone();
            }
            initMetaPopulations(state);
        }

        // Update meta-population age
        for (MetaPopulation mp : metaPops) {
            mp.age++;
        }

        updateDistanceMatrix(state);
        splitProcess(state);
        // Scores should not be updated after merge -- the individuals of each subpop can be different
        updateScores(state);
        if (sharedReps) {
            updateRepresentatives(state);
        }
        mergeProcess(state);
        Population pop = prepareForBreeding(state);
        return pop;
    }

    protected void initMetaPopulations(EvolutionState state) {
        if (homogeneousStart) { // Initially homogeneous
            Individual[] inds = new Individual[popSize];
            int drawPop = 0;
            for (int i = 0; i < popSize; i++) {
                int r = state.random[0].nextInt(popSize);
                inds[i] = state.population.subpops[drawPop].individuals[r];
                drawPop = (drawPop + 1) % state.population.subpops.length;
            }
            MetaPopulation mp = new MetaPopulation();
            for (int i = 0; i < state.population.subpops.length; i++) {
                mp.populations.add(i);
            }
            mp.individuals = inds;
            metaPops.add(mp);
        } else { // All initially heterogeneous
            for (int i = 0; i < state.population.subpops.length; i++) {
                MetaPopulation pi = new MetaPopulation();
                pi.populations.add(i);
                pi.individuals = state.population.subpops[i].individuals;
                metaPops.add(pi);
            }
        }

    }

    protected void updateDistanceMatrix(EvolutionState state) {
        Subpopulation[] subpops = state.population.subpops;

        // TEST Covariance Matrix
        /*for (int i = 0; i < subpops.length; i++) {
            BehaviourResult[] elite = getElitePortion(subpops[i].individuals, state);
            RealMatrix m = new Array2DRowRealMatrix(elite.length, ((VectorBehaviourResult) elite[0]).getBehaviour().length);
            for (int j = 0; j < elite.length; j++) {
                VectorBehaviourResult vbr = (VectorBehaviourResult) elite[j];
                double[] v = new double[vbr.getBehaviour().length];
                for (int k = 0; k < v.length; k++) {
                    v[k] = vbr.getBehaviour()[k];
                }
                m.setRow(j, v);
            }
            Covariance cov = new Covariance(m);
            RealMatrix covM = cov.getCovarianceMatrix();
            System.out.println("Sub" + i + " trace: " + covM.getTrace());
        }*/

        originalMatrix = new double[subpops.length][subpops.length];
        for (int i = 0; i < subpops.length; i++) {
            for (int j = 0; j < subpops.length; j++) {
                if (j == i) {
                    BehaviourResult[] elite = getElitePortion(subpops[i].individuals, state);
                    double sim = distance(elite, elite, state);
                    originalMatrix[i][j] = sim;
                } else if (j > i) {
                    BehaviourResult[] eliteI = getElitePortion(subpops[i].individuals, state);
                    BehaviourResult[] eliteJ = getElitePortion(subpops[j].individuals, state);
                    double sim = distance(eliteI, eliteJ, state);
                    originalMatrix[i][j] = sim;
                } else {
                    originalMatrix[i][j] = originalMatrix[j][i];
                }
            }
        }
        
        // TEST self distance
        /*for(int i = 0 ; i < subpops.length ; i++) {
            System.out.println("Sub" +i + " self: " + distanceMatrix[i][i]);
        }*/

        // TEST matrix
        /*for(double[] m : distanceMatrix) {
            state.output.message(Arrays.toString(m));
        }*/
        
        // TEST normalisation
        distanceMatrix = new double[originalMatrix.length][originalMatrix.length];
        for(int i = 0 ; i < originalMatrix.length ; i++) {
            for(int j = 0 ; j < originalMatrix.length ; j++) {
                distanceMatrix[i][j] = originalMatrix[i][j] / ((originalMatrix[i][i] + originalMatrix[j][j]) / 2);
            }            
        }
        /*System.out.println("Normalised");
        for(double[] m : normMatrix) {
            state.output.message(Arrays.toString(m));
        }*/
    }

    protected BehaviourResult[] getElitePortion(Individual[] inds, EvolutionState state) {
        int size = (int) Math.ceil(inds.length * elitePortion);
        Individual[] indsCopy = Arrays.copyOf(inds, inds.length);
        Arrays.sort(indsCopy, new FitnessComparator());
        BehaviourResult[] brs = new BehaviourResult[size];
        for (int i = 0; i < size; i++) {
            brs[i] = getAgentBR(indsCopy[i]);
        }
        return brs;
    }

    private BehaviourResult getAgentBR(Individual ind) {
        NoveltyFitness nf = (NoveltyFitness) ind.fitness;
        /*return nf.getNoveltyBehaviour();*/
        for (EvaluationResult er : nf.getEvaluationResults()) {
            if (er instanceof SubpopEvaluationResult) {
                SubpopEvaluationResult ser = (SubpopEvaluationResult) er;
                return (BehaviourResult) ser.getSubpopEvaluation(nf.getCorrespondingSubpop());
            }
        }
        return null;
    }

    protected double distance(BehaviourResult[] brs1, BehaviourResult[] brs2, EvolutionState state) {
        // all to all
        int count = 0;
        double total = 0;
        for (int i = 0; i < brs1.length; i++) {
            for (int j = 0; j < brs2.length; j++) {
                if (brs1[i] != brs2[j]) {
                    total += brs1[i].distanceTo(brs2[j]);
                    count++;
                }
            }
        }
        return total / count;
    }

    protected void splitProcess(EvolutionState state) {
        splits = 0;
        List<MetaPopulation> created = new ArrayList<MetaPopulation>();
        for (MetaPopulation mp : metaPops) {
            if (mp.populations.size() > 1 && mp.age > stabilityThreshold) {
                // Find the biggest distance between populations of the same MetaPopulation
                int maxI = -1, maxJ = -1;
                for (Integer i : mp.populations) {
                    for (Integer j : mp.populations) {
                        if (j > i && (maxI == -1 || distanceMatrix[i][j] > distanceMatrix[maxI][maxJ])) {
                            maxI = i;
                            maxJ = j;
                        }
                    }
                }

                // Check if it needs to be split
                if (distanceMatrix[maxI][maxJ] > splitThreshold) {
                    // Determine which one will leave the metapopulation
                    int exitPop = maxJ;
                    if (mp.populations.size() > 2) {
                        double maxIdist = 0, maxJdist = 0;
                        for (Integer k : mp.populations) {
                            maxIdist += distanceMatrix[k][maxI];
                            maxJdist += distanceMatrix[k][maxJ];
                        }
                        exitPop = maxIdist > maxJdist ? maxI : maxJ;
                    }

                    // Do the split -- remove subpop from current metapopulation
                    System.out.println("Spliting " + exitPop + " from " + mp.toString());
                    mp.populations.remove((Object) exitPop);
                    mp.age = 0;

                    // Create new metapopulation with subpop and the same individuals as the former metapop
                    MetaPopulation mpj = new MetaPopulation();
                    mpj.individuals = state.population.subpops[exitPop].individuals;
                    mpj.populations.add(exitPop);
                    created.add(mpj);
                    splits++;
                }

            }
        }
        metaPops.addAll(created);
    }

    protected void updateScores(EvolutionState state) {
        for (MetaPopulation mp : metaPops) {
            for (int i = 0; i < mp.individuals.length; i++) {
                ExpandedFitness ef = (ExpandedFitness) mp.individuals[i].fitness;
                float score = 0;
                for (Integer p : mp.populations) {
                    score += state.population.subpops[p].individuals[i].fitness.fitness();
                }
                ef.setFitness(state, score, false);
            }
            Arrays.sort(mp.individuals, new FitnessComparator());
        }
    }

    protected void updateRepresentatives(EvolutionState state) {
        MultiPopCoevolutionaryEvaluator2 base = (MultiPopCoevolutionaryEvaluator2) ((MetaEvaluator) state.evaluator).getBaseEvaluator();
        Individual[][] elites = base.getEliteIndividuals();
        for (MetaPopulation mp : metaPops) {
            for (Integer p : mp.populations) {
                for (int i = 0; i < elites[p].length; i++) {
                    elites[p][i] = (Individual) mp.individuals[i].clone();
                }
            }
        }
    }

    protected void mergeProcess(EvolutionState state) {
        Iterator<MetaPopulation> iter = metaPops.iterator();
        while (iter.hasNext()) {
            MetaPopulation next = iter.next();
            // The population is alone: candidate to merging
            if (next.populations.size() == 1 && next.age > stabilityThreshold) {
                // Find the closest metapopulation to merge with
                int subpop = next.populations.get(0); // get the single subpopulation
                MetaPopulation closest = null;
                double distance = Double.POSITIVE_INFINITY;
                for (MetaPopulation mp : metaPops) {
                    if (mp != next && mp.age > stabilityThreshold) { // can not merge with itself
                        double d = maxDistance(subpop, mp);
                        if (d < mergeThreshold && d < distance) {
                            distance = d;
                            closest = mp;
                        }
                    }
                }

                // One metapop was found, merge with it
                if (closest != null) {
                    iter.remove(); // delete current metapop
                    // Integrate in the closest
                    System.out.println("Merging " + next.toString() + " with " + closest.toString());
                    closest.populations.add(subpop);
                    closest.waitingIndividuals.add(next.individuals);
                    closest.age = 0;
                }
            }
        }
    }
    
    protected double maxDistance(int subpop, MetaPopulation mp) {
        double max = Double.NEGATIVE_INFINITY;
        for (Integer s : mp.populations) {
            max = Math.max(distanceMatrix[subpop][s], max);
        }
        return max;
    }

    protected Population prepareForBreeding(EvolutionState state) {
        // Breed only the MetaPopulations that did not receive merges
        breedMetaPops.clear();
        for (MetaPopulation mp : metaPops) {
            if (mp.waitingIndividuals.isEmpty()) {
                breedMetaPops.add(mp);
            }
        }

        Population newPop = (Population) state.population.emptyClone();
        newPop.subpops = new Subpopulation[breedMetaPops.size()];
        for (int i = 0; i < breedMetaPops.size(); i++) {
            MetaPopulation mp = breedMetaPops.get(i);
            int anySub = mp.populations.get(0);
            Subpopulation newSub = (Subpopulation) prototypeSubs[anySub].emptyClone();
            for (int j = 0; j < mp.individuals.length; j++) {
                Individual newInd = (Individual) mp.individuals[j].clone();
                newSub.individuals[j] = newInd;
            }
            newPop.subpops[i] = newSub;
        }
        return newPop;
    }

    /*
     Clone the populations for evaluation
     */
    @Override
    public Population postBreedingExchangePopulation(EvolutionState state) {
        // Update the individuals of the populations that went through breeding
        for (int i = 0; i < breedMetaPops.size(); i++) {
            MetaPopulation mp = breedMetaPops.get(i);
            mp.individuals = state.population.subpops[i].individuals;
        }

        // Integrate the merges
        for (MetaPopulation mp : metaPops) {
            if (!mp.waitingIndividuals.isEmpty()) {
                integrateMerges(mp, state);
            }
        }

        // Clone populations for evaluation
        Population newPop = (Population) state.population.emptyClone();
        newPop.subpops = new Subpopulation[prototypeSubs.length];
        for (MetaPopulation mp : metaPops) {
            for (Integer p : mp.populations) {
                newPop.subpops[p] = (Subpopulation) prototypeSubs[p].emptyClone();
                for (int j = 0; j < mp.individuals.length; j++) {
                    newPop.subpops[p].individuals[j] = (Individual) mp.individuals[j].clone();
                }
            }
        }
        return newPop;
    }

    protected void integrateMerges(MetaPopulation mp, EvolutionState state) {
        if(mergeMode == MergeMode.elites || mergeMode == MergeMode.partialrandom || mergeMode == MergeMode.fullrandom) {
            int eachReplace = mp.individuals.length / mp.populations.size();
            int index = mp.individuals.length - 1;
                for (Individual[] candidates : mp.waitingIndividuals) {
                    for (int i = 0; i < eachReplace; i++) {
                        if(mergeMode == MergeMode.elites) {
                            mp.individuals[index] = candidates[i];
                        } else if(mergeMode == MergeMode.partialrandom) {
                            int ind = state.random[0].nextInt(candidates.length);
                            mp.individuals[index] = candidates[ind];
                        } else if(mergeMode == MergeMode.fullrandom) {
                            int insider = state.random[0].nextInt(mp.individuals.length);
                            int outsider = state.random[0].nextInt(candidates.length);
                            mp.individuals[insider] = candidates[outsider];
                        }
                        index--;
                    }
                }

        } else if(mergeMode == MergeMode.pickone) {
            // Do nothing -- the waiting individuals are ignored
        }
        mp.waitingIndividuals.clear();
    }

    private class FitnessComparator implements Comparator<Individual> {

        @Override
        public int compare(Individual o1, Individual o2) {
            return Float.compare(o2.fitness.fitness(), o1.fitness.fitness());
        }
    }

    @Override
    public String runComplete(EvolutionState state) {
        return null;
    }

    protected class MetaPopulation {

        List<Integer> populations;
        Individual[] individuals;
        List<Individual[]> waitingIndividuals;
        int age;

        MetaPopulation() {
            this.populations = new ArrayList<Integer>();
            this.waitingIndividuals = new ArrayList<Individual[]>();
            this.age = 0;
        }

        @Override
        public String toString() {
            String s = "[";
            for (int i = 0; i < populations.size() - 1; i++) {
                s += populations.get(i) + ",";
            }
            if (populations.size() > 0) {
                s += populations.get(populations.size() - 1);
            }
            return s + "]";
        }

    }

}
