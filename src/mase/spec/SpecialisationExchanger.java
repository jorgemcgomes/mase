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
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
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
    public static final String P_SIMILARITY_THRESHOLD = "similarity-threshold";
    double[][] distanceMatrix;
    List<MetaPopulation> metaPops;
    double elitePortion;
    double similarityThreshold;
    int subpopN;

    // stats
    int splits;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        elitePortion = state.parameters.getDouble(base.push(P_ELITE_PORTION), null);
        similarityThreshold = state.parameters.getDouble(base.push(P_SIMILARITY_THRESHOLD), null);
    }

    /*
     Merge the populations for breeding
     */
    @Override
    public Population preBreedingExchangePopulation(EvolutionState state) {
        // First generation
        if (metaPops == null) {
            initMetaPopulations(state);
        }

        updateDistanceMatrix(state);
        splitProcess(state);
        mergeProcess(state);
        Population pop = prepareForBreeding(state);
        return pop;
    }

    /*
     Clone the populations for evaluation
     */
    @Override
    public Population postBreedingExchangePopulation(EvolutionState state) {
        String str = "";
        for (MetaPopulation mp : metaPops) {
            str += mp + " ; ";
        }
        state.output.message(str);

        // Prepare for evaluation
        Population newPop = (Population) state.population.emptyClone();
        newPop.subpops = new Subpopulation[subpopN];
        for (int i = 0; i < metaPops.size(); i++) {
            MetaPopulation mp = metaPops.get(i);
            updateMetaPopulationIndividuals(mp, state.population.subpops[i].individuals, state);
            for (Integer p : mp.populations) {
                Subpopulation newSub = (Subpopulation) state.population.subpops[i].emptyClone();
                for (int j = 0; j < newSub.individuals.length; j++) {
                    newSub.individuals[j] = (Individual) mp.individuals[j].clone();
                }
                newPop.subpops[p] = newSub;
            }
        }
        return newPop;
    }

    protected void updateMetaPopulationIndividuals(MetaPopulation mp, Individual[] newInds, EvolutionState state) {
        mp.individuals = newInds;
        if (!mp.waitingIndividuals.isEmpty()) {
            int eachReplace = mp.individuals.length / mp.populations.size();
            int index = mp.individuals.length - 1;
            for (Individual[] candidates : mp.waitingIndividuals) {
                Arrays.sort(candidates, new Comparator<Individual>() {
                    @Override
                    public int compare(Individual o1, Individual o2) {
                        return Float.compare(o2.fitness.fitness(), o1.fitness.fitness());
                    }
                });
                for (int i = 0; i < eachReplace; i++) {
                    mp.individuals[index] = candidates[i];
                    index--;
                }
            }
            mp.waitingIndividuals.clear();
        }
    }

    protected Population prepareForBreeding(EvolutionState state) {
        // Prepare for breeding -- populations = metapopulations
        subpopN = state.population.subpops.length;
        Population newPop = (Population) state.population.emptyClone();
        newPop.subpops = new Subpopulation[metaPops.size()];
        for (int i = 0; i < metaPops.size(); i++) {
            MetaPopulation mp = metaPops.get(i);
            int anySub = mp.populations.get(0);
            Subpopulation newSub = (Subpopulation) state.population.subpops[anySub].emptyClone();
            for (int j = 0; j < newSub.individuals.length; j++) {
                float score = 0;
                for (Integer k : mp.populations) {
                    score += state.population.subpops[k].individuals[j].fitness.fitness();
                }
                Individual newInd = (Individual) state.population.subpops[anySub].individuals[j].clone();
                ((ExpandedFitness) newInd.fitness).setFitness(state, score, false);
                newSub.individuals[j] = newInd;
            }
            newPop.subpops[i] = newSub;
        }
        return newPop;
    }

    protected void splitProcess(EvolutionState state) {
        splits = 0;
        List<MetaPopulation> created = new ArrayList<MetaPopulation>();
        for (MetaPopulation mp : metaPops) {
            if (mp.populations.size() > 1) {
                // Find the biggest distance between populations of the same MetaPopulation
                int maxI = 0, maxJ = 0;
                for (Integer i : mp.populations) {
                    for (Integer j : mp.populations) {
                        if (j > i && distanceMatrix[i][j] > distanceMatrix[maxI][maxJ]) {
                            maxI = i;
                            maxJ = j;
                        }
                    }
                }

                // Check if it needs to be split
                if (distanceMatrix[maxI][maxJ] > similarityThreshold) {
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

                    // Do the split
                    System.out.println("Spliting " + exitPop + " from " + mp.toString());
                    mp.populations.remove((Object) exitPop);

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

    protected void mergeProcess(EvolutionState state) {
        Iterator<MetaPopulation> iter = metaPops.iterator();
        while (iter.hasNext()) {
            MetaPopulation next = iter.next();
            // The population is alone: candidate to merging
            if (next.populations.size() == 1) {
                // Find a metapopulation to merge with
                int subpop = next.populations.get(0);
                MetaPopulation closest = null;
                double distance = Double.POSITIVE_INFINITY;
                for (MetaPopulation mp : metaPops) {
                    if (mp != next) { // can not merge with itself
                        double d = maxDistance(subpop, mp);
                        if (d < similarityThreshold && d < distance) {
                            distance = d;
                            closest = mp;
                        }
                    }
                }

                // One metapop was found, merge with it
                if (closest != null) {
                    iter.remove(); // delete current metapop
                    // add to closest
                    System.out.println("Merging " + subpop + " with " + closest.toString());
                    closest.populations.add(subpop);
                    closest.waitingIndividuals.add(state.population.subpops[subpop].individuals);
                }
            }
        }
    }

    private double maxDistance(int subpop, MetaPopulation mp) {
        double max = Double.NEGATIVE_INFINITY;
        for (Integer s : mp.populations) {
            max = Math.max(distanceMatrix[subpop][s], max);
        }
        return max;
    }

    protected void initMetaPopulations(EvolutionState state) {
        // All initially heterogeneous
        metaPops = new ArrayList<MetaPopulation>();
        for (int i = 0; i < state.population.subpops.length; i++) {
            MetaPopulation pi = new MetaPopulation();
            pi.populations.add(i);
            pi.individuals = state.population.subpops[i].individuals;
            metaPops.add(pi);
        }
    }

    protected void updateDistanceMatrix(EvolutionState state) {
        Subpopulation[] subpops = state.population.subpops;
        distanceMatrix = new double[subpops.length][subpops.length];
        for (int i = 0; i < subpops.length; i++) {
            for (int j = 0; j < subpops.length; j++) {
                if (j == i) {
                    distanceMatrix[i][j] = 0;
                } else if (j > i) {
                    BehaviourResult[] eliteI = getElitePortion(subpops[i].individuals, state);
                    BehaviourResult[] eliteJ = getElitePortion(subpops[j].individuals, state);
                    double sim = distance(eliteI, eliteJ, state);
                    distanceMatrix[i][j] = sim;
                } else {
                    distanceMatrix[i][j] = distanceMatrix[j][i];
                }
            }
        }
    }

    protected BehaviourResult[] getElitePortion(Individual[] inds, EvolutionState state) {
        int size = (int) Math.ceil(inds.length * elitePortion);
        Individual[] indsCopy = Arrays.copyOf(inds, inds.length);
        Arrays.sort(indsCopy, new Comparator<Individual>() {
            @Override
            public int compare(Individual o1, Individual o2) {
                return Float.compare(o2.fitness.fitness(), o1.fitness.fitness());
            }
        });

        BehaviourResult[] brs = new BehaviourResult[size];
        for (int i = 0; i < size; i++) {
            brs[i] = getAgentBR(inds[i]);
        }
        return brs;
    }

    private BehaviourResult getAgentBR(Individual ind) {
        NoveltyFitness nf = (NoveltyFitness) ind.fitness;
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
            for (int j = i; j < brs2.length; j++) {
                total += brs1[i].distanceTo(brs2[j]);
                count++;
            }
        }
        return total / count;
    }

    @Override
    public String runComplete(EvolutionState state) {
        return null;
    }

    protected class MetaPopulation {

        List<Integer> populations;
        Individual[] individuals;
        List<Individual[]> waitingIndividuals;

        MetaPopulation() {
            this.populations = new ArrayList<Integer>();
            this.waitingIndividuals = new ArrayList<Individual[]>();
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
