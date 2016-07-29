/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.soccer;

import ec.EvolutionState;
import ec.util.Parameter;
import java.awt.Color;
import mase.controllers.GroupController;
import mase.mason.GUIState2D;
import mase.mason.MasonSimState;
import mase.mason.MasonSimulationProblem;
import mase.mason.ParamUtils;
import sim.display.GUIState;

/**
 *
 * @author jorge
 */
public class SoccerSimulator extends MasonSimulationProblem {

    private static final long serialVersionUID = 1L;
    private SoccerParams par;
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        par = new SoccerParams();
        ParamUtils.autoSetParameters(par, state.parameters, base, defaultBase(), true);
        
    }

    @Override
    public MasonSimState createSimState(GroupController gc, long seed) {
        return new Soccer(seed, par, gc);
    }
    
    @Override
    public GUIState createSimStateWithUI(GroupController gc, long seed) {
        Soccer sim = (Soccer) createSimState(gc, seed);
        double w = sim.par.fieldLength;
        double h = sim.par.fieldWidth;
        double ratio = 1000 / Math.max(w, h);
        return new GUIState2D(sim, "Soccer",
                (int) Math.round(w * ratio), (int) Math.round(h * ratio), Color.GREEN.darker());
    }    
    
    public static void main(String[] args) {
        SoccerSimulator ss = new SoccerSimulator();
        ss.par = new SoccerParams();
        ss.par.formation = new int[]{1,2,2};
        //ss.par.fieldLength = 400;
        //ss.par.fieldWidth = 225;
        //ss.par.goalWidth = 50;
        
        GUIState gui = ss.createSimStateWithUI(null, 0);
        gui.createController();
        
        /*Soccer sim = (Soccer) ss.createSimState(null, new Random().nextLong());
        sim.start();
        for(int i = 0 ; i < 100000 ; i++) {
            sim.schedule.step(sim);
        }
        System.out.println(sim.referee.leftTeamScore + " | " + sim.referee.rightTeamScore);   */
    }
    
}
