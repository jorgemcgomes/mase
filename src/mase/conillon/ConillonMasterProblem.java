package mase.conillon;

import client.Client;
import comm.ClientPriority;
import ec.EvolutionState;
import ec.Individual;
import ec.eval.MasterProblem;
import ec.util.Parameter;
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
    public static final String P_TIMEOUT = "timeout";

    private static class Evaluation {

        Individual[] ind;
        boolean[] updateFitness;
        boolean countVictoriesOnly;
        int[] subpops;
        int threadnum;
    }

    private HashMap<Integer, Evaluation> jobs;
    private ArrayList<SlaveTask> tasks;
    private int idCounter;
    private Client client;
    private int serverPort;
    private int codePort;
    private int priorityNumber;
    private String serverName;
    private int jobSize;
    private int lastNumberOfTasks;
    private long timeout;

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
        priorityNumber = state.parameters.getInt(base.push(P_PRIORITY), defaultBase().push(P_PRIORITY));
        serverName = state.parameters.getString(base.push(P_SERVER_NAME), defaultBase().push(P_SERVER_NAME));
        timeout = state.parameters.getLong(base.push(P_TIMEOUT), defaultBase().push(P_TIMEOUT));
    }

    @Override
    public void prepareToEvaluate(EvolutionState state, int threadnum) {
        jobs = new HashMap<>();
        tasks = new ArrayList<>();
        idCounter = 0;
        FitnessStat stat = (FitnessStat) state.statistics.children[1];
        client.setDesc(stat.statisticsFile.getParent() + " / job " + state.job[0] + " / gen " + state.generation + " (" + state.numGenerations + ")");
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
        while (true) {
            try {
                ArrayList<SlaveResult> resList = runWithTimeout(new EvaluationExecutor(state), timeout, TimeUnit.SECONDS);
                parseResults(resList, state, threadnum);
                break;
            } catch (Exception e) {
                // Something went wrong or timeout
                System.out.println("ERROR EVALUATING WITH CONILON... TRYING AGAIN...");
                e.printStackTrace();
                client = null; // to force the init of new client
            }
        }
    }
    
    private class EvaluationExecutor implements Callable<ArrayList<SlaveResult>> {

        private final EvolutionState state;

        public EvaluationExecutor(EvolutionState state) {
            this.state = state;
        }
        
        @Override
        public ArrayList<SlaveResult> call() throws Exception {
            if(client == null) {
                client = new Client("MASE resume", ClientPriority.VERY_HIGH, serverName, serverPort, serverName, codePort);
                client.setTotalNumberOfTasks((state.numGenerations - state.generation) * state.population.subpops.length * state.population.subpops[0].individuals.length / jobSize);
                state.output.message("***********************************\n*** CONILLON CLIENT RECONECTED ***\n***********************************");
            }
            // Send tasks to client
            ArrayList<SlaveTask> tasksCopy = new ArrayList<>(tasks);
            commitTasks(tasksCopy);
            // Wait and receive results
            ArrayList<SlaveResult> res = receiveResults();
            return res;
        }
        
    }

    private void commitTasks(ArrayList<SlaveTask> tasks) {
        if (jobSize == 1) {
            for (SlaveTask t : tasks) {
                client.commit(t);
            }
            lastNumberOfTasks = tasks.size();
            tasks.clear();
        } else {
            lastNumberOfTasks = 0;
            ArrayList<SlaveTask> batch = new ArrayList<>();
            Iterator<SlaveTask> iter = tasks.iterator();
            while (iter.hasNext()) {
                if (batch.size() == jobSize) {
                    MetaSlaveTask meta = new MetaSlaveTask(batch);
                    client.commit(meta);
                    lastNumberOfTasks++;
                    batch = new ArrayList<>();
                } else {
                    batch.add(iter.next());
                    iter.remove();
                }
            }
            if (!batch.isEmpty()) {
                MetaSlaveTask meta = new MetaSlaveTask(batch);
                client.commit(meta);
                lastNumberOfTasks++;
                tasks.clear();
            }
        }

    }

    private ArrayList<SlaveResult> receiveResults() {
        ArrayList<SlaveResult> resList = new ArrayList<>();
        if (jobSize == 1) {
            for (int i = 0; i < lastNumberOfTasks; i++) {
                resList.add((SlaveResult) client.getNextResult());
            }
        } else {
            for (int i = 0; i < lastNumberOfTasks; i++) {
                MetaSlaveResult meta = (MetaSlaveResult) client.getNextResult();
                resList.addAll(meta.getResults());
            }
        }
        return resList;
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
        } catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        } catch (ExecutionException e) {
            throw e;
        }
    }

    @Override
    public void closeContacts(EvolutionState state, int result) {
        client.disconnect();
    }

    @Override
    public void reinitializeContacts(EvolutionState state) {

    }

    @Override
    public void initializeContacts(EvolutionState state) {
        client = new Client("MASE / job " + state.job[0], ClientPriority.VERY_HIGH, serverName, serverPort, serverName, codePort);
        client.setTotalNumberOfTasks(state.numGenerations * state.population.subpops.length * state.population.subpops[0].individuals.length / jobSize);
        state.output.message("***********************************\n*** CONILLON CLIENT INITIALIZED ***\n***********************************");
    }

}
