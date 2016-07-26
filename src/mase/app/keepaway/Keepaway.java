/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.keepaway;

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
import mase.mason.world.EmboddiedAgent;
import mase.mason.world.SmartAgent;
import net.jafama.FastMath;
import sim.field.continuous.Continuous2D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class Keepaway extends MasonSimState implements TaskDescriptionProvider, SmartAgentProvider {

    protected KeepawayParams par;
    protected Continuous2D field;
    protected List<Keeper> keepers;
    protected List<EmboddiedAgent> takers;
    protected Ball ball;
    protected boolean caught;
    protected boolean outOfLimits;
    protected Double2D center;
    protected TaskDescription td;
    public static final double BALL_OFFSET = 5;
    
    public Keepaway(long seed, KeepawayParams par, GroupController gc) {
        super(gc, seed);
        this.par = par;
    }

    @Override
    public void start() {
        super.start();
        this.field = new Continuous2D(par.discretization, par.size, par.size);
        this.center = new Double2D(par.size / 2, par.size / 2);
        this.caught = false;
        this.outOfLimits = false;
        placeKeepers();
        placeBall();
        placeTakers();
        
        for(Keeper k : keepers) {
            k.setupSensors();
        }
        
        this.td = new TaskDescription(
                new EntityGroup(keepers, keepers.size(), keepers.size(), false),
                new EntityGroup(takers, takers.size(), takers.size(), false),
                new EntityGroup(Collections.singletonList(ball), 1, 1, false)
        );
    }

    @Override
    public FieldPortrayal2D createFieldPortrayal() {
        return new ContinuousPortrayal2D();
    }

    @Override
    public void setupPortrayal(FieldPortrayal2D port) {
        port.setField(field);
    }

    protected void placeKeepers() {
        keepers = new ArrayList<Keeper>(par.numKeepers);
        AgentController[] acs = gc.getAgentControllers(par.numKeepers);
        for (int i = 0; i < par.numKeepers; i++) {
            Keeper k = new Keeper(this, field, acs[i].clone(), par.passSpeed[i], par.moveSpeed[i], par.color[i]);
            if (par.keeperStartPos != null && par.keeperStartAngle != null) {
                k.setLocation(par.keeperStartPos[i]);
                k.setOrientation(par.keeperStartAngle[i]);
            } else {
                double slice = Math.PI * 2 / par.numKeepers;
                double rot = i * slice + (random.nextDouble() - 0.5) * par.keepersPlacement;
                Double2D up = new Double2D(0, par.ringSize / 2);
                Double2D v = up.rotate(rot);
                k.setLocation(v.add(center));
                k.setOrientation(v.negate().angle());
            }
            k.setStopper(schedule.scheduleRepeating(k));
            k.enableAgentCollisions(par.collisions);
            keepers.add(k);
        }
    }

    protected void placeTakers() {
        takers = new ArrayList<EmboddiedAgent>(1);
        Taker t = new Taker(this, field);
        if (par.takersPlacement == 0) {
            t.setLocation(center);
        } else {
            double q = random.nextDouble() * Math.PI * 2;
            double r = Math.sqrt(random.nextDouble());
            double x = (par.takersPlacement * r) * Math.cos(q) + center.getX();
            double y = (par.takersPlacement * r) * Math.sin(q) + center.getY();
            t.setLocation(new Double2D(x, y));
        }
        Double2D ballDir = ball.getLocation().subtract(t.getLocation());
        t.setOrientation(ballDir.angle());
        t.enableAgentCollisions(par.collisions);
        t.setStopper(schedule.scheduleRepeating(t));
        takers.add(t);
    }

    protected void placeBall() {
        ball = new Ball(this, field);
        int ag = par.ballPlacement;
        if (ag == -1) {
            ag = random.nextInt(keepers.size());
        }
        Keeper k = keepers.get(ag);
        
        double angle = k.orientation2D();
        double op = FastMath.sin(angle) * BALL_OFFSET;
        double ad = FastMath.cos(angle) * BALL_OFFSET;
        ball.setLocation(k.getLocation().add(new Double2D(ad, op)));
        ball.setStopper(schedule.scheduleRepeating(ball));
    }

    @Override
    public List<? extends SmartAgent> getSmartAgents() {
        return keepers;
    }

    @Override
    public TaskDescription getTaskDescription() {
        return td;
    }
}
