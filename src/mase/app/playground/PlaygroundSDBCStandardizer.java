/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author jorge
 */
public class PlaygroundSDBCStandardizer {
    
    public static final File STORE_FILE = new File("build/classes/mase/app/playground/sdbcstandardization.txt");
    
    public static void main(String[] args) throws IOException {
        MasonSimulationProblem sim = (MasonSimulationProblem) ReevaluationTools.createSimulator(args);
        PlaygroundSDBC fun = null;
        for (EvaluationFunction ef : sim.getEvalFunctions()) {
            if (ef instanceof PlaygroundSDBC) {
                fun = (PlaygroundSDBC) ef;
                break;
            }
        }
        
        if(fun == null) {
            System.err.println("PlaygroundSDBCRaw evaluation function not found. Standardizer not run.");
            return;
        }
        
        DescriptiveStatistics[] ds = null;
        for (int i = 0; i < 1000; i++) {
            Playground pl = (Playground) sim.getSimState(new HomogeneousGroupController(null), i);
            pl.par.randomPosition = true;
            pl.start();
            double[] s = fun.state(pl);
            
            if (ds == null) {
                ds = new DescriptiveStatistics[s.length];
                for (int j = 0; j < s.length; j++) {
                    ds[j] = new DescriptiveStatistics();
                }
            }
            
            for (int j = 0; j < s.length; j++) {
                ds[j].addValue(s[j]);
            }            
        }
        
        FileWriter fw = new FileWriter(STORE_FILE);
        
        for(int i = 0 ; i < ds.length - 2 ; i++) { // the last two are the linear and turning speed
            System.out.println("Feature " + i + ": Mean: " + ds[i].getMean() + " SD: " + ds[i].getStandardDeviation() + " Min: " + ds[i].getMin() + " Max: " + ds[i].getMax());
            fw.write(i + " " + ds[i].getMean() + " " +  ds[i].getStandardDeviation() + "\n");
        }        
        
        Playground pl = (Playground) sim.getSimState(new HomogeneousGroupController(null), 0);  
        if(pl.par.backMove) {
            fw.write(ds.length - 2 + " " + 0.0 + " " + pl.par.linearSpeed + "\n");
        } else {
            fw.write(ds.length - 2 + " " + pl.par.linearSpeed / 2 + " " + (pl.par.linearSpeed / 2) + "\n");
        }
        fw.write(ds.length - 1 + " " + 0.0 + " " + pl.par.turnSpeed);

        fw.close();
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
