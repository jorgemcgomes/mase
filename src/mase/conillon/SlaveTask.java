/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.conillon;

import java.util.ArrayList;
import mase.evaluation.EvaluationFunction;
import mase.evaluation.EvaluationResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.mason.MasonSimState;
import result.Result;
import tasks.Task;

/**
 *
 * @author jorge
 */
public class SlaveTask extends Task {

    private MasonSimState sim;
    private EvaluationFunction[] functions;
    private int repetitions;
    private int maxSteps;
    private SlaveResult res;

    public SlaveTask(MasonSimState sim, int id, EvaluationFunction[] functions, int repetitions, int maxSteps) {
        super(id);
        this.sim = sim;
        this.functions = functions;
        this.repetitions = repetitions;
        this.maxSteps = maxSteps;
    }

    @Override
    public SlaveResult getResult() {
        return res;
    }

    @Override
    public void run() {
        EvaluationResult[] evalResults = sim.evaluate(repetitions, maxSteps, functions);
        ArrayList<EvaluationResult> resList = new ArrayList<>();
        /*
        Dirty fix to remove SubpopEvaluationResults (causing problems with Conillon)
        The results from SER are added to the list in between nulls
        */
        for(EvaluationResult er : evalResults) {
            if(er instanceof SubpopEvaluationResult) {
                resList.add(null);
                SubpopEvaluationResult ser = (SubpopEvaluationResult) er;
                resList.addAll(ser.getAllEvaluations());
                resList.add(null);
            } else {
                resList.add(er);
            }
        }
        res = new SlaveResult(resList, id);
        
    }

}
