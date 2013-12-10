/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import mase.evaluation.EvaluationResult;
import mase.controllers.GroupController;
import mase.evaluation.SubpopEvaluationResult;
import mase.stat.PersistentSolution;
import mase.stat.SolutionPersistence;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class MasonReevaluate {

    public static final String P_NREPS = "-r";

    public static void main(String[] args) throws Exception {
        // Parameter loading
        File gc = null;
        int x;
        int nreps = 0;
        for (x = 0; x < args.length; x++) {
            if (args[x].equals(MasonPlayer.P_CONTROLLER)) {
                gc = new File(args[1 + x++]);
            } else if (args[x].equals(P_NREPS)) {
                nreps = Integer.parseInt(args[1 + x++]);
            }
        }
        if (nreps <= 0 || gc == null || !gc.exists()) {
            System.out.println("Wrong or missing arguments.");
            System.exit(1);
        }

        // Init
        MasonSimulator sim = MasonPlayer.createSimulator(args);
        PersistentSolution c = SolutionPersistence.readSolution(new FileInputStream(gc));
        // Eval
        Reevaluation res = reevaluate(c, sim, nreps);
        for (int i = 0; i < res.mergedResults.length; i++) {
            System.out.println("Evaluation " + i + ":\n" + res.mergedResults[i].toString() + "\n");
        }

    }

    /*
     * WARNING: assumes that fitness is always the first evaluation result
     */
    public static Reevaluation reevaluate(PersistentSolution gc, MasonSimulator sim, int reps) {
        return reevaluate(gc.getController(), gc.getSubpop(), sim, reps);
    }
    
    public static Reevaluation reevaluate(GroupController gc, int subpop, MasonSimulator sim, int reps) {
                // Make simulations
        ArrayList<EvaluationResult[]> results = new ArrayList<EvaluationResult[]>(reps);
        for (int i = 0; i < reps; i++) {
            results.add(sim.evaluateSolution(gc, i));
        }

        // Merge evals
        EvaluationResult[] merged = new EvaluationResult[results.get(0).length];
        for (int i = 0; i < merged.length; i++) {
            EvaluationResult[] samples = new EvaluationResult[results.size()];
            for (int j = 0; j < samples.length; j++) {
                samples[j] = results.get(j)[i];
            }
            merged[i] = samples[0].mergeEvaluations(samples);
        }

        DescriptiveStatistics ds = new DescriptiveStatistics(results.size());
        for (EvaluationResult[] rs : results) {
            float fit;
            if(rs[0] instanceof SubpopEvaluationResult) {
                SubpopEvaluationResult ser = (SubpopEvaluationResult) rs[0];
                fit = (Float) ser.getSubpopEvaluation(subpop).value();
            } else {
                fit = (Float) rs[0].value();
            }
            ds.addValue(fit);
        }

        Reevaluation res = new Reevaluation();
        res.allResults = results;
        res.mergedResults = merged;
        res.meanFitness = ds.getMean();
        res.sdFitness = ds.getStandardDeviation();

        return res;
    }

    public static class Reevaluation {

        public List<EvaluationResult[]> allResults;
        public EvaluationResult[] mergedResults;
        public double meanFitness;
        public double sdFitness;
    }
}
