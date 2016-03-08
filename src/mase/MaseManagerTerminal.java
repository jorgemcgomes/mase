/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import mase.MaseManager.Job;
import mase.MaseManager.JobRunner;
import mase.MaseManager.StatusListener;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author jorge
 */
public class MaseManagerTerminal implements StatusListener {

    public static void main(String[] args) {
        File r = null, j = null;
        if (args.length > 0) {
            r = new File(args[0]);
        }
        if (args.length > 1) {
            j = new File(args[1]);
        }

        final MaseManager manager = new MaseManager();
        final MaseManagerTerminal terminal = new MaseManagerTerminal(manager);
        manager.setStatusListener(terminal);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                terminal.message("Exiting gracefully");
                manager.cancelAll();
            }
        }));

        manager.startExecution();
        terminal.run(r, j);
    }

    private final DateFormat df = new SimpleDateFormat("HH-mm-ss");
    private final MaseManager mng;
    private int lines = 5;

    public MaseManagerTerminal(MaseManager mng) {
        this.mng = mng;
    }

    public void run(File runnersFile, File jobsFile) {
        try {
            if (runnersFile != null) {
                loadRunners(runnersFile);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            if (jobsFile != null) {
                loadJobs(jobsFile);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        final Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("> ");
            String option = sc.next();
            try {
                switch (option) {
                    case "addrunner":
                        mng.addRunner(sc.nextLine());
                        break;
                    case "loadrunners":
                        loadRunners(new File(sc.nextLine()));
                        break;
                    case "addjobs":case"addjob":
                        List<Job> parsed = mng.parse(sc.nextLine());
                        for (Job j : parsed) {
                            mng.addJob(j);
                        }
                        break;
                    case "loadjobs":
                        File f = new File(sc.nextLine());
                        loadJobs(f);
                        break;
                    case "killrunner":
                        for (int id : expand(sc.nextLine())) {
                            mng.killRunner(id);
                        }
                        break;
                    case "killjob":
                        for (int id : expand(sc.nextLine())) {
                            mng.killJob(id);
                        }
                        break;
                    case "output":
                        String idStr = sc.next();
                        int l = lines;
                        if(sc.hasNextInt()) {
                            l = sc.nextInt();
                        }
                        for(int i : expand(idStr)) {
                            System.out.println(mng.getOutput(i, l));
                        }
                        break;
                    case "status":
                        int ls = lines;
                        if(sc.hasNextInt()) {
                            ls = sc.nextInt();
                        }
                        System.out.println("Completed: " + mng.completed.size() + "\tWaiting: " + mng.waitingList.size() + "\tFailed: " + mng.failed.size() + "\tRunning: " + mng.running.size() + "/" + mng.runners.size());
                        for (Entry<JobRunner, Job> e : mng.running.entrySet()) {
                            System.out.println("== " + e.getValue() + " @ " + e.getKey() + " ========");
                            System.out.println(mng.getOutput(e.getKey().id, ls));
                        }
                        break;
                    case "waitlist":
                        for (Job j : mng.waitingList) {
                            System.out.println(df.format(j.submitted) + " " + j);
                        }
                        break;
                    case "completed":
                        for (Job j : mng.completed) {
                            System.out.println(df.format(j.completed) + " " + j);
                        }
                        break;
                    case "failed":
                        for (Job j : mng.failed) {
                            System.out.println(df.format(j.completed) + " " + j);
                        }
                        break;
                    case "runners": case "running":
                        for (JobRunner r : mng.runners.values()) {
                            if (mng.running.containsKey(r)) {
                                Job runningJob = mng.running.get(r);
                                System.out.println(df.format(runningJob.started) + " " + r + " @ " + runningJob);
                            } else {
                                System.out.println("Idle     " + r);
                            }
                        }
                        break;
                    case "retry":
                        for (int i : expand(sc.nextLine())) {
                            mng.retry(i);
                        }
                        break;
                    case "retryfailed":
                        mng.retryFailed();
                        break;
                    case "clearwaiting":
                        mng.waitingList.clear();
                        break;
                    case "clearfailed":
                        mng.failed.clear();
                        break;
                    case "clearcompleted":
                        mng.completed.clear();
                        break;
                    case "stop":
                        mng.failed.addAll(mng.waitingList);
                        mng.waitingList.clear();
                        for(Job j : mng.running.values()) {
                            mng.killJob(j.id);
                        }
                        break;
                    case "exit":
                        System.exit(0);
                        break;
                    case "setlines":
                        lines = sc.nextInt();
                        break;
                    case "help":
                        System.out.println("Available commands:\n"
                                + "-- addrunner      <runner_type> [config]\n"
                                + "-- loadrunners    <file>\n"
                                + "-- addjobs        <job>\n"
                                + "-- loadjobs       <file>\n"
                                + "-- killrunner     <id_range>\n"
                                + "-- killjob        <id_range>\n"
                                + "-- output         <id_range> [lines]\n"
                                + "-- status         [lines]\n"
                                + "-- waitlist       <>\n"
                                + "-- completed      <>\n"
                                + "-- failed         <>\n"
                                + "-- runners        <>\n"
                                + "-- retry          <id_range>\n"
                                + "-- retryfailed    <>\n"
                                + "-- clearwaiting   <>\n"
                                + "-- clearfailed    <>\n"
                                + "-- clearcompleted <>\n"
                                + "-- stop           <>\n"
                                + "-- exit           <>\n"
                                + "-- setlines       <lines>");
                        break;                        
                    default:
                        System.out.println("Unknown command. Try help.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loadJobs(File f) throws IOException {
        String content = FileUtils.readFileToString(f);
        String[] fileLines = content.split(System.getProperty("line.separator"));
        ArrayList<Job> jobs = new ArrayList<>();
        for (String l : fileLines) {
            if (!l.trim().isEmpty() && !l.startsWith("#")) {
                jobs.addAll(mng.parse(l));
            }
        }
        for (Job j : jobs) {
            mng.addJob(j);
        }
    }

    public void loadRunners(File f) throws Exception {
        String content = FileUtils.readFileToString(f);
        String[] fileLines = content.split(System.getProperty("line.separator"));
        for (String l : fileLines) {
            if (!l.trim().isEmpty() && !l.startsWith("#")) {
                mng.addRunner(l);
            }
        }
    }

    public List<Integer> expand(String s) {
        List<Integer> res = new ArrayList<>();
        String[] commaSplit = s.trim().split(",");
        for (String sp : commaSplit) {
            if (sp.contains("-")) {
                String[] rangeSplit = sp.split("-");
                int start = Integer.parseInt(rangeSplit[0].trim());
                int end = Integer.parseInt(rangeSplit[rangeSplit.length - 1].trim());
                for (int i = start; i <= end; i++) {
                    res.add(i);
                }
            } else {
                int id = Integer.parseInt(sp.trim());
                res.add(id);
            }
        }
        return res;
    }

    @Override
    public void message(String str) {
        System.out.println("[+] " + df.format(new Date()) + " " + str);
    }

    @Override
    public void error(String str) {
        System.out.println("[!] " + df.format(new Date()) + " " + str);
    }
}
