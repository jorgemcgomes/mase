/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.deprecated;

import edu.wlu.cs.levy.CG.KDTree;
import java.text.DecimalFormat;
import java.util.ArrayList;
import net.jafama.FastMath;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class KDTreeBenchmark {

    private final int nSamples, sampleLength, nLookups, nTests, nearestK;
    private static DecimalFormat df =  new DecimalFormat("0.000");

    public KDTreeBenchmark(int nSamples, int sampleLength, int nLookups, int nearestK, int nTests) {
        this.nSamples = nSamples;
        this.sampleLength = sampleLength;
        this.nLookups = nLookups;
        this.nTests = nTests;
        this.nearestK = nearestK;
    }

    public void run() throws Exception {
        System.out.println("Num samples:" + nSamples + "\t| Length: " + sampleLength + "\t| Num lookups: " + nLookups + "\t| Num tests: " + nTests);

        DescriptiveStatistics addTime = new DescriptiveStatistics();
        DescriptiveStatistics removeTime = new DescriptiveStatistics();
        DescriptiveStatistics lookupTimeKD = new DescriptiveStatistics();
        DescriptiveStatistics lookupTimeBF = new DescriptiveStatistics();

        for (int x = 0; x < nTests; x++) {
            // dataset creation
            ArrayList<double[]> clusters = new ArrayList<double[]>(nSamples);
            for (int i = 0; i < nSamples; i++) {
                clusters.add(generateOne(sampleLength));
            }
            ArrayList<double[]> candidates = new ArrayList<double[]>(nLookups);
            for (int i = 0; i < nLookups; i++) {
                candidates.add(generateOne(sampleLength));
            }

            // insert bench
            long start = System.currentTimeMillis();
            KDTree<double[]> tree = new KDTree<double[]>(sampleLength);
            for (double[] e : clusters) {
                tree.insert(e, e);
            }
            long end = System.currentTimeMillis();
            addTime.addValue(end - start);

            // tree NN bench
            start = System.currentTimeMillis();
            for (double[] c : candidates) {
                tree.nearest(c, nearestK);
            }
            end = System.currentTimeMillis();
            lookupTimeKD.addValue(end - start);

            // brute force NN bench
            start = System.currentTimeMillis();
            for (double[] e : candidates) {
                for (double[] cl : clusters) {
                    double d = distance(e, cl);
                }
            }
            end = System.currentTimeMillis();
            lookupTimeBF.addValue(end - start);

            // remove bench
            start = System.currentTimeMillis();
            for (double[] c : clusters) {
                tree.delete(c);
            }
            end = System.currentTimeMillis();
            removeTime.addValue(end - start);
        }
        
        System.out.println("Imp: " + lookupTimeBF.getMean() / lookupTimeKD.getMean() + "\tNN-KD: " + lookupTimeKD.getMean() + "\tNN-BF: " + 
                lookupTimeBF.getMean() + "\tAdd: " + addTime.getMean() + "\tRemove: " + removeTime.getMean());
    }

    double[] generateOne(int size) {
        double[] b = new double[size];
        for (int i = 0; i < b.length; i++) {
            b[i] = Math.random();
        }
        return b;
    }

    double distance(double[] a, double[] b) {
        double d = 0;
        for (int i = 0; i < a.length; i++) {
            d += FastMath.pow(a[i] - b[i], 2);
        }
        return d;
    }

    public static void main(String[] args) throws Exception {
        /*int[] nSamples = new int[]{25,50,100};
        int[] sampleLength = new int[]{10,20,50};
        int[] nLookups = new int[]{5000,10000,100000};

        for (int ns : nSamples) {
            for (int sl : sampleLength) {
                for (int nl : nLookups) {
                    KDTreeBenchmark bench = new KDTreeBenchmark(ns, sl, nl, 1, 5);
                    bench.run();
                }
            }
        }*/
        
        int[] repoSize = new int[]{100, 500, 1000, 2500};
        int[] length = new int[]{4, 20, 50, 100};
        for(int l : length) {
            for(int s : repoSize) {
                new KDTreeBenchmark(s, l, s, 15, 5).run();
            }
        }
    }
}
