import java.util.Vector;
import EDU.gatech.cc.is.util.Vec2;

import EDU.gatech.cc.is.abstractrobot.*;

/**
 * A homogeneous robot soccer team.
 *

 * @author H&aring;kan L. Younes
 */
public class AIKHomoG extends ControlSystemSS {
    private static final double FIELD_WIDTH = 1.525;

    private static final double FIELD_LENGTH = 2.74;

    private static final double GOAL_WIDTH = 0.5;


    private static final double MARGIN = 0.02;

    private static final double RANGE = 0.3;

    private static final double TEAMMATE_G = 1.0;

    private static final double WALL_G = 1.0;

    private static final double GOALIE_G = 2.0;

    private static final double FORCE_LIMIT = 1.0;


    /**
     * @return the cross product of the two given vectors.
     */
    protected static double cross(Vec2 v, Vec2 u) {
        return v.x * u.y - v.y * u.x;
    }

    /**
     * @return the dot product of the two given vectors.
     */

    protected static double dot(Vec2 v, Vec2 u) {
        return v.x * u.x + v.y * u.y;
    }


    /**
     * @return the angle, in the range 0 through <I>pi</I>, between
     * the two given vectors.
     */
    protected static double angle(Vec2 v, Vec2 u) {
        return Math.acos(dot(v, u) / (v.r * u.r));
    }


    /**
     * @return the angle, in the range 0 through <I>pi</I>, between
     * the two given angles.
     */
    protected static double angle(double alpha, double beta) {
        return Math.acos(Math.cos(alpha - beta));
    }

    /**
     * @return the given angle converted to degrees.
     */
    protected static int rad2deg(double alpha) {
        return (int)(180.0 * alpha / Math.PI);
    }


    private int side;
    private double forward_angle;
    private double goalie_x;
    private Vec2 offensive_pos1, offensive_pos2;

    public void Configure() {
        Vec2 goal = abstract_robot.getOpponentsGoal(0L);
        if (goal.x > 0.0) {
            side = -1;
            forward_angle = 0.0;
            goalie_x = -FIELD_LENGTH / 2 + abstract_robot.RADIUS;
            offensive_pos1 = new Vec2(4.0 * FIELD_LENGTH / 10.0,
                                      GOAL_WIDTH / 2.0
                                      + abstract_robot.RADIUS);
            offensive_pos2 = new Vec2(4.0 * FIELD_LENGTH / 10.0,
                                      -GOAL_WIDTH / 2.0
                                      - abstract_robot.RADIUS);
        } else {
            side = 1;
            forward_angle = Math.PI;
            goalie_x = FIELD_LENGTH / 2 - abstract_robot.RADIUS;
            offensive_pos1 = new Vec2(-4.0 * FIELD_LENGTH / 10.0,
                                      GOAL_WIDTH / 2.0
                                      + abstract_robot.RADIUS);
            offensive_pos2 = new Vec2(-4.0 * FIELD_LENGTH / 10.0,
                                      -GOAL_WIDTH / 2.0
                                      - abstract_robot.RADIUS);
        }
    }

    private boolean isBehind(double x1, double x2) {
        if (side < 0) {
            return x1 < x2;
        } else {
            return x2 < x1;
        }
    }

    public int TakeStep() {
        long time = abstract_robot.getTime();
        Vec2 ball = abstract_robot.getBall(time);
        if (!isClosestToBall(ball, time)) {
            Vec2 f = getForce(time);

            if (f.r < FORCE_LIMIT) {
                abstract_robot.setSteerHeading(time, ball.t);
                abstract_robot.setSpeed(time, 0.0);
                abstract_robot.setDisplayString("tracking ball");
            } else {
                abstract_robot.setSteerHeading(time, getFreeDirection(f, RANGE,
                                                                      time));
                abstract_robot.setSpeed(time, 0.5);
                abstract_robot.setDisplayString("force: "
                                                + (int)(100.0 * f.r)
                                                + ", " + rad2deg(f.t));
            }
            return CSSTAT_OK;
        }
        Vec2 oppGoal = abstract_robot.getOpponentsGoal(time);
        Vec2 freeGoalDrive = new Vec2(oppGoal);
        freeGoalDrive.sett(getFreeDirection(oppGoal, oppGoal.r, time));
        boolean kick = false;
        if (angle(freeGoalDrive.t, forward_angle) > Math.PI / 6.0) {
            kick = (angle(abstract_robot.getSteerHeading(time), forward_angle)
                    < Math.PI / 2.0);
        } else {
            oppGoal = freeGoalDrive;
        }
        Vec2 goal = getKickPosition(ball,
                                    oppGoal,
                                    time);
        if (!abstract_robot.canKick(time)) {
            double dir = getFreeDirection(goal, RANGE, time);
            abstract_robot.setSteerHeading(time, dir);
            abstract_robot.setSpeed(time, 1.0);
        } else {
            if (angle(freeGoalDrive.t, ball.t) > Math.PI / 12) {
                abstract_robot.setSteerHeading(time, -freeGoalDrive.t);
            } else {
                Vec2 pos = getKickPosition(ball, freeGoalDrive, time);
                abstract_robot.setSteerHeading(time, pos.t);
                if (true || kick) {
                    abstract_robot.kick(time);
                }
            }
            abstract_robot.setSpeed(time, 1.0);
        }
        return CSSTAT_OK;
    }

    private boolean isClosestToBall(Vec2 ball, long time) {
        Vec2[] mates = abstract_robot.getTeammates(time);
        for (int i = 0; i < mates.length; i++) {
            mates[i] = new Vec2(mates[i]);
            mates[i].setr(mates[i].r + abstract_robot.RADIUS);
            Vec2 diff = new Vec2(ball);
            diff.sub(mates[i]);
            if (isBehind(mates[i].x, ball.x) && isBehind(ball.x, 0.0)
                    && diff.r < RANGE) {
                return false;
            }
            if (ball.r >= RANGE && diff.r + MARGIN < ball.r) {
                return false;
            }
        }
        return true;
    }

    private Vec2 getKickPosition(Vec2 ball, Vec2 target, long time) {
        Vec2 v = new Vec2(target);
        v.sub(ball);
        double alpha = v.t + Math.PI;
        v = new Vec2((1.0/Math.sqrt(2)) * abstract_robot.RADIUS
                     * Math.cos(alpha),
                     (1.0/Math.sqrt(2)) * abstract_robot.RADIUS
                     * Math.sin(alpha));
        alpha = angle(ball, v);
        if (alpha < Math.PI / 2.0) {
            if (cross(ball, v) > 0.0) {
                v.sett(v.t + (Math.PI / 2.0 - alpha));
            } else {
                v.sett(v.t - (Math.PI / 2.0 - alpha));
            }
        }
        v.add(ball);
        return v;
    }

    // Should also consider walls!!!
    private double getFreeDirection(Vec2 goal, double range, long time) {
        ObstacleList obstacles = new ObstacleList();
        for (int k = 0; k < 2; k++) {
            Vec2[] ps = (k == 0) ? abstract_robot.getOpponents(time) :
                                    abstract_robot.getTeammates(time);
            Vec2[] players = new Vec2[ps.length];
            for (int i = 0; i < players.length; i++) {
                players[i] = new Vec2(ps[i]);
                players[i].setr(players[i].r + abstract_robot.RADIUS);
                Vec2 diff = new Vec2(goal);
                diff.sub(players[i]);
                if ((players[i].r < 2 * abstract_robot.RADIUS + MARGIN)
                        || (players[i].r < range
                            && players[i].r < goal.r + abstract_robot.RADIUS
                            && diff.r < goal.r)) {
                    obstacles.add(new Obstacle(goal, players[i],
                                               abstract_robot.RADIUS,
                                               abstract_robot.RADIUS));
                }
            }

        }
        double dir = goal.t;
        if (obstacles.size() > 0) {
            Obstacle bound = obstacles.getBoundaries();
            if (bound.obscures(dir)) {
                for (int i = 0; i < obstacles.size(); i++) {
                    Obstacle o = obstacles.get(0);
                    if (o.obscures(dir)) {
                        if (angle(dir, o.getLeft())
                                < angle(o.getRight(), dir)) {
                            dir = o.getLeft();
                        } else {
                            dir = o.getRight();
                        }
                        break;
                    }
                }
            }
        }
        StringBuffer sb = new StringBuffer();
        sb.append(rad2deg(goal.t));
        sb.append(' ').append(obstacles);
        sb.append(" -> ").append(rad2deg(dir));
        abstract_robot.setDisplayString(sb.toString());
        return dir;
    }

    private Vec2 getForce(long time) {
        Vec2 pos = abstract_robot.getPosition(time);
        Vec2 ball = abstract_robot.getBall(time);
        Vec2 f = new Vec2(0, 0);
        /* add negative force for teammates */
        Vec2[] teammates = abstract_robot.getTeammates(time);
        for (int i = 0; i < teammates.length; i++) {
            Vec2 p = new Vec2(teammates[i]);
            p.setr(p.r + abstract_robot.RADIUS);
            p.rotate(Math.PI);
            p.setr(TEAMMATE_G / (p.r * p.r));
            f.add(p);
        }
        /* add negative force for walls on the long sides */
        double r1 = FIELD_WIDTH / 2 - pos.y;
        double r2 = FIELD_WIDTH / 2 + pos.y;
        Vec2 w = new Vec2(0.0, WALL_G / (r2 * r2) - WALL_G / (r1 * r1));
        f.add(w);
        /* add negative force for walls on the short sides */
        r1 = FIELD_LENGTH / 2 - pos.x;
        r2 = FIELD_LENGTH / 2 + pos.x;
        w = new Vec2(WALL_G / (r2 * r2) - WALL_G / (r1 * r1), 0.0);
        f.add(w);
        /* add positive force for goalie position */
        Vec2 gp = new Vec2(goalie_x, 0.0);
        gp.sub(pos);
        Vec2 gpb = new Vec2(ball);
        gpb.sub(gp);
        double k = (isBehind(gpb.x, 0.0) ?
                    ((gpb.y < 0.0) ? -1.0 : 1.0) : Math.sin(gpb.t));
        gp = new Vec2(goalie_x, k * GOAL_WIDTH / 2);
        gp.sub(pos);
        /* check if I'm already acting goalie */
        if (gp.r < MARGIN) {
            return new Vec2(0.0, 0.0);
        }
        boolean goalie = false;
        /* check if teammates is in goalie position */
        for (int i = 0; i < teammates.length && !goalie; i++) {
            Vec2 p = new Vec2(teammates[i]);
            p.setr(p.r + abstract_robot.RADIUS);
            p.sub(gp);
            if (p.r < MARGIN) {
                goalie = true;
            }
        }
        if (!goalie) {
            gp.setr(GOALIE_G / (gp.r * gp.r));
            f.add(gp);
        }
        /* add positive force for offensive positions */
        Vec2 rf = getRoleForce(offensive_pos1, GOALIE_G, time);
        if (rf == null) {
            return new Vec2(0.0, 0.0);
        }
        f.add(rf);
        rf = getRoleForce(offensive_pos2, GOALIE_G, time);
        if (rf == null) {
            return new Vec2(0.0, 0.0);
        }
        f.add(rf);

        return f;
    }

    private Vec2 getRoleForce(Vec2 rolePos, double roleG, long time) {
        Vec2 pos = abstract_robot.getPosition(time);
        Vec2[] teammates = abstract_robot.getTeammates(time);
        Vec2 rp = new Vec2(rolePos);
        rp.sub(pos);
        /* check if I'm already acting in this role */
        if (rp.r < MARGIN) {

            return null;
        }
        boolean roleFilled = false;
        /* check if teammate is acting in this role */
        for (int i = 0; i < teammates.length && !roleFilled; i++) {
            Vec2 p = new Vec2(teammates[i]);
            p.setr(p.r + abstract_robot.RADIUS);
            p.sub(rp);
            if (p.r < MARGIN) {
                roleFilled = true;
            }
        }
        if (!roleFilled) {
            rp.setr(roleG / (rp.r * rp.r));
            return rp;
        }


        return new Vec2(0.0, 0.0);
    }

    /**
     * An obstacle, modelled as a left and right angle.
     */
    class Obstacle extends Object {
        /**
         * The left angle.
         */
        private double left;

        /**
         * The right angle.
         */
        private double right;



        /**
         * Creates an obstacle with the given boundaries.
         *
         * @param left left angle given in radians.
         * @param right right anfle given in radians.
         */
        protected Obstacle(double left, double right) {
            this.left = left;
            this.right = right;
        }

        protected Obstacle(Vec2 g, Vec2 p, double r, double ownR) {
            double cp = cross(g, p);
            double d = ownR + r + MARGIN;
            double t = g.t;
            if (cp >= 0.0) {
                if (p.r <= d) {
                    t = p.t - 1.1 * Math.PI / 2.0;
                } else {
                    t -= Math.PI / 2.0;
                }
            } else {
                if (p.r <= d) {
                    t = p.t + 1.1 * Math.PI / 2.0;
                } else {
                    t += Math.PI / 2.0;
                }
            }
            Vec2 v1 = new Vec2(d * Math.cos(t), d * Math.sin(t));
            if (p.r > d) {
                v1.add(p);
            }
            if (p.r <= d) {
                if (cp >= 0) {
                    t = p.t + 1.1 * Math.PI / 2.0;
                } else {
                    t = p.t - 1.1 * Math.PI / 2.0;
                }
            } else {
                t = g.t + Math.PI;
            }
            Vec2 v2 = new Vec2(d * Math.cos(t), d * Math.sin(t));
            if (p.r > d) {
                v2.add(p);
            }
            if (cp >= 0.0) {
                left = v2.t;
                right = v1.t;
            } else {
                left = v1.t;
                right = v2.t;
            }
        }


        /**
         * @return the left boundary, in radians, of this obstacle.
         */
        public double getLeft() {
            return left;
        }

        /**
         * @return the right boundary, in radians, of this obstacle.
         */
        public double getRight() {
            return right;
        }

        /**
         * @param alpha an angle to check given in radians.
         *
         * @return <CODE>true</CODE> if this obstacle obscures the
         * given angle; <CODE>false</CODE> otherwise.
         */
        protected boolean obscures(double alpha) {
            if (left * right < 0.0) {
                if (left > 0.0) {
                    return left > alpha && alpha > right;
                } else {
                    return left > alpha || alpha > right;
                }
            } else if (left > right) {

                return left > alpha && alpha > right;
            } else {
                return left > alpha || alpha > right;
            }
        }

        /**
         * @param o an obstacle to compare with.
         *
         * @return -1 if this obstacle is completely to the left of
         * <CODE>o</CODE>, 1 if it is completely to the right, and 0
         * otherwise.
         */
        protected int compare(Obstacle o) {
            if (obscures(o.left) || obscures(o.right)
                    || o.obscures(left) || o.obscures(right)) {
                return 0;
            } else {
                return (angle(left, o.right) < angle(right, o.left)) ? 1 : -1;
            }
        }

        /**
         * Merges this obstacle with the given obstacle.
         *
         * @param o obstacle to merge with.
         */
        protected void merge(Obstacle o) {
            if (o.obscures(left)) {
                left = o.left;
            }
            if (o.obscures(right)) {
                right = o.right;
            }
        }

        /**
         * @return a string representation of this object.
         */
        public String toString() {
            return ("[" + rad2deg(left) + "," + rad2deg(right) + "]");
        }
    }

    class ObstacleList extends Object {
        private Vector obstacles;

        protected ObstacleList() {
            obstacles = new Vector();

        }

        protected int size() {
            return obstacles.size();
        }

        protected Obstacle get(int i) {
            return (Obstacle) obstacles.elementAt(i);
        }

        protected Obstacle getBoundaries() {
            return new Obstacle(get(0).getLeft(), get(size() - 1).getRight());
        }

        protected void add(Obstacle o) {
            if (obstacles.isEmpty()) {
                obstacles.addElement(o);
            } else {
                for (int i = obstacles.size() - 1; i >= 0; i--) {
                    Obstacle tmp = (Obstacle) obstacles.elementAt(i);

                    int c = o.compare(tmp);
                    if (c < 0) {
                        obstacles.insertElementAt(tmp, i + 1);
                        if (i == 0) {
                            obstacles.setElementAt(o, 0);
                        }
                    } else if (c > 0) {
                        obstacles.insertElementAt(o, i + 1);
                        break;
                    } else {
                        tmp.merge(o);
                        if (i > 0) {
                            obstacles.removeElement(tmp);
                            o = tmp;
                        }
                    }
                }
            }
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append('{');
            if (size() > 0) {
                sb.append(get(0));
                for (int i = 1; i < size(); i++) {
                    sb.append(' ').append(get(i));
                }
            }
            sb.append('}');
            return sb.toString();
        }
    }
}
