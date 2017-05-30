/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.controllers;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class HomogeneousGroupController implements GroupController {
        
    private static final long serialVersionUID = 1;
    private final AgentController controller;
    
    public HomogeneousGroupController(AgentController controller) {
        this.controller = controller;
    }

    @Override
    public AgentController[] getAgentControllers(int nAgents) {
        AgentController[] acs = new AgentController[nAgents];
        for(int i = 0 ; i < nAgents ; i++) {
            acs[i] = controller == null ? null : controller.clone();
        }
        return acs;
    }
    
      @Override
    public String toString() {
        return controller.toString();
    } 
}
