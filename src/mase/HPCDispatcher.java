/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import mase.MaseManager.Job;
import mase.MaseManager.StatusListener;

/**
 * USAGE INSTRUCTIONS:
 * 1 - Run hpcsync.sh to copy all the required stuff to the cluster home
 * 2 - Run hpcrun.sh in the cluster, which launches this main
 * Ex: ./hpcrun.sh -t <number_threads> [-dry] [-nosort] <config_file>
 * @author jorge
 */
public class HPCDispatcher {

    public static final String P_NOSORT = "-nosort";
    public static final String P_DRY = "-dry";
    public static final String P_THREADS = "-t";

    public static void main(String[] args) throws IOException, InterruptedException {
        /*
        Read params from command line
         */
        String file = args[args.length - 1];
        boolean sort = true;
        boolean dryRun = false;
        int threads = 8;
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equalsIgnoreCase(P_NOSORT)) {
                sort = false;
            } else if (args[i].equalsIgnoreCase(P_DRY)) {
                dryRun = true;
            } else if (args[i].equalsIgnoreCase(P_THREADS)) {
                threads = Integer.parseInt(args[i + 1].trim());
                i++;
            } else {
                System.out.println("Unknown param");
            }
        }

        /*
        Parse jobs
         */
        MaseManager mng = new MaseManager();
        mng.setStatusListener(new StatusListener() {
            @Override
            public void message(String str) {
                System.out.println(str);
            }

            @Override
            public void error(String str) {
                System.err.println(str);
            }
            
        });
        mng.loadJobs(new File(file));

        if (sort) {
            mng.sortJobFirst();
        }
        

        // TODO: Clean existing scripts
        // Potential issue: might screw up with things if some are already running
        
        // Generate scripts
        List<String> scripts = new ArrayList<>();
        for (Job j : mng.waitingList) {
            String name = "mase_" + j.outfolder.replace("/", "_") + "_" + j.jobNumber + ".sh";
            FileWriter fw = new FileWriter(new File(name));
            j.params = j.params + " -p evalthreads=" + threads; // TODO: possible problem with breed threads?
            fw.write("#!/bin/bash\n"
                    + "java -cp \"build/classes:lib/*\" mase.MaseEvolve -out " + j.outfolder + " " + j.params);
            fw.close();
            scripts.add(name);
        }

        /*
        Confirmation
         */
        int c = 0;
        for (Job j : mng.waitingList) {
            System.out.println("[" + c + "] " + j.params);
            c++;
        }
        System.out.println("Enter y to submit jobs");
        if (dryRun) {
            System.out.println("** THIS IS A DRY RUN **");
        }
        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine();
        sc.close();
        if (!input.equalsIgnoreCase("y")) {
            System.exit(0);
        }

        /*
        Submit scripts
         */
        for (String s : scripts) { /*for hpc: -pe mp*/
            String cmd = "qsub -pe smp " + threads + /*" -q hpcgrid "*/ " " + s;
            System.out.println(cmd);
            if (!dryRun) {
                Process p = Runtime.getRuntime().exec(cmd);
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                p.waitFor();
                System.out.println("Here is the standard output of the command:\n");
                String str = null;
                while ((str = stdInput.readLine()) != null) {
                    System.out.println(str);
                }
                while ((str = stdError.readLine()) != null) {
                    System.err.println(str);
                }
            }
        }
    }

}
