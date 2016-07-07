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

    private static final long serialVersionUID = 1L;
    
    protected FlyingEffector effector;
    protected DistanceSensorArcs itemArcs, botArcs;
    protected RangeBearingSensor centre;
    public static final Color COLOR = Color.RED;

    public FlyingRobot(ForagingTask sim, Continuous2D field, AgentController ac) {
        super(sim, field, sim.par.flyingRadius, COLOR, ac);

        this.enableAgentCollisions(false);
        this.enableBoundedArena(false);
        this.enablePolygonCollisions(false);
        this.enableCollisionRebound(false);

        effector = new FlyingEffector(sim, field, this);
        super.addEffector(effector);
        effector.enableAltitude(sim.par.flyingVerticalMovement);
        effector.setHeight(sim.par.flyingStartHeight);
        effector.setMaxHeight(sim.par.flyingMaxHeight);
        effector.setAccelerationLimits(sim.par.flyingLinearAcc, sim.par.flyingAngAcc);
        effector.calculateDragCoefficients(sim.par.flyingLinearSpeed, sim.par.flyingAngSpeed);
        effector.setNoise(sim.par.actuatorNoise);
        
        double vRange = sim.par.flyingStartHeight * FastMath.tan(sim.par.flyingVisionAngle / 2);

        itemArcs = new DistanceSensorArcs(sim, field, this);
        super.addSensor(itemArcs);
        itemArcs.ignoreRadius(true);
        itemArcs.setRange(vRange);
        itemArcs.setArcs(sim.par.flyingArcs);
        itemArcs.setBinary(false);
        itemArcs.setObjectTypes(Item.class);
        itemArcs.setNoise(sim.par.sensorRangeNoise, sim.par.sensorAngleNoise, DistanceSensorArcs.UNIFORM);
        
        botArcs = new DistanceSensorArcs(sim, field, this);
        super.addSensor(botArcs);
        botArcs.ignoreRadius(true);
        botArcs.setRange(vRange);
        botArcs.setArcs(sim.par.flyingArcs);
        botArcs.setBinary(false);
        botArcs.setObjectTypes(LandRobot.class);
        botArcs.setNoise(sim.par.sensorRangeNoise, sim.par.sensorAngleNoise, DistanceSensorArcs.UNIFORM);
        
        if(sim.par.flyingVerticalMovement) {
            HeightSensor hs = new HeightSensor(sim, field, this);
            hs.setFlyingEffector(effector);
            hs.setNoise(sim.par.sensorRangeNoise);
            super.addSensor(hs);
            effector.updateHeight();
        }
        
        centre = new RangeBearingSensor(sim, field, this);
        centre.setObjects(Collections.singletonList(new Double2D(sim.par.arenaSize.x / 2, sim.par.arenaSize.y / 2)));
        centre.setRange(Double.POSITIVE_INFINITY);
        centre.setNoise(sim.par.sensorRangeNoise, sim.par.sensorAngleNoise, DistanceSensorArcs.UNIFORM);
        super.addSensor(centre);        
    }
}
