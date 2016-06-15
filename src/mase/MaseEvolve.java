/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase;

import ec.Evolve;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jorge
 */
public class MaseEvolve {

    public static final String OUT_DIR_OPTION = "-out";
    public static final File OUT_DIR_DEFAULT = new File("exps");
    public static final String DEFAULT_CONFIG = "config.params";
    public static final String FORCE = "-force";

    public static void main(String[] args) throws Exception {
        File outDir = getOutDir(args);
        boolean force = Arrays.asList(args).contains(FORCE);
        if (!outDir.exists()) {
            outDir.mkdirs();
        } else if (!force) {
            System.out.println("Folder already exists: " + outDir.getAbsolutePath() + ". Waiting 5 sec.");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
            }
        }

        // Get config file
        Map<String, String> pars = readParams(args);

        // Copy config to outdir
        try {
            File rawConfig = writeConfig(args, pars, outDir, false);
            File destiny = new File(outDir, DEFAULT_CONFIG);
            destiny.delete();
            FileUtils.moveFile(rawConfig, new File(outDir, DEFAULT_CONFIG));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // JBOT INTEGRATION: copy jbot config file to the outdir
        // Does nothing when jbot is not used
        if (pars.containsKey("problem.jbot-config")) {
            File jbot = new File(pars.get("problem.jbot-config"));
            FileUtils.copyFile(jbot, new File(outDir, jbot.getName()));
        }

        // Write config to system temp file
        File config = writeConfig(args, pars, outDir, true);
        // Launch
        launchExperiment(config);
    }

    public static File getOutDir(String[] args) {
        File outDir = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(OUT_DIR_OPTION)) {
                outDir = new File(args[i + 1]);
            }
        }
        if (outDir == null) {
            System.out.println("-out parameter not found. Going with default: " + OUT_DIR_DEFAULT);
            outDir = OUT_DIR_DEFAULT;
        }
        return outDir;
    }

    public static Thread launchExperiment(File config) throws IOException {
        // Call ec.Evolve
        final String[] args = new String[]{Evolve.A_FILE, config.getAbsolutePath()};
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                ec.Evolve.main(args);
            }
        });
        t.start();
        return t;
    }

    public static Map<String, String> readParams(String[] args) throws Exception {
        // Reads command line parameters to a map
        // Checks the parent order
        Map<String, String> argsPars = new LinkedHashMap<>();
        int parentOrder = 0;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-p")) {
                String par = args[i + 1];
                String[] kv = par.split("=");
                if (kv[0].startsWith("parent")) { // Parent parameter
                    if (kv[0].equals("parent")) { // Parent with no number
                        argsPars.put("parent." + parentOrder, kv[1]);
                        parentOrder++;
                    } else { // Numbered parent
                        String[] split = kv[0].split("\\.");
                        int num = Integer.parseInt(split[1]);
                        if (num != parentOrder) {
                            throw new Exception("Parent out of order: " + par);
                        }
                        argsPars.put("parent." + num, kv[1]);
                        parentOrder++;
                    }
                } else { // Not a parent parameter
                    argsPars.put(kv[0], kv[1]);
                }
            } else if (args[i].equals("-file")) {
                argsPars.put("parent.0", args[i + 1]);
            }
        }

        // Write command line parameters to a temp file -- the root file
        File tempFile = File.createTempFile("masetemp", ".params", new File("."));        
        BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
        for (Entry<String, String> e : argsPars.entrySet()) {
            bw.write(e.getKey() + " = " + e.getValue());
            bw.newLine();
        }
        bw.close();

        // Load all the parameter files
        Map<String, String> pars = new LinkedHashMap<>();
        loadParams(pars, tempFile);
        tempFile.delete();
        return pars;
    }

    private static void loadParams(Map<String, String> pars, File file) throws FileNotFoundException, IOException {
        // Read the file contents
        Map<String, String> filePars = new LinkedHashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = null;
        while ((line = br.readLine()) != null) {
            if (!line.startsWith("#") && !line.trim().isEmpty()) {
                String[] kv = line.split("=");
                filePars.put(kv[0].trim(), kv[1].trim());
            }
        }
        br.close();

        // Read the parents
        for (int i = 100; i >= 0; i--) {
            String parentNum = "parent." + i;
            if (filePars.containsKey(parentNum)) {
                String path = filePars.get(parentNum);
                File parent = new File(file.getParentFile(), path);
                loadParams(pars, parent);
            }
        }

        // All the parents have been processed at this point
        System.out.println("Writting: " + file.getAbsolutePath());
        for (Entry<String, String> e : filePars.entrySet()) {
            if (!e.getKey().startsWith("parent")) {
                pars.put(e.getKey(), e.getValue());
            }
        }
    }

    public static File writeConfig(String[] args, Map<String, String> params, File outDir, boolean replaceDirWildcard) throws IOException {
        File configFile = File.createTempFile("maseconfig", ".params");

        // Write the parameter map into a new file
        BufferedWriter bw = new BufferedWriter(new FileWriter(configFile));

        // Write the command line arguments in a comment
        bw.write("#");
        for (String arg : args) {
            bw.write(" " + arg);
        }
        bw.newLine();

        // Write the parameters correctly padded
        int maxLen = -1;
        for (String str : params.keySet()) {
            maxLen = Math.max(maxLen, str.length());
        }

        for (Entry<String, String> e : params.entrySet()) {
            String value = e.getValue();
            if (value.contains("$") && replaceDirWildcard) {
                File f = new File(outDir, value.replace("$", ""));
                value = f.getAbsolutePath();
            }
            bw.write(StringUtils.rightPad(e.getKey(), maxLen) + " = " + value);
            bw.newLine();
        }
        bw.close();

        return configFile;
    }
}
