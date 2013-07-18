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
    protected boolean hasPossession = false;
    protected double passSpeed;
    protected double moveSpeed;
    private boolean justKicked = false;

    public Keeper(SimState sim, Continuous2D field, AgentController ac, double passSpeed, double moveSpeed, Color color) {
        super(sim, field, RADIUS, color, ac);
        this.passSpeed = passSpeed;
        this.moveSpeed = moveSpeed;
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
        for (EmboddiedAgent a : agents) {
            input[index++] = (this.distanceTo(a) / (kw.par.size)) * 2 - 1;
            input[index++] = this.angleTo(a.getLocation()) / Math.PI;
        }

        // distance of the ball to the centre
        //input[index] = (kw.ball.distanceToCenter / (kw.par.size / 2)) * 2 - 1;
        input[index] = (kw.ball.distanceToCenter / (kw.par.ringSize / 2)) * 2 - 1;
        return input;
    }

    @Override
    public void action(double[] output) {
        Keepaway kw = (Keepaway) sim;
        if (justKicked
                && (kw.ball.getSpeed() < 0.0001
                || kw.ball.getLocation().distance(getLocation()) > Keeper.RADIUS + Ball.RADIUS)) {
            justKicked = false;
        }
        if (!justKicked && kw.ball.distanceTo(this) < KICK_DISTANCE) {
            this.hasPossession = true;
            justKicked = true;
            double kickPower = output[2] * passSpeed;
            Double2D kickDir = getDirection().rotate(output[3] * Math.PI * 2 - 1);
            kw.ball.kick(kickDir, kickPower);
        } else {
            this.hasPossession = false;
            double dashPower = output[0] * moveSpeed;
            Double2D dashDir = getDirection().rotate(output[1] * Math.PI - Math.PI / 2);
            super.move(dashDir, dashPower);
        }
    }
}
