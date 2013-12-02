/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.sharing;

import mase.generic.SemiGenericEvaluator;

/**
 *
 * @author jorge
 */
public class RSGenericEval extends SemiGenericEvaluator {

    @Override
    protected void preSimulation() {
        super.preSimulation();
        ResourceSharing rs = (ResourceSharing) sim;
        super.addEnvironmentFeature(new PointFeature(rs.resource.getLocation()));
        RSAgent[] agents = new RSAgent[rs.agents.size()];
        rs.agents.toArray(agents);
        super.addAgentGroup(agents);
    }

}
