/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.generic.SmartAgentProvider;
import mase.generic.systematic.EntityGroup;
import mase.generic.systematic.TaskDescription;
import mase.generic.systematic.TaskDescriptionProvider;
import mase.mason.MasonSimState;
import mase.mason.world.GenericDistanceFunction;
import mase.mason.world.StaticPolygon;
import mase.mason.world.SmartAgent;
import sim.field.continuous.Continuous2D;
import sim.portrayal.FieldPortrayal2D;
import sim.util.Bag;
import sim.util.Double2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class PredatorPrey extends MasonSimState implements TaskDescriptionProvider, SmartAgentProvider {

    private static final long serialVersionUID = 1L;

    protected PredParams original;
    protected PredParams par;
    protected Continuous2D field;
    protected List<Predator> predators;
    protected List<Prey> preys;
    protected List<Prey> activePreys;
    protected int captureCount;
    protected StaticPolygon boundaries;
    protected TaskDescription td;

    public PredatorPrey(long seed, PredParams params, GroupController gc) {
        super(gc, seed);
        this.original = params;
    }

    @Override
    public void start() {
        super.start();
        try {
            this.par = original.clone();
            par.escapeDistance += (random.nextDouble() * 2 - 1) * par.escapeDistanceRandom;
            par.predatorLinearSpeed += par.predatorLinearSpeed * (random.nextDouble() * 2 - 1) * par.speedsOffset;
            par.predatorTurnSpeed += par.predatorTurnSpeed * (random.nextDouble() * 2 - 1) * par.speedsOffset;
        } catch(Exception e) {
            e.printStackTrace();
        }
        this.boundaries = new StaticPolygon(new Double2D(0, 0), new Double2D(par.size, 0), new Double2D(par.size, par.size), new Double2D(0, par.size), new Double2D(0, 0));
        boundaries.paint = Color.WHITE;
        
        this.field = new Continuous2D(par.discretization, par.size, par.size);
        field.setObjectLocation(boundaries, new Double2D(0, 0));

        this.predators = null;
        this.preys = null;
        this.activePreys = null;
        this.captureCount = 0;
        placePreys();
        placePredators();

        this.td = new TaskDescription(new GenericDistanceFunction(field),
                new EntityGroup(predators, par.nPredators, par.nPredators, false),
                new EntityGroup(activePreys, 0, par.nPreys, false),
                new EntityGroup(Collections.singletonList(boundaries), 1, 1, true));
    }

    protected void placePreys() {
        // randomly place preys, in the opposite half of the field
        preys = new ArrayList<>();
        while (preys.size() < par.nPreys) {
            double x = 0;
            double y = 0;
            if (par.preyPlacement == PredParams.V_CENTER) {
                x = par.size / 2;
                y = par.size / 2;
            } else if (par.preyPlacement == PredParams.V_RANDOM) {
                x = par.preyMargin + random.nextDouble() * (par.size - par.preyMargin * 2);
                y = par.preyMargin + random.nextDouble() * (par.size - par.preyMargin * 2);
                Bag close = field.getNeighborsExactlyWithinDistance(new Double2D(x, y), par.preySeparation + Prey.RADIUS * 2);
                if (!close.isEmpty()) {
                    continue;
                }
            }
            Prey newPrey = new Prey(this, field);
            newPrey.setLocation(new Double2D(x, y));
            newPrey.setStopper(schedule.scheduleRepeating(newPrey));
            preys.add(newPrey);
        }
        activePreys = new LinkedList<>(preys);
    }

    protected void placePredators() {
        // align predators in a row in the bottom
        double w = par.predatorSeparation * (par.nPredators - 1);
        double y = 0;
        double startX = (field.width - w) / 2;
        predators = new ArrayList<>(par.nPredators);
        AgentController[] controllers = gc.getAgentControllers(par.nPredators);
        for (int i = 0; i < par.nPredators; i++) {
            double x = startX + i * par.predatorSeparation;
            AgentController controller = controllers[i].clone();
            controller.reset();
            Predator newPred = new Predator(this, field, controller);
            newPred.setLocation(new Double2D(x, y));
            newPred.setOrientation(Math.PI / 2);
            newPred.setStopper(schedule.scheduleRepeating(newPred));
            predators.add(newPred);
        }
        for(Predator pred : predators) {
            pred.setupSensors();
        }
    }

    public int getCaptureCount() {
        return captureCount;
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