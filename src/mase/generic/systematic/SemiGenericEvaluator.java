/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic.systematic;

import ec.EvolutionState;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mase.evaluation.EvaluationResult;
import mase.mason.MaseSimState;
import mase.mason.MasonEvaluation;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class SemiGenericEvaluator extends MasonEvaluation {

    public static final String P_TIME_MODE = "time-mode";
    public static final String P_TIME_FRAMES = "time-frames";

    public enum TimeMode {

        mean, simplereg, meanslope ,frames
    }

    public static final int MAX_FEATURES = 100;
    public static final int INITIAL_SIZE = 1000;

    protected SemiGenericResult vbr;
    protected AgentGroup[] agentGroups;
    protected EnvironmentalFeature[] envFeatures;
    protected Map<Agent, Double2D> lastPosition;
    protected Map<AgentGroup, Double2D> lastCentreMass;

    protected List<List<Double>> features;
    protected int size;

    protected TimeMode timeMode;
    protected int timeFrames;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.timeMode = TimeMode.valueOf(state.parameters.getString(base.push(P_TIME_MODE), null));
        if (timeMode == TimeMode.frames) {
            this.timeFrames = state.parameters.getInt(base.push(P_TIME_FRAMES), null);
        }
    }

    @Override
    protected void preSimulation() {
        TaskDescription td = (TaskDescription) sim;
        this.agentGroups = td.getAgentGroups();
        this.envFeatures = td.getEnvironmentalFeatures();
        this.features = new ArrayList<List<Double>>(MAX_FEATURES);
        for (int i = 0; i < MAX_FEATURES; i++) {
            features.add(new ArrayList(INITIAL_SIZE));
        }

        // init positions
        this.lastCentreMass = new HashMap<AgentGroup, Double2D>();
        this.lastPosition = new HashMap<Agent, Double2D>();
        for (AgentGroup ag : agentGroups) {
            lastCentreMass.put(ag, ag.getCentreOfMass());
            for (Agent a : ag) {
                lastPosition.put(a, a.getPosition());
            }
        }
    }

    @Override
    protected void evaluate() {
        if (!((MaseSimState) sim).continueSimulation()) {
            return;
        }

        int index = 0;
        for (int gi = 0; gi < agentGroups.length; gi++) {
            AgentGroup ag = agentGroups[gi];

            // Percentage of alive agents
            int al = ag.countAlive();
            features.get(index++).add(al / (double) ag.size());

            Double2D centreMass = ag.getCentreOfMass();

            // If the AgentGroup is really a group
            if (ag.size() > 1) {
                // If the group is alive
                if (al > 0) {
                    // Movement of the centre of mass
                    features.get(index++).add(centreMass.distance(lastCentreMass.get(ag)));
                    lastCentreMass.put(ag, centreMass);
                } else {
                    features.get(index++).add(Double.NaN);
                }

                // If the group has more than one alive
                if (al > 1) {
                    // Dispersion
                    double avgDisp = 0;
                    for (Agent a : ag) {
                        if (a.isAlive()) {
                            avgDisp += centreMass.distance(a.getPosition());
                        }
                    }
                    features.get(index++).add(avgDisp / al);
                } else {
                    features.get(index++).add(Double.NaN);
                }
            }

            // If the group is alive
            if (al > 0) {
                // Position of the centre of mass
                features.get(index++).add(centreMass.x);
                features.get(index++).add(centreMass.y);

                // Average individual agent movement
                double avgMov = 0;
                for (Agent a : ag) {
                    if (a.isAlive()) {
                        Double2D pos = a.getPosition();
                        avgMov += lastPosition.get(a).distance(pos);
                        lastPosition.put(a, pos);
                    }
                }
                features.get(index++).add(avgMov / al);

                // Average agent state
                double[] averageState = ag.getAverageState();
                for (int i = 0; i < averageState.length; i++) {
                    features.get(index++).add(averageState[i]);
                }

                // Distance to the environment features
                for (EnvironmentalFeature ef : envFeatures) {
                    double dist = 0;
                    for (Agent a : ag) {
                        dist += ef.distanceTo(a.getPosition());
                    }
                    features.get(index++).add(dist / al);
                }

                // Distance to other agent groups
                for (int go = gi + 1; go < agentGroups.length; go++) {
                    AgentGroup otherG = agentGroups[go];
                    double dist = ag.distanceToGroup(otherG);
                    if (!Double.isNaN(dist)) {
                        features.get(index++).add(dist);
                    } else {
                        features.get(index++).add(Double.NaN);
                    }
                }
            } else {
                int toAdd = 2 + 1 + ag.get(0).getStateVariables().length + envFeatures.length + (agentGroups.length - gi - 1);
                for (int i = 0; i < toAdd; i++) {
                    features.get(index++).add(Double.NaN);
                }
            }
        }

        // Environmental features state
        for (EnvironmentalFeature ef : envFeatures) {
            double[] state = ef.getStateVariables();
            for (int i = 0; i < state.length; i++) {
                features.get(index++).add(state[i]);
            }
        }

        size = index;
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
            vbr = new SemiGenericResult(res);
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
                
                if(timeMode == TimeMode.simplereg) {
                    res[i * 2] = (float) (reg.getN() >= 2 ? reg.getIntercept() : 0);
                } else if(timeMode == TimeMode.meanslope) {
                    res[i * 2] = (float) (reg.getN() > 0 ? sum / reg.getN() : 0);
                }
                res[i * 2 + 1] = (float) (reg.getN() >= 2 ? reg.getSlope() : 0);
            }
            res[size * 2] = sim.schedule.getSteps();
            vbr = new SemiGenericResult(res);
        } else if (timeMode == TimeMode.frames) {
            int len = features.get(0).size();
            int frameLen = (int) Math.ceil((double) len / timeFrames);

            // DEBUG
            for (int i = 0 ; i < size ; i++) {
                if (features.get(i).size() != len) {
                    System.out.println("DIFFERENT FEATURE LENGTHS. Expected: " + len + " Got: " + features.get(i).size());
                    for(List<Double> sample : features) {
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
                    //System.out.println(t + " " + i + " " + (t*size + i) );
                    res[t*size + i] = (float) (count == 0 ? 0 : sum / count);
                }
            }
            res[size * timeFrames] = sim.schedule.getSteps();
            vbr = new SemiGenericResult(res);
        }
    }

    @Override
    public EvaluationResult getResult() {
        return vbr;
    }

}
