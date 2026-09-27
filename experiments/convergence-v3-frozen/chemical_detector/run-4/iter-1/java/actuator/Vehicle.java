package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.data.Angle;

/**
 * The Vehicle's actuation surface: move, randomWalk, shortRandomWalk, the flag signal
 * and a bounded pause. Records the last-issued commands for inspection.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double lastVelocity;
    private Angle lastDirection = Angle.Front;
    private boolean randomWalking;
    @RoboChartType("nat")
    private int shortRandomWalkCount;
    private boolean flagged;
    @RoboChartType("nat")
    private long lastPause;

    /** Move at the given velocity in the given body-relative direction; zero velocity halts. */
    public void move(@RoboChartType("real") double velocity, Angle direction) {
        this.lastVelocity = velocity;
        this.lastDirection = direction;
        this.randomWalking = false;
    }

    /** Start an unbounded random walk. */
    public void randomWalk() {
        this.randomWalking = true;
    }

    /** Perform a bounded (terminating) random walk. */
    public void shortRandomWalk() {
        this.shortRandomWalkCount = this.shortRandomWalkCount + 1;
        this.randomWalking = false;
    }

    /** Signal that the chemical source has been located. */
    public void flag() {
        this.flagged = true;
    }

    /** Let the given number of clock units pass before the next behaviour. */
    public void pause(@RoboChartType("nat") long duration) {
        this.lastPause = duration;
    }

    @RoboChartType("real")
    public double lastVelocity() {
        return lastVelocity;
    }

    public Angle lastDirection() {
        return lastDirection;
    }

    public boolean isRandomWalking() {
        return randomWalking;
    }

    @RoboChartType("nat")
    public int shortRandomWalkCount() {
        return shortRandomWalkCount;
    }

    public boolean isFlagged() {
        return flagged;
    }

    @RoboChartType("nat")
    public long lastPause() {
        return lastPause;
    }
}
