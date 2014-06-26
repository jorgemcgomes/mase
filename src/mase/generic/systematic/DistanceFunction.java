/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.generic.systematic;

/**
 *
 * @author jorge
 */
public abstract class DistanceFunction {
    
    public abstract double distance(Object e1, Object e2);
    

    /*
    Average linkage
    */
    public double distance(EntityGroup eg1, EntityGroup eg2) {
        double total = 0 ;
        int count = 0 ;
        for(Entity e1: eg1) {
            for(Entity e2 : eg2) {
                total += distance(e1, e2);
                count++;
            }
        }
        return count == 0 ? Double.NaN : total / count;
    }    
}
