package chemical_detector.actuator;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.OutputEvent;

/** The Vehicle platform: movement operations and the flag signal; records the last command issued. */
public final class Vehicle {
    @RoboChartType("real") private double lastVelocity = 0.0;
    private Angle lastDirection = Angle.Front;
    private boolean randomWalking = false;
    private boolean shortRandomWalking = false;
    @RoboChartType("nat") private long lastPause = 0;
    private boolean flagRaised = false;

    /** CD-OP1: move at the given linear velocity in the given body-relative direction. */
    public void move(@RoboChartType("real") double velocity, Angle direction) {
        this.lastVelocity = velocity;
        this.lastDirection = direction;
        this.randomWalking = false;
        this.shortRandomWalking = false;
    }

    /** CD-OP2: start an unbounded random-walk search. */
    public void randomWalk() {
        this.randomWalking = true;
        this.shortRandomWalking = false;
    }

    /** CD-OP3: start a bounded (short) random walk. */
    public void shortRandomWalk() {
        this.shortRandomWalking = true;
        this.randomWalking = false;
    }

    /** CD-OP4: steer away from an obstacle at the linear-velocity constant. */
    public void changeDirection(Loc obstacleSide) {
        if (obstacleSide == Loc.left) {
            move(ChemicalDetectorConstants.LV, Angle.Right);
        } else if (obstacleSide == Loc.right) {
            move(ChemicalDetectorConstants.LV, Angle.Left);
        } else {
            move(ChemicalDetectorConstants.LV, Angle.Back);
        }
    }

    /** Holds the current manoeuvre for the given number of time units (a RoboChart wait). */
    public void pause(@RoboChartType("nat") long duration) {
        this.lastPause = duration;
    }

    /** Receives a signal for the platform; the flag reports that the source is located (CD-Evt7). */
    public void apply(OutputEvent signal) {
        if (signal instanceof OutputEvent.Flag) {
            this.flagRaised = true;
        }
    }

    /** The velocity of the last move. */
    @RoboChartType("real")
    public double lastVelocity() {
        return lastVelocity;
    }

    /** The direction of the last move. */
    public Angle lastDirection() {
        return lastDirection;
    }

    /** Whether an unbounded random walk is the current motion. */
    public boolean randomWalking() {
        return randomWalking;
    }

    /** Whether a short random walk is the current motion. */
    public boolean shortRandomWalking() {
        return shortRandomWalking;
    }

    /** The duration of the last pause. */
    @RoboChartType("nat")
    public long lastPause() {
        return lastPause;
    }

    /** Whether the flag signal has been received. */
    public boolean flagRaised() {
        return flagRaised;
    }
}
