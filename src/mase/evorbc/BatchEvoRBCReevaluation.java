/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import mase.mason.MasonSimulationProblem;
import mase.stat.ReevaluationTools;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 *
 * @author jorge
 */
public class BatchEvoRBCReevaluation {

    public static final String FOLDER = "-f";
    public static final String FORCE = "-force";
    public static final String RECURSIVE = "-recursive";

    public static void main(String[] args) throws Exception {
        /* Parse command line arguments */
        List<File> folders = new ArrayList<>();
        int reps = 0;
        boolean force = false;
        boolean recursive = false;
        for (int x = 0; x < args.length; x++) {
            if (args[x].equalsIgnoreCase(FOLDER)) {
                File folder = new File(args[1 + x++]);
                if (!folder.exists() || !folder.isDirectory()) {
                    throw new Exception("Folder does not exist: " + folder.getAbsolutePath());
                }
                folders.add(folder);
            } else if (args[x].equalsIgnoreCase(ReevaluationTools.P_NREPS)) {
                reps = Integer.parseInt(args[1 + x++]);
            } else if (args[x].equalsIgnoreCase(RECURSIVE)) {
                recursive = true;
            } else if (args[x].equalsIgnoreCase(FORCE)) {
                force = true;
            }
        }
        if (reps <= 0) {
            System.out.println("Invalid number of repetitions: " + reps);
            return;
        }
        if (folders.isEmpty()) {
            System.out.println("Nothing to evaluate!");
            return;
        }

        /* Recursive expansion */
        Set<File> dirSet = new HashSet<>();
        if (recursive) {
            for (File f : folders) {
                dirSet.addAll(FileUtils.listFilesAndDirs(
                        f, new NotFileFilter(TrueFileFilter.INSTANCE), DirectoryFileFilter.DIRECTORY));
            }
        } else {
            dirSet.addAll(folders);
        }

        /* Perform evaluation */
 /*ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (File f : dirSet) {
            try {
                es.submit(new DirReevaluate(f, args, reps, force));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        es.shutdown();
        es.awaitTermination(1, TimeUnit.DAYS);*/
        for (File dir : dirSet) {
            try {
                Collection<File> list = FileUtils.listFiles(dir, new String[]{"postbest.xml"}, false);
                if (!list.isEmpty()) {
                    System.out.println(dir.getAbsolutePath());
                    MasonSimulationProblem simulator = (MasonSimulationProblem) ReevaluationTools.createSimulator(args, dir);
                    for (File f : list) {
                        File out = new File(f.getPath() + ".stat");
                        if (force || !out.exists()) {
                            try {
                                EvoRBCReevaluation.logController(f, simulator, reps, out);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        System.out.print(".");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /*private static class DirReevaluate implements Runnable {

        private final File dir;
        private final String[] args;
        private final int reps;
        private final boolean force;

        private DirReevaluate(File dir, String[] args, int reps, boolean force) {
            this.dir = dir;
            this.args = args;
            this.reps = reps;
            this.force = force;
        }

        @Override
        public void run() {
            // Reevaluate this folder if there are any files here
            Collection<File> list = FileUtils.listFiles(dir, new String[]{"postbest.xml"}, false);
            if (!list.isEmpty()) {
                System.out.println(dir.getAbsolutePath());
                MasonSimulationProblem simulator = (MasonSimulationProblem) ReevaluationTools.createSimulator(args, dir);
                for (File f : list) {
                    File out = new File(f.getPath() + ".stat");
                    if (force || !out.exists()) {
                        try {
                            EvoRBCReevaluation.logController(f, simulator, reps, out);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    System.out.print(".");
                }
            }
        }
    }*/
}
