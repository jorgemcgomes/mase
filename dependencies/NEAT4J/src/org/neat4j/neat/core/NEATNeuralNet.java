/*
 * Created on 22-Jun-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.neat4j.neat.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.neat4j.neat.data.core.NetworkInput;
import org.neat4j.neat.data.core.NetworkOutputSet;
import org.neat4j.neat.ga.core.Chromosome;
import org.neat4j.neat.ga.core.Gene;
import org.neat4j.neat.nn.core.ActivationFunction;
import org.neat4j.neat.nn.core.NeuralNet;
import org.neat4j.neat.nn.core.NeuralNetDescriptor;
import org.neat4j.neat.nn.core.NeuralNetLayer;
import org.neat4j.neat.nn.core.Neuron;
import org.neat4j.neat.nn.core.Synapse;
import org.neat4j.neat.nn.core.functions.LinearFunction;
import org.neat4j.neat.nn.core.functions.SigmoidFunction;
import org.neat4j.neat.nn.core.functions.TanhFunction;

/**
 * @author MSimmerson
 *
 * The NEAT Neural Network
 */
public class NEATNeuralNet implements NeuralNet {
    
        private static final long serialVersionUID = -1L;
	private NEATNetDescriptor descriptor;
	private transient Synapse[] connections;
	private transient NEATNeuron[] neurons;
        private transient NEATNeuron[] outputNeurons;
        private transient double[] activations;
	
	public NEATNeuron[] neurons() {
		return (this.neurons);
	}
        
        public Synapse[] connections() {
            return connections;
        }
	
	/**
	 * Exercises the network for the given input data set
	 */
	public NetworkOutputSet execute(NetworkInput netInput) {
		NEATNetOutputSet opSet;
		double[] outputs;
		int i;
		// trawl through the graph bacwards from each output node
		if (outputNeurons.length == 0) {
			System.err.println("No output neurons");
		}
                
                Arrays.fill(activations, Double.NaN);
                        
		outputs = new double[outputNeurons.length];
		for (i = 0; i < outputs.length; i++) {
			outputs[i] = this.neuronOutput((NEATNeuron)outputNeurons[i], netInput);
		}
		
		opSet = new NEATNetOutputSet();
		opSet.addNetworkOutput(new NEATNetOutput(outputs));
		return (opSet);
	}
	
	public NEATNeuron[] outputNeurons() {
            return outputNeurons;
	}
	
	private double neuronOutput(NEATNeuron neuron, NetworkInput netInput) {
		double output = 0;
		double[] inputPattern;
		// find its inputs
		NEATNeuron[] sourceNodes = neuron.sourceNeurons();
		int i;
		
		if (neuron.neuronType() == NEATNodeGene.INPUT) {
			inputPattern = new double[1];
			// match the input column to the input node, id's start from 1
			inputPattern[0] = netInput.pattern()[neuron.id() - 1];
		} else {
			inputPattern = new double[sourceNodes.length];
			for (i = 0; i < sourceNodes.length; i++) {
				if (neuron.id() == sourceNodes[i].id()) {				
					// Self Recurrent
					inputPattern[i] = neuron.lastActivation();
				} else if (neuron.neuronDepth() > sourceNodes[i].neuronDepth()) {
					// Recurrent
					inputPattern[i] = sourceNodes[i].lastActivation();
				} else {
                                        double act = activations[sourceNodes[i].auxIndex];
                                        inputPattern[i] = Double.isNaN(act) ? 
                                                this.neuronOutput(sourceNodes[i], netInput) : act;                                            
				}
			}
		}
		output = neuron.activate(inputPattern);
                activations[neuron.auxIndex] = output;
		return (output);
	}

	/**
	 * Generates a neural network structure based on the network descriptor
	 *
	 */
	public void updateNetStructure() {
		// use descriptor's chromo to create net 
		Chromosome netStructure = this.descriptor.neatStructure();
		ArrayList nodes = new ArrayList();
		ArrayList links = new ArrayList();
		Gene[] genes = netStructure.genes();
		int i;
		
		for (i = 0; i < netStructure.size(); i++) {
			if (genes[i] instanceof NEATNodeGene) {					
				nodes.add(genes[i]);
			} else if (genes[i] instanceof NEATLinkGene) {	
				if (((NEATLinkGene)genes[i]).isEnabled()) {
					// only add enabled links to the net structure
					links.add(genes[i]);
				}
			}
		}
		
		this.connections = this.createLinks(links, this.createNeurons(nodes));
                
                ArrayList<NEATNeuron> outputNeuronsList = new ArrayList();
		for (i = 0; i < this.neurons.length; i++) {
			if (this.neurons[i].neuronType() == NEATNodeGene.OUTPUT) {
				outputNeuronsList.add(this.neurons[i]);
			}
		}
                this.outputNeurons = new NEATNeuron[outputNeuronsList.size()];
                outputNeuronsList.toArray(this.outputNeurons);
		this.assignNeuronDepth(outputNeurons, 0);
                
                activations = new double[neurons.length];
                for(i = 0 ; i < neurons.length ; i++) {
                    neurons[i].auxIndex = i;
                    activations[i] = Double.NaN;
                }                
	}
	
	private void assignNeuronDepth(NEATNeuron[] neurons, int depth) {
		int i;
		NEATNeuron neuron;
		for (i = 0; i < neurons.length; i++) {
			neuron = (NEATNeuron)neurons[i];
			if (neuron.neuronType() == NEATNodeGene.OUTPUT) {
				if (neuron.neuronDepth() == -1) {
					neuron.setNeuronDepth(depth);
					this.assignNeuronDepth(neuron.sourceNeurons(), depth + 1);
				}
			} else if (neuron.neuronType() == NEATNodeGene.HIDDEN) {
				if (neuron.neuronDepth() == -1) {
					neuron.setNeuronDepth(depth);
					this.assignNeuronDepth(neuron.sourceNeurons(), depth + 1);				
				}
			} else if (neuron.neuronType() == NEATNodeGene.INPUT) {
				neuron.setNeuronDepth(Integer.MAX_VALUE);
			}
		}
	}
	
	private NEATNeuron[] createNeurons(ArrayList nodes) {
		this.neurons = new NEATNeuron[nodes.size()];
		NEATNodeGene gene;
		int i;
		
		for (i = 0; i < neurons.length; i++) {
			gene = (NEATNodeGene)nodes.get(i);
			this.neurons[i] = new NEATNeuron(this.createActivationFunction(gene), gene.id(), gene.getType());
			this.neurons[i].modifyBias(gene.bias(), 0, true);
		}

		return (neurons);
	}
	
	private ActivationFunction createActivationFunction(NEATNodeGene gene) {
		ActivationFunction function = null;
		// inputs are passed through
		if (gene.getType() == NEATNodeGene.INPUT) {
			function = new LinearFunction();
		} else if (gene.getType() == NEATNodeGene.OUTPUT){
			function = new SigmoidFunction(gene.sigmoidFactor());
		} else {
			function = new TanhFunction();
		}
		
		return (function);		
	}
	
	private Synapse[] createLinks(ArrayList links, NEATNeuron[] neurons) {
		NEATLinkGene gene;
		Synapse[] synapses = new Synapse[links.size()];
		int i;
		NEATNeuron from;
		NEATNeuron to;
		
		for (i = 0; i < links.size(); i++) {
			gene = (NEATLinkGene)links.get(i);
			from = this.findNeuronById(neurons, gene.getFromId());
			to = this.findNeuronById(neurons, gene.getToId());
			to.addSourceNeuron(from);
			synapses[i] = new Synapse(from, to, gene.getWeight());
			synapses[i].setEnabled(gene.isEnabled());
			to.addIncomingSynapse(synapses[i]);
		}
		
		return (synapses);
	}
	
	private NEATNeuron findNeuronById(NEATNeuron[] neurons, int id) {
		boolean found = false;
		NEATNeuron neuron = null;
		int i = 0;
		
		while (!found) {
			if (neurons[i].id() == id) {
				neuron = neurons[i];
				found = true;
			} else {
				i++;
			}
		}
		
		return (neuron);
	}
	
	/**
	 * Updates the internal network structure
	 */
	public void createNetStructure(NeuralNetDescriptor descriptor) {
		this.descriptor = (NEATNetDescriptor)descriptor;
	}

	public NeuralNetDescriptor netDescriptor() {
		return (this.descriptor);
	}

	public Collection hiddenLayers() {
		return null;
	}

	public NeuralNetLayer outputLayer() {
		return null;
	}

	public void seedNet(double[] weights) {
	}

	public int requiredWeightCount() {
		return 0;
	}

	public int netID() {
		return 0;
	}

	public Neuron neuronAt(int x, int y) {
		return null;
	}
}
