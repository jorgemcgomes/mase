/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import mase.app.pred.PredParams.SensorMode;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.generic.systematic.EntityGroup;
import mase.generic.systematic.TaskDescription;
import mase.generic.systematic.TaskDescriptionProvider;
import mase.mason.MaseSimState;
import mase.mason.MasonDistanceFunction;
import mase.mason.PolygonEntity;
import mase.mason.SmartAgent;
import sim.field.continuous.Continuous2D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.util.Bag;
import sim.util.Double2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class PredatorPrey extends MaseSimState implements TaskDescriptionProvider {

    protected PredParams par;
    protected Continuous2D field;
    protected GroupController gc;
    protected List<Predator> predators;
    protected List<Prey> preys;
    protected List<Prey> activePreys;
    protected int captureCount;
    protected TaskDescription td;
    
    public PredatorPrey(long seed, PredParams params, GroupController gc) {
        super(seed);
        this.gc = gc;
        this.par = params;
    }

    @Override
    public void start() {
        super.start();
        this.field = new Continuous2D(par.discretization, par.size, par.size);
        this.predators = null;
        this.preys = null;
        this.activePreys = null;
        this.captureCount = 0;
        placePredators();
        placePreys();
        
        PolygonEntity boundaries = new PolygonEntity(new Double2D(0,0), new Double2D(par.size,0), new Double2D(par.size, par.size), new Double2D(0, par.size), new Double2D(0,0));
        field.setObjectLocation(boundaries, new Double2D(0,0));
        this.td = new TaskDescription(new MasonDistanceFunction(field),
                new EntityGroup(predators, par.nPredators, par.nPredators, false),
                new EntityGroup(preys, 0, par.nPreys, false),
                new EntityGroup(Collections.singletonList(boundaries), 1, 1, true));
    }

    protected void placePreys() {
        // randomly place preys, in the opposite half of the field
        preys = new ArrayList<Prey>();
        while (preys.size() < par.nPreys) {
            double x = 0;
            double y = 0;
            if (par.preyPlacement.equalsIgnoreCase(PredParams.V_CENTER)) {
                x = par.size / 2;
                y = par.size / 2;
            } else if (par.preyPlacement.equalsIgnoreCase(PredParams.V_RANDOM)) {
                x = par.preyMargin + random.nextDouble() * (par.size - par.preyMargin * 2);
                y = par.preyMargin + random.nextDouble() * (par.size - par.preyMargin * 2);
                Bag close = field.getNeighborsExactlyWithinDistance(new Double2D(x, y), par.preySeparation + Prey.RADIUS * 2);
                if (!close.isEmpty()) {
                    continue;
                }
            }
            Prey newPrey = newPrey();
            newPrey.setLocation(new Double2D(x, y));
            newPrey.setStopper(schedule.scheduleRepeating(newPrey));
            preys.add(newPrey);
        }
        activePreys = new LinkedList<Prey>(preys);
    }

    protected void placePredators() {
        // align predators in a row in the bottom
        double w = par.predatorSeparation * (par.nPredators - 1);
        double y = 0;
        double startX = (field.width - w) / 2;
        predators = new ArrayList<Predator>(par.nPredators);
        AgentController[] controllers = gc.getAgentControllers(par.nPredators);
        for (int i = 0; i < par.nPredators; i++) {
            double x = startX + i * par.predatorSeparation;
            Predator newPred = newPredator(controllers[i].clone());
            newPred.setLocation(new Double2D(x, y));
            newPred.setOrientation(Math.PI / 2);
            newPred.setStopper(schedule.scheduleRepeating(newPred));
            predators.add(newPred);
        }
    }

    protected Predator newPredator(AgentController ac) {
        if (par.sensorMode == SensorMode.arcs) {
            return new MultiPredator(this, field, ac);
        } else if(par.sensorMode == SensorMode.closest) {
            return new Predator(this, field, ac);
        } else if(par.sensorMode == SensorMode.otherpreds) {
            return new PredatorHomo(this, field, ac);
        }
        return null;
    }

    protected Prey newPrey() {
        return new Prey(this, field);
    }

    public int getCaptureCount() {
        return captureCount;
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
        return predators;
    }

    @Override
    public TaskDescription getTaskDescription() {
        return td;
    }
}
