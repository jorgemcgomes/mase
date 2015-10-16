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
import mase.controllers.GroupController;
import mase.evaluation.EvaluationResult;
import mase.evaluation.ExpandedFitness;
import mase.mason.MasonSimState;
import mase.mason.MasonSimulationProblem;

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
    
    private static class Evaluation {

        Individual[] ind;
        boolean[] updateFitness;
        boolean countVictoriesOnly;
        int[] subpops;
        int threadnum;
    }

    private HashMap<Integer, Evaluation> jobs;
    private int idCounter;
    private Client client;
    private int serverPort;
    private int codePort;
    private int priorityNumber;
    private String serverName;

    private synchronized int nextID() {
        idCounter++;
        return idCounter;
    }

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        serverPort = state.parameters.getInt(base.push(P_SERVER_PORT), defaultBase().push(P_SERVER_PORT));
        codePort = state.parameters.getInt(base.push(P_CODE_PORT), defaultBase().push(P_CODE_PORT));
        priorityNumber = state.parameters.getInt(base.push(P_PRIORITY), defaultBase().push(P_PRIORITY));
        serverName = state.parameters.getString(base.push(P_SERVER_NAME), defaultBase().push(P_SERVER_NAME));
    }

    @Override
    public void prepareToEvaluate(EvolutionState state, int threadnum) {
        jobs = new HashMap<>();
        idCounter = 0;
        client.setDesc("MASE / job " + state.job[0] + " / gen " + state.generation);
    }

    @Override
    public void finishEvaluating(EvolutionState state, int threadnum) {
        ArrayList<SlaveResult> resList = new ArrayList<>();
        for(int i = 0 ; i < jobs.size() ; i++) {
            resList.add((SlaveResult) client.getNextResult());
        }
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
        client.commit(task);
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
        client.commit(task);
    }

    @Override
    public void closeContacts(EvolutionState state, int result) {
        client.disconnect();
    }

    @Override
    public void reinitializeContacts(EvolutionState state) {
        // TODO: initialize CLIENT
    }

    @Override
    public void initializeContacts(EvolutionState state) {
        ClientPriority priority = getPriority(priorityNumber);
        client = new Client("MASE / job " + state.job[0], priority, serverName, serverPort, serverName, codePort);
        client.setTotalNumberOfTasks(state.numGenerations * state.population.subpops.length * state.population.subpops[0].individuals.length);
        state.output.message("***********************************\n*** CONILLON CLIENT INITIALIZED ***\n***********************************");
    }

    private ClientPriority getPriority(int priority) {
        return (priority < 2) ? ClientPriority.VERY_HIGH
                : (priority < 4) ? ClientPriority.HIGH
                        : (priority < 7) ? ClientPriority.NORMAL
                                : ClientPriority.LOW;
    }

}
