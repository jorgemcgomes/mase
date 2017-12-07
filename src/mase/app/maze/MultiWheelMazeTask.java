/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.maze;

import mase.mason.world.MultipleWheelAxesActuator;

/**
 *
 * @author Jorge
 */
public class MultiWheelMazeTask extends MazeTask {

    private static final long serialVersionUID = 1L;

    public MultiWheelMazeTask(long seed) {
        super(seed);
    }

    @Override
    protected MazeAgent createAgent() {
        ac.reset();
        MazeAgent ag = new MazeAgent(this, field, ac);
        ag.setupSensors(this);
        
        MultiWheelMazeParams mwpar = (MultiWheelMazeParams) par;

        MultipleWheelAxesActuator mw = new MultipleWheelAxesActuator(this, field, ag, mwpar.wheels);
        mw.setSpeedLimits(-mwpar.linearSpeed, mwpar.linearSpeed, mwpar.linearAcceleration);
        mw.setRotationLimits(Math.toRadians(mwpar.turnLimit), Math.toRadians(mwpar.turnAcceleration));
        mw.setSlipLimits(Math.toRadians(mwpar.slipAngleLimit), Math.toRadians(mwpar.parallelAngle), mwpar.friction);
        ag.addEffector(mw);
        return ag;
    }

}
