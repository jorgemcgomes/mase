package mase;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jorge
 */
public class MaseManager {

    protected LinkedBlockingDeque<Job> waitingList = new LinkedBlockingDeque<>();
    protected LinkedBlockingDeque<Job> completed = new LinkedBlockingDeque<>();
    protected LinkedBlockingDeque<Job> failed = new LinkedBlockingDeque<>();
    protected ConcurrentHashMap<Integer, JobRunner> runners = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<JobRunner, Job> running = new ConcurrentHashMap<>();
    private volatile int jobId = 0;
    private volatile int runnerId = 0;
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
                if (!waitingList.isEmpty()) {
                    // find an available runner
                    JobRunner n = null;
                    for (JobRunner r : runners.values()) {
                        if (!running.containsKey(r)) {
                            n = r;
                        }
                    }
                    if (n != null) {
                        final JobRunner nextRunner = n;
                        final Job nextJob = waitingList.pop();
                        running.put(nextRunner, nextJob);
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
                                    completed.offer(nextJob);
                                    listener.message("Job completed: " + nextJob + " @ " + nextRunner);
                                } else {
                                    failed.offer(nextJob);
                                    listener.error("Job failed: " + nextJob + " @ " + nextRunner);
                                }
                            }
                        });
                        t.start();
                    }
                }
                for (JobRunner r : running.keySet()) {
                    r.updateOutput();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void addRunner(String config) throws Exception {
        String[] split = config.trim().split(" ");
        if(split[0].equalsIgnoreCase("local")) {
            LocalRunner lr = new LocalRunner(runnerId++);
            runners.put(lr.id, lr);
        } else if(split[0].equalsIgnoreCase("ssh")) {
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
        } else if(split[0].equalsIgnoreCase("conillon")) {
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
                waitingList.addFirst(cancelled);
            }
        }
    }
    
    public void retry(int id) {
       Job found = null;
       for(Job j : waitingList) {
           if(j.id == id) {
               listener.error("Job already in waiting list: " + j);
               return;
           }
       }
       for(Job j : failed) {
           if(j.id == id) {
               waitingList.add(j);
               failed.remove(j);
               return;
           }
       }
       for(Job j : completed) {
           if(j.id == id) {
               waitingList.add(j);
               completed.remove(j);
               return;
           }
       }
       if(found == null) {
           listener.error("Job not found: " + id);
       }
    }
    
    public void retryFailed() {
        for(Job j : failed) {
            waitingList.add(j);
        }
        listener.message("Added " + failed.size() + " jobs to waiting list");
        failed.clear();
    }

    public void addJob(Job job) {
        job.id = jobId++;
        job.submitted = new Date();
        waitingList.add(job);
    }

    public void killJob(int id) {
        for (Job j : waitingList) {
            if (j.id == id) {
                waitingList.remove(j);
                return;
            }
        }
        for (Entry<JobRunner, Job> e : running.entrySet()) {
            if (e.getValue().id == id) {
                e.getKey().interrupt();
                return;
            }
        }
        listener.error("Job " + id + " not found.");
    }

    public String getOutput(int id, int maxLines) throws Exception {
        JobRunner get = runners.get(id);
        return get.getOutput(maxLines);
    }

    public List<Job> parse(String args) {
        String out = StringUtils.substringBetween(args, "-out ", " ");
        int jobs = Integer.parseInt(StringUtils.substringBetween(args, "-p jobs=", " "));
        int currentJob = 0;
        if (args.contains("-p current-job")) {
            currentJob = Integer.parseInt(StringUtils.substringBetween(args, "-p current-job=", " "));
        }
        boolean checkRepetitions = !args.contains("-force");
        
        String clean = StringUtils.removePattern(args, "-p jobs=\\d+");
        clean = StringUtils.removePattern(clean, "-p current-job=\\d+");
        clean = StringUtils.removePattern(clean, "-out\\s\\S+\\s");

        ArrayList<Job> newJobs = new ArrayList<>();
        for (int j = currentJob; j < jobs; j++) {
            File check = new File(out, "job."+j+".postfitness.stat");
            File checkAlt = new File(out.replace("/home/jorge/", ""), "job."+j+".postfitness.stat"); // non-absolute path
            if((check.exists() || checkAlt.exists()) && checkRepetitions) {
                listener.message("Job already completed, not adding: " + out + " (" + j + ")");
            } else {
                String cur = "-out " + out + " -p current-job=" + j + " -p jobs=" + (j + 1) + clean + " -force";
                Job newJob = new Job();
                newJob.outfolder = new File(out);
                newJob.params = cur;
                newJob.jobNumber = j;
                newJobs.add(newJob);                
            }
        }
        return newJobs;
    }
    
    public interface StatusListener {
        
        public void message(String str);
        
        public void error(String str);
        
    }

    public static class Job {

        int id;
        String params;
        File outfolder;
        int jobNumber;
        Date submitted;
        Date started;
        Date completed;

        @Override
        public String toString() {
            return id + ":" + outfolder.getPath() + "(" + jobNumber + ")";
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
            while(buffer.size() > maxSize) {
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

        public LocalRunner(int id) {
            super("local", id);
        }

        @Override
        public boolean runJob(Job job) throws IOException, InterruptedException {
            process = Runtime.getRuntime().exec("java -cp build/classes:dist/lib/* mase.MaseEvolve " + job.params);
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            return process.waitFor() == 0;
        }
    }

    public class SSHRunner extends JobRunner {

        private final String ip;

        public SSHRunner(String ip, int id) throws Exception {
            super("ssh"+ip, id);
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
            String tempOut = "masetemp/" + RandomStringUtils.randomAlphabetic(6);
            String newPar = StringUtils.replacePattern(job.params, "-out\\s\\S+\\s", "-out " + tempOut + " ");

            process = Runtime.getRuntime().exec("ssh -t -t " + ip + " java -cp build/classes:lib/* mase.MaseEvolve " + newPar);
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            err = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            boolean success = false;
            if (process.waitFor() == 0) {
                success = Runtime.getRuntime().exec("scp -q -r " + ip + ":" + tempOut + "/* " + job.outfolder).waitFor() == 0;
            }
            try {
                // best effort -- no problem if it fails
                Runtime.getRuntime().exec("ssh " + ip + " rm -rf " + tempOut);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return success;
        }
    }

    public class ConillonRunner extends JobRunner {

        public ConillonRunner(int id) {
            super("conillon", id);
        }

        @Override
        public boolean runJob(Job job) throws Exception {
            // remove absolute path if it exists
            String newParams = job.params.replace("/home/jorge/", "");
            // add conillon config if it doesnt exist
            if(!newParams.contains("conillon.params")) {
                newParams = StringUtils.replaceOnce(newParams, "-p parent", "-p parent=build/classes/mase/conillon/conillon.params -p parent");                
            }
            process = Runtime.getRuntime().exec("java -cp build/classes:lib/* mase.MaseEvolve " + newParams);
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            return process.waitFor() == 0;
        }

    }
}
