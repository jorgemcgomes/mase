/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.conillon;

import mase.SimulationProblem;
import mase.controllers.GroupController;
import mase.evaluation.EvaluationResult;
import result.Result;
import tasks.Task;

/**
 *
 * @author jorge
 */
public class SlaveTask extends Task {

    private final SimulationProblem sim;
    private final GroupController gc;
    private final long seed;
    private SlaveResult res;

    public SlaveTask(SimulationProblem sim, GroupController gc, long seed, int id) {
        super(id);
        this.sim = sim;
        this.gc = gc;
        this.seed = seed;
    }

    @Override
    public Result getResult() {
        return res;
    }

    @Override
    public void run() {
        EvaluationResult[] evalResults = sim.evaluateSolution(gc, seed);
        res = new SlaveResult(evalResults, id);
    }

}
