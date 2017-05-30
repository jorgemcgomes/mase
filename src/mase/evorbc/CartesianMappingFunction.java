/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author jorge
 */
public class CartesianMappingFunction implements MappingFunction {

    private static final long serialVersionUID = 1L;
    private final double[] min, range;
    
    public CartesianMappingFunction(Pair<Double,Double>[] bounds) {
        this.min = new double[bounds.length];
        this.range = new double[bounds.length];
        for(int i = 0 ; i < bounds.length ; i++) {
            min[i] = bounds[i].getLeft();
            range[i] = bounds[i].getRight() - bounds[i].getLeft();
        }
    }
    
    @Override
    /**
     * Assumes that arbitratorOutput is in the range [0,1]
     */
    public double[] outputToCoordinates(double[] arbOutput) {
        double[] t = new double[arbOutput.length];
        for(int i = 0 ; i < arbOutput.length ; i++) {
            t[i] = arbOutput[i] * range[i] + min[i];
        }
        return t;
    }
    
}
