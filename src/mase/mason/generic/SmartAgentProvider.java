/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.mason.generic;

import java.util.List;
import mase.mason.world.SmartAgent;

/**
 *
 * @author jorge
 */
public interface SmartAgentProvider {
    
    public List<? extends SmartAgent> getSmartAgents();
    
}
