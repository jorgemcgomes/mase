/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import mase.evaluation.BehaviourResult;
import mase.PostEvaluator;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class NoveltyEvaluation implements PostEvaluator {

    protected List<ArchiveEntry>[] archives;
    public static final String P_K_NN = "ns-k";
    public static final String P_ARCHIVE_ADD_PROB = "ns-archive-prob";
    public static final String P_ARCHIVE_SIZE_LIMIT = "ns-archive-size";
    public static final String P_ARCHIVE_MODE = "ns-archive-mode";
    public static final String P_USE_CURRENT_POP = "ns-use-current";
    public static final String V_NONE = "none", V_SHARED = "shared", V_MULTIPLE = "multiple";
    protected String archiveMode;
    protected int k;
    protected double addProb;
    protected int sizeLimit;
    protected boolean useCurrent;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        this.k = state.parameters.getInt(base.push(P_K_NN), null);
        this.addProb = state.parameters.getDouble(base.push(P_ARCHIVE_ADD_PROB), null);
        this.sizeLimit = state.parameters.getInt(base.push(P_ARCHIVE_SIZE_LIMIT), null);
        this.useCurrent = state.parameters.getBoolean(base.push(P_USE_CURRENT_POP), null, true);
        String m = state.parameters.getStringWithDefault(base.push(P_ARCHIVE_MODE), null, V_SHARED);
        if (m.equalsIgnoreCase(V_NONE)) {
            archiveMode = V_NONE;
        } else if (m.equalsIgnoreCase(V_SHARED)) {
            archiveMode = V_SHARED;
        } else if (m.equalsIgnoreCase(V_MULTIPLE)) {
            archiveMode = V_MULTIPLE;
        } else {
            state.output.fatal("Unknown archive mode", base.push(P_ARCHIVE_MODE));
        }

        int nPops = state.parameters.getInt(new Parameter("pop.subpops"), null); // TODO: this must be more flexible
        this.archives = new ArrayList[nPops];
        if (archiveMode == V_NONE) {
            for (int i = 0; i < nPops; i++) {
                archives[i] = new ArrayList<ArchiveEntry>();
            }
        } else if (archiveMode == V_SHARED) {
            ArrayList<ArchiveEntry> arch = new ArrayList<ArchiveEntry>(sizeLimit);
            for (int i = 0; i < nPops; i++) {
                archives[i] = arch;
            }
        } else if (archiveMode == V_MULTIPLE) {
            for (int i = 0; i < nPops; i++) {
                archives[i] = new ArrayList<ArchiveEntry>(sizeLimit);
            }
        }
    }

    @Override
    public void processPopulation(EvolutionState state) {
        // calculate novelty
        setNoveltyScores(state, state.population);
        // update archive
        updateArchive(state, state.population);
    }

    protected void setNoveltyScores(EvolutionState state, Population pop) {
        for (int p = 0; p < pop.subpops.length; p++) {
            List<ArchiveEntry> archive = archives[p];
            // calculate novelty scores
            for (int j = 0; j < pop.subpops[p].individuals.length; j++) {
                Individual ind = pop.subpops[p].individuals[j];
                NoveltyFitness indFit = (NoveltyFitness) ind.fitness;

                ArrayList<Pair<Float, Boolean>> distances
                        = new ArrayList<Pair<Float, Boolean>>(archive.size() + pop.subpops[p].individuals.length);

                // from subpop
                if (useCurrent) {
                    for (Individual i : pop.subpops[p].individuals) {
                        if (ind != i) {
                            BehaviourResult er1 = ((NoveltyFitness) i.fitness).getNoveltyBehaviour();
                            BehaviourResult er2 = indFit.getNoveltyBehaviour();
                            distances.add(Pair.of(distance(er1, er2), false));
                        }
                    }
                }

                // from repo
                for (ArchiveEntry ar : archive) {
                    BehaviourResult erInd = (BehaviourResult) indFit.getNoveltyBehaviour();
                    distances.add(Pair.of(distance(ar.getBehaviour(), erInd), true));
                }

                // sort the distances
                Collections.sort(distances, new Comparator<Pair<Float, Boolean>>() {
                    @Override
                    public int compare(Pair<Float, Boolean> o1, Pair<Float, Boolean> o2) {
                        return Float.compare(o1.getLeft(), o2.getLeft());
                    }
                });

                // average to k nearest
                indFit.noveltyScore = 0;
                indFit.repoComparisons = 0;
                for (int i = 0; i < k && i < distances.size() ; i++) {
                    indFit.noveltyScore += distances.get(i).getLeft();
                    if (distances.get(i).getRight()) {
                        indFit.repoComparisons++;
                    }
                }
                indFit.noveltyScore /= k;
                indFit.setFitness(state, (float) indFit.noveltyScore, false);
            }
        }
    }

    protected float distance(BehaviourResult br1, BehaviourResult br2) {
        return br1.distanceTo(br2);
    }

    protected void updateArchive(EvolutionState state, Population pop) {
        if (archiveMode != V_NONE) {
            for (int i = 0; i < pop.subpops.length; i++) {
                List<ArchiveEntry> archive = archives[i];
                for (int j = 0; j < pop.subpops[i].individuals.length; j++) {
                    Individual ind = pop.subpops[i].individuals[j];
                    if (state.random[0].nextDouble() < addProb) {
                        ArchiveEntry ar = new ArchiveEntry(state, ind);
                        if (archive.size() == sizeLimit) {
                            int index = state.random[0].nextInt(archive.size());
                            archive.set(index, ar);
                        } else {
                            archive.add(ar);
                        }
                    }
                }
            }
        }
    }

    public List<ArchiveEntry>[] getArchives() {
        return archives;
    }
    
    public static class ArchiveEntry {
        
        protected ArchiveEntry(EvolutionState state, Individual ind) {
            this.behaviour = (BehaviourResult) ((NoveltyFitness) ind.fitness).getNoveltyBehaviour();
            this.fitness = ((NoveltyFitness) ind.fitness).getFitnessScore();
            this.individual = ind;
            this.generation = state.generation;
        }
        
        protected BehaviourResult behaviour;
        protected Individual individual;
        protected int generation;
        protected float fitness;

        public BehaviourResult getBehaviour() {
            return behaviour;
        }

        public Individual getIndividual() {
            return individual;
        }

        public int getGeneration() {
            return generation;
        }
        
        public float getFitness() {
            return fitness;
        }
    }
}
