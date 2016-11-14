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
    private int lines = 3;
    private boolean mute = false;

    public MaseManagerTerminal(MaseManager mng) {
        this.mng = mng;
    }

    public void run(File runnersFile, File jobsFile) {
        try {
            if (runnersFile != null) {
                mng.loadRunners(runnersFile);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            if (jobsFile != null) {
                mng.loadJobs(jobsFile);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        final Scanner lineSC = new Scanner(System.in);

        while (true) {
            System.out.print("> ");
            String option = lineSC.next();
            Scanner sc = new Scanner(lineSC.nextLine());
            try {
                switch (option) {
                    case "addrunner":
                        mng.addRunner(sc.nextLine());
                        break;
                    case "loadrunners":
                        while (sc.hasNext()) {
                            mng.loadRunners(new File(sc.next()));
                        }
                        break;
                    case "addjobs":
                        mng.addJob(sc.nextLine());
                        break;
                    case "loadjobs":
                        while (sc.hasNext()) {
                            mng.loadJobs(new File(sc.next()));
                        }
                        break;
                    case "remove":
                        while (sc.hasNext()) {
                            mng.removeFromWaiting(sc.next());
                        }                        
                        break;
                    case "killrunner":
                        while (sc.hasNextInt()) {
                            mng.killRunner(sc.nextInt());
                        }
                        break;
                    case "kill":
                        while (sc.hasNext()) {
                            mng.killJob(sc.next());
                        }
                        break;
                    case "killall":
                        mng.failed.addAll(mng.waitingList);
                        mng.waitingList.clear();
                        List<Job> running = new ArrayList<>(mng.running.values());
                        for (Job j : running) {
                            mng.killJob(j.id);
                        }
                        break;
                    case "output":
                        int id = sc.nextInt();
                        int l = sc.hasNextInt() ? sc.nextInt() : lines;
                        System.out.println(mng.getOutput(id, l));
                        break;
                    case "jobs":
                        while (sc.hasNext()) {
                            String jobid = sc.next();
                            List<Job> found = mng.findJobs(jobid);
                            for (Job j : found) {
                                System.out.println(j.detailedToString() + "\n-----------------");
                            }
                        }
                        break;
                    case "status":
                        int ls = sc.hasNextInt() ? sc.nextInt() : lines;
                        System.out.println("Completed: " + mng.completed.size() + "\tWaiting: " + mng.waitingList.size() + "\tFailed: " + mng.failed.size() + "\tRunning: " + mng.running.size() + "/" + mng.runners.size() + " " + (mng.runningStatus() ? "(ACTIVE)" : "(PAUSED)"));
                        for (Entry<JobRunner, Job> e : mng.running.entrySet()) {
                            System.out.println("== " + e.getValue() + " @ " + e.getKey() + " ========");
                            System.out.println(mng.getOutput(e.getKey().id, ls));
                        }
                        break;
                    case "list":
                        while (sc.hasNext()) {
                            String t = sc.next();
                            if (t.equals("failed")) {
                                for(int i = mng.failed.size() - 1 ; i >= 0 ; i--) {
                                    Job j = mng.failed.get(i);
                                    System.out.println(df.format(j.submitted) + " " + j);
                                }
                            } else if (t.equals("completed")) {
                                for(int i = mng.completed.size() - 1 ; i >= 0 ; i--) {
                                    Job j = mng.completed.get(i);
                                    System.out.println(df.format(j.submitted) + " " + j);
                                }
                            } else if (t.equals("waiting")) {
                                for(int i = mng.waitingList.size() - 1 ; i >= 0 ; i--) {
                                    Job j = mng.waitingList.get(i);
                                    System.out.println(df.format(j.submitted) + " " + j);
                                }
                            } else if (t.equals("runners")) {
                                for (JobRunner r : mng.runners.values()) {
                                    if (mng.running.containsKey(r)) {
                                        Job runningJob = mng.running.get(r);
                                        System.out.println(df.format(runningJob.started) + " " + r + " @ " + runningJob);
                                    } else {
                                        System.out.println("Idle     " + r);
                                    }
                                }
                            } else {
                                error("Unknown list: " + t);
                            }
                        }
                        break;
                    case "retry":
                        while (sc.hasNext()) {
                            mng.retryJob(sc.next());
                        }
                        break;
                    case "retryfailed":
                        mng.retryFailed();
                        break;
                    case "clear":
                        while (sc.hasNext()) {
                            String t = sc.next();
                            if (t.equals("failed")) {
                                mng.failed.clear();
                            } else if (t.equals("completed")) {
                                mng.completed.clear();
                            } else if (t.equals("waiting")) {
                                mng.waitingList.clear();
                            } else if (t.equals("runners")) {
                                List<Integer> runners = new ArrayList<>(mng.runners.keySet());
                                for (Integer r : runners) {
                                    mng.killRunner(r);
                                }
                            } else {
                                error("Unknown list: " + t);
                            }
                        }
                        break;
                    case "priority":
                        String type = sc.next();
                        while (sc.hasNext()) {
                            String i = sc.next();
                            if (type.equals("top")) {
                                mng.topPriority(i);
                            } else if (type.equals("bottom")) {
                                mng.lowestPriority(i);
                            }
                        }
                        break;
                    case "sort":
                        String sort = sc.next();
                        if (sort.equals("job")) {
                            mng.sortJobFirst();
                        } else if (sort.equals("date")) {
                            mng.sortSubmissionDate();
                        } else {
                            error("Unknown sorting method: " + sort);
                        }
                        break;
                    case "pause":
                        mng.pause(sc.hasNext() && sc.next().equals("force"));
                        break;
                    case "start":
                        mng.resume();
                        break;
                    case "exit":
                        System.exit(0);
                        break;
                    case "set":
                        String par = sc.next();
                        switch (par) {
                            case "lines":
                                lines = sc.nextInt();
                                break;
                            case "maxtries":
                                mng.setMaxTries(sc.nextInt());
                                break;
                        }

                        break;
                    case "mute":
                        this.mute = true;
                        break;
                    case "unmute":
                        this.mute = false;
                        break;
                    case "help":
                        System.out.println("Available commands:\n"
                                + "-- addrunner      runner_type [config]\n"
                                + "-- loadrunners    [file]...\n"
                                + "-- addjobs        job_params\n"
                                + "-- loadjobs       [file]...\n"
                                + "-- killrunner     [runner_id]...\n"
                                + "-- remove         [job_id]...\n"
                                + "-- kill           [job_id]...\n"
                                + "-- killall        \n"
                                + "-- output         runner_id [lines]\n"
                                + "-- jobs           [job_id]...\n"
                                + "-- status         [lines]\n"
                                + "-- list           [waiting|completed|failed|runners]...\n"
                                + "-- retry          [job_id]...\n"
                                + "-- retryfailed    \n"
                                + "-- priority       top|bottom [job_id]...\n"
                                + "-- sort           batch|job|date\n"
                                + "-- clear          [waiting|completed|failed|runners]...\n"
                                + "-- pause          [force]\n"
                                + "-- start          \n"
                                + "-- mute|unmute    \n"
                                + "-- exit           \n"
                                + "-- set            lines|tries value"
                        );
                        break;
                    default:
                        System.out.println("Unknown command. Try help.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void message(String str) {
        if (!mute) {
            System.out.println("[+] " + df.format(new Date()) + " " + str);
        }
    }

    @Override
    public void error(String str) {
        System.out.println("[!] " + df.format(new Date()) + " " + str);
    }
}
