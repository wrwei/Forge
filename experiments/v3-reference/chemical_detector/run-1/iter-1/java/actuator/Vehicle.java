package chemical_detector.actuator;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.OutputEvent;

/**
 * Movement operations of the Vehicle and the flag signal it receives;
 * records the last command issued.
 */
public final class Vehicle {
    @RoboChartType("real") private double velocity = 0.0;
    private Angle heading = Angle.Front;
    private boolean randomWalking = false;
    private boolean shortRandomWalking = false;
    private boolean flagged = false;

    /** Moves at the given velocity in the given body-relative direction. */
    public void move(@RoboChartType("real") double velocity, Angle direction) {
        this.velocity = velocity;
        this.heading = direction;
        this.randomWalking = false;
        this.shortRandomWalking = false;
    }

    /** Starts an unbounded random walk. */
    public void randomWalk() {
        this.randomWalking = true;
        this.shortRandomWalking = false;
    }

    /** Performs a bounded (short) random walk. */
    public void shortRandomWalk() {
        this.randomWalking = false;
        this.shortRandomWalking = true;
    }

    /** Steers away from an obstacle on the given side at the linear velocity. */
    public void changeDirection(Loc side) {
        if (side == Loc.left) {
            move(ChemicalDetectorConstants.LV, Angle.Right);
        } else if (side == Loc.right) {
            move(ChemicalDetectorConstants.LV, Angle.Left);
        } else {
            move(ChemicalDetectorConstants.LV, Angle.Back);
        }
    }

    /** Receives a signal from the movement subsystem (flag). */
    public void apply(OutputEvent signal) {
        if (signal instanceof OutputEvent.Flag) {
            this.flagged = true;
        }
    }

    /** Velocity of the last move command. */
    @RoboChartType("real")
    public double lastVelocity() {
        return velocity;
    }

    /** Direction of the last move command. */
    public Angle lastHeading() {
        return heading;
    }

    /** True while an unbounded random walk is the current command. */
    public boolean isRandomWalking() {
        return randomWalking;
    }

    /** True while a short random walk is the current command. */
    public boolean isShortRandomWalking() {
        return shortRandomWalking;
    }

    /** True once the flag signal has been received. */
    public boolean isFlagged() {
        return flagged;
    }
}
