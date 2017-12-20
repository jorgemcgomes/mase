/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.evorbc.NeuralArbitratorController;
import mase.evorbc.Repertoire.Primitive;
import mase.evorbc.SubsetNeuralArbitratorController;
import mase.evorbc.gp.GPArbitratorController;
import mase.mason.generic.SmartAgentProvider;
import mase.mason.world.SmartAgent;
import mase.stat.ReevaluationTools;
import mase.stat.SolutionPersistence;
import mase.util.FormatUtils;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 *
 * @author jorge
 */
public class ExecutionReevaluation {

    public static void main(String[] args) {
        int reps = ReevaluationTools.getRepetitions(args);
        File gc = ReevaluationTools.findControllerFile(args);
        MasonSimulationProblem simulator = (MasonSimulationProblem) ReevaluationTools.createSimulator(args, gc.getParentFile());

        File out = new File(gc.getAbsolutePath() + ".stat");
        try {
            logController(gc, simulator, reps, out);
        } catch (Exception ex) {
            ex.printStackTrace();
            out.delete();
        }
    }

    public static void logController(File gc, MasonSimulationProblem simulator, int reps, File out) throws Exception {
        simulator.loggers().clear();
        GroupController controller = SolutionPersistence.readSolutionFromFile(gc).getController();
        // Assumes that all the controllers have the same type
        AgentController ac = controller.getAgentControllers(1)[0];

        // Create the logger
        FileWriter w = new FileWriter(out);
        Steppable log = null;
        if (ac instanceof NeuralArbitratorController) {
            log = new NeuralEvoRBCLogger(w);
        } else if (ac instanceof GPArbitratorController) {
            log = new GPEvoRBCLogger(w);
        } else if (ac instanceof SubsetNeuralArbitratorController) {
            log = new SubsetEvoRBCLogger(w);
        } else {
            log = new AgentLogger(w);
        }

        // Log
        simulator.loggers().add(log);
        for (int i = 0; i < reps; i++) {
            simulator.evaluateSolution(controller, i);
        }
        w.close();
    }

    public static class AgentLogger implements Steppable {

        private final Writer writer;
        private boolean inited = false;

        private AgentLogger(Writer writer) {
            this.writer = writer;
        }

        @Override
        public void step(SimState state) {
            try {
                SmartAgentProvider sp = (SmartAgentProvider) state;
                List<? extends SmartAgent> agents = sp.getSmartAgents();
                for (int i = 0; i < agents.size(); i++) {
                    SmartAgent ag = agents.get(i);

                    if (!inited) {
                        writer.write("Seed Repetition Time Agent");
                        for (int j = 0; j < ag.lastNormalisedInputs().length; j++) {
                            writer.write(" SensorInput_" + j);
                        }
                        for (int j = 0; j < ag.lastNormalisedOutputs().length; j++) {
                            writer.write(" ActuatorOut_" + j);
                        }
                        writer.write(extraHeader(ag.getAgentController()) + "\n");
                        inited = true;
                    }

                    writer.write(state.seed() + " " + ((MasonSimState) state).getRepetitionNumber() + " " + state.schedule.getTime() + " " + i);
                    writer.write(" " + FormatUtils.toStringSpaceSeparated(ag.lastNormalisedInputs()));
                    writer.write(" " + FormatUtils.toStringSpaceSeparated(ag.lastNormalisedOutputs()));
                    writer.write(extraLog(ag.getAgentController()) + "\n");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        protected String extraHeader(AgentController ac) {
            return "";
        }

        protected String extraLog(AgentController ac) {
            return "";
        }

    }

    public static class NeuralEvoRBCLogger extends AgentLogger {

        public NeuralEvoRBCLogger(Writer writer) {
            super(writer);
        }

        @Override
        protected String extraHeader(AgentController ac) {
            String h = "";
            NeuralArbitratorController nac = (NeuralArbitratorController) ac;
            for (int j = 0; j < nac.lastArbitratorOutput.length; j++) {
                h += " ArbitratorOut_" + j;
            }
            for (int j = 0; j < nac.lastRepertoireCoords.length; j++) {
                h += " Coord_" + j;
            }
            h += " Primitive";
            return h;
        }

        @Override
        protected String extraLog(AgentController ac) {
            NeuralArbitratorController nac = (NeuralArbitratorController) ac;
            return " " + FormatUtils.toStringSpaceSeparated(nac.lastArbitratorOutput)
                    + " " + FormatUtils.toStringSpaceSeparated(nac.lastRepertoireCoords)
                    + " " + nac.lastPrimitive.id;
        }
    }

    public static class GPEvoRBCLogger extends AgentLogger {

        public GPEvoRBCLogger(Writer writer) {
            super(writer);
        }

        @Override
        protected String extraHeader(AgentController ac) {
            return " Primitive";
        }

        @Override
        protected String extraLog(AgentController ac) {
            GPArbitratorController nac = (GPArbitratorController) ac;
            return " " + nac.getLastPrimitive().id;
        }
    }

    public static class SubsetEvoRBCLogger extends AgentLogger {

        public SubsetEvoRBCLogger(Writer writer) {
            super(writer);
        }

        @Override
        protected String extraHeader(AgentController ac) {
            String h = "";
            SubsetNeuralArbitratorController nac = (SubsetNeuralArbitratorController) ac;
            for (int i = 0; i < nac.getPrimitiveSubset().length; i++) {
                h += " Primitive_" + i;
            }
            for (int i = 0; i < nac.getLastArbitratorActivations().length; i++) {
                h += " ArbitratorOut_" + i;
            }
            return h + " Primitive";
        }

        @Override
        protected String extraLog(AgentController ac) {
            SubsetNeuralArbitratorController nac = (SubsetNeuralArbitratorController) ac;
            String l = "";
            for (Primitive p : nac.getPrimitiveSubset()) {
                l += " " + p.id;
            }
            l += " " + FormatUtils.toStringSpaceSeparated(nac.getLastArbitratorActivations());
            l += " " + nac.getLastPrimitive().id;
            return l;
        }
    }

}
