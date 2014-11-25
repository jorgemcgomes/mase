/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.maze;

import java.awt.Color;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.mason.GUICompatibleSimState;
import org.apache.commons.lang3.tuple.Pair;
import sim.field.continuous.Continuous2D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class MazeTask extends GUICompatibleSimState {
    
    private GroupController gc;
    protected MazeParams par;
    protected Continuous2D field;
    protected MazeAgent agent;

    public MazeTask(long seed, MazeParams par, GroupController gc) {
        super(seed);
        this.par = par;
        this.gc = gc;
    }

    @Override
    public void start() {
        super.start(); 
        // Setup maze
        field = new Continuous2D(20, par.maze.getWidth(), par.maze.getHeight());
        field.setObjectLocation(par.maze, new Double2D(0,0));
        field.setObjectLocation(new Target(par.targetRadius), par.targetPos);
        
        // Place agent
        AgentController ac = gc.getAgentControllers(1)[0];
        agent = new MazeAgent(this, field, ac);
        agent.setLocation(par.startPos);
        agent.setOrientation(par.startOrientation);
        schedule.scheduleRepeating(agent);
    }
    
    @Override
    public FieldPortrayal2D createFieldPortrayal() {
        return new ContinuousPortrayal2D();
    }

    @Override
    public void setupPortrayal(FieldPortrayal2D port) {
        port.setField(field);
    }
    
    protected static class Target extends OvalPortrayal2D {
        
        Target(double radius) {
            super(Color.RED, radius * 2, true);
        }
        
    }
    
}
