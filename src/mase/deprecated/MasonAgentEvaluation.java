/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import mase.mason.world.EmboddiedAgent;

/**
 *
 * @author jorge
 */
public abstract class MasonAgentEvaluation extends MasonEvaluation {

    private static final long serialVersionUID = 1L;
    protected EmboddiedAgent agent;

    public void setAgent(EmboddiedAgent ag) {
        this.agent = ag;
    }
}
