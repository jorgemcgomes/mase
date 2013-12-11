/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import java.awt.Color;
import net.jafama.FastMath;
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
    private double orientation;
    private final double radius;
    private double speed;
    private boolean collisionStatus;
    private Stoppable stopper;
    private boolean detectCollisions;
    private boolean boundedArena;
    protected SimState sim;
    public static final double COLLISION_SPEED_DECAY = 0.5;
    public static final double COLLISION_DIRECTION = Math.PI / 2;
    private boolean isAlive;

    public EmboddiedAgent(SimState sim, Continuous2D field, double radius, Color c) {
        super(new OvalPortrayal2D(c, radius * 2, true), 0, radius,
                new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue()));
        this.sim = sim;
        this.radius = radius;
        this.field = field;
        this.collisionStatus = false;
        this.speed = 0;
        this.detectCollisions = false;
        this.boundedArena = false;
        this.isAlive = true;
        this.setLocation(new Double2D(0, 0));
        this.setOrientation(0);
    }

    public double getRadius() {
        return radius;
    }
    
    public boolean isAlive() {
        return isAlive;
    }
    

    public void enableCollisionDetection(boolean enable) {
        this.detectCollisions = enable;
    }

    public void enableBoundedArena(boolean enable) {
        this.boundedArena = enable;
    }

    protected boolean move(double orientation, double speed) {
        this.orientation = normalizeAngle(orientation);

        if (!attemptMove(orientation, speed)) { // cannot move
            // try to escape to both sides with a random order
            double angle = sim.random.nextBoolean() ? COLLISION_DIRECTION : -COLLISION_DIRECTION;
            if (!attemptMove(normalizeAngle(orientation + angle), speed * COLLISION_SPEED_DECAY)
                    && !attemptMove(normalizeAngle(orientation - angle), COLLISION_SPEED_DECAY)) {
                return false;
            }
        }
        return true;
    }

    // from anything to [-PI,PI]
    protected double normalizeAngle(double ang) {
        if(ang > Math.PI * 2) {
            ang = ang % (Math.PI * 2);
        } else if(ang < -Math.PI * 2) {
            ang = -(Math.abs(ang) % (Math.PI * 2));
        }
        if (ang > Math.PI) {
            ang = ang - Math.PI * 2;
        } else if (ang < -Math.PI) {
            ang = ang + Math.PI * 2;
        }
        return ang;
    }

    private boolean attemptMove(double ori, double speed) {
        Double2D displacement = new Double2D(speed * FastMath.cos(ori), speed * FastMath.sin(ori));
        Double2D newPos = getLocation().add(displacement);
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
        return (!boundedArena || checkEnvironmentValidty(target)) &&
                (!detectCollisions || checkAgentCollisions(target));
    }

    protected boolean checkEnvironmentValidty(Double2D target) {
        return target.x >= radius && target.x <= field.width - radius && target.y >= radius && target.y <= field.height - radius;
    }

    protected boolean checkAgentCollisions(Double2D target) {
        Bag objects = field.getNeighborsExactlyWithinDistance(target, radius * 2);

        for (Object o : objects) {
            if (o != this && o instanceof EmboddiedAgent && ((EmboddiedAgent) o).detectCollisions) {
                return false;
            }
        }
        return true;
    }

    public void setStopper(Stoppable s) {
        this.stopper = s;
    }

    public void stop() {
        stopper.stop();
        this.isAlive = false;
    }

    @Override
    public double orientation2D() {
        return orientation;
    }

    public void setOrientation(double angle) {
        this.orientation = angle;
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
        Double2D agentDir = new Double2D(FastMath.cos(orientation), FastMath.sin(orientation));
        return FastMath.atan2(agentDir.x * agentToPoint.y - agentDir.y * agentToPoint.x, agentDir.x * agentToPoint.x + agentDir.y * agentToPoint.y);
    }

    public static void main(String[] args) {
        double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < 10000000; i++) {
            double a = Math.atan2(Math.random() * 2 - 1, Math.random() * 2 - 1);
            min = Math.min(min, a);
            max = Math.max(max, a);
        }
        System.out.println(min + " , " + max);
    }

    public double distanceTo(EmboddiedAgent other) {
        return Math.max(0, pos.distance(other.getLocation()) - other.radius - this.radius);
    }
}
