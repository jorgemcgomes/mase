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
import mase.mason.MasonSimState;
import org.apache.commons.math3.util.MathArrays;

/**
 * Number of captured rocks of each type.
 * Amount of time each actuator was active.
 * Average distance to the closest rock.
 * Average distance to the closest neighbor.
 * @author jorge
 */
public class MultiRoverIndEval extends MasonEvaluation {

    private static final long serialVersionUID = 1L;
    
    private SubpopEvaluationResult br;
    private int[][] actuatorTime;
    private double[] distanceToRock;
    private double[] distanceToNeighbor;

    @Override
    public EvaluationResult getResult() {
        return br;
    }

    @Override
    protected void preSimulation(MasonSimState sim) {
        super.preSimulation(null);
        MultiRover mr = (MultiRover) sim;
        actuatorTime = new int[mr.rovers.size()][mr.par.numActuators];        
        distanceToRock = new double[mr.rovers.size()];
        distanceToNeighbor = new double[mr.rovers.size()];
    }

    @Override
    protected void evaluate(MasonSimState sim) {
        MultiRover mr = (MultiRover) sim;
        for (int i = 0 ; i < mr.rovers.size() ; i++) {
            Rover r = mr.rovers.get(i);
            
            // Distance to closest rock
            double min = Double.POSITIVE_INFINITY;
            for(Rock rock : mr.rocks) {
                min = Math.min(min, rock.distanceTo(r));
            }
            distanceToRock[i] += min;
            
            // Distance to closest rover
            min = Double.POSITIVE_INFINITY;
            for(Rover other : mr.rovers) {
                if(r != other) {
                    min = Math.min(min, r.distanceTo(other));
                }
            }
            distanceToNeighbor[i] += min;
                        
            // Amount of time each actuator was active
            for(int a = 0 ; a < actuatorTime.length ; a++) {
                if(r.getActuatorType() == a) {
                    actuatorTime[i][a]++;
                }
            }
        }
    }
    
    @Override
    protected void postSimulation(MasonSimState sim) {
        MultiRover mr = (MultiRover) sim;
        VectorBehaviourResult[] res = new VectorBehaviourResult[mr.rovers.size()];
        
        for(int i = 0 ; i < mr.rovers.size() ; i++) {
            Rover r = mr.rovers.get(i);
            
            // Number of rocks of each type captured
            double[] scores = new double[r.captured.length];
            for(int t = 0 ; t < scores.length ; t++) {
                scores[t] = (double) r.captured[t] / mr.par.usedTypesFrequency.get(t) * mr.par.numAgents;
            }
            
            // Time each actuator was active
            double[] tt = new double[actuatorTime[i].length];
            for(int j = 0 ; j < tt.length ; j++) {
                tt[j] = (double) actuatorTime[i][j] / currentEvaluationStep;
            }
            
            // Distance to closest rock
            double dRock = distanceToRock[i] / currentEvaluationStep / mr.field.width * 3;
            
            // Distance to closest rover
            double dRover = distanceToNeighbor[i] / currentEvaluationStep / mr.field.width * 3;
                        
            res[i] = new VectorBehaviourResult(MathArrays.concatenate(scores, tt, new double[]{dRock, dRover}));
        }
        this.br = new SubpopEvaluationResult(res);
    }    
}
