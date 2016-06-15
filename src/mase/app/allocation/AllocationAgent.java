/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.allocation;

import java.util.Arrays;
import mase.controllers.AgentController;

/**
 *
 * @author jorge
 */
public class AllocationAgent implements AgentController {

    private static final long serialVersionUID = 1L;
    private final double[] location;
    
    AllocationAgent(double[] location) {
        this.location = location;
    }

    public double[] getLocation() {
        return location;
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
        return new AllocationAgent(location);
    }

    @Override
    public String toString() {
        return Arrays.toString(location);
    }
    
    
    
}
