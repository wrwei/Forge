package chemical_detector.actuator;

import chemical_detector.DetectorConstants;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.OutputEvent;

/**
 * The Vehicle's movement operations and its flag input. Records the last
 * command issued.
 */
public final class VehicleActuator {

    @RoboChartType("real") private double lastVelocity = 0.0;
    private Angle lastDirection = Angle.Front;
    private boolean randomWalking = false;
    private boolean flagRaised = false;

    /**
     * Moves at the given velocity in the given body-relative direction until a
     * later command supersedes it; zero velocity halts the Vehicle.
     */
    public void move(@RoboChartType("real") double velocity, Angle direction) {
        this.lastVelocity = velocity;
        this.lastDirection = direction;
        this.randomWalking = false;
    }

    /**
     * Starts an unbounded random-walk search, until a later command supersedes it.
     */
    public void randomWalk() {
        this.randomWalking = true;
    }

    /**
     * Performs a bounded random walk; it ends by itself.
     */
    public void shortRandomWalk() {
        this.randomWalking = false;
    }

    /**
     * Steers away from an obstacle at linear velocity: an obstacle on the left
     * turns the robot right, on the right turns it left, in front turns it back.
     */
    public void changeDirection(Loc side) {
        if (side == Loc.left) {
            move(DetectorConstants.LV, Angle.Right);
        } else if (side == Loc.right) {
            move(DetectorConstants.LV, Angle.Left);
        } else {
            move(DetectorConstants.LV, Angle.Back);
        }
    }

    /**
     * Receives a signal from the movement controller (the flag event).
     */
    public void apply(OutputEvent signal) {
        if (signal instanceof OutputEvent.Flag) {
            this.flagRaised = true;
        }
    }

    /**
     * Velocity of the last move command.
     */
    @RoboChartType("real")
    public double lastVelocity() {
        return lastVelocity;
    }

    /**
     * Direction of the last move command.
     */
    public Angle lastDirection() {
        return lastDirection;
    }

    /**
     * Whether an unbounded random walk is in progress.
     */
    public boolean randomWalking() {
        return randomWalking;
    }

    /**
     * Whether the flag event has been received.
     */
    public boolean flagRaised() {
        return flagRaised;
    }
}
