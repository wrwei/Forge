package chemical_detector.actuator;

import chemical_detector.DetectorConstants;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.OutputEvent;

/**
 * The Vehicle's actuation surface: the movement operations it provides
 * (CD-OP1..3), the controller's changeDirection (CD-OP4), and the flag
 * signal (CD-Evt7). It records the last command issued.
 */
public final class VehicleActuator {
    @RoboChartType("real") private double velocity = 0.0;
    private Angle direction = Angle.Front;
    private boolean randomWalking = false;
    private boolean shortRandomWalking = false;
    @RoboChartType("nat") private long lastPause = 0;
    private boolean flagRaised = false;

    /**
     * move(lv, a) (CD-OP1): move at the given velocity in the given direction
     * until a later command supersedes it; zero velocity facing Front halts.
     */
    public void move(@RoboChartType("real") double velocity, Angle direction) {
        this.velocity = velocity;
        this.direction = direction;
        this.randomWalking = false;
        this.shortRandomWalking = false;
    }

    /** randomWalk() (CD-OP2): start an unbounded random walk. */
    public void randomWalk() {
        this.randomWalking = true;
        this.shortRandomWalking = false;
    }

    /** shortRandomWalk() (CD-OP3): start a random walk that ends by itself. */
    public void shortRandomWalk() {
        this.shortRandomWalking = true;
        this.randomWalking = false;
    }

    /**
     * changeDirection(l) (CD-OP4): steer away from an obstacle at lv; left
     * turns Right, right turns Left, front turns Back.
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

    /** Holds the current manoeuvre for the given number of time units. */
    public void pause(@RoboChartType("nat") long duration) {
        this.lastPause = duration;
    }

    /** Receives flag (CD-Evt7): the chemical source has been located. */
    public void apply(OutputEvent output) {
        if (output instanceof OutputEvent.Flag) {
            this.flagRaised = true;
        }
    }

    /** The last commanded velocity. */
    @RoboChartType("real")
    public double velocity() {
        return velocity;
    }

    /** The last commanded direction. */
    public Angle direction() {
        return direction;
    }

    /** Whether an unbounded random walk is in progress. */
    public boolean randomWalking() {
        return randomWalking;
    }

    /** Whether a bounded random walk is in progress. */
    public boolean shortRandomWalking() {
        return shortRandomWalking;
    }

    /** The duration of the last pause. */
    @RoboChartType("nat")
    public long lastPause() {
        return lastPause;
    }

    /** Whether flag has been received. */
    public boolean flagRaised() {
        return flagRaised;
    }
}
