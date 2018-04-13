/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.neat;

import java.util.ArrayList;
import org.apache.commons.lang3.ArrayUtils;
import org.neat4j.neat.core.NEATChromosome;
import org.neat4j.neat.core.NEATFeatureGene;
import org.neat4j.neat.core.NEATLinkGene;
import org.neat4j.neat.core.NEATNetDescriptor;
import org.neat4j.neat.core.NEATNeuralNet;
import org.neat4j.neat.core.NEATNodeGene;
import org.neat4j.neat.ga.core.Gene;

/**
 *
 * @author jorge
 */
public class NEATSerializer {

    public static final double NODE = 0d, LINK = 1d, FEATURE = 2d;

    public static double[] serializeToArray(NEATNeuralNet net) {
        NEATNetDescriptor descr = (NEATNetDescriptor) net.netDescriptor();
        NEATChromosome chromo = (NEATChromosome) descr.neatStructure();
        Gene[] genes = chromo.genes();
        ArrayList<Double> res = new ArrayList<>();
        for (Gene gene : genes) {
            if (gene instanceof NEATNodeGene) {
                NEATNodeGene neatGene = (NEATNodeGene) gene;
                res.add(NODE);
                res.add((double) neatGene.id());
                res.add(neatGene.sigmoidFactor());
                res.add((double) neatGene.getType());
                res.add(neatGene.bias());
            } else if (gene instanceof NEATLinkGene) {
                NEATLinkGene neatGene = (NEATLinkGene) gene;
                res.add(LINK);
                res.add(neatGene.isEnabled() ? 1d : 0d);
                res.add((double) neatGene.getFromId());
                res.add((double) neatGene.getToId());
                res.add(neatGene.getWeight());
            } else if(gene instanceof NEATFeatureGene) {
                NEATFeatureGene neatGene = (NEATFeatureGene) gene;
                res.add(FEATURE);
                res.add(neatGene.geneAsNumber().doubleValue());
            }
        }
        Double[] array = new Double[res.size()];
        res.toArray(array);
        return ArrayUtils.toPrimitive(array);
    }

    public static NEATNeuralNet deserialize(double[] weights) {
        ArrayList<Gene> genes = new ArrayList<>();
        int inputCount = 0;
        for (int i = 0; i < weights.length; ) {
            double type = weights[i++];
            if (type == NODE) {
                int id = (int) (double) weights[i++];
                double sigF = weights[i++];
                int t = (int) (double) weights[i++];
                double bias = weights[i++];
                genes.add(new NEATNodeGene(0, id, sigF, t, bias));
                if(t == NEATNodeGene.INPUT) {
                    inputCount++;
                }
            } else if (type == LINK) {
                boolean enabled = weights[i++] == 1d;
                int from = (int) (double) weights[i++];
                int to = (int) (double) weights[i++];
                double weight = weights[i++];
                genes.add(new NEATLinkGene(0, enabled, from, to, weight));
            } else if(type == FEATURE) {
                double v = weights[i++];
                genes.add(new NEATFeatureGene(0, v));
            }
        }
        Gene[] geneArray = new Gene[genes.size()];
        genes.toArray(geneArray);
        NEATChromosome chromo = new NEATChromosome(geneArray);
        NEATNetDescriptor descr = new NEATNetDescriptor(inputCount, null);
        descr.updateStructure(chromo);
        NEATNeuralNet network = new NEATNeuralNet();
        network.createNetStructure(descr);
        network.updateNetStructure();
        return network;
    }
    
    public static String serializeToString(NEATNeuralNet net) {
        double[] array = serializeToArray(net);
        StringBuilder str = new StringBuilder();
        str.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            str.append(",").append(array[i]);
        }
        return str.toString();
    }

    public static NEATNeuralNet deserialize(String ser) {
        String[] split = ser.split(",");
        double[] stuff = new double[split.length];
        for (int i = 0; i < stuff.length; i++) {
            stuff[i] = Double.parseDouble(split[i]);
        }
        return deserialize(stuff);
    }

}
