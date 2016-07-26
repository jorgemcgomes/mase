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
import sim.util.MutableDouble2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public abstract class EmboddiedAgent extends WorldObject implements Steppable, Oriented2D, Entity {

    private static final long serialVersionUID = 1L;

    private double orientation;
    private double speed;
    private double turningSpeed;
    private boolean collisionStatus;
    private Stoppable stopper;
    private boolean agentCollisions;
    private boolean boundedArena;
    private boolean polygonCollisions;
    private boolean collisionRebound;
    public static final double COLLISION_SPEED_DECAY = 0.5;
    public static final double COLLISION_DIRECTION = Math.PI / 2;
    private boolean isAlive;
    private List<StaticPolygon> obstacleList;
    private boolean rotate = true;

    protected OvalPortrayal2D ovalPortrayal;
    protected OrientedPortrayal2D orientedPortrayal;

    public EmboddiedAgent(SimState sim, Continuous2D field, double radius, Color c) {
        super(new OrientedPortrayal2D(new OvalPortrayal2D()), sim, field, radius);

        this.orientedPortrayal = (OrientedPortrayal2D) this.woChild;
        this.orientedPortrayal.offset = 0;
        this.orientedPortrayal.scale = radius;

        this.ovalPortrayal = (OvalPortrayal2D) orientedPortrayal.child;
        this.ovalPortrayal.scale = radius * 2;
        this.ovalPortrayal.filled = true;

        this.setColor(c);

        this.collisionStatus = false;
        this.speed = 0;
        this.turningSpeed = 0;
        this.agentCollisions = false;
        this.boundedArena = false;
        this.polygonCollisions = false;
        this.collisionRebound = true;
        this.isAlive = true;
    }

    public boolean isAlive() {
        return isAlive;
    }

    @Override
    public void setColor(Color c) {
        super.setColor(c);
        this.ovalPortrayal.paint = c;
        this.orientedPortrayal.paint = new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue());
    }

    @Override
    public double[] getStateVariables() {
        return new double[]{getLocation().x, getLocation().y, getTurningSpeed(), getSpeed()};
    }

    public final void enableRotation(boolean r) {
        this.rotate = r;
        orientedPortrayal.setOrientationShowing(r);
    }

    public final void enableAgentCollisions(boolean enable) {
        this.agentCollisions = enable;
    }

    public final void enableBoundedArena(boolean enable) {
        this.boundedArena = enable;
    }

    public final void enablePolygonCollisions(boolean enable) {
        this.polygonCollisions = enable;
    }

    public final void enableCollisionRebound(boolean enable) {
        this.collisionRebound = enable;
    }

    protected boolean move(double orientation, double speed) {
        double o = normalizeAngle(orientation);
        if (rotate) {
            this.turningSpeed = o - this.orientation;
            this.orientation = o;
        }

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
        if (ang > Math.PI * 2) {
            ang = ang % (Math.PI * 2);
        } else if (ang < -Math.PI * 2) {
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
        return (!boundedArena || checkInsideArena(target))
                && (!agentCollisions || checkAgentCollisions(target))
                && (!polygonCollisions || checkPolygonCollisions(target));
    }

    protected boolean checkPolygonCollisions(Double2D target) {
        // initialisation
        if (obstacleList == null) {
            obstacleList = new ArrayList<>();
            for (Object o : field.allObjects) {
                if (o instanceof StaticPolygon) {
                    obstacleList.add((StaticPolygon) o);
                }
            }
        }

        // check for collisions
        for (StaticPolygon p : obstacleList) {
            double d = p.closestDistance(target);
            if (d <= radius) {
                return false;
            }
        }
        return true;
    }

    protected boolean checkInsideArena(Double2D target) {
        return target.x >= radius && target.x <= field.width - radius && target.y >= radius && target.y <= field.height - radius;
    }

    protected boolean checkAgentCollisions(Double2D target) {
        Bag objects = field.allObjects.size() < 20 ? field.allObjects : field.getNeighborsWithinDistance(target, radius, false, true);
        for (Object o : objects) {
            if (o != this && o instanceof EmboddiedAgent) {
                EmboddiedAgent a = (EmboddiedAgent) o;
                if (a.agentCollisions && a.distanceTo(target) <= radius) {
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
        // hardcoded for speed
        double agToPointX = point.x - pos.x;
        double agToPointY = point.y - pos.y;
                
        if (agToPointX == 0 && agToPointY == 0) {
            return 0;
        }
        
        // Normalize
        double l = FastMath.sqrtQuick(agToPointX * agToPointX  + agToPointY * agToPointY);
        agToPointX = agToPointX / l;
        agToPointY = agToPointY / l;
        
        double agentDirX = FastMath.cosQuick(orientation);
        double agentDirY =  FastMath.sinQuick(orientation);
        return FastMath.atan2(agentDirX * agToPointY - agentDirY * agToPointX, agentDirX * agToPointX + agentDirY * agToPointY);
    }
}
