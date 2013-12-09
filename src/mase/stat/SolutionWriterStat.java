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
}
