package mase.conillon;

import client.Client;
import comm.ClientPriority;
import ec.EvolutionState;
import ec.Individual;
import ec.eval.MasterProblem;
import ec.util.Parameter;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import mase.controllers.GroupController;
import mase.evaluation.EvaluationResult;
import mase.evaluation.ExpandedFitness;
import mase.mason.MasonSimState;
import mase.mason.MasonSimulationProblem;
import mase.stat.FitnessStat;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author jorge
 */
public class ConillonMasterProblem extends MasterProblem {

    public static final String P_SERVER_PORT = "server-port";
    public static final String P_CODE_PORT = "code-port";
    public static final String P_PRIORITY = "priority";
    public static final String P_SERVER_NAME = "server-name";
    public static final String P_MIN_TIMEOUT = "min-timeout";
    public static final String P_MAX_TIMEOUT = "max-timeout";
    public static final String P_MAX_TRIES = "maxtries";
    private static final long serialVersionUID = 1L;

    private static class Evaluation {

        Individual[] ind;
        boolean[] updateFitness;
        boolean countVictoriesOnly;
        int[] subpops;
        int threadnum;
    }

    private int serverPort;
    private int codePort;
    private String serverName;
    private int jobSize;
    private long minTimeout;
    private long maxTimeout;
    private int maxTries;

    private transient HashMap<Integer, Evaluation> jobs;
    private transient ArrayList<SlaveTask> tasks;
    private transient Client client;
    private transient int idCounter;
    private transient int tries;
    private transient long currentTimeout;

    private synchronized int nextID() {
        idCounter++;
        return idCounter;
    }

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        serverPort = state.parameters.getInt(base.push(P_SERVER_PORT), defaultBase().push(P_SERVER_PORT));
        jobSize = state.parameters.getInt(base.push(P_JOB_SIZE), defaultBase().push(P_JOB_SIZE));
        codePort = state.parameters.getInt(base.push(P_CODE_PORT), defaultBase().push(P_CODE_PORT));
        //priorityNumber = state.parameters.getInt(base.push(P_PRIORITY), defaultBase().push(P_PRIORITY));
        serverName = state.parameters.getString(base.push(P_SERVER_NAME), defaultBase().push(P_SERVER_NAME));
        minTimeout = state.parameters.getLong(base.push(P_MIN_TIMEOUT), defaultBase().push(P_MIN_TIMEOUT));
        maxTimeout = state.parameters.getLong(base.push(P_MAX_TIMEOUT), defaultBase().push(P_MAX_TIMEOUT));        
        maxTries = state.parameters.getInt(base.push(P_MAX_TRIES), defaultBase().push(P_MAX_TRIES));
    }

    @Override
    public void prepareToEvaluate(EvolutionState state, int threadnum) {
        jobs = new HashMap<>();
        tasks = new ArrayList<>();
        idCounter = 0;
    }

    private String getDesc(EvolutionState state) {
        File statFile = ((FitnessStat) state.statistics.children[1]).statisticsFile;
        String exp = statFile.getParentFile().getParentFile().getParentFile().getName() + "/" + statFile.getParentFile().getParentFile().getName() + "/" + statFile.getParentFile().getName();
        String desc = exp + " / job " + state.job[0] + " / gen " + state.generation + " (" + state.numGenerations + ")";
        return desc;
    }

    @Override
    public void evaluate(EvolutionState state, Individual[] inds, boolean[] updateFitness, boolean countVictoriesOnly, int[] subpops, int threadnum) {
        Evaluation job = new Evaluation();
        job.ind = Arrays.copyOf(inds, inds.length);
        job.updateFitness = Arrays.copyOf(updateFitness, updateFitness.length);
        job.countVictoriesOnly = countVictoriesOnly;
        job.subpops = Arrays.copyOf(subpops, subpops.length);
        job.threadnum = threadnum;
        MasonSimulationProblem simProblem = (MasonSimulationProblem) problem;
        GroupController gc = simProblem.createController(state, job.ind);
        int id = nextID();
        MasonSimState sim = simProblem.createSimState(gc, simProblem.nextSeed(state, threadnum));
        SlaveTask task = new SlaveTask(sim, id, simProblem.getEvalFunctions(), simProblem.getRepetitions(), simProblem.getMaxSteps());
        jobs.put(id, job);
        tasks.add(task);
    }

    @Override
    public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum) {
        Evaluation job = new Evaluation();
        job.ind = new Individual[]{ind};
        job.updateFitness = null;
        job.countVictoriesOnly = false;
        job.subpops = new int[]{subpopulation};
        job.threadnum = threadnum;
        MasonSimulationProblem simProblem = (MasonSimulationProblem) problem;
        GroupController gc = simProblem.createController(state, ind);
        int id = nextID();
        MasonSimState sim = simProblem.createSimState(gc, simProblem.nextSeed(state, threadnum));
        SlaveTask task = new SlaveTask(sim, id, simProblem.getEvalFunctions(), simProblem.getRepetitions(), simProblem.getMaxSteps());
        jobs.put(id, job);
        tasks.add(task);
    }

    @Override
    public void finishEvaluating(final EvolutionState state, final int threadnum) {
        tries = 0;
        boolean done = false;
        currentTimeout = minTimeout;
        while (!done && tries < maxTries) {
            try {
                client.setDesc(getDesc(state));
                // Old client failed, needs to create new one
                if (tries > 0) {
                    // try to kill existing client -- no problem if it doesnt work
                    state.output.message("*** CLOSING EXISTING CLIENT " + client.getMyID() + " ***");
                    try {
                        runWithTimeout(new Runnable() {
                            @Override
                            public void run() {
                                closeContacts(state, 0);
                            }
                        }, 30, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        state.output.message("*** ERROR DISCONNECTING CLIENT " + client.getMyID() + ". CONTINUING AS NORMAL ***");
                    }
                    // try to create new client -- abort if it doesnt work
                    state.output.message("*** TRYING TO CONNECT NEW CLIENT ***");
                    runWithTimeout(new Runnable() {
                        @Override
                        public void run() {
                            initializeContacts(state);
                        }
                    }, currentTimeout, TimeUnit.SECONDS);

                    // replace client in Prototype
                    ConillonMasterProblem prob = (ConillonMasterProblem) state.evaluator.masterproblem;
                    prob.client = this.client;
                }
                // try to execute tasks
                state.output.message("*** SENDING TASKS FOR CLIENT " + client.getMyID() + " ***");
                ArrayList<SlaveResult> resList = runWithTimeout(
                        new EvaluationExecutor(this.client, tasks, jobSize),
                        currentTimeout, TimeUnit.SECONDS);
                parseResults(resList, state, threadnum);
                done = true;
            } catch (Exception e) {
                // Something went wrong or timeout
                state.output.message("*** ERROR WITH CLIENT " + client.getMyID() + " / TRY " + tries + "***");
                e.printStackTrace();
                currentTimeout = Math.min(currentTimeout * 2, maxTimeout);
                tries++;
            }
        }
    }

    private static class EvaluationExecutor implements Callable<ArrayList<SlaveResult>> {

        private final Client client;
        private final ArrayList<SlaveTask> tasks;
        private final int jobSize;

        public EvaluationExecutor(Client client, ArrayList<SlaveTask> tasks, int jobSize) {
            this.client = client;
            this.tasks = new ArrayList<>(tasks);
            this.jobSize = jobSize;
        }

        @Override
        public ArrayList<SlaveResult> call() throws Exception {
            // Send tasks to client
            int numTasks = commitTasks(tasks, client);
            // Wait and receive results
            ArrayList<SlaveResult> res = receiveResults(numTasks, client);
            return res;
        }

        private int commitTasks(ArrayList<SlaveTask> tasks, Client client) {
            int numberOfTasks = 0;
            if (jobSize == 1) {
                for (SlaveTask t : tasks) {
                    client.commit(t);
                }
                numberOfTasks = tasks.size();
                tasks.clear();
            } else {
                ArrayList<SlaveTask> batch = new ArrayList<>();
                Iterator<SlaveTask> iter = tasks.iterator();
                while (iter.hasNext()) {
                    if (batch.size() == jobSize) {
                        MetaSlaveTask meta = new MetaSlaveTask(batch);
                        client.commit(meta);
                        numberOfTasks++;
                        batch = new ArrayList<>();
                    } else {
                        batch.add(iter.next());
                        iter.remove();
                    }
                }
                if (!batch.isEmpty()) {
                    MetaSlaveTask meta = new MetaSlaveTask(batch);
                    client.commit(meta);
                    numberOfTasks++;
                    tasks.clear();
                }
            }
            return numberOfTasks;
        }

        private ArrayList<SlaveResult> receiveResults(int numberOfTasks, Client client) {
            ArrayList<SlaveResult> resList = new ArrayList<>();
            if (jobSize == 1) {
                for (int i = 0; i < numberOfTasks; i++) {
                    resList.add((SlaveResult) client.getNextResult());
                }
            } else {
                for (int i = 0; i < numberOfTasks; i++) {
                    MetaSlaveResult meta = (MetaSlaveResult) client.getNextResult();
                    resList.addAll(meta.getResults());
                }
            }
            return resList;
        }

    }

    private void parseResults(ArrayList<SlaveResult> resList, EvolutionState state, int threadnum) {
        for (SlaveResult r : resList) {
            Evaluation j = jobs.get(r.getID());
            ArrayList<EvaluationResult> evalList = r.getEvaluationResults();
            EvaluationResult[] eval = new EvaluationResult[evalList.size()];
            evalList.toArray(eval);
            if (j.ind.length == 1) { // non coevolution
                ExpandedFitness fit = (ExpandedFitness) j.ind[0].fitness;
                fit.setEvaluationResults(state, eval, j.subpops[0]);
                j.ind[0].evaluated = true;
            } else { // coevolution
                for (int i = 0; i < j.ind.length; i++) {
                    if (j.updateFitness[i]) {
                        ExpandedFitness trial = (ExpandedFitness) j.ind[i].fitness.clone();
                        trial.setEvaluationResults(state, eval, j.subpops[i]);
                        trial.setContext(j.ind);
                        j.ind[i].fitness.trials.add(trial);
                    }
                }
            }
        }
    }

    public static <T> T runWithTimeout(Callable<T> callable, long timeout, TimeUnit timeUnit) throws Exception {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<T> future = executor.submit(callable);
        executor.shutdown(); // This does not cancel the already-scheduled task.
        try {
            return future.get(timeout, timeUnit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            future.cancel(true);
            throw e;
        }
    }

    public static void runWithTimeout(Runnable runnable, long timeout, TimeUnit timeUnit) throws Exception {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future future = executor.submit(runnable);
        executor.shutdown(); // This does not cancel the already-scheduled task.
        try {
            future.get(timeout, timeUnit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            future.cancel(true);
            throw e;
        }
    }

    @Override
    public void closeContacts(EvolutionState state, int result) {
        client.cancelAllTasks();
        client.disconnect();
        state.output.message("*** CLIENT " + client.getMyID() + " CLOSED ***");
    }

    @Override
    public void reinitializeContacts(EvolutionState state) {
        initializeContacts(state);
    }
    
    

    @Override
    public void initializeContacts(EvolutionState state) {
        client = new Client(getDesc(state), ClientPriority.VERY_HIGH, serverName, serverPort, serverName, codePort);
        client.setTotalNumberOfTasks((state.numGenerations - state.generation) * state.population.subpops.length * state.population.subpops[0].individuals.length / jobSize);
        state.output.message("*** CONILLON CLIENT " + client.getMyID() + " INITIALIZED ***");
    }

}
