package chemdetector.actuator;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.RoboChartWait;
import chemdetector.constants.DetectorConstants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Loc;
import chemdetector.event.OutputEvent;

/**
 * Sensing-and-actuation surface of the robot (CD-ARCH1): movement
 * operations (CD-OP1..4) and the flag output channel (CD-Evt7).
 */
public final class Vehicle {

    @RoboChartType("real")
    private double lastVelocity;
    private Angle lastDirection = Angle.Front;
    private boolean randomWalking;
    private OutputEvent lastSignal;

    /** Move with linear velocity in the given direction (CD-OP1). */
    public void move(@RoboChartType("real") double velocity, Angle direction) {
        this.lastVelocity = velocity;
        this.lastDirection = direction;
        this.randomWalking = false;
    }

    /** Unbounded random-walk search (CD-OP2). */
    public void randomWalk() {
        this.randomWalking = true;
    }

    /** Bounded random-walk recovery manoeuvre (CD-OP3). */
    public void shortRandomWalk() {
        this.randomWalking = true;
    }

    /** Steer away from a detected obstacle (CD-OP4). */
    public void changeDirection(Loc side) {
        if (side == Loc.left) {
            move(DetectorConstants.LV, Angle.Right);
        } else if (side == Loc.right) {
            move(DetectorConstants.LV, Angle.Left);
        } else if (side == Loc.front) {
            move(DetectorConstants.LV, Angle.Back);
        }
    }

    /** Timing primitive: wait for the given duration. */
    @RoboChartWait
    public void pause(@RoboChartType("nat") int duration) {
    }

    /** Receive an output signal from the movement subsystem (CD-Evt7). */
    public void apply(OutputEvent command) {
        this.lastSignal = command;
    }

    public double lastVelocity() {
        return lastVelocity;
    }

    public Angle lastDirection() {
        return lastDirection;
    }

    public boolean randomWalking() {
        return randomWalking;
    }

    public OutputEvent lastSignal() {
        return lastSignal;
    }
}
