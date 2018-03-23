/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.playground;

import mase.mason.world.SmartAgent;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 *
 * @author jorge
 */
public class MazePlayground extends Playground {

    private static final long serialVersionUID = 1L;

    public MazePlayground(long seed) {
        super(seed);
    }

    @Override
    protected void placeAgent(SmartAgent ag) {
        super.placeAgent(ag);
        ag.virtuallyBoundedArena(false);
        ag.enableCollisionRebound(false);
    }

    @Override
    protected void placeWalls() {
        // no walls
    }

    @Override
    public void start() {
        super.start();
        // stop simulation when the agent escapes
        schedule.scheduleRepeating(new Steppable() {
            @Override
            public void step(SimState state) {
                if (agent.getLocation().x > par.arenaSize || agent.getLocation().x < 0
                        || agent.getLocation().y > par.arenaSize || agent.getLocation().y < 0
                        || agent.isInCollision()) {
                    state.kill();
                }
            }
        });
    }

}
