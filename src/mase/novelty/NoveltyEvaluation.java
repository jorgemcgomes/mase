/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.util.Parameter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import mase.evaluation.PostEvaluator;
import mase.evaluation.BehaviourResult;
import mase.evaluation.ExpandedFitness;
import mase.evaluation.VectorBehaviourResult;
import mase.util.KdTree;
import net.jafama.FastMath;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class NoveltyEvaluation implements PostEvaluator {

    private static final long serialVersionUID = 1L;

    public static final Parameter DEFAULT_BASE = new Parameter("novelty");
    public static final String LC_SCORE = "lc";
    public static final String P_BEHAVIOUR_INDEX = "behaviour-index";
    public static final String P_K_NN = "knn";
    public static final String P_ARCHIVE_GROWTH = "archive-growth";
    public static final String P_ARCHIVE_SIZE_LIMIT = "archive-size";
    public static final String P_ARCHIVE_MODE = "archive-mode";
    public static final String P_ARCHIVE_CRITERIA = "archive-criteria";
    public static final String P_REMOVAL_CRITERIA = "removal-criteria";
    public static final String P_KD_TREE = "kd-tree";
    public static final String P_SCORE_NAME = "score-name";
    public static final String P_THREADED = "threaded";
    public static final String P_NSLC = "nslc";
    protected ArchiveMode archiveMode;
    protected ArchiveCriteria archiveCriteria;
    protected ArchiveCriteria removalCriteria;
    protected int k;
    protected double archiveGrowth;
    protected int sizeLimit;
    protected boolean useKDTree;
    protected int behaviourIndex;
    protected String scoreName;
    protected List<ArchiveEntry>[] archives;
    protected boolean threaded;
    protected boolean nslc;

    private transient ExecutorService executor;

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
        this.removalCriteria = ArchiveCriteria.valueOf(state.parameters.getString(base.push(P_REMOVAL_CRITERIA), DEFAULT_BASE.push(P_REMOVAL_CRITERIA)));
        this.useKDTree = state.parameters.getBoolean(base.push(P_KD_TREE), DEFAULT_BASE.push(P_KD_TREE), true);
        this.behaviourIndex = state.parameters.getInt(base.push(P_BEHAVIOUR_INDEX), DEFAULT_BASE.push(P_BEHAVIOUR_INDEX));
        this.scoreName = state.parameters.getString(base.push(P_SCORE_NAME), DEFAULT_BASE.push(P_SCORE_NAME));
        this.threaded = state.parameters.getBoolean(base.push(P_THREADED), DEFAULT_BASE.push(P_THREADED), true);
        this.nslc = state.parameters.getBoolean(base.push(P_NSLC), DEFAULT_BASE.push(P_NSLC), false);

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

        if (threaded) {
            executor = Executors.newFixedThreadPool(state.evalthreads);
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
        for (int p = 0; p < pop.subpops.size(); p++) {
            // Set the individuals pool that will be used to compute novelty
            List<ArchiveEntry> archive = archives[p];
            List<BehaviourResult> pool = new ArrayList<>();
            Map<BehaviourResult, Double> fitnessMap = new HashMap<>();

            // Archive
            for (ArchiveEntry e : archive) {
                pool.add(e.getBehaviour());
                if (nslc) {
                    fitnessMap.put(e.getBehaviour(), e.getFitness());
                }
            }
            // Current population
            for (Individual ind : pop.subpops.get(p).individuals) {
                ExpandedFitness indFit = (ExpandedFitness) ind.fitness;
                BehaviourResult br = (BehaviourResult) indFit.getCorrespondingEvaluation(behaviourIndex);
                pool.add(br);
                if (nslc) {
                    fitnessMap.put(br, indFit.getFitnessScore());
                }
            }

            // Calculate novelty for each individual
            List<BehaviourResult> inds = new ArrayList<>(pop.subpops.get(p).individuals.size());
            for (Individual ind : pop.subpops.get(p).individuals) {
                ExpandedFitness indFit = (ExpandedFitness) ind.fitness;
                BehaviourResult br = (BehaviourResult) indFit.getCorrespondingEvaluation(behaviourIndex);
                inds.add(br);
            }

            if (!nslc) {
                // Assign novelty scores
                List<Double> scores = computeKNNDistances(pool, inds, k);
                for (int i = 0; i < pop.subpops.get(p).individuals.size(); i++) {
                    ExpandedFitness indFit = (ExpandedFitness) pop.subpops.get(p).individuals.get(i).fitness;
                    double novScore = scores.get(i);
                    indFit.scores().put(scoreName, novScore);
                    indFit.setFitness(state, novScore, false);
                }
            } else {
                List<List<Pair<BehaviourResult, Double>>> nearest = computeNearest(pool, inds, k);
                for (int i = 0; i < pop.subpops.get(p).individuals.size(); i++) {
                    ExpandedFitness indFit = (ExpandedFitness) pop.subpops.get(p).individuals.get(i).fitness;

                    double novScore = 0;
                    double betterThan = 0;
                    for (Pair<BehaviourResult, Double> n : nearest.get(i)) {
                        novScore += n.getRight();
                        if (indFit.getFitnessScore() > fitnessMap.get(n.getLeft())) {
                            betterThan++;
                        }
                    }
                    novScore /= k;
                    indFit.scores().put(scoreName, novScore);
                    indFit.scores().put(LC_SCORE, betterThan);
                    indFit.setFitness(state, novScore, false);
                }
            }
        }
    }

    public List<Double> computeKNNDistances(List<BehaviourResult> pool, List<BehaviourResult> targets, final int k) {
        final KNNDistanceCalculator calc = getKNNCalculator(pool);
        List<Double> res = new ArrayList<>(targets.size());
        if (threaded) {
            List<Future<Double>> futures = new ArrayList<>(targets.size());
            for (final BehaviourResult t : targets) {
                Future<Double> task = executor.submit(new Callable<Double>() {
                    @Override
                    public Double call() throws Exception {
                        return calc.meanKNNDistance(t, k);
                    }
                });
                futures.add(task);
            }
            try {
                for (Future<Double> f : futures) {
                    res.add(f.get());
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        } else {
            for (BehaviourResult t : targets) {
                res.add(calc.meanKNNDistance(t, k));
            }
        }
        return res;
    }

    public List<List<Pair<BehaviourResult, Double>>> computeNearest(List<BehaviourResult> pool, List<BehaviourResult> targets, final int k) {
        final KNNDistanceCalculator calc = getKNNCalculator(pool);
        List<List<Pair<BehaviourResult, Double>>> res = new ArrayList<>(targets.size());
        if (threaded) {
            List<Future<List<Pair<BehaviourResult, Double>>>> futures = new ArrayList<>(targets.size());
            for (final BehaviourResult t : targets) {
                Future<List<Pair<BehaviourResult, Double>>> task = executor.submit(new Callable<List<Pair<BehaviourResult, Double>>>() {
                    @Override
                    public List<Pair<BehaviourResult, Double>> call() throws Exception {
                        return calc.nearest(t, k);
                    }
                });
                futures.add(task);
            }
            try {
                for (Future<List<Pair<BehaviourResult, Double>>> f : futures) {
                    res.add(f.get());
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        } else {
            for (BehaviourResult t : targets) {
                res.add(calc.nearest(t, k));
            }
        }
        return res;
    }

    public KNNDistanceCalculator getKNNCalculator(List<BehaviourResult> pool) {
        BehaviourResult sample = pool.get(0);
        boolean kdtree = false;
        if (useKDTree && sample instanceof VectorBehaviourResult) {
            VectorBehaviourResult vbr = (VectorBehaviourResult) sample;
            int dim = vbr.getBehaviour().length;
            kdtree = pool.size() > FastMath.pow2(dim);
        }
        KNNDistanceCalculator calc = kdtree ? new KDTreeCalculator() : new BruteForceCalculator();
        calc.setPool(pool);
        return calc;
    }

    public interface KNNDistanceCalculator {

        public void setPool(List<BehaviourResult> pool);

        public double meanKNNDistance(BehaviourResult target, int k);

        public List<Pair<BehaviourResult, Double>> nearest(BehaviourResult target, int k);

    }

    public class BruteForceCalculator implements KNNDistanceCalculator {

        private List<BehaviourResult> pool;

        @Override
        public void setPool(List<BehaviourResult> pool) {
            this.pool = pool;
        }

        @Override
        public double meanKNNDistance(BehaviourResult target, int k) {
            ArrayList<Double> distances = new ArrayList<>();
            for (BehaviourResult br : pool) {
                if (target != br) {
                    distances.add(distance(target, br));
                }
            }
            Collections.sort(distances);
            double dist = 0;
            for (int i = 0; i < k; i++) {
                dist += distances.get(i);
            }
            return dist / k;
        }

        @Override
        public List<Pair<BehaviourResult, Double>> nearest(BehaviourResult target, int k) {
            ArrayList<Pair<BehaviourResult, Double>> nearest = new ArrayList<>();
            for (BehaviourResult br : pool) {
                if (target != br) {
                    nearest.add(Pair.of(br, distance(target, br)));
                }
            }
            Collections.sort(nearest, new Comparator<Pair<BehaviourResult, Double>>() {
                @Override
                public int compare(Pair<BehaviourResult, Double> o1, Pair<BehaviourResult, Double> o2) {
                    return Double.compare(o1.getRight(), o2.getRight());
                }
            });
            ArrayList<Pair<BehaviourResult, Double>> res = new ArrayList<>(k);
            for (int i = 0; i < k && i < nearest.size(); i++) {
                res.add(nearest.get(i));
            }
            return res;
        }
    }

    public class KDTreeCalculator implements KNNDistanceCalculator {

        private KdTree<BehaviourResult> tree;

        @Override
        public void setPool(List<BehaviourResult> pool) {
            tree = new KdTree.Euclidean<>(((VectorBehaviourResult) pool.get(0)).value().length);
            for (BehaviourResult br : pool) {
                tree.addPoint(((VectorBehaviourResult) br).value(), br);
            }
        }

        @Override
        public double meanKNNDistance(final BehaviourResult target, int k) {
            VectorBehaviourResult vbr = (VectorBehaviourResult) target;
            ArrayList<KdTree.SearchResult<BehaviourResult>> nearest = tree.nearestNeighbours(vbr.getBehaviour(), k + 1); // +1 to exclude self
            double dist = 0;
            for (KdTree.SearchResult<BehaviourResult> e : nearest) {
                dist += e.distance;
            }
            return dist / k;
        }

        @Override
        public List<Pair<BehaviourResult, Double>> nearest(BehaviourResult target, int k) {
            VectorBehaviourResult vbr = (VectorBehaviourResult) target;
            ArrayList<KdTree.SearchResult<BehaviourResult>> nearest = tree.nearestNeighbours(vbr.getBehaviour(), k + 1); // +1 to exclude self
            ArrayList<Pair<BehaviourResult, Double>> res = new ArrayList<>(k);
            for (KdTree.SearchResult<BehaviourResult> n : nearest) {
                res.add(Pair.of(n.payload, n.distance));
            }
            return res;
        }
    }

    protected double distance(BehaviourResult br1, BehaviourResult br2) {
        return br1.distanceTo(br2);
    }

    protected void updateArchive(EvolutionState state, Population pop) {
        if (archiveCriteria != ArchiveCriteria.none) {
            for (int i = 0; i < pop.subpops.size(); i++) {
                List<Individual> popInds = pop.subpops.get(i).individuals;
                List<ArchiveEntry> archive = archives[i];
                LinkedHashSet<Individual> toAdd = new LinkedHashSet<>();

                // select individuals to add to archive
                if (archiveCriteria == ArchiveCriteria.random) {
                    while (toAdd.size() < archiveGrowth * popInds.size()) {
                        int index = state.random[0].nextInt(popInds.size());
                        toAdd.add(popInds.get(index));
                    }
                } else if (archiveCriteria == ArchiveCriteria.novel) {
                    List<Individual> copy = new ArrayList<>(popInds);
                    Collections.sort(copy, new Comparator<Individual>() {
                        @Override
                        public int compare(Individual o1, Individual o2) {
                            double nov1 = ((ExpandedFitness) o1.fitness).getScore(scoreName);
                            double nov2 = ((ExpandedFitness) o2.fitness).getScore(scoreName);
                            return Double.compare(nov2, nov1);
                        }
                    });
                    toAdd.addAll(copy.subList(0, (int) (archiveGrowth * popInds.size())));
                }

                // open up space for new individuals if needed
                removeN(state, archive, archive.size() - sizeLimit + toAdd.size());

                // add new individuals to the archive
                for (Individual ind : toAdd) {
                    ArchiveEntry ar = new ArchiveEntry(state, ind);
                    archive.add(ar);
                }
            }
        }
    }

    public List<ArchiveEntry>[] getArchives() {
        return archives;
    }

    protected void removeN(EvolutionState state, List<ArchiveEntry> archive, int n) {
        if (removalCriteria == ArchiveCriteria.random) {
            for (int i = 0; i < n; i++) {
                int index = state.random[0].nextInt(archive.size());
                archive.remove(index);
            }
        } else if (removalCriteria == ArchiveCriteria.novel) {
            // compute novelty of all elements in the archive
            List<BehaviourResult> pool = new ArrayList<>(archive.size());
            for (ArchiveEntry e : archive) {
                pool.add(e.getBehaviour());
            }
            List<Double> scores = computeKNNDistances(pool, pool, k);
            for (int i = 0; i < archive.size(); i++) {
                ArchiveEntry e = archive.get(i);
                e.novelty = scores.get(i);
            }

            // sort archive according to novelty
            Collections.sort(archive, new Comparator<ArchiveEntry>() {
                @Override
                public int compare(ArchiveEntry o1, ArchiveEntry o2) {
                    return Double.compare(o1.novelty, o2.novelty);
                }
            });

            // remove least novel
            Iterator<ArchiveEntry> iter = archive.iterator();
            for (int i = 0; i < n; i++) {
                iter.next();
                iter.remove();
            }
        }
    }

    public class ArchiveEntry implements Serializable {

        private static final long serialVersionUID = 1L;

        protected BehaviourResult behaviour;
        protected int generation;
        protected double fitness;
        protected Individual ind;
        protected double novelty;

        protected ArchiveEntry(EvolutionState state, Individual ind) {
            this.ind = ind;
            this.behaviour = (BehaviourResult) ((ExpandedFitness) ind.fitness).getCorrespondingEvaluation(behaviourIndex);
            this.fitness = ((ExpandedFitness) ind.fitness).getFitnessScore();
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

        public Individual getIndividual() {
            return ind;
        }
    }

}
