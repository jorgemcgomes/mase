/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.keepaway;

import mase.generic.SemiGenericEvaluator;

/**
 *
 * @author Jorge
 */
public class KeepawaySemiGeneric extends SemiGenericEvaluator {

    @Override
    protected void preSimulation() {
        super.preSimulation();
        Keepaway kw = (Keepaway) sim;
        Keeper[] kA = new Keeper[kw.keepers.size()];
        kw.keepers.toArray(kA);
        super.addAgentGroup(kA);
        Taker[] tA = new Taker[kw.takers.size()];
        kw.takers.toArray(tA);
        super.addAgentGroup(tA);
        super.addEnvironmentFeature(new AgentFeature(kw.ball));
    }
}
