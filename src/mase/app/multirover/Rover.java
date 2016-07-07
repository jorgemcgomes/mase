/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.multirover;

import java.awt.Color;
import java.util.Collections;
import mase.controllers.AgentController;
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

        super.circledPortrayal.scale = sim.par.rockSensorRange * 2;
        
        // movement effector
        DashMovementEffector dm = new DashMovementEffector(sim, field, this);
        dm.setSpeeds(sim.par.linearSpeed, sim.par.turnSpeed);
        dm.allowBackwardMove(false);
        dm.setNoise(sim.par.actuatorNoise, sim.par.actuatorNoise);
        super.addEffector(dm);

    }

    protected void setupSensors() {
        MultiRover mr = (MultiRover) sim;
        MRParams par = mr.par;
        
        // Obstacle sensor
        DistanceSensorRays dw = new DistanceSensorRays(sim, field, this);
        dw.setRays(5, -Math.PI / 6, Math.PI / 6);
        dw.setObjects(Collections.singleton(mr.walls));
        dw.setBinary(true);
        dw.setNoise(par.sensorRangeNoise, par.sensorAngleNoise, DistanceSensorRays.UNIFORM);
        super.addSensor(dw);

        // rock sensor
        DistanceSensorArcs ds = new DistanceSensorArcs(sim, field, this);
        ds.setArcs(4);
        ds.setRange(par.rockSensorRange);
        ds.setObjectTypes(Rock.class);
        ds.setNoise(par.sensorRangeNoise, par.sensorAngleNoise, DistanceSensorRays.UNIFORM);
        ds.ignoreRadius(true);
        super.addSensor(ds);

        // closest rovers distance
        DistanceSensorArcs dsr = new DistanceSensorArcs(sim, field, this);
        dsr.setArcs(4);
        dsr.setRange(par.roverSensorRange);
        dsr.setObjects(mr.rovers);
        dsr.setNoise(par.sensorRangeNoise, par.sensorAngleNoise, DistanceSensorRays.UNIFORM);
        super.addSensor(dsr);

        // closest rovers type
        for (int i = 0; i < par.numActuators; i++) {
            NeighbourTypeSensor ts = new NeighbourTypeSensor(dsr, i);
            super.addSensor(ts);
        }

        // rock type sensor
        /*for (RockType t : par.usedTypes) {
            RockTypeSensor rts = new RockTypeSensor(ds, t);
            super.addSensor(rts);
        }*/
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
