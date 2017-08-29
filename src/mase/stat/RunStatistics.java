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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

/**
 *
 * @author jorge
 */
public class RunStatistics extends Statistics {

    public static final String P_FILE = "file";
    private static final long serialVersionUID = 1L;
    private int log;
    protected File file;
    
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        file = state.parameters.getFile(base.push(P_FILE), null);
        try {
            log = state.output.addLog(file, true);
        } catch (IOException ex) {
            state.output.fatal("An IOException occurred while trying to create the log " + file);
        }
    }

    @Override
    public void preInitializationStatistics(EvolutionState state) {
        super.preInitializationStatistics(state);

        state.output.println("# " + getComputerName(), log);
        state.output.println("# Generation " + state.generation + " " + new Date().toString(), log);
        
        // Entire parameter tree
        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);
        writer.write("# Definitive parameter list\n");
        state.parameters.list(writer, false);
        writer.write("\n# Original parameter tree (shadowed included)\n");
        state.parameters.list(writer, true);
        state.output.println(sw.toString() + "\n", log);
    }
    
    @Override
    public void finalStatistics(EvolutionState state, int result) {
        super.finalStatistics(state, result);
        state.output.println("# Generation " + state.generation + " " + new Date().toString(), log);
        
        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);
        sw.write("\n# Gotten parameters\n");
        state.parameters.listGotten(writer);
        sw.write("\n# Not-gotten parameaters\n");
        state.parameters.listNotAccessed(writer);
        state.output.println(sw.toString(), log);
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
