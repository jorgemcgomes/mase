/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.soccer;

import java.awt.Color;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class Referee implements Steppable {

    private static final long serialVersionUID = 1L;
    
    protected int leftTeamScore = 0;
    protected int rightTeamScore = 0;
    protected Color teamPossession = null;
    protected Double2D lastBallPos;
    protected int ballStuckTime = 0;
    protected SoccerAgent scorer;
    protected SoccerAgent lastTouched = null;
    
    @Override
    public void step(SimState state) {
        Soccer soc = (Soccer) state;
        /*
        Determine which the team has the possession
        */
        for(SoccerAgent ag : soc.all) {
            if(ag.hasPossession) {
                teamPossession = ag.teamColor;
                lastTouched = ag;
                break;
            }
        }
        
        /*
        Check if the ball is stuck and the game needs to be reset
        The ball is stuck if it is near a player and hasnt moved for a given amount of time
        */
        if(lastBallPos == null) {
            lastBallPos = soc.ball.getCenterLocation();
            ballStuckTime = 0;
        } else {
            boolean close = false;
            for(SoccerAgent ag : soc.all) {
                if(ag.hasPossession || soc.ball.distanceTo(ag) < ag.getRadius() * 2) {
                    close = true;
                    break;
                }
            }
            if(!close || soc.ball.getCenterLocation().distance(lastBallPos) > soc.par.ballStuckMovement) {
                lastBallPos = soc.ball.getCenterLocation();
                ballStuckTime = 0;
            } else {
                ballStuckTime++;
            }
            if(ballStuckTime > soc.par.ballStuckTime) {
                state.kill();
            }
        }

        /*
        Check if a goal was scored or the game max time was reached
        */
        if(soc.ball.getCenterLocation().x < 0) { // right team scored
            rightTeamScore++;
            if(lastTouched != null && lastTouched.teamColor == soc.rightTeamColor) {
                scorer = lastTouched;
            }
            state.kill();
        } else if(soc.ball.getCenterLocation().x > soc.field.width) { // left team scored
            leftTeamScore++;
            if(lastTouched != null && lastTouched.teamColor == soc.leftTeamColor) {
                scorer = lastTouched;
            }
            state.kill();
        }
    }
}
