/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class Aggregate {

    public static final String[] OPTIONS = new String[]{"ALL", "AVG", "MAX", "MIN", "STDEV"};
    public static final String ROOT = "-r";
    public static final String FILE_NAME = "-f";
    public static final String OUTPUT = "-o";
    public static final String COLUMN = "-c";
    public static final String HEADER_INPUT = "-hi";
    public static final String HEADER_OUTPUT = "-ho";

    public static void main(String[] args) {
        /* Read args */
        File root = null;
        String fileName = null;
        File output = null;
        ArrayList<String> columns = new ArrayList<String>();
        ArrayList<String> options = new ArrayList<String>();
        
        for (int i = 0; i < args.length; i++) {
            if(args[i].equals(ROOT)) {
                root = new File(args[i+1]);
                i++;
            } else if(args[i].equals(FILE_NAME)) {
                fileName = args[i+1];
                i++;
            } else if(args[i].equals(OUTPUT)) {
                output = new File(args[i+1]);
                i++;
            } else if(args[i].equals(COLUMN)) {
                
            }
        }
    }

    static protected void aggregate(File base, String fileName, String expName, String config, File output) throws Exception {
        /*
         * Find files
         */
        IOFileFilter filter = FileFilterUtils.suffixFileFilter(".csv", IOCase.INSENSITIVE);
        if (!fileName.equals("")) {
            filter = FileFilterUtils.and(filter, FileFilterUtils.nameFileFilter(fileName));
        }
        Collection<File> rawList = FileUtils.listFiles(base, filter, TrueFileFilter.TRUE);
        ArrayList<File> csvList = new ArrayList<>(rawList);
        Collections.sort(csvList);

        /*
         * Open files
         */
        System.out.println("Going to aggregate " + csvList.size() + " files");
        List<CSVReader> readers = new ArrayList<>(csvList.size());
        for (File csv : csvList) {
            readers.add(new CSVReader(new FileReader(csv), SEPARATOR));
        }

        /*
         * Map the columns of each file
         */
        ArrayList<HashMap<String, Integer>> columnMaps = new ArrayList<>();
        for (CSVReader r : readers) {
            String[] header = r.readNext();
            HashMap<String, Integer> map = new HashMap<>();
            for (int i = 0; i < header.length; i++) {
                String colName = header[i].trim().toLowerCase().replace(" ", ".");
                map.put(colName, i);
            }
            columnMaps.add(map);
        }

        /*
         * Interpret the options
         */
        String[] params = config.replace("\n", "").replace("\t", "").split(";");
        ArrayList<Pair<String, Integer>> transforms = new ArrayList<>(params.length);
        for (String p : params) {
            String[] split = p.split("-");
            String colName = split[0].trim().toLowerCase().replace(" ", ".");
            String option = split[1].trim();
            int optionIndex = -1;
            for (int i = 0; i < OPTIONS.length; i++) {
                if (OPTIONS[i].equalsIgnoreCase(option)) {
                    optionIndex = i;
                    break;
                }
            }
            if (optionIndex == -1 || colName == null) {
                throw new Exception("Inexistent column name or option in " + p);
            } else {
                transforms.add(Pair.of(colName, optionIndex));
            }
        }

        /*
         * Build the header
         */
        ArrayList<String> outputHeader = new ArrayList<>();
        for (Pair<String, Integer> t : transforms) {
            switch (t.getRight()) {
                case 0: // ALL
                    for (int i = 0; i < readers.size(); i++) {
                        outputHeader.add(expName + "." + t.getLeft() + "." + i);
                    }
                    break;
                case 1: // AVG
                    outputHeader.add(expName + "." + t.getLeft() + ".AVG");
                    break;
                case 2: // MAX
                    outputHeader.add(expName + "." + t.getLeft() + ".MAX");
                    break;
                case 3: // MIN
                    outputHeader.add(expName + "." + t.getLeft() + ".MIN");
                    break;
            }
        }

        /*
         * Open the output file and write the header
         */
        CSVWriter writer = new CSVWriter(new BufferedWriter(new FileWriter(output)), '\t');
        String[] outHeader = new String[outputHeader.size()];
        outHeader = outputHeader.toArray(outHeader);
        System.out.println(Arrays.toString(outHeader));
        writer.writeNext(outHeader);

        /*
         * WRITE THE CONTENT
         */
        HashSet<CSVReader> deadReaders = new HashSet<>();
        while (true) {
            System.out.print(".");
            List<String[]> lines = new ArrayList<>(readers.size());
            // Check what files reach the end
            for (CSVReader reader : readers) {
                if (deadReaders.contains(reader)) {
                    lines.add(null);
                } else {
                    String[] line = reader.readNext();
                    if (line == null || !line[0].matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")) { // se nao for um numero - messy fix para detectar novos headers
                        deadReaders.add(reader);
                        lines.add(null);
                    } else {
                        lines.add(line);
                    }
                }
            }
            if (deadReaders.size() == readers.size()) { // ALL DEAD
                break;
            }

            // Write the line
            List<String> newLine = new ArrayList<>(outHeader.length);
            for (Pair<String, Integer> t : transforms) {
                String transformCol = t.getLeft();
                switch (t.getRight()) {
                    case 0: // ALL
                        for (int i = 0; i < lines.size(); i++) {
                            String[] l = lines.get(i);
                            if (l != null && columnMaps.get(i).containsKey(transformCol)) {
                                newLine.add(l[columnMaps.get(i).get(transformCol)]);
                            } else {
                                newLine.add("");
                            }
                        }
                        break;
                    case 1: // AVG
                        double avg = 0;
                        int count = 0;
                        for (int i = 0; i < lines.size(); i++) {
                            String[] l = lines.get(i);
                            if (l != null && columnMaps.get(i).containsKey(transformCol)) {
                                avg += Double.parseDouble(l[columnMaps.get(i).get(transformCol)]);
                                count++;
                            }
                        }
                        if (count > 0) {
                            newLine.add(Double.toString(avg / count));
                        } else {
                            newLine.add("NA");
                        }
                        break;
                    case 2: // MAX
                        double max = Double.NEGATIVE_INFINITY;
                        for (int i = 0; i < lines.size(); i++) {
                            String[] l = lines.get(i);
                            if (l != null && columnMaps.get(i).containsKey(transformCol)) {
                                max = Math.max(Double.parseDouble(l[columnMaps.get(i).get(transformCol)]), max);
                            }
                        }
                        if (max != Double.NEGATIVE_INFINITY) {
                            newLine.add(Double.toString(max));
                        } else {
                            newLine.add("NA");
                        }
                        break;
                    case 3: // MIN
                        double min = Double.POSITIVE_INFINITY;
                        for (int i = 0; i < lines.size(); i++) {
                            String[] l = lines.get(i);
                            if (l != null && columnMaps.get(i).containsKey(transformCol)) {
                                min = Math.min(Double.parseDouble(l[columnMaps.get(i).get(transformCol)]), min);
                            }
                        }
                        if (min != Double.POSITIVE_INFINITY) {
                            newLine.add(Double.toString(min));
                        } else {
                            newLine.add("NA");
                        }
                        break;
                }
            }
            String[] l = new String[newLine.size()];
            writer.writeNext(newLine.toArray(l));
        }
        writer.close();
        for (CSVReader r : readers) {
            r.close();
        }
        System.out.println();
        System.out.println("DONE");
    }
}
