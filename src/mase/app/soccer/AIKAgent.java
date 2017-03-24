package mase.app.soccer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import mase.mason.world.GeomUtils;
import mase.mason.world.GeomUtils.Segment;
import mase.mason.world.StaticMultilineObject;
import net.jafama.FastMath;
import org.apache.commons.lang3.tuple.Pair;
import sim.engine.SimState;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

/**
 * A homogeneous robot soccer team. Based on the AIKHomoG player found in
 * TeamBots. Differences to the original player: 1) Force limit significantly
 * reduced. 2) Fixed bugs in obstacle detection. 3) Include walls in the
 * obstacle list. 4) When shooting the ball, do not consider teammates as
 * obstacles. 5) MARGIN increased.
 *
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

    protected static double deg2Rad(double deg) {
        return deg * Math.PI / 180;
    }

    private int side;
    private double goalieX;
    private Double2D offensivePos1, offensivePos2;
    private Pair<Double, Integer> kickR;

    public AIKAgent(Soccer sim) {
        super(sim, null, sim.par.agentMoveSpeed * sim.par.progSpeedFactor, sim.par.agentKickSpeed * sim.par.progSpeedFactor);
        this.fieldLength = sim.par.fieldLength;
        this.fieldWidth = sim.par.fieldWidth;
        this.goalWidth = sim.par.goalWidth;
        this.kickR = sim.ball.maxDistance(sim, kickSpeed);
    }

    public String getStatus() {
        return status;
    }

    @Override
    public void setTeamContext(List<SoccerAgent> ownTeam, List<SoccerAgent> opponents, Double2D ownGoal, Double2D oppGoal, Color teamColor) {
        super.setTeamContext(ownTeam, opponents, ownGoal, oppGoal, teamColor);
        Double2D goal = this.oppGoal.getLocation();
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
        return (side < 0 && x1 < x2) || (side > 0 && x2 < x1);
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
                double freeDir = getFreeMoveDirection(f, RANGE, false);
                super.move(freeDir, moveSpeed * 0.75);
                status = "Moving without ball.";
            }
            return;
        }

        // Is closest to ball
        if (!hasPossession) {
            // go towards the ball
            double dir = getFreeMoveDirection(getBallVector(), RANGE, true);
            super.move(dir, moveSpeed);
            status = "Closest to ball -- moving to it";
        } else { // go for the kick
            Double2D oppGoalVector = getOppGoalVector();
            double freeDir = getFreeKickDirection(oppGoalVector, Math.min(oppGoalVector.length(), kickR.getLeft())); // max range
            status = "Kicking ball";
            super.kickBall(freeDir, kickSpeed);
        }
    }

    private boolean isClosestToBall() {
        Double2D ball = getBallVector();
        for (Double2D p : getTeammateVectors()) {
            p = p.resize(p.length() + getRadius());
            Double2D diff = ball.subtract(p);
            if (isBehind(p.x, ball.x) && isBehind(ball.x, 0.0) && diff.length() < RANGE) {
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

        // add negative force for teammates (dispersion)
        for (Double2D p : getTeammateVectors()) {
            p = p.resize(p.length() + this.getRadius());
            p = p.rotate(Math.PI);
            p = p.resize(TEAMMATE_G / (p.length() * p.length()));
            f.addIn(p);
        }

        // add negative force for walls on the long sides (stay away from walls)
        double r1 = fieldWidth - pos.y;
        double r2 = pos.y;
        Double2D w = new Double2D(0.0, WALL_G / (r2 * r2) - WALL_G / (r1 * r1));
        f.addIn(w);

        // add negative force for walls on the short sides
        r1 = fieldLength - pos.x;
        r2 = pos.x;
        w = new Double2D(WALL_G / (r2 * r2) - WALL_G / (r1 * r1), 0.0);
        f.addIn(w);

        // add positive force for goalie position  (go towards goal)
        Double2D gp = new Double2D(goalieX, fieldWidth / 2).subtract(pos);
        Double2D gpb = new Double2D(ball.x, ball.y).subtract(gp);
        double k = isBehind(gpb.x, 0.0) ? ((gpb.y < 0.0) ? -1.0 : 1.0) : Math.sin(gpb.angle());
        gp = new Double2D(goalieX, fieldWidth / 2 + k * goalWidth / 2).subtract(pos);
        if (gp.length() < 0.01) {
            return new Double2D(0, 0);
        }
        Double2D gpForce = gp.resize(GOALIE_G / (gp.length() * gp.length()));
        // check if I'm already acting goalie
        if (gp.length() < MARGIN) {
            return gpForce;
        }

        // check if teammates is in goalie position (sensitive to MARGIN)
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

        // add positive force for offensive positions (makes players move to forward positions)
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
        // check if I'm already acting in this role
        if (rp.length() < MARGIN) {
            return null;
        }
        boolean roleFilled = false;
        // check if teammate is acting in this role
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

    private double getFreeKickDirection(Double2D goalVector, double range) {
        Soccer soc = (Soccer) sim;
        ObstacleList obstacles = new ObstacleList();
        // Add opponents as obstacles
        for (Double2D p : getOpponentVectors()) {
            if (p.length() < range + getRadius()) {
                // The obstacle 'size' depends on the opponent's distance, so that they cannot catch the ball
                double r = this.getRadius() + (p.length() / kickR.getLeft()) * kickR.getRight() * soc.par.agentMoveSpeed * 0.6;
                Obstacle o = new Obstacle(p, r, soc.ball.getRadius());
                obstacles.add(o);
            }
        }
        // Add walls as obstacles
        for (Segment s : ((Soccer) sim).fieldBoundaries.segments()) {
            Obstacle o = edgeObstacle(s, range);
            if (o != null) {
                obstacles.add(o);
            }
        }
        // Add own goal as obstacle -- prevent own-goals
        Obstacle ownG = edgeObstacle(ownGoalSegment, range);
        if (ownG != null) {
            obstacles.add(ownG);
        }

        // Find the free direction
        double dir = obstacles.getFreeDirection(goalVector.angle());
        //System.out.println(this + ": " + rad2deg(goalVector.angle()) + ' ' + obstacles.toString() + " -> " + rad2deg(dir));
        return dir;
    }

    private double getFreeMoveDirection(Double2D goalVector, double range, boolean ignoreWalls) {
        ObstacleList obstacles = new ObstacleList();
        // Add all players as obstacles
        for (List<Double2D> ps : new List[]{getOpponentVectors(), getTeammateVectors()}) {
            for (Double2D p : ps) {
                if (p.length() < range + getRadius()) {
                    Obstacle o = new Obstacle(p, this.getRadius(), this.getRadius());
                    obstacles.add(o);
                }
            }
        }
        // Add walls as obstacles
        if (!ignoreWalls) {
            for (Segment s : ((Soccer) sim).fieldBoundaries.segments()) {
                Obstacle o = edgeObstacle(s, range);
                if (o != null) {
                    obstacles.add(o);
                }
            }
        }
        // Find the free direction
        double dir = obstacles.getFreeDirection(goalVector.angle());
        //System.out.println(this + ": " + rad2deg(goalVector.angle()) + ' ' + obstacles.toString() + " -> " + rad2deg(dir));
        return dir;
    }

    /*
    Obstacle handling
     */
    private Obstacle edgeObstacle(Segment seg, double range) {
        double d = GeomUtils.distToSegment(getLocation(), seg.start, seg.end);
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
    static class Obstacle extends Object {

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

        protected Obstacle(Double2D p, double otherR, double ownR) {
            this.p = p;

            Double2D vLeft = p.rotate(Math.PI / 2);
            vLeft = vLeft.resize(otherR + ownR /*+ MARGIN*/);
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
            if (left > right) {
                return alpha < left && alpha > right;
            } else {
                return alpha < left || alpha > right;
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

    static class ObstacleList extends Object {

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
                // first pass to check if needs to be merged
                for (int i = 0; i < obstacles.size(); i++) {
                    Obstacle tmp = obstacles.get(i);
                    if (o.compare(tmp) == 0) {
                        obstacles.remove(tmp);
                        tmp.merge(o); // merge
                        add(tmp);
                        return;
                    }
                }

                // does not obscure any other, fit it in the right place
                for (int i = obstacles.size() - 1; i >= 0; i--) {
                    Obstacle tmp = obstacles.get(i);
                    int c = o.compare(tmp);
                    if (c < 0) { // completely to the left, keep searching unless it reached the end
                        if (i == 0) { // reached the end
                            obstacles.add(0, o);
                            return;
                        }
                    } else { // completely to the right, insert now
                        obstacles.add(i + 1, o);
                        return;
                    }
                }
            }
        }

        public double getFreeDirection(double targetAngle) {
            if (obstacles.size() > 0) {
                Obstacle bound = getBoundaries();
                if (bound.obscures(targetAngle)) {
                    // Find the one obstacle that obscures the trajectory (if any)
                    // remember that by the implementation of the obstacle list, a given direction
                    // can only be obscured by ONE obstacle
                    for (Obstacle o : obstacles) {
                        if (o.obscures(targetAngle)) {
                            if (angle(targetAngle, o.getLeft()) < angle(o.getRight(), targetAngle)) {
                                targetAngle = o.getLeft();
                            } else {
                                targetAngle = o.getRight();
                            }
                            break;
                        }
                    }
                }
            }
            return targetAngle;
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

    /*
    General functions for perception
     */
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
            ownGCache = ownGoal.getLocation().subtract(this.getLocation());
            ownGTime = sim.schedule.getSteps();
        }
        return ownGCache;
    }

    private Double2D getOppGoalVector() {
        if (oppGTime != sim.schedule.getSteps()) {
            oppGCache = oppGoal.getLocation().subtract(this.getLocation());
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
}
