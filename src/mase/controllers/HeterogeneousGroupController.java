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
public class HeterogeneousGroupController implements GroupController {

    private AgentController[] controllers;
    
    public HeterogeneousGroupController(AgentController[] controllers) {
        this.controllers = controllers;
    }
    
    @Override
    public AgentController[] getAgentControllers(int n) {
        if(n != controllers.length) {
            return null;
        }
        return controllers;
    }

    @Override
    public String toString() {
        String s = "";
        for(int i = 0 ; i < controllers.length ; i++) {
            s += "Agent Controller " + i + ":\n" + controllers[i].toString()+"\n";
        }
        return s;
    }    
}
