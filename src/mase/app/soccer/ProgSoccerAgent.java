/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.soccer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import mase.mason.world.StaticPolygon;
import org.apache.commons.lang3.tuple.Pair;
import sim.engine.SimState;
import sim.util.Double2D;

/**
 * If agent has ball: 1 - Shoot to goal if it has clear shot and it is within
 * range 2 - Pass to a teammate that is open and closer to the goal 3 - Drible
 * towards the goal if open 4 - Drible away from closest in front if there is
 * space to do so 5 - Pass to any open teammate 6 - Shoot ball away from closest
 * in front If agent doesnt have the ball: 1 - Move towards the ball if it is
 * the closest one, unless it just passed the ball 2 - Defend the goal if it is
 * the closest to it 3 - If it is too far away from closest teammate, go towards
 * it If on defense: 4 - Mark the nearest opponent that I'm the closest Else: 4
 * - Get open: move away from the agent between the ball and me
 *
 * @author jorge
 */
public class ProgSoccerAgent extends SoccerAgent {

    private static final long serialVersionUID = 1L;
    private Double2D[] goalTargets;
    double kickRange;
    boolean openToDrible;
    double openDistance;
    double openAngle = Math.PI / 3;
    double dribleSpeed;
    boolean justPassed = false;
    double clearDistance;
    
    String lastAction = "";
    /*double[][] kickTable = new double[][]{
        new double[]{0, 1},
        new double[]{10, 1.5},
        new double[]{25, 2},
        new double[]{35, 2.5},
        new double[]{60, 3},
        new double[]{90, 3.5},
        new double[]{110, 4}
    };*/
    double[][] kickTable = new double[][]{
            new double[]{0, 1.5},
            new double[]{10, 2},
            new double[]{25, 2.5},
            new double[]{35, 3},
            new double[]{60, 3.5},
            new double[]{90, 4}
    };
    
    public ProgSoccerAgent(Soccer sim) {
        super(sim, null, sim.par.agentMoveSpeed, sim.par.agentKickSpeed);

        this.openDistance = sim.par.agentRadius * 4; // 2 robots of distance
        this.moveSpeed = sim.par.agentMoveSpeed;
        this.kickSpeed = sim.par.agentKickSpeed;
        this.dribleSpeed = 1.5;
        this.kickRange = kickTable[kickTable.length - 1][0];
        this.clearDistance = sim.par.agentRadius * 2;
    }

    private double getSpeed(double dist) {
        for (int i = kickTable.length - 1; i >= 0; i--) {
            if (dist >= kickTable[i][0]) {
                return kickTable[i][1];
            }
        }
        return 0;
    }

    public String getLastAction() {
        return lastAction;
    }

    public boolean isOpenToDrible() {
        return openToDrible;
    }

    @Override
    public void setTeamContext(List<SoccerAgent> teamMates, List<SoccerAgent> opponents, Double2D ownGoal, Double2D oppGoal, Color teamColor) {
        super.setTeamContext(teamMates, opponents, ownGoal, oppGoal, teamColor);
        this.goalTargets = new Double2D[]{
            oppGoal,
            new Double2D(oppGoal.x, oppGoal.y + (((Soccer) sim).par.goalWidth / 2) * 0.7),
            new Double2D(oppGoal.x, oppGoal.y - (((Soccer) sim).par.goalWidth / 2) * 0.7)
        };
        /*
             int numTargets = 5;
            double startTarget = oppGoal.y - soc.par.goalWidth / 2 + soc.par.ballRadius;
            double increment = (soc.par.goalWidth - soc.par.ballRadius * 2) / numTargets;
         */
    }

    @Override
    public void step(SimState state) {
        super.step(state);
        Soccer soc = (Soccer) state;
        Ball ball = soc.ball;

        /*
        Update status
         */
        SoccerAgent impedingDrible = closestInFront(oppGoal.subtract(getLocation()).angle());
        this.openToDrible = impedingDrible == null;
        if (justKicked) {
            return;
        }
        if (ball.getCurrentSpeed() < 0.01) {
            justPassed = false;
        } else {
            for (SoccerAgent a : others) {
                if (a.hasPossession) {
                    justPassed = false;
                    break;
                }
            }
        }

        /*
        Actions
         */
        if (hasPossession) {
            // Shoot to goal if it has clear shot and it is within range
            for (Double2D target : goalTargets) {
                if (clearShot(ball, target)) {
                    kickTowards(ball, target, kickSpeed);
                    lastAction = "Clear shot to goal: " + target;
                    return;
                }
            }

            // Pass to a teammate that is open and closer to the goal
            List<Pair<SoccerAgent, Double>> sortedOwn = sortTeamByProximity(oppGoal);
            double distToGoal = this.distanceTo(oppGoal);
            for (Pair<SoccerAgent, Double> p : sortedOwn) {
                // at least one robot of distance closer
                if (((ProgSoccerAgent) p.getKey()).openToDrible && p.getValue() < distToGoal - soc.par.agentRadius * 2) {
                    double d = this.distanceTo(p.getKey());
                    if (clearShot(ball, p.getKey())) {
                        kickTowards(ball, p.getKey().getLocation(), getSpeed(d));
                        lastAction = "Pass to advanced teammate: " + p.getKey();
                        justPassed = true;
                        return;
                    }
                }
            }

            // Drible towards the goal if open
            if (openToDrible) {
                kickTowards(ball, oppGoal, dribleSpeed);
                lastAction = "Drible towards goal";
                return;
            }

            // Drible away from closest in front if there is space to do so
            Double2D awayVec;
            Double2D agToClosest = impedingDrible.getLocation().subtract(getLocation());
            if (StaticPolygon.isLeftOf(impedingDrible.getLocation(), getLocation(), oppGoal)) {
                awayVec = agToClosest.rotate(-Math.PI / 2);
            } else {
                awayVec = agToClosest.rotate(Math.PI / 2);
            }
            awayVec = awayVec.normalize().multiply(openDistance);
            Double2D future = ball.getLocation().add(awayVec);
            if (future.x < field.width && future.x > 0 && future.y > 0 && future.y < field.height && closestInFront(awayVec.angle()) == null) {
                kickBall(awayVec.angle(), dribleSpeed);
                lastAction = "Drible away from closest: " + impedingDrible;
                return;
            }

            // Pass to any open teammate
            for (Pair<SoccerAgent, Double> p : sortedOwn) {
                if (((ProgSoccerAgent) p.getKey()).openToDrible && clearShot(ball, p.getKey())) {
                    kickTowards(ball, p.getKey().getLocation(), getSpeed(this.distanceTo(p.getKey())));
                    lastAction = "Pass to any open teammate: " + p.getKey();
                    justPassed = true;
                    return;
                }
            }

            // Shoot ball away from closest in front -- should this exist?
            kickBall(awayVec.angle(), kickSpeed);
            lastAction = "Shooting away from the closest agent";
        } else {
            // Move towards the ball if it is the closest one, unless it just passed the ball
            SoccerAgent closestToBall = closestTeammate(ball.getLocation(), true, false);
            if (!justPassed && closestToBall == this) {
                moveTowards(ball.getLocation());
                lastAction = "Move towards the ball";
                return;
            }

            // Defend the goal if it is the closest to it
            SoccerAgent closestToGoal = closestTeammate(ownGoal, true, true);
            if (closestToGoal == this) {
                Double2D gkPosition = new Double2D(ownGoal.x < oppGoal.x ? ownGoal.x + soc.par.agentRadius + 1 : ownGoal.x - soc.par.agentRadius - 1,
                        Math.min(ownGoal.y + soc.par.goalWidth / 2, Math.max(ownGoal.y - soc.par.goalWidth / 2, ball.getLocation().y)));
                moveTowards(gkPosition);
                lastAction = "Goalkeeper duty: " + gkPosition;
                return;

            }

            // If it is too far away from closest teammate, go towards it
            /*SoccerAgent closest = closestAgent(ownTeam);
            if (distanceTo(closest) > kickRange) {
                moveTowards(closest.getLocation());
                lastAction = "moving towards closest agent: " + closest;
                return;
            }*/
            // If on defense
            if (soc.referee.teamPossession == null || soc.referee.teamPossession != this.teamColor) {
                // TODO: tackle ball
                
                // Mark the nearest opponent that I'm the closest
                SoccerAgent closestOpp = null;
                double min = Double.POSITIVE_INFINITY;
                for (SoccerAgent a : oppTeam) {
                    if (closestTeammate(a.getLocation(), true, true) == this) { // check if I'm the closest
                        double d = this.distanceTo(a);
                        if (d < min) {
                            min = d;
                            closestOpp = a;
                        }
                    }
                }
                if (closestOpp != null) {
                    // Move to the front of the open agent
                    Double2D intersect = ownGoal.subtract(closestOpp.getLocation()).normalize().multiply(soc.par.agentRadius * 2).add(closestOpp.getLocation());
                    moveTowards(intersect);
                    lastAction = "Mark closest: " + closestOpp;
                    return;
                }
            } else { // On attack
                // Keep within passing distance of the agent with the ball
                if (this.distanceTo(closestToBall) > kickRange) {
                    moveTowards(closestToBall.getLocation());
                    lastAction = "Moving towards agent with ball: " + closestToBall;
                    return;
                }

                // Find the closest agent between me and the ball
                SoccerAgent closestOpp = null;
                double min = Double.POSITIVE_INFINITY;
                for (SoccerAgent a : others) {
                    if (StaticPolygon.distToSegment(a.getLocation(), this.getLocation(), ball.getLocation()) < soc.par.agentRadius * 2) {
                        double d = this.distanceTo(a);
                        if (d < min) {
                            min = d;
                            closestOpp = a;
                        }
                    }
                }
                // Move perendicularly to the agent-ball vector, in the direction opposite to the closest agent
                if (closestOpp != null) {
                    boolean left = StaticPolygon.isLeftOf(closestOpp.getLocation(), this.getLocation(), ball.getLocation());
                    double angle = ball.getLocation().subtract(this.getLocation()).angle();
                    if (left) { // Go to right
                        super.move(angle - Math.PI / 2, moveSpeed);
                    } else {
                        super.move(angle + Math.PI / 2, moveSpeed);
                    }
                    lastAction = "Get open";
                    return;
                }
            }

            lastAction = "Do nothing without the ball";
        }
    }

    private SoccerAgent closestTeammate(Double2D point, boolean includeSelf, boolean includeJustPassed) {
        SoccerAgent closest = null;
        double min = Double.POSITIVE_INFINITY;
        for (SoccerAgent a : ownTeam) {
            if (includeJustPassed || !((ProgSoccerAgent) a).justPassed) {
                double d = a.distanceTo(point);
                if (d < min) {
                    min = d;
                    closest = a;
                }
            }
        }
        if (includeSelf && this.distanceTo(point) < min) {
            closest = this;
        }
        return closest;
    }

    private List<Pair<SoccerAgent, Double>> sortTeamByProximity(Double2D target) {
        List<Pair<SoccerAgent, Double>> closer = new ArrayList<>();
        for (SoccerAgent a : ownTeam) {
            closer.add(Pair.of(a, a.distanceTo(target)));
        }
        Collections.sort(closer, new Comparator<Pair<SoccerAgent, Double>>() {
            @Override
            public int compare(Pair<SoccerAgent, Double> o1, Pair<SoccerAgent, Double> o2) {
                return Double.compare(o1.getValue(), o2.getValue());
            }

        });
        return closer;
    }

    /*private boolean openToDrible(double direction) {
        for (SoccerAgent a : others) {
            if (this.distanceTo(a) < openDistance) {
                double agToOther = a.getLocation().subtract(this.getLocation()).angle();
                if (angleDifference(direction, agToOther) < openAngle) {
                    return false;
                }
            }
        }
        return true;
    }*/
    private SoccerAgent closestInFront(double direction) {
        double min = Double.POSITIVE_INFINITY;
        SoccerAgent closest = null;
        for (SoccerAgent a : others) {
            double d = this.distanceTo(a);
            if (d < openDistance) {
                double agToOther = a.getLocation().subtract(getLocation()).angle();
                if (angleDifference(direction, agToOther) < openAngle) {
                    if (d < min) {
                        min = d;
                        closest = a;
                    }
                }
            }
        }
        return closest;
    }

    private boolean clearShot(Ball ball, Double2D target) {
        if (ball.getLocation().distance(target) > kickRange) {
            return false;
        }
        for (SoccerAgent a : others) {
            if (StaticPolygon.distToSegment(a.getLocation(), ball.getLocation(), target) < clearDistance) {
                return false;
            }
        }
        return true;
    }

    private boolean clearShot(Ball ball, SoccerAgent target) {
        if (target.distanceTo(ball.getLocation()) > kickRange) {
            return false;
        }
        for (SoccerAgent a : others) {
            if (a != target && StaticPolygon.distToSegment(a.getLocation(), ball.getLocation(), target.getLocation()) < clearDistance) {
                return false;
            }
        }
        return true;
    }

    private void kickTowards(Ball ball, Double2D target, double power) {
        Double2D kickVec = target.subtract(ball.getLocation());
        kickBall(kickVec.angle(), power);
    }

    private void moveTowards(Double2D target) {
        Double2D moveVec = target.subtract(this.getLocation());
        double dist = this.getLocation().distance(target);
        if (dist > 0) {
            double moveDist = dist > moveSpeed ? moveSpeed : dist;
            super.move(moveVec.angle(), moveDist);
        }
    }

    private double angleDifference(double a, double b) {
        double diff = a - b;
        if (diff > Math.PI) {
            diff -= Math.PI * 2;
        }
        if (diff < -Math.PI) {
            diff += Math.PI * 2;
        }
        return Math.abs(diff);
    }

}
