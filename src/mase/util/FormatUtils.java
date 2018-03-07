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

    public static final String SEPARATOR_ARRAY = "[,;]+";

    public static String toStringRounded(double[] array, int decimalPlaces) {
        double[] rounded = new double[array.length];
        for (int i = 0; i < rounded.length; i++) {
            rounded[i] = Precision.round(array[i], decimalPlaces);
        }
        return Arrays.toString(rounded);
    }

    public static String toStringRounded(double[] array) {
        return toStringRounded(array, 2);
    }

    public static String toStringSpaceSeparated(double[] array) {
        if (array.length == 0) {
            return "";
        }
        String r = array[0] + "";
        for (int i = 1; i < array.length; i++) {
            r += " " + array[i];
        }
        return r;
    }

    public static int[] parseIntArray(String s) {
        String[] split = s.split(SEPARATOR_ARRAY);
        int[] r = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            r[i] = Integer.parseInt(split[i]);
        }
        return r;
    }

    public static double[] parseDoubleArray(String s) {
        String[] split = s.split(SEPARATOR_ARRAY);
        double[] r = new double[split.length];
        for (int i = 0; i < split.length; i++) {
            r[i] = Double.parseDouble(split[i]);
        }
        return r;
    }

}
