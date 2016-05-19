/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.allocation;

import mase.controllers.AgentController;

/**
 *
 * @author jorge
 */
public class AllocationAgent implements AgentController {

    private static final long serialVersionUID = 1L;
    private final double[] type;
    
    AllocationAgent(double[] type) {
        this.type = type;
    }

    public double[] getType() {
        return type;
    }
    
    @Override
    public double[] processInputs(double[] input) {
        return null;
    }

    @Override
    public void reset() {
        ;
    }

    @Override
    public AgentController clone() {
        return new AllocationAgent(type);
    }
    
}
