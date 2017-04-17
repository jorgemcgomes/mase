/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.evaluation.EvaluationResult;
import mase.mason.MasonSimState;
import mase.mason.generic.SmartAgentProvider;
import mase.mason.world.CircularObject;
import mase.mason.world.DashMovementEffector;
import mase.mason.world.DistanceSensorArcs;
import mase.mason.world.MultilineObject;
import mase.mason.world.RaySensor;
import mase.mason.world.SmartAgent;
import mase.mason.world.WorldObject;
import sim.field.continuous.Continuous2D;
import sim.portrayal.FieldPortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class Playground extends MasonSimState implements SmartAgentProvider {

    private static final long serialVersionUID = 1L;

    protected MultilineObject walls;
    protected Continuous2D field;
    protected PlaygroundParams par;
    protected SmartAgent agent;

    public Playground(GroupController gc, long seed, PlaygroundParams par) {
        super(gc, seed);
        this.par = par;
    }

    @Override
    public void start() {
        super.start();

        field = new Continuous2D(par.coneRange, par.arenaSize, par.arenaSize);

        walls = new MultilineObject(field,
                new Double2D(0, 0),
                new Double2D(par.arenaSize, 0),
                new Double2D(par.arenaSize, par.arenaSize),
                new Double2D(0, par.arenaSize),
                new Double2D(0, 0));
        field.setObjectLocation(walls, new Double2D(0, 0));

        agent = createAgent();
        agent.enableBoundedArena(true);
        agent.enableCollisionRebound(true);
        agent.setCollidableTypes(WorldObject.class);

        // leave at least one robot of distance to the boundaries
        Double2D pos = new Double2D(par.radius * 2 + (random.nextDouble() * (par.arenaSize - par.radius * 4)),
                par.radius * 2 + (random.nextDouble() * (par.arenaSize - par.radius * 4)));
        agent.setLocation(pos);
        agent.setOrientation(-Math.PI + random.nextDouble() * Math.PI * 2);
        schedule.scheduleRepeating(agent);
        
        randomFill();

    }

    protected SmartAgent createAgent() {
        AgentController ac = gc.getAgentControllers(1)[0];
        SmartAgent sa = new SmartAgent(this, field, par.radius, Color.RED, ac);

        // Obstacle whisker sensor
        RaySensor dw = new RaySensor(this, field, sa);
        dw.setRays(par.whiskerRange, -Math.PI / 6, Math.PI / 6);
        dw.setObjectTypes(MultilineObject.class);
        dw.setBinary(false);
        sa.addSensor(dw);

        // Object cone sensor
        DistanceSensorArcs dsr = new DistanceSensorArcs(this, field, sa);
        dsr.setArcs(par.numCones);
        dsr.setRange(par.coneRange);
        dsr.setObjectTypes(CircularObject.class);
        dsr.centerToCenter(false);
        dsr.setBinary(false);
        sa.addSensor(dsr);

        // Effector
        DashMovementEffector dm = new DashMovementEffector(this, field, sa);
        dm.setSpeeds(par.linearSpeed, par.turnSpeed);
        dm.allowBackwardMove(false);
        sa.addEffector(dm);

        return sa;
    }

    protected void randomFill() {
        int objects = par.minObjects + (par.maxObjects > par.minObjects ? random.nextInt(par.maxObjects - par.minObjects) : 0);

        if (par.maxObstacles > 0) {
            // Add closed polygons of random size. The addition of objects next must ensure no overlaps
            throw new UnsupportedOperationException("Obstacles not implemented yet");
        }

        ArrayList<WorldObject> added = new ArrayList<>();
        added.add(agent);

        for (int i = 0; i < objects; i++) {
            Double2D candidate = new Double2D(par.objectRadius * 2 + (random.nextDouble() * (par.arenaSize - par.objectRadius * 4)),
                    par.objectRadius * 2 + (random.nextDouble() * (par.arenaSize - par.objectRadius * 4)));
            boolean valid = true;
            for (WorldObject wo : added) {
                // leave at least one object of distance
                if (wo.distanceTo(candidate) < par.objectRadius * 2) {
                    valid = false;
                    break;
                }
            }

            if (valid) {
                CircularObject co = new CircularObject(Color.BLUE, this, field, par.objectRadius);
                co.setLocation(candidate);
                added.add(co);
            }
        }
    }

    @Override
    public void setupPortrayal(FieldPortrayal2D port) {
        port.setField(field);
    }

    @Override
    public List<? extends SmartAgent> getSmartAgents() {
        return Collections.singletonList(agent);
    }


}
