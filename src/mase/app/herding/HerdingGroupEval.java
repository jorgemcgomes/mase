/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.herding;

import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;

/**
 *
 * @author jorge
 */
public class HerdingGroupEval extends MasonEvaluation {

    private static final long serialVersionUID = 1L;

    private VectorBehaviourResult vbr;
    private double sheepFence;
    private double sheepFox;
    private double initSheepCurral, initSheepFox;

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(null);
        sheepFence = 0;
        sheepFox = 0;
        Herding herd = (Herding) sim;
        initSheepCurral =  herd.curral.closestDistance(herd.sheeps.get(0).getCenterLocation());
        initSheepFox = 0;
        for (Fox f : herd.foxes) {
            initSheepFox += herd.sheeps.get(0).distanceTo(f) / herd.foxes.size();
        }
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(null);

        Herding herd = (Herding) sim;
        Sheep s = herd.sheeps.get(0);

        sheepFence += herd.fence.closestDistance(s.getCenterLocation());
        for (Fox f : herd.foxes) {
            sheepFox += s.distanceTo(f);
        }
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(null);
        Herding herd = (Herding) sim;
        Sheep s = herd.sheeps.get(0);
        double sheepCurral = herd.curral.closestDistance(s.getCenterLocation());

        vbr = new VectorBehaviourResult(
                 (sheepCurral / initSheepCurral),
                (double) currentEvaluationStep / maxEvaluationSteps,
                 (sheepFence / currentEvaluationStep / (herd.par.arenaSize / 2)),
                 (sheepFox / currentEvaluationStep / herd.par.numFoxes / initSheepFox)
        );
    }

    @Override
    public EvaluationResult getResult() {
        return vbr;
    }

}
