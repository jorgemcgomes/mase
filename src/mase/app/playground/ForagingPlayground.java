/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mase.controllers.GroupController;
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

    public ForagingPlayground(GroupController gc, long seed, PlaygroundParams par) {
        super(gc, seed, par);
    }

    @Override
    public void start() {
        super.start();
        itemRemover = new ItemRemover(super.objects);
        schedule.scheduleRepeating(itemRemover);
    }
    
    protected class ItemRemover implements Steppable {

        private static final long serialVersionUID = 1L;
        protected List<CircularObject> aliveObjects;

        private ItemRemover(List<CircularObject> objects) {
            aliveObjects = new ArrayList<>(objects);
        }

        @Override
        public void step(SimState state) {
            Iterator<CircularObject> iterator = aliveObjects.iterator();
            while(iterator.hasNext()) {
                CircularObject next = iterator.next();
                if(agent.distanceTo(next) == 0) {
                    iterator.remove();
                    field.remove(next);
                }
            }
            if(aliveObjects.isEmpty()) {
                kill();
            }
        }
    }
    
}
