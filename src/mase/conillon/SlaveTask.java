/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.conillon;

import java.util.ArrayList;
import mase.SimulationProblem;
import mase.controllers.GroupController;
import mase.evaluation.EvaluationResult;
import mase.evaluation.SubpopEvaluationResult;
import tasks.Task;

/**
 *
 * @author jorge
 */
public class SlaveTask extends Task {

    private static final long serialVersionUID = 1L;

    private final SimulationProblem sim;
    private final long seed;
    private SlaveResult res;
    private final GroupController gc;

    public SlaveTask(int id, SimulationProblem sim, GroupController gc, long seed) {
        super(id);
        this.sim = sim;
        this.seed = seed;
        this.gc = gc;
    }

    @Override
    public SlaveResult getResult() {
        return res;
    }

    @Override
    public void run() {
        EvaluationResult[] evalResults = sim.evaluateSolution(gc, seed);
        ArrayList<EvaluationResult> resList = new ArrayList<>();
        /*
        Dirty fix to remove SubpopEvaluationResults (causing problems with Conillon)
        The results from SER are added to the list in between nulls
         */
        for (EvaluationResult er : evalResults) {
            if (er instanceof SubpopEvaluationResult) {
                resList.add(null);
                SubpopEvaluationResult ser = (SubpopEvaluationResult) er;
                resList.addAll(ser.getAllEvaluations());
                resList.add(null);
            } else {
                resList.add(er);
            }
        }
        this.res = new SlaveResult(resList, getId());
    }

}
