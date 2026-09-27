package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;

/**
 * The robotic platform's movement surface: move, randomWalk and
 * shortRandomWalk, the timed pause, and the flag signal. Records the last
 * command issued so it can be inspected.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double velocity;
    private Angle direction = Angle.Front;
    private boolean randomWalking;
    private boolean shortRandomWalking;
    private boolean flagged;
    @RoboChartType("nat")
    private long lastPause;

    /** Moves at velocity {@code lv} in direction {@code a} until superseded. */
    public void move(@RoboChartType("real") double lv, Angle a) {
        this.velocity = lv;
        this.direction = a;
        this.randomWalking = false;
        this.shortRandomWalking = false;
    }

    /** Starts an unbounded random-walk search. */
    public void randomWalk() {
        this.randomWalking = true;
        this.shortRandomWalking = false;
    }

    /** Starts a bounded (terminating) random walk. */
    public void shortRandomWalk() {
        this.shortRandomWalking = true;
        this.randomWalking = false;
    }

    /** Holds the current behaviour for {@code duration} time units. */
    public void pause(@RoboChartType("nat") long duration) {
        this.lastPause = duration;
    }

    /** Signals that the chemical source has been located. */
    public void flag() {
        this.flagged = true;
    }

    @RoboChartType("real")
    public double velocity() {
        return velocity;
    }

    public Angle direction() {
        return direction;
    }

    public boolean isRandomWalking() {
        return randomWalking;
    }

    public boolean isShortRandomWalking() {
        return shortRandomWalking;
    }

    public boolean isFlagged() {
        return flagged;
    }

    @RoboChartType("nat")
    public long lastPause() {
        return lastPause;
    }
}
