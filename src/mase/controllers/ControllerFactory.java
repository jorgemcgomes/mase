/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.controllers;

import ec.EvolutionState;
import ec.Individual;
import ec.Singleton;

/**
 *
 * @author jorge
 */
public interface ControllerFactory extends Singleton {
        
    public GroupController createController(EvolutionState state, Individual... inds);
    
}
