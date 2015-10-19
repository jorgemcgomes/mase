/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import ec.EvolutionState;
import ec.util.Parameter;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author jorge
 */
public class LastCheckpointStat extends SolutionWriterStat {

    public static final String P_FILE = "file";
    protected File outFile;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        outFile = state.parameters.getFile(base.push(P_FILE), null);
        outFile = new File(outFile.getParent(), jobPrefix + outFile.getName());
    }

    @Override
    public void finalStatistics(EvolutionState state, int result) {
        super.finalStatistics(state, result);
        try {
            ObjectOutputStream s
                    = new ObjectOutputStream(
                            new GZIPOutputStream(
                                    new BufferedOutputStream(
                                            new FileOutputStream(outFile))));

            s.writeObject(state);
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
