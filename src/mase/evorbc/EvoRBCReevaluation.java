/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import mase.controllers.GroupController;
import mase.mason.MasonSimulationProblem;
import mase.mason.generic.SmartAgentProvider;
import mase.mason.world.SmartAgent;
import mase.stat.ReevaluationTools;
import mase.stat.SolutionPersistence;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 *
 * @author jorge
 */
public class EvoRBCReevaluation {

    public static void main(String[] args) throws Exception {
        int reps = ReevaluationTools.getRepetitions(args);
        File gc = ReevaluationTools.findControllerFile(args);
        MasonSimulationProblem simulator = (MasonSimulationProblem) ReevaluationTools.createSimulator(args, gc.getParentFile());

        File out = new File(gc.getAbsolutePath() + ".stat");
        logController(gc, simulator, reps, out);
    }

    public static void logController(File gc, MasonSimulationProblem simulator, int reps, File out) {
        simulator.loggers().clear();
        try {
            GroupController controller = SolutionPersistence.readSolutionFromFile(gc).getController();
            if (controller.getAgentControllers(1)[0] instanceof NeuralArbitratorController) {
                FileWriter w = new FileWriter(out);
                EvoRBCLogger log = new EvoRBCLogger(w);
                simulator.loggers().add(log);
                for (int i = 0; i < reps; i++) {
                    simulator.evaluateSolution(controller, i);
                }
                w.close();
            } else {
                System.out.println("Not an EvoRBC controller: " + gc.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.delete();
        }

    }

    public static class EvoRBCLogger implements Steppable {

        private static final long serialVersionUID = 1L;

        private final Writer writer;
        private boolean inited = false;

        private EvoRBCLogger(Writer writer) {
            this.writer = writer;
        }

        @Override
        public void step(SimState state) {
            try {
                SmartAgentProvider sp = (SmartAgentProvider) state;
                List<? extends SmartAgent> agents = sp.getSmartAgents();
                for (int i = 0; i < agents.size(); i++) {
                    SmartAgent ag = agents.get(i);
                    NeuralArbitratorController ac = (NeuralArbitratorController) ag.getAgentController();

                    if (!inited) {
                        writer.write("Seed Time Agent");
                        for (int j = 0; j < ag.lastNormalisedInputs().length; j++) {
                            writer.write(" Input_" + j);
                        }
                        for (int j = 0; j < ac.lastArbitratorOutput.length; j++) {
                            writer.write(" ArbitratorOut_" + j);
                        }
                        for (int j = 0; j < ac.lastRepertoireCoords.length; j++) {
                            writer.write(" Coord_" + j);
                        }
                        writer.write(" Primitive");
                        for (int j = 0; j < ag.lastNormalisedOutputs().length; j++) {
                            writer.write(" PrimitiveOut_" + j);
                        }
                        writer.write("\n");
                        inited = true;
                    }

                    writer.write(state.seed() + " " + state.schedule.getTime() + " " + i);
                    writeVec(ag.lastNormalisedInputs());
                    writeVec(ac.lastArbitratorOutput);
                    writeVec(ac.lastRepertoireCoords);
                    writer.write(" " + ac.lastPrimitive.id);
                    writeVec(ag.lastNormalisedOutputs());
                    writer.write("\n");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private void writeVec(double[] vec) throws IOException {
            for (double v : vec) {
                writer.write(" " + v);
            }
        }
    }
}
