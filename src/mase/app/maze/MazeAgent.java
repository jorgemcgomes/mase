/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.maze;

import java.awt.Color;
import java.util.Collections;
import mase.controllers.AgentController;
import mase.mason.world.DashMovementEffector;
import mase.mason.world.DistanceSensorArcs;
import mase.mason.world.RaySensor;
import mase.mason.world.SmartAgent;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author jorge
 */
public class MazeAgent extends SmartAgent {

    public static final Color COLOR = Color.BLUE;
    private static final long serialVersionUID = 1L;

    public MazeAgent(MazeTask sim, Continuous2D field, AgentController ac) {
        super(sim, field, sim.par.agentRadius, COLOR, ac);
        this.enableAgentCollisions(false);
        this.enableBoundedArena(false);
        this.enablePolygonCollisions(true);
        this.enableCollisionRebound(false);
        
        DistanceSensorArcs arcSensor = new DistanceSensorArcs(sim, field, this);
        super.addSensor(arcSensor);
        arcSensor.setRange(Double.POSITIVE_INFINITY);
        arcSensor.setArcs(4);
        arcSensor.setBinary(true);
        arcSensor.setObjects(Collections.singleton(sim.target));
        
        RaySensor raySensor = new RaySensor(sim, field, this);
        super.addSensor(raySensor);
        raySensor.setBinary(false);
        raySensor.setRays(sim.par.sensorRange, -Math.PI/2, -Math.PI / 4, 0, Math.PI / 4, Math.PI / 2, Math.PI);
        

        DashMovementEffector dm = new DashMovementEffector(sim, field, this);
        super.addEffector(dm);
        dm.allowBackwardMove(true);
        dm.setSpeeds(sim.par.linearSpeed, sim.par.turnSpeed);
    }

    @Override
    public void action(double[] output) {
        super.action(output);
        
        MazeTask mt = (MazeTask) sim;
        // target reached
        if(mt.target.distanceTo(this) <= 0) {
            mt.kill(); // done
        }
    }
}
