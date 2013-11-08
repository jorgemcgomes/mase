/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.indiana;

import java.util.ArrayList;
import java.util.List;
import mase.AgentController;
import mase.GroupController;
import mase.mason.MaseSimState;
import mase.mason.SmartAgent;
import org.apache.commons.math3.util.FastMath;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class Indiana extends MaseSimState {

    protected IndianaParams par;
    protected List<IndianaAgent> agents;
    protected Continuous2D field;
    protected GroupController gc;
    protected Double2D target;
    long openTime = -1;

    protected enum AgentPlacement {

        LINE, CIRCLES
    }

    public Indiana(long seed, IndianaParams par, GroupController gc) {
        super(seed);
        this.par = par;
        this.gc = gc;
    }

    public Double2D getTarget() {
        return target;
    }

    @Override
    public Object getField() {
        return field;
    }

    @Override
    public List<? extends SmartAgent> getSmartAgents() {
        return agents;
    }

    @Override
    public void start() {
        super.start();
        this.field = new Continuous2D(par.discretization, par.size, par.size);
        this.target = new Double2D(-IndianaAgent.RADIUS * 2, par.size / 2);
        placeAgents();
        IndianaMonitor m = new IndianaMonitor();
        m.stop = schedule.scheduleRepeating(m);
    }

    protected void placeAgents() {
        // TODO: place in single file
        agents = new ArrayList<IndianaAgent>(par.numAgents);
        AgentController[] acs = gc.getAgentControllers(par.numAgents);
        double agentSeparation = (par.size - IndianaAgent.RADIUS) / par.numAgents;
        if (par.agentPlacement == AgentPlacement.LINE) {
            for (int i = 0; i < par.numAgents; i++) {
                IndianaAgent ag = new IndianaAgent(this, field, acs[i].clone());
                Double2D p = new Double2D((i + 1) * agentSeparation, par.size / 2);
                ag.setLocation(p);
                ag.setOrientation(Math.PI);
                ag.setStopper(schedule.scheduleRepeating(ag));
                agents.add(ag);
            }
        } else if (par.agentPlacement == AgentPlacement.CIRCLES) {
            for (int i = 0; i < par.numAgents; i++) {
                IndianaAgent ag = new IndianaAgent(this, field, acs[i].clone());
                double radius = (i + 1) * agentSeparation;
                Double2D p = null;
                while (p == null) {
                    double randAngle = this.random.nextDouble() * Math.PI * 2;
                    Double2D candidate = new Double2D(FastMath.cos(randAngle) * radius, target.y + FastMath.sin(randAngle) * radius);
                    if (ag.checkEnvironmentValidty(candidate)) {
                        p = candidate;
                    }
                }
                ag.setLocation(p);
                ag.setOrientation(random.nextDouble() * Math.PI * 2 - Math.PI);
                ag.setStopper(schedule.scheduleRepeating(ag));
                agents.add(ag);
            }
        }
    }

    private class IndianaMonitor implements Steppable {

        Stoppable stop;

        @Override
        public void step(SimState state) {
            boolean anyInside = false;
            for (IndianaAgent a : agents) {
                if (!a.escaped) {
                    anyInside = true;
                    if (a.passingGate && a.getLocation().x < 0) {
                        if (openTime == -1) {
                            openTime = Indiana.this.schedule.getSteps();
                        }
                        a.stop();
                        a.escaped = true;
                        field.remove(a);
                    }
                }
            }
            if (!anyInside) {
                stop.stop();
                return;
            }
            if (openTime != -1 && schedule.getSteps() - openTime > par.gateInterval) {
                for (IndianaAgent a : agents) {
                    if (!a.escaped) {
                        a.stop();
                    }
                }
                stop.stop();
            }
        }
    }
}
