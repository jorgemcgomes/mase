/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic.systematic;

import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 *
 * @author jorge
 */
public class SlopeTest {

    public static void main(String[] args) {
        SimpleRegression reg = new SimpleRegression(true);
        reg.addData(0, 1);
        reg.addData(1, 2);
        reg.addData(2, 4);
        reg.addData(3, 8);
        reg.addData(4, 12);
        System.out.println(reg.getSlope());
        reg.clear();
        reg.addData(0, 1);
        reg.addData(1, 2);
        reg.addData(2, 3);
        reg.addData(3, 4);
        reg.addData(4, 5);
        System.out.println(reg.getSlope());
        reg.clear();
        reg.addData(0, 5);
        reg.addData(1, 4);
        reg.addData(2, 3);
        reg.addData(3, 2);
        reg.addData(4, 1);
        System.out.println(reg.getSlope());
    }

}
