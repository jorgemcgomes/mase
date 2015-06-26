/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

    public static void main(String[] args) throws IOException {
        File outDir = getOutDir(args);
        if (!outDir.exists()) {
            outDir.mkdirs();
        } else {
            System.out.println("Folder already exists: " + outDir.getAbsolutePath() + ". Waiting 5 sec.");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
            }
        }
        
        System.out.println(Thread.currentThread().getName());

        // Get config file
        Map<String, String> pars = readParams(args);
        File config = writeConfig(args, pars, outDir);
        
        // DIRTY FIX FOR JBOT INTEGRATION
        // Does nothing when jbot is not used
        if(pars.containsKey("problem.jbot-config")) {
            File jbot = new File(pars.get("problem.jbot-config"));
            FileUtils.copyFile(jbot, new File(outDir, jbot.getName()));
        }
            
        Thread t = launchExperiment(config);
        System.out.println(t.getName());
    }
    
    public static File getOutDir(String[] args) {
        File outDir = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(OUT_DIR_OPTION)) {
                outDir = new File(args[i + 1]);
            }
        }
        if(outDir == null) {
            System.out.println("-out parameter not found. Going with default: " + OUT_DIR_DEFAULT);
            outDir = OUT_DIR_DEFAULT;
        }
        return outDir;
    }    

    public static Thread launchExperiment(File config) throws IOException {
        // Call ec.Evolve
        final String[] args = new String[]{"-file", config.getAbsolutePath()};
        
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                ec.Evolve.main(args);
            }
        });
        t.start();
        return t;
    }

    public static Map<String, String> readParams(String[] args) throws IOException {
        // Write command line parameters in a temp file
        File tempFile = new File("temp" + (int) (Math.random() * 10000)); // Generate random file name
        BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-p")) {
                bw.write(args[i + 1]);
                bw.newLine();
            } else if (args[i].equals("-file")) {
                bw.write("parent.0=" + args[i + 1]);
                bw.newLine();
            }
        }
        bw.close();

        // Load all the parameter files
        Map<String, String> pars = new LinkedHashMap<String, String>();
        loadParams(pars, tempFile);
        tempFile.delete();
        return pars;
    }

    private static void loadParams(Map<String, String> pars, File file) throws FileNotFoundException, IOException {
        // Read the file contents
        Map<String, String> filePars = new LinkedHashMap<String, String>();
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

    public static File writeConfig(String[] args, Map<String, String> params, File outDir) throws IOException {
        File configFile = new File(outDir, "config.params");

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
            if (value.contains("$")) {
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
