/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.foraging;

import java.awt.Color;
import java.util.Collections;
import mase.controllers.AgentController;
import mase.mason.world.DistanceSensorArcs;
import mase.mason.world.RangeBearingSensor;
import mase.mason.world.SmartAgent;
import org.apache.commons.math3.util.FastMath;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class FlyingRobot extends SmartAgent {
    
    protected FlyingEffector effector;
    protected DistanceSensorArcs itemArcs, botArcs;
    protected RangeBearingSensor centre;
    public static final Color COLOR = Color.YELLOW;

    public FlyingRobot(ForagingTask sim, Continuous2D field, AgentController ac) {
        super(sim, field, sim.par.flyingRadius, COLOR, ac);

        this.enableAgentCollisions(false);
        this.enableBoundedArena(false);
        this.enablePolygonCollisions(false);
        this.enableCollisionRebound(false);

        effector = new FlyingEffector();
        super.addEffector(effector);
        effector.enableAltitude(sim.par.flyingVerticalMovement);
        effector.setHeight(sim.par.flyingStartHeight);
        effector.setMaxHeight(sim.par.flyingMaxHeight);
        effector.setAccelerationLimits(sim.par.flyingLinearAcc, sim.par.flyingAngAcc);
        effector.calculateDragCoefficients(sim.par.flyingLinearSpeed, sim.par.flyingAngSpeed);
        
        double vRange = sim.par.flyingStartHeight * FastMath.tan(sim.par.flyingVisionAngle / 2);

        itemArcs = new DistanceSensorArcs();
        super.addSensor(itemArcs);
        itemArcs.ignoreRadius(true);
        itemArcs.setRange(vRange);
        itemArcs.setArcs(sim.par.flyingArcs);
        itemArcs.setBinary(false);
        itemArcs.setObjectTypes(Item.class);
        
        botArcs = new DistanceSensorArcs();
        super.addSensor(botArcs);
        botArcs.ignoreRadius(true);
        botArcs.setRange(vRange);
        botArcs.setArcs(sim.par.flyingArcs);
        botArcs.setBinary(false);
        botArcs.setObjectTypes(LandRobot.class);
        
        if(sim.par.flyingVerticalMovement) {
            HeightSensor hs = new HeightSensor();
            hs.setFlyingEffector(effector);
            super.addSensor(hs);
            
            effector.updateHeight();
        }
        
        centre = new RangeBearingSensor();
        centre.setObjects(Collections.singletonList(new Double2D(sim.par.arenaSize.x / 2, sim.par.arenaSize.y / 2)));
        centre.setRange(Double.POSITIVE_INFINITY);
        super.addSensor(centre);        
    }
}
