/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.vrep;

import ec.EvolutionState;
import ec.util.Output;
import ec.util.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import mase.MaseProblemBatch;
import mase.controllers.AgentController;
import mase.controllers.EncodableAgentController;
import mase.controllers.GroupController;
import mase.evaluation.EvaluationFunction;
import mase.evaluation.EvaluationResult;
import mase.vrep.VRepComm.VRepClient;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author jorge
 */
public class VRepProblem extends MaseProblemBatch {

    public static final String P_WORKERS = "workers";
    public static final String P_TIMEOUT = "timeout"; // seconds
    public static final String P_RETRY_DELAY = "retry-delay"; // seconds
    public static final String P_ALLOWED_FAULTS = "allowed-faults";
    public static final String P_GLOBALPAR = "global-params";
    public static final String P_BASE_PORT = "base-port";
    private static final long serialVersionUID = 1L;

    private int timeout;
    private int retryDelay;
    private int allowedFaults;
    private float[] globalParams;
    private final List<VRepClient> allClients = new ArrayList<>();
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private Output out;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.out = state.output;

        timeout = state.parameters.getInt(base.push(P_TIMEOUT), defaultBase().push(P_TIMEOUT));
        retryDelay = state.parameters.getInt(base.push(P_RETRY_DELAY), defaultBase().push(P_RETRY_DELAY));
        allowedFaults = state.parameters.getInt(base.push(P_ALLOWED_FAULTS), defaultBase().push(P_ALLOWED_FAULTS));

        String gp = state.parameters.getString(base.push(P_GLOBALPAR), defaultBase().push(P_GLOBALPAR));
        String[] values = gp.split("[\\;\\,]");
        globalParams = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            globalParams[i] = Float.parseFloat(values[i]);
        }

        int basePort = state.parameters.getInt(base.push(P_BASE_PORT), defaultBase().push(P_BASE_PORT));

        // TODO: the configuration of the workers should be better, to allow for different baseports, etc
        String workers = state.parameters.getString(base.push(P_WORKERS), defaultBase().push(P_WORKERS));
        String[] ips = workers.split("[;,]");
        String[] remoteIps = new String[ips.length];
        int[] remoteInstances = new int[ips.length];
        for (int i = 0; i < ips.length; i++) {
            String[] split = ips[i].split("[\\-\\:]");
            remoteIps[i] = split[0];
            remoteInstances[i] = Integer.parseInt(split[1]);
        }

        VRepComm.terminateAll();
        for (int r = 0; r < remoteIps.length; r++) {
            for (int i = 0; i < remoteInstances[r]; i++) {
                VRepClient c = new VRepClient(remoteIps[r], basePort + i);
                allClients.add(c);
            }
        }
    }     

    @Override
    public synchronized List<EvaluationResult[]> evaluateSolutions(List<GroupController> gcs, List<Long> seeds) {
        Map<Integer, GroupController> toEval = new LinkedHashMap<>();
        for (int i = 0; i < gcs.size(); i++) {
            toEval.put(i, gcs.get(i));
        }

        Map<Integer, EvaluationResult[]> results = evaluate(toEval);

        // Sort the result for return
        List<EvaluationResult[]> res = new ArrayList<>(gcs.size());
        for (int i = 0; i < gcs.size(); i++) {
            res.add(results.get(i));
        }
        return res;
    }

    private Map<Integer, EvaluationResult[]> evaluate(Map<Integer, GroupController> toEval) {
        List<VRepClient> available = getAvailableClients();
        
        // None are available, wait a bit and try again
        if(available.isEmpty()) {
            try {
                Thread.sleep(retryDelay * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(VRepProblem.class.getName()).log(Level.SEVERE, null, ex);
            }
            return evaluate(toEval);
        }

        // Divide the controllers among the workers
        List<VRepCallable> tasks = new ArrayList<>();
        int div = toEval.size() / available.size();
        int rem = toEval.size() % available.size();

        for (VRepClient c : available) {
            int currentTasks = div;
            if (rem > 0) {
                currentTasks++;
                rem--;
            }
            Map<Integer, GroupController> miniBatch = new LinkedHashMap<>();
            Iterator<Entry<Integer, GroupController>> iter = toEval.entrySet().iterator();
            for (int i = 0; i < currentTasks; i++) {
                Entry<Integer, GroupController> next = iter.next();
                iter.remove();
                miniBatch.put(next.getKey(), next.getValue());
            }
            VRepCallable call = new VRepCallable(miniBatch, c);
            tasks.add(call);
        }

        // Submit to the workers
        Map<Integer, EvaluationResult[]> resultsMap = new LinkedHashMap<>();
        Map<Integer, GroupController> missing = new LinkedHashMap<>();
        try {
            List<Future<Map<Integer, EvaluationResult[]>>> futures = threadPool.invokeAll(tasks, timeout, TimeUnit.SECONDS);
            for (int i = 0; i < futures.size(); i++) {
                Future<Map<Integer, EvaluationResult[]>> f = futures.get(i);
                try {
                    Map<Integer, EvaluationResult[]> res = f.get();
                    resultsMap.putAll(res);
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("Error executing batch " + i + ": " + e.getMessage());
                    missing.putAll(tasks.get(i).gcs);
                }
            }

            // Resend missing evaluations
            if (!missing.isEmpty()) {
                Thread.sleep(retryDelay * 1000); // Wait a bit before sending again
                Map<Integer, EvaluationResult[]> tryAgain = evaluate(missing);
                resultsMap.putAll(tryAgain);
            }
            return resultsMap;
        } catch (InterruptedException ex) {
            System.err.println("Fatal error in VRep evaluation: " + ex.getMessage());
            return null;
        }
    }

    private List<VRepClient> getAvailableClients() {
        // If all are over the fault limit, reset the fault limit
        int count = 0;
        for(VRepClient c : allClients) {
            if( c.faults > allowedFaults) {
                count++;
            }
        }
        if(count == allClients.size()) {
            for(VRepClient c : allClients) {
                c.faults = 0;
            }            
        }
        
        // Init or restart clients if needed
        List<VRepInit> attemptRestart = new ArrayList<>(allClients.size());
        for (VRepClient c : allClients) {
            if (!c.isAvailable() && c.faults <= allowedFaults) {
                attemptRestart.add(new VRepInit(c));
            }
        }
        try {
            threadPool.invokeAll(attemptRestart, timeout, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Logger.getLogger(VRepProblem.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Filter the available clients
        List<VRepClient> available = new ArrayList<>(allClients.size());
        for(VRepClient c : allClients) {
            if(c.isAvailable()) {
                available.add(c);
            }
        }
        return available;
    }
    
    class VRepInit implements Callable<Boolean> {

        final VRepClient c;
        
        VRepInit(VRepClient c) {
            this.c = c;
        }
        
        @Override
        public Boolean call() throws Exception {
            return c.init();
        }
    }

    class VRepCallable implements Callable<Map<Integer, EvaluationResult[]>> {

        VRepClient client;
        Map<Integer, GroupController> gcs;

        VRepCallable(Map<Integer, GroupController> gcs, VRepClient client) {
            this.client = client;
            this.gcs = gcs;
        }

        @Override
        public Map<Integer, EvaluationResult[]> call() throws Exception {
            float[] a = vrepMessage(gcs);
            boolean success = client.sendDataToVREP(a);
            if (success) {
                float[] res = client.getDataFromVREP();
                if (res == null) {
                    throw new Exception("Error receiving from client " + client);
                }
                Map<Integer, EvaluationResult[]> decoded = decodeVrepResults(res);
                return decoded;
            } else {
                throw new Exception("Error sending to client " + client);
            }
        }

        // <num_global_params> <value>*num_global_params <num_controllers> [<id> <controller_type> <length> <values>*length]*num_controllers
        private float[] vrepMessage(Map<Integer, GroupController> gcs) {
            float[] msg = new float[1 + globalParams.length + 1];
            msg[0] = globalParams.length;
            System.arraycopy(globalParams, 0, msg, 1, globalParams.length);
            msg[globalParams.length + 1] = gcs.size();
            for (Entry<Integer, GroupController> e : gcs.entrySet()) {
                msg = ArrayUtils.addAll(msg, encodeController(e.getKey(), e.getValue()));
            }
            return msg;
        }

        // <id> <controller_type> <length> <values>*length 
        private float[] encodeController(int id, GroupController gc) {
            AgentController[] acs = gc.getAgentControllers(1);
            if (!(acs[0] instanceof EncodableAgentController)) {
                out.fatal("Agent controller does not implement EncodableAgentController");
            }
            EncodableAgentController ac = (EncodableAgentController) acs[0];
            float[] encoded = ControllerFactory.doubleToFloat(ac.encode());
            int type = ControllerFactory.getControllerClassType(ac.getClass());

            float[] submsg = new float[1 + 1 + 1 + encoded.length];
            submsg[0] = id;
            submsg[1] = type;
            submsg[2] = encoded.length;
            System.arraycopy(encoded, 0, submsg, 3, encoded.length);
            return submsg;
        }

        // expected: <num_evaluations> [<id> <length> <value>*length]*num_evaluations
        private Map<Integer, EvaluationResult[]> decodeVrepResults(float[] a) {
            Map<Integer, EvaluationResult[]> res = new LinkedHashMap<>();
            int index = 0;
            int num = (int) a[index++];
            for (int i = 0; i < num; i++) {
                int id = (int) a[index++];
                int len = (int) a[index++];
                float[] values = Arrays.copyOfRange(a, index, index + len);
                index += values.length;
                EvaluationResult[] decodedResult = decodeControllerResult(values);
                res.put(id, decodedResult);
            }
            if (index != a.length) {
                out.fatal("Something went wrong processing the received message. Received: " + a.length + ". Processed: " + index);
            }
            return res;
        }

        // expected: <value>*length
        private EvaluationResult[] decodeControllerResult(float[] a) {
            EvaluationResult[] ers = new EvaluationResult[evalFunctions.length];
            for (int i = 0; i < evalFunctions.length; i++) {
                EvaluationFunction proto = evalFunctions[i];
                if (!(proto instanceof VRepEvaluationFunction)) {
                    out.fatal("Only VRepEvaluationFunction's are allowed: " + proto.getClass().getCanonicalName());
                }
                VRepEvaluationFunction ef = (VRepEvaluationFunction) proto.clone();
                ef.setValues(ControllerFactory.floatToDouble(a));
                ers[i] = ef.getResult();
            }
            return ers;
        }
    }

}
