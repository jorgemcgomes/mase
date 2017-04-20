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
import sim.engine.SimState;

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
        for(CircularObject obj : objects) {
            schedule.scheduleRepeating((RandomMovingAgent) obj);
        }
    }
        
    protected class RandomMovingAgent extends EmboddiedAgent {

        private double currentOrientation = Double.NaN;
        
        public RandomMovingAgent(Playground pl) {
            super(pl, pl.field, pl.par.objectRadius, Color.BLUE);
            this.enableBoundedArena(true);
            this.setCollidableTypes(MultilineObject.class);
            this.enableCollisionRebound(false); // the rebound is handled special
        }

        @Override
        public void step(SimState state) {
            // orientation change
            if(Double.isNaN(currentOrientation)) {
                currentOrientation = -Math.PI + random.nextDouble() * Math.PI * 2;
            }
            
            boolean success = super.move(currentOrientation, par.linearSpeed * par.objectSpeed);
            if(!success) {
                currentOrientation = Double.NaN;
            }
        }
        
    }
    
}
