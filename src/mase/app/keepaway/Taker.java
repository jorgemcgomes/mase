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
public class Taker extends EmboddiedAgent {

    public static final double RADIUS = 2;
    private boolean caughtBall = false;
    
    public Taker(SimState sim, Continuous2D field) {
        super(sim, field, RADIUS, Color.RED);
    }
    
    @Override
    public void step(SimState state) {
        Keepaway kw = (Keepaway) sim;
        // check if it caught the ball
        if(this.distanceTo(kw.ball) == 0) {
            this.caughtBall = true;
            kw.caught = true;
            kw.terminate();
            return;
        }
        
        // naive behaviour -- go towards the ball
        Double2D dir = kw.ball.getLocation().subtract(this.getLocation());
        super.move(dir, kw.par.takerSpeed);
    }
    
    public boolean hasCaughtBall() {
        return caughtBall;
    }
}
