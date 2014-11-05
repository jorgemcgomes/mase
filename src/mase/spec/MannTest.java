/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.spec;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

/**
 *
 * @author jorge
 */
public class MannTest {
    
    public static void main(String[] args) {
        /*double[] b = new double[]{1,2,3,4,5};
        double[] a  = new double[]{6,7,8,9,10};*/
        /*double[] a = new double[]{1,2,3,4,5};
        double[] b  = new double[]{6,7,8,9,10};*/
        double[] a = new double[]{1,3,5,7,9};
        double[] b  = new double[]{0,2,4,6,8,10};
        MannWhitneyUTest test = new MannWhitneyUTest();
        double u1 = test.mannWhitneyU(a, b);
        double u2 = test.mannWhitneyU(b, a);
        System.out.println(u1 + " " + u2 + " Max: " + a.length * b.length);
    }
    
}
