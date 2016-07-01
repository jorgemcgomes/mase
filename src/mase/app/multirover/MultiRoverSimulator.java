/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.multirover;

import ec.EvolutionState;
import ec.util.Parameter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import mase.controllers.GroupController;
import mase.mason.GUIState2D;
import mase.mason.MasonSimulationProblem;
import mase.mason.ParamUtils;
import org.apache.commons.lang3.ArrayUtils;
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
        
        // Parse rock types
        RockType[] allTypes = new RockType[par.numRockTypes];
        for(int i = 0 ; i < par.numRockTypes ; i++) {
            RockType t = new RockType(par.color[i], i, par.actuators[i], par.time[i], par.radius[i]);
            allTypes[i] = t;
        }
        
        // Parse rock distribution
        List<RockType> dist = new ArrayList<>();
        for(String rStr : par.rocks) {
            if(rStr.contains("*")) {
                String[] split = rStr.split("\\*");
                int num = Integer.parseInt(split[0].trim());
                int idx = Integer.parseInt(split[1].trim());
                for(int i = 0 ; i < num ; i++) {
                    dist.add(allTypes[idx]);
                }
            } else {
                int idx = Integer.parseInt(rStr);
                dist.add(allTypes[idx]);
            }
        }
        par.rockDistribution = dist.toArray(new RockType[dist.size()]);
        state.output.message("Using a total of " + dist.size() + " rocks");
        
        // Find the rocktypes that are actually used
        par.usedTypes = new LinkedList<>();
        for(RockType t : allTypes) {
            if(ArrayUtils.contains(par.rockDistribution, t)) {
                par.usedTypes.add(t);
            }
        }
        state.output.message("There is a total of " + par.usedTypes.size() + " unique types");
                
        // Determine the number of acuatorts for the used rock types
        par.numActuators = 0;
        for(RockType r : par.usedTypes) {
            for(int a : r.actuators) {
                par.numActuators = Math.max(0, a + 1);
            }
        }
        state.output.message("Auto determined number of rock actuators: " + par.numActuators);        
    }

    @Override
    public MultiRover createSimState(GroupController gc, long seed) {
        return new MultiRover(seed, par, gc);
    }

    @Override
    public GUIState createSimStateWithUI(GroupController cs, long seed) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int size = (int) screenSize.getHeight() - 200;
        return new GUIState2D(createSimState(cs, seed), "Multi-rover", size, size, Color.WHITE);
    }
    
}
