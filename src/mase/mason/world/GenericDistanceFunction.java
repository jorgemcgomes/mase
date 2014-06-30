/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.mason.world;

import mase.generic.systematic.DistanceFunction;
import sim.field.continuous.Continuous2D;
import sim.portrayal.SimplePortrayal2D;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class GenericDistanceFunction extends DistanceFunction {

    private final Continuous2D field;
    
    public GenericDistanceFunction(Continuous2D field) {
        this.field = field;
    }
    
    @Override
    public double distance(Object e1, Object e2) {
        if(field.exists(e1) && field.exists(e2)) {
            return distance(e1, e2, false);
        } else {
            return Double.NaN;
        }
    }
    
    private double distance(Object e1, Object e2, boolean reverse) {
        double res = Double.NaN;
        if(e1 instanceof EmboddiedAgent && e2 instanceof EmboddiedAgent) {
            EmboddiedAgent a1 = (EmboddiedAgent) e1;
            EmboddiedAgent a2 = (EmboddiedAgent) e2;
            res = a1.distanceTo(a2);
        } else if(e1 instanceof EmboddiedAgent && e2 instanceof StaticPolygon) {
            EmboddiedAgent a1 = (EmboddiedAgent) e1;
            StaticPolygon a2 = (StaticPolygon) e2;
            res = a2.closestDistance(a1.getLocation()) - a1.getRadius();
        } else if(e1 instanceof EmboddiedAgent && e2 instanceof SimplePortrayal2D) {
            EmboddiedAgent a1 = (EmboddiedAgent) e1;
            SimplePortrayal2D a2 = (SimplePortrayal2D) e2;
            res = a1.getLocation().distance(field.getObjectLocation(a2)) - a1.getRadius();
        } else if(e1 instanceof StaticPolygon && e2 instanceof StaticPolygon) {
            StaticPolygon a1 = (StaticPolygon) e2;
            StaticPolygon a2 = (StaticPolygon) e2;
            return a1.closestDistance(a2);
        } else if(e1 instanceof StaticPolygon && e2 instanceof SimplePortrayal2D) { 
            StaticPolygon a1 = (StaticPolygon) e1;
            SimplePortrayal2D a2 = (SimplePortrayal2D) e2;
            return a1.closestDistance(field.getObjectLocation(a2));
        } else if(e1 instanceof SimplePortrayal2D && e2 instanceof SimplePortrayal2D) {
            SimplePortrayal2D a1 = (SimplePortrayal2D) e1;
            SimplePortrayal2D a2 = (SimplePortrayal2D) e2;
            return field.getObjectLocation(a1).distance(field.getObjectLocation(a2));
        }
        
        if(Double.isNaN(res) && !reverse) {
            return distance(e2, e1, true);
        } else {
            return Math.max(0, res);
        }
    }

}
