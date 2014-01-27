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
public interface EnvironmentalFeature {
    
    public double distanceTo(Double2D position);
    
    public double[] getStateVariables();
    
}
