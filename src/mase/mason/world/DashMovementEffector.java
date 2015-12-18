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

    public static final int UNIFORM = 0, GAUSSIAN = 1;
    private double linearSpeed;
    private double turnSpeed;
    private boolean backward = false;
    private double turnNoise = 0;
    private double linearNoise = 0;
    private int noiseType;

    public void setSpeeds(double linear, double turn) {
        this.linearSpeed = linear;
        this.turnSpeed = turn;
    }

    public void allowBackwardMove(boolean allow) {
        this.backward = allow;
    }

    public void setTurnNoise(double noise) {
        this.turnNoise = noise;
    }

    public void setLinearNoise(double noise) {
        this.linearNoise = noise;
    }

    public void setNoiseType(int type) {
        this.noiseType = type;
    }

    @Override
    public int valueCount() {
        return 2;
    }

    @Override
    public void action(double[] values) {
        double maxSpeed = linearSpeed;
        if (linearNoise > 0) {
            maxSpeed += linearNoise * (noiseType == UNIFORM ? state.random.nextDouble() * 2 - 1 : state.random.nextGaussian());
            maxSpeed = Math.max(0, maxSpeed);
        }
        double maxTurnSpeed = turnSpeed;
        if (turnNoise > 0) {
            maxTurnSpeed += turnNoise * (noiseType == UNIFORM ? state.random.nextDouble() * 2 - 1 : state.random.nextGaussian());
            maxTurnSpeed = Math.max(0, maxTurnSpeed);
        }
        double forward = backward ? (values[0] * 2 - 1) * maxSpeed : values[0] * maxSpeed;
        double r = (values[1] * 2 - 1) * maxTurnSpeed;
        ag.move(ag.orientation2D() + r, forward);
    }

}
