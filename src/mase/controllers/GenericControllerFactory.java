/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.controllers;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import mase.spec.AbstractHybridExchanger;

/**
 *
 * @author jorge
 */
public class GenericControllerFactory implements ControllerFactory {

    private static final long serialVersionUID = 1L;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        // nothing to setup
    }
    
    @Override
    public GroupController createController(EvolutionState state, Individual... inds) {
        ArrayList<AgentController> acs = new ArrayList<>();
        for (Individual ind : inds) {
            if (ind instanceof AgentControllerIndividual) {
                acs.add(((AgentControllerIndividual) ind).decodeController());
            } else if (ind instanceof MultiAgentControllerIndividual) {
                AgentController[] as = ((MultiAgentControllerIndividual) ind).decodeControllers();
                acs.addAll(Arrays.asList(as));
            }
        }
        GroupController gc;
        if (acs.size() == 1) {
            gc = new HomogeneousGroupController(acs.get(0));
        } else {
            AgentController[] acsArray = new AgentController[acs.size()];
            acs.toArray(acsArray);
            
            if (state.exchanger instanceof AbstractHybridExchanger) {
                AbstractHybridExchanger exc = (AbstractHybridExchanger) state.exchanger;
                int[] allocations = exc.getAllocations();
                AgentController[] temp = new AgentController[allocations.length];
                for (int i = 0; i < allocations.length; i++) {
                    temp[i] = acsArray[allocations[i]].clone();
                }
                acsArray = temp;
            }
            gc = new HeterogeneousGroupController(acsArray);
        }
        return gc;
    }
}
