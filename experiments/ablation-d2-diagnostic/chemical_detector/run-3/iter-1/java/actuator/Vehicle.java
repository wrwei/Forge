package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.Constants;
import chemical_detector.data.Angle;
import chemical_detector.data.Loc;

/** Motion surface of the Vehicle; records the last command issued. */
public final class Vehicle {

    @RoboChartType("real")
    private double velocity;

    private Angle direction = Angle.Front;

    private boolean randomWalking;

    private boolean shortRandomWalking;

    @RoboChartType("nat")
    private int lastPause;

    /** Moves at {@code speed} in {@code heading}; zero speed halts the Vehicle. */
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

    /**
     * Steers away from an obstacle on {@code side} at the linear-velocity constant:
     * left turns Right, right turns Left, front turns Back.
     */
    public void changeDirection(Loc side) {
        if (side == Loc.left) {
            move(Constants.LV, Angle.Right);
        } else if (side == Loc.right) {
            move(Constants.LV, Angle.Left);
        } else {
            move(Constants.LV, Angle.Back);
        }
    }

    /** Holds the current manoeuvre for {@code duration} time units. */
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

    public boolean shortRandomWalking() {
        return shortRandomWalking;
    }

    @RoboChartType("nat")
    public int lastPause() {
        return lastPause;
    }
}
