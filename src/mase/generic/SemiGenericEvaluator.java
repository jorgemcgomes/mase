/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import mase.EvaluationResult;
import mase.mason.EmboddiedAgent;
import mase.mason.MaseSimState;
import mase.mason.MasonEvaluation;
import net.jafama.FastMath;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

/**
 *
 * @author jorge
 */
public class SemiGenericEvaluator extends MasonEvaluation {

    protected SemiGenericResult vbr;
    protected List<EmboddiedAgent[]> agentGroups;
    protected List<EnvironmentFeature> environmentFeatures;
    protected Map<EmboddiedAgent, Double2D> lastPosition;
    protected Map<EmboddiedAgent[], Double2D> lastCentreMass;
    private float[] features;
    private int[] norm;
    private int size;

    @Override
    protected void preSimulation() {
        this.agentGroups = new LinkedList<EmboddiedAgent[]>();
        this.environmentFeatures = new LinkedList<EnvironmentFeature>();
        this.features = new float[100]; // TODO: it should be possible to calculate the exact number
        this.norm = new int[100];
        this.lastCentreMass = new HashMap<EmboddiedAgent[], Double2D>();
        this.lastPosition = new HashMap<EmboddiedAgent, Double2D>();
    }

    protected void addEnvironmentFeature(EnvironmentFeature ef) {
        environmentFeatures.add(ef);
    }

    protected void addAgentGroup(EmboddiedAgent[] group) {
        agentGroups.add(group);
        // initial centre mass
        MutableDouble2D centreMass = new MutableDouble2D();
        int al = 0;
        for (EmboddiedAgent a : group) {
            if (a.isAlive()) {
                centreMass.addIn(a.getLocation());
                al++;
            }
            lastPosition.put(a, a.getLocation());
        }
        centreMass.multiplyIn(1.0 / al);
        lastCentreMass.put(group, new Double2D(centreMass));
    }

    @Override
    protected void evaluate() {
        if(!((MaseSimState) sim).continueSimulation()) {
            return;
        }
        
        int index = 0;
        for (int gi = 0; gi < agentGroups.size(); gi++) {
            EmboddiedAgent[] g = agentGroups.get(gi);
            // Percentage of alive agents
            int al = 0;
            for (EmboddiedAgent a : g) {
                if (a.isAlive()) {
                    al++;
                }
            }
            features[index] += al / (float) g.length;
            norm[index]++;
            index++;
            if (g.length > 1) {
                MutableDouble2D centreMass = new MutableDouble2D();
                if (al > 0) {
                    // Calculate centre of mass
                    for (EmboddiedAgent a : g) {
                        if (a.isAlive()) {
                            centreMass.addIn(a.getLocation());
                        }
                    }
                    centreMass.multiplyIn(1.0 / al);

                    // Average movement of the centre of mass
                    features[index] += centreMass.distance(lastCentreMass.get(g));
                    lastCentreMass.put(g, new Double2D(centreMass));
                    norm[index]++;
                }
                index++;

                if (al > 1) {
                    // Average distance to centre of mass
                    double avgDisp = 0;
                    for (EmboddiedAgent a : g) {
                        if (a.isAlive()) {
                            avgDisp += centreMass.distance(a.getLocation());
                        }
                    }
                    avgDisp /= al;
                    features[index] += avgDisp;
                    norm[index]++;
                }
                index++;
            }

            // Individual agent movement
            for (EmboddiedAgent a : g) {
                if (a.isAlive()) {
                    features[index] += lastPosition.get(a).distance(a.getLocation());
                    lastPosition.put(a, a.getLocation());
                    norm[index]++;
                }
            }
            index++;

            for (EnvironmentFeature ef : environmentFeatures) {
                // average distance of the agents to the environment feature
                float avgDist = 0;
                int alive = 0;
                for (EmboddiedAgent a : g) {
                    if (a.isAlive()) {
                        avgDist += ef.distanceTo(a);
                        alive++;
                    }
                }
                if (alive > 0) {
                    features[index] += avgDist / alive;
                    norm[index]++;
                }
                index++;
            }

            // average distance of the agents to the other agents
            for (int go = gi + 1; go < agentGroups.size(); go++) {
                EmboddiedAgent[] otherG = agentGroups.get(go);
                double dist = 0;
                int count = 0;
                for (EmboddiedAgent a : g) {
                    if (a.isAlive()) {
                        for (EmboddiedAgent otherA : otherG) {
                            if (otherA.isAlive()) {
                                dist += a.distanceTo(otherA);
                                count++;
                            }
                        }
                    }
                }
                if (count > 0) {
                    features[index] += dist / count;
                    norm[index]++;
                }
                index++;
            }
        }
        size = index;
    }

    @Override
    protected void postSimulation() {
        for (int i = 0; i < size; i++) {
            features[i] = norm[i] > 0 ? features[i] / norm[i] : 0;
        }
        features[size] = sim.schedule.getSteps();
        float[] newVec = Arrays.copyOf(features, size + 1);
        vbr = new SemiGenericResult(newVec);
    }

    @Override
    public EvaluationResult getResult() {
        return vbr;
    }

    public interface EnvironmentFeature {

        double distanceTo(EmboddiedAgent ag);
    }

    public static class AgentFeature implements EnvironmentFeature {

        private EmboddiedAgent ag;

        public AgentFeature(EmboddiedAgent ag) {
            this.ag = ag;
        }

        @Override
        public double distanceTo(EmboddiedAgent other) {
            return ag.distanceTo(other);
        }
    }

    public static class PointFeature implements EnvironmentFeature {

        private Double2D point;
        private double radius;

        public PointFeature(Double2D point) {
            this(point, 0);
        }

        public PointFeature(Double2D point, double radius) {
            this.point = point;
            this.radius = radius;
        }

        @Override
        public double distanceTo(EmboddiedAgent ag) {
            return Math.max(0, ag.getLocation().distance(point) - radius - ag.getRadius());
        }
    }

    public static class PolygonFeature implements EnvironmentFeature {

        private final Double2D[] segStarts, segEnds;

        public PolygonFeature(Double2D[] segStarts, Double2D[] segEnds) {
            this.segStarts = segStarts;
            this.segEnds = segEnds;
        }

        @Override
        public double distanceTo(EmboddiedAgent ag) {
            double min = Double.POSITIVE_INFINITY;
            for (int i = 0; i < segStarts.length; i++) {
                double d = distToSegment(ag.getLocation(), segStarts[i], segEnds[i]);
                min = Math.min(min, Math.max(0, d - ag.getRadius()));
            }
            return min;
        }

        // Return minimum distance between line segment vw and point p
        private double distToSegment(Double2D p, Double2D v, Double2D w) {

            double l2 = FastMath.pow2(v.x - w.x) + FastMath.pow2(v.y - w.y);
            if (l2 == 0.0) {
                return p.distance(v);   // v == w case
            }
            // Consider the line extending the segment, parameterized as v + t (w - v).
            // We find projection of point p onto the line. 
            // It falls where t = [(p-v) . (w-v)] / |w-v|^2
            double t = p.subtract(v).dot(w.subtract(v)) / l2;
            if (t < 0.0) {
                return p.distance(v); // Beyond the 'v' end of the segment
            } else if (t > 1.0) {
                return p.distance(w);  // Beyond the 'w' end of the segment
            }
            Double2D projection = v.add((w.subtract(v)).multiply(t));  // Projection falls on the segment
            return p.distance(projection);
        }
    }

    public static class CompositeFeature implements EnvironmentFeature {

        private final EnvironmentFeature[] features;

        public CompositeFeature(EnvironmentFeature[] features) {
            this.features = features;
        }

        @Override
        public double distanceTo(EmboddiedAgent ag) {
            double min = Double.POSITIVE_INFINITY;
            for (EnvironmentFeature ef : features) {
                min = Math.min(min, ef.distanceTo(ag));
            }
            return min;
        }
    }
}
