/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.swarm;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import mase.controllers.AgentController;
import mase.mason.MasonSimState;
import mase.mason.generic.SmartAgentProvider;
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
public class SwarmPlayground extends MasonSimState<SwarmParams> implements SmartAgentProvider {

    private static final long serialVersionUID = 1L;

    protected Continuous2D field;
    protected List<SwarmAgent> agents;
    protected MultilineObject walls;
    protected List<MultilineObject> obstacles;
    protected List<POI> objects;

    public SwarmPlayground(long seed) {
        super(seed);
    }

    @Override
    public void start() {
        super.start();

        field = new Continuous2D(par.coneRange, par.arenaSize, par.arenaSize);

        placeWalls();
        placeObstacles();
        placePOIs();
        placeAgents();
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

    protected void placeObstacles() {
        // Add closed polygons of random size.
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

            // overlap with other obstacles
            boolean valid = true;
            for (MultilineObject other : obstacles) {
                if (obs.getPolygon().boundingBoxOverlap(other.getPolygon())) {
                    valid = false;
                    break;
                }
            }

            if (valid) {
                obstacles.add(obs);
            } else {
                field.remove(obs);
            }
        }

    }

    protected MultilineObject createObstacle() {
        double w = par.minObstacleSize + random.nextDouble() * (par.maxObstacleSize - par.minObstacleSize);
        double h = par.minObstacleSize + random.nextDouble() * (par.maxObstacleSize - par.minObstacleSize);
        PolygonObject po = new PolygonObject(field, new Double2D(0, 0), new Double2D(w, 0), new Double2D(w, h), new Double2D(0, h));
        return po;
    }

    protected void placePOIs() {
        int nObjects = par.maxObjects < par.minObjects ? 0
                : par.minObjects + (par.maxObjects > par.minObjects ? random.nextInt(par.maxObjects - par.minObjects) : 0);
        objects = new ArrayList<>(nObjects);
        while (objects.size() < nObjects) {
            Double2D candidate = new Double2D(par.objectRadius * 2 + (random.nextDouble() * (par.arenaSize - par.objectRadius * 4)),
                    par.objectRadius * 2 + (random.nextDouble() * (par.arenaSize - par.objectRadius * 4)));

            if (candidatePositionValid(candidate)) {
                POI co = createPOI();
                co.setLabel("P"+objects.size());
                co.setLocation(candidate);
                objects.add(co);
                co.stop = schedule.scheduleRepeating(co);
            }
        }
    }

    protected POI createPOI() {
        POI co = new POI(Color.BLUE, this, field, par.objectRadius, par.objectSpeed);
        return co;
    }

    protected void placeAgents() {
        int nAgents = par.minSwarmSize + random.nextInt(par.maxSwarmSize - par.minSwarmSize + 1);
        agents = new ArrayList<>(nAgents);
        AgentController[] acs = gc.getAgentControllers(nAgents);
        int index = 0;
        for (AgentController ac : acs) {
            SwarmAgent sa = createAgent(ac);
            Double2D pos = null;
            while (pos == null) {
                // leave at least half robot of distance to the boundaries
                pos = new Double2D(par.radius * 2 + (random.nextDouble() * (par.arenaSize - par.radius * 4)),
                        par.radius * 2 + (random.nextDouble() * (par.arenaSize - par.radius * 4)));
                if (!candidatePositionValid(pos)) {
                    pos = null;
                }
            }
            sa.setLocation(pos);
            sa.setOrientation(-Math.PI + random.nextDouble() * Math.PI * 2);
            sa.setLabel("A"+(index++));
            agents.add(sa);
            schedule.scheduleRepeating(sa);
        }
    }

    private boolean candidatePositionValid(Double2D pos) {
        // Check distance to other agents already placed
        if (agents != null) {
            for (SwarmAgent a : agents) {
                if (a.distanceTo(pos) < par.safetyDistance) {
                    return false;
                }
            }
        }
        // Check distance to the POIs
        if (objects != null) {
            for (POI p : objects) {
                if (p.distanceTo(pos) < par.safetyDistance) {
                    return false;
                }
            }
        }
        // Check with obstacles
        if (obstacles != null) {
            for (MultilineObject obs : obstacles) {
                if (obs.isInside(pos) || obs.distanceTo(pos) < par.safetyDistance) {
                    return false;
                }
            }
        }
        return true;
    }

    protected SwarmAgent createAgent(AgentController ac) {
        if (ac != null) {
            ac.reset();
        }
        SwarmAgent sa = new SwarmAgent(this, ac);
        sa.setLabel("");
        return sa;
    }

    @Override
    public void setupPortrayal(FieldPortrayal2D port) {
        port.setField(field);
    }

    @Override
    public List<? extends SmartAgent> getSmartAgents() {
        return agents;
    }
}
