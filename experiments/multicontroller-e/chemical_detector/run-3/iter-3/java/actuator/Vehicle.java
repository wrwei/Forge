package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.types.Angle;

/**
 * Movement operations provided by the robotic platform. The last command is
 * kept for inspection.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double velocity;

    private Angle direction = Angle.Front;

    private boolean randomWalking;

    private boolean shortRandomWalking;

    @RoboChartType("nat")
    private long waited;

    /** Moves at {@code speed} towards {@code heading}; zero speed halts. */
    public void move(@RoboChartType("real") double speed, Angle heading) {
        this.velocity = speed;
        this.direction = heading;
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

    /** Waits {@code duration} clock units before the next behaviour fires. */
    public void pause(@RoboChartType("nat") long duration) {
        this.waited = this.waited + duration;
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
    public long waited() {
        return waited;
    }
}
