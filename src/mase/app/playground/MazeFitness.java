/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import mase.evaluation.FitnessResult;
import mase.mason.MasonEvaluation;
import mase.mason.MasonSimState;
import net.jafama.FastMath;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class MazeFitness extends MasonEvaluation<FitnessResult> {

    private static final long serialVersionUID = 1L;

    private FitnessResult res;

    @Override
    protected void postSimulation(MasonSimState sim) {
        super.postSimulation(sim);
        Playground pl = (Playground) sim;
        if (pl.agent.getLocation().x > pl.par.arenaSize || pl.agent.getLocation().x < 0
                || pl.agent.getLocation().y > pl.par.arenaSize || pl.agent.getLocation().y < 0) {
            res = new FitnessResult((2 - pl.schedule.getSteps() / (double) maxSteps) / 2);
        } else {
            Double2D c = new Double2D(pl.par.arenaSize / 2, pl.par.arenaSize / 2);
            double d = pl.agent.distanceTo(c);
            res = new FitnessResult((d / FastMath.sqrt(FastMath.pow2(pl.par.arenaSize / 2) * 2)) / 2);
        }
    }

    @Override
    public FitnessResult getResult() {
        return res;
    }

}
