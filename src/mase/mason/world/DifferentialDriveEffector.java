/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author jorge
 */
public class DifferentialDriveEffector extends AbstractEffector {

    private double leftWheelMaxSpeed;
    private double rightWheelMaxSpeed;
    private boolean allowBackwardsMove = true;
    private double leftWheelAcc = Double.POSITIVE_INFINITY;
    private double rightWheelAcc = Double.POSITIVE_INFINITY;

    private double lwSpeed = 0;
    private double rwSpeed = 0;

    public DifferentialDriveEffector(SimState state, Continuous2D field, EmboddiedAgent ag) {
        super(state, field, ag);
    }

    /**
     * Linear speed limit -- how much can the wheel travel in one unit of time?
     *
     * @param leftWheelSpeed
     * @param rightWheelSpeed
     */
    public void setSpeeds(double leftWheelSpeed, double rightWheelSpeed) {
        this.leftWheelMaxSpeed = leftWheelSpeed;
        this.rightWheelMaxSpeed = rightWheelSpeed;
    }
    
    /**
     * Linear acceleration limit -- how much can the linear wheel speed change
     * in one unit of time?
     *
     * @param leftWheelAcc
     * @param rightWheelAcc
     */
    public void setAccelerations(double leftWheelAcc, double rightWheelAcc) {
        this.leftWheelAcc = leftWheelAcc;
        this.rightWheelAcc = rightWheelAcc;
    }

    public void allowBackwardMove(boolean allow) {
        this.allowBackwardsMove = allow;
    }

    @Override
    public int valueCount() {
        return 2;
    }

    @Override
    public void action(double[] values) {
        double lwSpeedDesired = allowBackwardsMove ? (values[0] * 2 - 1) * leftWheelMaxSpeed : values[0] * leftWheelMaxSpeed;
        double rwSpeedDesired = allowBackwardsMove ? (values[1] * 2 - 1) * rightWheelMaxSpeed : values[1] * rightWheelMaxSpeed;

        if (!Double.isInfinite(leftWheelAcc) && Math.abs(lwSpeedDesired - lwSpeed) > leftWheelAcc) {
            lwSpeedDesired = lwSpeedDesired < lwSpeed ? lwSpeed - leftWheelAcc : lwSpeed + leftWheelAcc;
        }
        if (!Double.isInfinite(rightWheelAcc) && Math.abs(rwSpeedDesired - rwSpeed) > rightWheelAcc) {
            rwSpeedDesired = rwSpeedDesired < rwSpeed ? rwSpeed - rightWheelAcc : rwSpeed + rightWheelAcc;
        }
        
        lwSpeed = lwSpeedDesired;
        rwSpeed = rwSpeedDesired;
        
        if(Math.abs(lwSpeed - rwSpeed) < 0.0001) { // go straight
            ag.move(ag.orientation2D(), lwSpeed);
        } else {
            double oriDelta = (rwSpeed - lwSpeed) / (2 * ag.getRadius());
            double distDelta = (lwSpeed + rwSpeed) / 2;
            ag.move(ag.orientation2D() + oriDelta, distDelta);
        }
    }
}
