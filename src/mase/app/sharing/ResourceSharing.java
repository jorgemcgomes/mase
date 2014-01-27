/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.sharing;

import java.util.ArrayList;
import java.util.List;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.generic.systematic.AgentGroup;
import mase.generic.systematic.EnvironmentalFeature;
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
public class ResourceSharing extends MaseSimState implements TaskDescription {

    protected RSParams par;
    protected List<RSAgent> agents;
    protected Continuous2D field;
    protected GroupController gc;
    protected Resource resource;

    public ResourceSharing(long seed, RSParams par, GroupController gc) {
        super(seed);
        this.par = par;
        this.gc = gc;
    }

    @Override
    public void start() {
        super.start();
        this.field = new Continuous2D(par.discretization, par.size, par.size);
        placeResource();
        placeAgents();
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

    protected void placeResource() {
        resource = new Resource(this, field);
        resource.setLocation(new Double2D(field.height / 2, field.width / 2));
        resource.setStopper(schedule.scheduleRepeating(resource));
    }

    protected void placeAgents() {
        agents = new ArrayList<RSAgent>(par.numAgents);
        AgentController[] acs = gc.getAgentControllers(par.numAgents);
        for (int i = 0; i < par.numAgents; i++) {
            RSAgent ag = new RSAgent(this, field, acs[i].clone());
            Double2D p;
            double margin = 0;
            do {
                p = new Double2D(margin + random.nextDouble() * (par.size - margin * 2),
                        margin + random.nextDouble() * (par.size - margin * 2));
            } while (!field.getNeighborsExactlyWithinDistance(p, par.agentSensorRange + par.agentRadius * 2 + 1).isEmpty());
            ag.setLocation(p);
            ag.setOrientation(random.nextDouble() * Math.PI * 2);
            ag.setStopper(schedule.scheduleRepeating(ag));
            agents.add(ag);
        }
    }

    @Override
    public EnvironmentalFeature[] getEnvironmentalFeatures() {
        return new EnvironmentalFeature[]{resource};
    }

    @Override
    public AgentGroup[] getAgentGroups() {
        AgentGroup a = new AgentGroup();
        a.addAll(agents);
        return new AgentGroup[]{a};
    }
}
