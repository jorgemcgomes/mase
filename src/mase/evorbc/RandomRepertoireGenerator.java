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
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.pattern.NeuralNetworkPattern;

/**
 *
 * @author jorge
 */
public class RandomRepertoireGenerator {

    public static void main(String[] args) throws IOException {

        ParameterDatabase db = Evolve.loadParameterDatabase(args);
        EvolutionState state = Evolve.initialize(db, 0);
        Parameter prob = new Parameter(EvolutionState.P_EVALUATOR).push(MetaEvaluator.P_BASE_EVAL).push(Evaluator.P_PROBLEM);
        MaseProblem sim = (MaseProblem) db.getInstanceForParameter(prob, null, MaseProblem.class);
        sim.setup(state, prob);

        int maxHidden = 10;
        int inputs = 7;
        int outputs = 2;
        int n = 5000;
        int jobs = 10;

        Random rand = new Random();
        for (int j = 0; j < jobs; j++) {
            System.out.println("\n---------- Job " + j + " ----------\n");
            File f = new File("job." + j + ".finalarchive.tar.gz");
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
                    params[p] = -2 + rand.nextDouble() * 4; // [-2,2]
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
        }
    }

}
