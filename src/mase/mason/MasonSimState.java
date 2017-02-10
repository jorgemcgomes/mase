/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import mase.controllers.GroupController;
import mase.evaluation.EvaluationFunction;
import mase.evaluation.EvaluationResult;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public abstract class MasonSimState extends SimState {

    private static final long serialVersionUID = 1L;

    protected final GroupController gc;
    protected EvaluationFunction[] evalPrototypes = new EvaluationFunction[0];
    protected int maxSteps = 0;
    public MasonEvaluation[] currentEvals;

    public MasonSimState(GroupController gc, long seed) {
        super(seed);
        this.gc = gc;
    }

    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    public void setEvalFunctions(EvaluationFunction[] evalPrototypes) {
        this.evalPrototypes = evalPrototypes;
    }

    @Override
    public void start() {
        super.start();

        // Init eval functions and schedule them
        currentEvals = new MasonEvaluation[evalPrototypes.length];
        for (int i = 0; i < evalPrototypes.length; i++) {
            currentEvals[i] = (MasonEvaluation) evalPrototypes[i].clone();
            if (currentEvals[i].updateFrequency > 0) {
                super.schedule.scheduleRepeating(currentEvals[i], currentEvals[i].updateFrequency, 1 + i);
            }
        }

        // Schedule pre-simulation evaluation
        super.schedule.scheduleOnce(Schedule.EPOCH, -1, new Steppable() {
            @Override
            public void step(SimState state) {
                for (MasonEvaluation e : currentEvals) {
                    e.preSimulation(MasonSimState.this);
                }
            }
        });

        // Schedule termination by max steps
        super.schedule.scheduleOnce(maxSteps, Integer.MAX_VALUE, new Steppable() {
            @Override
            public void step(SimState state) {
                state.kill();
            }
        });
    }

    @Override
    public void kill() {
        for (MasonEvaluation e : currentEvals) {
            e.postSimulation(this);
        }
        super.kill();
    }

    public synchronized EvaluationResult[] evaluate(int repetitions) {
        EvaluationResult[][] evalResults = new EvaluationResult[evalPrototypes.length][repetitions];
        for (int r = 0; r < repetitions; r++) {
            start();
            while (schedule.step(this));
            for (int i = 0; i < currentEvals.length; i++) {
                evalResults[i][r] = currentEvals[i].getResult();
            }
        }

        EvaluationResult[] mergedResult = new EvaluationResult[evalPrototypes.length];
        for (int i = 0; i < evalPrototypes.length; i++) {
            mergedResult[i] = evalResults[i][0].mergeEvaluations(evalResults[i]);
        }
        return mergedResult;
    }

    public FieldPortrayal2D createFieldPortrayal() {
        return new ContinuousPortrayal2D();
    }

    public abstract void setupPortrayal(FieldPortrayal2D port);

}
