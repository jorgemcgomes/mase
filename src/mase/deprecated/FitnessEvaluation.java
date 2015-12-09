/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import ec.EvolutionState;
import ec.util.Parameter;
import static mase.Evaluation.P_COMBINATION;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public abstract class FitnessEvaluation extends MasonEvaluation {

    public static final String P_MAX = "max", P_MIN = "min", P_ARITHMETIC = "arithmetic", P_HARMONIC = "harmonic";
    public static final int MAX = 0, MIN = 1, ARITHMETIC = 2, HARMONIC = 3;
    public static final float FLOAT_THRESHOLD = 0.0001f;
    protected int combination;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        super.setup(state, base);
        String str = state.parameters.getStringWithDefault(base.push(P_COMBINATION), defaultBase().push(P_COMBINATION), P_ARITHMETIC);
        if(str.equalsIgnoreCase(P_MAX)) {
            combination = MAX;
        } else if(str.equalsIgnoreCase(P_MIN)) {
            combination = MIN;
        } else if(str.equalsIgnoreCase(P_ARITHMETIC)) {
            combination = ARITHMETIC;
        } else if(str.equalsIgnoreCase(P_HARMONIC)) {
            combination = HARMONIC;
        } else {
            state.output.fatal("Unknow parameter value: " + str, base.push(P_COMBINATION), defaultBase().push(P_COMBINATION));
        }
    }

    @Override
    public Object mergeEvaluations(Object[] results) {
        float score = 0;
        switch (combination) {
            case MAX:
                score = Float.NEGATIVE_INFINITY;
                for (Object f : results) {
                    score = Math.max(score, (Float) f);
                }
                break;
            case MIN:
                score = Float.POSITIVE_INFINITY;
                for (Object f : results) {
                    score = Math.min(score, (Float) f);
                }
                break;
            case ARITHMETIC:
                for (Object f : results) {
                    score += (Float) f;
                }
                score /= results.length;
                break;
            case HARMONIC:
                for (Object f : results) {
                    Float val = (Float) f;
                    if(val > -FLOAT_THRESHOLD && val < FLOAT_THRESHOLD) {
                        score += 1 / FLOAT_THRESHOLD;
                    } else {
                        score += 1/ val;
                    }
                }
                score = results.length / score;
                break;
        }
        return score;
    }

    @Override
    public abstract Float getResult();
}
