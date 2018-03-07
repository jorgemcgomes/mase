/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jorge
 */
public class CommandLineUtils {

    public static double getDoubleFromArgs(String[] args, String arg) {
        String s = getValueFromArgs(args, arg);
        if (s == null) {
            throw new RuntimeException(arg + " not found");
        }
        return Double.parseDouble(s.trim());
    }

    public static double getDoubleFromArgsWithDefault(String[] args, String arg, double def) {
        try {
            return getDoubleFromArgs(args, arg);
        } catch (Exception e) {
            System.err.println("Error getting " + arg + " from args\n" + e.getMessage());
            System.err.println("Going with default value: " + def);
            return def;
        }
    }

    public static boolean isFlagPresent(String[] args, String arg) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(arg)) {
                return true;
            }
        }
        return false;
    }

    public static String getValueFromArgs(String[] args, String arg) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals(arg)) {
                return args[i + 1];
            }
        }
        return null;
    }

    public static String getValueFromArgsWithDefault(String[] args, String arg, String def) {
        String s = getValueFromArgs(args, arg);
        return s == null ? def : s;
    }

    public static int getIntFromArgs(String[] args, String arg) {
        String s = getValueFromArgs(args, arg);
        if (s == null) {
            throw new RuntimeException(arg + " not found");
        }
        return Integer.parseInt(s.trim());
    }

    public static int getIntFromArgsWithDefault(String[] args, String arg, int def) {
        try {
            return getIntFromArgs(args, arg);
        } catch (Exception e) {
            System.err.println("Error getting " + arg + " from args\n" + e.getMessage());
            System.err.println("Going with default value: " + def);
            return def;
        }
    }
    
    public static File getFileFromArgs(String[] args, String arg, boolean checkExists) {
        String v = getValueFromArgs(args, arg);
        if(v == null) {
            return null;
        } else {
            File f = new File(v);
            return !checkExists || f.exists() ? f : null;
        }
    }

    public static List<String> getValuesFromArgs(String[] args, String p) {
        List<String> list = new ArrayList<>(args.length);
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(p)) {
                list.add(args[i + 1]);
                i++;
            }
        }
        return list;
    }

    public static List<File> getFilesFromArgs(String[] args, String p, boolean checkExists) {
        List<String> values = getValuesFromArgs(args, p);
        List<File> list = new ArrayList<>(values.size());
        for (String v : values) {
            File f = new File(v);
            if (!checkExists || f.exists()) {
                list.add(f);
            } else {
                System.err.println("File not found: " + f.getAbsolutePath());
            }
        }
        return list;
    }

}
