/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.maze;

import java.awt.BasicStroke;
import java.awt.Color;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.mason.MasonSimState;
import mase.mason.world.CircularObject;
import mase.mason.world.MultilineObject;
import mase.mason.world.StaticPointObject;
import sim.field.continuous.Continuous2D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class MazeTask extends MasonSimState {

    private static final long serialVersionUID = 1L;

    protected MazeParams par;
    protected Continuous2D field;
    protected MazeAgent agent;
    protected CircularObject target;

    public MazeTask(long seed, MazeParams par, GroupController gc) {
        super(gc, seed);
        this.par = par;
    }

    @Override
    public void start() {
        super.start();
        field = new Continuous2D(20, par.maze.width, par.maze.height);
        MultilineObject maze = new MultilineObject(field, par.maze);
        maze.filled = false;
        maze.paint = Color.BLACK;
        maze.setStroke(new BasicStroke(2f));

        field.setObjectLocation(maze, new Double2D(0, 0));
        target = new CircularObject(Color.RED, this, field, par.targetRadius);
        field.setObjectLocation(target, par.targetPos);

        // Place agent
        AgentController ac = gc.getAgentControllers(1)[0];
        agent = new MazeAgent(this, field, ac);
        agent.setLocation(par.startPos);
        agent.setOrientation(par.startOrientation);
        schedule.scheduleRepeating(agent);
    }

    @Override
    public void setupPortrayal(FieldPortrayal2D port) {
        port.setField(field);
    }
}
