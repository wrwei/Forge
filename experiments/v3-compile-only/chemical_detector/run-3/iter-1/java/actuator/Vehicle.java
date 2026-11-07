package chemical_detector.actuator;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;

/** Motion operations of the Vehicle platform (CD-OP1 to CD-OP4). */
public final class Vehicle {
    @RoboChartType("real") private double velocity = 0.0;
    private Angle heading = Angle.Front;
    private boolean randomWalking = false;
    private boolean shortRandomWalking = false;
    @RoboChartType("nat") private long pauseDuration = 0;

    /**
     * Moves at the given velocity in the given body-relative direction until a
     * later command supersedes it; zero velocity facing Front halts (CD-OP1).
     */
    public void move(@RoboChartType("real") double speed, Angle direction) {
        this.velocity = speed;
        this.heading = direction;
        this.randomWalking = false;
        this.shortRandomWalking = false;
    }

    /** Starts an unbounded random-walk search (CD-OP2). */
    public void randomWalk() {
        this.randomWalking = true;
        this.shortRandomWalking = false;
    }

    /** Starts a bounded (short) random walk for recovery (CD-OP3). */
    public void shortRandomWalk() {
        this.randomWalking = false;
        this.shortRandomWalking = true;
    }

    /**
     * Steers away from an obstacle at lv (CD-OP4): left turns Right, right
     * turns Left, front turns Back.
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

    /** Holds the current manoeuvre for the given number of time units. */
    public void delay(@RoboChartType("nat") long duration) {
        this.pauseDuration = duration;
    }

    /** Last commanded velocity. */
    @RoboChartType("real")
    public double velocity() {
        return velocity;
    }

    /** Last commanded direction. */
    public Angle heading() {
        return heading;
    }

    /** Whether an unbounded random walk is in progress. */
    public boolean randomWalking() {
        return randomWalking;
    }

    /** Whether a short random walk is in progress. */
    public boolean shortRandomWalking() {
        return shortRandomWalking;
    }

    /** Duration of the last requested pause. */
    @RoboChartType("nat")
    public long pauseDuration() {
        return pauseDuration;
    }
}
