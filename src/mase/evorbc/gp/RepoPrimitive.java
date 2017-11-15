/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc.gp;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.ERC;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.util.Parameter;
import java.util.Collection;
import java.util.Iterator;
import mase.MaseProblem;
import mase.evorbc.ArbitratorFactory;
import mase.evorbc.CartesianMappingFunction;
import mase.evorbc.KdTreeRepertoire;
import mase.evorbc.Repertoire;
import mase.evorbc.Repertoire.Primitive;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author jorge
 */
public class RepoPrimitive extends ERC {
    
    private static final long serialVersionUID = 1L;
    public static final String P_MUTATION_SD = "mutation-sd";
    public static final String V_RESET = "reset";
    
    private int primitive;
    private double sd;
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        if (state.parameters.getString(base.push(P_MUTATION_SD), defaultBase().push(P_MUTATION_SD)).equalsIgnoreCase(V_RESET)) {
            sd = Double.NaN;
        } else {
            sd = state.parameters.getDouble(base.push(P_MUTATION_SD), defaultBase().push(P_MUTATION_SD));
        }
    }
    
    @Override
    public void resetNode(EvolutionState state, int thread) {
        MaseProblem p = (MaseProblem) state.evaluator.p_problem;
        Repertoire repo = ((ArbitratorFactory) p.getControllerFactory()).getRepertoire();

        // Pick one at random
        Collection<Primitive> allPrimitives = repo.allPrimitives();
        int n = allPrimitives.size();
        int rand = state.random[thread].nextInt(n);
        Iterator<Primitive> iter = allPrimitives.iterator();
        for (int i = 0; i < n; i++) {
            Primitive next = iter.next();
            if (i == rand) {
                this.primitive = next.id;
                return;
            }
        }
    }
    
    @Override
    public void mutateERC(EvolutionState state, int thread) {        
        if (Double.isNaN(sd)) {
            this.resetNode(state, thread);
        } else {
            MaseProblem p = (MaseProblem) state.evaluator.p_problem;
            KdTreeRepertoire repo = (KdTreeRepertoire) ((ArbitratorFactory) p.getControllerFactory()).getRepertoire();
            CartesianMappingFunction map = (CartesianMappingFunction) ((ArbitratorFactory) p.getControllerFactory()).getMappingFunction();            
            Pair<double[], double[]> range = map.getRange();
            double[] coords = repo.getPrimitiveById(primitive).coordinates;
            double[] newCoords = new double[coords.length];
            for (int i = 0; i < newCoords.length; i++) {
                do {
                    // Try again until the coordinate falls within the boundaries of the repertoire
                    newCoords[i] = coords[i] + state.random[thread].nextGaussian() * sd * range.getRight()[i];
                } while (newCoords[i] < range.getLeft()[i] || newCoords[i] > range.getLeft()[i] + range.getRight()[i]);
            }
            this.primitive = repo.nearest(newCoords).id;
        }        
    }
    
    @Override
    public boolean nodeEquals(GPNode node) {
        return node instanceof RepoPrimitive && primitive == ((RepoPrimitive) node).primitive;
    }
    
    @Override
    public String toString() {
        return "#" + primitive;
    }
    
    @Override
    public String encode() {
        return toString();
    }
    
    @Override
    public String name() {
        return "P";
    }
    
    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
        Data d = (Data) input;
        d.primitive = primitive;
    }
    
}
