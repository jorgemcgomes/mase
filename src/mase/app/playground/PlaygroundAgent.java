/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import java.awt.Color;
import mase.controllers.AgentController;
import mase.mason.world.CircularObject;
import mase.mason.world.DashMovementEffector;
import mase.mason.world.DistanceSensorArcs;
import mase.mason.world.MultilineObject;
import mase.mason.world.RaySensor;
import mase.mason.world.SmartAgent;
import net.jafama.FastMath;

/**
 *
 * @author jorge
 */
public class PlaygroundAgent extends SmartAgent {

    private static final long serialVersionUID = 1L;

    public PlaygroundAgent(Playground sim, AgentController ac) {
        super(sim, sim.field, sim.par.radius, Color.RED, ac);
        
        this.circledPortrayal.setOnlyCircleWhenSelected(false);
        this.circledPortrayal.setCircleShowing(true);
        this.circledPortrayal.scale = sim.par.coneRange;     
        
        // Obstacle whisker sensor
        RaySensor dw = new RaySensor(sim, field, this);
        dw.setRays(sim.par.whiskerRange, -FastMath.toRadians(45), 0, FastMath.toRadians(45)); // left, front, right
        dw.setObjectTypes(MultilineObject.class);
        dw.setBinary(false);
        this.addSensor(dw);

        // Object cone sensor
        DistanceSensorArcs dsr = new DistanceSensorArcs(sim, field, this);
        dsr.setArcs(sim.par.numCones);
        dsr.setRange(sim.par.coneRange);
        dsr.setObjectTypes(CircularObject.class);
        dsr.centerToCenter(false);
        dsr.setBinary(false);
        this.addSensor(dsr);

        // Effector
        DashMovementEffector dm = new DashMovementEffector(sim, field, this);
        dm.setSpeeds(sim.par.linearSpeed, sim.par.turnSpeed);
        dm.allowBackwardMove(sim.par.backMove);
        this.addEffector(dm);        

        this.enableBoundedArena(true);
        this.enableCollisionRebound(false);
        this.setCollidableTypes(MultilineObject.class);
    }
    
    
    
}
