/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.indiana;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Arrays;
import mase.controllers.AgentController;
import mase.mason.world.SmartAgent;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class IndianaAgent extends SmartAgent {

    public static final double RADIUS = 2.5;
    private final boolean even;
    private final double slice;
    private final double start;
    private final Double2D[] rayStarts, rayEnds;
    private static final DecimalFormat DF = new DecimalFormat("0.0");
    protected boolean passingGate = false;
    protected boolean escaped = false;

    public IndianaAgent(Indiana sim, Continuous2D field, AgentController ac) {
        super(sim, field, RADIUS, Color.BLUE, ac);
        this.enableAgentCollisions(true);
        this.enableBoundedArena(true);

        // aux variables for agent sensors
        even = sim.par.agentSensorArcs % 2 == 0;
        slice = Math.PI * 2 / sim.par.agentSensorArcs;
        start = Math.PI - slice / 2;

        // aux variables for wall sensors -- sensor rays
        rayStarts = new Double2D[sim.par.wallRays];
        rayEnds = new Double2D[sim.par.wallRays];
        double ang = Math.PI * 2 / sim.par.wallRays;
        for (int i = 0; i < rayStarts.length; i++) {
            if (i == 0) {
                rayStarts[i] = new Double2D(RADIUS, 0).rotate(-ang / 2);
                rayEnds[i] = new Double2D(RADIUS + sim.par.wallRadius, 0).rotate(-ang / 2);
            } else {
                rayStarts[i] = rayStarts[i - 1].rotate(ang);
                rayEnds[i] = rayEnds[i - 1].rotate(ang);
            }
        }
    }

    @Override
    protected boolean checkInsideArena(Double2D target) {
        IndianaParams par = ((Indiana) sim).par;
        this.passingGate = target.x >= -RADIUS && target.x <= RADIUS && target.y > par.size / 2 - par.gateSize / 2
                && target.y < par.size / 2 + par.gateSize / 2;
        return passingGate || super.checkInsideArena(target);
    }

    @Override
    public double[] readNormalisedSensors() {
        Indiana indSim = (Indiana) sim;
        IndianaParams par = indSim.par;
        double[] sens = new double[3 + par.agentSensorArcs + par.wallRays];

        // target sensor
        Double2D target = ((Indiana) sim).gate.getCenter();
        double d = this.getCenterLocation().distance(target);
        if (d <= par.gateSensorRange) {
            sens[0] = (this.getCenterLocation().distance(target) / par.gateSensorRange) * 2 - 1;
            sens[1] = this.angleTo(target) / Math.PI;
        } else {
            sens[0] = 1;
            sens[1] = 0;
        }
        // initialize distance sensors to the max value
        for (int i = 3; i < sens.length; i++) {
            sens[i] = 1;
        }
        // agent sensors
        int count = 0;
        Bag neighbours = field.getNeighborsWithinDistance(this.getCenterLocation(), par.agentSensorRadius + RADIUS * 2);
        for (Object n : neighbours) {
            if (n != this && n instanceof IndianaAgent) {
                IndianaAgent aa = (IndianaAgent) n;
                double dist = this.distanceTo(aa);
                if (dist <= par.agentSensorRadius) {
                    count++;
                    double angle = this.angleTo(aa.getCenterLocation());
                    int arc = angleToArc(angle);
                    sens[arc + 3] = Math.min(sens[arc + 3], (dist / par.agentSensorRadius) * 2 - 1); // arc sensors
                }
            }
        }
        // count sensor
        sens[2] = ((double) count / (par.numAgents - 1)) * 2 - 1;
        // wall sensors
        Double2D l = getCenterLocation();
        double r = par.wallRadius + RADIUS;
        // only if it is close to the boundaries
        if (l.x <= r || l.x >= field.width - r || l.y <= r || l.y >= field.height - r) {
            for (int i = 0; i < rayStarts.length; i++) {
                int si = i + par.agentSensorArcs + 3;
                Double2D rs = rayStarts[i].rotate(orientation2D()).add(getCenterLocation());
                Double2D re = rayEnds[i].rotate(orientation2D()).add(getCenterLocation());
                double dist = indSim.walls.closestDistance(rs, re);
                if(!Double.isInfinite(dist)) {
                    sens[si] = dist / par.wallRadius * 2 - 1;
                }
            }
        }
        return sens;
    }

    private int angleToArc(double angle) {
        if (even) {
            if (angle > start || angle < -start) {
                return 0;
            } else {
                return (int) ((angle - 0.0001 + start) / slice) + 1;
            }
        } else {
            return (int) ((angle - 0.0001 + Math.PI) / slice);
        }
    }

    @Override
    public void action(double[] output) {
        IndianaParams par = ((Indiana) sim).par;
        double speed = output[2] > 0.5 ? 0 : output[0] * par.agentSpeed;
        double r = (output[1] * 2 - 1) * par.agentRotation;
        double dir = orientation2D() + r;
        super.move(dir, speed);
    }

    @Override
    public String getActionReport() {
        IndianaParams par = ((Indiana) sim).par;
        return "Move speed: " + DF.format(lastActionOutput[0] * par.agentSpeed)
                + " | Move rot: " + Math.round((lastActionOutput[1] * 2 - 1) * par.agentRotation * 180 / Math.PI) + "\u00B0"
                + " | Stop: " + Boolean.toString(lastActionOutput[2] > 0.5);
    }

    @Override
    public String getSensorsReport() {
        return super.getSensorsReport();
    }

    @Override
    public double[] getStateVariables() {
        double[] vars = super.getStateVariables();
        double[] newVars = Arrays.copyOf(vars, vars.length + 1);
        newVars[vars.length] = passingGate ? 1 : 0;
        return newVars;
    }
}
