package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;

/**
 * Motion surface of the robotic platform. Records the last command issued so
 * the platform driver (and tests) can inspect it.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double velocity;
    private Angle heading = Angle.Front;
    private boolean randomWalking;
    private boolean shortRandomWalking;
    @RoboChartType("nat")
    private int holdDuration;
    private boolean flagRaised;

    /** Moves at velocity {@code lv} in direction {@code a} until superseded; zero velocity halts. */
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

    /** Starts a bounded (terminating) random walk. */
    public void shortRandomWalk() {
        this.shortRandomWalking = true;
        this.randomWalking = false;
    }

    /** Holds the current manoeuvre for {@code duration} time units before the next behaviour. */
    public void pause(@RoboChartType("nat") int duration) {
        this.holdDuration = duration;
    }

    /** Receives the flag signal: the chemical source has been located. */
    public void flag() {
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
    public int holdDuration() {
        return holdDuration;
    }

    public boolean flagRaised() {
        return flagRaised;
    }
}
