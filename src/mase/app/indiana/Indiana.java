/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.indiana;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.generic.systematic.EntityGroup;
import mase.mason.world.GenericDistanceFunction;
import mase.mason.world.PolygonEntity;
import mase.generic.systematic.TaskDescription;
import mase.generic.systematic.TaskDescriptionProvider;
import mase.mason.MaseSimState;
import mase.mason.world.SmartAgent;
import org.apache.commons.math3.util.FastMath;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class Indiana extends MaseSimState implements TaskDescriptionProvider {

    protected IndianaParams par;
    protected List<IndianaAgent> agents;
    protected Continuous2D field;
    protected GroupController gc;
    protected PolygonEntity walls;
    protected Gate gate;
    protected TaskDescription td;

    @Override
    public TaskDescription getTaskDescription() {
        return td;
    }

    protected enum AgentPlacement {

        LINE, CIRCLES
    }

    public Indiana(long seed, IndianaParams par, GroupController gc) {
        super(seed);
        this.par = par;
        this.gc = gc;

        this.walls = new PolygonEntity(new Double2D[]{
            new Double2D(0, par.size / 2 + par.gateSize / 2),
            new Double2D(0, par.size),
            new Double2D(par.size, par.size),
            new Double2D(par.size, 0),
            new Double2D(0, 0),
            new Double2D(0, par.size / 2 - par.gateSize / 2)});
        this.walls.filled = false;
        this.walls.paint = Color.BLACK;
        this.walls.setStroke(new BasicStroke(2f));
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
        this.gate = new Gate(this, field);
        schedule.scheduleRepeating(gate);
        field.setObjectLocation(gate, new Double2D(0.5,0));
        field.setObjectLocation(walls, new Double2D(0,0));
        placeAgents();

        this.td = new TaskDescription(
                new GenericDistanceFunction(field),
                new EntityGroup(agents, 0, agents.size(), false),
                new EntityGroup(Collections.singletonList(gate), 1, 1, true),
                new EntityGroup(Collections.singletonList(walls), 1, 1, true));
    }

    protected void placeAgents() {
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
                    Double2D candidate = new Double2D(FastMath.cos(randAngle) * radius, gate.getCenter().y + FastMath.sin(randAngle) * radius);
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

    protected static class Gate extends PolygonEntity implements Steppable {

        protected long openTime = -1;
        protected boolean closed = false;
        protected Double2D center;
        
        protected Gate(Indiana sim, Continuous2D field) {
            super(new Double2D[]{new Double2D(-0.1, sim.par.size / 2 - sim.par.gateSize / 2),
                new Double2D(-0.1, sim.par.size / 2 + sim.par.gateSize / 2)});
            this.paint = Color.BLUE;
            this.filled = false;
            this.setStroke(new BasicStroke(3));
            this.center = new Double2D(0, sim.par.size / 2);
            
        }

        @Override
        public void step(SimState state) {
            Indiana ind = (Indiana) state;
            boolean anyInside = false;
            for (IndianaAgent a : ind.agents) {
                if (!a.escaped) {
                    anyInside = true;
                    if (a.passingGate && a.getLocation().x < 0) {
                        if (openTime == -1) {
                            this.paint = Color.RED;
                            openTime = ind.schedule.getSteps();
                        }
                        a.stop();
                        a.escaped = true;
                        ind.field.remove(a);
                        ind.td.groups()[0].remove(a);
                    }
                }
            }
            if (!anyInside || (openTime != -1 && ind.schedule.getSteps() - openTime > ind.par.gateInterval)) {
                closed = true;
                state.kill();
            }
        }

        @Override
        public double[] getStateVariables() {
            return new double[]{openTime == -1 ? 0 : 1};
        }
        
        protected Double2D getCenter() {
            return center;
        }
    }
}
