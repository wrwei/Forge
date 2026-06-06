package chemdetector.actuator;

import chemdetector.annotation.RoboChartType;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Loc;
import chemdetector.event.OutputEvent;

/**
 * Actuation surface of the Vehicle: movement operations and the output
 * event channel. Records the last issued command for inspection.
 */
public final class Vehicle {

    private Angle lastDirection = Angle.Front;

    @RoboChartType("real")
    private double lastVelocity;

    private OutputEvent lastOutput;

    private boolean walking;

    /** Emits an output event (turn, stop, resume, flag). */
    public void apply(OutputEvent output) {
        this.lastOutput = output;
    }

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

    /** Steers away from an obstacle on side l at the linear velocity. */
    public void changeDirection(Loc side) {
        if (side == Loc.left) {
            this.lastDirection = Angle.Right;
        } else if (side == Loc.right) {
            this.lastDirection = Angle.Left;
        } else {
            this.lastDirection = Angle.Back;
        }
        this.walking = false;
    }

    /** Waits for the given duration (RoboChart wait primitive). */
    public void pause(@RoboChartType("nat") int duration) {
    }

    public OutputEvent lastOutput() {
        return lastOutput;
    }

    public Angle lastDirection() {
        return lastDirection;
    }

    public double lastVelocity() {
        return lastVelocity;
    }

    public boolean walking() {
        return walking;
    }
}
