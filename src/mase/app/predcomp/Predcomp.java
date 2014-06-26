/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.predcomp;

import java.util.ArrayList;
import java.util.List;
import mase.app.predcomp.PredcompParams.ORIENTATION;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.controllers.HeterogeneousGroupController;
import mase.mason.MaseSimState;
import mase.mason.world.SmartAgent;
import sim.field.continuous.Continuous2D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class Predcomp extends MaseSimState {

    protected PredcompParams par;
    protected GroupController gc;
    protected Prey prey;
    protected Predator predator;
    protected Continuous2D field;

    public Predcomp(long seed, PredcompParams par, GroupController gc) {
        super(seed);
        this.par = par;
        this.gc = gc;
    }

    @Override
    public void start() {
        super.start();
        this.field = new Continuous2D(par.discretization, par.size, par.size);

        HeterogeneousGroupController hgc = (HeterogeneousGroupController) gc;
        AgentController[] acs = hgc.getAgentControllers(2);
        predator = new Predator(this, field, acs[0]);
        prey = new Prey(this, field, acs[1]);

        predator.setLocation(new Double2D(par.size / 3, par.size / 2));
        if (par.orientation == ORIENTATION.random) {
            predator.setOrientation(random.nextDouble() * Math.PI * 2 - Math.PI);
        } else if(par.orientation == ORIENTATION.opposing){
            predator.setOrientation(Math.PI);
        }
        schedule.scheduleRepeating(predator);

        prey.setLocation(new Double2D(par.size * 2 / 3, par.size / 2));
        if(par.orientation == ORIENTATION.random) {
            prey.setOrientation(random.nextDouble() * Math.PI * 2 - Math.PI);
        } else if(par.orientation == ORIENTATION.opposing) {
            prey.setOrientation(0);
        }
        
        schedule.scheduleRepeating(prey);
    }

    @Override
    public FieldPortrayal2D createFieldPortrayal() {
        return new ContinuousPortrayal2D();
    }

    @Override
    public void setupPortrayal(FieldPortrayal2D port) {
        port.setField(field);
    }

    @Override
    public List<? extends SmartAgent> getSmartAgents() {
        ArrayList<SmartAgent> l = new ArrayList<SmartAgent>(2);
        l.add(prey);
        l.add(predator);
        return l;
    }
}
