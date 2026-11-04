package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.event.OutputEvent;

/**
 * The Vehicle's movement operations and its flag input; records the last
 * command. The Vehicle starts in an unbounded random walk, the behaviour of the
 * movement subsystem's initial state.
 */
public final class VehicleActuator {

    @RoboChartType("real") private double velocity = 0.0;
    private Angle heading = Angle.Front;
    private boolean randomWalking = true;
    private boolean shortRandomWalking = false;
    private boolean flagged = false;

    /**
     * CD-OP1: move at the given linear velocity in the given direction until a
     * later command supersedes it; zero velocity facing Front halts the Vehicle.
     */
    public void move(@RoboChartType("real") double speed, Angle direction) {
        velocity = speed;
        heading = direction;
        randomWalking = false;
        shortRandomWalking = false;
    }

    /** CD-OP2: unbounded random-walk search, until a later command supersedes it. */
    public void randomWalk() {
        randomWalking = true;
        shortRandomWalking = false;
    }

    /** CD-OP3: random walk for a bounded period. */
    public void shortRandomWalk() {
        randomWalking = false;
        shortRandomWalking = true;
    }

    /** CD-Evt7: receives the flag event signalling that the source is located. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Flag) {
            flagged = true;
        }
    }

    /** Velocity of the last move command. */
    @RoboChartType("real")
    public double commandedVelocity() {
        return velocity;
    }

    /** Direction of the last move command. */
    public Angle commandedHeading() {
        return heading;
    }

    /** Whether an unbounded random walk is in progress. */
    public boolean randomWalkActive() {
        return randomWalking;
    }

    /** Whether a bounded random walk is in progress. */
    public boolean shortRandomWalkActive() {
        return shortRandomWalking;
    }

    /** Whether the flag event has been received. */
    public boolean flagRaised() {
        return flagged;
    }
}
