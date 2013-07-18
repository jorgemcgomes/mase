/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.keepaway;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Arrays;
import mase.AgentController;
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
    private String sensorsReport = "";

    public Keeper(SimState sim, Continuous2D field, AgentController ac, double passSpeed, double moveSpeed, Color color) {
        super(sim, field, RADIUS, color, ac);
        this.passSpeed = passSpeed;
        this.moveSpeed = moveSpeed;
        df = new DecimalFormat("0.0");
    }

    @Override
    public double[] readNormalisedSensors() {
        Keepaway kw = (Keepaway) sim;

        double[] input = new double[kw.keepers.size() * 2 + kw.takers.size() * 2 + 1];
        int index = 0;
        StringBuilder sb = new StringBuilder();

        // Ball
        double dist = this.distanceTo(kw.ball);
        double ang = this.angleTo(kw.ball.getLocation());
        input[index++] = (dist / kw.par.size) * 2 - 1;
        input[index++] = ang / Math.PI;
        sb.append("Ball: (" + df.format(dist) + "," + Math.round(ang / Math.PI * 180) + "\u00B0)");
        
        // Keepers
        for (int i = 0; i < kw.keepers.size(); i++) {
            Keeper k = kw.keepers.get(i);
            if (k != this) {
                dist = this.distanceTo(k);
                ang = this.angleTo(k.getLocation());
                input[index++] = (dist / kw.par.size) * 2 - 1;
                input[index++] = ang / Math.PI;
                sb.append(" | Keeper" + i + ": (" + df.format(dist) + "," + Math.round(ang / Math.PI * 180) + "\u00B0)");
            }
        }
        
        // Takers
        for (int i = 0; i < kw.takers.size(); i++) {
            Taker t = kw.takers.get(i);
            dist = this.distanceTo(t);
            ang = this.angleTo(t.getLocation());
            input[index++] = (dist / kw.par.size) * 2 - 1;
            input[index++] = ang / Math.PI;
            sb.append(" | Taker" + i + ": (" + df.format(dist) + "," + Math.round(ang / Math.PI * 180) + "\u00B0)");
        }

        // Distance of the ball to the centre
        input[index] = (kw.ball.distanceToCenter / (kw.par.size / 2)) * 2 - 1;
        sb.append(" | BallToCenter: " + df.format(kw.ball.distanceToCenter));
        sensorsReport = sb.toString();
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
        return sensorsReport;
    }
}
