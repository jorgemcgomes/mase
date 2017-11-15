package mase.mason.world;

import java.util.Arrays;
import mase.mason.world.GeomUtils.Line;
import net.jafama.FastMath;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.MathUtils;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.ProvidesInspector;
import sim.util.Double2D;

public class MultipleWheelAxesActuator extends AbstractEffector implements ProvidesInspector {

    private static final long serialVersionUID = 1L;

    // Movement capabilities -- can be changed during simulation
    public double minSpeed = -1, maxSpeed = 1;
    protected double speedAcceleration = 0.1;
    public double rotationLimit = Math.toRadians(45);
    protected double rotationAcceleration = Math.toRadians(90) / 10.0;
    protected double maxSlipAngle = Math.toRadians(10);
    protected double friction = 100;
    protected double parallelMovementAngle = Math.toRadians(1);

    // Robot geometry -- non-changeable
    protected final boolean[] movableWheels;
    protected final int movableWheelsCount;
    protected final double diameter;
    public final Double2D[] wheelPos;

    // Actuator status
    protected double currentSpeed = 0;
    protected final double[] currentRotation;
    protected Double2D lastMove;

    public MultipleWheelAxesActuator(SimState state, Continuous2D field, EmboddiedAgent ag, boolean[] movableWheels) {
        super(state, field, ag);
        int wheels = movableWheels.length;
        if (wheels % 2 != 0 || wheels < 2) {
            throw new IllegalArgumentException("Number of wheels must be even and > 1. Got: " + wheels);
        }
        diameter = ag.getRadius() * 2;

        // wheel positions relative to the robot referential
        // The front of the robot is aligned in the x axis. the left is the positive y
        // Order: front axis left, right, .... , rear axis left, right
        wheelPos = new Double2D[wheels];
        double axisSpacing = wheels == 2 ? 0 : (diameter / (wheels / 2 - 1));
        for (int axis = 0; axis < wheels / 2; axis++) {
            double axisX = wheels == 2 ? 0 : diameter / 2 - axis * axisSpacing;
            wheelPos[axis * 2] = new Double2D(axisX, diameter / 2); // right
            wheelPos[axis * 2 + 1] = new Double2D(axisX, -diameter / 2); // left
        }

        this.currentRotation = new double[wheels];
        Arrays.fill(currentRotation, 0);

        this.movableWheels = movableWheels;
        int c = 0;
        for (boolean b : movableWheels) {
            if (b) {
                c++;
            }
        }
        this.movableWheelsCount = c;
        if (movableWheelsCount == 0) {
            throw new IllegalArgumentException("There must be at least one moving wheel: " + Arrays.toString(movableWheels));
        }
    }

    public void setSpeedLimits(double minSpeed, double maxSpeed, double maxAcceleration) {
        this.minSpeed = minSpeed;
        this.maxSpeed = maxSpeed;
        this.speedAcceleration = maxAcceleration;
    }

    public void setRotationLimits(double angleLimit, double maxAcceleration) {
        this.rotationLimit = angleLimit;
        this.rotationAcceleration = maxAcceleration;
    }

    /**
     *
     * @param maxSlipAngle
     * @param parallelMovementAngle
     * @param friction How much to reduce speed (in %) for each angle of average
     * slip
     */
    public void setSlipLimits(double maxSlipAngle, double parallelMovementAngle, double friction) {
        this.maxSlipAngle = maxSlipAngle;
        this.parallelMovementAngle = parallelMovementAngle;
        this.friction = friction;
    }

    @Override
    public int valueCount() {
        return movableWheelsCount + 1;
    }

    @Override
    // If you rotate point (px, py) around point (ox, oy) by angle theta you'll get:
    //p'x = cos(theta) * (px-ox) - sin(theta) * (py-oy) + ox
    //p'y = sin(theta) * (px-ox) + cos(theta) * (py-oy) + oy
    /**
     * values in the range [0,1]
     */
    public void action(double[] values) {
        currentSpeed = applyAcceleration(values[0], currentSpeed, minSpeed, maxSpeed, speedAcceleration);
        int index = 1;
        for (int i = 0; i < movableWheels.length; i++) {
            if (movableWheels[i]) {
                currentRotation[i] = applyAcceleration(values[index], currentRotation[i], -rotationLimit, rotationLimit, rotationAcceleration);
                index++;
            }
        }

        double meanAngle = StatUtils.mean(currentRotation);
        double maxDifference = Double.NEGATIVE_INFINITY;
        for (double r : currentRotation) {
            maxDifference = Math.max(maxDifference, Math.abs(meanAngle - r));
        }

        if (maxDifference < parallelMovementAngle) {
            // if wheels are parallel and speeds are coherent, move in the meanAngle direction
            // ex: moving straight in front, moving sideways, diagonally, etc
            // the orientation of the robot does not change, but its position does
            double[] slips = new double[currentRotation.length];
            for (int i = 0; i < slips.length; i++) {
                slips[i] = Math.abs(meanAngle - currentRotation[i]);
            }
            currentSpeed = this.applyFriction(currentSpeed, slips);
            if (currentSpeed != 0) {
                double absoluteDirection = MathUtils.normalizeAngle(ag.orientation2D() + meanAngle, 0);
                lastMove = new Double2D(currentSpeed * FastMath.cos(meanAngle), currentSpeed * FastMath.sin(meanAngle));
                Double2D newPos = new Double2D(currentSpeed * FastMath.cos(absoluteDirection), currentSpeed * FastMath.sin(absoluteDirection)).add(ag.getLocation());
                ag.move(ag.orientation2D(), newPos);
            }
        } else {
            // Not all wheels are parallel
            // This means the robot's orientation is going to change
            Line[] axisLines = calculateAxisLines(currentRotation);
            Double2D icr = calculateICR(axisLines);
            double[] wheelSlip = calculateSlipAngles(axisLines, icr);
            //System.out.println(StatUtils.max(wheelSlip));
            // ICR cannot be inside the robot
            if (StatUtils.max(wheelSlip) < maxSlipAngle
                    && (icr.x > diameter / 2 || icr.x < -diameter / 2 || icr.y > diameter / 2 || icr.y < -diameter / 2)) {
                currentSpeed = applyFriction(currentSpeed, wheelSlip);
                if (currentSpeed != 0) {
                    // convert movement along the circle to an angle
                    double moveAngle = Math.signum(icr.y) * currentSpeed / icr.length(); // abs currentSpeed?
                    // compute new position in the robot referential
                    Double2D newPos = new Double2D(
                            icr.x + (-icr.x) * FastMath.cos(moveAngle) - (-icr.y) * FastMath.sin(moveAngle),
                            icr.y + (-icr.x) * FastMath.sin(moveAngle) + (-icr.y) * FastMath.cos(moveAngle));

                    ag.move(ag.orientation2D() + moveAngle, newPos.length() * Math.signum(currentSpeed));
                    lastMove = newPos;
                } else {
                    lastMove = new Double2D(0, 0);
                    ag.move(ag.orientation2D(), 0); // STOP
                    //System.out.println("TOO MUCH FRICTION (TRYING ICR ROTATION)");
                }

            } else {
                lastMove = new Double2D(0, 0);
                ag.move(ag.orientation2D(), 0); // STOP
                //System.out.println("SLIP HARD LIMIT");
            }
        }
    }

    private double applyAcceleration(double output, double actual, double minValue, double maxValue, double acceleration) {
        double desired = output * (maxValue - minValue) + minValue;
        if (!Double.isInfinite(acceleration) && Math.abs(desired - actual) > acceleration) {
            desired = desired < actual ? actual - acceleration : actual + acceleration;
        }
        return desired;
    }

    private double applyFriction(double speed, double[] slipAngles) {
        if (friction > 0) {
            double rmse = 0;
            for (double d : slipAngles) {
                rmse += FastMath.pow2(FastMath.toDegrees(d));
            }
            rmse = FastMath.sqrt(rmse) / slipAngles.length;
            // to be generalizable to any number of wheels
            // more wheels can result in more total slip, but they also result in more traction power
            double coeff = Math.max(0, 1 - rmse * friction); // [0,1]
            return speed * coeff;
        } else {
            return speed;
        }
    }

    private double[] calculateSlipAngles(Line[] axisLines, Double2D icr) {
        double[] wheelSlip = new double[axisLines.length];
        for (int i = 0; i < wheelSlip.length; i++) {
            // slip = angle difference between the axis line and the wheel-ICR line
            Line axisICR = new Line(icr, wheelPos[i]);
            wheelSlip[i] = axisICR.angle(axisLines[i]);
        }
        return wheelSlip;
    }

    /*
     * For each wheel, returns a line passing through the middle of it, with an angle perpendicular to the wheel's rotation
     */
    private Line[] calculateAxisLines(double[] rotations) {
        Line[] axisLines = new Line[rotations.length];
        for (int i = 0; i < rotations.length; i++) {
            axisLines[i] = new Line(wheelPos[i], MathUtils.normalizeAngle(rotations[i] + Math.PI / 2, 0));
        }
        return axisLines;
    }

    // todo: if one of the sides has parallel wheels it is not very coherent
    private Double2D calculateICR(Line[] axisLines) {
        // left side intersection
        Line[] leftAxisLines = new Line[axisLines.length / 2];
        Line[] rightAxisLines = new Line[axisLines.length / 2];
        for (int i = 0; i < leftAxisLines.length; i++) {
            leftAxisLines[i] = axisLines[i * 2];
            rightAxisLines[i] = axisLines[i * 2 + 1];
        }
        Double2D leftICR = calculateSideICR(leftAxisLines);
        Double2D rightICR = calculateSideICR(rightAxisLines);

        if (rightICR == null && leftICR == null) {
            // Both sides have all wheels parallel, do not rotate
            return new Double2D(0, 0);
        } else if (rightICR != null && leftICR != null) {
            // Both sides have non-parallel wheels
            // ICR is the average of the left and right ICR
            Double2D icr = rightICR.add(leftICR).multiply(0.5);
            return icr;
        } else if (rightICR != null) {
            // Only right side has non-parallel wheels, ignore left side
            return rightICR;
        } else {
            // Only left side has non-parallel wheels, ignore right side   
            return leftICR;
        }
    }

    private Double2D calculateSideICR(Line[] axisLines) {
        Double2D sum = new Double2D(0, 0);
        int n = 0;
        for (int i = 0; i < axisLines.length; i++) {
            for (int j = i + 1; j < axisLines.length; j++) {
                if (axisLines[i].angle(axisLines[j]) > parallelMovementAngle) {
                    sum = sum.add(axisLines[i].intersect(axisLines[j]));
                    n++;
                }
            }
        }
        return n == 0 ? null : sum.multiply(1d / n);
    }

    public double[] getCurrentRotations() {
        return currentRotation;
    }

    public double getCurrentSpeed() {
        return currentSpeed;
    }

    public Double2D getLastMove() {
        return lastMove;
    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
        return new MultiWheeledInspector(this, 250);
    }
}
