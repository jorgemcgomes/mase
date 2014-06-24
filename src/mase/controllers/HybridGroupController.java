/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.controllers;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author jorge
 */
public class HybridGroupController implements GroupController {

    private static final long serialVersionUID = 1;
    //private final Pair<AgentController, List<Integer>>[] allocations;
    private final AgentController[] acs;

    public HybridGroupController(Pair<AgentController, List<Integer>>[] controllers) {
        //this.allocations = controllers;
        AgentController[] temp = new AgentController[100];
        int count = 0;
        for (Pair<AgentController, List<Integer>> p : controllers) {
            for (Integer i : p.getRight()) {
                //System.out.println(i);
                temp[i] = p.getLeft().clone();
                count++;
            }
        }

        acs = Arrays.copyOf(temp, count);
    }

    @Override
    public AgentController[] getAgentControllers(int nAgents) {
        if (acs.length != nAgents) {
            throw new RuntimeException("Unexpected controller size. Wanted: " + nAgents + ". Has: " + acs.length);
        }
        return acs;
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < acs.length; i++) {
            s += "AC-" + i + ": " + acs[i].toString() + "\n";
        }
        return s;
    }
}
