/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import ec.EvolutionState;
import ec.Statistics;
import ec.util.Parameter;

/**
 *
 * @author jorge
 */
public class FileWriterStat extends Statistics {

    private static final long serialVersionUID = 1L;

    protected String jobPrefix;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        int jobN = (Integer) state.job[0];
        jobPrefix = "job." + jobN + ".";
    }

}
