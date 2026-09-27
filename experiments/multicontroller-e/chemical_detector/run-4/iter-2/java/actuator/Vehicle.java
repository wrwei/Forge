package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.event.OutputEvent;

/**
 * Actuation surface of the robot. Records the last command of each kind so the
 * platform (and tests) can inspect it; it also receives the flag event.
 */
public final class Vehicle implements OutputPort {

    @RoboChartType("real")
    private double velocity;
    private Angle heading = Angle.Front;
    private boolean randomWalking;
    private boolean shortRandomWalking;
    @RoboChartType("nat")
    private int pauseDuration;
    private boolean flagged;

    /** Moves at velocity {@code lv} in direction {@code a} until superseded. */
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

    /** Starts a bounded random walk used to get unstuck. */
    public void shortRandomWalk() {
        this.shortRandomWalking = true;
        this.randomWalking = false;
    }

    /** Holds the current manoeuvre for {@code duration} time units. */
    public void pause(@RoboChartType("nat") int duration) {
        this.pauseDuration = duration;
    }

    @Override
    public void send(OutputEvent outputEvent) {
        if (outputEvent instanceof OutputEvent.Flag) {
            this.flagged = true;
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
    public int pauseDuration() {
        return pauseDuration;
    }

    public boolean flagged() {
        return flagged;
    }
}
