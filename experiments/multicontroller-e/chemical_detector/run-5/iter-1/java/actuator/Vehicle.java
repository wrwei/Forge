package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;

/**
 * Movement surface of the Vehicle (CD-OP1..3). Records the last command issued so the
 * platform can execute it and tests can inspect it.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double velocity;
    private Angle direction = Angle.Front;
    private boolean randomWalking;
    @RoboChartType("nat")
    private int shortRandomWalks;
    @RoboChartType("nat")
    private long lastPause;
    private boolean sourceFound;

    /** Moves at the given linear velocity in the given body-relative direction (CD-OP1). */
    public void move(@RoboChartType("real") double velocity, Angle direction) {
        this.velocity = velocity;
        this.direction = direction;
        this.randomWalking = false;
    }

    /** Starts an unbounded random-walk search (CD-OP2). */
    public void randomWalk() {
        this.randomWalking = true;
    }

    /** Performs a bounded random walk used to get unstuck (CD-OP3). */
    public void shortRandomWalk() {
        this.randomWalking = false;
        this.shortRandomWalks = this.shortRandomWalks + 1;
    }

    /** Lets the current manoeuvre run for the given duration before the next behaviour. */
    public void pause(@RoboChartType("nat") long duration) {
        this.lastPause = duration;
    }

    /** Receives the flag signal: the chemical source has been located (CD-Evt7). */
    public void signalSourceFound() {
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

    @RoboChartType("nat")
    public int shortRandomWalks() {
        return shortRandomWalks;
    }

    @RoboChartType("nat")
    public long lastPause() {
        return lastPause;
    }

    public boolean sourceFound() {
        return sourceFound;
    }
}
