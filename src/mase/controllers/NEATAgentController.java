/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.controllers;

import ec.neat.NEATNetwork;

/**
 *
 * @author jorge
 */
public class NEATAgentController implements AgentController {

    private static final long serialVersionUID = 1L;
    
    private final NEATNetwork net;
    
    public NEATAgentController(NEATNetwork net) {
        this.net = net;
    }

    @Override
    public double[] processInputs(double[] input) {
        // TODO: bias is manually added here, this shouldn't be needed...
        double[] withBias = new double[input.length + 1];
        withBias[0] = 1;
        System.arraycopy(input, 0, withBias, 1, input.length);
        net.loadSensors(withBias);
        net.activate(null);
        return net.getOutputResults();
    }

    @Override
    public void reset() {
        net.flush();
    }

    @Override
    public AgentController clone() {
        // TODO: this SHOULD not use the individual
        //NEATNetwork clone = net.individual.createNetwork();
        return new NEATAgentController((NEATNetwork) net.clone());
    }
    
}
