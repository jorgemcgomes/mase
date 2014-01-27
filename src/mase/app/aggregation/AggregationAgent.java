/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.aggregation;

import java.awt.Color;
import java.text.DecimalFormat;
import mase.controllers.AgentController;
import mase.generic.systematic.Agent;
import mase.mason.SmartAgent;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class AggregationAgent extends SmartAgent implements Agent{

    public static final double RADIUS = 2.5;
    private boolean even;
    private double slice;
    private double start;
    private Double2D[] rayStarts, rayEnds;
    private static DecimalFormat DF = new DecimalFormat("0.0");

    public static void main(String[] args) {
        System.out.println(new Double2D(1, 0).angle());
    }

    public AggregationAgent(Aggregation sim, Continuous2D field, AgentController ac) {
        super(sim, field, RADIUS, Color.BLUE, ac);
        this.enableCollisionDetection(true);
        this.enableBoundedArena(true);

        // aux variables for agent sensors
        even = sim.par.agentArcs % 2 == 0;
        slice = Math.PI * 2 / sim.par.agentArcs;
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
    public double[] readNormalisedSensors() {
        Aggregation agg = (Aggregation) sim;
        AggregationParams par = agg.par;
        double[] sens = new double[par.agentArcs + par.wallRays + 1];

        // robot sensors
        for (int i = 1; i < par.agentArcs + par.wallRays + 1; i++) {
            sens[i] = 1; // initialize distance sensors to max value
        }
        int count = 0;
        Bag neighbours = field.getNeighborsWithinDistance(this.getLocation(), par.agentRadius + RADIUS * 2);
        for (Object n : neighbours) {
            if (n != this) {
                AggregationAgent aa = (AggregationAgent) n;
                double dist = this.distanceTo(aa);
                if (dist <= par.agentRadius) {
                    count++;
                    double angle = this.angleTo(aa.getLocation());
                    int arc = angleToArc(angle);
                    sens[arc + 1] = Math.min(sens[arc + 1], (dist / par.agentRadius) * 2 - 1); // arc sensors
                }
            }
        }
        sens[0] = ((double) count / (par.numAgents - 1)) * 2 - 1; // count sensor

        // wall sensors
        for (int i = 0; i < rayStarts.length; i++) {
            sens[i + par.agentArcs + 1] = 1;
        }

        Double2D l = getLocation();
        double r = par.wallRadius + RADIUS;
        // only if it is close to the boundaries
        if (l.x <= r || l.x >= field.width - r || l.y <= r || l.y >= field.height - r) {
            for (int i = 0; i < rayStarts.length; i++) {
                int si = i + par.agentArcs + 1;
                Double2D rs = rayStarts[i].rotate(orientation2D()).add(getLocation());
                Double2D re = rayEnds[i].rotate(orientation2D()).add(getLocation());
                for (int j = 0; j < agg.walls.getSegStarts().length; j++) {
                    Double2D inters = segmentIntersection(rs, re, agg.walls.getSegStarts()[j], agg.walls.getSegEnds()[j]);
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
        AggregationParams par = ((Aggregation) sim).par;
        double speed = output[2] > 0.5 ? 0 : output[0] * par.agentSpeed;
        double r = (output[1] * 2 - 1) * par.agentRotation;
        double dir = orientation2D() + r;
        super.move(dir, speed);
    }

    @Override
    public String getActionReport() {
        AggregationParams par = ((Aggregation) sim).par;
        return "Move speed: " + DF.format(lastAction[0] * par.agentSpeed)
                + " | Move rot: " + Math.round((lastAction[1] * 2 - 1) * par.agentRotation * 180 / Math.PI) + "\u00B0"
                + " | Stop: " + Boolean.toString(lastAction[2] > 0.5);
    }

    @Override
    public String getSensorsReport() {
        
        
        
        return super.getSensorsReport(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Double2D getPosition() {
        return getLocation();
    }

    @Override
    public double[] getStateVariables() {
        return new double[0];
    }
}
