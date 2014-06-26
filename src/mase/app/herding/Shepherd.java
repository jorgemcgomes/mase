/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.herding;

import java.awt.Color;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import mase.controllers.AgentController;
import mase.mason.world.DashMovementEffector;
import mase.mason.world.DistanceSensorArcs;
import mase.mason.world.RangeBearingSensor;
import mase.mason.world.SmartAgent;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class Shepherd extends SmartAgent {

    public Shepherd(Herding sim, Continuous2D field, AgentController ac) {
        super(sim, field, sim.par.agentRadius, Color.BLUE, ac);
        this.enableCollisionDetection(true);
        this.enableBoundedArena(true);

        DistanceSensorArcs ds = new DistanceSensorArcs();
        ds.setArcs(4);
        ds.setRange(sim.par.shepherdSensorRange);
        ds.setObjectTypes(Shepherd.class);
        
        RangeBearingSensor rbSheep = new RangeBearingSensor();
        rbSheep.setObjects(sim.sheeps);
        rbSheep.setSort(true);
        
        RangeBearingSensor rbGate = new RangeBearingSensor();
        rbGate.setObjects(Collections.singletonList(new Double2D(sim.par.arenaSize, sim.par.arenaSize / 2)));
        
        RangeBearingSensor rbFoxes = new RangeBearingSensor();
        rbFoxes.setObjects(sim.foxes);
        
        DashMovementEffector dm = new DashMovementEffector();
        dm.setSpeeds(sim.par.shepherdSpeed, sim.par.shepherdTurnSpeed);
        
        super.addSensor(ds);
        super.addSensor(rbSheep);
        super.addSensor(rbGate);
        super.addSensor(rbFoxes);
        super.addEffector(dm);
    }
}
