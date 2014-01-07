/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic;

import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.List;
import mase.MetaEvaluator;
import mase.PostEvaluator;
import mase.novelty.NoveltyEvaluation;
import mase.novelty.NoveltyEvaluation.ArchiveEntry;
import mase.novelty.NoveltyFitness;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author jorge
 */
public class SemiGenericStandardiser implements PostEvaluator {

    // TODO: write original behaviours somewhere
    private List<List<ArchiveEntry>> archives;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        ;
    }

    @Override
    public void processPopulation(EvolutionState state) {
        // TODO: needs different mechanisms for shared and individual archives

        // initialization
        if (archives == null) {
            PostEvaluator[] evals = ((MetaEvaluator) state.evaluator).getPostEvaluators();
            for (PostEvaluator pe : evals) {
                if (pe instanceof NoveltyEvaluation) {
                    NoveltyEvaluation ne = (NoveltyEvaluation) pe;
                    archives = ne.getArchives();
                }
            }
        }

        // join all behaviour results in one list
        // from archive
        ArrayList<SemiGenericResult> results = new ArrayList<SemiGenericResult>(2000);
        for (ArchiveEntry ar : archives.get(0)) {
            results.add((SemiGenericResult) ar.getBehaviour());
        }
        // from the population
        for (Subpopulation sub : state.population.subpops) {
            for (Individual ind : sub.individuals) {
                NoveltyFitness nf = (NoveltyFitness) ind.fitness;
                results.add((SemiGenericResult) nf.getNoveltyBehaviour());
            }
        }
        
        // calculate mean and sd for each feature
        int size = results.get(0).getOriginalResult().length;
        DescriptiveStatistics[] ds = new DescriptiveStatistics[size];
        for (int i = 0; i < size; i++) {
            ds[i] = new DescriptiveStatistics();
            for (SemiGenericResult vbr : results) {
                ds[i].addValue(vbr.getOriginalResult()[i]);
            }
        }
        double[] means = new double[size];
        double[] sds = new double[size];
        for (int i = 0; i < size; i++) {
            means[i] = ds[i].getMean();
            double sd = ds[i].getStandardDeviation();
            sds[i] = sd == 0 || Double.isNaN(sd) || Double.isInfinite(sd) ? 1 : sd;
        }

        for (SemiGenericResult vbr : results) {
            for (int i = 0; i < size; i++) {
                vbr.getBehaviour()[i] = (float) ((vbr.getOriginalResult()[i] - means[i]) / sds[i]);
            }
        }
    }
}
