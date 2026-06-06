package chemdetector.vehicle;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.RoboChartWait;
import chemdetector.datatype.Angle;

/**
 * Vehicle facade providing motion primitives and the wait operation.
 * The implementations are stubs that record the most recent command;
 * in deployment they would forward to the underlying platform.
 */
public final class Vehicle {

    private double lastVel;
    private Angle lastAngle = Angle.Front;
    private boolean randomWalking;
    private boolean shortRandomWalking;
    private boolean flagged;

    public void move(@RoboChartType("real") double vel, Angle a) {
        this.lastVel = vel;
        this.lastAngle = a;
        this.randomWalking = false;
        this.shortRandomWalking = false;
    }

    public void randomWalk() {
        this.randomWalking = true;
        this.shortRandomWalking = false;
    }

    public void shortRandomWalk() {
        this.shortRandomWalking = true;
        this.randomWalking = false;
    }

    public void flag() {
        this.flagged = true;
    }

    @RoboChartWait
    public void pause(@RoboChartType("nat") int duration) {
        // Logical wait: in deployment, blocks for `duration` time units.
    }

    public double lastVel() { return lastVel; }
    public Angle lastAngle() { return lastAngle; }
    public boolean randomWalking() { return randomWalking; }
    public boolean shortRandomWalking() { return shortRandomWalking; }
    public boolean flagged() { return flagged; }
}
