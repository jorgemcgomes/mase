/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.soccer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import mase.mason.world.StaticPolygon;
import org.apache.commons.lang3.tuple.Pair;
import sim.engine.SimState;
import sim.util.Double2D;

/**
 * Based on algorithms in TeamBots http://www.teambots.org/ Such as DoogHomoG If
 * agent has ball: 1 - Shoot to goal if it has clear shot and it is within range
 * 2 - Pass to a teammate that is open and closer to the goal 3 - Drible towards
 * the goal if open 4 - Drible away from closest in front if there is space to
 * do so 5 - Pass to any open teammate 6 - Shoot ball away from closest in front
 * If agent doesnt have the ball: 1 - Move towards the ball if it is the closest
 * one, unless it just passed the ball 2 - Defend the goal if it is the closest
 * to it 3 - If it is too far away from closest teammate, go towards it If on
 * defense: 4 - Mark the nearest opponent that I'm the closest Else: 4 - Get
 * open: move away from the agent between the ball and me
 *
 * @author jorge
 */
public class ProgSoccerAgent extends SoccerAgent {

    private static final long serialVersionUID = 1L;
    private List<Double2D> goalTargets;
    double kickRange;
    boolean openToDrible;
    double openDistance;
    double openAngle = Math.PI / 3;
    double dribleSpeed;
    boolean justPassed = false;
    double clearDistance;
    Random rand;

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
        this.rand = new Random(sim.random.nextLong());
    }

    private double optimalKickSpeed(double dist) {
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
        this.goalTargets = new ArrayList<>();
        goalTargets.add(oppGoal);
        goalTargets.add(new Double2D(oppGoal.x, oppGoal.y + (((Soccer) sim).par.goalWidth / 2) * 0.75));
        goalTargets.add(new Double2D(oppGoal.x, oppGoal.y - (((Soccer) sim).par.goalWidth / 2) * 0.75));
    }

    @Override
    public void step(SimState state) {
        super.step(state);
        Soccer soc = (Soccer) state;
        Ball ball = soc.ball;

        /**
         * ***** Update agent status ******
         */
        // the agent just kicked the ball (is over the ball), do nothing
        if (justKicked) {
            return;
        }

        // Is the agent free to drible towards the goal?
        SoccerAgent impedingDrible = closestInFront(oppGoal.subtract(getLocation()).angle(), openDistance, openAngle);
        this.openToDrible = impedingDrible == null;

        // Update the justPassed flag
        if (justPassed) {
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
        }

        /**
         * ***** Actions ******
         */
        // Execute the first possible action
        // If the agent has possession of the ball:
        // -- Shoot to goal if it has clear shot and it is within range
        // -- Pass to a teammate that is open and closer to the goal
        // -- Drible towards the goal if open
        // -- Drible away from the closest agent in front, if there is space to do so
        // -- Pass the ball to any teammate open to drible
        // -- Pass the ball to any teammate with clear pass
        // -- Shoot the ball away from the closest agent in front
        // If the agent doesnt have the ball:
        // -- Move towards the ball if it is the closest teammate closest to it
        // -- Move towards the ball if it is the closest teammate to it with the path clear (clearDist = agentRadius)
        // -- Defend the goal if it is the closest teammate to it
        // -- If on defense or team possession is undefined:
        //    -- Mark the nearest undefended opponent (undefended = no nearby teammate, move to between the opponent and their goal)
        // -- If on attack
        //    -- ??
        //    -- Keep within passing distance of the closest agent (what about 2 isolate agents, with no ball)
        //    -- Get open
        if (hasPossession) {
            // Shoot to goal if it has clear shot and it is within range
            List<Double2D> shuffledTargets = new ArrayList<>(goalTargets);
            Collections.shuffle(shuffledTargets, rand);
            for (Double2D target : shuffledTargets) {
                if (clearShot(ball, target, kickRange, clearDistance)) {
                    kickTowards(ball, target, kickSpeed);
                    lastAction = "Clear shot to goal: " + target;
                    return;
                }
            }

            // Pass to a teammate that is open and closer to the goal
            List<SoccerAgent> shuffledTeam = new ArrayList<>(teamMates);
            Collections.shuffle(shuffledTeam, rand);
            double distToGoal = this.distanceTo(oppGoal);
            for (SoccerAgent mate : shuffledTeam) {
                // at least one robot of distance closer
                if (((ProgSoccerAgent) mate).openToDrible && mate.distanceTo(oppGoal) < distToGoal - soc.par.agentRadius * 2) {
                    double d = this.distanceTo(mate);
                    if (clearShot(ball, mate, kickRange, clearDistance)) {
                        kickTowards(ball, mate.getLocation(), optimalKickSpeed(d));
                        lastAction = "Pass to advanced teammate: " + mate;
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
            // Calculate the vectors perpendicular to the agent-obstacle vector
            Double2D agToClosest = impedingDrible.getLocation().subtract(getLocation());
            Double2D awayOne = agToClosest.rotate(-Math.PI / 2);
            Double2D awayTwo = agToClosest.rotate(Math.PI / 2);
            Double2D futureOne = ball.getLocation().add(awayOne.normalize().multiply(openDistance));
            Double2D futureTwo = ball.getLocation().add(awayTwo.normalize().multiply(openDistance));
            // One is the closest to goal, Two is the farthest
            if (futureTwo.distance(oppGoal) < futureOne.distance(oppGoal)) {
                Double2D temp = futureOne;
                futureOne = futureTwo;
                futureTwo = temp;
                temp = awayOne;
                awayOne = awayTwo;
                awayTwo = temp;
            }
            // Try to move in the One direction, checking if it is inside the field and is clear
            if (futureOne.x < field.width && futureOne.x > 0 && futureOne.y > 0 && futureOne.y < field.height
                    && closestInFront(awayOne.angle(), openDistance, openAngle) == null) {
                kickBall(awayOne.angle(), dribleSpeed);
                lastAction = "Drible away from closest (1): " + impedingDrible;
                return;
            }
            // Otherwise, try to move in the Two direction
            if (futureTwo.x < field.width && futureTwo.x > 0 && futureTwo.y > 0 && futureTwo.y < field.height
                    && closestInFront(awayTwo.angle(), openDistance, openAngle) == null) {
                kickBall(awayTwo.angle(), dribleSpeed);
                lastAction = "Drible away from closest (2): " + impedingDrible;
                return;
            }

            // Pass to any teammate that can drible towards goal
            for (SoccerAgent mate : shuffledTeam) {
                if (((ProgSoccerAgent) mate).openToDrible && clearShot(ball, mate, kickRange, clearDistance)) {
                    kickTowards(ball, mate.getLocation(), optimalKickSpeed(this.distanceTo(mate)));
                    lastAction = "Pass to any teammate open to drible: " + mate;
                    justPassed = true;
                    return;
                }
            }

            // Pass to any teammate with clear pass
            for (SoccerAgent mate : shuffledTeam) {
                if (clearShot(ball, mate, kickRange, clearDistance)) {
                    kickTowards(ball, mate.getLocation(), optimalKickSpeed(this.distanceTo(mate)));
                    lastAction = "Pass to any teammate with clear pass: " + mate;
                    justPassed = true;
                    return;
                }
            }

            // Shoot ball away from closest in front -- last case scenario, should this exist?
            kickBall(awayOne.angle(), kickSpeed);
            lastAction = "Shooting away from the closest agent";

        } else {  // NO BALL POSSESSION

            List<Pair<SoccerAgent, Double>> sorted = sortByProximity(ownTeam, ball.getLocation());

            // Move towards the ball it is the closest agent, regardless of clear paths
            if (!justPassed && sorted.get(0).getLeft() == this) {
                moveTowards(ball.getLocation());
                lastAction = "Closest, move to the ball";
                return;
            }

            // Defend the goal if it is the closest to it (excluding the agent closest to the ball)
            sorted = sortByProximity(ownTeam, ownGoal);
            boolean closestToGoal = false;
            for (Pair<SoccerAgent, Double> p : sorted) {
                ProgSoccerAgent pp = (ProgSoccerAgent) p.getLeft();
                if (!pp.lastAction.equals("Closest, move to the ball")) {
                    closestToGoal = (pp == this);
                    break;
                }
            }
            if (closestToGoal) {
                Double2D gkPosition = new Double2D(ownGoal.x < oppGoal.x ? ownGoal.x + soc.par.agentRadius + 1 : ownGoal.x - soc.par.agentRadius - 1,
                        Math.min(ownGoal.y + soc.par.goalWidth / 2, Math.max(ownGoal.y - soc.par.goalWidth / 2, ball.getLocation().y)));
                moveTowards(gkPosition);
                lastAction = "Goalkeeper duty: " + gkPosition;
                return;
            }

            if (!justPassed) {
                // Move towards the ball if it is the closest one with clear line
                boolean closestClear = false;
                for (Pair<SoccerAgent, Double> p : sorted) {
                    ProgSoccerAgent pp = (ProgSoccerAgent) p.getLeft();
                    if (pp.clearPath(ball.getLocation(), pp.getRadius())) {
                        closestClear = (pp == this);
                        break;
                    }
                }
                if (closestClear) {
                    moveTowards(ball.getLocation());
                    lastAction = "Closest with clear path, move to the ball";
                    return;
                }
            }

            // If on defense
            if (soc.referee.teamPossession == null || soc.referee.teamPossession != this.teamColor) {
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
                    // Move to between the agent and my goal
                    Double2D intersect = ownGoal.subtract(closestOpp.getLocation()).normalize().multiply(soc.par.agentRadius * 3).add(closestOpp.getLocation());
                    moveTowards(intersect);
                    lastAction = "Mark closest: " + closestOpp;
                    return;
                }
            } else { // On attack
                // Find the closest agent between me and the ball
                SoccerAgent closestOther = null;
                double min = Double.POSITIVE_INFINITY;
                for (SoccerAgent a : others) {
                    if (StaticPolygon.distToSegment(a.getLocation(), this.getLocation(), ball.getLocation()) < soc.par.agentRadius * 2) {
                        double d = this.distanceTo(a);
                        if (d < min) {
                            min = d;
                            closestOther = a;
                        }
                    }
                }
                // Move perendicularly to the agent-ball vector, in the direction opposite to the closest agent
                if (closestOther != null) {
                    boolean left = StaticPolygon.isLeftOf(closestOther.getLocation(), this.getLocation(), ball.getLocation());
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
        for (SoccerAgent a : teamMates) {
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

    private List<Pair<SoccerAgent, Double>> sortByProximity(Collection<SoccerAgent> agents, Double2D target) {
        List<Pair<SoccerAgent, Double>> closer = new ArrayList<>();
        for (SoccerAgent a : agents) {
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

    /*
    Is there any other agent in front of this one?
    Check a cone defined by direction, distance, and angle
     */
    private SoccerAgent closestInFront(double direction, double distance, double angle) {
        double min = Double.POSITIVE_INFINITY;
        SoccerAgent closest = null;
        for (SoccerAgent a : others) {
            double d = this.distanceTo(a);
            if (d < distance) {
                double agToOther = a.getLocation().subtract(getLocation()).angle();
                if (angleDifference(direction, agToOther) < angle) {
                    if (d < min) {
                        min = d;
                        closest = a;
                    }
                }
            }
        }
        return closest;
    }

    private boolean clearPath(Double2D target, double clearDist) {
        for (SoccerAgent a : others) {
            if (StaticPolygon.distToSegment(a.getLocation(), this.getLocation(), target) < clearDist) {
                return false;
            }
        }
        return true;
    }

    private boolean clearShot(Ball ball, Double2D target, double maxRange, double clearDist) {
        if (ball.getLocation().distance(target) > maxRange) {
            return false;
        }
        for (SoccerAgent a : others) {
            // check the distance of each other agent to the line segment between the ball and the target
            if (StaticPolygon.distToSegment(a.getLocation(), ball.getLocation(), target) < clearDist) {
                return false;
            }
        }
        return true;
    }

    private boolean clearShot(Ball ball, SoccerAgent target, double maxRange, double clearDist) {
        if (target.distanceTo(ball.getLocation()) > maxRange) {
            return false;
        }
        for (SoccerAgent a : others) {
            if (a != target && StaticPolygon.distToSegment(a.getLocation(), ball.getLocation(), target.getLocation()) < clearDist) {
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
