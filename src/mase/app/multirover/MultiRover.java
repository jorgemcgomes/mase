/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.multirover;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.mason.GUICompatibleSimState;
import sim.field.continuous.Continuous2D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.util.Bag;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class MultiRover extends GUICompatibleSimState {

    protected Continuous2D field;
    protected GroupController gc;
    protected MRParams par;
    protected int[] scores;
    protected List<Rover> rovers;

    public MultiRover(long seed, MRParams par, GroupController gc) {
        super(seed);
        this.par = par;
        this.gc = gc;
        this.scores = new int[RedRock.RockType.values().length];

    }

    @Override
    public void start() {
        super.start();
        this.field = new Continuous2D(par.discretization, par.size, par.size);
        Arrays.fill(scores, 0);
        placeRocks();
        placeRovers();
    }

    protected void placeRovers() {
        // align predators in a row in the bottom
        double w = par.separation * (par.numAgents - 1);
        double y = Rover.RADIUS * 2;
        double startX = (field.width - w) / 2;
        AgentController[] controllers = gc.getAgentControllers(par.numAgents);
        rovers = new ArrayList<Rover>(par.numAgents);
        for (int i = 0; i < par.numAgents; i++) {
            double x = startX + i * par.separation;
            AgentController controller = controllers[i].clone();
            controller.reset();
            Rover rover = new Rover(this, field, controller);
            rover.setLocation(new Double2D(x, y));
            rover.setOrientation(Math.PI / 2);
            rover.setStopper(schedule.scheduleRepeating(rover));
            rovers.add(rover);
        }
    }

    protected void placeRocks() {
        /*int count = 0;
        int types = RedRock.RockType.values().length;
        while (count < par.numRocks) {
            double x = par.rockRadius * 2 + random.nextDouble() * (par.size - par.rockRadius * 4);
            double y = par.rockRadius * 2 + random.nextDouble() * (par.size - par.rockRadius * 4);
            Bag close = field.getNeighborsExactlyWithinDistance(new Double2D(x, y), par.rockRadius * 4);
            if (!close.isEmpty()) {
                continue;
            }
            RedRock newRock = new RedRock(this, RedRock.RockType.values()[Math.min(count / (par.numRocks / types), types - 1)]);
            field.setObjectLocation(newRock, new Double2D(x, y));
            newRock.setStopper(schedule.scheduleRepeating(newRock));
            count++;
        }*/

        RedRock rock = new RedRock(this, RedRock.RockType.A);
        field.setObjectLocation(rock, new Double2D(0.25 * field.width, 0.5 * field.height));
        rock.setStopper(schedule.scheduleRepeating(rock));
        rock = new RedRock(this, RedRock.RockType.A);
        field.setObjectLocation(rock, new Double2D(0.25 * field.width, 0.75 * field.height));
        rock.setStopper(schedule.scheduleRepeating(rock));

        rock = new RedRock(this, RedRock.RockType.B);
        field.setObjectLocation(rock, new Double2D(0.50 * field.width, 0.5 * field.height));
        rock.setStopper(schedule.scheduleRepeating(rock));
        rock = new RedRock(this, RedRock.RockType.B);
        field.setObjectLocation(rock, new Double2D(0.50 * field.width, 0.75 * field.height));
        rock.setStopper(schedule.scheduleRepeating(rock));

        rock = new RedRock(this, RedRock.RockType.C);
        field.setObjectLocation(rock, new Double2D(0.75 * field.width, 0.5 * field.height));
        rock.setStopper(schedule.scheduleRepeating(rock));
        rock = new RedRock(this, RedRock.RockType.C);
        field.setObjectLocation(rock, new Double2D(0.75 * field.width, 0.75 * field.height));
        rock.setStopper(schedule.scheduleRepeating(rock));
    }

    @Override
    public FieldPortrayal2D createFieldPortrayal() {
        return new ContinuousPortrayal2D();
    }

    @Override
    public void setupPortrayal(FieldPortrayal2D port) {
        port.setField(field);
    }

}
