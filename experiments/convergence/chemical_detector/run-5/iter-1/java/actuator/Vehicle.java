package chemdetector.actuator;

import chemdetector.annotation.RoboChartType;
import chemdetector.constants.ChemConstants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Loc;

/**
 * Sensing-and-actuation surface of the robot: movement operations
 * provided to the controllers.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double lastVelocity;
    private Angle lastDirection = Angle.Front;
    private boolean walking;

    /** Moves with linear velocity lv in direction a; move(0, Front) halts. */
    public void move(@RoboChartType("real") double lv, Angle a) {
        this.lastVelocity = lv;
        this.lastDirection = a;
        this.walking = false;
    }

    /** Unbounded random-walk search. */
    public void randomWalk() {
        this.walking = true;
    }

    /** Bounded random-walk recovery manoeuvre. */
    public void shortRandomWalk() {
        this.walking = true;
    }

    /** Steers away from the detected obstacle at the linear-velocity constant. */
    public void changeDirection(Loc value) {
        if (value == Loc.left) {
            move(ChemConstants.lv, Angle.Right);
        } else if (value == Loc.right) {
            move(ChemConstants.lv, Angle.Left);
        } else {
            move(ChemConstants.lv, Angle.Back);
        }
    }

    /** Timing primitive: wait for the given duration. */
    public void pause(@RoboChartType("nat") int duration) {
    }

    public double lastVelocity() {
        return lastVelocity;
    }

    public Angle lastDirection() {
        return lastDirection;
    }

    public boolean walking() {
        return walking;
    }
}
