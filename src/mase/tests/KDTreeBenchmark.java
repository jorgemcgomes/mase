/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.tests;

import edu.wlu.cs.levy.CG.KDTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class KDTreeBenchmark {

    private int SIZE, N, L;

    public KDTreeBenchmark(int size, int n, int l) {
        this.SIZE = size;
        this.N = n;
        this.L = l;
    }

    public void run() throws Exception {
        System.out.print("Size:" + SIZE + "\t| N: " + N + "\t| L: " + L);
        
        // insert bench
        ArrayList<double[]> elements = new ArrayList<double[]>(SIZE);
        for (int i = 0; i < SIZE; i++) {
            elements.add(generateOne(L));
        }
        long start = System.currentTimeMillis();
        KDTree<double[]> tree = new KDTree<double[]>(L);
        for (int i = 0; i < SIZE; i++) {
            tree.insert(elements.get(i), elements.get(i));
        }
        long end = System.currentTimeMillis();
        //System.out.println("Tree insert time: " + (end - start) / (double) SIZE);
        System.out.print("\t" + (end - start) / (double) SIZE);

        // NN bench
        ArrayList<Double> treeDists = new ArrayList<Double>(SIZE);
        ArrayList<Double> normalDists = new ArrayList<Double>(SIZE);

        // tree NN bench
        start = System.currentTimeMillis();
        for (double[] e : elements) {
            double d = 0;
            List<double[]> nearest = tree.nearest(e, N);
            for (double[] n : nearest) {
                d += distance(e, n);
            }
            treeDists.add(d);
        }
        end = System.currentTimeMillis();
        //System.out.println("Tree NN time: " + (end - start) / (double) SIZE);
        System.out.print("\t" + (end - start) / (double) SIZE);

        // normal NN bench
        start = System.currentTimeMillis();
        for (double[] e : elements) {
            ArrayList<Double> alldists = new ArrayList<Double>(elements.size());
            for (double[] n : elements) {
                alldists.add(distance(e, n));
            }
            Collections.sort(alldists);
            double d = 0;
            for (int i = 0; i < N; i++) {
                d += alldists.get(i);
            }
            normalDists.add(d);
        }
        end = System.currentTimeMillis();
        //System.out.println("Normal NN time: " + (end - start) / (double) SIZE);
        System.out.print("\t" + (end - start) / (double) SIZE);

        // remove bench
        start = System.currentTimeMillis();
        for (int i = 0; i < SIZE; i++) {
            tree.delete(elements.get(i));
        }
        end = System.currentTimeMillis();
        //System.out.println("Remove tree time: " + (end - start) / (double) SIZE);
        System.out.print("\t" + (end - start) / (double) SIZE);

        // check results
        double maxError = Double.NEGATIVE_INFINITY;
        double avgError = 0;
        double avgNormal = 0;
        double avgTree = 0;
        for (int i = 0; i < SIZE; i++) {
            double error = Math.abs(normalDists.get(i) - treeDists.get(i));
            avgError += error;
            maxError = Math.max(error, maxError);
            avgNormal += normalDists.get(i);
            avgTree += treeDists.get(i);
        }
        avgTree /= SIZE;
        avgNormal /= SIZE;
        avgError /= SIZE;
        //System.out.println("Max error: " + maxError + " | Avg error: " + avgError + " | Avg normal: " + avgNormal + " | Avg tree: " + avgTree);
        System.out.print("\t" + maxError + "\n");
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
            d += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(d);
    }
/*
Size:50 	| N: 15	| L: 10	0.01	0.46	0.18	0.0	5.329070518200751E-15
Size:50 	| N: 15	| L: 99	0.0	0.28	0.2	0.0	2.1316282072803006E-14
Size:100	| N: 15	| L: 10	0.01	0.17	0.13	0.0	3.552713678800501E-15
Size:100	| N: 15	| L: 99	0.0	0.18	0.24	0.01	2.1316282072803006E-14
Size:500	| N: 15	| L: 10	0.004	0.1	0.184	0.0	5.329070518200751E-15
Size:500	| N: 15	| L: 99	0.002	0.476	1.026	0.002	2.1316282072803006E-14
Size:1000	| N: 15	| L: 10	0.011	0.218	0.349	0.001	3.552713678800501E-15
Size:1000	| N: 15	| L: 99	0.001	0.833	2.015	0.001	2.1316282072803006E-14
Size:5000	| N: 15	| L: 10	6.0E-4	0.3516	1.569	6.0E-4	4.440892098500626E-15
Size:5000	| N: 15	| L: 99	0.001	3.8168	10.4168	0.001	2.8421709430404007E-14
Size:10000	| N: 15	| L: 10	3.0E-4	0.5267	3.2472	6.0E-4	4.440892098500626E-15
Size:10000	| N: 15	| L: 99	0.001	7.6298	21.7867	8.0E-4	2.8421709430404007E-14
Size:30000	| N: 15	| L: 10	3.33-4	0.9104	11.2682	3.66E-4	3.552713678800501E-15
Size:30000	| N: 15	| L: 99	0.0016	22.360	63.7509	0.00103	2.8421709430404007E-14 

 */
    public static void main(String[] args) throws Exception {

        int[] sizes = new int[]{1000};
        int[] ns = new int[]{15};
        int[] ls = new int[]{10,100,1000,10000,100000};

        for (int s : sizes) {
            for (int n : ns) {
                for (int l : ls) {
                    KDTreeBenchmark bench = new KDTreeBenchmark(s, n, l);
                    bench.run();
                }
            }
        }
    }
}
