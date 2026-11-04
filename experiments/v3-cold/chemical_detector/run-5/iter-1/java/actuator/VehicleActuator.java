package chemical_detector.actuator;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.OutputEvent;

/**
 * The Vehicle's actuation surface: the motion operations it provides
 * (CD-OP1..3), the movement subsystem's changeDirection operation built on
 * move (CD-OP4), and the flag signal (CD-Evt7). Records the last command.
 */
public final class VehicleActuator {

    @RoboChartType("real") private double lastVelocity = 0.0;
    private Angle lastDirection = Angle.Front;
    private boolean randomWalkActive = false;
    private boolean shortRandomWalkActive = false;
    private boolean flagRaised = false;

    /** Moves at {@code velocity} towards {@code direction} until superseded; zero velocity halts (CD-OP1). */
    public void move(@RoboChartType("real") double velocity, Angle direction) {
        this.lastVelocity = velocity;
        this.lastDirection = direction;
        this.randomWalkActive = false;
        this.shortRandomWalkActive = false;
    }

    /** Starts an unbounded random walk (CD-OP2). */
    public void randomWalk() {
        this.randomWalkActive = true;
        this.shortRandomWalkActive = false;
    }

    /** Starts a bounded random walk (CD-OP3). */
    public void shortRandomWalk() {
        this.randomWalkActive = false;
        this.shortRandomWalkActive = true;
    }

    /**
     * Steers away from an obstacle at linear velocity (CD-OP4): left turns
     * right, right turns left, front turns back.
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

    /** Receives an output signal from the movement subsystem (CD-Evt7). */
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

    /** Whether the unbounded random walk is the current command. */
    public boolean randomWalkActive() {
        return randomWalkActive;
    }

    /** Whether the bounded random walk is the current command. */
    public boolean shortRandomWalkActive() {
        return shortRandomWalkActive;
    }

    /** Whether the flag signal has been received. */
    public boolean flagRaised() {
        return flagRaised;
    }
}
