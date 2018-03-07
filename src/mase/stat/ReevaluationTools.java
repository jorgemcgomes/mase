/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import ec.Evaluator;
import ec.EvolutionState;
import ec.Evolve;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mase.MaseEvolve;
import mase.evaluation.MetaEvaluator;
import mase.MaseProblem;
import mase.controllers.AgentController;
import mase.evaluation.EvaluationResult;
import mase.controllers.GroupController;
import mase.controllers.HeterogeneousGroupController;
import mase.evaluation.CompoundEvaluationResult;
import mase.evaluation.FitnessResult;
import mase.util.CommandLineUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.FastMath;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class ReevaluationTools {

    public static final String P_AGENT_CONTROLLER = "-c";
    public static final String P_CONTROLLER = "-gc";
    public static final String P_NREPS = "-r";

    public static void main(String[] args) throws Exception {
        int reps = CommandLineUtils.getIntFromArgs(args, P_NREPS);
        // In case there is no GroupController, but multiple AgentControllers, 
        // the file of the first AC is returned
        File gc = CommandLineUtils.getFileFromArgs(args, P_CONTROLLER, true);
        if(gc == null) {
            gc = CommandLineUtils.getFileFromArgs(args, P_AGENT_CONTROLLER, true);
        }
        MaseProblem simulator = createSimulator(args, gc.getParentFile());
        GroupController controller = createController(args);
        Reevaluation res = reevaluate(controller, simulator, reps);
        System.out.println(res);
    }

    public static MaseProblem createSimulator(String[] args) {
        ParameterDatabase db = Evolve.loadParameterDatabase(args);
        EvolutionState state = Evolve.initialize(db, 0);
        Parameter base = new Parameter(EvolutionState.P_EVALUATOR).push(Evaluator.P_PROBLEM);
        Parameter def = new Parameter(EvolutionState.P_EVALUATOR).push(MetaEvaluator.P_BASE_EVAL).push(Evaluator.P_PROBLEM);
        MaseProblem sim = (MaseProblem) db.getInstanceForParameter(base, def, MaseProblem.class);
        sim.setup(state, base);
        return sim;
    }    
    
    public static MaseProblem createSimulator(String[] args, File searchDir) {
        boolean found = false;
        for(String a : args) {
            if(a.equalsIgnoreCase(Evolve.A_FILE)) {
                found = true;
                break;
            }
        }
        if(!found) {
            File config = new File(searchDir, MaseEvolve.DEFAULT_CONFIG);
            if(!config.exists()) {
                System.out.println("File does not exist: " + config.getAbsolutePath());
            } else {
                String[] newArgs = Arrays.copyOf(args, args.length + 2);
                newArgs[newArgs.length - 2] = Evolve.A_FILE;
                newArgs[newArgs.length - 1] = config.getAbsolutePath();
                args = newArgs;
            }
        }
        return createSimulator(args);
    }

    public static GroupController createController(String[] args) throws Exception {
        // Parameter loading
        File gc = CommandLineUtils.getFileFromArgs(args, P_CONTROLLER, true);
        List<File> controllers = CommandLineUtils.getFilesFromArgs(args, P_AGENT_CONTROLLER, true);
        if (gc == null && controllers.isEmpty()) {
            System.out.println("No controllers to run.");
            return null;
        }
        if (gc != null && !controllers.isEmpty()) {
            System.out.println("Both agent controllers and a group controller were provided.");
            return null;
        }

        // Create controller
        GroupController controller = null;
        if (gc != null) {
            PersistentSolution c = SolutionPersistence.readSolution(new FileInputStream(gc));
            System.out.println(c);
            controller = c.getController();
        } else {
            AgentController[] acs = new AgentController[controllers.size()];
            for (int i = 0; i < controllers.size(); i++) {
                PersistentSolution c = SolutionPersistence.readSolution(new FileInputStream(controllers.get(i)));
                System.out.println("------------- Controller " + i + " -------------");
                System.out.println(c);
                HeterogeneousGroupController hgc = (HeterogeneousGroupController) c.getController();
                acs[i] = hgc.getAgentControllers(controllers.size())[i];
            }
            controller = new HeterogeneousGroupController(acs);
        }
        return controller;
    }
    
    public static class Reevaluation {

        public List<EvaluationResult[]> allResults;
        public EvaluationResult[] mergedResults;
        public double meanFitness;
        public double sdFitness;
        public double minFitness;
        public double maxFitness;

        @Override
        public String toString() {
            double ci = 1.645 * (sdFitness / FastMath.sqrt(allResults.size()));
            String res = "Mean fitness: " + meanFitness + " (" + sdFitness + ") " + 
                    "[" + minFitness + "," + maxFitness + "] CI-90%: [" + (meanFitness-ci) +"," + (meanFitness+ci) + "]\n";
            for (int i = 0; i < mergedResults.length; i++) {
                res += "Evaluation " + i + ":\n" + mergedResults[i].toString() + "\n\n";
            }
            return res;
        }

    }

    public static Reevaluation reevaluate(PersistentSolution gc, MaseProblem sim, int reps) {
        return reevaluate(gc.getController(), gc.getSubpop(), sim, reps);
    }

    public static Reevaluation reevaluate(GroupController gc, MaseProblem sim, int reps) {
        return reevaluate(gc, 0, sim, reps);
    }

    public static Reevaluation reevaluate(GroupController gc, int subpop, MaseProblem sim, int reps) {
        // Make simulations
        ArrayList<EvaluationResult[]> results = new ArrayList<>(reps);
        for (int i = 0; i < reps; i++) {
            results.add(sim.evaluateSolution(gc, i));
        }

        // Merge evals
        EvaluationResult[] merged = new EvaluationResult[results.get(0).length];
        for (int i = 0; i < merged.length; i++) {
            List<EvaluationResult> samples = new ArrayList(results.size());
            for(EvaluationResult[] ers : results) {
                samples.add(ers[i]);
            }
            merged[i] = samples.get(0).mergeEvaluations(samples);
        }

        DescriptiveStatistics ds = new DescriptiveStatistics(results.size());
        for (EvaluationResult[] rs : results) {
            EvaluationResult fr = null;
            if (rs[0] instanceof CompoundEvaluationResult) {
                CompoundEvaluationResult ser = (CompoundEvaluationResult) rs[0];
                fr = ser.getEvaluation(subpop);
            } else {
                fr = rs[0];
            }            
            if(fr != null && fr instanceof FitnessResult) {
                ds.addValue(((FitnessResult) fr).value());
            }
        }

        Reevaluation res = new Reevaluation();
        res.allResults = results;
        res.mergedResults = merged;
        if(ds.getN() > 0) {
            res.meanFitness = ds.getMean();
            res.sdFitness = ds.getStandardDeviation();
            res.minFitness = ds.getMin();
            res.maxFitness = ds.getMax();
        }
        return res;
    }
}
