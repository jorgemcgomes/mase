/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.swarm;

import java.awt.Color;
import mase.controllers.AgentController;
import mase.mason.world.DifferentialDriveEffector;
import mase.mason.world.DistanceSensorArcs;
import mase.mason.world.MultilineObject;
import mase.mason.world.RaySensor;
import mase.mason.world.SmartAgent;

/**
 *
 * @author jorge
 */
public class SwarmAgent extends SmartAgent {

    private static final long serialVersionUID = 1L;

    public SwarmAgent(SwarmPlayground sim, AgentController ac) {
        super(sim, sim.field, sim.par.radius, Color.RED, ac);

        // Obstacle whisker sensors
        RaySensor dw = new RaySensor(sim, field, this);
        dw.setRays(sim.par.whiskerRange, -Math.PI / 2, -Math.PI / 4, 0, Math.PI / 4, Math.PI / 2, -Math.PI);
        dw.setObjectTypes(MultilineObject.class);
        dw.setBinary(false);
        this.addSensor(dw);

        // Agent cone sensor
        DistanceSensorArcs dsr = new DistanceSensorArcs(sim, field, this);
        dsr.setArcs(sim.par.numCones);
        dsr.setRange(sim.par.coneRange);
        dsr.setObjectTypes(SwarmAgent.class);
        dsr.centerToCenter(false);
        dsr.setBinary(false);
        dsr.setDrawColor(Color.RED);
        this.addSensor(dsr);

        // Object cone sensor (only if there is such thing)
        DistanceSensorArcs dso = new DistanceSensorArcs(sim, field, this);
        dso.setArcs(sim.par.poiNumCones);
        dso.setRange(sim.par.poiConeRange);
        dso.setObjectTypes(POI.class);
        dso.centerToCenter(false);
        dso.setBinary(false);
        dso.setDrawColor(Color.BLUE);
        this.addSensor(dso);

        // Effector
        DifferentialDriveEffector dd = new DifferentialDriveEffector(sim, field, this);
        dd.setSpeeds(sim.par.wheelSpeed, sim.par.wheelSpeed);
        dd.setAccelerations(sim.par.wheelAcc, sim.par.wheelAcc);
        dd.allowBackwardMove(sim.par.backMove);
        this.addEffector(dd);

        this.virtuallyBoundedArena(false); // the arena is bounded by physical walls
        this.enableCollisionRebound(true);
        this.setCollidableTypes(MultilineObject.class, SwarmAgent.class);
    }
}
