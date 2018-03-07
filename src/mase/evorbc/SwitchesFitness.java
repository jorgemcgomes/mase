/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import java.util.List;
import mase.controllers.AgentController;
import mase.evaluation.FitnessResult;
import mase.evorbc.Repertoire.Primitive;
import mase.evorbc.gp.GPArbitratorController;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import mase.mason.generic.SmartAgentProvider;
import mase.mason.world.SmartAgent;

/**
 *
 * @author jorge
 */
public class SwitchesFitness extends MasonEvaluation<FitnessResult> {

    private static final long serialVersionUID = 1L;
    private FitnessResult fr;
    private int switches;
    private AgentController ac;
    private Primitive previousPrimitive;

    @Override
    public FitnessResult getResult() {
        return fr;
    }

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(sim);
        SmartAgentProvider sp = (SmartAgentProvider) sim;
        List<? extends SmartAgent> agents = sp.getSmartAgents();
        if(agents.size() > 1) {
            throw new RuntimeException("SwitchesFitness only supports one agent");
        }
        ac = agents.get(0).getAgentController();
        switches = 0;
        previousPrimitive = null;
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        fr = new FitnessResult(1 - (double) switches / currentEvaluationStep);
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(sim); 
        Primitive lastUsed;
        if(ac instanceof NeuralArbitratorController) {
            lastUsed = ((NeuralArbitratorController) ac).getLastPrimitive();
        } else if(ac instanceof GPArbitratorController) {
            lastUsed = ((GPArbitratorController) ac).getLastPrimitive();
        } else if(ac instanceof SubsetNeuralArbitratorController) {
            lastUsed = ((SubsetNeuralArbitratorController) ac).getLastPrimitive();
        } else {
            throw new RuntimeException("Controller incompatible with SwitchesFitness: " + ac.getClass());
        }    
        if(lastUsed != previousPrimitive) {
            switches++;
            previousPrimitive = lastUsed;
        }
    }
}
