package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;

/**
 * Movement operations provided by the Vehicle platform. Tracks the last command issued.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double velocity;
    private Angle direction = Angle.Front;
    private boolean randomWalking;
    private boolean shortRandomWalking;
    @RoboChartType("nat")
    private int pauseRequested;
    private boolean sourceFound;

    /** Moves at velocity {@code lv} in direction {@code a} until superseded; zero velocity halts. */
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

    /** Starts a bounded random walk used for recovery when stuck. */
    public void shortRandomWalk() {
        this.randomWalking = false;
        this.shortRandomWalking = true;
    }

    /** Waits {@code duration} time units before the next behaviour. */
    public void pause(@RoboChartType("nat") int duration) {
        this.pauseRequested = duration;
    }

    /** Receives the flag signal: the chemical source has been located. */
    public void reportSourceFound() {
        this.sourceFound = true;
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

    public boolean shortRandomWalking() {
        return shortRandomWalking;
    }

    @RoboChartType("nat")
    public int pauseRequested() {
        return pauseRequested;
    }

    public boolean sourceFound() {
        return sourceFound;
    }
}
