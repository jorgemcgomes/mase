/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.herding;

import java.awt.Color;
import java.util.LinkedList;
import mase.mason.world.EmboddiedAgent;
import net.jafama.FastMath;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class Sheep extends EmboddiedAgent {

    public static final int HISTORY_SIZE = 10;
    private static final long serialVersionUID = 1L;
    protected int corraledTime = -1;
    protected LinkedList<Double2D> positionHistory;
    protected Double2D realVelocity = new Double2D(0,0);

    public Sheep(Herding sim, Continuous2D field) {
        super(sim, field, sim.par.agentRadius, Color.GREEN);
        this.setCollidableTypes(EmboddiedAgent.class);
        this.virtuallyBoundedArena(true);
        this.positionHistory = new LinkedList<>();
    }

    @Override
    public void step(SimState state) {
        Herding herd = (Herding) state;

        Double2D thisLoc = this.getLocation();
        Shepherd closest = null;
        for (Shepherd s : herd.shepherds) {
            double d = this.distanceTo(s);

            if (d < herd.par.herdingRange && (closest == null || d < this.distanceTo(closest))) {
                closest = s;
            }
        }

        if (closest != null) { // if there is a shepherd nearby, run away from it
            move(thisLoc.subtract(closest.getLocation()).angle(), herd.par.sheepSpeed);
        } else if (herd.par.activeSheep) { // else, go straight towards the escape
            move(new Double2D(-1, 0).angle(), herd.par.sheepSpeed);
        }
        
        // Update real velocity
        positionHistory.addLast(getLocation());
        if(positionHistory.size() > HISTORY_SIZE) {
            positionHistory.removeFirst();
        }
        if(positionHistory.size() == 1) {
            realVelocity = new Double2D(FastMath.cosQuick(orientation2D()), FastMath.sinQuick(orientation2D())).resize(herd.par.sheepSpeed);
        } else {
            realVelocity = positionHistory.getLast().subtract(positionHistory.getFirst()).multiply(1.0 / positionHistory.size());
        }

        // Check if the sheep escaped
        /*if (this.getLocation().x <= herd.par.agentRadius + herd.par.sheepSpeed) {
            status = Status.ESCAPED;
            disappear();
        }*/
        // check if sheep entered the curral
        if (getLocation().x >= herd.par.arenaSize - herd.par.agentRadius - herd.par.sheepSpeed
                && getLocation().y >= herd.par.arenaSize / 2 - herd.par.gateSize / 2
                && getLocation().y <= herd.par.arenaSize / 2 + herd.par.gateSize / 2) {
            corraledTime = (int) state.schedule.getSteps();
            disappear();
        }
    }
    
    protected void disappear() {
        this.stop();
        Herding herd = (Herding) sim;
        herd.field.remove(this);
        herd.activeSheeps.remove(this);
        if(herd.activeSheeps.isEmpty()) {
            herd.kill();
        }
    }
    
    public Double2D getRealVelocity() {
        return realVelocity;
    }
}
