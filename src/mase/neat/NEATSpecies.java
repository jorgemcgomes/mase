/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.neat;

import ec.Species;
import ec.util.Parameter;

/**
 *
 * @author jorge
 */
public class NEATSpecies extends Species {
    
    public static final String P_NEAT_SPECIES = "neat.species";

    @Override
    public Parameter defaultBase() {
        return new Parameter(P_NEAT_SPECIES);
    }
}
