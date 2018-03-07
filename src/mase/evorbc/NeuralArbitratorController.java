/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import mase.controllers.AgentController;
import mase.evorbc.Repertoire.Primitive;
import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.ProvidesInspector;

/**
 *
 * @author jorge
 */
public class NeuralArbitratorController implements AgentController, ProvidesInspector {

    private static final long serialVersionUID = 1L;

    private AgentController arbitrator;
    private Repertoire repo;
    private MappingFunction mapFun;
    private int frequency = 1;
    private transient int ticks = 0;
    private transient LinkedList<double[]> sensorHistory;
    public transient Primitive lastPrimitive;
    public transient double[] lastArbitratorOutput;
    public transient double[] lastRepertoireCoords;

    public NeuralArbitratorController() {
        this(null, null, null);
    }

    public NeuralArbitratorController(AgentController arbitrator, Repertoire repo, MappingFunction fun) {
        this.arbitrator = arbitrator;
        this.repo = repo;
        this.mapFun = fun;
    }
    
    public void setFrequency(int freq) {
        this.frequency = freq;
    }

    /**
     * Assumes that the values of the keys are in the range [0,1]
     *
     * @param repo
     */
    public void setRepertoire(Repertoire repo) {
        this.repo = repo;
    }

    public Repertoire getRepertoire() {
        return repo;
    }

    public void setArbitrator(AgentController arbitrator) {
        this.arbitrator = arbitrator;
    }

    public void setMappingFunction(MappingFunction mapFun) {
        this.mapFun = mapFun;
    }

    @Override
    public double[] processInputs(double[] input) {
        // Update the sensor history if needed for a low-frequency arbitrator
        if(frequency > 1) {
            sensorHistory.addLast(Arrays.copyOf(input, input.length));
            if(sensorHistory.size() > frequency) {
                sensorHistory.removeFirst();
            }
        }
        if(lastPrimitive == null || ticks % frequency == 0) {
            if(frequency > 1) {
                // Take the decision based on the history of sensor values
                lastArbitratorOutput = arbitrator.processInputs(averagedInput());
            } else {
                // Take the decision based on the current sensor values
                lastArbitratorOutput = arbitrator.processInputs(input);
            }
            
            lastRepertoireCoords = mapFun.outputToCoordinates(lastArbitratorOutput);

            Primitive primitive = repo.nearest(lastRepertoireCoords);
            if (lastPrimitive == null || primitive != lastPrimitive) {
                primitive.ac.reset();
                lastPrimitive = primitive;
            }            
        }
        //System.out.println(ticks + "  " + frequency);
        ticks++;
        double[] out = lastPrimitive.ac.processInputs(input);
        //System.out.println(FormatUtils.toStringRounded(input) + " -> " + FormatUtils.toStringRounded(lastArbitratorOutput) + " -> " + FormatUtils.toStringRounded(lastRepertoireCoords) + " -> "+ lastPrimitive.id + " -> " + FormatUtils.toStringRounded(out));
        return out;
    }
    
    private double[] averagedInput() {
        double weightSum = 0;
        double weight = 1;
        double[] average = new double[sensorHistory.getFirst().length];
        for(double[] sensorValues : sensorHistory) {
            for(int i = 0 ; i < average.length ; i++) {
                average[i] += sensorValues[i] * weight;
            }
            weightSum += weight;
            weight++;
        }
        for(int i = 0 ; i < average.length ; i++) {
            average[i] /= weightSum;
        }
        return average;
    }

    @Override
    public void reset() {
        arbitrator.reset();
        lastPrimitive = null;
        sensorHistory = new LinkedList<>();
        this.ticks = 0;
    }

    @Override
    public AgentController clone() {
        NeuralArbitratorController copy = new NeuralArbitratorController(arbitrator.clone(), repo.deepCopy(), mapFun);
        copy.setFrequency(frequency);
        return copy;
    }

    @Override
    public String toString() {
        return arbitrator.toString() + "\n" + repo.toString() + "\n" + mapFun.toString();
    }

    public Primitive getLastPrimitive() {
        return lastPrimitive;
    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
        RepertoireInspector insp = new RepertoireInspector(400, 7) {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawAxis(g);
                drawPrimitives(repo.allPrimitives(), Color.GRAY, g);
                if(lastPrimitive != null) {
                    highlightPrimitive(lastPrimitive, g);
                    markSpot(lastRepertoireCoords, g);
                }
            }
        };
        insp.setBounds(repo.allPrimitives());
        return insp;
    }

}
