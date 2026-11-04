package chemical_detector.actuator;

import chemical_detector.Angle;
import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.Loc;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.event.OutputEvent;

/**
 * The Vehicle's actuation surface: the movement operations it provides (CD-OP1 to CD-OP3),
 * the changeDirection manoeuvre (CD-OP4), timed waits, and the flag signal (CD-Evt7).
 * Records the last command issued.
 */
public final class VehicleActuator {

    @RoboChartType("real") private double velocity = 0.0;
    private Angle direction = Angle.Front;
    private boolean randomWalking = false;
    private boolean boundedWalk = false;
    @RoboChartType("nat") private int pauseDuration = 0;
    private boolean flagRaised = false;

    /**
     * CD-OP1: moves at the given velocity in the given body-relative direction until a later
     * command supersedes it; zero velocity facing Front halts the Vehicle.
     */
    public void move(@RoboChartType("real") double velocity, Angle direction) {
        this.velocity = velocity;
        this.direction = direction;
        this.randomWalking = false;
        this.boundedWalk = false;
    }

    /** CD-OP2: starts an unbounded random walk. */
    public void randomWalk() {
        this.randomWalking = true;
        this.boundedWalk = false;
    }

    /** CD-OP3: starts a random walk that runs for a bounded period. */
    public void shortRandomWalk() {
        this.randomWalking = true;
        this.boundedWalk = true;
    }

    /**
     * CD-OP4: steers away from an obstacle at the linear velocity: an obstacle on the left
     * turns the robot right, on the right turns it left, in front turns it back.
     */
    public void changeDirection(Loc side) {
        if (side == Loc.left) {
            move(ChemicalDetectorConstants.LV, Angle.Right);
        } else if (side == Loc.right) {
            move(ChemicalDetectorConstants.LV, Angle.Left);
        } else {
            move(ChemicalDetectorConstants.LV, Angle.Back);
        }
    }

    /** Holds the current command for the given number of control cycles. */
    public void pause(@RoboChartType("nat") int duration) {
        this.pauseDuration = duration;
    }

    /** CD-Evt7: receives the flag signal from the movement subsystem. */
    public void apply(OutputEvent signal) {
        if (signal instanceof OutputEvent.Flag) {
            this.flagRaised = true;
        }
    }

    /** The velocity of the last move command. */
    @RoboChartType("real")
    public double velocity() {
        return velocity;
    }

    /** The direction of the last move command. */
    public Angle direction() {
        return direction;
    }

    /** Whether the flag signal has been received. */
    public boolean flagRaised() {
        return flagRaised;
    }
}
