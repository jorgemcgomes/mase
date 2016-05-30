package mase;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jorge
 */
public class MaseManager {

    public static final String ALLOWED_NAMES_PATTERN = "[^\\\\/:\\*\\?\"<>\\|\\s_]{1,15}";
    protected List<Job> waitingList = Collections.synchronizedList(new ArrayList<Job>());
    protected List<Job> completed = Collections.synchronizedList(new ArrayList<Job>());
    protected List<Job> failed = Collections.synchronizedList(new ArrayList<Job>());
    protected ConcurrentHashMap<Integer, JobRunner> runners = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<JobRunner, Job> running = new ConcurrentHashMap<>();
    private volatile int jobId = 0;
    private volatile int runnerId = 0;
    private volatile boolean run = false;
    private int maxTries = 3;
    private StatusListener listener;

    public void startExecution() {
        Thread t = new Thread(new RunnerThread());
        t.start();
    }

    public void setStatusListener(StatusListener sl) {
        this.listener = sl;
    }

    class RunnerThread implements Runnable {

        @Override
        public void run() {
            while (true) {
                if (!waitingList.isEmpty() && run) {
                    // find an available runner
                    JobRunner n = null;
                    for (JobRunner r : runners.values()) {
                        if (!running.containsKey(r)) {
                            n = r;
                        }
                    }
                    if (n != null) {
                        final JobRunner nextRunner = n;
                        final Job nextJob = waitingList.remove(0);
                        running.put(nextRunner, nextJob);
                        nextJob.tries++;
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                boolean success = false;
                                try {
                                    listener.message("Submitting Job: " + nextJob + " @ " + nextRunner);
                                    nextJob.started = new Date();
                                    success = nextRunner.runJob(nextJob);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                nextRunner.updateOutput();
                                running.remove(nextRunner);
                                nextJob.completed = new Date();
                                if (success) {
                                    completed.add(nextJob);
                                    listener.message("Job completed: " + nextJob + " @ " + nextRunner);
                                } else if (nextJob.tries < maxTries) {
                                    listener.message("Job failed: " + nextJob + " @ " + nextRunner + " -- Retrying later (" + nextJob.tries + "/" + maxTries + ")");
                                    waitingList.add(nextJob);
                                } else {
                                    failed.add(nextJob);
                                    listener.error("Job failed: " + nextJob + " @ " + nextRunner);
                                }
                            }
                        });
                        t.start();
                    }
                }
                for (Entry<JobRunner,Job> e : running.entrySet()) {
                    e.getKey().updateOutput();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void pause(boolean cancel) {
        this.run = false;
        if (cancel) {
            for (JobRunner r : runners.values()) {
                Job toCancel = running.get(r);
                r.interrupt();
                waitingList.add(0, toCancel);
            }
        } else if (!running.isEmpty()) {
            listener.message("Not cancelling jobs, " + running.size() + " jobs still running");
        }
    }

    public void setMaxTries(int n) {
        this.maxTries = n;
    }

    public void resume() {
        this.run = true;
    }

    public boolean runningStatus() {
        return run;
    }

    public void addRunner(String config) throws Exception {
        String[] split = config.trim().split(" ");
        if (split[0].equalsIgnoreCase("local")) {
            LocalRunner lr = new LocalRunner(runnerId++);
            if (split.length > 1) {
                int evalthreads = Integer.parseInt(split[1]);
                lr.setEvalthreads(evalthreads);
            }
            runners.put(lr.id, lr);
        } else if (split[0].equalsIgnoreCase("ssh")) {
            final String ip = split[1];
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        SSHRunner sr = new SSHRunner(ip, runnerId++);
                        runners.put(sr.id, sr);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();
        } else if (split[0].equalsIgnoreCase("conillon")) {
            ConillonRunner cr = new ConillonRunner(runnerId++);
            runners.put(cr.id, cr);
        } else {
            throw new Exception("Unknown runner: " + split[0]);
        }
    }

    public void cancelAll() {
        for (Entry<JobRunner, Job> e : running.entrySet()) {
            listener.message("Cancelling " + e.getValue() + " @ " + e.getKey());
            e.getKey().interrupt();
        }
    }

    public void killRunner(int id) {
        JobRunner r = runners.get(id);
        if (r == null) {
            listener.error("Runner " + id + " not found.");
        } else {
            runners.remove(id);
            if (running.containsKey(r)) {
                // Kill this runner, and add the running job back to the waiting list
                Job cancelled = running.get(r);
                r.interrupt();
                waitingList.add(0, cancelled);
            }
        }
    }

    public void retryJob(String id) {
        for (Job j : findJobs(waitingList, id)) {
            listener.error("Job already in waiting list: " + j);
        }
        List<Job> retry = findJobs(failed, id);
        retry.addAll(findJobs(completed, id));
        for(Job j : retry) {
            j.tries = 0;
        }
        failed.removeAll(retry);
        completed.removeAll(retry);
        waitingList.addAll(retry);
        listener.message("Jobs added to waiting list: " + retry.size());
    }

    public void retryFailed() {
        for (Job j : failed) {
            j.tries = 0;
            waitingList.add(j);
        }
        failed.clear();
        listener.message("Added " + failed.size() + " jobs to waiting list");
    }

    private String optionValue(String args, String option) {
        String v = StringUtils.substringBetween(args, option, " ");
        if (v == null) {
            v = StringUtils.substringAfter(args, option).trim();
            if (v.isEmpty()) {
                v = null;
            }
        }
        return v;
    }

    public void addJob(String args) {
        String out = optionValue(args, "-out ");
        if (out == null) {
            listener.error(args + "\nNo -out directory speciefied");
            return;
        }
        out = StringUtils.remove(out, "~/");
        out = StringUtils.removePattern(out, "/home/\\w+/"); // Strip the absolute path

        String jobsStr = optionValue(args, "-p jobs=");
        if (jobsStr == null) {
            listener.error(args + "\nNo jobs specified");
        }
        int jobs = Integer.parseInt(jobsStr);
        int currentJob = 0;
        String curJobStr = optionValue(args, "-p current-job=");
        if (curJobStr != null) {
            currentJob = Integer.parseInt(curJobStr);
        }

        boolean checkRepetitions = !args.contains("-force");

        String clean = StringUtils.removePattern(args, "-p jobs=\\d+");
        clean = StringUtils.removePattern(clean, "-p current-job=\\d+");
        clean = StringUtils.removePattern(clean, "-out\\s\\S+\\s");

        String[] options = StringUtils.substringsBetween(clean, "{", "}");
        if (options == null || options.length == 0) {
            addJob(out, currentJob, jobs, clean, checkRepetitions);
        } else {
            List<List<String>> optionsList = new ArrayList<>();
            List<Boolean> useFull = new ArrayList<>();
            for (String option : options) {
                String[] pars = option.split(";");
                ArrayList<String> parList = new ArrayList<>();
                boolean ui = true;
                for (String par : pars) {
                    par = par.trim();
                    parList.add(par);
                    ui = par.matches(ALLOWED_NAMES_PATTERN) && ui;
                }
                useFull.add(ui);
                optionsList.add(parList);
            }
            List<List<String>> permutations = permutations(optionsList);
            for (List<String> p : permutations) {
                String newClean = clean;
                String newOut = StringUtils.removeEnd(out, "/");
                for (int j = 0; j < options.length; j++) {
                    String value = p.get(j);
                    newClean = newClean.replaceFirst("\\{.+?\\}", value);
                    newOut = newOut + "_" + (useFull.get(j) ? value : optionsList.get(j).indexOf(value));
                }
                addJob(newOut, currentJob, jobs, newClean, checkRepetitions);
            }
        }

    }

    private void addJob(String out, int currentJob, int jobs, String cleanParams, boolean checkRepetitions) {
        String strId = strId(jobId);
        for (int j = currentJob; j < jobs; j++) {
            File check = new File(absolute(out), "job." + j + ".postfitness.stat");
            if (check.exists() && checkRepetitions) {
                listener.message("Job already completed, not adding: " + out + " (" + j + ")");
            } else {
                Job newJob = new Job();
                newJob.outfolder = out;
                newJob.params = " -p current-job=" + j + " -p jobs=" + (j + 1) + " " + cleanParams + " -force";
                newJob.jobNumber = j;
                newJob.submitted = new Date();
                newJob.id = strId + j;
                if (!existsSimilar(newJob)) {
                    waitingList.add(newJob);
                }
            }
        }
        jobId++;
    }

    public static <T> List<List<T>> permutations(List<List<T>> collections) {
        if (collections == null || collections.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<List<T>> res = new LinkedList<>();
            permutationsImpl(collections, res, 0, new LinkedList<T>());
            return res;
        }
    }

    private static <T> void permutationsImpl(List<List<T>> ori, List<List<T>> res, int d, List<T> current) {
        if (d == ori.size()) {
            res.add(current);
            return;
        }
        Collection<T> currentCollection = ori.get(d);
        for (T element : currentCollection) {
            List<T> copy = new LinkedList<>(current);
            copy.add(element);
            permutationsImpl(ori, res, d + 1, copy);
        }
    }

    private boolean existsSimilar(Job check) {
        for (Job j : waitingList) {
            if (j.params.equals(check.params)) {
                listener.error("Job already exists in waiting list: " + j);
                return true;
            }
        }
        for (Entry<JobRunner, Job> e : running.entrySet()) {
            if (e.getValue().params.equals(check.params)) {
                listener.error("Job is already running: " + e.getValue() + " @ " + e.getKey());
                return true;
            }
        }
        return false;
    }

    private static String strId(int i) {
        return i < 0 ? "" : strId((i / 26) - 1) + (char) (97 + i % 26);
    }

    public void killJob(String id) {
        List<Job> waitingKill = findJobs(waitingList, id);
        waitingList.removeAll(waitingKill);
        int found = waitingKill.size();

        Map<JobRunner, Job> runningCopy = new HashMap<>(running); // to avoid concurrent modifications in the iterator
        for (Entry<JobRunner, Job> e : runningCopy.entrySet()) {
            if (jobMatch(e.getValue(), id)) {
                e.getValue().tries = 9999; // avoid being added again
                e.getKey().interrupt();
                found++;
            }
        }
        listener.message("Jobs killed: " + found);
    }

    public void topPriority(String id) {
        List<Job> up = findJobs(waitingList, id);
        waitingList.removeAll(up);
        waitingList.addAll(0, up);
        listener.message("Jobs moved to top priority: " + up.size());
    }

    public void lowestPriority(String id) {
        List<Job> down = findJobs(waitingList, id);
        waitingList.removeAll(down);
        waitingList.addAll(0, down);
        listener.message("Jobs moved to lowest priority: " + down.size());
    }

    public String getOutput(int id, int maxLines) throws Exception {
        JobRunner get = runners.get(id);
        return get.getOutput(maxLines);
    }

    public void sortJobFirst() {
        Collections.sort(waitingList, new Comparator<Job>() {
            @Override
            public int compare(Job o1, Job o2) {
                if (o1.jobNumber == o2.jobNumber) {
                    return o1.submitted.compareTo(o2.submitted);
                } else {
                    return Integer.compare(o1.jobNumber, o2.jobNumber);
                }
            }
        });
    }

    public void sortSubmissionDate() {
        Collections.sort(waitingList, new Comparator<Job>() {
            @Override
            public int compare(Job o1, Job o2) {
                if (o1.submitted.equals(o2.submitted)) {
                    return Integer.compare(o1.jobNumber, o2.jobNumber);
                } else {
                    return o1.submitted.compareTo(o2.submitted);
                }
            }
        });
    }

    public List<Job> findJobs(Collection<Job> col, String jobId) {
        LinkedList<Job> res = new LinkedList<>();
        for (Job j : col) {
            if (jobMatch(j, jobId)) {
                res.add(j);
            }
        }
        return res;
    }

    public List<Job> findJobs(String jobId) {
        LinkedList<Job> res = new LinkedList<>();
        res.addAll(findJobs(waitingList, jobId));
        res.addAll(findJobs(failed, jobId));
        res.addAll(findJobs(completed, jobId));
        res.addAll(findJobs(running.values(), jobId));
        return res;
    }

    private boolean jobMatch(Job j, String jobId) {
        return j.id.equals(jobId) || (StringUtils.isAlpha(jobId) && j.id.startsWith(jobId));
    }

    public interface StatusListener {

        public void message(String str);

        public void error(String str);

    }

    public static class Job {

        String id;
        String params;
        String outfolder;
        int jobNumber;
        Date submitted;
        Date started;
        Date completed;
        int tries = 0;

        @Override
        public String toString() {
            return id + ":" + outfolder + "(" + jobNumber + ")";
        }

        public String detailedToString() {
            return toString() + "\nSubmitted: " + submitted + " Started: " + started + " Completed: " + completed + " Tries: " + tries + "\n" + params;
        }
    }

    public abstract class JobRunner {

        private final String name;
        final int id;
        private volatile ArrayList<String> buffer = new ArrayList<>();
        private final int maxSize = 1000;
        protected volatile Process process;
        protected volatile BufferedReader in, err;

        JobRunner(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public abstract boolean runJob(Job job) throws Exception;

        public void updateOutput() {
            try {
                while (in != null && in.ready()) {
                    buffer.add(in.readLine());
                }
                while (err != null && err.ready()) {
                    buffer.add(err.readLine());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            while (buffer.size() > maxSize) {
                buffer.remove(0);
            }
        }

        public String getOutput(int nLines) throws Exception {
            String str = "";
            for (int i = Math.max(0, buffer.size() - nLines); i < buffer.size(); i++) {
                str += buffer.get(i) + "\n";
            }
            return str;
        }

        protected void interrupt() {
            process.destroy();
        }

        @Override
        public String toString() {
            return id + ":" + name;
        }
    }

    public class LocalRunner extends JobRunner {

        private int evalthreads = 0;

        public LocalRunner(int id) {
            super("local", id);
        }

        @Override
        public boolean runJob(Job job) throws IOException, InterruptedException {
            String par = job.params;
            if (evalthreads != 0) {
                par += " -p evalthreads=" + evalthreads;
            }
            process = Runtime.getRuntime().exec("java -cp build/classes:dist/lib/* mase.MaseEvolve -out " + absolute(job.outfolder) + " " + par);
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            return process.waitFor() == 0;
        }

        private void setEvalthreads(int evalthreads) {
            if (evalthreads > 0) {
                this.evalthreads = evalthreads;
            }
        }
    }

    public class SSHRunner extends JobRunner {

        private final String ip;

        public SSHRunner(String ip, int id) throws Exception {
            super("ssh" + ip, id);
            this.ip = ip;
            listener.message("Initializing @ " + toString());
            if (Runtime.getRuntime().exec("ssh " + ip + " rm -rf build lib").waitFor() != 0
                    || Runtime.getRuntime().exec("ssh " + ip + " mkdir -p build masetemp").waitFor() != 0
                    || Runtime.getRuntime().exec("scp -r -q build/classes " + ip + ":build/classes").waitFor() != 0
                    || Runtime.getRuntime().exec("scp -r -q dist/lib " + ip + ":lib").waitFor() != 0) {
                throw new Exception("Initialization problems @ " + toString());
            }
            listener.message("Initialized @ " + toString());
        }

        @Override
        public boolean runJob(Job job) throws Exception {
            if (!checkConnection()) {
                runners.remove(id);
                return false;
            }

            String tempOut = "masetemp/" + RandomStringUtils.randomAlphabetic(4).toLowerCase();
            process = Runtime.getRuntime().exec("ssh -t -t " + ip + " java -cp build/classes:lib/* mase.MaseEvolve -out " + tempOut + " " + job.params);
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            err = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            boolean jobSuccess = process.waitFor() == 0;
            boolean copySuccess = false;
            String localDestiny = absolute(job.outfolder);
            File localDestinyFolder = new File(localDestiny);
            if (!localDestinyFolder.exists()) {
                localDestinyFolder.mkdirs();
            }

            if (jobSuccess) {
                copySuccess = Runtime.getRuntime().exec("scp -q -r " + ip + ":" + tempOut + "/* " + localDestiny).waitFor() == 0;
            }
            if (copySuccess || !jobSuccess) {
                try {
                    // best effort -- no problem if it fails
                    Runtime.getRuntime().exec("ssh " + ip + " rm -rf " + tempOut);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return jobSuccess && copySuccess;
        }

        private boolean checkConnection() {
            try {
                int status = Runtime.getRuntime().exec("ssh " + ip + " ls").waitFor();
                return status == 0;
            } catch (Exception ex) {
                return false;
            }
        }
    }

    private static String absolute(String path) {
        String home = System.getProperty("user.home");
        if (!path.startsWith(home)) {
            path = home + "/" + path + "/";
        }
        if (path.endsWith("/")) {
            return path;
        } else {
            return path + "/";
        }
    }

    public class ConillonRunner extends JobRunner {

        public ConillonRunner(int id) {
            super("conillon", id);
        }

        @Override
        public boolean runJob(Job job) throws Exception {
            // add conillon config if it doesnt exist
            String newParams = job.params;
            if (!newParams.contains("conillon.params")) {
                newParams = StringUtils.replaceOnce(newParams, "-p parent", "-p parent=build/classes/mase/conillon/conillon.params -p parent");
            }
            process = Runtime.getRuntime().exec("java -cp build/classes:lib/* mase.MaseEvolve -out " + job.outfolder + " " + newParams);
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            return process.waitFor() == 0;
        }

    }
}
