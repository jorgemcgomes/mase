/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.multirover;

import ec.EvolutionState;
import ec.util.Parameter;
import java.awt.Color;
import mase.app.multirover.RedRock.RockType;
import mase.controllers.GroupController;
import mase.mason.GUIState2D;
import mase.mason.MasonSimulationProblem;
import mase.mason.ParamUtils;
import sim.display.GUIState;

/**
 *
 * @author jorge
 */
public class MultiRoverSimulator extends MasonSimulationProblem {

    private static final long serialVersionUID = 1L;
    private MRParams par;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        par = new MRParams();
        ParamUtils.autoSetParameters(par, state.parameters, base, super.defaultBase(), true);
        
        // Validate rock types
        for(RockType r : par.rocks) {
            if(r.actuators.length > par.numAgents) {
                state.output.fatal("Invalid rock type: " + r.name() + ". More agents required than available.");
            }
            for(int a : r.actuators) {
                if(a != Rover.NO_ACTIVATION && a > par.numActuators - 1) {
                    state.output.fatal("Invalid rock type: " + r.name() + ". The required actuator does not exist.");
                }
            }
        }
    }

    @Override
    public MultiRover createSimState(GroupController gc, long seed) {
        return new MultiRover(seed, par, gc);
    }

    @Override
    public GUIState createSimStateWithUI(GroupController cs, long seed) {
        return new GUIState2D(createSimState(cs, seed), "Multi-rover", 500, 500, Color.WHITE);
    }
    
}
