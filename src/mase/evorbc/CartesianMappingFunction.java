/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import ec.EvolutionState;
import ec.util.Parameter;
import mase.evorbc.Repertoire.Primitive;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author jorge
 */
public class CartesianMappingFunction implements MappingFunction {

    private static final long serialVersionUID = 1L;
    private double[] min, range;

    @Override
    public void setup(EvolutionState state, Parameter base) {
    }

    @Override
    public void additionalSetup(EvolutionState state, Repertoire rep) {
        int n = rep.allPrimitives().iterator().next().coordinates.length;
        this.min = new double[n];
        this.range = new double[n];
        for (int i = 0; i < n; i++) {
            double mi = Double.POSITIVE_INFINITY;
            double ma = Double.NEGATIVE_INFINITY;
            for (Primitive p : rep.allPrimitives()) {
                double[] c = p.coordinates;
                mi = Math.min(mi, c[i]);
                ma = Math.max(ma, c[i]);
            }
            min[i] = mi;
            range[i] = ma - mi;
        }       
    }

    @Override
    /**
     * Assumes that arbitratorOutput is in the range [0,1]
     */
    public double[] outputToCoordinates(double[] arbOutput) {
        double[] t = new double[arbOutput.length];
        for (int i = 0; i < arbOutput.length; i++) {
            t[i] = arbOutput[i] * range[i] + min[i];
        }
        return t;
    }
    
    /**
     * @return Minimum values for each dimension, and range for each dimension
     */
    public Pair<double[],double[]> getRange() {
        return Pair.of(min, range);
    }

}
