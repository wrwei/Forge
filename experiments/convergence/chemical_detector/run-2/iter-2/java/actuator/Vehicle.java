package chemdetector.actuator;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.RoboChartWait;
import chemdetector.constants.ChemConstants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Loc;

/**
 * The Vehicle: the robot's sensing-and-actuation surface (CD-ARCH2). It
 * provides the movement operations (CD-OP1..4) and the cumulative-distance
 * odometer reading (CD-Evt3).
 */
public final class Vehicle {

    @RoboChartType("real")
    private double lastVelocity;
    private Angle lastDirection = Angle.Front;
    @RoboChartType("real")
    private double travelled;

    /**
     * Moves with linear velocity lv in direction a; move(0, Front) halts
     * the Vehicle (CD-OP1).
     */
    public void move(@RoboChartType("real") double lv, Angle a) {
        this.lastVelocity = lv;
        this.lastDirection = a;
    }

    /** Unbounded random-walk search (CD-OP2). */
    public void randomWalk() {
        this.lastVelocity = ChemConstants.LV;
    }

    /** Bounded random-walk recovery manoeuvre (CD-OP3). */
    public void shortRandomWalk() {
        this.lastVelocity = ChemConstants.LV;
    }

    /**
     * Steers away from a detected obstacle: left obstacle turns right,
     * right obstacle turns left, front obstacle turns back (CD-OP4).
     */
    public void changeDirection(Loc l) {
        if (l == Loc.left) {
            move(ChemConstants.LV, Angle.Right);
        } else if (l == Loc.right) {
            move(ChemConstants.LV, Angle.Left);
        } else {
            move(ChemConstants.LV, Angle.Back);
        }
    }

    /** Waits for the given duration; extracted as a RoboChart wait (CD-Const3/6). */
    @RoboChartWait
    public void pause(@RoboChartType("nat") int duration) {
    }

    /** Cumulative distance travelled (CD-Evt3). */
    @RoboChartType("real")
    public double odometer() {
        return travelled;
    }

    /** Records travelled distance; called by the simulation harness. */
    public void advance(@RoboChartType("real") double d) {
        this.travelled = this.travelled + d;
    }

    public double lastVelocity() {
        return lastVelocity;
    }

    public Angle lastDirection() {
        return lastDirection;
    }
}
