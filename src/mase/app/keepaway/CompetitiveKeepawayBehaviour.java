/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.keepaway;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimulator;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class CompetitiveKeepawayBehaviour extends MasonEvaluation {

    private VectorBehaviourResult res;
    public static final String P_PASS_LENGTH = "pass-length";
    public static final String P_PASS_NORMALIZATION = "pass-normalization";
    private double minPass;
    private int numPasses;
    private transient Keeper lastKeeper;
    private Double2D lastPossession;
    private int maxSteps;
    private int passesNormalization;
    private float takerDist;
    
    private int allPassesCount;
    private float passLength;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.minPass = state.parameters.getDouble(base.push(P_PASS_LENGTH), null);
        this.maxSteps = state.parameters.getInt(base.pop().pop().push(MasonSimulator.P_MAX_STEPS), null);
        this.passesNormalization = state.parameters.getInt(base.push(P_PASS_NORMALIZATION), null);
    }

    @Override
    protected void preSimulation() {
        this.numPasses = 0;
        this.lastKeeper = null;
        this.passLength = 0;
        this.allPassesCount = 0;
        this.takerDist = 0;
    }

    @Override
    protected void evaluate() {        
        Keepaway kw = (Keepaway) sim;
        // number of effective passes and pass length
        for (Keeper k : kw.keepers) {
            if (k.hasPossession) {
                if(lastKeeper != null && k != lastKeeper) { // pass between different agents
                    double passDist = lastPossession.distance(kw.ball.getLocation());
                    if(passDist > minPass) {
                        numPasses++;
                    }
                    allPassesCount++;
                    passLength += passDist;
                }
                lastKeeper = k;
                lastPossession = kw.ball.getLocation();
                return;
            }
        }

        takerDist += kw.takers.get(0).distanceTo(kw.ball);
        
    }

    @Override
    protected void postSimulation() {
        Keepaway kw = (Keepaway) sim;
        float steps = sim.schedule.getSteps();
        this.res = new VectorBehaviourResult(
                numPasses / (float) passesNormalization,
                steps / maxSteps,
                allPassesCount == 0 ? 0 : (float) (passLength / allPassesCount / kw.par.size),
                (float) (takerDist / steps / kw.par.size));
    }

    @Override
    public EvaluationResult getResult() {
        return res;
    }
    
}
