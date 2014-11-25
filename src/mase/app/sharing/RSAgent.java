/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.sharing;

import java.awt.Color;
import java.util.Arrays;
import mase.controllers.AgentController;
import mase.mason.world.SmartAgent;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;

/**
 *
 * @author jorge
 */
public class RSAgent extends SmartAgent {

    private final boolean even;
    private final double slice;
    private final double start;
    protected double energyLevel;
    protected boolean inStation;

    public RSAgent(ResourceSharing sim, Continuous2D field, AgentController ac) {
        super(sim, field, sim.par.agentRadius, Color.BLUE, ac);

        this.enableAgentCollisions(true);
        this.enableBoundedArena(false);

        // aux variables for agent sensors
        this.even = sim.par.agentSensorArcs % 2 == 0;
        this.slice = Math.PI * 2 / sim.par.agentSensorArcs;
        this.start = Math.PI - slice / 2;
        this.inStation = false;
        this.energyLevel = sim.par.maxEnergy;
    }

    @Override
    public double[] readNormalisedSensors() {
        ResourceSharing rs = (ResourceSharing) sim;
        double[] sens = new double[rs.par.agentSensorArcs + 4];

        // robot sensors
        for (int i = 0; i < rs.par.agentSensorArcs; i++) {
            sens[i] = 1; // initialize distance sensors to max value
        }
        Bag neighbours = field.getNeighborsWithinDistance(this.getLocation(), rs.par.agentSensorRange + rs.par.agentRadius * 2);
        for (Object n : neighbours) {
            if (n != this && n instanceof RSAgent) {
                RSAgent a = (RSAgent) n;
                double dist = this.distanceTo(a);
                if (dist <= rs.par.agentSensorRange) {
                    double angle = this.angleTo(a.getLocation());
                    int arc = angleToArc(angle);
                    sens[arc] = Math.min(sens[arc], (dist / rs.par.agentSensorRange) * 2 - 1); // arc sensors
                }
            }
        }

        sens[rs.par.agentSensorArcs] = (this.getLocation().distance(rs.resource.getLocation()) / (rs.par.size / 2)) * 2 - 1;
        sens[rs.par.agentSensorArcs + 1] = this.angleTo(rs.resource.getLocation()) / Math.PI;
        sens[rs.par.agentSensorArcs + 2] = (this.energyLevel / rs.par.maxEnergy) * 2 - 1;
        sens[rs.par.agentSensorArcs + 3] = inStation ? 1 : -1;
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
        ResourceSharing rs = (ResourceSharing) sim;
        // Movement
        double speed = output[2] > 0.5 ? 0 : output[0];
        double rot = (output[1] * 2 - 1) * rs.par.agentRotation; // [-AgentRot, AgentRot]
        double dir = output[2] > 0.5 ? orientation2D() : orientation2D() + rot;
        super.move(dir, speed * rs.par.agentSpeed);

        /*double speed;
         if (output[3] < 0.5) {
         speed = output[2];
         double rot = output[0] * rs.par.agentRotation - output[1] * rs.par.agentRotation;
         Double2D dir = getDirection().rotate(rot);
         super.move(dir, speed);
         } else {
         speed = 0;
         super.move(getDirection(), 0); // stay put
         }*/


        // Update inner sensors        
        energyLevel = Math.max(0, energyLevel - rs.par.minEnergyDecay
                - (rs.par.maxEnergyDecay - rs.par.minEnergyDecay) * speed);
        inStation = getLocation().distance(rs.resource.getLocation())
                < rs.par.resourceRadius - rs.par.agentRadius;

        // Death procedures
        if (!isAlive()) {
            this.stop();
            rs.activeAgents.remove(this);
            int i = 0;
            for (; i < rs.par.agentSensorArcs; i++) {
                lastNormSensors[i] = 1;
            }
            lastNormSensors[i++] = 1;
            lastNormSensors[i++] = 0;
            lastNormSensors[i] = -1;
            Arrays.fill(lastActionOutput, 0);
            move(orientation2D(), 0);
        }
    }

    public double getEnergyLevel() {
        return energyLevel;
    }

    public boolean isInStation() {
        return inStation;
    }

    @Override
    public boolean isAlive() {
        return super.isAlive() && energyLevel > 0.0001;
    }

    @Override
    public double[] getStateVariables() {
        double[] superVars = super.getStateVariables();
        double[] extVars = Arrays.copyOf(superVars, superVars.length + 2);
        extVars[extVars.length - 2] = getEnergyLevel();
        extVars[extVars.length - 1] = isInStation() ? 1 : 0;
        return extVars;
    }
}
