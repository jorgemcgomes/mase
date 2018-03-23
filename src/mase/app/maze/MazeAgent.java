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
import mase.mason.world.MultilineObject;
import sim.field.continuous.Continuous2D;
import sim.portrayal.simple.RectanglePortrayal2D;

/**
 *
 * @author jorge
 */
public class MazeAgent extends SmartAgent {

    public static final Color COLOR = Color.BLUE;
    private static final long serialVersionUID = 1L;
    private boolean instaKill = false;

    public MazeAgent(MazeTask sim, Continuous2D field, AgentController ac) {
        super(sim, field, sim.par.agentRadius, COLOR, ac);
        ac.reset();
        this.virtuallyBoundedArena(false);
        this.enableCollisionRebound(false);
        this.enableRotationWithCollisions(true);
        this.setCollidableTypes(MultilineObject.class);
        this.instaKill = sim.par.instaKill;
        this.setLabel("Agent");
        this.replaceAgentPortrayal(new RectanglePortrayal2D());
    }
    
    protected void setupSensors(MazeTask sim) {
        DistanceSensorArcs arcSensor = new DistanceSensorArcs(sim, field, this);
        super.addSensor(arcSensor);
        arcSensor.setRange(Double.POSITIVE_INFINITY);
        arcSensor.setArcs(4);
        arcSensor.setBinary(true);
        arcSensor.setObjects(Collections.singleton(sim.target));
        
        RaySensor raySensor = new RaySensor(sim, field, this);
        super.addSensor(raySensor);
        raySensor.setObjects(Collections.singleton(sim.maze));
        raySensor.setBinary(false);
        raySensor.setRays(sim.par.sensorRange, -Math.PI/2, -Math.PI / 4, 0, Math.PI / 4, Math.PI / 2, Math.PI);
        
        if(sim.par.zonesMaxSpeed != null) {
            ZoneSensor zs = new ZoneSensor(sim, field, this);
            super.addSensor(zs);
        }
    }
    
    protected void setupActuators(MazeTask sim) {
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
        if(this.distanceTo(mt.target) <= 0) {
            mt.kill(); // done
        }
        // hit wall
        if(instaKill && isInCollision()) {
            mt.kill(); // stop
        }
    }
    
    
}
