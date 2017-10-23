/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import ec.Setup;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import mase.controllers.AgentController;

/**
 *
 * @author jorge
 */
public interface Repertoire extends Setup {
    
    /**
     * AgentController nearest to the given coordinates
     * @param coordinates
     * @return the Id of the AgentController and the AgentController itself
     */
    public Primitive nearest(double[] coordinates);
    
    /**
     * A collection with the complete repertoire
     * @return 
     */
    public Collection<Primitive> allPrimitives();
    
    /**
     * Clones the repertoire, also cloning the controllers contained in it
     * @return 
     */
    public Repertoire deepCopy();
    
    
    public static class Primitive implements Serializable {

        private static final long serialVersionUID = 1L;
        
        public final AgentController ac;
        public final int id;
        public final double[] coordinates;

        public Primitive(AgentController ac, int id, double[] coordinates) {
            this.ac = ac;
            this.id = id;
            this.coordinates = coordinates;
        }
        
        public Primitive clone() {
            return new Primitive(ac.clone(), id, coordinates);
        }
        
    }
        
}
