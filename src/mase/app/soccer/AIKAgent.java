package mase.app.soccer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import mase.mason.world.StaticPolygon;
import mase.mason.world.StaticPolygon.Segment;
import net.jafama.FastMath;
import sim.engine.SimState;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

/**
 * A homogeneous robot soccer team. Based on the AIKHomoG player found in TeamBots.
 * Differences to the original player:
 * 1) Force limit significantly reduced.
 * 2) Fixed bugs in obstacle detection.
 * 3) Include walls in the obstacle list.
 * 4) When shooting the ball, do not consider teammates as obstacles.
 * 5) MARGIN increased.
 * @author H&aring;kan L. Younes
 */
public class AIKAgent extends SoccerAgent {

    private static final double MARGIN = 10;

    private static final double RANGE = 30;

    private static final double TEAMMATE_G = 100;

    private static final double WALL_G = 100;

    private static final double GOALIE_G = 200;

    private static final double FORCE_LIMIT = 0.01;
    private static final long serialVersionUID = 1L;

    private final double fieldWidth;
    private final double fieldLength;
    private final double goalWidth;
    private String status = "";

    /**
     * @return the cross product of the two given vectors.
     */
    protected static double cross(Double2D v, Double2D u) {
        return v.x * u.y - v.y * u.x;
    }

    /**
     * @return the dot product of the two given vectors.
     */
    protected static double dot(Double2D v, Double2D u) {
        return v.x * u.x + v.y * u.y;
    }

    /**
     * @return the angle, in the range 0 through <I>pi</I>, between the two
     * given vectors.
     */
    protected static double angle(Double2D v, Double2D u) {
        return FastMath.acos(dot(v, u) / (v.length() * u.length()));
    }

    /**
     * @return the angle, in the range 0 through <I>pi</I>, between the two
     * given angles.
     */
    protected static double angle(double alpha, double beta) {
        return FastMath.acos(Math.cos(alpha - beta));
    }

    /**
     * @return the given angle converted to degrees.
     */
    protected static int rad2deg(double alpha) {
        return (int) (180.0 * alpha / Math.PI);
    }

    private int side;
    private double goalieX;
    private Double2D offensivePos1, offensivePos2;

    public AIKAgent(Soccer sim) {
        super(sim, null, sim.par.agentMoveSpeed, sim.par.agentKickSpeed);
        this.fieldLength = sim.par.fieldLength;
        this.fieldWidth = sim.par.fieldWidth;
        this.goalWidth = sim.par.goalWidth;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public void setTeamContext(List<SoccerAgent> ownTeam, List<SoccerAgent> opponents, Double2D ownGoal, Double2D oppGoal, Color teamColor) {
        super.setTeamContext(ownTeam, opponents, ownGoal, oppGoal, teamColor);
        Double2D goal = this.oppGoal;
        if (goal.x > fieldLength / 2) { // left team
            side = -1;
            //forwardAngle = 0.0;
            goalieX = ownGoal.x + getRadius();
            offensivePos1 = new Double2D((fieldLength / 2) + 4.0 * fieldLength / 10.0,
                    (fieldWidth / 2) + goalWidth / 2.0
                    + this.getRadius());
            offensivePos2 = new Double2D((fieldLength / 2) + 4.0 * fieldLength / 10.0,
                    (fieldWidth / 2) - goalWidth / 2.0
                    - this.getRadius());
        } else { // right team
            side = 1;
            //forwardAngle = Math.PI;
            goalieX = ownGoal.x - getRadius();
            offensivePos1 = new Double2D((fieldLength / 2) - 4.0 * fieldLength / 10.0,
                    (fieldWidth / 2) + goalWidth / 2.0
                    + this.getRadius());
            offensivePos2 = new Double2D((fieldLength / 2) - 4.0 * fieldLength / 10.0,
                    (fieldWidth / 2) - goalWidth / 2.0
                    - this.getRadius());
        }
    }

    private boolean isBehind(double x1, double x2) {
        if (side < 0) {
            return x1 < x2;
        } else {
            return x2 < x1;
        }
    }

    @Override
    public void step(SimState state) {
        super.step(state);
        // I dont have the ball
        if (!isClosestToBall()) {
            Double2D f = getForce();

            if (f.length() < FORCE_LIMIT) {
                // do not move
                status = "Not enough force -- do nothing";
            } else {
                double freeDir = getFreeDirection(f, RANGE, true);
                super.move(freeDir, moveSpeed * 0.75);
                status = "Moving without ball.";// Force: " + f + ", " + rad2deg(f.angle());
            }
            return;
        }

        // Is closest to ball
        // not near the ball
        if (!hasPossession) {
            // go towards the ball
            double dir = getFreeDirection(getBallVector(), RANGE, false);
            super.move(dir, moveSpeed);
            status = "Closest to ball -- moving to it";
            // go for the kick
        } else {
            // desired kick direction (towards the goal, avoiding stuff)
            Double2D oppGoal = getOppGoalVector();
            double freeDir = getFreeDirection(oppGoal, oppGoal.length(), false); // max range
            status = "Kicking ball";
            super.kickBall(freeDir, kickSpeed);
        }
    }

    private boolean isClosestToBall() {
        Double2D ball = getBallVector();
        for (Double2D p : getTeammateVectors()) {
            p = p.resize(p.length() + getRadius());
            Double2D diff = ball;
            diff = diff.subtract(p);
            if (isBehind(p.x, ball.x) && isBehind(ball.x, 0.0)
                    && diff.length() < RANGE) {
                return false;
            }
            if (ball.length() >= RANGE && diff.length() + MARGIN < ball.length()) {
                return false;
            }
        }
        return true;
    }

    private Double2D getForce() {
        Double2D ball = getBallVector();
        MutableDouble2D f = new MutableDouble2D(0, 0);

        // add negative force for teammates -- working (dispersion)
        for (Double2D p : getTeammateVectors()) {
            p = p.resize(p.length() + this.getRadius());
            p = p.rotate(Math.PI);
            p = p.resize(TEAMMATE_G / (p.length() * p.length()));
            f.addIn(p);
        }

        // add negative force for walls on the long sides -- working (stay way from walls)
        double r1 = fieldWidth - pos.y;
        double r2 = pos.y;
        Double2D w = new Double2D(0.0, WALL_G / (r2 * r2) - WALL_G / (r1 * r1));
        f.addIn(w);

        // add negative force for walls on the short sides -- working
        r1 = fieldLength - pos.x;
        r2 = pos.x;
        w = new Double2D(WALL_G / (r2 * r2) - WALL_G / (r1 * r1), 0.0);
        f.addIn(w);

        // add positive force for goalie position  -- confirmed
        Double2D gp = new Double2D(goalieX, fieldWidth / 2);
        gp = gp.subtract(pos);
        Double2D gpb = new Double2D(ball.x, ball.y);
        gpb = gpb.subtract(gp);
        double k = (isBehind(gpb.x, 0.0)
                ? ((gpb.y < 0.0) ? -1.0 : 1.0) : Math.sin(gpb.angle()));
        gp = new Double2D(goalieX, fieldWidth / 2 + k * goalWidth / 2);
        gp = gp.subtract(pos);
        Double2D gpForce = gp.resize(GOALIE_G / (gp.length() * gp.length()));
        // check if I'm already acting goalie
        if (gp.length() < MARGIN) {
            return gpForce;
        }

        // check if teammates is in goalie position -- confirmed (sensitive to MARGIN)
        boolean goalie = false;
        for (Double2D p : getTeammateVectors()) {
            Double2D diff = p.subtract(gp);
            if (diff.length() < MARGIN) {
                goalie = true;
                break;
            }
        }
        if (!goalie) {
            f.addIn(gpForce);
        }

        // add positive force for offensive positions -- confirmed (makes players move forward)
        Double2D rf = getRoleForce(offensivePos1, GOALIE_G);
        if (rf == null) {
            return new Double2D(0.0, 0.0);
        }
        f.addIn(rf);
        rf = getRoleForce(offensivePos2, GOALIE_G);
        if (rf == null) {
            return new Double2D(0.0, 0.0);
        }
        f.addIn(rf);

        return new Double2D(f);
    }

    private Double2D getRoleForce(Double2D rolePos, double roleG) {
        Double2D rp = rolePos.subtract(pos);
        /* check if I'm already acting in this role */
        if (rp.length() < MARGIN) {
            return null;
        }
        boolean roleFilled = false;
        /* check if teammate is acting in this role */
        for (Double2D p : getTeammateVectors()) {
            Double2D diff = p.subtract(rp);
            if (diff.length() < MARGIN) {
                roleFilled = true;
                break;
            }
        }
        if (!roleFilled) {
            rp = rp.resize(roleG / (rp.length() * rp.length()));
            return rp;
        }

        return new Double2D(0.0, 0.0);
    }

    private List<Double2D> tmCache = null;
    private long tmTime = -1;
    private List<Double2D> oppCache = null;
    private long oppTime = -1;
    private Double2D ballCache = null, ownGCache = null, oppGCache = null;
    private long ballTime = -1, ownGTime = -1, oppGTime = -1;

    private List<Double2D> getTeammateVectors() {
        if (tmTime != sim.schedule.getSteps()) {
            tmCache = new ArrayList<>(teamMates.size());
            for (SoccerAgent a : teamMates) {
                tmCache.add(a.getLocation().subtract(this.getLocation()));
            }
            tmTime = sim.schedule.getSteps();
        }
        return tmCache;
    }

    private List<Double2D> getOpponentVectors() {
        if (oppTime != sim.schedule.getSteps()) {
            oppCache = new ArrayList<>(oppTeam.size());
            for (SoccerAgent a : oppTeam) {
                oppCache.add(a.getLocation().subtract(this.getLocation()));
            }
            oppTime = sim.schedule.getSteps();
        }
        return oppCache;
    }

    private Double2D getOwnGoalVector() {
        if (ownGTime != sim.schedule.getSteps()) {
            ownGCache = ownGoal.subtract(this.getLocation());
            ownGTime = sim.schedule.getSteps();
        }
        return ownGCache;
    }

    private Double2D getOppGoalVector() {
        if (oppGTime != sim.schedule.getSteps()) {
            oppGCache = oppGoal.subtract(this.getLocation());
            oppGTime = sim.schedule.getSteps();
        }
        return oppGCache;
    }

    private Double2D getBallVector() {
        if (ballTime != sim.schedule.getSteps()) {
            ballCache = ((Soccer) sim).ball.getLocation().subtract(this.getLocation());
            ballTime = sim.schedule.getSteps();
        }
        return ballCache;
    }

    // TODO: Should also consider walls!!!
    private double getFreeDirection(Double2D goal, double range, boolean includeTeam) {
        ObstacleList obstacles = new ObstacleList();
        for (int k = 0; k < (includeTeam ? 2 : 1); k++) {
            List<Double2D> ps = (k == 0) ? getOpponentVectors() : getTeammateVectors();
            for (Double2D p : ps) {
                //p = p.resize(p.length() + getRadius());
                Double2D diff = goal.subtract(p);
                if ((p.length() < 2 * getRadius() + MARGIN) // very close
                        || (p.length() < range + getRadius() && p.length() < goal.length() + getRadius() // potentially obscuring goal
                        && diff.length() < goal.length())) {
                    Obstacle o = new Obstacle(p, getRadius(), getRadius());
                    obstacles.add(o);
                    //System.out.println(this + " A " + p + "-> [" + rad2deg(o.getLeft()) + "," + rad2deg(o.getRight()) + "]");
                }
            }
        }
        for (Segment s : ((Soccer) sim).fieldBoundaries.segments()) {
            Obstacle o = edgeObstacle(s, RANGE);
            if (o != null) {
                obstacles.add(o);
                //System.out.println(this + " S " + s.start + "-" + s.end + "-> [" + rad2deg(o.getLeft()) + "," + rad2deg(o.getRight()) + "]");
            }
        }
        double dir = goal.angle();
        if (obstacles.size() > 0) {
            Obstacle bound = obstacles.getBoundaries();
            if (bound.obscures(dir)) {
                for (Obstacle o : obstacles.obstacles) {
                    // Find the one obstacle that obscures the trajectory (if any)
                    // remember that by the implementation of the obstacle list, a given direction
                    // can only be obscured by ONE obstacle
                    if (o.obscures(dir)) {
                        if (angle(dir, o.getLeft()) < angle(o.getRight(), dir)) {
                            dir = o.getLeft();
                        } else {
                            dir = o.getRight();
                        }
                        break;
                    }
                }
            }
        }
        //System.out.println(this + ": " + rad2deg(goal.angle()) + ' ' + obstacles.toString() + " -> " + rad2deg(dir));
        return dir;
    }

    private Obstacle edgeObstacle(Segment seg, double range) {
        double d = StaticPolygon.distToSegment(getLocation(), seg.start, seg.end);
        if (d < range) {
            // find the intersection between the line defined by the points of the segment
            // and the perpendicular line that passes through the agent's center
            // the intersection might fall outside the line segment
            double l2 = FastMath.pow2(seg.start.x - seg.end.x) + FastMath.pow2(seg.start.y - seg.end.y);
            double t = getLocation().subtract(seg.start).dot(seg.end.subtract(seg.start)) / l2;
            Double2D projection = seg.start.add((seg.end.subtract(seg.start)).multiply(t));

            Double2D robToP = projection.subtract(getLocation());

            double size = FastMath.sqrtQuick(FastMath.pow2(range) - FastMath.pow2(d));
            Double2D rightLimit = robToP.rotate(Math.PI / 2).resize(size).add(projection);
            Double2D leftLimit = robToP.rotate(-Math.PI / 2).resize(size).add(projection);

            Double2D closestEndRight = rightLimit.distance(seg.start) < rightLimit.distance(seg.end) ? seg.start : seg.end;
            Double2D closestEndLeft = leftLimit.distance(seg.start) < leftLimit.distance(seg.end) ? seg.start : seg.end;

            // falls outside the segment
            if (getLocation().distance(rightLimit) > getLocation().distance(closestEndRight)) {
                rightLimit = closestEndRight;
            }
            if (getLocation().distance(leftLimit) > getLocation().distance(closestEndLeft)) {
                leftLimit = closestEndLeft;
            }
            if (rightLimit.distance(leftLimit) < 0.01) {
                return null;
            }

            double leftAngle = leftLimit.subtract(getLocation()).angle();
            double rightAngle = rightLimit.subtract(getLocation()).angle();
            return new Obstacle(rightAngle, leftAngle);
        } else {
            return null;
        }
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

        public Double2D p;

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

        protected Obstacle(Double2D p, double r, double ownR) {
            this.p = p;

            Double2D vLeft = p.rotate(Math.PI / 2);
            vLeft = vLeft.resize(r + ownR + MARGIN);
            Double2D vRight = vLeft.negate();
            
            this.left = vLeft.add(p).angle();
            this.right = vRight.add(p).angle();
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
         * @return <CODE>true</CODE> if this obstacle obscures the given angle;
         * <CODE>false</CODE> otherwise.
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
         * <CODE>o</CODE>, 1 if it is completely to the right, and 0 otherwise.
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
        @Override
        public String toString() {
            return ("[" + rad2deg(left) + "," + rad2deg(right) + "]");
        }
    }

    class ObstacleList extends Object {

        private ArrayList<Obstacle> obstacles;

        protected ObstacleList() {
            obstacles = new ArrayList();

        }

        protected int size() {
            return obstacles.size();
        }

        protected Obstacle get(int i) {
            return obstacles.get(i);
        }

        protected Obstacle getBoundaries() {
            return new Obstacle(get(0).getLeft(), get(size() - 1).getRight());
        }

        protected void add(Obstacle o) {
            if (obstacles.isEmpty()) {
                obstacles.add(o);
            } else {
                // Keep list sorted from left to right
                for (int i = obstacles.size() - 1; i >= 0; i--) {
                    Obstacle tmp = obstacles.get(i);

                    int c = o.compare(tmp);
                    if (c < 0) { // completely to the left, keep searching unless it reached the end
                        if (i == 0) {
                            obstacles.add(0, o);
                        }
                    } else if (c > 0) { // completely to the right, insert now and stop!
                        obstacles.add(i + 1, o);
                        break;
                    } else { // there is overlap between obstacles
                        obstacles.remove(tmp);
                        tmp.merge(o); // merge
                        //if(i > 0) { // re-add to keep order
                        //obstacles.remove(tmp);
                        this.add(tmp);
                        //}
                        break;
                    }
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
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
