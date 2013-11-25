/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.indiana;

import java.awt.Color;
import java.text.DecimalFormat;
import mase.AgentController;
import mase.mason.SmartAgent;
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
    private Double2D[] obsStarts, obsEnds;
    private static final DecimalFormat DF = new DecimalFormat("0.0");
    protected boolean passingGate = false;
    protected boolean escaped = false;

    public IndianaAgent(Indiana sim, Continuous2D field, AgentController ac) {
        super(sim, field, RADIUS, Color.BLUE, ac);
        this.enableCollisionDetection(true);
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
        if (sim.par.wallRays > 0) {
            // aux variables for wall sensors -- wall segments
            obsStarts = new Double2D[5];
            obsEnds = new Double2D[5];
            obsStarts[0] = new Double2D(0, 0);
            obsEnds[0] = new Double2D(sim.par.size, 0);
            obsStarts[1] = obsEnds[0];
            obsEnds[1] = new Double2D(sim.par.size, sim.par.size);
            obsStarts[2] = obsEnds[1];
            obsEnds[2] = new Double2D(0, sim.par.size);
            obsStarts[3] = obsEnds[2];
            obsEnds[3] = new Double2D(0, sim.par.size / 2 + sim.par.gateSize / 2);
            obsStarts[4] = new Double2D(0, sim.par.size / 2 - sim.par.gateSize / 2);
            obsEnds[4] = obsStarts[0];
        }
    }

    @Override
    protected boolean checkEnvironmentValidty(Double2D target) {
        IndianaParams par = ((Indiana) sim).par;
        this.passingGate = target.x >= -RADIUS && target.x <= RADIUS && target.y > par.size / 2 - par.gateSize / 2
                && target.y < par.size / 2 + par.gateSize / 2;
        return passingGate || super.checkEnvironmentValidty(target);
    }

    @Override
    public double[] readNormalisedSensors() {
        IndianaParams par = ((Indiana) sim).par;
        double[] sens = new double[3 + par.agentSensorArcs + par.wallRays];

        // target sensor
        Double2D target = ((Indiana) sim).gate.getLocation();
        double d = this.getLocation().distance(target);
        if (d <= par.gateSensorRange) {
            sens[0] = (this.getLocation().distance(target) / par.gateSensorRange) * 2 - 1;
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
        Bag neighbours = field.getNeighborsWithinDistance(this.getLocation(), par.agentSensorRadius + RADIUS * 2);
        for (Object n : neighbours) {
            if (n != this && n instanceof IndianaAgent) {
                IndianaAgent aa = (IndianaAgent) n;
                double dist = this.distanceTo(aa);
                if (dist <= par.agentSensorRadius) {
                    count++;
                    double angle = this.angleTo(aa.getLocation());
                    int arc = angleToArc(angle);
                    sens[arc + 3] = Math.min(sens[arc + 3], (dist / par.agentSensorRadius) * 2 - 1); // arc sensors
                }
            }
        }
        // count sensor
        sens[2] = ((double) count / (par.numAgents - 1)) * 2 - 1;
        // wall sensors
        Double2D l = getLocation();
        double r = par.wallRadius + RADIUS;
        // only if it is close to the boundaries
        if (l.x <= r || l.x >= field.width - r || l.y <= r || l.y >= field.height - r) {
            for (int i = 0; i < rayStarts.length; i++) {
                int si = i + par.agentSensorArcs + 3;
                Double2D rs = rayStarts[i].rotate(orientation2D()).add(getLocation());
                Double2D re = rayEnds[i].rotate(orientation2D()).add(getLocation());
                for (int j = 0; j < obsStarts.length; j++) {
                    Double2D inters = segmentIntersection(rs, re, obsStarts[j], obsEnds[j]);
                    if (inters != null) {
                        double dist = rs.distance(inters);
                        sens[si] = Math.min(sens[si], (dist / par.wallRadius) * 2 - 1);
                    }
                }
            }
        }
        return sens;
    }

    // http://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
    protected Double2D segmentIntersection(Double2D p0, Double2D p1, Double2D p2, Double2D p3) {
        Double2D s1 = new Double2D(p1.x - p0.x, p1.y - p0.y);
        Double2D s2 = new Double2D(p3.x - p2.x, p3.y - p2.y);
        double s = (-s1.y * (p0.x - p2.x) + s1.x * (p0.y - p2.y)) / (-s2.x * s1.y + s1.x * s2.y);
        double t = (s2.x * (p0.y - p2.y) - s2.y * (p0.x - p2.x)) / (-s2.x * s1.y + s1.x * s2.y);
        if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
            // collision detected
            return new Double2D(p0.x + t * s1.x, p0.y + t * s1.y);
        } else {
            // no collision
            return null;
        }
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
        return "Move speed: " + DF.format(lastAction[0] * par.agentSpeed)
                + " | Move rot: " + Math.round((lastAction[1] * 2 - 1) * par.agentRotation * 180 / Math.PI) + "\u00B0"
                + " | Stop: " + Boolean.toString(lastAction[2] > 0.5);
    }

    @Override
    public String getSensorsReport() {
        return super.getSensorsReport();
    }
}
