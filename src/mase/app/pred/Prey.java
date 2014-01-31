/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.pred;

import java.awt.Color;
import mase.mason.EmboddiedAgent;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class Prey extends EmboddiedAgent {

    public static final double RADIUS = 1.5;
    public static final Color COLOUR = Color.BLUE;
    protected PredatorPrey predSim;

    public Prey(PredatorPrey sim, Continuous2D field) {
        super(sim, field, RADIUS, COLOUR);
        super.setOrientationShowing(false);
        this.predSim = sim;
        if(sim.par.collisions) {
            this.enableCollisionDetection(true);
        }
    }

    @Override
    public void step(SimState state) {
        Bag objects = field.getNeighborsWithinDistance(getLocation(), predSim.par.escapeDistance + Prey.RADIUS + Predator.RADIUS, false);
        Double2D escapeVec = null;
        
        if (predSim.par.escapeStrategy == PredParams.V_NEAREST) { // escape from the nearest one, ignoring all the others
            double closest = Double.POSITIVE_INFINITY;
            for (Object o : objects.objs) {
                if (o instanceof Predator) {
                    Predator pred = (Predator) o;
                     double dist = pred.distanceTo(this);
                    if (dist < closest && dist < predSim.par.escapeDistance) {
                        escapeVec = getLocation().subtract(pred.getLocation());
                        closest = dist;
                    }
                }
            }
        } else if (predSim.par.escapeStrategy == PredParams.V_MEAN_VECTOR) { // escape having in consideration all the predators within the danger area
            MutableDouble2D escape = new MutableDouble2D(0, 0);
            for (Object o : objects.objs) {
                if (o instanceof Predator) {
                    Predator pred = ((Predator) o);
                    double dist = pred.distanceTo(this);
                    if (dist < predSim.par.escapeDistance && dist > 0) {
                        MutableDouble2D vec = new MutableDouble2D(getLocation());
                        vec.subtractIn(pred.getLocation()); // predator to prey vector
                        dist = 1 / dist;
                        vec.normalize();
                        vec.multiplyIn(dist);
                        escape.addIn(vec);
                    }
                }
            }
            escapeVec = new Double2D(escape);
        }
        
        if (escapeVec != null && (escapeVec.x != 0 || escapeVec.y != 0)) {
            move(escapeVec.angle(), predSim.par.preySpeed);
        }
        if (getLocation().x > predSim.par.size || getLocation().y > predSim.par.size || getLocation().x < 0 || getLocation().y < 0) {
            disappear();
        }
    }

    public void disappear() {
        stop();
        field.remove(this);
        predSim.activePreys.remove(this);
        if(predSim.activePreys.isEmpty()) {
            predSim.kill();
        }
    }
}
