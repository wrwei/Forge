package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;

/** Movement operations provided by the Vehicle; records the last command. */
public final class VehicleActuator {

    @RoboChartType("real") private double lastSpeed = 0.0;
    private Angle lastHeading = Angle.Front;
    private boolean randomWalking = false;
    @RoboChartType("nat") private int shortRandomWalks = 0;

    /**
     * Moves at {@code speed} in direction {@code heading} until superseded
     * (CD-OP1); zero speed facing Front halts the Vehicle.
     */
    public void move(@RoboChartType("real") double speed, Angle heading) {
        this.lastSpeed = speed;
        this.lastHeading = heading;
        this.randomWalking = false;
    }

    /** Starts an unbounded random walk (CD-OP2). */
    public void randomWalk() {
        this.randomWalking = true;
    }

    /** Performs a bounded random walk (CD-OP3). */
    public void shortRandomWalk() {
        this.randomWalking = false;
        this.shortRandomWalks = shortRandomWalks + 1;
    }

    /** Speed of the last move command. */
    @RoboChartType("real")
    public double lastSpeed() {
        return lastSpeed;
    }

    /** Heading of the last move command. */
    public Angle lastHeading() {
        return lastHeading;
    }

    /** Whether an unbounded random walk is in progress. */
    public boolean randomWalking() {
        return randomWalking;
    }

    /** Number of bounded random walks performed. */
    @RoboChartType("nat")
    public int shortRandomWalks() {
        return shortRandomWalks;
    }
}
