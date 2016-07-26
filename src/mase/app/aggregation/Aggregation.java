/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.aggregation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.generic.SmartAgentProvider;
import mase.generic.systematic.EntityGroup;
import mase.generic.systematic.TaskDescription;
import mase.generic.systematic.TaskDescriptionProvider;
import mase.mason.MasonSimState;
import mase.mason.world.StaticPolygon;
import mase.mason.world.SmartAgent;
import sim.field.continuous.Continuous2D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class Aggregation extends MasonSimState implements TaskDescriptionProvider, SmartAgentProvider {

    protected AggregationParams par;
    protected List<AggregationAgent> agents;
    protected Continuous2D field;
    protected StaticPolygon walls;
    protected TaskDescription td;

    public Aggregation(long seed, AggregationParams par, GroupController gc) {
        super(gc, seed);
        this.par = par;

        walls = new StaticPolygon(new Double2D[]{
            new Double2D(0,0),
            new Double2D(par.size, 0),
            new Double2D(par.size, par.size),
            new Double2D(0, par.size),
            new Double2D(0,0)
        });
        walls.paint = Color.BLACK;
        walls.setStroke(new BasicStroke(3f));
        walls.filled = false;
    }

    @Override
    public void start() {
        super.start();
        this.field = new Continuous2D(par.discretization, par.size, par.size);
        placeAgents();
        field.setObjectLocation(walls, new Double2D(0,0));
        
        this.td = new TaskDescription(
                new EntityGroup(agents, agents.size(), agents.size(), false),
                new EntityGroup(Collections.singletonList(walls), 1, 1, true)
        );
    }

    @Override
    public List<? extends SmartAgent> getSmartAgents() {
        return agents;
    }

    protected void placeAgents() {
        agents = new ArrayList<AggregationAgent>(par.numAgents);
        AgentController[] acs = gc.getAgentControllers(par.numAgents);
        for (int i = 0; i < par.numAgents; i++) {
            AggregationAgent ag = new AggregationAgent(this, field, acs[i].clone());
            Double2D p;
            double margin = par.wallRadius + AggregationAgent.RADIUS + 1;
            do {
                p = new Double2D(margin + random.nextDouble() * (par.size - margin * 2),
                        margin + random.nextDouble() * (par.size - margin * 2));
            } while (!field.getNeighborsExactlyWithinDistance(p, par.agentRadius + AggregationAgent.RADIUS * 2 + 1).isEmpty());
            ag.setLocation(p);
            ag.setOrientation(random.nextDouble() * Math.PI * 2);
            schedule.scheduleRepeating(ag);
            agents.add(ag);
        }
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
    public TaskDescription getTaskDescription() {
        return td;
    }
}
