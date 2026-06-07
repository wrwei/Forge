package chemdetector.actuator;

import chemdetector.annotation.RoboChartType;
import chemdetector.controller.Constants;
import chemdetector.data.Angle;
import chemdetector.data.Loc;

/**
 * Sensing-and-actuation surface of the robot (CD-ARCH2). Provides the
 * movement operations move, randomWalk, shortRandomWalk (CD-OP1..3) and
 * the obstacle-evasion steering operation changeDirection (CD-OP4).
 */
public final class Vehicle {

    @RoboChartType("real")
    private double lastVelocity;
    private Angle lastDirection = Angle.Front;
    private String lastWalk = "NONE";

    /**
     * Moves at the given linear velocity in the given body-relative
     * direction; zero velocity halts the Vehicle (CD-OP1).
     */
    public void move(@RoboChartType("real") double lv, Angle direction) {
        this.lastVelocity = lv;
        this.lastDirection = direction;
    }

    /** Unbounded random-walk search (CD-OP2). */
    public void randomWalk() {
        this.lastWalk = "RANDOM";
    }

    /** Bounded random-walk recovery manoeuvre (CD-OP3). */
    public void shortRandomWalk() {
        this.lastWalk = "SHORT_RANDOM";
    }

    /**
     * Steers away from a detected obstacle at the linear-velocity
     * constant (CD-OP4): obstacle left turns right, right turns left,
     * front turns back.
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

    /** Bounded wait; recognised as a RoboChart wait primitive. */
    public void pause(@RoboChartType("nat") int duration) {
        // Intentionally empty: the formal model interprets this as wait(duration).
    }

    public double lastVelocity() {
        return lastVelocity;
    }

    public Angle lastDirection() {
        return lastDirection;
    }

    public String lastWalk() {
        return lastWalk;
    }
}
