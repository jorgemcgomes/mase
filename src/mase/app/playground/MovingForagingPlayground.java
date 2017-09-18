/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import mase.app.playground.RandomMovingAgentsPlayground.RandomMovingAgent;
import mase.mason.world.CircularObject;

/**
 *
 * @author jorge
 */
public class MovingForagingPlayground extends ForagingPlayground {

    private static final long serialVersionUID = 1L;

    public MovingForagingPlayground(long seed) {
        super(seed);
    }

    @Override
    protected CircularObject createObject() {
        return new RandomMovingAgent(this);
    }

    @Override
    protected void placeObjects() {
        super.placeObjects();
        for (CircularObject obj : objects) {
            RandomMovingAgent rma = (RandomMovingAgent) obj;
            rma.stop = schedule.scheduleRepeating(rma);
        }
    }

}
