/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.tests;

import net.jafama.FastMath;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.encog.mathutil.BoundMath;

/**
 *
 * @author jorge
 */
public class FastMathTest {

    public static void main(String[] args) {
        double MIN = -10;
        double MAX = 10;
        int N = 10000;
        DescriptiveStatistics diff = new DescriptiveStatistics();
        DescriptiveStatistics diffJava = new DescriptiveStatistics();
        long tFast = 0, tNormal = 0, tBounded = 0, tJava = 0;
        for (int i = 0; i < N; i++) {
            double x = Math.random() * (MAX - MIN) + MIN;
            long t = System.nanoTime();
            double v1 = (1.0 / (1.0 + FastMath.expQuick(-1 * x)));
            tFast += System.nanoTime() - t;
            t = System.nanoTime();
            double v2 = (1.0 / (1.0 + FastMath.exp(-1 * x)));
            tNormal += System.nanoTime() - t;
            t = System.nanoTime();
            double v3 = (1.0 / (1.0 + BoundMath.exp(-1 * x)));
            tBounded += System.nanoTime() - t;
            t = System.nanoTime();
            double v4 = (1.0 / (1.0 + Math.exp(-1 * x)));
            tJava += System.nanoTime() - t;
            diff.addValue(Math.abs(v1 - v2));
            diffJava.addValue(Math.abs(v3 - v1));
        }        

        System.out.println("MAX: " + diff.getMax());
        System.out.println("MEAN: " + diff.getMean());
        System.out.println("MAX JAVA: " + diffJava.getMax());
        System.out.println("MEAN JAVA: " + diffJava.getMean());
        
        System.out.println("Fast: " + tFast);
        System.out.println("Normal: " + tNormal);
        System.out.println("Bounded: " + tBounded);
        System.out.println("Java: " + tJava);
    }

}
