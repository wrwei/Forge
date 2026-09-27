package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;

/**
 * Sensing-and-actuation surface of the robot. Records the last command of each
 * kind so the platform (and tests) can inspect what the controllers issued.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double distanceTravelled;
    @RoboChartType("real")
    private double velocity;
    private Angle direction = Angle.Front;
    private boolean randomWalking;
    private boolean shortRandomWalking;
    @RoboChartType("nat")
    private int pendingPause;
    @RoboChartType("nat")
    private int flagCount;

    /** Moves at {@code lv} towards {@code a} until superseded; zero velocity halts. */
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

    /** Starts a bounded (terminating) random walk. */
    public void shortRandomWalk() {
        this.shortRandomWalking = true;
        this.randomWalking = false;
    }

    /** Requests that the controller's behaviour wait {@code duration} time units. */
    public void pause(@RoboChartType("nat") int duration) {
        this.pendingPause = duration;
    }

    /** Signals that the chemical source has been located. */
    public void flag() {
        this.flagCount = this.flagCount + 1;
    }

    /** Cumulative distance travelled. */
    @RoboChartType("real")
    public double odometer() {
        return this.distanceTravelled;
    }

    /** Platform side: accumulates distance reported by the wheels. */
    public void travel(@RoboChartType("real") double distance) {
        this.distanceTravelled = this.distanceTravelled + distance;
    }

    @RoboChartType("real")
    public double velocity() {
        return this.velocity;
    }

    public Angle direction() {
        return this.direction;
    }

    public boolean isRandomWalking() {
        return this.randomWalking;
    }

    public boolean isShortRandomWalking() {
        return this.shortRandomWalking;
    }

    @RoboChartType("nat")
    public int pendingPause() {
        return this.pendingPause;
    }

    @RoboChartType("nat")
    public int flagCount() {
        return this.flagCount;
    }
}
