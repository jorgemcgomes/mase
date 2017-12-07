/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc.gp;

import ec.EvolutionState;
import ec.gp.GPNode;
import ec.util.Parameter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import mase.mason.generic.SmartAgentProvider;
import mase.mason.world.SmartAgent;

/**
 * @author jorge
 */
public class UsedNodesEvaluation extends MasonEvaluation<NodeSetResult> {

    private static final long serialVersionUID = 1L;
    private Set<GPNode> used;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        
    }
    
    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(sim);
        this.used = new HashSet<>();
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(sim);
        SmartAgentProvider sp = (SmartAgentProvider) sim;
        List<? extends SmartAgent> agents = sp.getSmartAgents();
        for (SmartAgent a : agents) {
            GPArbitratorController arb = (GPArbitratorController) a.getAgentController();
            GPNode selected = arb.getLastSelectedNode();
            used.add(selected);
        }
    }

    @Override
    public NodeSetResult getResult() {
        return new NodeSetResult(used);
    }
}
