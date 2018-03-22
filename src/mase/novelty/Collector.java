/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty;

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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import mase.evaluation.BehaviourResult;
import mase.evaluation.ExpandedFitness;
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
    //public static final String P_EPSILON = "epsilon";
    private File textFile, tarFile;
    private int statLog, historyLog;
    private double threshold;
    private boolean headed = false;
    //private double epsilon = 0.1;
    private final List<CollectionEntry> collection = new ArrayList<>();
    private int behaviourIndex;
    // TODO: replace with https://www.programcreek.com/java-api-examples/index.php?source_dir=spark-dbscan-master/src/main/java/indexing/KDTree.java#
    // but needs delete: https://www.cs.cmu.edu/~ckingsf/bioinfo-lectures/kdtrees.pdf

    private class CollectionEntry implements Serializable {

        private static final long serialVersionUID = 1L;

        private final Individual ind;
        private final BehaviourResult br;
        private final double fitness;
        private final int generation;

        private CollectionEntry(Individual ind, int generation) {
            this.ind = ind;
            ExpandedFitness ef = (ExpandedFitness) ind.fitness;
            this.br = (BehaviourResult) ef.getCorrespondingEvaluation(behaviourIndex);
            this.fitness = ef.getFitnessScore();
            this.generation = generation;
        }

        private boolean dominates(CollectionEntry other) {
            return this.fitness > other.fitness;
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
        threshold = state.parameters.getDouble(base.push(P_THRESHOLD), null);
        behaviourIndex = state.parameters.getInt(base.push(NoveltyEvaluation.P_BEHAVIOUR_INDEX),
                NoveltyEvaluation.DEFAULT_BASE.push(NoveltyEvaluation.P_BEHAVIOUR_INDEX));
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
                state.output.print("Generation NNDistance Direction Fitness", historyLog);
                for (int i = 0; i < candidate.br.toString().split(" ").length; i++) {
                    state.output.print(" Behav_" + i, historyLog);
                }
                state.output.println("", historyLog);
                headed = true;
            }

            // get the nearest neighbour in the collection
            Pair<CollectionEntry, Double> nn = getNearestNeighbour(candidate);
            if (nn == null || nn.getRight() > threshold) {
                // if the distance is greater than the threshold, add it to the collection
                addToCollection(candidate);
                state.output.println(state.generation + " " + (nn == null ? "NA" : nn.getRight())
                        + " new " + candidate.fitness + " " + candidate.br, historyLog);
                newFills++;
            } else if (candidate.dominates(nn.getLeft())) {
                // if the distance is less than threshold
                // check if the individual has higher fitness than the nearest neighbour
                // if yes, remove that one from the collection and add the new one         
                replaceInCollection(nn.getLeft(), candidate);
                state.output.println(state.generation + " " + nn.getRight() + " out " + nn.getLeft().fitness + " " + nn.getLeft().br, historyLog);
                state.output.println(state.generation + " NA in " + candidate.fitness + " " + candidate.br, historyLog);
                replaced++;
            } else {
                // The individual adds no value to the collection
                discarded++;
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
            String sample = collection.get(0).br.toString();
            state.output.print("Index OriginGeneration Fitness", textLog);
            for (int i = 0; i < sample.split(" ").length; i++) {
                state.output.print(" Behav_" + i, textLog);
            }
            state.output.println("", textLog);

            for (int x = 0; x < collection.size(); x++) {
                CollectionEntry e = collection.get(x);
                // tar log
                PersistentSolution p = SolutionPersistence.createPersistentController(state, e.ind, 0, x);
                SolutionPersistence.writeSolutionToTar(p, taos);
                // text log
                state.output.println(x + " " + e.generation + " " + e.fitness + " " + e.br.toString(), textLog);
            }
            taos.close();
        } catch (IOException ex) {
            Logger.getLogger(FinalArchiveStat.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private Pair<CollectionEntry, Double> getNearestNeighbour(CollectionEntry entry) {
        if (collection.isEmpty()) {
            return null;
        } else {
            double minDist = Double.POSITIVE_INFINITY;
            CollectionEntry closest = null;
            for (CollectionEntry e : collection) {
                double d = e.br.distanceTo(entry.br);
                if (d < minDist) {
                    minDist = d;
                    closest = e;
                }
            }
            return Pair.of(closest, minDist);
        }
    }

    private void addToCollection(CollectionEntry entry) {
        collection.add(entry);
    }

    private void replaceInCollection(CollectionEntry oldEntry, CollectionEntry newEntry) {
        collection.remove(oldEntry);
        collection.add(newEntry);
    }

}
