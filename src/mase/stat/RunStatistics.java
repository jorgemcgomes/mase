/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import ec.EvolutionState;
import ec.Statistics;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author jorge
 */
public class RunStatistics extends Statistics {

    public static final String P_FILE = "file";
    private int log;
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        File file = state.parameters.getFile(base.push(P_FILE), null);
        try {
            log = state.output.addLog(file, false);
        } catch (IOException ex) {
            state.output.fatal("An IOException occurred while trying to create the log " + file);
        }
    }

    @Override
    public void preInitializationStatistics(EvolutionState state) {
        super.preInitializationStatistics(state);

        state.output.println("# " + getComputerName(), log);
        state.output.println("# Generation " + state.generation + " " + new Date().toString(), log);
        
        // Fetch config file
        String[] args = state.runtimeArguments;
        String f = null;
        for(int i = 0 ; i < args.length ; i++) {
            if(args[i].equalsIgnoreCase("-file")) {
                f = args[i + 1];
                break;
            }
        }
        File file = f != null ? new File(f) : null;
        // Write config file to log
        if(file != null && file.exists()) {
            try {
                state.output.println("\n# " + file.getName(), log);
                String content = FileUtils.readFileToString(file);
                state.output.println(content+"\n", log);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void finalStatistics(EvolutionState state, int result) {
        super.finalStatistics(state, result);
        state.output.println("# Generation " + state.generation + " " + new Date().toString(), log);
    }
    
    public static String getComputerName() {
        String result = System.getProperty("user.name") + " @ ";
        try {
            result += InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            result += "Unknown";
        }
        return result;
    }    

}
