package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.data.Angle;

/** Movement operations provided by the Vehicle; records the last command for inspection. */
public final class Vehicle {

    @RoboChartType("real")
    private double velocity;

    private Angle heading = Angle.Front;

    private boolean randomWalking;

    private boolean shortRandomWalking;

    @RoboChartType("nat")
    private long lastPause;

    /** move: travel at velocity lv in direction a until superseded; zero velocity halts. */
    public void move(@RoboChartType("real") double lv, Angle a) {
        this.velocity = lv;
        this.heading = a;
        this.randomWalking = false;
        this.shortRandomWalking = false;
    }

    /** randomWalk: unbounded random-walk search. */
    public void randomWalk() {
        this.randomWalking = true;
        this.shortRandomWalking = false;
    }

    /** shortRandomWalk: bounded random walk used to get out when stuck. */
    public void shortRandomWalk() {
        this.shortRandomWalking = true;
        this.randomWalking = false;
    }

    /** Holds the current manoeuvre for the given number of clock units. */
    public void pause(@RoboChartType("nat") long duration) {
        this.lastPause = duration;
    }

    @RoboChartType("real")
    public double velocity() {
        return velocity;
    }

    public Angle heading() {
        return heading;
    }

    public boolean isRandomWalking() {
        return randomWalking;
    }

    public boolean isShortRandomWalking() {
        return shortRandomWalking;
    }

    @RoboChartType("nat")
    public long lastPause() {
        return lastPause;
    }
}
