/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase;

import ec.Individual;
import ec.Singleton;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public interface ControllerDecoder extends Singleton {
    
    public GroupController decodeController(Individual[] ind);
    
}
