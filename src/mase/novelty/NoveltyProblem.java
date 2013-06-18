/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.novelty;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import mase.SimulationProblem;
import mase.evaluation.BehaviourResult;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class NoveltyProblem extends SimulationProblem {

    protected ArrayList<BehaviourResult> archive;
    public static final String P_K_NN = "ns-k";
    public static final String P_ARCHIVE_ADD_PROB = "ns-archive-prob";
    public static final String P_ARCHIVE_SIZE_LIMIT = "ns-archive-size";
    public static final String P_BLEND = "ns-blend";
    protected int k;
    protected double addProb;
    protected int sizeLimit;
    protected double blend;
    protected MersenneTwisterFast random;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base); //To change body of generated methods, choose Tools | Templates.

        this.random = state.random[0];
        this.k = state.parameters.getInt(base.push(P_K_NN), null);
        this.addProb = state.parameters.getDouble(base.push(P_ARCHIVE_ADD_PROB), null);
        this.sizeLimit = state.parameters.getInt(base.push(P_ARCHIVE_SIZE_LIMIT), null);
        this.blend = state.parameters.getDouble(base.push(P_BLEND), null);

        this.archive = new ArrayList<BehaviourResult>(sizeLimit);
    }

    @Override
    public void postprocessPopulation(EvolutionState state, Population pop, boolean[] assessFitness, boolean countVictoriesOnly) {
        // merge the collaboration trials
        super.postprocessPopulation(state, pop, assessFitness, countVictoriesOnly);
        // calculate novelty
        setNoveltyScores(state, pop);
        // post-process scores
        setFinalScores(state, pop);
        // update archive
        updateArchive(state, pop);
    }

    protected void setFinalScores(EvolutionState state, Population pop) {
        for (int i = 0; i < pop.subpops.length; i++) {
            // normalization
            float noveltyMin = Float.POSITIVE_INFINITY;
            float noveltyMax = Float.NEGATIVE_INFINITY;
            float fitnessMin = Float.POSITIVE_INFINITY;
            float fitnessMax = Float.NEGATIVE_INFINITY;
            for (int j = 0; j < pop.subpops[i].individuals.length; j++) {
                NoveltyFitness nf = (NoveltyFitness) pop.subpops[i].individuals[j].fitness;
                fitnessMin = (float) Math.min(fitnessMin, nf.fitnessScore());
                fitnessMax = (float) Math.max(fitnessMax, nf.fitnessScore());
                noveltyMin = (float) Math.min(noveltyMin, nf.noveltyScore);
                noveltyMax = (float) Math.max(noveltyMax, nf.noveltyScore);
            }

            // mix
            for (int j = 0; j < pop.subpops[i].individuals.length; j++) {
                NoveltyFitness nf = (NoveltyFitness) pop.subpops[i].individuals[j].fitness;
                nf.normalizedFitnessScore = fitnessMax == fitnessMin ? 0 : (nf.fitnessScore() - fitnessMin) / (fitnessMax - fitnessMin);
                nf.normalizedNoveltyScore = noveltyMax == noveltyMin ? 0 : (nf.noveltyScore - noveltyMin) / (noveltyMax - noveltyMin);
                nf.setFitness(state, (float) ((1 - blend) * nf.normalizedFitnessScore + blend * nf.normalizedNoveltyScore), false);
            }
        }
    }

    protected void setNoveltyScores(EvolutionState state, Population pop) {
        for (int p = 0; p < pop.subpops.length; p++) {
            // calculate novelty scores
            for (int j = 0; j < pop.subpops[p].individuals.length; j++) {
                Individual ind = pop.subpops[p].individuals[j];
                NoveltyFitness indFit = (NoveltyFitness) ind.fitness;

                ArrayList<Pair<Float, Boolean>> distances =
                        new ArrayList<Pair<Float, Boolean>>(archive.size() + pop.subpops[p].individuals.length);

                // from subpop
                for (Individual i : pop.subpops[p].individuals) {
                    if (ind != i) {
                        BehaviourResult er1 = ((NoveltyFitness) i.fitness).getNoveltyBehaviour();
                        BehaviourResult er2 = indFit.getNoveltyBehaviour();
                        distances.add(Pair.of(er1.distanceTo(er2), false));
                    }
                }

                // from repo
                for (BehaviourResult er : archive) {
                    BehaviourResult erInd = (BehaviourResult) ((NoveltyFitness) ind.fitness).getNoveltyBehaviour();
                    distances.add(Pair.of(er.distanceTo(erInd), true));
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
                for (int i = 0; i < k; i++) {
                    indFit.noveltyScore += distances.get(i).getLeft();
                    if (distances.get(i).getRight()) {
                        indFit.repoComparisons++;
                    }
                }
                indFit.noveltyScore /= k;
            }
        }
    }

    protected void updateArchive(EvolutionState state, Population pop) {
        for (int i = 0; i < pop.subpops.length; i++) {
            for (int j = 0; j < pop.subpops[i].individuals.length; j++) {
                Individual ind = pop.subpops[i].individuals[j];
                if (random.nextDouble() < addProb) {
                    BehaviourResult br = (BehaviourResult) ((NoveltyFitness) ind.fitness).getNoveltyBehaviour();
                    if (archive.size() == sizeLimit) {
                        int index = random.nextInt(archive.size());
                        archive.set(index, br);
                    } else {
                        archive.add(br);
                    }
                }
            }
        }
    }
}
