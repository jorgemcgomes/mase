/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc.gp;

import ec.gp.GPNode;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import mase.evorbc.KdTreeRepertoire;
import mase.evorbc.Repertoire.Primitive;
import mase.stat.PersistentSolution;
import mase.stat.SolutionPersistence;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.Precision;

/**
 * WARNING: this class is highly tailored to the maze-decision tree experiments
 * It can and should be adapted to work more generally with all functions from this gp package
 * @author jorge
 */
public class DecisionTreeViz {
    
    private static final Map<Integer,String> PRIMITIVE_STRINGS = new HashMap<>();

    public static void main(String[] args) throws Exception {
        for (String a : args) {
            File f = new File(a);
            if (f.exists()) {
                PersistentSolution sol = SolutionPersistence.readSolutionFromFile(f);
                GPArbitratorController gpa = (GPArbitratorController) sol.getController().getAgentControllers(1)[0];
                System.out.println(f.getAbsolutePath() + "\n");
                System.out.println(makeGraphvizTree(gpa.getProgramTree().child, (KdTreeRepertoire) gpa.getRepertoire()));
            }
        }
    }

    // only works for 2D
    public static String makeGraphvizTree(GPNode root, KdTreeRepertoire repo) {
        Pair<double[], double[]> bounds = bounds(repo);
        
        // Linear mode
        // TODO: this can very easily be made generic for any number of dimensions
        for(Primitive p : repo.allPrimitives()) {
            String t1 = slider(p.coordinates[1], bounds.getLeft()[1], bounds.getRight()[1], 6);
            String t2 = slider(p.coordinates[0], bounds.getLeft()[0], bounds.getRight()[0], 6);
            PRIMITIVE_STRINGS.put(p.id, "L/R " + t1 + "\\nB/F " + t2);
        }
        
        // 2D Mode
        /*
        double res = 7;
        int c0 = (int) Math.round((bounds.getRight()[0] - bounds.getLeft()[0]) / res) + 1;
        int c1 = (int) Math.round((bounds.getRight()[1] - bounds.getLeft()[1]) / res) + 1;

        String[] baseRepo = new String[c1];
        for(int j = 0 ; j < c1 ; j++) {
            baseRepo[j] = StringUtils.repeat("\u2591", c0 * 2); // double to account for the width/height char difference
        }
        
        //for(Primitive p : repo.allPrimitives()) {
        //    int[] pos = position(p.coordinates, bounds, res);
        //    String l = baseRepo[pos[1]];
        //    baseRepo[pos[1]] = l.substring(0, pos[0]*2) + "\u2592\u2592" + l.substring(pos[0]*2 + 2, l.length());
        //} 
        //System.out.println(StringUtils.join(baseRepo, "\n"));
        
        for(Primitive p : repo.allPrimitives()) {
            String[] copy = Arrays.copyOf(baseRepo, baseRepo.length);
            int[] pos = position(p.coordinates, bounds, res);
            //System.out.println(Arrays.toString(p.coordinates) + " --> " + Arrays.toString(pos));
            String l = copy[pos[1]];
            copy[pos[1]] = l.substring(0, pos[0]*2) + "\u2588\u2588" + l.substring(pos[0]*2 + 2, l.length());
            //System.out.println(StringUtils.join(copy, "\n"));
            primitiveStrings.put(p.id, StringUtils.join(copy, "\\n"));
        }*/
        
        return "digraph g {\ngraph [ordering=out fontname = \"helvetica\"];\n"
                + "node [shape=rectangle fontname = \"helvetica\"];\n" 
                + "edge [fontname = \"helvetica\"];\n" +
                makeGraphvizSubtree("n", root) + "}\n";
    }
    
    private static int[] position(double[] coords, Pair<double[], double[]> bounds, double res) {
        int[] p = new int[coords.length];
        for(int i = 0 ; i < coords.length ; i++) {
            //p[i] = (int) Math.round(coords[i] / res)
            p[i] = (int) Math.round((coords[i] - bounds.getLeft()[i]) / res);
        }
        return p;
    }

    /**
     * Produces the inner code for a graphviz subtree. Called from
     * makeGraphvizTree(). Note that this isn't particularly efficient and
     * should only be used to generate occasional trees for display, not for
     * storing individuals or sending them over networks.
     */
    private static String makeGraphvizSubtree(String prefix, GPNode node) {
        String lab = "";
        GPNode[] showableChildren = new GPNode[0];
        String[] childrenLabels = new String[0];
        boolean terminal = false;
        if (node instanceof SensorLower) {
            SensorConstant c = (SensorConstant) node.children[0];
            double val = ((c.getValue() + 1) / 2) * 50; // scale to the range of the sensor (50)
            lab = node.toStringForHumans() + " < " + Precision.round(val, 2) + " ?";
            showableChildren = ArrayUtils.subarray(node.children, 1, 3);
            childrenLabels = new String[]{"Y", "N"};
        } else if (node instanceof SensorBinary) {
            lab = node.toStringForHumans() + "?";
            showableChildren = ArrayUtils.subarray(node.children, 0, 2);
            childrenLabels = new String[]{"Y", "N"};
        } else if (node instanceof RepoPrimitive) {
            int p = ((RepoPrimitive) node).getPrimitive();
            lab = node.toStringForHumans() + "\\n" + PRIMITIVE_STRINGS.get(p);
            terminal = true;
        }
        String body = prefix + "[label = \"" + lab + "\"" + (terminal ? "" : " style=rounded") + "];\n";
        for (int x = 0; x < showableChildren.length; x++) {
            String newprefix = x < 10 ? prefix + x : prefix + "n" + x;
            body = body + makeGraphvizSubtree(newprefix, showableChildren[x]);
            body = body + prefix + " -> " + newprefix + " [ label=\"" + childrenLabels[x] + "\" ];\n";
        }
        return body;
    }
    
    private static String slider(double v, double min, double max, int steps) {
        int pos = (int) Math.round((v - min) / (max - min) * steps); // scale to [0,steps]
        StringBuilder t = new StringBuilder();
        for(int i = 0 ; i < pos ; i++) {
            t.append("\u2591");
        }
        t.append("\u2588");
        for(int i = 0 ; i < steps - pos ; i++) {
            t.append("\u2591");
        }
        if(t.charAt(steps / 2)=='\u2591') {
            t.setCharAt(steps / 2, '\u2592');
        }
        return t.toString();
    }

    private static Pair<double[], double[]> bounds(KdTreeRepertoire rep) {
        int n = rep.allPrimitives().iterator().next().coordinates.length;
        double[] min = new double[n];
        double[] max = new double[n];
        Arrays.fill(min, Double.POSITIVE_INFINITY);
        Arrays.fill(max, Double.NEGATIVE_INFINITY);
        for (int i = 0; i < n; i++) {
            for (Primitive p : rep.allPrimitives()) {
                double[] c = p.coordinates;
                min[i] = Math.min(min[i], c[i]);
                max[i] = Math.max(max[i], c[i]);
            }
        }
        return Pair.of(min, max);
    }

}
