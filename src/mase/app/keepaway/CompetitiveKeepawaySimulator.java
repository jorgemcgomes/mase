/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.keepaway;

import mase.controllers.GroupController;
import mase.mason.MasonSimState;

/**
 *
 * @author jorge
 */
public class CompetitiveKeepawaySimulator extends KeepawaySimulator {

    @Override
    public MasonSimState createSimState(GroupController gc, long seed) {
        CompetitiveKeepaway kw = new CompetitiveKeepaway(seed, par, gc);
        return kw;
    }
}
