/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import mase.controllers.AgentController;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author jorge
 */
public interface Repertoire extends Serializable {
    
    /**
     * AgentController nearest to the given coordinates
     * @param coordinates
     * @return 
     */
    public AgentController nearest(double[] coordinates);
    
    /**
     * The min and max values of the coordinates in the repertoire
     * @return 
     */
    public Pair<Double,Double>[] coordinateBounds();

    /**
     * Number of controllers in the repo
     * @return
     */
    public int size();
    
    /**
     * Load the repertoire with the given controllers and the given coordinates (optional)
     * @param repo File of the archive containing the PersistentSolutions
     * @param coordinates File with the coordinates. null if the coordinates encoded in the solutions should be used instead
     * @throws java.io.IOException If anything goes wrong with the loading
     */
    public void load(File repo, File coordinates) throws IOException;
            
    /**
     * Clones the repertoire, also cloning the controllers contained in it
     * @return 
     */
    public Repertoire deepCopy();
    
}
