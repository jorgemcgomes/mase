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

    public static final Color NOACTUATOR_COLOUR = Color.BLACK;
    public static final Color ACTUATING_COLOUR = Color.RED;
    public static final int NO_ACTIVATION = -1;
    public static final double ACTIVATION_THRESHOLD = 0.5;

    protected int actuatorType = NO_ACTIVATION;
    protected int captured = 0;

    public Rover(MultiRover sim, Continuous2D field, AgentController ac) {
        super(sim, field, sim.par.agentRadius, NOACTUATOR_COLOUR, ac);
        this.enableAgentCollisions(true);
        this.enableBoundedArena(true);

        // Obstacle sensor
        DistanceSensorRays dw = new DistanceSensorRays();
        dw.setAgent(sim, field, this);
        dw.setRays(10.0, -Math.PI / 6, Math.PI / 6);
        dw.setBinary(true);
        dw.setNoise(sim.par.sensorRangeNoise, sim.par.sensorAngleNoise, DistanceSensorRays.UNIFORM);
        super.addSensor(dw);

        // rock sensor
        DistanceSensorArcs ds = new DistanceSensorArcs();
        ds.setArcs(4);
        ds.setRange(sim.par.sensorRange);
        ds.setObjectTypes(Rock.class);
        ds.setNoise(sim.par.sensorRangeNoise, sim.par.sensorAngleNoise, DistanceSensorRays.UNIFORM);
        super.addSensor(ds);

        // closest rovers distance
        DistanceSensorArcs dsr = new DistanceSensorArcs();
        dsr.setArcs(4);
        dsr.setRange(sim.par.sensorRange);
        dsr.setObjectTypes(Rover.class);
        dsr.setNoise(sim.par.sensorRangeNoise, sim.par.sensorAngleNoise, DistanceSensorRays.UNIFORM);
        super.addSensor(dsr);

        // closest rovers type
        for (int i = 0; i < sim.par.numActuators; i++) {
            NeighbourTypeSensor ts = new NeighbourTypeSensor(dsr, i);
            super.addSensor(ts);
        }

        // rock type sensor
        for (RockType t : sim.par.usedTypes) {
            RockTypeSensor rts = new RockTypeSensor(ds, t);
            super.addSensor(rts);
        }

        // movement effector
        DashMovementEffector dm = new DashMovementEffector();
        dm.setSpeeds(sim.par.linearSpeed, sim.par.turnSpeed);
        dm.allowBackwardMove(false);
        dm.setNoise(sim.par.actuatorNoise, sim.par.actuatorNoise);
        super.addEffector(dm);

    }

    private static class RockTypeSensor extends AbstractSensor {

        private final DistanceSensorArcs rockSensor;
        private final RockType type;

        RockTypeSensor(DistanceSensorArcs rockSensor, RockType type) {
            this.rockSensor = rockSensor;
            this.type = type;
        }

        @Override
        public int valueCount() {
            return 1;
        }

        @Override
        public double[] readValues() {
            Object[] rocks = rockSensor.getClosestObjects();
            double[] dists = rockSensor.getLastDistances();
            double minDist = Double.POSITIVE_INFINITY;
            Rock closestRock = null;
            for (int i = 0; i < rocks.length; i++) {
                if (rocks[i] != null && dists[i] < minDist) {
                    minDist = dists[i];
                    closestRock = (Rock) rocks[i];
                }
            }
            if (closestRock != null && closestRock.getType() == type) {
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

    private static class NeighbourTypeSensor extends AbstractSensor {

        private final DistanceSensorArcs roverSensor;
        private final int type;

        NeighbourTypeSensor(DistanceSensorArcs roverSensor, int type) {
            this.roverSensor = roverSensor;
            this.type = type;
        }

        @Override
        public int valueCount() {
            return 1;
        }

        @Override
        public double[] readValues() {
            Object[] rovers = roverSensor.getClosestObjects();
            double[] dists = roverSensor.getLastDistances();
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
        actuatorType = NO_ACTIVATION;
        double highestActivation = 0;
        for (int i = 0; i < par.numActuators; i++) {
            if (output[2 + i] > ACTIVATION_THRESHOLD && output[2 + i] > highestActivation) {
                highestActivation = output[2 + i];
                actuatorType = i;
            }
        }
        // Only move if there is no actuator active
        if (actuatorType == NO_ACTIVATION) {
            this.setColor(NOACTUATOR_COLOUR);
            super.action(output);
        } else {
            this.setColor(ACTUATING_COLOUR);
        }
    }
}
