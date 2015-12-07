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
    private static final long serialVersionUID = 1L;
    protected File outFile;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        outFile = state.parameters.getFile(base.push(P_FILE), null);
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postEvaluationStatistics(state);
        if (state.generation == state.numGenerations - 1) {
            File out = new File(outFile.getParent(), jobPrefix + state.generation + "." + outFile.getName());
            try {
                ObjectOutputStream s
                        = new ObjectOutputStream(
                                new GZIPOutputStream(
                                        new BufferedOutputStream(
                                                new FileOutputStream(out))));
                s.writeObject(state);
                s.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
