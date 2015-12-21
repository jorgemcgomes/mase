/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.foraging;

import java.awt.Color;
import java.util.Iterator;
import mase.controllers.AgentController;
import mase.mason.world.DashMovementEffector;
import mase.mason.world.DistanceSensorArcs;
import mase.mason.world.SmartAgent;
import org.apache.commons.math3.util.FastMath;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author jorge
 */
public class LandRobot extends SmartAgent {

    private static final long serialVersionUID = 1L;
    
    protected DistanceSensorArcs itemArcs;
    protected DistanceSensorArcs botArcs;
    protected DashMovementEffector dm;

    public LandRobot(ForagingTask sim, Continuous2D field, AgentController ac) {
        super(sim, field, sim.par.landRadius, Color.BLUE, ac);

        this.enableAgentCollisions(sim.par.flyingStartHeight < 5);
        this.enableCollisionRebound(sim.par.flyingStartHeight < 5);
        this.enableBoundedArena(false);
        this.enablePolygonCollisions(false);
        //this.setOrientationShowing(false);

        // movement effector
        dm = new DashMovementEffector();
        super.addEffector(dm);
        dm.allowBackwardMove(false);
        dm.setSpeeds(sim.par.landLinearSpeed, sim.par.landTurnSpeed);
        dm.setNoise(sim.par.actuatorNoise, sim.par.actuatorNoise);

        itemArcs = new DistanceSensorArcs();
        super.addSensor(itemArcs);
        itemArcs.setRange(sim.par.landSensingRange);
        itemArcs.setArcs(sim.par.landArcs);
        itemArcs.setBinary(true);
        itemArcs.setObjectTypes(Item.class);
        itemArcs.setNoise(sim.par.sensorRangeNoise, sim.par.sensorAngleNoise, DistanceSensorArcs.UNIFORM);

        botArcs = new DistanceSensorArcs();
        super.addSensor(botArcs);
        botArcs.ignoreRadius(true);
        double vRange = sim.par.flyingStartHeight * FastMath.tan(sim.par.flyingVisionAngle / 2);
        botArcs.setRange(vRange);
        botArcs.setArcs(sim.par.landArcs);
        botArcs.setBinary(true);
        botArcs.setObjectTypes(FlyingRobot.class);
        botArcs.setNoise(sim.par.sensorRangeNoise, sim.par.sensorAngleNoise, DistanceSensorArcs.UNIFORM);
    }

    @Override
    public void action(double[] output) {
        super.action(output);

        ForagingTask ft = (ForagingTask) super.sim;
        Iterator<Item> iter = ft.items.iterator();
        while (iter.hasNext()) {
            Item i = iter.next();
            if (this.distanceTo(i.getLocation()) < i.radius) {
                ft.field.remove(i);
                iter.remove();
            }
        }
        if (ft.items.isEmpty()) {
            ft.kill();
        }
    }

}
