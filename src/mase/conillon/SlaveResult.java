/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.conillon;

import mase.evaluation.EvaluationResult;
import result.Result;

/**
 *
 * @author jorge
 */
public class SlaveResult extends Result {
    
    private final EvaluationResult[] results;
    private final int id;
    
    public SlaveResult(EvaluationResult[] results, int id) {
        this.results = results;
        this.id = id;
    }
    
    public EvaluationResult[] getEvaluationResults() {
        return results;
    }
    
    public int getID() {
        return id;
    }
    
}
