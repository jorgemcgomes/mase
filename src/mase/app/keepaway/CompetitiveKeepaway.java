/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.keepaway;

import java.util.ArrayList;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.mason.EmboddiedAgent;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class CompetitiveKeepaway extends Keepaway {

    public CompetitiveKeepaway(long seed, KeepawayParams par, GroupController gc) {
        super(seed, par, gc);
    }

    @Override
    protected void placeKeepers() {
        keepers = new ArrayList<Keeper>(par.numKeepers);
        AgentController[] acs = gc.getAgentControllers(2);
        // place keepers evenly distributed around the circle
        Double2D up = new Double2D(0, par.ringSize / 2);
        double rot = Math.PI * 2 / par.numKeepers;
        for(int i = 0 ; i < par.numKeepers ; i++) {
            Keeper k = new Keeper(this, field, acs[0].clone(), par.passSpeed[i], par.moveSpeed[i], par.color[i]);
            Double2D v = up.rotate(rot * i);
            k.setLocation(v.add(center));
            k.setOrientation(v.negate().angle());
            k.setStopper(schedule.scheduleRepeating(k));
            k.enableCollisionDetection(par.collisions);
            keepers.add(k);
        }
    }

    @Override
    protected void placeTakers() {
        takers = new ArrayList<EmboddiedAgent>(1);
        AgentController[] acs = gc.getAgentControllers(2);
        CompetitiveTaker t = new CompetitiveTaker(this, field, acs[1].clone());
        if(par.takersPlacement == KeepawayParams.V_CENTER) {
            t.setLocation(center);
        } else if(par.takersPlacement == KeepawayParams.V_RANDOM) {
            double q = random.nextDouble() * Math.PI *  2;
            double r = Math.sqrt(random.nextDouble());
            double x = (par.placeRadius * r) * Math.cos(q) + center.getX();
            double y = (par.placeRadius * r) * Math.sin(q) + center.getY();
            t.setLocation(new Double2D(x,y));
        }
        t.enableCollisionDetection(par.collisions);
        t.setStopper(schedule.scheduleRepeating(t));
        takers.add(t);
    }
}
