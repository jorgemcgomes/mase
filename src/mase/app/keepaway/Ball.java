/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.keepaway;

import java.awt.Color;
import mase.mason.EmboddiedAgent;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class Ball extends EmboddiedAgent {

    public static final double RADIUS = 1;
    private double nextKickPower;
    private Double2D nextKickDirection;
    protected double distanceToCenter;
    
    public Ball(SimState sim, Continuous2D field) {
        super(sim, field, RADIUS, Color.WHITE);
    }

    @Override
    public void step(SimState state) {
        Keepaway kw = (Keepaway) sim;
        if(nextKickPower > 0) { // apply kick
            super.move(nextKickDirection, nextKickPower);
            nextKickPower = 0;
        } else if(getSpeed() > 0) { // ball movement
            // move
            super.move(getDirection(), getSpeed() - kw.par.ballDecay);
        }
        distanceToCenter = kw.center.distance(getLocation());
        
        // check if the ball has escaped the limits
        if(getLocation().x < 0 || getLocation().x > kw.par.size || getLocation().y < 0 || getLocation().y > kw.par.size) {
            kw.outOfLimits = true;
            kw.terminate();
        }
    }
    
    public void kick(Double2D direction, double power) {
        nextKickPower = power;
        nextKickDirection = direction;
    }
    
}
