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

    private static final long serialVersionUID = 1;
    private final AgentController[] controllers;
    
    public HeterogeneousGroupController(AgentController[] controllers) {
        this.controllers = controllers;
    }
    
    @Override
    public AgentController[] getAgentControllers(int n) {
        AgentController[] out = new AgentController[n];
        for(int i = 0 ; i < n ; i++) {
            out[i] = controllers[i].clone();
        }
        return out;
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
