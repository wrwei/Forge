package chemical_detector.actuator;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.OutputEvent;

/**
 * The Vehicle's movement operations (CD-OP1..4) and its receipt of the flag
 * event (CD-Evt7). Records the last command issued.
 */
public final class VehicleActuator {
    @RoboChartType("real") private double velocity = 0.0;
    private Angle heading = Angle.Front;
    private boolean randomWalking = false;
    private boolean shortRandomWalking = false;
    @RoboChartType("nat") private int lastPause = 0;
    private boolean flagged = false;

    /**
     * Moves at the given velocity in the given body-relative direction until
     * superseded (CD-OP1); zero velocity facing Front halts the Vehicle.
     */
    public void move(@RoboChartType("real") double velocity, Angle direction) {
        this.velocity = velocity;
        this.heading = direction;
        this.randomWalking = false;
        this.shortRandomWalking = false;
    }

    /** Starts an unbounded random walk (CD-OP2). */
    public void randomWalk() {
        this.randomWalking = true;
        this.shortRandomWalking = false;
    }

    /** Runs a bounded (short) random walk (CD-OP3). */
    public void shortRandomWalk() {
        this.randomWalking = false;
        this.shortRandomWalking = true;
    }

    /**
     * Steers away from an obstacle at the linear velocity (CD-OP4): an obstacle
     * on the left turns Right, on the right turns Left, in front turns Back.
     */
    public void changeDirection(Loc obstacleSide) {
        if (obstacleSide == Loc.left) {
            move(ChemicalDetectorConstants.LV, Angle.Right);
        } else if (obstacleSide == Loc.right) {
            move(ChemicalDetectorConstants.LV, Angle.Left);
        } else {
            move(ChemicalDetectorConstants.LV, Angle.Back);
        }
    }

    /** Waits the given number of time units before the controller continues. */
    public void pause(@RoboChartType("nat") int duration) {
        this.lastPause = duration;
    }

    /** Receives an event addressed to the Vehicle: the flag (CD-Evt7). */
    public void apply(OutputEvent signal) {
        if (signal instanceof OutputEvent.Flag) {
            this.flagged = true;
        }
    }

    /** The velocity of the last move. */
    @RoboChartType("real")
    public double velocity() {
        return velocity;
    }

    /** The direction of the last move. */
    public Angle heading() {
        return heading;
    }

    /** Whether an unbounded random walk is in progress. */
    public boolean isRandomWalking() {
        return randomWalking;
    }

    /** Whether a short random walk was the last motion command. */
    public boolean isShortRandomWalking() {
        return shortRandomWalking;
    }

    /** The duration of the last pause. */
    @RoboChartType("nat")
    public int lastPause() {
        return lastPause;
    }

    /** Whether the flag event has been received. */
    public boolean isFlagged() {
        return flagged;
    }
}
