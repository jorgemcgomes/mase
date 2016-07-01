/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.multirover;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.mason.MasonSimState;
import mase.mason.world.StaticPolygon;
import sim.field.continuous.Continuous2D;
import sim.portrayal.FieldPortrayal2D;
import sim.util.Bag;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class MultiRover extends MasonSimState {

    private static final long serialVersionUID = 1L;

    protected Continuous2D field;
    protected MRParams par;
    protected final MRParams originalPar;
    protected int[] scores;
    protected List<Rover> rovers;
    protected List<Rock> rocks;

    public MultiRover(long seed, MRParams par, GroupController gc) {
        super(gc, seed);
        this.originalPar = par;
    }

    @Override
    public void start() {
        super.start();
        
        par = originalPar.clone();
        par.linearSpeed += par.linearSpeed * par.actuatorOffset * (random.nextDouble() * 2 - 1);
        par.turnSpeed += par.turnSpeed * par.actuatorOffset * (random.nextDouble() * 2 - 1);
        par.sensorRange += par.sensorRange * par.sensorOffset * (random.nextDouble() * 2 - 1);
        
        this.field = new Continuous2D(par.discretization, par.size, par.size);
        this.scores = new int[par.usedTypes.size()];
        Arrays.fill(scores, 0);

        StaticPolygon walls = new StaticPolygon(new Double2D[]{
            new Double2D(0, 0),
            new Double2D(par.size, 0),
            new Double2D(par.size, par.size),
            new Double2D(0, par.size),
            new Double2D(0, 0)
        });
        walls.filled = false;
        field.setObjectLocation(walls, new Double2D(0, 0));

        placeRocks();
        placeRovers();

    }

    protected void placeRovers() {
        // align predators in a row in the bottom
        AgentController[] controllers = gc.getAgentControllers(par.numAgents);
        rovers = new ArrayList<>(par.numAgents);
        for (int i = 0; i < par.numAgents;) {
            double x = par.agentRadius + random.nextDouble() * (par.size - par.agentRadius * 2);
            double y = par.agentRadius + random.nextDouble() * (par.size - par.agentRadius * 2);
            Double2D newLoc = new Double2D(x, y);
            boolean check = true;
            for (Rover r : rovers) {
                if (r.getLocation().distance(newLoc) < par.sensorRange + par.agentRadius * 2) {
                    check = false;
                    break;
                }
            }

            if (check) {
                AgentController controller = controllers[i].clone();
                controller.reset();
                Rover rover = new Rover(this, field, controller);
                rover.setLocation(newLoc);
                rover.setOrientation(random.nextDouble() * Math.PI * 2 - Math.PI);
                rover.setStopper(schedule.scheduleRepeating(rover));
                rover.setLabel("R"+i);
                rovers.add(rover);
                i++;
            }
        }

    }

    protected void placeRocks() {
        
        this.rocks = new LinkedList<>();
        int count = 0;
        while (count < par.rockDistribution.length) {
            RockType type = par.rockDistribution[count];
            double x = type.radius + random.nextDouble() * (par.size - type.radius * 2);
            double y = type.radius + random.nextDouble() * (par.size - type.radius * 2);
            Bag close = field.getNeighborsExactlyWithinDistance(new Double2D(x, y), type.radius * 4);
            if (!close.isEmpty()) {
                continue;
            }
            Rock newRock = new Rock(this, type);
            newRock.setLabel(Arrays.toString(type.actuators));
            newRock.setLocation(new Double2D(x, y));
            newRock.setStopper(schedule.scheduleRepeating(newRock));
            count++;
            rocks.add(newRock);
        }

    }

    @Override
    public void setupPortrayal(FieldPortrayal2D port) {
        port.setField(field);
    }

}
