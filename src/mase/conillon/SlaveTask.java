/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.conillon;

import java.util.ArrayList;
import java.util.Arrays;
import mase.controllers.GroupController;
import mase.evaluation.EvaluationFunction;
import mase.evaluation.EvaluationResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.mason.MasonStandaloneSimulator;
import result.Result;
import tasks.Task;

/**
 *
 * @author jorge
 */
public class SlaveTask extends Task {

    private MasonStandaloneSimulator sim;
    private EvaluationFunction[] functions;
    private int repetitions;
    private int maxSteps;
    private GroupController gc;
    private long seed;
    private SlaveResult res;
    //private ArrayList<EvaluationResult> resList;

    public SlaveTask(MasonStandaloneSimulator sim, GroupController gc, long seed, int id, EvaluationFunction[] functions, int repetitions, int maxSteps) {
        super(id);
        this.sim = sim;
        this.gc = gc;
        this.seed = seed;
        this.functions = functions;
        this.repetitions = repetitions;
        this.maxSteps = maxSteps;
    }

    @Override
    public Result getResult() {
        //return new SlaveResult(resList, id);
        return res;
    }

    @Override
    public void run() {
        EvaluationResult[] evalResults = sim.evaluateSolution(gc, seed, repetitions, maxSteps, functions);
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
