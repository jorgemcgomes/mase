/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic.systematic;

import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.List;
import mase.evaluation.MetaEvaluator;
import mase.evaluation.PostEvaluator;
import mase.evaluation.ExpandedFitness;
import mase.novelty.NoveltyEvaluation;
import mase.novelty.NoveltyEvaluation.ArchiveEntry;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author jorge
 */
public class SystematicStandardiser implements PostEvaluator {
    
    private static final long serialVersionUID = 1L;
    protected List<ArchiveEntry>[] archives;
    protected int behavIndex;
    public static final double BOUND = 3; // 68–95–99.7 rule -- three-sigma rule
    
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
                    behavIndex = ne.getBehaviourIndex();
                }
            }
        }

        // join all behaviour results in one list
        // from archive
        ArrayList<SystematicResult> results = new ArrayList<>(2000);
        for (ArchiveEntry ar : archives[0]) {
            results.add((SystematicResult) ar.getBehaviour());
        }
        // from the population
        for (Subpopulation sub : state.population.subpops) {
            for (Individual ind : sub.individuals) {
                ExpandedFitness nf = (ExpandedFitness) ind.fitness;
                results.add((SystematicResult) nf.getCorrespondingEvaluation(behavIndex));
            }
        }
        
        // calculate mean and sd for each feature
        int size = results.get(0).getOriginalResult().length;
        DescriptiveStatistics[] ds = new DescriptiveStatistics[size];
        for (int i = 0; i < size; i++) {
            ds[i] = new DescriptiveStatistics();
            for (SystematicResult vbr : results) {
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

        for (SystematicResult vbr : results) {
            for (int i = 0; i < size; i++) {
                double v = (vbr.getOriginalResult()[i] - means[i]) / sds[i];
                vbr.getBehaviour()[i] = (float) Math.max(-BOUND, Math.min(BOUND, v));
            }
        }
    }
}
