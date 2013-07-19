/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.keepaway;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
    protected boolean hasPossession = false;
    protected double passSpeed;
    protected double moveSpeed;
    private boolean justKicked = false;
    private DecimalFormat df;
    private List<EmboddiedAgent> agents;
    private double[] sensorValues;

    public Keeper(SimState sim, Continuous2D field, AgentController ac, double passSpeed, double moveSpeed, Color color) {
        super(sim, field, RADIUS, color, ac);
        this.passSpeed = passSpeed;
        this.moveSpeed = moveSpeed;
        df = new DecimalFormat("0.0");
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
        sensorValues = new double[input.length];
        int index = 0;
        // relative positions and angles of the ball, keepers and takers
        for (EmboddiedAgent a : agents) {
            sensorValues[index] = this.distanceTo(a);
            input[index] = (sensorValues[index++] / (kw.par.size)) * 2 - 1;
            sensorValues[index] = this.angleTo(a.getLocation());
            input[index] = sensorValues[index++] / Math.PI;
        }
        

        // distance of the ball to the centre
        sensorValues[index] = kw.ball.distanceToCenter;
        input[index] = (sensorValues[index] / (kw.par.size / 2)) * 2 - 1;
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
            Double2D kickDir = getDirection().rotate(output[3] * Math.PI * 2 - Math.PI);
            kw.ball.kick(kickDir, kickPower);
        } else {
            this.hasPossession = false;
            double dashPower = output[0] * moveSpeed;
            Double2D dashDir = getDirection().rotate(output[1] * Math.PI - Math.PI / 2);
            super.move(dashDir, dashPower);
        }
    }

    @Override
    public String getActionReport() {
        return "Dash speed: " + df.format(lastAction[0] * moveSpeed)
                + " | Dash angle: " + Math.round(lastAction[1] * 180 - 90) + "\u00B0"
                + " | Kick speed: " + df.format(lastAction[2] * passSpeed)
                + " | Kick angle: " + Math.round(lastAction[3] * 360 - 180) + "\u00B0";
    }

    @Override
    public String getSensorsReport() {
        StringBuilder sb = new StringBuilder();
        Keepaway kw = (Keepaway) sim;

        // Ball
        sb.append("Ball: (" + df.format(sensorValues[0]) + ";" + Math.round(sensorValues[1] / Math.PI * 180) + "\u00B0)");
        int index = 2;
        // Keepers
        for (int i = 0; i < kw.keepers.size(); i++) {
            if (kw.keepers.get(i) != this) {
                sb.append(" | Keeper" + i + ": (" + df.format(sensorValues[index++]));
                sb.append(";" + Math.round(sensorValues[index++] / Math.PI * 180) + "\u00B0)");
            }
        }
        // Takers
        for (int i = 0; i < kw.takers.size(); i++) {
            sb.append(" | Taker" + i + ": (" + df.format(sensorValues[index++]));
            sb.append(";" + Math.round(sensorValues[index++] / Math.PI * 180) + "\u00B0)");
        }
        // Distance of the ball to the centre
        sb.append(" | BallToCenter: " + df.format(sensorValues[index]));

        return sb.toString();
    }
}
