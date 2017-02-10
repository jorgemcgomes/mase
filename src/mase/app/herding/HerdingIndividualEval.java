/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.herding;

import mase.evaluation.EvaluationResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class HerdingIndividualEval extends MasonEvaluation {

    private static final long serialVersionUID = 1L;

    private int[] sheepLife;
    private double[][] sheepDist;
    private double[][] foxDist;
    private double[] curralDist;
    private Double2D curral;
    private SubpopEvaluationResult ser;

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(null);
        Herding herd = (Herding) sim;
        sheepDist = new double[herd.shepherds.size()][herd.sheeps.size()];
        foxDist = new double[herd.shepherds.size()][herd.foxes.size()];
        sheepLife = new int[herd.sheeps.size()];
        curralDist = new double[herd.shepherds.size()];
        curral = new Double2D(herd.par.arenaSize, herd.par.arenaSize / 2);
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        super.evaluate(null);
        Herding herd = (Herding) sim;
        for (int i = 0; i < herd.shepherds.size(); i++) {
            Shepherd shep = herd.shepherds.get(i);

            // Sheeps
            for (int s = 0; s < herd.sheeps.size(); s++) {
                Sheep sheep = herd.sheeps.get(s);
                if (sheep.isAlive()) {
                    sheepDist[i][s] += shep.distanceTo(sheep);
                }
            }

            // Foxes
            for (int f = 0; f < herd.foxes.size(); f++) {
                foxDist[i][f] += shep.distanceTo(herd.foxes.get(f));
            }

            // Gate
            curralDist[i] += shep.getLocation().distance(curral);
        }
        for (int s = 0; s < herd.sheeps.size(); s++) {
            sheepLife[s]++;
        }
    }

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(null);
        Herding herd = (Herding) sim;
        VectorBehaviourResult[] res = new VectorBehaviourResult[herd.shepherds.size()];
        double maxD = herd.par.arenaSize;
        for (int i = 0; i < herd.shepherds.size(); i++) {
            double[] br = new double[1 + herd.foxes.size() + herd.sheeps.size()];
            int index = 0;
            br[index++] = curralDist[i] / currentEvaluationStep / maxD;
            for (int j = 0; j < foxDist[i].length; j++) {
                br[index++] = foxDist[i][j] / currentEvaluationStep / maxD;
            }
            for (int j = 0; j < sheepDist[i].length; j++) {
                br[index++] = sheepDist[i][j] / sheepLife[j] / maxD;
            }
            res[i] = new VectorBehaviourResult(br);
        }
        this.ser = new SubpopEvaluationResult(res);
    }

    @Override
    public EvaluationResult getResult() {
        return ser;
    }

}
