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
import mase.SimulationProblem;
import mase.controllers.AgentController;
import mase.evaluation.EvaluationResult;
import mase.controllers.GroupController;
import mase.controllers.HeterogeneousGroupController;
import mase.evaluation.SubpopEvaluationResult;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class Reevaluate {

    public static final String P_AGENT_CONTROLLER = "-c";
    public static final String P_CONTROLLER = "-gc";
    public static final String P_NREPS = "-r";

    public static void main(String[] args) throws Exception {
        int reps = getRepetitions(args);
        File gc = findControllerFile(args);
        SimulationProblem simulator = createSimulator(args, gc.getParentFile());
        GroupController controller = createController(args);
        Reevaluation res = reevaluate(controller, simulator, reps);
        System.out.println(res);
    }

    public static SimulationProblem createSimulator(String[] args) {
        ParameterDatabase db = Evolve.loadParameterDatabase(args);
        EvolutionState state = Evolve.initialize(db, 0);
        Parameter base = new Parameter(EvolutionState.P_EVALUATOR).push(Evaluator.P_PROBLEM);
        Parameter def = new Parameter(EvolutionState.P_EVALUATOR).push(MetaEvaluator.P_BASE_EVAL).push(Evaluator.P_PROBLEM);
        SimulationProblem sim = (SimulationProblem) db.getInstanceForParameter(base, def, SimulationProblem.class);
        sim.setup(state, base);
        sim.setRepetitions(1);
        return sim;
    }    
    
    public static SimulationProblem createSimulator(String[] args, File searchDir) {
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
    
    /**
     * In case there is no GroupController, but multiple AgentControllers, the file of the first AC is returned
     * @param args
     * @return 
     */
    public static File findControllerFile(String[] args) {
        File gc = null;
        for(int i = 0 ; i < args.length - 1 ; i++) {
            if(args[i].equalsIgnoreCase(P_CONTROLLER) || args[i].equalsIgnoreCase(P_AGENT_CONTROLLER)) {
                gc = new File(args[i+1]);
                break;
            }
        }
        if(gc != null) {
            return gc;
        }
        return null;
    }

    public static GroupController createController(String[] args) throws Exception {
        // Parameter loading
        File gc = null;
        ArrayList<File> controllers = new ArrayList<>();
        int x;
        for (x = 0; x < args.length; x++) {
            if (args[x].equals(P_CONTROLLER)) {
                gc = new File(args[1 + x++]);
            } else if (args[x].equals(P_AGENT_CONTROLLER)) {
                controllers.add(new File(args[1 + x++]));
            }
        }
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

    public static int getRepetitions(String[] args) {
        for (int x = 0; x < args.length; x++) {
            if (args[x].equals(P_NREPS)) {
                return Integer.parseInt(args[x + 1]);
            }
        }
        return -1;
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
            String res = "Mean fitness: " + meanFitness + " +- " + sdFitness + " [" + minFitness + "," + maxFitness + "]\n\n";
            for (int i = 0; i < mergedResults.length; i++) {
                res += "Evaluation " + i + ":\n" + mergedResults[i].toString() + "\n\n";
            }
            return res;
        }

    }

    /*
     * WARNING: assumes that fitness is always the first evaluation result
     */
    public static Reevaluation reevaluate(PersistentSolution gc, SimulationProblem sim, int reps) {
        return reevaluate(gc.getController(), gc.getSubpop(), sim, reps);
    }

    public static Reevaluation reevaluate(GroupController gc, SimulationProblem sim, int reps) {
        return reevaluate(gc, 0, sim, reps);
    }

    public static Reevaluation reevaluate(GroupController gc, int subpop, SimulationProblem sim, int reps) {
        // Make simulations
        ArrayList<EvaluationResult[]> results = new ArrayList<>(reps);
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
            double fit;
            if (rs[0] instanceof SubpopEvaluationResult) {
                SubpopEvaluationResult ser = (SubpopEvaluationResult) rs[0];
                fit = (Double) ser.getSubpopEvaluation(subpop).value();
            } else {
                fit = (Double) rs[0].value();
            }
            ds.addValue(fit);
        }

        Reevaluation res = new Reevaluation();
        res.allResults = results;
        res.mergedResults = merged;
        res.meanFitness = ds.getMean();
        res.sdFitness = ds.getStandardDeviation();
        res.minFitness = ds.getMin();
        res.maxFitness = ds.getMax();
        return res;
    }
}
