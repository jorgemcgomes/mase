/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.multirover;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class RedRock extends OvalPortrayal2D implements Steppable {

    private static final long serialVersionUID = 1L;

    public enum RockType {

        A(Color.RED, new int[]{0, 0}),
        B(Color.BLUE, new int[]{1, 1}),
        C(Color.CYAN, new int[]{0, 1}),
        D(Color.GREEN, new int[]{Rover.NO_ACTIVATION, Rover.NO_ACTIVATION});

        public final Color color;
        public final int[] actuators;

        RockType(Color c, int[] actuators) {
            this.color = c;
            this.actuators = actuators;
        }
    }

    private final HashMap<Rover, Long> cache;
    private final RockType type;
    private Stoppable stop;

    public RedRock(MultiRover sim, RockType type) {
        super(type.color, sim.par.rockRadius * 2, true);
        this.type = type;
        this.cache = new HashMap<>();
    }

    protected void setStopper(Stoppable stop) {
        this.stop = stop;
    }

    protected RockType getType() {
        return type;
    }

    @Override
    public void step(SimState state) {
        MultiRover mr = (MultiRover) state;
        Double2D pos = mr.field.getObjectLocation(this);

        // find near rovers
        for (Rover r : mr.rovers) {
            if (r.getLocation().distance(pos) < mr.par.rockRadius) {
                Long time = cache.get(r);
                if (time == null) {
                    // rover just arrived at the rock, add it
                    cache.put(r, state.schedule.getSteps());
                } else if (r.lastActivation > time) {
                    // rover changed the actuator while in the rock
                    // update the timestamp accordingly
                    cache.put(r, r.lastActivation);
                }
                // else, nothing changed, leave it there
            } else {
                // rover is not near the rock, remove it
                cache.remove(r);
            }
        }

        if (cache.size() >= type.actuators.length) {
            LinkedList<Integer> requiredAct = new LinkedList<>();
            for (int t : type.actuators) {
                requiredAct.add(t);
            }

            // check if it has all the required types for long enough
            for (Entry<Rover, Long> e : cache.entrySet()) {
                if (state.schedule.getSteps() - e.getValue() >= mr.par.collectionTime) {
                    requiredAct.remove((Integer) e.getKey().getActuatorType());
                }
            }

            // has all the required types, remove the red rock
            if (requiredAct.isEmpty()) {
                stop.stop();
                mr.field.remove(this);
                mr.scores[type.ordinal()]++;
                mr.rocks.remove(this);
                if (mr.rocks.isEmpty()) {
                    mr.kill();
                }
            }
        }
    }
}
