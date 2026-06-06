package chemdetector.actuator;

import chemdetector.annotation.RoboChartType;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Loc;
import chemdetector.event.VehicleEvent;

/**
 * The sensing-and-actuation surface of the robot (CD-ARCH2). Receives
 * movement commands and reports the cumulative travelled distance.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double lastVelocity;
    private Angle lastDirection = Angle.Front;
    private boolean flagged;
    private boolean walking;
    @RoboChartType("real")
    private double travelled;

    /**
     * Moves with linear velocity lv in direction a; move(0, Front)
     * halts the Vehicle (CD-OP1).
     */
    public void move(@RoboChartType("real") double lv, Angle a) {
        this.lastVelocity = lv;
        this.lastDirection = a;
        this.walking = false;
    }

    /** Unbounded random-walk search (CD-OP2). */
    public void randomWalk() {
        this.walking = true;
    }

    /** Bounded recovery random walk (CD-OP3). */
    public void shortRandomWalk() {
        this.walking = true;
    }

    /**
     * Steers away from the detected obstacle: left obstacle turns
     * right, right obstacle turns left, front obstacle turns back
     * (CD-OP4).
     */
    public void changeDirection(VehicleEvent command) {
        if (command instanceof VehicleEvent.ChangeDirection) {
            VehicleEvent.ChangeDirection cd = (VehicleEvent.ChangeDirection) command;
            if (cd.value() == Loc.left) {
                move(this.lastVelocity, Angle.Right);
            } else if (cd.value() == Loc.right) {
                move(this.lastVelocity, Angle.Left);
            } else {
                move(this.lastVelocity, Angle.Back);
            }
        }
    }

    /** Signals that the chemical source has been located (CD-Evt7). */
    public void flag() {
        this.flagged = true;
    }

    /** Cumulative distance travelled (CD-Evt3). */
    @RoboChartType("real")
    public double odometer() {
        return travelled;
    }

    /** Pauses behaviour for the given duration (timing primitive). */
    public void pause(@RoboChartType("nat") int duration) {
    }

    public double lastVelocity() {
        return lastVelocity;
    }

    public Angle lastDirection() {
        return lastDirection;
    }

    public boolean flagged() {
        return flagged;
    }

    public boolean walking() {
        return walking;
    }
}
