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
public class TaskDescription {
    
    private final EntityGroup[] groups;
    private final DistanceFunction df;
        
    public TaskDescription(DistanceFunction df, EntityGroup ... groups) {
        this.groups = groups;
        this.df = df;
    }
    
    public EntityGroup[] groups() {
        return groups;
    }
    
    public DistanceFunction distanceFunction() {
        return df;
    }
    
}
