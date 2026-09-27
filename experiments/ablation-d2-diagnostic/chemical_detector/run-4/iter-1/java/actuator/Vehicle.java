package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;

/**
 * Movement operations provided by the Vehicle platform (CD-OP1..3) plus the bounded
 * wait used by timed entry behaviours. Tracks the last command issued for inspection.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double velocity;

    private Angle heading = Angle.Front;

    private boolean randomWalking;

    private boolean shortRandomWalking;

    @RoboChartType("nat")
    private int lastPause;

    /** Moves at {@code velocity} towards {@code direction} until superseded (CD-OP1). */
    public void move(@RoboChartType("real") double velocity, Angle direction) {
        this.velocity = velocity;
        this.heading = direction;
        this.randomWalking = false;
        this.shortRandomWalking = false;
    }

    /** Starts an unbounded random-walk search (CD-OP2). */
    public void randomWalk() {
        this.randomWalking = true;
        this.shortRandomWalking = false;
    }

    /** Starts a bounded, terminating random walk (CD-OP3). */
    public void shortRandomWalk() {
        this.shortRandomWalking = true;
        this.randomWalking = false;
    }

    /** Waits for {@code duration} time units before the next behaviour fires. */
    public void pause(@RoboChartType("nat") int duration) {
        this.lastPause = duration;
    }

    @RoboChartType("real")
    public double velocity() {
        return velocity;
    }

    public Angle heading() {
        return heading;
    }

    public boolean randomWalking() {
        return randomWalking;
    }

    public boolean shortRandomWalking() {
        return shortRandomWalking;
    }

    @RoboChartType("nat")
    public int lastPause() {
        return lastPause;
    }
}
