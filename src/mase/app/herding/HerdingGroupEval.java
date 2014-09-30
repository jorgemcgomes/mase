/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.herding;

import mase.evaluation.EvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;

/**
 *
 * @author jorge
 */
public class HerdingGroupEval extends MasonEvaluation {

    private VectorBehaviourResult vbr;
    private float sheepFence;
    private float sheepFox;
    private float initSheepCurral, initSheepFox;

    @Override
    protected void preSimulation() {
        super.preSimulation();
        sheepFence = 0;
        sheepFox = 0;
        Herding herd = (Herding) sim;
        initSheepCurral = (float) herd.curral.closestDistance(herd.sheeps.get(0).getLocation());
        initSheepFox = 0;
        for (Fox f : herd.foxes) {
            initSheepFox += herd.sheeps.get(0).distanceTo(f) / herd.foxes.size();
        }
    }

    @Override
    protected void evaluate() {
        super.evaluate();

        Herding herd = (Herding) sim;
        Sheep s = herd.sheeps.get(0);

        sheepFence += herd.fence.closestDistance(s.getLocation());
        for (Fox f : herd.foxes) {
            sheepFox += s.distanceTo(f);
        }
    }

    @Override
    protected void postSimulation() {
        super.postSimulation();
        Herding herd = (Herding) sim;
        Sheep s = herd.sheeps.get(0);
        double sheepCurral = herd.curral.closestDistance(s.getLocation());

        vbr = new VectorBehaviourResult(
                (float) (sheepCurral / initSheepCurral),
                (float) currentEvaluationStep / maxEvaluationSteps,
                (float) (sheepFence / currentEvaluationStep / (herd.par.arenaSize / 2)),
                (float) (sheepFox / currentEvaluationStep / herd.par.numFoxes / initSheepFox)
        );
    }

    @Override
    public EvaluationResult getResult() {
        return vbr;
    }

}
