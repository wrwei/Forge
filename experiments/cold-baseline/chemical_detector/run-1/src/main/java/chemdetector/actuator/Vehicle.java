package chemdetector.actuator;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.RoboChartWait;
import chemdetector.datatype.Angle;

/**
 * Vehicle actuator. Receives motion commands and the flag signal.
 *
 * Operations (CD-OP1..3) are exposed as Java methods that the movement
 * controller invokes from entry actions and transition actions.
 */
public final class Vehicle {

    @RoboChartType("real")
    private double lastVelocity;
    private Angle lastAngle = Angle.Front;
    private boolean lastWasRandomWalk;
    private boolean lastWasShortRandomWalk;
    private boolean flagged;

    /** CD-OP1: move at linear velocity lv in direction a. */
    public void move(@RoboChartType("real") double lv, Angle a) {
        this.lastVelocity = lv;
        this.lastAngle = a;
        this.lastWasRandomWalk = false;
        this.lastWasShortRandomWalk = false;
    }

    /** CD-OP2: unbounded random-walk. */
    public void randomWalk() {
        this.lastWasRandomWalk = true;
        this.lastWasShortRandomWalk = false;
    }

    /** CD-OP3: bounded random-walk used during recovery. */
    public void shortRandomWalk() {
        this.lastWasShortRandomWalk = true;
        this.lastWasRandomWalk = false;
    }

    /** Pauses execution; mapped to a RoboChart wait. */
    @RoboChartWait
    public void pause(@RoboChartType("nat") int duration) {
        // Pure model placeholder; in a real platform this would block.
    }

    /** CD-Evt7 flag signal sink: the Vehicle records that the source was located. */
    public void flag() {
        this.flagged = true;
    }

    @RoboChartType("real")
    public double lastVelocity() { return lastVelocity; }
    public Angle lastAngle() { return lastAngle; }
    public boolean lastWasRandomWalk() { return lastWasRandomWalk; }
    public boolean lastWasShortRandomWalk() { return lastWasShortRandomWalk; }
    public boolean flagged() { return flagged; }
}
