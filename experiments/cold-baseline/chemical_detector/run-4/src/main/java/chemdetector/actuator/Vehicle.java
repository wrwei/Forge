package chemdetector.actuator;

import chemdetector.annotation.RoboChartType;
import chemdetector.datatype.Angle;
import chemdetector.event.OutputEvent;

/**
 * The Vehicle is the actuation surface of the Chemical Detector. The controllers
 * call into it to issue motion commands (move, randomWalk, shortRandomWalk) and
 * to emit the flag event when the chemical source has been found.
 */
public final class Vehicle {

    private OutputEvent lastEmitted;
    @RoboChartType("real")
    private double currentVelocity;
    private Angle currentAngle = Angle.Front;
    private boolean randomWalking;
    private boolean shortRandomWalking;
    private boolean flagged;

    /** CD-OP1. Move with linear velocity lv in direction a relative to the body. */
    public void move(@RoboChartType("real") double lv, Angle a) {
        this.currentVelocity = lv;
        this.currentAngle = a;
        this.randomWalking = false;
        this.shortRandomWalking = false;
    }

    /** CD-OP2. Unbounded random-walk search. */
    public void randomWalk() {
        this.randomWalking = true;
        this.shortRandomWalking = false;
    }

    /** CD-OP3. Bounded random-walk recovery manoeuvre. */
    public void shortRandomWalk() {
        this.shortRandomWalking = true;
        this.randomWalking = false;
    }

    /** Bounded wait. Recognised by the ETL via the @RoboChartWait annotation. */
    @chemdetector.annotation.RoboChartWait
    public void pause(@RoboChartType("nat") int durationTicks) {
        // No-op: in the executable surface, time is advanced by the runtime.
    }

    /** Emit the flag event to the Vehicle (CD-Evt7). */
    public void emit(OutputEvent event) {
        this.lastEmitted = event;
        if (event instanceof OutputEvent.Flag) {
            this.flagged = true;
        }
    }

    public OutputEvent lastEmitted() { return lastEmitted; }
    @RoboChartType("real")
    public double currentVelocity() { return currentVelocity; }
    public Angle currentAngle() { return currentAngle; }
    public boolean randomWalking() { return randomWalking; }
    public boolean shortRandomWalking() { return shortRandomWalking; }
    public boolean flagged() { return flagged; }
}
