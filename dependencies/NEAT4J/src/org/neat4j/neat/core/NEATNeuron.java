/*
 * Created on 23-Jun-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.neat4j.neat.core;

import java.util.ArrayList;
import java.util.Arrays;

import org.neat4j.neat.nn.core.ActivationFunction;
import org.neat4j.neat.nn.core.Neuron;
import org.neat4j.neat.nn.core.Synapse;

/**
 * @author MSimmerson
 *
 * Specific NEAT neuron
 */
public class NEATNeuron implements Neuron {
        private static final long serialVersionUID = -4271581501033634190L;
    
	private double lastActivation;
	private double bias;
	private double[] weights;
	private ActivationFunction activationFunction;
	private int id;
	private int type;
	private int depth;
	private NEATNeuron[] sourceNeurons;
	private Synapse[] incomingSynapses;
	private boolean isInput = false;
        
        protected int auxIndex;

	public NEATNeuron(ActivationFunction function, int id, int type) {		
		this.activationFunction = function;
		this.id = id;
		this.type = type;
		this.sourceNeurons = new NEATNeuron[0];
		this.incomingSynapses = new Synapse[0];
		this.isInput = (type == NEATNodeGene.INPUT);
		this.depth = -1;
	}
	
	public void addSourceNeuron(NEATNeuron neuron) {
            sourceNeurons = Arrays.copyOf(sourceNeurons, sourceNeurons.length + 1);
            sourceNeurons[sourceNeurons.length - 1] = neuron;
	}
	
	public void addIncomingSynapse(Synapse synapse) {
            incomingSynapses = Arrays.copyOf(incomingSynapses, incomingSynapses.length + 1);
            incomingSynapses[incomingSynapses.length - 1] = synapse;
	}
	
	public Synapse[] incomingSynapses() {
		return (this.incomingSynapses);
	}
	
	public NEATNeuron[] sourceNeurons() {
		return (this.sourceNeurons);
	}
	
	public double lastActivation() {
		return (this.lastActivation);
	}
	
	/**
	 * If it is an input neuron, returns the input, else will run through the specified activation function.
	 * 
	 */
	public double activate(double[] nInputs) {
		double neuronIp = 0;
		int i = 0;
		double weight;
		double input;
		Synapse synapse;
		// acting as a bias neuron
		this.lastActivation = -1;

		if (!this.isInput) {			
			if (nInputs.length > 0) {
				for (i = 0; i < nInputs.length; i++) {
					input = nInputs[i];
					synapse = (Synapse)incomingSynapses[i];
					if (synapse.isEnabled()) {
						weight = synapse.getWeight();
						neuronIp += (input * weight);
					}
				}
				neuronIp += (-1 * this.bias);
				this.lastActivation = this.activationFunction.activate(neuronIp);
			}
		} else {
			//neuronIp = nInputs[0];
			this.lastActivation = nInputs[0];
		}
		
		return (this.lastActivation);
	}

	public ActivationFunction function() {
		return (this.activationFunction);
	}

	public void modifyWeights(double[] weightMods, double[] momentum, boolean mode) {
		System.arraycopy(weightMods, 0, this.weights, 0, this.weights.length);

	}

	public void modifyBias(double biasMod, double momentum, boolean mode) {
		this.bias = biasMod;
	}

	public double[] weights() {
		return (this.weights);
	}

	public double bias() {
		return (this.bias);
	}

	public double[] lastWeightDeltas() {
		return null;
	}

	public double lastBiasDelta() {
		return 0;
	}
	
	public int id() {
		return (this.id);
	}
	
	public int neuronType() {
		return (this.type);
	}
	
	public int neuronDepth() {
		return (this.depth);
	}

	public void setNeuronDepth(int depth) {
		this.depth = depth;
	}
}
