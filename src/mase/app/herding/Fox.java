/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.herding;

import java.awt.Color;
import mase.mason.world.EmboddiedAgent;
import net.jafama.FastMath;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class Fox extends EmboddiedAgent {

    private static final long serialVersionUID = 1L;

    public Fox(Herding sim, Continuous2D field) {
        super(sim, field, sim.par.agentRadius, Color.RED);
        this.enableBoundedArena(true);
        this.enableAgentCollisions(true);
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
        if (closestSheep == null) {
            return;
        }

        Double2D moveVec;
        if (closestShepherd != null && this.distanceTo(closestShepherd) < this.distanceTo(closestSheep)) { // if there is a shepherd nearby, run away from it
            if (herd.par.smartFox) {
                // go perpendicular to the predator, in the directo of the sheep
                Double2D sheepPos = futureSheepPosition(herd, closestSheep);
                Double2D foxToShepherd = (closestShepherd.getLocation().subtract(thisLoc)).normalize();
                double sheepAngle = angleTo(thisLoc, foxToShepherd, sheepPos);
                if (sheepAngle > Math.PI / 2 || sheepAngle < -Math.PI / 2) {
                    // Ignore shepherd
                    moveVec = sheepPos.subtract(thisLoc);
                } else if (sheepAngle > 0) {
                    moveVec = foxToShepherd.rotate(/*2 * Math.PI / 3*/ Math.PI / 2 /*Math.PI / 3*/);
                } else {
                    moveVec = foxToShepherd.rotate(/*-2 * Math.PI / 3*/ -Math.PI / 2 /*-Math.PI / 3*/);
                }
            } else {
                // run away from the shepherd
                moveVec = thisLoc.subtract(closestShepherd.getLocation());
            }
        } else { // else, go towards the sheep
            Double2D future = futureSheepPosition(herd, closestSheep);
            moveVec = future.subtract(thisLoc);
        }

        move(moveVec.angle(), herd.par.foxSpeed);

        // Check if captured the sheep
        if (this.distanceTo(closestSheep) <= herd.par.foxSpeed) {
            closestSheep.disappear();
        }
    }

    private double angleTo(Double2D sourcePos, Double2D sourceDir, Double2D target) {
        Double2D sourceToTarget = target.subtract(sourcePos).normalize();
        return FastMath.atan2(sourceDir.x * sourceToTarget.y - sourceDir.y * sourceToTarget.x, sourceDir.x * sourceToTarget.x + sourceDir.y * sourceToTarget.y);
    }

    private Double2D futureSheepPosition(Herding sim, Sheep s) {
        Double2D targetPos = s.getLocation();
        Double2D targetDir = s.getRealVelocity();
        double targetSpeed = s.getRealVelocity().length();

        double T = Math.min(50, this.distanceTo(s) / sim.par.sheepSpeed);
        // targetDir.normalize().multiply(targetSpeed) == targetDir
        Double2D future = targetSpeed < 0.001 ? targetPos : targetPos.add(targetDir.normalize().multiply(targetSpeed).multiply(T));
        return future;
    }
}
