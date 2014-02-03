/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic.systematic;

import ec.EvolutionState;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mase.evaluation.EvaluationResult;
import mase.mason.MaseSimState;
import mase.mason.MasonEvaluation;
import net.jafama.FastMath;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 *
 * @author jorge
 */
public class SystematicEvaluator extends MasonEvaluation {

    @Override
    public Parameter defaultBase() {
        return new Parameter(P_SYSTEMATIC_BASE);
    }

    public static final String P_TIME_MODE = "time-mode";
    public static final String P_TIME_FRAMES = "time-frames";
    public static final String P_NUM_ALIVE = "num-alive";
    public static final String P_STATE_MEAN = "state-mean";
    public static final String P_STATE_DEVIATION = "state-deviation";
    public static final String P_PHYSICAL_RELATIONS = "physical-relations";
    public static final String P_SYSTEMATIC_BASE = "systematic";
    public static final String P_IGNORE_GROUPS = "ignore-groups";

    public enum TimeMode {

        mean, simplereg, meanslope, frames
    }

    public static final int MAX_FEATURES = 100;
    public static final int INITIAL_SIZE = 1000;

    protected SystematicResult vbr;
    protected TaskDescription td;

    protected List<List<Double>> features;
    protected int size = -1;

    protected TimeMode timeMode;
    protected int timeFrames;

    protected boolean[] evaluateGroup;
    protected boolean numAlive, stateMean, stateDeviation, physicalRelations;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        Parameter df = defaultBase();
        this.timeMode = TimeMode.valueOf(state.parameters.getString(base.push(P_TIME_MODE), df.push(P_TIME_MODE)));
        if (timeMode == TimeMode.frames) {
            this.timeFrames = state.parameters.getInt(base.push(P_TIME_FRAMES), df.push(P_TIME_FRAMES));
        }
        this.numAlive = state.parameters.getBoolean(base.push(P_NUM_ALIVE), df.push(P_NUM_ALIVE), false);
        this.stateMean = state.parameters.getBoolean(base.push(P_STATE_MEAN), df.push(P_STATE_MEAN), false);
        this.stateDeviation = state.parameters.getBoolean(base.push(P_STATE_DEVIATION), df.push(P_STATE_DEVIATION), false);
        this.physicalRelations = state.parameters.getBoolean(base.push(P_PHYSICAL_RELATIONS), df.push(P_PHYSICAL_RELATIONS), false);

        String s = state.parameters.getStringWithDefault(base.push(P_IGNORE_GROUPS), df.push(P_IGNORE_GROUPS), "");
        this.evaluateGroup = new boolean[100];
        Arrays.fill(evaluateGroup, true);
        for (String sp : s.split(",")) {
            try {
                int index = Integer.parseInt(sp.trim());
                evaluateGroup[index] = false;
                state.output.message("Ignoring group " + index);
            } catch (Exception ex) {
            }
        }
    }

    @Override
    protected void preSimulation() {
        this.td = ((TaskDescriptionProvider) sim).getTaskDescription();

        this.features = new ArrayList<List<Double>>(MAX_FEATURES);
        for (int i = 0; i < MAX_FEATURES; i++) {
            features.add(new ArrayList(INITIAL_SIZE));
        }
    }

    @Override
    protected void evaluate() {
        int index = 0;
        for (int gi = 0; gi < td.groups().length; gi++) {
            EntityGroup eg = td.groups()[gi];
            if (evaluateGroup[gi]) {
                // Percentage of alive agents
                if (numAlive && eg.getMaxSize() > eg.getMinSize()) {
                    features.get(index++).add((double) (eg.size() - eg.getMinSize())
                            / (eg.getMaxSize() - eg.getMinSize()));
                }

                // Average state of the group -- if none alive it is filled with NaN
                double[] averageState = eg.getAverageState();
                if (stateMean) {
                    for (int i = 0; i < averageState.length; i++) {
                        features.get(index++).add(averageState[i]);
                    }
                }

                // StateVariables dispersion -- average difference to the mean value
                if (stateDeviation && eg.getMaxSize() > 1) {
                    // If the group has more than one alive
                    if (eg.size() > 1) {
                        double[] diffs = new double[averageState.length];
                        for (Entity e : eg) {
                            double[] vars = e.getStateVariables();
                            for (int i = 0; i < averageState.length; i++) {
                                diffs[i] += FastMath.pow2(averageState[i] - vars[i]);
                            }
                        }
                        for (int i = 0; i < diffs.length; i++) {
                            double sd = FastMath.sqrtQuick(diffs[i] / eg.size());
                            features.get(index++).add(sd);
                        }
                    } else {
                        for (int i = 0; i < 1 + averageState.length; i++) {
                            features.get(index++).add(Double.NaN);
                        }
                    }
                }

                // Physical dispersion -- mean distance of each entity to the other entities
                if (physicalRelations && td.distanceFunction() != null && eg.getMaxSize() > 1 && !eg.isStatic()) {
                    double sumDists = 0;
                    int count = 0;
                    for (int i = 0; i < eg.size(); i++) {
                        for (int j = i + 1; j < eg.size(); j++) {
                            sumDists += td.distanceFunction().distance(eg.get(i), eg.get(j));
                            count++;
                        }
                    }
                    features.get(index++).add(count > 0 ? sumDists / count : Double.NaN);
                }
            }

            // Relations with other entities -- physical distances
            if (physicalRelations && td.distanceFunction() != null) {
                for (int go = gi + 1; go < td.groups().length; go++) {
                    EntityGroup otherG = td.groups()[go];
                    if ((!eg.isStatic() || !otherG.isStatic()) && (evaluateGroup[gi] || evaluateGroup[go])) {
                        features.get(index++).add(td.distanceFunction().distance(eg, otherG));
                    }
                }
            }
        }
        if (size == -1) {
            size = index;
        } else if (size != index) {
            System.out.println("Incoherent size! Expected: " + size + " Got: " + index);
        }
    }

    @Override
    protected void postSimulation() {
        if (timeMode == TimeMode.mean) {
            // Make averages
            float[] res = new float[size + 1];
            for (int i = 0; i < size; i++) {
                List<Double> featureSample = features.get(i);
                double sum = 0;
                int count = 0;
                for (Double d : featureSample) {
                    if (!Double.isNaN(d)) {
                        sum += d;
                        count++;
                    }
                }
                res[i] = (float) (count == 0 ? 0 : sum / count);
            }
            res[size] = sim.schedule.getSteps();
            vbr = new SystematicResult(res);
        } else if (timeMode == TimeMode.simplereg || timeMode == TimeMode.meanslope) {
            SimpleRegression reg = new SimpleRegression(true);
            float[] res = new float[size * 2 + 1];
            for (int i = 0; i < size; i++) {
                reg.clear();
                double sum = 0;
                List<Double> featureSample = features.get(i);
                for (int j = 0; j < featureSample.size(); j++) {
                    double d = featureSample.get(j);
                    if (!Double.isNaN(d)) {
                        reg.addData(j, d);
                        sum += d;
                    }
                }

                if (timeMode == TimeMode.simplereg) {
                    res[i * 2] = (float) (reg.getN() >= 2 ? reg.getIntercept() : 0);
                } else if (timeMode == TimeMode.meanslope) {
                    res[i * 2] = (float) (reg.getN() > 0 ? sum / reg.getN() : 0);
                }
                res[i * 2 + 1] = (float) (reg.getN() >= 2 ? reg.getSlope() : 0);
            }
            res[size * 2] = sim.schedule.getSteps();
            vbr = new SystematicResult(res);
        } else if (timeMode == TimeMode.frames) {
            int len = features.get(0).size();
            int frameLen = (int) Math.ceil((double) len / timeFrames);

            // DEBUG
            for (int i = 0; i < size; i++) {
                if (features.get(i).size() != len) {
                    System.out.println("DIFFERENT FEATURE LENGTHS. Expected: " + len + " Got: " + features.get(i).size());
                    for (List<Double> sample : features) {
                        System.out.print(sample.size() + " ");
                    }
                    System.out.println();
                }
            }

            float[] res = new float[size * timeFrames + 1];
            for (int t = 0; t < timeFrames; t++) {
                for (int i = 0; i < size; i++) {
                    double sum = 0;
                    int count = 0;
                    for (int x = t * frameLen; x < Math.min(len, (t + 1) * frameLen); x++) {
                        double d = features.get(i).get(x);
                        if (!Double.isNaN(d)) {
                            sum += d;
                            count++;
                        }
                    }
                    res[t * size + i] = (float) (count == 0 ? 0 : sum / count);
                }
            }
            res[size * timeFrames] = features.get(0).size();
            vbr = new SystematicResult(res);
        }
    }

    @Override
    public EvaluationResult getResult() {
        return vbr;
    }

}
