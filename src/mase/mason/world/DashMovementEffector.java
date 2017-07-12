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
public class DashMovementEffector extends AbstractEffector {

    private double linearSpeed;
    private double turnSpeed;
    private double linearAcc = Double.POSITIVE_INFINITY;
    private double turnAcc = Double.POSITIVE_INFINITY;
    private boolean backward = false;
    private double turnNoise = 0;
    private double linearNoise = 0;

    public DashMovementEffector(SimState state, Continuous2D field, EmboddiedAgent ag) {
        super(state, field, ag);
    }

    public void setSpeeds(double linear, double turn) {
        this.linearSpeed = linear;
        this.turnSpeed = turn;
    }
    
    public void setAccelerations(double linear, double turn) {
        this.linearAcc = linear;
        this.turnAcc = turn;
    }

    public void allowBackwardMove(boolean allow) {
        this.backward = allow;
    }

    /**
     * @param linearNoise In percentage, relative to max linear speed
     * @param turnNoise In percentage, relative to max turn speed
     */
    public void setNoise(double linearNoise, double turnNoise) {
        this.linearNoise = linearNoise;
        this.turnNoise = turnNoise;
    }

    @Override
    public int valueCount() {
        return 2;
    }

    @Override
    public void action(double[] values) {
        double maxSpeed = linearSpeed;
        if (linearNoise > 0) {
            maxSpeed += (linearNoise * maxSpeed) * (state.random.nextDouble() * 2 - 1);
            maxSpeed = Math.max(0, maxSpeed);
        }
        double maxTurnSpeed = turnSpeed;
        if (turnNoise > 0) {
            maxTurnSpeed += (turnNoise * maxTurnSpeed) * (state.random.nextDouble() * 2 - 1);
            maxTurnSpeed = Math.max(0, maxTurnSpeed);
        }
        double linear = backward ? (values[0] * 2 - 1) * maxSpeed : values[0] * maxSpeed;
        double turn = (values[1] * 2 - 1) * maxTurnSpeed;
        
        if(!Double.isInfinite(linearAcc) && Math.abs(linear - ag.getSpeed()) > linearAcc) {
           linear = linear < ag.getSpeed() ? ag.getSpeed() - linearAcc : ag.getSpeed() + linearAcc;
        }
        if(!Double.isInfinite(turnAcc) && Math.abs(turn - ag.getTurningSpeed()) > turnAcc) {
            turn = turn < ag.getTurningSpeed() ? ag.getTurningSpeed() - turnAcc : ag.getTurningSpeed() + turnAcc;
        }
                
        ag.move(ag.orientation2D() + turn, linear);
    }

}
