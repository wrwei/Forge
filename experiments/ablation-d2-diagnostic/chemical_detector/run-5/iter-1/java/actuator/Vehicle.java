package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;

/**
 * The robotic platform's actuation surface. Each command supersedes the previous one;
 * the last issued command is kept for inspection.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double velocity;
    private Angle heading = Angle.Front;
    private boolean randomWalking;
    private boolean shortRandomWalking;
    @RoboChartType("nat")
    private int lastPause;
    private boolean flagRaised;

    /** Moves at velocity {@code lv} in direction {@code a}; zero velocity halts. */
    public void move(@RoboChartType("real") double lv, Angle a) {
        this.velocity = lv;
        this.heading = a;
        this.randomWalking = false;
        this.shortRandomWalking = false;
    }

    /** Starts an unbounded random-walk search. */
    public void randomWalk() {
        this.randomWalking = true;
        this.shortRandomWalking = false;
    }

    /** Starts a bounded random walk that terminates on its own. */
    public void shortRandomWalk() {
        this.shortRandomWalking = true;
        this.randomWalking = false;
    }

    /** Lets the current manoeuvre run for {@code duration} time units. */
    public void pause(@RoboChartType("nat") int duration) {
        this.lastPause = duration;
    }

    /** Receives the flag signal: the chemical source has been located. */
    public void raiseFlag() {
        this.flagRaised = true;
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

    public boolean flagRaised() {
        return flagRaised;
    }
}
