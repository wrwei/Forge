package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.data.Angle;

/**
 * Movement surface of the Vehicle. Records the last command issued so the
 * platform (or a test) can act on it.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double velocity;
    private Angle direction = Angle.Front;
    private boolean randomWalking;
    private boolean boundedWalk;
    @RoboChartType("nat")
    private int requestedWait;

    /**
     * Moves at linear velocity {@code lv} in body-relative direction {@code a}
     * until superseded. Zero velocity facing Front halts the Vehicle.
     */
    public void move(@RoboChartType("real") double lv, Angle a) {
        this.velocity = lv;
        this.direction = a;
        this.randomWalking = false;
        this.boundedWalk = false;
    }

    /** Starts an unbounded random-walk search. */
    public void randomWalk() {
        this.randomWalking = true;
        this.boundedWalk = false;
    }

    /** Starts a random walk that terminates after a bounded period. */
    public void shortRandomWalk() {
        this.randomWalking = true;
        this.boundedWalk = true;
    }

    /** Holds the current behaviour for {@code duration} time units. */
    public void pause(@RoboChartType("nat") int duration) {
        this.requestedWait = duration;
    }

    @RoboChartType("real")
    public double velocity() {
        return velocity;
    }

    public Angle direction() {
        return direction;
    }

    public boolean randomWalking() {
        return randomWalking;
    }

    public boolean boundedWalk() {
        return boundedWalk;
    }

    @RoboChartType("nat")
    public int requestedWait() {
        return requestedWait;
    }

    /** True when the last command was a zero-velocity move. */
    public boolean halted() {
        return velocity == 0.0 && !randomWalking;
    }
}
