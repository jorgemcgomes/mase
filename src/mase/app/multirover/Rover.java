/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.multirover;

import java.util.Arrays;
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
    protected int actuatorType = RockEffector.NO_ACTIVATION;
    protected int captured = 0;

    public Rover(MultiRover sim, Continuous2D field, AgentController ac) {
        super(sim, field, sim.par.agentRadius, RockEffector.NOACTUATOR_COLOUR, ac);
        this.enableAgentCollisions(true);
        this.enableBoundedArena(true);

        super.circledPortrayal.scale = (sim.par.agentRadius + sim.par.rockSensorRange) * 2;
        
        DashMovementEffector dm = new DashMovementEffector(sim, field, this);
        dm.setSpeeds(sim.par.linearSpeed, sim.par.turnSpeed);
        dm.allowBackwardMove(false);
        dm.setNoise(sim.par.actuatorNoise, sim.par.actuatorNoise);
        super.addEffector(dm);
        
        RockEffector re = new RockEffector(sim, field, this);
        super.addEffector(re);

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
        SelectiveRockSensor rockSensor = new SelectiveRockSensor(sim, field, this);
        rockSensor.setArcs(4);
        rockSensor.setRange(par.rockSensorRange);
        rockSensor.setObjectTypes(Rock.class);
        rockSensor.setNoise(par.sensorRangeNoise, par.sensorAngleNoise, DistanceSensorRays.UNIFORM);
        rockSensor.ignoreRadius(true);
        super.addSensor(rockSensor);

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
    }

    public int getActuatorType() {
        return actuatorType;
    }
}
