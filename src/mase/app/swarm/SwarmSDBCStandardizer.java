/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.swarm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import mase.controllers.HomogeneousGroupController;
import mase.evaluation.EvaluationFunction;
import mase.mason.MasonSimulationProblem;
import mase.stat.ReevaluationTools;
import mase.util.CommandLineUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author jorge
 */
public class SwarmSDBCStandardizer {
    
    public static final String STORE = "-store";
    public static final File STORE_BASE = new File("build/classes/mase/app/swarm");
    public static final File SRC_BASE = new File("src/mase/app/swarm");
    
    public static void main(String[] args) throws IOException {
        String storeFile = CommandLineUtils.getValueFromArgs(args, STORE);
        MasonSimulationProblem sim = (MasonSimulationProblem) ReevaluationTools.createSimulator(args);
        SwarmSDBC fun = new SwarmSDBC();
        sim.setEvalFunctions(new EvaluationFunction[]{fun});
        
        DescriptiveStatistics[] ds = null;
        for (int i = 0; i < 1000; i++) {
            SwarmPlayground pl = (SwarmPlayground) sim.getSimState(new HomogeneousGroupController(null), i);
            pl.start();
            double[] s = fun.state(pl);
            if (ds == null) {
                ds = new DescriptiveStatistics[s.length];
                for (int j = 0; j < s.length; j++) {
                    ds[j] = new DescriptiveStatistics();
                }
            }
            for (int j = 0; j < s.length; j++) {
                if(!Double.isNaN(s[j])) {
                    ds[j].addValue(s[j]);
                }
            }            
        }
        
        File out = new File(STORE_BASE, storeFile);
        FileWriter fw = new FileWriter(out);
        System.out.println("Writing to " + out.getAbsolutePath());
        
        for(int i = 0 ; i < ds.length - 2 ; i++) { // the last two are the linear and turning speed
            System.out.println("Feature " + i + ": Mean: " + ds[i].getMean() + " SD: " + ds[i].getStandardDeviation() + " Min: " + ds[i].getMin() + " Max: " + ds[i].getMax());
            fw.write(i + " " + ds[i].getMean() + " " +  ds[i].getStandardDeviation() + "\n");
        }        
        
        SwarmPlayground pl = (SwarmPlayground) sim.getSimState(new HomogeneousGroupController(null), 0);  
        // linear and turn speed (special case)
        if(pl.par.backMove) {
            fw.write(ds.length - 2 + " " + 0.0 + " " + pl.par.wheelSpeed + "\n");
            fw.write(ds.length - 1 + " " + 0.0 + " " + pl.par.wheelSpeed / pl.par.radius);
        } else {
            fw.write(ds.length - 2 + " " + pl.par.wheelSpeed / 2 + " " + (pl.par.wheelSpeed / 2) + "\n");
            fw.write(ds.length - 1 + " " + 0.0 + " " + pl.par.wheelSpeed / (pl.par.radius * 2));
        }
        fw.close();
        
        File copy = new File(SRC_BASE, storeFile);
        System.out.println("Writing to " + copy.getAbsolutePath());
        FileUtils.copyFile(out, copy);
    }
    
    public static Pair<double[], double[]> readStandardization(File f) throws FileNotFoundException {
        ArrayList<Double> means = new ArrayList<>();
        ArrayList<Double> sds = new ArrayList<>();
        Scanner sc = new Scanner(f);
        sc.useLocale(Locale.ENGLISH);
        while(sc.hasNext()) {
            int idx = sc.nextInt();
            double mean = sc.nextDouble();
            double sd = sc.nextDouble();
            means.add(mean);
            sds.add(sd);
        }
        Double[] ma = means.toArray(new Double[means.size()]);
        Double[] sa = sds.toArray(new Double[sds.size()]);
        return Pair.of(ArrayUtils.toPrimitive(ma), ArrayUtils.toPrimitive(sa));
    }
}
