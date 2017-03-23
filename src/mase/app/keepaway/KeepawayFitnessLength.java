/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.keepaway;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.evaluation.EvaluationResult;
import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class KeepawayFitnessLength extends MasonEvaluation {

    private FitnessResult fitnessResult;
    private transient Keeper lastKeeper;
    private Double2D lastBallPos;
    private double sumPasses;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
    }

    @Override
    protected void preSimulation(MasonSimState sim) {
        this.lastKeeper = null;
        this.sumPasses = 0;
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        Keepaway kw = (Keepaway) sim;
        for (Keeper k : kw.keepers) {
            if (k.hasPossession) {
                if (lastKeeper != null && k != lastKeeper) {
                    sumPasses += lastBallPos.distance(kw.ball.getCenterLocation());
                }
                lastKeeper = k;
                lastBallPos = kw.ball.getCenterLocation();
                return;
            }
        }
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        fitnessResult = new FitnessResult( sumPasses);
    }

    @Override
    public EvaluationResult getResult() {
        return fitnessResult;
    }    
    
}
