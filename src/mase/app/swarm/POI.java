/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.swarm;

import java.awt.Color;
import mase.mason.world.EmboddiedAgent;
import mase.mason.world.MultilineObject;
import sim.engine.SimState;
import sim.engine.Stoppable;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class POI extends EmboddiedAgent {

    private static final long serialVersionUID = 1L;
    protected double speed;
    protected double currentOrientation = Double.NaN;
    protected Stoppable stop;

    public POI(Color color, SimState sim, Continuous2D field, double radius, double speed) {
        super(sim, field, radius, color);
        this.speed = speed;
        this.virtuallyBoundedArena(true);
        this.setCollidableTypes(MultilineObject.class); // collides with walls and obstacles
        this.enableCollisionRebound(false); // the rebound is handled special
    }

    @Override
    public void step(SimState state) {
        if(speed > 0) {
            // orientation change
            if (Double.isNaN(currentOrientation)) {
                currentOrientation = -Math.PI + sim.random.nextDouble() * Math.PI * 2;
            }
            boolean success = super.move(currentOrientation, speed);
            if (!success) {
                currentOrientation = Double.NaN;
            }
        }
    }
}
