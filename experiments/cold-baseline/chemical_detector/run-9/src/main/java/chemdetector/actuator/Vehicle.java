package chemdetector.actuator;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.RoboChartWait;
import chemdetector.datatype.Angle;

/**
 * Vehicle: actuation surface that exposes move / randomWalk / shortRandomWalk
 * to the movement controller and consumes the flag signal when the source has
 * been located.
 *
 * <p>This is a baseline mock — every operation simply records its invocation so
 * the controller behaviour can be observed in tests. CD-OP1..OP3 + CD-Evt7.</p>
 */
public final class Vehicle {

    @RoboChartType("real")
    private double lastLv = 0.0;
    private Angle lastAngle = Angle.Front;
    private String lastOp = "NONE";
    private boolean flagRaised = false;

    /** CD-OP1: move with linear velocity lv in direction a. */
    public void move(@RoboChartType("real") double lv, Angle a) {
        this.lastLv = lv;
        this.lastAngle = a;
        this.lastOp = "move";
    }

    /** CD-OP2: unbounded random-walk search. */
    public void randomWalk() {
        this.lastOp = "randomWalk";
    }

    /** CD-OP3: bounded random-walk recovery manoeuvre. */
    public void shortRandomWalk() {
        this.lastOp = "shortRandomWalk";
    }

    /** CD-Evt7: source confirmed. */
    public void flag() {
        this.flagRaised = true;
    }

    /** Generic bounded pause; recognised as a RoboChart wait. */
    @RoboChartWait
    public void pause(@RoboChartType("nat") int duration) {
        this.lastOp = "pause";
    }

    @RoboChartType("real")
    public double lastLv() {
        return lastLv;
    }

    public Angle lastAngle() {
        return lastAngle;
    }

    public String lastOp() {
        return lastOp;
    }

    public boolean flagRaised() {
        return flagRaised;
    }
}
