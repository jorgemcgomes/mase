/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.controllers;

import java.io.Serializable;

/**
 *
 * @author jorge
 */
public interface AgentController extends Serializable {
    
    public double[] processInputs(double[] input);
    
    public void reset();
    
    public AgentController clone();
    
}
