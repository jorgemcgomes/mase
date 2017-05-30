/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import java.io.Serializable;

/**
 *
 * @author jorge
 */
public interface MappingFunction extends Serializable {
    
    public double[] outputToCoordinates(double[] arbOutput);
    
}
