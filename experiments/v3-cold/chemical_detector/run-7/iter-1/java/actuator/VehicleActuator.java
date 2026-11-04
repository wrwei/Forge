package chemical_detector.actuator;

import chemical_detector.DetectorConstants;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.OutputEvent;

/** The Vehicle's movement operations and flag output; records the last command issued. */
public final class VehicleActuator {

    @RoboChartType("real") private double velocity = 0.0;
    private Angle heading = Angle.Front;
    private boolean randomWalking = false;
    private boolean shortRandomWalking = false;
    private boolean flagRaised = false;

    /** Moves at the given velocity in the given body-relative direction until superseded. */
    public void move(@RoboChartType("real") double velocity, Angle heading) {
        this.velocity = velocity;
        this.heading = heading;
        this.randomWalking = false;
        this.shortRandomWalking = false;
    }

    /** Starts an unbounded random walk. */
    public void randomWalk() {
        this.randomWalking = true;
        this.shortRandomWalking = false;
    }

    /** Starts a random walk that terminates after a bounded period. */
    public void shortRandomWalk() {
        this.randomWalking = false;
        this.shortRandomWalking = true;
    }

    /** Steers away from an obstacle: left turns Right, right turns Left, front turns Back, at lv. */
    public void changeDirection(Loc obstacleSide) {
        if (obstacleSide == Loc.left) {
            move(DetectorConstants.LV, Angle.Right);
        } else if (obstacleSide == Loc.right) {
            move(DetectorConstants.LV, Angle.Left);
        } else {
            move(DetectorConstants.LV, Angle.Back);
        }
    }

    /** Receives an output event; a flag event reports that the source has been located. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Flag) {
            this.flagRaised = true;
        }
    }

    /** The last commanded velocity. */
    @RoboChartType("real")
    public double velocity() {
        return velocity;
    }

    /** The last commanded direction. */
    public Angle heading() {
        return heading;
    }

    /** Whether an unbounded random walk is the current motion. */
    public boolean randomWalking() {
        return randomWalking;
    }

    /** Whether a bounded random walk is the current motion. */
    public boolean shortRandomWalking() {
        return shortRandomWalking;
    }

    /** Whether the flag event has been received. */
    public boolean flagRaised() {
        return flagRaised;
    }
}
