/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.foraging;

import java.awt.Color;
import mase.mason.world.AbstractEffector;
import mase.mason.world.DistanceSensorArcs;
import mase.mason.world.EmboddiedAgent;
import mase.mason.world.Sensor;
import mase.mason.world.SmartAgent;
import org.apache.commons.math3.util.FastMath;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class FlyingEffector extends AbstractEffector {

    public static final double MIN_SCALE = 10;

    private double maxLinearAcc;
    private double maxAngularAcc;

    private double xVelocity;
    private double yVelocity;
    private double rVelocity;
    private double zVelocity;

    private double height;
    private double maxHeight;

    private double linearDragCoeff = 0;
    private double rotationDragCoeff = 0;

    private boolean enableAltitude;
    
    private double noise = 0;

    public FlyingEffector(SimState state, Continuous2D field, EmboddiedAgent ag) {
        super(state, field, ag);
    }


    public void setAccelerationLimits(double linear, double turn) {
        this.maxLinearAcc = linear;
        this.maxAngularAcc = turn;
    }

    /*
     * Always call setAccelerationLimits first.
     */
    public void calculateDragCoefficients(double maxLinearVelocity, double maxAngularVelocity) {
        this.linearDragCoeff = maxLinearAcc / maxLinearVelocity;
        this.rotationDragCoeff = maxAngularAcc / maxAngularVelocity;
        this.xVelocity = 0;
        this.yVelocity = 0;
        this.rVelocity = 0;
        this.zVelocity = 0;
    }

    @Override
    public int valueCount() {
        return enableAltitude ? 4 : 3;
    }

    public void enableAltitude(boolean enable) {
        this.enableAltitude = enable;
    }

    public void setMaxHeight(double h) {
        this.maxHeight = h;
    }

    public void setHeight(double h) {
        this.height = h;
    }

    /**
     * @param linearNoise In percentage, relative to max linear speed
     * @param turnNoise In percentage, relative to max turn speed
     * @param type Uniform (0) or Gaussian (1)
     */
    public void setNoise(double noise) {
        this.noise = noise;
    }    
    
    @Override
    public void action(double[] values) {
        if (height > 0.1) {
            // PLANAR MOVEMENT
            double xThrust = (values[1] * 2 - 1) * maxLinearAcc; // left -- right
            double yThrust = (values[0] * 2 - 1) * maxLinearAcc; // forward -- backwards
            if(noise > 0) {
                xThrust += xThrust * (state.random.nextDouble() * 2 - 1) * noise;
                yThrust += yThrust * (state.random.nextDouble() * 2 - 1) * noise;
            }
            
            double xDrag = xVelocity * linearDragCoeff;
            double yDrag = yVelocity * linearDragCoeff;

            xVelocity = xVelocity + xThrust - xDrag;
            yVelocity = yVelocity + yThrust - yDrag;
            Double2D velocity = new Double2D(xVelocity, yVelocity);

            velocity = velocity.rotate(ag.orientation2D());
            Double2D newLoc = ag.getLocation().add(velocity);

            ag.setLocation(newLoc);
            ag.setSpeed(velocity.length());

            // ROTATION
            double rThrust = (values[2] * 2 - 1) * maxAngularAcc; // left -- right// rotation around the axis
            if(noise > 0) {
                rThrust += rThrust * (state.random.nextDouble() * 2 - 1) * noise;
            }
            double rDrag = /*FastMath.pow2*/ (rVelocity) * rotationDragCoeff;
            double rAcc = rThrust - rDrag;
            rVelocity = rVelocity + rAcc;
            ag.setOrientation(EmboddiedAgent.normalizeAngle(ag.orientation2D() + rVelocity));

        } else {
            xVelocity = 0;
            yVelocity = 0;
            ag.setSpeed(0);
            rVelocity = 0;
        }

        // VERTICAL MOVEMENT
        if (enableAltitude) {
            double zThrust = (values[3] * 2 - 1) * maxLinearAcc;
            if(noise > 0) {
                zThrust += zThrust * (state.random.nextDouble() * 2 - 1) * noise;
            }
            double zDrag = zVelocity * linearDragCoeff;
            zVelocity = zVelocity + zThrust - zDrag;
            if (height + zVelocity < 0) {
                zVelocity = -height;
            }
            height += zVelocity;

            // Altitude changed, change sensors
            if (zVelocity > 0.01 || zVelocity < 0.01) {
                updateHeight();
            }
        }
    }

    public void updateHeight() {
        ForagingTask ft = (ForagingTask) super.state;
        // Update flying bot sensors
        double vr = height > maxHeight ? 0 : height * FastMath.tan(ft.par.flyingVisionAngle / 2);
        for (Sensor sens : ((SmartAgent) ag).getSensors()) {
            if (sens instanceof DistanceSensorArcs) {
                DistanceSensorArcs dsa = (DistanceSensorArcs) sens;
                dsa.setRange(vr);
            }
        }

        // Update land bot sensors
        double lr = height > maxHeight ? 0 : height * FastMath.tan(ft.par.landVisionAngle / 2);
        DistanceSensorArcs sens = (DistanceSensorArcs) ft.landBot.getSensors().get(1);
        sens.setRange(lr);

        // Update apperance
        double newScale = MIN_SCALE + (ft.par.flyingRadius * 2 - MIN_SCALE) * ((-Math.min(height, maxHeight) + maxHeight) / maxHeight);
        OvalPortrayal2D child = (OvalPortrayal2D) ag.getChild(null);
        child.scale = newScale;
        if (height < 0.1) {
            child.paint = Color.BLACK;
        } else if (height > maxHeight) {
            child.paint = Color.ORANGE;
        } else {
            child.paint = FlyingRobot.COLOR;
        }
        if(height < 5) {
            ag.setCollidableTypes(EmboddiedAgent.class);
            ft.landBot.setCollidableTypes(EmboddiedAgent.class);
        } else {
            ag.setCollidableTypes();
            ft.landBot.setCollidableTypes();            
        }
        
    }

    public double getHeight() {
        return height;
    }

    public double getMaxHeight() {
        return maxHeight;
    }
}
