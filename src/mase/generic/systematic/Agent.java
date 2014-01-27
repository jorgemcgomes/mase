/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.generic.systematic;

import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public interface Agent {
    
    public Double2D getPosition();
    
    public boolean isAlive();
    
    public double[] getStateVariables();
    
}
