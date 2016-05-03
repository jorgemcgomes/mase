/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.soccer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import mase.controllers.AgentController;
import mase.mason.world.SmartAgent;
import sim.engine.SimState;
import sim.util.Double2D;

/**
 *
 * @author jorge
 */
public class SoccerAgent extends SmartAgent {

    private static final long serialVersionUID = 1L;

    protected boolean justKicked;
    protected boolean hasPossession;
    protected double moveSpeed;
    protected double kickSpeed;
    protected List<SoccerAgent> ownTeam;
    protected List<SoccerAgent> oppTeam;
    protected List<SoccerAgent> others;
    protected Double2D ownGoal, oppGoal;
    protected Color teamColor;

    public SoccerAgent(Soccer sim, AgentController ac, double moveSpeed, double kickSpeed) {
        super(sim, sim.field, sim.par.agentRadius, Color.WHITE, ac);
        this.enableAgentCollisions(true);
        this.enablePolygonCollisions(true);
        this.enableCollisionRebound(true); 
        this.enableRotation(false);
        this.moveSpeed = moveSpeed;
        this.kickSpeed = kickSpeed;
    }
    
    public boolean getHasPossession() {
        return hasPossession;
    }
    
    public boolean getJustKicked() {
        return justKicked;
    }
    
    public void setTeamContext(List<SoccerAgent> teamMates, List<SoccerAgent> opponents, Double2D ownGoal, Double2D oppGoal, Color teamColor) {
        this.teamColor = teamColor;
        this.setColor(teamColor);
        this.ownTeam = new ArrayList<>(teamMates);
        this.ownTeam.remove(this);
        this.oppTeam = opponents;
        this.ownGoal = ownGoal;
        this.oppGoal = oppGoal;
        this.others = new ArrayList<>(ownTeam);
        this.others.addAll(opponents);
    }

    @Override
    public void step(SimState state) {
        Soccer soc = (Soccer) state;
        // is the ball within kicking distance?
        hasPossession = distanceTo(soc.ball.getLocation()) <= soc.par.agentKickDistance;
        // If the ball is stoped or the agent does not have possession, turn-off the just-kicked flag
        if (justKicked && (soc.ball.getCurrentSpeed() < 0.01 || !hasPossession)) {
            justKicked = false;
        }
        super.step(state);
    }

    @Override
    protected boolean move(double orientation, double speed) {
        // do not move if justKicked
        if(!justKicked) {
            return super.move(orientation, speed);
        } else {
            return true;
        }
    }

    protected void kickBall(double kickDir, double kickPower) {
        if (!justKicked && hasPossession && kickPower >= 1) {
            justKicked = true;
            Soccer soc = (Soccer) sim;
            soc.ball.kick(this, kickDir, kickPower);
        }
    }
}
