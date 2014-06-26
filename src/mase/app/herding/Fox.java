/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.herding;

import java.awt.Color;
import mase.mason.world.EmboddiedAgent;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class Fox extends EmboddiedAgent {

    public Fox(Herding sim, Continuous2D field) {
        super(sim, field, sim.par.agentRadius, Color.RED);
        this.enableBoundedArena(true);
        this.enableCollisionDetection(true);
    }

    @Override
    public void step(SimState state) {
        Herding herd = (Herding) state;
        Double2D thisLoc = this.getLocation();
        Shepherd closestShepherd = null;
        for (Shepherd s : herd.shepherds) {
            double d = this.distanceTo(s);
            if (d < herd.par.herdingRange && (closestShepherd == null || d < this.distanceTo(closestShepherd))) {
                closestShepherd = s;
            }
        }
        Sheep closestSheep = null;
        for (Sheep s : herd.activeSheeps) {
            double d = this.distanceTo(s);
            if (closestSheep == null || d < this.distanceTo(closestSheep)) {
                closestSheep = s;
            }
        }
        if(closestSheep == null) {
            return;
        }
      
        Double2D moveVec;
        if (closestShepherd != null && this.distanceTo(closestShepherd) < this.distanceTo(closestSheep)) { // if there is a shepherd nearby, run away from it
            moveVec = thisLoc.subtract(closestShepherd.getLocation());
        } else { // else, go towards the sheep
            Double2D targetPos = closestSheep.getLocation();
            Double2D targetDir = closestSheep.getRealVelocity();
            double targetSpeed = closestSheep.getRealVelocity().length();

            double T = Math.min(50, this.distanceTo(closestSheep) / herd.par.sheepSpeed);
            Double2D future = targetSpeed < 0.001 ? targetPos : targetPos.add(targetDir.normalize().multiply(targetSpeed).multiply(T));
            moveVec = future.subtract(this.getLocation());
        }

        move(moveVec.angle(), herd.par.foxSpeed);
        //move(herd.sheep.getLocation().subtract(thisLoc).angle(), herd.par.foxSpeed);

        // Check if captured the sheep
        if (this.distanceTo(closestSheep) <= herd.par.foxSpeed) {
            closestSheep.status = Sheep.Status.DEAD;
            closestSheep.disappear();
        }
    }
}
