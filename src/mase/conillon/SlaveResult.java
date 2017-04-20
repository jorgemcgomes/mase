/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.conillon;

import java.util.ArrayList;
import mase.evaluation.EvaluationResult;
import mase.evaluation.CompoundEvaluationResult;
import result.Result;

/**
 *
 * @author jorge
 */
public class SlaveResult extends Result {

    private static final long serialVersionUID = 1L;
    
    private ArrayList<EvaluationResult> results;
    
    public SlaveResult(ArrayList<EvaluationResult> results, int id) {
        super(id);
        this.results = results;
    }
    
    public ArrayList<EvaluationResult> getEvaluationResults() {
        /*
        Dirty fix to avoid SubpopEvaluationResults (causing problems with Conillon)
        Restore the SubpopEvaluationResults, contained in the list between nulls
        */
        ArrayList<EvaluationResult> resList = new ArrayList<>();
        ArrayList<EvaluationResult> temp = null;
        for(EvaluationResult er : results) {
            if(er == null && temp == null) { // start new SER
                temp = new ArrayList<>();
            } else if(er != null && temp != null) { // add to existing SER
                temp.add(er);
            } else if(er == null && temp != null) { // end existing SER
                resList.add(new CompoundEvaluationResult(temp));
                temp = null;
            } else { // not SER
                resList.add(er);
            }
        }
        return resList;
    }    
}
