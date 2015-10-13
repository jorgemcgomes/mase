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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mase.evaluation.PostEvaluator;
import mase.evaluation.BehaviourResult;
import mase.evaluation.ExpandedFitness;
import mase.evaluation.VectorBehaviourResult;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class NoveltyEvaluation implements PostEvaluator {

    public static final String P_BEHAVIOUR_INDEX = "behaviour-index";
    
    public static final Parameter DEFAULT_BASE = new Parameter("novelty");
    public static final String P_K_NN = "knn";
    public static final String P_ARCHIVE_GROWTH = "archive-growth";
    public static final String P_ARCHIVE_SIZE_LIMIT = "archive-size";
    public static final String P_ARCHIVE_MODE = "archive-mode";
    public static final String P_ARCHIVE_CRITERIA = "archive-criteria";
    public static final String P_KD_TREE = "kd-tree";
    public static final String P_SCORE_NAME = "score-name";
    protected ArchiveMode archiveMode;
    protected ArchiveCriteria archiveCriteria;
    protected int k;
    protected double archiveGrowth;
    protected int sizeLimit;
    protected boolean useKDTree;
    protected int behaviourIndex;
    protected String scoreName;
    protected List<ArchiveEntry>[] archives;
    
    public enum ArchiveMode {

        shared, multiple;
    }

    public enum ArchiveCriteria {

        none, random, novel

    }

    @Override
    public void setup(EvolutionState state, Parameter base) {
        this.k = state.parameters.getInt(base.push(P_K_NN), DEFAULT_BASE.push(P_K_NN));
        this.archiveGrowth = state.parameters.getDouble(base.push(P_ARCHIVE_GROWTH), DEFAULT_BASE.push(P_ARCHIVE_GROWTH));
        this.sizeLimit = state.parameters.getInt(base.push(P_ARCHIVE_SIZE_LIMIT), DEFAULT_BASE.push(P_ARCHIVE_SIZE_LIMIT));
        this.archiveMode = ArchiveMode.valueOf(state.parameters.getString(base.push(P_ARCHIVE_MODE), DEFAULT_BASE.push(P_ARCHIVE_MODE)));
        this.archiveCriteria = ArchiveCriteria.valueOf(state.parameters.getString(base.push(P_ARCHIVE_CRITERIA), DEFAULT_BASE.push(P_ARCHIVE_CRITERIA)));
        this.useKDTree = state.parameters.getBoolean(base.push(P_KD_TREE), DEFAULT_BASE.push(P_KD_TREE), false);
        this.behaviourIndex = state.parameters.getInt(base.push(P_BEHAVIOUR_INDEX), DEFAULT_BASE.push(P_BEHAVIOUR_INDEX));
        this.scoreName = state.parameters.getString(base.push(P_SCORE_NAME), DEFAULT_BASE.push(P_SCORE_NAME));
        
        int nPops = state.parameters.getInt(new Parameter("pop.subpops"), null); // TODO: this should be more flexible?
        this.archives = new ArrayList[nPops];
        if (archiveMode == ArchiveMode.shared) {
            ArrayList<ArchiveEntry> arch = new ArrayList<>();
            for (int i = 0; i < nPops; i++) {
                archives[i] = arch;
            }
        } else if (archiveMode == ArchiveMode.multiple) {
            for (int i = 0; i < nPops; i++) {
                archives[i] = new ArrayList<>();
            }
        }
    }
    
    public int getBehaviourIndex() {
        return behaviourIndex;
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
                ExpandedFitness indFit = (ExpandedFitness) ind.fitness;
                pool.add((BehaviourResult) indFit.getCorrespondingEvaluation(behaviourIndex));
            }
            KNNDistanceCalculator calc = useKDTree && pool.size() >= k * 2 ?
                    new KDTreeCalculator() :
                    new BruteForceCalculator();
            calc.setPool(pool);

            // Calculate novelty for each individual
            for (Individual ind : pop.subpops[p].individuals) {
                ExpandedFitness indFit = (ExpandedFitness) ind.fitness;
                BehaviourResult br = (BehaviourResult) indFit.getCorrespondingEvaluation(behaviourIndex);
                double novScore = calc.getDistance(br, k);
                indFit.scores().put(scoreName, novScore);
                indFit.setFitness(state, novScore, false);
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
            ArrayList<Double> distances = new ArrayList<>();
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
            this.tree = new KDTree<>(vbr.getBehaviour().length);
            for(BehaviourResult br : pool) {
                vbr = (VectorBehaviourResult) br;
                double[] key = vbr.getBehaviour();
                try {
                    if(tree.search(key) == null) {
                        tree.insert(key, vbr);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(NoveltyEvaluation.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
        }

        @Override
        public double getDistance(final BehaviourResult target, int k) {
            try {
                VectorBehaviourResult vbr = (VectorBehaviourResult) target;
                List<VectorBehaviourResult> nearest = tree.nearest(
                        vbr.getBehaviour(), k, new Checker<VectorBehaviourResult>() {
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
            } catch (Exception ex) {
                Logger.getLogger(NoveltyEvaluation.class.getName()).log(Level.SEVERE, null, ex);
            }
            return Double.NaN;
        }
    }

    protected double distance(BehaviourResult br1, BehaviourResult br2) {
        return br1.distanceTo(br2);
    }

    protected void updateArchive(EvolutionState state, Population pop) {
        if (archiveCriteria != ArchiveCriteria.none) {
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
                            double nov1 = ((ExpandedFitness) o1.fitness).getScore(scoreName);
                            double nov2 = ((ExpandedFitness) o2.fitness).getScore(scoreName);
                            return Double.compare(nov2, nov1);
                        }
                    });
                    for (int j = 0; j < archiveGrowth * popInds.length; j++) {
                        toAdd.add(copy[j]);
                    }
                }
                for (Individual ind : toAdd) {
                    ArchiveEntry ar = new ArchiveEntry(state, 
                            (BehaviourResult) ((ExpandedFitness) ind.fitness).getCorrespondingEvaluation(behaviourIndex), 
                            ((ExpandedFitness) ind.fitness).getFitnessScore());
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
        protected int generation;
        protected double fitness;

        protected ArchiveEntry(EvolutionState state, BehaviourResult behaviour, double fitness) {
            this.behaviour = behaviour;
            this.fitness = fitness;
            this.generation = state.generation;
        }

        public BehaviourResult getBehaviour() {
            return behaviour;
        }

        public int getGeneration() {
            return generation;
        }

        public double getFitness() {
            return fitness;
        }
    }

}
