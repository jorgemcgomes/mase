/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.keepaway;

import java.util.ArrayList;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.mason.world.EmboddiedAgent;
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
        for (int i = 0; i < par.numKeepers; i++) {
            Keeper k = new Keeper(this, field, acs[0].clone(), par.passSpeed[i], par.moveSpeed[i], par.color[i]);
            k.setLocation(par.keeperStartPos[i]);
            k.setOrientation(par.keeperStartAngle[i]);
            k.setStopper(schedule.scheduleRepeating(k));
            k.enableAgentCollisions(par.collisions);
            keepers.add(k);
        }
    }

    @Override
    protected void placeTakers() {
        takers = new ArrayList<EmboddiedAgent>(1);
        AgentController[] acs = gc.getAgentControllers(2);
        CompetitiveTaker t = new CompetitiveTaker(this, field, acs[1].clone());
        if (par.takersPlacement == 0) {
            t.setLocation(center);
        } else  {
            double q = random.nextDouble() * Math.PI * 2;
            double r = Math.sqrt(random.nextDouble());
            double x = (par.takersPlacement * r) * Math.cos(q) + center.getX();
            double y = (par.takersPlacement * r) * Math.sin(q) + center.getY();
            t.setLocation(new Double2D(x, y));
        }
        Double2D ballDir = ball.getLocation().subtract(t.getLocation());
        t.setOrientation(ballDir.angle());
        t.enableAgentCollisions(par.collisions);
        t.setStopper(schedule.scheduleRepeating(t));
        takers.add(t);
    }
}
