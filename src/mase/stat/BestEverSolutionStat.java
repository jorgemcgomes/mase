/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.stat;

import ec.EvolutionState;
import ec.util.Parameter;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import mase.stat.StatUtils.IndividualInfo;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
 */
public class BestEverSolutionStat extends FileWriterStat {

    public static final String P_FILE = "file";
    public static final String P_DO_SUBPOPS = "do-subpops";
    private static final long serialVersionUID = 1L;
    private File baseFile;
    protected File bestFile[];
    protected double bestSoFar[];
    private boolean doSubpops = false;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        baseFile = state.parameters.getFile(base.push(P_FILE), null);
        doSubpops = state.parameters.getBoolean(base.push(P_DO_SUBPOPS), null, false);
    }

    @Override
    public void postInitializationStatistics(EvolutionState state) {
        super.postInitializationStatistics(state);
        if (doSubpops) {
            bestSoFar = new double[state.population.subpops.length];
            Arrays.fill(bestSoFar, Double.NEGATIVE_INFINITY);
            bestFile = new File[state.population.subpops.length];
            for (int i = 0; i < state.population.subpops.length; i++) {
                bestFile[i] = new File(baseFile.getParent(), jobPrefix + FilenameUtils.getBaseName(baseFile.getName())
                        + "_" + i + FilenameUtils.EXTENSION_SEPARATOR_STR + FilenameUtils.getExtension(baseFile.getName()));
            }
        } else {
            bestSoFar = new double[]{Double.NEGATIVE_INFINITY};
            bestFile = new File[]{new File(baseFile.getParent(), jobPrefix + baseFile.getName())};
        }
    }

    @Override
    public void postEvaluationStatistics(EvolutionState state) {
        super.postInitializationStatistics(state);
        if (doSubpops) {
            IndividualInfo[] bests = StatUtils.getSubpopBests(state);
            for(int i = 0 ; i < bests.length ; i++) {
                if(bests[i].fitness > bestSoFar[i]) {
                    bestSoFar[i] = bests[i].fitness;
                    PersistentSolution c = SolutionPersistence.createPersistentController(state, bests[i].ind, bests[i].sub, bests[i].index);
                    try {
                        SolutionPersistence.writeSolution(c, bestFile[i]);
                    } catch (IOException ex) {
                        Logger.getLogger(BestEverSolutionStat.class.getName()).log(Level.SEVERE, null, ex);
                    }                    
                }
            }
        } else {
            IndividualInfo best = StatUtils.getBest(state);
            if (best.fitness > bestSoFar[0]) {
                bestSoFar[0] = best.fitness;
                PersistentSolution c = SolutionPersistence.createPersistentController(state, best.ind, best.sub, best.index);
                try {
                    SolutionPersistence.writeSolution(c, bestFile[0]);
                } catch (IOException ex) {
                    Logger.getLogger(BestEverSolutionStat.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
