/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase;

import ec.EvolutionState;
import ec.Individual;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import mase.controllers.GroupController;
import mase.evaluation.EvaluationResult;
import mase.evaluation.ExpandedFitness;

/**
 * Same functionality as MaseProblem, but the evaluation is delayed. The
 * individuals are evaluated in batch in finishEvaluating() phase.
 *
 * @author jorge
 */
public abstract class MaseProblemBatch extends MaseProblem {

    private static final long serialVersionUID = 1L;

    protected static class Job {

        Individual[] ind;
        boolean[] updateFitness;
        boolean countVictoriesOnly;
        int[] subpops;
        int threadnum;
    }

    protected List<Job> queue;

    @Override
    public void evaluate(EvolutionState state, Individual[] inds, boolean[] updateFitness, boolean countVictoriesOnly, int[] subpops, int threadnum) {
        Job job = new Job();
        job.ind = Arrays.copyOf(inds, inds.length);
        job.updateFitness = Arrays.copyOf(updateFitness, updateFitness.length);
        job.countVictoriesOnly = countVictoriesOnly;
        job.subpops = Arrays.copyOf(subpops, subpops.length);
        job.threadnum = threadnum;
        queue.add(job);
    }

    @Override
    public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum) {
        Job job = new Job();
        job.ind = new Individual[]{ind};
        job.updateFitness = null;
        job.countVictoriesOnly = false;
        job.subpops = new int[]{subpopulation};
        job.threadnum = threadnum;
        queue.add(job);
    }

    @Override
    public void prepareToEvaluate(EvolutionState state, int threadnum) {
        super.prepareToEvaluate(state, threadnum);
        this.queue = new ArrayList<>();
    }

    @Override
    public void finishEvaluating(EvolutionState state, int threadnum) {
        super.finishEvaluating(state, threadnum);

        // build list of controllers to evaluate
        List<GroupController> gcs = new ArrayList<>();
        List<Long> seeds = new ArrayList<>();
        for (Job e : queue) {
            gcs.add(controllerFactory.createController(state, e.ind));
            seeds.add(nextSeed(state, threadnum));
        }

        // ask and wait for evaluations
        List<EvaluationResult[]> sols = evaluateSolutions(gcs, seeds);

        // modify the individuals according to the received results
        for (int i = 0; i < queue.size(); i++) {
            Job job = queue.get(i);
            EvaluationResult[] results = sols.get(i);
            if (job.ind.length == 1) { // not coevolution
                ExpandedFitness fit = (ExpandedFitness) job.ind[0].fitness;
                fit.setEvaluationResults(state, results, job.subpops[0]);
                job.ind[0].evaluated = true;
            } else {
                for (int j = 0; j < job.ind.length; j++) {
                    if (job.updateFitness[j]) {
                        ExpandedFitness trial = (ExpandedFitness) job.ind[j].fitness.clone();
                        trial.setEvaluationResults(state, results, job.subpops[j]);
                        trial.setContext(job.ind);
                        trial.trials = null;
                        job.ind[j].fitness.trials.add(trial);
                    }
                }
            }
        }
    }

    @Override
    public EvaluationResult[] evaluateSolution(GroupController gc, long seed) {
        List<EvaluationResult[]> res = evaluateSolutions(Collections.singletonList(gc), Collections.singletonList(seed));
        return res.get(0);
    }

    public abstract List<EvaluationResult[]> evaluateSolutions(List<GroupController> gcs, List<Long> seeds);

}
