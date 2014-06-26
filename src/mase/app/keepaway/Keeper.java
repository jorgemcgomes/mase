/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.keepaway;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import mase.controllers.AgentController;
import mase.mason.world.EmboddiedAgent;
import mase.mason.world.SmartAgent;
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
    private final DecimalFormat df;
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
            if (kw.par.sortKeepers) {
                ArrayList<Keeper> sorted = new ArrayList<Keeper>(kw.keepers.size() - 1);
                for (Keeper k : kw.keepers) {
                    if (k != this) {
                        sorted.add(k);
                    }
                }
                Collections.sort(sorted, new Comparator<Keeper>() {
                    @Override
                    public int compare(Keeper o1, Keeper o2) {
                        Double2D thisLoc = Keeper.this.getLocation();
                        return Double.compare(thisLoc.distance(o1.getLocation()), thisLoc.distance(o2.getLocation()));
                    }
                });
                agents.addAll(sorted);
            } else {
                for (Keeper k : kw.keepers) {
                    if (k != this) {
                        agents.add(k);
                    }
                }
            }
            agents.addAll(kw.takers);
            agents.add(kw.ball);
        }

        double[] input = new double[agents.size() * 2 + 2];
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
        /*sensorValues[index] = kw.ball.distanceToCenter;
        input[index] = (sensorValues[index] / (kw.par.size / 2)) * 2 - 1;*/
        
        Double2D centre = new Double2D(field.width / 2, field.height / 2);
        sensorValues[index] = this.getLocation().distance(centre);
        input[index] = (sensorValues[index++] / (kw.par.size / 2)) * 2 - 1;
        sensorValues[index] = this.angleTo(centre);
        input[index] = sensorValues[index] / Math.PI;
        
        return input;
    }

    @Override
    public void action(double[] output) {
        Keepaway kw = (Keepaway) sim;
        if (justKicked
                && (kw.ball.getSpeed() < 0.0001
                || kw.ball.getLocation().distance(getLocation()) > Keeper.RADIUS + Ball.RADIUS + KICK_DISTANCE)) {
            justKicked = false;
        }
        if (!justKicked && kw.ball.distanceTo(this) < KICK_DISTANCE) {
            this.hasPossession = true;
            justKicked = true;
            double kickPower = output[2] * passSpeed;
            double kickDir = orientation2D() + (output[3] * Math.PI * 2 - Math.PI);
            kw.ball.kick(kickDir, kickPower);
        } else {
            this.hasPossession = false;
            double dashPower = output[0] * moveSpeed;
            double dashDir = orientation2D() + (output[1] * Math.PI - Math.PI / 2);
            super.move(dashDir, dashPower);
        }
    }

    @Override
    public String getActionReport() {
        return "Dash speed: " + df.format(lastActionOutput[0] * moveSpeed)
                + " | Dash angle: " + Math.round(lastActionOutput[1] * 180 - 90) + "\u00B0"
                + " | Kick speed: " + df.format(lastActionOutput[2] * passSpeed)
                + " | Kick angle: " + Math.round(lastActionOutput[3] * 360 - 180) + "\u00B0";
    }

    @Override
    public String getSensorsReport() {
        StringBuilder sb = new StringBuilder();
        Keepaway kw = (Keepaway) sim;

        // Ball
        sb.append("Ball: (").append(df.format(sensorValues[0])).append(";").append(Math.round(sensorValues[1] / Math.PI * 180)).append("\u00B0)");
        int index = 2;
        // Keepers
        for (int i = 0; i < kw.keepers.size(); i++) {
            if (kw.keepers.get(i) != this) {
                sb.append(" | Keeper").append(i).append(": (").append(df.format(sensorValues[index++]));
                sb.append(";").append(Math.round(sensorValues[index++] / Math.PI * 180)).append("\u00B0)");
            }
        }
        // Takers
        for (int i = 0; i < kw.takers.size(); i++) {
            sb.append(" | Taker").append(i).append(": (").append(df.format(sensorValues[index++]));
            sb.append(";").append(Math.round(sensorValues[index++] / Math.PI * 180)).append("\u00B0)");
        }
        // Distance of the ball to the centre
        //sb.append(" | BallToCenter: ").append(df.format(sensorValues[index]));
        sb.append(" | Centre: (").append(df.format(sensorValues[index++]));
        sb.append(";").append(Math.round(sensorValues[index++] / Math.PI * 180)).append("\u00B0)");
        
        return sb.toString();
    }
    
    @Override
    public double[] getStateVariables() {
        double[] agVars = super.getStateVariables();
        double[] newVars = Arrays.copyOf(agVars, agVars.length + 1);
        newVars[newVars.length - 1] = hasPossession || justKicked ? 1 : 0;
        return newVars;
    }
}
