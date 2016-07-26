/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.multirover;

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
    protected int[] captured;

    public Rover(MultiRover sim, Continuous2D field, AgentController ac) {
        super(sim, field, sim.par.agentRadius, RockEffector.NOACTUATOR_COLOUR, ac);
        this.enableAgentCollisions(true);
        this.enableBoundedArena(true);

        if (Double.isInfinite(sim.par.rockSensorRange)) {
            super.circledPortrayal.scale = sim.par.roverSensorRange * 2;
        } else {
            super.circledPortrayal.scale = sim.par.rockSensorRange * 2;
        }

        DashMovementEffector dm = new DashMovementEffector(sim, field, this);
        dm.setSpeeds(sim.par.linearSpeed, sim.par.turnSpeed);
        dm.allowBackwardMove(false);
        dm.setNoise(sim.par.actuatorNoise, sim.par.actuatorNoise);
        super.addEffector(dm);

        if (sim.par.typeSensorMode == MRParams.TYPE_BINARY) {
            RockEffector re = new RockEffector(sim, field, this);
            super.addEffector(re);
        } else if (sim.par.typeSensorMode == MRParams.TYPE_DISCRETISED) {
            MultiRockTypeEffector re = new MultiRockTypeEffector(sim, field, this);
            super.addEffector(re);
        }
        
        captured = new int[sim.par.usedTypes.size()];
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
        SelectiveRockSensor rockSensor = new SelectiveRockSensor(mr, field, this);
        rockSensor.setArcs(6);
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
        dsr.ignoreRadius(true); // not relevant
        super.addSensor(dsr);

        // closest rovers type
        if (mr.par.typeSensorMode == MRParams.TYPE_BINARY) {
            for (int i = 0; i < par.numActuators; i++) {
                NeighbourTypeSensor ts = new NeighbourTypeSensor(dsr, i);
                super.addSensor(ts);
            }
        } else if (mr.par.typeSensorMode == MRParams.TYPE_DISCRETISED) {
            NeighbourMultiTypeSensor ts = new NeighbourMultiTypeSensor(dsr);
            super.addSensor(ts);
        }
    }

    public int getActuatorType() {
        return actuatorType;
    }
}
