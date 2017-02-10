/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mase.app.keepaway;

import mase.controllers.GroupController;

/**
 *
 * @author jorge
 */
public class CompetitiveKeepawaySimulator extends KeepawaySimulator {

    private static final long serialVersionUID = 1L;

    @Override
    public CompetitiveKeepaway createSimState(GroupController gc, long seed) {
        return new CompetitiveKeepaway(seed, par, gc);
    }
}
