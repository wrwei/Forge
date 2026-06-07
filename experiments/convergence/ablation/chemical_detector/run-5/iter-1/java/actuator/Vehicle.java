package chemdetector.actuator;

import chemdetector.annotation.RoboChartType;
import chemdetector.constants.Constants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Loc;

/**
 * The sensing-and-actuation surface of the robot (CD-ARCH2). Provides the
 * movement operations move, randomWalk, shortRandomWalk, and
 * changeDirection (CD-OP1..4).
 */
public final class Vehicle {

    @RoboChartType("real")
    private double velocity;
    private Angle direction = Angle.Front;
    private boolean walking;

    /**
     * Moves at the given linear velocity in the given body-relative
     * direction; zero velocity facing Front halts the Vehicle (CD-OP1).
     */
    public void move(@RoboChartType("real") double lv, Angle a) {
        this.velocity = lv;
        this.direction = a;
        this.walking = false;
    }

    /**
     * Unbounded random-walk search (CD-OP2).
     */
    public void randomWalk() {
        this.walking = true;
    }

    /**
     * Bounded random-walk recovery manoeuvre (CD-OP3).
     */
    public void shortRandomWalk() {
        this.walking = true;
    }

    /**
     * Steers away from a detected obstacle: left obstacle turns right,
     * right obstacle turns left, front obstacle turns back (CD-OP4).
     */
    public void changeDirection(Loc side) {
        if (side == Loc.left) {
            move(Constants.lv, Angle.Right);
        } else if (side == Loc.right) {
            move(Constants.lv, Angle.Left);
        } else {
            move(Constants.lv, Angle.Back);
        }
    }

    /**
     * Timing primitive: waits for the given duration. Recognised by the
     * model extraction as a RoboChart wait.
     */
    public void pause(@RoboChartType("nat") int duration) {
        // Platform-level wait; no behaviour needed for model extraction.
    }

    public double velocity() {
        return velocity;
    }

    public Angle direction() {
        return direction;
    }

    public boolean walking() {
        return walking;
    }
}
