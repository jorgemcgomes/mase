/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.herding;

import java.util.ArrayList;
import java.util.List;
import mase.controllers.AgentController;
import mase.controllers.GroupController;
import mase.mason.MaseSimState;
import mase.mason.SmartAgent;
import net.jafama.FastMath;
import sim.field.continuous.Continuous2D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class Herding extends MaseSimState {

    protected HerdingParams par;
    protected Continuous2D field;
    protected GroupController gc;
    protected List<Shepherd> shepherds;
    protected List<Fox> foxes;
    protected List<Sheep> sheeps;
    protected List<Sheep> activeSheeps;
    public static final Double2D[] FOX_POSITIONS = new Double2D[]{
        new Double2D(0.95, 0.05), new Double2D(0.95, 0.95), new Double2D(0.95, 0.5),
        new Double2D(0.5, 0.95), new Double2D(0.5, 0.05)
    };

    public Herding(long seed, HerdingParams par, GroupController gc) {
        super(seed);
        this.par = par;
        this.gc = gc;
    }

    @Override
    public void start() {
        super.start();
        this.field = new Continuous2D(par.discretization, par.arenaSize, par.arenaSize);

        placeShepherds();
        placeSheeps();
        placeFoxes();
    }

    protected void placeSheeps() {
        sheeps = new ArrayList<Sheep>(par.numSheeps);
        double inc = par.arenaSize / (par.numSheeps + 1);
        double y = 0;
        double x = par.sheepX * par.arenaSize;
        for (int i = 0; i < par.numSheeps; i++) {
            y += inc;
            Sheep sheep = new Sheep(this, field);
            Double2D displacement;
            if (par.placeRadius > 0) {
                double q = 2 * Math.PI * random.nextDouble();
                double r = FastMath.sqrtQuick(random.nextDouble());
                displacement = new Double2D(par.placeRadius * r * FastMath.cosQuick(q), par.placeRadius * r * FastMath.sinQuick(q));
            } else {
                displacement = new Double2D(0, 0);
            }
            sheep.setLocation(new Double2D(x, y).add(displacement));
            sheep.setStopper(schedule.scheduleRepeating(sheep));
            sheeps.add(sheep);
        }
        activeSheeps = new ArrayList<Sheep>(sheeps);
    }

    protected void placeFoxes() {
        foxes = new ArrayList<Fox>(par.numFoxes);
        for (int i = 0; i < par.numFoxes; i++) {
            Fox f = new Fox(this, field);
            f.setLocation(new Double2D(FOX_POSITIONS[i].x * par.arenaSize,
                    FOX_POSITIONS[i].y * par.arenaSize));
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
            Shepherd newShepherd = new Shepherd(this, field, controllers[i].clone());
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
    public List<? extends SmartAgent> getSmartAgents() {
        return shepherds;
    }

}
