/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.maze;

import java.awt.BasicStroke;
import java.awt.Color;
import mase.controllers.AgentController;
import mase.mason.MasonSimState;
import mase.mason.world.CircularObject;
import mase.mason.world.MultilineObject;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.portrayal.FieldPortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class MazeTask extends MasonSimState<MazeParams> {

    private static final long serialVersionUID = 1L;

    protected Continuous2D field;
    protected MazeAgent agent;
    protected CircularObject target;
    protected MultilineObject maze;
    protected SpeedingMonitor speedingMonitor;

    public MazeTask(long seed) {
        super(seed);
    }

    @Override
    public void start() {
        super.start();
        int mid = getRepetitionNumber() % par.maze.length;
        field = new Continuous2D(20, par.maze[mid].width, par.maze[mid].height);
        maze = new MultilineObject(field, par.maze[mid]);
        maze.filled = false;
        maze.paint = Color.BLACK;
        maze.setStroke(new BasicStroke(2f));
        maze.setLocation(new Double2D(0, 0));

        target = new CircularObject(Color.RED, this, field, par.targetRadius);
        target.setLocation(par.targetPos[mid]);

        Double2D startPos = null;
        if (par.xRandom > 0 || par.yRandom > 0) {
            while (startPos == null) {
                Double2D rand = new Double2D(par.startPos[mid].x + (random.nextDouble() * 2 - 1) * par.xRandom,
                        par.startPos[mid].y + (random.nextDouble() * 2 - 1) * par.yRandom);
                if (par.maze[mid].isInsideBB(rand) && maze.distanceTo(rand) > par.agentRadius) {
                    startPos = rand;
                }
            }
        } else {
            startPos = par.startPos[mid];
        }

        // Place agent
        agent = createAgent();
        agent.setLocation(startPos);
        agent.setOrientation(par.startOrientation[mid]);
        schedule.scheduleRepeating(agent);
        
        if(par.zonesMaxSpeed != null && par.zonesMaxSpeed.length > 0) {
            speedingMonitor = new SpeedingMonitor();
            schedule.scheduleRepeating(speedingMonitor);
        }
    }

    protected MazeAgent createAgent() {
        AgentController ac = gc.getAgentControllers(1)[0];
        MazeAgent ag = new MazeAgent(this, field, ac);
        ag.setupSensors(this);
        ag.setupActuators(this);
        return ag;
    }

    @Override
    public void setupPortrayal(FieldPortrayal2D port) {
        port.setField(field);
    }

    protected class SpeedingMonitor implements Steppable {

        private static final long serialVersionUID = 1L;
        protected double speedingAccum = 0;
        protected int speedingSteps = 0;

        @Override
        public void step(SimState state) {
            double speedLimit = getZoneSpeedLimit();
            if (Math.abs(agent.getSpeed()) > speedLimit) {
                speedingAccum += Math.abs(agent.getSpeed()) - speedLimit;
                speedingSteps++;
            }
            if ((par.maxSpeedingAccum != 0 && speedingAccum > par.maxSpeedingAccum)
                    || (par.maxSpeedingSteps != 0 && speedingSteps > par.maxSpeedingSteps)) {
                kill();
            }
        }
        
        public double getZoneSpeedLimit() {
            if (par.zonesMaxSpeed == null || agent == null || agent.getLocation() == null) {
                return par.linearSpeed;
            }
            double speedLimit = 1;
            for (int i = 0; i < par.zonesMaxSpeed.length; i++) {
                Double2D up = par.zonesUpperCorner[i];
                Double2D low = par.zonesLowerCorner[i];
                Double2D pos = agent.getLocation();
                if (pos.x >= low.x && pos.x <= up.x && pos.y >= low.y && pos.y <= up.y) { // is inside zone i
                    speedLimit = Math.min(speedLimit, par.zonesMaxSpeed[i]);
                }
            }
            return speedLimit * par.linearSpeed;
        }        

    }
}
