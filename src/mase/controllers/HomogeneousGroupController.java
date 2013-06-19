/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.controllers;

import mase.AgentController;
import mase.GroupController;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class HomogeneousGroupController implements GroupController {
        
    private AgentController controller;
    
    public HomogeneousGroupController(AgentController controller) {
        this.controller = controller;
    }

    @Override
    public AgentController[] getAgentControllers(int nAgents) {
        AgentController[] acs = new AgentController[nAgents];
        for(int i = 0 ; i < nAgents ; i++) {
            acs[i] = controller.clone();
        }
        return acs;
    }
    
      @Override
    public String toString() {
        return controller.toString();
    } 
}
