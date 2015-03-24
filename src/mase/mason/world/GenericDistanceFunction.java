/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason.world;

import mase.generic.systematic.DistanceFunction;
import sim.field.continuous.Continuous2D;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
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
        if ((field.exists(e1) || e1 instanceof Double2D) && (field.exists(e2) || e2 instanceof Double2D)) {
            return distance(e1, e2, false);
        } else {
            return Double.NaN;
        }
    }

    public double agentToObjectDistance(EmboddiedAgent a1, Object e2) {
        if (e2 instanceof EmboddiedAgent) {
            EmboddiedAgent a2 = (EmboddiedAgent) e2;
            return a1.distanceTo(a2);
        } else if (e2 instanceof Double2D) {
            return a1.distanceTo((Double2D) e2);
        } else if (e2 instanceof SimplePortrayal2D) {
            if (e2 instanceof OvalPortrayal2D) {
                OvalPortrayal2D a2 = (OvalPortrayal2D) e2;
                return a1.getLocation().distance(field.getObjectLocation(a2)) - a1.getRadius()/* - a2.scale / 2*/;
            } else {
                SimplePortrayal2D a2 = (SimplePortrayal2D) e2;
                return a1.getLocation().distance(field.getObjectLocation(a2)) - a1.getRadius();
            }
        } else if (e2 instanceof StaticPolygon) {
            StaticPolygon a2 = (StaticPolygon) e2;
            return a2.closestDistance(a1.getLocation()) - a1.getRadius();
        }
        return Double.NaN;
    }

    private double distance(Object e1, Object e2, boolean reverse) {
        double res = Double.NaN;
        if (e1 instanceof EmboddiedAgent) {
            EmboddiedAgent a1 = (EmboddiedAgent) e1;
            res = agentToObjectDistance(a1, e2);
        } else if (e1 instanceof StaticPolygon) {
            StaticPolygon a1 = (StaticPolygon) e2;
            if (e2 instanceof StaticPolygon) {
                StaticPolygon a2 = (StaticPolygon) e2;
                return a1.closestDistance(a2);
            } else if (e2 instanceof SimplePortrayal2D) {
                SimplePortrayal2D a2 = (SimplePortrayal2D) e2;
                return a1.closestDistance(field.getObjectLocation(a2));
            }
        } else if (e1 instanceof SimplePortrayal2D && e2 instanceof SimplePortrayal2D) {
            SimplePortrayal2D a1 = (SimplePortrayal2D) e1;
            SimplePortrayal2D a2 = (SimplePortrayal2D) e2;
            return field.getObjectLocation(a1).distance(field.getObjectLocation(a2));
        }

        if (Double.isNaN(res) && !reverse) {
            return distance(e2, e1, true);
        } else {
            return Math.max(0, res);
        }
    }

}
