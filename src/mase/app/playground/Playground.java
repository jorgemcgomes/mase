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
import mase.mason.MasonSimState;
import mase.mason.generic.SmartAgentProvider;
import mase.mason.world.CircularObject;
import mase.mason.world.MultilineObject;
import mase.mason.world.PolygonObject;
import mase.mason.world.SmartAgent;
import org.apache.commons.lang3.tuple.Pair;
import sim.field.continuous.Continuous2D;
import sim.portrayal.FieldPortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class Playground extends MasonSimState<PlaygroundParams> implements SmartAgentProvider {

    private static final long serialVersionUID = 1L;

    protected MultilineObject walls;
    protected Continuous2D field;
    protected SmartAgent agent;
    protected List<MultilineObject> obstacles;
    protected List<CircularObject> objects;
    protected AgentController ac;

    public Playground(long seed) {
        super(seed);
    }
    
    @Override
    public void setGroupController(GroupController gc) {
        super.setGroupController(gc);
        this.ac = gc.getAgentControllers(1)[0];
    }

    @Override
    public void start() {
        super.start();

        field = new Continuous2D(par.coneRange, par.arenaSize, par.arenaSize);
        placeWalls();

        agent = createAgent();
        if (agent.getAgentController() != null) {
            agent.getAgentController().reset();
        }
        placeAgent(agent);
        schedule.scheduleRepeating(agent);

        placeObstacles();

        placeObjects();
        
        agent.setLabel("");
        for(CircularObject o : objects) {
            o.setLabel("");
        }
    }

    protected void placeWalls() {
        walls = new MultilineObject(field,
                new Double2D(0, 0),
                new Double2D(par.arenaSize, 0),
                new Double2D(par.arenaSize, par.arenaSize),
                new Double2D(0, par.arenaSize),
                new Double2D(0, 0));
        field.setObjectLocation(walls, new Double2D(0, 0));
    }

    protected void placeAgent(SmartAgent ag) {
        if (par.randomPosition) {
            // leave at least half robot of distance to the boundaries
            Double2D pos = new Double2D(par.radius * 2 + (random.nextDouble() * (par.arenaSize - par.radius * 4)),
                    par.radius * 2 + (random.nextDouble() * (par.arenaSize - par.radius * 4)));
            ag.setLocation(pos);
            ag.setOrientation(-Math.PI + random.nextDouble() * Math.PI * 2);
        } else {
            // start in the middle
            ag.setLocation(new Double2D(par.arenaSize / 2, par.arenaSize / 2));
            ag.setOrientation(-Math.PI + random.nextDouble() * Math.PI * 2);
        }
    }

    protected void placeObstacles() {
        // Add closed polygons of random size. The addition of objects next must ensure no overlaps
        int nObstacles = par.minObstacles + (par.maxObstacles > par.minObstacles ? random.nextInt(par.maxObstacles - par.minObstacles) : 0);
        obstacles = new ArrayList<>(nObstacles);
        while (obstacles.size() < nObstacles) {
            MultilineObject obs = createObstacle();
            Pair<Double2D, Double2D> bb = obs.getPolygon().boundingBox;
            double w = bb.getRight().x - bb.getLeft().x;
            double h = bb.getRight().y - bb.getLeft().y;
            Double2D candidate = new Double2D(random.nextDouble() * (par.arenaSize - w),
                    random.nextDouble() * (par.arenaSize - h));
            obs.setLocation(candidate);

            // overlap with the agent
            boolean valid = !obs.isInside(agent.getLocation()) && obs.distanceTo(agent.getLocation()) > agent.getRadius();

            if (valid) {
                // overlap with other obstacles
                for (MultilineObject other : obstacles) {
                    if (obs.getPolygon().boundingBoxOverlap(other.getPolygon())) {
                        valid = false;
                        break;
                    }
                }
            }

            if (valid) {
                obstacles.add(obs);
            } else {
                field.remove(obs);
            }
        }

    }

    protected void placeObjects() {
        int nObjects = par.minObjects + (par.maxObjects > par.minObjects ? random.nextInt(par.maxObjects - par.minObjects) : 0);
        objects = new ArrayList<>(nObjects);

        while (objects.size() < nObjects) {
            Double2D candidate = new Double2D(par.objectRadius * 2 + (random.nextDouble() * (par.arenaSize - par.objectRadius * 4)),
                    par.objectRadius * 2 + (random.nextDouble() * (par.arenaSize - par.objectRadius * 4)));

            // distance to the agent
            boolean valid = agent.distanceTo(candidate) > Math.max(par.minObjectDistance, agent.getRadius());

            // check collisions with other objects
            if (valid) {
                for (CircularObject wo : objects) {
                    // do not overlap objects
                    if (wo.distanceTo(candidate) < par.objectRadius) {
                        valid = false;
                        break;
                    }
                }
            }

            // check overlap with obstacles
            if (valid) {
                for (MultilineObject mo : obstacles) {
                    if (mo.isInside(candidate) || mo.distanceTo(candidate) < par.objectRadius) {
                        valid = false;
                        break;
                    }
                }
            }

            if (valid) {
                CircularObject co = createObject();
                co.setLocation(candidate);
                objects.add(co);
            }
        }
    }

    protected CircularObject createObject() {
        return new CircularObject(Color.BLUE, this, field, par.objectRadius);
    }

    protected MultilineObject createObstacle() {
        double w = par.minObstacleSize + random.nextDouble() * (par.maxObstacleSize - par.minObstacleSize);
        double h = par.minObstacleSize + random.nextDouble() * (par.maxObstacleSize - par.minObstacleSize);
        PolygonObject po = new PolygonObject(field, new Double2D(0, 0), new Double2D(w, 0), new Double2D(w, h), new Double2D(0, h));
        return po;
    }

    protected SmartAgent createAgent() {
        ac.reset();
        return new PlaygroundAgent(this, ac);
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
