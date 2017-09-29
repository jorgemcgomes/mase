/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import ec.EvolutionState;
import ec.Setup;

/**
 *
 * @author jorge
 */
public interface MappingFunction extends Setup {
    
    public void additionalSetup(EvolutionState state, Repertoire rep);
    
    public double[] outputToCoordinates(double[] arbOutput);
    
}
