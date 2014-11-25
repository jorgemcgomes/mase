/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.predcomp;

import java.awt.Color;
import java.util.Arrays;
import mase.controllers.AgentController;
import mase.mason.world.SmartAgent;
import net.jafama.FastMath;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class Predator extends SmartAgent {

    public static final double RADIUS = 2.5;
    public static final double THRESHOLD = 0.01;

    private final double[] visionArcStart;
    private final double[] visionArcEnd;
    private final Double2D[] rayStarts, rayEnds;
    private final Double2D[] obsStarts, obsEnds;

    public Predator(Predcomp sim, Continuous2D field, AgentController ac) {
        super(sim, field, RADIUS, Color.RED, ac);
        this.enableBoundedArena(true);
        this.enableAgentCollisions(false);

        // Vision sensor aux variables
        visionArcStart = new double[sim.par.visionNeurons];
        visionArcEnd = new double[sim.par.visionNeurons];
        for (int i = 0; i < sim.par.visionNeurons; i++) {
            visionArcStart[i] = i == 0 ? -sim.par.viewAngle / 2 : visionArcEnd[i - 1];
            visionArcEnd[i] = visionArcStart[i] + sim.par.viewAngle / sim.par.visionNeurons;
        }

        // Aux variables for proximity sensors -- sensor rays
        rayStarts = new Double2D[sim.par.proximitySensors];
        rayEnds = new Double2D[sim.par.proximitySensors];
        double ang = Math.PI * 2 / sim.par.proximitySensors;
        for (int i = 0; i < rayStarts.length; i++) {
            if (i == 0) {
                rayStarts[i] = new Double2D(RADIUS, 0).rotate(-ang / 2);
                rayEnds[i] = new Double2D(RADIUS + sim.par.proximityRange, 0).rotate(-ang / 2);
            } else {
                rayStarts[i] = rayStarts[i - 1].rotate(ang);
                rayEnds[i] = rayEnds[i - 1].rotate(ang);
            }
        }

        // Aux variables for proximity sensors -- wall segments
        obsStarts = new Double2D[4];
        obsEnds = new Double2D[4];
        obsStarts[0] = new Double2D(0, 0);
        obsEnds[0] = new Double2D(sim.par.size, 0);
        obsStarts[1] = obsEnds[0];
        obsEnds[1] = new Double2D(sim.par.size, sim.par.size);
        obsStarts[2] = obsEnds[1];
        obsEnds[2] = new Double2D(0, sim.par.size);
        obsStarts[3] = obsEnds[2];
        obsEnds[3] = obsStarts[0];
    }

    @Override
    public double[] readNormalisedSensors() {
        double[] sensorValues = new double[visionArcStart.length + rayStarts.length];
        Arrays.fill(sensorValues, -1);
        

        // Vision sensors
        Predcomp pred = (Predcomp) sim;
        double dist = this.getLocation().distance(pred.prey.getLocation()) - RADIUS;
        double angle = this.angleTo(pred.prey.getLocation());
        double extra = FastMath.atan(pred.prey.getRadius() / dist);
        if (this.distanceTo(pred.prey) < pred.par.visionRange) {
            for (int i = 0; i < visionArcStart.length; i++) {
                if (angle + extra >= visionArcStart[i] && angle - extra <= visionArcEnd[i]) {
                    sensorValues[i] = 1;
                }
            }
        }
        
        // Proximity sensors
        for (int i = 0; i < rayStarts.length; i++) {
            Double2D rs = rayStarts[i].rotate(orientation2D()).add(getLocation());
            Double2D re = rayEnds[i].rotate(orientation2D()).add(getLocation());
            boolean wallInt = false;
            for(int j = 0 ; j < obsStarts.length && !wallInt ; j++) {
                wallInt = segmentIntersection(rs, re, obsStarts[j], obsEnds[j]) != null;
            }
            double d = distToSegment(pred.prey.getLocation(), rs, re);
            boolean preyInt = d <= RADIUS;
            if (wallInt || preyInt) {
                sensorValues[i + visionArcStart.length] = 1;
            }
        }

        return sensorValues;
    }

    protected static Double2D segmentIntersection(Double2D p0, Double2D p1, Double2D p2, Double2D p3) {
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

    protected static double distToSegment(Double2D p, Double2D v, Double2D w) {
        double l2 = FastMath.pow2(v.x - w.x) + FastMath.pow2(v.y - w.y);
        if (l2 == 0.0) {
            return p.distance(v);   // v == w case
        }
        double t = p.subtract(v).dot(w.subtract(v)) / l2;
        if (t < 0.0) {
            return p.distance(v); // Beyond the 'v' end of the segment
        } else if (t > 1.0) {
            return p.distance(w);  // Beyond the 'w' end of the segment
        }
        Double2D projection = v.add((w.subtract(v)).multiply(t));  // Projection falls on the segment
        return p.distance(projection);
    }

    @Override
    public void action(double[] output) {
        Predcomp pred = (Predcomp) sim;
        double l = (output[0] * 2 - 1) * pred.par.predatorSpeed;
        double r = (output[1] * 2 - 1) * pred.par.predatorSpeed;
        
        double s = (l + r) / 2;
        double o = orientation2D() + (l - r) / (RADIUS * 2);
        super.move(o, s);
        
        if(this.distanceTo(pred.prey) < THRESHOLD) {
            pred.kill();
        } 
    }

}
