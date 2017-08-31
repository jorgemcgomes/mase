package mase.controllers;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public interface AgentControllerIndividual<T extends AgentController> {
    
    public T decodeController();
    
}
