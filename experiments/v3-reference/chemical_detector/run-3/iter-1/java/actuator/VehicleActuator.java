package chemical_detector.actuator;

import chemical_detector.DetectorConstants;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.OutputEvent;

/**
 * The Vehicle's actuation surface: the movement operations it provides
 * (CD-OP1 to CD-OP4) and the flag signal it receives (CD-Evt7). Records the
 * last command issued.
 */
public final class VehicleActuator {

    @RoboChartType("real") private double lastVelocity = 0.0;
    private Angle lastDirection = Angle.Front;
    private boolean randomWalking = false;
    private boolean flagged = false;

    /**
     * Moves at a velocity in a body-relative direction until superseded
     * (CD-OP1); zero velocity facing Front halts the Vehicle.
     *
     * @param velocity the linear velocity
     * @param direction the body-relative direction
     */
    public void move(@RoboChartType("real") double velocity, Angle direction) {
        this.lastVelocity = velocity;
        this.lastDirection = direction;
        this.randomWalking = false;
    }

    /** Starts an unbounded random-walk search (CD-OP2). */
    public void randomWalk() {
        this.randomWalking = true;
    }

    /** Performs a bounded random walk (CD-OP3). */
    public void shortRandomWalk() {
        this.randomWalking = true;
    }

    /**
     * Steers away from an obstacle at lv (CD-OP4): left turns Right, right
     * turns Left, front turns Back.
     *
     * @param side the side on which the obstacle was detected
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
     * Receives a signal sent to the Vehicle; the flag (CD-Evt7) reports that
     * the source is found.
     *
     * @param sent the signal
     */
    public void apply(OutputEvent sent) {
        if (sent instanceof OutputEvent.Flag) {
            this.flagged = true;
        }
    }

    /**
     * The last commanded velocity.
     *
     * @return the velocity of the last move
     */
    @RoboChartType("real")
    public double lastVelocity() {
        return lastVelocity;
    }

    /**
     * The last commanded direction.
     *
     * @return the direction of the last move
     */
    public Angle lastDirection() {
        return lastDirection;
    }

    /**
     * Whether a random walk was the last motion commanded.
     *
     * @return true while random-walking
     */
    public boolean randomWalking() {
        return randomWalking;
    }

    /**
     * Whether the flag has been received.
     *
     * @return true once the source has been reported found
     */
    public boolean flagged() {
        return flagged;
    }
}
