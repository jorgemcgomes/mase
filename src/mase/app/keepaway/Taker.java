/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.keepaway;

import java.awt.Color;
import mase.mason.EmboddiedAgent;
import net.jafama.FastMath;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class Taker extends EmboddiedAgent {

    public static final double RADIUS = 2;
    
    public Taker(SimState sim, Continuous2D field) {
        super(sim, field, RADIUS, Color.BLACK);
    }
    
    @Override
    public void step(SimState state) {
        Keepaway kw = (Keepaway) sim;
        // check if it caught the ball
        if(this.distanceTo(kw.ball) == 0) {
            kw.caught = true;
            kw.ball.stop();
            kw.kill();
            return;
        }
        
        // naive behaviour -- go towards the ball
        Double2D d = kw.ball.getLocation().subtract(this.getLocation());
        //double dir = this.angleTo(kw.ball.getLocation());
        super.move(FastMath.atan2(d.y, d.x), kw.par.takerSpeed);
    }
}
