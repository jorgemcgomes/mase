/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase;

import ec.EvolutionState;
import ec.Evolve;
import ec.util.Output;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;

/**
 * This is very much based on the original Evolve The most noteworthy changes
 * being allowing the specification of an output directory, which is added to
 * the job prefix for logging; and allowing the specification of multiple -file
 * arguments in the main args
 *
 * @author jorge
 */
public class MaseEvolve {

    public static final String OUT_DIR = "-out";
    public static final String FORCE = "-force";
    public static final String DEFAULT_CONFIG = "config.params";

    public static void main(String[] args) throws IOException {
        EvolutionState state;
        ParameterDatabase parameters;

        // should we print the help message and quit?
        Evolve.checkForHelp(args);

        // if we're loading from checkpoint, let's finish out the most recent job
        state = Evolve.possiblyRestoreFromCheckpoint(args);
        int currentJob = 0;                             // the next job number (0 by default)

        // this simple job iterator just uses the 'jobs' parameter, iterating from 0 to 'jobs' - 1
        // inclusive.  The current job number is stored in state.jobs[0], so we'll begin there if
        // we had loaded from checkpoint.
        if (state != null) { // loaded from checkpoint
            // check if the number of generations or evaluations needs to be updated
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals(EvolutionState.P_GENERATIONS)) {
                    int generations = Integer.parseInt(args[++i]);
                    state.numGenerations = generations;
                } else if (args[i].equals(EvolutionState.P_EVALUATIONS)) {
                    long evaluations = Long.parseLong(args[++i]);
                    state.numEvaluations = evaluations;
                }
            }

            // extract the next job number from state.job[0]
            args = state.runtimeArguments;                          // restore runtime arguments from checkpoint
            currentJob = ((Integer) (state.job[0])).intValue() + 1;  // extract next job number

            state.run(EvolutionState.C_STARTED_FROM_CHECKPOINT);
            Evolve.cleanup(state);
        }

        // Get the output directory (-out). Mandatory argument!
        File outDir = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(OUT_DIR)) {
                outDir = new File(args[i + 1]);
            }
        }
        if (outDir == null) {
            System.out.println("No out dir specified. Use -out");
            System.exit(1);
        }
        
        // Possibly warn that the output directory already exists and give some time to cancel
        boolean force = Arrays.asList(args).contains(FORCE);
        if (!outDir.exists()) {
            outDir.mkdirs();
        } else if (!force) {
            System.out.println("Folder already exists: " + outDir.getAbsolutePath() + ". Waiting 3 sec.");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
            }
        }
        
        // Load the parameters
        parameters = loadMultipleParameterDatabase(args);

        // Log the parameters in the output directory
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        parameters.list(pw, false);
        String parlist = sw.toString().replaceAll("(?m)^parent.*", ""); // remove parent.x params
        pw = new PrintWriter(new File(outDir, DEFAULT_CONFIG));
        pw.write("# " + StringUtils.join(args, ' ') + "\n" + parlist);
        pw.close();

        if (currentJob == 0) { // no current job number yet
            currentJob = parameters.getIntWithDefault(new Parameter("current-job"), null, 0);
        }
        if (currentJob < 0) {
            Output.initialError("The 'current-job' parameter must be >= 0 (or not exist, which defaults to 0)");
        }

        int numJobs = parameters.getIntWithDefault(new Parameter("jobs"), null, 1);
        if (numJobs < 1) {
            Output.initialError("The 'jobs' parameter must be >= 1 (or not exist, which defaults to 1)");
        }

        // Now we know how many jobs remain.  Let's loop for that many jobs.  Each time we'll
        // load the parameter database scratch (except the first time where we reuse the one we
        // just loaded a second ago).  The reason we reload from scratch each time is that the
        // experimenter is free to scribble all over the parameter database and it'd be nice to
        // have everything fresh and clean.  It doesn't take long to load the database anyway,
        // it's usually small.
        for (int job = currentJob; job < numJobs; job++) {
            try {
                // load the parameter database (reusing the very first if it exists)
                if (parameters == null) {
                    parameters = loadMultipleParameterDatabase(args);
                }

                // Initialize the EvolutionState, then set its job variables
                state = Evolve.initialize(parameters, job);                // pass in job# as the seed increment
                state.output.systemMessage("Job: " + job);
                state.job = new Object[1];                                  // make the job argument storage
                state.job[0] = Integer.valueOf(job);                    // stick the current job in our job storage
                state.runtimeArguments = args;                              // stick the runtime arguments in our storage
                // The output directory is appended to the jobprefix
                String jobFilePrefix = Paths.get(outDir.getAbsolutePath(), "job." + job + ".").toString();
                state.output.setFilePrefix(jobFilePrefix);     // add a prefix for checkpoint/output files 
                state.checkpointPrefix = jobFilePrefix + state.checkpointPrefix;  // also set up checkpoint prefix

                state.run(EvolutionState.C_STARTED_FRESH);
                Evolve.cleanup(state);  // flush and close various streams, print out parameters if necessary
                parameters = null;  // so we load a fresh database next time around
            } catch (Throwable e) { // such as an out of memory error caused by this job
                e.printStackTrace();
                state = null;
                System.gc();  // take a shot!
            }
        }
        System.exit(0);
    }

    /**
     * Extends the original functionality by enabling the specification of multiple 
     * parameter files using multiple -file <filepath> in the arguments
     * The extra parameter DBs are added as parents of the first database
     * @param args
     * @return
     * @throws IOException 
     */
    public static ParameterDatabase loadMultipleParameterDatabase(String[] args) throws IOException {
        ParameterDatabase db = Evolve.loadParameterDatabase(args); // only loads the first -file

        // load additional -file (if any)
        boolean first = true;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(Evolve.A_FILE)) {
                if (first) {
                    first = false;
                } else {
                    File f = new File(args[++i]);
                    ParameterDatabase extraParent = new ParameterDatabase(f);
                    db.addParent(extraParent);
                }
            }
        }
        return db;
    }
}
