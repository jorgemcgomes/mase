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
public interface GroupController extends Serializable {
    
    AgentController[] getAgentControllers(int nAgents);
    
}
