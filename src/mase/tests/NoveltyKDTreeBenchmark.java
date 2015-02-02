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
public class NoveltyKDTreeBenchmark {

    private final int SIZE, N, L;

    public NoveltyKDTreeBenchmark(int size, int n, int l) {
        this.SIZE = size;
        this.N = n;
        this.L = l;
    }

    public void run() throws Exception {
        System.out.printf("%6d\t%4d\t%4d",SIZE,N,L);
        
        // Generate dataset
        ArrayList<double[]> elements = new ArrayList<double[]>(SIZE);
        for (int i = 0; i < SIZE; i++) {
            elements.add(generateOne(L));
        }
        
        // Insert benchmark
        long start = System.currentTimeMillis();
        KDTree<double[]> tree = new KDTree<double[]>(L);
        for (int i = 0; i < SIZE; i++) {
            tree.insert(elements.get(i), elements.get(i));
        }
        long end = System.currentTimeMillis();
        System.out.printf("\t%6d",end - start);

        // NN bench
        //ArrayList<Double> treeDists = new ArrayList<Double>(SIZE);
        //ArrayList<Double> normalDists = new ArrayList<Double>(SIZE);

        // KD NN bench
        start = System.currentTimeMillis();
        for (double[] e : elements) {
            double d = 0;
            List<double[]> nearest = tree.nearest(e, N);
            for (double[] n : nearest) {
                d += distance(e, n);
            }
            //treeDists.add(d);
        }
        end = System.currentTimeMillis();
        long kdTime = end - start;
        System.out.printf("\t%6d",kdTime);

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
            //normalDists.add(d);
        }
        end = System.currentTimeMillis();
        long bfTime = end - start;
        System.out.printf("\t%6d",bfTime);
        System.out.printf("\t%6.2f",(float)bfTime/kdTime);

        // KD remove bench
        start = System.currentTimeMillis();
        for (int i = 0; i < SIZE; i++) {
            tree.delete(elements.get(i));
        }
        end = System.currentTimeMillis();
        System.out.printf("\t%6d",end - start);
        System.out.println();
        
        // check results
        /*double maxError = Double.NEGATIVE_INFINITY;
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
        System.out.print("\t" + maxError + "\n");*/
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
Size  	   N	   L	KD_ins	 KD_nn	 BF_nn	 Factor	KD_rem
   200	   1	   2	     5	   514	    27	  0.05	     0
   200	   1	  20	     1	     8	    69	  8.63	     1
   200	  15	   2	     0	     5	    11	  2.20	     0
   200	  15	  20	     2	    20	    68	  3.40	     0
   200	 200	   2	     0	    23	    14	  0.61	     0
   200	 200	  20	     0	    81	    62	  0.77	     0
  1000	   1	   2	     0	     5	   253	 50.60	     0
  1000	   1	  20	     1	    18	  1509	 83.83	     0
  1000	  15	   2	     1	    12	   251	 20.92	     0
  1000	  15	  20	     0	   257	  1521	  5.92	     0
  1000	 200	   2	     0	   100	   248	  2.48	     0
  1000	 200	  20	     1	   542	  1504	  2.77	     0
  5000	   1	   2	     2	    16	  6740	421.25	     1
  5000	   1	  20	     1	    95	 37619	395.99	     2
  5000	  15	   2	     1	    73	  6921	 94.81	     1
  5000	  15	  20	     2	  5019	 37616	  7.49	     1
  5000	 200	   2	     2	   552	  6717	 12.17	     1
  5000	 200	  20	     1	  6723	 37826	  5.63	     1
    */
    
    public static void main(String[] args) throws Exception {

        int[] sizes = new int[]{200};
        int[] ns = new int[]{1,15,50,100,150,200};
        int[] ls = new int[]{2,20};

        System.out.println("Size  \t   N\t   L\tKD_ins\t KD_nn\t BF_nn\t Factor\tKD_rem");
        for (int s : sizes) {
            for (int n : ns) {
                for (int l : ls) {
                    NoveltyKDTreeBenchmark bench = new NoveltyKDTreeBenchmark(s, n, l);
                    bench.run();
                }
            }
        }
    }
}
