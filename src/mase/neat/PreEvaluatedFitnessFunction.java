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

    private final Map<Chromosome, Float> scores;

    public PreEvaluatedFitnessFunction(Map<Chromosome, Float> scores) {
        super(null, null);
        this.scores = scores;
    }

    @Override
    public double evaluate(Chromosome genoType) {
        Float sc = scores.get(genoType);
        if (sc == null) {
            throw new RuntimeException("Genotype not previously evaluated!");
        } else {
            return sc;
        }
    }
}
