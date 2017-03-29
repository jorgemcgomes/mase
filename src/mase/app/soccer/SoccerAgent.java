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
import mase.mason.world.GeomUtils.Segment;
import mase.mason.world.SmartAgent;
import mase.mason.world.MultilineObject;
import mase.mason.world.PointObject;
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
    protected List<SoccerAgent> teamMates; // other players of my team
    protected List<SoccerAgent> ownTeam; // my team, including myself
    protected List<SoccerAgent> oppTeam; // the opponents
    protected List<SoccerAgent> others; // all other players
    protected List<SoccerAgent> all; // all players, including myself
    protected PointObject ownGoal, oppGoal;
    protected Color teamColor;
    protected Segment ownGoalSegment, oppGoalSegment;

    public SoccerAgent(Soccer sim, AgentController ac, double moveSpeed, double kickSpeed) {
        super(sim, sim.field, sim.par.agentRadius, Color.WHITE, ac);
        this.setCollidableTypes(SoccerAgent.class, MultilineObject.class);
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
    
    public void setTeamContext(List<SoccerAgent> ownTeam, List<SoccerAgent> opponents, PointObject ownGoal, PointObject oppGoal, Color teamColor) {
        this.teamColor = teamColor;
        this.setColor(teamColor);
        this.ownTeam = ownTeam;
        this.teamMates = new ArrayList<>(ownTeam);
        this.teamMates.remove(this);
        this.oppTeam = opponents;
        this.ownGoal = ownGoal;
        this.oppGoal = oppGoal;
        this.others = new ArrayList<>(this.teamMates);
        this.others.addAll(oppTeam);
        this.all = new ArrayList<>(this.ownTeam);
        this.all.addAll(oppTeam);
        double w = ((Soccer) sim).par.goalWidth;
        this.ownGoalSegment = new Segment(new Double2D(ownGoal.getLocation().x,ownGoal.getLocation().y-w/2), 
                new Double2D(ownGoal.getLocation().x,ownGoal.getLocation().y+w/2));
        this.oppGoalSegment = new Segment(new Double2D(oppGoal.getLocation().x,oppGoal.getLocation().y-w/2), 
                new Double2D(oppGoal.getLocation().x,oppGoal.getLocation().y+w/2));
    }

    @Override
    public void step(SimState state) {
        Soccer soc = (Soccer) state;
        // is the ball within kicking distance?
        hasPossession = this.distanceTo(soc.ball) <= soc.par.agentKickDistance;
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

    @Override
    protected boolean isValidMove(Double2D target) {
        Soccer soc = (Soccer) sim;
        // do not allow to move through the goals
        return super.isValidMove(target) && target.x > 0 && target.x < soc.par.fieldLength;
    }
    
    

    protected void kickBall(double kickDir, double kickPower) {
        if (!justKicked && hasPossession) {
            justKicked = true;
            Soccer soc = (Soccer) sim;
            soc.ball.kick(this, kickDir, kickPower);
        }
    }
}
