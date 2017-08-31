/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import ec.util.Parameter;
import ec.util.ParameterDatabase;
import java.util.ArrayList;
import java.util.List;
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
public abstract class MasonSimState<T> extends SimState {

    private static final long serialVersionUID = 1L;

    protected GroupController gc;
    protected EvaluationFunction[] evalPrototypes = new EvaluationFunction[0];
    protected int maxSteps = 0;
    public MasonEvaluation[] currentEvals;
    protected List<Steppable> loggers;
    public T par;

    public MasonSimState(long seed) {
        super(seed);
    }

    public MasonSimState(GroupController gc, long seed) {
        this(seed);
        this.gc = gc;
    }
    
    public MasonSimState(GroupController gc, long seed, T params) {
        this(gc, seed);
        this.par = params;
    }
    
    public void setParams(T par) {
        this.par = par;
    }
    
    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }
    
    public int getMaxSteps() {
        return maxSteps;
    }

    public void setEvalFunctions(EvaluationFunction[] evalPrototypes) {
        this.evalPrototypes = evalPrototypes;
    }
    
    public void setLoggers(List<Steppable> loggers) {
        this.loggers = loggers;
    }
    
    public void setGroupController(GroupController gc) {
        this.gc = gc;
    }

    @Override
    public void start() {
        super.start();

        // Init eval functions and schedule them
        currentEvals = new MasonEvaluation[evalPrototypes.length];
        for (int i = 0; i < evalPrototypes.length; i++) {
            currentEvals[i] = (MasonEvaluation) evalPrototypes[i].clone();
            if (currentEvals[i].updateFrequency > 0) {
                // Time of first event (begining); ordering (after agents, in the declared order); Steppable; interval (update-freq)
                super.schedule.scheduleRepeating(Schedule.EPOCH, 1000 + i, currentEvals[i], currentEvals[i].updateFrequency);
            }
        }

        // Schedule pre-simulation evaluation
        super.schedule.scheduleOnce(Schedule.EPOCH, -Integer.MAX_VALUE, new Steppable() {
            @Override
            public void step(SimState state) {
                for (MasonEvaluation e : currentEvals) {
                    e.preSimulation(MasonSimState.this);
                }
            }
        });

        // Schedule logging
        for(int i = 0 ; i < loggers.size() ; i++) {
            super.schedule.scheduleRepeating(loggers.get(i), 2000 + i, 1);
        }

        // Schedule termination by max steps
        super.schedule.scheduleOnce(maxSteps - 1, Integer.MAX_VALUE, new Steppable() {
            @Override
            public void step(SimState state) {
                state.kill();
            }
        });
    }

    @Override
    public void kill() {
        // let the current epoch finish and schedule termination for next
        schedule.clear();
        schedule.scheduleOnceIn(Schedule.EPOCH_PLUS_EPSILON, new Steppable() {
            @Override
            public void step(SimState state) {
                for (MasonEvaluation e : currentEvals) {
                    e.postSimulation(MasonSimState.this);
                }
                MasonSimState.super.kill();                
            }
        });
    }

    public synchronized EvaluationResult[] evaluate(int repetitions) {
        List<EvaluationResult[]> evalResults = new ArrayList<>(repetitions);
        for (int r = 0; r < repetitions; r++) {
            start();
            while (schedule.step(this));
            EvaluationResult[] temp = new EvaluationResult[evalPrototypes.length];
            for (int i = 0; i < currentEvals.length; i++) {
                temp[i] = currentEvals[i].getResult();
            }
            evalResults.add(temp);
        }
        return mergeResults(evalResults);
    }
    
    protected EvaluationResult[] mergeResults(List<EvaluationResult[]> evalResults) {
        EvaluationResult[] mergedResult = new EvaluationResult[evalPrototypes.length];
        for (int i = 0; i < evalPrototypes.length; i++) {
            List<EvaluationResult> temp = new ArrayList<>(evalResults.size());
            for(EvaluationResult[] e : evalResults) {
                temp.add(e[i]);
            }
            mergedResult[i] = temp.get(0).mergeEvaluations(temp);
        }
        return mergedResult;        
    }

    public FieldPortrayal2D createFieldPortrayal() {
        return new ContinuousPortrayal2D();
    }

    public abstract void setupPortrayal(FieldPortrayal2D port);

}
