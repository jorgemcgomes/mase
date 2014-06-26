/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.mason.world;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author jorge
 */
public interface Sensor extends Cloneable {
    
    public void setAgent(SimState state, Continuous2D field, EmboddiedAgent ag);
    
    public int valueCount();
        
    public double[] readValues();
    
    public double[] normaliseValues(double[] vals);
    
}
