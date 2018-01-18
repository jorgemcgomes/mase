/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc.gp;

import ec.gp.GPNode;
import ec.gp.GPTree;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import mase.MaseProblem;
import mase.controllers.AgentController;
import mase.stat.PersistentSolution;
import mase.stat.ReevaluationTools;
import mase.stat.SolutionPersistence;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author jorge
 */
public class BestTreeStatistics {

    public static final String FOLDER = "-f";
    public static final String RECURSIVE = "-recursive";
    public static final String EXTENSION = "-e";

    public static void main(String[] args) throws Exception {
        /* Parse command line arguments */
        List<File> folders = new ArrayList<>();
        boolean recursive = false;
        String[] extensions = new String[]{"postbest.xml"};
        for (int x = 0; x < args.length; x++) {
            if (args[x].equalsIgnoreCase(FOLDER)) {
                File folder = new File(args[1 + x++]);
                if (!folder.exists() || !folder.isDirectory()) {
                    throw new Exception("Folder does not exist: " + folder.getAbsolutePath());
                }
                folders.add(folder);
            } else if (args[x].equalsIgnoreCase(RECURSIVE)) {
                recursive = true;
            } else if (args[x].equalsIgnoreCase(EXTENSION)) {
                extensions = ArrayUtils.add(extensions, args[1 + x++]);
            }
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
            Collection<File> list = FileUtils.listFiles(dir, extensions, false);
            if (!list.isEmpty()) {
                System.out.println(dir.getAbsolutePath());
                for (File f : list) {
                    File out = new File(f.getPath() + ".gp.stat");
                    try {
                        logSolution(f, out);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        out.delete();
                    }
                }
            }
        }
    }

    private static void logSolution(File in, File out) throws Exception {
        PersistentSolution sol = SolutionPersistence.readSolutionFromFile(in);
        AgentController ac = sol.getController().getAgentControllers(1)[0];
        if(ac instanceof GPArbitratorController) {
            GPTree tree = ((GPArbitratorController) ac).getProgramTree();
            FileWriter fw = new FileWriter(out);
            HashMap<String,Integer> nodeTypes = new HashMap<>();
            int size = tree.child.numNodes(GPNode.NODESEARCH_ALL);
            int term = tree.child.numNodes(GPNode.NODESEARCH_TERMINALS);
            int nonterm = tree.child.numNodes(GPNode.NODESEARCH_NONTERMINALS);
            int depth = tree.child.depth();
            Iterator iter = tree.child.iterator();
            while(iter.hasNext()) {
                GPNode n = (GPNode) iter.next();
                String name = n.name();
                if(nodeTypes.containsKey(name)) {
                    nodeTypes.put(name, nodeTypes.get(name) + 1);
                } else {
                    nodeTypes.put(name, 1);
                }
            }
            
            fw.write("Generation Subpop Index Fitness Size Terminals Non-Terminals Depth");
            for(String n : nodeTypes.keySet()) {
                fw.write(" f." + n);
            }
            fw.write("\n");
            fw.write(sol.getGeneration() + " " + sol.getSubpop() + " " + sol.getIndex() + " " + 
                    sol.getFitness() + " " + size + " " + term + " " + nonterm + " " + depth);
            for(String n : nodeTypes.keySet()) {
                fw.write(" " + nodeTypes.get(n));
            }
            fw.close();
        }
        
    }

}
