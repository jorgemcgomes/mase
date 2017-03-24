/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import sim.util.Double2D;

/**
 * All objects to be added to the world should be implement this in order to be
 * able to interact with the agents
 * @author jorge
 */
public interface WorldObject {
    
    /**
     * Gets the current object location. In case of non-point objects, this should be the center.
     * @return The current location of the object.
     */
    public Double2D getLocation();
    
    /**
     * Returns the point belonging to the object that intersects the given ray.
     * In case multiple intersections are possible, it should return the point closest to start.
     * @param start The start of the test ray
     * @param end  The end of the test ray
     * @return The intersection point closest to start, or null if there are no intersections
     */
    public double closestRayIntersection(Double2D start, Double2D end);
        
    /**
     * Returns the closest distance of this object to the given point.
     * @param p The test point
     * @return The distance
     */
    public double distanceTo(Double2D p);
    
    /**
     * Returns whether the given point is inside this object.
     * @param p The test point
     * @return true if p is inside this object, false otherwise
     */
    public boolean isInside(Double2D p);
    
}
