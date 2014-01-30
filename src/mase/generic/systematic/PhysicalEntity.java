/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.generic.systematic;

/**
 *
 * @author jorge
 */
public interface PhysicalEntity {
        
    public boolean isAlive();
    
    public double[] getStateVariables();
    
    public double distance(PhysicalEntity other);
    
}
