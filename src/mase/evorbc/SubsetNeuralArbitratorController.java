/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map.Entry;
import mase.controllers.AgentController;
import mase.evorbc.Repertoire.Primitive;
import mase.util.FormatUtils;
import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.ProvidesInspector;

/**
 *
 * @author jorge
 */
public class SubsetNeuralArbitratorController implements AgentController, ProvidesInspector {

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
        for (int i = 1; i < arbitratorOut.length; i++) {
            if (arbitratorOut[i] > arbitratorOut[winnerIndex]) {
                winnerIndex = i;
            }
        }
        Primitive chosen = primitives[winnerIndex];
        if (lastPrimitive == null || lastPrimitive != chosen) {
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
        for (int i = 0; i < primitives.length; i++) {
            copy[i] = primitives[i].clone();
        }
        return new SubsetNeuralArbitratorController(arbitrator.clone(), copy);
    }

    @Override
    public String toString() {
        String str = arbitrator.toString() + "\n";
        for (int i = 0; i < primitives.length; i++) {
            str += "[" + i + "] " + primitives[i] + "\n";
        }
        return str;
    }

    public String getActivationsReport() {
        return FormatUtils.toStringRounded(arbitratorOut);
    }

    public String getActivationsPlot() {
        String plot = "\n";
        // bars
        for (double y = 0.9; y >= 0; y = y - 0.1) {
            for (int i = 0; i < arbitratorOut.length; i++) {
                plot += arbitratorOut[i] > y ? (i == winnerIndex ? "\u2588 " : "\u2593 ") : "\u2591 ";
            }
            plot += "\n";
        }

        // index label
        for (int i = 0; i < arbitratorOut.length; i++) {
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

    @Override
    public Inspector provideInspector(GUIState state, String name) {
        RepertoireInspector insp = new RepertoireInspector(400, 7) {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                super.drawAxis(g);

                // Find the maximum activation values for each primitive
                // The same primitive can be assigned to multiple outputs...
                // This is to avoid over-plotting
                HashMap<Integer, Double> maxAct = new HashMap<>();
                HashMap<Integer, Primitive> pKey = new HashMap<>();
                if (arbitratorOut != null) {

                    for (int i = 0; i < primitives.length; i++) {
                        if (!maxAct.containsKey(primitives[i].id)) {
                            maxAct.put(primitives[i].id, arbitratorOut[i]);
                        } else {
                            maxAct.put(primitives[i].id, Math.max(maxAct.get(primitives[i].id), arbitratorOut[i]));
                        }
                        pKey.put(primitives[i].id, primitives[i]);
                    }
                }

                // Draw the primitives and their activation levels
                if (!maxAct.isEmpty()) {
                    for (Entry<Integer, Double> e : maxAct.entrySet()) {
                        float f = 0.8f - (float) (double) e.getValue() * 0.8f;
                        Color c = new Color(f, f, f);
                        String act = String.format("%.3f", e.getValue());
                        g.setColor(Color.BLACK);
                        Primitive p = pKey.get(e.getKey());
                        g.drawChars(act.toCharArray(), 0, act.length(), tx(p.coordinates[0]) + 5, ty(p.coordinates[1]));
                        drawPrimitive(p, c, g);
                    }

                    for (int i = 0; i < primitives.length; i++) {

                    }
                    highlightPrimitive(lastPrimitive, g);
                }
            }
        };
        insp.setRepertoireBounds(-20d, 20d, -14d, 14d);
        //insp.setBounds(Arrays.asList(primitives));
        return insp;
    }

}
