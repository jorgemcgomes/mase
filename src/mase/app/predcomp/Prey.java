/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.predcomp;

import java.awt.Color;
import java.util.Arrays;
import static mase.app.predcomp.Predator.RADIUS;
import static mase.app.predcomp.Predator.segmentIntersection;
import mase.controllers.AgentController;
import mase.mason.world.SmartAgent;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class Prey extends SmartAgent {

    private final Double2D[] rayStarts, rayEnds;
    private final Double2D[] obsStarts, obsEnds;

    public Prey(Predcomp sim, Continuous2D field, AgentController ac) {
        super(sim, field, RADIUS, Color.BLUE, ac);
        this.enableBoundedArena(true);
        this.enableAgentCollisions(false);
        
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
        double[] sensorValues = new double[rayStarts.length];
        Arrays.fill(sensorValues, -1);
        Predcomp pred = (Predcomp) sim;

        // Proximity sensors
        for (int i = 0; i < rayStarts.length; i++) {
            Double2D rs = rayStarts[i].rotate(orientation2D()).add(getCenterLocation());
            Double2D re = rayEnds[i].rotate(orientation2D()).add(getCenterLocation());
            boolean wallInt = false;
            for(int j = 0 ; j < obsStarts.length && !wallInt ; j++) {
                wallInt = segmentIntersection(rs, re, obsStarts[j], obsEnds[j]) != null;
            }
            double d = Predator.distToSegment(pred.predator.getCenterLocation(), rs, re);
            boolean preyInt = d <= RADIUS;
            if (wallInt || preyInt) {
                sensorValues[i] = 1;
            }
        }
        return sensorValues;
    }

    @Override
    public void action(double[] output) {
        Predcomp pred = (Predcomp) sim;
        double l = (output[0] * 2 - 1) * pred.par.preySpeed;
        double r = (output[1] * 2 - 1) * pred.par.preySpeed;
        
        double s = (l + r) / 2;
        double o = orientation2D() + (l - r) / (RADIUS * 2);
        super.move(o, s);
        
        if(this.distanceTo(pred.predator) < Predator.THRESHOLD) {
            pred.kill();
        } 
    }
    
}
