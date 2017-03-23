/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.multirover;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import mase.mason.world.CircularObject;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.portrayal.simple.OvalPortrayal2D;

/**
 *
 * @author jorge
 */
public class Rock extends CircularObject implements Steppable {

    private static final long serialVersionUID = 1L;

    private final HashMap<Rover, Pair<Long,Integer>> cache;
    private final RockType type;
    private Stoppable stop;

    public Rock(MultiRover sim, RockType type) {
        super(new OvalPortrayal2D(type.color, type.radius * 2, true), sim, sim.field, type.radius);
        this.setColor(type.color);
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

        // find near rovers
        for (Rover r : mr.rovers) {
            if (r.getCenterLocation().distance(pos) <= type.radius) {
                Pair<Long, Integer> mem = cache.get(r);
                if (mem == null || mem.getRight() != r.getActuatorType()) {
                    // rover just arrived at this rock or the actuator changed
                    cache.put(r, Pair.of(state.schedule.getSteps(),r.getActuatorType()));
                }
            } else {
                // rover is not near the rock any longer, remove it
                cache.remove(r);
            }
        }

        if (cache.size() >= type.actuators.length) {
            // Using list instead of set since low numbers of actuators are expected
            List<Integer> requiredAct = new LinkedList<>(Arrays.asList(ArrayUtils.toObject(type.actuators)));
            List<Rover> ableRover = new LinkedList<>(); // used just for behaviour measures

            // check if it has all the required types for long enough
            for (Entry<Rover, Pair<Long,Integer>> e : cache.entrySet()) {
                if (state.schedule.getSteps() - e.getValue().getLeft() >= type.collectionTime) {
                    requiredAct.remove((Integer) e.getKey().getActuatorType());
                    ableRover.add(e.getKey());
                }
            }

            // has all the required types, remove the red rock
            if (requiredAct.isEmpty()) {
                int typeIndex = mr.par.usedTypes.indexOf(type);
                stop.stop();
                mr.field.remove(this);
                mr.scores[typeIndex]++;
                mr.rocks.remove(this);
                for(int a : type.actuators) {
                    mr.matchingRocks[a].remove(this);
                }
                for(Rover r : ableRover) { // for behaviour measures
                    r.captured[typeIndex]++;
                }
                if (mr.rocks.isEmpty()) { // all rocks have been collected
                    mr.kill();
                }
            }
        }
    }
}
