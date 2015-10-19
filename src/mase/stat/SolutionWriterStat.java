/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import ec.EvolutionState;
import ec.Statistics;
import ec.util.Parameter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author jorge
 */
public class SolutionWriterStat extends Statistics {

    protected String jobPrefix;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        if (state.parameters.getIntWithDefault(new Parameter("jobs"), null, 1) > 1) {
            int jobN = (Integer) state.job[0];
            jobPrefix = "job." + jobN + ".";
        } else {
            jobPrefix = "";
        }
    }

    public static TarArchiveOutputStream reopen(File file) {
        try {
            File temp = File.createTempFile("tararchive", ".gz");
            FileUtils.copyFile(file, temp);
            TarArchiveInputStream tais = new TarArchiveInputStream(
                    new GZIPInputStream(
                            new BufferedInputStream(new FileInputStream(temp))));
            file.delete();
            TarArchiveOutputStream taos = new TarArchiveOutputStream(
                    new GZIPOutputStream(
                            new BufferedOutputStream(new FileOutputStream(file))));

            ArchiveEntry nextEntry;
            while ((nextEntry = tais.getNextEntry()) != null) {
                taos.putArchiveEntry(nextEntry);
                IOUtils.copy(tais, taos, (int) nextEntry.getSize());
                taos.closeArchiveEntry();
            }
            return taos;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
