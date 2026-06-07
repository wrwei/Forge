package chemdetector.actuator;

import chemdetector.annotation.RoboChartType;
import chemdetector.data.Angle;
import chemdetector.data.Loc;

/**
 * The sensing-and-actuation surface of the robot: movement operations,
 * the odometer, and the success flag. Motion state is recorded for
 * inspection; the physical platform realises the commands.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double lastVelocity;
    private Angle lastDirection = Angle.Front;
    private boolean flagged;
    private boolean randomWalking;
    @RoboChartType("real")
    private double distanceTravelled;

    /**
     * Moves with linear velocity {@code velocity} in body-relative
     * direction {@code direction}. Zero velocity facing Front halts the
     * Vehicle.
     */
    public void move(@RoboChartType("real") double velocity, Angle direction) {
        this.lastVelocity = velocity;
        this.lastDirection = direction;
        this.randomWalking = false;
    }

    /** Performs an unbounded random-walk search. */
    public void randomWalk() {
        this.randomWalking = true;
    }

    /** Performs a bounded (short) random walk for recovery. */
    public void shortRandomWalk() {
        this.randomWalking = true;
    }

    /**
     * Steers away from an obstacle on side {@code side}, moving at
     * velocity {@code velocity}: left turns right, right turns left,
     * front turns back.
     */
    public void changeDirection(@RoboChartType("real") double velocity, Loc side) {
        if (side == Loc.left) {
            move(velocity, Angle.Right);
        } else if (side == Loc.right) {
            move(velocity, Angle.Left);
        } else if (side == Loc.front) {
            move(velocity, Angle.Back);
        }
    }

    /** Waits {@code duration} time units for the current action to settle. */
    public void pause(@RoboChartType("nat") int duration) {
        // Timing is realised by the platform; no simulated delay here.
    }

    /** Signals that the chemical source has been located. */
    public void flag() {
        this.flagged = true;
    }

    /** Cumulative distance travelled. */
    @RoboChartType("real")
    public double odometer() {
        return distanceTravelled;
    }

    /** Advances the odometer; called by the platform as the robot moves. */
    public void advance(@RoboChartType("real") double dist) {
        this.distanceTravelled = this.distanceTravelled + dist;
    }

    public double lastVelocity() {
        return lastVelocity;
    }

    public Angle lastDirection() {
        return lastDirection;
    }

    public boolean flagged() {
        return flagged;
    }

    public boolean randomWalking() {
        return randomWalking;
    }
}
