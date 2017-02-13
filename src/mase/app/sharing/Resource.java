/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.sharing;

import java.awt.Color;
import mase.mason.generic.systematic.Entity;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class Resource extends OvalPortrayal2D implements Steppable, Entity {

    private final int[] stay;
    private boolean charging;
    protected Double2D location;
    protected double radius;

    public Resource(ResourceSharing sim) {
        super(Color.YELLOW, sim.par.resourceRadius * 2, true);
        this.radius = sim.par.resourceRadius;
        this.stay = new int[sim.par.numAgents];
        this.location = new Double2D(sim.par.size / 2, sim.par.size / 2);
    }

    @Override
    public void step(SimState state) {
        ResourceSharing rs = (ResourceSharing) state;
        int i = 0;
        boolean someAlive = false;
        charging = false;
        for (RSAgent a : rs.agents) {
            someAlive = someAlive || a.isAlive();
            if (a.isAlive() && a.inStation && Math.abs(a.getSpeed()) < 0.001) {
                if (stay[i] >= rs.par.rechargeDelay) {
                    charging = true;
                    a.energyLevel = Math.min(rs.par.maxEnergy, a.energyLevel + rs.par.rechargeRate);
                } else {
                    stay[i]++;
                }
            } else {
                stay[i] = 0;
            }
            i++;
        }
        if(!someAlive) {
            state.kill();
        }
    }
    
    public Double2D getLocation() {
        return location;
    }

    @Override
    public double[] getStateVariables() {
        return new double[]{charging ? 1 : 0};
    }
}
