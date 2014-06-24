/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.herding;

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedList;
import mase.controllers.AgentController;
import mase.mason.SmartAgent;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class Shepherd extends SmartAgent {

    private AgentController ac;
    private final double[] arcStart;
    private final double[] arcEnd;

    public Shepherd(Herding sim, Continuous2D field, AgentController ac) {
        super(sim, field, sim.par.agentRadius, Color.BLUE, ac);
        this.ac = ac;
        this.enableCollisionDetection(true);
        this.enableBoundedArena(true);

        int arcs = 4;
        arcStart = new double[arcs];
        arcEnd = new double[arcs];
        double arcAngle = (Math.PI * 2) / arcs;
        arcStart[0] = -arcAngle / 2; // first arc aligned with front
        arcEnd[0] = arcAngle / 2;
        for (int i = 1; i < arcs; i++) {
            arcStart[i] = arcEnd[i - 1];
            arcEnd[i] = arcStart[i] + arcAngle;
            if (arcEnd[i] > Math.PI) {
                arcEnd[i] -= Math.PI * 2;
            }
        }
    }

    @Override
    public double[] readNormalisedSensors() {
        Herding herd = (Herding) sim;
        double[] sensorValues = new double[4 + 2 + 2 + herd.par.numFoxes * 2];
        int sensorIndex = 0;

        // Shepherds sensor
        LinkedList<Shepherd> tempList = new LinkedList<Shepherd>(herd.shepherds);
        tempList.remove(this);
        for (int i = 0; i < arcStart.length; i++) {
            double closest = Double.POSITIVE_INFINITY;
            for (Iterator<Shepherd> iter = tempList.iterator(); iter.hasNext();) {
                Shepherd s = iter.next();
                double angle = this.angleTo(s.getLocation());
                if (angle > arcStart[i] && angle < arcEnd[i]
                        || (arcStart[i] > arcEnd[i] && (angle > arcStart[i] || angle < arcEnd[i]))) {
                    closest = Math.min(closest, this.distanceTo(s));
                    iter.remove();
                }
            }
            sensorValues[sensorIndex++] = Math.min(1, (closest / herd.par.shepherdSensorRange) * 2 - 1);
        }

        // Closest sheep
        Sheep closestSheep = null;
        for (Sheep s : herd.activeSheeps) {
            double d = this.distanceTo(s);
            if (closestSheep == null || d < this.distanceTo(closestSheep)) {
                closestSheep = s;
            }
        }
        if (closestSheep != null) {
            sensorValues[sensorIndex++] = (this.distanceTo(closestSheep) / herd.par.arenaSize) * 2 - 1;
            sensorValues[sensorIndex++] = this.angleTo(closestSheep.getLocation()) / Math.PI;
        }

        // Gate
        Double2D gate = new Double2D(herd.par.arenaSize, herd.par.arenaSize / 2);
        sensorValues[sensorIndex++] = this.getLocation().distance(gate) / herd.par.arenaSize * 2 - 1;
        sensorValues[sensorIndex++] = this.angleTo(gate) / Math.PI;

        // Foxes
        for (Fox f : herd.foxes) {
            sensorValues[sensorIndex++] = (this.distanceTo(f) / herd.par.arenaSize) * 2 - 1;
            sensorValues[sensorIndex++] = this.angleTo(f.getLocation()) / Math.PI;
        }

        return sensorValues;
    }

    @Override
    public void action(double[] output) {
        Herding herd = (Herding) sim;
        double forward = output[0] * herd.par.shepherdSpeed;
        double r = (output[1] * 2 - 1) * herd.par.shepherdTurnSpeed;
        super.move(orientation2D() + r, forward);
    }

}
