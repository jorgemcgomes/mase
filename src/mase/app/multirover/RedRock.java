/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.multirover;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Bag;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class RedRock extends OvalPortrayal2D implements Steppable {

    public enum RockType {

        A(Color.RED, new int[]{Rover.LOW, Rover.LOW}),
        B(Color.BLUE, new int[]{Rover.HIGH, Rover.HIGH}),
        C(Color.GREEN, new int[]{Rover.HIGH, Rover.LOW});

        private final Color color;
        private final int[] actuators;

        RockType(Color c, int[] actuators) {
            this.color = c;
            this.actuators = actuators;
        }

    }

    private final RockType type;
    private Stoppable stop;

    public RedRock(MultiRover sim, RockType type) {
        super(type.color, sim.par.rockRadius, true);
        this.type = type;
    }

    protected void setStopper(Stoppable stop) {
        this.stop = stop;
    }

    @Override
    public void step(SimState state) {
        MultiRover mr = (MultiRover) state;
        Double2D pos = mr.field.getObjectLocation(this);
        Bag neighbours = mr.field.getNeighborsExactlyWithinDistance(pos, mr.par.rockRadius);
        LinkedList<Integer> requiredAct = new LinkedList<Integer>();
        for (int t : type.actuators) {
            requiredAct.add(t);
        }

        for (Object n : neighbours.objs) {
            if (n instanceof Rover) {
                requiredAct.remove((Integer) ((Rover) n).getActuatorType());
            }
        }

        // has all the required types, remove the red rock
        if (requiredAct.isEmpty()) {
            stop.stop();
            mr.field.remove(this);
            mr.scores[type.ordinal()]++;
        }
    }

}
