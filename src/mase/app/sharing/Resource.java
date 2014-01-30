/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.sharing;

import java.awt.Color;
import mase.mason.EmboddiedAgent;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author jorge
 */
public class Resource extends EmboddiedAgent {

    private int[] stay;
    private boolean charging;

    public Resource(ResourceSharing sim, Continuous2D field) {
        super(sim, field, sim.par.resourceRadius, Color.YELLOW);
        this.enableCollisionDetection(false);
        this.stay = new int[sim.par.numAgents];
    }

    @Override
    public void step(SimState state) {
        ResourceSharing rs = (ResourceSharing) sim;
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

    @Override
    public double[] getStateVariables() {
        return new double[]{charging ? 1 : 0};
    }
}
