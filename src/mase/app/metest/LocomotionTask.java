/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.metest;

import java.awt.Color;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.mason.MasonSimState;
import mase.mason.world.DashMovementEffector;
import mase.mason.world.SmartAgent;
import sim.field.continuous.Continuous2D;
import sim.portrayal.FieldPortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class LocomotionTask extends MasonSimState {

    protected int size = 100;
    protected Continuous2D field;
    protected SmartAgent agent;

    public LocomotionTask(GroupController gc, long seed) {
        super(gc, seed);
    }

    @Override
    public void start() {
        super.start();
        this.field = new Continuous2D(20, size, size);
        AgentController ac = gc.getAgentControllers(1)[0];
        this.agent = new SmartAgent(this, field, 4, Color.BLUE, ac);
        DashMovementEffector ef = new DashMovementEffector(this, field, agent);
        ef.allowBackwardMove(true);
        ef.setSpeeds(1, Math.toRadians(2));
        agent.addEffector(ef);
        agent.setLocation(new Double2D(size / 2, size / 2));
        schedule.scheduleRepeating(agent);
    }

    @Override
    public void setupPortrayal(FieldPortrayal2D port) {
        port.setField(field);
    }
}
