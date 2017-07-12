/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import java.awt.Color;
import mase.controllers.GroupController;
import mase.mason.world.CircularObject;
import mase.mason.world.EmboddiedAgent;
import mase.mason.world.MultilineObject;
import mase.mason.world.SmartAgent;
import sim.engine.SimState;
import sim.engine.Stoppable;

/**
 *
 * @author jorge
 */
public class RandomMovingAgentsPlayground extends Playground {

    private static final long serialVersionUID = 1L;

    public RandomMovingAgentsPlayground(GroupController gc, long seed, PlaygroundParams par) {
        super(gc, seed, par);
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

    protected static class RandomMovingAgent extends EmboddiedAgent {

        private static final long serialVersionUID = 1L;

        private double currentOrientation = Double.NaN;
        protected Stoppable stop;

        public RandomMovingAgent(Playground pl) {
            super(pl, pl.field, pl.par.objectRadius, Color.BLUE);
            this.enableBoundedArena(true);
            this.setCollidableTypes(MultilineObject.class);
            this.enableCollisionRebound(false); // the rebound is handled special
        }

        @Override
        public void step(SimState state) {
            // orientation change
            if (Double.isNaN(currentOrientation)) {
                currentOrientation = -Math.PI + sim.random.nextDouble() * Math.PI * 2;
            }

            boolean success = super.move(currentOrientation, ((Playground) sim).par.linearSpeed * ((Playground) sim).par.objectSpeed);
            if (!success) {
                currentOrientation = Double.NaN;
            }
        }
        
    }

}
