/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.multirover;

import java.awt.Color;
import mase.controllers.AgentController;
import mase.mason.world.AbstractSensor;
import mase.mason.world.DashMovementEffector;
import mase.mason.world.DistanceSensorArcs;
import mase.mason.world.DistanceSensorRays;
import mase.mason.world.SmartAgent;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author jorge
 */
public class Rover extends SmartAgent {

    private static final long serialVersionUID = 1L;

    public static final double RADIUS = 2;
    public static final Color COLOUR = Color.BLACK;
    public static final int NO_ACTIVATION = -1;
    public static final double ACTIVATION_THRESHOLD = 0.5;

    protected int actuatorType = NO_ACTIVATION;
    protected long lastActivation = -10000;
    protected int numActivations = 0;
    protected int captured = 0;

    public Rover(MultiRover sim, Continuous2D field, AgentController ac) {
        super(sim, field, RADIUS, COLOUR, ac);
        this.enableAgentCollisions(true);
        this.enableBoundedArena(true);

        // Obstacle sensor
        DistanceSensorRays dw = new DistanceSensorRays();
        dw.setAgent(sim, field, this);
        dw.setRays(5.0, -Math.PI / 6, Math.PI / 6);
        dw.setBinary(true);
        super.addSensor(dw);

        // redrock sensor
        DistanceSensorArcs ds = new DistanceSensorArcs();
        ds.setArcs(new double[]{-Math.PI / 2, -Math.PI / 6, Math.PI / 6}, new double[]{-Math.PI / 6, Math.PI / 6, Math.PI / 2});
        ds.setRange(sim.par.sensorRange);
        ds.setObjectTypes(RedRock.class);
        super.addSensor(ds);

        // closest rovers distance
        DistanceSensorArcs dsr = new DistanceSensorArcs();
        dsr.setArcs(new double[]{-Math.PI / 2, -Math.PI / 6, Math.PI / 6}, new double[]{-Math.PI / 6, Math.PI / 6, Math.PI / 2});
        dsr.setRange(sim.par.sensorRange);
        dsr.setObjectTypes(Rover.class);
        super.addSensor(dsr);

        // closest rovers type
        for (int i = 0; i < sim.par.numActuators; i++) {
            TypeSensor ts = new TypeSensor(dsr, i);
            super.addSensor(ts);
        }

        // movement effector
        DashMovementEffector dm = new DashMovementEffector();
        dm.setSpeeds(sim.par.linearSpeed, sim.par.turnSpeed);
        dm.allowBackwardMove(false);
        super.addEffector(dm);

    }

    private static class TypeSensor extends AbstractSensor {

        private final DistanceSensorArcs ds;
        private final int type;

        TypeSensor(DistanceSensorArcs ds, int type) {
            this.ds = ds;
            this.type = type;
        }

        @Override
        public int valueCount() {
            return 1;
        }

        @Override
        public double[] readValues() {
            Object[] rovers = ds.getClosestObjects();
            double[] dists = ds.getLastDistances();
            double closestDist = Double.POSITIVE_INFINITY;
            Rover closestRover = null;
            for (int i = 0; i < rovers.length; i++) {
                if (rovers[i] != null && dists[i] < closestDist) {
                    closestDist = dists[i];
                    closestRover = (Rover) rovers[i];
                }
            }
            if (closestRover != null && closestRover.getActuatorType() == type) {
                return new double[]{1};
            } else {
                return new double[]{-1};
            }
        }

        @Override
        public double[] normaliseValues(double[] vals) {
            return vals;
        }

    }

    public int getActuatorType() {
        return actuatorType;
    }

    @Override
    public void action(double[] output) {
        MRParams par = ((MultiRover) sim).par;
        if (sim.schedule.getSteps() - lastActivation < par.minActivationTime) {
            // locked in the actuator -- do not move
        } else {
            int newActuator = NO_ACTIVATION;
            double highestActivation = 0;
            for (int i = 0; i < par.numActuators; i++) {
                if (output[2 + i] > ACTIVATION_THRESHOLD && output[2 + i] > highestActivation) {
                    highestActivation = output[2 + i];
                    newActuator = i;
                }
            }
            if(newActuator != actuatorType) {
                lastActivation = sim.schedule.getSteps();
                actuatorType = newActuator;
                numActivations++;
            }
            // Only move if there is no actuator active or if there is no minActivationTime
            if(newActuator == NO_ACTIVATION || par.minActivationTime == 0) {
                actuatorType = newActuator;
                super.action(output);
            }
        }
    }

}
