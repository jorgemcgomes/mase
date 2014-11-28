/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.util.Parameter;
import edu.wlu.cs.levy.CG.Checker;
import edu.wlu.cs.levy.CG.KDTree;
import edu.wlu.cs.levy.CG.KeyDuplicateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import mase.PostEvaluator;
import mase.evaluation.BehaviourResult;
import mase.evaluation.VectorBehaviourResult;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class NoveltyEvaluation implements PostEvaluator {

    protected List<ArchiveEntry>[] archives;
    public static final String P_K_NN = "knn";
    public static final String P_ARCHIVE_GROWTH = "archive-growth";
    public static final String P_ARCHIVE_SIZE_LIMIT = "archive-size";
    public static final String P_ARCHIVE_MODE = "archive-mode";
    public static final String P_ARCHIVE_CRITERIA = "archive-criteria";
    public static final String P_KD_TREE = "kd-tree";
    protected ArchiveMode archiveMode;
    protected ArchiveCriteria archiveCriteria;
    protected int k;
    protected double archiveGrowth;
    protected int sizeLimit;
    private KNNDistanceCalculator knnDistance;

    public enum ArchiveMode {

        none, shared, multiple;
    }

    public enum ArchiveCriteria {

        random, novel

    }

    @Override
    public void setup(EvolutionState state, Parameter base) {
        this.k = state.parameters.getInt(base.push(P_K_NN), null);
        this.archiveGrowth = state.parameters.getDouble(base.push(P_ARCHIVE_GROWTH), null);
        this.sizeLimit = state.parameters.getInt(base.push(P_ARCHIVE_SIZE_LIMIT), null);
        this.archiveMode = ArchiveMode.valueOf(state.parameters.getString(base.push(P_ARCHIVE_MODE), null));
        this.archiveCriteria = ArchiveCriteria.valueOf(state.parameters.getString(base.push(P_ARCHIVE_CRITERIA), null));
        boolean kdTree = state.parameters.getBoolean(base.push(P_KD_TREE), null, false);
        knnDistance = kdTree ? new KDTreeCalculator() : new BruteForceCalculator();

        int nPops = state.parameters.getInt(new Parameter("pop.subpops"), null); // TODO: this must be more flexible
        this.archives = new ArrayList[nPops];
        if (archiveMode == ArchiveMode.none) {
            for (int i = 0; i < nPops; i++) {
                archives[i] = new ArrayList<ArchiveEntry>();
            }
        } else if (archiveMode == ArchiveMode.shared) {
            ArrayList<ArchiveEntry> arch = new ArrayList<ArchiveEntry>(sizeLimit);
            for (int i = 0; i < nPops; i++) {
                archives[i] = arch;
            }
        } else if (archiveMode == ArchiveMode.multiple) {
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
            // Set the individuals pool that will be used to compute novelty
            List<ArchiveEntry> archive = archives[p];
            List<BehaviourResult> pool = new ArrayList<BehaviourResult>();
            // Archive
            for (ArchiveEntry e : archive) {
                pool.add(e.getBehaviour());
            }
            // Current population
            for (Individual ind : pop.subpops[p].individuals) {
                NoveltyFitness indFit = (NoveltyFitness) ind.fitness;
                pool.add(indFit.getNoveltyBehaviour());
            }
            knnDistance.setPool(pool);

            // Calculate novelty for each individual
            for (Individual ind : pop.subpops[p].individuals) {
                NoveltyFitness indFit = (NoveltyFitness) ind.fitness;
                BehaviourResult br = indFit.getNoveltyBehaviour();
                indFit.noveltyScore = knnDistance.getDistance(br, k);
                indFit.setFitness(state, (float) indFit.noveltyScore, false);
            }
        }
    }

    public interface KNNDistanceCalculator {

        public void setPool(List<BehaviourResult> pool);

        public double getDistance(BehaviourResult target, int k);

    }

    public class BruteForceCalculator implements KNNDistanceCalculator {

        private List<BehaviourResult> pool;

        @Override
        public void setPool(List<BehaviourResult> pool) {
            this.pool = pool;
        }

        @Override
        public double getDistance(BehaviourResult target, int k) {
            ArrayList<Float> distances = new ArrayList<Float>();
            for (BehaviourResult br : pool) {
                if (target != br) {
                    distances.add(distance(target,br));
                }
            }
            Collections.sort(distances);
            double dist = 0;
            for (int i = 0; i < k; i++) {
                dist += distances.get(i);
            }
            return dist / k;
        }
    }

    public class KDTreeCalculator implements KNNDistanceCalculator {

        private KDTree<VectorBehaviourResult> tree;

        @Override
        public void setPool(List<BehaviourResult> pool) {
            VectorBehaviourResult vbr = (VectorBehaviourResult) pool.get(0);
            this.tree = new KDTree<VectorBehaviourResult>(vbr.getBehaviour().length);
            for(BehaviourResult br : pool) {
                vbr = (VectorBehaviourResult) br;
                tree.insert(toDoubleArray(vbr.getBehaviour()), vbr);
            }
        }

        @Override
        public double getDistance(final BehaviourResult target, int k) {
            VectorBehaviourResult vbr = (VectorBehaviourResult) target;
            List<VectorBehaviourResult> nearest = tree.nearest(
                    toDoubleArray(vbr.getBehaviour()), k, new Checker<VectorBehaviourResult>() {
                @Override
                public boolean usable(VectorBehaviourResult v) {
                    return v != target;
                }
            });
            double dist = 0;
            for(VectorBehaviourResult v : nearest) {
                dist += distance(target,v);
            }
            return dist / k;
        }
        
        private double[] toDoubleArray(float[] a) {
            double[] d = new double[a.length];
            for(int i = 0 ; i < a.length ; i++) {
                d[i] = a[i];
            }
            return d;
        }

    }

    protected float distance(BehaviourResult br1, BehaviourResult br2) {
        return br1.distanceTo(br2);
    }

    protected void updateArchive(EvolutionState state, Population pop) {
        if (archiveMode != ArchiveMode.none) {
            for (int i = 0; i < pop.subpops.length; i++) {
                Individual[] popInds = pop.subpops[i].individuals;
                List<ArchiveEntry> archive = archives[i];
                LinkedHashSet<Individual> toAdd = new LinkedHashSet<Individual>();
                if (archiveCriteria == ArchiveCriteria.random) {
                    while (toAdd.size() < archiveGrowth * popInds.length) {
                        int index = state.random[0].nextInt(popInds.length);
                        toAdd.add(popInds[index]);
                    }
                } else if (archiveCriteria == ArchiveCriteria.novel) {
                    Individual[] copy = Arrays.copyOf(popInds, popInds.length);
                    Arrays.sort(copy, new Comparator<Individual>() {
                        @Override
                        public int compare(Individual o1, Individual o2) {
                            double nov1 = ((NoveltyFitness) o1.fitness).getNoveltyScore();
                            double nov2 = ((NoveltyFitness) o2.fitness).getNoveltyScore();
                            return Double.compare(nov2, nov1);
                        }
                    });
                    for (int j = 0; j < archiveGrowth * popInds.length; j++) {
                        toAdd.add(copy[j]);
                    }
                }
                for (Individual ind : toAdd) {
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

    public List<ArchiveEntry>[] getArchives() {
        return archives;
    }

    public static class ArchiveEntry {

        protected BehaviourResult behaviour;
        protected Individual individual;
        protected int generation;
        protected float fitness;

        protected ArchiveEntry(EvolutionState state, Individual ind) {
            this(state, ind, (BehaviourResult) ((NoveltyFitness) ind.fitness).getNoveltyBehaviour());
        }

        protected ArchiveEntry(EvolutionState state, Individual ind, BehaviourResult behaviour) {
            this.behaviour = behaviour;
            this.fitness = ((NoveltyFitness) ind.fitness).getFitnessScore();
            this.individual = ind;
            this.generation = state.generation;
        }

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
