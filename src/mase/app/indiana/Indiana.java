/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.indiana;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.generic.systematic.AgentGroup;
import mase.generic.systematic.EnvironmentalFeature;
import mase.generic.systematic.PolygonFeature;
import mase.generic.systematic.TaskDescription;
import mase.mason.EmboddiedAgent;
import mase.mason.MaseSimState;
import mase.mason.SmartAgent;
import org.apache.commons.math3.util.FastMath;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class Indiana extends MaseSimState implements TaskDescription {

    protected IndianaParams par;
    protected List<IndianaAgent> agents;
    protected Continuous2D field;
    protected GroupController gc;
    protected Gate gate;
    protected PolygonFeature wallsFeature, gateFeature;

    @Override
    public EnvironmentalFeature[] getEnvironmentalFeatures() {
        return new EnvironmentalFeature[]{wallsFeature, gate};
    }

    @Override
    public AgentGroup[] getAgentGroups() {
        AgentGroup ag = new AgentGroup();
        ag.addAll(agents);
        return new AgentGroup[]{ag};
    }

    protected enum AgentPlacement {

        LINE, CIRCLES
    }

    public Indiana(long seed, IndianaParams par, GroupController gc) {
        super(seed);
        this.par = par;
        this.gc = gc;
        // aux variables for wall sensors -- wall segments

        Double2D[] obsStarts = new Double2D[5];
        Double2D[] obsEnds = new Double2D[5];
        obsStarts[0] = new Double2D(0, 0);
        obsEnds[0] = new Double2D(par.size, 0);
        obsStarts[1] = obsEnds[0];
        obsEnds[1] = new Double2D(par.size, par.size);
        obsStarts[2] = obsEnds[1];
        obsEnds[2] = new Double2D(0, par.size);
        obsStarts[3] = obsEnds[2];
        obsEnds[3] = new Double2D(0, par.size / 2 + par.gateSize / 2);
        obsStarts[4] = new Double2D(0, par.size / 2 - par.gateSize / 2);
        obsEnds[4] = obsStarts[0];
        wallsFeature = new PolygonFeature(obsStarts, obsEnds);

        Double2D[] gateStart = new Double2D[]{new Double2D(0, par.size / 2.0 - par.gateSize / 2.0)};
        Double2D[] gateEnd = new Double2D[]{new Double2D(0, par.size / 2.0 + par.gateSize / 2.0)};
        gateFeature = new PolygonFeature(gateStart, gateEnd);
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
        return agents;
    }

    @Override
    public void start() {
        super.start();
        this.field = new Continuous2D(par.discretization, par.size, par.size);
        this.gate = new Gate(this, field, gateFeature);
        gate.setLocation(new Double2D(-0.001, par.size / 2));
        gate.setOrientation(Math.PI);
        gate.setStopper(schedule.scheduleRepeating(gate));
        placeAgents();
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
                    Double2D candidate = new Double2D(FastMath.cos(randAngle) * radius, gate.getLocation().y + FastMath.sin(randAngle) * radius);
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

    @Override
    public boolean continueSimulation() {
        return !gate.closed;
    }

    protected static class Gate extends EmboddiedAgent implements EnvironmentalFeature {

        protected long openTime = -1;
        protected boolean closed = false;
        protected PolygonFeature gate;

        protected Gate(Indiana sim, Continuous2D field, PolygonFeature gate) {
            super(sim, field, sim.par.gateSize / 2, Color.RED);
            this.enableCollisionDetection(false);
            this.gate = gate;
        }

        @Override
        public void step(SimState state) {
            Indiana ind = (Indiana) sim;
            boolean anyInside = false;
            for (IndianaAgent a : ind.agents) {
                if (!a.escaped) {
                    anyInside = true;
                    if (a.passingGate && a.getLocation().x < 0) {
                        if (openTime == -1) {
                            openTime = ind.schedule.getSteps();
                        }
                        a.stop();
                        a.escaped = true;
                        field.remove(a);
                    }
                }
            }
            if (!anyInside || (openTime != -1 && ind.schedule.getSteps() - openTime > ind.par.gateInterval)) {
                closed = true;
            }
        }

        @Override
        public double distanceTo(Double2D position) {
            return gate.distanceTo(position);
        }

        @Override
        public double[] getStateVariables() {
            // is the gate closing?
            return new double[]{openTime == -1 ? 0 : 1};
        }
    }
}
