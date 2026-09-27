package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.data.Angle;
import chemical_detector.event.OutputEvent;

/**
 * Actuation surface of the Vehicle. Records the last command of each kind so
 * the platform (or a test) can inspect it.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double velocity;
    private Angle heading = Angle.Front;
    private boolean randomWalking;
    private boolean shortRandomWalking;
    @RoboChartType("nat")
    private int lastPause;
    @RoboChartType("nat")
    private int flags;

    /** Moves at velocity {@code lv} towards {@code a} until superseded; zero velocity halts. */
    public void move(@RoboChartType("real") double lv, Angle a) {
        velocity = lv;
        heading = a;
        randomWalking = false;
        shortRandomWalking = false;
    }

    /** Starts an unbounded random-walk search. */
    public void randomWalk() {
        randomWalking = true;
        shortRandomWalking = false;
    }

    /** Starts a bounded random walk used to get unstuck. */
    public void shortRandomWalk() {
        shortRandomWalking = true;
        randomWalking = false;
    }

    /** Holds the current manoeuvre for {@code duration} time units. */
    public void pause(@RoboChartType("nat") int duration) {
        lastPause = duration;
    }

    /** Receives an event addressed to the Vehicle. */
    public void apply(OutputEvent event) {
        if (event instanceof OutputEvent.Flag) {
            flags = flags + 1;
        }
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

    /** Number of flag events received. */
    @RoboChartType("nat")
    public int flags() {
        return flags;
    }
}
