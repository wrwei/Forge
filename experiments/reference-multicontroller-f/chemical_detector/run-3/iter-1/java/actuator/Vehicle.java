package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.types.Angle;

/**
 * Motion surface of the robot platform. Records the last command issued for inspection.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double velocity;
    private Angle direction = Angle.Front;
    private boolean randomWalking;
    @RoboChartType("nat")
    private int shortRandomWalks;
    @RoboChartType("nat")
    private int lastPause;
    private boolean sourceFound;

    /** Moves at {@code linearVelocity} towards {@code heading} until superseded; zero velocity halts. */
    public void move(@RoboChartType("real") double linearVelocity, Angle heading) {
        this.velocity = linearVelocity;
        this.direction = heading;
        this.randomWalking = false;
    }

    /** Starts an unbounded random walk. */
    public void randomWalk() {
        this.randomWalking = true;
    }

    /** Performs a bounded random walk. */
    public void shortRandomWalk() {
        this.randomWalking = false;
        this.shortRandomWalks = shortRandomWalks + 1;
    }

    /** Waits {@code duration} time units. */
    public void pause(@RoboChartType("nat") int duration) {
        this.lastPause = duration;
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

    @RoboChartType("nat")
    public int shortRandomWalks() {
        return shortRandomWalks;
    }

    @RoboChartType("nat")
    public int lastPause() {
        return lastPause;
    }

    public boolean sourceFound() {
        return sourceFound;
    }
}
