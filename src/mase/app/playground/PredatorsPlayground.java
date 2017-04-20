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
import mase.mason.world.WorldObject;
import sim.engine.SimState;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class PredatorsPlayground extends Playground {

    private static final long serialVersionUID = 1L;
    
    public PredatorsPlayground(GroupController gc, long seed, PlaygroundParams par) {
        super(gc, seed, par);
    }

    @Override
    protected CircularObject createObject() {
        return new PredatorAgent(this);
    }

    @Override
    protected void placeObjects() {
        super.placeObjects();
        for(CircularObject obj : objects) {
            schedule.scheduleRepeating((PredatorAgent) obj);
        }
    }
        
    protected class PredatorAgent extends EmboddiedAgent {

        
        public PredatorAgent(Playground pl) {
            super(pl, pl.field, pl.par.objectRadius, Color.BLUE);
            this.enableBoundedArena(true);
            this.enableCollisionRebound(true);
            this.setCollidableTypes(WorldObject.class);
            this.setCollisionSpeedDecay(1); // no decay
        }

        @Override
        public void step(SimState state) {
            // orientation change
            Double2D pursueVec = agent.getLocation().subtract(getLocation());
            super.move(pursueVec.angle(), par.linearSpeed * par.objectSpeed);
        }
        
    }
    
}
