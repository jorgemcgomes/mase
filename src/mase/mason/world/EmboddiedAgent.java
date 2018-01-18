/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import mase.mason.generic.systematic.Entity;
import net.jafama.FastMath;
import org.apache.commons.math3.util.MathUtils;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.continuous.Continuous2D;
import sim.portrayal.Oriented2D;
import sim.portrayal.simple.AbstractShapePortrayal2D;
import sim.portrayal.simple.OrientedPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.portrayal.simple.TransformedPortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public abstract class EmboddiedAgent extends CircularObject implements Steppable, Oriented2D, Entity {

    private static final long serialVersionUID = 1L;

    private double orientation;
    private double speed;
    private double turningSpeed;
    private boolean collisionStatus;
    private Stoppable stopper;
    private boolean boundedArena;
    private boolean collisionRebound;
    private boolean rotateWithCollision;
    private Class<? extends WorldObject>[] collidableTypes;
    private double collisionSpeedDecay = 0.5;
    private double collisionReboundDirection = Math.PI / 2;
    private boolean isAlive;
    private boolean rotate = true;

    protected AbstractShapePortrayal2D agentPortrayal;
    protected OrientedPortrayal2D orientedPortrayal;
    protected TransformedPortrayal2D transformPortrayal;

    public EmboddiedAgent(SimState sim, Continuous2D field, double radius, Color c) {
        super(new OrientedPortrayal2D(new TransformedPortrayal2D(new OvalPortrayal2D(), new AffineTransform())), sim, field, radius);

        this.orientedPortrayal = (OrientedPortrayal2D) this.woChild;
        this.orientedPortrayal.offset = 0;
        this.orientedPortrayal.scale = radius;

        this.transformPortrayal = (TransformedPortrayal2D) orientedPortrayal.child;
        this.agentPortrayal = (AbstractShapePortrayal2D) transformPortrayal.child;
        this.agentPortrayal.scale = radius * 2;
        this.agentPortrayal.filled = true;

        this.setColor(c);

        this.collisionStatus = false;
        this.speed = 0;
        this.turningSpeed = 0;
        this.boundedArena = false;
        this.collisionRebound = true;
        this.rotateWithCollision = true;
        this.isAlive = true;
        
    }
    
    public void replaceAgentPortrayal(AbstractShapePortrayal2D port) {
        port.scale = radius * 2;
        port.filled = true;
        port.paint = this.paint;
        this.transformPortrayal.child = port;
    }

    public boolean isAlive() {
        return isAlive;
    }

    @Override
    public void setColor(Color c) {
        super.setColor(c);
        this.agentPortrayal.paint = c;
        this.orientedPortrayal.paint = new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue(), c.getAlpha());
    }

    @Override
    public double[] getStateVariables() {
        return new double[]{getLocation().x, getLocation().y, getTurningSpeed(), getSpeed()};
    }

    public final void enableRotation(boolean r) {
        this.rotate = r;
        orientedPortrayal.setOrientationShowing(r);
    }
    
    public void enableRotationWithCollisions(boolean r) {
        this.rotateWithCollision = r;
    }

    public void setCollidableTypes(Class<? extends WorldObject>... types) {
        this.collidableTypes = types;
    }

    public void setCollisionSpeedDecay(double collisionSpeedDecay) {
        this.collisionSpeedDecay = collisionSpeedDecay;
    }

    public void setCollisionReboundDirection(double collisionReboundDirection) {
        this.collisionReboundDirection = collisionReboundDirection;
    }

    private boolean collisionFree(Double2D target) {
        if (collidableTypes == null || collidableTypes.length == 0) {
            return true;
        }
        // TODO: this should use nearest neighbours
        WorldObject[] possible = candidateCollisions();
        for (WorldObject so : possible) {
            double d = so.distanceTo(target);
            if (d <= getRadius()) {
                return false;
            }
        }
        return true;
    }

    private WorldObject[] cache;
    private int cacheHash;

    private int fieldHash() {
        int h = 0;
        for (Object o : field.allObjects) {
            if (o instanceof WorldObject) {
                h += o.hashCode();
            }
        }
        return h;
    }

    private void updateCache() {
        int fieldHash = fieldHash();
        if (cache == null || fieldHash != cacheHash) {
            cacheHash = fieldHash;
            cache = new WorldObject[field.allObjects.size()];
            int index = 0;
            for (Object n : field.allObjects) {
                if (n != this && n instanceof WorldObject) {
                    for (Class<? extends WorldObject> type : collidableTypes) {
                        if (type.isInstance(n)) {
                            cache[index++] = (WorldObject) n;
                            break;
                        }
                    }
                }
            }
            if (index != cache.length) {
                cache = Arrays.copyOf(cache, index);
            }
        }
    }

    private WorldObject[] candidateCollisions() {
        updateCache();
        WorldObject[] candidates = new WorldObject[cache.length];
        int index = 0;
        for (WorldObject so : cache) {
            // Optional quick check to speedup things
            if (so instanceof MultilineObject) {
                if (((MultilineObject) so).quickProximityCheck(getLocation(), getRadius() * 2)) {
                    candidates[index++] = so;
                }
            } else {
                candidates[index++] = so;
            }
        }
        if (index != candidates.length) {
            candidates = Arrays.copyOf(candidates, index);
        }
        return candidates;
    }

    public final void enableBoundedArena(boolean enable) {
        this.boundedArena = enable;
    }

    public final void enableCollisionRebound(boolean enable) {
        this.collisionRebound = enable;
    }
    
    /**
     * Move to the newPosition, and get the newOrientation
     * @param newOrientation
     * @param newPosition
     * @return 
     */
    protected boolean move(double newOrientation, Double2D newPosition) {
        double oldOrientation = this.orientation;
        Double2D moveVec = newPosition.subtract(this.getLocation());
        boolean success = this.move(moveVec.angle(), moveVec.length());
        if(rotate && (rotateWithCollision || success)) {
            this.turningSpeed = newOrientation - oldOrientation;
            this.orientation = MathUtils.normalizeAngle(newOrientation, 0);            
        } else {
            this.turningSpeed = 0;
        }
        return success;
    }

    /**
     * Try to move in the direction of the newOrientation, and get that orientation
     * @param newOrientation
     * @param speed
     * @return 
     */
    protected boolean move(double newOrientation, double speed) {        
        boolean success = attemptMove(newOrientation, speed);
        if (!success && collisionRebound) { // cannot move, rebound if allowed
            // try to escape to both sides with a random order
            double angle = sim.random.nextBoolean() ? collisionReboundDirection : -collisionReboundDirection;
            success = attemptMove(MathUtils.normalizeAngle(newOrientation + angle, 0), speed * collisionSpeedDecay) ||
                    attemptMove(MathUtils.normalizeAngle(newOrientation - angle, 0), speed * collisionSpeedDecay);
        }
        
        if (rotate && (rotateWithCollision || success)) {
            this.turningSpeed = newOrientation - this.orientation;
            this.orientation = MathUtils.normalizeAngle(newOrientation, 0);
        } else {
            this.turningSpeed = 0;
        }
        return success;
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
        return (!boundedArena || checkInsideArena(target)) && collisionFree(target);
    }

    protected boolean checkInsideArena(Double2D target) {
        return target.x >= radius && target.x <= field.width - radius && target.y >= radius && target.y <= field.height - radius;
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
    
    public double getOrientationInDegrees() {
        return Math.toDegrees(orientation);
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
        double l = FastMath.sqrtQuick(agToPointX * agToPointX + agToPointY * agToPointY);
        agToPointX = agToPointX / l;
        agToPointY = agToPointY / l;

        double agentDirX = FastMath.cosQuick(orientation);
        double agentDirY = FastMath.sinQuick(orientation);
        return FastMath.atan2(agentDirX * agToPointY - agentDirY * agToPointX, agentDirX * agToPointX + agentDirY * agToPointY);
    }

    public double distanceTo(WorldObject so) {
        return Math.max(0, so.distanceTo(this.getLocation()) - this.getRadius());
    }
    
    
}
