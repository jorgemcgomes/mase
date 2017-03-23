/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public interface SensableObject {
    
    public Double2D getCenterLocation();
    
    public double closestRayIntersection(Double2D start, Double2D end);
    
    public double distanceTo(EmboddiedAgent ag);
    
    public boolean isInside(EmboddiedAgent ag);
    
}
