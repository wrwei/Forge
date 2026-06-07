package chemdetector.actuator;

import chemdetector.annotation.RoboChartType;
import chemdetector.constants.ChemConstants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Loc;

/**
 * Actuation surface of the Chemical Detector platform: movement
 * operations and the source-found flag.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double lastVelocity = 0.0;
    private Angle lastDirection = Angle.Front;
    private boolean flagged = false;

    /**
     * Moves at the given linear velocity in the given body-relative
     * direction; the motion continues until superseded. Zero velocity
     * facing Front halts the Vehicle.
     */
    public void move(@RoboChartType("real") double linVel, Angle direction) {
        this.lastVelocity = linVel;
        this.lastDirection = direction;
    }

    /** Unbounded random-walk search. */
    public void randomWalk() {
        // Platform-level behaviour; no controller-visible state change.
    }

    /** Bounded random-walk recovery manoeuvre. */
    public void shortRandomWalk() {
        // Platform-level behaviour; no controller-visible state change.
    }

    /**
     * Steers away from the detected obstacle: left turns right, right
     * turns left, front turns back, each at the linear-velocity constant.
     */
    public void changeDirection(Loc side) {
        if (side == Loc.left) {
            move(ChemConstants.LV, Angle.Right);
        } else if (side == Loc.right) {
            move(ChemConstants.LV, Angle.Left);
        } else if (side == Loc.front) {
            move(ChemConstants.LV, Angle.Back);
        }
    }

    /** Signals that the chemical source has been located. */
    public void flag() {
        this.flagged = true;
    }

    /** Waits for the given duration (time units). */
    public void pause(@RoboChartType("nat") int duration) {
        // Bounded wait; modelled as a RoboChart wait(duration).
    }

    @RoboChartType("real")
    public double lastVelocity() {
        return lastVelocity;
    }

    public Angle lastDirection() {
        return lastDirection;
    }

    public boolean flagged() {
        return flagged;
    }
}
