/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.keepaway;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import mase.AgentController;
import mase.mason.EmboddiedAgent;
import mase.mason.SmartAgent;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class Keeper extends SmartAgent {

    public static final double RADIUS = 2;
    public static final double KICK_DISTANCE = 0.5;
    private List<EmboddiedAgent> agents;

    public Keeper(SimState sim, Continuous2D field, AgentController ac) {
        super(sim, field, RADIUS, Color.BLUE, ac);
    }

    @Override
    public double[] readNormalisedSensors() {
        Keepaway kw = (Keepaway) sim;
        // builds auxiliary list
        if (agents == null) {
            agents = new ArrayList<EmboddiedAgent>(kw.keepers.size() + kw.takers.size());
            agents.add(kw.ball);
            for (Keeper k : kw.keepers) {
                if (k != this) {
                    agents.add(k);
                }
            }
            agents.addAll(kw.takers);
        }

        double[] input = new double[agents.size() * 2 + 1];
        int index = 0;
        // relative positions and angles of the ball, keepers and takers
        for(EmboddiedAgent a : agents) {
            input[index++] = (this.distanceTo(a) / (kw.par.ringSize)) * 2 - 1;
            input[index++] = this.angleTo(a.getLocation()) / Math.PI;
        }
        
        // distance of the ball to the centre
        input[index] = (kw.ball.distanceToCenter / (kw.par.ringSize / 2)) * 2 - 1;
        return input;
    }

    @Override
    public void action(double[] output) {
        Keepaway kw = (Keepaway) sim;
        if(kw.ball.distanceTo(this) < KICK_DISTANCE) {
            double kickPower = output[2] * kw.par.ballSpeed;
            Double2D kickDir = getDirection().rotate(output[3] * Math.PI * 2 - 1);
            kw.ball.kick(kickDir, kickPower);
        } else {
            double dashPower = output[0] * kw.par.keeperSpeed;
            Double2D dashDir = getDirection().rotate(output[1] * Math.PI * 2 - Math.PI);
            super.move(dashDir, dashPower);
        }
    }
}
