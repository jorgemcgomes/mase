/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.jbot;

//import commoninterface.neuralnetwork.CINEATNetwork;
import controllers.DroneNeuralNetworkController;
import ec.EvolutionState;
import ec.util.Parameter;
import evolutionaryrobotics.JBotEvolver;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import mase.SimulationProblem;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.evaluation.EvaluationFunction;
import mase.evaluation.EvaluationResult;
import simulation.Simulator;
import simulation.robot.Robot;

/**
 *
 * @author jorge
 */
public class JBotSimulator extends SimulationProblem {

    public static final String P_CONFIG_FILE = "jbot-config";
    protected File configFile;
    protected JBotEvolver jbotEvolver;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        base = defaultBase();

        String confFile = state.parameters.getString(base.push(P_CONFIG_FILE), null);
        System.out.println("***** " + confFile);
        try {
            jbotEvolver = new JBotEvolver(new String[]{confFile});

        } catch (Exception ex) {
            Logger.getLogger(JBotSimulator.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (EvaluationFunction ef : evalFunctions) {
            JBotEvaluation e = (JBotEvaluation) ef;
            e.setup(jbotEvolver);
        }
    }

    @Override
    public EvaluationResult[] evaluateSolution(GroupController gc, long seed) {
        EvaluationResult[][] evalResults = new EvaluationResult[evalFunctions.length][repetitions];
        for (int r = 0; r < repetitions; r++) {
            JBotEvaluation[] evals = new JBotEvaluation[evalFunctions.length];
            for (int i = 0; i < evalFunctions.length; i++) {
                evals[i] = (JBotEvaluation) evalFunctions[i].clone();
            }

            Simulator simulator = setupSimulator(jbotEvolver, seed++, gc);
            for (JBotEvaluation e : evals) {
                simulator.addCallback(e);
            }
            simulator.simulate();

            for (int i = 0; i < evals.length; i++) {
                evalResults[i][r] = evals[i].getResult();
            }
        }

        EvaluationResult[] mergedResult = new EvaluationResult[evalFunctions.length];
        for (int i = 0; i < evalFunctions.length; i++) {
            mergedResult[i] = evalResults[i][0].mergeEvaluations(evalResults[i]);
        }
        return mergedResult;
    }

    static Simulator setupSimulator(JBotEvolver evo, long seed, GroupController gc) {
        Simulator simulator = evo.createSimulator(new Random(seed));
        ArrayList<Robot> robots = evo.createRobots(simulator);
        setupControllers(robots, gc);
        simulator.addRobots(robots);
        simulator.setupEnvironment();
        return simulator;
    }

    static void setupControllers(ArrayList<Robot> robots, GroupController gc) {
        AgentController[] agentControllers = gc.getAgentControllers(robots.size());
        for (int i = 0; i < robots.size(); i++) {
            AgentController ac = agentControllers[i];
            DroneNeuralNetworkController contr = (DroneNeuralNetworkController) robots.get(i).getController();
            /*if (contr.getNeuralNetwork() instanceof CINEATNetwork) {
             NEATAgentController nac = (NEATAgentController) ac;
             CINEATNetwork neatNet = (CINEATNetwork) contr.getNeuralNetwork();
             double[] weights = NEATSerializer.serializeToArray(nac.getNetwork());
             neatNet.setWeights(weights);
             } else {*/
            MaseNetworkWrapper newNetwork = new MaseNetworkWrapper(
                    contr.getNeuralNetwork().getInputs(), contr.getNeuralNetwork().getOutputs());
            newNetwork.setRealController(ac);
            contr.setNeuralNetwork(newNetwork);
            //}
        }
    }
}
