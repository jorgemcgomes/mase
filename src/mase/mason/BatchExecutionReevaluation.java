/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mase.stat.ReevaluationTools;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author jorge
 */
public class BatchExecutionReevaluation {

    public static final String FOLDER = "-f";
    public static final String FORCE = "-force";
    public static final String RECURSIVE = "-recursive";
    public static final String EXTENSION = "-e";

    public static void main(String[] args) throws Exception {
        /* Parse command line arguments */
        List<File> folders = new ArrayList<>();
        int reps = 0;
        boolean force = false;
        boolean recursive = false;
        String[] extensions = new String[]{"postbest.xml"};
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
            } else if (args[x].equalsIgnoreCase(EXTENSION)) {
                extensions = ArrayUtils.add(extensions, args[1 + x++]);
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

        for (File dir : dirSet) {
            try {
                Collection<File> list = FileUtils.listFiles(dir, extensions, false);
                if (!list.isEmpty()) {
                    System.out.println(dir.getAbsolutePath());
                    MasonSimulationProblem simulator = (MasonSimulationProblem) ReevaluationTools.createSimulator(args, dir);
                    for (File f : list) {
                        File out = new File(f.getPath() + ".stat");
                        if (force || !out.exists()) {
                            try {
                                ExecutionReevaluation.logController(f, simulator, reps, out);
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
}
