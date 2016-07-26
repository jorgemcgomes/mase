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
    protected int stuckRestarts = 0;
    protected int timeOuts = 0;
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
        The ball is stuck if it is near a player and hasnt moved for a given amount of time
        */
        if(lastBallPos == null) {
            lastBallPos = soc.ball.getLocation();
            ballStuckTime = 0;
        } else {
            boolean close = false;
            for(SoccerAgent ag : soc.all) {
                if(ag.hasPossession || ag.distanceTo(soc.ball.getLocation()) < ag.getRadius() * 2) {
                    close = true;
                    break;
                }
            }
            if(!close || soc.ball.getLocation().distance(lastBallPos) > soc.par.ballStuckMovement) {
                lastBallPos = soc.ball.getLocation();
                ballStuckTime = 0;
            } else {
                ballStuckTime++;
            }
            if(ballStuckTime > soc.par.ballStuckTime) {
                // give the ball to the other team
                stuckRestarts++;
                restart(soc, !leftClosest(soc));
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
            timeOuts++;
            restart(soc, !leftClosest(soc));
        }
    }
    
    
    private boolean leftClosest(Soccer soc) {
        double leftD = Double.POSITIVE_INFINITY;
        double rightD = Double.POSITIVE_INFINITY;
        for(SoccerAgent ag : soc.leftTeam) {
            leftD = Math.min(leftD, ag.distanceTo(soc.ball.getLocation()));
        }
        for(SoccerAgent ag : soc.rightTeam) {
            rightD = Math.min(rightD, ag.distanceTo(soc.ball.getLocation()));
        }        
        return leftD < rightD;
    }
    
    private void restart(Soccer soc, boolean leftStart) {
        soc.resetTeams(leftStart);
        lastBallPos = null;
        gameStart = soc.schedule.getSteps();
        teamPossession = null;
        games++;
    }
    
}
