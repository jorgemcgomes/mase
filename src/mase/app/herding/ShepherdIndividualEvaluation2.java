/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.herding;

import java.util.ArrayList;
import java.util.List;
import mase.evaluation.EvaluationResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class ShepherdIndividualEvaluation2 extends MasonEvaluation {

    private double[] sheepDist;
    private List<double[]> foxDist;
    private double[] gateDist;
    private Double2D gate;
    private SubpopEvaluationResult ser;

    @Override
    protected void preSimulation() {
        super.preSimulation();
        Herding herd = (Herding) sim;
        sheepDist = new double[herd.shepherds.size()];
        foxDist = new ArrayList<double[]>(herd.foxes.size());
        for (int i = 0; i < herd.foxes.size(); i++) {
            foxDist.add(new double[herd.shepherds.size()]);
        }
        gateDist = new double[herd.shepherds.size()];
        gate = new Double2D(herd.par.arenaSize, herd.par.arenaSize / 2);
    }

    @Override
    protected void evaluate() {
        super.evaluate();
        Herding herd = (Herding) sim;
        for (int i = 0; i < herd.shepherds.size(); i++) {
            Shepherd shep = herd.shepherds.get(i);
            // Closest sheep
            Sheep closestSheep = null;
            for (Sheep s : herd.activeSheeps) {
                double d = shep.distanceTo(s);
                if (closestSheep == null || d < shep.distanceTo(closestSheep)) {
                    closestSheep = s;
                }
            }
            if(closestSheep != null) {
                sheepDist[i] += shep.distanceTo(closestSheep);
            }
            
            // Foxes
            for (int f = 0; f < herd.foxes.size(); f++) {
                foxDist.get(f)[i] += shep.distanceTo(herd.foxes.get(f));
            }

            // Gate
            gateDist[i] += shep.getLocation().distance(gate);
        }
    }

    @Override
    protected void postSimulation() {
        super.postSimulation();
        Herding herd = (Herding) sim;
        VectorBehaviourResult[] res = new VectorBehaviourResult[herd.shepherds.size()];
        double maxD = herd.par.arenaSize;
        for(int i = 0 ; i < herd.shepherds.size() ; i++) {
            float[] br = new float[2 + foxDist.size()];
            br[0] = (float) (sheepDist[i] / currentEvaluationStep / maxD);
            br[1] = (float) (gateDist[i] / currentEvaluationStep / maxD);
            for(int j = 0 ; j < foxDist.size() ; j++) {
                br[2 + j] = (float) (foxDist.get(j)[i] / currentEvaluationStep / maxD);
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
