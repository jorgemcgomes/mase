/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.indiana;

import mase.generic.SemiGenericEvaluator;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class IndianaSemiGeneric extends SemiGenericEvaluator {

    @Override
    protected void preSimulation() {
        super.preSimulation();

        // agents
        Indiana ind = (Indiana) sim;
        IndianaAgent[] ags = new IndianaAgent[ind.agents.size()];
        ind.agents.toArray(ags);
        super.addAgentGroup(ags);

        // walls
        Double2D[] obsStarts, obsEnds;
        obsStarts = new Double2D[5];
        obsEnds = new Double2D[5];
        obsStarts[0] = new Double2D(0, 0);
        obsEnds[0] = new Double2D(ind.par.size, 0);
        obsStarts[1] = obsEnds[0];
        obsEnds[1] = new Double2D(ind.par.size, ind.par.size);
        obsStarts[2] = obsEnds[1];
        obsEnds[2] = new Double2D(0, ind.par.size);
        obsStarts[3] = obsEnds[2];
        obsEnds[3] = new Double2D(0, ind.par.size / 2 + ind.par.gateSize / 2);
        obsStarts[4] = new Double2D(0, ind.par.size / 2 - ind.par.gateSize / 2);
        obsEnds[4] = obsStarts[0];
        super.addEnvironmentFeature(new PolygonFeature(obsStarts, obsEnds));

        // gate
        Double2D[] gateStart = new Double2D[]{new Double2D(0, ind.par.size / 2.0 - ind.par.gateSize / 2.0)};
        Double2D[] gateEnd = new Double2D[]{new Double2D(0, ind.par.size / 2.0 + ind.par.gateSize / 2.0)};
        super.addEnvironmentFeature(new PolygonFeature(gateStart, gateEnd));
    }

}
