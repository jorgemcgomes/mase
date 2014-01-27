/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.aggregation;

import java.util.ArrayList;
import java.util.List;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.generic.systematic.AgentGroup;
import mase.generic.systematic.EnvironmentalFeature;
import mase.generic.systematic.PolygonFeature;
import mase.generic.systematic.TaskDescription;
import mase.mason.MaseSimState;
import mase.mason.SmartAgent;
import sim.field.continuous.Continuous2D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class Aggregation extends MaseSimState implements TaskDescription {

    protected AggregationParams par;
    protected List<AggregationAgent> agents;
    protected Continuous2D field;
    protected GroupController gc;
    private final Double2D[] obsStarts, obsEnds;
    protected PolygonFeature walls;

    public Aggregation(long seed, AggregationParams par, GroupController gc) {
        super(seed);
        this.par = par;
        this.gc = gc;

        // aux variables for wall sensors -- wall segments
        obsStarts = new Double2D[4];
        obsEnds = new Double2D[4];
        obsStarts[0] = new Double2D(0, 0);
        obsEnds[0] = new Double2D(par.size, 0);
        obsStarts[1] = obsEnds[0];
        obsEnds[1] = new Double2D(par.size, par.size);
        obsStarts[2] = obsEnds[1];
        obsEnds[2] = new Double2D(0, par.size);
        obsStarts[3] = obsEnds[2];
        obsEnds[3] = obsStarts[0];
        walls = new PolygonFeature(obsStarts, obsEnds);
    }

    @Override
    public void start() {
        super.start();
        this.field = new Continuous2D(par.discretization, par.size, par.size);
        placeAgents();
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
    public EnvironmentalFeature[] getEnvironmentalFeatures() {
        return new EnvironmentalFeature[]{walls};
    }

    @Override
    public AgentGroup[] getAgentGroups() {
        AgentGroup ag = new AgentGroup();
        ag.addAll(agents);
        return new AgentGroup[]{ag};
    }
}
