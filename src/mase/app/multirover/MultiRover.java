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
import mase.mason.GUICompatibleSimState;
import mase.mason.world.StaticPolygon;
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
    protected List<RedRock> rocks;

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

        StaticPolygon walls = new StaticPolygon(new Double2D[]{
            new Double2D(0, 0),
            new Double2D(par.size, 0),
            new Double2D(par.size, par.size),
            new Double2D(0, par.size),
            new Double2D(0, 0)
        });
        field.setObjectLocation(walls, new Double2D(0, 0));

        placeRocks();
        placeRovers();

    }

    protected void placeRovers() {
        // align predators in a row in the bottom
        AgentController[] controllers = gc.getAgentControllers(par.numAgents);
        rovers = new ArrayList<Rover>(par.numAgents);
        for (int i = 0; i < par.numAgents;) {
            double x = Rover.RADIUS + random.nextDouble() * (par.size - Rover.RADIUS * 2);
            double y = Rover.RADIUS + random.nextDouble() * (par.size - Rover.RADIUS * 2);
            Double2D newLoc = new Double2D(x, y);
            boolean check = true;
            for (Rover r : rovers) {
                if (r.getLocation().distance(newLoc) < par.sensorRange + Rover.RADIUS * 2) {
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
                rovers.add(rover);
                i++;
            }
        }

    }

    protected void placeRocks() {

        this.rocks = new LinkedList<RedRock>();
        int count = 0;
        while (count < par.numRocks) {
            double x = par.rockRadius + random.nextDouble() * (par.size - par.rockRadius * 2);
            double y = par.rockRadius + random.nextDouble() * (par.size - par.rockRadius * 2);
            Bag close = field.getNeighborsExactlyWithinDistance(new Double2D(x, y), par.rockRadius * 4);
            if (!close.isEmpty()) {
                continue;
            }
            RedRock newRock = new RedRock(this, RedRock.RockType.C);
            field.setObjectLocation(newRock, new Double2D(x, y));
            newRock.setStopper(schedule.scheduleRepeating(newRock));
            count++;
            rocks.add(newRock);
        }

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

       /*RedRock rock = new RedRock(this, RedRock.RockType.C);
         field.setObjectLocation(rock, new Double2D(0.1 * field.width, 0.3 * field.height));
         rock.setStopper(schedule.scheduleRepeating(rock));
         rocks.add(rock);

         rock = new RedRock(this, RedRock.RockType.C);
         field.setObjectLocation(rock, new Double2D(0.366 * field.width, 0.3 * field.height));
         rock.setStopper(schedule.scheduleRepeating(rock));
         rocks.add(rock);

         rock = new RedRock(this, RedRock.RockType.C);
         field.setObjectLocation(rock, new Double2D(0.633 * field.width, 0.3 * field.height));
         rock.setStopper(schedule.scheduleRepeating(rock));
         rocks.add(rock);

         rock = new RedRock(this, RedRock.RockType.C);
         field.setObjectLocation(rock, new Double2D(0.9 * field.width, 0.3 * field.height));
         rock.setStopper(schedule.scheduleRepeating(rock));
         rocks.add(rock);

         rock = new RedRock(this, RedRock.RockType.C);
         field.setObjectLocation(rock, new Double2D(0.233 * field.width, 0.6 * field.height));
         rock.setStopper(schedule.scheduleRepeating(rock));
         rocks.add(rock);

         rock = new RedRock(this, RedRock.RockType.C);
         field.setObjectLocation(rock, new Double2D(0.5 * field.width, 0.6 * field.height));
         rock.setStopper(schedule.scheduleRepeating(rock));
         rocks.add(rock);

         rock = new RedRock(this, RedRock.RockType.C);
         field.setObjectLocation(rock, new Double2D(0.766 * field.width, 0.6 * field.height));
         rock.setStopper(schedule.scheduleRepeating(rock));
         rocks.add(rock);

         rock = new RedRock(this, RedRock.RockType.C);
         field.setObjectLocation(rock, new Double2D(0.1 * field.width, 0.9 * field.height));
         rock.setStopper(schedule.scheduleRepeating(rock));
         rocks.add(rock);

         rock = new RedRock(this, RedRock.RockType.C);
         field.setObjectLocation(rock, new Double2D(0.366 * field.width, 0.9 * field.height));
         rock.setStopper(schedule.scheduleRepeating(rock));
         rocks.add(rock);

         rock = new RedRock(this, RedRock.RockType.C);
         field.setObjectLocation(rock, new Double2D(0.633 * field.width, 0.9 * field.height));
         rock.setStopper(schedule.scheduleRepeating(rock));
         rocks.add(rock);

         rock = new RedRock(this, RedRock.RockType.C);
         field.setObjectLocation(rock, new Double2D(0.9 * field.width, 0.9 * field.height));
         rock.setStopper(schedule.scheduleRepeating(rock));
         rocks.add(rock);*/
