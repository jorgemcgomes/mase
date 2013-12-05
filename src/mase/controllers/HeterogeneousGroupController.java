/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.controllers;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class HeterogeneousGroupController implements GroupController {

    private final AgentController[] controllers;
    
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
            s += "AC-" + i + ": " + controllers[i].toString()+"\n";
        }
        return s;
    }    
}
