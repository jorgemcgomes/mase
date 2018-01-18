/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import java.awt.Graphics;
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
        lastArbitratorOutput = arbitrator.processInputs(input);
        double[] output = lastArbitratorOutput;
        lastRepertoireCoords = mapFun.outputToCoordinates(output);

        Primitive primitive = repo.nearest(lastRepertoireCoords);
        if (lastPrimitive == null || primitive != lastPrimitive) {
            primitive.ac.reset();
            lastPrimitive = primitive;
            //System.out.println(primitive.id);
        }

        double[] out = lastPrimitive.ac.processInputs(input);

        return out;
    }

    @Override
    public void reset() {
        arbitrator.reset();
        lastPrimitive = null;
    }

    @Override
    public AgentController clone() {
        return new NeuralArbitratorController(arbitrator.clone(), repo.deepCopy(), mapFun);
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
                drawPrimitives(repo.allPrimitives(), g);
                if(lastPrimitive != null) {
                    highlightPrimitive(lastPrimitive, g);
                    markSpot(lastRepertoireCoords[0], lastRepertoireCoords[1], g);
                }
            }
        };
        insp.setBounds(repo.allPrimitives());
        return insp;
    }

}
