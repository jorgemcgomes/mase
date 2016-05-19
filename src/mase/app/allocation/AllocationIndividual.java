/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.allocation;

import ec.vector.DoubleVectorIndividual;
import mase.controllers.AgentController;
import mase.controllers.AgentControllerIndividual;

/**
 *
 * @author jorge
 */
public class AllocationIndividual extends DoubleVectorIndividual implements AgentControllerIndividual {

    private static final long serialVersionUID = 1L;

    @Override
    public AgentController decodeController() {
        return new AllocationAgent(genome);
    }
    
}
