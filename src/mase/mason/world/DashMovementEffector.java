/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.mason.world;

/**
 *
 * @author jorge
 */
public class DashMovementEffector extends AbstractEffector {
    
    private double linearSpeed;
    private double turnSpeed;
    
    public void setSpeeds(double linear, double turn) {
        this.linearSpeed = linear;
        this.turnSpeed = turn;
    }

    @Override
    public int valueCount() {
        return 2;
    }

    @Override
    public void action(double[] values) {
        double forward = values[0] * linearSpeed;
        double r = (values[1] * 2 - 1) * turnSpeed;
        ag.move(ag.orientation2D() + r, forward);
    }
    
}
