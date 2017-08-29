/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.neat;

import java.util.Map;
import org.neat4j.neat.core.NEATFitnessFunction;
import org.neat4j.neat.ga.core.Chromosome;

/**
 *
 * @author jorge
 */
public class PreEvaluatedFitnessFunction extends NEATFitnessFunction {
    private static final long serialVersionUID = 1L;

    private final Map<Chromosome, Double> scores;

    public PreEvaluatedFitnessFunction(Map<Chromosome, Double> scores) {
        super(null, null);
        this.scores = scores;
    }

    @Override
    public double evaluate(Chromosome genoType) {
        Double sc = scores.get(genoType);
        if (sc == null) {
            throw new RuntimeException("Genotype not previously evaluated!");
        } else {
            return sc;
        }
    }
}
