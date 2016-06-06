/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.spec;

import ec.EvolutionState;
import ec.Singleton;
import java.util.List;
import mase.evaluation.BehaviourResult;

/**
 *
 * @author jorge
 */
public interface DistanceCalculator extends Singleton {
    
    public static final String P_BASE = "distance";

    public double[][] computeDistances(List<BehaviourResult>[] list, EvolutionState state);
    
}
