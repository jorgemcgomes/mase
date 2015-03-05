/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import mase.generic.systematic.Entity;
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
public abstract class EmboddiedAgent extends OrientedPortrayal2D implements Steppable, Oriented2D, Entity {

    protected Continuous2D field;
    private Double2D pos;
    private double orientation;
    private final double radius;
    private double speed;
    private boolean collisionStatus;
    private Stoppable stopper;
    private boolean agentCollisions;
    private boolean boundedArena;
    private boolean polygonCollisions;
    private boolean collisionRebound;
    protected SimState sim;
    public static final double COLLISION_SPEED_DECAY = 0.5;
    public static final double COLLISION_DIRECTION = Math.PI / 2;
    private boolean isAlive;
    private double turningSpeed;
    private List<StaticPolygon> obstacleList;

    public EmboddiedAgent(SimState sim, Continuous2D field, double radius, Color c) {
        super(new OvalPortrayal2D(c, radius * 2, true), 0, radius,
                new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue()));
        this.sim = sim;
        this.radius = radius;
        this.field = field;
        this.collisionStatus = false;
        this.speed = 0;
        this.turningSpeed = 0;
        this.agentCollisions = false;
        this.boundedArena = false;
        this.polygonCollisions = false;
        this.collisionRebound = true;
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

    @Override
    public double[] getStateVariables() {
        return new double[] {getLocation().x, getLocation().y, getTurningSpeed(), getSpeed()};
    }

    public void enableAgentCollisions(boolean enable) {
        this.agentCollisions = enable;
    }

    public void enableBoundedArena(boolean enable) {
        this.boundedArena = enable;
    }
    
    public void enablePolygonCollisions(boolean enable) {
        this.polygonCollisions = enable;
    }
    
    public void enableCollisionRebound(boolean enable) {
        this.collisionRebound = enable;
    }

    protected boolean move(double orientation, double speed) {
        double o = normalizeAngle(orientation);
        this.turningSpeed = o - this.orientation;
        this.orientation = o;
        
        if (!attemptMove(orientation, speed) && collisionRebound) { // cannot move
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
    public static double normalizeAngle(double ang) {
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
        return (!boundedArena || checkInsideArena(target)) &&
                (!agentCollisions || checkAgentCollisions(target)) &&
                (!polygonCollisions || checkPolygonCollisions(target));
    }
    
    protected boolean checkPolygonCollisions(Double2D target) {
        // initialisation
        if(obstacleList == null) {
            obstacleList = new ArrayList<StaticPolygon>();
            for(Object o : field.allObjects) {
                if(o instanceof StaticPolygon) {
                    obstacleList.add((StaticPolygon) o);
                }
            }
        }
        
        // check for collisions
        for(StaticPolygon p : obstacleList) {
            double d = p.closestDistance(target);
            if(d <= radius) {
                return false;
            }
        }
        return true;
    }

    protected boolean checkInsideArena(Double2D target) {
        return target.x >= radius && target.x <= field.width - radius && target.y >= radius && target.y <= field.height - radius;
    }

    protected boolean checkAgentCollisions(Double2D target) {
        Bag objects = field.getNeighborsWithinDistance(target, radius * 5);

        for (Object o : objects) {
            if (o != this && o instanceof EmboddiedAgent) {
                EmboddiedAgent a = (EmboddiedAgent) o;
                if(a.agentCollisions && a.distanceTo(target) <= radius) {
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
    
    public double getTurningSpeed() {
        return turningSpeed;
    }
    
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
    public void setTurningSpeed(double turnSpeed) {
        this.turningSpeed = turnSpeed;
    }

    /**
     * @param point
     * @return From -PI to PI.
     */
    public double angleTo(Double2D point) {
        Double2D agentToPoint = point.subtract(pos);
        if((agentToPoint.x == 0 && agentToPoint.y == 0) || Double.isInfinite(agentToPoint.x) || Double.isInfinite(agentToPoint.y) || Double.isNaN(agentToPoint.x) || Double.isNaN(agentToPoint.y)) {
            return 0;
        }
        agentToPoint = agentToPoint.normalize();
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
    
    public double distanceTo(Double2D point) {
        return Math.max(0, pos.distance(point) - this.radius);
    }
}
