/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.herding;

import java.awt.Color;
import java.util.Collections;
import mase.controllers.AgentController;
import mase.mason.world.DashMovementEffector;
import mase.mason.world.DistanceSensorArcs;
import mase.mason.world.EmboddiedAgent;
import mase.mason.world.RangeBearingSensor;
import mase.mason.world.SmartAgent;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author jorge
 */
public class Shepherd extends SmartAgent {

    private static final long serialVersionUID = 1L;
    
    public Shepherd(Herding sim, Continuous2D field, AgentController ac) {
        super(sim, field, sim.par.agentRadius, Color.BLUE, ac);
        this.setCollidableTypes(EmboddiedAgent.class);
        this.virtuallyBoundedArena(true);
        
        DistanceSensorArcs ds = new DistanceSensorArcs(sim, field, this);
        ds.setArcs(4);
        ds.setRange(sim.par.shepherdSensorRange);
        ds.setObjectTypes(Shepherd.class);
        ds.setNoise(sim.par.sensorRangeNoise, sim.par.sensorAngleNoise, DistanceSensorArcs.UNIFORM);
        super.addSensor(ds);
        
        RangeBearingSensor rbSheep = new RangeBearingSensor(sim, field, this);
        rbSheep.setObjects(sim.sheeps);
        rbSheep.setSort(false);
        rbSheep.setNoise(sim.par.sensorRangeNoise, sim.par.sensorAngleNoise, DistanceSensorArcs.UNIFORM);
        super.addSensor(rbSheep);
        
        RangeBearingSensor rbGate = new RangeBearingSensor(sim, field, this);
        rbGate.setObjects(Collections.singletonList(sim.curralCenter));
        rbGate.setNoise(sim.par.sensorRangeNoise, sim.par.sensorAngleNoise, DistanceSensorArcs.UNIFORM);
        super.addSensor(rbGate);
        
        if (sim.par.shepherdArcSensor) {
            ds = new DistanceSensorArcs(sim, field, this);
            ds.setArcs(4);
            ds.setRange(sim.par.shepherdSensorRange);
            ds.setObjectTypes(Fox.class);
            ds.setNoise(sim.par.sensorRangeNoise, sim.par.sensorAngleNoise, DistanceSensorArcs.UNIFORM);
            super.addSensor(ds);
        } else {
            RangeBearingSensor rbFoxes = new RangeBearingSensor(sim, field, this);
            rbFoxes.setObjects(sim.foxes);
            rbFoxes.setSort(false);
            rbFoxes.setNoise(sim.par.sensorRangeNoise, sim.par.sensorAngleNoise, DistanceSensorArcs.UNIFORM);
            super.addSensor(rbFoxes);
        }
        
        DashMovementEffector dm = new DashMovementEffector(sim, field, this);
        dm.setSpeeds(sim.par.shepherdLinearSpeed, sim.par.shepherdTurnSpeed);
        dm.setNoise(sim.par.actuatorNoise, sim.par.actuatorNoise);
        super.addEffector(dm);
        
    }
}
