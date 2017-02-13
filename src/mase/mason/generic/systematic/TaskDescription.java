/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.mason.generic.systematic;

/**
 *
 * @author jorge
 */
public class TaskDescription {
    
    private final EntityGroup[] groups;
        
    public TaskDescription(EntityGroup ... groups) {
        this.groups = groups;
    }
    
    public EntityGroup[] groups() {
        return groups;
    }
}
