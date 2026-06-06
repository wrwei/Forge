package chemdetector.actuator;

import chemdetector.annotation.RoboChartType;
import chemdetector.constants.Constants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Loc;

/**
 * Actuation surface of the platform: linear movement, random-walk
 * search, obstacle evasion, and the timing primitive.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double velocity = 0.0;
    private Angle heading = Angle.Front;
    private boolean walking = false;

    /**
     * Moves with linear velocity lv in direction a relative to the
     * robot body. move(0, Angle.Front) halts the Vehicle.
     */
    public void move(@RoboChartType("real") double lv, Angle a) {
        this.velocity = lv;
        this.heading = a;
        this.walking = false;
    }

    /**
     * Unbounded random-walk search.
     */
    public void randomWalk() {
        this.walking = true;
    }

    /**
     * Bounded random walk used for stuck recovery.
     */
    public void shortRandomWalk() {
        this.walking = true;
    }

    /**
     * Steers away from a detected obstacle: left obstacle turns right,
     * right obstacle turns left, front obstacle turns back, in each
     * case at the configured linear velocity.
     */
    public void changeDirection(Loc side) {
        if (side == Loc.left) {
            move(Constants.LV, Angle.Right);
        } else if (side == Loc.right) {
            move(Constants.LV, Angle.Left);
        } else if (side == Loc.front) {
            move(Constants.LV, Angle.Back);
        }
    }

    /**
     * Timing primitive: gives the current manoeuvre time to complete.
     */
    public void pause(@RoboChartType("nat") int duration) {
        // Maps to the RoboChart wait(duration) statement; no runtime
        // behaviour is needed in the extracted model.
    }

    public double velocity() {
        return velocity;
    }

    public Angle heading() {
        return heading;
    }

    public boolean walking() {
        return walking;
    }
}
