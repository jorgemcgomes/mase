/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.conillon;

import java.util.ArrayList;
import result.Result;

/**
 *
 * @author jorge
 */
public class MetaSlaveResult extends Result {
    
    ArrayList<SlaveResult> results;
    
    public MetaSlaveResult(ArrayList<SlaveResult> results) {
        this.results = results;
    }
    
    public ArrayList<SlaveResult> getResults() {
        return results;
    }
    
}
