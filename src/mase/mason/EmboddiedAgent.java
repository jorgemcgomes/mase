/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import java.awt.Color;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.continuous.Continuous2D;
import sim.portrayal.Oriented2D;
import sim.portrayal.simple.OrientedPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Bag;
import sim.util.Double2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public abstract class EmboddiedAgent extends OrientedPortrayal2D implements Steppable, Oriented2D {

    protected Continuous2D field;
    private Double2D pos;
    private Double2D direction;
    private double orientation;
    private double radius;
    private double speed;
    private boolean collisionStatus;
    private Stoppable stopper;
    private boolean detectCollisions;
    private boolean boundedArena;
    protected SimState sim;
    public static final double COLLISION_SPEED_DECAY = 0.5;
    public static final double COLLISION_DIRECTION = Math.PI / 2;

    public EmboddiedAgent(SimState sim, Continuous2D field, double radius, Color c) {
        super(new OvalPortrayal2D(c, radius * 2, true), 0, 1, 
                new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue()));
        this.sim = sim;
        this.radius = radius;
        this.field = field;
        this.collisionStatus = false;
        this.speed = 0;
        this.detectCollisions = false;
        this.boundedArena = false;
        this.setLocation(new Double2D(0, 0));
        this.setOrientation(0);
    }

    public void enableCollisionDetection(boolean enable) {
        this.detectCollisions = enable;
    }

    public void enableBoundedArena(boolean enable) {
        this.boundedArena = enable;
    }

    protected boolean move(Double2D direction, double speed) {
        this.direction = direction.normalize();
        this.orientation = direction.angle();

        if (!attemptMove(direction, speed)) { // cannot move
            // try to escape to both sides with a random order
            double angle = sim.random.nextBoolean() ? COLLISION_DIRECTION : -COLLISION_DIRECTION;
            if (!attemptMove(direction.rotate(angle), speed * COLLISION_SPEED_DECAY)
                    && !attemptMove(direction.rotate(-angle), COLLISION_SPEED_DECAY)) {
                return false;
            }
        }
        return true;
    }

    private boolean attemptMove(Double2D direction, double speed) {
        Double2D newPos = direction.normalize().resize(speed).add(pos);
        if (isValidMove(newPos)) {
            this.collisionStatus = false;
            this.speed = speed;
            setLocation(newPos);
            return true;
        } else {
            this.collisionStatus = true;
            this.speed = 0;
            return false;
        }
    }

    /**
     * Warning: assumes that all agents have the same size
     *
     * @param target
     * @return
     */
    protected boolean isValidMove(Double2D target) {
        if (boundedArena) {
            boolean inside = target.x >= radius && target.x <= field.width - radius && target.y >= radius && target.y <= field.height - radius;
            if (!inside) {
                return false;
            }
        }

        if (detectCollisions) {
            Bag objects = field.getNeighborsExactlyWithinDistance(target, radius * 2);

            for (Object o : objects) {
                if (o != this && o instanceof EmboddiedAgent && ((EmboddiedAgent) o).detectCollisions) {
                    return false;
                }
            }
        }
        return true;
    }

    public void setStopper(Stoppable s) {
        this.stopper = s;
    }

    public void stop() {
        stopper.stop();
    }

    @Override
    public double orientation2D() {
        return orientation;
    }

    public Double2D getDirection() {
        return direction;
    }

    public void setOrientation(double angle) {
        this.orientation = angle;
        this.direction = new Double2D(Math.sin(orientation), Math.cos(orientation));
    }

    public void setDirection(Double2D dir) {
        this.direction = dir.normalize();
        this.orientation = dir.angle();
    }

    public Double2D getLocation() {
        return pos;
    }

    public void setLocation(Double2D loc) {
        this.pos = loc;
        field.setObjectLocation(this, pos);
    }

    public boolean getCollisionStatus() {
        return collisionStatus;
    }

    public double getSpeed() {
        return speed;
    }

    /**
     * @param point
     * @return From -PI to PI.
     */
    public double angleTo(Double2D point) {
        Double2D agentToPoint = point.subtract(pos).normalize();
        double angle = Math.atan2(agentToPoint.y, agentToPoint.x) - Math.atan2(direction.y, direction.x);
        if (Math.abs(angle) > Math.PI) {
            angle -= Math.PI * 2;
        }
        return angle;
    }

    public double distanceTo(EmboddiedAgent other) {
        return Math.max(0, pos.distance(other.getLocation()) - other.radius - this.radius);
    }
}
