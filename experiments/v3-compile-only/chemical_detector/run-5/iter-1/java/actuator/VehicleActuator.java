package chemical_detector.actuator;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.controller.ChemicalDetectorConstants;
import chemical_detector.event.OutputEvent;
import chemical_detector.types.Angle;
import chemical_detector.types.Loc;

/**
 * The Vehicle's actuation surface: the flag signal and the movement
 * operations (CD-OP1 to CD-OP4). Records the last command of each kind.
 */
public final class VehicleActuator {

    private OutputEvent lastOutput;
    @RoboChartType("real") private double lastVelocity = 0.0;
    private Angle lastDirection = Angle.Front;
    private boolean randomWalking = false;
    private boolean boundedWalk = false;

    /** Sends a signal to the Vehicle (the flag event, CD-Evt7). */
    public void apply(OutputEvent signal) {
        this.lastOutput = signal;
    }

    /**
     * Moves at the given velocity in the given body-relative direction,
     * superseding any earlier motion; velocity 0 halts (CD-OP1).
     */
    public void move(@RoboChartType("real") double velocity, Angle direction) {
        this.lastVelocity = velocity;
        this.lastDirection = direction;
        this.randomWalking = false;
    }

    /** Starts an unbounded random walk (CD-OP2). */
    public void randomWalk() {
        this.randomWalking = true;
        this.boundedWalk = false;
    }

    /** Starts a bounded, terminating random walk (CD-OP3). */
    public void shortRandomWalk() {
        this.randomWalking = true;
        this.boundedWalk = true;
    }

    /**
     * Steers away from an obstacle at the given side, at velocity lv
     * (CD-OP4): left turns right, right turns left, front turns back.
     */
    public void changeDirection(Loc side) {
        if (side == Loc.left) {
            move(ChemicalDetectorConstants.LV, Angle.Right);
        } else if (side == Loc.right) {
            move(ChemicalDetectorConstants.LV, Angle.Left);
        } else {
            move(ChemicalDetectorConstants.LV, Angle.Back);
        }
    }

    /** The last signal sent, or null before the first. */
    public OutputEvent lastOutput() {
        return lastOutput;
    }

    /** The velocity of the last move command. */
    @RoboChartType("real")
    public double lastVelocity() {
        return lastVelocity;
    }

    /** The direction of the last move command. */
    public Angle lastDirection() {
        return lastDirection;
    }

    /** True while the last motion command was a random walk. */
    public boolean randomWalking() {
        return randomWalking;
    }

    /** True if the last random walk started was the bounded (short) one. */
    public boolean boundedWalk() {
        return boundedWalk;
    }
}
