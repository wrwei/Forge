package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.types.Angle;

/**
 * Movement operations provided by the Vehicle platform. Tracks the last issued command.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double velocity;
    private Angle direction = Angle.Front;
    private boolean randomWalking;
    @RoboChartType("nat")
    private int shortWalks;
    @RoboChartType("nat")
    private int lastPause;

    /** Moves at velocity {@code lv} in direction {@code a} until superseded; zero velocity halts. */
    public void move(@RoboChartType("real") double lv, Angle a) {
        this.velocity = lv;
        this.direction = a;
        this.randomWalking = false;
    }

    /** Starts an unbounded random-walk search. */
    public void randomWalk() {
        this.randomWalking = true;
    }

    /** Performs a bounded (terminating) random walk. */
    public void shortRandomWalk() {
        this.randomWalking = false;
        this.shortWalks = shortWalks + 1;
    }

    /** Waits {@code duration} clock units. */
    public void pause(@RoboChartType("nat") int duration) {
        this.lastPause = duration;
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

    @RoboChartType("nat")
    public int shortWalks() {
        return shortWalks;
    }

    @RoboChartType("nat")
    public int lastPause() {
        return lastPause;
    }
}
