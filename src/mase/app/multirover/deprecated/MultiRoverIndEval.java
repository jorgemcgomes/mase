/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.multirover;

import mase.evaluation.EvaluationResult;
import mase.evaluation.SubpopEvaluationResult;
import mase.evaluation.VectorBehaviourResult;
import mase.mason.MasonEvaluation;
import org.apache.commons.lang3.ArrayUtils;
import sim.util.Double2D;

/**
 * Total number of captured rocks
 * Mean distance to the nearest rock of each type
 * Amount of time each actuator was active
 * @author jorge
 */
public class MultiRoverIndEval extends MasonEvaluation {

    private static final long serialVersionUID = 1L;
    
    private SubpopEvaluationResult br;
    private double[][] nearestOfTypeDist;
    private int[][] nearestOfTypeCount;
    private int[][] actuatorTime;

    @Override
    public EvaluationResult getResult() {
        return br;
    }

    @Override
    protected void preSimulation() {
        super.preSimulation();
        MultiRover mr = (MultiRover) sim;
        
        nearestOfTypeDist = new double[mr.rovers.size()][mr.par.usedTypes.size()];
        nearestOfTypeCount = new int[mr.rovers.size()][mr.par.usedTypes.size()];
        actuatorTime = new int[mr.rovers.size()][mr.par.numActuators];        
    }

    @Override
    protected void postSimulation() {
        MultiRover mr = (MultiRover) sim;
        VectorBehaviourResult[] res = new VectorBehaviourResult[mr.rovers.size()];
        
        for(int i = 0 ; i < mr.rovers.size() ; i++) {
            Rover r = mr.rovers.get(i);
            double[] vals = new double[1];
            vals[0] = r.captured / (double) mr.par.rockDistribution.length;
            for(int j = 0 ; j < nearestOfTypeDist[i].length ; j++) {
                vals = ArrayUtils.add(vals, nearestOfTypeDist[i][j] / nearestOfTypeCount[i][j] / mr.field.width);
            }
            for(int time : actuatorTime[i]) {
                vals = ArrayUtils.add(vals, (double) time / currentEvaluationStep);
            }
            res[i] = new VectorBehaviourResult(vals);
        }
        this.br = new SubpopEvaluationResult(res);
    }

    @Override
    protected void evaluate() {
        MultiRover mr = (MultiRover) sim;
        for (int i = 0 ; i < mr.rovers.size() ; i++) {
            Rover r = mr.rovers.get(i);
            // Distance to closest rock of each type
            int index = 0;
            for(RockType type : mr.par.usedTypes) {
                double closest = Double.POSITIVE_INFINITY;
                for (Rock rock : mr.rocks) {
                    if(rock.getType() == type) {
                        Double2D p = mr.field.getObjectLocation(rock);
                        closest = Math.min(closest, r.distanceTo(p));
                    }
                }
                if(!Double.isInfinite(closest)) {
                    nearestOfTypeDist[i][index] += closest;
                    nearestOfTypeCount[i][index]++;
                }
                index++;
            }
            
            // Amount of time each actuator was active
            for(int a = 0 ; a < actuatorTime.length ; a++) {
                if(r.getActuatorType() == a) {
                    actuatorTime[i][a]++;
                }
            }
        }
    }
}
