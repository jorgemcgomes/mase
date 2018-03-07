/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import ec.Evaluator;
import ec.EvolutionState;
import ec.Evolve;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.zip.GZIPOutputStream;
import mase.MaseProblem;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.controllers.HomogeneousGroupController;
import mase.controllers.NeuralAgentController;
import mase.controllers.NeuralControllerIndividual;
import mase.evaluation.EvaluationResult;
import mase.evaluation.MetaEvaluator;
import mase.stat.PersistentSolution;
import mase.stat.SolutionPersistence;
import mase.util.CommandLineUtils;
import mase.util.FormatUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.pattern.NeuralNetworkPattern;

/**
 *
 * @author jorge
 */
public class RandomRepertoireGenerator {

    public static final String MAX_HIDDEN = "-h";
    public static final String INPUTS = "-i";
    public static final String OUTPUTS = "-o";
    public static final String SIZE = "-size";
    public static final String JOBS = "-jobs";
    public static final String OUT = "-out";
    public static final String FORCE = "-force";

    public static void main(String[] args) throws IOException {

        ParameterDatabase db = Evolve.loadParameterDatabase(args);
        EvolutionState state = Evolve.initialize(db, 0);
        Parameter prob = new Parameter(EvolutionState.P_EVALUATOR).push(MetaEvaluator.P_BASE_EVAL).push(Evaluator.P_PROBLEM);
        MaseProblem sim = (MaseProblem) db.getInstanceForParameter(prob, null, MaseProblem.class);
        sim.setup(state, prob);

        int maxHidden = CommandLineUtils.getIntFromArgs(args, MAX_HIDDEN);
        int inputs = CommandLineUtils.getIntFromArgs(args, INPUTS);
        int outputs = CommandLineUtils.getIntFromArgs(args, OUTPUTS);
        int n = CommandLineUtils.getIntFromArgs(args, SIZE);
        int jobs = CommandLineUtils.getIntFromArgs(args, JOBS);
        File out = new File(CommandLineUtils.getValueFromArgs(args, OUT));
        boolean force = CommandLineUtils.isFlagPresent(args, FORCE);
        out.mkdirs();

        Random rand = new Random();
        for (int j = 0; j < jobs; j++) {
            System.out.println("\n---------- Job " + j + " ----------\n");
            File f = new File(out, "job." + j + ".finalarchive.tar.gz");
            if (!f.exists() || force) {
                TarArchiveOutputStream taos = new TarArchiveOutputStream(new GZIPOutputStream(
                        new BufferedOutputStream(new FileOutputStream(f))));

                for (int i = 0; i < n; i++) {
                    int h = rand.nextInt(maxHidden + 1);
                    NeuralNetworkPattern pattern = NeuralControllerIndividual.createNetworkPattern(
                            NeuralControllerIndividual.FEED_FORWARD, inputs, h, outputs, false);
                    BasicNetwork network = (BasicNetwork) pattern.generate();
                    int nParams = network.getStructure().calculateSize();

                    double[] params = new double[nParams];
                    for (int p = 0; p < nParams; p++) {
                        params[p] = -5 + rand.nextDouble() * 10; // weights in [-5,5]
                    }
                    network.decodeFromArray(params);
                    AgentController ac = new NeuralAgentController(network.getFlat());
                    GroupController gc = new HomogeneousGroupController(ac);

                    EvaluationResult[] eval = sim.evaluateSolution(gc, sim.nextSeed(state, 0));

                    PersistentSolution sol = new PersistentSolution();
                    sol.setController(gc);
                    sol.setEvalResults(eval);
                    sol.setFitness(0);
                    sol.setOrigin(0, 0, i);

                    SolutionPersistence.writeSolutionToTar(sol, taos);
                    System.out.print(".");
                }
                taos.close();

                // Generate text
                File s = new File(out, "job." + j + ".archive.stat");
                try {
                    RepertoireToText.toText(f, s);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                System.out.println("Already found. Skipping.");
            }
        }
    }

}
