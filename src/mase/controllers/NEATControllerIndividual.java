/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.controllers;

import ec.neat.NEATIndividual;
import ec.neat.NEATNetwork;

/**
 *
 * @author jorge
 */
public class NEATControllerIndividual extends NEATIndividual implements AgentControllerIndividual {

    private static final long serialVersionUID = 1L;

    @Override
    public AgentController decodeController() {
        NEATNetwork net = createNetwork();
        return new NEATAgentController(net);
    }
    
    
    
}
