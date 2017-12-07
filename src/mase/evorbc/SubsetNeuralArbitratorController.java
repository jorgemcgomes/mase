/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import mase.controllers.AgentController;
import mase.evorbc.Repertoire.Primitive;
import mase.util.FormatUtils;

/**
 *
 * @author jorge
 */
public class SubsetNeuralArbitratorController implements AgentController {
    
    private static final long serialVersionUID = 1L;
    private final AgentController arbitrator;
    private final Primitive[] primitives;
    private Primitive lastPrimitive;
    private int winnerIndex;
    private double[] arbitratorOut;

    public SubsetNeuralArbitratorController(AgentController arbitrator, Primitive[] primitives) {
        this.arbitrator = arbitrator;
        this.primitives = primitives;
    }

    @Override
    public double[] processInputs(double[] input) {
        arbitratorOut = arbitrator.processInputs(input);
        winnerIndex = 0;
        for(int i = 1 ; i < arbitratorOut.length ; i++) {
            if(arbitratorOut[i] > arbitratorOut[winnerIndex]) {
                winnerIndex = i;
            }
        }
        Primitive chosen = primitives[winnerIndex];
        if(lastPrimitive == null || lastPrimitive != chosen) {
            lastPrimitive = chosen;
            lastPrimitive.ac.reset();
        }
        return lastPrimitive.ac.processInputs(input);
    }

    @Override
    public void reset() {
        arbitrator.reset();
        lastPrimitive = null;
    }

    @Override
    public AgentController clone() {
        Primitive[] copy = new Primitive[primitives.length];
        for(int i = 0 ; i < primitives.length ; i++) {
            copy[i] = primitives[i].clone();
        }
        return new SubsetNeuralArbitratorController(arbitrator.clone(), copy);
    }
    
    @Override
    public String toString() {
        String str = arbitrator.toString() + "\n";
        for(int i = 0 ; i < primitives.length ; i++) {
            str += "["+ i + "] " + primitives[i] + "\n";
        }
        return str;
    }    
    
    public String getActivationsReport() {
        return FormatUtils.toStringRounded(arbitratorOut);
    }
    
    public String getActivationsPlot() {
        String plot = "\n";
        // bars
        for(double y = 0.9 ; y >= 0 ; y=y-0.1) {
            for(int i = 0 ; i < arbitratorOut.length ; i++) {
                plot += arbitratorOut[i] > y ? (i == winnerIndex ?"\u2588 " : "\u2593 ") : "\u2591 ";
            }
            plot +="\n";
        }
        
        // index label
        for(int i = 0 ; i < arbitratorOut.length ; i++) {
            plot += String.format("%2d", i);
        }
        return plot + "\n";
    }
    
    public double[] getLastArbitratorActivations() {
        return arbitratorOut;
    }
    
    public Primitive getLastPrimitive() {
        return lastPrimitive;
    }
    
    public Primitive[] getPrimitiveSubset() {
        return primitives;
    }
    
}
