/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.util;

import java.util.Arrays;
import org.apache.commons.math3.util.Precision;

/**
 *
 * @author jorge
 */
public class FormatUtils {
    
    public static String toString(double[] array, int decimalPlaces) {
        double[] rounded = new double[array.length];
        for(int i = 0 ; i < rounded.length ; i++) {
            rounded[i] = Precision.round(array[i], decimalPlaces);
        }
        return Arrays.toString(rounded);
    }
    
    public static String toString(double[] array) {
        return toString(array, 2);
    }    
}
