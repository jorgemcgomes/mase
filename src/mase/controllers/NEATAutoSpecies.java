/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.controllers;

import ec.EvolutionState;
import ec.Individual;
import ec.neat.NEATDefaults;
import ec.neat.NEATGene;
import ec.neat.NEATIndividual;
import ec.neat.NEATInnovation;
import ec.neat.NEATNode;
import ec.neat.NEATSpecies;
import ec.util.Parameter;
import ec.vector.Gene;
import java.util.ArrayList;

/**
 *
 * @author jorge
 */
public class NEATAutoSpecies extends NEATSpecies {

    private static final long serialVersionUID = 1L;

    public static final String P_INPUTS = "inputs";
    public static final String P_OUTPUTS = "outputs";
    private int inputs, outputs;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        inputs = state.parameters.getInt(base.push(P_INPUTS), NEATDefaults.base().push(P_INPUTS));
        outputs = state.parameters.getInt(base.push(P_OUTPUTS), NEATDefaults.base().push(P_OUTPUTS));
    }

    @Override
    /**
     * Creates a new individual with all inputs connected to all outputs, and no
     * hidden
     */
    public Individual newIndividual(EvolutionState state, int thread) {
        NEATIndividual ind = (NEATIndividual) super.newIndividual(state, thread);

        // Create nodes
        ArrayList<NEATNode> nodes = new ArrayList<>();
        int id = 1;
        // bias
        NEATNode node = (NEATNode) super.nodePrototype.clone();
        node.reset(NEATNode.NodeType.SENSOR, id++, NEATNode.NodePlace.BIAS);
        nodes.add(node);
        // inputs
        for (int i = 0; i < inputs; i++) {
            node = (NEATNode) super.nodePrototype.clone();
            node.reset(NEATNode.NodeType.SENSOR, id++, NEATNode.NodePlace.INPUT);
            nodes.add(node);
        }
        // outputs
        for (int i = 0; i < outputs; i++) {
            node = (NEATNode) super.nodePrototype.clone();
            node.reset(NEATNode.NodeType.NEURON, id++, NEATNode.NodePlace.OUTPUT);
            nodes.add(node);
        }

        // Create connections
        ArrayList<Gene> genes = new ArrayList<>();
        for (int out = nodes.size() - outputs; out < nodes.size(); out++) {
            for (int in = 0; in < nodes.size() - outputs; in++) {
                NEATInnovation inno = (NEATInnovation) super.innovationPrototype.clone();
                inno.reset(nodes.get(in).nodeId, nodes.get(out).nodeId, 0, 1, false);
                int innovNr;
                if(hasInnovation(inno)) {
                    NEATInnovation innovation = super.getInnovation(inno);
                    innovNr = innovation.innovationNum1;
                } else { // create and add innovation
                    innovNr = nextInnovationNumber();
                    inno.innovationNum1 = innovNr;
                    addInnovation(inno);                    
                }
                NEATGene g = (NEATGene) this.genePrototype.clone();
                g.reset(0, nodes.get(in).nodeId, nodes.get(out).nodeId, false, innovNr, 0);
                genes.add(g);
            }
        }
        ind.reset(nodes, genes);
        return ind;
    }
}
