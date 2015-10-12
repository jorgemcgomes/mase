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
    protected void preSimulation() {
        this.lastKeeper = null;
        this.sumPasses = 0;
    }

    @Override
    protected void evaluate() {
        Keepaway kw = (Keepaway) sim;
        for (Keeper k : kw.keepers) {
            if (k.hasPossession) {
                if (lastKeeper != null && k != lastKeeper) {
                    sumPasses += lastBallPos.distance(kw.ball.getLocation());
                }
                lastKeeper = k;
                lastBallPos = kw.ball.getLocation();
                return;
            }
        }
    }

    @Override
    protected void postSimulation() {
        fitnessResult = new FitnessResult( sumPasses);
    }

    @Override
    public EvaluationResult getResult() {
        return fitnessResult;
    }    
    
}
