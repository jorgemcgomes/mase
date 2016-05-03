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
    protected int games = 1;
    protected Color teamPossession = null;
    protected long gameStart = 0;
    protected Double2D lastBallPos;
    protected int ballStuckTime = 0;
    
    @Override
    public void step(SimState state) {
        Soccer soc = (Soccer) state;
        /*
        Determine which the team has the possession
        */
        for(SoccerAgent ag : soc.all) {
            if(ag.hasPossession) {
                teamPossession = ag.teamColor;
                break;
            }
        }
        
        /*
        Check if the ball is stuck and the game needs to be reset
        */
        if(lastBallPos == null) {
            lastBallPos = soc.ball.getLocation();
            ballStuckTime = 0;
        } else {
            if(soc.ball.getLocation().distance(lastBallPos) > soc.par.ballStuckMovement) {
                lastBallPos = soc.ball.getLocation();
                ballStuckTime = 0;
            } else {
                ballStuckTime++;
            }
            if(ballStuckTime > soc.par.ballStuckTime) {
                restart(soc, teamPossession == null ? state.random.nextBoolean() : soc.rightTeamColor == teamPossession);
                return;
            }
        }

        /*
        Check if a goal was scored or the game max time was reached
        */
        if(soc.ball.getLocation().x < 0) {
            rightTeamScore++;
            restart(soc, true);
        } else if(soc.ball.getLocation().x > soc.field.width) {
            leftTeamScore++;
            restart(soc, false);
        } else if(state.schedule.getSteps() - gameStart > soc.par.maxGameDuration) {
            restart(soc, teamPossession == null ? state.random.nextBoolean() : soc.rightTeamColor == teamPossession);
        }
    }
    
    private void restart(Soccer soc, boolean leftStart) {
        soc.resetTeams(leftStart);
        lastBallPos = null;
        gameStart = soc.schedule.getSteps();
        teamPossession = null;
        games++;
    }
    
}
