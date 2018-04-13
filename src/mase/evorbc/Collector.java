/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import ec.EvolutionState;
import ec.Individual;
import ec.Statistics;
import ec.util.Parameter;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import mase.evaluation.BehaviourResult;
import mase.evaluation.ExpandedFitness;
import mase.novelty.FinalArchiveStat;
import mase.novelty.NoveltyEvaluation;
import mase.stat.PersistentSolution;
import mase.stat.SolutionPersistence;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.lang3.tuple.Pair;

/**
 * http://ieeexplore.ieee.org/stamp/stamp.jsp?arnumber=7959075
 *
 * @author jorge
 */
public class Collector extends Statistics {

    private static final long serialVersionUID = 1L;

    public static final String P_STAT_FILE = "stat-file";
    public static final String P_TAR_FILE = "tar-file";
    public static final String P_TEXT_FILE = "text-file";
    public static final String P_HISTORY_FILE = "history-file";
    public static final String P_THRESHOLD = "threshold";
    public static final String P_EPSILON = "epsilon";
    public static final String P_DOMINATION = "domination";

    public enum DominationMode {
        epsilon, noveltyepsilon, fitness, novelty, recency, random
    }
    private File textFile, tarFile;
    private int statLog, historyLog;
    // the minimum distance between two individuals 
    // in the collection is actually going to be (1-epsilon)*distThreshold
    private double distThreshold; 
    private boolean headed = false;
    private double epsilon; // 0.1 suggested by Cully
    private int knn;
    protected final Set<CollectionEntry> collection = new HashSet<>();
    protected int behaviourIndex;
    protected DominationMode domination;

    protected class CollectionEntry implements Serializable {

        private static final long serialVersionUID = 1L;
        protected Individual ind;
        protected BehaviourResult br;
        protected double fitness;
        protected int generation;
        protected double novelty;

        private CollectionEntry(Individual ind, int generation) {
            this.ind = ind;
            ExpandedFitness ef = (ExpandedFitness) ind.fitness;
            this.br = (BehaviourResult) ef.getCorrespondingEvaluation(behaviourIndex);
            this.fitness = ef.getFitnessScore();
            this.generation = generation;
            this.novelty = -1;
        }

        private boolean dominates(EvolutionState state, CollectionEntry other) {
            // check if we need the novelty calculated for both entries
            if (domination == DominationMode.epsilon || domination == DominationMode.novelty) {
                // the other one is excluded from the nearest neighbour calculation
                // since the objective is to see if one should replace the other
                // if the "exclusion" is not in the current collection, it has no effect
                if (novelty < 0) {
                    this.calculateNoveltyExcluding(other);
                }
                if (other.novelty < 0) {
                    other.calculateNoveltyExcluding(this);
                }
            }
            switch (domination) {
                case epsilon:
                    return this.novelty >= (1 - epsilon) * other.novelty
                            && this.fitness >= (1 - epsilon) * other.fitness
                            && (this.novelty - other.novelty) * other.fitness >= -(this.fitness - other.fitness) * other.novelty;
                case noveltyepsilon:
                    return this.fitness >= other.fitness && 
                            this.novelty >= (1 - epsilon) * other.novelty;
                case novelty:
                    return this.novelty >= other.novelty;
                case fitness:
                    return this.fitness >= other.fitness;
                case recency:
                    return this.generation >= other.generation;
                case random:
                    return state.random[0].nextBoolean();
                default:
                    throw new RuntimeException("Unknown domination mode: " + domination);
            }
        }

        private void invalidateNovelty() {
            this.novelty = -1;
        }

        private void calculateNoveltyExcluding(CollectionEntry exclude) {
            // average distance to the k-nearest neighbours currently in the collection
            List<Pair<CollectionEntry, Double>> nns = getNearestNeighbours(this, knn + 1);
            int c = 0;
            this.novelty = 0;
            for (Pair<CollectionEntry, Double> nn : nns) {
                if (nn.getLeft() != exclude && nn.getLeft() != this) {
                    this.novelty += nn.getRight();
                    c++;
                    if (c == knn) {
                        break;
                    }
                }
            }
            this.novelty /= c;
        }
    }

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        textFile = state.parameters.getFile(base.push(P_TEXT_FILE), null);
        tarFile = state.parameters.getFile(base.push(P_TAR_FILE), null);
        File statFile = state.parameters.getFile(base.push(P_STAT_FILE), null);
        File historyFile = state.parameters.getFile(base.push(P_HISTORY_FILE), null);
        try {
            statLog = state.output.addLog(statFile, false);
            historyLog = state.output.addLog(historyFile, false);
        } catch (IOException ex) {
            Logger.getLogger(Collector.class.getName()).log(Level.SEVERE, null, ex);
        }
        distThreshold = state.parameters.getDouble(base.push(P_THRESHOLD), null);

        behaviourIndex = state.parameters.getInt(base.push(NoveltyEvaluation.P_BEHAVIOUR_INDEX),
                NoveltyEvaluation.DEFAULT_BASE.push(NoveltyEvaluation.P_BEHAVIOUR_INDEX));
        domination = DominationMode.valueOf(state.parameters.getString(base.push(P_DOMINATION), null));
        epsilon = state.parameters.getDouble(base.push(P_EPSILON), null);
        if (domination == DominationMode.novelty || domination == DominationMode.epsilon) {
            knn = state.parameters.getInt(base.push(NoveltyEvaluation.P_K_NN),
                    NoveltyEvaluation.DEFAULT_BASE.push(NoveltyEvaluation.P_K_NN));
        }
    }

    @Override
    public void postInitializationStatistics(EvolutionState state) {
        super.postInitializationStatistics(state);
        state.output.println("Generation Size MeanFitness Replaced New Discarded", statLog);
    }

    /**
     * Cully's proposal is more complex: check if the individual
     * epsilon-dominates that nearest neighbour. For the epsilon-domination, the
     * NS of each individual must be calculated, with respect to the current
     * collection
     *
     * @param state
     */
    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);
        int replaced = 0;
        int newFills = 0;
        int discarded = 0;
        for (Individual ind : state.population.subpops[0].individuals) {
            CollectionEntry candidate = new CollectionEntry(ind, state.generation);
            // Add the file header to the history log
            if (!headed) {
                state.output.print("Generation NNDistance Direction Fitness Novelty", historyLog);
                for (int i = 0; i < candidate.br.toString().split(" ").length; i++) {
                    state.output.print(" Behav_" + i, historyLog);
                }
                state.output.println("", historyLog);
                headed = true;
            }

            // get the NN and the second closest in the collection
            List<Pair<CollectionEntry, Double>> knns = getNearestNeighbours(candidate, 2);
            CollectionEntry nn = knns.isEmpty() ? null : knns.get(0).getLeft();
            double nnDistance = nn == null ? -1 : knns.get(0).getRight();
            if (nn == null || nnDistance > distThreshold) {
                // if the distance is greater than the threshold, add it to the collection
                addToCollection(candidate);
                state.output.println(state.generation + " " + (knns.isEmpty() ? "NA" : nnDistance)
                        + " new " + candidate.fitness + " " + candidate.novelty + " " + candidate.br, historyLog);
                newFills++;
            } else if(knns.size() == 1) { 
                // there is only one in the archive and the its too close to the candidate
                discarded++;
            } else {
                // the second NN is going to be the new NN if the current NN is replaced by the candidate
                double secondNNDist = knns.get(1).getRight();
                // check if the candidate is not going to be too close to its new NN
                // and if it dominates the current NN
                if(secondNNDist >= (1-epsilon) * distThreshold && candidate.dominates(state, nn)) {
                    // if yes, remove that one from the collection and add the new one         
                    state.output.println(state.generation + " " + nnDistance + " out " + nn.fitness + " " + nn.novelty + " " + nn.br, historyLog);
                    state.output.println(state.generation + " " + nnDistance + " in " + candidate.fitness + " " + candidate.novelty + " " + candidate.br, historyLog);
                    removeFromCollection(nn);
                    addToCollection(candidate);
                    replaced++;
                } else {
                    // the individual adds no value to the collection, discard it
                    discarded++;
                }
            }
        }

        // Statistics
        double meanFit = 0;
        for (CollectionEntry e : collection) {
            meanFit += e.fitness;
        }
        state.output.println(state.generation + " " + collection.size() + " "
                + meanFit / collection.size() + " " + replaced + " " + newFills + " " + discarded, statLog);
        state.output.message("Collection size: " + collection.size() + " Mean quality: " + meanFit / collection.size() + " New: " + newFills + " Replaced: " + replaced);
    }

    @Override
    public void finalStatistics(EvolutionState state, int result) {
        super.finalStatistics(state, result);
        // save the collection of individuals
        try {
            int tarLog = state.output.addLog(tarFile, false);
            TarArchiveOutputStream taos = new TarArchiveOutputStream(new GZIPOutputStream(
                    new BufferedOutputStream(new FileOutputStream(state.output.getLog(tarLog).filename))));
            int textLog = state.output.addLog(textFile, false);

            // text log header
            String sample = collection.iterator().next().br.toString();
            state.output.print("Index OriginGeneration Fitness", textLog);
            for (int i = 0; i < sample.split(" ").length; i++) {
                state.output.print(" Behav_" + i, textLog);
            }
            state.output.println("", textLog);

            int index = 0;
            for (CollectionEntry e : collection) {
                // tar log
                PersistentSolution p = SolutionPersistence.createPersistentController(state, e.ind, 0, index);
                SolutionPersistence.writeSolutionToTar(p, taos);
                // text log
                state.output.println(index + " " + e.generation + " " + e.fitness + " " + e.br.toString(), textLog);
                index++;
            }
            taos.close();
        } catch (IOException ex) {
            Logger.getLogger(FinalArchiveStat.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    protected List<Pair<CollectionEntry, Double>> getNearestNeighbours(CollectionEntry entry, int k) {
        if (collection.isEmpty()) {
            return new ArrayList<>();
        } else {
            ArrayList<Pair<CollectionEntry, Double>> result = new ArrayList<>();
            // calculate distance to entry
            for (CollectionEntry e : collection) {
                if (entry != e) {
                    double d = e.br.distanceTo(entry.br);
                    result.add(Pair.of(e, d));
                }
            }
            // sort by distance to entry
            result.sort(new Comparator<Pair<CollectionEntry, Double>>() {
                @Override
                public int compare(Pair<CollectionEntry, Double> o1, Pair<CollectionEntry, Double> o2) {
                    return Double.compare(o1.getRight(), o2.getRight());
                }
            });
            // return k elements at most
            return result.subList(0, Math.min(result.size(), k));
        }
    }

    protected void addToCollection(CollectionEntry entry) {
        collection.add(entry);
        // since a new entry was added, the novelty scores are no longer valid
        // this could be improved to only invalidate the nearest neighbours...
        for (CollectionEntry e : collection) {
            e.invalidateNovelty();
        }
    }

    protected void removeFromCollection(CollectionEntry oldEntry) {
        collection.remove(oldEntry);
        // since an entry was removed, the novelty scores are no longer valid
        for (CollectionEntry e : collection) {
            e.invalidateNovelty();
        }
    }

}
