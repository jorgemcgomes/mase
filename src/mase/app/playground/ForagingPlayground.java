/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mase.app.playground.RandomMovingAgentsPlayground.RandomMovingAgent;
import mase.mason.world.CircularObject;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 *
 * @author jorge
 */
public class ForagingPlayground extends Playground {

    private static final long serialVersionUID = 1L;
    protected ItemRemover itemRemover;

    public ForagingPlayground(long seed) {
        super(seed);
    }

    @Override
    public void start() {
        super.start();
        itemRemover = new ItemRemover(this);
        schedule.scheduleRepeating(itemRemover);
    }
    
    protected static class ItemRemover implements Steppable {

        private static final long serialVersionUID = 1L;
        protected List<CircularObject> aliveObjects;
        protected Playground pl;
        protected List<ForagingHook> hooks;

        protected ItemRemover(Playground pl) {
            this.pl = pl;
            aliveObjects = new ArrayList<>(pl.objects);
            hooks = new ArrayList<>();
        }

        @Override
        public void step(SimState state) {
            Iterator<CircularObject> iterator = aliveObjects.iterator();
            while(iterator.hasNext()) {
                CircularObject next = iterator.next();
                if(pl.agent.distanceTo(next) == 0) {
                    iterator.remove();
                    pl.field.remove(next);
                    if(next instanceof RandomMovingAgent) {
                        ((RandomMovingAgent) next).stop.stop();
                    }
                    for(ForagingHook h : hooks) {
                        h.foraged((ForagingPlayground) state, (PlaygroundAgent) pl.agent, next);
                    }
                }
            }
            if(aliveObjects.isEmpty()) {
                pl.kill();
            }
        }
        
        protected void addForagingHook(ForagingHook hook) {
            hooks.add(hook);
        }
        
        protected static interface ForagingHook {
            public void foraged(ForagingPlayground sim, PlaygroundAgent ag, CircularObject foraged);
        }
    }
        
}
