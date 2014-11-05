/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.herding;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.generic.systematic.EntityGroup;
import mase.generic.systematic.TaskDescription;
import mase.generic.systematic.TaskDescriptionProvider;
import mase.mason.GUICompatibleSimState;
import mase.mason.world.GenericDistanceFunction;
import mase.mason.world.StaticPolygon;
import mase.mason.world.StaticPolygon.Segment;
import sim.field.continuous.Continuous2D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class Herding extends GUICompatibleSimState implements TaskDescriptionProvider {

    protected TaskDescription td;
    protected HerdingParams par;
    protected Continuous2D field;
    protected GroupController gc;
    protected List<Shepherd> shepherds;
    protected List<Fox> foxes;
    protected List<Sheep> sheeps;
    protected List<Sheep> activeSheeps;
    protected final StaticPolygon fence, openSide, curral;
    public static final Double2D[] FOX_POSITIONS = new Double2D[]{
        new Double2D(0.95, 0.05), new Double2D(0.95, 0.95), new Double2D(0.95, 0.5),
        new Double2D(0.5, 0.95), new Double2D(0.5, 0.05)
    };

    public Herding(long seed, HerdingParams par, GroupController gc) {
        super(seed);
        this.par = par;
        this.gc = gc;

        // Static environment
        fence = new StaticPolygon(new Segment(0, 0, par.arenaSize, 0),
                new Segment(par.arenaSize, 0, par.arenaSize, par.arenaSize / 2 - par.gateSize / 2),
                new Segment(par.arenaSize, par.arenaSize / 2 + par.gateSize / 2, par.arenaSize, par.arenaSize),
                new Segment(par.arenaSize, par.arenaSize, 0, par.arenaSize));
        fence.paint = Color.BLACK;

        openSide = new StaticPolygon(new Segment(0, 0, 0, par.arenaSize));
        openSide.paint = Color.RED;

        curral = new StaticPolygon(new Segment(par.arenaSize, par.arenaSize / 2 - par.gateSize / 2,
                par.arenaSize, par.arenaSize / 2 + par.gateSize / 2));
        curral.paint = Color.BLUE;
    }

    @Override
    public void start() {
        super.start();
        this.field = new Continuous2D(par.discretization, par.arenaSize, par.arenaSize);

        placeSheeps();
        placeFoxes();
        placeShepherds();

        field.setObjectLocation(fence, new Double2D(0, 0));
        field.setObjectLocation(openSide, new Double2D(0, 0));
        field.setObjectLocation(curral, new Double2D(0, 0));

        this.td = new TaskDescription(new GenericDistanceFunction(field),
                new EntityGroup(shepherds, shepherds.size(), shepherds.size(), false),
                new EntityGroup(activeSheeps, 0, sheeps.size(), false),
                new EntityGroup(foxes, foxes.size(), foxes.size(), false),
                new EntityGroup(Collections.singletonList(fence), 1, 1, true),
                new EntityGroup(Collections.singletonList(openSide), 1, 1, true),
                new EntityGroup(Collections.singletonList(curral), 1, 1, true)
        );
    }

    protected void placeSheeps() {
        sheeps = new ArrayList<Sheep>(par.numSheeps);
        double range = par.arenaSize / par.numSheeps;
        for (int i = 0; i < par.numSheeps; i++) {
            Sheep sheep = new Sheep(this, field);
            Double2D newLoc = null;
            if (par.randomSheepPosition) {
                newLoc = new Double2D(par.sheepX * par.arenaSize,
                        i * range + sheep.getRadius() + random.nextDouble() * (range - sheep.getRadius() * 2));
            } else {
                newLoc = new Double2D(par.sheepX * par.arenaSize, i * range + range / 2);
            }
            sheep.setLocation(newLoc);            
            sheep.setStopper(schedule.scheduleRepeating(sheep));
            sheeps.add(sheep);
        }
        activeSheeps = new ArrayList<Sheep>(sheeps);
    }

    protected void placeFoxes() {
        foxes = new ArrayList<Fox>(par.numFoxes);

        double range = par.arenaSize / par.numFoxes;

        for (int i = 0; i < par.numFoxes; i++) {
            Fox f = new Fox(this, field);
            Double2D newLoc = null;
            if (par.randomFoxPosition) {
                newLoc = new Double2D(par.foxX * par.arenaSize,
                        i * range + f.getRadius() + random.nextDouble() * (range - f.getRadius() * 2));
            } else {
                newLoc = new Double2D(par.foxX * par.arenaSize, i * range + range / 2);
            }
            f.setLocation(newLoc);
            foxes.add(f);
            schedule.scheduleRepeating(f);

        }
    }

    protected void placeShepherds() {
        shepherds = new ArrayList<Shepherd>(par.numShepherds);
        //double x = par.agentRadius + 1;
        double x = par.shepherdX * par.arenaSize;
        double startY = (field.width - par.shepherdSeparation * (par.numShepherds - 1)) / 2;
        AgentController[] controllers = gc.getAgentControllers(par.numShepherds);
        for (int i = 0; i < par.numShepherds; i++) {
            double y = startY + i * par.shepherdSeparation;
            AgentController contr = controllers[i].clone();
            contr.reset();
            Shepherd newShepherd = new Shepherd(this, field, contr);
            newShepherd.setLocation(new Double2D(x, y));
            newShepherd.setOrientation(0);
            shepherds.add(newShepherd);
            schedule.scheduleRepeating(newShepherd);
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
