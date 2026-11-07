package chemical_detector.actuator;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.OutputEvent;

/**
 * Movement commands and signals sent to the Vehicle (CD-OP1..4, CD-Evt7).
 * Records the last command issued so the platform state can be inspected.
 */
public final class VehicleActuator {

    @RoboChartType("real") private double lastVelocity = 0.0;
    private Angle lastDirection = Angle.Front;
    private boolean randomWalking = false;
    private boolean shortRandomWalking = false;
    private boolean flagRaised = false;

    /**
     * Moves at the given velocity in the given body-relative direction until
     * the next motion command (CD-OP1); velocity 0.0 facing Front halts.
     */
    public void move(@RoboChartType("real") double velocity, Angle direction) {
        this.lastVelocity = velocity;
        this.lastDirection = direction;
        this.randomWalking = false;
        this.shortRandomWalking = false;
    }

    /** Starts an unbounded random walk (CD-OP2). */
    public void randomWalk() {
        this.randomWalking = true;
        this.shortRandomWalking = false;
    }

    /** Starts a bounded random walk (CD-OP3). */
    public void shortRandomWalk() {
        this.randomWalking = false;
        this.shortRandomWalking = true;
    }

    /**
     * Steers away from an obstacle on the given side at velocity LV (CD-OP4):
     * left turns Right, right turns Left, front turns Back.
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

    /** Delivers a signal to the Vehicle (CD-Evt7). */
    public void apply(OutputEvent output) {
        if (output instanceof OutputEvent.Flag) {
            this.flagRaised = true;
        }
    }

    /** Velocity of the last move command. */
    @RoboChartType("real")
    public double lastVelocity() {
        return lastVelocity;
    }

    /** Direction of the last move command. */
    public Angle lastDirection() {
        return lastDirection;
    }

    /** True while an unbounded random walk is the last motion command. */
    public boolean randomWalking() {
        return randomWalking;
    }

    /** True while a bounded random walk is the last motion command. */
    public boolean shortRandomWalking() {
        return shortRandomWalking;
    }

    /** True once the flag signal has been delivered. */
    public boolean flagRaised() {
        return flagRaised;
    }
}
